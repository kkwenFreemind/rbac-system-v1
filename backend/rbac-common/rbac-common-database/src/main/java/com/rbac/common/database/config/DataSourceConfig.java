package com.rbac.common.database.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 資料庫連線池配置
 *
 * 配置 HikariCP 連線池，針對 PostgreSQL 優化
 * 支援連線池監控和效能調優
 *
 * @author RBAC System
 */
@Configuration
@Slf4j
public class DataSourceConfig {

    @Value("${spring.datasource.url:}")
    private String url;

    @Value("${spring.datasource.username:}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    // HikariCP 連線池配置
    @Value("${spring.datasource.hikari.maximum-pool-size:10}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long maxLifetime;

    @Value("${spring.datasource.hikari.connection-test-query:SELECT 1}")
    private String connectionTestQuery;

    @Value("${spring.datasource.hikari.pool-name:RBAC-HikariPool}")
    private String poolName;

    /**
     * 配置主資料來源
     *
     * @return HikariDataSource 實例
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("初始化 HikariCP 連線池配置");

        HikariDataSource dataSource = new HikariDataSource();

        // 基本連線資訊
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);

        // 連線池配置
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(minimumIdle);
        dataSource.setConnectionTimeout(connectionTimeout);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setMaxLifetime(maxLifetime);

        // 連線測試
        dataSource.setConnectionTestQuery(connectionTestQuery);

        // 連線池名稱（用於監控）
        dataSource.setPoolName(poolName);

        // PostgreSQL 特定優化
        dataSource.addDataSourceProperty("cachePrepStmts", "true");
        dataSource.addDataSourceProperty("prepStmtCacheSize", "250");
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource.addDataSourceProperty("useServerPrepStmts", "true");

        // 連線洩漏檢測（開發環境）
        dataSource.setLeakDetectionThreshold(60000); // 60 秒

        log.info("HikariCP 連線池配置完成：poolName={}, maxPoolSize={}, minIdle={}",
                poolName, maximumPoolSize, minimumIdle);

        return dataSource;
    }
}