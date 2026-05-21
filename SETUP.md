# 🗄️ SETUP DATABASE - GudangKu

Panduan lengkap untuk setup database MySQL untuk aplikasi GudangKu.

## 📋 Informasi Database

- **Database Name:** `gudang_akhir`
- **Character Set:** utf8mb4
- **Collation:** utf8mb4_unicode_ci
- **MySQL Server:** 5.7+

---

## 🔑 Credentials

**phpMyAdmin / MySQL Login:**
- **Username:** `root`
- **Password:** `12345`
- **Host:** `localhost:3306`

---

## 🚀 Cara Setup (Pilih Salah Satu)

### **OPSI 1: Via phpMyAdmin (Recommended untuk Pemula)**

1. **Buka phpMyAdmin**
   - Akses: `http://localhost/phpmyadmin`
   - Login dengan username: `root`, password: `12345`

2. **Import Database**
   - Klik menu **"Import"** di phpMyAdmin
   - Pilih file: `src/main/resources/sql/schema.sql`
   - Klik **"Go"** / **"Import"**
   - Tunggu hingga proses selesai (✅ ditandai dengan pesan sukses)

3. **Verifikasi Database**
   - Cek di sidebar kiri, database `gudang_akhir` seharusnya sudah ada
   - Expand `gudang_akhir` → lihat 6 tabel (users, barang, supplier, section, rak, transaksi)

---

### **OPSI 2: Via MySQL CLI (Terminal)**

```bash
# Masuk ke folder project
cd c:\laragon\www\tugas_akhir.worktrees\agents-file-check-and-validation

# Jalankan schema.sql
mysql -u root -p12345 < src/main/resources/sql/schema.sql
```

**Expected Output:**
```
Query OK, 1 row affected (0.01 sec)
Query OK, 0 rows affected (0.02 sec)
... (multiple queries)
Query OK, 1 row affected (0.01 sec)
```

---

### **OPSI 3: Via MySQL GUI Tools (HeidiSQL, DBeaver, etc.)**

1. Buka connection ke `localhost` dengan username `root`, password `12345`
2. Open SQL file: `src/main/resources/sql/schema.sql`
3. Execute all queries
4. Check database `gudang_akhir` created successfully

---

## 🧪 Test Account untuk Login

Setelah database berhasil dibuat, gunakan akun ini untuk testing:

| Field | Value |
|-------|-------|
| **Email** | `testuser@example.com` |
| **Password** | `Testing123` |
| **Role** | user |
| **Full Name** | Test User |

---

## 📊 Data Awal yang Diisi Otomatis

### 1. **Sections** (3 section)
- Section A, B, C
- Masing-masing dengan kapasitas 12 rak

### 2. **Rak Storage** (36 total)
- **Section A:** A-01 s/d A-12 (dengan sample data terisi)
- **Section B:** B-01 s/d B-12 (dengan sample data terisi)
- **Section C:** C-01 s/d C-12 (penuh semua sebagai contoh)

### 3. **Suppliers** (4 supplier)
- PT. Satu (08111111111)
- PT. Dua (08222222222)
- PT. Tiga (08333333333)
- PT. Empat (08444444444)

### 4. **Barang/Products** (5 item)
| Kode | Nama | Kategori | Stok | Stok Min |
|------|------|----------|------|----------|
| BRG-001 | Barang A | Umum | 500 | 50 |
| BRG-002 | Barang B | Umum | 300 | 30 |
| BRG-003 | Barang C | Umum | 150 | 20 |
| BRG-004 | Barang D | Umum | 200 | 25 |
| BRG-005 | Barang E | Umum | 769 | 100 |

### 5. **Test User** (1 user untuk testing)
- Email: `testuser@example.com`
- Password: `Testing123`
- Role: user

---

## 🔍 Verifikasi Database Berhasil Dibuat

Setelah import, jalankan queries ini untuk verifikasi:

