package com.devreport.shared;

import com.devreport.controller.dto.AnalysisRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PeriodValidator implements ConstraintValidator<ValidPeriod, AnalysisRequestDTO> {

    @Override
    public boolean isValid(AnalysisRequestDTO dto, ConstraintValidatorContext context) {
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            return true;
        }
        return !dto.getEndDate().isBefore(dto.getStartDate());
    }
}
