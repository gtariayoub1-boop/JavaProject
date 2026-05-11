package gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import jakarta.mail.MessagingException;
import services.AuthService;
import services.PasswordResetTokenData;
import utils.PasswordResetContext;
import utils.SceneManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class ForgotPasswordController {
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{8,}$");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private VBox requestSection;

    @FXML
    private VBox resetSection;

    @FXML
    private TextField emailField;

    @FXML
    private TextField resetEmailField;

    @FXML
    private Label tokenStatusLabel;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label feedbackLabel;

    @FXML
    private Button sendEmailButton;

    @FXML
    private Button resetPasswordButton;

    private final AuthService authService = new AuthService();
    private String activeToken;

    @FXML
    private void initialize() {
        activateRequestMode();
        String token = PasswordResetContext.consumePendingToken();
        if (token != null && !token.isBlank()) {
            loadToken(token);
        }
    }

    @FXML
    private void handleResetRequest() {
        String newPassword = newPasswordField.getText() != null ? newPasswordField.getText() : "";
        String confirmPassword = confirmPasswordField.getText() != null ? confirmPasswordField.getText() : "";

        if (activeToken == null || activeToken.isBlank()) {
            feedbackLabel.setText("Open the reset page from the email link first.");
            return;
        }

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            feedbackLabel.setText("Fill the new password and confirmation.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            feedbackLabel.setText("Password confirmation does not match.");
            return;
        }

        if (!isStrongPassword(newPassword)) {
            feedbackLabel.setText("Password must contain at least 8 characters, one uppercase letter, and one number.");
            return;
        }

        try {
            boolean updated = authService.resetPassword(activeToken, newPassword);
            if (updated) {
                feedbackLabel.setText("Password updated successfully. You can now sign in.");
                newPasswordField.clear();
                confirmPasswordField.clear();
                activeToken = null;
                activateRequestMode();
            } else {
                feedbackLabel.setText("This reset link is invalid or expired. Request a new one.");
            }
        } catch (SQLException e) {
            feedbackLabel.setText("Password reset failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleSendResetEmail() {
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        if (email.isEmpty()) {
            feedbackLabel.setText("Enter your email address.");
            return;
        }

        setBusy(true);
        feedbackLabel.setText("Sending reset email...");

        Thread worker = new Thread(() -> {
            try {
                boolean sent = authService.requestPasswordReset(email);
                Platform.runLater(() -> {
                    if (sent) {
                        feedbackLabel.setText("If the email exists, a reset link was sent. Open it while the desktop app is running.");
                    } else {
                        feedbackLabel.setText("No account found for this email.");
                    }
                    setBusy(false);
                });
            } catch (SQLException | MessagingException e) {
                Platform.runLater(() -> {
                    feedbackLabel.setText("Unable to send reset email: " + e.getMessage());
                    setBusy(false);
                });
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    @FXML
    private void goBack() {
        try {
            SceneManager.switchScene("/login.fxml", "Campus Access");
        } catch (IOException e) {
            feedbackLabel.setText("Unable to return to login.");
        }
    }

    private void loadToken(String token) {
        try {
            PasswordResetTokenData tokenData = authService.validateResetToken(token);
            if (tokenData == null) {
                activateRequestMode();
                feedbackLabel.setText("This reset link is invalid or expired. Request a new email.");
                return;
            }

            activeToken = tokenData.getToken();
            requestSection.setVisible(false);
            requestSection.setManaged(false);
            resetSection.setVisible(true);
            resetSection.setManaged(true);
            resetEmailField.setText(tokenData.getEmail());
            tokenStatusLabel.setText("Reset link valid until " + tokenData.getExpiresAt().format(DATE_TIME_FORMATTER));
            feedbackLabel.setText("Choose a new password for " + tokenData.getEmail() + ".");
        } catch (SQLException e) {
            activateRequestMode();
            feedbackLabel.setText("Unable to validate reset link: " + e.getMessage());
        }
    }

    private void activateRequestMode() {
        requestSection.setVisible(true);
        requestSection.setManaged(true);
        resetSection.setVisible(false);
        resetSection.setManaged(false);
        resetEmailField.clear();
        tokenStatusLabel.setText("");
    }

    private void setBusy(boolean busy) {
        sendEmailButton.setDisable(busy);
        resetPasswordButton.setDisable(busy);
    }

    private boolean isStrongPassword(String password) {
        return STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }
}
