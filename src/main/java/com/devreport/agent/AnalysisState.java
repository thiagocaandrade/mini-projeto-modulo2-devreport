package com.devreport.agent;

import com.devreport.domain.*;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.time.LocalDate;
import java.util.*;

public class AnalysisState extends AgentState {

    // Channel keys
    public static final String START_DATE_KEY = "startDate";
    public static final String END_DATE_KEY = "endDate";
    public static final String REPOSITORIES_KEY = "repositories";
    public static final String ISSUES_KEY = "issues";
    public static final String PULL_REQUESTS_KEY = "pullRequests";
    public static final String METRICS_KEY = "metrics";
    public static final String PR_METRICS_KEY = "prMetrics";
    public static final String REPOSITORY_SUMMARIES_KEY = "repositorySummaries";
    public static final String PERIOD_CHART_KEY = "periodChart";
    public static final String CATEGORY_CHART_KEY = "categoryChart";
    public static final String PR_SIZE_CHART_KEY = "prSizeChart";
    public static final String REPOSITORIES_COUNT_KEY = "repositoriesCount";
    public static final String SUMMARY_KEY = "summary";
    public static final String DASHBOARD_KEY = "dashboard";
    public static final String MESSAGE_KEY = "message";
    public static final String ERRORS_KEY = "errors";

    // Schema: defines how channels merge values (built with LinkedHashMap for 16+ entries)
    @SuppressWarnings("unchecked")
    public static final Map<String, Channel<?>> SCHEMA = createSchema();

    private static Map<String, Channel<?>> createSchema() {
        Map<String, Channel<?>> schema = new LinkedHashMap<>();
        schema.put(START_DATE_KEY, Channels.base(() -> LocalDate.now()));
        schema.put(END_DATE_KEY, Channels.base(() -> LocalDate.now()));
        schema.put(REPOSITORIES_KEY, Channels.base(ArrayList::new));
        schema.put(ISSUES_KEY, Channels.base(ArrayList::new));
        schema.put(PULL_REQUESTS_KEY, Channels.base(ArrayList::new));
        schema.put(METRICS_KEY, Channels.base(() -> new Metric(0, 0, 0, 0)));
        schema.put(PR_METRICS_KEY, Channels.base(() -> new PRMetrics(0, 0, 0, 0, 0.0, 0, java.util.Map.of())));
        schema.put(REPOSITORY_SUMMARIES_KEY, Channels.base(ArrayList::new));
        schema.put(PERIOD_CHART_KEY, Channels.base(() -> new ChartData(java.util.List.of(), java.util.List.of())));
        schema.put(CATEGORY_CHART_KEY, Channels.base(() -> new ChartData(java.util.List.of(), java.util.List.of())));
        schema.put(PR_SIZE_CHART_KEY, Channels.base(() -> new ChartData(java.util.List.of(), java.util.List.of())));
        schema.put(REPOSITORIES_COUNT_KEY, Channels.base(() -> 0));
        schema.put(SUMMARY_KEY, Channels.base(() -> new Insight("")));
        schema.put(DASHBOARD_KEY, Channels.base(() -> new DashboardReport(
                new Metric(0, 0, 0, 0),
                new ChartData(java.util.List.of(), java.util.List.of()),
                new ChartData(java.util.List.of(), java.util.List.of()),
                null, "")));
        schema.put(MESSAGE_KEY, Channels.base(() -> ""));
        schema.put(ERRORS_KEY, Channels.appender(ArrayList::new));
        return Collections.unmodifiableMap(schema);
    }

    public AnalysisState(Map<String, Object> initData) {
        super(initData);
    }

    // ── Typed accessors (read-only — state is immutable, changes flow via return Map) ──

    @SuppressWarnings("unchecked")
    public LocalDate getStartDate() {
        return (LocalDate) data().get(START_DATE_KEY);
    }

    @SuppressWarnings("unchecked")
    public LocalDate getEndDate() {
        return (LocalDate) data().get(END_DATE_KEY);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRepositories() {
        return (List<String>) data().getOrDefault(REPOSITORIES_KEY, Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    public List<Issue> getIssues() {
        return (List<Issue>) data().getOrDefault(ISSUES_KEY, Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    public List<PullRequest> getPullRequests() {
        return (List<PullRequest>) data().getOrDefault(PULL_REQUESTS_KEY, Collections.emptyList());
    }

    public Metric getMetrics() {
        return (Metric) data().get(METRICS_KEY);
    }

    public PRMetrics getPrMetrics() {
        return (PRMetrics) data().get(PR_METRICS_KEY);
    }

    @SuppressWarnings("unchecked")
    public List<RepositorySummary> getRepositorySummaries() {
        return (List<RepositorySummary>) data().getOrDefault(REPOSITORY_SUMMARIES_KEY, Collections.emptyList());
    }

    public ChartData getPeriodChart() {
        return (ChartData) data().get(PERIOD_CHART_KEY);
    }

    public ChartData getCategoryChart() {
        return (ChartData) data().get(CATEGORY_CHART_KEY);
    }

    public ChartData getPrSizeChart() {
        return (ChartData) data().get(PR_SIZE_CHART_KEY);
    }

    public int getRepositoriesCount() {
        Object val = data().get(REPOSITORIES_COUNT_KEY);
        return val instanceof Integer i ? i : 0;
    }

    public Insight getSummary() {
        return (Insight) data().get(SUMMARY_KEY);
    }

    public DashboardReport getDashboard() {
        return (DashboardReport) data().get(DASHBOARD_KEY);
    }

    public String getMessage() {
        return (String) data().get(MESSAGE_KEY);
    }

    @SuppressWarnings("unchecked")
    public List<String> getErrors() {
        return (List<String>) data().getOrDefault(ERRORS_KEY, Collections.emptyList());
    }
}
