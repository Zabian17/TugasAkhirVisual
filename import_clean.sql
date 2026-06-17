-- Drop and recreate database
DROP DATABASE IF EXISTS gudang_akhir;
CREATE DATABASE gudang_akhir;
USE gudang_akhir;

-- Import the updated schema and data
SOURCE gudang_akhir_updated.sql;

-- Remove duplicate suppliers (keep only the first occurrence)
DELETE FROM supplier
WHERE id NOT IN (
    SELECT MIN(id)
    FROM (
        SELECT MIN(id) as id FROM supplier GROUP BY LOWER(nama_supplier)
    ) AS min_ids
);

-- Remove duplicate rak/shelves (keep only the first occurrence)
DELETE FROM rak
WHERE id NOT IN (
    SELECT MIN(id)
    FROM (
        SELECT MIN(id) as id FROM rak GROUP BY LOWER(kode_rak)
    ) AS min_ids
);

-- Remove duplicate barang (keep only the first occurrence)
DELETE FROM barang
WHERE id NOT IN (
    SELECT MIN(id)
    FROM (
        SELECT MIN(id) as id FROM barang GROUP BY LOWER(nama_barang)
    ) AS min_ids
);

-- Verify cleanup
SELECT 'Supplier records:' as info, COUNT(*) as count FROM supplier
UNION ALL
SELECT 'Rak records:', COUNT(*) FROM rak
UNION ALL
SELECT 'Barang records:', COUNT(*) FROM barang
UNION ALL
SELECT 'User records:', COUNT(*) FROM user
UNION ALL
SELECT 'Transaksi records:', COUNT(*) FROM transaksi;
