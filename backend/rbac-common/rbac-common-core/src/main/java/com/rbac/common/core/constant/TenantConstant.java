package com.rbac.common.core.constant;

/**
 * Tenant-related constants used across the RBAC system.
 *
 * This class contains constants for tenant isolation, tenant states,
 * tenant types, and other tenant-specific values used throughout the system.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
public final class TenantConstant {

    /**
     * Private constructor to prevent instantiation.
     */
    private TenantConstant() {
        throw new UnsupportedOperationException("TenantConstant class cannot be instantiated");
    }

    // ==================== Tenant ID Constants ====================

    /**
     * Default tenant ID for system operations.
     */
    public static final String DEFAULT_TENANT_ID = "default";

    /**
     * System tenant ID for internal operations.
     */
    public static final String SYSTEM_TENANT_ID = "system";

    /**
     * Maximum length of tenant ID.
     */
    public static final int MAX_TENANT_ID_LENGTH = 50;

    /**
     * Minimum length of tenant ID.
     */
    public static final int MIN_TENANT_ID_LENGTH = 3;

    // ==================== Tenant Status Constants ====================

    /**
     * Tenant status: Active.
     */
    public static final int TENANT_STATUS_ACTIVE = 1;

    /**
     * Tenant status: Inactive.
     */
    public static final int TENANT_STATUS_INACTIVE = 0;

    /**
     * Tenant status: Suspended.
     */
    public static final int TENANT_STATUS_SUSPENDED = 2;

    /**
     * Tenant status: Deleted.
     */
    public static final int TENANT_STATUS_DELETED = 3;

    // ==================== Tenant Type Constants ====================

    /**
     * Tenant type: Standard.
     */
    public static final String TENANT_TYPE_STANDARD = "standard";

    /**
     * Tenant type: Premium.
     */
    public static final String TENANT_TYPE_PREMIUM = "premium";

    /**
     * Tenant type: Enterprise.
     */
    public static final String TENANT_TYPE_ENTERPRISE = "enterprise";

    /**
     * Tenant type: Trial.
     */
    public static final String TENANT_TYPE_TRIAL = "trial";

    // ==================== Tenant Header Constants ====================

    /**
     * HTTP header name for tenant identification.
     */
    public static final String TENANT_HEADER_NAME = "X-Tenant-Id";

    /**
     * Alternative HTTP header name for tenant identification.
     */
    public static final String TENANT_HEADER_NAME_ALT = "X-TenantID";

    // ==================== Tenant Context Constants ====================

    /**
     * ThreadLocal key for tenant context.
     */
    public static final String TENANT_CONTEXT_KEY = "TENANT_CONTEXT";

    /**
     * Default tenant isolation level.
     */
    public static final String DEFAULT_ISOLATION_LEVEL = "tenant";

    // ==================== Tenant Validation Constants ====================

    /**
     * Maximum number of users per tenant (standard plan).
     */
    public static final int MAX_USERS_STANDARD = 100;

    /**
     * Maximum number of users per tenant (premium plan).
     */
    public static final int MAX_USERS_PREMIUM = 1000;

    /**
     * Maximum number of users per tenant (enterprise plan).
     */
    public static final int MAX_USERS_ENTERPRISE = 10000;

    /**
     * Maximum number of roles per tenant.
     */
    public static final int MAX_ROLES_PER_TENANT = 100;

    /**
     * Maximum number of permissions per role.
     */
    public static final int MAX_PERMISSIONS_PER_ROLE = 200;

    // ==================== Tenant Database Constants ====================

    /**
     * Database column name for tenant ID.
     */
    public static final String TENANT_ID_COLUMN = "tenant_id";

    /**
     * Database table prefix for tenant-specific tables.
     */
    public static final String TENANT_TABLE_PREFIX = "t_";

    /**
     * Database schema name for tenant data.
     */
    public static final String TENANT_SCHEMA_NAME = "tenant";

    // ==================== Tenant Cache Constants ====================

    /**
     * Cache key prefix for tenant data.
     */
    public static final String TENANT_CACHE_PREFIX = "tenant:";

    /**
     * Cache key for tenant information.
     */
    public static final String TENANT_INFO_CACHE_KEY = TENANT_CACHE_PREFIX + "info:";

    /**
     * Cache key for tenant configuration.
     */
    public static final String TENANT_CONFIG_CACHE_KEY = TENANT_CACHE_PREFIX + "config:";

    /**
     * Cache TTL for tenant data (1 hour).
     */
    public static final int TENANT_CACHE_TTL = 3600;

    // ==================== Tenant Audit Constants ====================

    /**
     * Audit event type for tenant creation.
     */
    public static final String AUDIT_TENANT_CREATED = "TENANT_CREATED";

    /**
     * Audit event type for tenant update.
     */
    public static final String AUDIT_TENANT_UPDATED = "TENANT_UPDATED";

    /**
     * Audit event type for tenant deletion.
     */
    public static final String AUDIT_TENANT_DELETED = "TENANT_DELETED";

    /**
     * Audit event type for tenant suspension.
     */
    public static final String AUDIT_TENANT_SUSPENDED = "TENANT_SUSPENDED";

    // ==================== Tenant Error Messages ====================

    /**
     * Error message when tenant is not found.
     */
    public static final String ERR_TENANT_NOT_FOUND = "Tenant not found";

    /**
     * Error message when tenant is suspended.
     */
    public static final String ERR_TENANT_SUSPENDED = "Tenant is suspended";

    /**
     * Error message when tenant limit exceeded.
     */
    public static final String ERR_TENANT_LIMIT_EXCEEDED = "Tenant limit exceeded";

    /**
     * Error message when tenant context is missing.
     */
    public static final String ERR_MISSING_TENANT_CONTEXT = "Missing tenant context";

    /**
     * Error message when invalid tenant ID.
     */
    public static final String ERR_INVALID_TENANT_ID = "Invalid tenant ID";
}