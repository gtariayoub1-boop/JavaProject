package services;

import entities.Notification;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private Connection connection;

    public NotificationService() {
        connection = DBConnection.getInstance().getConnection();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String req = "CREATE TABLE IF NOT EXISTS notification (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "utilisateur_id BIGINT NOT NULL, " +
                "titre VARCHAR(255) NOT NULL, " +
                "contenu TEXT NOT NULL, " +
                "est_lu BOOLEAN DEFAULT FALSE, " +
                "date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Statement st = connection.createStatement()) {
            st.execute(req);
        } catch (SQLException e) {
            System.err.println("Error creating notification table: " + e.getMessage());
        }
    }

    public void ajouter(Notification notification) throws SQLException {
        String req = "INSERT INTO notification (utilisateur_id, titre, contenu) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, notification.getUtilisateurId());
            ps.setString(2, notification.getTitre());
            ps.setString(3, notification.getContenu());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    notification.setId(rs.getLong(1));
                }
            }
        }
    }

    public List<Notification> getNotificationsForUser(long utilisateurId) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String req = "SELECT * FROM notification WHERE utilisateur_id = ? ORDER BY date_creation DESC";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setLong(1, utilisateurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(new Notification(
                            rs.getLong("id"),
                            rs.getLong("utilisateur_id"),
                            rs.getString("titre"),
                            rs.getString("contenu"),
                            rs.getBoolean("est_lu"),
                            rs.getTimestamp("date_creation")
                    ));
                }
            }
        }
        return notifications;
    }

    public int countUnreadNotifications(long utilisateurId) throws SQLException {
        String req = "SELECT COUNT(*) FROM notification WHERE utilisateur_id = ? AND est_lu = FALSE";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setLong(1, utilisateurId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public void markAsRead(long notificationId) throws SQLException {
        String req = "UPDATE notification SET est_lu = TRUE WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setLong(1, notificationId);
            ps.executeUpdate();
        }
    }

    public void markAllAsReadForUser(long utilisateurId) throws SQLException {
        String req = "UPDATE notification SET est_lu = TRUE WHERE utilisateur_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setLong(1, utilisateurId);
            ps.executeUpdate();
        }
    }
}
