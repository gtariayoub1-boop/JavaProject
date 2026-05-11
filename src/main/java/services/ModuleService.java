package services;

import entities.Module;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleService implements IService<Module> {

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public void create(Module module) throws SQLException {
        String sql = "INSERT INTO module (titre_module, description, ordre_affichage, objectifs_apprentissage, "
                + "duree_estimee_heures, date_publication, statut, ressources_complementaires) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, module);
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    module.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Module module) throws SQLException {
        requireId(module.getId(), "update module");
        String sql = "UPDATE module SET titre_module = ?, description = ?, ordre_affichage = ?, "
                + "objectifs_apprentissage = ?, duree_estimee_heures = ?, date_publication = ?, statut = ?, "
                + "ressources_complementaires = ? WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            fillStatement(ps, module);
            ps.setInt(9, module.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        CoursService coursService = new CoursService();
        for (var cours : coursService.getByModuleId(id)) {
            coursService.delete(cours.getId());
        }
        String sql = "DELETE FROM module WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Module getById(int id) throws SQLException {
        String sql = "SELECT * FROM module WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapModule(rs, true);
                }
            }
        }
        return null;
    }

    @Override
    public List<Module> getAll() throws SQLException {
        List<Module> modules = new ArrayList<>();
        String sql = "SELECT * FROM module ORDER BY ordre_affichage, id";

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modules.add(mapModule(rs, false));
            }
        }

        return modules;
    }

    private void fillStatement(PreparedStatement ps, Module module) throws SQLException {
        ps.setString(1, module.getTitreModule());
        ps.setString(2, module.getDescription());
        ps.setInt(3, module.getOrdreAffichage());
        ps.setString(4, module.getObjectifsApprentissage());
        if (module.getDureeEstimeeHeures() != null) {
            ps.setInt(5, module.getDureeEstimeeHeures());
        } else {
            ps.setNull(5, java.sql.Types.INTEGER);
        }
        ps.setTimestamp(6, timestampOf(module.getDatePublication()));
        ps.setString(7, module.getStatut());
        ps.setString(8, serializeList(module.getRessourcesComplementaires()));
    }

    Module mapModule(ResultSet rs, boolean loadRelations) throws SQLException {
        Module module = new Module();
        module.setId(rs.getInt("id"));
        module.setTitreModule(rs.getString("titre_module"));
        module.setDescription(rs.getString("description"));
        module.setOrdreAffichage(rs.getInt("ordre_affichage"));
        module.setObjectifsApprentissage(rs.getString("objectifs_apprentissage"));

        int dureeEstimeeHeures = rs.getInt("duree_estimee_heures");
        if (!rs.wasNull()) {
            module.setDureeEstimeeHeures(dureeEstimeeHeures);
        }

        Timestamp datePublication = rs.getTimestamp("date_publication");
        module.setDatePublication(datePublication != null ? datePublication.toLocalDateTime() : null);
        module.setStatut(rs.getString("statut"));
        module.setRessourcesComplementaires(deserializeList(rs.getString("ressources_complementaires")));
        if (loadRelations) {
            CoursService coursService = new CoursService();
            module.setCours(coursService.getByModuleId(module.getId()));
        }
        return module;
    }

    Module getShallowById(int id) throws SQLException {
        String sql = "SELECT * FROM module WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapModule(rs, false);
                }
            }
        }
        return null;
    }

    private Timestamp timestampOf(LocalDateTime value) {
        return value != null ? Timestamp.valueOf(value) : null;
    }

    private String serializeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.joining(","));
    }

    private List<String> deserializeList(String values) {
        if (values == null || values.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(values.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void requireId(Integer id, String action) throws SQLException {
        if (id == null) {
            throw new SQLException("Missing module id for " + action + ".");
        }
    }
}
