package com.devreport.agent;

import com.devreport.dashboard.DashboardService;
import com.devreport.domain.DashboardReport;
import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BuildDashboardNode implements NodeAction<AnalysisState> {

    private static final Logger log = LoggerFactory.getLogger(BuildDashboardNode.class);

    private final DashboardService dashboardService;

    public BuildDashboardNode(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    public Map<String, Object> apply(AnalysisState state) {
        log.info("Building dashboard report");

        String message = state.getMessage();

        // Set empty state message when no issues and no errors
        if (message == null && state.getErrors().isEmpty()) {
            if (state.getMetrics() == null || state.getMetrics().getTotal() == 0) {
                message = "Não existem entregas concluídas para o período informado.";
            }
        }

        DashboardReport report = dashboardService.build(
                state.getMetrics(),
                state.getPeriodChart(),
                state.getCategoryChart(),
                state.getSummary(),
                message,
                state.getPrMetrics(),
                state.getRepositorySummaries(),
                state.getRepositoriesCount(),
                state.getPrSizeChart()
        );

        log.info("Dashboard report built successfully");
        return Map.of(AnalysisState.DASHBOARD_KEY, report,
                      AnalysisState.MESSAGE_KEY, message != null ? message : "");
    }
}
