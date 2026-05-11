package services;

import entities.SignalementSpam;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SignalementSpamService {
    private Connection connection;

    public SignalementSpamService() {
        connection = DBConnection.getInstance().getConnection();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String req = "CREATE TABLE IF NOT EXISTS signalement_spam (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "expediteur_id BIGINT NOT NULL, " +
                "contenu_message TEXT NOT NULL, " +
                "date_signalement TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "est_traite BOOLEAN DEFAULT FALSE" +
                ")";
        try (Statement st = connection.createStatement()) {
            st.execute(req);
        } catch (SQLException e) {
            System.err.println("Error creating signalement_spam table: " + e.getMessage());
        }
    }

    public void ajouter(SignalementSpam signalement) throws SQLException {
        String req = "INSERT INTO signalement_spam (expediteur_id, contenu_message, est_traite) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, signalement.getExpediteurId());
            ps.setString(2, signalement.getContenuMessage());
            ps.setBoolean(3, signalement.isEstTraite());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    signalement.setId(rs.getLong(1));
                }
            }
        }
    }

    public List<SignalementSpam> getAllNonTraites() throws SQLException {
        List<SignalementSpam> liste = new ArrayList<>();
        String req = "SELECT * FROM signalement_spam WHERE est_traite = FALSE ORDER BY date_signalement DESC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                liste.add(new SignalementSpam(
                        rs.getLong("id"),
                        rs.getLong("expediteur_id"),
                        rs.getString("contenu_message"),
                        rs.getTimestamp("date_signalement").toLocalDateTime(),
                        rs.getBoolean("est_traite")
                ));
            }
        }
        return liste;
    }
}
