package com.mycompany.tugas_akhir;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class untuk mengelola koneksi ke database MySQL.
 * Gunakan DatabaseConnection.getInstance().getConnection() untuk mendapat koneksi.
 */
public class DatabaseConnection {

    // ── Konfigurasi Database ──────────────────────────────────────────────────
    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "gudang_akhir";
    private static final String USER     = "root";
    private static final String PASSWORD = "12345";

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta";

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        connect();
    }

    /**
     * Mengembalikan instance tunggal DatabaseConnection.
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // ── Koneksi ───────────────────────────────────────────────────────────────

    private void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Koneksi berhasil ke database: " + DATABASE);
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver MySQL tidak ditemukan: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DB] Gagal konek ke MySQL: " + e.getMessage());
        }
    }

    /**
     * Mengembalikan objek Connection.
     * Akan reconnect otomatis jika koneksi terputus.
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("[DB] Reconnecting...");
                connect();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Gagal cek status koneksi: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Menutup koneksi database (panggil saat aplikasi ditutup).
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Koneksi ditutup.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Gagal menutup koneksi: " + e.getMessage());
        }
    }
}
