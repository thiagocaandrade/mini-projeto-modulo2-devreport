package com.devreport.dashboard;

import com.devreport.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    public DashboardReport build(Metric metrics, ChartData periodChart, ChartData categoryChart,
                                  Insight summary, String message) {
        return new DashboardReport(metrics, periodChart, categoryChart, summary, message,
                null, null, 0, null);
    }

    public DashboardReport build(Metric metrics, ChartData periodChart, ChartData categoryChart,
                                  Insight summary, String message,
                                  PRMetrics prMetrics, List<RepositorySummary> repositorySummaries,
                                  int repositoriesCount, ChartData prSizeChart) {
        return new DashboardReport(metrics, periodChart, categoryChart, summary, message,
                prMetrics, repositorySummaries, repositoriesCount, prSizeChart);
    }
}
