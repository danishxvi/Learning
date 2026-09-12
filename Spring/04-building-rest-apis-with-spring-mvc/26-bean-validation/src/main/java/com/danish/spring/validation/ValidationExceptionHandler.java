package com.danish.spring.validation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Objects;

// Reuses lesson 25's exact pattern - one @RestControllerAdvice, one @ExceptionHandler per
// failure type. MethodArgumentNotValidException is what Spring MVC throws when @Valid
// finds ANY constraint violation on a @RequestBody - never a generic "Bad Request" text,
// always this one specific exception type, carrying every violation found.
@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ValidationErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .toList();
        ValidationErrorResponse body = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "Validation Failed", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    private ValidationErrorResponse.FieldErrorDetail toDetail(FieldError fieldError) {
        Object rejected = fieldError.getRejectedValue();
        return new ValidationErrorResponse.FieldErrorDetail(
                fieldError.getField(),
                Objects.toString(rejected, "null"),
                fieldError.getDefaultMessage());
    }
}
