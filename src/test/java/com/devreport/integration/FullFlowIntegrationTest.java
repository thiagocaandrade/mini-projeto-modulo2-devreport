package com.devreport.integration;

import com.devreport.agent.DevReportAgent;
import com.devreport.dashboard.PdfService;
import com.devreport.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "devreport.mock-mode=false")
class FullFlowIntegrationTest {

    @Autowired
    private DevReportAgent agent;

    @Autowired
    private PdfService pdfService;

    @MockitoBean
    private com.devreport.infrastructure.github.GitHubClient gitHubClient;

    @MockitoBean
    private ChatClient chatClient;

    // JSON fixtures: raw GitHub API format (without 'repository' — service injects it)
    private static final String ISSUES_JSON = "[{\"id\":\"1001\",\"title\":\"Add login feature\"," +
            "\"labels\":[{\"name\":\"feature\"}]," +
            "\"closed_at\":\"2026-07-10T00:00:00Z\"," +
            "\"user\":{\"login\":\"alice\"}}," +
            "{\"id\":\"1002\",\"title\":\"Fix payment bug\"," +
            "\"labels\":[{\"name\":\"bug\"}]," +
            "\"closed_at\":\"2026-07-12T00:00:00Z\"," +
            "\"user\":{\"login\":\"bob\"}}]";

    private static final String PRS_JSON = "[{\"id\":3001,\"number\":101,\"title\":\"Login feature PR\"," +
            "\"created_at\":\"2026-07-05T00:00:00Z\"," +
            "\"merged_at\":\"2026-07-09T00:00:00Z\"," +
            "\"additions\":150,\"deletions\":30,\"changed_files\":12," +
            "\"requested_reviewers\":[{\"login\":\"alice\"},{\"login\":\"bob\"}]," +
            "\"review_comments\":8," +
            "\"labels\":[{\"name\":\"feature\"}]}," +
            "{\"id\":3002,\"number\":102,\"title\":\"Fix payment\"," +
            "\"created_at\":\"2026-07-06T00:00:00Z\"," +
            "\"merged_at\":\"2026-07-11T00:00:00Z\"," +
            "\"additions\":45,\"deletions\":15,\"changed_files\":3," +
            "\"requested_reviewers\":[{\"login\":\"alice\"}]," +
            "\"review_comments\":2," +
            "\"labels\":[{\"name\":\"bugfix\"}]}]";

    @BeforeEach
    void setUp() {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("Período produtivo com entregas relevantes.");
    }

    // === TASK-058: Full integration tests ===

    @Test
    void shouldGenerateFullDashboardReportWithIssuesAndPRs() {
        // TASK-058: Issues + PRs → consolidated dashboard with all metrics
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 7, 15);

