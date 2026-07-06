package com.mycompany.tugas_akhir;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class DashboardController implements Initializable {

    private UserDAO.User currentUser;
    private Stage primaryStage;

    @FXML private HBox navDashboard;
    @FXML private HBox navStorage;
    @FXML private HBox navMovements;
    @FXML private HBox navCustomer;
    @FXML private HBox navReport;
    @FXML private HBox navSetting;

    @FXML private Label pageTitleLabel;
    @FXML private TextField searchField;
    @FXML private Label topbarAvatarLabel;
    @FXML private ImageView topbarAvatarImg;
    @FXML private StackPane topbarAvatarFallback;

    @FXML private Label lblBarangMasuk;
    @FXML private Label lblBarangKeluar;

    @FXML private ScrollPane mainScrollPane;
    
    private String currentPage = "dashboard";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setActiveNav("dashboard");

        lblBarangMasuk.setText("1,234");
        lblBarangKeluar.setText("900");
        
        loadDashboardPage();
        
        if (pageTitleLabel != null && pageTitleLabel.getScene() != null) {
            primaryStage = (Stage) pageTitleLabel.getScene().getWindow();
        }
    }

    public void initUser(UserDAO.User user) {
        this.currentUser = user;
        if (user != null) {
            updateTopbarAvatar(user);
        }
        
        if (primaryStage == null && pageTitleLabel != null && pageTitleLabel.getScene() != null) {
            primaryStage = (Stage) pageTitleLabel.getScene().getWindow();
        }
    }

    public void setCurrentUser(UserDAO.User user) {
        initUser(user);
    }


    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }


    public void updateTopbarAvatar(UserDAO.User user) {
        if (user == null) return;

        String picPath = user.profilePicturePath;
        

        if (picPath != null && !picPath.isBlank()) {
            File f = new File(picPath);
            if (f.exists()) {
                try {
                    Image img = new Image(f.toURI().toString(), 34, 34, true, true);
                    topbarAvatarImg.setImage(img);
                    
                    Circle clip = new Circle(17, 17, 17);
                    topbarAvatarImg.setClip(clip);
                    
                    topbarAvatarImg.setVisible(true);
                    topbarAvatarImg.setManaged(true);
                    topbarAvatarFallback.setVisible(false);
                    topbarAvatarFallback.setManaged(false);
                    return;
                } catch (Exception e) {
                    System.err.println("[DashboardController] Gagal load avatar: " + e.getMessage());
                }
            }
        }
        
        topbarAvatarLabel.setText(user.getInitial());
        topbarAvatarImg.setVisible(false);
        topbarAvatarImg.setManaged(false);
        topbarAvatarFallback.setVisible(true);
        topbarAvatarFallback.setManaged(true);
    }


    public String getCurrentPage() {
        return currentPage;
    }

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
        currentPage = "movements";
        loadMovementsPage();
    }

    @FXML
    private void handleNavCustomer() {
        setActiveNav("customer");
        pageTitleLabel.setText("Customer");
        currentPage = "customer";
        loadCustomerPage();
    }

    @FXML
    private void handleNavReport() {
        setActiveNav("report");
        pageTitleLabel.setText("Report");
        currentPage = "report";
        loadReportPage();
    }

    @FXML
    private void handleNavSetting() {
        setActiveNav("setting");
        pageTitleLabel.setText("Setting");
        currentPage = "setting";
        loadSettingPage();
    }

    @FXML
    private void handleNotification() {
        showInfo("Notifikasi", "Tidak ada notifikasi baru.");
    }

    @FXML
    private void handleSettingShortcut() {
        setActiveNav("setting");
        pageTitleLabel.setText("Setting");
        currentPage = "setting";
        loadSettingPage();
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

    private void loadMovementsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/movements.fxml")
            );
            VBox movementsPage = loader.load();
            
            MovementsController controller = loader.getController();
            if (controller != null && currentUser != null) {
                controller.initUser(currentUser);
            }
            
            mainScrollPane.setContent(movementsPage);
        } catch (IOException e) {
            e.printStackTrace();
            showInfo("Error", "Gagal load halaman Movements: " + e.getMessage());
        }
    }

    private void loadCustomerPage() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/customer.fxml")
            );
            VBox customerPage = loader.load();

            CustomerController controller = loader.getController();
            if (controller != null && currentUser != null) {
                controller.initUser(currentUser);
            }

            mainScrollPane.setContent(customerPage);
        } catch (IOException e) {
            e.printStackTrace();
            showInfo("Error", "Gagal load halaman Customer: " + e.getMessage());
        }
    }

    private void loadReportPage() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/report.fxml")
            );
            VBox reportPage = loader.load();

            ReportController controller = loader.getController();
            if (controller != null && currentUser != null) {
                controller.initUser(currentUser);
            }

            mainScrollPane.setContent(reportPage);
        } catch (IOException e) {
            e.printStackTrace();
            showInfo("Error", "Gagal load halaman Report: " + e.getMessage());
        }
    }

    private void loadSettingPage() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/setting.fxml")
            );
            VBox settingPage = loader.load();

            SettingController controller = loader.getController();
            if (controller != null && currentUser != null) {
                controller.initUser(currentUser);
                controller.setDashboardController(this);
            }

            mainScrollPane.setContent(settingPage);
        } catch (IOException e) {
            e.printStackTrace();
            showInfo("Error", "Gagal load halaman Setting: " + e.getMessage());
        }
    }

    private void loadDashboardPage() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/dashboard-content.fxml")
            );
            VBox dashboardContent = loader.load();
            mainScrollPane.setContent(dashboardContent);
        } catch (IOException e) {
            e.printStackTrace();
            showInfo("Error", "Gagal load halaman Dashboard: " + e.getMessage());
        }
    }
        


    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
