package com.devreport.agent;

import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ValidateRequestNode implements NodeAction<AnalysisState> {

    private static final Logger log = LoggerFactory.getLogger(ValidateRequestNode.class);

    @Override
    public Map<String, Object> apply(AnalysisState state) {
        log.info("Validating request period: {} to {}", state.getStartDate(), state.getEndDate());

        if (state.getStartDate() == null || state.getEndDate() == null) {
            log.warn("Validation failed: null dates");
            return Map.of(
                AnalysisState.MESSAGE_KEY, "Informe um período válido para gerar o relatório.",
                AnalysisState.ERRORS_KEY, List.of("startDate or endDate is null")
            );
        }

        if (state.getEndDate().isBefore(state.getStartDate())) {
            log.warn("Validation failed: endDate {} is before startDate {}", state.getEndDate(), state.getStartDate());
            return Map.of(
                AnalysisState.MESSAGE_KEY, "Informe um período válido para gerar o relatório.",
                AnalysisState.ERRORS_KEY, List.of("endDate is before startDate")
            );
        }

        log.info("Validation passed");
        return Map.of();
    }

    public boolean hasErrors(AnalysisState state) {
        return !state.getErrors().isEmpty();
    }
}
