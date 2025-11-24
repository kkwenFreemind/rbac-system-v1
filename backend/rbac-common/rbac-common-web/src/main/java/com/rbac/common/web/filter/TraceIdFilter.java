package com.rbac.common.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Trace ID Filter - 生成並傳遞 Trace ID 用於日誌串聯
 *
 * 此過濾器負責：
 * 1. 從請求標頭提取現有的 Trace ID（如果有）
 * 2. 如果沒有 Trace ID，生成新的 UUID 作為 Trace ID
 * 3. 將 Trace ID 設定到 MDC（Mapped Diagnostic Context），讓所有日誌自動包含 Trace ID
 * 4. 將 Trace ID 添加到響應標頭，供客戶端追蹤
 * 5. 在請求結束時清理 MDC 以防止線程池污染
 *
 * 執行順序：HIGHEST_PRECEDENCE + 10（在 TenantFilter 之後，確保 Trace ID 可用於後續所有日誌）
 *
 * @author RBAC System
 * @since 1.0.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@Slf4j
public class TraceIdFilter extends OncePerRequestFilter {

    /**
     * MDC 中 Trace ID 的鍵名
     */
    public static final String TRACE_ID_KEY = "traceId";

    /**
     * 請求標頭中 Trace ID 的名稱
     */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String traceId = null;

        try {
            // 1. 提取或生成 Trace ID
            traceId = extractOrGenerateTraceId(request);

            // 2. 設定到 MDC（所有日誌會自動包含此值）
            MDC.put(TRACE_ID_KEY, traceId);

            // 3. 將 Trace ID 添加到響應標頭（供客戶端追蹤）
            response.setHeader(TRACE_ID_HEADER, traceId);

            // 4. 記錄請求開始（此日誌會自動包含 traceId）
            log.debug("Request started: {} {} (Trace ID: {})", 
                     request.getMethod(), request.getRequestURI(), traceId);

            // 5. 繼續處理請求
            chain.doFilter(request, response);

            // 6. 記錄請求完成
            log.debug("Request completed: {} {} (Trace ID: {})", 
                     request.getMethod(), request.getRequestURI(), traceId);

        } catch (Exception e) {
            // 記錄異常（會自動包含 traceId）
            log.error("Request failed: {} {} (Trace ID: {}) - Error: {}", 
                     request.getMethod(), request.getRequestURI(), traceId, e.getMessage(), e);
            throw e;
        } finally {
            // 關鍵：清理 MDC，防止線程池污染
            // 如果不清理，下一個使用此線程的請求會看到舊的 Trace ID
            MDC.remove(TRACE_ID_KEY);
        }
    }

    /**
     * 從請求中提取現有的 Trace ID，如果沒有則生成新的
     *
     * 優先順序：
     * 1. 請求標頭（X-Trace-Id）- 支持分布式追蹤場景
     * 2. 生成新的 UUID - 本地追蹤場景
     *
     * @param request HTTP 請求
     * @return Trace ID
     */
    private String extractOrGenerateTraceId(HttpServletRequest request) {
        // 選項 1：從請求標頭提取（支持分布式追蹤）
        String traceId = request.getHeader(TRACE_ID_HEADER);

        if (traceId != null && !traceId.trim().isEmpty()) {
            // 驗證格式（可選）
            if (isValidTraceIdFormat(traceId)) {
                log.debug("Extracted existing Trace ID from header: {}", traceId);
                return traceId.trim();
            } else {
                log.warn("Invalid Trace ID format in header: {}, generating new one", traceId);
            }
        }

        // 選項 2：生成新的 Trace ID
        String newTraceId = generateTraceId();
        log.debug("Generated new Trace ID: {}", newTraceId);
        return newTraceId;
    }

    /**
     * 生成新的 Trace ID
     *
     * 使用 UUID.randomUUID() 生成唯一的追蹤 ID
     * 格式：8-4-4-4-12 十六進制字符（例如：550e8400-e29b-41d4-a716-446655440000）
     *
     * 注意：在分布式系統中，您可能想使用 Snowflake ID 或其他分布式 ID 生成策略
     * 以確保更好的性能和有序性。
     *
     * @return 新的 Trace ID
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 驗證 Trace ID 格式
     *
     * 基本驗證：
     * - 不為空
     * - 長度合理（1-100 個字符）
     * - 只包含字母、數字和連字號
     *
     * 注意：UUID 格式為 8-4-4-4-12，總長度為 36 個字符
     *
     * @param traceId Trace ID
     * @return true 如果格式有效
     */
    private boolean isValidTraceIdFormat(String traceId) {
        if (traceId == null || traceId.trim().isEmpty()) {
            return false;
        }

        // 長度限制：1-100 個字符
        if (traceId.length() > 100) {
            return false;
        }

        // 基本驗證：只允許字母、數字和連字號
        return traceId.matches("^[a-zA-Z0-9-]+$");
    }
}
