package services;

import entities.Quiz;
import entities.Utilisateur;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuizService implements IService<Quiz> {

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    // ─── CRUD ─────────────────────────────────────────────────────

    @Override
    public void create(Quiz quiz) throws SQLException {
        String sql = "INSERT INTO quiz (titre, description, type_quiz, duree_minutes, "
                + "nombre_tentatives_autorisees, instructions, date_creation, "
                + "date_debut_disponibilite, date_fin_disponibilite, "
                + "afficher_correction_apres, id_createur, id_cours) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, quiz);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) quiz.setId(keys.getLong(1));
            }
        }
    }

    @Override
    public void update(Quiz quiz) throws SQLException {
        if (quiz.getId() == null) throw new SQLException("ID manquant pour la mise à jour.");

        String sql = "UPDATE quiz SET titre = ?, description = ?, type_quiz = ?, duree_minutes = ?, "
                + "nombre_tentatives_autorisees = ?, instructions = ?, date_creation = ?, "
                + "date_debut_disponibilite = ?, date_fin_disponibilite = ?, "
                + "afficher_correction_apres = ?, id_createur = ?, id_cours = ? "
                + "WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            fillStatement(ps, quiz);
            ps.setLong(13, quiz.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM quiz WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Quiz getById(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM quiz WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapQuiz(rs);
            }
        }
        return null;
    }

    @Override
    public List<Quiz> getAll() throws SQLException {
        List<Quiz> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM quiz ORDER BY date_creation DESC, id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapQuiz(rs));
        }
        return list;
    }

    // ─── Méthodes métier ──────────────────────────────────────────

    public List<Quiz> getByCreateur(int idCreateur) throws SQLException {
        List<Quiz> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM quiz WHERE id_createur = ? ORDER BY date_creation DESC, id DESC")) {
            ps.setInt(1, idCreateur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapQuiz(rs));
            }
        }
        return list;
    }

    public List<Quiz> getByCours(int idCours) throws SQLException {
        List<Quiz> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM quiz WHERE id_cours = ? ORDER BY date_creation DESC, id DESC")) {
            ps.setInt(1, idCours);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapQuiz(rs));
            }
        }
        return list;
    }

    public List<Quiz> getQuizzesDisponibles() throws SQLException {
        List<Quiz> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz WHERE "
                + "(date_debut_disponibilite IS NULL OR date_debut_disponibilite <= NOW()) AND "
                + "(date_fin_disponibilite  IS NULL OR date_fin_disponibilite  >= NOW()) "
                + "ORDER BY date_creation DESC, id DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapQuiz(rs));
        }
        return list;
    }

    public List<Quiz> getByType(String typeQuiz) throws SQLException {
        List<Quiz> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM quiz WHERE type_quiz = ? ORDER BY date_creation DESC, id DESC")) {
            ps.setString(1, typeQuiz);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapQuiz(rs));
            }
        }
        return list;
    }

    // ─── Helpers privés ───────────────────────────────────────────

    private void fillStatement(PreparedStatement ps, Quiz quiz) throws SQLException {
        ps.setString(1, quiz.getTitre());
        ps.setString(2, quiz.getDescription());
        ps.setString(3, quiz.getTypeQuiz());

        if (quiz.getDureeMinutes() != null)          ps.setInt(4, quiz.getDureeMinutes());
        else                                          ps.setNull(4, Types.INTEGER);

        if (quiz.getNombreTentativesAutorisees() != null) ps.setInt(5, quiz.getNombreTentativesAutorisees());
        else                                              ps.setNull(5, Types.INTEGER);

        ps.setString(6, quiz.getInstructions());

        ps.setTimestamp(7, Timestamp.valueOf(
                quiz.getDateCreation() != null ? quiz.getDateCreation() : LocalDateTime.now()));

        if (quiz.getDateDebutDisponibilite() != null)
            ps.setTimestamp(8, Timestamp.valueOf(quiz.getDateDebutDisponibilite()));
        else ps.setNull(8, Types.TIMESTAMP);

        if (quiz.getDateFinDisponibilite() != null)
            ps.setTimestamp(9, Timestamp.valueOf(quiz.getDateFinDisponibilite()));
        else ps.setNull(9, Types.TIMESTAMP);

        ps.setString(10, quiz.getAfficherCorrectionApres());

        if (quiz.getCreateur() != null && quiz.getCreateur().getId() != null)
            ps.setLong(11, quiz.getCreateur().getId());
        else ps.setNull(11, Types.BIGINT);

        if (quiz.getIdCours() != null) ps.setInt(12, quiz.getIdCours());
        else                           ps.setNull(12, Types.INTEGER);
    }

    private Quiz mapQuiz(ResultSet rs) throws SQLException {
        Quiz quiz = new Quiz();
        quiz.setId(rs.getLong("id"));
        quiz.setTitre(rs.getString("titre"));
        quiz.setDescription(rs.getString("description"));
        quiz.setTypeQuiz(rs.getString("type_quiz"));

        int duree = rs.getInt("duree_minutes");
        if (!rs.wasNull()) quiz.setDureeMinutes(duree);

        int tentatives = rs.getInt("nombre_tentatives_autorisees");
        if (!rs.wasNull()) quiz.setNombreTentativesAutorisees(tentatives);

        quiz.setInstructions(rs.getString("instructions"));

        Timestamp tc = rs.getTimestamp("date_creation");
        if (tc != null) quiz.setDateCreation(tc.toLocalDateTime());

        Timestamp td = rs.getTimestamp("date_debut_disponibilite");
        if (td != null) quiz.setDateDebutDisponibilite(td.toLocalDateTime());

        Timestamp tf = rs.getTimestamp("date_fin_disponibilite");
        if (tf != null) quiz.setDateFinDisponibilite(tf.toLocalDateTime());

        quiz.setAfficherCorrectionApres(rs.getString("afficher_correction_apres"));

        int idCours = rs.getInt("id_cours");
        if (!rs.wasNull()) quiz.setIdCours(idCours);

        // Charger le créateur via UtilisateurService
        long idCreateur = rs.getLong("id_createur");
        if (!rs.wasNull()) {
            try {
                Utilisateur createur = new UtilisateurService().getUtilisateurById((int) idCreateur);
                quiz.setCreateur(createur);
            } catch (Exception ignored) { /* createur reste null */ }
        }

        return quiz;
    }
}