package services;

import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoursTempsPasseService {

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    public void addElapsedSeconds(long studentId, int courseId, long elapsedSeconds) throws SQLException {
        if (elapsedSeconds <= 0) {
            return;
        }

        String selectSql = "SELECT id FROM cours_temps_passe WHERE etudiant_id = ? AND cours_id = ? LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(selectSql)) {
            ps.setLong(1, studentId);
            ps.setInt(2, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    updateElapsedSeconds(rs.getInt("id"), elapsedSeconds);
                    return;
                }
            }
        }

        insertElapsedSeconds(studentId, courseId, elapsedSeconds);
    }

    public Map<Long, TimeSpentSnapshot> getTimeByCourseAndStudentIds(int courseId, List<Long> studentIds) throws SQLException {
        if (studentIds == null || studentIds.isEmpty()) {
            return Map.of();
        }

        String sql = "SELECT etudiant_id, secondes, updated_at FROM cours_temps_passe WHERE cours_id = ? AND etudiant_id IN ("
                + placeholders(studentIds.size()) + ")";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            bindLongValues(ps, 2, studentIds);
            return extractSnapshots(ps, "secondes");
        }
    }

    public Map<Long, TimeSpentSnapshot> getAggregateTimeByStudentIds(List<Integer> courseIds, List<Long> studentIds) throws SQLException {
        if (courseIds == null || courseIds.isEmpty() || studentIds == null || studentIds.isEmpty()) {
            return Map.of();
        }

        String sql = "SELECT etudiant_id, SUM(secondes) AS total_secondes, MAX(updated_at) AS updated_at "
                + "FROM cours_temps_passe WHERE cours_id IN (" + placeholders(courseIds.size()) + ") "
                + "AND etudiant_id IN (" + placeholders(studentIds.size()) + ") GROUP BY etudiant_id";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            int index = bindIntegerValues(ps, 1, courseIds);
            bindLongValues(ps, index, studentIds);
            return extractSnapshots(ps, "total_secondes");
        }
    }

    public TimeSpentSnapshot emptySnapshot() {
        return new TimeSpentSnapshot(0, null);
    }

    private void updateElapsedSeconds(int rowId, long elapsedSeconds) throws SQLException {
        String sql = "UPDATE cours_temps_passe SET secondes = secondes + ?, updated_at = NOW() WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, elapsedSeconds);
            ps.setInt(2, rowId);
            ps.executeUpdate();
        }
    }

    private void insertElapsedSeconds(long studentId, int courseId, long elapsedSeconds) throws SQLException {
        String sql = "INSERT INTO cours_temps_passe (secondes, updated_at, etudiant_id, cours_id) VALUES (?, NOW(), ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, elapsedSeconds);
            ps.setLong(2, studentId);
            ps.setInt(3, courseId);
            ps.executeUpdate();
        }
    }

    private Map<Long, TimeSpentSnapshot> extractSnapshots(PreparedStatement ps, String secondsColumn) throws SQLException {
        Map<Long, TimeSpentSnapshot> result = new HashMap<>();

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long studentId = rs.getLong("etudiant_id");
                long seconds = rs.getLong(secondsColumn);
                Timestamp updatedAt = rs.getTimestamp("updated_at");
                result.put(studentId, new TimeSpentSnapshot(
                        seconds,
                        updatedAt != null ? updatedAt.toLocalDateTime() : null
                ));
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

    private void bindLongValues(PreparedStatement ps, int startIndex, List<Long> values) throws SQLException {
        int index = startIndex;
        for (Long value : values) {
            ps.setLong(index++, value);
        }
    }

    private String placeholders(int size) {
        return String.join(",", java.util.Collections.nCopies(size, "?"));
    }

    public static final class TimeSpentSnapshot {
        private final long seconds;
        private final LocalDateTime updatedAt;

        public TimeSpentSnapshot(long seconds, LocalDateTime updatedAt) {
            this.seconds = Math.max(seconds, 0);
            this.updatedAt = updatedAt;
        }

        public long getSeconds() {
            return seconds;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }
    }
}
