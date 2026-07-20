package com.devreport.agent;

import com.devreport.ai.InsightService;
import com.devreport.domain.Insight;
import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GenerateInsightsNode implements NodeAction<AnalysisState> {

    private static final Logger log = LoggerFactory.getLogger(GenerateInsightsNode.class);

    private final InsightService insightService;

    public GenerateInsightsNode(InsightService insightService) {
        this.insightService = insightService;
    }

    @Override
    public Map<String, Object> apply(AnalysisState state) {
        log.info("Generating insights");

        if (state.getMetrics() == null) {
            log.warn("No metrics available, skipping insights");
            return Map.of();
        }

        try {
            Insight insight = insightService.generateInsight(
                    state.getMetrics(),
                    state.getPrMetrics(),
                    state.getRepositorySummaries(),
                    state.getRepositoriesCount(),
                    state.getIssues(),
                    state.getPullRequests(),
                    state.getStartDate(),
                    state.getEndDate()
            );

            if (insight != null) {
                log.info("Insight generated successfully");
                return Map.of(AnalysisState.SUMMARY_KEY, insight);
            } else {
                log.warn("Insight generation returned null (AI unavailable or no data)");
            }
        } catch (Exception e) {
            log.error("Failed to generate insights: {}", e.getMessage());
            // Non-blocking: do NOT add to state.errors
        }

        return Map.of();
    }
}
