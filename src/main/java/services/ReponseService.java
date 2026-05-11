package services;

import entities.Question;
import entities.Reponse;
import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReponseService implements IService<Reponse> {

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    // ─── CRUD ─────────────────────────────────────────────────────

    @Override
    public void create(Reponse reponse) throws SQLException {
        String sql = "INSERT INTO reponse "
                + "(texte_reponse, est_correcte, ordre_affichage, "
                + " pourcentage_points, feedback_specifique, media_url, question_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, reponse);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) reponse.setId(keys.getLong(1));
            }
        }
    }

    @Override
    public void update(Reponse reponse) throws SQLException {
        if (reponse.getId() == null) throw new SQLException("ID manquant pour la mise a jour.");

        String sql = "UPDATE reponse SET "
                + "texte_reponse = ?, est_correcte = ?, ordre_affichage = ?, "
                + "pourcentage_points = ?, feedback_specifique = ?, media_url = ?, question_id = ? "
                + "WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            fillStatement(ps, reponse);
            ps.setLong(8, reponse.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "DELETE FROM reponse WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Reponse getById(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM reponse WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapReponse(rs);
            }
        }
        return null;
    }

    @Override
    public List<Reponse> getAll() throws SQLException {
        List<Reponse> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM reponse ORDER BY question_id ASC, ordre_affichage ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapReponse(rs));
        }
        return list;
    }

    // ─── Methodes metier ──────────────────────────────────────────

    public List<Reponse> getByQuestion(long questionId) throws SQLException {
        List<Reponse> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM reponse WHERE question_id = ? ORDER BY ordre_affichage ASC")) {
            ps.setLong(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapReponse(rs));
            }
        }
        return list;
    }

    public List<Reponse> getCorrectAnswers(long questionId) throws SQLException {
        List<Reponse> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM reponse WHERE question_id = ? AND est_correcte = 1 "
                        + "ORDER BY ordre_affichage ASC")) {
            ps.setLong(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapReponse(rs));
            }
        }
        return list;
    }

    /**
     * Retourne les reponses d'une question dans un ordre aleatoire.
     * Utile pour eviter que les etudiants memorisent la position des bonnes reponses.
     */
    public List<Reponse> shuffleReponses(long questionId) throws SQLException {
        List<Reponse> reponses = getByQuestion(questionId);
        Collections.shuffle(reponses);
        return reponses;
    }

    // ─── Helpers prives ───────────────────────────────────────────

    private void fillStatement(PreparedStatement ps, Reponse reponse) throws SQLException {
        ps.setString(1, reponse.getTexteReponse());

        if (reponse.getEstCorrecte() != null) ps.setBoolean(2, reponse.getEstCorrecte());
        else                                  ps.setNull(2, Types.TINYINT);

        if (reponse.getOrdreAffichage() != null) ps.setInt(3, reponse.getOrdreAffichage());
        else                                      ps.setNull(3, Types.INTEGER);

        if (reponse.getPourcentagePoints() != null)
            ps.setBigDecimal(4, reponse.getPourcentagePoints());
        else
            ps.setNull(4, Types.DECIMAL);

        ps.setString(5, reponse.getFeedbackSpecifique());
        ps.setString(6, reponse.getMediaUrl());

        // question_id — NOT NULL dans la DB
        if (reponse.getQuestion() != null && reponse.getQuestion().getId() != null)
            ps.setLong(7, reponse.getQuestion().getId());
        else
            throw new SQLException("question_id est obligatoire pour une reponse.");
    }

    private Reponse mapReponse(ResultSet rs) throws SQLException {
        Reponse r = new Reponse();
        r.setId(rs.getLong("id"));
        r.setTexteReponse(rs.getString("texte_reponse"));

        // est_correcte est TINYINT(4) dans la DB
        int estCorrecte = rs.getInt("est_correcte");
        if (!rs.wasNull()) r.setEstCorrecte(estCorrecte == 1);

        int ordre = rs.getInt("ordre_affichage");
        if (!rs.wasNull()) r.setOrdreAffichage(ordre);

        BigDecimal pct = rs.getBigDecimal("pourcentage_points");
        if (pct != null) r.setPourcentagePoints(pct);

        r.setFeedbackSpecifique(rs.getString("feedback_specifique"));
        r.setMediaUrl(rs.getString("media_url"));

        // Question — objet leger avec juste l'ID
        long questionId = rs.getLong("question_id");
        if (!rs.wasNull()) {
            Question question = new Question();
            question.setId(questionId);
            r.setQuestion(question);
        }

        return r;
    }
}