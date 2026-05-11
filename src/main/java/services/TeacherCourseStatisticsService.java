package services;

import entities.Cours;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherCourseStatisticsService {

    private final CoursService coursService = new CoursService();
    private final CoursVueService coursVueService = new CoursVueService();

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    public DashboardData loadDashboardData(long teacherId) throws SQLException {
        List<Cours> courses = coursService.getByTeacherId(teacherId);
        return buildDashboardData(courses);
    }

    public List<StudentEngagementRow> loadStudentRankingForCourse(int courseId) throws SQLException {
        return loadStudentRanking(List.of(courseId));
    }

    private DashboardData buildDashboardData(List<Cours> courses) throws SQLException {
        List<Integer> courseIds = courses.stream()
                .map(Cours::getId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (courseIds.isEmpty()) {
            return new DashboardData(List.of(), List.of());
        }

        Map<Integer, Integer> viewCounts = coursVueService.getViewCountsByCourseIds(courseIds);
        Map<Integer, CourseTimeAggregate> timeAggregates = loadCourseTimeAggregates(courseIds);

        List<CourseRankingRow> courseRankings = new ArrayList<>();
        for (Cours cours : courses) {
            if (cours.getId() == null) {
                continue;
            }
            CourseTimeAggregate aggregate = timeAggregates.getOrDefault(cours.getId(), CourseTimeAggregate.EMPTY);
            courseRankings.add(new CourseRankingRow(
                    cours.getId(),
                    safeValue(cours.getCodeCours(), "-"),
                    safeValue(cours.getTitre(), "Cours"),
                    cours.getModule() != null ? safeValue(cours.getModule().getTitreModule(), "-") : "-",
                    viewCounts.getOrDefault(cours.getId(), 0),
                    aggregate.totalSeconds(),
                    aggregate.activeStudents()
            ));
        }

        courseRankings.sort(Comparator
                .comparingInt(CourseRankingRow::getViewCount).reversed()
                .thenComparingLong(CourseRankingRow::getTotalTimeSeconds).reversed()
                .thenComparing(CourseRankingRow::getCourseTitle, String.CASE_INSENSITIVE_ORDER));

        List<StudentEngagementRow> overallStudentRankings = loadStudentRanking(courseIds);
        return new DashboardData(courseRankings, overallStudentRankings);
    }

    private Map<Integer, CourseTimeAggregate> loadCourseTimeAggregates(List<Integer> courseIds) throws SQLException {
        String sql = "SELECT cours_id, SUM(secondes) AS total_secondes, COUNT(DISTINCT etudiant_id) AS active_students "
                + "FROM cours_temps_passe WHERE cours_id IN (" + placeholders(courseIds.size()) + ") GROUP BY cours_id";
        Map<Integer, CourseTimeAggregate> result = new HashMap<>();

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bindIntegerValues(ps, courseIds);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("cours_id"), new CourseTimeAggregate(
                            rs.getLong("total_secondes"),
                            rs.getInt("active_students")
                    ));
                }
            }
        }

        return result;
    }

    private List<StudentEngagementRow> loadStudentRanking(List<Integer> courseIds) throws SQLException {
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }

        String sql = "SELECT tp.etudiant_id, SUM(tp.secondes) AS total_secondes, MAX(tp.updated_at) AS last_activity, "
                + "COUNT(DISTINCT tp.cours_id) AS active_courses, u.nom, u.prenom, COALESCE(e.matricule, '-') AS matricule "
                + "FROM cours_temps_passe tp "
                + "INNER JOIN utilisateur u ON tp.etudiant_id = u.id "
                + "LEFT JOIN etudiant e ON tp.etudiant_id = e.id "
                + "WHERE tp.cours_id IN (" + placeholders(courseIds.size()) + ") "
                + "GROUP BY tp.etudiant_id, u.nom, u.prenom, e.matricule "
                + "ORDER BY total_secondes DESC, last_activity DESC";

        List<StudentEngagementRow> result = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bindIntegerValues(ps, courseIds);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp lastActivity = rs.getTimestamp("last_activity");
                    result.add(new StudentEngagementRow(
                            rs.getLong("etudiant_id"),
                            rs.getString("prenom") + " " + rs.getString("nom"),
                            rs.getString("matricule"),
                            rs.getLong("total_secondes"),
                            lastActivity != null ? lastActivity.toLocalDateTime() : null,
                            rs.getInt("active_courses")
                    ));
                }
            }
        }

        return result;
    }

    private void bindIntegerValues(PreparedStatement ps, List<Integer> values) throws SQLException {
        for (int index = 0; index < values.size(); index++) {
            ps.setInt(index + 1, values.get(index));
        }
    }

    private String placeholders(int size) {
        return String.join(",", Collections.nCopies(size, "?"));
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public static final class DashboardData {
        private final List<CourseRankingRow> courseRankings;
        private final List<StudentEngagementRow> overallStudentRankings;

        public DashboardData(List<CourseRankingRow> courseRankings, List<StudentEngagementRow> overallStudentRankings) {
            this.courseRankings = List.copyOf(courseRankings);
            this.overallStudentRankings = List.copyOf(overallStudentRankings);
        }

        public List<CourseRankingRow> getCourseRankings() {
            return courseRankings;
        }

        public List<StudentEngagementRow> getOverallStudentRankings() {
            return overallStudentRankings;
        }

        public int getTotalViews() {
            return courseRankings.stream().mapToInt(CourseRankingRow::getViewCount).sum();
        }

        public long getTotalTimeSeconds() {
            return courseRankings.stream().mapToLong(CourseRankingRow::getTotalTimeSeconds).sum();
        }

        public CourseRankingRow getMostVisitedCourse() {
            return courseRankings.stream()
                    .max(Comparator.comparingInt(CourseRankingRow::getViewCount)
                            .thenComparingLong(CourseRankingRow::getTotalTimeSeconds))
                    .orElse(null);
        }

        public CourseRankingRow getCourseWithMostTime() {
            return courseRankings.stream()
                    .max(Comparator.comparingLong(CourseRankingRow::getTotalTimeSeconds)
                            .thenComparingInt(CourseRankingRow::getViewCount))
                    .orElse(null);
        }

        public StudentEngagementRow getTopStudent() {
            return overallStudentRankings.stream().findFirst().orElse(null);
        }
    }

    public static final class CourseRankingRow {
        private final int courseId;
        private final String courseCode;
        private final String courseTitle;
        private final String moduleTitle;
        private final int viewCount;
        private final long totalTimeSeconds;
        private final int activeStudents;

        public CourseRankingRow(
                int courseId,
                String courseCode,
                String courseTitle,
                String moduleTitle,
                int viewCount,
                long totalTimeSeconds,
                int activeStudents
        ) {
            this.courseId = courseId;
            this.courseCode = courseCode;
            this.courseTitle = courseTitle;
            this.moduleTitle = moduleTitle;
            this.viewCount = viewCount;
            this.totalTimeSeconds = totalTimeSeconds;
            this.activeStudents = activeStudents;
        }

        public int getCourseId() {
            return courseId;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public String getCourseTitle() {
            return courseTitle;
        }

        public String getModuleTitle() {
            return moduleTitle;
        }

        public int getViewCount() {
            return viewCount;
        }

        public long getTotalTimeSeconds() {
            return totalTimeSeconds;
        }

        public int getActiveStudents() {
            return activeStudents;
        }
    }

    public static final class StudentEngagementRow {
        private final long studentId;
        private final String studentName;
        private final String matricule;
        private final long totalTimeSeconds;
        private final LocalDateTime lastActivity;
        private final int activeCourses;

        public StudentEngagementRow(
                long studentId,
                String studentName,
                String matricule,
                long totalTimeSeconds,
                LocalDateTime lastActivity,
                int activeCourses
        ) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.matricule = matricule;
            this.totalTimeSeconds = totalTimeSeconds;
            this.lastActivity = lastActivity;
            this.activeCourses = activeCourses;
        }

        public long getStudentId() {
            return studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getMatricule() {
            return matricule;
        }

        public long getTotalTimeSeconds() {
            return totalTimeSeconds;
        }

        public LocalDateTime getLastActivity() {
            return lastActivity;
        }

        public int getActiveCourses() {
            return activeCourses;
        }
    }

    private record CourseTimeAggregate(long totalSeconds, int activeStudents) {
        private static final CourseTimeAggregate EMPTY = new CourseTimeAggregate(0, 0);
    }
}
