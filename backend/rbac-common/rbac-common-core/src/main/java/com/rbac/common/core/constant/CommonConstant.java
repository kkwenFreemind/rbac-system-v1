package com.rbac.common.core.constant;

/**
 * Common system constants used across all modules.
 *
 * This class contains system-wide constants for pagination, encoding,
 * date formats, and other common values used throughout the RBAC system.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
public final class CommonConstant {

    /**
     * Private constructor to prevent instantiation.
     */
    private CommonConstant() {
        throw new UnsupportedOperationException("CommonConstant class cannot be instantiated");
    }

    // ==================== Pagination Constants ====================

    /**
     * Default page number for pagination (1-based).
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * Default page size for pagination.
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * Maximum allowed page size to prevent performance issues.
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Minimum allowed page size.
     */
    public static final int MIN_PAGE_SIZE = 1;

    // ==================== Encoding Constants ====================

    /**
     * UTF-8 character encoding.
     */
    public static final String UTF8 = "UTF-8";

    /**
     * ISO-8859-1 character encoding.
     */
    public static final String ISO_8859_1 = "ISO-8859-1";

    // ==================== Date/Time Format Constants ====================

    /**
     * Standard date format (yyyy-MM-dd).
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Standard datetime format (yyyy-MM-dd HH:mm:ss).
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Standard datetime with milliseconds format (yyyy-MM-dd HH:mm:ss.SSS).
     */
    public static final String DATETIME_MS_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * Standard time format (HH:mm:ss).
     */
    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * Standard datetime with timezone format (yyyy-MM-dd HH:mm:ss Z).
     */
    public static final String DATETIME_TZ_FORMAT = "yyyy-MM-dd HH:mm:ss Z";

    // ==================== Boolean String Constants ====================

    /**
     * String representation of true.
     */
    public static final String TRUE = "true";

    /**
     * String representation of false.
     */
    public static final String FALSE = "false";

    /**
     * String representation of yes.
     */
    public static final String YES = "Y";

    /**
     * String representation of no.
     */
    public static final String NO = "N";

    // ==================== Common Symbols ====================

    /**
     * Comma separator.
     */
    public static final String COMMA = ",";

    /**
     * Semicolon separator.
     */
    public static final String SEMICOLON = ";";

    /**
     * Colon separator.
     */
    public static final String COLON = ":";

    /**
     * Dot separator.
     */
    public static final String DOT = ".";

    /**
     * Hyphen separator.
     */
    public static final String HYPHEN = "-";

    /**
     * Underscore separator.
     */
    public static final String UNDERSCORE = "_";

    /**
     * Empty string.
     */
    public static final String EMPTY = "";

    /**
     * Space character.
     */
    public static final String SPACE = " ";

    // ==================== HTTP Constants ====================

    /**
     * HTTP GET method.
     */
    public static final String HTTP_GET = "GET";

    /**
     * HTTP POST method.
     */
    public static final String HTTP_POST = "POST";

    /**
     * HTTP PUT method.
     */
    public static final String HTTP_PUT = "PUT";

    /**
     * HTTP DELETE method.
     */
    public static final String HTTP_DELETE = "DELETE";

    /**
     * HTTP PATCH method.
     */
    public static final String HTTP_PATCH = "PATCH";

    // ==================== File Constants ====================

    /**
     * Default file buffer size (8KB).
     */
    public static final int BUFFER_SIZE = 8192;

    /**
     * Maximum file name length.
     */
    public static final int MAX_FILE_NAME_LENGTH = 255;

    // ==================== Cache Constants ====================

    /**
     * Default cache expiration time in seconds (30 minutes).
     */
    public static final int DEFAULT_CACHE_TTL = 1800;

    /**
     * Maximum cache expiration time in seconds (24 hours).
     */
    public static final int MAX_CACHE_TTL = 86400;

    // ==================== Lock Constants ====================

    /**
     * Default lock timeout in seconds.
     */
    public static final int DEFAULT_LOCK_TIMEOUT = 30;

    /**
     * Maximum lock timeout in seconds (5 minutes).
     */
    public static final int MAX_LOCK_TIMEOUT = 300;

    // ==================== Database Constants ====================

    /**
     * Logical delete flag - not deleted.
     */
    public static final int NOT_DELETED = 0;

    /**
     * Logical delete flag - deleted.
     */
    public static final int DELETED = 1;

    // ==================== System Constants ====================

    /**
     * System user ID for automated operations.
     */
    public static final String SYSTEM_USER_ID = "system";

    /**
     * Anonymous user ID for unauthenticated operations.
     */
    public static final String ANONYMOUS_USER_ID = "anonymous";

    /**
     * Default tenant ID for system operations.
     */
    public static final String DEFAULT_TENANT_ID = "default";

    // ==================== Validation Constants ====================

    /**
     * Minimum password length.
     */
    public static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * Maximum password length.
     */
    public static final int MAX_PASSWORD_LENGTH = 128;

    /**
     * Minimum username length.
     */
    public static final int MIN_USERNAME_LENGTH = 3;

    /**
     * Maximum username length.
     */
    public static final int MAX_USERNAME_LENGTH = 50;

    // ==================== API Constants ====================

    /**
     * API version prefix.
     */
    public static final String API_VERSION_PREFIX = "/api/v1";

    /**
     * Default API timeout in milliseconds (30 seconds).
     */
    public static final int DEFAULT_API_TIMEOUT = 30000;

    // ==================== Logging Constants ====================

    /**
     * MDC key for trace ID.
     */
    public static final String MDC_TRACE_ID = "traceId";

    /**
     * MDC key for tenant ID.
     */
    public static final String MDC_TENANT_ID = "tenantId";

    /**
     * MDC key for user ID.
     */
    public static final String MDC_USER_ID = "userId";
}