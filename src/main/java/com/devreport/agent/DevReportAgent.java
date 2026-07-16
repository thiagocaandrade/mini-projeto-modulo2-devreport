package com.devreport.agent;

import com.devreport.domain.AnalysisRequest;
import com.devreport.domain.DashboardReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DevReportAgent {

    private static final Logger log = LoggerFactory.getLogger(DevReportAgent.class);

    private final StartNode startNode;
    private final ValidateRequestNode validateRequestNode;
    private final FetchGitHubDataNode fetchGitHubDataNode;
    private final CalculateMetricsNode calculateMetricsNode;
    private final GenerateInsightsNode generateInsightsNode;
    private final BuildDashboardNode buildDashboardNode;

    public DevReportAgent(StartNode startNode,
                          ValidateRequestNode validateRequestNode,
                          FetchGitHubDataNode fetchGitHubDataNode,
                          CalculateMetricsNode calculateMetricsNode,
                          GenerateInsightsNode generateInsightsNode,
                          BuildDashboardNode buildDashboardNode) {
        this.startNode = startNode;
        this.validateRequestNode = validateRequestNode;
        this.fetchGitHubDataNode = fetchGitHubDataNode;
        this.calculateMetricsNode = calculateMetricsNode;
        this.generateInsightsNode = generateInsightsNode;
        this.buildDashboardNode = buildDashboardNode;
    }

    public DashboardReport analyze(AnalysisRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("=== Analysis started ===");
        log.info("Period: {} to {}, Repositories: {}",
                request.getStartDate(), request.getEndDate(),
                request.getRepositories().isEmpty() ? "default" : request.getRepositories());

        // Node 1: Start
        AnalysisState state = startNode.execute(request);

        // Node 2: Validate
        state = validateRequestNode.execute(state);
        if (hasErrors(state)) {
            log.warn("Validation failed, aborting analysis");
            state = buildDashboardNode.execute(state);
            logExecutionTime(startTime);
            return state.getDashboard();
        }

        // Node 3: Fetch GitHub Data
        log.info("Fetching GitHub issues...");
        state = fetchGitHubDataNode.execute(state);
        if (hasErrors(state)) {
            log.warn("GitHub fetch failed, building dashboard with error");
            state.setMessage("Não foi possível consultar os dados do GitHub no momento.");
            state = calculateMetricsNode.execute(state);
            state = buildDashboardNode.execute(state);
            logExecutionTime(startTime);
            return state.getDashboard();
        }

        // Node 4: Calculate Metrics
        log.info("Calculating metrics...");
        state = calculateMetricsNode.execute(state);
        log.info("Metrics calculated: total={}, features={}, bugs={}, tasks={}",
                state.getMetrics().getTotal(), state.getMetrics().getFeatures(),
                state.getMetrics().getBugs(), state.getMetrics().getTasks());

        // Node 5: Generate Insights (non-blocking - errors don't stop the flow)
        log.info("Generating insights...");
        state = generateInsightsNode.execute(state);

        // Node 6: Build Dashboard
        log.info("Building dashboard...");
        state = buildDashboardNode.execute(state);

        logExecutionTime(startTime);
        log.info("=== Analysis completed ===");

        return state.getDashboard();
    }

    private boolean hasErrors(AnalysisState state) {
        return !state.getErrors().isEmpty();
    }

    private void logExecutionTime(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("Total processing time: {} ms", duration);
    }
}
