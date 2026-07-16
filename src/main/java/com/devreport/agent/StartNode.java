package com.devreport.agent;

import com.devreport.domain.AnalysisRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class StartNode {

    private static final Logger log = LoggerFactory.getLogger(StartNode.class);

    public AnalysisState execute(AnalysisRequest request) {
        log.info("Starting analysis - period: {} to {}, repos: {}",
                request.getStartDate(), request.getEndDate(),
                request.getRepositories().isEmpty() ? "default" : request.getRepositories());
        AnalysisState state = new AnalysisState(request.getStartDate(), request.getEndDate());
        state.setRepositories(request.getRepositories());
        return state;
    }
}
