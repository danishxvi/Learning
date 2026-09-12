package com.danish.spring.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// The actual check, kept deliberately simple: a 13-digit ISBN, digits only. Real ISBN
// validation includes a checksum digit - simplified here since the POINT of this lesson
// is "how do I write my OWN constraint," not "how does the ISBN-13 checksum work."
public class IsbnValidator implements ConstraintValidator<ValidIsbn, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // let @NotBlank/@NotNull handle absence - this constraint only judges FORMAT
        }
        return value.matches("\\d{13}");
    }
}
