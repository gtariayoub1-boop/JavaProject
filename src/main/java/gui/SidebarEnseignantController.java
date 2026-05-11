package gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;

public class SidebarEnseignantController {

    @FXML
    private Circle avatarCircle;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private Button overviewBtn;

    @FXML
    private Button teacherStudentsBtn;

    @FXML
    private Button statisticsBtn;

    @FXML
    private Button newCourseBtn;

    @FXML
    private Button eventsBtn;

    @FXML
    private Button mesEvaluationsBtn;

    @FXML
    private Button mesCorrectionsBtn;

    @FXML
    private Button coursesBtn;

    @FXML
    private Button quizBtn;

    @FXML
    private Button newQuizBtn;

    @FXML
    private Button forumBtn;

    @FXML
    private Button messagesBtn;

    @FXML
    private Button deconnexionBtn;

    private String teacherId = "PROF001";
    private String teacherName = "Enseignant";
    private MainLayoutEnseignantController mainController;

    @FXML
    public void initialize() {
        userNameLabel.setText(teacherName);
        userRoleLabel.setText(teacherId);
    }

    public void setTeacherInfo(String id, String name) {
        teacherId = id;
        teacherName = name;
        userNameLabel.setText(name);
        userRoleLabel.setText(id);
    }

    public void setMainController(MainLayoutEnseignantController controller) {
        mainController = controller;
    }

    @FXML
    private void handleOverview() {
        if (mainController != null) {
            mainController.loadTeacherOverview();
        }
    }

    @FXML
    private void handleEventsHub() {
        if (mainController != null) {
            mainController.showEventsHub();
        }
    }

    @FXML
    private void handleStudentProfiles() {
        if (mainController != null) {
            mainController.showTeacherStudents();
        }
    }

    @FXML
    private void handleStatistics() {
        if (mainController != null) {
            mainController.showTeacherCourseStatistics();
        }
    }


    @FXML
    private void handleNewCourse() {
        if (mainController != null) {
            mainController.showTeacherCourseForm(null);
        }
    }


    @FXML
    private void handleCourseManagement() {
        if (mainController != null) {
            mainController.showCourseManagement();
        }
    }

    @FXML
    private void handleMesEvaluations() {
        if (mainController != null) {
            mainController.showMesEvaluations();
        }
    }

    @FXML
    private void handleMesCorrections() {
        if (mainController != null) {
            mainController.showMesCorrections();
        }
    }

    @FXML
    private void handleQuizHub() {
        if (mainController != null) {
            mainController.showTeacherQuizPage();
        }
    }

    @FXML
    private void handleNewQuizHub() {
        if (mainController != null) {
            mainController.showTeacherNewQuizPage();
        }
    }

    @FXML
    private void handleForumHub() {
        if (mainController != null) {
            mainController.showTeacherForumPage();
        }
    }

    @FXML
    private void handleMessagesHub() {
        if (mainController != null) {
            mainController.showTeacherMessagesPage();
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
            case "statistics" -> statisticsBtn;
            case "students" -> teacherStudentsBtn;
            case "new_course" -> newCourseBtn;
            case "events" -> eventsBtn;
            case "evaluations" -> mesEvaluationsBtn;
            case "corrections" -> mesCorrectionsBtn;
            case "course_management" -> coursesBtn;
            case "quiz" -> quizBtn;
            case "new_quiz" -> newQuizBtn;
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
        if (teacherStudentsBtn != null) {
            teacherStudentsBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (statisticsBtn != null) {
            statisticsBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (newCourseBtn != null) {
            newCourseBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (eventsBtn != null) {
            eventsBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (mesEvaluationsBtn != null) {
            mesEvaluationsBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (mesCorrectionsBtn != null) {
            mesCorrectionsBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (coursesBtn != null) {
            coursesBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (quizBtn != null) {
            quizBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (newQuizBtn != null) {
            newQuizBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (forumBtn != null) {
            forumBtn.getStyleClass().remove("sidebar-button-active");
        }
        if (messagesBtn != null) {
            messagesBtn.getStyleClass().remove("sidebar-button-active");
        }
    }
}
