package services;

import entities.Soumission;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SoumissionService {

    private Connection getConnection() throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) {
            throw new SQLException("Database connection is not available");
        }
        return conn;
    }

    public void add(Soumission soumission) throws SQLException {
        if (soumission.getDateSoumission() == null) {
            soumission.setDateSoumission(LocalDateTime.now());
        }

        String sql = "INSERT INTO soumission (evaluation_id, id_etudiant, fichier_soumission_url, commentaire_etudiant, date_soumission, statut, pdf_filename) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, soumission.getEvaluationId());
            stmt.setString(2, soumission.getIdEtudiant());
            stmt.setString(3, soumission.getFichierSoumissionUrl());
            stmt.setString(4, soumission.getCommentaireEtudiant());
            stmt.setTimestamp(5, Timestamp.valueOf(soumission.getDateSoumission()));
            stmt.setString(6, soumission.getStatut());
            stmt.setString(7, soumission.getPdfFilename());
            stmt.executeUpdate();
        }
    }

    public void update(Soumission soumission) throws SQLException {
        String sql = "UPDATE soumission SET evaluation_id = ?, id_etudiant = ?, fichier_soumission_url = ?, commentaire_etudiant = ?, date_soumission = ?, statut = ?, pdf_filename = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, soumission.getEvaluationId());
            stmt.setString(2, soumission.getIdEtudiant());
            stmt.setString(3, soumission.getFichierSoumissionUrl());
            stmt.setString(4, soumission.getCommentaireEtudiant());
            stmt.setTimestamp(5, Timestamp.valueOf(soumission.getDateSoumission()));
            stmt.setString(6, soumission.getStatut());
            stmt.setString(7, soumission.getPdfFilename());
            stmt.setInt(8, soumission.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(Soumission soumission) throws SQLException {
        delete(soumission.getId());
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM soumission WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Soumission getById(int id) throws SQLException {
        String sql = "SELECT * FROM soumission WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Soumission soumission = new Soumission();
                soumission.setId(rs.getInt("id"));
                soumission.setEvaluationId(rs.getInt("evaluation_id"));
                soumission.setIdEtudiant(rs.getString("id_etudiant"));
                soumission.setFichierSoumissionUrl(rs.getString("fichier_soumission_url"));
                soumission.setCommentaireEtudiant(rs.getString("commentaire_etudiant"));
                soumission.setDateSoumission(rs.getTimestamp("date_soumission").toLocalDateTime());
                soumission.setStatut(rs.getString("statut"));
                soumission.setPdfFilename(rs.getString("pdf_filename"));
                return soumission;
            }
        }
    }

    public List<Soumission> getAll() throws SQLException {
        String sql = "SELECT * FROM soumission";
        List<Soumission> soumissions = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Soumission soumission = new Soumission();
                soumission.setId(rs.getInt("id"));
                soumission.setEvaluationId(rs.getInt("evaluation_id"));
                soumission.setIdEtudiant(rs.getString("id_etudiant"));
                soumission.setFichierSoumissionUrl(rs.getString("fichier_soumission_url"));
                soumission.setCommentaireEtudiant(rs.getString("commentaire_etudiant"));
                soumission.setDateSoumission(rs.getTimestamp("date_soumission").toLocalDateTime());
                soumission.setStatut(rs.getString("statut"));
                soumission.setPdfFilename(rs.getString("pdf_filename"));
                soumissions.add(soumission);
            }
        }
        return soumissions;
    }
}
