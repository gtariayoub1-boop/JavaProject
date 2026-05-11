package gui;

import entities.Cours;
import entities.Enseignant;
import entities.Evaluation;
import entities.Score;
import entities.Soumission;
import entities.Utilisateur;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.util.Duration;
import utils.DashboardNavigation;
import utils.QuizNavigation;
import utils.UserSession;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

interface MainControllerAware {
    void setMainController(MainLayoutEnseignantController controller);
}

public class MainLayoutEnseignantController {

    @FXML private StackPane contentPane;
    @FXML private SidebarEnseignantController sidebarController;

    private String           teacherId    = "PROF001";
    private String           teacherName  = "Enseignant";
    private long             enseignantId = 0L;
    private Cours            editingCourse;
    private Thread           sseThread;
    private volatile boolean sseRunning   = false;

    // ─────────────────────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        Utilisateur u = UserSession.getCurrentUser();
        if (u instanceof Enseignant e) {
            teacherId    = e.getMatriculeEnseignant();
            teacherName  = e.getNomComplet();
            enseignantId = e.getId();
        }
        if (sidebarController != null) {
            sidebarController.setTeacherInfo(teacherId, teacherName);
            sidebarController.setMainController(this);
        }
        loadTeacherOverview();
        if (enseignantId > 0) demarrerSSE();
    }

    // ─────────────────────────────────────────────────────────────
    // SSE
    // ─────────────────────────────────────────────────────────────
    private void demarrerSSE() {
        if (sseThread != null && sseThread.isAlive()) return;
        sseRunning = true;
        sseThread = new Thread(() -> {
            while (sseRunning) {
                try {
                    // ✅ Topic spécifique à cet enseignant
                    String topic  = "quiz/resultat/" + enseignantId;
                    String urlStr = "http://localhost:3000/.well-known/mercure?topic="
                            + java.net.URLEncoder.encode(topic, StandardCharsets.UTF_8);

                    HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "text/event-stream");
                    conn.setRequestProperty("Cache-Control", "no-cache");
                    conn.setRequestProperty("Connection", "keep-alive");
                    conn.setUseCaches(false);
                    conn.setDoInput(true);
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(0);
                    conn.connect();

                    System.out.println("SSE connecté → topic=" + topic);

                    InputStream is = conn.getInputStream();
                    StringBuilder buf = new StringBuilder();
                    int c;
                    while (sseRunning && (c = is.read()) != -1) {
                        char ch = (char) c;
                        if (ch == '\n') {
                            String line = buf.toString().trim();
                            buf.setLength(0);
                            if (line.startsWith("data:")) {
                                final String data = line.substring(5).trim();
                                if (!data.isEmpty())
                                    Platform.runLater(() -> afficherToast(data));
                            }
                        } else {
                            buf.append(ch);
                        }
                    }
                } catch (Exception ex) {
                    if (sseRunning) {
                        System.err.println("SSE erreur : " + ex.getMessage());
                        try { Thread.sleep(5000); } catch (InterruptedException ie) { break; }
                    }
                }
            }
        }, "mercure-sse");
        sseThread.setDaemon(true);
        sseThread.start();
    }

    public void arreterSSE() {
        sseRunning = false;
        if (sseThread != null) sseThread.interrupt();
    }

    // ─────────────────────────────────────────────────────────────
    // TOAST
    // ─────────────────────────────────────────────────────────────
    private void afficherToast(String data) {
        String etudiant = extraireChamp(data, "etudiant");
        String quiz     = extraireChamp(data, "quiz");
        String score    = extraireChamp(data, "score");
        if (score != null) score = score.replace(',', '.');

        String nomEtudiant = etudiant != null ? etudiant : "Un etudiant";
        String nomQuiz     = quiz     != null ? quiz     : "un quiz";
        String scoreStr    = score    != null ? score    : "?";

        Label titre = new Label("Nouvelle soumission");
        titre.setStyle("-fx-text-fill:#2dd4bf;-fx-font-size:13px;-fx-font-weight:800;");

        Label msg = new Label(nomEtudiant + " a termine \"" + nomQuiz + "\"");
        msg.setWrapText(true);
        msg.setMaxWidth(280);
        msg.setStyle("-fx-text-fill:#e5e7eb;-fx-font-size:12px;");

        Label scoreLabel = new Label("Score : " + scoreStr + "%");
        scoreLabel.setStyle("-fx-text-fill:#86efac;-fx-font-size:13px;-fx-font-weight:700;");

        Button btnClose = new Button("X");
        btnClose.setStyle(
                "-fx-background-color:rgba(255,255,255,0.10);"
                        + "-fx-text-fill:#94a3b8;-fx-font-size:11px;-fx-cursor:hand;"
                        + "-fx-background-radius:999px;-fx-min-width:22px;-fx-min-height:22px;"
                        + "-fx-max-width:22px;-fx-max-height:22px;-fx-padding:0;"
        );

        HBox header = new HBox(8, titre);
        header.setAlignment(Pos.CENTER_LEFT);
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(spacer, btnClose);

        VBox container = new VBox(6, header, msg, scoreLabel);
        container.setStyle(
                "-fx-background-color:#0f172a;-fx-background-radius:14px;"
                        + "-fx-border-color:#2dd4bf;-fx-border-radius:14px;-fx-border-width:1.5px;"
                        + "-fx-padding:14px 16px;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.70),20,0,0,6);"
        );
        container.setPrefWidth(320);
        container.setMaxWidth(320);
        container.setOpacity(0);

        Popup popup = new Popup();
        popup.getContent().add(container);
        popup.setAutoFix(false);
        popup.setAutoHide(false);

        javafx.stage.Stage stage = utils.SceneManager.getStage();
        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        popup.show(stage, screen.getMaxX() - 340, screen.getMaxY() - 130);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), container);
        fadeIn.setFromValue(0); fadeIn.setToValue(1); fadeIn.play();

        Runnable supprimer = () -> {
            FadeTransition fo = new FadeTransition(Duration.millis(250), container);
            fo.setFromValue(1); fo.setToValue(0);
            fo.setOnFinished(e -> popup.hide());
            fo.play();
        };

        btnClose.setOnAction(e -> supprimer.run());
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> supprimer.run());
        pause.play();
    }

    private String extraireChamp(String json, String champ) {
        try {
            String key = "\"" + champ + "\":";
            int idx = json.indexOf(key);
            if (idx < 0) return null;
            int start = idx + key.length();
            if (start < json.length() && json.charAt(start) == '"') {
                int q2 = json.indexOf('"', start + 1);
                return q2 >= 0 ? json.substring(start + 1, q2) : null;
            }
            int end = start;
            while (end < json.length() && ",}".indexOf(json.charAt(end)) < 0) end++;
            return json.substring(start, end).trim();
        } catch (Exception ignored) {}
        return null;
    }

    // ─────────────────────────────────────────────────────────────
    // NAVIGATION
    // ─────────────────────────────────────────────────────────────
    private void setContent(Node content) {
        contentPane.getChildren().setAll(content);
    }

    public void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();

            if (content instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMaxHeight(Double.MAX_VALUE);
                StackPane.setAlignment(region, javafx.geometry.Pos.TOP_LEFT);
            }

            setContent(content);
            Object controller = loader.getController();
            if (controller instanceof MainControllerAware aware)
                aware.setMainController(this);
        } catch (IOException e) {
            System.err.println("Erreur chargement : " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void loadEmbeddedCenterContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Node content = (root instanceof BorderPane bp && bp.getCenter() != null)
                    ? bp.getCenter() : root;
            setContent(content);
        } catch (IOException e) {
            System.err.println("Erreur chargement embarqué : " + fxmlPath);
            e.printStackTrace();
        }
    }

    public void loadTeacherOverview() {
        loadContent("/teacher-overview.fxml");
        if (sidebarController != null) sidebarController.setActiveButton("overview");
    }

    public void showTeacherCourses() {
        loadContent("/teacher-courses.fxml");
        if (sidebarController != null) sidebarController.setActiveButton("courses");
    }

    public void showTeacherStudents() {
        loadContent("/teacher-students.fxml");
        if (sidebarController != null) sidebarController.setActiveButton("students");
    }

    public void showTeacherCourseStatistics() {
        loadContent("/teacher-course-statistics.fxml");
        if (sidebarController != null) sidebarController.setActiveButton("statistics");
    }

    public void showTeacherCourseForm(Cours cours) {
        editingCourse = cours;
        loadContent("/teacher-course-form.fxml");
        if (sidebarController != null) sidebarController.setActiveButton("new_course");
    }

    public void showMesEvaluations()   { loadContent("/evaluation-list.fxml"); }
    public void showMesCorrections()   { loadContent("/score-list.fxml"); }

    public void showCourseManagement() {
        loadContent("/course-management.fxml");
        if (sidebarController != null) sidebarController.setActiveButton("course_management");
    }

    public void showTeacherProfilePage() {
        DashboardNavigation.openTeacherSection(DashboardNavigation.TeacherSection.PROFILE);
        loadEmbeddedCenterContent("/teacher-dashboard.fxml");
        if (sidebarController != null) sidebarController.setActiveButton("profile");
    }

    public void showTeacherDashboardStudentsPage() {
        DashboardNavigation.openTeacherSection(DashboardNavigation.TeacherSection.STUDENTS);
        loadEmbeddedCenterContent("/teacher-dashboard.fxml");
        if (sidebarController != null) sidebarController.setActiveButton("students_profile");
    }

    public void showTeacherChangePasswordPage() {
        DashboardNavigation.openTeacherSection(DashboardNavigation.TeacherSection.CHANGE_PASSWORD);
        loadEmbeddedCenterContent("/teacher-dashboard.fxml");
        if (sidebarController != null) sidebarController.setActiveButton("security");
    }

    public void showTeacherQuizPage() {
        QuizNavigation.openTeacherSection(QuizNavigation.TeacherSection.LIST);
        loadContent("/teacher-quiz.fxml");
        if (sidebarController != null) sidebarController.setActiveButton("quiz");
    }

    public void showTeacherNewQuizPage() {
        QuizNavigation.openTeacherSection(QuizNavigation.TeacherSection.CREATE);
        loadContent("/teacher-quiz.fxml");
        if (sidebarController != null) sidebarController.setActiveButton("new_quiz");
    }

    public void showTeacherForumPage() {
        loadContent("/teacher_forum.fxml"); // ✅
        if (sidebarController != null) sidebarController.setActiveButton("forum");
    }

    public void showTeacherMessagesPage() {
        loadContent("/teacher_message.fxml"); // ✅
        if (sidebarController != null) sidebarController.setActiveButton("messages");
    }

    public void showEventsHub() {
        loadEmbeddedCenterContent("/gui/EventManagement.fxml");
        if (sidebarController != null) {
            sidebarController.setActiveButton("events");
        }
    }

    public Cours consumeEditingCourse() {
        Cours cours = editingCourse;
        editingCourse = null;
        return cours;
    }

    public void showEvaluationCreate() { loadContent("/evaluation-create.fxml"); }

    public void showEvaluationDetail(Evaluation evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/evaluation-detail.fxml"));
            Node content = loader.load();
            EvaluationDetailController c = loader.getController();
            c.setEvaluation(evaluation); c.setMainController(this);
            setContent(content);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void showEvaluationEdit(Evaluation evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/evaluation-edit.fxml"));
            Node content = loader.load();
            EvaluationEditController c = loader.getController();
            c.setEvaluation(evaluation); c.setMainController(this);
            setContent(content);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void showScoreCreate(Soumission soumission) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/score-create.fxml"));
            Node content = loader.load();
            ScoreCreateController c = loader.getController();
            c.setTeacherId(teacherId); c.setSoumission(soumission); c.setMainController(this);
            setContent(content);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void showScoreDetail(Score score) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/score-detail.fxml"));
            Node content = loader.load();
            ScoreDetailController c = loader.getController();
            c.setScore(score); c.setMainController(this);
            setContent(content);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void showScoreEdit(Score score) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/score-edit.fxml"));
            Node content = loader.load();
            ScoreEditController c = loader.getController();
            c.setScore(score); c.setMainController(this);
            setContent(content);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void setTeacherInfo(String id, String name) {
        this.teacherId   = id;
        this.teacherName = name;
        if (sidebarController != null) sidebarController.setTeacherInfo(id, name);
    }
}