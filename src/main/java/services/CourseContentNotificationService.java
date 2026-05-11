package services;

import entities.Contenu;
import entities.Cours;
import entities.Etudiant;
import entities.Utilisateur;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class CourseContentNotificationService {

    private final CoursService coursService = new CoursService();

    public NotificationDispatchResult notifyStudentsForNewContent(Contenu contenu, Utilisateur auteur) throws SQLException {
        if (contenu == null || contenu.getCours() == null || contenu.getCours().getId() == null) {
            return NotificationDispatchResult.none();
        }

        Cours cours = contenu.getCours();
        List<Etudiant> recipients = collectRecipients(coursService.getStudentsByCoursId(cours.getId()));
        if (recipients.isEmpty()) {
            return NotificationDispatchResult.none();
        }

        try {
            EmailService.ContentMailResult result =
                    new EmailService().envoyerNouveauContenuSync(recipients, cours, contenu, auteur);
            return new NotificationDispatchResult(
                    result.getRecipientCount(),
                    result.getDeliveredCount(),
                    result.getErrorSummary()
            );
        } catch (RuntimeException e) {
            return NotificationDispatchResult.failed(recipients.size(), 0, e.getMessage());
        }
    }

    private List<Etudiant> collectRecipients(List<Etudiant> students) {
        LinkedHashMap<String, Etudiant> uniqueRecipients = new LinkedHashMap<>();
        if (students == null) {
            return new ArrayList<>();
        }

        for (Etudiant etudiant : students) {
            if (etudiant == null || etudiant.getEmail() == null || etudiant.getEmail().isBlank()) {
                continue;
            }

            String normalizedEmail = etudiant.getEmail().trim().toLowerCase(Locale.ROOT);
            uniqueRecipients.putIfAbsent(normalizedEmail, etudiant);
        }

        return new ArrayList<>(uniqueRecipients.values());
    }

    public static final class NotificationDispatchResult {
        private final int recipientCount;
        private final int deliveredCount;
        private final String errorMessage;

        private NotificationDispatchResult(int recipientCount, int deliveredCount, String errorMessage) {
            this.recipientCount = recipientCount;
            this.deliveredCount = deliveredCount;
            this.errorMessage = errorMessage == null ? "" : errorMessage.trim();
        }

        public static NotificationDispatchResult none() {
            return new NotificationDispatchResult(0, 0, "");
        }

        public static NotificationDispatchResult failed(int recipientCount, int deliveredCount, String errorMessage) {
            return new NotificationDispatchResult(recipientCount, deliveredCount, errorMessage);
        }

        public int getRecipientCount() {
            return recipientCount;
        }

        public int getDeliveredCount() {
            return deliveredCount;
        }

        public boolean hasRecipients() {
            return recipientCount > 0;
        }

        public boolean hasFailure() {
            return !errorMessage.isBlank() || deliveredCount < recipientCount;
        }

        public boolean isFullyDelivered() {
            return recipientCount > 0 && deliveredCount == recipientCount && errorMessage.isBlank();
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
