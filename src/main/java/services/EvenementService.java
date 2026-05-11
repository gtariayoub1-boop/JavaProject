package services;

import entities.Evenement;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService implements IService<Evenement> {
    private Connection conn;

    public EvenementService() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    @Override
    public void create(Evenement evenement) throws SQLException {
        String SQL = "INSERT INTO evenement (createur_id, titre, description, type_evenement, date_debut, date_fin, lieu, capacite_max, statut, visibilite) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, evenement.getCreateur_id());
            pstmt.setString(2, evenement.getTitre());
            pstmt.setString(3, evenement.getDescription());
            pstmt.setString(4, evenement.getType_evenement());
            pstmt.setTimestamp(5, Timestamp.valueOf(evenement.getDate_debut()));
            pstmt.setTimestamp(6, Timestamp.valueOf(evenement.getDate_fin()));
            pstmt.setString(7, evenement.getLieu());
            pstmt.setInt(8, evenement.getCapacite_max());
            pstmt.setString(9, evenement.getStatut());
            pstmt.setString(10, evenement.getVisibilite());
            pstmt.executeUpdate();
            System.out.println("Evenement created successfully!");
        } catch (SQLException e) {
            System.err.println("Error creating evenement: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(Evenement evenement) throws SQLException {
        String SQL = "UPDATE evenement SET createur_id = ?, titre = ?, description = ?, type_evenement = ?, date_debut = ?, date_fin = ?, lieu = ?, capacite_max = ?, statut = ?, visibilite = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, evenement.getCreateur_id());
            pstmt.setString(2, evenement.getTitre());
            pstmt.setString(3, evenement.getDescription());
            pstmt.setString(4, evenement.getType_evenement());
            pstmt.setTimestamp(5, Timestamp.valueOf(evenement.getDate_debut()));
            pstmt.setTimestamp(6, Timestamp.valueOf(evenement.getDate_fin()));
            pstmt.setString(7, evenement.getLieu());
            pstmt.setInt(8, evenement.getCapacite_max());
            pstmt.setString(9, evenement.getStatut());
            pstmt.setString(10, evenement.getVisibilite());
            pstmt.setInt(11, evenement.getId());
            pstmt.executeUpdate();
            System.out.println("Evenement updated successfully!");
        } catch (SQLException e) {
            System.err.println("Error updating evenement: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String SQL = "DELETE FROM evenement WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Evenement deleted successfully!");
        } catch (SQLException e) {
            System.err.println("Error deleting evenement: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Evenement getById(int id) throws SQLException {
        String SQL = "SELECT * FROM evenement WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Evenement ev = new Evenement();
                    ev.setId(rs.getInt("id"));
                    ev.setCreateur_id(rs.getInt("createur_id"));
                    ev.setTitre(rs.getString("titre"));
                    ev.setDescription(rs.getString("description"));
                    ev.setType_evenement(rs.getString("type_evenement"));
                    ev.setDate_debut(rs.getTimestamp("date_debut").toLocalDateTime());
                    ev.setDate_fin(rs.getTimestamp("date_fin").toLocalDateTime());
                    ev.setLieu(rs.getString("lieu"));
                    ev.setCapacite_max(rs.getInt("capacite_max"));
                    ev.setStatut(rs.getString("statut"));
                    ev.setVisibilite(rs.getString("visibilite"));
                    return ev;
                }
            }
        }
        return null;
    }

    @Override
    public List<Evenement> getAll() throws SQLException {
        String SQL = "SELECT * FROM evenement";
        List<Evenement> evenements = new ArrayList<>();
        try (Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery(SQL)) {
            while (rs.next()) {
                Evenement ev = new Evenement();
                ev.setId(rs.getInt("id"));
                ev.setCreateur_id(rs.getInt("createur_id"));
                ev.setTitre(rs.getString("titre"));
                ev.setDescription(rs.getString("description"));
                ev.setType_evenement(rs.getString("type_evenement"));
                ev.setDate_debut(rs.getTimestamp("date_debut").toLocalDateTime());
                ev.setDate_fin(rs.getTimestamp("date_fin").toLocalDateTime());
                ev.setLieu(rs.getString("lieu"));
                ev.setCapacite_max(rs.getInt("capacite_max"));
                ev.setStatut(rs.getString("statut"));
                ev.setVisibilite(rs.getString("visibilite"));
                evenements.add(ev);
            }
        } catch (SQLException e) {
            System.err.println("Error reading evenements: " + e.getMessage());
            throw e;
        }
        return evenements;
    }
    public java.util.Map<String, Integer> getEventCountByType() throws SQLException {
        String SQL = "SELECT type_evenement, COUNT(*) as count FROM evenement GROUP BY type_evenement";
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        try (Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery(SQL)) {
            while (rs.next()) {
                stats.put(rs.getString("type_evenement"), rs.getInt("count"));
            }
        }
        return stats;
    }
}
