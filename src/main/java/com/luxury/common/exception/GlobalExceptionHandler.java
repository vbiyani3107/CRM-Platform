package com.luxury.common.exception;

import com.luxury.common.dto.ApiResponse;
import com.luxury.core.api.exception.DynamicValidationException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler following RFC 7807 error response conventions.
 * All API errors are wrapped in ApiResponse with structured ErrorDetail.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(
                        "https://luxury.com/errors/not-found",
                        "Resource Not Found",
                        ex.getMessage(),
                        404
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");

        log.warn("Validation error: {}", detail);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(
                        "https://luxury.com/errors/validation",
                        "Validation Error",
                        detail,
                        400
                ));
    }

    @ExceptionHandler(DynamicValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDynamicValidation(DynamicValidationException ex) {
        String detail = String.join("; ", ex.getViolations());
        log.warn("Dynamic validation error: {}", detail);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(
                        "https://luxury.com/errors/dynamic-validation",
                        "Dynamic Validation Error",
                        detail,
                        400
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(
                        "https://luxury.com/errors/bad-request",
                        "Bad Request",
                        ex.getMessage(),
                        400
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(
                        "https://luxury.com/errors/internal",
                        "Internal Server Error",
                        "An unexpected error occurred. Please contact support.",
                        500
                ));
    }
}
