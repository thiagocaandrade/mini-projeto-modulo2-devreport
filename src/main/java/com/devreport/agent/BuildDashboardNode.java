package com.devreport.agent;

import com.devreport.dashboard.DashboardService;
import com.devreport.domain.DashboardReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BuildDashboardNode {

    private static final Logger log = LoggerFactory.getLogger(BuildDashboardNode.class);

    private final DashboardService dashboardService;

    public BuildDashboardNode(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    public AnalysisState execute(AnalysisState state) {
        log.info("Building dashboard report");

        // Set empty state message when no issues and no errors
        if (state.getMessage() == null && state.getErrors().isEmpty()) {
            if (state.getMetrics() == null || state.getMetrics().getTotal() == 0) {
                state.setMessage("Não existem entregas concluídas para o período informado.");
            }
        }

        DashboardReport report = dashboardService.build(
                state.getMetrics(),
                state.getPeriodChart(),
                state.getCategoryChart(),
                state.getSummary(),
                state.getMessage(),
                state.getPrMetrics(),
                state.getRepositorySummaries(),
                state.getRepositoriesCount(),
                state.getPrSizeChart()
        );

        state.setDashboard(report);
        log.info("Dashboard report built successfully");
        return state;
    }
}
