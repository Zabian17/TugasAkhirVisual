package com.mycompany.tugas_akhir;

/**
 * Utility class untuk hashing dan verifikasi password.
 * Sementara menggunakan plaintext (untuk testing/debugging).
 * 
 * TODO: Implementasi BCrypt untuk production.
 */
public class PasswordHelper {

    /**
     * Store password sebagai plaintext (untuk testing).
     * @param plainPassword password dari user
     * @return plaintext password
     */
    public static String hashPassword(String plainPassword) {
        System.out.println("[PasswordHelper] Password stored as plaintext (TESTING MODE)");
        return plainPassword;
    }

    /**
     * Verifikasi password plaintext.
     * @param plainPassword password yang dimasukkan user saat login
     * @param storedPassword password yang tersimpan di database
     * @return true jika password cocok
     */
    public static boolean verifyPassword(String plainPassword, String storedPassword) {
        boolean matches = plainPassword.equals(storedPassword);
        System.out.println("[PasswordHelper] Password verification: " + (matches ? "MATCH ✓" : "MISMATCH ✗"));
        System.out.println("[PasswordHelper] Input: '" + plainPassword + "', Stored: '" + storedPassword + "'");
        return matches;
    }
}
