package services;

import entities.Message;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageService {

    private final Connection connection;

    public MessageService(Connection connection) {
        this.connection = connection;
    }

    public void ajouter(Message message) throws SQLException {
        String sql = "INSERT INTO message " +
                "(expediteur_id, destinataire_id, objet, contenu, date_envoi, date_lecture, statut, priorite, piece_jointe_url, parent_id, categorie, est_archive_expediteur, est_archive_destinataire, est_supprime_expediteur, est_supprime_destinataire) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, message.getExpediteurId());
            ps.setLong(2, message.getDestinataireId());
            ps.setString(3, message.getObjet());
            ps.setString(4, message.getContenu());
            ps.setTimestamp(5, Timestamp.valueOf(message.getDateEnvoi()));

            if (message.getDateLecture() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(message.getDateLecture()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }

            ps.setString(7, message.getStatut());
            ps.setString(8, message.getPriorite());
            ps.setString(9, message.getPieceJointeUrl());

            if (message.getParentId() != null) {
                ps.setLong(10, message.getParentId());
            } else {
                ps.setNull(10, Types.BIGINT);
            }

            ps.setString(11, message.getCategorie());
            ps.setBoolean(12, message.isEstArchiveExpediteur());
            ps.setBoolean(13, message.isEstArchiveDestinataire());
            ps.setBoolean(14, message.isEstSupprimeExpediteur());
            ps.setBoolean(15, message.isEstSupprimeDestinataire());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    message.setId(rs.getLong(1));
                }
            }
        }
    }

    public void modifier(Message message) throws SQLException {
        String sql = "UPDATE message SET " +
                "expediteur_id=?, destinataire_id=?, objet=?, contenu=?, date_envoi=?, date_lecture=?, statut=?, priorite=?, piece_jointe_url=?, parent_id=?, categorie=?, est_archive_expediteur=?, est_archive_destinataire=?, est_supprime_expediteur=?, est_supprime_destinataire=? " +
                "WHERE id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, message.getExpediteurId());
            ps.setLong(2, message.getDestinataireId());
            ps.setString(3, message.getObjet());
            ps.setString(4, message.getContenu());
            ps.setTimestamp(5, Timestamp.valueOf(message.getDateEnvoi()));

            if (message.getDateLecture() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(message.getDateLecture()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }

            ps.setString(7, message.getStatut());
            ps.setString(8, message.getPriorite());
            ps.setString(9, message.getPieceJointeUrl());

            if (message.getParentId() != null) {
                ps.setLong(10, message.getParentId());
            } else {
                ps.setNull(10, Types.BIGINT);
            }

            ps.setString(11, message.getCategorie());
            ps.setBoolean(12, message.isEstArchiveExpediteur());
            ps.setBoolean(13, message.isEstArchiveDestinataire());
            ps.setBoolean(14, message.isEstSupprimeExpediteur());
            ps.setBoolean(15, message.isEstSupprimeDestinataire());
            ps.setLong(16, message.getId());

            ps.executeUpdate();
        }
    }

    public void supprimer(long id) throws SQLException {
        String sql = "DELETE FROM message WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public Message getById(long id) throws SQLException {
        String sql = "SELECT * FROM message WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapMessage(rs);
                }
            }
        }
        return null;
    }

    public List<Message> getAll() throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message ORDER BY date_envoi DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                messages.add(mapMessage(rs));
            }
        }
        return messages;
    }

    public List<Message> getMessagesRecus(long destinataireId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE destinataire_id = ? ORDER BY date_envoi DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, destinataireId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapMessage(rs));
                }
            }
        }
        return messages;
    }

    public List<Message> getMessagesEnvoyes(long expediteurId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE expediteur_id = ? ORDER BY date_envoi DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, expediteurId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapMessage(rs));
                }
            }
        }
        return messages;
    }

    public void marquerCommeLu(long id) throws SQLException {
        String sql = "UPDATE message SET statut = ?, date_lecture = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "lu");
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    private Message mapMessage(ResultSet rs) throws SQLException {
        Message message = new Message();

        message.setId(rs.getLong("id"));
        message.setExpediteurId(rs.getLong("expediteur_id"));
        message.setDestinataireId(rs.getLong("destinataire_id"));
        message.setObjet(rs.getString("objet"));
        message.setContenu(rs.getString("contenu"));

        Timestamp dateEnvoi = rs.getTimestamp("date_envoi");
        if (dateEnvoi != null) {
            message.setDateEnvoi(dateEnvoi.toLocalDateTime());
        }

        Timestamp dateLecture = rs.getTimestamp("date_lecture");
        if (dateLecture != null) {
            message.setDateLecture(dateLecture.toLocalDateTime());
        }

        message.setStatut(rs.getString("statut"));
        message.setPriorite(rs.getString("priorite"));
        message.setPieceJointeUrl(rs.getString("piece_jointe_url"));

        long parentId = rs.getLong("parent_id");
        if (!rs.wasNull()) {
            message.setParentId(parentId);
        }

        message.setCategorie(rs.getString("categorie"));
        message.setEstArchiveExpediteur(rs.getBoolean("est_archive_expediteur"));
        message.setEstArchiveDestinataire(rs.getBoolean("est_archive_destinataire"));
        message.setEstSupprimeExpediteur(rs.getBoolean("est_supprime_expediteur"));
        message.setEstSupprimeDestinataire(rs.getBoolean("est_supprime_destinataire"));

        return message;
    }
}