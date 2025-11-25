package com.rbac.auth.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帳號鎖定資訊
 *
 * <p>記錄帳號鎖定狀態和失敗嘗試資訊</p>
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@Data
public class AccountLockInfo {

    /**
     * 使用者名稱
     */
    private String username;

    /**
     * 失敗嘗試次數
     */
    private int failedAttempts;

    /**
     * 鎖定結束時間戳（秒）
     */
    private Long lockUntil;

    /**
     * 最後嘗試時間
     */
    private LocalDateTime lastAttemptAt;

    /**
     * 檢查帳號是否被鎖定
     *
     * @return 如果被鎖定返回 true
     */
    public boolean isLocked() {
        if (lockUntil == null) {
            return false;
        }
        long currentTime = System.currentTimeMillis() / 1000;
        return lockUntil > currentTime;
    }

    /**
     * 取得剩餘鎖定時間（秒）
     *
     * @return 剩餘鎖定時間，如果未鎖定返回 0
     */
    public long getRemainingLockTime() {
        if (!isLocked()) {
            return 0;
        }
        long currentTime = System.currentTimeMillis() / 1000;
        return lockUntil - currentTime;
    }

    /**
     * 重置失敗嘗試次數
     */
    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.lockUntil = null;
    }

    /**
     * 增加失敗嘗試次數
     */
    public void incrementFailedAttempts() {
        this.failedAttempts++;
        this.lastAttemptAt = LocalDateTime.now();
    }
}