package com.rbac.common.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Request Log Filter - 記錄詳細的請求日誌資訊
 *
 * 此過濾器負責：
 * 1. 記錄請求的詳細資訊（方法、URI、參數、客戶端資訊）
 * 2. 測量請求處理時間
 * 3. 記錄響應資訊（狀態碼、響應大小）
 * 4. 避免記錄敏感資訊（如密碼、token）
 * 5. 使用適當的日誌級別（生產環境減少日誌量）
 *
 * 執行順序：Ordered.LOWEST_PRECEDENCE（在所有其他過濾器之後，確保能獲取完整資訊）
 *
 * 注意：此過濾器會包裝請求和響應以便讀取內容，可能會影響性能。
 * 在生產環境中應謹慎使用，並考慮只對特定路徑啟用詳細日誌。
 *
 * @author RBAC System
 * @since 1.0.0
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class RequestLogFilter extends OncePerRequestFilter {

    /**
     * 請求開始時間的請求屬性鍵
     */
    private static final String REQUEST_START_TIME = "requestStartTime";

    /**
     * 需要過濾掉的敏感參數名稱
     */
    private static final String[] SENSITIVE_PARAMS = {
        "password", "token", "authorization", "secret", "key", "credential"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 記錄請求開始時間
        long startTime = System.currentTimeMillis();
        request.setAttribute(REQUEST_START_TIME, startTime);

        // 包裝請求和響應以便讀取內容
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            // 記錄請求開始資訊
            logRequestStart(requestWrapper);

            // 繼續處理請求
            chain.doFilter(requestWrapper, responseWrapper);

            // 記錄請求完成資訊
            logRequestComplete(requestWrapper, responseWrapper, startTime);

        } catch (Exception e) {
            // 記錄請求失敗資訊
            logRequestError(requestWrapper, e, startTime);
            throw e;
        } finally {
            // 確保響應內容被寫出
            responseWrapper.copyBodyToResponse();
        }
    }

    /**
     * 記錄請求開始資訊
     *
     * @param request 請求包裝器
     */
    private void logRequestStart(ContentCachingRequestWrapper request) {
        if (!log.isInfoEnabled()) {
            return;
        }

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        // 構建完整URI
        String fullUri = uri + (queryString != null ? "?" + queryString : "");

        // 記錄基本請求資訊
        log.info("REQUEST START | {} {} | Client: {} | User-Agent: {}",
                method, fullUri, clientIp, truncateString(userAgent, 100));

        // 詳細調試日誌（只在 debug 級別）
        if (log.isDebugEnabled()) {
            // 記錄請求標頭（過濾敏感資訊）
            Map<String, String> safeHeaders = getSafeHeaders(request);
            log.debug("REQUEST HEADERS | {}", safeHeaders);

            // 記錄請求參數（過濾敏感資訊）
            Map<String, String[]> safeParams = getSafeParameters(request);
            if (!safeParams.isEmpty()) {
                log.debug("REQUEST PARAMS | {}", safeParams);
            }

            // 記錄請求體大小（如果有內容）
            int contentLength = request.getContentLength();
            if (contentLength > 0) {
                log.debug("REQUEST BODY SIZE | {} bytes", contentLength);
            }
        }
    }

    /**
     * 記錄請求完成資訊
     *
     * @param request 請求包裝器
     * @param response 響應包裝器
     * @param startTime 請求開始時間
     */
    private void logRequestComplete(ContentCachingRequestWrapper request,
                                   ContentCachingResponseWrapper response,
                                   long startTime) {
        if (!log.isInfoEnabled()) {
            return;
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        String method = request.getMethod();
        String uri = request.getRequestURI();
        int statusCode = response.getStatus();
        int responseSize = response.getContentSize();

        // 判斷日誌級別：正常響應用 info，錯誤響應用 warn
        if (statusCode >= 400) {
            log.warn("REQUEST COMPLETE | {} {} | Status: {} | Duration: {}ms | Response Size: {} bytes",
                    method, uri, statusCode, duration, responseSize);
        } else {
            log.info("REQUEST COMPLETE | {} {} | Status: {} | Duration: {}ms | Response Size: {} bytes",
                    method, uri, statusCode, duration, responseSize);
        }

        // 詳細調試日誌
        if (log.isDebugEnabled()) {
            // 記錄響應標頭（過濾敏感資訊）
            Map<String, String> safeResponseHeaders = getSafeResponseHeaders(response);
            if (!safeResponseHeaders.isEmpty()) {
                log.debug("RESPONSE HEADERS | {}", safeResponseHeaders);
            }
        }
    }

    /**
     * 記錄請求錯誤資訊
     *
     * @param request 請求包裝器
     * @param error 異常
     * @param startTime 請求開始時間
     */
    private void logRequestError(ContentCachingRequestWrapper request, Exception error, long startTime) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        String method = request.getMethod();
        String uri = request.getRequestURI();

        log.error("REQUEST ERROR | {} {} | Duration: {}ms | Error: {}",
                method, uri, duration, error.getMessage(), error);
    }

    /**
     * 獲取客戶端 IP 地址
     *
     * 考慮代理伺服器和負載均衡器
     *
     * @param request HTTP 請求
     * @return 客戶端 IP 地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // 檢查 X-Forwarded-For 標頭（代理伺服器）
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            // X-Forwarded-For 可能包含多個 IP，取第一個
            return xForwardedFor.split(",")[0].trim();
        }

        // 檢查 X-Real-IP 標頭（Nginx）
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.trim().isEmpty()) {
            return xRealIp.trim();
        }

        // 回退到遠端地址
        return request.getRemoteAddr();
    }

    /**
     * 獲取安全的請求標頭（過濾敏感資訊）
     *
     * @param request HTTP 請求
     * @return 安全的標頭映射
     */
    private Map<String, String> getSafeHeaders(HttpServletRequest request) {
        Map<String, String> safeHeaders = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            // 過濾敏感標頭
            if (!isSensitiveHeader(headerName)) {
                safeHeaders.put(headerName, truncateString(headerValue, 200));
            } else {
                safeHeaders.put(headerName, "[FILTERED]");
            }
        }

        return safeHeaders;
    }

    /**
     * 獲取安全的響應標頭（過濾敏感資訊）
     *
     * @param response HTTP 響應
     * @return 安全的標頭映射
     */
    private Map<String, String> getSafeResponseHeaders(ContentCachingResponseWrapper response) {
        Map<String, String> safeHeaders = new HashMap<>();
        Collection<String> headerNames = response.getHeaderNames();

        for (String headerName : headerNames) {
            String headerValue = response.getHeader(headerName);

            // 過濾敏感標頭
            if (!isSensitiveHeader(headerName)) {
                safeHeaders.put(headerName, truncateString(headerValue, 200));
            } else {
                safeHeaders.put(headerName, "[FILTERED]");
            }
        }

        return safeHeaders;
    }

    /**
     * 獲取安全的請求參數（過濾敏感資訊）
     *
     * @param request HTTP 請求
     * @return 安全的參數映射
     */
    private Map<String, String[]> getSafeParameters(HttpServletRequest request) {
        Map<String, String[]> safeParams = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();

        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);

            // 過濾敏感參數
            if (!isSensitiveParameter(paramName)) {
                // 截斷參數值以避免過長日誌
                String[] truncatedValues = new String[paramValues.length];
                for (int i = 0; i < paramValues.length; i++) {
                    truncatedValues[i] = truncateString(paramValues[i], 100);
                }
                safeParams.put(paramName, truncatedValues);
            } else {
                safeParams.put(paramName, new String[]{"[FILTERED]"});
            }
        }

        return safeParams;
    }

    /**
     * 判斷是否為敏感標頭
     *
     * @param headerName 標頭名稱
     * @return true 如果是敏感標頭
     */
    private boolean isSensitiveHeader(String headerName) {
        if (headerName == null) {
            return false;
        }

        String lowerHeader = headerName.toLowerCase();
        return lowerHeader.contains("authorization") ||
               lowerHeader.contains("token") ||
               lowerHeader.contains("cookie") ||
               lowerHeader.contains("set-cookie") ||
               lowerHeader.contains("authentication");
    }

    /**
     * 判斷是否為敏感參數
     *
     * @param paramName 參數名稱
     * @return true 如果是敏感參數
     */
    private boolean isSensitiveParameter(String paramName) {
        if (paramName == null) {
            return false;
        }

        String lowerParam = paramName.toLowerCase();
        for (String sensitive : SENSITIVE_PARAMS) {
            if (lowerParam.contains(sensitive)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 截斷字符串到指定長度
     *
     * @param str 原始字符串
     * @param maxLength 最大長度
     * @return 截斷後的字符串
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}