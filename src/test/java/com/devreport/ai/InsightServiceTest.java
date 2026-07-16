package com.devreport.ai;

import com.devreport.domain.Issue;
import com.devreport.domain.Insight;
import com.devreport.domain.Metric;
import com.devreport.metrics.IssueClassifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsightServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec promptSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private InsightService insightService;

    @BeforeEach
    void setUp() {
        IssueClassifier classifier = new IssueClassifier();
        IssueSelector issueSelector = new IssueSelector(classifier);
        insightService = new InsightService(chatClient, issueSelector);
    }

    @Test
    void shouldGenerateInsightSuccessfully() {
        Metric metrics = new Metric(10, 5, 3, 2);
        List<Issue> issues = List.of(
                createIssue("Login feature", "feature"),
                createIssue("Fix payment bug", "bug"),
                createIssue("Update docs", "task")
        );

        when(chatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.user(anyString())).thenReturn(promptSpec);
        when(promptSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Período produtivo com bom equilíbrio entre entregas.");

        Insight result = insightService.generateInsight(
                metrics, issues, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31)
        );

        assertNotNull(result);
        assertEquals("Período produtivo com bom equilíbrio entre entregas.", result.getContent());
    }

    @Test
    void shouldReturnNullWhenMetricsNull() {
        Insight result = insightService.generateInsight(
                null, Collections.emptyList(),
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31)
        );
        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenTotalIsZero() {
        Metric metrics = new Metric(0, 0, 0, 0);

        Insight result = insightService.generateInsight(
                metrics, Collections.emptyList(),
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31)
        );
        assertNull(result);
    }

    @Test
    void shouldReturnNullOnAiFailure() {
        Metric metrics = new Metric(5, 3, 1, 1);
        List<Issue> issues = List.of(createIssue("Test", "task"));

        when(chatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.user(anyString())).thenReturn(promptSpec);
        when(promptSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenThrow(new RuntimeException("AI service unavailable"));

        Insight result = insightService.generateInsight(
                metrics, issues, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31)
        );

        assertNull(result);
    }

    @Test
    void shouldReturnNullOnEmptyResponse() {
        Metric metrics = new Metric(3, 2, 0, 1);
        List<Issue> issues = List.of(createIssue("Test", "feature"));

        when(chatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.user(anyString())).thenReturn(promptSpec);
        when(promptSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("");

        Insight result = insightService.generateInsight(
                metrics, issues, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31)
        );

        assertNull(result);
    }

    @Test
    void shouldHandleEmptyIssuesList() {
        Metric metrics = new Metric(0, 0, 0, 0);
        // total == 0, should return null early
        Insight result = insightService.generateInsight(
                metrics, Collections.emptyList(),
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31)
        );
        assertNull(result);
    }

    private Issue createIssue(String title, String... labels) {
        return new Issue(
                "1", title, "Description for " + title,
                labels != null ? List.of(labels) : Collections.emptyList(),
                LocalDateTime.now(), "user", null
        );
    }
}
