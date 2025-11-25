package com.rbac.auth.config;

import com.rbac.auth.exception.AccountLockedException;
import com.rbac.auth.exception.AuthenticationException;
import com.rbac.auth.exception.TokenExpiredException;
import com.rbac.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全域異常處理器
 *
 * <p>統一處理認證模組的各種異常</p>
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 處理認證異常
     *
     * @param e 認證異常
     * @return 錯誤結果
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 處理 Token 過期異常
     *
     * @param e Token 過期異常
     * @return 錯誤結果
     */
    @ExceptionHandler(TokenExpiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleTokenExpiredException(TokenExpiredException e) {
        log.warn("Token expired: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 處理帳號鎖定異常
     *
     * @param e 帳號鎖定異常
     * @return 錯誤結果
     */
    @ExceptionHandler(AccountLockedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccountLockedException(AccountLockedException e) {
        log.warn("Account locked: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 處理驗證異常
     *
     * @param e 驗證異常
     * @return 錯誤結果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("參數驗證失敗");

        log.warn("Validation failed: {}", message);
        return Result.error(400, message);
    }

    /**
     * 處理通用異常
     *
     * @param e 通用異常
     * @return 錯誤結果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleGenericException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return Result.error(500, "系統內部錯誤");
    }
}