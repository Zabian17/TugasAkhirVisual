package com.mycompany.tugas_akhir;

import java.sql.*;

/**
 * Data Access Object untuk tabel `users`.
 * Menangani registrasi dan autentikasi login.
 */
public class UserDAO {

    // ── Model sederhana hasil login ───────────────────────────────────────────
    public static class User {
        public int    id;
        public String fullName;
        public String email;
        public String role;

        public User(int id, String fullName, String email, String role) {
            this.id       = id;
            this.fullName = fullName;
            this.email    = email;
            this.role     = role;
        }
    }

    // ── Registrasi ────────────────────────────────────────────────────────────

    /**
     * Mendaftarkan user baru ke database.
     * @return true jika berhasil, false jika email sudah terdaftar atau error.
     */
    public boolean register(String fullName, String email, String plainPassword) {
        // Cek apakah email sudah ada
        if (emailExists(email)) {
            return false;
        }

        String sql = "INSERT INTO users (full_name, email, password_hash, role) VALUES (?, ?, ?, 'user')";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String hashedPassword = PasswordHelper.hashPassword(plainPassword);

            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, hashedPassword);

            int affected = ps.executeUpdate();
            System.out.println("[UserDAO] Registrasi berhasil: " + email);
            return affected > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] Gagal registrasi: " + e.getMessage());
            return false;
        }
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    /**
     * Memvalidasi login user.
     * @param emailOrUsername  email yang dimasukkan user
     * @param plainPassword    password plaintext dari form
     * @return objek User jika berhasil login, null jika gagal
     */
    public User login(String emailOrUsername, String plainPassword) {
        String sql = "SELECT id, full_name, email, password_hash, role FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, emailOrUsername);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");

                if (PasswordHelper.verifyPassword(plainPassword, storedHash)) {
                    System.out.println("[UserDAO] Login berhasil: " + emailOrUsername);
                    return new User(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("role")
                    );
                } else {
                    System.out.println("[UserDAO] Password salah untuk: " + emailOrUsername);
                }
            } else {
                System.out.println("[UserDAO] Email tidak ditemukan: " + emailOrUsername);
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] Gagal login query: " + e.getMessage());
        }

        return null; // login gagal
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Mengecek apakah email sudah terdaftar.
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] Gagal cek email: " + e.getMessage());
        }

        return false;
    }
}
