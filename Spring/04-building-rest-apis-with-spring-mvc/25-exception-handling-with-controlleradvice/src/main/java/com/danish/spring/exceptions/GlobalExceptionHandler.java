package com.danish.spring.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

// @RestControllerAdvice = @ControllerAdvice + @ResponseBody (the same relationship
// @RestController has to @Controller, lesson 21). It applies to EVERY @RestController in
// the application, not just one - exception handling logic lives here exactly once,
// instead of being duplicated with a try/catch in every controller method.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // @ExceptionHandler names the EXACT exception type this method handles. Spring MVC
    // catches BookNotFoundException wherever it's thrown from ANY controller method,
    // unwinds back here, and calls this method instead of letting it propagate further.
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFound(BookNotFoundException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // Handles the SAME failure lesson 22 left with Spring's generic default body - a
    // path variable that fails to convert (e.g. "/api/books/not-a-number") - but now
    // with a message that actually says what went wrong, instead of a bare "Bad Request".
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Parameter '" + ex.getName() + "' should be of type "
                + (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "a different type")
                + ", but was '" + ex.getValue() + "'";
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "Bad Request", message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // WITHOUT this handler, ResponseStatusException falls all the way through to the
    // Exception.class catch-all below and comes back as a wrong, misleading 500 - see
    // the .md for exactly that failure, captured before this method was added. Spring
    // MVC has its OWN built-in handling for ResponseStatusException, but any
    // @ExceptionHandler(Exception.class) in a @RestControllerAdvice intercepts it FIRST,
    // since @ControllerAdvice handlers take priority over the framework's defaults.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                ex.getStatusCode().value(), ex.getStatusCode().toString(), ex.getReason(), request.getRequestURI());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    // The CATCH-ALL - anything not handled by a more specific @ExceptionHandler above
    // lands here. Deliberately generic: ex.getMessage() for an UNEXPECTED exception could
    // leak internal detail (a SQL fragment, a file path) to a client, so this always
    // returns the same safe, fixed message regardless of what actually broke - the real
    // detail still goes to the server's own logs (lesson 18), just never to the response.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                "Something went wrong. Please try again later.", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
