package services;

import entities.Evaluation;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EvaluationService implements IService<Evaluation> {

    private Connection getConnection() throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) {
            throw new SQLException("Database connection is not available");
        }
        return conn;
    }

    @Override
    public void create(Evaluation evaluation) throws SQLException {
        if (evaluation.getDateCreation() == null) {
            evaluation.setDateCreation(LocalDateTime.now());
        }

        String sql = "INSERT INTO evaluation (titre, type_evaluation, description, cours_id, id_enseignant, date_creation, date_limite, note_max, mode_remise, statut, pdf_filename) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, evaluation.getTitre());
            stmt.setString(2, evaluation.getTypeEvaluation());
            stmt.setString(3, evaluation.getDescription());
            stmt.setInt(4, evaluation.getCoursId());
            stmt.setString(5, evaluation.getIdEnseignant());
            stmt.setTimestamp(6, Timestamp.valueOf(evaluation.getDateCreation()));
            stmt.setTimestamp(7, Timestamp.valueOf(evaluation.getDateLimite()));
            stmt.setDouble(8, evaluation.getNoteMax());
            stmt.setString(9, evaluation.getModeRemise());
            stmt.setString(10, evaluation.getStatut());
            stmt.setString(11, evaluation.getPdfFilename());
            stmt.executeUpdate();
        }
    }

    public void add(Evaluation evaluation) throws SQLException {
        create(evaluation);
    }

    @Override
    public void update(Evaluation evaluation) throws SQLException {
        String sql = "UPDATE evaluation SET titre = ?, type_evaluation = ?, description = ?, cours_id = ?, id_enseignant = ?, date_creation = ?, date_limite = ?, note_max = ?, mode_remise = ?, statut = ?, pdf_filename = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, evaluation.getTitre());
            stmt.setString(2, evaluation.getTypeEvaluation());
            stmt.setString(3, evaluation.getDescription());
            stmt.setInt(4, evaluation.getCoursId());
            stmt.setString(5, evaluation.getIdEnseignant());
            stmt.setTimestamp(6, Timestamp.valueOf(evaluation.getDateCreation()));
            stmt.setTimestamp(7, Timestamp.valueOf(evaluation.getDateLimite()));
            stmt.setDouble(8, evaluation.getNoteMax());
            stmt.setString(9, evaluation.getModeRemise());
            stmt.setString(10, evaluation.getStatut());
            stmt.setString(11, evaluation.getPdfFilename());
            stmt.setInt(12, evaluation.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM evaluation WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void delete(Evaluation evaluation) throws SQLException {
        delete(evaluation.getId());
    }

    @Override
    public Evaluation getById(int id) throws SQLException {
        String sql = "SELECT * FROM evaluation WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Evaluation evaluation = new Evaluation();
                evaluation.setId(rs.getInt("id"));
                evaluation.setTitre(rs.getString("titre"));
                evaluation.setTypeEvaluation(rs.getString("type_evaluation"));
                evaluation.setDescription(rs.getString("description"));
                evaluation.setCoursId(rs.getInt("cours_id"));
                evaluation.setIdEnseignant(rs.getString("id_enseignant"));
                evaluation.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                evaluation.setDateLimite(rs.getTimestamp("date_limite").toLocalDateTime());
                evaluation.setNoteMax(rs.getDouble("note_max"));
                evaluation.setModeRemise(rs.getString("mode_remise"));
                evaluation.setStatut(rs.getString("statut"));
                evaluation.setPdfFilename(rs.getString("pdf_filename"));
                return evaluation;
            }
        }
    }

    @Override
    public List<Evaluation> getAll() throws SQLException {
        String sql = "SELECT * FROM evaluation";
        List<Evaluation> evaluations = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Evaluation evaluation = new Evaluation();
                evaluation.setId(rs.getInt("id"));
                evaluation.setTitre(rs.getString("titre"));
                evaluation.setTypeEvaluation(rs.getString("type_evaluation"));
                evaluation.setDescription(rs.getString("description"));
                evaluation.setCoursId(rs.getInt("cours_id"));
                evaluation.setIdEnseignant(rs.getString("id_enseignant"));
                evaluation.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                evaluation.setDateLimite(rs.getTimestamp("date_limite").toLocalDateTime());
                evaluation.setNoteMax(rs.getDouble("note_max"));
                evaluation.setModeRemise(rs.getString("mode_remise"));
                evaluation.setStatut(rs.getString("statut"));
                evaluation.setPdfFilename(rs.getString("pdf_filename"));
                evaluations.add(evaluation);
            }
        }
        return evaluations;
    }
}
