package com.rbac.common.core.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Audit information model.
 *
 * This class contains audit trail information for tracking who performed
 * what action and when, essential for compliance and security monitoring.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
@Data
public class AuditInfo {

    /**
     * ID of the user who performed the action.
     */
    private String userId;

    /**
     * Name of the user who performed the action.
     */
    private String userName;

    /**
     * IP address from which the action was performed.
     */
    private String ipAddress;

    /**
     * User agent string from the client.
     */
    private String userAgent;

    /**
     * Timestamp when the action was performed.
     */
    private LocalDateTime timestamp;

    /**
     * Session ID associated with the action.
     */
    private String sessionId;

    /**
     * Trace ID for distributed tracing.
     */
    private String traceId;

    /**
     * Tenant ID for multi-tenant context.
     */
    private String tenantId;

    /**
     * Default constructor.
     */
    public AuditInfo() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor with user ID and tenant ID.
     *
     * @param userId the user ID
     * @param tenantId the tenant ID
     */
    public AuditInfo(String userId, String tenantId) {
        this();
        this.userId = userId;
        this.tenantId = tenantId;
    }

    /**
     * Constructor with full audit information.
     *
     * @param userId the user ID
     * @param userName the user name
     * @param ipAddress the IP address
     * @param userAgent the user agent
     * @param sessionId the session ID
     * @param traceId the trace ID
     * @param tenantId the tenant ID
     */
    public AuditInfo(String userId, String userName, String ipAddress,
                    String userAgent, String sessionId, String traceId, String tenantId) {
        this(userId, tenantId);
        this.userName = userName;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.sessionId = sessionId;
        this.traceId = traceId;
    }

    /**
     * Create an AuditInfo instance with user ID and tenant ID.
     *
     * @param userId the user ID
     * @param tenantId the tenant ID
     * @return AuditInfo instance
     */
    public static AuditInfo of(String userId, String tenantId) {
        return new AuditInfo(userId, tenantId);
    }

    /**
     * Create an AuditInfo instance for system operations.
     *
     * @return AuditInfo instance for system
     */
    public static AuditInfo system() {
        AuditInfo auditInfo = new AuditInfo();
        auditInfo.setUserId("system");
        auditInfo.setUserName("System");
        auditInfo.setTenantId("system");
        return auditInfo;
    }

    /**
     * Create an AuditInfo instance for anonymous operations.
     *
     * @return AuditInfo instance for anonymous user
     */
    public static AuditInfo anonymous() {
        AuditInfo auditInfo = new AuditInfo();
        auditInfo.setUserId("anonymous");
        auditInfo.setUserName("Anonymous");
        return auditInfo;
    }

    /**
     * Check if this audit info represents a system operation.
     *
     * @return true if system operation, false otherwise
     */
    public boolean isSystemOperation() {
        return "system".equals(userId);
    }

    /**
     * Check if this audit info represents an anonymous operation.
     *
     * @return true if anonymous operation, false otherwise
     */
    public boolean isAnonymousOperation() {
        return "anonymous".equals(userId);
    }

    /**
     * Get a formatted audit string.
     *
     * @return formatted audit string
     */
    public String getFormattedAuditString() {
        return String.format("User: %s, IP: %s, Time: %s, Tenant: %s",
                           userId, ipAddress, timestamp, tenantId);
    }

    /**
     * Get audit summary for logging.
     *
     * @return audit summary string
     */
    public String getAuditSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Audit[");
        if (userId != null) summary.append("user=").append(userId);
        if (tenantId != null) summary.append(", tenant=").append(tenantId);
        if (ipAddress != null) summary.append(", ip=").append(ipAddress);
        if (traceId != null) summary.append(", trace=").append(traceId);
        summary.append("]");
        return summary.toString();
    }

    /**
     * Create a copy of this AuditInfo with a new timestamp.
     *
     * @return new AuditInfo instance with current timestamp
     */
    public AuditInfo withCurrentTimestamp() {
        AuditInfo copy = new AuditInfo(userId, userName, ipAddress, userAgent,
                                     sessionId, traceId, tenantId);
        copy.setTimestamp(LocalDateTime.now());
        return copy;
    }

    /**
     * Create a copy of this AuditInfo with additional context.
     *
     * @param ipAddress the IP address
     * @param userAgent the user agent
     * @param sessionId the session ID
     * @param traceId the trace ID
     * @return new AuditInfo instance with additional context
     */
    public AuditInfo withContext(String ipAddress, String userAgent, String sessionId, String traceId) {
        return new AuditInfo(userId, userName, ipAddress, userAgent, sessionId, traceId, tenantId);
    }
}