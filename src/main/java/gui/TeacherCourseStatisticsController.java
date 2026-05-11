package gui;

import entities.Enseignant;
import entities.Utilisateur;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import services.TeacherCourseStatisticsService;
import utils.UserSession;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TeacherCourseStatisticsController implements MainControllerAware {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.FRENCH);

    @FXML private Label pageSubtitleLabel;
    @FXML private Label totalViewsValueLabel;
    @FXML private Label totalViewsMetaLabel;
    @FXML private Label totalTimeValueLabel;
    @FXML private Label totalTimeMetaLabel;
    @FXML private Label topVisitedCourseValueLabel;
    @FXML private Label topVisitedCourseMetaLabel;
    @FXML private Label topTimeCourseValueLabel;
    @FXML private Label topTimeCourseMetaLabel;
    @FXML private Label topStudentValueLabel;
    @FXML private Label topStudentMetaLabel;

    @FXML private TableView<TeacherCourseStatisticsService.CourseRankingRow> courseStatsTable;
    @FXML private TableColumn<TeacherCourseStatisticsService.CourseRankingRow, String> courseCodeColumn;
    @FXML private TableColumn<TeacherCourseStatisticsService.CourseRankingRow, String> courseTitleColumn;
    @FXML private TableColumn<TeacherCourseStatisticsService.CourseRankingRow, String> courseModuleColumn;
    @FXML private TableColumn<TeacherCourseStatisticsService.CourseRankingRow, Number> courseViewsColumn;
    @FXML private TableColumn<TeacherCourseStatisticsService.CourseRankingRow, String> courseTimeColumn;
    @FXML private TableColumn<TeacherCourseStatisticsService.CourseRankingRow, Number> courseActiveStudentsColumn;

    @FXML private TableView<TeacherCourseStatisticsService.StudentEngagementRow> overallStudentsTable;
    @FXML private TableColumn<TeacherCourseStatisticsService.StudentEngagementRow, String> overallStudentNameColumn;
    @FXML private TableColumn<TeacherCourseStatisticsService.StudentEngagementRow, String> overallStudentMatriculeColumn;
    @FXML private TableColumn<TeacherCourseStatisticsService.StudentEngagementRow, String> overallStudentTimeColumn;
    @FXML private TableColumn<TeacherCourseStatisticsService.StudentEngagementRow, Number> overallStudentCoursesColumn;
    @FXML private TableColumn<TeacherCourseStatisticsService.StudentEngagementRow, String> overallStudentLastActivityColumn;

    @FXML private ComboBox<CourseFilterOption> courseFilterCombo;
    @FXML private TableView<TeacherCourseStatisticsService.StudentEngagementRow> filteredStudentsTable;
    @FXML private TableColumn<TeacherCourseStatisticsService.StudentEngagementRow, String> filteredStudentNameColumn;
    @FXML private TableColumn<TeacherCourseStatisticsService.StudentEngagementRow, String> filteredStudentMatriculeColumn;
    @FXML private TableColumn<TeacherCourseStatisticsService.StudentEngagementRow, String> filteredStudentTimeColumn;
    @FXML private TableColumn<TeacherCourseStatisticsService.StudentEngagementRow, String> filteredStudentLastActivityColumn;

    private final TeacherCourseStatisticsService statisticsService = new TeacherCourseStatisticsService();
    private final Map<Integer, List<TeacherCourseStatisticsService.StudentEngagementRow>> courseStudentRankingCache = new HashMap<>();
    private TeacherCourseStatisticsService.DashboardData dashboardData =
            new TeacherCourseStatisticsService.DashboardData(List.of(), List.of());

    @FXML
    public void initialize() {
        configureTables();
        loadStatistics();
    }

    private void configureTables() {
        courseCodeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCourseCode()));
        courseTitleColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCourseTitle()));
        courseModuleColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getModuleTitle()));
        courseViewsColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getViewCount()));
        courseTimeColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatDuration(cell.getValue().getTotalTimeSeconds())));
        courseActiveStudentsColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getActiveStudents()));

        overallStudentNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStudentName()));
        overallStudentMatriculeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMatricule()));
        overallStudentTimeColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatDuration(cell.getValue().getTotalTimeSeconds())));
        overallStudentCoursesColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getActiveCourses()));
        overallStudentLastActivityColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatDateTime(cell.getValue().getLastActivity())));

        filteredStudentNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStudentName()));
        filteredStudentMatriculeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMatricule()));
        filteredStudentTimeColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatDuration(cell.getValue().getTotalTimeSeconds())));
        filteredStudentLastActivityColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatDateTime(cell.getValue().getLastActivity())));

        courseFilterCombo.valueProperty().addListener((obs, oldValue, newValue) -> applyCourseFilter(newValue));
    }

    private void loadStatistics() {
        Utilisateur currentUser = UserSession.getCurrentUser();
        if (!(currentUser instanceof Enseignant enseignant) || enseignant.getId() == null) {
            pageSubtitleLabel.setText("Session enseignant introuvable.");
            resetSummaryLabels();
            return;
        }

        try {
            dashboardData = statisticsService.loadDashboardData(enseignant.getId());
            pageSubtitleLabel.setText(dashboardData.getCourseRankings().isEmpty()
                    ? "Aucune statistique disponible pour vos cours."
                    : "Suivez les cours les plus visites, le temps cumule et les etudiants les plus engages.");
            courseStatsTable.setItems(FXCollections.observableArrayList(dashboardData.getCourseRankings()));
            overallStudentsTable.setItems(FXCollections.observableArrayList(dashboardData.getOverallStudentRankings()));
            courseStudentRankingCache.clear();
            updateSummaryLabels();
            populateCourseFilter();
        } catch (SQLException e) {
            pageSubtitleLabel.setText("Chargement impossible: " + e.getMessage());
            dashboardData = new TeacherCourseStatisticsService.DashboardData(List.of(), List.of());
            courseStatsTable.setItems(FXCollections.observableArrayList());
            overallStudentsTable.setItems(FXCollections.observableArrayList());
            filteredStudentsTable.setItems(FXCollections.observableArrayList());
            resetSummaryLabels();
        }
    }

    @FXML
    private void handleRefresh() {
        loadStatistics();
    }

    private void populateCourseFilter() {
        List<CourseFilterOption> options = new ArrayList<>();
        options.add(new CourseFilterOption(null, "Tous les cours"));
        for (TeacherCourseStatisticsService.CourseRankingRow row : dashboardData.getCourseRankings()) {
            options.add(new CourseFilterOption(row.getCourseId(), row.getCourseCode() + " - " + row.getCourseTitle()));
        }
        courseFilterCombo.setItems(FXCollections.observableArrayList(options));
        courseFilterCombo.setValue(options.get(0));
        applyCourseFilter(options.get(0));
    }

    private void applyCourseFilter(CourseFilterOption option) {
        if (option == null || option.courseId() == null) {
            filteredStudentsTable.setItems(FXCollections.observableArrayList(dashboardData.getOverallStudentRankings()));
            return;
        }

        List<TeacherCourseStatisticsService.StudentEngagementRow> rows = courseStudentRankingCache.computeIfAbsent(
                option.courseId(),
                courseId -> {
                    try {
                        return statisticsService.loadStudentRankingForCourse(courseId);
                    } catch (SQLException e) {
                        return List.of();
                    }
                }
        );
        filteredStudentsTable.setItems(FXCollections.observableArrayList(rows));
    }

    private void updateSummaryLabels() {
        totalViewsValueLabel.setText(String.valueOf(dashboardData.getTotalViews()));
        totalViewsMetaLabel.setText("Vues uniques sur vos cours");

        totalTimeValueLabel.setText(formatDuration(dashboardData.getTotalTimeSeconds()));
        totalTimeMetaLabel.setText("Temps cumule par tous les etudiants");

        TeacherCourseStatisticsService.CourseRankingRow mostVisited = dashboardData.getMostVisitedCourse();
        topVisitedCourseValueLabel.setText(mostVisited != null ? mostVisited.getCourseTitle() : "-");
        topVisitedCourseMetaLabel.setText(mostVisited != null
                ? mostVisited.getViewCount() + " vue(s) unique(s)"
                : "Aucune vue");

        TeacherCourseStatisticsService.CourseRankingRow mostTime = dashboardData.getCourseWithMostTime();
        topTimeCourseValueLabel.setText(mostTime != null ? mostTime.getCourseTitle() : "-");
        topTimeCourseMetaLabel.setText(mostTime != null
                ? formatDuration(mostTime.getTotalTimeSeconds())
                : "Aucun temps mesure");

        TeacherCourseStatisticsService.StudentEngagementRow topStudent = dashboardData.getTopStudent();
        topStudentValueLabel.setText(topStudent != null ? topStudent.getStudentName() : "-");
        topStudentMetaLabel.setText(topStudent != null
                ? formatDuration(topStudent.getTotalTimeSeconds()) + " sur " + topStudent.getActiveCourses() + " cours"
                : "Aucune activite");
    }

    private void resetSummaryLabels() {
        totalViewsValueLabel.setText("0");
        totalViewsMetaLabel.setText("Vues uniques sur vos cours");
        totalTimeValueLabel.setText("0 s");
        totalTimeMetaLabel.setText("Temps cumule par tous les etudiants");
        topVisitedCourseValueLabel.setText("-");
        topVisitedCourseMetaLabel.setText("Aucune vue");
        topTimeCourseValueLabel.setText("-");
        topTimeCourseMetaLabel.setText("Aucun temps mesure");
        topStudentValueLabel.setText("-");
        topStudentMetaLabel.setText("Aucune activite");
    }

    private String formatDuration(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "0 s";
        }
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return minutes > 0 ? hours + " h " + minutes + " min" : hours + " h";
        }
        if (minutes > 0) {
            return seconds > 0 ? minutes + " min " + seconds + " s" : minutes + " min";
        }
        return seconds + " s";
    }

    private String formatDateTime(LocalDateTime value) {
        return value != null ? DATE_TIME_FORMATTER.format(value) : "-";
    }

    @Override
    public void setMainController(MainLayoutEnseignantController controller) {
    }

    private record CourseFilterOption(Integer courseId, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
