package com.rbac.common.core.exception;

/**
 * Exception thrown when permission is denied.
 *
 * This exception is used when a user attempts to perform an operation
 * they don't have permission for, or when authorization checks fail.
 * This is a critical security exception that indicates access control violations.
 *
 * @author RBAC System
 * @since 1.0.0
 */
public class PermissionDeniedException extends BusinessException {

    /**
     * Constructor with message (defaults to PERMISSION_DENIED).
     *
     * @param message the error message
     */
    public PermissionDeniedException(String message) {
        super("PERMISSION_DENIED", message);
    }

    /**
     * Constructor with code and message.
     *
     * @param code the error code
     * @param message the error message
     */
    public PermissionDeniedException(String code, String message) {
        super(code, message);
    }

    /**
     * Constructor with code, message, and cause.
     *
     * @param code the error code
     * @param message the error message
     * @param cause the cause of this exception
     */
    public PermissionDeniedException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    /**
     * Create a PermissionDeniedException for general permission denied.
     *
     * @param userId the user ID that was denied access
     * @param resource the resource being accessed
     * @param action the action being performed
     * @return PermissionDeniedException instance
     */
    public static PermissionDeniedException accessDenied(String userId, String resource, String action) {
        return new PermissionDeniedException("PERMISSION_DENIED",
            "User '" + userId + "' does not have permission to '" + action + "' resource '" + resource + "'");
    }

    /**
     * Create a PermissionDeniedException for insufficient permissions.
     *
     * @param userId the user ID with insufficient permissions
     * @param requiredPermission the required permission
     * @return PermissionDeniedException instance
     */
    public static PermissionDeniedException insufficientPermissions(String userId, String requiredPermission) {
        return new PermissionDeniedException("INSUFFICIENT_PERMISSIONS",
            "User '" + userId + "' has insufficient permissions. Required: " + requiredPermission);
    }

    /**
     * Create a PermissionDeniedException for role not found.
     *
     * @param roleName the role name that was not found
     * @return PermissionDeniedException instance
     */
    public static PermissionDeniedException roleNotFound(String roleName) {
        return new PermissionDeniedException("ROLE_NOT_FOUND",
            "Role not found: " + roleName);
    }

    /**
     * Create a PermissionDeniedException for role already exists.
     *
     * @param roleName the role name that already exists
     * @return PermissionDeniedException instance
     */
    public static PermissionDeniedException roleAlreadyExists(String roleName) {
        return new PermissionDeniedException("ROLE_ALREADY_EXISTS",
            "Role already exists: " + roleName);
    }

    /**
     * Create a PermissionDeniedException for permission not found.
     *
     * @param permissionName the permission name that was not found
     * @return PermissionDeniedException instance
     */
    public static PermissionDeniedException permissionNotFound(String permissionName) {
        return new PermissionDeniedException("PERMISSION_NOT_FOUND",
            "Permission not found: " + permissionName);
    }

    /**
     * Create a PermissionDeniedException for forbidden access.
     *
     * @param userId the user ID that was forbidden
     * @param reason the reason for forbidding access
     * @return PermissionDeniedException instance
     */
    public static PermissionDeniedException forbidden(String userId, String reason) {
        return new PermissionDeniedException("FORBIDDEN",
            "Access forbidden for user '" + userId + "': " + reason);
    }

    /**
     * Create a PermissionDeniedException for unauthorized access.
     *
     * @param reason the reason for unauthorized access
     * @return PermissionDeniedException instance
     */
    public static PermissionDeniedException unauthorized(String reason) {
        return new PermissionDeniedException("UNAUTHORIZED",
            "Unauthorized access: " + reason);
    }
}