package com.rbac.common.web.filter;

import com.rbac.common.web.context.TraceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TraceIdFilter 整合測試
 *
 * 測試 Trace ID 過濾器的核心功能：
 * 1. 自動生成 Trace ID
 * 2. 從請求標頭提取現有 Trace ID
 * 3. Trace ID 添加到響應標頭
 * 4. Trace ID 設定到 MDC（Mapped Diagnostic Context）
 * 5. MDC 清理（防止線程池污染）
 * 6. 並發請求的 Trace ID 隔離
 *
 * @author RBAC System
 * @since 1.0.0
 */
class TraceIdFilterTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .addFilters(new TraceIdFilter())
                .build();
    }

    @AfterEach
    void tearDown() {
        // 確保每個測試後清理 MDC
        TraceContext.clear();
    }

    /**
     * 測試自動生成 Trace ID
     *
     * 驗證：
     * - 如果請求沒有 Trace ID，過濾器應該生成新的
     * - 響應標頭應該包含生成的 Trace ID
     * - Trace ID 格式應該是有效的 UUID
     */
    @Test
    void testTraceIdGeneration_ShouldGenerateNewTraceId() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/trace")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists(TraceContext.TRACE_ID_HEADER))
                .andReturn();

        // 驗證響應標頭中的 Trace ID
        String traceId = result.getResponse().getHeader(TraceContext.TRACE_ID_HEADER);
        assertThat(traceId).isNotNull();
        assertThat(traceId).isNotEmpty();

        // 驗證 Trace ID 格式（UUID 格式：8-4-4-4-12）
        assertThat(traceId).matches("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$");
    }

    /**
     * 測試從請求標頭提取 Trace ID
     *
     * 驗證：
     * - 如果請求已有 Trace ID，過濾器應該使用現有的
     * - 響應標頭應該包含相同的 Trace ID
     * - 支持分布式追蹤場景
     */
    @Test
    void testTraceIdExtraction_ShouldUseExistingTraceId() throws Exception {
        String existingTraceId = "550e8400-e29b-41d4-a716-446655440000";

        MvcResult result = mockMvc.perform(get("/test/trace")
                        .header(TraceContext.TRACE_ID_HEADER, existingTraceId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(TraceContext.TRACE_ID_HEADER, existingTraceId))
                .andReturn();

        // 驗證響應標頭中的 Trace ID 與請求的相同
        String responseTraceId = result.getResponse().getHeader(TraceContext.TRACE_ID_HEADER);
        assertThat(responseTraceId).isEqualTo(existingTraceId);
    }

    /**
     * 測試 Trace ID 在 MDC 中的可用性
     *
     * 驗證：
     * - 過濾器應該將 Trace ID 設定到 MDC
     * - 控制器可以通過 TraceContext.getTraceId() 獲取 Trace ID
     * - MDC 中的 Trace ID 與響應標頭中的一致
     */
    @Test
    void testTraceIdInMDC_ShouldBeAccessibleInController() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/trace-check")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 驗證控制器能夠從 MDC 中獲取 Trace ID
        String response = result.getResponse().getContentAsString();
        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
        assertThat(response).isNotEqualTo("null");

        // 驗證 MDC 中的 Trace ID 與響應標頭中的一致
        String headerTraceId = result.getResponse().getHeader(TraceContext.TRACE_ID_HEADER);
        assertThat(response).isEqualTo(headerTraceId);
    }

    /**
     * 測試 MDC 清理
     *
     * 驗證：
     * - 請求完成後，MDC 應該被清理
     * - 防止線程池污染
     * - 下一個請求不應該看到舊的 Trace ID
     */
    @Test
    void testMDCCleanup_ShouldClearTraceIdAfterRequest() throws Exception {
        // 第一個請求
        MvcResult result1 = mockMvc.perform(get("/test/trace")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists(TraceContext.TRACE_ID_HEADER))
                .andReturn();

        String traceId1 = result1.getResponse().getHeader(TraceContext.TRACE_ID_HEADER);

        // 驗證請求後 MDC 被清理
        assertThat(TraceContext.getTraceId()).isNull();

        // 第二個請求應該生成新的 Trace ID
        MvcResult result2 = mockMvc.perform(get("/test/trace")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists(TraceContext.TRACE_ID_HEADER))
                .andReturn();

        String traceId2 = result2.getResponse().getHeader(TraceContext.TRACE_ID_HEADER);

        // 驗證兩個 Trace ID 不同
        assertThat(traceId1).isNotEqualTo(traceId2);
    }

    /**
     * 測試異常情況下的 MDC 清理
     *
     * 驗證：
     * - 即使控制器拋出異常，MDC 也應該被清理
     * - 防止線程池污染
     */
    @Test
    void testMDCCleanupOnException_ShouldClearTraceIdEvenOnError() throws Exception {
        // 請求會拋出異常，但我們仍然檢查 Trace ID 是否存在
        try {
            mockMvc.perform(get("/test/exception")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().exists(TraceContext.TRACE_ID_HEADER));
        } catch (Exception e) {
            // 預期會拋出異常，這是正常的
        }

        // 驗證即使發生異常，MDC 也被清理
        assertThat(TraceContext.getTraceId()).isNull();
    }

    /**
     * 測試無效 Trace ID 格式的處理
     *
     * 驗證：
     * - 如果請求提供的 Trace ID 格式無效，過濾器應該生成新的
     * - 響應標頭應該包含新生成的 Trace ID
     * - 新生成的 Trace ID 應該與請求的不同
     */
    @Test
    void testInvalidTraceIdFormat_ShouldGenerateNew() throws Exception {
        String invalidTraceId = "invalid-trace-id-with-special-chars-!@#$";

        MvcResult result = mockMvc.perform(get("/test/trace")
                        .header(TraceContext.TRACE_ID_HEADER, invalidTraceId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists(TraceContext.TRACE_ID_HEADER))
                .andReturn();

        String responseTraceId = result.getResponse().getHeader(TraceContext.TRACE_ID_HEADER);

        // 驗證生成了新的 Trace ID
        assertThat(responseTraceId).isNotEqualTo(invalidTraceId);
        assertThat(responseTraceId).matches("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$");
    }

    /**
     * 測試空字串 Trace ID 的處理
     *
     * 驗證：
     * - 如果請求提供空字串 Trace ID，過濾器應該生成新的
     * - 響應標頭應該包含新生成的有效 Trace ID
     */
    @Test
    void testEmptyTraceId_ShouldGenerateNew() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/trace")
                        .header(TraceContext.TRACE_ID_HEADER, "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists(TraceContext.TRACE_ID_HEADER))
                .andReturn();

        String responseTraceId = result.getResponse().getHeader(TraceContext.TRACE_ID_HEADER);

        // 驗證生成了新的有效 Trace ID
        assertThat(responseTraceId).isNotEmpty();
        assertThat(responseTraceId).matches("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$");
    }

    /**
     * 測試並發請求的 Trace ID 隔離
     *
     * 驗證：
     * - 多個並發請求應該有各自獨立的 Trace ID
     * - 不同請求的 Trace ID 不應該相互干擾
     * - 驗證 ThreadLocal 的線程隔離特性
     */
    @Test
    void testConcurrentRequestsTraceIdIsolation() throws Exception {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        List<String> traceIds = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 等待所有線程準備好
                    startLatch.await();

                    // 執行請求
                    MvcResult result = mockMvc.perform(get("/test/trace")
                                    .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk())
                            .andExpect(header().exists(TraceContext.TRACE_ID_HEADER))
                            .andReturn();

                    // 收集 Trace ID
                    String traceId = result.getResponse().getHeader(TraceContext.TRACE_ID_HEADER);
                    traceIds.add(traceId);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 啟動所有線程
        startLatch.countDown();

        // 等待所有線程完成
        boolean completed = endLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(completed).isTrue();
        assertThat(traceIds).hasSize(threadCount);

        // 驗證所有 Trace ID 都不同
        long uniqueTraceIds = traceIds.stream().distinct().count();
        assertThat(uniqueTraceIds).isEqualTo(threadCount);

        // 驗證每個 Trace ID 格式都有效
        for (String traceId : traceIds) {
            assertThat(traceId).matches("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$");
        }
    }

    /**
     * 測試控制器
     *
     * 用於模擬真實的 HTTP 請求場景
     */
    @RestController
    static class TestController {

        /**
         * 基本測試端點
         */
        @GetMapping("/test/trace")
        public String testTrace() {
            return "OK";
        }

        /**
         * 檢查 MDC 中的 Trace ID
         */
        @GetMapping("/test/trace-check")
        public String checkTraceId() {
            String traceId = TraceContext.getTraceId();
            return traceId != null ? traceId : "null";
        }

        /**
         * 拋出異常的端點
         */
        @GetMapping("/test/exception")
        public void throwException() {
            throw new RuntimeException("Test exception for Trace ID cleanup");
        }
    }
}
