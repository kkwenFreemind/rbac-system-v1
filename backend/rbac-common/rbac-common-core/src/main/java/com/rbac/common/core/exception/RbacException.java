package com.rbac.common.core.exception;

import lombok.Getter;

/**
 * Base exception class for RBAC system.
 *
 * This is the root exception class for all custom exceptions in the RBAC system.
 * It provides a standardized way to handle errors with error codes.
 *
 * @author RBAC System
 * @since 1.0.0
 */
@Getter
public class RbacException extends RuntimeException {

    /**
     * The error code associated with this exception.
     */
    private final String code;

    /**
     * Constructor with code and message.
     *
     * @param code the error code
     * @param message the error message
     */
    public RbacException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Constructor with code, message, and cause.
     *
     * @param code the error code
     * @param message the error message
     * @param cause the cause of this exception
     */
    public RbacException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}