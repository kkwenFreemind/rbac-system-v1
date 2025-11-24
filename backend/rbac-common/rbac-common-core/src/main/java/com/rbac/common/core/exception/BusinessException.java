package com.rbac.common.core.exception;

import com.rbac.common.core.constant.ErrorCode;

/**
 * Exception thrown for business logic errors.
 *
 * This exception is used when a business rule is violated or when
 * an operation cannot be completed due to business constraints.
 * Examples include user not found, duplicate data, invalid state transitions, etc.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
public class BusinessException extends RbacException {

    /**
     * Constructor with error code and message.
     *
     * @param errorCode the business error code
     * @param message the error message
     */
    public BusinessException(int errorCode, String message) {
        super(errorCode, message);
        validateErrorCode(errorCode);
    }

    /**
     * Constructor with error code, message, and cause.
     *
     * @param errorCode the business error code
     * @param message the error message
     * @param cause the cause of this exception
     */
    public BusinessException(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
        validateErrorCode(errorCode);
    }

    /**
     * Constructor with message only (defaults to generic business error).
     *
     * @param message the error message
     */
    public BusinessException(String message) {
        super(ErrorCode.INVALID_INPUT, message);
    }

    /**
     * Constructor with message and cause (defaults to generic business error).
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public BusinessException(String message, Throwable cause) {
        super(ErrorCode.INVALID_INPUT, message, cause);
    }

    /**
     * Validate that the error code is within the business error range.
     *
     * @param errorCode the error code to validate
     * @throws IllegalArgumentException if the error code is not a business error
     */
    private void validateErrorCode(int errorCode) {
        if (!ErrorCode.isBusinessError(errorCode)) {
            throw new IllegalArgumentException(
                "BusinessException must use business error codes (1000-1999). " +
                "Provided code: " + errorCode + " is not a business error code."
            );
        }
    }

    /**
     * Create a BusinessException for user not found.
     *
     * @param userId the user ID that was not found
     * @return BusinessException instance
     */
    public static BusinessException userNotFound(String userId) {
        return new BusinessException(ErrorCode.USER_NOT_FOUND,
            "User not found with ID: " + userId);
    }

    /**
     * Create a BusinessException for user already exists.
     *
     * @param username the username that already exists
     * @return BusinessException instance
     */
    public static BusinessException userAlreadyExists(String username) {
        return new BusinessException(ErrorCode.USER_ALREADY_EXISTS,
            "User already exists with username: " + username);
    }

    /**
     * Create a BusinessException for invalid credentials.
     *
     * @return BusinessException instance
     */
    public static BusinessException invalidCredentials() {
        return new BusinessException(ErrorCode.INVALID_CREDENTIALS,
            "Invalid username or password");
    }

    /**
     * Create a BusinessException for user disabled.
     *
     * @param userId the disabled user ID
     * @return BusinessException instance
     */
    public static BusinessException userDisabled(String userId) {
        return new BusinessException(ErrorCode.USER_DISABLED,
            "User account is disabled: " + userId);
    }

    /**
     * Create a BusinessException for user locked.
     *
     * @param userId the locked user ID
     * @return BusinessException instance
     */
    public static BusinessException userLocked(String userId) {
        return new BusinessException(ErrorCode.USER_LOCKED,
            "User account is locked: " + userId);
    }

    /**
     * Create a BusinessException for password expired.
     *
     * @param userId the user ID with expired password
     * @return BusinessException instance
     */
    public static BusinessException passwordExpired(String userId) {
        return new BusinessException(ErrorCode.PASSWORD_EXPIRED,
            "Password has expired for user: " + userId);
    }
}