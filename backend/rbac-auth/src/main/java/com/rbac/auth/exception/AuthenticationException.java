package com.rbac.auth.exception;

import com.rbac.common.core.exception.RbacException;

/**
 * Authentication exception for authentication-related errors.
 *
 * This exception is thrown when authentication fails or when authentication
 * context is not available.
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
public class AuthenticationException extends RbacException {

    /**
     * Error code for authentication exceptions.
     */
    private static final String ERROR_CODE = "401";

    /**
     * Constructor with message.
     *
     * @param message the error message
     */
    public AuthenticationException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Constructor with message and cause.
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public AuthenticationException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}