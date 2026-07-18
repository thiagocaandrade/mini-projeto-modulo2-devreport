package com.devreport.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ValidateRequestNode {

    private static final Logger log = LoggerFactory.getLogger(ValidateRequestNode.class);

    public AnalysisState execute(AnalysisState state) {
        log.info("Validating request period: {} to {}", state.getStartDate(), state.getEndDate());

        if (state.getStartDate() == null || state.getEndDate() == null) {
            state.setMessage("Informe um período válido para gerar o relatório.");
            state.getErrors().add("startDate or endDate is null");
            log.warn("Validation failed: null dates");
            return state;
        }

        if (state.getEndDate().isBefore(state.getStartDate())) {
            state.setMessage("Informe um período válido para gerar o relatório.");
            state.getErrors().add("endDate is before startDate");
            log.warn("Validation failed: endDate {} is before startDate {}", state.getEndDate(), state.getStartDate());
            return state;
        }

        log.info("Validation passed");
        return state;
    }

    public boolean hasErrors(AnalysisState state) {
        return !state.getErrors().isEmpty();
    }
}
