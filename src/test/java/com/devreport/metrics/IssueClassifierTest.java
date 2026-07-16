package com.devreport.metrics;

import com.devreport.domain.Issue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IssueClassifierTest {

    private IssueClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new IssueClassifier();
    }

    @Test
    void shouldClassifyFeatureLabel() {
        Issue issue = createIssue("Add login", "feature");
        assertEquals(IssueCategory.FEATURE, classifier.classify(issue));
    }

    @Test
    void shouldClassifyBugLabel() {
        Issue issue = createIssue("Fix crash", "bug");
        assertEquals(IssueCategory.BUG, classifier.classify(issue));
    }

    @Test
    void shouldClassifyTaskLabel() {
        Issue issue = createIssue("Update docs", "task");
        assertEquals(IssueCategory.TASK, classifier.classify(issue));
    }

    @Test
    void shouldClassifyCaseInsensitive() {
        Issue issue = createIssue("Feature work", "Feature");
        assertEquals(IssueCategory.FEATURE, classifier.classify(issue));

        issue = createIssue("Bug work", "BUG");
        assertEquals(IssueCategory.BUG, classifier.classify(issue));
    }

    @Test
    void shouldPrioritizeFirstMatchingLabel() {
        Issue issue = createIssue("Mixed", "feature", "bug");
        assertEquals(IssueCategory.FEATURE, classifier.classify(issue));
    }

    @Test
    void shouldFallbackToTaskWhenNoLabels() {
        Issue issue = createIssue("No labels");
        assertEquals(IssueCategory.TASK, classifier.classify(issue));
    }

    @Test
    void shouldFallbackToTaskWhenNullLabels() {
        Issue issue = new Issue("1", "Title", "Desc", null, LocalDateTime.now(), "user", null);
        assertEquals(IssueCategory.TASK, classifier.classify(issue));
    }

    @Test
    void shouldFallbackToTaskForUnknownLabels() {
        Issue issue = createIssue("Docs update", "documentation", "help-wanted");
        assertEquals(IssueCategory.TASK, classifier.classify(issue));
    }

    @Test
    void shouldReturnOnlyValidCategories() {
        Issue feature = createIssue("F", "feature");
        Issue bug = createIssue("B", "bug");
        Issue task = createIssue("T", "task");
        Issue unknown = createIssue("U", "unknown");

        assertNotNull(classifier.classify(feature));
        assertNotNull(classifier.classify(bug));
        assertNotNull(classifier.classify(task));
        assertNotNull(classifier.classify(unknown));

        for (Issue issue : Arrays.asList(feature, bug, task, unknown)) {
            IssueCategory cat = classifier.classify(issue);
            assertTrue(cat == IssueCategory.FEATURE || cat == IssueCategory.BUG || cat == IssueCategory.TASK);
        }
    }

    private Issue createIssue(String title, String... labels) {
        List<String> labelList = labels != null ? Arrays.asList(labels) : Collections.emptyList();
        return new Issue("1", title, "Description", labelList, LocalDateTime.now(), "user", null);
    }
}
