package com.mycompany.tugas_akhir;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel `transaksi`.
 * Mencatat pergerakan barang masuk/keluar dan mengupdate stok otomatis.
 */
public class TransaksiDAO {

    // ── Model ─────────────────────────────────────────────────────────────────
    public static class Transaksi {
        public int    id;
        public String kodeTransaksi;
        public String tipe;
        public int    barangId;
        public String namaBarang;
        public String namaSupplier;
        public String kodeRak;
        public int    jumlah;
        public String keterangan;
        public String tanggal;

        public Transaksi(int id, String kodeTransaksi, String tipe, int barangId,
                         String namaBarang, String namaSupplier, String kodeRak,
                         int jumlah, String keterangan, String tanggal) {
            this.id             = id;
            this.kodeTransaksi  = kodeTransaksi;
            this.tipe           = tipe;
            this.barangId       = barangId;
            this.namaBarang     = namaBarang;
            this.namaSupplier   = namaSupplier;
            this.kodeRak        = kodeRak;
            this.jumlah         = jumlah;
            this.keterangan     = keterangan;
            this.tanggal        = tanggal;
        }
    }

    // ── Ambil semua transaksi ─────────────────────────────────────────────────
    public List<Transaksi> getAllTransaksi() {
        List<Transaksi> list = new ArrayList<>();
        String sql = "SELECT t.id, t.kode_transaksi, t.tipe, t.barang_id, "
                   + "b.nama_barang, "
                   + "COALESCE(s.nama_supplier, '-') AS supplier, "
                   + "COALESCE(r.kode_rak, '-') AS kode_rak, "
                   + "t.jumlah, COALESCE(t.keterangan, '') AS keterangan, "
                   + "DATE_FORMAT(t.tanggal, '%d/%m/%Y %H:%i') AS tgl "
                   + "FROM transaksi t "
                   + "JOIN barang b ON t.barang_id = b.id "
                   + "LEFT JOIN supplier s ON t.supplier_id = s.id "
                   + "LEFT JOIN rak r ON t.rak_id = r.id "
                   + "ORDER BY t.tanggal DESC LIMIT 200";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Transaksi(
                    rs.getInt("id"),
                    rs.getString("kode_transaksi"),
                    rs.getString("tipe"),
                    rs.getInt("barang_id"),
                    rs.getString("nama_barang"),
                    rs.getString("supplier"),
                    rs.getString("kode_rak"),
                    rs.getInt("jumlah"),
                    rs.getString("keterangan"),
                    rs.getString("tgl")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[TransaksiDAO] getAllTransaksi: " + e.getMessage());
        }
        return list;
    }

    // ── Tambah transaksi + update stok (atomic) ───────────────────────────────
    /**
     * @return true  = berhasil
     *         false = gagal (termasuk stok tidak cukup untuk keluar)
     */
    public boolean addTransaksi(String tipe, int barangId, Integer supplierId,
                                Integer rakId, int jumlah, String keterangan, int userId) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);

            // 1. Generate kode transaksi
            String kode = generateKode(conn);

            // 2. Insert ke tabel transaksi
            String sqlIns = "INSERT INTO transaksi "
                          + "(kode_transaksi, tipe, barang_id, supplier_id, rak_id, jumlah, keterangan, user_id) "
                          + "VALUES (?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlIns)) {
                ps.setString(1, kode);
                ps.setString(2, tipe);
                ps.setInt(3, barangId);
                if (supplierId != null) ps.setInt(4, supplierId);  else ps.setNull(4, Types.INTEGER);
                if (rakId     != null) ps.setInt(5, rakId);        else ps.setNull(5, Types.INTEGER);
                ps.setInt(6, jumlah);
                ps.setString(7, keterangan.isEmpty() ? null : keterangan);
                ps.setInt(8, userId);
                ps.executeUpdate();
            }

            // 3. Update stok barang
            if ("masuk".equals(tipe)) {
                String sqlUp = "UPDATE barang SET stok = stok + ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlUp)) {
                    ps.setInt(1, jumlah);
                    ps.setInt(2, barangId);
                    ps.executeUpdate();
                }
            } else {
                // Keluar: pastikan stok cukup
                String sqlUp = "UPDATE barang SET stok = stok - ? WHERE id = ? AND stok >= ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlUp)) {
                    ps.setInt(1, jumlah);
                    ps.setInt(2, barangId);
                    ps.setInt(3, jumlah);
                    int rows = ps.executeUpdate();
                    if (rows == 0) {
                        conn.rollback();
                        conn.setAutoCommit(true);
                        System.err.println("[TransaksiDAO] Stok tidak mencukupi, barang_id=" + barangId);
                        return false;
                    }
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("[TransaksiDAO] Transaksi berhasil: " + kode);
            return true;

        } catch (SQLException e) {
            System.err.println("[TransaksiDAO] addTransaksi: " + e.getMessage());
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ex) { /* ignore */ }
            return false;
        }
    }

    // ── Statistik 24 jam terakhir ─────────────────────────────────────────────
    /**
     * @return int[0] = total barang masuk, int[1] = total barang keluar
     */
    public int[] getStatistik24H() {
        int masuk = 0, keluar = 0;
        String sql = "SELECT tipe, COALESCE(SUM(jumlah), 0) AS total FROM transaksi "
                   + "WHERE tanggal >= DATE_SUB(NOW(), INTERVAL 24 HOUR) GROUP BY tipe";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if ("masuk".equals(rs.getString("tipe")))  masuk  = rs.getInt("total");
                else                                        keluar = rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("[TransaksiDAO] getStatistik24H: " + e.getMessage());
        }
        return new int[]{masuk, keluar};
    }

    // ── Helper: generate kode transaksi ───────────────────────────────────────
    private String generateKode(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(kode_transaksi, 5) AS UNSIGNED)), 0) "
                   + "FROM transaksi WHERE kode_transaksi REGEXP '^TRX-'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return String.format("TRX-%05d", rs.getInt(1) + 1);
        }
        return "TRX-00001";
    }
}
