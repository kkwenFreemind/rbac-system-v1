package com.rbac.common.core.util;

import com.rbac.common.core.constant.CommonConstant;
import com.rbac.common.core.constant.ErrorCode;
import com.rbac.common.core.exception.BusinessException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validation utility class.
 *
 * This class provides common validation methods for input data validation
 * used throughout the RBAC system, including bean validation and custom validations.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
public final class ValidationUtil {

    /**
     * Thread-safe Validator instance.
     */
    private static final Validator VALIDATOR = createValidator();

    /**
     * Email regex pattern.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    /**
     * Phone number regex pattern (supports international formats).
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    );

    /**
     * Strong password pattern (at least 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char).
     */
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    /**
     * Username pattern (alphanumeric, underscore, dash, 3-32 chars).
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_-]{3,32}$"
    );

    /**
     * Private constructor to prevent instantiation.
     */
    private ValidationUtil() {
        throw new UnsupportedOperationException("ValidationUtil class cannot be instantiated");
    }

    /**
     * Create and configure Validator instance.
     *
     * @return configured Validator
     */
    private static Validator createValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator();
        }
    }

    // ==================== Bean Validation ====================

    /**
     * Validate object using bean validation annotations.
     *
     * @param object the object to validate
     * @throws BusinessException if validation fails
     */
    public static void validate(Object object) {
        if (object == null) {
            throw new BusinessException("PARAMETER_INVALID", "Object cannot be null");
        }

        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(object);
        if (!violations.isEmpty()) {
            StringBuilder message = new StringBuilder("Validation failed: ");
            for (ConstraintViolation<Object> violation : violations) {
                message.append(violation.getPropertyPath())
                       .append(" ")
                       .append(violation.getMessage())
                       .append("; ");
            }
            throw new BusinessException("PARAMETER_INVALID", message.toString());
        }
    }

    /**
     * Validate object and return validation results.
     *
     * @param object the object to validate
     * @return set of constraint violations
     */
    public static <T> Set<ConstraintViolation<T>> validateAndGetViolations(T object) {
        if (object == null) {
            return Set.of();
        }
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<T>> violations = (Set<ConstraintViolation<T>>) (Set<?>) VALIDATOR.validate(object);
        return violations;
    }

    /**
     * Check if object is valid.
     *
     * @param object the object to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(Object object) {
        if (object == null) {
            return false;
        }
        return VALIDATOR.validate(object).isEmpty();
    }

    // ==================== String Validations ====================

    /**
     * Validate required string field.
     *
     * @param value the value to validate
     * @param fieldName the field name for error message
     * @throws BusinessException if validation fails
     */
    public static void requireNonEmpty(String value, String fieldName) {
        if (StringUtil.isEmpty(value)) {
            throw new BusinessException("PARAMETER_INVALID",
                String.format("%s cannot be empty", fieldName));
        }
    }

    /**
     * Validate string length.
     *
     * @param value the value to validate
     * @param fieldName the field name for error message
     * @param minLength minimum length
     * @param maxLength maximum length
     * @throws BusinessException if validation fails
     */
    public static void validateLength(String value, String fieldName, int minLength, int maxLength) {
        if (value == null) {
            return; // Allow null unless required
        }

        if (value.length() < minLength) {
            throw new BusinessException("PARAMETER_INVALID",
                String.format("%s must be at least %d characters", fieldName, minLength));
        }

        if (value.length() > maxLength) {
            throw new BusinessException("PARAMETER_INVALID",
                String.format("%s must not exceed %d characters", fieldName, maxLength));
        }
    }

    /**
     * Validate email format.
     *
     * @param email the email to validate
     * @throws BusinessException if validation fails
     */
    public static void validateEmail(String email) {
        requireNonEmpty(email, "email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException("PARAMETER_INVALID", "Invalid email format");
        }
    }

    /**
     * Check if email format is valid.
     *
     * @param email the email to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return !StringUtil.isEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number format.
     *
     * @param phone the phone number to validate
     * @throws BusinessException if validation fails
     */
    public static void validatePhone(String phone) {
        requireNonEmpty(phone, "phone");
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException("PARAMETER_INVALID", "Invalid phone number format");
        }
    }

    /**
     * Check if phone number format is valid.
     *
     * @param phone the phone number to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        return !StringUtil.isEmpty(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate username format.
     *
     * @param username the username to validate
     * @throws BusinessException if validation fails
     */
    public static void validateUsername(String username) {
        requireNonEmpty(username, "username");
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new BusinessException("PARAMETER_INVALID",
                "Username must be 3-32 characters, containing only letters, numbers, underscores, and dashes");
        }
    }

    /**
     * Check if username format is valid.
     *
     * @param username the username to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        return !StringUtil.isEmpty(username) && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validate password strength.
     *
     * @param password the password to validate
     * @throws BusinessException if validation fails
     */
    public static void validatePassword(String password) {
        requireNonEmpty(password, "password");
        if (password.length() < 8) {
            throw new BusinessException("PARAMETER_INVALID",
                "Password must be at least 8 characters long");
        }
        if (!STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BusinessException("PARAMETER_INVALID",
                "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
        }
    }

    /**
     * Check if password is strong.
     *
     * @param password the password to check
     * @return true if strong, false otherwise
     */
    public static boolean isStrongPassword(String password) {
        return !StringUtil.isEmpty(password) && STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }

    // ==================== Numeric Validations ====================

    /**
     * Validate integer range.
     *
     * @param value the value to validate
     * @param fieldName the field name for error message
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @throws BusinessException if validation fails
     */
    public static void validateRange(int value, String fieldName, int min, int max) {
        if (value < min || value > max) {
            throw new BusinessException("PARAMETER_INVALID",
                String.format("%s must be between %d and %d", fieldName, min, max));
        }
    }

    /**
     * Validate long range.
     *
     * @param value the value to validate
     * @param fieldName the field name for error message
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @throws BusinessException if validation fails
     */
    public static void validateRange(long value, String fieldName, long min, long max) {
        if (value < min || value > max) {
            throw new BusinessException("PARAMETER_INVALID",
                String.format("%s must be between %d and %d", fieldName, min, max));
        }
    }

    /**
     * Validate positive number.
     *
     * @param value the value to validate
     * @param fieldName the field name for error message
     * @throws BusinessException if validation fails
     */
    public static void validatePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new BusinessException("PARAMETER_INVALID",
                String.format("%s must be positive", fieldName));
        }
    }

    /**
     * Validate positive number.
     *
     * @param value the value to validate
     * @param fieldName the field name for error message
     * @throws BusinessException if validation fails
     */
    public static void validatePositive(long value, String fieldName) {
        if (value <= 0) {
            throw new BusinessException("PARAMETER_INVALID",
                String.format("%s must be positive", fieldName));
        }
    }

    /**
     * Validate non-negative number.
     *
     * @param value the value to validate
     * @param fieldName the field name for error message
     * @throws BusinessException if validation fails
     */
    public static void validateNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new BusinessException("PARAMETER_INVALID",
                String.format("%s must not be negative", fieldName));
        }
    }

    /**
     * Validate non-negative number.
     *
     * @param value the value to validate
     * @param fieldName the field name for error message
     * @throws BusinessException if validation fails
     */
    public static void validateNonNegative(long value, String fieldName) {
        if (value < 0) {
            throw new BusinessException("PARAMETER_INVALID",
                String.format("%s must not be negative", fieldName));
        }
    }

    // ==================== Collection Validations ====================

    /**
     * Validate collection is not empty.
     *
     * @param collection the collection to validate
     * @param fieldName the field name for error message
     * @throws BusinessException if validation fails
     */
    public static void validateNotEmpty(java.util.Collection<?> collection, String fieldName) {
        if (collection == null || collection.isEmpty()) {
            throw new BusinessException("PARAMETER_INVALID",
                String.format("%s cannot be empty", fieldName));
        }
    }

    /**
     * Validate array is not empty.
     *
     * @param array the array to validate
     * @param fieldName the field name for error message
     * @throws BusinessException if validation fails
     */
    public static void validateNotEmpty(Object[] array, String fieldName) {
        if (array == null || array.length == 0) {
            throw new BusinessException("PARAMETER_INVALID",
                String.format("%s cannot be empty", fieldName));
        }
    }

    /**
     * Validate collection size.
     *
     * @param collection the collection to validate
     * @param fieldName the field name for error message
     * @param minSize minimum size
     * @param maxSize maximum size
     * @throws BusinessException if validation fails
     */
    public static void validateSize(java.util.Collection<?> collection, String fieldName, int minSize, int maxSize) {
        if (collection == null) {
            return; // Allow null unless required
        }

        int size = collection.size();
        if (size < minSize) {
            throw new BusinessException("PARAMETER_INVALID",
                String.format("%s must contain at least %d items", fieldName, minSize));
        }

        if (size > maxSize) {
            throw new BusinessException("PARAMETER_INVALID",
                String.format("%s must not contain more than %d items", fieldName, maxSize));
        }
    }

    // ==================== Business Logic Validations ====================

    /**
     * Validate tenant ID format and range.
     *
     * @param tenantId the tenant ID to validate
     * @throws BusinessException if validation fails
     */
    public static void validateTenantId(Long tenantId) {
        if (tenantId == null) {
            throw new BusinessException("TENANT_NOT_FOUND", "Tenant ID cannot be null");
        }
        validatePositive(tenantId, "tenantId");
        // Additional tenant ID validation can be added here
    }

    /**
     * Validate user ID format and range.
     *
     * @param userId the user ID to validate
     * @throws BusinessException if validation fails
     */
    public static void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException("USER_NOT_FOUND", "User ID cannot be null");
        }
        validatePositive(userId, "userId");
    }

    /**
     * Validate pagination parameters.
     *
     * @param pageNum page number (1-based)
     * @param pageSize page size
     * @throws BusinessException if validation fails
     */
    public static void validatePagination(int pageNum, int pageSize) {
        validatePositive(pageNum, "pageNum");
        validatePositive(pageSize, "pageSize");

        if (pageSize > CommonConstant.MAX_PAGE_SIZE) {
            throw new BusinessException("PARAMETER_INVALID",
                String.format("Page size cannot exceed %d", CommonConstant.MAX_PAGE_SIZE));
        }
    }

    /**
     * Validate sorting field name.
     *
     * @param sortField the sort field name
     * @param allowedFields array of allowed field names
     * @throws BusinessException if validation fails
     */
    public static void validateSortField(String sortField, String[] allowedFields) {
        if (StringUtil.isEmpty(sortField)) {
            return; // Allow empty sort field
        }

        if (allowedFields != null) {
            for (String allowedField : allowedFields) {
                if (sortField.equals(allowedField)) {
                    return;
                }
            }
        }

        throw new BusinessException("PARAMETER_INVALID",
            String.format("Invalid sort field: %s", sortField));
    }

    /**
     * Validate sort direction.
     *
     * @param sortOrder the sort direction (asc/desc)
     * @throws BusinessException if validation fails
     */
    public static void validateSortOrder(String sortOrder) {
        if (StringUtil.isEmpty(sortOrder)) {
            return; // Allow empty sort order
        }

        String lowerOrder = sortOrder.toLowerCase();
        if (!"asc".equals(lowerOrder) && !"desc".equals(lowerOrder)) {
            throw new BusinessException("PARAMETER_INVALID",
                "Sort order must be 'asc' or 'desc'");
        }
    }
}
