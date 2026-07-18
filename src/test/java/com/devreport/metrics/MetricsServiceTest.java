package com.devreport.metrics;

import com.devreport.domain.Issue;
import com.devreport.domain.Metric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MetricsServiceTest {

    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        IssueClassifier classifier = new IssueClassifier();
        metricsService = new MetricsService(classifier);
    }

    @Test
    void shouldReturnZerosForEmptyList() {
        Metric result = metricsService.calculate(Collections.emptyList());
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getFeatures());
        assertEquals(0, result.getBugs());
        assertEquals(0, result.getTasks());
    }

    @Test
    void shouldReturnZerosForNullList() {
        Metric result = metricsService.calculate(null);
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getFeatures());
        assertEquals(0, result.getBugs());
        assertEquals(0, result.getTasks());
    }

    @Test
    void shouldCalculateTotalCorrectly() {
        List<Issue> issues = Arrays.asList(
                createIssue("Feature A", "feature"),
                createIssue("Bug fix", "bug"),
                createIssue("Regular task", "task"),
                createIssue("Another feature", "feature", "enhancement")
        );
        Metric result = metricsService.calculate(issues);
        assertEquals(4, result.getTotal());
        assertEquals(2, result.getFeatures());
        assertEquals(1, result.getBugs());
        assertEquals(1, result.getTasks());
    }

    @Test
    void shouldEnsureTotalEqualsSumOfCategories() {
        List<Issue> issues = Arrays.asList(
                createIssue("F1", "feature"),
                createIssue("F2", "feature"),
                createIssue("B1", "bug"),
                createIssue("T1", "task"),
                createIssue("T2", "task"),
                createIssue("T3", "task")
        );
        Metric result = metricsService.calculate(issues);
        assertEquals(result.getFeatures() + result.getBugs() + result.getTasks(), result.getTotal());
    }

    @Test
    void shouldClassifyUnknownLabelsAsTask() {
        List<Issue> issues = List.of(
                createIssue("Unknown label", "documentation")
        );
        Metric result = metricsService.calculate(issues);
        assertEquals(1, result.getTotal());
        assertEquals(0, result.getFeatures());
        assertEquals(0, result.getBugs());
        assertEquals(1, result.getTasks());
    }

    @Test
    void shouldClassifyNoLabelsAsTask() {
        List<Issue> issues = List.of(
                createIssue("No labels")
        );
        Metric result = metricsService.calculate(issues);
        assertEquals(1, result.getTotal());
        assertEquals(0, result.getFeatures());
        assertEquals(0, result.getBugs());
        assertEquals(1, result.getTasks());
    }

    private Issue createIssue(String title, String... labels) {
        return new Issue(
                "1", title, "Description for " + title,
                labels != null ? Arrays.asList(labels) : Collections.emptyList(),
                LocalDateTime.now(), "test-user", null
        );
    }
}
