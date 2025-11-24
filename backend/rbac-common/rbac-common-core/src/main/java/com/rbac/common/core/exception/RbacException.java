package com.rbac.common.core.exception;

import com.rbac.common.core.constant.ErrorCode;

/**
 * Base exception class for RBAC system.
 *
 * This is the root exception class for all custom exceptions in the RBAC system.
 * It provides a standardized way to handle errors with error codes and messages.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
public class RbacException extends RuntimeException {

    /**
     * The error code associated with this exception.
     */
    private final int errorCode;

    /**
     * Constructor with error code and message.
     *
     * @param errorCode the error code
     * @param message the error message
     */
    public RbacException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructor with error code, message, and cause.
     *
     * @param errorCode the error code
     * @param message the error message
     * @param cause the cause of this exception
     */
    public RbacException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Constructor with message only (defaults to internal server error).
     *
     * @param message the error message
     */
    public RbacException(String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    }

    /**
     * Constructor with message and cause (defaults to internal server error).
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public RbacException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    }

    /**
     * Get the error code.
     *
     * @return the error code
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Get the error category based on the error code.
     *
     * @return the error category
     */
    public String getErrorCategory() {
        return ErrorCode.getErrorCategory(errorCode);
    }

    /**
     * Check if this is a business error.
     *
     * @return true if business error, false otherwise
     */
    public boolean isBusinessError() {
        return ErrorCode.isBusinessError(errorCode);
    }

    /**
     * Check if this is a tenant error.
     *
     * @return true if tenant error, false otherwise
     */
    public boolean isTenantError() {
        return ErrorCode.isTenantError(errorCode);
    }

    /**
     * Check if this is a permission error.
     *
     * @return true if permission error, false otherwise
     */
    public boolean isPermissionError() {
        return ErrorCode.isPermissionError(errorCode);
    }

    /**
     * Check if this is a validation error.
     *
     * @return true if validation error, false otherwise
     */
    public boolean isValidationError() {
        return ErrorCode.isValidationError(errorCode);
    }

    /**
     * Check if this is a system error.
     *
     * @return true if system error, false otherwise
     */
    public boolean isSystemError() {
        return ErrorCode.isSystemError(errorCode);
    }

    /**
     * Check if this is a security error.
     *
     * @return true if security error, false otherwise
     */
    public boolean isSecurityError() {
        return ErrorCode.isSecurityError(errorCode);
    }
}