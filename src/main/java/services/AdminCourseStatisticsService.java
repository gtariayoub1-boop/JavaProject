package services;

import entities.Cours;
import entities.Module;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AdminCourseStatisticsService {

    private final ModuleService moduleService = new ModuleService();
    private final CoursService coursService = new CoursService();
    private final CoursVueService coursVueService = new CoursVueService();

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    public DashboardData loadDashboardData() throws SQLException {
        List<Module> modules = moduleService.getAll();
        List<Cours> courses = coursService.getAll();

        List<Integer> courseIds = courses.stream()
                .map(Cours::getId)
                .filter(Objects::nonNull)
                .toList();
        if (courseIds.isEmpty()) {
            return new DashboardData(List.of(), List.of());
        }

        Map<Integer, Integer> viewCounts = coursVueService.getViewCountsByCourseIds(courseIds);
        Map<Integer, CourseTimeAggregate> timeAggregates = loadCourseTimeAggregates(courseIds);
        Map<Integer, Integer> contentCounts = loadContentCounts(courseIds);
        Map<Integer, Integer> moduleActiveStudents = loadModuleActiveStudents();

        List<CourseStatsRow> courseRows = new ArrayList<>();
        Map<Integer, List<CourseStatsRow>> moduleCourseRows = new HashMap<>();

        for (Cours cours : courses) {
            if (cours.getId() == null) {
                continue;
            }

            CourseTimeAggregate timeAggregate = timeAggregates.getOrDefault(cours.getId(), CourseTimeAggregate.EMPTY);
            CourseStatsRow row = new CourseStatsRow(
                    cours.getId(),
                    safeValue(cours.getCodeCours(), "-"),
                    safeValue(cours.getTitre(), "Cours"),
                    cours.getModule() != null ? cours.getModule().getId() : null,
                    cours.getModule() != null ? safeValue(cours.getModule().getTitreModule(), "Sans module") : "Sans module",
                    safeValue(cours.getStatut(), "-"),
                    contentCounts.getOrDefault(cours.getId(), 0),
                    viewCounts.getOrDefault(cours.getId(), 0),
                    timeAggregate.totalSeconds(),
                    timeAggregate.activeStudents()
            );
            courseRows.add(row);
            if (row.getModuleId() != null) {
                moduleCourseRows.computeIfAbsent(row.getModuleId(), ignored -> new ArrayList<>()).add(row);
            }
        }

        List<ModuleStatsRow> moduleRows = new ArrayList<>();
        for (Module module : modules) {
            if (module.getId() == null) {
                continue;
            }

            List<CourseStatsRow> rows = moduleCourseRows.getOrDefault(module.getId(), List.of());
            int courseCount = rows.size();
            int contentCount = rows.stream().mapToInt(CourseStatsRow::getContentCount).sum();
            int totalViews = rows.stream().mapToInt(CourseStatsRow::getViewCount).sum();
            long totalTimeSeconds = rows.stream().mapToLong(CourseStatsRow::getTotalTimeSeconds).sum();

            moduleRows.add(new ModuleStatsRow(
                    module.getId(),
                    safeValue(module.getTitreModule(), "Module"),
                    safeValue(module.getStatut(), "-"),
                    courseCount,
                    contentCount,
                    totalViews,
                    totalTimeSeconds,
                    moduleActiveStudents.getOrDefault(module.getId(), 0)
            ));
        }

        courseRows.sort(Comparator
                .comparingInt(CourseStatsRow::getViewCount).reversed()
                .thenComparingLong(CourseStatsRow::getTotalTimeSeconds).reversed()
                .thenComparing(CourseStatsRow::getCourseTitle, String.CASE_INSENSITIVE_ORDER));

        moduleRows.sort(Comparator
                .comparingInt(ModuleStatsRow::getTotalViews).reversed()
                .thenComparingLong(ModuleStatsRow::getTotalTimeSeconds).reversed()
                .thenComparing(ModuleStatsRow::getModuleTitle, String.CASE_INSENSITIVE_ORDER));

        return new DashboardData(courseRows, moduleRows);
    }

    private Map<Integer, CourseTimeAggregate> loadCourseTimeAggregates(List<Integer> courseIds) throws SQLException {
        String sql = "SELECT cours_id, SUM(secondes) AS total_secondes, COUNT(DISTINCT etudiant_id) AS active_students "
                + "FROM cours_temps_passe WHERE cours_id IN (" + placeholders(courseIds.size()) + ") GROUP BY cours_id";
        Map<Integer, CourseTimeAggregate> result = new HashMap<>();

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bindIntegerValues(ps, 1, courseIds);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(
                            rs.getInt("cours_id"),
                            new CourseTimeAggregate(rs.getLong("total_secondes"), rs.getInt("active_students"))
                    );
                }
            }
        }

        return result;
    }

    private Map<Integer, Integer> loadContentCounts(List<Integer> courseIds) throws SQLException {
        String sql = "SELECT cours_id, COUNT(*) AS content_count FROM contenu "
                + "WHERE cours_id IN (" + placeholders(courseIds.size()) + ") GROUP BY cours_id";
        Map<Integer, Integer> result = new HashMap<>();

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bindIntegerValues(ps, 1, courseIds);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("cours_id"), rs.getInt("content_count"));
                }
            }
        }

        return result;
    }

    private Map<Integer, Integer> loadModuleActiveStudents() throws SQLException {
        String sql = "SELECT c.module_id, COUNT(DISTINCT ctp.etudiant_id) AS active_students "
                + "FROM cours_temps_passe ctp "
                + "INNER JOIN cours c ON c.id = ctp.cours_id "
                + "WHERE c.module_id IS NOT NULL "
                + "GROUP BY c.module_id";
        Map<Integer, Integer> result = new HashMap<>();

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getInt("module_id"), rs.getInt("active_students"));
            }
        }

        return result;
    }

    private int bindIntegerValues(PreparedStatement ps, int startIndex, List<Integer> values) throws SQLException {
        int index = startIndex;
        for (Integer value : values) {
            ps.setInt(index++, value);
        }
        return index;
    }

    private String placeholders(int size) {
        return String.join(",", java.util.Collections.nCopies(size, "?"));
    }

    private String safeValue(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    public static final class DashboardData {
        private final List<CourseStatsRow> courseRows;
        private final List<ModuleStatsRow> moduleRows;

        public DashboardData(List<CourseStatsRow> courseRows, List<ModuleStatsRow> moduleRows) {
            this.courseRows = courseRows;
            this.moduleRows = moduleRows;
        }

        public List<CourseStatsRow> getCourseRows() {
            return courseRows;
        }

        public List<ModuleStatsRow> getModuleRows() {
            return moduleRows;
        }
    }

    public static final class CourseStatsRow {
        private final int courseId;
        private final String courseCode;
        private final String courseTitle;
        private final Integer moduleId;
        private final String moduleTitle;
        private final String status;
        private final int contentCount;
        private final int viewCount;
        private final long totalTimeSeconds;
        private final int activeStudents;

        public CourseStatsRow(
                int courseId,
                String courseCode,
                String courseTitle,
                Integer moduleId,
                String moduleTitle,
                String status,
                int contentCount,
                int viewCount,
                long totalTimeSeconds,
                int activeStudents
        ) {
            this.courseId = courseId;
            this.courseCode = courseCode;
            this.courseTitle = courseTitle;
            this.moduleId = moduleId;
            this.moduleTitle = moduleTitle;
            this.status = status;
            this.contentCount = contentCount;
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

        public Integer getModuleId() {
            return moduleId;
        }

        public String getModuleTitle() {
            return moduleTitle;
        }

        public String getStatus() {
            return status;
        }

        public int getContentCount() {
            return contentCount;
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

    public static final class ModuleStatsRow {
        private final int moduleId;
        private final String moduleTitle;
        private final String status;
        private final int courseCount;
        private final int contentCount;
        private final int totalViews;
        private final long totalTimeSeconds;
        private final int activeStudents;

        public ModuleStatsRow(
                int moduleId,
                String moduleTitle,
                String status,
                int courseCount,
                int contentCount,
                int totalViews,
                long totalTimeSeconds,
                int activeStudents
        ) {
            this.moduleId = moduleId;
            this.moduleTitle = moduleTitle;
            this.status = status;
            this.courseCount = courseCount;
            this.contentCount = contentCount;
            this.totalViews = totalViews;
            this.totalTimeSeconds = totalTimeSeconds;
            this.activeStudents = activeStudents;
        }

        public int getModuleId() {
            return moduleId;
        }

        public String getModuleTitle() {
            return moduleTitle;
        }

        public String getStatus() {
            return status;
        }

        public int getCourseCount() {
            return courseCount;
        }

        public int getContentCount() {
            return contentCount;
        }

        public int getTotalViews() {
            return totalViews;
        }

        public long getTotalTimeSeconds() {
            return totalTimeSeconds;
        }

        public int getActiveStudents() {
            return activeStudents;
        }
    }

    private record CourseTimeAggregate(long totalSeconds, int activeStudents) {
        private static final CourseTimeAggregate EMPTY = new CourseTimeAggregate(0, 0);
    }
}
