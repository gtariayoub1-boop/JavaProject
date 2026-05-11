package gui;

import entities.Cours;
import entities.Etudiant;
import entities.Score;
import entities.Soumission;
import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import utils.DashboardNavigation;
import utils.QuizNavigation;
import utils.UserSession;

import java.io.IOException;

interface MainControllerAwareEtudiant {
    void setMainController(MainLayoutEtudiantController controller);
}

public class MainLayoutEtudiantController {

    @FXML
    private StackPane contentPane;

    @FXML
    private SidebarEtudiantController sidebarController;

    private String studentId = "ETU001";
    private String studentName = "Etudiant";

    @FXML
    public void initialize() {
        Utilisateur currentUser = UserSession.getCurrentUser();
        if (currentUser instanceof Etudiant etudiant) {
            studentId = etudiant.getMatricule();
            studentName = etudiant.getNomComplet();
        }
        if (sidebarController != null) {
            sidebarController.setStudentInfo(studentId, studentName);
            sidebarController.setMainController(this);
        }
        loadStudentOverview();
    }

    public void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            contentPane.getChildren().setAll(content);

            Object controller = loader.getController();
            if (controller instanceof MainControllerAwareEtudiant aware) {
                aware.setMainController(this);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void loadEmbeddedCenterContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Node content = root;
            if (root instanceof BorderPane borderPane && borderPane.getCenter() != null) {
                content = borderPane.getCenter();
            }
            contentPane.getChildren().setAll(content);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement embarque de la vue: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public void loadStudentOverview() {
        loadContent("/student-overview.fxml");
        if (sidebarController != null) {
            sidebarController.setActiveButton("overview");
        }
    }

    public void showMyCourses() {
        loadContent("/student-my-courses.fxml");
        if (sidebarController != null) {
            sidebarController.setActiveButton("my_courses");
        }
    }

    public void showAllCourses() {
        loadContent("/student-all-courses.fxml");
        if (sidebarController != null) {
            sidebarController.setActiveButton("all_courses");
        }
    }

    public void showStudentCourseAssistant() {
        StudentCourseAssistantDialog.open(null);
    }

    public void showStudentCourseContent(Cours cours) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/student-course-content.fxml"));
            Node content = loader.load();

            StudentCourseContentController controller = loader.getController();
            controller.setMainController(this);
            controller.setCours(cours);

            contentPane.getChildren().setAll(content);
            if (sidebarController != null) {
                sidebarController.setActiveButton("my_courses");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du contenu du cours: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showSoumissionCreate() {
        loadContent("/soumission-create.fxml");
    }

    public void showSoumissionDetail(Soumission soumission) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/soumission-detail.fxml"));
            Node content = loader.load();

            SoumissionDetailController controller = loader.getController();
            controller.setSoumission(soumission);
            controller.setStudentId(studentId);
            controller.setMainController(this);

            contentPane.getChildren().setAll(content);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue detail: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showSoumissionEdit(Soumission soumission) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/soumission-edit.fxml"));
            Node content = loader.load();

            SoumissionEditController controller = loader.getController();
            controller.setSoumission(soumission);
            controller.setStudentId(studentId);
            controller.setMainController(this);

            contentPane.getChildren().setAll(content);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue edition: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showStudentScoreDetail(Score score) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/student-score-detail.fxml"));
            Node content = loader.load();

            StudentScoreDetailController controller = loader.getController();
            controller.setScore(score);
            controller.setStudentId(studentId);
            controller.setMainController(this);

            contentPane.getChildren().setAll(content);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue note: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showMesSoumissions() {
        loadContent("/soumission-list.fxml");
    }

    public void showMesNotes() {
        loadContent("/student-score-list.fxml");
    }

    public void showStudentProfilePage() {
        DashboardNavigation.openStudentSection(DashboardNavigation.StudentSection.PROFILE);
        loadEmbeddedCenterContent("/student-dashboard.fxml");
        if (sidebarController != null) {
            sidebarController.setActiveButton("profile");
        }
    }

    public void showStudentChangePasswordPage() {
        DashboardNavigation.openStudentSection(DashboardNavigation.StudentSection.CHANGE_PASSWORD);
        loadEmbeddedCenterContent("/student-dashboard.fxml");
        if (sidebarController != null) {
            sidebarController.setActiveButton("security");
        }
    }

    public void showStudentQuizPage() {
        QuizNavigation.openStudentSection(QuizNavigation.StudentSection.LIST);
        loadEmbeddedCenterContent("/student-quiz.fxml");
        if (sidebarController != null) {
            sidebarController.setActiveButton("quiz");
        }
    }

    public void showStudentQuizResultsPage() {
        QuizNavigation.openStudentSection(QuizNavigation.StudentSection.RESULTS);
        loadEmbeddedCenterContent("/student-quiz.fxml");
        if (sidebarController != null) {
            sidebarController.setActiveButton("quiz_results");
        }
    }

    public void showStudentForumPage() {
        loadContent("/student_forum.fxml");
        if (sidebarController != null) sidebarController.setActiveButton("forum");
    }

    public void showStudentMessagesPage() {
        loadContent("/student_message.fxml");
        if (sidebarController != null) sidebarController.setActiveButton("messages");
    }

    public void showEventsHub() {
        loadEmbeddedCenterContent("/gui/EventManagement.fxml");
        if (sidebarController != null) {
            sidebarController.setActiveButton("events");
        }
    }

    public void setStudentInfo(String id, String name) {
        this.studentId = id;
        this.studentName = name;
        if (sidebarController != null) {
            sidebarController.setStudentInfo(id, name);
        }
    }
}
