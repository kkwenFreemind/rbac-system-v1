package com.rbac.common.core.constant;

/**
 * Error codes used across the RBAC system.
 *
 * This class defines standardized error codes for different types of errors
 * including system errors, business errors, validation errors, and security errors.
 * All error codes follow a consistent format and are unique across the system.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
public final class ErrorCode {

    /**
     * Private constructor to prevent instantiation.
     */
    private ErrorCode() {
        throw new UnsupportedOperationException("ErrorCode class cannot be instantiated");
    }

    // ==================== Success Codes ====================

    /**
     * Operation completed successfully.
     */
    public static final int SUCCESS = 200;

    /**
     * Operation completed successfully with no content.
     */
    public static final int SUCCESS_NO_CONTENT = 204;

    // ==================== Client Error Codes (4xx) ====================

    /**
     * Bad request - invalid input parameters.
     */
    public static final int BAD_REQUEST = 400;

    /**
     * Unauthorized - authentication required.
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * Forbidden - insufficient permissions.
     */
    public static final int FORBIDDEN = 403;

    /**
     * Not found - resource does not exist.
     */
    public static final int NOT_FOUND = 404;

    /**
     * Method not allowed.
     */
    public static final int METHOD_NOT_ALLOWED = 405;

    /**
     * Conflict - resource already exists or state conflict.
     */
    public static final int CONFLICT = 409;

    /**
     * Unprocessable entity - validation failed.
     */
    public static final int UNPROCESSABLE_ENTITY = 422;

    /**
     * Too many requests - rate limit exceeded.
     */
    public static final int TOO_MANY_REQUESTS = 429;

    // ==================== Server Error Codes (5xx) ====================

    /**
     * Internal server error.
     */
    public static final int INTERNAL_SERVER_ERROR = 500;

    /**
     * Service unavailable.
     */
    public static final int SERVICE_UNAVAILABLE = 503;

    /**
     * Gateway timeout.
     */
    public static final int GATEWAY_TIMEOUT = 504;

    // ==================== Business Error Codes (1000-1999) ====================

    /**
     * User not found.
     */
    public static final int USER_NOT_FOUND = 1001;

    /**
     * User already exists.
     */
    public static final int USER_ALREADY_EXISTS = 1002;

    /**
     * Invalid username or password.
     */
    public static final int INVALID_CREDENTIALS = 1003;

    /**
     * User account is disabled.
     */
    public static final int USER_DISABLED = 1004;

    /**
     * User account is locked.
     */
    public static final int USER_LOCKED = 1005;

    /**
     * Password expired.
     */
    public static final int PASSWORD_EXPIRED = 1006;

    // ==================== Tenant Error Codes (2000-2999) ====================

    /**
     * Tenant not found.
     */
    public static final int TENANT_NOT_FOUND = 2001;

    /**
     * Tenant already exists.
     */
    public static final int TENANT_ALREADY_EXISTS = 2002;

    /**
     * Tenant is suspended.
     */
    public static final int TENANT_SUSPENDED = 2003;

    /**
     * Tenant limit exceeded.
     */
    public static final int TENANT_LIMIT_EXCEEDED = 2004;

    /**
     * Missing tenant context.
     */
    public static final int MISSING_TENANT_CONTEXT = 2005;

    /**
     * Invalid tenant ID.
     */
    public static final int INVALID_TENANT_ID = 2006;

    // ==================== Permission Error Codes (3000-3999) ====================

    /**
     * Permission denied.
     */
    public static final int PERMISSION_DENIED = 3001;

    /**
     * Role not found.
     */
    public static final int ROLE_NOT_FOUND = 3002;

    /**
     * Role already exists.
     */
    public static final int ROLE_ALREADY_EXISTS = 3003;

    /**
     * Insufficient permissions.
     */
    public static final int INSUFFICIENT_PERMISSIONS = 3004;

    /**
     * Permission not found.
     */
    public static final int PERMISSION_NOT_FOUND = 3005;

    // ==================== Validation Error Codes (4000-4999) ====================

    /**
     * Invalid input parameters.
     */
    public static final int INVALID_INPUT = 4001;

    /**
     * Parameter validation failed.
     */
    public static final int PARAMETER_INVALID = 4002;

    /**
     * Required field is missing.
     */
    public static final int REQUIRED_FIELD_MISSING = 4002;

    /**
     * Invalid format.
     */
    public static final int INVALID_FORMAT = 4003;

    /**
     * Value out of range.
     */
    public static final int VALUE_OUT_OF_RANGE = 4004;

    /**
     * Invalid length.
     */
    public static final int INVALID_LENGTH = 4005;

    // ==================== System Error Codes (5000-5999) ====================

    /**
     * Database connection error.
     */
    public static final int DATABASE_ERROR = 5001;

    /**
     * Redis connection error.
     */
    public static final int REDIS_ERROR = 5002;

    /**
     * External service error.
     */
    public static final int EXTERNAL_SERVICE_ERROR = 5003;

    /**
     * Configuration error.
     */
    public static final int CONFIGURATION_ERROR = 5004;

    /**
     * File system error.
     */
    public static final int FILE_SYSTEM_ERROR = 5005;

    // ==================== Security Error Codes (6000-6999) ====================

    /**
     * Invalid token.
     */
    public static final int INVALID_TOKEN = 6001;

    /**
     * Token expired.
     */
    public static final int TOKEN_EXPIRED = 6002;

    /**
     * Invalid API key.
     */
    public static final int INVALID_API_KEY = 6003;

    /**
     * Suspicious activity detected.
     */
    public static final int SUSPICIOUS_ACTIVITY = 6004;

    /**
     * IP blocked.
     */
    public static final int IP_BLOCKED = 6005;

    // ==================== Cache Error Codes (7000-7999) ====================

    /**
     * Cache operation failed.
     */
    public static final int CACHE_ERROR = 7001;

    /**
     * Cache key not found.
     */
    public static final int CACHE_KEY_NOT_FOUND = 7002;

    /**
     * Cache serialization error.
     */
    public static final int CACHE_SERIALIZATION_ERROR = 7003;

    // ==================== Lock Error Codes (8000-8999) ====================

    /**
     * Lock acquisition failed.
     */
    public static final int LOCK_ACQUISITION_FAILED = 8001;

    /**
     * Lock timeout.
     */
    public static final int LOCK_TIMEOUT = 8002;

    /**
     * Lock already held.
     */
    public static final int LOCK_ALREADY_HELD = 8003;

    // ==================== Audit Error Codes (9000-9999) ====================

    /**
     * Audit logging failed.
     */
    public static final int AUDIT_LOGGING_FAILED = 9001;

    /**
     * Audit data corrupted.
     */
    public static final int AUDIT_DATA_CORRUPTED = 9002;

    // ==================== Error Code Ranges ====================

    /**
     * Minimum business error code.
     */
    public static final int BUSINESS_ERROR_MIN = 1000;

    /**
     * Maximum business error code.
     */
    public static final int BUSINESS_ERROR_MAX = 1999;

    /**
     * Minimum tenant error code.
     */
    public static final int TENANT_ERROR_MIN = 2000;

    /**
     * Maximum tenant error code.
     */
    public static final int TENANT_ERROR_MAX = 2999;

    /**
     * Minimum permission error code.
     */
    public static final int PERMISSION_ERROR_MIN = 3000;

    /**
     * Maximum permission error code.
     */
    public static final int PERMISSION_ERROR_MAX = 3999;

    /**
     * Minimum validation error code.
     */
    public static final int VALIDATION_ERROR_MIN = 4000;

    /**
     * Maximum validation error code.
     */
    public static final int VALIDATION_ERROR_MAX = 4999;

    /**
     * Minimum system error code.
     */
    public static final int SYSTEM_ERROR_MIN = 5000;

    /**
     * Maximum system error code.
     */
    public static final int SYSTEM_ERROR_MAX = 5999;

    /**
     * Minimum security error code.
     */
    public static final int SECURITY_ERROR_MIN = 6000;

    /**
     * Maximum security error code.
     */
    public static final int SECURITY_ERROR_MAX = 6999;

    /**
     * Minimum cache error code.
     */
    public static final int CACHE_ERROR_MIN = 7000;

    /**
     * Maximum cache error code.
     */
    public static final int CACHE_ERROR_MAX = 7999;

    /**
     * Minimum lock error code.
     */
    public static final int LOCK_ERROR_MIN = 8000;

    /**
     * Maximum lock error code.
     */
    public static final int LOCK_ERROR_MAX = 8999;

    /**
     * Minimum audit error code.
     */
    public static final int AUDIT_ERROR_MIN = 9000;

    /**
     * Maximum audit error code.
     */
    public static final int AUDIT_ERROR_MAX = 9999;

    // ==================== Utility Methods ====================

    /**
     * Check if the error code is a business error.
     *
     * @param errorCode the error code to check
     * @return true if it's a business error, false otherwise
     */
    public static boolean isBusinessError(int errorCode) {
        return errorCode >= BUSINESS_ERROR_MIN && errorCode <= BUSINESS_ERROR_MAX;
    }

    /**
     * Check if the error code is a tenant error.
     *
     * @param errorCode the error code to check
     * @return true if it's a tenant error, false otherwise
     */
    public static boolean isTenantError(int errorCode) {
        return errorCode >= TENANT_ERROR_MIN && errorCode <= TENANT_ERROR_MAX;
    }

    /**
     * Check if the error code is a permission error.
     *
     * @param errorCode the error code to check
     * @return true if it's a permission error, false otherwise
     */
    public static boolean isPermissionError(int errorCode) {
        return errorCode >= PERMISSION_ERROR_MIN && errorCode <= PERMISSION_ERROR_MAX;
    }

    /**
     * Check if the error code is a validation error.
     *
     * @param errorCode the error code to check
     * @return true if it's a validation error, false otherwise
     */
    public static boolean isValidationError(int errorCode) {
        return errorCode >= VALIDATION_ERROR_MIN && errorCode <= VALIDATION_ERROR_MAX;
    }

    /**
     * Check if the error code is a system error.
     *
     * @param errorCode the error code to check
     * @return true if it's a system error, false otherwise
     */
    public static boolean isSystemError(int errorCode) {
        return errorCode >= SYSTEM_ERROR_MIN && errorCode <= SYSTEM_ERROR_MAX;
    }

    /**
     * Check if the error code is a security error.
     *
     * @param errorCode the error code to check
     * @return true if it's a security error, false otherwise
     */
    public static boolean isSecurityError(int errorCode) {
        return errorCode >= SECURITY_ERROR_MIN && errorCode <= SECURITY_ERROR_MAX;
    }

    /**
     * Get the error category name for the given error code.
     *
     * @param errorCode the error code
     * @return the category name
     */
    public static String getErrorCategory(int errorCode) {
        if (errorCode >= BUSINESS_ERROR_MIN && errorCode <= BUSINESS_ERROR_MAX) {
            return "BUSINESS_ERROR";
        } else if (errorCode >= TENANT_ERROR_MIN && errorCode <= TENANT_ERROR_MAX) {
            return "TENANT_ERROR";
        } else if (errorCode >= PERMISSION_ERROR_MIN && errorCode <= PERMISSION_ERROR_MAX) {
            return "PERMISSION_ERROR";
        } else if (errorCode >= VALIDATION_ERROR_MIN && errorCode <= VALIDATION_ERROR_MAX) {
            return "VALIDATION_ERROR";
        } else if (errorCode >= SYSTEM_ERROR_MIN && errorCode <= SYSTEM_ERROR_MAX) {
            return "SYSTEM_ERROR";
        } else if (errorCode >= SECURITY_ERROR_MIN && errorCode <= SECURITY_ERROR_MAX) {
            return "SECURITY_ERROR";
        } else if (errorCode >= CACHE_ERROR_MIN && errorCode <= CACHE_ERROR_MAX) {
            return "CACHE_ERROR";
        } else if (errorCode >= LOCK_ERROR_MIN && errorCode <= LOCK_ERROR_MAX) {
            return "LOCK_ERROR";
        } else if (errorCode >= AUDIT_ERROR_MIN && errorCode <= AUDIT_ERROR_MAX) {
            return "AUDIT_ERROR";
        } else {
            return "UNKNOWN_ERROR";
        }
    }
}