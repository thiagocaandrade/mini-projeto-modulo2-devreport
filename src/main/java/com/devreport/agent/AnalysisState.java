package com.devreport.agent;

import com.devreport.domain.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AnalysisState {

    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> repositories = new ArrayList<>();
    private List<Issue> issues = new ArrayList<>();
    private List<PullRequest> pullRequests = new ArrayList<>();
    private Metric metrics;
    private PRMetrics prMetrics;
    private List<RepositorySummary> repositorySummaries = new ArrayList<>();
    private ChartData periodChart;
    private ChartData categoryChart;
    private ChartData prSizeChart;
    private int repositoriesCount;
    private Insight summary;
    private DashboardReport dashboard;
    private String message;
    private List<String> errors = new ArrayList<>();

    public AnalysisState() {
    }

    public AnalysisState(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public List<String> getRepositories() { return repositories; }
    public void setRepositories(List<String> repositories) { this.repositories = repositories != null ? repositories : new ArrayList<>(); }

    public List<Issue> getIssues() { return issues; }
    public void setIssues(List<Issue> issues) { this.issues = issues != null ? issues : new ArrayList<>(); }

    public List<PullRequest> getPullRequests() { return pullRequests; }
    public void setPullRequests(List<PullRequest> pullRequests) { this.pullRequests = pullRequests != null ? pullRequests : new ArrayList<>(); }

    public Metric getMetrics() { return metrics; }
    public void setMetrics(Metric metrics) { this.metrics = metrics; }

    public ChartData getPrSizeChart() { return prSizeChart; }
    public void setPrSizeChart(ChartData prSizeChart) { this.prSizeChart = prSizeChart; }

    public int getRepositoriesCount() { return repositoriesCount; }
    public void setRepositoriesCount(int repositoriesCount) { this.repositoriesCount = repositoriesCount; }

    public PRMetrics getPrMetrics() { return prMetrics; }
    public void setPrMetrics(PRMetrics prMetrics) { this.prMetrics = prMetrics; }

    public List<RepositorySummary> getRepositorySummaries() { return repositorySummaries; }
    public void setRepositorySummaries(List<RepositorySummary> repositorySummaries) { this.repositorySummaries = repositorySummaries != null ? repositorySummaries : new ArrayList<>(); }

    public ChartData getPeriodChart() { return periodChart; }
    public void setPeriodChart(ChartData periodChart) { this.periodChart = periodChart; }

    public ChartData getCategoryChart() { return categoryChart; }
    public void setCategoryChart(ChartData categoryChart) { this.categoryChart = categoryChart; }

    public Insight getSummary() { return summary; }
    public void setSummary(Insight summary) { this.summary = summary; }

    public DashboardReport getDashboard() { return dashboard; }
    public void setDashboard(DashboardReport dashboard) { this.dashboard = dashboard; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors != null ? errors : new ArrayList<>(); }
}
