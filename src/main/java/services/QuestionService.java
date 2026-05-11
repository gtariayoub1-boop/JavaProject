package services;

import entities.Question;
import entities.Quiz;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuestionService implements IService<Question> {

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    // ─── CRUD ─────────────────────────────────────────────────────

    @Override
    public void create(Question question) throws SQLException {
        String sql = "INSERT INTO question "
                + "(texte, type_question, points, ordre_affichage, explication_reponse, "
                + " metadata, date_creation, quiz_id, id_cours, createur_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, question);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) question.setId(keys.getLong(1));
            }
        }
    }

    @Override
    public void update(Question question) throws SQLException {
        if (question.getId() == null) throw new SQLException("ID manquant pour la mise à jour.");

        String sql = "UPDATE question SET "
                + "texte = ?, type_question = ?, points = ?, ordre_affichage = ?, "
                + "explication_reponse = ?, metadata = ?, date_creation = ?, "
                + "quiz_id = ?, id_cours = ?, createur_id = ? "
                + "WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            fillStatement(ps, question);
            ps.setLong(11, question.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        // 1. Supprimer d'abord les réponses liées
        try (PreparedStatement ps = getConnection().prepareStatement(
                "DELETE FROM reponse WHERE question_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }

        // 2. Ensuite supprimer la question
        try (PreparedStatement ps = getConnection().prepareStatement(
                "DELETE FROM question WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
    @Override
    public Question getById(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM question WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapQuestion(rs);
            }
        }
        return null;
    }

    @Override
    public List<Question> getAll() throws SQLException {
        List<Question> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM question ORDER BY quiz_id ASC, ordre_affichage ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapQuestion(rs));
        }
        return list;
    }

    // ─── Méthodes métier ──────────────────────────────────────────

    public List<Question> getByQuiz(long quizId) throws SQLException {
        List<Question> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM question WHERE quiz_id = ? ORDER BY ordre_affichage ASC")) {
            ps.setLong(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapQuestion(rs));
            }
        }
        return list;
    }

    public List<Question> getByType(String typeQuestion) throws SQLException {
        List<Question> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM question WHERE type_question = ? ORDER BY ordre_affichage ASC")) {
            ps.setString(1, typeQuestion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapQuestion(rs));
            }
        }
        return list;
    }

    public List<Question> getByCours(int idCours) throws SQLException {
        List<Question> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM question WHERE id_cours = ? ORDER BY ordre_affichage ASC")) {
            ps.setInt(1, idCours);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapQuestion(rs));
            }
        }
        return list;
    }

    /**
     * Reordonne les questions d'un quiz selon la liste d'IDs fournie.
     * L'index dans la liste determine le nouvel ordreAffichage (commence a 1).
     */
    public void reorderQuestions(long quizId, List<Long> orderedIds) throws SQLException {
        String sql = "UPDATE question SET ordre_affichage = ? WHERE id = ? AND quiz_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < orderedIds.size(); i++) {
                ps.setInt(1, i + 1);
                ps.setLong(2, orderedIds.get(i));
                ps.setLong(3, quizId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ─── Helpers prives ───────────────────────────────────────────

    private void fillStatement(PreparedStatement ps, Question question) throws SQLException {
        ps.setString(1, question.getTexte());
        ps.setString(2, question.getTypeQuestion());

        if (question.getPoints() != null)         ps.setInt(3, question.getPoints());
        else                                       ps.setNull(3, Types.INTEGER);

        if (question.getOrdreAffichage() != null) ps.setInt(4, question.getOrdreAffichage());
        else                                       ps.setNull(4, Types.INTEGER);

        ps.setString(5, question.getExplicationReponse());
        ps.setString(6, question.getMetadata());

        ps.setTimestamp(7, Timestamp.valueOf(
                question.getDateCreation() != null
                        ? question.getDateCreation()
                        : LocalDateTime.now()));

        // quiz_id — NOT NULL dans la DB
        if (question.getQuiz() != null && question.getQuiz().getId() != null)
            ps.setLong(8, question.getQuiz().getId());
        else
            throw new SQLException("quiz_id est obligatoire pour une question.");

        if (question.getIdCours() != null) ps.setInt(9, question.getIdCours());
        else                               ps.setNull(9, Types.INTEGER);

        // createur_id (nom reel de la colonne dans la DB)
        if (question.getCreateur() != null && question.getCreateur().getId() != null)
            ps.setLong(10, question.getCreateur().getId());
        else
            ps.setNull(10, Types.INTEGER);
    }

    private Question mapQuestion(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setId(rs.getLong("id"));
        q.setTexte(rs.getString("texte"));
        q.setTypeQuestion(rs.getString("type_question"));

        int points = rs.getInt("points");
        if (!rs.wasNull()) q.setPoints(points);

        int ordre = rs.getInt("ordre_affichage");
        if (!rs.wasNull()) q.setOrdreAffichage(ordre);

        q.setExplicationReponse(rs.getString("explication_reponse"));
        q.setMetadata(rs.getString("metadata"));

        Timestamp tc = rs.getTimestamp("date_creation");
        if (tc != null) q.setDateCreation(tc.toLocalDateTime());

        int idCours = rs.getInt("id_cours");
        if (!rs.wasNull()) q.setIdCours(idCours);

        // Quiz — objet leger avec juste l'ID
        long quizId = rs.getLong("quiz_id");
        if (!rs.wasNull()) {
            Quiz quiz = new Quiz();
            quiz.setId(quizId);
            q.setQuiz(quiz);
        }

        // Createur — colonne reelle : createur_id
        long createurId = rs.getLong("createur_id");
        if (!rs.wasNull()) {
            try {
                q.setCreateur(new UtilisateurService().getUtilisateurById((int) createurId));
            } catch (Exception ignored) {}
        }

        return q;
    }
}