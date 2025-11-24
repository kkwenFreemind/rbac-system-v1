package com.rbac.common.core.exception;

import com.rbac.common.core.constant.ErrorCode;

/**
 * Exception thrown for system-level errors.
 *
 * This exception is used when system components fail, such as database connections,
 * external service calls, file system operations, or configuration issues.
 * These are typically not recoverable by user action and require system administrator intervention.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
public class SystemException extends RbacException {

    /**
     * Constructor with error code and message.
     *
     * @param errorCode the system error code
     * @param message the error message
     */
    public SystemException(int errorCode, String message) {
        super(errorCode, message);
        validateErrorCode(errorCode);
    }

    /**
     * Constructor with error code, message, and cause.
     *
     * @param errorCode the system error code
     * @param message the error message
     * @param cause the cause of this exception
     */
    public SystemException(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
        validateErrorCode(errorCode);
    }

    /**
     * Constructor with message only (defaults to internal server error).
     *
     * @param message the error message
     */
    public SystemException(String message) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * Constructor with message and cause (defaults to internal server error).
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public SystemException(String message, Throwable cause) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, message, cause);
    }

    /**
     * Validate that the error code is within the system error range.
     *
     * @param errorCode the error code to validate
     * @throws IllegalArgumentException if the error code is not a system error
     */
    private void validateErrorCode(int errorCode) {
        if (!ErrorCode.isSystemError(errorCode)) {
            throw new IllegalArgumentException(
                "SystemException must use system error codes (5000-5999). " +
                "Provided code: " + errorCode + " is not a system error code."
            );
        }
    }

    /**
     * Create a SystemException for database connection error.
     *
     * @param cause the underlying cause
     * @return SystemException instance
     */
    public static SystemException databaseConnectionError(Throwable cause) {
        return new SystemException(ErrorCode.DATABASE_ERROR,
            "Database connection failed", cause);
    }

    /**
     * Create a SystemException for database connection error with custom message.
     *
     * @param message custom error message
     * @param cause the underlying cause
     * @return SystemException instance
     */
    public static SystemException databaseConnectionError(String message, Throwable cause) {
        return new SystemException(ErrorCode.DATABASE_ERROR, message, cause);
    }

    /**
     * Create a SystemException for Redis connection error.
     *
     * @param cause the underlying cause
     * @return SystemException instance
     */
    public static SystemException redisConnectionError(Throwable cause) {
        return new SystemException(ErrorCode.REDIS_ERROR,
            "Redis connection failed", cause);
    }

    /**
     * Create a SystemException for Redis connection error with custom message.
     *
     * @param message custom error message
     * @param cause the underlying cause
     * @return SystemException instance
     */
    public static SystemException redisConnectionError(String message, Throwable cause) {
        return new SystemException(ErrorCode.REDIS_ERROR, message, cause);
    }

    /**
     * Create a SystemException for external service error.
     *
     * @param serviceName the name of the external service
     * @param cause the underlying cause
     * @return SystemException instance
     */
    public static SystemException externalServiceError(String serviceName, Throwable cause) {
        return new SystemException(ErrorCode.EXTERNAL_SERVICE_ERROR,
            "External service '" + serviceName + "' is unavailable", cause);
    }

    /**
     * Create a SystemException for external service error with custom message.
     *
     * @param serviceName the name of the external service
     * @param message custom error message
     * @param cause the underlying cause
     * @return SystemException instance
     */
    public static SystemException externalServiceError(String serviceName, String message, Throwable cause) {
        return new SystemException(ErrorCode.EXTERNAL_SERVICE_ERROR,
            "External service '" + serviceName + "' error: " + message, cause);
    }

    /**
     * Create a SystemException for configuration error.
     *
     * @param configKey the configuration key that is missing or invalid
     * @return SystemException instance
     */
    public static SystemException configurationError(String configKey) {
        return new SystemException(ErrorCode.CONFIGURATION_ERROR,
            "Configuration error for key: " + configKey);
    }

    /**
     * Create a SystemException for configuration error with custom message.
     *
     * @param message custom error message
     * @return SystemException instance
     */
    public static SystemException configurationError(String message, Throwable cause) {
        return new SystemException(ErrorCode.CONFIGURATION_ERROR, message, cause);
    }

    /**
     * Create a SystemException for file system error.
     *
     * @param operation the file operation that failed (read, write, delete, etc.)
     * @param filePath the file path
     * @param cause the underlying cause
     * @return SystemException instance
     */
    public static SystemException fileSystemError(String operation, String filePath, Throwable cause) {
        return new SystemException(ErrorCode.FILE_SYSTEM_ERROR,
            "File system operation '" + operation + "' failed for path: " + filePath, cause);
    }

    /**
     * Create a SystemException for file system error with custom message.
     *
     * @param message custom error message
     * @param cause the underlying cause
     * @return SystemException instance
     */
    public static SystemException fileSystemError(String message, Throwable cause) {
        return new SystemException(ErrorCode.FILE_SYSTEM_ERROR, message, cause);
    }
}