package gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;

public class SidebarEtudiantController {

    @FXML
    private Circle avatarCircle;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private Button overviewBtn;

    @FXML
    private Button myCoursesBtn;

    @FXML
    private Button aiAssistantBtn;

    @FXML
    private Button allCoursesBtn;

    @FXML
    private Button eventsBtn;

    @FXML
    private Button mesSoumissionsBtn;

    @FXML
    private Button mesNotesBtn;

    @FXML
    private Button quizBtn;

    @FXML
    private Button quizResultsBtn;

    @FXML
    private Button forumBtn;

    @FXML
    private Button messagesBtn;

    @FXML
    private Button deconnexionBtn;

    private String studentId = "ETU001";
    private String studentName = "Etudiant";
    private MainLayoutEtudiantController mainController;

    @FXML
    public void initialize() {
        userNameLabel.setText(studentName);
        userRoleLabel.setText(studentId);
    }

    public void setStudentInfo(String id, String name) {
        studentId = id;
        studentName = name;
        userNameLabel.setText(name);
        userRoleLabel.setText(id);
    }

    public void setMainController(MainLayoutEtudiantController controller) {
        mainController = controller;
    }

    @FXML
    private void handleOverview() {
        if (mainController != null) {
            mainController.loadStudentOverview();
        }
    }

    @FXML
    private void handleEventsHub() {
        if (mainController != null) {
            mainController.showEventsHub();
        }
    }

    @FXML
    private void handleMyCourses() {
        if (mainController != null) {
            mainController.showMyCourses();
        }
    }

    @FXML
    private void handleAiAssistant() {
        if (mainController != null) {
            mainController.showStudentCourseAssistant();
        }
    }

    @FXML
    private void handleAllCourses() {
        if (mainController != null) {
            mainController.showAllCourses();
        }
    }

    @FXML
    private void handleMesSoumissions() {
        if (mainController != null) {
            mainController.showMesSoumissions();
        }
    }

    @FXML
    private void handleMesNotes() {
        if (mainController != null) {
            mainController.showMesNotes();
        }
    }

    @FXML
    private void handleQuizHub() {
        if (mainController != null) {
            mainController.showStudentQuizPage();
        }
    }

    @FXML
    private void handleQuizResultsHub() {
        if (mainController != null) {
            mainController.showStudentQuizResultsPage();
        }
    }

    @FXML
    private void handleForumHub() {
        if (mainController != null) {
            mainController.showStudentForumPage();
        }
    }

    @FXML
    private void handleMessagesHub() {
        if (mainController != null) {
            mainController.showStudentMessagesPage();
        }
    }

    @FXML
    private void handleDeconnexion() {
        StudentDashboardController.logoutToLogin();
    }

    public void setActiveButton(String buttonId) {
        resetButtonStyles();

        Button activeBtn = switch (buttonId) {
            case "overview" -> overviewBtn;
            case "my_courses" -> myCoursesBtn;
            case "all_courses" -> allCoursesBtn;
            case "events" -> eventsBtn;
            case "soumissions" -> mesSoumissionsBtn;
            case "notes" -> mesNotesBtn;
            case "quiz" -> quizBtn;
            case "quiz_results" -> quizResultsBtn;
            case "forum" -> forumBtn;
            case "messages" -> messagesBtn;
            default -> null;
        };

        if (activeBtn != null) {
            activeBtn.getStyleClass().add("sidebar-button-active");
        }
    }

    private void resetButtonStyles() {
        if (overviewBtn != null) {
            overviewBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (myCoursesBtn != null) {
            myCoursesBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (allCoursesBtn != null) {
            allCoursesBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (aiAssistantBtn != null) {
            aiAssistantBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (eventsBtn != null) {
            eventsBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (mesSoumissionsBtn != null) {
            mesSoumissionsBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (mesNotesBtn != null) {
            mesNotesBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (quizBtn != null) {
            quizBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (quizResultsBtn != null) {
            quizResultsBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (forumBtn != null) {
            forumBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (messagesBtn != null) {
            messagesBtn.getStyleClass().remove("sidebar-button-active");
        }
    }
}
