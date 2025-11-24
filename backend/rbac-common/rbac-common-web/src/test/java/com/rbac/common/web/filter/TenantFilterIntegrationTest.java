package com.rbac.common.web.filter;

import com.rbac.common.core.config.RbacProperties;
import com.rbac.common.database.context.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TenantFilter 整合測試
 * 測試 tenant_id 從 HTTP 請求中提取並設定到 TenantContextHolder，以及 ThreadLocal 清理
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
class TenantFilterIntegrationTest {

    private MockMvc mockMvc;
    private RbacProperties rbacProperties;

    @RestController
    static class TestController {
        @GetMapping("/test/tenant")
        public String getTenantId() {
            return TenantContextHolder.getTenantId();
        }
    }

    @BeforeEach
    void setUp() {
        // 清理租戶上下文
        TenantContextHolder.clear();

        // 建立模擬的 RbacProperties
        rbacProperties = new RbacProperties();
        rbacProperties.getTenant().setHeaderName("X-Tenant-Id");
        rbacProperties.getTenant().setEnabled(true);

        // 設定 MockMvc，包含 TenantFilter
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .addFilter(new TenantFilter(rbacProperties))
                .build();
    }

    @AfterEach
    void tearDown() {
        // 清理租戶上下文
        TenantContextHolder.clear();
    }

    @Test
    void testTenantIdExtractionFromHeader() throws Exception {
        // 測試從 X-Tenant-Id header 提取租戶 ID
        mockMvc.perform(get("/test/tenant")
                .header("X-Tenant-Id", "tenant_001"))
                .andExpect(status().isOk())
                .andExpect(content().string("tenant_001"));

        // 驗證請求後 ThreadLocal 已清理
        assertNull(TenantContextHolder.getTenantId(), "ThreadLocal should be cleared after request");
    }

    @Test
    void testTenantIdExtractionFromSubdomain() throws Exception {
        // 測試從子域名提取租戶 ID（模擬 tenant.example.com）
        mockMvc.perform(get("/test/tenant")
                .header("Host", "tenant_002.example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("tenant_002"));

        // 驗證請求後 ThreadLocal 已清理
        assertNull(TenantContextHolder.getTenantId(), "ThreadLocal should be cleared after request");
    }

    @Test
    void testMissingTenantIdReturnsNull() throws Exception {
        // 測試沒有提供租戶 ID 的情況 - 應該返回 400 錯誤
        mockMvc.perform(get("/test/tenant"))
                .andExpect(status().isBadRequest());

        // 驗證請求後 ThreadLocal 已清理
        assertNull(TenantContextHolder.getTenantId(), "ThreadLocal should be cleared after request");
    }

    @Test
    void testInvalidTenantIdFormat() throws Exception {
        // 測試無效的租戶 ID 格式（應被拒絕，返回 400 錯誤）
        mockMvc.perform(get("/test/tenant")
                .header("X-Tenant-Id", "invalid-tenant-id!@#"))
                .andExpect(status().isBadRequest());

        // 驗證請求後 ThreadLocal 已清理
        assertNull(TenantContextHolder.getTenantId(), "ThreadLocal should be cleared after request");
    }

    @Test
    void testConcurrentRequestsTenantIsolation() throws Exception {
        // 測試多個並發請求的租戶隔離
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 1; i <= 5; i++) {
            final int tenantIndex = i;
            executor.submit(() -> {
                try {
                    String tenantId = "tenant_00" + tenantIndex;

                    // 每個執行緒發送請求
                    mockMvc.perform(get("/test/tenant")
                            .header("X-Tenant-Id", tenantId))
                            .andExpect(status().isOk())
                            .andExpect(content().string(tenantId));

                    // 驗證請求後 ThreadLocal 已清理（防止洩漏到下一個請求）
                    assertNull(TenantContextHolder.getTenantId(),
                            "ThreadLocal should be cleared after request in thread " + tenantIndex);

                } catch (Exception e) {
                    fail("Request failed in thread " + tenantIndex + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有請求完成
        assertTrue(latch.await(10, TimeUnit.SECONDS), "All concurrent requests should complete within timeout");
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "Executor should shutdown cleanly");
    }

    @Test
    void testTenantContextCleanupOnException() throws Exception {
        // 測試當控制器拋出異常時，ThreadLocal 仍然被清理
        @RestController
        class ExceptionController {
            @GetMapping("/test/exception")
            public String throwException() {
                throw new RuntimeException("Test exception");
            }
        }

        // 重新設定 MockMvc 包含異常控制器
        mockMvc = MockMvcBuilders.standaloneSetup(new ExceptionController())
                .addFilter(new TenantFilter(rbacProperties))
                .build();

        // 發送會拋出異常的請求
        mockMvc.perform(get("/test/exception")
                .header("X-Tenant-Id", "tenant_exception"))
                .andExpect(status().isInternalServerError());

        // 即使發生異常，ThreadLocal 也應該被清理
        assertNull(TenantContextHolder.getTenantId(),
                "ThreadLocal should be cleared even when exception occurs");
    }

    @Test
    void testHeaderPriorityOverSubdomain() throws Exception {
        // 測試當同時提供 header 和 subdomain 時，header 優先
        mockMvc.perform(get("/test/tenant")
                .header("X-Tenant-Id", "tenant_header")
                .header("Host", "tenant_subdomain.example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("tenant_header"));

        // 驗證請求後 ThreadLocal 已清理
        assertNull(TenantContextHolder.getTenantId(), "ThreadLocal should be cleared after request");
    }
}