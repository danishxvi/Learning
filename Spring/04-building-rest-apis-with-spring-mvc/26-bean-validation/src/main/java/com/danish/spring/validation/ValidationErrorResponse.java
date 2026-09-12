package com.danish.spring.validation;

import java.time.Instant;
import java.util.List;

public class ValidationErrorResponse {
    private final Instant timestamp = Instant.now();
    private final int status;
    private final String error;
    private final List<FieldErrorDetail> fieldErrors;

    public ValidationErrorResponse(int status, String error, List<FieldErrorDetail> fieldErrors) {
        this.status = status;
        this.error = error;
        this.fieldErrors = fieldErrors;
    }

    public Instant getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public List<FieldErrorDetail> getFieldErrors() { return fieldErrors; }

    public record FieldErrorDetail(String field, String rejectedValue, String message) {
    }
}
