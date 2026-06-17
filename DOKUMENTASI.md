# 📦 GudangKu — Dokumentasi Aplikasi

> **GudangKu** adalah aplikasi desktop manajemen gudang berbasis JavaFX.  
> Teknologi: Java 17 + JavaFX 21 + MySQL 8

---

## 🗂 Daftar Halaman

| Halaman | File FXML | Controller |
|---------|-----------|------------|
| [Login](#1-login) | `login.fxml` | `LoginController.java` |
| [Dashboard](#2-dashboard) | `dashboard.fxml` | `DashboardController.java` |
| [Storage](#3-storage) | `storage.fxml` | `StorageController.java` |
| [Movements](#4-movements) | `movements.fxml` | `MovementsController.java` |
| [Customer](#5-customer) | `customer.fxml` | `CustomerController.java` |
| [Report](#6-report) | `report.fxml` | `ReportController.java` |
| [Setting](#7-setting) | `setting.fxml` | `SettingController.java` |

---

## 1. Login

### Deskripsi
Halaman awal aplikasi untuk autentikasi pengguna. Terdiri dari dua tab: **Sign Up** (registrasi akun baru) dan **Sign In** (masuk ke aplikasi).

### Fitur
- **Tab Sign Up** — registrasi dengan nama lengkap, email, dan password (min. 6 karakter)
- **Tab Sign In** — login dengan email + password, validasi dari database
- **Lupa Password** — placeholder untuk fitur reset password
- Transisi ke Dashboard setelah login berhasil (**fullscreen otomatis**)

### Flowchart

```mermaid
flowchart TD
    A([🚀 App Dibuka]) --> B[Tampilkan Login Page]
    B --> C{User pilih tab}

    C -->|Sign Up| D[Isi Nama, Email, Password]
    D --> E{Validasi input}
    E -->|❌ Gagal| F[Tampilkan pesan error]
    F --> D
    E -->|✅ Valid| G[UserDAO.register ke DB]
    G --> H{Email sudah ada?}
    H -->|Ya| I[Alert: Email sudah terdaftar]
    I --> D
    H -->|Tidak| J[Insert ke tabel users]
    J --> K[Alert: Akun berhasil dibuat]
    K --> L[Pindah ke tab Sign In]

    C -->|Sign In| M[Isi Email, Password]
    M --> N{Validasi input}
    N -->|❌ Kosong| O[Alert: Field harus diisi]
    O --> M
    N -->|✅ Terisi| P[UserDAO.login ke DB]
    P --> Q{Password cocok?}
    Q -->|❌ Salah| R[Alert: Email/Password salah]
    R --> M
    Q -->|✅ Benar| S[Buat objek User]
    S --> T[Load dashboard.fxml]
    T --> U[stage.setMaximized true]
    U --> V([🏠 Dashboard Fullscreen])
```

---

## 2. Dashboard

### Deskripsi
Halaman utama yang menjadi **shell/wrapper** seluruh aplikasi. Terdiri dari **Sidebar navigasi** (kiri), **Topbar** (atas), dan **ScrollPane konten** (tengah) yang diisi secara dinamis sesuai menu yang diklik.

### Komponen Topbar
- Judul halaman aktif
- Search box
- Tombol notifikasi 🔔
- **Avatar bulat** — menampilkan foto profil atau inisial nama, klik untuk ke Setting

### Komponen Sidebar
| Menu | Icon | Keterangan |
|------|------|------------|
| Dashboard | ⊞ | Halaman ringkasan |
| Storage | 🗄 | Manajemen rak & section |
| Movements | 🚚 | Transaksi masuk/keluar |
| Customer | 👤 | Data pelanggan |
| Report | 📋 | Laporan periode |
| Setting | ⚙ | Profil & keamanan |
| Logout | ⏻ | Keluar aplikasi |

### Konten Default (Dashboard)
- **Section Overview** — Grid slot rak Section A, B, C dengan warna status
- **Recent Activity** — Barang masuk & keluar 24 jam terakhir
- **Recent Transaction** — 5 transaksi terbaru
- **Top Movers** — 3 barang paling aktif
- **Storage Alert** — Rak yang mendekati/mencapai kapasitas

### Flowchart

```mermaid
flowchart TD
    A([🏠 Dashboard Shell Load]) --> B[initUser: ambil data user dari DB]
    B --> C[updateTopbarAvatar: tampilkan foto/inisial]
    C --> D[Tampilkan konten default Dashboard]

    D --> E{User klik menu sidebar}

    E -->|Dashboard| F[loadDashboardPage]
    E -->|Storage| G[loadStoragePage]
    E -->|Movements| H[loadMovementsPage + initUser]
    E -->|Customer| I[loadCustomerPage + initUser]
    E -->|Report| J[loadReportPage + initUser]
    E -->|Setting| K[loadSettingPage + initUser + setDashboardController]
    E -->|Logout| L{Konfirmasi dialog}
    L -->|Batal| D
    L -->|OK| M[Load login.fxml]
    M --> N([🔐 Login Page])

    E -->|Klik Avatar Topbar| K
```

---

## 3. Storage

### Deskripsi
Halaman manajemen lokasi fisik gudang: **Section** dan **Rak**. Menampilkan peta visual slot rak beserta status pengisian.

### Fitur
- Tampilan grid slot rak per section (A, B, C)
- Kode warna status:
  - 🔵 Biru — Normal (tersedia)
  - 🟢 Hijau — Terisi
  - 🔴 Merah — Penuh/kritis
- Informasi kapasitas & persentase pengisian
- Data real dari tabel `rak` dan `section`

### Flowchart

```mermaid
flowchart TD
    A([📂 Storage Dibuka]) --> B[Load storage.fxml]
    B --> C[StorageController.initialize]
    C --> D[Query: SELECT * FROM section]
    D --> E[Query: SELECT * FROM rak per section]
    E --> F[Render grid slot dengan warna status]
    F --> G{User interaksi}
    G -->|Klik slot rak| H[Tampilkan detail kapasitas & terisi]
    G -->|Refresh| C
```

---

## 4. Movements

### Deskripsi
Halaman pencatatan **transaksi pergerakan barang** — masuk dan keluar gudang. Menggunakan tab untuk memisahkan view **Barang Masuk** dan **Barang Keluar**.

### Fitur
- **Tab Masuk** — form tambah transaksi masuk + tabel riwayat masuk
- **Tab Keluar** — form tambah transaksi keluar + tabel riwayat keluar
- Field form: Barang, Supplier, Rak, Jumlah, Keterangan
- Auto-generate kode transaksi (TRX-00001, TRX-00002, ...)
- Update stok barang otomatis setelah transaksi
- Validasi stok tidak boleh negatif saat keluar

### Flowchart

```mermaid
flowchart TD
    A([🚚 Movements Dibuka]) --> B[Load movements.fxml]
    B --> C[initUser + loadAllData]
    C --> D[Load dropdown: Barang, Supplier, Rak]
    D --> E[Load tabel transaksi dari DB]

    E --> F{User pilih tab}
    F -->|Masuk| G[Tampilkan form & tabel masuk]
    F -->|Keluar| H[Tampilkan form & tabel keluar]

    G --> I[User isi form masuk]
    I --> J[Klik Tambah Masuk]
    J --> K{Validasi input}
    K -->|❌| L[Alert error]
    L --> I
    K -->|✅| M[TransaksiDAO.addTransaksi tipe=masuk]
    M --> N[UPDATE stok barang + N]
    N --> O[Refresh tabel]

    H --> P[User isi form keluar]
    P --> Q[Klik Tambah Keluar]
    Q --> R{Stok cukup?}
    R -->|❌ Tidak| S[Alert: Stok tidak mencukupi]
    S --> P
    R -->|✅ Ya| T[TransaksiDAO.addTransaksi tipe=keluar]
    T --> U[UPDATE stok barang - N]
    U --> O
```

---

## 5. Customer

### Deskripsi
Halaman manajemen data pelanggan/supplier yang berhubungan dengan gudang.

### Fitur
- Grid card pelanggan dengan informasi: nama, kontak, email, alamat
- Filter berdasarkan kategori dan status
- Search berdasarkan nama, ID, atau perusahaan
- Tambah pelanggan baru

### Flowchart

```mermaid
flowchart TD
    A([👤 Customer Dibuka]) --> B[Load customer.fxml]
    B --> C[CustomerController.initialize]
    C --> D[Load semua data customer dari DB]
    D --> E[Render TilePane card customer]

    E --> F{User aksi}
    F -->|Filter kategori/status| G[Filter data lokal]
    G --> E
    F -->|Search| H[Filter by nama/ID]
    H --> E
    F -->|Tambah Customer| I[Dialog form customer baru]
    I --> J[Simpan ke DB]
    J --> D
```

---

## 6. Report

### Deskripsi
Halaman laporan pergerakan barang berdasarkan **filter periode waktu** dan **tipe transaksi**. Menyediakan ringkasan statistik, tabel detail, dan ranking barang paling aktif.

### Fitur
- **Filter bar** — DatePicker dari/sampai + ComboBox tipe (Semua/Masuk/Keluar)
- Default periode: 30 hari terakhir
- **4 Summary Cards** — Total Transaksi, Total Masuk (qty), Total Keluar (qty), Barang Aktif
- **Tabel Laporan** — 8 kolom, warna tipe (🟢 Masuk / 🔴 Keluar), maks. 500 baris
- **Top 5 Barang Aktif** — ranking 🥇🥈🥉 berdasarkan qty bergerak
- **Export CSV** — simpan ke `~/laporan_gudang_YYYY-MM-DD.csv`

### Flowchart

```mermaid
flowchart TD
    A([📋 Report Dibuka]) --> B[initUser + Load default 30 hari]
    B --> C[handleGenerateReport auto]
    C --> D[Validasi DatePicker start ≤ end]
    D --> E[TransaksiDAO.getReportSummary]
    E --> F[Update 4 stat cards]
    F --> G[TransaksiDAO.getTransaksiByPeriod]
    G --> H[Populate TableView]
    H --> I[TransaksiDAO.getTopBarang limit 5]
    I --> J[Render Top 5 ranking]
    J --> K[Tampilkan info periode]

    K --> L{User aksi}
    L -->|Ubah filter + Generate| C
    L -->|Reset| M[Set tanggal default + reload]
    M --> C
    L -->|Export CSV| N{Ada data?}
    N -->|Tidak| O[Alert: Tidak ada data]
    N -->|Ya| P[Build CSV string]
    P --> Q[FileWriter ke home dir]
    Q --> R[Alert: File tersimpan di path]
```

---

## 7. Setting

### Deskripsi
Halaman manajemen akun pengguna. Terdiri dari **dua tab**: **Profile** untuk edit data pribadi & foto, dan **Security** untuk ganti password.

### Tab Profile
- **Avatar bulat** — gradient biru dengan inisial, atau foto yang diupload
- Upload foto: FileChooser → copy ke `~/.gudangku/avatars/user_{id}.ext`
- Hapus foto → kembali ke avatar inisial
- Edit: Full Name (**), Display Name, Phone, Bio
- Email ditampilkan read-only (identifier login, tidak bisa diubah)
- Setelah simpan → **topbar avatar otomatis terupdate**

### Tab Security
- Ganti password dengan verifikasi password lama
- **Password strength indicator** — 4 bar visual (🔴 Lemah → 🟡 Sedang → 🟢 Kuat → 🟢 Sangat Kuat)
- Real-time konfirmasi kecocokan password
- Tips keamanan password
- Danger Zone: Logout semua perangkat

### Database (migration_v3.sql)
```sql
ALTER TABLE users ADD display_name VARCHAR(100)
ALTER TABLE users ADD phone VARCHAR(20)
ALTER TABLE users ADD bio TEXT
ALTER TABLE users ADD profile_picture_path VARCHAR(500)
```

### Flowchart

```mermaid
flowchart TD
    A([⚙ Setting Dibuka]) --> B[initUser: ambil data fresh dari DB]
    B --> C[setDashboardController: simpan ref]
    C --> D[populateProfileForm: isi semua field]
    D --> E[loadAvatar: tampilkan foto/inisial]

    E --> F{User pilih tab}

    F -->|Tab Profile| G[Edit nama/phone/bio/display name]
    G --> H{Upload foto?}
    H -->|Ya| I[FileChooser pilih gambar]
    I --> J[Copy ke ~/.gudangku/avatars/]
    J --> K[Preview avatar di Setting]
    K --> L[updateProfilePicture ke DB]
    L --> M[dashboardController.updateTopbarAvatar]

    H -->|Tidak| N[Klik Simpan Perubahan]
    M --> N
    N --> O{Validasi nama tidak kosong}
    O -->|❌| P[Tampilkan pesan error merah]
    O -->|✅| Q[UserDAO.updateProfile ke DB]
    Q --> R[Update state lokal user]
    R --> S[dashboardController.updateTopbarAvatar]
    S --> T[Status: ✅ Profil berhasil disimpan]

    F -->|Tab Security| U[Isi password lama + baru + konfirmasi]
    U --> V{Real-time strength check}
    V --> W[Update bar indikator]
    U --> X[Klik Ubah Password]
    X --> Y{Validasi: semua terisi, min 6 char, cocok}
    Y -->|❌| Z[Tampilkan pesan validasi]
    Y -->|✅| AA[UserDAO.changePassword]
    AA --> AB{Password lama benar?}
    AB -->|❌| AC[Alert: Password lama salah]
    AB -->|✅| AD[Update password_hash di DB]
    AD --> AE[Alert: Berhasil]
    AE --> AF[Clear semua form password]
```

---

## 🏗 Arsitektur Sistem

```mermaid
flowchart LR
    subgraph UI["🖥 View Layer (FXML)"]
        L[login.fxml]
        D[dashboard.fxml]
        S[storage.fxml]
        M[movements.fxml]
        C[customer.fxml]
        R[report.fxml]
        ST[setting.fxml]
    end

    subgraph CTRL["🎮 Controller Layer"]
        LC[LoginController]
        DC[DashboardController]
        SC[StorageController]
        MC[MovementsController]
        CC[CustomerController]
        RC[ReportController]
        STC[SettingController]
    end

    subgraph DAO["🗄 DAO Layer"]
        UD[UserDAO]
        BD[BarangDAO]
        TD[TransaksiDAO]
        SD[SupplierDAO]
    end

    subgraph DB["💾 Database (MySQL)"]
        U[(users)]
        B[(barang)]
        T[(transaksi)]
        SP[(supplier)]
        RK[(rak)]
        SK[(section)]
    end

    L --> LC
    D --> DC
    S --> SC
    M --> MC
    C --> CC
    R --> RC
    ST --> STC

    LC --> UD
    DC --> UD
    MC --> TD & BD & SD
    RC --> TD
    STC --> UD

    UD --> U
    BD --> B
    TD --> T & B & SP & RK
    SD --> SP
```

---

## 🚀 Cara Menjalankan

```powershell
# 1. Pastikan MySQL Laragon berjalan
# 2. Import schema: src/main/resources/sql/schema.sql
# 3. Import migration: src/main/resources/sql/migration_v3.sql

# Jalankan aplikasi
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd javafx:run
```

### Akun Test Default
| Email | Password | Role |
|-------|----------|------|
| `testuser@example.com` | `Testing123` | user |

---

## 📁 Struktur File Penting

```
src/
├── main/
│   ├── java/com/mycompany/tugas_akhir/
│   │   ├── App.java                  ← Entry point
│   │   ├── DashboardController.java  ← Shell utama + routing
│   │   ├── LoginController.java
│   │   ├── StorageController.java
│   │   ├── MovementsController.java
│   │   ├── CustomerController.java
│   │   ├── ReportController.java
│   │   ├── SettingController.java
│   │   ├── UserDAO.java              ← Auth + profil user
│   │   ├── BarangDAO.java
│   │   ├── TransaksiDAO.java         ← Movements + Report queries
│   │   ├── DatabaseConnection.java   ← Singleton DB connection
│   │   └── PasswordHelper.java
│   └── resources/
│       ├── fxml/                     ← Semua layout halaman
│       ├── css/dashboard.css         ← Global styling
│       └── sql/
│           ├── schema.sql            ← Schema awal
│           ├── migration_v2.sql      ← Tambah stok_max
│           └── migration_v3.sql      ← Tambah kolom profil user
```
