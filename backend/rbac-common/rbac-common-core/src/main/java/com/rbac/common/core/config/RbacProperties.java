package com.rbac.common.core.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

/**
 * RBAC System Configuration Properties
 *
 * Centralized configuration for all RBAC-related settings including
 * tenant isolation, caching, distributed locking, and audit features.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "rbac")
@Validated
@Data
public class RbacProperties {

    /**
     * Tenant-related configuration properties
     */
    @NotNull
    private TenantProperties tenant = new TenantProperties();

    /**
     * Cache-related configuration properties
     */
    @NotNull
    private CacheProperties cache = new CacheProperties();

    /**
     * Distributed lock configuration properties
     */
    @NotNull
    private LockProperties lock = new LockProperties();

    /**
     * Audit configuration properties
     */
    @NotNull
    private AuditProperties audit = new AuditProperties();

    /**
     * CORS configuration properties
     */
    @NotNull
    private CorsProperties cors = new CorsProperties();

    /**
     * API versioning configuration properties
     */
    @NotNull
    private ApiVersionProperties apiVersion = new ApiVersionProperties();

    /**
     * Tenant configuration properties
     */
    @Data
    public static class TenantProperties {
        /**
         * HTTP header name for tenant identification
         */
        private String headerName = "X-Tenant-Id";

        /**
         * Whether tenant isolation is enabled
         */
        private boolean enabled = true;

        /**
         * Tables excluded from tenant filtering
         */
        private List<String> excludedTables = new ArrayList<>();
    }

    /**
     * Cache configuration properties
     */
    @Data
    public static class CacheProperties {
        /**
         * Whether caching is enabled
         */
        private boolean enabled = true;

        /**
         * Default TTL for cache entries (seconds)
         */
        private int ttl = 1800;

        /**
         * Cache key prefix
         */
        private String prefix = "rbac";
    }

    /**
     * Distributed lock configuration properties
     */
    @Data
    public static class LockProperties {
        /**
         * Default lock timeout (seconds)
         */
        private int timeout = 30;

        /**
         * Number of retry attempts for lock acquisition
         */
        private int retryCount = 3;

        /**
         * Delay between retry attempts (milliseconds)
         */
        private int retryDelay = 100;
    }

    /**
     * Audit configuration properties
     */
    @Data
    public static class AuditProperties {
        /**
         * Whether audit logging is enabled
         */
        private boolean enabled = true;

        /**
         * Whether audit operations should be performed asynchronously
         */
        private boolean async = true;
    }

    /**
     * CORS configuration properties
     */
    @Data
    public static class CorsProperties {
        /**
         * Whether CORS is enabled
         */
        private boolean enabled = true;

        /**
         * Allowed origins for CORS requests
         */
        private List<String> allowedOrigins = new ArrayList<>();

        /**
         * Allowed HTTP methods for CORS requests
         */
        private List<String> allowedMethods = new ArrayList<>();

        /**
         * Allowed headers for CORS requests
         */
        private List<String> allowedHeaders = new ArrayList<>();

        /**
         * Whether credentials are allowed in CORS requests
         */
        private boolean allowCredentials = true;

        /**
         * Max age for preflight requests cache (seconds)
         */
        private long maxAge = 3600;

        /**
         * Exposed headers in CORS responses
         */
        private List<String> exposedHeaders = new ArrayList<>();
    }

    /**
     * API versioning configuration properties
     */
    @Data
    public static class ApiVersionProperties {
        /**
         * Whether API versioning is enabled
         */
        private boolean enabled = true;

        /**
         * Default API version
         */
        private String defaultVersion = "v1";

        /**
         * Supported API versions
         */
        private List<String> supportedVersions = new ArrayList<>();

        /**
         * API version header name
         */
        private String headerName = "X-API-Version";

        /**
         * API version parameter name
         */
        private String parameterName = "version";

        /**
         * Whether to include version in URL path
         */
        private boolean urlPathEnabled = true;

        /**
         * API base path
         */
        private String basePath = "/api";

        /**
         * Whether to allow unversioned requests (fallback to default version)
         */
        private boolean allowUnversionedRequests = true;
    }
}