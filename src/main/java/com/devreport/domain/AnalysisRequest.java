package com.devreport.domain;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class AnalysisRequest {

    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<String> repositories;

    public AnalysisRequest(LocalDate startDate, LocalDate endDate) {
        this(startDate, endDate, null);
    }

    public AnalysisRequest(LocalDate startDate, LocalDate endDate, List<String> repositories) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.repositories = repositories != null ? Collections.unmodifiableList(repositories) : Collections.emptyList();
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public List<String> getRepositories() {
        return repositories;
    }
}
