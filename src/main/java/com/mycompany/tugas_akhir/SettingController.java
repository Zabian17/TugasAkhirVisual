package com.mycompany.tugas_akhir;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class SettingController implements Initializable {

    
    private final UserDAO userDAO = new UserDAO();

    
    private UserDAO.User currentUser;
    private String pendingPicturePath = null;
    private DashboardController dashboardController; 

    
    @FXML private Button btnTabProfile;
    @FXML private Button btnTabSecurity;

    
    @FXML private HBox tabProfile;
    @FXML private HBox tabSecurity;

    
    @FXML private StackPane avatarStack;
    @FXML private ImageView imgAvatar;
    @FXML private StackPane avatarFallback;
    @FXML private Label     lblAvatarInitial;
    @FXML private Button    btnHapusFoto;

    
    @FXML private Label lblProfileName;
    @FXML private Label lblProfileRole;
    @FXML private Label lblProfileEmail;
    @FXML private Label lblInfoId;
    @FXML private Label lblInfoRole;
    @FXML private Label lblInfoSince;

    
    @FXML private TextField tfFullName;
    @FXML private TextField tfDisplayName;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private TextArea  taBio;
    @FXML private Label     lblSaveStatus;

    
    @FXML private PasswordField pfOldPassword;
    @FXML private PasswordField pfNewPassword;
    @FXML private PasswordField pfConfirmPassword;
    @FXML private Label         lblStrength;
    @FXML private Label         lblConfirmMatch;
    @FXML private Pane          bar1, bar2, bar3, bar4;

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        showTab("profile");

        
        pfConfirmPassword.setOnKeyReleased(e -> checkPasswordMatch());
        pfNewPassword.setOnKeyReleased(e -> {
            updateStrengthBar(pfNewPassword.getText());
            checkPasswordMatch();
        });
    }

    
    public void initUser(UserDAO.User user) {
        this.currentUser = user;
        if (user != null) {
            
            UserDAO.User fresh = userDAO.getUserById(user.id);
            if (fresh != null) this.currentUser = fresh;
            populateProfileForm();
            loadAvatar();
        }
    }

    
    public void setDashboardController(DashboardController dc) {
        this.dashboardController = dc;
    }

    

    @FXML
    private void handleTabProfile() {
        showTab("profile");
    }

    @FXML
    private void handleTabSecurity() {
        showTab("security");
        clearPasswordForm();
    }

    private void showTab(String tab) {
        boolean isProfile = "profile".equals(tab);

        tabProfile.setVisible(isProfile);
        tabProfile.setManaged(isProfile);
        tabSecurity.setVisible(!isProfile);
        tabSecurity.setManaged(!isProfile);

        if (isProfile) {
            btnTabProfile.getStyleClass().setAll("setting-tab-active");
            btnTabSecurity.getStyleClass().setAll("setting-tab");
        } else {
            btnTabProfile.getStyleClass().setAll("setting-tab");
            btnTabSecurity.getStyleClass().setAll("setting-tab-active");
        }
    }

    

    @FXML
    private void handleUploadPhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Pilih Foto Profil");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("File Gambar", "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("Semua File", "*.*")
        );

        Stage stage = (Stage) tfFullName.getScene().getWindow();
        File selectedFile = chooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                
                File avatarDir = new File(System.getProperty("user.home"), ".gudangku/avatars");
                avatarDir.mkdirs();

                
                String ext = getFileExtension(selectedFile.getName());
                File destFile = new File(avatarDir, "user_" + currentUser.id + "." + ext);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                pendingPicturePath = destFile.getAbsolutePath();

                
                showImageAvatar(destFile.toURI().toString());

                
                btnHapusFoto.setVisible(true);
                btnHapusFoto.setManaged(true);

                
                userDAO.updateProfilePicture(currentUser.id, pendingPicturePath);
                currentUser.profilePicturePath = pendingPicturePath;

                
                if (dashboardController != null) {
                    dashboardController.updateTopbarAvatar(currentUser);
                }

                showSaveStatus("✅ Foto profil berhasil diperbarui", "#10b981");
                System.out.println("[SettingController] Foto profil disimpan: " + pendingPicturePath);

            } catch (IOException e) {
                showAlert("Upload Gagal", "Tidak dapat menyimpan foto: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleHapusFoto() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Hapus Foto Profil");
        confirm.setHeaderText(null);
        confirm.setContentText("Apakah Anda yakin ingin menghapus foto profil?");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                userDAO.updateProfilePicture(currentUser.id, null);
                currentUser.profilePicturePath = null;
                pendingPicturePath = null;
                showFallbackAvatar();
                btnHapusFoto.setVisible(false);
                btnHapusFoto.setManaged(false);
                
                
                if (dashboardController != null) {
                    dashboardController.updateTopbarAvatar(currentUser);
                }
                
                showSaveStatus("✅ Foto profil dihapus", "#10b981");
            }
        });
    }

    @FXML
    private void handleSimpanProfile() {
        String fullName    = tfFullName.getText().trim();
        String displayName = tfDisplayName.getText().trim();
        String phone       = tfPhone.getText().trim();
        String bio         = taBio.getText().trim();

        
        if (fullName.isEmpty()) {
            showSaveStatus("⚠ Full Name tidak boleh kosong", "#ef4444");
            tfFullName.requestFocus();
            return;
        }
        if (phone.length() > 0 && !phone.matches("^[0-9+\\-() ]{6,20}$")) {
            showSaveStatus("⚠ Format nomor telepon tidak valid", "#ef4444");
            tfPhone.requestFocus();
            return;
        }

        boolean ok = userDAO.updateProfile(currentUser.id, fullName, displayName, phone, bio);
        if (ok) {
            
            currentUser.fullName    = fullName;
            currentUser.displayName = displayName.isEmpty() ? null : displayName;
            currentUser.phone       = phone.isEmpty() ? null : phone;
            currentUser.bio         = bio.isEmpty() ? null : bio;

            
            lblAvatarInitial.setText(currentUser.getInitial());
            lblProfileName.setText(currentUser.getEffectiveName());

            
            if (dashboardController != null) {
                dashboardController.updateTopbarAvatar(currentUser);
            }

            showSaveStatus("✅ Profil berhasil disimpan", "#10b981");
        } else {
            showSaveStatus("❌ Gagal menyimpan profil", "#ef4444");
        }
    }

    @FXML
    private void handleResetForm() {
        populateProfileForm();
        showSaveStatus("", "#9ba3b8");
    }

    

    @FXML
    private void handlePasswordStrength() {
        updateStrengthBar(pfNewPassword.getText());
        checkPasswordMatch();
    }

    @FXML
    private void handleUbahPassword() {
        String oldPw   = pfOldPassword.getText();
        String newPw   = pfNewPassword.getText();
        String confirm = pfConfirmPassword.getText();

        
        if (oldPw.isEmpty() || newPw.isEmpty() || confirm.isEmpty()) {
            showAlert("Validasi", "Semua field password harus diisi.");
            return;
        }
        if (newPw.length() < 6) {
            showAlert("Validasi", "Password baru minimal 6 karakter.");
            pfNewPassword.requestFocus();
            return;
        }
        if (!newPw.equals(confirm)) {
            lblConfirmMatch.setText("❌ Password konfirmasi tidak cocok");
            lblConfirmMatch.setStyle("-fx-font-size: 11px; -fx-text-fill: #ef4444;");
            pfConfirmPassword.requestFocus();
            return;
        }
        if (oldPw.equals(newPw)) {
            showAlert("Validasi", "Password baru tidak boleh sama dengan password lama.");
            return;
        }

        
        boolean ok = userDAO.changePassword(currentUser.id, oldPw, newPw);
        if (ok) {
            showInfo("Berhasil", "Password berhasil diubah.\nSilakan login kembali dengan password baru jika diperlukan.");
            clearPasswordForm();
        } else {
            showAlert("Gagal", "Password saat ini tidak sesuai. Silakan coba lagi.");
            pfOldPassword.requestFocus();
        }
    }

    @FXML
    private void handleLogoutAll() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText(null);
        confirm.setContentText("Fitur logout semua perangkat belum tersedia di versi ini.\n\nGunakan tombol Logout di sidebar untuk keluar dari sesi ini.");
        confirm.showAndWait();
    }

    

    private void populateProfileForm() {
        if (currentUser == null) return;

        tfFullName.setText(currentUser.fullName != null ? currentUser.fullName : "");
        tfDisplayName.setText(currentUser.displayName != null ? currentUser.displayName : "");
        tfEmail.setText(currentUser.email != null ? currentUser.email : "");
        tfPhone.setText(currentUser.phone != null ? currentUser.phone : "");
        taBio.setText(currentUser.bio != null ? currentUser.bio : "");

        
        lblProfileName.setText(currentUser.getEffectiveName());
        lblProfileRole.setText(currentUser.role != null ? currentUser.role.toUpperCase() : "USER");
        lblProfileEmail.setText(currentUser.email != null ? currentUser.email : "");
        lblInfoId.setText("#" + currentUser.id);
        lblInfoRole.setText(currentUser.role != null
                ? currentUser.role.substring(0, 1).toUpperCase() + currentUser.role.substring(1)
                : "User");
        lblInfoSince.setText("2026"); 

        lblAvatarInitial.setText(currentUser.getInitial());
    }

    private void loadAvatar() {
        if (currentUser == null) return;
        String picPath = currentUser.profilePicturePath;
        if (picPath != null && !picPath.isBlank()) {
            File f = new File(picPath);
            if (f.exists()) {
                showImageAvatar(f.toURI().toString());
                btnHapusFoto.setVisible(true);
                btnHapusFoto.setManaged(true);
                return;
            }
        }
        showFallbackAvatar();
    }

    private void showImageAvatar(String uriString) {
        try {
            Image img = new Image(uriString, 100, 100, true, true);
            imgAvatar.setImage(img);

            
            Circle clip = new Circle(50, 50, 50);
            imgAvatar.setClip(clip);

            imgAvatar.setVisible(true);
            imgAvatar.setManaged(true);
            avatarFallback.setVisible(false);
            avatarFallback.setManaged(false);
        } catch (Exception e) {
            showFallbackAvatar();
        }
    }

    private void showFallbackAvatar() {
        imgAvatar.setVisible(false);
        imgAvatar.setManaged(false);
        avatarFallback.setVisible(true);
        avatarFallback.setManaged(true);
        if (currentUser != null) {
            lblAvatarInitial.setText(currentUser.getInitial());
        }
    }

    

    private void updateStrengthBar(String pw) {
        int score = calculateStrength(pw);
        Pane[] bars = {bar1, bar2, bar3, bar4};
        String[] colors;

        if (pw.isEmpty()) {
            colors = new String[]{"#e5e7eb", "#e5e7eb", "#e5e7eb", "#e5e7eb"};
            lblStrength.setText("");
        } else if (score <= 1) {
            colors = new String[]{"#ef4444", "#e5e7eb", "#e5e7eb", "#e5e7eb"};
            lblStrength.setText("Lemah");
            lblStrength.setStyle("-fx-font-size: 11px; -fx-text-fill: #ef4444;");
        } else if (score == 2) {
            colors = new String[]{"#f59e0b", "#f59e0b", "#e5e7eb", "#e5e7eb"};
            lblStrength.setText("Sedang");
            lblStrength.setStyle("-fx-font-size: 11px; -fx-text-fill: #f59e0b;");
        } else if (score == 3) {
            colors = new String[]{"#10b981", "#10b981", "#10b981", "#e5e7eb"};
            lblStrength.setText("Kuat");
            lblStrength.setStyle("-fx-font-size: 11px; -fx-text-fill: #10b981;");
        } else {
            colors = new String[]{"#10b981", "#10b981", "#10b981", "#10b981"};
            lblStrength.setText("Sangat Kuat");
            lblStrength.setStyle("-fx-font-size: 11px; -fx-text-fill: #10b981; -fx-font-weight: bold;");
        }

        for (int i = 0; i < bars.length; i++) {
            bars[i].setStyle("-fx-background-color: " + colors[i]
                + "; -fx-pref-width: 48px; -fx-pref-height: 4px; -fx-background-radius: 2;");
        }
    }

    private int calculateStrength(String pw) {
        if (pw == null || pw.isEmpty()) return 0;
        int score = 0;
        if (pw.length() >= 6)  score++;
        if (pw.length() >= 10) score++;
        if (pw.matches(".*[A-Z].*") && pw.matches(".*[a-z].*")) score++;
        if (pw.matches(".*[0-9].*")) score++;
        if (pw.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++;
        return Math.min(score, 4);
    }

    private void checkPasswordMatch() {
        String newPw   = pfNewPassword.getText();
        String confirm = pfConfirmPassword.getText();
        if (confirm.isEmpty()) {
            lblConfirmMatch.setText("");
            return;
        }
        if (newPw.equals(confirm)) {
            lblConfirmMatch.setText("✅ Password cocok");
            lblConfirmMatch.setStyle("-fx-font-size: 11px; -fx-text-fill: #10b981;");
        } else {
            lblConfirmMatch.setText("❌ Password tidak cocok");
            lblConfirmMatch.setStyle("-fx-font-size: 11px; -fx-text-fill: #ef4444;");
        }
    }

    private void clearPasswordForm() {
        pfOldPassword.clear();
        pfNewPassword.clear();
        pfConfirmPassword.clear();
        lblStrength.setText("");
        lblConfirmMatch.setText("");
        updateStrengthBar("");
    }

    private void showSaveStatus(String message, String color) {
        lblSaveStatus.setText(message);
        lblSaveStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot >= 0) ? fileName.substring(lastDot + 1).toLowerCase() : "jpg";
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
