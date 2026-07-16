package com.devreport.agent;

import com.devreport.ai.InsightService;
import com.devreport.domain.Insight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GenerateInsightsNode {

    private static final Logger log = LoggerFactory.getLogger(GenerateInsightsNode.class);

    private final InsightService insightService;

    public GenerateInsightsNode(InsightService insightService) {
        this.insightService = insightService;
    }

    public AnalysisState execute(AnalysisState state) {
        log.info("Generating insights");

        if (state.getMetrics() == null) {
            log.warn("No metrics available, skipping insights");
            return state;
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
                state.setSummary(insight);
                log.info("Insight generated successfully");
            } else {
                log.warn("Insight generation returned null (AI unavailable or no data)");
            }
        } catch (Exception e) {
            log.error("Failed to generate insights: {}", e.getMessage());
            // Non-blocking: do NOT add to state.errors
        }

        return state;
    }
}
