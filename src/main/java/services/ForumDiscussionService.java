package services;

import entities.ForumDiscussion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ForumDiscussionService {

    private final Connection connection;

    public ForumDiscussionService(Connection connection) {
        this.connection = connection;
    }

    public void ajouter(ForumDiscussion forum) throws SQLException {
        String sql = "INSERT INTO forum_discussion " +
                "(titre, description, id_createur, id_cours, date_creation, type, statut, nombre_vues, derniere_activite, tags, regles_moderation, image_couverture_url, piece_jointe_url, likes, dislikes, signalements, est_modifie, date_modification) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, forum.getTitre());
            ps.setString(2, forum.getDescription());
            ps.setLong(3, forum.getCreateurId());

            if (forum.getIdCours() != null) {
                ps.setInt(4, forum.getIdCours());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setTimestamp(5, Timestamp.valueOf(forum.getDateCreation()));
            ps.setString(6, forum.getType());
            ps.setString(7, forum.getStatut());
            ps.setInt(8, forum.getNombreVues());

            if (forum.getDerniereActivite() != null) {
                ps.setTimestamp(9, Timestamp.valueOf(forum.getDerniereActivite()));
            } else {
                ps.setNull(9, Types.TIMESTAMP);
            }

            ps.setString(10, serializeTags(forum.getTags()));
            ps.setString(11, forum.getReglesModeration());
            ps.setString(12, forum.getImageCouvertureUrl());
            ps.setString(13, forum.getPieceJointeUrl());
            ps.setInt(14, forum.getLikes());
            ps.setInt(15, forum.getDislikes());
            ps.setInt(16, forum.getSignalements());
            ps.setBoolean(17, forum.isEstModifie());

            if (forum.getDateModification() != null) {
                ps.setTimestamp(18, Timestamp.valueOf(forum.getDateModification()));
            } else {
                ps.setNull(18, Types.TIMESTAMP);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    forum.setId(rs.getLong(1));
                }
            }
        }
    }

    public void modifier(ForumDiscussion forum) throws SQLException {
        String sql = "UPDATE forum_discussion SET " +
                "titre=?, description=?, id_createur=?, id_cours=?, date_creation=?, type=?, statut=?, nombre_vues=?, derniere_activite=?, tags=?, regles_moderation=?, image_couverture_url=?, piece_jointe_url=?, likes=?, dislikes=?, signalements=?, est_modifie=?, date_modification=? " +
                "WHERE id_forum=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, forum.getTitre());
            ps.setString(2, forum.getDescription());
            ps.setLong(3, forum.getCreateurId());

            if (forum.getIdCours() != null) {
                ps.setInt(4, forum.getIdCours());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setTimestamp(5, Timestamp.valueOf(forum.getDateCreation()));
            ps.setString(6, forum.getType());
            ps.setString(7, forum.getStatut());
            ps.setInt(8, forum.getNombreVues());

            if (forum.getDerniereActivite() != null) {
                ps.setTimestamp(9, Timestamp.valueOf(forum.getDerniereActivite()));
            } else {
                ps.setNull(9, Types.TIMESTAMP);
            }

            ps.setString(10, serializeTags(forum.getTags()));
            ps.setString(11, forum.getReglesModeration());
            ps.setString(12, forum.getImageCouvertureUrl());
            ps.setString(13, forum.getPieceJointeUrl());
            ps.setInt(14, forum.getLikes());
            ps.setInt(15, forum.getDislikes());
            ps.setInt(16, forum.getSignalements());
            ps.setBoolean(17, forum.isEstModifie());

            if (forum.getDateModification() != null) {
                ps.setTimestamp(18, Timestamp.valueOf(forum.getDateModification()));
            } else {
                ps.setNull(18, Types.TIMESTAMP);
            }

            ps.setLong(19, forum.getId());

            ps.executeUpdate();
        }
    }

    public void supprimer(long id) throws SQLException {
        String sql = "DELETE FROM forum_discussion WHERE id_forum = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public ForumDiscussion getById(long id) throws SQLException {
        String sql = "SELECT * FROM forum_discussion WHERE id_forum = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapForum(rs);
                }
            }
        }
        return null;
    }

    public List<ForumDiscussion> getAll() throws SQLException {
        List<ForumDiscussion> forums = new ArrayList<>();
        String sql = "SELECT * FROM forum_discussion ORDER BY date_creation DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                forums.add(mapForum(rs));
            }
        }
        return forums;
    }

    public List<ForumDiscussion> getByCreateur(long createurId) throws SQLException {
        List<ForumDiscussion> forums = new ArrayList<>();
        String sql = "SELECT * FROM forum_discussion WHERE id_createur = ? ORDER BY date_creation DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, createurId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    forums.add(mapForum(rs));
                }
            }
        }
        return forums;
    }

    public void incrementerVues(long idForum) throws SQLException {
        String sql = "UPDATE forum_discussion SET nombre_vues = nombre_vues + 1, derniere_activite = ? WHERE id_forum = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, idForum);
            ps.executeUpdate();
        }
    }

    public void liker(long idForum) throws SQLException {
        String sql = "UPDATE forum_discussion SET likes = likes + 1, derniere_activite = ? WHERE id_forum = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, idForum);
            ps.executeUpdate();
        }
    }

    public void disliker(long idForum) throws SQLException {
        String sql = "UPDATE forum_discussion SET dislikes = dislikes + 1, derniere_activite = ? WHERE id_forum = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, idForum);
            ps.executeUpdate();
        }
    }

    public void signaler(long idForum) throws SQLException {
        String sql = "UPDATE forum_discussion SET signalements = signalements + 1, derniere_activite = ? WHERE id_forum = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, idForum);
            ps.executeUpdate();
        }
    }

    private ForumDiscussion mapForum(ResultSet rs) throws SQLException {
        ForumDiscussion forum = new ForumDiscussion();

        forum.setId(rs.getLong("id_forum"));
        forum.setTitre(rs.getString("titre"));
        forum.setDescription(rs.getString("description"));
        forum.setCreateurId(rs.getLong("id_createur"));

        int idCours = rs.getInt("id_cours");
        if (!rs.wasNull()) {
            forum.setIdCours(idCours);
        }

        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null) {
            forum.setDateCreation(dateCreation.toLocalDateTime());
        }

        forum.setType(rs.getString("type"));
        forum.setStatut(rs.getString("statut"));
        forum.setNombreVues(rs.getInt("nombre_vues"));

        Timestamp derniereActivite = rs.getTimestamp("derniere_activite");
        if (derniereActivite != null) {
            forum.setDerniereActivite(derniereActivite.toLocalDateTime());
        }

        forum.setTags(deserializeTags(rs.getString("tags")));
        forum.setReglesModeration(rs.getString("regles_moderation"));
        forum.setImageCouvertureUrl(rs.getString("image_couverture_url"));
        forum.setPieceJointeUrl(rs.getString("piece_jointe_url"));
        forum.setLikes(rs.getInt("likes"));
        forum.setDislikes(rs.getInt("dislikes"));
        forum.setSignalements(rs.getInt("signalements"));
        forum.setEstModifie(rs.getBoolean("est_modifie"));

        Timestamp dateModification = rs.getTimestamp("date_modification");
        if (dateModification != null) {
            forum.setDateModification(dateModification.toLocalDateTime());
        }

        return forum;
    }

    private String serializeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "[]";
        }
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < tags.size(); i++) {
            json.append("\"").append(tags.get(i).trim().replace("\"", "\\\"")).append("\"");
            if (i < tags.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    private List<String> deserializeTags(String tags) {
        if (tags == null || tags.trim().isEmpty() || "[]".equals(tags.trim())) {
            return new ArrayList<>();
        }
        tags = tags.trim();
        if (tags.startsWith("[") && tags.endsWith("]")) {
            tags = tags.substring(1, tags.length() - 1);
            if (tags.trim().isEmpty()) return new ArrayList<>();
            return Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .map(t -> t.startsWith("\"") && t.endsWith("\"") ? t.substring(1, t.length() - 1) : t)
                    .collect(Collectors.toList());
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}