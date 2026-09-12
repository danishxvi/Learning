package com.danish.spring.exceptions;

import java.time.Instant;

// A structured, PREDICTABLE error shape - the same fields, every time, for every kind
// of failure this API can produce. Compare this to lesson 22's generic Spring Boot
// default body: {"timestamp":...,"status":400,"error":"Bad Request","path":...} - this
// keeps that same spirit but adds a real, specific "message" a client can act on, and a
// stable field set an API consumer can write parsing code against with confidence.
public class ErrorResponse {
    private final Instant timestamp = Instant.now();
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public Instant getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
}
