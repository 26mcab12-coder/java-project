package com.smartfoodmess.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * The single place where password hashing happens.
 *
 * <h2>How it works</h2>
 * <ol>
 *   <li>A random 16-byte <b>salt</b> is generated for every password.</li>
 *   <li>We compute {@code SHA-256(salt + ":" + password)} and keep it as a
 *       hex string.</li>
 *   <li>The value stored in the database is {@code salt$hash}, for example
 *       {@code 9f3c...$a41b...}. The salt is stored next to the hash so we
 *       can verify the password later without needing a second column.</li>
 * </ol>
 *
 * <h2>Why SHA-256 (and not plain text)?</h2>
 * Plain text passwords would let anyone who reads the database log in as
 * any user. SHA-256 is built into the JDK, needs no extra library, and is
 * easy to explain in a college project. The random salt also prevents
 * "rainbow table" attacks and hash collisions between identical passwords.
 *
 * <h2>Upgrade path</h2>
 * For a real production system you should use a slow, dedicated password
 * algorithm such as <b>BCrypt</b>, <b>Scrypt</b> or <b>Argon2</b>. Because
 * all hashing lives in this one class, upgrading later only means changing
 * these few methods - no other code has to change.
 */
public final class PasswordUtil {

    /** Used to create cryptographically strong random salts. */
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Separates salt and password while hashing. */
    private static final String HASH_SEPARATOR = ":";

    /** Separates salt and hash in the stored value: salt$hash. */
    private static final String STORE_SEPARATOR = "$";

    private PasswordUtil() {
        // utility class - cannot be instantiated
    }

    /** Generates a random 16-byte salt as a hex string (32 characters). */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    /** Computes SHA-256(salt:password) and returns it as a lowercase hex string. */
    public static String hash(String rawPassword, String salt) {
        if (rawPassword == null || salt == null) {
            throw new IllegalArgumentException("Password and salt must not be null.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = (salt + HASH_SEPARATOR + rawPassword).getBytes(StandardCharsets.UTF_8);
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 exists in every standard JVM - this should never happen
            throw new IllegalStateException("SHA-256 hashing is not available in this JVM.", e);
        }
    }

    /**
     * Creates the value that will be stored in {@code users.password}:
     * a fresh random salt, the separator and the hash.
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password must not be empty.");
        }
        String salt = generateSalt();
        return salt + STORE_SEPARATOR + hash(rawPassword, salt);
    }

    /**
     * Verifies a raw password against a stored {@code salt$hash} value.
     * Returns {@code false} (never throws) for malformed stored values.
     */
    public static boolean verify(String rawPassword, String storedValue) {
        if (rawPassword == null || storedValue == null) {
            return false;
        }
        int index = storedValue.indexOf(STORE_SEPARATOR);
        if (index <= 0 || index == storedValue.length() - 1) {
            return false; // malformed value
        }
        String salt = storedValue.substring(0, index);
        String expected = storedValue.substring(index + STORE_SEPARATOR.length());
        String actual = hash(rawPassword, salt);

        // MessageDigest.isEqual compares in constant time (timing-attack safe)
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
