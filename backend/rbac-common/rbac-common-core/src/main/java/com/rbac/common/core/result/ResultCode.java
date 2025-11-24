package com.rbac.common.core.result;

import com.rbac.common.core.constant.ErrorCode;

/**
 * Standardized result codes for API responses.
 *
 * This enum defines all possible result codes that can be returned in API responses,
 * providing a consistent way to communicate operation outcomes across the system.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
public enum ResultCode {

    // ==================== Success Codes ====================

    /**
     * Operation completed successfully.
     */
    SUCCESS(ErrorCode.SUCCESS, "Operation completed successfully"),

    /**
     * Operation completed successfully with no content.
     */
    SUCCESS_NO_CONTENT(ErrorCode.SUCCESS_NO_CONTENT, "Operation completed successfully with no content"),

    // ==================== Client Error Codes ====================

    /**
     * Bad request - invalid input parameters.
     */
    BAD_REQUEST(ErrorCode.BAD_REQUEST, "Bad request - invalid input parameters"),

    /**
     * Unauthorized - authentication required.
     */
    UNAUTHORIZED(ErrorCode.UNAUTHORIZED, "Unauthorized - authentication required"),

    /**
     * Forbidden - insufficient permissions.
     */
    FORBIDDEN(ErrorCode.FORBIDDEN, "Forbidden - insufficient permissions"),

    /**
     * Not found - resource does not exist.
     */
    NOT_FOUND(ErrorCode.NOT_FOUND, "Not found - resource does not exist"),

    /**
     * Method not allowed.
     */
    METHOD_NOT_ALLOWED(ErrorCode.METHOD_NOT_ALLOWED, "Method not allowed"),

    /**
     * Conflict - resource already exists or state conflict.
     */
    CONFLICT(ErrorCode.CONFLICT, "Conflict - resource already exists or state conflict"),

    /**
     * Unprocessable entity - validation failed.
     */
    UNPROCESSABLE_ENTITY(ErrorCode.UNPROCESSABLE_ENTITY, "Unprocessable entity - validation failed"),

    /**
     * Too many requests - rate limit exceeded.
     */
    TOO_MANY_REQUESTS(ErrorCode.TOO_MANY_REQUESTS, "Too many requests - rate limit exceeded"),

    // ==================== Server Error Codes ====================

    /**
     * Internal server error.
     */
    INTERNAL_SERVER_ERROR(ErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"),

    /**
     * Service unavailable.
     */
    SERVICE_UNAVAILABLE(ErrorCode.SERVICE_UNAVAILABLE, "Service unavailable"),

    /**
     * Gateway timeout.
     */
    GATEWAY_TIMEOUT(ErrorCode.GATEWAY_TIMEOUT, "Gateway timeout"),

    // ==================== Business Error Codes ====================

    /**
     * User not found.
     */
    USER_NOT_FOUND(ErrorCode.USER_NOT_FOUND, "User not found"),

    /**
     * User already exists.
     */
    USER_ALREADY_EXISTS(ErrorCode.USER_ALREADY_EXISTS, "User already exists"),

    /**
     * Invalid username or password.
     */
    INVALID_CREDENTIALS(ErrorCode.INVALID_CREDENTIALS, "Invalid username or password"),

    /**
     * User account is disabled.
     */
    USER_DISABLED(ErrorCode.USER_DISABLED, "User account is disabled"),

    /**
     * User account is locked.
     */
    USER_LOCKED(ErrorCode.USER_LOCKED, "User account is locked"),

    /**
     * Password expired.
     */
    PASSWORD_EXPIRED(ErrorCode.PASSWORD_EXPIRED, "Password expired"),

    // ==================== Tenant Error Codes ====================

    /**
     * Tenant not found.
     */
    TENANT_NOT_FOUND(ErrorCode.TENANT_NOT_FOUND, "Tenant not found"),

    /**
     * Tenant already exists.
     */
    TENANT_ALREADY_EXISTS(ErrorCode.TENANT_ALREADY_EXISTS, "Tenant already exists"),

    /**
     * Tenant is suspended.
     */
    TENANT_SUSPENDED(ErrorCode.TENANT_SUSPENDED, "Tenant is suspended"),

    /**
     * Tenant limit exceeded.
     */
    TENANT_LIMIT_EXCEEDED(ErrorCode.TENANT_LIMIT_EXCEEDED, "Tenant limit exceeded"),

    /**
     * Missing tenant context.
     */
    MISSING_TENANT_CONTEXT(ErrorCode.MISSING_TENANT_CONTEXT, "Missing tenant context"),

    /**
     * Invalid tenant ID.
     */
    INVALID_TENANT_ID(ErrorCode.INVALID_TENANT_ID, "Invalid tenant ID"),

    // ==================== Permission Error Codes ====================

    /**
     * Permission denied.
     */
    PERMISSION_DENIED(ErrorCode.PERMISSION_DENIED, "Permission denied"),

    /**
     * Role not found.
     */
    ROLE_NOT_FOUND(ErrorCode.ROLE_NOT_FOUND, "Role not found"),

    /**
     * Role already exists.
     */
    ROLE_ALREADY_EXISTS(ErrorCode.ROLE_ALREADY_EXISTS, "Role already exists"),

    /**
     * Insufficient permissions.
     */
    INSUFFICIENT_PERMISSIONS(ErrorCode.INSUFFICIENT_PERMISSIONS, "Insufficient permissions"),

    /**
     * Permission not found.
     */
    PERMISSION_NOT_FOUND(ErrorCode.PERMISSION_NOT_FOUND, "Permission not found"),

    // ==================== Validation Error Codes ====================

    /**
     * Invalid input parameters.
     */
    INVALID_INPUT(ErrorCode.INVALID_INPUT, "Invalid input parameters"),

    /**
     * Required field is missing.
     */
    REQUIRED_FIELD_MISSING(ErrorCode.REQUIRED_FIELD_MISSING, "Required field is missing"),

    /**
     * Invalid format.
     */
    INVALID_FORMAT(ErrorCode.INVALID_FORMAT, "Invalid format"),

    /**
     * Value out of range.
     */
    VALUE_OUT_OF_RANGE(ErrorCode.VALUE_OUT_OF_RANGE, "Value out of range"),

    /**
     * Invalid length.
     */
    INVALID_LENGTH(ErrorCode.INVALID_LENGTH, "Invalid length"),

    // ==================== System Error Codes ====================

    /**
     * Database connection error.
     */
    DATABASE_ERROR(ErrorCode.DATABASE_ERROR, "Database connection error"),

    /**
     * Redis connection error.
     */
    REDIS_ERROR(ErrorCode.REDIS_ERROR, "Redis connection error"),

    /**
     * External service error.
     */
    EXTERNAL_SERVICE_ERROR(ErrorCode.EXTERNAL_SERVICE_ERROR, "External service error"),

    /**
     * Configuration error.
     */
    CONFIGURATION_ERROR(ErrorCode.CONFIGURATION_ERROR, "Configuration error"),

    /**
     * File system error.
     */
    FILE_SYSTEM_ERROR(ErrorCode.FILE_SYSTEM_ERROR, "File system error"),

    // ==================== Security Error Codes ====================

    /**
     * Invalid token.
     */
    INVALID_TOKEN(ErrorCode.INVALID_TOKEN, "Invalid token"),

    /**
     * Token expired.
     */
    TOKEN_EXPIRED(ErrorCode.TOKEN_EXPIRED, "Token expired"),

    /**
     * Invalid API key.
     */
    INVALID_API_KEY(ErrorCode.INVALID_API_KEY, "Invalid API key"),

    /**
     * Suspicious activity detected.
     */
    SUSPICIOUS_ACTIVITY(ErrorCode.SUSPICIOUS_ACTIVITY, "Suspicious activity detected"),

    /**
     * IP blocked.
     */
    IP_BLOCKED(ErrorCode.IP_BLOCKED, "IP blocked"),

    // ==================== Cache Error Codes ====================

    /**
     * Cache operation failed.
     */
    CACHE_ERROR(ErrorCode.CACHE_ERROR, "Cache operation failed"),

    /**
     * Cache key not found.
     */
    CACHE_KEY_NOT_FOUND(ErrorCode.CACHE_KEY_NOT_FOUND, "Cache key not found"),

    /**
     * Cache serialization error.
     */
    CACHE_SERIALIZATION_ERROR(ErrorCode.CACHE_SERIALIZATION_ERROR, "Cache serialization error"),

    // ==================== Lock Error Codes ====================

    /**
     * Lock acquisition failed.
     */
    LOCK_ACQUISITION_FAILED(ErrorCode.LOCK_ACQUISITION_FAILED, "Lock acquisition failed"),

    /**
     * Lock timeout.
     */
    LOCK_TIMEOUT(ErrorCode.LOCK_TIMEOUT, "Lock timeout"),

    /**
     * Lock already held.
     */
    LOCK_ALREADY_HELD(ErrorCode.LOCK_ALREADY_HELD, "Lock already held"),

    // ==================== Audit Error Codes ====================

    /**
     * Audit logging failed.
     */
    AUDIT_LOGGING_FAILED(ErrorCode.AUDIT_LOGGING_FAILED, "Audit logging failed"),

    /**
     * Audit data corrupted.
     */
    AUDIT_DATA_CORRUPTED(ErrorCode.AUDIT_DATA_CORRUPTED, "Audit data corrupted");

    // ==================== Fields ====================

    /**
     * The numeric error code.
     */
    private final int code;

    /**
     * The human-readable message.
     */
    private final String message;

    // ==================== Constructor ====================

    /**
     * Constructor for ResultCode enum.
     *
     * @param code the numeric error code
     * @param message the human-readable message
     */
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    // ==================== Getters ====================

    /**
     * Get the numeric error code.
     *
     * @return the error code
     */
    public int getCode() {
        return code;
    }

    /**
     * Get the human-readable message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    // ==================== Utility Methods ====================

    /**
     * Check if this result code represents a successful operation.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return code >= 200 && code < 300;
    }

    /**
     * Check if this result code represents a client error.
     *
     * @return true if client error, false otherwise
     */
    public boolean isClientError() {
        return code >= 400 && code < 500;
    }

    /**
     * Check if this result code represents a server error.
     *
     * @return true if server error, false otherwise
     */
    public boolean isServerError() {
        return code >= 500 && code < 600;
    }

    /**
     * Get the ResultCode enum value by error code.
     *
     * @param code the error code
     * @return the ResultCode enum value, or null if not found
     */
    public static ResultCode fromCode(int code) {
        for (ResultCode resultCode : values()) {
            if (resultCode.code == code) {
                return resultCode;
            }
        }
        return null;
    }

    /**
     * Get the error category for this result code.
     *
     * @return the error category
     */
    public String getCategory() {
        if (isSuccess()) {
            return "SUCCESS";
        } else if (isClientError()) {
            return "CLIENT_ERROR";
        } else if (isServerError()) {
            return "SERVER_ERROR";
        } else {
            return "UNKNOWN";
        }
    }
}