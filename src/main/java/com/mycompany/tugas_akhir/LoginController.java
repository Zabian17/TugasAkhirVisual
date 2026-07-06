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

public class LoginController implements Initializable {

    private final UserDAO userDAO = new UserDAO();

    @FXML private VBox signUpPane;
    @FXML private VBox signInPane;
    @FXML private StackPane tabSignUp;
    @FXML private StackPane tabSignIn;
    @FXML private Label lblSignUp;
    @FXML private Label lblSignIn;

    @FXML private TextField tfFullName;
    @FXML private TextField tfEmail;
    @FXML private PasswordField pfPassword;

    @FXML private TextField tfLoginEmail;
    @FXML private PasswordField pfLoginPassword;
    @FXML private CheckBox cbRemember;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showSignUp();
    }

    @FXML
    private void showSignUp() {
        signUpPane.setVisible(true);
        signUpPane.setManaged(true);
        signInPane.setVisible(false);
        signInPane.setManaged(false);

        tabSignUp.getStyleClass().setAll("tab-btn-active");
        tabSignIn.getStyleClass().setAll("tab-btn");
        lblSignUp.getStyleClass().setAll("tab-label-active");
        lblSignIn.getStyleClass().setAll("tab-label");
    }

    @FXML
    private void showSignIn() {
        signInPane.setVisible(true);
        signInPane.setManaged(true);
        signUpPane.setVisible(false);
        signUpPane.setManaged(false);

        tabSignIn.getStyleClass().setAll("tab-btn-active");
        tabSignUp.getStyleClass().setAll("tab-btn");
        lblSignIn.getStyleClass().setAll("tab-label-active");
        lblSignUp.getStyleClass().setAll("tab-label");
    }

    @FXML
    private void handleSignUp() {
        String name     = tfFullName.getText().trim();
        String email    = tfEmail.getText().trim();
        String password = pfPassword.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua field harus diisi!");
            return;
        }

        if (password.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Password minimal 6 karakter!");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Format email tidak valid!");
            return;
        }

        boolean success = userDAO.register(name, email, password);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Berhasil",
                    "Akun berhasil dibuat!\nSilakan sign in dengan email Anda.");
            tfFullName.clear();
            tfEmail.clear();
            pfPassword.clear();
            showSignIn();
        } else {
            showAlert(Alert.AlertType.ERROR, "Gagal",
                    "Email sudah terdaftar atau terjadi kesalahan.\nCoba gunakan email lain.");
        }
    }

    @FXML
    private void handleSignIn() {
        String email    = tfLoginEmail.getText().trim();
        String password = pfLoginPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Email dan password harus diisi!");
            return;
        }

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

        if (user != null) {
            navigateToDashboard(user);
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Gagal",
                    "Email atau password salah.\nSilakan coba lagi.");
            pfLoginPassword.clear();
        }
    }

    private void navigateToDashboard(UserDAO.User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/dashboard.fxml")
            );
            Scene scene = new Scene(loader.load(), 1200, 720);

            DashboardController dashCtrl = loader.getController();
            
            Stage stage = (Stage) tfLoginEmail.getScene().getWindow();
            stage.setTitle("GudangKu - Dashboard");
            stage.setResizable(true);
            stage.setScene(scene);

            dashCtrl.setPrimaryStage(stage);
            dashCtrl.initUser(user);
            
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword() {
        showAlert(Alert.AlertType.INFORMATION, "Lupa Password", "Fitur reset password akan segera tersedia.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
