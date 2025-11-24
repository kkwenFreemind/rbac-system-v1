package com.rbac.common.core.context;

import com.rbac.common.core.model.AuditInfo;

/**
 * User context interface.
 *
 * This interface provides methods to access current user information
 * and tenant context. It decouples business logic from specific
 * authentication/authorization implementations.
 *
 * Implementations should be thread-safe and handle multi-tenant scenarios.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
public interface UserContext {

    /**
     * Get current user ID.
     *
     * @return user ID, or null if not authenticated
     */
    Long getCurrentUserId();

    /**
     * Get current username.
     *
     * @return username, or null if not authenticated
     */
    String getCurrentUsername();

    /**
     * Get current user display name.
     *
     * @return display name, or null if not authenticated
     */
    String getCurrentUserDisplayName();

    /**
     * Get current tenant ID.
     *
     * @return tenant ID, or null if not in tenant context
     */
    Long getCurrentTenantId();

    /**
     * Get current tenant code/name.
     *
     * @return tenant code, or null if not in tenant context
     */
    String getCurrentTenantCode();

    /**
     * Check if current user is authenticated.
     *
     * @return true if authenticated, false otherwise
     */
    boolean isAuthenticated();

    /**
     * Check if current user has a specific role.
     *
     * @param roleName the role name to check
     * @return true if user has the role, false otherwise
     */
    boolean hasRole(String roleName);

    /**
     * Check if current user has a specific permission.
     *
     * @param permission the permission to check
     * @return true if user has the permission, false otherwise
     */
    boolean hasPermission(String permission);

    /**
     * Check if current user is a system administrator.
     *
     * @return true if system admin, false otherwise
     */
    boolean isSystemAdmin();

    /**
     * Check if current user is a tenant administrator.
     *
     * @return true if tenant admin, false otherwise
     */
    boolean isTenantAdmin();

    /**
     * Get audit information for current user.
     *
     * @return audit info, or system audit info if not authenticated
     */
    AuditInfo getAuditInfo();

    /**
     * Get current user's preferred language/locale.
     *
     * @return language code (e.g., "en", "zh-CN"), or default if not set
     */
    String getCurrentLanguage();

    /**
     * Get current user's timezone.
     *
     * @return timezone ID, or system default if not set
     */
    String getCurrentTimezone();

    /**
     * Check if current tenant is active.
     *
     * @return true if tenant is active, false otherwise
     */
    boolean isTenantActive();

    /**
     * Get current user's session ID.
     *
     * @return session ID, or null if not in session
     */
    String getSessionId();

    /**
     * Get current request ID for tracing.
     *
     * @return request ID, or null if not available
     */
    String getRequestId();

    /**
     * Check if current context is valid (authenticated and tenant active).
     *
     * @return true if context is valid, false otherwise
     */
    boolean isValidContext();
}