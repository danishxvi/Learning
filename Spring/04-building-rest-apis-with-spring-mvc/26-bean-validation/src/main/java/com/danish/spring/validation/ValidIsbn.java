package com.danish.spring.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

// A CUSTOM constraint - built the exact same way @NotBlank and @Email (lesson 16) are
// built: an annotation naming a ConstraintValidator that does the actual check. Once
// declared, @ValidIsbn is used exactly like a built-in constraint everywhere else.
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IsbnValidator.class)
public @interface ValidIsbn {
    String message() default "must be a valid 13-digit ISBN";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
