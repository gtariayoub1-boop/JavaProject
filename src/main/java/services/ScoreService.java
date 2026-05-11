package services;

import entities.Score;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScoreService {

    private final GradeApiService gradeApiService = new GradeApiService();

    private Connection getConnection() throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) {
            throw new SQLException("Database connection is not available");
        }
        return conn;
    }

    public void add(Score score) throws SQLException {
        if (score.getDateCorrection() == null) {
            score.setDateCorrection(LocalDateTime.now());
        }

        String sql = "INSERT INTO score (soumission_id, note, note_sur, commentaire_enseignant, date_correction, statut_correction) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, score.getSoumissionId());
            stmt.setDouble(2, score.getNote());
            stmt.setDouble(3, score.getNoteSur());
            stmt.setString(4, score.getCommentaireEnseignant());
            stmt.setTimestamp(5, Timestamp.valueOf(score.getDateCorrection()));
            stmt.setString(6, score.getStatutCorrection());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    score.setId(generatedKeys.getInt(1));
                }
            }
        }

        try {
            gradeApiService.syncGradeCreate(score);
        } catch (IOException e) {
            System.err.println("Failed to sync grade creation to API: " + e.getMessage());
        }
    }

    public void update(Score score) throws SQLException {
        String sql = "UPDATE score SET soumission_id = ?, note = ?, note_sur = ?, commentaire_enseignant = ?, date_correction = ?, statut_correction = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, score.getSoumissionId());
            stmt.setDouble(2, score.getNote());
            stmt.setDouble(3, score.getNoteSur());
            stmt.setString(4, score.getCommentaireEnseignant());
            stmt.setTimestamp(5, Timestamp.valueOf(score.getDateCorrection()));
            stmt.setString(6, score.getStatutCorrection());
            stmt.setInt(7, score.getId());
            stmt.executeUpdate();
        }

        try {
            gradeApiService.syncGradeUpdate(score);
        } catch (IOException e) {
            System.err.println("Failed to sync grade update to API: " + e.getMessage());
        }
    }

    public void delete(Score score) throws SQLException {
        delete(score.getId());
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM score WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }

        try {
            gradeApiService.syncGradeDelete(id);
        } catch (IOException e) {
            System.err.println("Failed to sync grade deletion to API: " + e.getMessage());
        }
    }

    public Score getById(int id) throws SQLException {
        String sql = "SELECT * FROM score WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Score score = new Score();
                score.setId(rs.getInt("id"));
                score.setSoumissionId(rs.getInt("soumission_id"));
                score.setNote(rs.getDouble("note"));
                score.setNoteSur(rs.getDouble("note_sur"));
                score.setCommentaireEnseignant(rs.getString("commentaire_enseignant"));
                score.setDateCorrection(rs.getTimestamp("date_correction").toLocalDateTime());
                score.setStatutCorrection(rs.getString("statut_correction"));
                return score;
            }
        }
    }

    public List<Score> getAll() throws SQLException {
        String sql = "SELECT * FROM score";
        List<Score> scores = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Score score = new Score();
                score.setId(rs.getInt("id"));
                score.setSoumissionId(rs.getInt("soumission_id"));
                score.setNote(rs.getDouble("note"));
                score.setNoteSur(rs.getDouble("note_sur"));
                score.setCommentaireEnseignant(rs.getString("commentaire_enseignant"));
                score.setDateCorrection(rs.getTimestamp("date_correction").toLocalDateTime());
                score.setStatutCorrection(rs.getString("statut_correction"));
                scores.add(score);
            }
        }
        return scores;
    }

    public Score getBySoumissionId(int soumissionId) throws SQLException {
        String sql = "SELECT * FROM score WHERE soumission_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, soumissionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Score score = new Score();
                score.setId(rs.getInt("id"));
                score.setSoumissionId(rs.getInt("soumission_id"));
                score.setNote(rs.getDouble("note"));
                score.setNoteSur(rs.getDouble("note_sur"));
                score.setCommentaireEnseignant(rs.getString("commentaire_enseignant"));
                score.setDateCorrection(rs.getTimestamp("date_correction").toLocalDateTime());
                score.setStatutCorrection(rs.getString("statut_correction"));
                return score;
            }
        }
    }

    /**
     * Get all scores for a specific evaluation by evaluation ID
     */
    public List<Score> getByEvaluationId(int evaluationId) throws SQLException {
        String sql = "SELECT s.* FROM score s " +
                     "JOIN soumission sub ON s.soumission_id = sub.id " +
                     "WHERE sub.evaluation_id = ? " +
                     "ORDER BY s.note DESC";
        List<Score> scores = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, evaluationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Score score = new Score();
                    score.setId(rs.getInt("id"));
                    score.setSoumissionId(rs.getInt("soumission_id"));
                    score.setNote(rs.getDouble("note"));
                    score.setNoteSur(rs.getDouble("note_sur"));
                    score.setCommentaireEnseignant(rs.getString("commentaire_enseignant"));
                    score.setDateCorrection(rs.getTimestamp("date_correction").toLocalDateTime());
                    score.setStatutCorrection(rs.getString("statut_correction"));
                    scores.add(score);
                }
            }
        }
        return scores;
    }

    /**
     * Get statistics for a student's score in an evaluation
     * Returns: [rank, maxNote, minNote, totalStudents]
     */
    public int[] getEvaluationStatistics(int evaluationId, int soumissionId, double studentNote) throws SQLException {
        List<Score> allScores = getByEvaluationId(evaluationId);

        if (allScores.isEmpty()) {
            return new int[]{0, 0, 0, 0};
        }

        double maxNote = allScores.get(0).getNote();
        double minNote = allScores.get(allScores.size() - 1).getNote();

        // Calculate rank (1-based, handling ties)
        int rank = 1;
        for (Score score : allScores) {
            if (score.getNote() > studentNote) {
                rank++;
            }
        }

        return new int[]{rank, (int) maxNote, (int) minNote, allScores.size()};
    }
}
