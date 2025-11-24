package com.rbac.common.core.exception;

/**
 * Exception thrown for business logic errors.
 *
 * This exception is used when a business rule is violated or when
 * an operation cannot be completed due to business constraints.
 * Examples include user not found, duplicate data, invalid state transitions, etc.
 *
 * @author RBAC System
 * @since 1.0.0
 */
public class BusinessException extends RbacException {

    /**
     * Constructor with message (defaults to BUSINESS_ERROR).
     *
     * @param message the error message
     */
    public BusinessException(String message) {
        super("BUSINESS_ERROR", message);
    }

    /**
     * Constructor with code and message.
     *
     * @param code the error code
     * @param message the error message
     */
    public BusinessException(String code, String message) {
        super(code, message);
    }

    /**
     * Constructor with code, message, and cause.
     *
     * @param code the error code
     * @param message the error message
     * @param cause the cause of this exception
     */
    public BusinessException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}