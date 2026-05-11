package services;

import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoursVueService {

    private Connection getConnection() throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        if (connection == null) {
            throw new SQLException("Database connection is not available");
        }
        return connection;
    }

    public void recordUniqueView(long studentId, int courseId) throws SQLException {
        String selectSql = "SELECT id FROM cours_vue WHERE etudiant_id = ? AND cours_id = ? LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(selectSql)) {
            ps.setLong(1, studentId);
            ps.setInt(2, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        String insertSql = "INSERT INTO cours_vue (date_vue, etudiant_id, cours_id) VALUES (NOW(), ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(insertSql)) {
            ps.setLong(1, studentId);
            ps.setInt(2, courseId);
            ps.executeUpdate();
        }
    }

    public Map<Integer, Integer> getViewCountsByCourseIds(List<Integer> courseIds) throws SQLException {
        if (courseIds == null || courseIds.isEmpty()) {
            return Map.of();
        }

        String sql = "SELECT cours_id, COUNT(DISTINCT etudiant_id) AS vue_count "
                + "FROM cours_vue WHERE cours_id IN (" + placeholders(courseIds.size()) + ") GROUP BY cours_id";
        Map<Integer, Integer> result = new HashMap<>();

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bindIntegerValues(ps, courseIds);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("cours_id"), rs.getInt("vue_count"));
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
        return String.join(",", java.util.Collections.nCopies(size, "?"));
    }
}
