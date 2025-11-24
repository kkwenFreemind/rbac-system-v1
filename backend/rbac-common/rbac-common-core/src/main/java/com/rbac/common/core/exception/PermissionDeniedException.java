package com.rbac.common.core.exception;

import com.rbac.common.core.constant.ErrorCode;

/**
 * Exception thrown when permission is denied.
 *
 * This exception is used when a user attempts to perform an operation
 * they don't have permission for, or when authorization checks fail.
 * This is a critical security exception that indicates access control violations.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
public class PermissionDeniedException extends RbacException {

    /**
     * Constructor with error code and message.
     *
     * @param errorCode the permission error code
     * @param message the error message
     */
    public PermissionDeniedException(int errorCode, String message) {
        super(errorCode, message);
        validateErrorCode(errorCode);
    }

    /**
     * Constructor with error code, message, and cause.
     *
     * @param errorCode the permission error code
     * @param message the error message
     * @param cause the cause of this exception
     */
    public PermissionDeniedException(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
        validateErrorCode(errorCode);
    }

    /**
     * Constructor with message only (defaults to permission denied).
     *
     * @param message the error message
     */
    public PermissionDeniedException(String message) {
        super(ErrorCode.PERMISSION_DENIED, message);
    }

    /**
     * Constructor with message and cause (defaults to permission denied).
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public PermissionDeniedException(String message, Throwable cause) {
        super(ErrorCode.PERMISSION_DENIED, message, cause);
    }

    /**
     * Validate that the error code is within the permission error range.
     *
     * @param errorCode the error code to validate
     * @throws IllegalArgumentException if the error code is not a permission error
     */
    private void validateErrorCode(int errorCode) {
        if (!ErrorCode.isPermissionError(errorCode)) {
            throw new IllegalArgumentException(
                "PermissionDeniedException must use permission error codes (3000-3999). " +
                "Provided code: " + errorCode + " is not a permission error code."
            );
        }
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
        return new PermissionDeniedException(ErrorCode.PERMISSION_DENIED,
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
        return new PermissionDeniedException(ErrorCode.INSUFFICIENT_PERMISSIONS,
            "User '" + userId + "' has insufficient permissions. Required: " + requiredPermission);
    }

    /**
     * Create a PermissionDeniedException for role not found.
     *
     * @param roleName the role name that was not found
     * @return PermissionDeniedException instance
     */
    public static PermissionDeniedException roleNotFound(String roleName) {
        return new PermissionDeniedException(ErrorCode.ROLE_NOT_FOUND,
            "Role not found: " + roleName);
    }

    /**
     * Create a PermissionDeniedException for role already exists.
     *
     * @param roleName the role name that already exists
     * @return PermissionDeniedException instance
     */
    public static PermissionDeniedException roleAlreadyExists(String roleName) {
        return new PermissionDeniedException(ErrorCode.ROLE_ALREADY_EXISTS,
            "Role already exists: " + roleName);
    }

    /**
     * Create a PermissionDeniedException for permission not found.
     *
     * @param permissionName the permission name that was not found
     * @return PermissionDeniedException instance
     */
    public static PermissionDeniedException permissionNotFound(String permissionName) {
        return new PermissionDeniedException(ErrorCode.PERMISSION_NOT_FOUND,
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
        return new PermissionDeniedException(ErrorCode.FORBIDDEN,
            "Access forbidden for user '" + userId + "': " + reason);
    }

    /**
     * Create a PermissionDeniedException for unauthorized access.
     *
     * @param reason the reason for unauthorized access
     * @return PermissionDeniedException instance
     */
    public static PermissionDeniedException unauthorized(String reason) {
        return new PermissionDeniedException(ErrorCode.UNAUTHORIZED,
            "Unauthorized access: " + reason);
    }
}