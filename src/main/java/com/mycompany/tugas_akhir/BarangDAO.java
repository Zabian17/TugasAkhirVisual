package com.mycompany.tugas_akhir;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel `barang`.
 * Menangani operasi CRUD master data barang.
 */
public class BarangDAO {

    // ── Model ─────────────────────────────────────────────────────────────────
    public static class Barang {
        public int    id;
        public String kodeBarang;
        public String namaBarang;
        public String kategori;
        public String satuan;
        public int    stok;
        public int    stokMin;

        public Barang(int id, String kodeBarang, String namaBarang,
                      String kategori, String satuan, int stok, int stokMin) {
            this.id         = id;
            this.kodeBarang = kodeBarang;
            this.namaBarang = namaBarang;
            this.kategori   = kategori;
            this.satuan     = satuan;
            this.stok       = stok;
            this.stokMin    = stokMin;
        }

        @Override
        public String toString() {
            return namaBarang + "  (" + kodeBarang + ")";
        }
    }

    // ── Ambil semua barang ────────────────────────────────────────────────────
    public List<Barang> getAllBarang() {
        List<Barang> list = new ArrayList<>();
        String sql = "SELECT id, kode_barang, nama_barang, kategori, satuan, stok, stok_min "
                   + "FROM barang ORDER BY id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[BarangDAO] getAllBarang: " + e.getMessage());
        }
        return list;
    }

    // ── Search barang ─────────────────────────────────────────────────────────
    public List<Barang> searchBarang(String keyword) {
        List<Barang> list = new ArrayList<>();
        String sql = "SELECT id, kode_barang, nama_barang, kategori, satuan, stok, stok_min "
                   + "FROM barang WHERE nama_barang LIKE ? OR kode_barang LIKE ? OR kategori LIKE ? "
                   + "ORDER BY id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[BarangDAO] searchBarang: " + e.getMessage());
        }
        return list;
    }

    // ── Tambah barang baru ────────────────────────────────────────────────────
    public boolean addBarang(String kode, String nama, String kategori,
                             String satuan, int stok, int stokMin) {
        String sql = "INSERT INTO barang (kode_barang, nama_barang, kategori, satuan, stok, stok_min) "
                   + "VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kode);
            ps.setString(2, nama);
            ps.setString(3, kategori.isEmpty() ? null : kategori);
            ps.setString(4, satuan);
            ps.setInt(5, stok);
            ps.setInt(6, stokMin);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BarangDAO] addBarang: " + e.getMessage());
            return false;
        }
    }

    // ── Update barang ─────────────────────────────────────────────────────────
    public boolean updateBarang(int id, String nama, String kategori,
                                String satuan, int stok, int stokMin) {
        String sql = "UPDATE barang SET nama_barang=?, kategori=?, satuan=?, "
                   + "stok=?, stok_min=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nama);
            ps.setString(2, kategori.isEmpty() ? null : kategori);
            ps.setString(3, satuan);
            ps.setInt(4, stok);
            ps.setInt(5, stokMin);
            ps.setInt(6, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BarangDAO] updateBarang: " + e.getMessage());
            return false;
        }
    }

    // ── Hapus barang ──────────────────────────────────────────────────────────
    public boolean deleteBarang(int id) {
        String sql = "DELETE FROM barang WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BarangDAO] deleteBarang: " + e.getMessage());
            return false;
        }
    }

    // ── Generate kode barang otomatis ─────────────────────────────────────────
    public String generateKodeBarang() {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(kode_barang, 5) AS UNSIGNED)), 0) "
                   + "FROM barang WHERE kode_barang REGEXP '^BRG-[0-9]+$'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return String.format("BRG-%03d", rs.getInt(1) + 1);
        } catch (SQLException e) {
            System.err.println("[BarangDAO] generateKode: " + e.getMessage());
        }
        return "BRG-001";
    }

    // ── Helper mapping ────────────────────────────────────────────────────────
    private Barang mapRow(ResultSet rs) throws SQLException {
        return new Barang(
            rs.getInt("id"),
            rs.getString("kode_barang"),
            rs.getString("nama_barang"),
            rs.getString("kategori") != null ? rs.getString("kategori") : "-",
            rs.getString("satuan"),
            rs.getInt("stok"),
            rs.getInt("stok_min")
        );
    }
}
