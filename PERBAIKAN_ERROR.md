# Perbaikan Error Aplikasi GudangKu

## Error yang Sudah Diperbaiki

### 1. ✅ Maven Dependency Error
**Error Original:**
```
There are 1 pathException(s). The related dependencies will be ignored.
Dependency: itext-core-8.0.3.pom
   - exception: Only outputDirectories and jars are accepted on the path
```

**Penyebab:**
- File `pom.xml` mendeklarasikan `itext-core` dengan `<type>pom</type>` yang salah

**Solusi:**
- **File:** `pom.xml`
- **Perubahan:** Hapus `<type>pom</type>` dari dependency itext-core
- **Sebelum:**
  ```xml
  <dependency>
      <groupId>com.itextpdf</groupId>
      <artifactId>itext-core</artifactId>
      <version>8.0.3</version>
      <type>pom</type>
  </dependency>
  ```
- **Sesudah:**
  ```xml
  <dependency>
      <groupId>com.itextpdf</groupId>
      <artifactId>itext-core</artifactId>
      <version>8.0.3</version>
  </dependency>
  ```

---

### 2. ✅ Database Connection Error
**Error Original:**
```
[DB] Gagal konek ke MySQL: Communications link failure
The last packet sent successfully to the server was 0 milliseconds ago. 
The driver has not received any packets from the server.
```

**Penyebab:**
- MySQL Server tidak berjalan atau tidak dapat dijangkau
- Error handling yang tidak memadai

**Solusi:**
- **File:** `DatabaseConnection.java`
- **Perubahan 1:** Tambah pesan error yang lebih informatif
  ```java
  private void connect() {
      try {
          Class.forName("com.mysql.cj.jdbc.Driver");
          connection = DriverManager.getConnection(URL, USER, PASSWORD);
          System.out.println("[DB] ✓ Koneksi berhasil ke database: " + DATABASE);
      } catch (ClassNotFoundException e) {
          System.err.println("[DB] ✗ Driver MySQL tidak ditemukan: " + e.getMessage());
          connection = null;
      } catch (SQLException e) {
          System.err.println("[DB] ✗ Gagal konek ke MySQL: " + e.getMessage());
          System.err.println("[DB] Pastikan MySQL server berjalan di " + HOST + ":" + PORT);
          connection = null;
      }
  }
  ```

- **Perubahan 2:** Tambah method untuk cek status koneksi
  ```java
  public boolean isConnected() {
      try {
          return connection != null && !connection.isClosed();
      } catch (SQLException e) {
          return false;
      }
  }
  ```

---

### 3. ✅ NullPointerException pada Login
**Error Original:**
```
Caused by: java.lang.NullPointerException: Cannot invoke "java.sql.Connection.prepareStatement(String)" 
because "conn" is null at com.mycompany.tugas_akhir.UserDAO.login(UserDAO.java:86)
```

**Penyebab:**
- Connection null tidak dicek sebelum digunakan
- NullPointerException karena PreparedStatement dipanggil pada null connection

**Solusi:**
- **File:** `UserDAO.java`
- **Perubahan:** Tambah null check di awal method login()
  ```java
  public User login(String emailOrUsername, String plainPassword) {
      Connection conn = DatabaseConnection.getInstance().getConnection();
      if (conn == null) {
          System.err.println("[UserDAO] ✗ Koneksi database tidak tersedia!");
          return null;
      }
      // ... rest of code
  }
  ```

---

### 4. ✅ Better Error Handling di Login Controller
**Error Original:**
- User tidak tahu apa penyebab login gagal (database error atau password salah)

