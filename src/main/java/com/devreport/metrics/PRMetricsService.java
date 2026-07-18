package com.devreport.metrics;

import com.devreport.domain.PRMetrics;
import com.devreport.domain.PullRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class PRMetricsService {

    private static final Logger log = LoggerFactory.getLogger(PRMetricsService.class);

    public PRMetrics calculate(List<PullRequest> pullRequests) {
        if (pullRequests == null || pullRequests.isEmpty()) {
            log.info("No pull requests to calculate metrics from");
            return new PRMetrics(0, 0, 0, 0, 0.0, 0,
                    Map.of("small", 0, "medium", 0, "large", 0));
        }

        int totalMerged = pullRequests.size();
        int totalAdditions = 0;
        int totalDeletions = 0;
        int totalChangedFiles = 0;
        Set<String> allReviewers = new HashSet<>();
        int small = 0, medium = 0, large = 0;

        List<Double> timeToMergeHours = new ArrayList<>();

        for (PullRequest pr : pullRequests) {
            totalAdditions += pr.getAdditions();
            totalDeletions += pr.getDeletions();
            totalChangedFiles += pr.getChangedFiles();

            if (pr.getReviewers() != null) {
                allReviewers.addAll(pr.getReviewers());
            }

            int totalLines = pr.getAdditions() + pr.getDeletions();
            if (totalLines < 100) {
                small++;
            } else if (totalLines <= 500) {
                medium++;
            } else {
                large++;
            }

            if (pr.getCreatedAt() != null && pr.getMergedAt() != null) {
                double hours = ChronoUnit.HOURS.between(pr.getCreatedAt(), pr.getMergedAt());
                if (hours >= 0) {
                    timeToMergeHours.add(hours);
                }
            }
        }

        double averageTimeToMerge = 0.0;
        if (!timeToMergeHours.isEmpty()) {
            averageTimeToMerge = timeToMergeHours.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
        }

        // QW-3: Review coverage percentage
        long prsWithReviewers = pullRequests.stream()
                .filter(pr -> pr.getReviewers() != null && !pr.getReviewers().isEmpty())
                .count();
        double reviewCoveragePercent = totalMerged > 0
                ? (prsWithReviewers * 100.0 / totalMerged)
                : 0.0;

        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("small", small);
        distribution.put("medium", medium);
        distribution.put("large", large);

        log.info("PR metrics calculated: totalMerged={}, additions={}, deletions={}, avgTime={}h, reviewers={}, reviewCoverage={}%",
                totalMerged, totalAdditions, totalDeletions,
                String.format("%.1f", averageTimeToMerge), allReviewers.size(),
                String.format("%.1f", reviewCoveragePercent));

        return new PRMetrics(totalMerged, totalAdditions, totalDeletions,
                totalChangedFiles, averageTimeToMerge, allReviewers.size(), distribution,
                reviewCoveragePercent);
    }
}
