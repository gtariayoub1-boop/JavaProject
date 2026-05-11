package services;

import entities.ParticipationEvenement;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationEvenementService implements IService<ParticipationEvenement> {
    private Connection conn;

    public ParticipationEvenementService() {
        this.conn = DBConnection.getInstance().getConnection();
        updateDatabaseSchema();
    }

    private void updateDatabaseSchema() {
        String[] columns = {
            "nom VARCHAR(255)",
            "prenom VARCHAR(255)",
            "telephone VARCHAR(255)",
            "email VARCHAR(255)",
            "annee_scolaire VARCHAR(255)",
            "description_participant TEXT"
        };
        try (Statement stmt = conn.createStatement()) {
            for (String col : columns) {
                try {
                    stmt.execute("ALTER TABLE participation_evenement ADD COLUMN " + col);
                } catch (SQLException e) {
                    // Ignore, column probably already exists
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating schema: " + e.getMessage());
        }
    }

    @Override
    public void create(ParticipationEvenement p) throws SQLException {
        String SQL = "INSERT INTO participation_evenement (evenement_id, utilisateur_id, statut, date_inscription, heure_arrivee, heure_depart, feedback_note, feedback_commentaire, nom, prenom, telephone, email, annee_scolaire, description_participant) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, p.getEvenement_id());
            pstmt.setInt(2, p.getUtilisateur_id());
            pstmt.setString(3, p.getStatut());
            pstmt.setTimestamp(4, p.getDate_inscription() != null ? Timestamp.valueOf(p.getDate_inscription()) : null);
            pstmt.setTimestamp(5, p.getHeure_arrivee() != null ? Timestamp.valueOf(p.getHeure_arrivee()) : null);
            pstmt.setTimestamp(6, p.getHeure_depart() != null ? Timestamp.valueOf(p.getHeure_depart()) : null);
            pstmt.setInt(7, p.getFeedback_note());
            pstmt.setString(8, p.getFeedback_commentaire());
            pstmt.setString(9, p.getNom());
            pstmt.setString(10, p.getPrenom());
            pstmt.setString(11, p.getTelephone());
            pstmt.setString(12, p.getEmail());
            pstmt.setString(13, p.getAnneeScolaire());
            pstmt.setString(14, p.getDescriptionParticipant());
            pstmt.executeUpdate();
            System.out.println("Participation created successfully!");
        } catch (SQLException e) {
            System.err.println("Error creating participation: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(ParticipationEvenement p) throws SQLException {
        String SQL = "UPDATE participation_evenement SET evenement_id = ?, utilisateur_id = ?, statut = ?, date_inscription = ?, heure_arrivee = ?, heure_depart = ?, feedback_note = ?, feedback_commentaire = ?, nom = ?, prenom = ?, telephone = ?, email = ?, annee_scolaire = ?, description_participant = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, p.getEvenement_id());
            pstmt.setInt(2, p.getUtilisateur_id());
            pstmt.setString(3, p.getStatut());
            pstmt.setTimestamp(4, p.getDate_inscription() != null ? Timestamp.valueOf(p.getDate_inscription()) : null);
            pstmt.setTimestamp(5, p.getHeure_arrivee() != null ? Timestamp.valueOf(p.getHeure_arrivee()) : null);
            pstmt.setTimestamp(6, p.getHeure_depart() != null ? Timestamp.valueOf(p.getHeure_depart()) : null);
            pstmt.setInt(7, p.getFeedback_note());
            pstmt.setString(8, p.getFeedback_commentaire());
            pstmt.setString(9, p.getNom());
            pstmt.setString(10, p.getPrenom());
            pstmt.setString(11, p.getTelephone());
            pstmt.setString(12, p.getEmail());
            pstmt.setString(13, p.getAnneeScolaire());
            pstmt.setString(14, p.getDescriptionParticipant());
            pstmt.setInt(15, p.getId());
            pstmt.executeUpdate();
            System.out.println("Participation updated successfully!");
        } catch (SQLException e) {
            System.err.println("Error updating participation: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String SQL = "DELETE FROM participation_evenement WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Participation deleted successfully!");
        } catch (SQLException e) {
            System.err.println("Error deleting participation: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public ParticipationEvenement getById(int id) throws SQLException {
        String SQL = "SELECT p.*, e.titre as event_titre FROM participation_evenement p JOIN evenement e ON p.evenement_id = e.id WHERE p.id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ParticipationEvenement p = new ParticipationEvenement();
                    p.setId(rs.getInt("id"));
                    p.setEvenement_id(rs.getInt("evenement_id"));
                    p.setUtilisateur_id(rs.getInt("utilisateur_id"));
                    p.setStatut(rs.getString("statut"));
                    Timestamp di = rs.getTimestamp("date_inscription");
                    if (di != null) p.setDate_inscription(di.toLocalDateTime());
                    Timestamp ha = rs.getTimestamp("heure_arrivee");
                    if (ha != null) p.setHeure_arrivee(ha.toLocalDateTime());
                    Timestamp hd = rs.getTimestamp("heure_depart");
                    if (hd != null) p.setHeure_depart(hd.toLocalDateTime());
                    p.setFeedback_note(rs.getInt("feedback_note"));
                    p.setFeedback_commentaire(rs.getString("feedback_commentaire"));
                    p.setNom(rs.getString("nom"));
                    p.setPrenom(rs.getString("prenom"));
                    p.setTelephone(rs.getString("telephone"));
                    p.setEmail(rs.getString("email"));
                    p.setAnneeScolaire(rs.getString("annee_scolaire"));
                    p.setDescriptionParticipant(rs.getString("description_participant"));
                    p.setTitreEvenement(rs.getString("event_titre"));
                    return p;
                }
            }
        }
        return null;
    }

    @Override
    public List<ParticipationEvenement> getAll() throws SQLException {
        String SQL = "SELECT p.*, e.titre as event_titre FROM participation_evenement p " +
                     "JOIN evenement e ON p.evenement_id = e.id";
        List<ParticipationEvenement> participations = new ArrayList<>();
        try (Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery(SQL)) {
            while (rs.next()) {
                ParticipationEvenement p = new ParticipationEvenement();
                // ... (existing mapping)
                p.setId(rs.getInt("id"));
                p.setEvenement_id(rs.getInt("evenement_id"));
                p.setUtilisateur_id(rs.getInt("utilisateur_id"));
                p.setStatut(rs.getString("statut"));
                
                Timestamp di = rs.getTimestamp("date_inscription");
                if (di != null) p.setDate_inscription(di.toLocalDateTime());
                
                Timestamp ha = rs.getTimestamp("heure_arrivee");
                if (ha != null) p.setHeure_arrivee(ha.toLocalDateTime());
                
                Timestamp hd = rs.getTimestamp("heure_depart");
                if (hd != null) p.setHeure_depart(hd.toLocalDateTime());
                
                p.setFeedback_note(rs.getInt("feedback_note"));
                p.setFeedback_commentaire(rs.getString("feedback_commentaire"));
                
                // Fetch user details
                p.setNom(rs.getString("nom"));
                p.setPrenom(rs.getString("prenom"));
                p.setTelephone(rs.getString("telephone"));
                p.setEmail(rs.getString("email"));
                p.setAnneeScolaire(rs.getString("annee_scolaire"));
                p.setDescriptionParticipant(rs.getString("description_participant"));
                
                // Fetch event details
                p.setTitreEvenement(rs.getString("event_titre"));
                
                participations.add(p);
            }
        }
 catch (SQLException e) {
            System.err.println("Error reading participations: " + e.getMessage());
            throw e;
        }
        return participations;
    }
    public List<ParticipationEvenement> getParticipationsByEvent(int eventId) throws SQLException {
        String SQL = "SELECT p.*, e.titre as event_titre FROM participation_evenement p " +
                     "JOIN evenement e ON p.evenement_id = e.id " +
                     "WHERE p.evenement_id = ?";
        List<ParticipationEvenement> participations = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, eventId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ParticipationEvenement p = new ParticipationEvenement();
                    p.setId(rs.getInt("id"));
                    p.setEvenement_id(rs.getInt("evenement_id"));
                    p.setUtilisateur_id(rs.getInt("utilisateur_id"));
                    p.setStatut(rs.getString("statut"));

                    Timestamp di = rs.getTimestamp("date_inscription");
                    if (di != null) p.setDate_inscription(di.toLocalDateTime());

                    Timestamp ha = rs.getTimestamp("heure_arrivee");
                    if (ha != null) p.setHeure_arrivee(ha.toLocalDateTime());

                    Timestamp hd = rs.getTimestamp("heure_depart");
                    if (hd != null) p.setHeure_depart(hd.toLocalDateTime());

                    p.setFeedback_note(rs.getInt("feedback_note"));
                    p.setFeedback_commentaire(rs.getString("feedback_commentaire"));

                    p.setNom(rs.getString("nom"));
                    p.setPrenom(rs.getString("prenom"));
                    p.setTelephone(rs.getString("telephone"));
                    p.setEmail(rs.getString("email"));
                    p.setAnneeScolaire(rs.getString("annee_scolaire"));
                    p.setDescriptionParticipant(rs.getString("description_participant"));
                    p.setTitreEvenement(rs.getString("event_titre"));

                    participations.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading participations by event: " + e.getMessage());
            throw e;
        }
        return participations;
    }
    public java.util.Map<String, Integer> getParticipationCountPerEvent() throws SQLException {
        String SQL = "SELECT e.titre, COUNT(p.id) as count FROM evenement e " +
                     "LEFT JOIN participation_evenement p ON e.id = p.evenement_id " +
                     "GROUP BY e.id, e.titre";
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        try (Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery(SQL)) {
            while (rs.next()) {
                stats.put(rs.getString("titre"), rs.getInt("count"));
            }
        }
        return stats;
    }
}
