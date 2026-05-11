package services;

import entities.Etudiant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EtudiantService extends UtilisateurService implements IService<Etudiant> {

    @Override
    public void create(Etudiant etudiant) throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                int id = insertUtilisateurBase(connection, etudiant);
                String sql = "INSERT INTO etudiant (id, matricule, niveau_etude, specialisation, date_naissance, telephone, adresse, date_inscription, statut) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.setString(2, etudiant.getMatricule());
                    ps.setString(3, etudiant.getNiveauEtude());
                    ps.setString(4, etudiant.getSpecialisation());
                    ps.setDate(5, dateOf(etudiant.getDateNaissance()));
                    ps.setString(6, etudiant.getTelephone());
                    ps.setString(7, etudiant.getAdresse());
                    ps.setTimestamp(8, timestampOf(etudiant.getDateInscription()));
                    ps.setString(9, etudiant.getStatut());
                    ps.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public void update(Etudiant etudiant) throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                updateUtilisateurBase(connection, etudiant);
                String sql = "UPDATE etudiant SET matricule = ?, niveau_etude = ?, specialisation = ?, date_naissance = ?, "
                        + "telephone = ?, adresse = ?, date_inscription = ?, statut = ? WHERE id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, etudiant.getMatricule());
                    ps.setString(2, etudiant.getNiveauEtude());
                    ps.setString(3, etudiant.getSpecialisation());
                    ps.setDate(4, dateOf(etudiant.getDateNaissance()));
                    ps.setString(5, etudiant.getTelephone());
                    ps.setString(6, etudiant.getAdresse());
                    ps.setTimestamp(7, timestampOf(etudiant.getDateInscription()));
                    ps.setString(8, etudiant.getStatut());
                    ps.setLong(9, etudiant.getId());
                    ps.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        deleteUtilisateur(id);
    }

    @Override
    public Etudiant getById(int id) throws SQLException {
        return (Etudiant) getUtilisateurById(id);
    }

    @Override
    public List<Etudiant> getAll() throws SQLException {
        List<Etudiant> etudiants = new ArrayList<>();
        for (var utilisateur : getAllUtilisateurs()) {
            if (utilisateur instanceof Etudiant) {
                etudiants.add((Etudiant) utilisateur);
            }
        }
        return etudiants;
    }

    public Etudiant getByMatricule(String matricule) throws SQLException {
        String sql = baseSelectSql() + " WHERE e.matricule = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, matricule);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return (Etudiant) mapUtilisateur(rs);
                }
            }
        }
        return null;
    }
}
