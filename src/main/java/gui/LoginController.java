package gui;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import services.AuthService;
import utils.SceneManager;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both email and password.");
            return;
        }

        try {
            Utilisateur utilisateur = authService.authenticate(email, password);
            if (utilisateur == null) {
                statusLabel.setText("Invalid credentials. Try again.");
                return;
            }

            navigateByRole(utilisateur);
        } catch (SQLException e) {
            statusLabel.setText("Login failed: " + e.getMessage());
        } catch (IOException e) {
            statusLabel.setText("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void openForgotPassword() {
        try {
            SceneManager.switchScene("/forgot-password.fxml", "Reset Password");
        } catch (IOException e) {
            statusLabel.setText("Unable to open reset page.");
        }
    }

    private void navigateByRole(Utilisateur utilisateur) throws IOException {
        String type = utilisateur.getType();
        if ("administrateur".equalsIgnoreCase(type)) {
            SceneManager.switchScene("/main-layout-admin.fxml", "Admin Dashboard");
            return;
        }
        if ("etudiant".equalsIgnoreCase(type)) {
            SceneManager.switchScene("/main-layout-etudiant.fxml", "Student Dashboard");
            return;
        }
        if ("enseignant".equalsIgnoreCase(type)) {
            SceneManager.switchScene("/main-layout-enseignant.fxml", "Teacher Dashboard");
            return;
        }
        statusLabel.setText("Unknown user role: " + type);
    }
}
