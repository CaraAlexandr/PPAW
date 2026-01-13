package com.ppaw.passwordvault.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator pentru @OptionalSize
 * Validează dimensiunea doar dacă string-ul nu este null sau gol
 */
public class OptionalSizeValidator implements ConstraintValidator<OptionalSize, String> {
    
    private int min;
    private int max;
    
    @Override
    public void initialize(OptionalSize constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Permite null sau string gol (opțional)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        
        // Validează dimensiunea doar dacă string-ul nu este gol
        int length = value.length();
        return length >= min && length <= max;
    }
}

