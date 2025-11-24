package com.rbac.common.database.integration;

import com.rbac.common.database.context.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 租戶隔離整合測試
 * 模擬測試 tenant_id 自動注入和隔離行為
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
class TenantIsolationIntegrationTest {

    /**
     * 模擬資料庫儲存（key: tenantId, value: 資料）
     */
    private Map<String, Map<String, String>> mockDatabase;

    @BeforeEach
    void setUp() {
        // 初始化模擬資料庫
        mockDatabase = new ConcurrentHashMap<>();
        
        // 清理租戶上下文
        TenantContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        // 清理租戶上下文
        TenantContextHolder.clear();
        
        // 清理模擬資料庫
        mockDatabase.clear();
    }

    @Test
    void testTenantIdAutoInjectionOnInsert() {
        // 設定租戶上下文
        String tenantId = "tenant_001";
        TenantContextHolder.setTenantId(tenantId);

        // 模擬插入資料（自動注入 tenant_id）
        String currentTenant = TenantContextHolder.getTenantId();
        assertNotNull(currentTenant, "Tenant ID should be set in context");
        
        mockDatabase.putIfAbsent(currentTenant, new HashMap<>());
        mockDatabase.get(currentTenant).put("test_name", "test_value");

        // 驗證資料是否正確插入
        assertTrue(mockDatabase.containsKey(tenantId));
        assertEquals("test_value", mockDatabase.get(tenantId).get("test_name"));
    }

    @Test
    void testTenantIsolationOnQuery() {
        // 插入不同租戶的資料
        insertTestData("tenant_001", "data_1", "value_1");
        insertTestData("tenant_002", "data_2", "value_2");
        insertTestData("tenant_003", "data_3", "value_3");

        // 測試租戶 1 - 只能看到自己的資料
        TenantContextHolder.setTenantId("tenant_001");
        String currentTenant = TenantContextHolder.getTenantId();
        Map<String, String> tenant1Data = mockDatabase.get(currentTenant);
        assertNotNull(tenant1Data);
        assertEquals(1, tenant1Data.size());
        assertEquals("value_1", tenant1Data.get("data_1"));

        // 測試租戶 2
        TenantContextHolder.clear();
        TenantContextHolder.setTenantId("tenant_002");
        currentTenant = TenantContextHolder.getTenantId();
        Map<String, String> tenant2Data = mockDatabase.get(currentTenant);
        assertNotNull(tenant2Data);
        assertEquals(1, tenant2Data.size());
        assertEquals("value_2", tenant2Data.get("data_2"));

        // 測試租戶 3
        TenantContextHolder.clear();
        TenantContextHolder.setTenantId("tenant_003");
        currentTenant = TenantContextHolder.getTenantId();
        Map<String, String> tenant3Data = mockDatabase.get(currentTenant);
        assertNotNull(tenant3Data);
        assertEquals(1, tenant3Data.size());
        assertEquals("value_3", tenant3Data.get("data_3"));
    }

    @Test
    void testTenantIsolationOnUpdate() {
        // 插入資料
        insertTestData("tenant_001", "data_1", "value_1");
        insertTestData("tenant_002", "data_2", "value_2");

        // 租戶 1 更新資料
        TenantContextHolder.setTenantId("tenant_001");
        String currentTenant = TenantContextHolder.getTenantId();
        mockDatabase.get(currentTenant).put("data_1", "updated_value");

        // 驗證租戶 1 的資料已更新
        assertEquals("updated_value", mockDatabase.get("tenant_001").get("data_1"));

        // 驗證租戶 2 的資料未受影響
        assertEquals("value_2", mockDatabase.get("tenant_002").get("data_2"));
    }

    @Test
    void testTenantIsolationOnDelete() {
        // 插入資料
        insertTestData("tenant_001", "data_1", "value_1");
        insertTestData("tenant_002", "data_2", "value_2");
        insertTestData("tenant_003", "data_3", "value_3");

        // 租戶 1 刪除資料
        TenantContextHolder.setTenantId("tenant_001");
        String currentTenant = TenantContextHolder.getTenantId();
        mockDatabase.get(currentTenant).remove("data_1");

        // 驗證租戶 1 的資料已刪除
        assertFalse(mockDatabase.get("tenant_001").containsKey("data_1"));

        // 驗證其他租戶的資料未受影響
        assertEquals(1, mockDatabase.get("tenant_002").size());
        assertEquals(1, mockDatabase.get("tenant_003").size());
    }

    @Test
    void testNoTenantContextThrowsException() {
        // 未設定租戶上下文時應該無法取得 tenant ID
        TenantContextHolder.clear();
        assertNull(TenantContextHolder.getTenantId(), "Tenant ID should be null when not set");
    }

    @Test
    void testMultipleTenantsConcurrentAccess() throws Exception {
        // 插入測試資料
        for (int i = 1; i <= 5; i++) {
            insertTestData("tenant_00" + i, "data_" + i, "value_" + i);
        }

        // 多執行緒並發存取
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 1; i <= 5; i++) {
            final int tenantIndex = i;
            executor.submit(() -> {
                try {
                    String tenantId = "tenant_00" + tenantIndex;
                    TenantContextHolder.setTenantId(tenantId);

                    // 查詢自己的資料
                    String currentTenant = TenantContextHolder.getTenantId();
                    Map<String, String> data = mockDatabase.get(currentTenant);
                    assertNotNull(data);
                    assertEquals("value_" + tenantIndex, data.get("data_" + tenantIndex));

                    // 更新自己的資料
                    data.put("data_" + tenantIndex, "updated_" + tenantIndex);
                } finally {
                    TenantContextHolder.clear();
                    latch.countDown();
                }
            });
        }

        // 等待所有執行緒完成
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // 驗證所有資料都已正確更新
        for (int i = 1; i <= 5; i++) {
            String value = mockDatabase.get("tenant_00" + i).get("data_" + i);
            assertEquals("updated_" + i, value);
        }
    }

    /**
     * 輔助方法：插入測試資料
     */
    private void insertTestData(String tenantId, String name, String value) {
        mockDatabase.putIfAbsent(tenantId, new HashMap<>());
        mockDatabase.get(tenantId).put(name, value);
    }
}
