package services;

import entities.Administrateur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdministrateurService extends UtilisateurService implements IService<Administrateur> {

    @Override
    public void create(Administrateur administrateur) throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                int id = insertUtilisateurBase(connection, administrateur);
                String sql = "INSERT INTO administrateur (id, departement, fonction, telephone, date_nomination, actif) "
                        + "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.setString(2, administrateur.getDepartement());
                    ps.setString(3, administrateur.getFonction());
                    ps.setString(4, administrateur.getTelephone());
                    ps.setTimestamp(5, timestampOf(administrateur.getDateNomination()));
                    ps.setBoolean(6, administrateur.isActif());
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
    public void update(Administrateur administrateur) throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                updateUtilisateurBase(connection, administrateur);
                String sql = "UPDATE administrateur SET departement = ?, fonction = ?, telephone = ?, date_nomination = ?, actif = ? WHERE id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, administrateur.getDepartement());
                    ps.setString(2, administrateur.getFonction());
                    ps.setString(3, administrateur.getTelephone());
                    ps.setTimestamp(4, timestampOf(administrateur.getDateNomination()));
                    ps.setBoolean(5, administrateur.isActif());
                    ps.setLong(6, administrateur.getId());
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
    public Administrateur getById(int id) throws SQLException {
        return (Administrateur) getUtilisateurById(id);
    }

    @Override
    public List<Administrateur> getAll() throws SQLException {
        List<Administrateur> administrateurs = new ArrayList<>();
        for (var utilisateur : getAllUtilisateurs()) {
            if (utilisateur instanceof Administrateur) {
                administrateurs.add((Administrateur) utilisateur);
            }
        }
        return administrateurs;
    }
}
