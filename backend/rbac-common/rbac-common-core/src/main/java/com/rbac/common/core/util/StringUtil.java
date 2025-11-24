package com.rbac.common.core.util;

import com.rbac.common.core.constant.CommonConstant;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * String utility class.
 *
 * This class provides common string manipulation and validation methods
 * used throughout the RBAC system.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
public final class StringUtil {

    /**
     * Private constructor to prevent instantiation.
     */
    private StringUtil() {
        throw new UnsupportedOperationException("StringUtil class cannot be instantiated");
    }

    // ==================== Null/Empty Checks ====================

    /**
     * Check if a string is null or empty.
     *
     * @param str the string to check
     * @return true if null or empty, false otherwise
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if a string is not null and not empty.
     *
     * @param str the string to check
     * @return true if not null and not empty, false otherwise
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Check if a string is null, empty, or contains only whitespace.
     *
     * @param str the string to check
     * @return true if null, empty, or whitespace only, false otherwise
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if a string is not null, not empty, and contains non-whitespace characters.
     *
     * @param str the string to check
     * @return true if not null, not empty, and contains non-whitespace, false otherwise
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    // ==================== Default Values ====================

    /**
     * Return the string if not null/empty, otherwise return default value.
     *
     * @param str the string to check
     * @param defaultValue the default value
     * @return the string or default value
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    /**
     * Return the string if not null/blank, otherwise return default value.
     *
     * @param str the string to check
     * @param defaultValue the default value
     * @return the string or default value
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    // ==================== Trimming ====================

    /**
     * Trim the string if not null, otherwise return null.
     *
     * @param str the string to trim
     * @return trimmed string or null
     */
    public static String trim(String str) {
        return str != null ? str.trim() : null;
    }

    /**
     * Trim the string, returning empty string if null.
     *
     * @param str the string to trim
     * @return trimmed string or empty string
     */
    public static String trimToEmpty(String str) {
        return str != null ? str.trim() : CommonConstant.EMPTY;
    }

    /**
     * Trim the string, returning null if result is empty.
     *
     * @param str the string to trim
     * @return trimmed string or null
     */
    public static String trimToNull(String str) {
        String trimmed = trim(str);
        return isEmpty(trimmed) ? null : trimmed;
    }

    // ==================== Case Conversion ====================

    /**
     * Convert first character to uppercase.
     *
     * @param str the string to capitalize
     * @return capitalized string
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }

    /**
     * Convert first character to lowercase.
     *
     * @param str the string to uncapitalize
     * @return uncapitalized string
     */
    public static String uncapitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Convert camelCase to snake_case.
     *
     * @param str the camelCase string
     * @return snake_case string
     */
    public static String camelToSnake(String str) {
        if (isEmpty(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                result.append('_');
            }
            result.append(Character.toLowerCase(c));
        }
        return result.toString();
    }

