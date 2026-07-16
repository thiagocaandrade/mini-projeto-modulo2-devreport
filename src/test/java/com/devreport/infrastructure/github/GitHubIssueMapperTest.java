package com.devreport.infrastructure.github;

import com.devreport.domain.Issue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GitHubIssueMapperTest {

    private GitHubIssueMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GitHubIssueMapper(new ObjectMapper());
    }

    @Test
    void shouldMapCompleteIssue() {
        String json = """
            [
                {
                    "id": 12345,
                    "title": "Add login feature",
                    "body": "Implement OAuth2 login with GitHub",
                    "labels": [{"name": "feature"}, {"name": "backend"}],
                    "closed_at": "2026-07-10T15:30:00Z",
                    "user": {"login": "developer1"}
                }
            ]
            """;

        List<Issue> issues = mapper.mapToIssues(json);
        assertEquals(1, issues.size());

        Issue issue = issues.get(0);
        assertEquals("12345", issue.getId());
        assertEquals("Add login feature", issue.getTitle());
        assertEquals("Implement OAuth2 login with GitHub", issue.getDescription());
        assertNotNull(issue.getClosedAt());
        assertEquals("developer1", issue.getAuthor());
    }

    @Test
    void shouldHandleNullInput() {
        List<Issue> issues = mapper.mapToIssues(null);
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    void shouldHandleEmptyString() {
        List<Issue> issues = mapper.mapToIssues("");
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    void shouldHandleBlankString() {
        List<Issue> issues = mapper.mapToIssues("   ");
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    void shouldHandleEmptyArray() {
        List<Issue> issues = mapper.mapToIssues("[]");
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    void shouldHandleInvalidJson() {
        List<Issue> issues = mapper.mapToIssues("{invalid json}");
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    void shouldHandleMissingOptionalFields() {
        String json = """
            [
                {
                    "id": 1,
                    "title": "Minimal issue",
                    "closed_at": "2026-07-10T15:30:00Z"
                }
            ]
            """;

        List<Issue> issues = mapper.mapToIssues(json);
        assertEquals(1, issues.size());
        Issue issue = issues.get(0);
        assertEquals("1", issue.getId());
        assertEquals("Minimal issue", issue.getTitle());
        assertNull(issue.getDescription());
        assertTrue(issue.getLabels().isEmpty());
    }

    @Test
    void shouldPreserveLabels() {
        String json = """
            [
                {
                    "id": 1,
                    "title": "Test",
                    "labels": [{"name": "bug"}, {"name": "critical"}],
                    "closed_at": "2026-07-10T15:30:00Z"
                }
            ]
            """;

        List<Issue> issues = mapper.mapToIssues(json);
        assertEquals(1, issues.size());
        Issue issue = issues.get(0);
        assertNotNull(issue.getLabels());
        assertEquals(2, issue.getLabels().size());
        assertTrue(issue.getLabels().contains("bug"));
        assertTrue(issue.getLabels().contains("critical"));
    }

    @Test
    void shouldMapMultipleIssues() {
        String json = """
            [
                {"id": 1, "title": "Issue 1", "closed_at": "2026-07-01T10:00:00Z"},
                {"id": 2, "title": "Issue 2", "closed_at": "2026-07-02T10:00:00Z"},
                {"id": 3, "title": "Issue 3", "closed_at": "2026-07-03T10:00:00Z"}
            ]
            """;

        List<Issue> issues = mapper.mapToIssues(json);
        assertEquals(3, issues.size());
    }
}