**Solusi:**
- **File:** `LoginController.java`
- **Perubahan:** Tambah check koneksi database sebelum proses login
  ```java
  @FXML
  private void handleSignIn() {
      String email    = tfLoginEmail.getText().trim();
      String password = pfLoginPassword.getText();

      if (email.isEmpty() || password.isEmpty()) {
          showAlert(Alert.AlertType.WARNING, "Peringatan", 
                    "Email dan password harus diisi!");
          return;
      }

      // Cek koneksi database
      if (!DatabaseConnection.getInstance().isConnected()) {
          showAlert(Alert.AlertType.ERROR, "Koneksi Database Error",
                  "Tidak dapat terhubung ke database.\n\n" +
                  "Pastikan:\n" +
                  "• MySQL Server sedang berjalan\n" +
                  "• Database 'gudang_akhir' sudah dibuat\n" +
                  "• Username/password database benar");
          return;
      }

      UserDAO.User user = userDAO.login(email, password);
      // ... rest of code
  }
  ```

---

## Langkah-Langkah untuk Menjalankan Aplikasi

### 1. Pastikan MySQL Server Berjalan
```bash
# Windows
net start MySQL80
# atau gunakan MySQL Workbench / phpMyAdmin

# Linux
sudo service mysql start
# atau
sudo systemctl start mysql
```

### 2. Import Database
```bash
# Buka MySQL command line
mysql -u root -p

# Masukkan password: 12345

# Buat database
CREATE DATABASE IF NOT EXISTS gudang_akhir;
USE gudang_akhir;

# Import file SQL
SOURCE /path/to/gudang_akhir.sql;
```

### 3. Build dan Run Aplikasi
```bash
cd /path/to/project

# Build
./mvnw clean compile

# Run
./mvnw clean javafx:run
```

---

## Konfigurasi Database
File: `DatabaseConnection.java`

```java
private static final String HOST     = "localhost";
private static final String PORT     = "3306";
private static final String DATABASE = "gudang_akhir";
private static final String USER     = "root";
private static final String PASSWORD = "12345";
```

**Jika konfigurasi berbeda, ubah nilai-nilai di atas sesuai dengan setup MySQL Anda.**

---

## Testing Login

### User Test yang Tersedia
Setelah import `gudang_akhir.sql`, user berikut sudah tersedia:

| Email | Password | Role | Catatan |
|-------|----------|------|---------|
| admin@gudangku.com | Admin@12345 | admin | Administrator |
| staff@gudangku.com | Staff@12345 | staff | Staff Warehouse |
| manager@gudangku.com | Manager@12345 | manager | Manager |

### Membuat User Baru
1. Klik "Sign Up" di login page
2. Isi data: Full Name, Email, Password
3. Klik "Sign Up"
4. Login dengan email dan password yang baru dibuat

---

## Error Messages yang Mungkin Muncul

### 1. "Tidak dapat terhubung ke database"
**Solusi:**
- Pastikan MySQL Server sedang berjalan
- Cek port MySQL (default 3306)
- Cek username dan password di `DatabaseConnection.java`
- Cek apakah database `gudang_akhir` sudah dibuat

### 2. "Email tidak ditemukan"
**Solusi:**
- Gunakan email yang sudah terdaftar
- Cek kembali nama email (case-sensitive)
- Buat user baru melalui Sign Up

### 3. "Password salah"
**Solusi:**
- Cek kembali password yang dimasukkan
- Password case-sensitive
- Reset password jika lupa (fitur belum tersedia, hubungi admin)

---

## Files yang Dimodifikasi

1. ✅ `DatabaseConnection.java` - Error handling koneksi
2. ✅ `UserDAO.java` - Null check dan error logging
3. ✅ `LoginController.java` - Database connection validation
4. ✅ `pom.xml` - Fix Maven dependency itext-core

---

## Ringkasan Perbaikan

| Komponen | Error | Solusi | Status |
|----------|-------|--------|--------|
| Maven | itext-core type pom | Hapus type pom | ✅ Fixed |
| Database | Connection null | Add null check | ✅ Fixed |
| Login | NullPointerException | Connection validation | ✅ Fixed |
| Error Handling | Pesan error tidak jelas | Add descriptive messages | ✅ Fixed |

---

**Aplikasi sekarang siap digunakan dengan error handling yang lebih baik!** 🎉
