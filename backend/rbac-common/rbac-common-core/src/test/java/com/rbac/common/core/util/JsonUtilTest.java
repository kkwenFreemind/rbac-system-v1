package com.rbac.common.core.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.rbac.common.core.exception.SystemException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JsonUtil class.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
class JsonUtilTest {

    @Test
    void testToJson() {
        // Test basic object serialization
        TestObject obj = new TestObject("John", 30);
        String json = JsonUtil.toJson(obj);

        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"John\""));
        assertTrue(json.contains("\"age\":30"));
    }

    @Test
    void testToJsonWithNull() {
        // Test null object
        String json = JsonUtil.toJson(null);
        assertNull(json);
    }

    @Test
    void testToPrettyJson() {
        // Test pretty print JSON
        TestObject obj = new TestObject("John", 30);
        String json = JsonUtil.toPrettyJson(obj);

        assertNotNull(json);
        assertTrue(json.contains("\"name\" : \"John\""));
        assertTrue(json.contains("\"age\" : 30"));
        assertTrue(json.contains("\n"));
    }

    @Test
    void testToPrettyJsonWithNull() {
        // Test pretty print null object
        String json = JsonUtil.toPrettyJson(null);
        assertNull(json);
    }

    @Test
    void testToJsonBytes() {
        // Test byte array serialization
        TestObject obj = new TestObject("John", 30);
        byte[] bytes = JsonUtil.toJsonBytes(obj);

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void testToJsonBytesWithNull() {
        // Test byte array with null object
        byte[] bytes = JsonUtil.toJsonBytes(null);
        assertNotNull(bytes);
        assertEquals(0, bytes.length);
    }

    @Test
    void testFromJson() {
        // Test deserialization
        String json = "{\"name\":\"John\",\"age\":30}";
        TestObject obj = JsonUtil.fromJson(json, TestObject.class);

        assertNotNull(obj);
        assertEquals("John", obj.getName());
        assertEquals(30, obj.getAge());
    }

    @Test
    void testFromJsonWithNull() {
        // Test deserialization with null JSON
        String nullJson = null;
        TestObject obj = JsonUtil.fromJson(nullJson, TestObject.class);
        assertNull(obj);
    }

    @Test
    void testFromJsonWithEmptyString() {
        // Test deserialization with empty string
        TestObject obj = JsonUtil.fromJson("", TestObject.class);
        assertNull(obj);
    }

    @Test
    void testFromJsonWithTypeReference() {
        // Test deserialization with TypeReference
        String json = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
        Map<String, String> map = JsonUtil.fromJson(json, new TypeReference<Map<String, String>>() {});

        assertNotNull(map);
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }

    @Test
    void testFromJsonBytes() {
        // Test deserialization from byte array
        String json = "{\"name\":\"John\",\"age\":30}";
        byte[] bytes = json.getBytes();
        TestObject obj = JsonUtil.fromJson(bytes, TestObject.class);

        assertNotNull(obj);
        assertEquals("John", obj.getName());
        assertEquals(30, obj.getAge());
    }

    @Test
    void testFromJsonBytesWithNull() {
        // Test deserialization from null byte array
        byte[] nullBytes = null;
        TestObject obj = JsonUtil.fromJson(nullBytes, TestObject.class);
        assertNull(obj);
    }

    @Test
    void testFromJsonBytesWithEmptyArray() {
        // Test deserialization from empty byte array
        TestObject obj = JsonUtil.fromJson(new byte[0], TestObject.class);
        assertNull(obj);
    }

    @Test
    void testParseJson() {
        // Test parsing JSON string to JsonNode
        String json = "{\"name\":\"John\",\"age\":30}";
        JsonNode node = JsonUtil.parseJson(json);

        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals("John", node.get("name").asText());
        assertEquals(30, node.get("age").asInt());
    }

    @Test
    void testParseJsonWithNull() {
        // Test parsing null JSON string
        JsonNode node = JsonUtil.parseJson(null);
        assertNull(node);
    }

    @Test
    void testParseJsonWithEmptyString() {
        // Test parsing empty JSON string
        JsonNode node = JsonUtil.parseJson("");
        assertNull(node);
    }

    @Test
    void testJsonNodeToJson() {
        // Test converting JsonNode to JSON string
        String json = "{\"name\":\"John\",\"age\":30}";
        JsonNode node = JsonUtil.parseJson(json);
        String result = JsonUtil.toJson(node);

        assertNotNull(result);
        assertTrue(result.contains("\"name\":\"John\""));
        assertTrue(result.contains("\"age\":30"));
    }

    @Test
    void testJsonNodeToJsonWithNull() {
        // Test converting null JsonNode to JSON string
        String result = JsonUtil.toJson((JsonNode) null);
        assertNull(result);
    }

    @Test
    void testIsValidJson() {
        // Test valid JSON detection
        assertTrue(JsonUtil.isValidJson("{\"name\":\"John\"}"));
        assertTrue(JsonUtil.isValidJson("[]"));
        assertTrue(JsonUtil.isValidJson("{}"));
        assertTrue(JsonUtil.isValidJson("[1,2,3]"));
        assertFalse(JsonUtil.isValidJson(null));
        assertFalse(JsonUtil.isValidJson(""));
        assertFalse(JsonUtil.isValidJson("invalid"));
        assertFalse(JsonUtil.isValidJson("{invalid}"));
        assertFalse(JsonUtil.isValidJson("{\"name\":}"));
    }

    @Test
    void testGetValue() {
        // Test getting value from JSON path
        String json = "{\"user\":{\"name\":\"John\",\"age\":30},\"items\":[1,2,3]}";
        
        assertEquals("John", JsonUtil.getValue(json, "user.name"));
        assertEquals("30", JsonUtil.getValue(json, "user.age"));
        assertEquals("1", JsonUtil.getValue(json, "items[0]"));
        assertEquals("2", JsonUtil.getValue(json, "items[1]"));
        assertEquals("3", JsonUtil.getValue(json, "items[2]"));
    }

    @Test
    void testGetValueWithNullJson() {
        // Test getting value from null JSON
        assertNull(JsonUtil.getValue(null, "user.name"));
    }

    @Test
    void testGetValueWithNullPath() {
        // Test getting value with null path
        String json = "{\"name\":\"John\"}";
        assertNull(JsonUtil.getValue(json, null));
    }

    @Test
    void testGetValueWithInvalidPath() {
        // Test getting value with invalid path
        String json = "{\"name\":\"John\"}";
        assertNull(JsonUtil.getValue(json, "nonexistent.path"));
    }

    @Test
    void testGetNode() {
        // Test getting JsonNode from path
        String json = "{\"user\":{\"name\":\"John\",\"age\":30}}";
        
        JsonNode node = JsonUtil.getNode(json, "user");
        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals("John", node.get("name").asText());
        
        JsonNode nameNode = JsonUtil.getNode(json, "user.name");
        assertNotNull(nameNode);
        assertEquals("John", nameNode.asText());
    }

    @Test
    void testGetNodeWithNullJson() {
        // Test getting node from null JSON
        assertNull(JsonUtil.getNode(null, "user.name"));
    }

    @Test
    void testToJsonSafe() {
        // Test safe serialization
        TestObject obj = new TestObject("John", 30);
        String json = JsonUtil.toJsonSafe(obj);

        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"John\""));
    }

    @Test
    void testToJsonSafeWithNull() {
        // Test safe serialization with null
        String json = JsonUtil.toJsonSafe(null);
        assertNull(json);
    }

    @Test
    void testFromJsonSafe() {
        // Test safe deserialization
        String json = "{\"name\":\"John\",\"age\":30}";
        TestObject obj = JsonUtil.fromJsonSafe(json, TestObject.class);

        assertNotNull(obj);
        assertEquals("John", obj.getName());
        assertEquals(30, obj.getAge());
    }

    @Test
    void testFromJsonSafeWithInvalidJson() {
        // Test safe deserialization with invalid JSON
        TestObject obj = JsonUtil.fromJsonSafe("invalid json", TestObject.class);
        assertNull(obj);
    }

    @Test
    void testClone() {
        // Test deep cloning
        TestObject original = new TestObject("John", 30);
        TestObject clone = JsonUtil.clone(original, TestObject.class);

        assertNotNull(clone);
        assertNotSame(original, clone);
        assertEquals(original.getName(), clone.getName());
        assertEquals(original.getAge(), clone.getAge());
    }

    @Test
    void testCloneWithNull() {
        // Test cloning null object
        TestObject clone = JsonUtil.clone(null, TestObject.class);
        assertNull(clone);
    }

    @Test
    void testCloneSafe() {
        // Test safe cloning
        TestObject original = new TestObject("John", 30);
        TestObject clone = JsonUtil.cloneSafe(original, TestObject.class);

        assertNotNull(clone);
        assertNotSame(original, clone);
        assertEquals(original.getName(), clone.getName());
        assertEquals(original.getAge(), clone.getAge());
    }

    @Test
    void testCloneSafeWithNull() {
        // Test safe cloning with null
        TestObject clone = JsonUtil.cloneSafe(null, TestObject.class);
        assertNull(clone);
    }

    @Test
    void testSerializationException() {
        // Test that serialization exception is wrapped
        Object circularRef = new Object() {
            @SuppressWarnings("unused")
            public Object getSelf() {
                return this;
            }
        };
        
        assertThrows(SystemException.class, () -> JsonUtil.toJson(circularRef));
    }

    @Test
    void testDeserializationException() {
        // Test that deserialization exception is wrapped
        String invalidJson = "{invalid json}";
        assertThrows(SystemException.class, () -> JsonUtil.fromJson(invalidJson, TestObject.class));
    }

    @Test
    void testParseJsonException() {
        // Test that parse exception is wrapped
        String invalidJson = "{invalid json}";
        assertThrows(SystemException.class, () -> JsonUtil.parseJson(invalidJson));
    }

    @Test
    void testGetObjectMapper() {
        // Test getting ObjectMapper instance
        assertNotNull(JsonUtil.getObjectMapper());
    }

    @Test
    void testUtilityClassCannotBeInstantiated() {
        // Test that the utility class cannot be instantiated
        assertThrows(UnsupportedOperationException.class, () -> {
            try {
                var constructor = JsonUtil.class.getDeclaredConstructor();
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

    /**
     * Test object for JSON serialization/deserialization.
     */
    static class TestObject {
        private String name;
        private int age;

        public TestObject() {
        }

        public TestObject(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
