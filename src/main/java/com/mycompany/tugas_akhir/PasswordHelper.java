package com.mycompany.tugas_akhir;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class untuk hashing dan verifikasi password menggunakan SHA-256 + Salt.
 *
 * Format penyimpanan di DB: base64(salt) + "$" + base64(hash)
 * Contoh: "aBcDef12==$xYz789hashResult=="
 */
public class PasswordHelper {

    private static final int SALT_LENGTH = 16; // bytes

    /**
     * Hash password dengan salt acak (SHA-256).
     * @param plainPassword password asli dari user
     * @return string aman untuk disimpan ke database
     */
    public static String hashPassword(String plainPassword) {
        try {
            // Generate salt acak
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // Hash password + salt
            byte[] hash = sha256(plainPassword, salt);

            // Encode ke Base64 untuk disimpan sebagai teks
            String saltB64 = Base64.getEncoder().encodeToString(salt);
            String hashB64 = Base64.getEncoder().encodeToString(hash);

            return saltB64 + "$" + hashB64;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 tidak tersedia: " + e.getMessage());
        }
    }

    /**
     * Verifikasi password plaintext dengan hash yang tersimpan di DB.
     * @param plainPassword password yang dimasukkan user saat login
     * @param storedHash    hash yang tersimpan di database
     * @return true jika password cocok
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        try {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 2) return false;

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            byte[] actualHash = sha256(plainPassword, salt);

            // Compare secara constant-time (anti timing attack)
            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (Exception e) {
            System.err.println("[Password] Gagal verifikasi: " + e.getMessage());
            return false;
        }
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private static byte[] sha256(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        return md.digest(password.getBytes());
    }
}
