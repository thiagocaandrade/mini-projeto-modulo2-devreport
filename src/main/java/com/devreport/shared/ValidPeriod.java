package com.devreport.shared;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PeriodValidator.class)
public @interface ValidPeriod {

    String message() default "A data final deve ser igual ou posterior à data inicial.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
