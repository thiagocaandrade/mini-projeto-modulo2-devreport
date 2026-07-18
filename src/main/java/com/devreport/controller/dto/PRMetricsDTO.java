package com.devreport.controller.dto;

import java.util.Map;

public class PRMetricsDTO {

    private int totalMerged;
    private int totalAdditions;
    private int totalDeletions;
    private int totalChangedFiles;
    private double averageTimeToMerge;
    private int uniqueReviewers;
    private Map<String, Integer> prSizeDistribution;
    private double reviewCoveragePercent;

    public PRMetricsDTO() {
    }

    public int getTotalMerged() { return totalMerged; }
    public void setTotalMerged(int totalMerged) { this.totalMerged = totalMerged; }

    public int getTotalAdditions() { return totalAdditions; }
    public void setTotalAdditions(int totalAdditions) { this.totalAdditions = totalAdditions; }

    public int getTotalDeletions() { return totalDeletions; }
    public void setTotalDeletions(int totalDeletions) { this.totalDeletions = totalDeletions; }

    public int getTotalChangedFiles() { return totalChangedFiles; }
    public void setTotalChangedFiles(int totalChangedFiles) { this.totalChangedFiles = totalChangedFiles; }

    public double getAverageTimeToMerge() { return averageTimeToMerge; }
    public void setAverageTimeToMerge(double averageTimeToMerge) { this.averageTimeToMerge = averageTimeToMerge; }

    public int getUniqueReviewers() { return uniqueReviewers; }
    public void setUniqueReviewers(int uniqueReviewers) { this.uniqueReviewers = uniqueReviewers; }

    public Map<String, Integer> getPrSizeDistribution() { return prSizeDistribution; }
    public void setPrSizeDistribution(Map<String, Integer> prSizeDistribution) { this.prSizeDistribution = prSizeDistribution; }

    public double getReviewCoveragePercent() { return reviewCoveragePercent; }
    public void setReviewCoveragePercent(double reviewCoveragePercent) { this.reviewCoveragePercent = reviewCoveragePercent; }
}
