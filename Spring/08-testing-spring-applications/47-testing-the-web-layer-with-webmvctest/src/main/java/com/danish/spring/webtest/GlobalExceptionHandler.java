package com.danish.spring.webtest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// @RestControllerAdvice IS part of the web layer - @WebMvcTest loads it, along with
// @RestController and @JsonComponent beans. This is exactly why @WebMvcTest exists as a
// separate slice from a plain @Mock-based test (lesson 46): it tests real Spring MVC
// request handling - path matching, validation, exception translation - not achievable
// by calling a controller method directly as a plain Java object.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(BookNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}
