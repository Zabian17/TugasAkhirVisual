package com.mycompany.tugas_akhir;


public class PasswordHelper {


    public static String hashPassword(String plainPassword) {
        System.out.println("[PasswordHelper] Password stored as plaintext (TESTING MODE)");
        return plainPassword;
    }


    public static boolean verifyPassword(String plainPassword, String storedPassword) {
        boolean matches = plainPassword.equals(storedPassword);
        System.out.println("[PasswordHelper] Password verification: " + (matches ? "MATCH ✓" : "MISMATCH ✗"));
        System.out.println("[PasswordHelper] Input: '" + plainPassword + "', Stored: '" + storedPassword + "'");
        return matches;
    }
}
