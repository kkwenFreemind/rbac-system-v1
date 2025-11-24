package com.rbac.common.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Redis 相關屬性配置
 * 
 * 從 application.yml 中讀取 Redis 相關配置
 * 提供快取和鎖定相關的客製化設定
 * 
 * @author RBAC System
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "rbac.redis")
public class RedisProperties {
    
    /**
     * 是否啟用 Redis 功能
     */
    private boolean enabled = true;
    
    /**
     * 快取鍵前綴
     */
    private String keyPrefix = "rbac";
    
    /**
     * 鎖定鍵前綴
     */
    private String lockKeyPrefix = "lock";
    
    /**
     * 預設快取 TTL（秒）
     */
    private int defaultTtl = 1800;
    
    /**
     * 鎖定預設逾時時間（秒）
     */
    private int lockTimeout = 30;
    
    /**
     * 鎖定重試次數
     */
    private int lockRetryCount = 3;
    
    /**
     * 鎖定重試間隔（毫秒）
     */
    private int lockRetryDelay = 100;
    
    // Getters and Setters
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getKeyPrefix() {
        return keyPrefix;
    }
    
    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }
    
    public String getLockKeyPrefix() {
        return lockKeyPrefix;
    }
    
    public void setLockKeyPrefix(String lockKeyPrefix) {
        this.lockKeyPrefix = lockKeyPrefix;
    }
    
    public int getDefaultTtl() {
        return defaultTtl;
    }
    
    public void setDefaultTtl(int defaultTtl) {
        this.defaultTtl = defaultTtl;
    }
    
    public int getLockTimeout() {
        return lockTimeout;
    }
    
    public void setLockTimeout(int lockTimeout) {
        this.lockTimeout = lockTimeout;
    }
    
    public int getLockRetryCount() {
        return lockRetryCount;
    }
    
    public void setLockRetryCount(int lockRetryCount) {
        this.lockRetryCount = lockRetryCount;
    }
    
    public int getLockRetryDelay() {
        return lockRetryDelay;
    }
    
    public void setLockRetryDelay(int lockRetryDelay) {
        this.lockRetryDelay = lockRetryDelay;
    }
}