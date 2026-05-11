package services;

import entities.Quiz;
import entities.ResultatQuiz;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ResultatQuizService implements IService<ResultatQuiz> {

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    // ─── CRUD ─────────────────────────────────────────────────────

    @Override
    public void create(ResultatQuiz resultat) throws SQLException {
        String sql = "INSERT INTO resultat_quiz "
                + "(quiz_id, id_etudiant, date_passation, "
                + " score, total_points, earned_points, reponses_etudiant) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, resultat);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) resultat.setId(keys.getLong(1));
            }
        }
    }

    @Override
    public void update(ResultatQuiz resultat) throws SQLException {
        if (resultat.getId() == null) throw new SQLException("ID manquant pour la mise a jour.");

        String sql = "UPDATE resultat_quiz SET "
                + "quiz_id = ?, id_etudiant = ?, date_passation = ?, "
                + "score = ?, total_points = ?, earned_points = ?, reponses_etudiant = ? "
                + "WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            fillStatement(ps, resultat);
            ps.setLong(8, resultat.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "DELETE FROM resultat_quiz WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public ResultatQuiz getById(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM resultat_quiz WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultat(rs);
            }
        }
        return null;
    }

    @Override
    public List<ResultatQuiz> getAll() throws SQLException {
        List<ResultatQuiz> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM resultat_quiz ORDER BY date_passation DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapResultat(rs));
        }
        return list;
    }

    // ─── Methodes metier ──────────────────────────────────────────

    public List<ResultatQuiz> getByEtudiant(int idEtudiant) throws SQLException {
        List<ResultatQuiz> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM resultat_quiz WHERE id_etudiant = ? ORDER BY date_passation DESC")) {
            ps.setInt(1, idEtudiant);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultat(rs));
            }
        }
        return list;
    }

    public List<ResultatQuiz> getByQuiz(long quizId) throws SQLException {
        List<ResultatQuiz> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM resultat_quiz WHERE quiz_id = ? ORDER BY date_passation DESC")) {
            ps.setLong(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultat(rs));
            }
        }
        return list;
    }

    /**
     * Verifie si un etudiant a deja passe un quiz au moins une fois.
     */
    public boolean hasAlreadyPassed(int idEtudiant, long quizId) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM resultat_quiz WHERE id_etudiant = ? AND quiz_id = ?")) {
            ps.setInt(1, idEtudiant);
            ps.setLong(2, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Calcule la moyenne des scores pour un quiz donne (sur 100).
     * Retourne 0.0 si aucun resultat n'existe.
     */
    public double getMoyenneScore(long quizId) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT AVG(score) FROM resultat_quiz WHERE quiz_id = ?")) {
            ps.setLong(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble(1);
                    return rs.wasNull() ? 0.0 : avg;
                }
            }
        }
        return 0.0;
    }

    /**
     * Retourne le nombre de tentatives d'un etudiant pour un quiz donne.
     */
    public int countTentatives(int idEtudiant, long quizId) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM resultat_quiz WHERE id_etudiant = ? AND quiz_id = ?")) {
            ps.setInt(1, idEtudiant);
            ps.setLong(2, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Retourne le meilleur score d'un etudiant pour un quiz donne.
     * Retourne 0.0 si aucun resultat.
     */
    public double getMeilleurScore(int idEtudiant, long quizId) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT MAX(score) FROM resultat_quiz WHERE id_etudiant = ? AND quiz_id = ?")) {
            ps.setInt(1, idEtudiant);
            ps.setLong(2, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double max = rs.getDouble(1);
                    return rs.wasNull() ? 0.0 : max;
                }
            }
        }
        return 0.0;
    }

    /**
     * Retourne les resultats d'un quiz enrichis avec nom/prenom/matricule de l'etudiant.
     * Retourne une liste de tableaux Object[] :
     *   [0] ResultatQuiz, [1] nom, [2] prenom, [3] matricule
     */
    public List<Object[]> getByQuizWithEtudiant(long quizId) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT rq.*, u.nom, u.prenom, e.matricule " +
                "FROM resultat_quiz rq " +
                "JOIN utilisateur u ON rq.id_etudiant = u.id " +
                "LEFT JOIN etudiant e ON rq.id_etudiant = e.id " +
                "WHERE rq.quiz_id = ? " +
                "ORDER BY rq.score DESC, rq.date_passation ASC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ResultatQuiz r = mapResultat(rs);
                    String nom       = rs.getString("nom");
                    String prenom    = rs.getString("prenom");
                    String matricule = rs.getString("matricule");
                    list.add(new Object[]{r, nom, prenom, matricule});
                }
            }
        }
        return list;
    }

    /**
     * Statistiques agregees pour un quiz.
     * Retourne double[] : [0] moyenne, [1] meilleur, [2] plus bas, [3] nb tentatives
     */
    public double[] getStatsQuiz(long quizId) throws SQLException {
        String sql = "SELECT AVG(score), MAX(score), MIN(score), COUNT(*) " +
                "FROM resultat_quiz WHERE quiz_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new double[]{
                            rs.wasNull() ? 0 : rs.getDouble(1),
                            rs.getDouble(2),
                            rs.getDouble(3),
                            rs.getDouble(4)
                    };
                }
            }
        }
        return new double[]{0, 0, 0, 0};
    }

    /**
     * Statistiques globales pour tous les quiz d'un enseignant.
     * Utilise id_createur (colonne reelle dans la table quiz).
     * Retourne liste de Object[] :
     *   [0] quiz_id (Long)
     *   [1] titre (String)
     *   [2] moyenne_score (Double)
     *   [3] nb_tentatives (Long)
     *   [4] taux_reussite (Double) — % d'etudiants >= 50
     *   [5] meilleur_score (Double)
     *   [6] pire_score (Double)
     */
    public List<Object[]> getStatsGlobalesPourEnseignant(long enseignantId) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql =
                "SELECT q.id, q.titre, " +
                        "       COALESCE(AVG(rq.score), 0)   AS moyenne, " +
                        "       COUNT(rq.id)                  AS nb_tentatives, " +
                        "       COALESCE(SUM(CASE WHEN rq.score >= 50 THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(rq.id), 0), 0) AS taux_reussite, " +
                        "       COALESCE(MAX(rq.score), 0)    AS meilleur, " +
                        "       COALESCE(MIN(rq.score), 0)    AS pire " +
                        "FROM quiz q " +
                        "LEFT JOIN resultat_quiz rq ON q.id = rq.quiz_id " +
                        "WHERE q.id_createur = ? " +
                        "GROUP BY q.id, q.titre " +
                        "ORDER BY moyenne ASC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, enseignantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                            rs.getLong("id"),
                            rs.getString("titre"),
                            rs.getDouble("moyenne"),
                            rs.getLong("nb_tentatives"),
                            rs.getDouble("taux_reussite"),
                            rs.getDouble("meilleur"),
                            rs.getDouble("pire")
                    });
                }
            }
        }
        return list;
    }

    /**
     * Resume global pour un enseignant :
     * [0] total_quiz, [1] total_tentatives, [2] moyenne_generale, [3] taux_reussite_global
     */
    public double[] getResumeGlobalEnseignant(long enseignantId) throws SQLException {
        String sql =
                "SELECT COUNT(DISTINCT q.id), " +
                        "       COUNT(rq.id), " +
                        "       COALESCE(AVG(rq.score), 0), " +
                        "       COALESCE(SUM(CASE WHEN rq.score >= 50 THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(rq.id), 0), 0) " +
                        "FROM quiz q " +
                        "LEFT JOIN resultat_quiz rq ON q.id = rq.quiz_id " +
                        "WHERE q.id_createur = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, enseignantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new double[]{
                            rs.getDouble(1),
                            rs.getDouble(2),
                            rs.getDouble(3),
                            rs.getDouble(4)
                    };
                }
            }
        }
        return new double[]{0, 0, 0, 0};
    }

    // ─── Helpers prives ───────────────────────────────────────────

    private void fillStatement(PreparedStatement ps, ResultatQuiz resultat) throws SQLException {
        if (resultat.getQuiz() != null && resultat.getQuiz().getId() != null)
            ps.setLong(1, resultat.getQuiz().getId());
        else
            throw new SQLException("quiz_id est obligatoire pour un resultat.");

        ps.setInt(2, resultat.getIdEtudiant());
        ps.setTimestamp(3, Timestamp.valueOf(
                resultat.getDatePassation() != null
                        ? resultat.getDatePassation()
                        : LocalDateTime.now()));
        ps.setDouble(4, resultat.getScore());
        ps.setInt(5, resultat.getTotalPoints());
        ps.setInt(6, resultat.getEarnedPoints());
        ps.setString(7, resultat.getReponsesEtudiant());
    }

    private ResultatQuiz mapResultat(ResultSet rs) throws SQLException {
        ResultatQuiz r = new ResultatQuiz();
        r.setId(rs.getLong("id"));
        r.setIdEtudiant(rs.getInt("id_etudiant"));

        Timestamp tp = rs.getTimestamp("date_passation");
        if (tp != null) r.setDatePassation(tp.toLocalDateTime());

        r.setScore(rs.getDouble("score"));
        r.setTotalPoints(rs.getInt("total_points"));
        r.setEarnedPoints(rs.getInt("earned_points"));
        r.setReponsesEtudiant(rs.getString("reponses_etudiant"));

        long quizId = rs.getLong("quiz_id");
        if (!rs.wasNull()) {
            Quiz quiz = new Quiz();
            quiz.setId(quizId);
            r.setQuiz(quiz);
        }

        return r;
    }
}