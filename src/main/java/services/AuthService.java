package services;

import entities.Utilisateur;
import jakarta.mail.MessagingException;
import utils.DBConnection;
import utils.AppSecrets;
import utils.UserSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public class AuthService {

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final MailService mailService = new MailService();

    private Connection requireConnection() throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        if (connection == null) {
            throw new SQLException("Database connection is not available");
        }
        return connection;
    }

    public Utilisateur authenticate(String email, String password) throws SQLException {
        Utilisateur utilisateur = utilisateurService.findByEmail(email);
        if (utilisateur == null) {
            return null;
        }

        if (verifyPassword(password, utilisateur.getMotDePasse())) {
            utilisateur.setLastLogin(LocalDateTime.now());
            updateLastLogin(utilisateur);
            UserSession.start(utilisateur);
            return utilisateur;
        }

        return null;
    }

    public boolean requestPasswordReset(String email) throws SQLException, MessagingException {
        Utilisateur utilisateur = utilisateurService.findByEmail(email);
        if (utilisateur == null) {
            return false;
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(AppSecrets.getInt("password.reset.validityMinutes", 30));
        String sql = "UPDATE utilisateur SET reset_token = ?, reset_token_expires_at = ? WHERE id = ?";

        try (Connection connection = requireConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setTimestamp(2, Timestamp.valueOf(expiresAt));
            ps.setLong(3, utilisateur.getId());
            boolean updated = ps.executeUpdate() > 0;
            if (!updated) {
                return false;
            }

            String baseUrl = AppSecrets.get("password.reset.baseUrl");
            String separator = baseUrl.contains("?") ? "&" : "?";
            String resetUrl = baseUrl + separator + "token=" + token;
            mailService.sendPasswordResetEmail(utilisateur.getEmail(), utilisateur.getNomComplet(), resetUrl, expiresAt);
            return true;
        }
    }

    public PasswordResetTokenData validateResetToken(String token) throws SQLException {
        String sql = "SELECT id, email, prenom, nom, reset_token, reset_token_expires_at "
                + "FROM utilisateur WHERE reset_token = ? AND reset_token_expires_at IS NOT NULL";

        try (Connection connection = requireConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Timestamp expiresTimestamp = rs.getTimestamp("reset_token_expires_at");
                LocalDateTime expiresAt = expiresTimestamp != null ? expiresTimestamp.toLocalDateTime() : null;
                if (expiresAt == null || !expiresAt.isAfter(LocalDateTime.now())) {
                    return null;
                }

                return new PasswordResetTokenData(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("prenom") + " " + rs.getString("nom"),
                        rs.getString("reset_token"),
                        expiresAt
                );
            }
        }
    }

    public boolean resetPassword(String token, String newPassword) throws SQLException {
        PasswordResetTokenData tokenData = validateResetToken(token);
        if (tokenData == null) {
            return false;
        }

        String hashedPassword = hashPassword(newPassword);
        String sql = "UPDATE utilisateur SET mot_de_passe = ?, reset_token = NULL, reset_token_expires_at = NULL WHERE id = ?";

        try (Connection connection = requireConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setLong(2, tokenData.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean changePassword(Utilisateur utilisateur, String currentPassword, String newPassword) throws SQLException {
        if (utilisateur == null || utilisateur.getId() == null) {
            return false;
        }
        if (currentPassword == null || newPassword == null) {
            return false;
        }

        String storedHash = fetchPasswordHashById(utilisateur.getId());
        if (storedHash == null || !verifyPassword(currentPassword, storedHash)) {
            return false;
        }

        String hashedPassword = hashPassword(newPassword);
        String sql = "UPDATE utilisateur SET mot_de_passe = ? WHERE id = ?";
        try (Connection connection = requireConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setLong(2, utilisateur.getId());
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                utilisateur.setMotDePasse(hashedPassword);
                Utilisateur sessionUser = UserSession.getCurrentUser();
                if (sessionUser != null && sessionUser.getId() != null && sessionUser.getId().equals(utilisateur.getId())) {
                    sessionUser.setMotDePasse(hashedPassword);
                }
            }
            return updated;
        }
    }

    private void updateLastLogin(Utilisateur utilisateur) throws SQLException {
        String sql = "UPDATE utilisateur SET last_login = ? WHERE id = ?";
        try (Connection connection = requireConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(utilisateur.getLastLogin()));
            ps.setLong(2, utilisateur.getId());
            ps.executeUpdate();
        }
    }

    private String fetchPasswordHashById(Long id) throws SQLException {
        String sql = "SELECT mot_de_passe FROM utilisateur WHERE id = ?";
        try (Connection connection = requireConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }

        // Check if password is BCrypt hashed (starts with $2a$, $2b$, or $2y$)
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedHash);
        }

        // Fallback to plain text comparison for backwards compatibility
        return rawPassword.equals(storedHash);
    }

    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
