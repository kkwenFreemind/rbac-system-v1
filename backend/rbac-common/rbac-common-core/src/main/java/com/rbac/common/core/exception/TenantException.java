package com.rbac.common.core.exception;

/**
 * Exception thrown for tenant-related errors.
 *
 * This exception is used when tenant isolation or tenant-specific operations fail,
 * such as missing tenant context, tenant not found, tenant suspended, etc.
 * These errors are critical for multi-tenant system security and data isolation.
 *
 * @author RBAC System
 * @since 1.0.0
 */
public class TenantException extends RbacException {

    /**
     * Constructor with message (defaults to TENANT_ERROR).
     *
     * @param message the error message
     */
    public TenantException(String message) {
        super("TENANT_ERROR", message);
    }

    /**
     * Constructor with code and message.
     *
     * @param code the error code
     * @param message the error message
     */
    public TenantException(String code, String message) {
        super(code, message);
    }

    /**
     * Constructor with code, message, and cause.
     *
     * @param code the error code
     * @param message the error message
     * @param cause the cause of this exception
     */
    public TenantException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}