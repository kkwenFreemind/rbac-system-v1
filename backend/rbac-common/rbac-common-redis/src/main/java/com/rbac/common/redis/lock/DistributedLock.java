package com.rbac.common.redis.lock;

import com.rbac.common.core.exception.SystemException;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分散式鎖介面
 * 用於並行操作的分散式鎖定，基於 Redis 實作
 *
 * @author RBAC System
 * @since 1.0.0
 */
public interface DistributedLock {

    /**
     * 嘗試取得鎖並指定逾時時間
     *
     * @param key    鎖鍵
     * @param timeout 鎖逾時持續時間
     * @param unit   時間單位
     * @return 若取得鎖回傳 true，否則回傳 false
     * @throws IllegalArgumentException 若 key 為 null 或空字串
     */
    boolean tryLock(String key, long timeout, TimeUnit unit);

    /**
     * 使用預設逾時時間（30 秒）嘗試取得鎖
     *
     * @param key 鎖鍵
     * @return 若取得鎖回傳 true
     */
    default boolean tryLock(String key) {
        return tryLock(key, 30, TimeUnit.SECONDS);
    }

    /**
     * 釋放鎖
     *
     * @param key 鎖鍵
     * @throws IllegalArgumentException 若 key 為 null 或空字串
     */
    void unlock(String key);

    /**
     * 在鎖定狀態下執行操作（自動取得與釋放）
     *
     * @param key     鎖鍵
     * @param timeout 鎖逾時時間
     * @param unit    時間單位
     * @param action  持有鎖時要執行的操作
     * @param <T>     操作回傳類型
     * @return 操作結果
     * @throws SystemException 若無法取得鎖
     */
    <T> T executeWithLock(String key, long timeout, TimeUnit unit, Supplier<T> action);

    /**
     * 使用預設逾時時間在鎖定狀態下執行操作
     *
     * @param key    鎖鍵
     * @param action 要執行的操作
     * @param <T>    操作回傳類型
     * @return 操作結果
     */
    default <T> T executeWithLock(String key, Supplier<T> action) {
        return executeWithLock(key, 30, TimeUnit.SECONDS, action);
    }

    /**
     * 在鎖定狀態下執行 runnable 操作（無回傳值）
     *
     * @param key     鎖鍵
     * @param timeout 鎖逾時時間
     * @param unit    時間單位
     * @param action  要執行的操作
     */
    default void executeWithLock(String key, long timeout, TimeUnit unit, Runnable action) {
        executeWithLock(key, timeout, unit, () -> {
            action.run();
            return null;
        });
    }

    /**
     * 使用預設逾時時間在鎖定狀態下執行 runnable 操作（無回傳值）
     *
     * @param key    鎖鍵
     * @param action 要執行的操作
     */
    default void executeWithLock(String key, Runnable action) {
        executeWithLock(key, 30, TimeUnit.SECONDS, action);
    }
}