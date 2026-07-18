package com.devreport.controller.dto;

import com.devreport.shared.ValidPeriod;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@ValidPeriod
public class AnalysisRequestDTO {

    @NotNull(message = "A data inicial é obrigatória.")
    private LocalDate startDate;

    @NotNull(message = "A data final é obrigatória.")
    private LocalDate endDate;

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
}
