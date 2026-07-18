package com.devreport.domain;

import java.util.Collections;
import java.util.Map;

public class PRMetrics {

    private final int totalMerged;
    private final int totalAdditions;
    private final int totalDeletions;
    private final int totalChangedFiles;
    private final double averageTimeToMerge;
    private final int uniqueReviewers;
    private final Map<String, Integer> prSizeDistribution;
    private final double reviewCoveragePercent;

    public PRMetrics(int totalMerged, int totalAdditions, int totalDeletions,
                     int totalChangedFiles, double averageTimeToMerge,
                     int uniqueReviewers, Map<String, Integer> prSizeDistribution) {
        this(totalMerged, totalAdditions, totalDeletions, totalChangedFiles,
                averageTimeToMerge, uniqueReviewers, prSizeDistribution, 0.0);
    }

    public PRMetrics(int totalMerged, int totalAdditions, int totalDeletions,
                     int totalChangedFiles, double averageTimeToMerge,
                     int uniqueReviewers, Map<String, Integer> prSizeDistribution,
                     double reviewCoveragePercent) {
        this.totalMerged = totalMerged;
        this.totalAdditions = totalAdditions;
        this.totalDeletions = totalDeletions;
        this.totalChangedFiles = totalChangedFiles;
        this.averageTimeToMerge = averageTimeToMerge;
        this.uniqueReviewers = uniqueReviewers;
        this.prSizeDistribution = prSizeDistribution != null
                ? Collections.unmodifiableMap(prSizeDistribution)
                : Collections.emptyMap();
        this.reviewCoveragePercent = reviewCoveragePercent;
    }

    public int getTotalMerged() { return totalMerged; }
    public int getTotalAdditions() { return totalAdditions; }
    public int getTotalDeletions() { return totalDeletions; }
    public int getTotalChangedFiles() { return totalChangedFiles; }
    public double getAverageTimeToMerge() { return averageTimeToMerge; }
    public int getUniqueReviewers() { return uniqueReviewers; }
    public Map<String, Integer> getPrSizeDistribution() { return prSizeDistribution; }
    public double getReviewCoveragePercent() { return reviewCoveragePercent; }
}
