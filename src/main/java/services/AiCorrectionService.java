package services;

import entities.Evaluation;
import entities.Soumission;
import utils.AppSecrets;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AiCorrectionService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public static class CorrectionSuggestion {
        public final double suggestedNote;
        public final double noteSur;
        public final String suggestedComment;
        public final String aiReasoning;

        public CorrectionSuggestion(double suggestedNote, double noteSur, String suggestedComment, String aiReasoning) {
            this.suggestedNote = suggestedNote;
            this.noteSur = noteSur;
            this.suggestedComment = suggestedComment;
            this.aiReasoning = aiReasoning;
        }
    }

    public CorrectionSuggestion suggestCorrection(Soumission soumission, Evaluation evaluation) throws IOException, InterruptedException {
        return suggestCorrection(soumission, evaluation, null);
    }

    public CorrectionSuggestion suggestCorrection(Soumission soumission, Evaluation evaluation, String consigneEnseignant) throws IOException, InterruptedException {
        String apiKey = AppSecrets.get("groq.apiKey");
        if (apiKey.isBlank()) {
            throw new IOException("Groq API key is missing in app-secrets.properties.");
        }

        String endpoint = AppSecrets.get("groq.endpoint");
        String model = AppSecrets.get("groq.model");
        String prompt = buildCorrectionPrompt(soumission, evaluation, consigneEnseignant);
        String payload = buildGroqPayload(model, prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            throw new IOException("La clé API Groq n'a pas les permissions nécessaires.");
        }
        if (response.statusCode() >= 400) {
            throw new IOException("Erreur API Groq " + response.statusCode() + ": " + response.body());
        }

        String aiResponse = extractGroqMessage(response.body());
        return parseAiResponse(aiResponse, evaluation.getNoteMax());
    }

    private String buildCorrectionPrompt(Soumission soumission, Evaluation evaluation, String consigneEnseignant) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Tu es un enseignant expert chargé de corriger une soumission d'étudiant.\n\n");
        
        prompt.append("=== INFORMATIONS DE L'ÉVALUATION ===\n");
        prompt.append("Titre: ").append(evaluation.getTitre()).append("\n");
        prompt.append("Type: ").append(evaluation.getTypeEvaluation()).append("\n");
        prompt.append("Description/Consigne: ").append(
            evaluation.getDescription() != null ? evaluation.getDescription() : "Non spécifiée"
        ).append("\n");
        prompt.append("Note maximale: ").append(evaluation.getNoteMax()).append("\n\n");
        
        // Consigne spécifique de l'enseignant
        if (consigneEnseignant != null && !consigneEnseignant.isBlank()) {
            prompt.append("=== CONSIGNE SPÉCIFIQUE DE L'ENSEIGNANT ===\n");
            prompt.append(consigneEnseignant).append("\n\n");
        }
        
        prompt.append("=== SOUMISSION DE L'ÉTUDIANT ===\n");
        prompt.append("ID Étudiant: ").append(soumission.getIdEtudiant()).append("\n");
        prompt.append("Date de soumission: ").append(soumission.getDateSoumission()).append("\n");
        prompt.append("Commentaire de l'étudiant: ").append(
            soumission.getCommentaireEtudiant() != null && !soumission.getCommentaireEtudiant().isBlank() 
                ? soumission.getCommentaireEtudiant() 
                : "Aucun commentaire"
        ).append("\n");
        
        // Extraire le texte du PDF si disponible
        String pdfText = extractPdfText(soumission);
        if (pdfText != null && !pdfText.isBlank()) {
            prompt.append("\n=== CONTENU DU PDF SOUMIS ===\n");
            // Limiter à 8000 caractères pour ne pas dépasser les limites de tokens
            String truncatedText = pdfText.length() > 8000 ? pdfText.substring(0, 8000) + "\n[... contenu tronqué ...]" : pdfText;
            prompt.append(truncatedText).append("\n");
        } else if (soumission.getPdfFilename() != null) {
            prompt.append("PDF soumis: ").append(soumission.getPdfFilename()).append(" (contenu non extrait)\n");
        } else {
            prompt.append("PDF soumis: Aucun fichier\n");
        }
        prompt.append("\n");
        
        prompt.append("=== TA TÂCHE ===\n");
        prompt.append("1. Analyse la qualité de la soumission basée sur les informations disponibles\n");
        prompt.append("2. Attribue une note sur ").append(evaluation.getNoteMax()).append("\n");
        prompt.append("3. Rédige un commentaire constructif pour l'étudiant\n");
        if (consigneEnseignant != null && !consigneEnseignant.isBlank()) {
            prompt.append("4. Respecte IMPÉRATIVEMENT la consigne de l'enseignant ci-dessus\n");
        }
        prompt.append("\n");
        
        prompt.append("=== FORMAT DE RÉPONSE REQUIS ===\n");
        prompt.append("Réponds UNIQUEMENT dans ce format exact:\n");
        prompt.append("NOTE_SUGGEREE: [nombre entre 0 et ").append(evaluation.getNoteMax()).append("]\n");
        prompt.append("COMMENTAIRE_SUGGERE: [ton commentaire pédagogique]\n");
        prompt.append("RAISONNEMENT: [explication brève de ton évaluation]\n\n");
        
        prompt.append("Règles importantes:\n");
        prompt.append("- La note doit être réaliste (0 à ").append(evaluation.getNoteMax()).append(")\n");
        prompt.append("- Le commentaire doit être encourageant mais honnête\n");
        if (consigneEnseignant != null && !consigneEnseignant.isBlank()) {
            prompt.append("- Respecte STRICTEMENT les critères de correction donnés par l'enseignant\n");
        }
        prompt.append("- Si peu d'informations sont disponibles, suggère une note moyenne (").append(evaluation.getNoteMax() / 2).append(") avec un commentaire demandant plus de détails");
        
        return prompt.toString();
    }
    
    private String extractPdfText(Soumission soumission) {
        if (soumission.getPdfFilename() == null || soumission.getPdfFilename().isBlank()) {
            return null;
        }
        
        String uploadsDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        File pdfFile = new File(uploadsDir + soumission.getPdfFilename());
        
        if (!pdfFile.exists()) {
            System.err.println("PDF file not found: " + pdfFile.getAbsolutePath());
            return null;
        }
        
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return text.trim();
        } catch (IOException e) {
            System.err.println("Error extracting PDF text: " + e.getMessage());
            return null;
        }
    }

    private CorrectionSuggestion parseAiResponse(String aiResponse, double noteMax) {
        double suggestedNote = noteMax / 2; // Default: middle score
        String suggestedComment = "Correction automatique par IA.";
        String reasoning = "Réponse par défaut";

        String[] lines = aiResponse.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("NOTE_SUGGEREE:")) {
                try {
                    String value = line.substring("NOTE_SUGGEREE:".length()).trim();
                    suggestedNote = Double.parseDouble(value.replace(",", "."));
                    // Clamp to valid range
                    suggestedNote = Math.max(0, Math.min(suggestedNote, noteMax));
                } catch (NumberFormatException e) {
                    // Keep default
                }
            } else if (line.startsWith("COMMENTAIRE_SUGGERE:")) {
                suggestedComment = line.substring("COMMENTAIRE_SUGGERE:".length()).trim();
                if (suggestedComment.isEmpty()) {
                    suggestedComment = "Correction automatique par IA.";
                }
            } else if (line.startsWith("RAISONNEMENT:")) {
                reasoning = line.substring("RAISONNEMENT:".length()).trim();
            }
        }

        return new CorrectionSuggestion(suggestedNote, noteMax, suggestedComment, reasoning);
    }

    private String buildGroqPayload(String model, String prompt) {
        // Groq uses OpenAI-compatible format with messages array
        String systemPrompt = "Tu es un assistant de correction pour enseignants. Tu analyses les soumissions d'étudiants et proposes des notes et commentaires appropriés. Tu dois toujours répondre dans le format demandé avec NOTE_SUGGEREE, COMMENTAIRE_SUGGERE et RAISONNEMENT.";

        return "{"
                + "\"model\":\"" + escapeJson(model) + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + escapeJson(systemPrompt) + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}"
                + "],"
                + "\"temperature\":0.3,"
                + "\"max_tokens\":2048"
                + "}";
    }

    private String extractGroqMessage(String responseBody) {
        // Groq uses OpenAI-compatible format: {"choices":[{"message":{"content":"..."}}]}
        String marker = "\"content\":";
        int contentIndex = responseBody.indexOf(marker);
        if (contentIndex < 0) {
            // Fallback to other formats
            marker = "\"output\":";
            contentIndex = responseBody.indexOf(marker);
        }
        if (contentIndex < 0) {
            marker = "\"text\":";
            contentIndex = responseBody.indexOf(marker);
        }
        if (contentIndex < 0) {
            return "";
        }

        int quoteStart = responseBody.indexOf('"', contentIndex + marker.length());
        if (quoteStart < 0) {
            return "";
        }

        int start = quoteStart + 1;
        StringBuilder builder = new StringBuilder();
        boolean escaping = false;

        for (int i = start; i < responseBody.length(); i++) {
            char current = responseBody.charAt(i);
            if (escaping) {
                switch (current) {
                    case 'n' -> builder.append('\n');
                    case 'r' -> builder.append('\r');
                    case 't' -> builder.append('\t');
                    case '"' -> builder.append('"');
                    case '\\' -> builder.append('\\');
                    default -> builder.append(current);
                }
                escaping = false;
                continue;
            }

            if (current == '\\') {
                escaping = true;
                continue;
            }
            if (current == '"') {
                break;
            }
            builder.append(current);
        }

        return builder.toString().trim();
    }

    // Keep for backward compatibility (Gemini format)
    private String extractAssistantMessage(String responseBody) {
        String marker = "\"text\":";
        int textIndex = responseBody.indexOf(marker);
        if (textIndex < 0) {
            return "";
        }

        int quoteStart = responseBody.indexOf('"', textIndex + marker.length());
        if (quoteStart < 0) {
            return "";
        }

        int start = quoteStart + 1;
        StringBuilder builder = new StringBuilder();
        boolean escaping = false;

        for (int i = start; i < responseBody.length(); i++) {
            char current = responseBody.charAt(i);
            if (escaping) {
                switch (current) {
                    case 'n' -> builder.append('\n');
                    case 'r' -> builder.append('\r');
                    case 't' -> builder.append('\t');
                    case '"' -> builder.append('"');
                    case '\\' -> builder.append('\\');
                    default -> builder.append(current);
                }
                escaping = false;
                continue;
            }

            if (current == '\\') {
                escaping = true;
                continue;
            }
            if (current == '"') {
                break;
            }
            builder.append(current);
        }

        return builder.toString().trim();
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
