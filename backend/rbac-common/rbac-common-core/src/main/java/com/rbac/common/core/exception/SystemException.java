package com.rbac.common.core.exception;

/**
 * Exception thrown for system-level errors.
 *
 * This exception is used when system components fail, such as database connections,
 * external service calls, file system operations, or configuration issues.
 * These are typically not recoverable by user action and require system administrator intervention.
 *
 * @author RBAC System
 * @since 1.0.0
 */
public class SystemException extends RbacException {

    /**
     * Constructor with message (defaults to SYSTEM_ERROR).
     *
     * @param message the error message
     */
    public SystemException(String message) {
        super("SYSTEM_ERROR", message);
    }

    /**
     * Constructor with code and message.
     *
     * @param code the error code
     * @param message the error message
     */
    public SystemException(String code, String message) {
        super(code, message);
    }

    /**
     * Constructor with code, message, and cause.
     *
     * @param code the error code
     * @param message the error message
     * @param cause the cause of this exception
     */
    public SystemException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}