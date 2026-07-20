package com.devreport.domain;

import java.util.Collections;
import java.util.List;

public class DashboardReport implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private final Metric metrics;
    private final ChartData periodChart;
    private final ChartData categoryChart;
    private final Insight summary;
    private final String message;
    private final PRMetrics prMetrics;
    private final List<RepositorySummary> repositorySummaries;
    private final int repositoriesCount;
    private final ChartData prSizeChart;

    public DashboardReport(Metric metrics, ChartData periodChart, ChartData categoryChart,
                           Insight summary, String message) {
        this(metrics, periodChart, categoryChart, summary, message,
                null, null, 0, null);
    }

    public DashboardReport(Metric metrics, ChartData periodChart, ChartData categoryChart,
                           Insight summary, String message,
                           PRMetrics prMetrics, List<RepositorySummary> repositorySummaries,
                           int repositoriesCount, ChartData prSizeChart) {
        this.metrics = metrics;
        this.periodChart = periodChart;
        this.categoryChart = categoryChart;
        this.summary = summary;
        this.message = message;
        this.prMetrics = prMetrics;
        this.repositorySummaries = repositorySummaries != null
                ? Collections.unmodifiableList(repositorySummaries)
                : Collections.emptyList();
        this.repositoriesCount = repositoriesCount;
        this.prSizeChart = prSizeChart;
    }

    public Metric getMetrics() {
        return metrics;
    }

    public ChartData getPeriodChart() {
        return periodChart;
    }

    public ChartData getCategoryChart() {
        return categoryChart;
    }

    public Insight getSummary() {
        return summary;
    }

    public String getMessage() {
        return message;
    }

    public PRMetrics getPrMetrics() {
        return prMetrics;
    }

    public List<RepositorySummary> getRepositorySummaries() {
        return repositorySummaries;
    }

    public int getRepositoriesCount() {
        return repositoriesCount;
    }

    public ChartData getPrSizeChart() {
        return prSizeChart;
    }
}
