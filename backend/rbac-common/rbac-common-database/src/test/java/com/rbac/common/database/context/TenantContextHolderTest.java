package com.rbac.common.database.context;

import com.rbac.common.core.exception.TenantException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TenantContextHolder 單元測試
 * 測試 ThreadLocal 租戶上下文的隔離和清理功能
 *
 * @author rbac-system
 * @version 1.0.0
 */
class TenantContextHolderTest {

    @BeforeEach
    void setUp() {
        // 確保每個測試前上下文都是乾淨的
        TenantContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        // 確保每個測試後清理上下文
        TenantContextHolder.clear();
    }

    @Test
    void testSetAndGetTenantId() {
        // 設定租戶 ID
        TenantContextHolder.setTenantId("tenant_123");

        // 驗證可以獲取
        assertEquals("tenant_123", TenantContextHolder.getTenantId());

        // 清理後應該為 null
        TenantContextHolder.clear();
        assertNull(TenantContextHolder.getTenantId());
    }

    @Test
    void testSetNullTenantIdThrowsException() {
        // 設定 null 應該拋出異常
        assertThrows(IllegalArgumentException.class, () -> {
            TenantContextHolder.setTenantId(null);
        });
    }

    @Test
    void testSetEmptyTenantIdThrowsException() {
        // 設定空字符串應該拋出異常
        assertThrows(IllegalArgumentException.class, () -> {
            TenantContextHolder.setTenantId("");
        });
    }

    @Test
    void testClear() {
        // 設定值
        TenantContextHolder.setTenantId("tenant_123");
        assertEquals("tenant_123", TenantContextHolder.getTenantId());

        // 清理
        TenantContextHolder.clear();
        assertNull(TenantContextHolder.getTenantId());
    }

    @Test
    void testThreadIsolation() throws Exception {
        // 測試多線程隔離
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);

        AtomicReference<String> tenant1Result = new AtomicReference<>();
        AtomicReference<String> tenant2Result = new AtomicReference<>();
        AtomicReference<String> tenant3Result = new AtomicReference<>();

        // 線程 1：租戶 A
        executor.submit(() -> {
            try {
                TenantContextHolder.setTenantId("tenant_a");
                Thread.sleep(100); // 模擬工作
                tenant1Result.set(TenantContextHolder.getTenantId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                TenantContextHolder.clear();
                latch.countDown();
            }
        });

        // 線程 2：租戶 B
        executor.submit(() -> {
            try {
                TenantContextHolder.setTenantId("tenant_b");
                Thread.sleep(100); // 模擬工作
                tenant2Result.set(TenantContextHolder.getTenantId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                TenantContextHolder.clear();
                latch.countDown();
            }
        });

        // 線程 3：沒有設定租戶（應該為 null）
        executor.submit(() -> {
            try {
                Thread.sleep(50); // 等待其他線程設定
                tenant3Result.set(TenantContextHolder.getTenantId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });

        // 等待所有線程完成
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        // 驗證每個線程的隔離性
        assertEquals("tenant_a", tenant1Result.get());
        assertEquals("tenant_b", tenant2Result.get());
        assertNull(tenant3Result.get());

        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
    }

    @Test
    void testMultipleSetAndClear() {
        // 多次設定和清理
        TenantContextHolder.setTenantId("tenant_1");
        assertEquals("tenant_1", TenantContextHolder.getTenantId());

        TenantContextHolder.setTenantId("tenant_2");
        assertEquals("tenant_2", TenantContextHolder.getTenantId());

        TenantContextHolder.clear();
        assertNull(TenantContextHolder.getTenantId());

        TenantContextHolder.setTenantId("tenant_3");
        assertEquals("tenant_3", TenantContextHolder.getTenantId());

        TenantContextHolder.clear();
        assertNull(TenantContextHolder.getTenantId());
    }

    @Test
    void testNestedOperations() {
        // 測試嵌套操作（模擬過濾器鏈）
        TenantContextHolder.setTenantId("tenant_123");

        // 模擬內層操作
        String innerTenant = TenantContextHolder.getTenantId();
        assertEquals("tenant_123", innerTenant);

        // 模擬外層清理
        TenantContextHolder.clear();
        assertNull(TenantContextHolder.getTenantId());
    }

    @Test
    void testMemoryLeakPrevention() throws Exception {
        // 測試記憶體洩漏防護
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // 提交多個任務到同一個線程
        for (int i = 0; i < 10; i++) {
            final String tenantId = "tenant_" + i;
            executor.submit(() -> {
                TenantContextHolder.setTenantId(tenantId);
                String retrieved = TenantContextHolder.getTenantId();
                assertEquals(tenantId, retrieved);
                TenantContextHolder.clear();
                return retrieved;
            }).get(); // 等待完成
        }

        // 最後一個任務執行完畢後，上下文應該被清理
        executor.submit(() -> {
            String shouldBeNull = TenantContextHolder.getTenantId();
            assertNull(shouldBeNull);
            return shouldBeNull;
        }).get();

        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
    }

    @Test
    void testConcurrentAccess() throws Exception {
        // 測試並發存取
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    String tenantId = "tenant_" + threadId;
                    TenantContextHolder.setTenantId(tenantId);

                    // 短暫持有鎖定以增加競爭
                    Thread.sleep(10);

                    assertEquals(tenantId, TenantContextHolder.getTenantId());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    TenantContextHolder.clear();
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
    }
}