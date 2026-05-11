package gui;

import entities.Cours;
import entities.Enseignant;
import entities.Etudiant;
import entities.Utilisateur;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import services.CoursTempsPasseService;
import services.CoursService;
import utils.UserSession;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class TeacherStudentsController implements MainControllerAware {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.FRENCH);

    @FXML private ComboBox<CourseFilterOption> courseFilterCombo;
    @FXML private TableView<StudentCourseRow> studentsTable;
    @FXML private TableColumn<StudentCourseRow, String> matriculeColumn;
    @FXML private TableColumn<StudentCourseRow, String> nameColumn;
    @FXML private TableColumn<StudentCourseRow, String> emailColumn;
    @FXML private TableColumn<StudentCourseRow, String> levelColumn;
    @FXML private TableColumn<StudentCourseRow, String> specialiteColumn;
    @FXML private TableColumn<StudentCourseRow, String> timeSpentColumn;
    @FXML private TableColumn<StudentCourseRow, String> lastLoginColumn;

    private final CoursService coursService = new CoursService();
    private final CoursTempsPasseService tempsPasseService = new CoursTempsPasseService();
    private final Map<Integer, List<StudentCourseRow>> rowsByCourseId = new HashMap<>();
    private List<StudentCourseRow> allRows = List.of();

    @FXML
    public void initialize() {
        matriculeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMatricule()));
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNomComplet()));
        emailColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail()));
        levelColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNiveauEtude()));
        specialiteColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSpecialisation()));
        timeSpentColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatDuration(cell.getValue().getTimeSpentSeconds())));
        lastLoginColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatDateTime(cell.getValue().getLastLogin())));

        courseFilterCombo.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter(newValue));
        loadStudents();
    }

    private void loadStudents() {
        Utilisateur utilisateur = UserSession.getCurrentUser();
        if (!(utilisateur instanceof Enseignant enseignant) || enseignant.getId() == null) {
            studentsTable.setItems(FXCollections.observableArrayList());
            return;
        }
        try {
            List<Cours> courses = coursService.getByTeacherId(enseignant.getId());
            List<CourseFilterOption> options = new ArrayList<>();
            options.add(new CourseFilterOption(null, "Tous les cours"));
            rowsByCourseId.clear();

            List<Integer> courseIds = new ArrayList<>();
            Map<Long, Etudiant> uniqueStudents = new HashMap<>();
            List<Etudiant> studentsWithoutId = new ArrayList<>();

            for (Cours cours : courses) {
                if (cours.getId() == null) {
                    continue;
                }
                courseIds.add(cours.getId());
                List<Etudiant> students = coursService.getStudentsByCoursId(cours.getId());
                rowsByCourseId.put(cours.getId(), buildRowsForSingleCourse(cours.getId(), students));
                options.add(new CourseFilterOption(cours.getId(), courseLabel(cours)));

                for (Etudiant etudiant : students) {
                    if (etudiant.getId() != null) {
                        uniqueStudents.putIfAbsent(etudiant.getId(), etudiant);
                    } else {
                        studentsWithoutId.add(etudiant);
                    }
                }
            }

            List<Etudiant> allStudents = new ArrayList<>(uniqueStudents.values());
            allStudents.addAll(studentsWithoutId);
            allRows = buildRowsForAllCourses(courseIds, allStudents);

            courseFilterCombo.setItems(FXCollections.observableArrayList(options));
            if (!options.isEmpty()) {
                courseFilterCombo.setValue(options.get(0));
                applyFilter(options.get(0));
            } else {
                studentsTable.setItems(FXCollections.observableArrayList());
            }
        } catch (SQLException e) {
            studentsTable.setItems(FXCollections.observableArrayList());
        }
    }

    private List<StudentCourseRow> buildRowsForSingleCourse(int courseId, List<Etudiant> students) {
        Map<Long, CoursTempsPasseService.TimeSpentSnapshot> snapshots = Map.of();
        try {
            snapshots = tempsPasseService.getTimeByCourseAndStudentIds(courseId, extractStudentIds(students));
        } catch (SQLException ignored) {
        }
        return buildRows(students, snapshots);
    }

    private List<StudentCourseRow> buildRowsForAllCourses(List<Integer> courseIds, List<Etudiant> students) {
        Map<Long, CoursTempsPasseService.TimeSpentSnapshot> snapshots = Map.of();
        try {
            snapshots = tempsPasseService.getAggregateTimeByStudentIds(courseIds, extractStudentIds(students));
        } catch (SQLException ignored) {
        }
        return buildRows(students, snapshots);
    }

    private List<StudentCourseRow> buildRows(
            List<Etudiant> students,
            Map<Long, CoursTempsPasseService.TimeSpentSnapshot> snapshots
    ) {
        List<StudentCourseRow> rows = new ArrayList<>();
        for (Etudiant etudiant : students) {
            CoursTempsPasseService.TimeSpentSnapshot snapshot = etudiant.getId() != null
                    ? snapshots.getOrDefault(etudiant.getId(), tempsPasseService.emptySnapshot())
                    : tempsPasseService.emptySnapshot();
            rows.add(new StudentCourseRow(etudiant, snapshot.getSeconds()));
        }
        rows.sort(Comparator.comparing(StudentCourseRow::getNomComplet, String.CASE_INSENSITIVE_ORDER));
        return rows;
    }

    private List<Long> extractStudentIds(List<Etudiant> students) {
        List<Long> studentIds = new ArrayList<>();
        for (Etudiant etudiant : students) {
            if (etudiant.getId() != null) {
                studentIds.add(etudiant.getId());
            }
        }
        return studentIds;
    }

    private void applyFilter(CourseFilterOption option) {
        if (option == null || option.courseId() == null) {
            studentsTable.setItems(FXCollections.observableArrayList(allRows));
            return;
        }
        studentsTable.setItems(FXCollections.observableArrayList(rowsByCourseId.getOrDefault(option.courseId(), List.of())));
    }

    private String courseLabel(Cours cours) {
        String titre = cours.getTitre();
        if (titre != null && !titre.isBlank()) {
            return titre;
        }
        String code = cours.getCodeCours();
        return code != null && !code.isBlank() ? code : "Cours";
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
        return value != null ? DATE_TIME_FORMATTER.format(value) : "Jamais";
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

    private static final class StudentCourseRow {
        private final Etudiant etudiant;
        private final long timeSpentSeconds;

        private StudentCourseRow(Etudiant etudiant, long timeSpentSeconds) {
            this.etudiant = etudiant;
            this.timeSpentSeconds = Math.max(timeSpentSeconds, 0);
        }

        public String getMatricule() {
            return safeValue(etudiant.getMatricule());
        }

        public String getNomComplet() {
            return safeValue(etudiant.getNomComplet());
        }

        public String getEmail() {
            return safeValue(etudiant.getEmail());
        }

        public String getNiveauEtude() {
            return safeValue(etudiant.getNiveauEtude());
        }

        public String getSpecialisation() {
            return safeValue(etudiant.getSpecialisation());
        }

        public long getTimeSpentSeconds() {
            return timeSpentSeconds;
        }

        public LocalDateTime getLastLogin() {
            return etudiant.getLastLogin();
        }

        private static String safeValue(String value) {
            return value != null && !value.isBlank() ? value : "-";
        }
    }
}
