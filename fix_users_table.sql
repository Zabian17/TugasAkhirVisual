-- Fix missing columns in users table
USE gudang_akhir;

-- Add missing columns if they don't exist
ALTER TABLE users ADD COLUMN email VARCHAR(100) NULL;
ALTER TABLE users ADD COLUMN display_name VARCHAR(100) NULL;
ALTER TABLE users ADD COLUMN phone VARCHAR(20) NULL;
ALTER TABLE users ADD COLUMN bio TEXT NULL;
ALTER TABLE users ADD COLUMN profile_picture_path VARCHAR(500) NULL;

-- Verify structure
DESC users;
