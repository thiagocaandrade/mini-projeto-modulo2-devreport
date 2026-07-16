package com.devreport.infrastructure.github;

import com.devreport.domain.Issue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IssueFilterTest {

    private IssueFilter filter;

    @BeforeEach
    void setUp() {
        filter = new IssueFilter();
    }

    @Test
    void shouldIncludeIssuesWithinPeriod() {
        Issue issue = createIssue("1", LocalDateTime.of(2026, 7, 10, 12, 0));
        List<Issue> result = filter.filterByPeriod(
                List.of(issue),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );
        assertEquals(1, result.size());
    }

    @Test
    void shouldExcludeIssuesBeforePeriod() {
        Issue issue = createIssue("1", LocalDateTime.of(2026, 6, 15, 12, 0));
        List<Issue> result = filter.filterByPeriod(
                List.of(issue),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldExcludeIssuesAfterPeriod() {
        Issue issue = createIssue("1", LocalDateTime.of(2026, 8, 15, 12, 0));
        List<Issue> result = filter.filterByPeriod(
                List.of(issue),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldExcludeIssuesWithoutClosedAt() {
        Issue issue = new Issue("1", "Title", "Desc", Collections.emptyList(), null, "user", null);
        List<Issue> result = filter.filterByPeriod(
                List.of(issue),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIncludeIssueAtStartBoundary() {
        Issue issue = createIssue("1", LocalDateTime.of(2026, 7, 1, 0, 0));
        List<Issue> result = filter.filterByPeriod(
                List.of(issue),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );
        assertEquals(1, result.size());
    }

    @Test
    void shouldIncludeIssueAtEndBoundary() {
        Issue issue = createIssue("1", LocalDateTime.of(2026, 7, 31, 23, 59));
        List<Issue> result = filter.filterByPeriod(
                List.of(issue),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );
        assertEquals(1, result.size());
    }

    @Test
    void shouldHandleNullIssueList() {
        List<Issue> result = filter.filterByPeriod(
                null,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleEmptyIssueList() {
        List<Issue> result = filter.filterByPeriod(
                Collections.emptyList(),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleNullDates() {
        Issue issue = createIssue("1", LocalDateTime.now());
        List<Issue> result = filter.filterByPeriod(List.of(issue), null, null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private Issue createIssue(String id, LocalDateTime closedAt) {
        return new Issue(id, "Title " + id, "Description",
                Collections.emptyList(), closedAt, "user", null);
    }
}
