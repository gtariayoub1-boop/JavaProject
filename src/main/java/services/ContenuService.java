package services;

import entities.Contenu;
import entities.Cours;
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

public class ContenuService implements IService<Contenu> {

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public void create(Contenu contenu) throws SQLException {
        String sql = "INSERT INTO contenu (cours_id, type_contenu, titre, url_contenu, description, duree, "
                + "ordre_affichage, est_public, date_ajout, nombre_vues, format, ressources) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, contenu);
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    contenu.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Contenu contenu) throws SQLException {
        requireId(contenu.getId(), "update contenu");
        String sql = "UPDATE contenu SET cours_id = ?, type_contenu = ?, titre = ?, url_contenu = ?, description = ?, "
                + "duree = ?, ordre_affichage = ?, est_public = ?, date_ajout = ?, nombre_vues = ?, format = ?, "
                + "ressources = ? WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            fillStatement(ps, contenu);
            ps.setInt(13, contenu.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM contenu WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Contenu getById(int id) throws SQLException {
        String sql = "SELECT * FROM contenu WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapContenu(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Contenu> getAll() throws SQLException {
        List<Contenu> contenus = new ArrayList<>();
        String sql = "SELECT * FROM contenu ORDER BY ordre_affichage, date_ajout DESC, id DESC";

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                contenus.add(mapContenu(rs));
            }
        }

        return contenus;
    }

    public List<Contenu> getByCoursId(int coursId) throws SQLException {
        List<Contenu> contenus = new ArrayList<>();
        String sql = "SELECT * FROM contenu WHERE cours_id = ? ORDER BY ordre_affichage, date_ajout DESC, id DESC";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, coursId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contenus.add(mapContenu(rs));
                }
            }
        }

        return contenus;
    }

    private void fillStatement(PreparedStatement ps, Contenu contenu) throws SQLException {
        if (contenu.getCours() != null && contenu.getCours().getId() != null) {
            ps.setInt(1, contenu.getCours().getId());
        } else {
            ps.setNull(1, java.sql.Types.INTEGER);
        }

        ps.setString(2, contenu.getTypeContenu());
        ps.setString(3, contenu.getTitre());
        ps.setString(4, contenu.getUrlContenu());
        ps.setString(5, contenu.getDescription());

        if (contenu.getDuree() != null) {
            ps.setInt(6, contenu.getDuree());
        } else {
            ps.setNull(6, java.sql.Types.INTEGER);
        }

        ps.setInt(7, contenu.getOrdreAffichage());
        ps.setBoolean(8, contenu.isEstPublic());
        ps.setTimestamp(9, timestampOf(contenu.getDateAjout()));
        ps.setInt(10, contenu.getNombreVues());
        ps.setString(11, contenu.getFormat());
        ps.setString(12, serializeList(contenu.getRessources()));
    }

    private Contenu mapContenu(ResultSet rs) throws SQLException {
        Contenu contenu = new Contenu();
        contenu.setId(rs.getInt("id"));

        int coursId = rs.getInt("cours_id");
        if (!rs.wasNull()) {
            contenu.setCours(new CoursService().getShallowById(coursId));
        }

        contenu.setTypeContenu(rs.getString("type_contenu"));
        contenu.setTitre(rs.getString("titre"));
        contenu.setUrlContenu(rs.getString("url_contenu"));
        contenu.setDescription(rs.getString("description"));

        int duree = rs.getInt("duree");
        if (!rs.wasNull()) {
            contenu.setDuree(duree);
        }

        contenu.setOrdreAffichage(rs.getInt("ordre_affichage"));
        contenu.setEstPublic(rs.getBoolean("est_public"));

        Timestamp dateAjout = rs.getTimestamp("date_ajout");
        contenu.setDateAjout(dateAjout != null ? dateAjout.toLocalDateTime() : null);

        contenu.setNombreVues(rs.getInt("nombre_vues"));
        contenu.setFormat(rs.getString("format"));
        contenu.setRessources(deserializeList(rs.getString("ressources")));
        return contenu;
    }

    private Timestamp timestampOf(LocalDateTime value) {
        return value != null ? Timestamp.valueOf(value) : null;
    }

    private String serializeList(List<String> values) {
        List<String> normalizedValues = values == null
                ? List.of()
                : values.stream()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
        if (normalizedValues.isEmpty()) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < normalizedValues.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append('"').append(escapeJson(normalizedValues.get(i))).append('"');
        }
        builder.append(']');
        return builder.toString();
    }

    private List<String> deserializeList(String values) {
        if (values == null || values.isBlank()) {
            return new ArrayList<>();
        }

        String trimmed = values.trim();
        if (!trimmed.startsWith("[")) {
            return Arrays.stream(trimmed.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        boolean escaping = false;

        for (int i = 0; i < trimmed.length(); i++) {
            char currentChar = trimmed.charAt(i);
            if (!inString) {
                if (currentChar == '"') {
                    inString = true;
                    current.setLength(0);
                }
                continue;
            }

            if (escaping) {
                switch (currentChar) {
                    case 'n' -> current.append('\n');
                    case 'r' -> current.append('\r');
                    case 't' -> current.append('\t');
                    case '"' -> current.append('"');
                    case '\\' -> current.append('\\');
                    default -> current.append(currentChar);
                }
                escaping = false;
                continue;
            }

            if (currentChar == '\\') {
                escaping = true;
                continue;
            }

            if (currentChar == '"') {
                result.add(current.toString());
                inString = false;
                continue;
            }

            current.append(currentChar);
        }

        return result;
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private void requireId(Integer id, String action) throws SQLException {
        if (id == null) {
            throw new SQLException("Missing contenu id for " + action + ".");
        }
    }
}
