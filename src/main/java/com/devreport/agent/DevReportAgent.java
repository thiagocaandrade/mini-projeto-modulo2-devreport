package com.devreport.agent;

import com.devreport.domain.AnalysisRequest;
import com.devreport.domain.DashboardReport;
import jakarta.annotation.PostConstruct;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Service
public class DevReportAgent {

    private static final Logger log = LoggerFactory.getLogger(DevReportAgent.class);

    private final StartNode startNode;
    private final ValidateRequestNode validateRequestNode;
    private final FetchGitHubDataNode fetchGitHubDataNode;
    private final CalculateMetricsNode calculateMetricsNode;
    private final GenerateInsightsNode generateInsightsNode;
    private final BuildDashboardNode buildDashboardNode;

    private CompiledGraph<AnalysisState> graph;

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

    @PostConstruct
    public void initGraph() {
        log.info("Initializing LangGraph4j StateGraph...");

        try {
            StateGraph<AnalysisState> stateGraph = new StateGraph<>(AnalysisState.SCHEMA, AnalysisState::new);

            stateGraph
                    .addNode("start", node_async(startNode))
                    .addNode("validate", node_async(validateRequestNode))
                    .addNode("fetchGitHubData", node_async(fetchGitHubDataNode))
                    .addNode("calculateMetrics", node_async(calculateMetricsNode))
                    .addNode("generateInsights", node_async(generateInsightsNode))
                    .addNode("buildDashboard", node_async(buildDashboardNode))

                    // Entry
                    .addEdge(START, "start")
                    .addEdge("start", "validate")

                    // Conditional: validate → ok → fetch, error → buildDashboard
                    .addConditionalEdges("validate",
                            edge_async(this::validateRouter),
                            Map.of("ok", "fetchGitHubData", "error", "buildDashboard"))

                    // fetch → calculateMetrics
                    .addEdge("fetchGitHubData", "calculateMetrics")

                    // Conditional: calculateMetrics → ok → generateInsights, error → buildDashboard
                    .addConditionalEdges("calculateMetrics",
                            edge_async(this::fetchRouter),
                            Map.of("ok", "generateInsights", "error", "buildDashboard"))

                    // generateInsights is non-blocking → always goes to buildDashboard
                    .addEdge("generateInsights", "buildDashboard")
                    .addEdge("buildDashboard", END);

            this.graph = stateGraph.compile();
            log.info("LangGraph4j StateGraph compiled successfully");
        } catch (Exception e) {
            log.error("Failed to compile LangGraph4j StateGraph: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize LangGraph4j agent graph", e);
        }
    }

    /**
     * Router after validation: if errors exist, skip to buildDashboard.
     */
    private String validateRouter(AnalysisState state) {
        if (validateRequestNode.hasErrors(state)) {
            log.warn("Validation failed, routing to buildDashboard");
            return "error";
        }
        log.info("Validation passed, routing to fetchGitHubData");
        return "ok";
    }

    /**
     * Router after fetch/calculate: if errors exist, skip to buildDashboard.
     */
    private String fetchRouter(AnalysisState state) {
        if (fetchGitHubDataNode.hasErrors(state)) {
            log.warn("Errors detected after fetch, routing to buildDashboard");
            return "error";
        }
        log.info("No errors, routing to generateInsights");
        return "ok";
    }

    public DashboardReport analyze(AnalysisRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("=== Analysis started ===");
        log.info("Period: {} to {}, Repositories: {}",
                request.getStartDate(), request.getEndDate(),
                request.getRepositories().isEmpty() ? "default" : request.getRepositories());

        // Build initial state from request
        AnalysisState initialState = startNode.buildInitialState(request);

        // Execute compiled graph
        log.info("Invoking LangGraph4j compiled graph...");
        log.info("Initial state data keys: {}", initialState.data().keySet());
        log.info("Graph compiled: {}", graph != null);
        Map<String, Object> inputs = new java.util.HashMap<>(initialState.data());
        log.info("Input map size: {}", inputs.size());
        var result = graph.invoke(inputs);

        DashboardReport report = result
                .map(AnalysisState::getDashboard)
                .orElse(null);

        if (report == null) {
            log.error("Agent execution produced no dashboard report");
            throw new RuntimeException("Agent execution failed: no result produced");
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("=== Analysis completed in {} ms ===", duration);
        return report;
    }
}
