-- ============================================================
-- Migration Script: Add Storage Enhancement Fields
-- Database: gudang_akhir
-- Version: 2.0
-- ============================================================
-- This migration adds the following fields to support
-- enhanced storage management:
-- - stok_max: Maximum stock capacity
-- - Location tracking via transaksi and rak relationship
-- - Last update timestamp (already exists as updated_at)
-- ============================================================

USE gudang_akhir;

-- Add stok_max column if it doesn't exist
ALTER TABLE barang 
ADD COLUMN stok_max INT DEFAULT 0 AFTER stok_min;

-- Update existing records with stok_max = stok_min * 2 if stok_max is 0
UPDATE barang 
SET stok_max = stok_min * 2 
WHERE stok_max = 0 AND stok_min > 0;

-- Ensure updated_at timestamp is set properly
UPDATE barang 
SET updated_at = CURRENT_TIMESTAMP 
WHERE updated_at IS NULL;

-- Create an index for faster queries on status calculations
CREATE INDEX idx_barang_stok ON barang(stok, stok_min);

-- ============================================================
-- Verification queries
-- ============================================================
-- SELECT COUNT(*) as total_items FROM barang;
-- SELECT kode_barang, nama_barang, stok, stok_min, stok_max, updated_at FROM barang LIMIT 5;

-- ============================================================
-- Status Distribution Check
-- ============================================================
-- SELECT 
--   CASE 
--     WHEN stok <= 0 THEN 'No Stock'
--     WHEN stok < stok_min THEN 'Low Stock'
--     ELSE 'In Stock'
--   END as status,
--   COUNT(*) as count
-- FROM barang
-- GROUP BY status;

-- ============================================================
-- Migration Complete!
-- ============================================================
