package com.rbac.auth.exception;

import com.rbac.common.core.exception.RbacException;

import java.time.Duration;
import java.time.Instant;

/**
 * 帳號鎖定異常
 *
 * <p>當帳號因密碼錯誤 5 次被鎖定時拋出</p>
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
public class AccountLockedException extends RbacException {

    /**
     * Error code for account locked exceptions.
     */
    private static final String ERROR_CODE = "403";

    private final Long lockUntil; // Unix timestamp (秒)

    /**
     * Constructor with message and lock until timestamp.
     *
     * @param message the error message
     * @param lockUntil the timestamp when the account will be unlocked (Unix timestamp in seconds)
     */
    public AccountLockedException(String message, Long lockUntil) {
        super(ERROR_CODE, message);
        this.lockUntil = lockUntil;
    }

    /**
     * Get the timestamp when the account will be unlocked.
     *
     * @return the lock until timestamp (Unix timestamp in seconds)
     */
    public Long getLockUntil() {
        return lockUntil;
    }

    /**
     * Get the remaining lock time as a Duration.
     *
     * @return the remaining lock time, or zero if already unlocked
     */
    public Duration getRemainingLockTime() {
        long now = Instant.now().getEpochSecond();
        long remaining = lockUntil - now;
        return Duration.ofSeconds(Math.max(0, remaining));
    }
}