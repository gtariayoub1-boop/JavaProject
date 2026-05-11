package gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import services.AdminCourseStatisticsService;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class AdminCourseStatisticsController {

    private final AdminCourseStatisticsService statisticsService = new AdminCourseStatisticsService();

    @FXML
    private Label totalModulesValueLabel;

    @FXML
    private Label totalCoursesValueLabel;

    @FXML
    private Label totalViewsValueLabel;

    @FXML
    private Label totalTimeValueLabel;

    @FXML
    private Label topCourseValueLabel;

    @FXML
    private Label topModuleValueLabel;

    @FXML
    private TableView<AdminCourseStatisticsService.CourseStatsRow> courseStatsTable;

    @FXML
    private TableColumn<AdminCourseStatisticsService.CourseStatsRow, String> courseCodeColumn;

    @FXML
    private TableColumn<AdminCourseStatisticsService.CourseStatsRow, String> courseTitleColumn;

    @FXML
    private TableColumn<AdminCourseStatisticsService.CourseStatsRow, String> courseModuleColumn;

    @FXML
    private TableColumn<AdminCourseStatisticsService.CourseStatsRow, Integer> courseViewsColumn;

    @FXML
    private TableColumn<AdminCourseStatisticsService.CourseStatsRow, Long> courseTimeColumn;

    @FXML
    private TableColumn<AdminCourseStatisticsService.CourseStatsRow, Integer> courseStudentsColumn;

    @FXML
    private TableColumn<AdminCourseStatisticsService.CourseStatsRow, Integer> courseContentsColumn;

    @FXML
    private TableView<AdminCourseStatisticsService.ModuleStatsRow> moduleStatsTable;

    @FXML
    private TableColumn<AdminCourseStatisticsService.ModuleStatsRow, String> moduleTitleColumn;

    @FXML
    private TableColumn<AdminCourseStatisticsService.ModuleStatsRow, String> moduleStatusColumn;

    @FXML
    private TableColumn<AdminCourseStatisticsService.ModuleStatsRow, Integer> moduleCoursesColumn;

    @FXML
    private TableColumn<AdminCourseStatisticsService.ModuleStatsRow, Integer> moduleContentsColumn;

    @FXML
    private TableColumn<AdminCourseStatisticsService.ModuleStatsRow, Integer> moduleViewsColumn;

    @FXML
    private TableColumn<AdminCourseStatisticsService.ModuleStatsRow, Long> moduleTimeColumn;

    @FXML
    private TableColumn<AdminCourseStatisticsService.ModuleStatsRow, Integer> moduleStudentsColumn;

    @FXML
    private BarChart<String, Number> topCoursesChart;

    @FXML
    private BarChart<String, Number> topModulesChart;

    @FXML
    public void initialize() {
        configureCourseTable();
        configureModuleTable();
        refreshStats();
    }

    public void refreshStats() {
        try {
            AdminCourseStatisticsService.DashboardData data = statisticsService.loadDashboardData();
            List<AdminCourseStatisticsService.CourseStatsRow> courseRows = data.getCourseRows();
            List<AdminCourseStatisticsService.ModuleStatsRow> moduleRows = data.getModuleRows();

            courseStatsTable.setItems(FXCollections.observableArrayList(courseRows));
            moduleStatsTable.setItems(FXCollections.observableArrayList(moduleRows));

            updateSummaryCards(courseRows, moduleRows);
            updateCharts(courseRows, moduleRows);
        } catch (SQLException e) {
            totalModulesValueLabel.setText("-");
            totalCoursesValueLabel.setText("-");
            totalViewsValueLabel.setText("-");
            totalTimeValueLabel.setText("-");
            topCourseValueLabel.setText("Erreur de chargement");
            topModuleValueLabel.setText(e.getMessage());
            courseStatsTable.setItems(FXCollections.observableArrayList());
            moduleStatsTable.setItems(FXCollections.observableArrayList());
            topCoursesChart.getData().clear();
            topModulesChart.getData().clear();
        }
    }

    @FXML
    private void handleRefresh() {
        refreshStats();
    }

    private void configureCourseTable() {
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        courseTitleColumn.setCellValueFactory(new PropertyValueFactory<>("courseTitle"));
        courseModuleColumn.setCellValueFactory(new PropertyValueFactory<>("moduleTitle"));
        courseViewsColumn.setCellValueFactory(new PropertyValueFactory<>("viewCount"));
        courseTimeColumn.setCellValueFactory(new PropertyValueFactory<>("totalTimeSeconds"));
        courseStudentsColumn.setCellValueFactory(new PropertyValueFactory<>("activeStudents"));
        courseContentsColumn.setCellValueFactory(new PropertyValueFactory<>("contentCount"));
        courseTimeColumn.setCellFactory(column -> new DurationTableCell<>());
    }

    private void configureModuleTable() {
        moduleTitleColumn.setCellValueFactory(new PropertyValueFactory<>("moduleTitle"));
        moduleStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        moduleCoursesColumn.setCellValueFactory(new PropertyValueFactory<>("courseCount"));
        moduleContentsColumn.setCellValueFactory(new PropertyValueFactory<>("contentCount"));
        moduleViewsColumn.setCellValueFactory(new PropertyValueFactory<>("totalViews"));
        moduleTimeColumn.setCellValueFactory(new PropertyValueFactory<>("totalTimeSeconds"));
        moduleStudentsColumn.setCellValueFactory(new PropertyValueFactory<>("activeStudents"));
        moduleTimeColumn.setCellFactory(column -> new DurationTableCell<>());
    }

    private void updateSummaryCards(
            List<AdminCourseStatisticsService.CourseStatsRow> courseRows,
            List<AdminCourseStatisticsService.ModuleStatsRow> moduleRows
    ) {
        totalModulesValueLabel.setText(Integer.toString(moduleRows.size()));
        totalCoursesValueLabel.setText(Integer.toString(courseRows.size()));

        int totalViews = courseRows.stream().mapToInt(AdminCourseStatisticsService.CourseStatsRow::getViewCount).sum();
        long totalTimeSeconds = courseRows.stream().mapToLong(AdminCourseStatisticsService.CourseStatsRow::getTotalTimeSeconds).sum();

        totalViewsValueLabel.setText(Integer.toString(totalViews));
        totalTimeValueLabel.setText(formatDuration(totalTimeSeconds));

        AdminCourseStatisticsService.CourseStatsRow topCourse = courseRows.stream()
                .max(Comparator.comparingInt(AdminCourseStatisticsService.CourseStatsRow::getViewCount)
                        .thenComparingLong(AdminCourseStatisticsService.CourseStatsRow::getTotalTimeSeconds))
                .orElse(null);
        AdminCourseStatisticsService.ModuleStatsRow topModule = moduleRows.stream()
                .max(Comparator.comparingLong(AdminCourseStatisticsService.ModuleStatsRow::getTotalTimeSeconds)
                        .thenComparingInt(AdminCourseStatisticsService.ModuleStatsRow::getTotalViews))
                .orElse(null);

        topCourseValueLabel.setText(topCourse == null
                ? "Aucune vue"
                : topCourse.getCourseTitle() + " (" + topCourse.getViewCount() + " vues)");
        topModuleValueLabel.setText(topModule == null
                ? "Aucune activite"
                : topModule.getModuleTitle() + " (" + formatDuration(topModule.getTotalTimeSeconds()) + ")");
    }

    private void updateCharts(
            List<AdminCourseStatisticsService.CourseStatsRow> courseRows,
            List<AdminCourseStatisticsService.ModuleStatsRow> moduleRows
    ) {
        topCoursesChart.getData().clear();
        topModulesChart.getData().clear();

        XYChart.Series<String, Number> courseSeries = new XYChart.Series<>();
        courseRows.stream()
                .sorted(Comparator.comparingInt(AdminCourseStatisticsService.CourseStatsRow::getViewCount).reversed())
                .limit(5)
                .forEach(row -> courseSeries.getData().add(new XYChart.Data<>(
                        compactLabel(row.getCourseTitle(), 22),
                        row.getViewCount()
                )));
        topCoursesChart.getData().add(courseSeries);

        XYChart.Series<String, Number> moduleSeries = new XYChart.Series<>();
        moduleRows.stream()
                .sorted(Comparator.comparingLong(AdminCourseStatisticsService.ModuleStatsRow::getTotalTimeSeconds).reversed())
                .limit(5)
                .forEach(row -> moduleSeries.getData().add(new XYChart.Data<>(
                        compactLabel(row.getModuleTitle(), 22),
                        row.getTotalTimeSeconds() / 60.0
                )));
        topModulesChart.getData().add(moduleSeries);
    }

    private String compactLabel(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 1)) + "…";
    }

    private static String formatDuration(long totalSeconds) {
        long safeSeconds = Math.max(0, totalSeconds);
        long hours = safeSeconds / 3600;
        long minutes = (safeSeconds % 3600) / 60;

        if (hours <= 0) {
            return minutes + " min";
        }
        if (minutes <= 0) {
            return hours + " h";
        }
        return hours + " h " + minutes + " min";
    }

    private static final class DurationTableCell<T> extends javafx.scene.control.TableCell<T, Long> {
        @Override
        protected void updateItem(Long item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                return;
            }
            setText(formatDuration(item));
        }
    }
}
