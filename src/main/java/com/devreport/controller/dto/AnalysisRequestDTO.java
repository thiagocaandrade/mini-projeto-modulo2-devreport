package com.devreport.controller.dto;

import com.devreport.shared.ValidPeriod;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@ValidPeriod
public class AnalysisRequestDTO {

    @NotNull(message = "A data inicial é obrigatória.")
    private LocalDate startDate;

    @NotNull(message = "A data final é obrigatória.")
    private LocalDate endDate;

    private List<String> repositories;

    public AnalysisRequestDTO() {
    }

    public AnalysisRequestDTO(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<String> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }
}
