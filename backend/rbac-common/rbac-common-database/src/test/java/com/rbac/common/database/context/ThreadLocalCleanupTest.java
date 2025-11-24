package com.rbac.common.database.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ThreadLocal 清理測試
 * 驗證執行緒池重用時不會發生記憶體洩漏或上下文污染
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
class ThreadLocalCleanupTest {

    @BeforeEach
    void setUp() {
        TenantContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void testThreadPoolReuse() throws Exception {
        // 建立固定大小的執行緒池（模擬實際應用）
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 第一輪：設定租戶上下文但不清理
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            executor.submit(() -> {
                TenantContextHolder.setTenantId("tenant_" + taskId);
                // 故意不清理
            }).get();
        }

        // 第二輪：檢查執行緒池中的執行緒是否還有舊的租戶上下文
        List<String> pollutedContexts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                String tenantId = TenantContextHolder.getTenantId();
                if (tenantId != null) {
                    pollutedContexts.add(tenantId);
                }
                return tenantId;
            }).get();
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // 應該檢測到執行緒池污染（如果沒有適當清理）
        // 這個測試驗證了問題的存在
        assertFalse(pollutedContexts.isEmpty(),
                "Thread pool pollution detected - ThreadLocal was not properly cleaned");
    }

    @Test
    void testProperCleanupPreventsThreadPoolPollution() throws Exception {
        // 建立固定大小的執行緒池
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 第一輪：正確設定和清理租戶上下文
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    TenantContextHolder.setTenantId("tenant_" + taskId);
                    assertEquals("tenant_" + taskId, TenantContextHolder.getTenantId());
                } finally {
                    TenantContextHolder.clear();
                }
                return null;
            }).get();
        }

        // 第二輪：檢查執行緒池中的執行緒是否已清理
        List<String> contexts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                String tenantId = TenantContextHolder.getTenantId();
                contexts.add(String.valueOf(tenantId));
                return tenantId;
            }).get();
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // 所有上下文都應該是 null
        contexts.forEach(context ->
                assertEquals("null", context, "ThreadLocal should be cleared"));
    }

    @Test
    void testMemoryLeakWithRepeatedOperations() throws Exception {
        // 建立單執行緒池模擬重用同一個執行緒
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // 執行大量操作
        for (int i = 0; i < 1000; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    TenantContextHolder.setTenantId("tenant_" + taskId);
                    String retrieved = TenantContextHolder.getTenantId();
                    assertEquals("tenant_" + taskId, retrieved);
                } finally {
                    TenantContextHolder.clear();
                }
                return null;
            }).get();
        }

        // 最後檢查上下文是否已清理
        String finalContext = executor.submit(() ->
                TenantContextHolder.getTenantId()
        ).get();

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertNull(finalContext, "ThreadLocal should be null after proper cleanup");
    }

    @Test
    void testConcurrentCleanup() throws Exception {
        // 建立多執行緒池
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 並發設定和清理
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    // 設定租戶上下文
                    TenantContextHolder.setTenantId("tenant_" + threadId);

                    // 短暫等待以增加並發競爭
                    Thread.sleep(10);

                    // 驗證自己的上下文
                    assertEquals("tenant_" + threadId, TenantContextHolder.getTenantId());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    // 清理
                    TenantContextHolder.clear();
                    latch.countDown();
                }
            });
        }

        // 等待所有執行緒完成
        assertTrue(latch.await(10, TimeUnit.SECONDS));

        // 再次提交任務檢查清理情況
        CountDownLatch verifyLatch = new CountDownLatch(threadCount);
        List<String> postCleanupContexts = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    String context = TenantContextHolder.getTenantId();
                    postCleanupContexts.add(String.valueOf(context));
                } finally {
                    verifyLatch.countDown();
                }
            });
        }

        assertTrue(verifyLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // 所有執行緒的上下文都應該是 null
        postCleanupContexts.forEach(context ->
                assertEquals("null", context, "All threads should have null context"));
    }

    @Test
    void testExceptionDoesNotPreventCleanup() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            // 提交一個會拋出異常的任務
            executor.submit(() -> {
                try {
                    TenantContextHolder.setTenantId("tenant_exception");
                    // 模擬業務邏輯異常
                    throw new RuntimeException("Simulated business exception");
                } finally {
                    TenantContextHolder.clear();
                }
            }).get();
        } catch (Exception e) {
            // 預期會拋出異常
            assertTrue(e.getCause() instanceof RuntimeException);
        }

        // 驗證上下文已被清理
        try {
            String context = executor.submit(() ->
                    TenantContextHolder.getTenantId()
            ).get(1, TimeUnit.SECONDS);
            assertNull(context, "Context should be null even after exception");
        } catch (Exception e) {
            fail("Should not throw exception when checking context");
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void testMultipleCleanupCallsAreSafe() {
        // 設定上下文
        TenantContextHolder.setTenantId("tenant_123");
        assertEquals("tenant_123", TenantContextHolder.getTenantId());

        // 多次清理應該是安全的
        TenantContextHolder.clear();
        assertNull(TenantContextHolder.getTenantId());

        TenantContextHolder.clear();
        assertNull(TenantContextHolder.getTenantId());

        TenantContextHolder.clear();
        assertNull(TenantContextHolder.getTenantId());
    }

    @Test
    void testScheduledTaskCleanup() throws Exception {
        // 模擬定時任務執行緒池
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        // 排程任務
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                TenantContextHolder.setTenantId("scheduled_tenant");
                // 模擬任務執行
                String context = TenantContextHolder.getTenantId();
                assertEquals("scheduled_tenant", context);
            } finally {
                TenantContextHolder.clear();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        // 運行一段時間
        Thread.sleep(500);
        future.cancel(false);

        // 驗證最後一次執行後上下文已清理
        String finalContext = scheduler.submit(() ->
                TenantContextHolder.getTenantId()
        ).get(1, TimeUnit.SECONDS);

        scheduler.shutdown();
        assertTrue(scheduler.awaitTermination(5, TimeUnit.SECONDS));

        assertNull(finalContext, "Scheduled task should clean up ThreadLocal");
    }

    @Test
    void testForkJoinPoolCleanup() throws Exception {
        // 測試 ForkJoinPool（Java 8+ 並行流使用的執行緒池）
        ForkJoinPool pool = new ForkJoinPool(4);

        try {
            // 第一輪：設定上下文
            pool.submit(() -> {
                TenantContextHolder.setTenantId("forkjoin_tenant");
                assertEquals("forkjoin_tenant", TenantContextHolder.getTenantId());
                TenantContextHolder.clear();
            }).get();

            // 第二輪：驗證清理
            String context = pool.submit(() ->
                    TenantContextHolder.getTenantId()
            ).get();

            assertNull(context, "ForkJoinPool should have clean ThreadLocal");
        } finally {
            pool.shutdown();
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void testThreadInterruptionDoesNotAffectCleanup() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            Future<?> future = executor.submit(() -> {
                try {
                    TenantContextHolder.setTenantId("interrupt_tenant");
                    // 模擬長時間運行的任務
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    TenantContextHolder.clear();
                }
            });

            // 立即中斷任務
            Thread.sleep(100);
            future.cancel(true);

            // 給予時間完成清理
            Thread.sleep(200);

            // 驗證上下文已清理
            String context = executor.submit(() ->
                    TenantContextHolder.getTenantId()
            ).get(1, TimeUnit.SECONDS);

            assertNull(context, "Context should be cleaned even after thread interruption");
        } finally {
            executor.shutdown();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }
}
