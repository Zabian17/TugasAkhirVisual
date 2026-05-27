package com.mycompany.tugas_akhir;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class DashboardController implements Initializable {

    // Data user yang sedang login (di-set oleh LoginController)
    private UserDAO.User currentUser;

    // --- Sidebar nav items ---
    @FXML private HBox navDashboard;
    @FXML private HBox navStorage;
    @FXML private HBox navMovements;
    @FXML private HBox navCustomer;
    @FXML private HBox navReport;
    @FXML private HBox navSetting;

    // --- Topbar ---
    @FXML private Label pageTitleLabel;
    @FXML private TextField searchField;
    @FXML private Button avatarBtn;

    // --- Activity stats ---
    @FXML private Label lblBarangMasuk;
    @FXML private Label lblBarangKeluar;

    // --- Content area ---
    @FXML private ScrollPane mainScrollPane;
    
    // --- Current active page ---
    private String currentPage = "dashboard";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set active nav to Dashboard on load
        setActiveNav("dashboard");

        // Load dummy stats (nanti replace dengan query DB)
        lblBarangMasuk.setText("1,234");
        lblBarangKeluar.setText("900");
    }

    /**
     * Dipanggil oleh LoginController setelah load FXML.
     * Menerima data user yang berhasil login.
     */
    public void initUser(UserDAO.User user) {
        this.currentUser = user;
        if (user != null) {
            // Tampilkan inisial nama di avatar button
            String inisial = user.fullName.substring(0, 1).toUpperCase();
            avatarBtn.setText(inisial);
            // Tooltip nama lengkap di avatar
            avatarBtn.setTooltip(new Tooltip(user.fullName + " (" + user.role + ")"));
        }
    }

    // ============================================================
    // NAVIGATION HANDLERS
    // ============================================================

    @FXML
    private void handleNavDashboard() {
        setActiveNav("dashboard");
        pageTitleLabel.setText("Dashboard");
        currentPage = "dashboard";
        loadDashboardPage();
    }

    @FXML
    private void handleNavStorage() {
        setActiveNav("storage");
        pageTitleLabel.setText("Storage");
        currentPage = "storage";
        loadStoragePage();
    }

    @FXML
    private void handleNavMovements() {
        setActiveNav("movements");
        pageTitleLabel.setText("Movements");
        showComingSoon("Movements");
    }

    @FXML
    private void handleNavCustomer() {
        setActiveNav("customer");
        pageTitleLabel.setText("Customer");
        showComingSoon("Customer");
    }

    @FXML
    private void handleNavReport() {
        setActiveNav("report");
        pageTitleLabel.setText("Report");
        showComingSoon("Report");
    }

    @FXML
    private void handleNavSetting() {
        setActiveNav("setting");
        pageTitleLabel.setText("Setting");
        showComingSoon("Setting");
    }

    @FXML
    private void handleNotification() {
        showInfo("Notifikasi", "Tidak ada notifikasi baru.");
    }

    @FXML
    private void handleSettingShortcut() {
        setActiveNav("setting");
        pageTitleLabel.setText("Setting");
        showComingSoon("Setting");
    }

    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Logout");
        confirm.setHeaderText(null);
        confirm.setContentText("Apakah Anda yakin ingin logout?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                goToLogin();
            }
        });
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private void setActiveNav(String active) {
        resetAllNav();
        switch (active) {
            case "dashboard" -> activateNav(navDashboard);
            case "storage"   -> activateNav(navStorage);
            case "movements" -> activateNav(navMovements);
            case "customer"  -> activateNav(navCustomer);
            case "report"    -> activateNav(navReport);
            case "setting"   -> activateNav(navSetting);
        }
    }

    private void resetAllNav() {
        HBox[] navItems = {navDashboard, navStorage, navMovements, navCustomer, navReport, navSetting};
        for (HBox nav : navItems) {
            if (nav != null) {
                nav.getStyleClass().setAll("nav-item");
                // reset icon and label styles in each nav item
                nav.getChildren().forEach(node -> {
                    if (node instanceof Label lbl) {
                        String sc = lbl.getStyleClass().stream().findFirst().orElse("");
                        if (sc.equals("nav-icon-active")) {
                            lbl.getStyleClass().setAll("nav-icon");
                        } else if (sc.equals("nav-label-active")) {
                            lbl.getStyleClass().setAll("nav-label");
                        }
                    }
                });
            }
        }
    }

    private void activateNav(HBox nav) {
        if (nav == null) return;
        nav.getStyleClass().setAll("nav-item-active");
        nav.getChildren().forEach(node -> {
            if (node instanceof Label lbl) {
                String sc = lbl.getStyleClass().stream().findFirst().orElse("");
                if (sc.equals("nav-icon")) {
                    lbl.getStyleClass().setAll("nav-icon-active");
                } else if (sc.equals("nav-label")) {
                    lbl.getStyleClass().setAll("nav-label-active");
                }
            }
        });
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml")
            );
            Scene scene = new Scene(loader.load(), 950, 600);
            Stage stage = (Stage) pageTitleLabel.getScene().getWindow();
            stage.setTitle("GudangKu - Login");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showInfo("Error", "Gagal kembali ke halaman login: " + e.getMessage());
        }
    }

    // ============================================================
    // PAGE LOADING
    // ============================================================

    private void loadStoragePage() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/storage.fxml")
            );
            VBox storagePage = loader.load();
            mainScrollPane.setContent(storagePage);
        } catch (IOException e) {
            e.printStackTrace();
            showInfo("Error", "Gagal load halaman Storage: " + e.getMessage());
        }
    }

    private void loadDashboardPage() {
        try {
            // Reload dashboard content dari FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/dashboard.fxml")
            );
            // Untuk sekarang, biarkan dashboard tetap sebagai default
            mainScrollPane.getStyleClass().clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showComingSoon(String feature) {
        showInfo(feature, "Halaman " + feature + " sedang dalam pengembangan. Segera hadir!");
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
