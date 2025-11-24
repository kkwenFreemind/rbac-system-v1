package com.rbac.common.core.util;

import com.rbac.common.core.exception.SystemException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Encryption utility class.
 *
 * This class provides common encryption, decryption, and hashing methods
 * used throughout the RBAC system for securing sensitive data.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
public final class EncryptUtil {

    /**
     * AES encryption algorithm.
     */
    private static final String AES_ALGORITHM = "AES";

    /**
     * AES/GCM encryption transformation.
     */
    private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";

    /**
     * GCM tag length in bits.
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * GCM IV length in bytes.
     */
    private static final int GCM_IV_LENGTH = 12;

    /**
     * SHA-256 algorithm.
     */
    private static final String SHA256_ALGORITHM = "SHA-256";

    /**
     * SHA-512 algorithm.
     */
    private static final String SHA512_ALGORITHM = "SHA-512";

    /**
     * MD5 algorithm (deprecated, use only for legacy compatibility).
     */
    private static final String MD5_ALGORITHM = "MD5";

    /**
     * Base64 encoder.
     */
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    /**
     * Base64 decoder.
     */
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    /**
     * Secure random instance.
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Private constructor to prevent instantiation.
     */
    private EncryptUtil() {
        throw new UnsupportedOperationException("EncryptUtil class cannot be instantiated");
    }

    // ==================== Hashing ====================

    /**
     * Generate SHA-256 hash of input string.
     *
     * @param input the input string
     * @return hex string of SHA-256 hash
     */
    public static String sha256(String input) {
        return hash(input, SHA256_ALGORITHM);
    }

    /**
     * Generate SHA-512 hash of input string.
     *
     * @param input the input string
     * @return hex string of SHA-512 hash
     */
    public static String sha512(String input) {
        return hash(input, SHA512_ALGORITHM);
    }

    /**
     * Generate MD5 hash of input string (deprecated).
     *
     * @param input the input string
     * @return hex string of MD5 hash
     * @deprecated Use SHA-256 or SHA-512 instead
     */
    @Deprecated
    public static String md5(String input) {
        return hash(input, MD5_ALGORITHM);
    }

