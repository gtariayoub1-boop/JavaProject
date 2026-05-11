package services;

import entities.Contenu;
import entities.Cours;
import entities.Etudiant;
import entities.Utilisateur;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class EmailService{

    private static final DateTimeFormatter CONTENT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);

    private final String senderEmail;
    private final String senderPassword;

    public EmailService() {
        try (InputStream input = getClass()
                .getResourceAsStream("/config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties introuvable");
            }
            Properties config = new Properties();
            config.load(input);
            this.senderEmail = firstNonBlank(
                    config.getProperty("mail.sender"),
                    config.getProperty("mail.username")
            );
            this.senderPassword = normalizeMailPassword(firstNonBlank(
                    config.getProperty("mail.password"),
                    config.getProperty("mail.appPassword")
            ));
        } catch (IOException e) {
            throw new RuntimeException("config.properties introuvable", e);
        }

        if (senderEmail.isBlank() || senderPassword.isBlank()) {
            throw new RuntimeException("Configuration email manquante: mail.sender/mail.password.");
        }
    }

    // ── Envoi asynchrone ─────────────────────────────────────────
    public void envoyerResultatAsync(String destinataireEmail,
                                     String nomEtudiant,
                                     String titreQuiz,
                                     int pointsObtenus,
                                     int pointsTotal,
                                     int score,
                                     String datePassation) {
        new Thread(() -> {
            try {
                envoyer(destinataireEmail, nomEtudiant, titreQuiz,
                        pointsObtenus, pointsTotal, score, datePassation);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void envoyerNouveauContenuAsync(List<Etudiant> destinataires,
                                           Cours cours,
                                           Contenu contenu,
                                           Utilisateur auteur) {
        if (destinataires == null || destinataires.isEmpty() || cours == null || contenu == null) {
            return;
        }

        new Thread(() -> {
            envoyerNouveauContenuSync(destinataires, cours, contenu, auteur);
        }, "content-mail-notifier").start();
    }

    public ContentMailResult envoyerNouveauContenuSync(List<Etudiant> destinataires,
                                                       Cours cours,
                                                       Contenu contenu,
                                                       Utilisateur auteur) {
        if (destinataires == null || destinataires.isEmpty() || cours == null || contenu == null) {
            return new ContentMailResult(0, 0, "");
        }

        List<Etudiant> recipients = List.copyOf(destinataires);
        String titreCours = cours.getTitre() != null ? cours.getTitre() : "Cours";
        String codeCours = cours.getCodeCours() != null ? cours.getCodeCours() : "-";
        String titreContenu = contenu.getTitre() != null && !contenu.getTitre().isBlank()
                ? contenu.getTitre()
                : "Nouveau contenu";
        String typeContenu = contenu.getTypeContenuList().stream()
                .map(this::formatTypeContenu)
                .collect(Collectors.joining(", "));
        String description = contenu.getDescription() != null ? contenu.getDescription().trim() : "";
        String auteurNom = auteur != null && auteur.getNomComplet() != null && !auteur.getNomComplet().isBlank()
                ? auteur.getNomComplet()
                : "Administration";
        String auteurRole = formatAuteurRole(auteur);
        String dateAjout = formatDateAjout(contenu.getDateAjout());

        int deliveredCount = 0;
        Map<String, String> failures = new LinkedHashMap<>();

        for (Etudiant etudiant : recipients) {
            if (etudiant == null || etudiant.getEmail() == null || etudiant.getEmail().isBlank()) {
                continue;
            }
            String email = etudiant.getEmail().trim();
            try {
                envoyerNouveauContenu(
                        email,
                        etudiant.getNomComplet(),
                        titreCours,
                        codeCours,
                        titreContenu,
                        typeContenu,
                        description,
                        auteurNom,
                        auteurRole,
                        dateAjout
                );
                deliveredCount++;
            } catch (MessagingException e) {
                failures.put(email, extractMailError(e));
            }
        }

        return new ContentMailResult(recipients.size(), deliveredCount, formatFailures(failures));
    }

    // ── Envoi principal ──────────────────────────────────────────
    private void envoyer(String destinataireEmail,
                         String nomEtudiant,
                         String titreQuiz,
                         int pointsObtenus,
                         int pointsTotal,
                         int score,
                         String datePassation) throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderEmail, senderPassword);
                    }
                });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(destinataireEmail));
        message.setSubject("Résultat — " + titreQuiz);
        message.setContent(
                construireCorps(nomEtudiant, titreQuiz,
                        pointsObtenus, pointsTotal, score, datePassation),
                "text/html; charset=utf-8");

        Transport.send(message);
    }

    private void envoyerNouveauContenu(String destinataireEmail,
                                       String nomEtudiant,
                                       String titreCours,
                                       String codeCours,
                                       String titreContenu,
                                       String typeContenu,
                                       String description,
                                       String auteurNom,
                                       String auteurRole,
                                       String dateAjout) throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderEmail, senderPassword);
                    }
                });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataireEmail));
        message.setSubject("Nouveau contenu - " + titreCours);
        message.setContent(
                construireCorpsNouveauContenu(
                        nomEtudiant,
                        titreCours,
                        codeCours,
                        titreContenu,
                        typeContenu,
                        description,
                        auteurNom,
                        auteurRole,
                        dateAjout
                ),
                "text/html; charset=utf-8"
        );

        Transport.send(message);
    }

    // ── Corps HTML ───────────────────────────────────────────────
    private String construireCorps(String nomEtudiant,
                                   String titreQuiz,
                                   int pointsObtenus,
                                   int pointsTotal,
                                   int score,
                                   String datePassation) {
        String mention = score >= 85 ? "Très bien"
                : score >= 70 ? "Bien"
                  : score >= 50 ? "Passable"
                    : "Insuffisant";

        String mentionColor = score >= 70 ? "#16a34a" : score >= 50 ? "#ca8a04" : "#dc2626";

        return """
            <div style="font-family:sans-serif;max-width:600px;margin:auto;padding:24px;">
              <h2 style="color:#0f766e;margin-bottom:4px;">Résultat de votre quiz</h2>
              <p style="color:#888;font-size:13px;margin-top:0;">Campus Access — Quiz Manager</p>
              <hr style="border:none;border-top:1px solid #e5e7eb;margin:16px 0;">
              <p>Bonjour <b>%s</b>,</p>
              <p>Voici votre résultat pour le quiz <b>%s</b> :</p>
              <div style="background:#f0fdf4;border-radius:12px;padding:24px;margin:16px 0;border:1px solid #bbf7d0;">
                <p style="font-size:40px;font-weight:900;color:#16a34a;margin:0;">%d / %d</p>
                <p style="color:#555;margin:6px 0;">Score : <b>%d%%</b></p>
                <p style="color:%s;font-weight:700;margin:4px 0;">%s</p>
                <p style="color:#999;font-size:12px;margin-top:8px;">Passé le : %s</p>
              </div>
              <p style="color:#aaa;font-size:11px;margin-top:24px;">
                Ce message est envoyé automatiquement — Campus Access
              </p>
            </div>
            """.formatted(
                nomEtudiant, titreQuiz,
                pointsObtenus, pointsTotal,
                score, mentionColor, mention,
                datePassation
        );
    }

    private String construireCorpsNouveauContenu(String nomEtudiant,
                                                 String titreCours,
                                                 String codeCours,
                                                 String titreContenu,
                                                 String typeContenu,
                                                 String description,
                                                 String auteurNom,
                                                 String auteurRole,
                                                 String dateAjout) {
        String safeStudentName = escapeHtml(nomEtudiant == null || nomEtudiant.isBlank() ? "Etudiant" : nomEtudiant);
        String safeCourseTitle = escapeHtml(titreCours == null || titreCours.isBlank() ? "Cours" : titreCours);
        String safeCourseCode = escapeHtml(codeCours == null || codeCours.isBlank() ? "-" : codeCours);
        String safeContentTitle = escapeHtml(titreContenu == null || titreContenu.isBlank() ? "Nouveau contenu" : titreContenu);
        String safeContentType = escapeHtml(typeContenu == null || typeContenu.isBlank() ? "Contenu" : typeContenu);
        String safeAuthorName = escapeHtml(auteurNom == null || auteurNom.isBlank() ? "Administration" : auteurNom);
        String safeAuthorRole = escapeHtml(auteurRole == null || auteurRole.isBlank() ? "Administration" : auteurRole);
        String safeDate = escapeHtml(dateAjout == null || dateAjout.isBlank() ? "-" : dateAjout);
        String safeDescription = escapeHtml(description == null ? "" : description);

        String descriptionBlock = safeDescription.isBlank()
                ? ""
                : "<p style=\"margin:12px 0 0;color:#475569;\"><b>Description :</b> " + safeDescription + "</p>";

        return """
            <div style="font-family:Arial,sans-serif;max-width:640px;margin:auto;padding:24px;background:#f8fafc;color:#1e293b;">
              <div style="background:#ffffff;border:1px solid #e2e8f0;border-radius:18px;padding:28px;box-shadow:0 18px 40px rgba(15,23,42,.08);">
                <h2 style="margin-top:0;color:#0f172a;">Nouveau contenu disponible</h2>
                <p style="color:#64748b;font-size:13px;margin-top:0;">Campus Access - Notification de cours</p>
                <p>Bonjour <b>%s</b>,</p>
                <p>Un nouveau contenu a ete ajoute dans votre cours <b>%s</b> (%s).</p>
                <div style="background:#eff6ff;border:1px solid #bfdbfe;border-radius:14px;padding:18px;margin:18px 0;">
                  <p style="margin:0 0 8px;"><b>Contenu :</b> %s</p>
                  <p style="margin:0 0 8px;"><b>Type :</b> %s</p>
                  <p style="margin:0 0 8px;"><b>Ajoute par :</b> %s (%s)</p>
                  <p style="margin:0;"><b>Date :</b> %s</p>
                </div>
                %s
                <p style="margin-top:18px;">Ouvrez l'application pour consulter ce contenu dans votre espace cours.</p>
              </div>
            </div>
            """.formatted(
                safeStudentName,
                safeCourseTitle,
                safeCourseCode,
                safeContentTitle,
                safeContentType,
                safeAuthorName,
                safeAuthorRole,
                safeDate,
                descriptionBlock
        );
    }

    private String formatTypeContenu(String type) {
        if (type == null || type.isBlank()) {
            return "Contenu";
        }
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "video" -> "Video";
            case "pdf" -> "Document PDF";
            case "ppt" -> "Presentation";
            case "quiz" -> "Quiz";
            case "lien" -> "Lien externe";
            case "texte" -> "Texte";
            default -> type;
        };
    }

    private String formatAuteurRole(Utilisateur auteur) {
        if (auteur == null || auteur.getType() == null || auteur.getType().isBlank()) {
            return "administration";
        }
        return switch (auteur.getType().toLowerCase(Locale.ROOT)) {
            case "enseignant" -> "enseignant";
            case "administrateur" -> "administration";
            default -> auteur.getType();
        };
    }

    private String formatDateAjout(LocalDateTime dateAjout) {
        return dateAjout == null ? "-" : CONTENT_DATE_FORMATTER.format(dateAjout);
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String normalizeMailPassword(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(" ", "").trim();
    }

    private String extractMailError(MessagingException e) {
        List<String> messages = new ArrayList<>();
        Throwable current = e;

        while (current != null && messages.size() < 4) {
            String message = current.getMessage();
            if (message != null && !message.isBlank() && !messages.contains(message.trim())) {
                messages.add(message.trim());
            }
            if (current instanceof MessagingException messagingException && messagingException.getNextException() != null) {
                current = messagingException.getNextException();
            } else {
                current = current.getCause();
            }
        }

        if (messages.isEmpty()) {
            return "Erreur SMTP inconnue";
        }
        return String.join(" | ", messages);
    }

    private String formatFailures(Map<String, String> failures) {
        if (failures.isEmpty()) {
            return "";
        }

        List<String> messages = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, String> entry : failures.entrySet()) {
            messages.add(entry.getKey() + ": " + entry.getValue());
            count++;
            if (count >= 3) {
                break;
            }
        }
        if (failures.size() > 3) {
            messages.add("... " + (failures.size() - 3) + " autre(s) erreur(s)");
        }
        return String.join(" | ", messages);
    }

    public static final class ContentMailResult {
        private final int recipientCount;
        private final int deliveredCount;
        private final String errorSummary;

        public ContentMailResult(int recipientCount, int deliveredCount, String errorSummary) {
            this.recipientCount = recipientCount;
            this.deliveredCount = deliveredCount;
            this.errorSummary = errorSummary == null ? "" : errorSummary.trim();
        }

        public int getRecipientCount() {
            return recipientCount;
        }

        public int getDeliveredCount() {
            return deliveredCount;
        }

        public boolean hasFailure() {
            return !errorSummary.isBlank() || deliveredCount < recipientCount;
        }

        public String getErrorSummary() {
            return errorSummary;
        }
    }
}
