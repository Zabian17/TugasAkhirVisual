# 🔧 LOGIN FIX - PLAINTEXT MODE (TESTING)

## ⚠️ IMPORTANT: This is TESTING MODE ONLY!

Password now stored as **PLAINTEXT** (not hashed) for debugging.

```
⚠️  NOT SECURE - For Development/Testing Only!
⚠️  DO NOT USE IN PRODUCTION!
```

---

## 📝 CHANGES MADE

### 1. **PasswordHelper.java**
```java
// BEFORE: Using BCrypt (hashed)
// AFTER: Using plaintext (for testing)

hashPassword("Testing123") → "Testing123" (stored as-is)
verifyPassword("Testing123", "Testing123") → true ✓
```

### 2. **schema.sql**
```sql
-- BEFORE: BCrypt hash
'$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KfzzwG0W1QV0/JBhDu'

-- AFTER: Plaintext
'Testing123'
```

---

## 🚀 STEPS TO TEST

### 1. **Delete old database**
```bash
# In phpMyAdmin or MySQL:
DROP DATABASE IF EXISTS gudang_akhir;
```

### 2. **Re-import schema.sql**

**Option A: phpMyAdmin**
- Open http://localhost/phpmyadmin
- Login: root / 12345
- Click Import → Select schema.sql → Go

**Option B: Terminal**
```bash
mysql -u root -p12345 < src/main/resources/sql/schema.sql
```

### 3. **Verify in Database**
```sql
SELECT email, password_hash FROM users;
```

Should show:
```
email: testuser@example.com
password_hash: Testing123  (← plaintext, not hashed!)
```

### 4. **Clean build**
```bash
mvn clean install
```

### 5. **Run aplikasi**
```bash
mvn javafx:run
```

### 6. **Test Login**
```
Email: testuser@example.com
Password: Testing123
```

Should login successfully! ✅

---

## 🔍 CONSOLE OUTPUT

After login attempt, check console for:

```
[DB] Koneksi berhasil ke database: gudang_akhir
[PasswordHelper] Password verification: MATCH ✓
[PasswordHelper] Input: 'Testing123', Stored: 'Testing123'
[UserDAO] Login berhasil: testuser@example.com
```

If you see "MATCH ✓", login works! 🎉

---

## 📊 COMPARISON

| Method | Storage | Security | Use Case |
|--------|---------|----------|----------|
| **SHA-256** | Hashed | Medium | Legacy |
| **BCrypt** | Hashed | High | Production ✓ |
| **Plaintext** | No hash | None | Testing ⚠️ |

---

## ⚠️ SECURITY WARNING

```
⚠️  PLAINTEXT PASSWORDS ARE NOT SECURE!
⚠️  ONLY USE FOR DEVELOPMENT/DEBUGGING!
⚠️  SWITCH TO BCRYPT BEFORE PRODUCTION!
```

Never deploy with plaintext passwords in production!

---

## 🔄 SWITCH BACK TO BCRYPT (Production)

When ready for production, replace PasswordHelper.java content with:

```java
import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordHelper {
    private static final int COST = 10;

    public static String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(COST, plainPassword.toCharArray());
    }

    public static boolean verifyPassword(String plainPassword, String storedHash) {
        return BCrypt.verifyer().verify(plainPassword.toCharArray(), storedHash).verified;
    }
}
```

And update schema.sql with BCrypt hashes.

---

## ✅ TESTING CHECKLIST

- [ ] Deleted old database
- [ ] Re-imported schema.sql
- [ ] Verified plaintext password in database
- [ ] Clean build (mvn clean install)
- [ ] Run app (mvn javafx:run)
- [ ] Login with testuser@example.com / Testing123
- [ ] Checked console for "MATCH ✓" message
- [ ] Dashboard loaded successfully

---

## 🎯 NEXT STEPS

1. ✅ Verify login works with plaintext mode
2. ✅ Check console output for verification messages
3. 🔜 Explore dashboard functionality
4. 🔜 Test other features (barang, transaksi, etc)
5. 🔜 Switch back to BCrypt when debugging complete

---

**Status:** ✅ Ready for testing (PLAINTEXT MODE)

⚠️ Remember: This is for development/debugging only!
