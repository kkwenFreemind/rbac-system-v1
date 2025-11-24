package com.rbac.common.redis.util;

/**
 * 快取服務介面
 *
 * 提供統一的快取操作介面，抽象化 Redis 操作。
 * 實作類應處理序列化、反序列化和錯誤處理。
 *
 * @author RBAC System
 * @since 1.0.0
 */
public interface CacheService {

    /**
     * 從快取取得值
     *
     * @param key 快取鍵
     * @param type 值的類別型別
     * @return 快取的值，若未找到則回傳 null
     */
    <T> T get(String key, Class<T> type);

    /**
     * 設定快取值並指定 TTL
     *
     * @param key 快取鍵
     * @param value 要快取的值
     * @param ttl 存活時間（秒）
     */
    void set(String key, Object value, long ttl);

    /**
     * 使用預設 TTL 設定快取值
     *
     * @param key 快取鍵
     * @param value 要快取的值
     */
    void set(String key, Object value);

    /**
     * 刪除單一鍵
     *
     * @param key 快取鍵
     * @return 若已刪除回傳 true，若鍵不存在回傳 false
     */
    boolean delete(String key);

    /**
     * 刪除符合模式的鍵
     *
     * @param pattern 鍵模式（例如："user:*"）
     * @return 已刪除的鍵數量
     */
    long deletePattern(String pattern);

    /**
     * 檢查鍵是否存在
     *
     * @param key 快取鍵
     * @return 若存在回傳 true
     */
    boolean exists(String key);

    /**
     * 為鍵設定過期時間
     *
     * @param key 快取鍵
     * @param ttl 存活時間（秒）
     * @return 若成功設定過期時間回傳 true
     */
    boolean expire(String key, long ttl);

    /**
     * 遞增數值
     *
     * @param key 快取鍵
     * @param delta 遞增量
     * @return 遞增後的新值
     */
    Long increment(String key, long delta);

    /**
     * 遞減數值
     *
     * @param key 快取鍵
     * @param delta 遞減量
     * @return 遞減後的新值
     */
    Long decrement(String key, long delta);
}