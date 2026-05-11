package services;

import entities.Administrateur;
import entities.Enseignant;
import entities.Etudiant;
import entities.Utilisateur;
import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService {

    protected Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    private Connection requireConnection() throws SQLException {
        Connection connection = getConnection();
        if (connection == null) {
            throw new SQLException("Database connection is not available");
        }
        return connection;
    }

    public void createUtilisateur(Utilisateur utilisateur) throws SQLException {
        if (utilisateur instanceof Etudiant) {
            new EtudiantService().create((Etudiant) utilisateur);
            return;
        }
        if (utilisateur instanceof Enseignant) {
            new EnseignantService().create((Enseignant) utilisateur);
            return;
        }
        if (utilisateur instanceof Administrateur) {
            new AdministrateurService().create((Administrateur) utilisateur);
            return;
        }
        throw new SQLException("Unsupported utilisateur type: " + utilisateur.getClass().getSimpleName());
    }

    public void updateUtilisateur(Utilisateur utilisateur) throws SQLException {
        if (utilisateur instanceof Etudiant) {
            new EtudiantService().update((Etudiant) utilisateur);
            return;
        }
        if (utilisateur instanceof Enseignant) {
            new EnseignantService().update((Enseignant) utilisateur);
            return;
        }
        if (utilisateur instanceof Administrateur) {
            new AdministrateurService().update((Administrateur) utilisateur);
            return;
        }
        throw new SQLException("Unsupported utilisateur type: " + utilisateur.getClass().getSimpleName());
    }

    public void deleteUtilisateur(int id) throws SQLException {
        String type = findTypeById(id);
        if (type == null) {
            return;
        }

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                deleteFromSubtypeTable(connection, type, id);
                try (PreparedStatement ps = connection.prepareStatement("DELETE FROM utilisateur WHERE id = ?")) {
                    ps.setInt(1, id);
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

    public Utilisateur getUtilisateurById(int id) throws SQLException {
        String sql = baseSelectSql() + " WHERE u.id = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUtilisateur(rs);
                }
            }
        }
        return null;
    }

    public List<Utilisateur> getAllUtilisateurs() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = baseSelectSql() + " ORDER BY u.id";

        try (PreparedStatement ps = requireConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                utilisateurs.add(mapUtilisateur(rs));
            }
        }

        return utilisateurs;
    }

    public Utilisateur findByEmail(String email) throws SQLException {
        String sql = baseSelectSql() + " WHERE u.email = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUtilisateur(rs);
                }
            }
        }
        return null;
    }

    protected int insertUtilisateurBase(Connection connection, Utilisateur utilisateur) throws SQLException {
        String sql = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, date_creation, reset_token, reset_token_expires_at, last_login, typeUtilisateur) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, utilisateur.getNom());
            ps.setString(2, utilisateur.getPrenom());
            ps.setString(3, utilisateur.getEmail());
            ps.setString(4, utilisateur.getMotDePasse());
            ps.setTimestamp(5, timestampOf(utilisateur.getDateCreation()));
            ps.setString(6, utilisateur.getResetToken());
            ps.setTimestamp(7, timestampOf(utilisateur.getResetTokenExpiresAt()));
            ps.setTimestamp(8, timestampOf(utilisateur.getLastLogin()));
            ps.setString(9, utilisateur.getType());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    utilisateur.setId((long) id);
                    return id;
                }
            }
        }

        throw new SQLException("Unable to create utilisateur base row.");
    }

    protected void updateUtilisateurBase(Connection connection, Utilisateur utilisateur) throws SQLException {
        String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, mot_de_passe = ?, date_creation = ?, "
                + "reset_token = ?, reset_token_expires_at = ?, last_login = ?, typeUtilisateur = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, utilisateur.getNom());
            ps.setString(2, utilisateur.getPrenom());
            ps.setString(3, utilisateur.getEmail());
            ps.setString(4, utilisateur.getMotDePasse());
            ps.setTimestamp(5, timestampOf(utilisateur.getDateCreation()));
            ps.setString(6, utilisateur.getResetToken());
            ps.setTimestamp(7, timestampOf(utilisateur.getResetTokenExpiresAt()));
            ps.setTimestamp(8, timestampOf(utilisateur.getLastLogin()));
            ps.setString(9, utilisateur.getType());
            ps.setLong(10, utilisateur.getId());
            ps.executeUpdate();
        }
    }

    protected Utilisateur mapUtilisateur(ResultSet rs) throws SQLException {
        String type = rs.getString("typeUtilisateur");
        Utilisateur utilisateur;

        if ("etudiant".equalsIgnoreCase(type)) {
            Etudiant etudiant = new Etudiant();
            etudiant.setMatricule(rs.getString("e_matricule"));
            etudiant.setNiveauEtude(rs.getString("e_niveau_etude"));
            etudiant.setSpecialisation(rs.getString("e_specialisation"));
            Date dateNaissance = rs.getDate("e_date_naissance");
            etudiant.setDateNaissance(dateNaissance != null ? dateNaissance.toLocalDate() : null);
            etudiant.setTelephone(rs.getString("e_telephone"));
            etudiant.setAdresse(rs.getString("e_adresse"));
            Timestamp dateInscription = rs.getTimestamp("e_date_inscription");
            etudiant.setDateInscription(dateInscription != null ? dateInscription.toLocalDateTime() : null);
            etudiant.setStatut(rs.getString("e_statut"));
            utilisateur = etudiant;
        } else if ("enseignant".equalsIgnoreCase(type)) {
            Enseignant enseignant = new Enseignant();
            enseignant.setMatriculeEnseignant(rs.getString("ens_matricule_enseignant"));
            enseignant.setDiplome(rs.getString("ens_diplome"));
            enseignant.setSpecialite(rs.getString("ens_specialite"));
            enseignant.setAnneesExperience(rs.getInt("ens_annees_experience"));
            if (rs.wasNull()) {
                enseignant.setAnneesExperience(null);
            }
            enseignant.setTypeContrat(rs.getString("ens_type_contrat"));
            enseignant.setTauxHoraire(rs.getBigDecimal("ens_taux_horaire"));
            enseignant.setDisponibilites(rs.getString("ens_disponibilites"));
            enseignant.setStatut(rs.getString("ens_statut"));
            utilisateur = enseignant;
        } else if ("administrateur".equalsIgnoreCase(type)) {
            Administrateur administrateur = new Administrateur();
            administrateur.setDepartement(rs.getString("a_departement"));
            administrateur.setFonction(rs.getString("a_fonction"));
            administrateur.setTelephone(rs.getString("a_telephone"));
            Timestamp dateNomination = rs.getTimestamp("a_date_nomination");
            administrateur.setDateNomination(dateNomination != null ? dateNomination.toLocalDateTime() : null);
            administrateur.setActif(rs.getBoolean("a_actif"));
            utilisateur = administrateur;
        } else {
            throw new SQLException("Unknown utilisateur type: " + type);
        }

        utilisateur.setId(rs.getLong("id"));
        utilisateur.setNom(rs.getString("nom"));
        utilisateur.setPrenom(rs.getString("prenom"));
        utilisateur.setEmail(rs.getString("email"));
        utilisateur.setMotDePasse(rs.getString("mot_de_passe"));
        Timestamp dateCreation = rs.getTimestamp("date_creation");
        utilisateur.setDateCreation(dateCreation != null ? dateCreation.toLocalDateTime() : null);
        utilisateur.setResetToken(rs.getString("reset_token"));
        Timestamp resetTokenExpiresAt = rs.getTimestamp("reset_token_expires_at");
        utilisateur.setResetTokenExpiresAt(resetTokenExpiresAt != null ? resetTokenExpiresAt.toLocalDateTime() : null);
        Timestamp lastLogin = rs.getTimestamp("last_login");
        utilisateur.setLastLogin(lastLogin != null ? lastLogin.toLocalDateTime() : null);

        return utilisateur;
    }

    protected Timestamp timestampOf(LocalDateTime value) {
        return value != null ? Timestamp.valueOf(value) : null;
    }

    protected Date dateOf(LocalDate value) {
        return value != null ? Date.valueOf(value) : null;
    }

    protected BigDecimal bigDecimalOrNull(BigDecimal value) {
        return value;
    }

    private String findTypeById(int id) throws SQLException {
        String sql = "SELECT typeUtilisateur FROM utilisateur WHERE id = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("typeUtilisateur");
                }
            }
        }
        return null;
    }

    private void deleteFromSubtypeTable(Connection connection, String type, int id) throws SQLException {
        String table;
        if ("etudiant".equalsIgnoreCase(type)) {
            table = "etudiant";
        } else if ("enseignant".equalsIgnoreCase(type)) {
            table = "enseignant";
        } else if ("administrateur".equalsIgnoreCase(type)) {
            table = "administrateur";
        } else {
            throw new SQLException("Unknown utilisateur type: " + type);
        }

        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM " + table + " WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    protected String baseSelectSql() {
        return "SELECT u.*, "
                + "e.matricule AS e_matricule, e.niveau_etude AS e_niveau_etude, e.specialisation AS e_specialisation, "
                + "e.date_naissance AS e_date_naissance, e.telephone AS e_telephone, e.adresse AS e_adresse, "
                + "e.date_inscription AS e_date_inscription, e.statut AS e_statut, "
                + "ens.matricule_enseignant AS ens_matricule_enseignant, ens.diplome AS ens_diplome, "
                + "ens.specialite AS ens_specialite, ens.annees_experience AS ens_annees_experience, "
                + "ens.type_contrat AS ens_type_contrat, ens.taux_horaire AS ens_taux_horaire, "
                + "ens.disponibilites AS ens_disponibilites, ens.statut AS ens_statut, "
                + "a.departement AS a_departement, a.fonction AS a_fonction, a.telephone AS a_telephone, "
                + "a.date_nomination AS a_date_nomination, a.actif AS a_actif "
                + "FROM utilisateur u "
                + "LEFT JOIN etudiant e ON u.id = e.id "
                + "LEFT JOIN enseignant ens ON u.id = ens.id "
                + "LEFT JOIN administrateur a ON u.id = a.id";
    }
}
