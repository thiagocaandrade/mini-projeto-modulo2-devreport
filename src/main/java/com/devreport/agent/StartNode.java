package com.devreport.agent;

import com.devreport.domain.AnalysisRequest;
import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StartNode implements NodeAction<AnalysisState> {

    private static final Logger log = LoggerFactory.getLogger(StartNode.class);

    /**
     * Legacy entry point: creates state from request and runs as a standalone step.
     * Used by DevReportAgent to build initial state before graph.invoke().
     */
    public AnalysisState buildInitialState(AnalysisRequest request) {
        Map<String, Object> initData = new java.util.LinkedHashMap<>();
        initData.put(AnalysisState.START_DATE_KEY, request.getStartDate());
        initData.put(AnalysisState.END_DATE_KEY, request.getEndDate());
        initData.put(AnalysisState.REPOSITORIES_KEY,
                request.getRepositories() != null ? request.getRepositories() : java.util.Collections.emptyList());
        return new AnalysisState(initData);
    }

    @Override
    public Map<String, Object> apply(AnalysisState state) {
        log.info("Starting analysis - period: {} to {}, repos: {}",
                state.getStartDate(), state.getEndDate(),
                state.getRepositories().isEmpty() ? "default" : state.getRepositories());
        // StartNode is the entry point: no changes needed, just log and pass through
        return Map.of();
    }
}
