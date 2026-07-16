package com.devreport.metrics;

import com.devreport.domain.ChartData;
import com.devreport.domain.Issue;
import com.devreport.domain.Metric;
import com.devreport.domain.PRMetrics;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ChartDataService {

    private final MetricsService metricsService;

    public ChartDataService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    public ChartData buildPeriodChart(List<Issue> issues) {
        if (issues == null || issues.isEmpty()) {
            return new ChartData(Collections.emptyList(), Collections.emptyList());
        }

        Map<String, Integer> countByDate = new TreeMap<>();
        for (Issue issue : issues) {
            if (issue.getClosedAt() != null) {
                String dateKey = issue.getClosedAt().toLocalDate().toString();
                countByDate.merge(dateKey, 1, Integer::sum);
            }
        }

        List<String> labels = new ArrayList<>(countByDate.keySet());
        List<Integer> values = countByDate.values().stream().toList();
        return new ChartData(labels, values);
    }

    public ChartData buildCategoryChart(List<Issue> issues) {
        Metric metric = metricsService.calculate(issues);
        List<String> labels = List.of("Features", "Bugs", "Tasks");
        List<Integer> values = List.of(metric.getFeatures(), metric.getBugs(), metric.getTasks());
        return new ChartData(labels, values);
    }

    public ChartData buildPRSizeChart(PRMetrics prMetrics) {
        if (prMetrics == null || prMetrics.getPrSizeDistribution() == null || prMetrics.getPrSizeDistribution().isEmpty()) {
            return new ChartData(List.of("Pequeno", "Médio", "Grande"), List.of(0, 0, 0));
        }

        Map<String, Integer> dist = prMetrics.getPrSizeDistribution();
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        if (dist.containsKey("small")) { labels.add("Pequeno"); values.add(dist.get("small")); }
        else { labels.add("Pequeno"); values.add(0); }

        if (dist.containsKey("medium")) { labels.add("Médio"); values.add(dist.get("medium")); }
        else { labels.add("Médio"); values.add(0); }

        if (dist.containsKey("large")) { labels.add("Grande"); values.add(dist.get("large")); }
        else { labels.add("Grande"); values.add(0); }

        return new ChartData(labels, values);
    }
}
