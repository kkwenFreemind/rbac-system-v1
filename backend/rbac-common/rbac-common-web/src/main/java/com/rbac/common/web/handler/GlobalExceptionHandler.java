package com.rbac.common.web.handler;

import com.rbac.common.core.exception.RbacException;
import com.rbac.common.core.result.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for the RBAC system.
 *
 * This class handles exceptions thrown by controllers and other components,
 * converting them into a standardized Result response format with proper HTTP status codes.
 *
 * @author RBAC System
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle RbacException (custom business exceptions).
     *
     * @param e the RbacException
     * @return ResponseEntity containing Result with error code and message
     */
    @ExceptionHandler(RbacException.class)
    public ResponseEntity<Result<Void>> handleRbacException(RbacException e) {
        log.error("RbacException: code={}, message={}", e.getCode(), e.getMessage());
        HttpStatus status = mapCodeToHttpStatus(e.getCode());
        return ResponseEntity.status(status)
                .body(Result.error(mapCodeToInt(e.getCode()), e.getMessage()));
    }

    /**
     * Handle MethodArgumentNotValidException (validation errors from @Valid).
     *
     * @param e the MethodArgumentNotValidException
     * @return ResponseEntity containing Result with validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(400, message));
    }

    /**
     * Handle BindException (validation errors from parameter binding).
     *
     * @param e the BindException
     * @return ResponseEntity containing Result with validation error details
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Binding failed: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(400, message));
    }

    /**
     * Handle ConstraintViolationException (validation errors from @Validated).
     *
     * @param e the ConstraintViolationException
     * @return ResponseEntity containing Result with validation error details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getMessage();
        log.warn("Constraint violation: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(400, message));
    }

    /**
     * Handle generic Exception (unexpected errors).
     *
     * @param e the Exception
     * @return ResponseEntity containing Result with internal server error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("Unexpected error occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500, e.getMessage()));
    }

    /**
     * Map error code string to int code.
     *
     * @param code the string error code
     * @return the int error code
     */
    private int mapCodeToInt(String code) {
        if (code == null) {
            return 500;
        }

        return switch (code) {
            case "BUSINESS_ERROR", "VALIDATION_ERROR" -> 400;
            case "TENANT_ERROR" -> 403;
            case "SYSTEM_ERROR", "INTERNAL_ERROR" -> 500;
            default -> 500;
        };
    }

    /**
     * Map error code to HTTP status.
     *
     * @param code the error code
     * @return the corresponding HTTP status
     */
    private HttpStatus mapCodeToHttpStatus(String code) {
        if (code == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return switch (code) {
            case "BUSINESS_ERROR", "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "TENANT_ERROR" -> HttpStatus.FORBIDDEN;
            case "SYSTEM_ERROR", "INTERNAL_ERROR" -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
