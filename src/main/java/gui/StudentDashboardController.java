package gui;

import entities.Etudiant;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import services.AuthService;
import utils.DashboardNavigation;
import utils.SceneManager;
import utils.UserSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class StudentDashboardController {
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{8,}$");

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label currentUserNameLabel;

    @FXML
    private Label currentUserRoleLabel;

    @FXML
    private MenuButton profileMenuButton;

    @FXML
    private ListView<String> profileList;

    @FXML
    private ListView<String> focusList;

    @FXML
    private VBox profileSection;

    @FXML
    private VBox changePasswordSection;

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label changePasswordFeedbackLabel;

    private final ObservableList<String> profileItems = FXCollections.observableArrayList();
    private final ObservableList<String> focusItems = FXCollections.observableArrayList();

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        profileList.setItems(profileItems);
        focusList.setItems(focusItems);

        Utilisateur utilisateur = UserSession.getCurrentUser();
        if (utilisateur instanceof Etudiant) {
            Etudiant etudiant = (Etudiant) utilisateur;
            welcomeLabel.setText("Hello, " + etudiant.getPrenom());
            roleLabel.setText("Student dashboard");
            currentUserNameLabel.setText(etudiant.getNomComplet());
            currentUserRoleLabel.setText(etudiant.getType());
            profileMenuButton.setText(buildInitials(etudiant));

            profileItems.setAll(
                    "Name: " + etudiant.getNomComplet(),
                    "Email: " + etudiant.getEmail(),
                    "Matricule: " + etudiant.getMatricule(),
                    "Level: " + etudiant.getNiveauEtude(),
                    "Specialisation: " + etudiant.getSpecialisation(),
                    "Status: " + etudiant.getStatut()
            );
        }

        focusItems.setAll(
                "Review your latest course activity",
                "Track your academic profile details",
                "Continue from your current specialization",
                "Prepare for upcoming platform modules"
        );

        DashboardNavigation.StudentSection initialSection = DashboardNavigation.consumeStudentSection();
        if (initialSection == DashboardNavigation.StudentSection.CHANGE_PASSWORD) {
            showChangePasswordPage();
        } else {
            showProfilePage();
        }
    }

    @FXML
    private void handleProfileLogout() throws IOException {
        logoutToLogin();
    }

    @FXML
    private void showProfilePage() {
        setSectionVisibility(true, false);
    }

    @FXML
    private void showChangePasswordPage() {
        if (changePasswordFeedbackLabel != null) {
            changePasswordFeedbackLabel.setText("");
            changePasswordFeedbackLabel.getStyleClass().remove("status-success");
        }
        if (currentPasswordField != null) currentPasswordField.clear();
        if (newPasswordField != null) newPasswordField.clear();
        if (confirmPasswordField != null) confirmPasswordField.clear();
        setSectionVisibility(false, true);
    }

    @FXML
    private void handleChangePassword() {
        Utilisateur currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            setChangePasswordFeedback("No active user session.", false);
            return;
        }

        String currentPassword = currentPasswordField.getText() != null ? currentPasswordField.getText() : "";
        String newPassword = newPasswordField.getText() != null ? newPasswordField.getText() : "";
        String confirmPassword = confirmPasswordField.getText() != null ? confirmPasswordField.getText() : "";

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            setChangePasswordFeedback("Fill current password, new password, and confirmation.", false);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            setChangePasswordFeedback("Password confirmation does not match.", false);
            return;
        }
        if (!isStrongPassword(newPassword)) {
            setChangePasswordFeedback("Password must contain at least 8 characters, one uppercase letter, and one number.", false);
            return;
        }

        try {
            boolean updated = authService.changePassword(currentUser, currentPassword, newPassword);
            if (updated) {
                setChangePasswordFeedback("Password updated successfully.", true);
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
            } else {
                setChangePasswordFeedback("Current password is incorrect.", false);
            }
        } catch (SQLException e) {
            setChangePasswordFeedback("Password update failed: " + e.getMessage(), false);
        }
    }

    private String buildInitials(Utilisateur utilisateur) {
        String prenom = utilisateur.getPrenom() != null && !utilisateur.getPrenom().isBlank()
                ? utilisateur.getPrenom().substring(0, 1).toUpperCase() : "";
        String nom = utilisateur.getNom() != null && !utilisateur.getNom().isBlank()
                ? utilisateur.getNom().substring(0, 1).toUpperCase() : "";
        return prenom + nom;
    }

    private void setSectionVisibility(boolean profileVisible, boolean changePasswordVisible) {
        if (profileSection != null) {
            profileSection.setVisible(profileVisible);
            profileSection.setManaged(profileVisible);
        }
        if (changePasswordSection != null) {
            changePasswordSection.setVisible(changePasswordVisible);
            changePasswordSection.setManaged(changePasswordVisible);
        }
    }

    private void setChangePasswordFeedback(String message, boolean success) {
        if (changePasswordFeedbackLabel == null) {
            return;
        }
        changePasswordFeedbackLabel.setText(message != null ? message : "");
        changePasswordFeedbackLabel.getStyleClass().remove("status-success");
        if (success) {
            changePasswordFeedbackLabel.getStyleClass().add("status-success");
        }
    }

    private boolean isStrongPassword(String password) {
        return STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }

    public static void logoutToLogin() {
        try {
            UserSession.clear();
            SceneManager.switchScene("/login.fxml", "Campus Access");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
