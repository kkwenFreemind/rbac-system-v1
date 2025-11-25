package com.rbac.auth.exception;

import com.rbac.common.core.exception.RbacException;

/**
 * Token expired exception for JWT token expiration.
 *
 * This exception is thrown when a JWT token has expired and needs to be refreshed
 * or when the user needs to re-authenticate.
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
public class TokenExpiredException extends RbacException {

    /**
     * Error code for token expired exceptions.
     */
    private static final String ERROR_CODE = "401";

    /**
     * Constructor with message.
     *
     * @param message the error message
     */
    public TokenExpiredException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Constructor with message and cause.
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public TokenExpiredException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}