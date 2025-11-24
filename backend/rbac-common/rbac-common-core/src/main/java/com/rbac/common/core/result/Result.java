package com.rbac.common.core.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rbac.common.core.constant.CommonConstant;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Unified API response format.
 *
 * This class provides a standardized response structure for all API endpoints
 * in the RBAC system, ensuring consistent error handling and data presentation.
 *
 * @param <T> the type of data being returned
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /**
     * Response code (200 for success, error codes for failures).
     */
    private int code;

    /**
     * Response message describing the result.
     */
    private String message;

    /**
     * Response data payload.
     */
    private T data;

    /**
     * Timestamp of the response.
     */
    private LocalDateTime timestamp;

    /**
     * Trace ID for request tracking (optional).
     */
    private String traceId;

    /**
     * Private constructor to enforce factory method usage.
     */
    private Result() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Private constructor with all fields.
     *
     * @param code the response code
     * @param message the response message
     * @param data the response data
     * @param traceId the trace ID
     */
    private Result(int code, String message, T data, String traceId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.traceId = traceId;
    }

    // ==================== Success Factory Methods ====================

    /**
     * Create a successful result with data.
     *
     * @param data the response data
     * @param <T> the type of data
     * @return Result instance
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data, null);
    }

    /**
     * Create a successful result with data and custom message.
     *
     * @param data the response data
     * @param message the custom success message
     * @param <T> the type of data
     * @return Result instance
     */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data, null);
    }

    /**
     * Create a successful result with no content.
     *
     * @param <T> the type of data
     * @return Result instance
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS_NO_CONTENT.getCode(),
                          ResultCode.SUCCESS_NO_CONTENT.getMessage(), null, null);
    }

    /**
     * Create a successful result with custom message and no content.
     *
     * @param message the custom success message
     * @param <T> the type of data
     * @return Result instance
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, null, null);
    }

    // ==================== Error Factory Methods ====================

    /**
     * Create an error result with ResultCode.
     *
     * @param resultCode the result code enum
     * @param <T> the type of data
     * @return Result instance
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null, null);
    }

    /**
     * Create an error result with ResultCode and custom message.
     *
     * @param resultCode the result code enum
     * @param message the custom error message
     * @param <T> the type of data
     * @return Result instance
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null, null);
    }

    /**
     * Create an error result with code and message.
     *
     * @param code the error code
     * @param message the error message
     * @param <T> the type of data
     * @return Result instance
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null, null);
    }

    /**
     * Create a bad request error result.
     *
     * @param message the error message
     * @param <T> the type of data
     * @return Result instance
     */
    public static <T> Result<T> badRequest(String message) {
        return error(ResultCode.BAD_REQUEST, message);
    }

    /**
     * Create an unauthorized error result.
     *
     * @param message the error message
     * @param <T> the type of data
     * @return Result instance
     */
    public static <T> Result<T> unauthorized(String message) {
        return error(ResultCode.UNAUTHORIZED, message);
    }

    /**
     * Create a forbidden error result.
     *
     * @param message the error message
     * @param <T> the type of data
     * @return Result instance
     */
    public static <T> Result<T> forbidden(String message) {
        return error(ResultCode.FORBIDDEN, message);
    }

    /**
     * Create a not found error result.
     *
     * @param message the error message
     * @param <T> the type of data
     * @return Result instance
     */
    public static <T> Result<T> notFound(String message) {
        return error(ResultCode.NOT_FOUND, message);
    }

    /**
     * Create an internal server error result.
     *
     * @param message the error message
     * @param <T> the type of data
     * @return Result instance
     */
    public static <T> Result<T> internalServerError(String message) {
        return error(ResultCode.INTERNAL_SERVER_ERROR, message);
    }

    // ==================== Utility Methods ====================

    /**
     * Check if this result represents a successful operation.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return this.code >= 200 && this.code < 300;
    }

    /**
     * Check if this result represents an error.
     *
     * @return true if error, false otherwise
     */
    public boolean isError() {
        return !isSuccess();
    }

    /**
     * Check if this result represents a client error (4xx).
     *
     * @return true if client error, false otherwise
     */
    public boolean isClientError() {
        return this.code >= 400 && this.code < 500;
    }

    /**
     * Check if this result represents a server error (5xx).
     *
     * @return true if server error, false otherwise
     */
    public boolean isServerError() {
        return this.code >= 500 && this.code < 600;
    }

    /**
     * Set the trace ID for this result.
     *
     * @param traceId the trace ID
     * @return this Result instance for method chaining
     */
    public Result<T> withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    /**
     * Get the result category.
     *
     * @return the result category
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