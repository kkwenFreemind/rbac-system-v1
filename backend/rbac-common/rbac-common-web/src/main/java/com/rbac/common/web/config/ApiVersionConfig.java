package com.rbac.common.web.config;

import com.rbac.common.core.config.RbacProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API 版本控制配置類
 *
 * 此配置類提供靈活的 API 版本控制支援，主要功能：
 * 1. URL 路徑版本控制（/api/v1/ 格式）
 * 2. 請求標頭版本控制（X-API-Version）
 * 3. 請求參數版本控制（?version=v1）
 * 4. 版本兼容性檢查和降級處理
 * 5. 版本路由映射和路徑匹配優化
 *
 * 支援的版本控制策略：
 * - URL Path: /api/v1/users, /api/v2/users
 * - Header: X-API-Version: v1
 * - Parameter: /api/users?version=v1
 *
 * @author RBAC System
 * @since 1.0.0
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class ApiVersionConfig implements WebMvcConfigurer {

    private final RbacProperties rbacProperties;

    /**
     * 配置路徑匹配
     *
     * 設定 API 版本控制的路徑匹配規則：
     * 1. 啟用 URL 路徑版本控制時，配置版本路徑匹配
     * 2. 設定版本路徑模式和解析規則
     * 3. 配置版本兼容性檢查
     *
     * @param configurer 路徑匹配配置器
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // TODO: Implement API versioning configuration
        // RbacProperties.ApiVersionProperties apiVersionProps = rbacProperties.getApiVersion();

        // if (!apiVersionProps.isEnabled()) {
        //     log.info("API versioning is disabled");
        //     return;
        // }

        // log.info("Configuring API versioning with settings: enabled={}, defaultVersion={}, urlPathEnabled={}",
        //         apiVersionProps.isEnabled(), apiVersionProps.getDefaultVersion(), apiVersionProps.isUrlPathEnabled());

        // if (apiVersionProps.isUrlPathEnabled()) {
        //     // 配置 URL 路徑版本控制
        //     configureUrlPathVersioning(configurer, apiVersionProps);
        // }

        // log.info("API versioning configuration applied successfully");
    }

    /**
     * 配置 URL 路徑版本控制
     *
     * 設定基於 URL 路徑的版本控制規則：
     * - 支援 /api/v1/, /api/v2/ 等格式
     * - 自動解析版本號並設定到請求上下文中
     * - 提供版本降級和兼容性處理
     *
     * @param configurer 路徑匹配配置器
     * @param apiVersionProps API 版本配置屬性
     */
    private void configureUrlPathVersioning(PathMatchConfigurer configurer,
                                           Object apiVersionProps) {
        // TODO: Implement URL path versioning
        // // 使用自訂的路徑匹配器來處理版本控制
        // // Spring Boot 3.x 中可以通過 PathMatchConfigurer 來實現

        // log.info("URL path versioning enabled with base path: {}", apiVersionProps.getBasePath());

        // // 驗證支援的版本
        // validateSupportedVersions(apiVersionProps);

        // // 記錄版本配置信息
        // logSupportedVersions(apiVersionProps);
    }

    /**
     * 驗證支援的 API 版本
     *
     * 檢查配置的版本是否符合規範：
     * - 版本格式應為 v1, v2, v3 等
     * - 至少應包含預設版本
     * - 版本號應為正整數
     *
     * @param apiVersionProps API 版本配置屬性
     */
    private void validateSupportedVersions(Object apiVersionProps) {
        // TODO: Implement version validation
        // String defaultVersion = apiVersionProps.getDefaultVersion();

        // if (defaultVersion == null || defaultVersion.trim().isEmpty()) {
        //     throw new IllegalArgumentException("Default API version cannot be null or empty");
        // }

        // // 驗證預設版本格式
        // if (!isValidVersionFormat(defaultVersion)) {
        //     throw new IllegalArgumentException("Invalid default version format: " + defaultVersion +
        //                                      ". Expected format: v1, v2, v3, etc.");
        // }

        // // 如果沒有明確設定支援的版本，自動添加預設版本
        // if (apiVersionProps.getSupportedVersions() == null || apiVersionProps.getSupportedVersions().isEmpty()) {
        //     apiVersionProps.getSupportedVersions().add(defaultVersion);
        //     log.info("No supported versions specified, using default version: {}", defaultVersion);
        // } else {
        //     // 驗證所有支援的版本格式
        //     for (String version : apiVersionProps.getSupportedVersions()) {
        //         if (!isValidVersionFormat(version)) {
        //             throw new IllegalArgumentException("Invalid supported version format: " + version +
        //                                              ". Expected format: v1, v2, v3, etc.");
        //         }
        //     }

        //     // 確保預設版本在支援的版本列表中
        //     if (!apiVersionProps.getSupportedVersions().contains(defaultVersion)) {
        //         apiVersionProps.getSupportedVersions().add(defaultVersion);
        //         log.warn("Default version {} not in supported versions list, added automatically", defaultVersion);
        //     }
        // }
    }

    /**
     * 驗證版本格式
     *
     * 檢查版本字串是否符合規範格式：
     * - 應以 'v' 開頭
     * - 後跟一個或多個數字
     * - 例如：v1, v2, v10, v100
     *
     * @param version 版本字串
     * @return 是否為有效格式
     */
    private boolean isValidVersionFormat(String version) {
        if (version == null || version.trim().isEmpty()) {
            return false;
        }

        // 版本應以 'v' 開頭，後跟數字
        return version.matches("^v\\d+$");
    }

    /**
     * 記錄支援的版本信息
     *
     * 輸出當前配置的版本控制信息：
     * - 預設版本
     * - 支援的版本列表
     * - 版本控制策略
     *
     * @param apiVersionProps API 版本配置屬性
     */
    private void logSupportedVersions(Object apiVersionProps) {
        // TODO: Implement version logging
        // log.info("API Version Configuration:");
        // log.info("  - Default Version: {}", apiVersionProps.getDefaultVersion());
        // log.info("  - Supported Versions: {}", String.join(", ", apiVersionProps.getSupportedVersions()));
        // log.info("  - URL Path Enabled: {}", apiVersionProps.isUrlPathEnabled());
        // log.info("  - Header Name: {}", apiVersionProps.getHeaderName());
        // log.info("  - Parameter Name: {}", apiVersionProps.getParameterName());
        // log.info("  - Allow Unversioned Requests: {}", apiVersionProps.isAllowUnversionedRequests());
        // log.info("  - Base Path: {}", apiVersionProps.getBasePath());
    }

    // 擴展點：可以添加更多的 API 版本控制功能
    // 例如：
    // - 自訂版本解析器 (VersionResolver)
    // - 版本兼容性檢查器 (VersionCompatibilityChecker)
    // - 版本路由映射 (VersionedRequestMappingHandler)
    // - 版本降級策略 (VersionFallbackStrategy)
}