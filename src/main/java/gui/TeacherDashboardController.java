package gui;

import entities.Enseignant;
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
import services.EtudiantService;
import utils.DashboardNavigation;
import utils.SceneManager;
import utils.UserSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class TeacherDashboardController {
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
    private ListView<String> teachingList;

    @FXML
    private ListView<String> studentsList;

    @FXML
    private ListView<String> studentInsightsList;

    @FXML
    private VBox profileSection;

    @FXML
    private VBox studentsSection;

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
    private final ObservableList<String> teachingItems = FXCollections.observableArrayList();
    private final ObservableList<String> studentItems = FXCollections.observableArrayList();
    private final ObservableList<String> studentInsightItems = FXCollections.observableArrayList();

    private final AuthService authService = new AuthService();
    private final EtudiantService etudiantService = new EtudiantService();

    @FXML
    private void initialize() {
        profileList.setItems(profileItems);
        teachingList.setItems(teachingItems);
        studentsList.setItems(studentItems);
        studentInsightsList.setItems(studentInsightItems);

        Utilisateur utilisateur = UserSession.getCurrentUser();
        if (utilisateur instanceof Enseignant) {
            Enseignant enseignant = (Enseignant) utilisateur;
            welcomeLabel.setText("Hello, " + enseignant.getPrenom());
            roleLabel.setText("Teacher dashboard");
            currentUserNameLabel.setText(enseignant.getNomComplet());
            currentUserRoleLabel.setText(enseignant.getType());
            profileMenuButton.setText(buildInitials(enseignant));

            profileItems.setAll(
                    "Name: " + enseignant.getNomComplet(),
                    "Email: " + enseignant.getEmail(),
                    "Teacher ID: " + enseignant.getMatriculeEnseignant(),
                    "Diploma: " + enseignant.getDiplome(),
                    "Speciality: " + enseignant.getSpecialite(),
                    "Contract: " + enseignant.getTypeContrat(),
                    "Status: " + enseignant.getStatut()
            );
        }

        teachingItems.setAll(
                "Review teaching availability",
                "Prepare your active course material",
                "Track upcoming student interactions",
                "Refresh academic content and announcements"
        );

        loadStudentProfiles();

        DashboardNavigation.TeacherSection initialSection = DashboardNavigation.consumeTeacherSection();
        if (initialSection == DashboardNavigation.TeacherSection.STUDENTS) {
            showStudentsPage();
        } else if (initialSection == DashboardNavigation.TeacherSection.CHANGE_PASSWORD) {
            showChangePasswordPage();
        } else {
            showProfilePage();
        }
    }

    @FXML
    private void handleProfileLogout() throws IOException {
        UserSession.clear();
        SceneManager.switchScene("/login.fxml", "Campus Access");
    }

    @FXML
    private void showProfilePage() {
        setSectionVisibility(true, false, false);
    }

    @FXML
    private void showStudentsPage() {
        setSectionVisibility(false, true, false);
    }
    @FXML
    private void openForumPage() throws IOException {
        SceneManager.switchScene("/gui/teacher_forum.fxml", "Forum");
    }

    @FXML
    private void openMessagePage() throws IOException {
        SceneManager.switchScene("/gui/teacher_message.fxml", "Messagerie");
    }
    @FXML
    private void showChangePasswordPage() {
        if (changePasswordFeedbackLabel != null) {
            changePasswordFeedbackLabel.setText("");
            changePasswordFeedbackLabel.getStyleClass().remove("status-success");
        }
        if (currentPasswordField != null) {
            currentPasswordField.clear();
        }
        if (newPasswordField != null) {
            newPasswordField.clear();
        }
        if (confirmPasswordField != null) {
            confirmPasswordField.clear();
        }
        setSectionVisibility(false, false, true);
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

    private void loadStudentProfiles() {
        try {
            List<Etudiant> etudiants = etudiantService.getAll();
            if (etudiants.isEmpty()) {
                studentItems.setAll("No student profiles found.");
                studentInsightItems.setAll("Student registry is currently empty.");
                return;
            }

            studentItems.setAll(etudiants.stream()
                    .map(etudiant -> etudiant.getNomComplet()
                            + " | " + etudiant.getMatricule()
                            + " | " + etudiant.getNiveauEtudeAbrege()
                            + " | " + etudiant.getSpecialisation())
                    .toList());

            long activeStudents = etudiants.stream().filter(Etudiant::isActif).count();
            studentInsightItems.setAll(
                    "Total students: " + etudiants.size(),
                    "Active students: " + activeStudents,
                    "Browse profiles before planning follow-up sessions.",
                    "Use this panel as a quick student directory."
            );
        } catch (SQLException e) {
            studentItems.setAll("Failed to load students: " + e.getMessage());
            studentInsightItems.setAll("Student data is unavailable right now.");
        }
    }

    private void setSectionVisibility(boolean profileVisible, boolean studentsVisible, boolean changePasswordVisible) {
        if (profileSection != null) {
            profileSection.setVisible(profileVisible);
            profileSection.setManaged(profileVisible);
        }
        if (studentsSection != null) {
            studentsSection.setVisible(studentsVisible);
            studentsSection.setManaged(studentsVisible);
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

    private String buildInitials(Utilisateur utilisateur) {
        String prenom = utilisateur.getPrenom() != null && !utilisateur.getPrenom().isBlank()
                ? utilisateur.getPrenom().substring(0, 1).toUpperCase() : "";
        String nom = utilisateur.getNom() != null && !utilisateur.getNom().isBlank()
                ? utilisateur.getNom().substring(0, 1).toUpperCase() : "";
        return prenom + nom;
    }

    private boolean isStrongPassword(String password) {
        return STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }
}
