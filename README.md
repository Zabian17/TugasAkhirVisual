# 🏢 GudangKu - Sistem Manajemen Gudang

Aplikasi desktop berbasis JavaFX untuk mengelola stok barang, supplier, dan transaksi gudang secara real-time dengan antarmuka modern dan user-friendly.

## 📋 Daftar Isi
- [Fitur Utama](#fitur-utama)
- [Teknologi yang Digunakan](#teknologi-yang-digunakan)
- [Instalasi](#instalasi)
- [Penggunaan](#penggunaan)
- [Struktur Project](#struktur-project)
- [Database Schema](#database-schema)

---

## ✨ Fitur Utama

### 🔐 Autentikasi & Manajemen User
- **Login/Sign Up** - Registrasi user baru dan login dengan email
- **Password Security** - Hashing password dengan bcrypt untuk keamanan
- **Role-based Access** - Admin dan user roles untuk akses terstruktur

### 📦 Manajemen Barang
- **Master Data Barang** - CRUD operasi untuk data barang
- **Tracking Stok** - Monitor stok real-time dengan alert stok rendah
- **Kategori & Satuan** - Organisasi barang dengan kategori dan satuan
- **Search & Filter** - Pencarian cepat barang berdasarkan kode/nama

### 🏭 Manajemen Gudang
- **Section Management** - Kelola section gudang (A, B, C, dst)
- **Rak/Slot Storage** - Tracking penempatan barang di rak dengan kapasitas
- **Visualisasi Penuh** - Dashboard menampilkan status gudang real-time

### 📥 Transaksi Barang
- **In & Out Tracking** - Catat setiap barang masuk dan keluar
- **Supplier Integration** - Kelola supplier dan transaksi pembelian
- **Keterangan Detail** - Setiap transaksi dapat dicatat dengan detail

### 📊 Dashboard
- **Statistik Real-Time** - Overview stok, transaksi, dan aktivitas
- **Chart & Visualization** - Grafik status gudang dan stok barang
- **Quick Actions** - Shortcut untuk operasi yang sering dilakukan

---

## 🛠️ Teknologi yang Digunakan

| Komponen | Teknologi | Versi |
|----------|-----------|-------|
| **UI Framework** | JavaFX | 21 |
| **Programming Language** | Java | 17 |
| **Database** | MySQL | 5.7+ |
| **Build Tool** | Maven | 3.6+ |
| **JDBC Driver** | MySQL Connector/J | 9.2.0 |
| **Security** | BCrypt Hashing | (via custom PasswordHelper) |

---

## 📦 Instalasi

### Prerequisites
- **Java Development Kit (JDK) 17+** - [Download](https://www.oracle.com/java/technologies/downloads/#java17)
- **MySQL Server 5.7+** - [Download](https://dev.mysql.com/downloads/mysql/)
- **Maven 3.6+** - [Download](https://maven.apache.org/download.cgi)

### Step 1: Setup Database

1. Buka MySQL CLI atau phpMyAdmin
2. Jalankan script di `src/main/resources/sql/schema.sql`:

```bash
# Via Terminal
mysql -u root -p < src/main/resources/sql/schema.sql

# Atau via MySQL CLI
mysql> source src/main/resources/sql/schema.sql;
```

Database `gudang_s6` akan dibuat otomatis dengan tabel dan data awal.

### Step 2: Konfigurasi Koneksi Database

Edit file `src/main/java/com/mycompany/tugas_akhir/DatabaseConnection.java`:

```java
private static final String HOST     = "localhost";
private static final String PORT     = "3306";
private static final String DATABASE = "gudang_s6";
private static final String USER     = "root";
private static final String PASSWORD = "12345";  // Ganti sesuai password MySQL Anda
```

### Step 3: Build & Run

```bash
# Clone repository (jika belum)
git clone <repository-url>
cd tugas_akhir

# Build dengan Maven
mvn clean package

# Jalankan aplikasi
mvn javafx:run

# Atau gunakan Maven Wrapper
./mvnw javafx:run        # Linux/Mac
mvnw.cmd javafx:run     # Windows
```

---

## 🚀 Penggunaan

### Login Pertama Kali
1. Pilih tab **SIGN UP** untuk membuat akun baru
2. Isi Full Name, Email, dan Password
3. Klik **Get Started**
4. Login dengan akun yang baru dibuat

### Default Test Account
Setelah menjalankan `schema.sql`, database berisi data awal:
- Section A, B, C dengan 12 rak masing-masing
- 5 barang sample (BRG-001 s/d BRG-005)
- 4 supplier sample (PT. Satu, PT. Dua, PT. Tiga, PT. Empat)

### Navigasi Menu Utama
- **Dashboard** - Lihat statistik dan overview gudang
- **Manajemen Barang** - Kelola master data barang
- **Transaksi** - Catat barang masuk/keluar
- **Supplier** - Kelola data supplier (jika tersedia)

---

## 📁 Struktur Project

```
tugas_akhir/
├── src/
│   └── main/
│       ├── java/com/mycompany/tugas_akhir/
│       │   ├── App.java                    # Entry point aplikasi
│       │   ├── LoginController.java        # Login/SignUp logic
│       │   ├── DashboardController.java    # Dashboard logic
│       │   ├── StorageController.java      # Barang management logic
│       │   ├── MovementsController.java    # Transaksi logic
│       │   ├── DatabaseConnection.java     # DB connection (Singleton)
│       │   ├── PasswordHelper.java         # Password hashing & verification
│       │   ├── UserDAO.java                # User CRUD & authentication
│       │   ├── BarangDAO.java              # Barang CRUD operations
│       │   ├── SupplierDAO.java            # Supplier CRUD operations
│       │   └── TransaksiDAO.java           # Transaksi CRUD operations
│       │
│       └── resources/
│           ├── fxml/
│           │   ├── login.fxml              # Login & SignUp UI
│           │   ├── dashboard.fxml          # Dashboard UI
│           │   ├── storage.fxml            # Barang management UI
│           │   └── movements.fxml          # Transaksi UI
│           │
│           ├── css/
│           │   ├── login.css               # Login page styling
│           │   └── dashboard.css           # Dashboard styling
│           │
│           └── sql/
│               └── schema.sql              # Database initialization script
│
├── pom.xml                    # Maven configuration
├── mvnw & mvnw.cmd           # Maven Wrapper
├── README.md                 # Project documentation (ini)
└── .gitignore               # Git ignore rules
```

### Class Architecture

**MVC Pattern:**
- **View (FXML)** - User interface components
- **Controller** - Business logic & user interaction handling
- **DAO (Data Access Object)** - Database operations & queries
- **Model** - Data objects (embedded in DAO classes)

**Database Layer:**
- `DatabaseConnection` - Singleton untuk manage koneksi database
- `UserDAO` - User registration, authentication, dan CRUD
- `BarangDAO` - Master data barang dengan inner model class
- `SupplierDAO` - Data supplier management
- `TransaksiDAO` - Transaction tracking & recording

---

## 🗄️ Database Schema

### Tabel Utama

#### `users`
```sql
- id (INT, PK, Auto-increment)
- full_name (VARCHAR 100)
- email (VARCHAR 150, Unique)
- password_hash (VARCHAR 255)
- role (ENUM: 'admin', 'user')
- created_at, updated_at (TIMESTAMP)
```

#### `barang`
```sql
- id (INT, PK)
- kode_barang (VARCHAR 20, Unique)
- nama_barang (VARCHAR 150)
- kategori (VARCHAR 80)
- satuan (VARCHAR 20, default: 'pcs')
- stok (INT, default: 0)
- stok_min (INT, alert jika < stok_min)
- created_at, updated_at (TIMESTAMP)
```

#### `supplier`
```sql
- id (INT, PK)
- nama_supplier (VARCHAR 150)
- kontak (VARCHAR 20)
- email (VARCHAR 150)
- alamat (TEXT)
- created_at (TIMESTAMP)
```

#### `section`
```sql
- id (INT, PK)
- kode_section (VARCHAR 10, Unique)
- nama_section (VARCHAR 80)
- kapasitas (INT, default: 12)
- created_at (TIMESTAMP)
```

#### `rak`
```sql
- id (INT, PK)
- section_id (INT, FK → section)
- kode_rak (VARCHAR 10, Unique)
- kapasitas (INT, default: 100)
- terisi (INT, default: 0)
- created_at (TIMESTAMP)
```

#### `transaksi`
```sql
- id (INT, PK)
- kode_transaksi (VARCHAR 20, Unique)
- tipe (ENUM: 'masuk', 'keluar')
- barang_id (INT, FK → barang)
- supplier_id (INT, FK → supplier, NULL jika keluar)
- rak_id (INT, FK → rak)
- jumlah (INT)
- keterangan (TEXT)
- user_id (INT, FK → users)
- tanggal (TIMESTAMP)
```

**Relationships:**
- rak → section (many-to-one)
- transaksi → barang, supplier, rak, users (many-to-one)

---

## 🔧 Development Tips

### Menjalankan di IDE

**NetBeans:**
1. Open project (File → Open Project)
2. Klik kanan project → Properties
3. Set Main Class: `com.mycompany.tugas_akhir.App`
4. Run Project (F6)

**IntelliJ IDEA / Eclipse:**
1. Import Maven project
2. Maven akan auto-download dependencies
3. Run `App.java` sebagai main class

### Logging & Debugging

Aplikasi menggunakan `System.out.println()` dan `System.err.println()` untuk logging:

```
[DB] Koneksi berhasil ke database: gudang_s6
[UserDAO] Registrasi berhasil: user@example.com
[BarangDAO] getAllBarang: ...
```

Cek console output untuk error messages.

### Hot Reload
Maven Wrapper mendukung auto-reload jika menggunakan IDE dengan live update.

---

## 📋 Checklist Feature Status

- ✅ Login & Registration
- ✅ Password Security (BCrypt)
- ✅ Database Connection & Singleton
- ✅ Barang CRUD Operations
- ✅ Supplier CRUD Operations
- ✅ Transaksi Tracking (In/Out)
- ✅ Dashboard Overview
- ✅ Search & Filter
- ✅ Stock Alert (Low Stock Alerts)
- ⏳ Chart Visualization (In Progress)
- ⏳ Export Reports (In Progress)

---

## 🐛 Troubleshooting

### Error: "Gagal konek ke MySQL"
**Solusi:**
- Pastikan MySQL server running
- Cek username/password di DatabaseConnection.java
- Pastikan database `gudang_s6` sudah dibuat (run schema.sql)

### Error: "Cannot load FXML"
**Solusi:**
- Pastikan FXML files ada di `src/main/resources/fxml/`
- Check FXMLLoader path di Controller

### Error: "Driver MySQL tidak ditemukan"
**Solusi:**
- Pastikan mysql-connector-j dependency di pom.xml
- Run `mvn clean install` untuk download dependencies

---

## 📞 Support & Contact

Untuk pertanyaan atau reporting bugs, silakan buat issue di repository atau hubungi tim development.

---

## 📜 License

Project ini dibuat sebagai Tugas Akhir (Capstone Project). Hak cipta terlindungi.

---

**Last Updated:** May 2026  
**Version:** 1.0  
**Status:** In Development 🚧
