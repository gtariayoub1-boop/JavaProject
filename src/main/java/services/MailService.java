package services;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import utils.AppSecrets;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class MailService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void sendPasswordResetEmail(String recipientEmail, String recipientName, String resetUrl, LocalDateTime expiresAt)
            throws MessagingException {
        String username = AppSecrets.get("mail.username");
        String appPassword = AppSecrets.get("mail.appPassword");
        String fromName = AppSecrets.get("mail.fromName");

        if (username.isBlank() || appPassword.isBlank()) {
            throw new MessagingException("Mail credentials are missing in app-secrets.properties.");
        }

        Session session = Session.getInstance(buildMailProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, appPassword);
            }
        });

        Message message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(username, fromName));
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Invalid mail sender configuration.", e);
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("PIDEV password reset");
        message.setContent(buildResetMailBody(recipientName, resetUrl, expiresAt), "text/html; charset=UTF-8");

        Transport.send(message);
    }

    public void sendScoreNotificationEmail(String recipientEmail, String studentName, String evaluationTitle,
            String evaluationType, double note, double noteSur, Double pourcentage, String commentaireEnseignant,
            LocalDateTime dateCorrection, boolean isUpdate) throws MessagingException {
        String username = AppSecrets.get("mail.username");
        String appPassword = AppSecrets.get("mail.appPassword");
        String fromName = AppSecrets.get("mail.fromName");

        if (username.isBlank() || appPassword.isBlank()) {
            throw new MessagingException("Mail credentials are missing in app-secrets.properties.");
        }

        Session session = Session.getInstance(buildMailProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, appPassword);
            }
        });

        Message message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(username, fromName));
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Invalid mail sender configuration.", e);
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));

        String subject = isUpdate ? "Votre note a été modifiée - " + evaluationTitle : "Nouvelle note disponible - " + evaluationTitle;
        message.setSubject(subject);
        message.setContent(buildScoreNotificationBody(studentName, evaluationTitle, evaluationType, note, noteSur,
                pourcentage, commentaireEnseignant, dateCorrection, isUpdate), "text/html; charset=UTF-8");

        Transport.send(message);
    }

    private Properties buildMailProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        return properties;
    }

    private String buildResetMailBody(String recipientName, String resetUrl, LocalDateTime expiresAt) {
        String safeName = recipientName == null || recipientName.isBlank() ? "User" : recipientName;
        return "<!DOCTYPE html><html><body style=\"font-family:Arial,sans-serif;background:#f5f7fb;padding:24px;color:#1b2338;\">"
                + "<div style=\"max-width:640px;margin:0 auto;background:#ffffff;border-radius:18px;padding:32px;"
                + "box-shadow:0 18px 40px rgba(24,38,70,.12);\">"
                + "<h2 style=\"margin-top:0;\">Reset your password</h2>"
                + "<p>Hello " + safeName + ",</p>"
                + "<p>We received a password reset request for your PIDEV desktop account.</p>"
                + "<p>This link stays valid until <strong>" + expiresAt.format(DATE_TIME_FORMATTER) + "</strong>.</p>"
                + "<p><a href=\"" + resetUrl + "\" style=\"display:inline-block;padding:12px 20px;background:#1f5eff;"
                + "color:#ffffff;text-decoration:none;border-radius:10px;\">Open reset page</a></p>"
                + "<p>The desktop application must be running on this computer when you click the link.</p>"
                + "<p>If you did not request this reset, you can ignore this message.</p>"
                + "</div></body></html>";
    }

    private String buildScoreNotificationBody(String studentName, String evaluationTitle, String evaluationType,
            double note, double noteSur, Double pourcentage, String commentaireEnseignant,
            LocalDateTime dateCorrection, boolean isUpdate) {
        String safeName = studentName == null || studentName.isBlank() ? "Étudiant" : studentName;
        String percentageStr = pourcentage != null ? String.format("%.1f", pourcentage) + "%" : "N/A";
        String commentStr = commentaireEnseignant != null && !commentaireEnseignant.isBlank()
                ? commentaireEnseignant : "Aucun commentaire";
        String typeStr = evaluationType != null ? evaluationType : "Évaluation";
        String dateStr = dateCorrection != null ? dateCorrection.format(DATE_TIME_FORMATTER) : "N/A";

        String headerText = isUpdate ? "Votre note a été modifiée" : "Nouvelle note disponible";
        String introText = isUpdate
                ? "Votre note a été modifiée par votre enseignant. Voici les détails de votre évaluation :"
                : "Votre enseignant a corrigé votre soumission. Voici les détails de votre évaluation :";

        return "<!DOCTYPE html><html><body style=\"font-family:Arial,sans-serif;background:#f5f7fb;padding:24px;color:#1b2338;\">"
                + "<div style=\"max-width:640px;margin:0 auto;background:#ffffff;border-radius:18px;padding:32px;"
                + "box-shadow:0 18px 40px rgba(24,38,70,.12);\">"
                + "<h2 style=\"margin-top:0;color:#1f5eff;\">" + headerText + "</h2>"
                + "<p>Bonjour " + safeName + ",</p>"
                + "<p>" + introText + "</p>"
                + "<div style=\"background:#f8fafc;border-radius:12px;padding:20px;margin:20px 0;\">"
                + "<h3 style=\"margin-top:0;color:#1b2338;\">" + evaluationTitle + "</h3>"
                + "<p style=\"margin:8px 0;\"><strong>Type:</strong> " + typeStr + "</p>"
                + "<p style=\"margin:8px 0;\"><strong>Note:</strong> <span style=\"font-size:24px;color:#1f5eff;font-weight:bold;\">"
                + note + "/" + noteSur + "</span></p>"
                + "<p style=\"margin:8px 0;\"><strong>Pourcentage:</strong> " + percentageStr + "</p>"
                + "<p style=\"margin:8px 0;\"><strong>Date de correction:</strong> " + dateStr + "</p>"
                + "</div>"
                + "<div style=\"background:#fef3c7;border-radius:12px;padding:20px;margin:20px 0;border-left:4px solid #f59e0b;\">"
                + "<p style=\"margin:0;\"><strong>Commentaire de l'enseignant:</strong></p>"
                + "<p style=\"margin:8px 0 0 0;font-style:italic;\">\"" + commentStr + "\"</p>"
                + "</div>"
                + "<p style=\"color:#64748b;font-size:14px;margin-top:24px;\">Ceci est un message automatique de la plateforme PIDEV."
                + "<br>Pour toute question, veuillez contacter votre enseignant.</p>"
                + "</div></body></html>";
    }
}
