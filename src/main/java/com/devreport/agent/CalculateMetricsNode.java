package com.devreport.agent;

import com.devreport.domain.*;
import com.devreport.metrics.MetricsService;
import com.devreport.metrics.ChartDataService;
import com.devreport.metrics.PRMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CalculateMetricsNode {

    private static final Logger log = LoggerFactory.getLogger(CalculateMetricsNode.class);

    private final MetricsService metricsService;
    private final ChartDataService chartDataService;
    private final PRMetricsService prMetricsService;

    public CalculateMetricsNode(MetricsService metricsService, ChartDataService chartDataService,
                                 PRMetricsService prMetricsService) {
        this.metricsService = metricsService;
        this.chartDataService = chartDataService;
        this.prMetricsService = prMetricsService;
    }

    public AnalysisState execute(AnalysisState state) {
        log.info("Calculating metrics for {} issues and {} PRs",
                state.getIssues().size(), state.getPullRequests().size());

        // Issue metrics
        var issues = state.getIssues();
        var metrics = metricsService.calculate(issues, state.getStartDate(), state.getEndDate());
        var periodChart = chartDataService.buildPeriodChart(issues);
        var categoryChart = chartDataService.buildCategoryChart(issues);

        state.setMetrics(metrics);
        state.setPeriodChart(periodChart);
        state.setCategoryChart(categoryChart);

        log.info("Metrics calculated: total={}, features={}, bugs={}, tasks={}",
                metrics.getTotal(), metrics.getFeatures(), metrics.getBugs(), metrics.getTasks());

        // PR metrics
        var prs = state.getPullRequests();
        var prMetrics = prMetricsService.calculate(prs);
        var prSizeChart = chartDataService.buildPRSizeChart(prMetrics);

        state.setPrMetrics(prMetrics);
        state.setPrSizeChart(prSizeChart);

        log.info("PR metrics calculated: totalMerged={}, additions={}, deletions={}",
                prMetrics.getTotalMerged(), prMetrics.getTotalAdditions(), prMetrics.getTotalDeletions());

        // Repository summaries
        List<RepositorySummary> repoSummaries = computeRepositorySummaries(issues, prs);
        state.setRepositorySummaries(repoSummaries);

        // Count unique repositories
        Set<String> repoNames = new HashSet<>();
        for (Issue issue : issues) {
            if (issue.getRepository() != null) repoNames.add(issue.getRepository());
        }
        for (PullRequest pr : prs) {
            if (pr.getRepository() != null) repoNames.add(pr.getRepository());
        }
        if (state.getRepositories() != null && !state.getRepositories().isEmpty()) {
            repoNames.addAll(state.getRepositories());
        }
        state.setRepositoriesCount(repoNames.size());

        return state;
    }

    private List<RepositorySummary> computeRepositorySummaries(List<Issue> issues, List<PullRequest> prs) {
        Map<String, Integer> issuesPerRepo = new HashMap<>();
        Map<String, Integer> prsPerRepo = new HashMap<>();
        Map<String, Integer> additionsPerRepo = new HashMap<>();

        for (Issue issue : issues) {
            String repo = issue.getRepository() != null ? issue.getRepository() : "unknown";
            issuesPerRepo.merge(repo, 1, Integer::sum);
        }

        for (PullRequest pr : prs) {
            String repo = pr.getRepository() != null ? pr.getRepository() : "unknown";
            prsPerRepo.merge(repo, 1, Integer::sum);
            additionsPerRepo.merge(repo, pr.getAdditions(), Integer::sum);
        }

        Set<String> allRepos = new HashSet<>();
        allRepos.addAll(issuesPerRepo.keySet());
        allRepos.addAll(prsPerRepo.keySet());

        return allRepos.stream()
                .map(repo -> new RepositorySummary(
                        repo,
                        issuesPerRepo.getOrDefault(repo, 0),
                        prsPerRepo.getOrDefault(repo, 0),
                        additionsPerRepo.getOrDefault(repo, 0)))
                .collect(Collectors.toList());
    }
}
