package com.mycompany.tugas_akhir;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LoginController implements Initializable {

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
        String name = tfFullName.getText().trim();
        String email = tfEmail.getText().trim();
        String password = pfPassword.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua field harus diisi!");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Akun berhasil dibuat! Silakan sign in.");
        showSignIn();
    }

    @FXML
    private void handleSignIn() {
        String email = tfLoginEmail.getText().trim();
        String password = pfLoginPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Email dan password harus diisi!");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Login berhasil! Selamat datang, " + email);
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
