-- ============================================================
--  GUDANGKU - Database Schema
--  Database : gudang_akhir
--  Dibuat   : 21 Mei 2026
--  Jalankan file ini di phpMyAdmin atau MySQL CLI
-- ============================================================

-- Buat database jika belum ada
CREATE DATABASE IF NOT EXISTS gudang_akhir
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE gudang_akhir;

-- ============================================================
-- TABEL: users
-- Menyimpan akun pengguna aplikasi
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    role          ENUM('admin', 'user') DEFAULT 'user',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- TABEL: barang
-- Menyimpan data master barang/produk
-- ============================================================
CREATE TABLE IF NOT EXISTS barang (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    kode_barang VARCHAR(20)   NOT NULL UNIQUE,
    nama_barang VARCHAR(150)  NOT NULL,
    kategori    VARCHAR(80),
    satuan      VARCHAR(20)   DEFAULT 'pcs',
    stok        INT           DEFAULT 0,
    stok_min    INT           DEFAULT 0,   -- alert jika stok di bawah ini
    stok_max    INT           DEFAULT 0,   -- kapasitas maksimal
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- TABEL: supplier
-- Menyimpan data pemasok/supplier barang
-- ============================================================
CREATE TABLE IF NOT EXISTS supplier (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    nama_supplier VARCHAR(150) NOT NULL,
    kontak        VARCHAR(20),
    email         VARCHAR(150),
    alamat        TEXT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABEL: section
-- Menyimpan data seksi gudang (A, B, C, dst.)
-- ============================================================
CREATE TABLE IF NOT EXISTS section (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    kode_section VARCHAR(10)  NOT NULL UNIQUE,
    nama_section VARCHAR(80),
    kapasitas    INT          DEFAULT 12,  -- jumlah slot
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABEL: rak
-- Slot penyimpanan dalam setiap section
-- ============================================================
CREATE TABLE IF NOT EXISTS rak (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    section_id   INT NOT NULL,
    kode_rak     VARCHAR(10)  NOT NULL UNIQUE,
    kapasitas    INT          DEFAULT 100, -- kapasitas dalam unit
    terisi       INT          DEFAULT 0,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (section_id) REFERENCES section(id) ON DELETE CASCADE
);

-- ============================================================
-- TABEL: transaksi
-- Mencatat setiap gerakan barang masuk dan keluar
-- ============================================================
CREATE TABLE IF NOT EXISTS transaksi (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    kode_transaksi VARCHAR(20) NOT NULL UNIQUE,
    tipe          ENUM('masuk', 'keluar') NOT NULL,
    barang_id     INT NOT NULL,
    supplier_id   INT,                         -- NULL jika tipe=keluar
    rak_id        INT,
    jumlah        INT          NOT NULL,
    keterangan    TEXT,
    user_id       INT NOT NULL,                -- siapa yang input
    tanggal       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (barang_id)   REFERENCES barang(id),
    FOREIGN KEY (supplier_id) REFERENCES supplier(id),
    FOREIGN KEY (rak_id)      REFERENCES rak(id),
    FOREIGN KEY (user_id)     REFERENCES users(id)
);

-- ============================================================
-- DATA AWAL (Seed)
-- ============================================================

-- Section A, B, C
INSERT IGNORE INTO section (kode_section, nama_section, kapasitas) VALUES
('A', 'Section A', 12),
('B', 'Section B', 12),
('C', 'Section C', 12);

-- Rak A-01 s/d A-12
INSERT IGNORE INTO rak (section_id, kode_rak, kapasitas, terisi) VALUES
(1, 'A-01', 100, 95),
(1, 'A-02', 100, 40),
(1, 'A-03', 100, 20),
(1, 'A-04', 100, 10),
(1, 'A-05', 100, 0),
(1, 'A-06', 100, 0),
(1, 'A-07', 100, 0),
(1, 'A-08', 100, 0),
(1, 'A-09', 100, 0),
(1, 'A-10', 100, 0),
(1, 'A-11', 100, 0),
(1, 'A-12', 100, 0);

-- Rak B-01 s/d B-12
INSERT IGNORE INTO rak (section_id, kode_rak, kapasitas, terisi) VALUES
(2, 'B-01', 100, 100),
(2, 'B-02', 100, 80),
(2, 'B-03', 100, 60),
(2, 'B-04', 100, 50),
(2, 'B-05', 100, 0),
(2, 'B-06', 100, 0),
(2, 'B-07', 100, 0),
(2, 'B-08', 100, 0),
(2, 'B-09', 100, 0),
(2, 'B-10', 100, 0),
(2, 'B-11', 100, 0),
(2, 'B-12', 100, 0);

-- Rak C-01 s/d C-12 (penuh semua)
INSERT IGNORE INTO rak (section_id, kode_rak, kapasitas, terisi) VALUES
(3, 'C-01', 100, 45),
(3, 'C-02', 100, 100),
(3, 'C-03', 100, 100),
(3, 'C-04', 100, 100),
(3, 'C-05', 100, 100),
(3, 'C-06', 100, 100),
(3, 'C-07', 100, 100),
(3, 'C-08', 100, 100),
(3, 'C-09', 100, 100),
(3, 'C-10', 100, 100),
(3, 'C-11', 100, 100),
(3, 'C-12', 100, 100);

-- Supplier
INSERT IGNORE INTO supplier (nama_supplier, kontak, email) VALUES
('PT. Satu',  '08111111111', 'satu@example.com'),
('PT. Dua',   '08222222222', 'dua@example.com'),
('PT. Tiga',  '08333333333', 'tiga@example.com'),
('PT. Empat', '08444444444', 'empat@example.com');

-- Barang sample
INSERT IGNORE INTO barang (kode_barang, nama_barang, kategori, satuan, stok, stok_min, stok_max) VALUES
('BRG-001', 'Barang A', 'Umum', 'pcs', 500, 50, 100),
('BRG-002', 'Barang B', 'Umum', 'pcs', 300, 30, 60),
('BRG-003', 'Barang C', 'Umum', 'pcs', 150, 20, 40),
('BRG-004', 'Barang D', 'Umum', 'pcs', 200, 25, 50),
('BRG-005', 'Barang E', 'Umum', 'pcs', 769, 100, 200);

-- ============================================================
-- TEST USER ACCOUNT
-- Username: testuser@example.com
-- Password: Testing123 (plaintext for testing)
-- ============================================================
INSERT IGNORE INTO users (full_name, email, password_hash, role) VALUES
('Test User', 'testuser@example.com', 'Testing123', 'user');

-- ============================================================
-- CARA PAKAI:
--   Di phpMyAdmin : Import file ini via tab Import
--   Di terminal   : mysql -u root -p12345 < schema.sql
-- ============================================================
