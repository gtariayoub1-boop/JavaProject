package services;

import entities.Enseignant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EnseignantService extends UtilisateurService implements IService<Enseignant> {

    @Override
    public void create(Enseignant enseignant) throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                int id = insertUtilisateurBase(connection, enseignant);
                String sql = "INSERT INTO enseignant (id, matricule_enseignant, diplome, specialite, annees_experience, type_contrat, taux_horaire, disponibilites, statut) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.setString(2, enseignant.getMatriculeEnseignant());
                    ps.setString(3, enseignant.getDiplome());
                    ps.setString(4, enseignant.getSpecialite());
                    if (enseignant.getAnneesExperience() != null) {
                        ps.setInt(5, enseignant.getAnneesExperience());
                    } else {
                        ps.setNull(5, java.sql.Types.INTEGER);
                    }
                    ps.setString(6, enseignant.getTypeContrat());
                    ps.setBigDecimal(7, bigDecimalOrNull(enseignant.getTauxHoraire()));
                    ps.setString(8, enseignant.getDisponibilites());
                    ps.setString(9, enseignant.getStatut());
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
    public void update(Enseignant enseignant) throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                updateUtilisateurBase(connection, enseignant);
                String sql = "UPDATE enseignant SET matricule_enseignant = ?, diplome = ?, specialite = ?, annees_experience = ?, "
                        + "type_contrat = ?, taux_horaire = ?, disponibilites = ?, statut = ? WHERE id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, enseignant.getMatriculeEnseignant());
                    ps.setString(2, enseignant.getDiplome());
                    ps.setString(3, enseignant.getSpecialite());
                    if (enseignant.getAnneesExperience() != null) {
                        ps.setInt(4, enseignant.getAnneesExperience());
                    } else {
                        ps.setNull(4, java.sql.Types.INTEGER);
                    }
                    ps.setString(5, enseignant.getTypeContrat());
                    ps.setBigDecimal(6, bigDecimalOrNull(enseignant.getTauxHoraire()));
                    ps.setString(7, enseignant.getDisponibilites());
                    ps.setString(8, enseignant.getStatut());
                    ps.setLong(9, enseignant.getId());
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
    public Enseignant getById(int id) throws SQLException {
        return (Enseignant) getUtilisateurById(id);
    }

    @Override
    public List<Enseignant> getAll() throws SQLException {
        List<Enseignant> enseignants = new ArrayList<>();
        for (var utilisateur : getAllUtilisateurs()) {
            if (utilisateur instanceof Enseignant) {
                enseignants.add((Enseignant) utilisateur);
            }
        }
        return enseignants;
    }
}