```sql
-- Check database exists
SHOW DATABASES LIKE 'gudang_akhir';

-- Check tables
USE gudang_akhir;
SHOW TABLES;

-- Check users table
SELECT COUNT(*) as 'Total Users' FROM users;

-- Check test user
SELECT id, full_name, email, role FROM users WHERE email = 'testuser@example.com';

-- Check barang
SELECT COUNT(*) as 'Total Barang' FROM barang;

-- Check sections & rak
SELECT COUNT(*) as 'Total Sections' FROM section;
SELECT COUNT(*) as 'Total Rak' FROM rak;
```

**Expected Results:**
- Database `gudang_akhir` ada ✅
- 6 tables tersedia ✅
- 1 user (test user) ✅
- 5 barang sample ✅
- 3 sections ✅
- 36 rak ✅

---

## ⚙️ Konfigurasi Aplikasi

Pastikan `DatabaseConnection.java` sudah dikonfigurasi dengan benar:

**File:** `src/main/java/com/mycompany/tugas_akhir/DatabaseConnection.java`

```java
private static final String HOST     = "localhost";
private static final String PORT     = "3306";
private static final String DATABASE = "gudang_akhir";    // ← Updated
private static final String USER     = "root";
private static final String PASSWORD = "12345";
```

✅ Sudah otomatis di-update saat pull

---

## 🐛 Troubleshooting

### ❌ Error: "Access denied for user 'root'@'localhost'"
**Solusi:**
- Pastikan MySQL server running
- Cek username/password benar (root / 12345)
- Restart MySQL service

### ❌ Error: "Database gudang_akhir already exists"
**Solusi:**
- Database sudah ada, skip bagian CREATE DATABASE
- Script menggunakan `IF NOT EXISTS`, aman dijalankan ulang
- Untuk reset, drop database: `DROP DATABASE gudang_akhir;`

### ❌ Error: "Can't find file schema.sql"
**Solusi:**
- Pastikan path benar: `src/main/resources/sql/schema.sql`
- Jalankan command dari root project folder

### ❌ Error: "Duplicate entry for key 'email'"
**Solusi:**
- Test user sudah ada, INSERT IGNORE akan skip
- Jika perlu buat user baru, ganti email-nya

### ❌ Login Gagal di Aplikasi
**Solusi:**
- Cek koneksi database di console (lihat log)
- Verifikasi credentials di DatabaseConnection.java
- Gunakan email: `testuser@example.com`, password: `Testing123`

---

## 🔐 Password Hashing Notes

Test user menggunakan bcrypt hashing dengan salt cost 10:
- Hash: `$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KfzzwG0W1QV0/JBhDu`
- Plain password: `Testing123`

Sistem keamanan password menggunakan `PasswordHelper.java` dengan BCrypt.

---

## 📝 Custom SQL untuk Testing

Jika ingin menambah data atau testing, gunakan queries ini:

```sql
-- Tambah user baru
INSERT INTO users (full_name, email, password_hash, role) 
VALUES ('New User', 'newuser@example.com', '[HASHED_PASSWORD]', 'user');

-- Tambah barang
INSERT INTO barang (kode_barang, nama_barang, kategori, satuan, stok, stok_min) 
VALUES ('BRG-006', 'Barang F', 'Umum', 'pcs', 100, 10);

-- Lihat semua transaksi
SELECT t.*, b.nama_barang, u.full_name 
FROM transaksi t 
JOIN barang b ON t.barang_id = b.id 
JOIN users u ON t.user_id = u.id;
```

---

## ✅ Next Steps

1. ✅ Database sudah siap
2. ✅ Test user sudah dibuat
3. 🔜 Build aplikasi: `mvn clean package`
4. 🔜 Run aplikasi: `mvn javafx:run`
5. 🔜 Login dengan `testuser@example.com` / `Testing123`

---

**Dibuat:** 21 Mei 2026  
**Status:** ✅ Ready to Use