    /**
     * Generate hash using specified algorithm.
     *
     * @param input the input string
     * @param algorithm the hash algorithm
     * @return hex string of hash
     */
    private static String hash(String input, String algorithm) {
        if (StringUtil.isEmpty(input)) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new SystemException("Hash algorithm not available: " + algorithm, e);
        }
    }

    /**
     * Generate salted hash with SHA-256.
     *
     * @param input the input string
     * @param salt the salt string
     * @return hex string of salted hash
     */
    public static String sha256WithSalt(String input, String salt) {
        if (StringUtil.isEmpty(input)) {
            return null;
        }
        String saltedInput = input + (salt != null ? salt : "");
        return sha256(saltedInput);
    }

    // ==================== AES Encryption/Decryption ====================

    /**
     * Generate a random AES key.
     *
     * @param keySize key size in bits (128, 192, or 256)
     * @return base64 encoded key
     */
    public static String generateAesKey(int keySize) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(keySize, SECURE_RANDOM);
            SecretKey secretKey = keyGenerator.generateKey();
            return BASE64_ENCODER.encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new SystemException("AES algorithm not available", e);
        }
    }

    /**
     * Generate a random AES-256 key.
     *
     * @return base64 encoded 256-bit key
     */
    public static String generateAes256Key() {
        return generateAesKey(256);
    }

    /**
     * Encrypt string using AES-GCM.
     *
     * @param plainText the plaintext to encrypt
     * @param base64Key the base64 encoded AES key
     * @return base64 encoded encrypted data (IV + ciphertext + tag)
     */
    public static String encryptAes(String plainText, String base64Key) {
        if (StringUtil.isEmpty(plainText) || StringUtil.isEmpty(base64Key)) {
            return null;
        }

        try {
            // Decode key
            byte[] keyBytes = BASE64_DECODER.decode(base64Key);
            SecretKey secretKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);

            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            // Encrypt
            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine IV + ciphertext + tag
            byte[] encryptedData = new byte[GCM_IV_LENGTH + cipherText.length];
            System.arraycopy(iv, 0, encryptedData, 0, GCM_IV_LENGTH);
            System.arraycopy(cipherText, 0, encryptedData, GCM_IV_LENGTH, cipherText.length);

            return BASE64_ENCODER.encodeToString(encryptedData);
        } catch (Exception e) {
            throw new SystemException("AES encryption failed", e);
        }
    }

    /**
     * Decrypt string using AES-GCM.
     *
     * @param encryptedText the base64 encoded encrypted data
     * @param base64Key the base64 encoded AES key
     * @return decrypted plaintext
     */
    public static String decryptAes(String encryptedText, String base64Key) {
        if (StringUtil.isEmpty(encryptedText) || StringUtil.isEmpty(base64Key)) {
            return null;
        }

        try {
            // Decode key and encrypted data
            byte[] keyBytes = BASE64_DECODER.decode(base64Key);
            byte[] encryptedData = BASE64_DECODER.decode(encryptedText);

            if (encryptedData.length < GCM_IV_LENGTH) {
                throw new SystemException("Invalid encrypted data length");
            }

            // Extract IV and ciphertext
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] cipherText = new byte[encryptedData.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, GCM_IV_LENGTH, cipherText, 0, cipherText.length);

            SecretKey secretKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            // Decrypt
            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            byte[] plainTextBytes = cipher.doFinal(cipherText);

            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SystemException("AES decryption failed", e);
        }
    }

    // ==================== Password Hashing ====================

    /**
     * Hash password with salt using SHA-256 (simple implementation).
     * For production use, consider using BCrypt, SCrypt, or Argon2.
     *
     * @param password the password
     * @param salt the salt
     * @return hashed password
     */
    public static String hashPassword(String password, String salt) {
        return sha256WithSalt(password, salt);
    }

    /**
     * Verify password against hash.
     *
     * @param password the password to verify
     * @param salt the salt used for hashing
     * @param expectedHash the expected hash
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String salt, String expectedHash) {
        if (StringUtil.isEmpty(password) || StringUtil.isEmpty(expectedHash)) {
            return false;
        }
        String actualHash = hashPassword(password, salt);
        return expectedHash.equals(actualHash);
    }

    // ==================== Random Token Generation ====================

    /**
     * Generate a random token of specified length.
     *
     * @param length the token length
     * @return random token string
     */
    public static String generateToken(int length) {
        if (length <= 0) {
            return "";
        }
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return BASE64_ENCODER.encodeToString(bytes).substring(0, length);
    }

    /**
     * Generate a secure random token (32 characters).
     *
     * @return random token string
     */
    public static String generateSecureToken() {
        return generateToken(32);
    }

    // ==================== Base64 Operations ====================

    /**
     * Encode string to Base64.
     *
     * @param input the input string
     * @return base64 encoded string
     */
    public static String encodeBase64(String input) {
        if (StringUtil.isEmpty(input)) {
            return null;
        }
        return BASE64_ENCODER.encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decode Base64 string.
     *
     * @param base64Input the base64 encoded string
     * @return decoded string
     */
    public static String decodeBase64(String base64Input) {
        if (StringUtil.isEmpty(base64Input)) {
            return null;
        }
        try {
            byte[] decodedBytes = BASE64_DECODER.decode(base64Input);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SystemException("Base64 decoding failed", e);
        }
    }

    /**
     * Encode bytes to Base64.
     *
     * @param bytes the input bytes
     * @return base64 encoded string
     */
    public static String encodeBase64(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return BASE64_ENCODER.encodeToString(bytes);
    }

    /**
     * Decode Base64 to bytes.
     *
     * @param base64Input the base64 encoded string
     * @return decoded bytes
     */
    public static byte[] decodeBase64ToBytes(String base64Input) {
        if (StringUtil.isEmpty(base64Input)) {
            return new byte[0];
        }
        try {
            return BASE64_DECODER.decode(base64Input);
        } catch (Exception e) {
            throw new SystemException("Base64 decoding failed", e);
        }
    }

    // ==================== Utility Methods ====================

    /**
     * Convert byte array to hex string.
     *
     * @param bytes the byte array
     * @return hex string
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Convert hex string to byte array.
     *
     * @param hex the hex string
     * @return byte array
     */
    public static byte[] hexToBytes(String hex) {
        if (StringUtil.isEmpty(hex) || hex.length() % 2 != 0) {
            return new byte[0];
        }
        int length = hex.length();
        byte[] bytes = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                  + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }

    /**
     * Generate a random salt string.
     *
     * @param length the salt length
     * @return random salt string
     */
    public static String generateSalt(int length) {
        byte[] salt = new byte[length];
        SECURE_RANDOM.nextBytes(salt);
        return BASE64_ENCODER.encodeToString(salt);
    }

    /**
     * Generate a standard salt (16 bytes).
     *
     * @return random salt string
     */
    public static String generateStandardSalt() {
        return generateSalt(16);
    }
}