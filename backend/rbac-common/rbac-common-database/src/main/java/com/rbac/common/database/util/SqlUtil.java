package com.rbac.common.database.util;

import com.rbac.common.core.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * SQL 工具類
 * 提供 SQL 相關的通用工具方法，包括 SQL 注入防護、查詢構建、分頁處理等
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
@Slf4j
public class SqlUtil {

    /**
     * SQL 注入關鍵詞模式
     */
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)\\b(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript|onload|onerror)\\b"
    );

    /**
     * SQL 註釋模式
     */
    private static final Pattern SQL_COMMENT_PATTERN = Pattern.compile(
        "(/\\*.*?\\*/)|(--.*?$)|(#.*?$)", Pattern.MULTILINE | Pattern.DOTALL
    );

    /**
     * 私有構造函數，防止實例化
     */
    private SqlUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 檢查 SQL 參數是否包含潛在的 SQL 注入攻擊
     *
     * @param param 要檢查的參數
     * @return 如果安全返回 true，否則拋出異常
     * @throws SystemException 如果檢測到 SQL 注入風險
     */
    public static boolean checkSqlInjection(String param) {
        if (!StringUtils.hasText(param)) {
            return true;
        }

        // 檢查 SQL 注入關鍵詞
        if (SQL_INJECTION_PATTERN.matcher(param).find()) {
            log.warn("檢測到潛在的 SQL 注入攻擊，參數: {}", param);
            throw new SystemException("檢測到潛在的安全威脅，操作被拒絕");
        }

        // 檢查 SQL 註釋
        if (SQL_COMMENT_PATTERN.matcher(param).find()) {
            log.warn("檢測到 SQL 註釋，參數: {}", param);
            throw new SystemException("檢測到潛在的安全威脅，操作被拒絕");
        }

        return true;
    }

    /**
     * 清理和驗證排序字段
     * 只允許字母、數字、下劃線，並防止 SQL 注入
     *
     * @param orderBy 排序字段
     * @return 清理後的排序字段
     * @throws SystemException 如果排序字段無效
     */
    public static String cleanOrderBy(String orderBy) {
        if (!StringUtils.hasText(orderBy)) {
            return null;
        }

        // 移除所有空白字符
        String cleaned = orderBy.trim().replaceAll("\\s+", "");

        // 只允許字母、數字、下劃線和點號（用於表別名）
        if (!cleaned.matches("^[a-zA-Z0-9_.]+$")) {
            log.warn("無效的排序字段: {}", orderBy);
            throw new SystemException("無效的排序字段");
        }

        checkSqlInjection(cleaned);
        return cleaned;
    }

    /**
     * 清理和驗證排序方向
     *
     * @param orderDirection 排序方向 (ASC/DESC)
     * @return 清理後的排序方向
     * @throws SystemException 如果排序方向無效
     */
    public static String cleanOrderDirection(String orderDirection) {
        if (!StringUtils.hasText(orderDirection)) {
            return "ASC";
        }

        String upperDirection = orderDirection.trim().toUpperCase();
        if (!"ASC".equals(upperDirection) && !"DESC".equals(upperDirection)) {
            log.warn("無效的排序方向: {}", orderDirection);
            throw new SystemException("無效的排序方向，僅支援 ASC 或 DESC");
        }

        return upperDirection;
    }

    /**
     * 構建 ORDER BY 子句
     *
     * @param orderBy 排序字段
     * @param orderDirection 排序方向
     * @return ORDER BY 子句，如果參數無效則返回空字符串
     */
    public static String buildOrderByClause(String orderBy, String orderDirection) {
        try {
            String cleanOrderBy = cleanOrderBy(orderBy);
            String cleanDirection = cleanOrderDirection(orderDirection);

            if (cleanOrderBy != null) {
                return "ORDER BY " + cleanOrderBy + " " + cleanDirection;
            }
        } catch (SystemException e) {
            log.warn("構建 ORDER BY 子句失敗: {}", e.getMessage());
        }
        return "";
    }

    /**
     * 構建 LIMIT 子句
     *
     * @param pageSize 頁面大小
     * @param offset 偏移量
     * @return LIMIT 子句
     * @throws SystemException 如果參數無效
     */
    public static String buildLimitClause(Integer pageSize, Integer offset) {
        if (pageSize != null && pageSize <= 0) {
            throw new SystemException("頁面大小必須大於 0");
        }
        if (offset != null && offset < 0) {
            throw new SystemException("偏移量不能小於 0");
        }

        StringBuilder limitClause = new StringBuilder("LIMIT ");

        if (pageSize != null) {
            limitClause.append(pageSize);
        } else {
            limitClause.append("ALL");
        }

        if (offset != null && offset > 0) {
            limitClause.append(" OFFSET ").append(offset);
        }

        return limitClause.toString();
    }

    /**
     * 構建 IN 子句參數列表
     * 將集合轉換為 SQL IN 子句的參數格式
     *
     * @param values 值集合
     * @return IN 子句參數字符串，如 "(1,2,3)"
     */
    public static String buildInClause(List<?> values) {
        if (values == null || values.isEmpty()) {
            return "()";
        }

        StringBuilder inClause = new StringBuilder("(");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                inClause.append(",");
            }
            Object value = values.get(i);
            if (value instanceof String) {
                // 字符串類型需要引號
                inClause.append("'").append(value).append("'");
            } else {
                inClause.append(value);
            }
        }
        inClause.append(")");

        return inClause.toString();
    }

    /**
     * 轉義 LIKE 查詢的特殊字符
     * 防止 LIKE 查詢中的特殊字符造成問題
     *
     * @param value 要轉義的值
     * @return 轉義後的值
     */
    public static String escapeLikeValue(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        // 轉義 % 和 _ 字符
        return value.replace("%", "\\%").replace("_", "\\_");
    }

    /**
     * 構建 LIKE 查詢條件
     *
     * @param column 列名
     * @param value 查詢值
     * @param matchType 匹配類型：START(前綴)、END(後綴)、ANY(任意位置)
     * @return LIKE 條件字符串
     */
    public static String buildLikeCondition(String column, String value, LikeMatchType matchType) {
        if (!StringUtils.hasText(column) || !StringUtils.hasText(value)) {
            return "";
        }

        checkSqlInjection(column);
        String escapedValue = escapeLikeValue(value);

        String pattern;
        switch (matchType) {
            case START:
                pattern = escapedValue + "%";
                break;
            case END:
                pattern = "%" + escapedValue;
                break;
            case ANY:
            default:
                pattern = "%" + escapedValue + "%";
                break;
        }

        return column + " LIKE '" + pattern + "'";
    }

    /**
     * 驗證表名
     *
     * @param tableName 表名
     * @return 如果有效返回 true
     * @throws SystemException 如果表名無效
     */
    public static boolean validateTableName(String tableName) {
        if (!StringUtils.hasText(tableName)) {
            throw new SystemException("表名不能為空");
        }

        // 表名只能包含字母、數字、下劃線
        if (!tableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new SystemException("無效的表名格式");
        }

        checkSqlInjection(tableName);
        return true;
    }

    /**
     * 驗證列名
     *
     * @param columnName 列名
     * @return 如果有效返回 true
     * @throws SystemException 如果列名無效
     */
    public static boolean validateColumnName(String columnName) {
        if (!StringUtils.hasText(columnName)) {
            throw new SystemException("列名不能為空");
        }

        // 列名只能包含字母、數字、下劃線
        if (!columnName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new SystemException("無效的列名格式");
        }

        checkSqlInjection(columnName);
        return true;
    }

    /**
     * 分割逗號分隔的字符串為列表
     * 用於處理前端傳來的多選參數
     *
     * @param commaSeparatedString 逗號分隔的字符串
     * @return 字符串列表
     */
    public static List<String> splitCommaSeparatedString(String commaSeparatedString) {
        if (!StringUtils.hasText(commaSeparatedString)) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();
        String[] parts = commaSeparatedString.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (StringUtils.hasText(trimmed)) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * LIKE 匹配類型枚舉
     */
    public enum LikeMatchType {
        /**
         * 前綴匹配 (value%)
         */
        START,

        /**
         * 後綴匹配 (%value)
         */
        END,

        /**
         * 任意位置匹配 (%value%)
         */
        ANY
    }
}