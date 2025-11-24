package com.rbac.common.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rbac.common.core.exception.SystemException;

/**
 * JSON utility class.
 *
 * This class provides common JSON serialization and deserialization methods
 * using Jackson ObjectMapper with proper configuration for the RBAC system.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
public final class JsonUtil {

    /**
     * Thread-safe ObjectMapper instance with proper configuration.
     */
    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    /**
     * Private constructor to prevent instantiation.
     */
    private JsonUtil() {
        throw new UnsupportedOperationException("JsonUtil class cannot be instantiated");
    }

    /**
     * Create and configure ObjectMapper instance.
     *
     * @return configured ObjectMapper
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Configure serialization features
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);

        // Configure deserialization features
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        // Register Java 8 time module for LocalDateTime, LocalDate, etc.
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }

    /**
     * Get the configured ObjectMapper instance.
     *
     * @return ObjectMapper instance
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    // ==================== Serialization ====================

    /**
     * Convert object to JSON string.
     *
     * @param object the object to serialize
     * @return JSON string
     * @throws SystemException if serialization fails
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SystemException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * Convert object to formatted JSON string (pretty print).
     *
     * @param object the object to serialize
     * @return formatted JSON string
     * @throws SystemException if serialization fails
     */
    public static String toPrettyJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SystemException("Failed to serialize object to pretty JSON", e);
        }
    }

    /**
     * Convert object to JSON bytes.
     *
     * @param object the object to serialize
     * @return JSON bytes
     * @throws SystemException if serialization fails
     */
    public static byte[] toJsonBytes(Object object) {
        if (object == null) {
            return new byte[0];
        }
        try {
            return OBJECT_MAPPER.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new SystemException("Failed to serialize object to JSON bytes", e);
        }
    }

    // ==================== Deserialization ====================

    /**
     * Convert JSON string to object.
     *
     * @param json the JSON string
     * @param clazz the target class
     * @param <T> the type parameter
     * @return deserialized object
     * @throws SystemException if deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new SystemException("Failed to deserialize JSON to object", e);
        }
    }

    /**
     * Convert JSON string to object with TypeReference.
     *
     * @param json the JSON string
     * @param typeReference the type reference
     * @param <T> the type parameter
     * @return deserialized object
     * @throws SystemException if deserialization fails
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new SystemException("Failed to deserialize JSON to object", e);
        }
    }

    /**
     * Convert JSON bytes to object.
     *
     * @param jsonBytes the JSON bytes
     * @param clazz the target class
     * @param <T> the type parameter
     * @return deserialized object
     * @throws SystemException if deserialization fails
     */
    public static <T> T fromJson(byte[] jsonBytes, Class<T> clazz) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonBytes, clazz);
        } catch (Exception e) {
            throw new SystemException("Failed to deserialize JSON bytes to object", e);
        }
    }

    // ==================== JSON Node Operations ====================

    /**
     * Parse JSON string to JsonNode.
     *
     * @param json the JSON string
     * @return JsonNode
     * @throws SystemException if parsing fails
     */
    public static JsonNode parseJson(String json) {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new SystemException("Failed to parse JSON string", e);
        } catch (Exception e) {
            throw new SystemException("Unexpected error parsing JSON string", e);
        }
    }

    /**
     * Convert JsonNode to JSON string.
     *
     * @param jsonNode the JsonNode
     * @return JSON string
     * @throws SystemException if conversion fails
     */
    public static String toJson(JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            throw new SystemException("Failed to convert JsonNode to JSON string", e);
        }
    }

    /**
     * Check if JSON string is valid.
     *
     * @param json the JSON string to validate
     * @return true if valid JSON, false otherwise
     */
    public static boolean isValidJson(String json) {
        if (StringUtil.isEmpty(json)) {
            return false;
        }
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Path Operations ====================

    /**
     * Get value from JSON path.
     *
     * @param json the JSON string
     * @param path the JSON path (e.g., "user.name", "items[0].id")
     * @return value as string, or null if not found
     */
    public static String getValue(String json, String path) {
        if (StringUtil.isEmpty(json) || StringUtil.isEmpty(path)) {
            return null;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);
            JsonNode node = getNodeByPath(root, path);
            return node != null && !node.isMissingNode() ? node.asText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get JsonNode from JSON path.
     *
     * @param json the JSON string
     * @param path the JSON path
     * @return JsonNode, or null if not found
     */
    public static JsonNode getNode(String json, String path) {
        if (StringUtil.isEmpty(json) || StringUtil.isEmpty(path)) {
            return null;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);
            return getNodeByPath(root, path);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Navigate JsonNode by path.
     *
     * @param root the root JsonNode
     * @param path the path string
     * @return JsonNode at path, or null
     */
    private static JsonNode getNodeByPath(JsonNode root, String path) {
        String[] parts = path.split("\\.");
        JsonNode current = root;

        for (String part : parts) {
            if (current == null || current.isMissingNode()) {
                return null;
            }

            // Handle array access like "items[0]"
            if (part.contains("[") && part.contains("]")) {
                int bracketStart = part.indexOf('[');
                int bracketEnd = part.indexOf(']');
                String arrayName = part.substring(0, bracketStart);
                String indexStr = part.substring(bracketStart + 1, bracketEnd);

                try {
                    int index = Integer.parseInt(indexStr);
                    current = current.path(arrayName);
                    if (current.isArray() && index >= 0 && index < current.size()) {
                        current = current.get(index);
                    } else {
                        return null;
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            } else {
                current = current.path(part);
            }
        }

        return current;
    }

    // ==================== Safe Operations ====================

    /**
     * Safely convert object to JSON string (returns null on failure).
     *
     * @param object the object to serialize
     * @return JSON string or null
     */
    public static String toJsonSafe(Object object) {
        try {
            return toJson(object);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Safely convert JSON string to object (returns null on failure).
     *
     * @param json the JSON string
     * @param clazz the target class
     * @param <T> the type parameter
     * @return deserialized object or null
     */
    public static <T> T fromJsonSafe(String json, Class<T> clazz) {
        try {
            return fromJson(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== Clone Operations ====================

    /**
     * Deep clone object using JSON serialization.
     *
     * @param object the object to clone
     * @param clazz the class of the object
     * @param <T> the type parameter
     * @return cloned object
     * @throws SystemException if cloning fails
     */
    public static <T> T clone(T object, Class<T> clazz) {
        if (object == null) {
            return null;
        }
        String json = toJson(object);
        return fromJson(json, clazz);
    }

    /**
     * Safely deep clone object using JSON serialization.
     *
     * @param object the object to clone
     * @param clazz the class of the object
     * @param <T> the type parameter
     * @return cloned object or null
     */
    public static <T> T cloneSafe(T object, Class<T> clazz) {
        try {
            return clone(object, clazz);
        } catch (Exception e) {
            return null;
        }
    }
}