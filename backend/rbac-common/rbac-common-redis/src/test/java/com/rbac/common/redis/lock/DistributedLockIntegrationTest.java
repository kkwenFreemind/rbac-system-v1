package com.rbac.common.redis.lock;

import com.rbac.common.core.exception.SystemException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分散式鎖整合測試
 * 使用 Testcontainers Redis 測試並發存取和鎖定機制
 *
 * @author RBAC System
 * @since 1.0.0
 */
@SpringBootTest
@Testcontainers
class DistributedLockIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379)
        .withCommand("redis-server", "--requirepass", "testpass");

    @DynamicPropertySource
    static void redisProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
        registry.add("spring.redis.password", () -> "testpass");
    }

    @Autowired
    private DistributedLock distributedLock;

    @Autowired
    private LockKeyGenerator lockKeyGenerator;

    @BeforeEach
    void setUp() {
        // 確保 Redis 容器正常運行
        assertTrue(redis.isRunning(), "Redis container should be running");
    }

    @Test
    void distributedLock_ShouldPreventConcurrentAccess() throws Exception {
        // Given
        String lockKey = lockKeyGenerator.globalLock("test", "counter");
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger successfulOperations = new AtomicInteger(0);

        // When: 10 個執行緒同時嘗試遞增計數器
        Runnable task = () -> {
            try {
                distributedLock.executeWithLock(lockKey, 10, TimeUnit.SECONDS, () -> {
                    int current = counter.get();
                    // 模擬一些工作
                    Thread.sleep(10);
                    counter.set(current + 1);
                    successfulOperations.incrementAndGet();
                    return null;
                });
            } catch (Exception e) {
                // 預期某些執行緒會因為無法取得鎖而失敗
                // 這是正常的行為
            } finally {
                latch.countDown();
            }
        };

        // 啟動所有執行緒
        for (int i = 0; i < 10; i++) {
            executor.submit(task);
        }

        // 等待所有執行緒完成
        assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete within timeout");

        // Then: 最終計數器應該是成功的操作數量
        assertEquals(successfulOperations.get(), counter.get(),
            "Counter should equal the number of successful operations");

        // 至少應該有一個操作成功
        assertTrue(successfulOperations.get() > 0,
            "At least one operation should succeed");

        // 總操作數應該等於成功操作數（沒有競爭條件）
        assertEquals(successfulOperations.get(), counter.get(),
            "No race condition should occur - counter should equal successful operations");

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void tryLock_ShouldReturnFalseWhenLockIsHeld() throws Exception {
        // Given
        String lockKey = lockKeyGenerator.globalLock("test", "exclusive");

        // When: 第一個鎖成功取得
        boolean firstLock = distributedLock.tryLock(lockKey, 5, TimeUnit.SECONDS);
        assertTrue(firstLock, "First lock should be acquired");

        // 第二個鎖應該失敗
        boolean secondLock = distributedLock.tryLock(lockKey, 1, TimeUnit.SECONDS);
        assertFalse(secondLock, "Second lock should fail when first lock is held");

        // Then: 釋放第一個鎖
        distributedLock.unlock(lockKey);
    }

    @Test
    void tryLock_ShouldSucceedAfterLockIsReleased() throws Exception {
        // Given
        String lockKey = lockKeyGenerator.globalLock("test", "release");

        // When: 取得並釋放鎖
        assertTrue(distributedLock.tryLock(lockKey, 5, TimeUnit.SECONDS));
        distributedLock.unlock(lockKey);

        // Then: 應該能夠再次取得鎖
        assertTrue(distributedLock.tryLock(lockKey, 5, TimeUnit.SECONDS));
        distributedLock.unlock(lockKey);
    }

    @Test
    void executeWithLock_ShouldExecuteActionWhenLockAcquired() {
        // Given
        String lockKey = lockKeyGenerator.globalLock("test", "action");
        AtomicInteger counter = new AtomicInteger(0);

        // When
        Integer result = distributedLock.executeWithLock(lockKey, () -> {
            counter.incrementAndGet();
            return 42;
        });

        // Then
        assertEquals(42, result);
        assertEquals(1, counter.get());
    }

    @Test
    void executeWithLock_ShouldThrowExceptionWhenCannotAcquireLock() {
        // Given
        String lockKey = lockKeyGenerator.globalLock("test", "timeout");

        // 先取得鎖
        assertTrue(distributedLock.tryLock(lockKey, 5, TimeUnit.SECONDS));

        // When & Then: 嘗試在短逾時內執行應該失敗
        assertThrows(SystemException.class, () ->
            distributedLock.executeWithLock(lockKey, 100, TimeUnit.MILLISECONDS, () -> "should not execute"));

        // Cleanup
        distributedLock.unlock(lockKey);
    }

    @Test
    void lock_ShouldAutoExpire() throws Exception {
        // Given
        String lockKey = lockKeyGenerator.globalLock("test", "expire");

        // When: 取得短逾時鎖
        assertTrue(distributedLock.tryLock(lockKey, 1, TimeUnit.SECONDS));

        // 等待鎖過期
        Thread.sleep(1100);

        // Then: 應該能夠再次取得鎖
        assertTrue(distributedLock.tryLock(lockKey, 1, TimeUnit.SECONDS),
            "Lock should be acquirable after expiration");

        distributedLock.unlock(lockKey);
    }

    @Test
    void unlock_ShouldOnlyReleaseOwnLock() throws Exception {
        // Given
        String lockKey1 = lockKeyGenerator.globalLock("test", "own1");
        String lockKey2 = lockKeyGenerator.globalLock("test", "own2");

        // When: 取得兩個不同的鎖
        assertTrue(distributedLock.tryLock(lockKey1, 5, TimeUnit.SECONDS));
        assertTrue(distributedLock.tryLock(lockKey2, 5, TimeUnit.SECONDS));

        // 釋放第一個鎖
        distributedLock.unlock(lockKey1);

        // Then: 第一個鎖應該可以重新取得，第二個鎖仍然被持有
        assertTrue(distributedLock.tryLock(lockKey1, 1, TimeUnit.SECONDS),
            "First lock should be releasable after unlock");
        assertFalse(distributedLock.tryLock(lockKey2, 1, TimeUnit.SECONDS),
            "Second lock should still be held");

        // Cleanup
        distributedLock.unlock(lockKey1);
        distributedLock.unlock(lockKey2);
    }

    @Test
    void lockKeyGenerator_ShouldGenerateProperKeys() {
        // Given & When
        String userLock = lockKeyGenerator.userLock("update", 123L);
        String roleLock = lockKeyGenerator.roleLock("assign", 456L);
        String assignmentLock = lockKeyGenerator.assignmentLock("role_permission", 123L, 456L);
        String globalLock = lockKeyGenerator.globalLock("maintenance", "system");

        // Then
        assertTrue(userLock.startsWith("rbac:lock:user:update:123"));
        assertTrue(roleLock.startsWith("rbac:lock:role:assign:456"));
        assertTrue(assignmentLock.startsWith("rbac:lock:role_permission:assign:123_456"));
        assertTrue(globalLock.startsWith("rbac:lock:global:maintenance:system"));
    }

    @Test
    void lockKeyGenerator_ShouldGenerateTenantSpecificKeys() {
        // Given
        String tenantId = "tenant_123";

        // When
        String userLock = lockKeyGenerator.userLock(tenantId, "update", 456L);
        String roleLock = lockKeyGenerator.roleLock(tenantId, "assign", 789L);

        // Then
        assertTrue(userLock.startsWith("rbac:lock:tenant_123:user:update:456"));
        assertTrue(roleLock.startsWith("rbac:lock:tenant_123:role:assign:789"));
    }
}