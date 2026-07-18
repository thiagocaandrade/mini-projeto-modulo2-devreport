package com.devreport.infrastructure.github;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.devreport.agent.*;
import com.devreport.config.GitHubProperties;
import com.devreport.domain.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiRepoResilienceTest {

    @Mock
    private GitHubClient gitHubClient;
    @Mock
    private GitHubIssueMapper issueMapper;
    @Mock
    private GitHubPRMapper prMapper;

    private GitHubIssueService issueService;
    private GitHubPRService prService;
    private FetchGitHubDataNode fetchNode;
    private GitHubProperties properties;

    // Log capture infrastructure for TASK-056 verification
    private ListAppender<ILoggingEvent> issueServiceLogAppender;
    private ListAppender<ILoggingEvent> prServiceLogAppender;
    private Logger issueServiceLogger;
    private Logger prServiceLogger;

    @BeforeEach
    void setUp() {
        properties = new GitHubProperties();
        properties.setOwner("testowner");
        properties.setRepository("default-repo");
        properties.setToken("test-token");

        issueService = new GitHubIssueService(gitHubClient, properties, false);
        prService = new GitHubPRService(gitHubClient, properties, false);
        IssueFilter issueFilter = new IssueFilter();
        fetchNode = new FetchGitHubDataNode(issueService, issueMapper, issueFilter, prService, prMapper);

        // Set up log capture for GitHubIssueService
        issueServiceLogger = (Logger) LoggerFactory.getLogger(GitHubIssueService.class);
        issueServiceLogAppender = new ListAppender<>();
        issueServiceLogAppender.start();
        issueServiceLogger.addAppender(issueServiceLogAppender);

        // Set up log capture for GitHubPRService
        prServiceLogger = (Logger) LoggerFactory.getLogger(GitHubPRService.class);
        prServiceLogAppender = new ListAppender<>();
        prServiceLogAppender.start();
        prServiceLogger.addAppender(prServiceLogAppender);
    }

    @AfterEach
    void tearDown() {
        if (issueServiceLogger != null && issueServiceLogAppender != null) {
            issueServiceLogger.detachAppender(issueServiceLogAppender);
            issueServiceLogAppender.stop();
        }
        if (prServiceLogger != null && prServiceLogAppender != null) {
            prServiceLogger.detachAppender(prServiceLogAppender);
            prServiceLogAppender.stop();
        }
    }

    @Test
    void shouldHandleFailureInOneRepoWhileOthersSucceed() {
        // Setup: 3 repos, repo B fails
        AnalysisState state = new AnalysisState();
        state.setStartDate(LocalDate.now().minusDays(7));
        state.setEndDate(LocalDate.now());
        state.setRepositories(List.of("owner/repoA", "owner/repoB", "owner/repoC"));

        // Repo A succeeds
        when(gitHubClient.fetchIssuesForRepo(eq("owner"), eq("repoA"), anyString(), anyInt(), anyInt()))
                .thenReturn("[{\"id\":\"1\",\"title\":\"Issue A\",\"closed_at\":\"2026-07-10T00:00:00Z\",\"repository\":\"owner/repoA\"}]");
        // Repo B fails
        when(gitHubClient.fetchIssuesForRepo(eq("owner"), eq("repoB"), anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Repo B unavailable"));
        // Repo C succeeds
        when(gitHubClient.fetchIssuesForRepo(eq("owner"), eq("repoC"), anyString(), anyInt(), anyInt()))
                .thenReturn("[{\"id\":\"2\",\"title\":\"Issue C\",\"closed_at\":\"2026-07-11T00:00:00Z\",\"repository\":\"owner/repoC\"}]");

        // PRs - repo A and C succeed, repo B fails
        when(gitHubClient.fetchPullRequests(eq("owner"), eq("repoA"), anyInt(), anyInt()))
                .thenReturn("[]");
        when(gitHubClient.fetchPullRequests(eq("owner"), eq("repoB"), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Repo B unavailable"));
        when(gitHubClient.fetchPullRequests(eq("owner"), eq("repoC"), anyInt(), anyInt()))
                .thenReturn("[]");

        // Mappers
        Issue issueA = new Issue("1", "Issue A", null, List.of("feature"),
                LocalDateTime.now().minusDays(4), "user", null, "owner/repoA");
        Issue issueC = new Issue("2", "Issue C", null, List.of("bug"),
                LocalDateTime.now().minusDays(3), "user", null, "owner/repoC");
        when(issueMapper.mapToIssues(anyString())).thenReturn(List.of(issueA, issueC));
        when(prMapper.mapToPullRequests(anyString())).thenReturn(Collections.emptyList());

        // Execute
        fetchNode.execute(state);

        // Verify: issues from repos A and C were collected
        assertNotNull(state.getIssues());
        assertFalse(state.getIssues().isEmpty());

        // TASK-056: Log registra falha do repositório específico
        List<ILoggingEvent> issueLogs = issueServiceLogAppender.list;
        boolean repoBFailureLogged = issueLogs.stream()
                .anyMatch(event -> event.getLevel() == Level.ERROR
                        && event.getFormattedMessage().contains("Failed to fetch issues for repo")
                        && event.getFormattedMessage().contains("repoB"));
        assertTrue(repoBFailureLogged,
                "Expected ERROR log for repoB issue fetch failure, but none found. Logs: " + issueLogs);

        List<ILoggingEvent> prLogs = prServiceLogAppender.list;
        boolean prRepoBFailureLogged = prLogs.stream()
                .anyMatch(event -> event.getLevel() == Level.ERROR
                        && event.getFormattedMessage().contains("Failed to fetch PRs for repo")
                        && event.getFormattedMessage().contains("repoB"));
        assertTrue(prRepoBFailureLogged,
                "Expected ERROR log for repoB PR fetch failure, but none found. Logs: " + prLogs);

        // TASK-056: Known gap — partial failure does not set a dashboard message.
        // The current code only logs per-repo errors silently. The state errors may be empty.
        // This documents the gap; a future enhancement would propagate partial failure info.
        assertNotNull(state.getIssues());
    }

    @Test
    void shouldFallbackToDefaultRepoWhenListIsEmpty() {
        AnalysisState state = new AnalysisState();
        state.setStartDate(LocalDate.now().minusDays(7));
        state.setEndDate(LocalDate.now());
        state.setRepositories(Collections.emptyList());

        when(gitHubClient.fetchIssuesForRepo(eq("testowner"), eq("default-repo"), anyString(), anyInt(), anyInt()))
                .thenReturn("[{\"id\":\"1\",\"title\":\"Default Issue\",\"closed_at\":\"2026-07-10T00:00:00Z\",\"repository\":\"testowner/default-repo\"}]");
        when(gitHubClient.fetchPullRequests(eq("testowner"), eq("default-repo"), anyInt(), anyInt()))
                .thenReturn("[]");

        Issue defaultIssue = new Issue("1", "Default Issue", null, List.of("task"),
                LocalDateTime.now().minusDays(4), "user", null, "testowner/default-repo");
        when(issueMapper.mapToIssues(anyString())).thenReturn(List.of(defaultIssue));
        when(prMapper.mapToPullRequests(anyString())).thenReturn(Collections.emptyList());

        fetchNode.execute(state);

        assertNotNull(state.getIssues());
        assertEquals(1, state.getIssues().size());
        assertEquals("testowner/default-repo", state.getIssues().get(0).getRepository());
    }

    @Test
    void shouldHandleAllReposFailingGracefully() {
        AnalysisState state = new AnalysisState();
        state.setStartDate(LocalDate.now().minusDays(7));
        state.setEndDate(LocalDate.now());
        state.setRepositories(List.of("owner/repoA", "owner/repoB"));

        when(gitHubClient.fetchIssuesForRepo(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("All repos down"));
        when(gitHubClient.fetchPullRequests(anyString(), anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("All repos down"));
        when(issueMapper.mapToIssues(anyString())).thenReturn(Collections.emptyList());
        when(prMapper.mapToPullRequests(anyString())).thenReturn(Collections.emptyList());

        fetchNode.execute(state);

        // Should have an error message
        assertNotNull(state.getMessage());

        // TASK-056: Verify error logs for each failing repo
        List<ILoggingEvent> issueLogs = issueServiceLogAppender.list;
        long issueErrorCount = issueLogs.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .filter(event -> event.getFormattedMessage().contains("Failed to fetch issues for repo"))
                .count();
        assertTrue(issueErrorCount >= 2,
                "Expected at least 2 issue fetch error logs (one per repo), got: " + issueErrorCount);

        List<ILoggingEvent> prLogs = prServiceLogAppender.list;
        long prErrorCount = prLogs.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .filter(event -> event.getFormattedMessage().contains("Failed to fetch PRs for repo"))
                .count();
        assertTrue(prErrorCount >= 2,
                "Expected at least 2 PR fetch error logs (one per repo), got: " + prErrorCount);
    }

    @Test
    void shouldHandlePRFailureInOneRepoWhileIssuesAndOtherPRsSucceed() {
        // TASK-056: Test that PR failure in one repo doesn't block issue collection
        // or PR collection from other repos
        AnalysisState state = new AnalysisState();
        state.setStartDate(LocalDate.now().minusDays(7));
        state.setEndDate(LocalDate.now());
        state.setRepositories(List.of("owner/repoA", "owner/repoB"));

        // Issues succeed for both repos
        when(gitHubClient.fetchIssuesForRepo(eq("owner"), eq("repoA"), anyString(), anyInt(), anyInt()))
                .thenReturn("[{\"id\":\"1\",\"title\":\"Issue A1\",\"closed_at\":\"2026-07-10T00:00:00Z\",\"repository\":\"owner/repoA\"}]");
        when(gitHubClient.fetchIssuesForRepo(eq("owner"), eq("repoB"), anyString(), anyInt(), anyInt()))
                .thenReturn("[{\"id\":\"2\",\"title\":\"Issue B1\",\"closed_at\":\"2026-07-11T00:00:00Z\",\"repository\":\"owner/repoB\"}]");

        // PRs: repo A succeeds, repo B fails
        when(gitHubClient.fetchPullRequests(eq("owner"), eq("repoA"), anyInt(), anyInt()))
                .thenReturn("[{\"id\":101,\"number\":1,\"title\":\"PR A\",\"merged_at\":\"2026-07-10T00:00:00Z\",\"additions\":50,\"deletions\":20,\"changed_files\":3,\"requested_reviewers\":[],\"repository\":\"owner/repoA\"}]");
        when(gitHubClient.fetchPullRequests(eq("owner"), eq("repoB"), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Repo B PRs unavailable"));

        // Mappers
        Issue issueA1 = new Issue("1", "Issue A1", null, List.of("feature"),
                LocalDateTime.now().minusDays(4), "user", null, "owner/repoA");
        Issue issueB1 = new Issue("2", "Issue B1", null, List.of("bug"),
                LocalDateTime.now().minusDays(3), "user", null, "owner/repoB");
        when(issueMapper.mapToIssues(anyString())).thenReturn(List.of(issueA1, issueB1));

        PullRequest prA = new PullRequest(101L, 1, "PR A",
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(3),
                50, 20, 3, Collections.emptyList(), 0,
                Collections.emptyList(), "owner/repoA");
        when(prMapper.mapToPullRequests(anyString())).thenReturn(List.of(prA));

        // Execute
        fetchNode.execute(state);

        // Issues from both repos collected
        assertNotNull(state.getIssues());
        assertEquals(2, state.getIssues().size());

        // PRs: only repo A's PR collected, repo B's failure didn't block
        assertNotNull(state.getPullRequests());
        assertEquals(1, state.getPullRequests().size());
        assertEquals("owner/repoA", state.getPullRequests().get(0).getRepository());

        // TASK-056: Verify PR fetch failure was logged
        List<ILoggingEvent> prLogs = prServiceLogAppender.list;
        boolean prRepoBFailureLogged = prLogs.stream()
                .anyMatch(event -> event.getLevel() == Level.ERROR
                        && event.getFormattedMessage().contains("Failed to fetch PRs for repo")
                        && event.getFormattedMessage().contains("repoB"));
        assertTrue(prRepoBFailureLogged,
                "Expected ERROR log for repoB PR fetch failure, but none found. Logs: " + prLogs);

        // Verify no issue logs have errors (issues succeeded)
        List<ILoggingEvent> issueLogs = issueServiceLogAppender.list;
        boolean issueErrorFound = issueLogs.stream()
                .anyMatch(event -> event.getLevel() == Level.ERROR);
        assertFalse(issueErrorFound,
                "Expected no ERROR logs for issues, but found: " + issueLogs);
    }
}
