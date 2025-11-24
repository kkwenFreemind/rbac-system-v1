package com.rbac.common.database.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 動態資料來源路由器
 *
 * 為未來讀寫分離預留的基礎架構
 * 目前僅支援單一資料來源，將來可以擴展為：
 * - 主庫（寫操作）
 * - 從庫（讀操作）
 * - 多個從庫的負載均衡
 *
 * 使用方式：
 * 1. 在需要切換資料來源的方法上標註 @Transactional(readOnly = true)
 * 2. 系統會自動路由到適當的資料來源
 *
 * @author RBAC System
 */
@Slf4j
public class DynamicDataSourceRouter extends AbstractRoutingDataSource {

    /**
     * 資料來源類型枚舉
     */
    public enum DataSourceType {
        /**
         * 主資料來源（寫操作）
         */
        MASTER("master"),

        /**
         * 從資料來源（讀操作）
         */
        SLAVE("slave");

        private final String value;

        DataSourceType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * ThreadLocal 儲存當前資料來源類型
     */
    private static final ThreadLocal<DataSourceType> CURRENT_DATA_SOURCE = new ThreadLocal<>();

    /**
     * 決定當前應該使用哪個資料來源
     *
     * @return 資料來源鍵
     */
    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType dataSourceType = CURRENT_DATA_SOURCE.get();

        if (dataSourceType == null) {
            // 預設使用主庫
            dataSourceType = DataSourceType.MASTER;
            log.debug("未指定資料來源類型，使用預設主庫");
        }

        log.debug("路由到資料來源：{}", dataSourceType.getValue());
        return dataSourceType.getValue();
    }

    /**
     * 設定當前執行緒的資料來源類型
     *
     * @param dataSourceType 資料來源類型
     */
    public static void setDataSourceType(DataSourceType dataSourceType) {
        CURRENT_DATA_SOURCE.set(dataSourceType);
        log.debug("設定資料來源類型：{}", dataSourceType.getValue());
    }

    /**
     * 獲取當前執行緒的資料來源類型
     *
     * @return 資料來源類型
     */
    public static DataSourceType getDataSourceType() {
        return CURRENT_DATA_SOURCE.get();
    }

    /**
     * 清除當前執行緒的資料來源類型設定
     * 必須在請求結束時呼叫以防止 ThreadLocal 洩漏
     */
    public static void clear() {
        CURRENT_DATA_SOURCE.remove();
        log.debug("清除資料來源類型設定");
    }

    /**
     * 強制使用主庫
     */
    public static void useMaster() {
        setDataSourceType(DataSourceType.MASTER);
    }

    /**
     * 強制使用從庫
     */
    public static void useSlave() {
        setDataSourceType(DataSourceType.SLAVE);
    }

    /**
     * 重置為預設行為（基於事務屬性自動選擇）
     */
    public static void reset() {
        clear();
    }
}