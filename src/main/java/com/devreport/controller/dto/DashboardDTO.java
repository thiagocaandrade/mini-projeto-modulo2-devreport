package com.devreport.controller.dto;

import java.util.Collections;
import java.util.List;

public class DashboardDTO {

    private MetricDTO metrics;
    private List<String> periodLabels = Collections.emptyList();
    private List<Integer> periodValues = Collections.emptyList();
    private List<String> categoryLabels = Collections.emptyList();
    private List<Integer> categoryValues = Collections.emptyList();
    private String summary;
    private String message;
    private PRMetricsDTO prMetrics;
    private List<RepositorySummaryDTO> repositorySummaries;
    private int repositoriesCount;
    private List<String> prSizeLabels = Collections.emptyList();
    private List<Integer> prSizeValues = Collections.emptyList();
    private List<String> repositoryNames = Collections.emptyList();

    public DashboardDTO() {
    }

    public MetricDTO getMetrics() {
        return metrics;
    }

    public void setMetrics(MetricDTO metrics) {
        this.metrics = metrics;
    }

    public List<String> getPeriodLabels() {
        return periodLabels;
    }

    public void setPeriodLabels(List<String> periodLabels) {
        this.periodLabels = periodLabels != null ? periodLabels : Collections.emptyList();
    }

    public List<Integer> getPeriodValues() {
        return periodValues;
    }

    public void setPeriodValues(List<Integer> periodValues) {
        this.periodValues = periodValues != null ? periodValues : Collections.emptyList();
    }

    public List<String> getCategoryLabels() {
        return categoryLabels;
    }

    public void setCategoryLabels(List<String> categoryLabels) {
        this.categoryLabels = categoryLabels != null ? categoryLabels : Collections.emptyList();
    }

    public List<Integer> getCategoryValues() {
        return categoryValues;
    }

    public void setCategoryValues(List<Integer> categoryValues) {
        this.categoryValues = categoryValues != null ? categoryValues : Collections.emptyList();
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PRMetricsDTO getPrMetrics() { return prMetrics; }
    public void setPrMetrics(PRMetricsDTO prMetrics) { this.prMetrics = prMetrics; }

    public List<RepositorySummaryDTO> getRepositorySummaries() { return repositorySummaries; }
    public void setRepositorySummaries(List<RepositorySummaryDTO> repositorySummaries) {
        this.repositorySummaries = repositorySummaries != null ? repositorySummaries : Collections.emptyList();
    }

    public int getRepositoriesCount() { return repositoriesCount; }
    public void setRepositoriesCount(int repositoriesCount) { this.repositoriesCount = repositoriesCount; }

    public List<String> getPrSizeLabels() { return prSizeLabels; }
    public void setPrSizeLabels(List<String> prSizeLabels) {
        this.prSizeLabels = prSizeLabels != null ? prSizeLabels : Collections.emptyList();
    }

    public List<Integer> getPrSizeValues() { return prSizeValues; }
    public void setPrSizeValues(List<Integer> prSizeValues) {
        this.prSizeValues = prSizeValues != null ? prSizeValues : Collections.emptyList();
    }

    public List<String> getRepositoryNames() { return repositoryNames; }
    public void setRepositoryNames(List<String> repositoryNames) {
        this.repositoryNames = repositoryNames != null ? repositoryNames : Collections.emptyList();
    }
}
