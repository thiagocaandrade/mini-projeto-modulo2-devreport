package com.devreport.metrics;

import com.devreport.domain.PRMetrics;
import com.devreport.domain.PullRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PRMetricsServiceTest {

    private PRMetricsService prMetricsService;

    @BeforeEach
    void setUp() {
        prMetricsService = new PRMetricsService();
    }

    @Test
    void shouldReturnZerosForEmptyList() {
        PRMetrics result = prMetricsService.calculate(Collections.emptyList());
        assertEquals(0, result.getTotalMerged());
        assertEquals(0, result.getTotalAdditions());
        assertEquals(0, result.getTotalDeletions());
        assertEquals(0, result.getTotalChangedFiles());
        assertEquals(0.0, result.getAverageTimeToMerge());
        assertEquals(0, result.getUniqueReviewers());
        assertTrue(result.getPrSizeDistribution().containsKey("small"));
        assertEquals(0, result.getPrSizeDistribution().get("small"));
    }

    @Test
    void shouldReturnZerosForNullList() {
        PRMetrics result = prMetricsService.calculate(null);
        assertEquals(0, result.getTotalMerged());
        assertEquals(0, result.getTotalAdditions());
        assertEquals(0, result.getTotalDeletions());
    }

    @Test
    void shouldCalculateTotalMergedCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        List<PullRequest> prs = Arrays.asList(
                createPR(1L, 100, 50, now.minusDays(5), now.minusDays(3),
                        Arrays.asList("reviewer1", "reviewer2")),
                createPR(2L, 200, 100, now.minusDays(10), now.minusDays(8),
                        Arrays.asList("reviewer1")),
                createPR(3L, 30, 10, now.minusDays(2), now.minusDays(1),
                        Collections.emptyList())
        );

        PRMetrics result = prMetricsService.calculate(prs);
        assertEquals(3, result.getTotalMerged());
        assertEquals(330, result.getTotalAdditions());
        assertEquals(160, result.getTotalDeletions());
    }

    @Test
    void shouldCalculateAverageTimeToMergeCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        List<PullRequest> prs = Arrays.asList(
                createPR(1L, 100, 50, now.minusDays(5), now.minusDays(3), null),
                createPR(2L, 200, 100, now.minusDays(10), now.minusDays(5), null)
        );

        PRMetrics result = prMetricsService.calculate(prs);
        // First PR: 2 days * 24 = 48h, Second PR: 5 days * 24 = 120h, avg = 84h
        assertEquals(84.0, result.getAverageTimeToMerge(), 1.0);
    }

    @Test
    void shouldCountUniqueReviewers() {
        LocalDateTime now = LocalDateTime.now();
        List<PullRequest> prs = Arrays.asList(
                createPR(1L, 100, 50, now.minusDays(5), now.minusDays(3),
                        Arrays.asList("alice", "bob")),
                createPR(2L, 200, 100, now.minusDays(10), now.minusDays(8),
                        Arrays.asList("bob", "charlie")),
                createPR(3L, 30, 10, now.minusDays(2), now.minusDays(1),
                        Arrays.asList("alice"))
        );

        PRMetrics result = prMetricsService.calculate(prs);
        assertEquals(3, result.getUniqueReviewers()); // alice, bob, charlie
    }

    @Test
    void shouldHandleNullReviewersGracefully() {
        LocalDateTime now = LocalDateTime.now();
        List<PullRequest> prs = Arrays.asList(
                createPR(1L, 100, 50, now.minusDays(5), now.minusDays(3), null),
                createPR(2L, 200, 100, now.minusDays(10), now.minusDays(8), null)
        );

        PRMetrics result = prMetricsService.calculate(prs);
        assertEquals(2, result.getTotalMerged());
        assertEquals(0, result.getUniqueReviewers());
    }

    @Test
    void shouldClassifyPRSizeDistribution() {
        LocalDateTime now = LocalDateTime.now();
        List<PullRequest> prs = Arrays.asList(
                createPR(1L, 30, 20, now.minusDays(5), now.minusDays(3), null),   // 50 = small
                createPR(2L, 200, 200, now.minusDays(10), now.minusDays(8), null), // 400 = medium
                createPR(3L, 400, 200, now.minusDays(2), now.minusDays(1), null),  // 600 = large
                createPR(4L, 80, 15, now.minusDays(4), now.minusDays(2), null)     // 95 = small
        );

        PRMetrics result = prMetricsService.calculate(prs);
        assertEquals(2, (int) result.getPrSizeDistribution().get("small"));
        assertEquals(1, (int) result.getPrSizeDistribution().get("medium"));
        assertEquals(1, (int) result.getPrSizeDistribution().get("large"));
    }

    @Test
    void shouldHandlePRWithNullDatesForTimeToMerge() {
        LocalDateTime now = LocalDateTime.now();
        List<PullRequest> prs = Arrays.asList(
                createPR(1L, 100, 50, null, now.minusDays(3), null),
                createPR(2L, 200, 100, now.minusDays(10), null, null),
                createPR(3L, 30, 10, now.minusDays(2), now.minusDays(1), null)
        );

        PRMetrics result = prMetricsService.calculate(prs);
        // Only PR #3 has both dates: 1 day = 24h
        assertEquals(24.0, result.getAverageTimeToMerge(), 1.0);
        assertEquals(3, result.getTotalMerged());
    }

    private PullRequest createPR(Long id, int additions, int deletions,
                                  LocalDateTime createdAt, LocalDateTime mergedAt,
                                  List<String> reviewers) {
        return new PullRequest(id, id.intValue(), "PR #" + id,
                createdAt, mergedAt, additions, deletions,
                5, reviewers, 3,
                Collections.emptyList(), "owner/repo");
    }
}
