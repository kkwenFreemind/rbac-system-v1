package com.rbac.common.web.context;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Trace ID 上下文管理器
 *
 * 此類提供對 Trace ID 的程式化訪問和 MDC（Mapped Diagnostic Context）操作的靜態包裝。
 * 主要用於：
 * 1. 獲取當前請求的 Trace ID（從 MDC）
 * 2. 設定 Trace ID 到 MDC（用於非 Web 請求場景，如異步任務）
 * 3. 生成新的 Trace ID
 * 4. 清理 Trace ID（防止線程池污染）
 *
 * 注意：此類依賴於 TraceIdFilter 自動設定 MDC。在非 Web 請求場景中，
 * 需要手動呼叫 setTraceId() 設定 Trace ID。
 *
 * 使用範例：
 * <pre>
 * // 獲取當前 Trace ID
 * String traceId = TraceContext.getTraceId();
 *
 * // 設定 Trace ID（異步任務場景）
 * TraceContext.setTraceId("550e8400-e29b-41d4-a716-446655440000");
 *
 * // 生成新 Trace ID
 * String newTraceId = TraceContext.generateTraceId();
 *
 * // 清理 Trace ID
 * TraceContext.clear();
 * </pre>
 *
 * @author RBAC System
 * @since 1.0.0
 */
public final class TraceContext {

    /**
     * MDC 中 Trace ID 的鍵名
     */
    public static final String TRACE_ID_KEY = "traceId";

    /**
     * 請求標頭中 Trace ID 的名稱
     */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * 私有建構函數，防止實例化
     */
    private TraceContext() {
        throw new UnsupportedOperationException("TraceContext is a utility class and cannot be instantiated");
    }

    /**
     * 獲取當前的 Trace ID
     *
     * 從 MDC 中獲取 Trace ID。如果 MDC 中沒有設定 Trace ID，則返回 null。
     * 在 Web 請求中，此值由 TraceIdFilter 自動設定。
     *
     * @return 當前 Trace ID，如果沒有設定則返回 null
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * 設定 Trace ID 到 MDC
     *
     * 將指定的 Trace ID 設定到 MDC 中。此方法主要用於非 Web 請求場景，
     * 如異步任務、定時任務等，需要手動設定 Trace ID 進行日誌追蹤。
     *
     * 注意：設定後必須在適當的時機呼叫 clear() 清理，以防止線程池污染。
     *
     * @param traceId Trace ID，不能為 null 或空字串
     * @throws IllegalArgumentException 如果 traceId 為 null 或空字串
     */
    public static void setTraceId(String traceId) {
        if (!StringUtils.hasText(traceId)) {
            throw new IllegalArgumentException("Trace ID cannot be null or empty");
        }
        MDC.put(TRACE_ID_KEY, traceId.trim());
    }

    /**
     * 生成新的 Trace ID
     *
     * 使用 UUID.randomUUID() 生成唯一的追蹤 ID。
     * 生成的 ID 格式為標準的 UUID 格式：8-4-4-4-12 十六進制字符。
     *
     * 注意：在分布式系統中，您可能想使用 Snowflake ID 或其他分布式 ID 生成策略
     * 以確保更好的性能和有序性。
     *
     * @return 新生成的 Trace ID
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 清理 Trace ID
     *
     * 從 MDC 中移除 Trace ID。此方法應該在手動設定 Trace ID 後呼叫，
     * 以防止線程池污染（舊的 Trace ID 影響後續請求的日誌）。
     *
     * 在 Web 請求中，此清理由 TraceIdFilter 自動處理。
     */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }

    /**
     * 檢查是否已設定 Trace ID
     *
     * @return true 如果 MDC 中已設定 Trace ID
     */
    public static boolean hasTraceId() {
        return StringUtils.hasText(getTraceId());
    }

    /**
     * 獲取或生成 Trace ID
     *
     * 如果 MDC 中已存在 Trace ID，則返回現有的；
     * 否則生成新的 Trace ID 並設定到 MDC 中。
     *
     * 此方法主要用於確保總是有 Trace ID 可用的場景。
     *
     * @return Trace ID（現有的或新生成的）
     */
    public static String getOrGenerateTraceId() {
        String existingTraceId = getTraceId();
        if (StringUtils.hasText(existingTraceId)) {
            return existingTraceId;
        }

        String newTraceId = generateTraceId();
        setTraceId(newTraceId);
        return newTraceId;
    }
}