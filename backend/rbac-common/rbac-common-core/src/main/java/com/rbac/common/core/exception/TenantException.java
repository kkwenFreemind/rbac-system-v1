package com.rbac.common.core.exception;

import com.rbac.common.core.constant.ErrorCode;

/**
 * Exception thrown for tenant-related errors.
 *
 * This exception is used when tenant isolation or tenant-specific operations fail,
 * such as missing tenant context, tenant not found, tenant suspended, etc.
 * These errors are critical for multi-tenant system security and data isolation.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
public class TenantException extends RbacException {

    /**
     * Constructor with error code and message.
     *
     * @param errorCode the tenant error code
     * @param message the error message
     */
    public TenantException(int errorCode, String message) {
        super(errorCode, message);
        validateErrorCode(errorCode);
    }

    /**
     * Constructor with error code, message, and cause.
     *
     * @param errorCode the tenant error code
     * @param message the error message
     * @param cause the cause of this exception
     */
    public TenantException(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
        validateErrorCode(errorCode);
    }

    /**
     * Constructor with message only (defaults to missing tenant context).
     *
     * @param message the error message
     */
    public TenantException(String message) {
        super(ErrorCode.MISSING_TENANT_CONTEXT, message);
    }

    /**
     * Constructor with message and cause (defaults to missing tenant context).
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public TenantException(String message, Throwable cause) {
        super(ErrorCode.MISSING_TENANT_CONTEXT, message, cause);
    }

    /**
     * Validate that the error code is within the tenant error range.
     *
     * @param errorCode the error code to validate
     * @throws IllegalArgumentException if the error code is not a tenant error
     */
    private void validateErrorCode(int errorCode) {
        if (!ErrorCode.isTenantError(errorCode)) {
            throw new IllegalArgumentException(
                "TenantException must use tenant error codes (2000-2999). " +
                "Provided code: " + errorCode + " is not a tenant error code."
            );
        }
    }

    /**
     * Create a TenantException for tenant not found.
     *
     * @param tenantId the tenant ID that was not found
     * @return TenantException instance
     */
    public static TenantException tenantNotFound(String tenantId) {
        return new TenantException(ErrorCode.TENANT_NOT_FOUND,
            "Tenant not found with ID: " + tenantId);
    }

    /**
     * Create a TenantException for tenant already exists.
     *
     * @param tenantId the tenant ID that already exists
     * @return TenantException instance
     */
    public static TenantException tenantAlreadyExists(String tenantId) {
        return new TenantException(ErrorCode.TENANT_ALREADY_EXISTS,
            "Tenant already exists with ID: " + tenantId);
    }

    /**
     * Create a TenantException for tenant suspended.
     *
     * @param tenantId the suspended tenant ID
     * @return TenantException instance
     */
    public static TenantException tenantSuspended(String tenantId) {
        return new TenantException(ErrorCode.TENANT_SUSPENDED,
            "Tenant is suspended: " + tenantId);
    }

    /**
     * Create a TenantException for tenant limit exceeded.
     *
     * @param tenantId the tenant ID that exceeded limits
     * @param limitType the type of limit exceeded (users, storage, etc.)
     * @return TenantException instance
     */
    public static TenantException tenantLimitExceeded(String tenantId, String limitType) {
        return new TenantException(ErrorCode.TENANT_LIMIT_EXCEEDED,
            "Tenant '" + tenantId + "' has exceeded " + limitType + " limit");
    }

    /**
     * Create a TenantException for missing tenant context.
     *
     * @return TenantException instance
     */
    public static TenantException missingTenantContext() {
        return new TenantException(ErrorCode.MISSING_TENANT_CONTEXT,
            "Tenant context is missing from current request");
    }

    /**
     * Create a TenantException for missing tenant context with operation details.
     *
     * @param operation the operation that requires tenant context
     * @return TenantException instance
     */
    public static TenantException missingTenantContext(String operation) {
        return new TenantException(ErrorCode.MISSING_TENANT_CONTEXT,
            "Tenant context is required for operation: " + operation);
    }

    /**
     * Create a TenantException for invalid tenant ID.
     *
     * @param tenantId the invalid tenant ID
     * @return TenantException instance
     */
    public static TenantException invalidTenantId(String tenantId) {
        return new TenantException(ErrorCode.INVALID_TENANT_ID,
            "Invalid tenant ID format: " + tenantId);
    }

    /**
     * Create a TenantException for invalid tenant ID with validation details.
     *
     * @param tenantId the invalid tenant ID
     * @param reason the reason why it's invalid
     * @return TenantException instance
     */
    public static TenantException invalidTenantId(String tenantId, String reason) {
        return new TenantException(ErrorCode.INVALID_TENANT_ID,
            "Invalid tenant ID '" + tenantId + "': " + reason);
    }
}