package com.ppaw.passwordvault.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validare pentru câmpuri opționale care trebuie să respecte @Size doar dacă nu sunt goale
 * Folosit pentru parole la update (permite gol, dar dacă se furnizează, trebuie să respecte min size)
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OptionalSizeValidator.class)
@Documented
public @interface OptionalSize {
    String message() default "Câmpul nu respectă dimensiunile cerute";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    int min() default 0;
    int max() default Integer.MAX_VALUE;
}

