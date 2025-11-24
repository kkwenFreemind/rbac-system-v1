package com.rbac.common.core.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StringUtil class.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
class StringUtilTest {

    @Test
    void testIsEmpty() {
        assertTrue(StringUtil.isEmpty(null));
        assertTrue(StringUtil.isEmpty(""));
        assertTrue(StringUtil.isEmpty("   "));
        assertFalse(StringUtil.isEmpty("test"));
        assertFalse(StringUtil.isEmpty(" test "));
    }

    @Test
    void testIsNotEmpty() {
        assertFalse(StringUtil.isNotEmpty(null));
        assertFalse(StringUtil.isNotEmpty(""));
        assertFalse(StringUtil.isNotEmpty("   "));
        assertTrue(StringUtil.isNotEmpty("test"));
        assertTrue(StringUtil.isNotEmpty(" test "));
    }

    @Test
    void testIsBlank() {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank(""));
        assertTrue(StringUtil.isBlank("   "));
        assertTrue(StringUtil.isBlank("\t\n\r"));
        assertFalse(StringUtil.isBlank("test"));
        assertFalse(StringUtil.isBlank(" test "));
    }

    @Test
    void testIsNotBlank() {
        assertFalse(StringUtil.isNotBlank(null));
        assertFalse(StringUtil.isNotBlank(""));
        assertFalse(StringUtil.isNotBlank("   "));
        assertTrue(StringUtil.isNotBlank("test"));
        assertTrue(StringUtil.isNotBlank(" test "));
    }

    @Test
    void testDefaultIfEmpty() {
        assertEquals("default", StringUtil.defaultIfEmpty(null, "default"));
        assertEquals("default", StringUtil.defaultIfEmpty("", "default"));
        assertEquals("default", StringUtil.defaultIfEmpty("   ", "default"));
        assertEquals("test", StringUtil.defaultIfEmpty("test", "default"));
    }

    @Test
    void testDefaultIfBlank() {
        assertEquals("default", StringUtil.defaultIfBlank(null, "default"));
        assertEquals("default", StringUtil.defaultIfBlank("", "default"));
        assertEquals("default", StringUtil.defaultIfBlank("   ", "default"));
        assertEquals("test", StringUtil.defaultIfBlank("test", "default"));
    }

    @Test
    void testTrim() {
        assertNull(StringUtil.trim(null));
        assertEquals("", StringUtil.trim(""));
        assertEquals("", StringUtil.trim("   "));
        assertEquals("test", StringUtil.trim("test"));
        assertEquals("test", StringUtil.trim(" test "));
        assertEquals("test", StringUtil.trim("  test  "));
    }

    @Test
    void testTrimToEmpty() {
        assertEquals("", StringUtil.trimToEmpty(null));
        assertEquals("", StringUtil.trimToEmpty(""));
        assertEquals("", StringUtil.trimToEmpty("   "));
        assertEquals("test", StringUtil.trimToEmpty("test"));
        assertEquals("test", StringUtil.trimToEmpty(" test "));
    }

    @Test
    void testTrimToNull() {
        assertNull(StringUtil.trimToNull(null));
        assertNull(StringUtil.trimToNull(""));
        assertNull(StringUtil.trimToNull("   "));
        assertEquals("test", StringUtil.trimToNull("test"));
        assertEquals("test", StringUtil.trimToNull(" test "));
    }

    @Test
    void testCapitalize() {
        assertNull(StringUtil.capitalize(null));
        assertEquals("", StringUtil.capitalize(""));
        assertEquals("Test", StringUtil.capitalize("test"));
        assertEquals("Test", StringUtil.capitalize("TEST"));
        assertEquals("Test", StringUtil.capitalize("Test"));
    }

    @Test
    void testUncapitalize() {
        assertNull(StringUtil.uncapitalize(null));
        assertEquals("", StringUtil.uncapitalize(""));
        assertEquals("test", StringUtil.uncapitalize("Test"));
        assertEquals("tEST", StringUtil.uncapitalize("TEST"));
        assertEquals("test", StringUtil.uncapitalize("test"));
    }

    @Test
    void testCamelToSnake() {
        assertNull(StringUtil.camelToSnake(null));
        assertEquals("", StringUtil.camelToSnake(""));
        assertEquals("user_name", StringUtil.camelToSnake("userName"));
        assertEquals("user_id", StringUtil.camelToSnake("userId"));
        assertEquals("user", StringUtil.camelToSnake("user"));
        assertEquals("i_o_exception", StringUtil.camelToSnake("IOException"));
    }

    @Test
    void testSnakeToCamel() {
        assertNull(StringUtil.snakeToCamel(null));
        assertEquals("", StringUtil.snakeToCamel(""));
        assertEquals("userName", StringUtil.snakeToCamel("user_name"));
        assertEquals("userId", StringUtil.snakeToCamel("user_id"));
        assertEquals("user", StringUtil.snakeToCamel("user"));
    }

    @Test
    void testSubstringBefore() {
        assertNull(StringUtil.substringBefore(null, "."));
        assertEquals("", StringUtil.substringBefore("", "."));
        assertEquals("user", StringUtil.substringBefore("user.name", "."));
        assertEquals("user", StringUtil.substringBefore("user.name.first", "."));
        assertEquals("user.name", StringUtil.substringBefore("user.name", null));
        assertEquals("user.name", StringUtil.substringBefore("user.name", "@"));
    }

    @Test
    void testSubstringAfter() {
        assertNull(StringUtil.substringAfter(null, "."));
        assertEquals("", StringUtil.substringAfter("", "."));
        assertEquals("name", StringUtil.substringAfter("user.name", "."));
        assertEquals("name.first", StringUtil.substringAfter("user.name.first", "."));
        assertEquals("user.name", StringUtil.substringAfter("user.name", null));
        assertEquals("user.name", StringUtil.substringAfter("user.name", "@"));
    }

    @Test
    void testSubstringBetween() {
        assertNull(StringUtil.substringBetween(null, "[", "]"));
        assertNull(StringUtil.substringBetween("", "[", "]"));
        assertNull(StringUtil.substringBetween("test", "[", "]"));
        assertNull(StringUtil.substringBetween("[test", "[", "]"));
        assertNull(StringUtil.substringBetween("test]", "[", "]"));
        assertEquals("test", StringUtil.substringBetween("[test]", "[", "]"));
        assertEquals("test", StringUtil.substringBetween("prefix[test]suffix", "[", "]"));
        assertEquals("", StringUtil.substringBetween("[]", "[", "]"));
    }

    @Test
    void testIsAlpha() {
        assertFalse(StringUtil.isAlpha(null));
        assertFalse(StringUtil.isAlpha(""));
        assertTrue(StringUtil.isAlpha("abc"));
        assertTrue(StringUtil.isAlpha("ABC"));
        assertTrue(StringUtil.isAlpha("abcABC"));
        assertFalse(StringUtil.isAlpha("abc123"));
        assertFalse(StringUtil.isAlpha("abc "));
        assertFalse(StringUtil.isAlpha("abc-def"));
    }

    @Test
    void testIsNumeric() {
        assertFalse(StringUtil.isNumeric(null));
        assertFalse(StringUtil.isNumeric(""));
        assertTrue(StringUtil.isNumeric("123"));
        assertTrue(StringUtil.isNumeric("0"));
        assertFalse(StringUtil.isNumeric("12.3"));
        assertFalse(StringUtil.isNumeric("abc"));
        assertFalse(StringUtil.isNumeric("123 "));
        assertFalse(StringUtil.isNumeric("12a3"));
    }

    @Test
    void testIsAlphanumeric() {
        assertFalse(StringUtil.isAlphanumeric(null));
        assertFalse(StringUtil.isAlphanumeric(""));
        assertTrue(StringUtil.isAlphanumeric("abc"));
        assertTrue(StringUtil.isAlphanumeric("123"));
        assertTrue(StringUtil.isAlphanumeric("abc123"));
        assertTrue(StringUtil.isAlphanumeric("ABC123"));
        assertFalse(StringUtil.isAlphanumeric("abc "));
        assertFalse(StringUtil.isAlphanumeric("abc-123"));
        assertFalse(StringUtil.isAlphanumeric("abc_123"));
    }

    @Test
    void testIsEmail() {
        assertFalse(StringUtil.isEmail(null));
        assertFalse(StringUtil.isEmail(""));
        assertTrue(StringUtil.isEmail("test@example.com"));
        assertTrue(StringUtil.isEmail("user.name@example.com"));
        assertTrue(StringUtil.isEmail("user+tag@example.co.uk"));
        assertFalse(StringUtil.isEmail("invalid"));
        assertFalse(StringUtil.isEmail("@example.com"));
        assertFalse(StringUtil.isEmail("user@"));
        assertFalse(StringUtil.isEmail("user@.com"));
        assertFalse(StringUtil.isEmail("user name@example.com"));
    }

    @Test
    void testJoinCollection() {
        assertEquals("", StringUtil.join((java.util.Collection<?>) null, ","));
        assertEquals("", StringUtil.join(Collections.emptyList(), ","));
        assertEquals("a", StringUtil.join(Collections.singletonList("a"), ","));
        assertEquals("a,b,c", StringUtil.join(Arrays.asList("a", "b", "c"), ","));
        assertEquals("a-b-c", StringUtil.join(Arrays.asList("a", "b", "c"), "-"));
        assertEquals("abc", StringUtil.join(Arrays.asList("a", "b", "c"), ""));
        assertEquals("a,b,c", StringUtil.join(Arrays.asList("a", "b", "c"), null));
        assertEquals("1,2,3", StringUtil.join(Arrays.asList(1, 2, 3), ","));
    }

    @Test
    void testJoinArray() {
        assertEquals("", StringUtil.join((Object[]) null, ","));
        assertEquals("", StringUtil.join(new Object[]{}, ","));
        assertEquals("a", StringUtil.join(new Object[]{"a"}, ","));
        assertEquals("a,b,c", StringUtil.join(new Object[]{"a", "b", "c"}, ","));
        assertEquals("a-b-c", StringUtil.join(new Object[]{"a", "b", "c"}, "-"));
        assertEquals("abc", StringUtil.join(new Object[]{"a", "b", "c"}, ""));
        assertEquals("a,b,c", StringUtil.join(new Object[]{"a", "b", "c"}, null));
        assertEquals("1,2,3", StringUtil.join(new Object[]{1, 2, 3}, ","));
    }

    @Test
    void testFormat() {
        assertNull(StringUtil.format(null));
        assertEquals("", StringUtil.format(""));
        assertEquals("Hello World", StringUtil.format("Hello %s", "World"));
        assertEquals("Value: 123", StringUtil.format("Value: %d", 123));
        assertEquals("Pi: 3.14", StringUtil.format("Pi: %.2f", 3.14159));
        assertEquals("Multiple: 1, 2, 3", StringUtil.format("Multiple: %d, %d, %d", 1, 2, 3));
    }

    @Test
    void testMask() {
        assertNull(StringUtil.mask(null, 2));
        assertEquals("", StringUtil.mask("", 2));
        assertEquals("test", StringUtil.mask("test", 2));
        assertEquals("te**st", StringUtil.mask("test123", 2));
        assertEquals("12**********90", StringUtil.mask("12345678901234567890", 2));
        assertEquals("abc**********yz", StringUtil.mask("abcdefghijklmnopqrstuvwxyz", 3));
    }

    @Test
    void testRandomString() {
        assertEquals("", StringUtil.randomString(0));
        assertEquals("", StringUtil.randomString(-1));

        String result5 = StringUtil.randomString(5);
        assertEquals(5, result5.length());
        assertTrue(result5.matches("[a-zA-Z0-9]+"));

        String result10 = StringUtil.randomString(10);
        assertEquals(10, result10.length());
        assertTrue(result10.matches("[a-zA-Z0-9]+"));

        // Test randomness (should be different)
        String result1 = StringUtil.randomString(20);
        String result2 = StringUtil.randomString(20);
        assertNotEquals(result1, result2);
    }

    @Test
    void testToBytes() {
        assertArrayEquals(new byte[0], StringUtil.toBytes(null));
        assertArrayEquals(new byte[0], StringUtil.toBytes(""));
        assertArrayEquals("test".getBytes(), StringUtil.toBytes("test"));
        assertArrayEquals("Hello World".getBytes(), StringUtil.toBytes("Hello World"));
    }

    @Test
    void testFromBytes() {
        assertNull(StringUtil.fromBytes(null));
        assertEquals("", StringUtil.fromBytes(new byte[0]));
        assertEquals("test", StringUtil.fromBytes("test".getBytes()));
        assertEquals("Hello World", StringUtil.fromBytes("Hello World".getBytes()));
    }

    @Test
    void testLength() {
        assertEquals(0, StringUtil.length(null));
        assertEquals(0, StringUtil.length(""));
        assertEquals(4, StringUtil.length("test"));
        assertEquals(11, StringUtil.length("Hello World"));
        assertEquals(3, StringUtil.length("   "));
    }

    @Test
    void testIsLengthBetween() {
        assertFalse(StringUtil.isLengthBetween(null, 1, 10));
        assertTrue(StringUtil.isLengthBetween("", 0, 10));
        assertTrue(StringUtil.isLengthBetween("test", 4, 10));
        assertTrue(StringUtil.isLengthBetween("test", 1, 4));
        assertTrue(StringUtil.isLengthBetween("test", 4, 4));
        assertFalse(StringUtil.isLengthBetween("test", 5, 10));
        assertFalse(StringUtil.isLengthBetween("test", 1, 3));
        assertTrue(StringUtil.isLengthBetween("Hello World", 10, 15));
    }

    @Test
    void testUtilityClassCannotBeInstantiated() {
        // Test that the utility class cannot be instantiated
        assertThrows(UnsupportedOperationException.class, () -> {
            try {
                var constructor = StringUtil.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            } catch (Exception e) {
                if (e.getCause() instanceof UnsupportedOperationException) {
                    throw (UnsupportedOperationException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
    }
}