        // GENERAL stubs first (won't override specific ones below)
        when(gitHubClient.fetchIssuesForRepo(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn("[]");
        when(gitHubClient.fetchPullRequests(any(), any(), anyInt(), anyInt()))
                .thenReturn("[]");

        // SPECIFIC stubs last (Mockito uses the most recently defined matching stub)
        when(gitHubClient.fetchIssuesForRepo(eq("owner"), eq("repoA"), any(), eq(1), anyInt()))
                .thenReturn(ISSUES_JSON);
        when(gitHubClient.fetchPullRequests(eq("owner"), eq("repoA"), eq(1), anyInt()))
                .thenReturn(PRS_JSON);

        AnalysisRequest request = new AnalysisRequest(start, end, List.of("owner/repoA"));
        DashboardReport report = agent.analyze(request);

        // Dashboard consolidated
        assertNotNull(report);
        assertNotNull(report.getMetrics());
        assertEquals(2, report.getMetrics().getTotal(), "Should have 2 issues");
        assertTrue(report.getMetrics().getFeatures() >= 1);
        assertTrue(report.getMetrics().getBugs() >= 1);

        // PR metrics consistency
        assertNotNull(report.getPrMetrics());
        assertEquals(2, report.getPrMetrics().getTotalMerged());
        assertEquals(195, report.getPrMetrics().getTotalAdditions());
        assertEquals(45, report.getPrMetrics().getTotalDeletions());
        assertEquals(2, report.getPrMetrics().getUniqueReviewers());
        assertTrue(report.getPrMetrics().getAverageTimeToMerge() > 0);

        // RepositorySummary
        assertNotNull(report.getRepositorySummaries());
        assertEquals(1, report.getRepositorySummaries().size());
        assertEquals("owner/repoA", report.getRepositorySummaries().get(0).getName());
        assertEquals(2, report.getRepositorySummaries().get(0).getTotalIssues());
        assertEquals(2, report.getRepositorySummaries().get(0).getTotalPRs());
        assertEquals(1, report.getRepositoriesCount());

        // Charts
        assertNotNull(report.getPeriodChart());
        assertNotNull(report.getCategoryChart());
        assertNotNull(report.getPrSizeChart());
    }

    @Test
    void shouldHandlePartialRepoFailureAtIntegrationLevel() {
        // TASK-058: One repo fails → other repos still analyzed
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 7, 15);

        // GENERAL stubs first
        when(gitHubClient.fetchIssuesForRepo(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn("[]");
        when(gitHubClient.fetchPullRequests(any(), any(), anyInt(), anyInt()))
                .thenReturn("[]");

        // SPECIFIC: Repo A succeeds
        when(gitHubClient.fetchIssuesForRepo(eq("owner"), eq("repoA"), any(), eq(1), anyInt()))
                .thenReturn(ISSUES_JSON);
        // SPECIFIC: Repo B throws
        when(gitHubClient.fetchIssuesForRepo(eq("owner"), eq("repoB"), any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Repo B unavailable"));

        AnalysisRequest request = new AnalysisRequest(start, end,
                List.of("owner/repoA", "owner/repoB"));
        DashboardReport report = agent.analyze(request);

        // Report generated despite repoB failure
        assertNotNull(report);
        assertNotNull(report.getMetrics());
        assertEquals(2, report.getMetrics().getTotal(), "Should have issues from repoA only");

        // Repository summary for repoA exists
        assertNotNull(report.getRepositorySummaries());
        assertTrue(report.getRepositorySummaries().stream()
                .anyMatch(rs -> rs.getName().equals("owner/repoA")),
                "repoA summary should exist");
    }

    @Test
    void shouldHandleEmptyRepositoryList() {
        AnalysisRequest request = new AnalysisRequest(
                LocalDate.now().minusDays(7), LocalDate.now(), Collections.emptyList());

        when(gitHubClient.fetchIssuesForRepo(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn("[]");
        when(gitHubClient.fetchPullRequests(any(), any(), anyInt(), anyInt()))
                .thenReturn("[]");

        DashboardReport report = agent.analyze(request);
        assertNotNull(report);
        assertNotNull(report.getMetrics());
    }

    @Test
    void shouldHandleNoPRsGracefully() {
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 7, 15);

        // GENERAL stubs first
        when(gitHubClient.fetchIssuesForRepo(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn("[]");
        when(gitHubClient.fetchPullRequests(any(), any(), anyInt(), anyInt()))
                .thenReturn("[]");

        // SPECIFIC
        when(gitHubClient.fetchIssuesForRepo(eq("owner"), eq("repoA"), any(), eq(1), anyInt()))
                .thenReturn(ISSUES_JSON);

        AnalysisRequest request = new AnalysisRequest(start, end, List.of("owner/repoA"));
        DashboardReport report = agent.analyze(request);

        assertNotNull(report);
        assertEquals(2, report.getMetrics().getTotal());
        assertNotNull(report.getPrMetrics());
        assertEquals(0, report.getPrMetrics().getTotalMerged(),
                "PR metrics should be zeroed when no PRs");
    }

    @Test
    void shouldGeneratePdfWithoutError() {
        // TASK-058: PDF é gerado sem erro
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 7, 15);

        // GENERAL stubs first
        when(gitHubClient.fetchIssuesForRepo(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn("[]");
        when(gitHubClient.fetchPullRequests(any(), any(), anyInt(), anyInt()))
                .thenReturn("[]");

        // SPECIFIC
        when(gitHubClient.fetchIssuesForRepo(eq("owner"), eq("repoA"), any(), eq(1), anyInt()))
                .thenReturn(ISSUES_JSON);
        when(gitHubClient.fetchPullRequests(eq("owner"), eq("repoA"), eq(1), anyInt()))
                .thenReturn(PRS_JSON);

        AnalysisRequest request = new AnalysisRequest(start, end, List.of("owner/repoA"));
        DashboardReport report = agent.analyze(request);

        byte[] pdfBytes = pdfService.generatePdf(report, request);

        assertNotNull(pdfBytes, "PDF bytes should not be null");
        assertTrue(pdfBytes.length > 0, "PDF should have content");
        // PDF magic bytes
        assertEquals('%', pdfBytes[0]);
        assertEquals('P', pdfBytes[1]);
        assertEquals('D', pdfBytes[2]);
        assertEquals('F', pdfBytes[3]);
    }
}
