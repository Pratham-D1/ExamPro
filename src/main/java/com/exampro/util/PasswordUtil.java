package com.exampro.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * PasswordUtil  –  Password Hashing Utility
 * ═══════════════════════════════════════════════════════════════
 *
 * PURPOSE:
 *   Provides a centralised, consistent way to:
 *   1. Hash a plain-text password using BCrypt before storing it.
 *   2. Verify a plain-text password against a stored BCrypt hash.
 *
 * WHY BCRYPT?
 *   BCrypt is designed specifically for password hashing.
 *   Unlike MD5 or SHA-256:
 *   - BCrypt includes a random "salt" automatically (prevents rainbow tables).
 *   - BCrypt is intentionally slow (adjustable "cost" factor).
 *   - The same password hashed twice produces different hashes (salt differs).
 *   - BCrypt hashes look like: $2a$12$... and are always 60 characters.
 *
 * COST FACTOR:
 *   The cost factor (BCRYPT_COST = 12) controls how much computation
 *   is needed to hash a password. Higher = slower = more secure.
 *   12 is a good default for 2024 hardware (takes ~300ms per hash).
 *   Never use a cost below 10 in production.
 *
 * USAGE:
 *   // When creating/registering a user:
 *   String hash = PasswordUtil.hash("myPassword123");
 *   // Store 'hash' in the database — NEVER store "myPassword123"
 *
 *   // When logging in:
 *   boolean valid = PasswordUtil.verify("myPassword123", storedHash);
 *   if (valid) { // allow login } else { // reject }
 *
 * LIBRARY:
 *   Uses "at.favre.lib:bcrypt" (declared in pom.xml).
 *   This is a well-tested, dependency-free BCrypt implementation.
 */
public class PasswordUtil {

    /**
     * BCrypt cost factor (work factor).
     * Determines how many rounds of hashing to apply.
     * Cost 12 = 2^12 = 4,096 rounds.
     */
    private static final int BCRYPT_COST = 12;

    /** Private constructor — utility class, never instantiate */
    private PasswordUtil() {
        throw new UnsupportedOperationException("PasswordUtil is a utility class.");
    }

    /**
     * Hashes a plain-text password using BCrypt.
     *
     * @param  plainTextPassword  The raw password from the registration form.
     *                            Must not be null or empty.
     * @return A BCrypt hash string (e.g., "$2a$12$...") safe to store in DB.
     * @throws IllegalArgumentException if password is null or blank.
     */
    public static String hash(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank.");
        }
        return BCrypt.withDefaults()
                     .hashToString(BCRYPT_COST, plainTextPassword.toCharArray());
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     *
     * @param  plainTextPassword  The password entered by the user at login.
     * @param  storedHash         The BCrypt hash retrieved from the database.
     * @return true if the password matches the hash; false otherwise.
     */
    public static boolean verify(String plainTextPassword, String storedHash) {
        if (plainTextPassword == null || storedHash == null) {
            return false;
        }
        BCrypt.Result result = BCrypt.verifyer()
                                     .verify(plainTextPassword.toCharArray(), storedHash);
        return result.verified;
    }
}