    /**
     * Convert snake_case to camelCase.
     *
     * @param str the snake_case string
     * @return camelCase string
     */
    public static String snakeToCamel(String str) {
        if (isEmpty(str)) {
            return str;
        }
        String[] parts = str.split("_");
        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            result.append(capitalize(parts[i]));
        }
        return result.toString();
    }

    // ==================== Substring Operations ====================

    /**
     * Get substring before the first occurrence of separator.
     *
     * @param str the string
     * @param separator the separator
     * @return substring before separator, or original string if separator not found
     */
    public static String substringBefore(String str, String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        int index = str.indexOf(separator);
        return index == -1 ? str : str.substring(0, index);
    }

    /**
     * Get substring after the first occurrence of separator.
     *
     * @param str the string
     * @param separator the separator
     * @return substring after separator, or original string if separator not found
     */
    public static String substringAfter(String str, String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        int index = str.indexOf(separator);
        return index == -1 ? str : str.substring(index + separator.length());
    }

    /**
     * Get substring between two strings.
     *
     * @param str the string
     * @param open the opening string
     * @param close the closing string
     * @return substring between open and close, or null if not found
     */
    public static String substringBetween(String str, String open, String close) {
        if (isEmpty(str) || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start == -1) {
            return null;
        }
        start += open.length();
        int end = str.indexOf(close, start);
        if (end == -1) {
            return null;
        }
        return str.substring(start, end);
    }

    // ==================== Validation ====================

    /**
     * Check if string contains only alphabetic characters.
     *
     * @param str the string to check
     * @return true if alphabetic only, false otherwise
     */
    public static boolean isAlpha(String str) {
        return isNotEmpty(str) && str.matches("[a-zA-Z]+");
    }

    /**
     * Check if string contains only numeric characters.
     *
     * @param str the string to check
     * @return true if numeric only, false otherwise
     */
    public static boolean isNumeric(String str) {
        return isNotEmpty(str) && str.matches("\\d+");
    }

    /**
     * Check if string contains only alphanumeric characters.
     *
     * @param str the string to check
     * @return true if alphanumeric only, false otherwise
     */
    public static boolean isAlphanumeric(String str) {
        return isNotEmpty(str) && str.matches("[a-zA-Z0-9]+");
    }

    /**
     * Check if string is a valid email format.
     *
     * @param str the string to check
     * @return true if valid email, false otherwise
     */
    public static boolean isEmail(String str) {
        if (isEmpty(str)) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(str).matches();
    }

    // ==================== Collection to String ====================

    /**
     * Join collection elements with separator.
     *
     * @param collection the collection
     * @param separator the separator
     * @return joined string
     */
    public static String join(Collection<?> collection, String separator) {
        if (collection == null || collection.isEmpty()) {
            return CommonConstant.EMPTY;
        }
        return String.join(separator != null ? separator : CommonConstant.COMMA,
                          collection.stream().map(Object::toString).toArray(String[]::new));
    }

    /**
     * Join array elements with separator.
     *
     * @param array the array
     * @param separator the separator
     * @return joined string
     */
    public static String join(Object[] array, String separator) {
        if (array == null || array.length == 0) {
            return CommonConstant.EMPTY;
        }
        return String.join(separator != null ? separator : CommonConstant.COMMA,
                          Arrays.stream(array).map(Object::toString).toArray(String[]::new));
    }

    // ==================== String Formatting ====================

    /**
     * Format string with parameters.
     *
     * @param template the template string
     * @param args the arguments
     * @return formatted string
     */
    public static String format(String template, Object... args) {
        if (isEmpty(template)) {
            return template;
        }
        return String.format(template, args);
    }

    /**
     * Mask sensitive information in string (e.g., password, token).
     *
     * @param str the string to mask
     * @param visibleChars number of characters to keep visible at start
     * @return masked string
     */
    public static String mask(String str, int visibleChars) {
        if (isEmpty(str) || str.length() <= visibleChars + 2) {
            return str;
        }
        String start = str.substring(0, visibleChars);
        String end = str.substring(str.length() - 2);
        String mask = "*".repeat(Math.min(10, str.length() - visibleChars - 2));
        return start + mask + end;
    }

    /**
     * Generate a random string of specified length.
     *
     * @param length the length of the string
     * @return random string
     */
    public static String randomString(int length) {
        if (length <= 0) {
            return CommonConstant.EMPTY;
        }
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(random.nextInt(chars.length())));
        }
        return result.toString();
    }

    // ==================== Encoding/Decoding ====================

    /**
     * Convert string to bytes using UTF-8.
     *
     * @param str the string
     * @return byte array
     */
    public static byte[] toBytes(String str) {
        return str != null ? str.getBytes(StandardCharsets.UTF_8) : new byte[0];
    }

    /**
     * Convert bytes to string using UTF-8.
     *
     * @param bytes the byte array
     * @return string
     */
    public static String fromBytes(byte[] bytes) {
        return bytes != null ? new String(bytes, StandardCharsets.UTF_8) : null;
    }

    // ==================== Length Operations ====================

    /**
     * Get string length, returning 0 for null.
     *
     * @param str the string
     * @return length or 0
     */
    public static int length(String str) {
        return str != null ? str.length() : 0;
    }

    /**
     * Check if string length is within bounds.
     *
     * @param str the string
     * @param minLength minimum length
     * @param maxLength maximum length
     * @return true if within bounds, false otherwise
     */
    public static boolean isLengthBetween(String str, int minLength, int maxLength) {
        int length = length(str);
        return length >= minLength && length <= maxLength;
    }
}