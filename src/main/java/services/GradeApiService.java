package services;

import entities.Evaluation;
import entities.Score;
import entities.Soumission;
import utils.AppSecrets;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;

public class GradeApiService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final SoumissionService soumissionService = new SoumissionService();
    private final EvaluationService evaluationService = new EvaluationService();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public void syncGradeCreate(Score score) throws IOException {
        String apiUrl = AppSecrets.get("grade.api.url");
        if (apiUrl.isBlank()) {
            System.out.println("Grade API URL not configured. Skipping API sync for create.");
            return;
        }

        try {
            Soumission soumission = soumissionService.getById(score.getSoumissionId());
            if (soumission == null) {
                System.err.println("Soumission not found for score. Skipping API sync.");
                return;
            }

            Evaluation evaluation = evaluationService.getById(soumission.getEvaluationId());

            String payload = buildGradePayload(score, soumission, evaluation, "CREATE");
            String endpoint = apiUrl + "/grades";

            System.out.println("[DEBUG] API Request:");
            System.out.println("  Endpoint: " + endpoint);
            System.out.println("  Method: POST");
            System.out.println("  Payload: " + payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + AppSecrets.get("grade.api.key"))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("[DEBUG] API Response: Status " + response.statusCode());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Grade successfully synced to API: " + response.body());
            } else {
                System.err.println("Failed to sync grade to API. Status: " + response.statusCode());
                System.err.println("Response Body: " + response.body().substring(0, Math.min(500, response.body().length())));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("API sync interrupted: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error syncing grade to API: " + e.getMessage());
        }
    }

    public void syncGradeUpdate(Score score) throws IOException {
        String apiUrl = AppSecrets.get("grade.api.url");
        if (apiUrl.isBlank()) {
            System.out.println("Grade API URL not configured. Skipping API sync for update.");
            return;
        }

        try {
            Soumission soumission = soumissionService.getById(score.getSoumissionId());
            if (soumission == null) {
                System.err.println("Soumission not found for score. Skipping API sync.");
                return;
            }

            Evaluation evaluation = evaluationService.getById(soumission.getEvaluationId());

            String payload = buildGradePayload(score, soumission, evaluation, "UPDATE");
            String endpoint = apiUrl + "/grades/" + score.getId();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + AppSecrets.get("grade.api.key"))
                    .PUT(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Grade update successfully synced to API: " + response.body());
            } else {
                System.err.println("Failed to sync grade update to API. Status: " + response.statusCode() + ", Body: " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("API sync interrupted: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error syncing grade update to API: " + e.getMessage());
        }
    }

    public void syncGradeDelete(int scoreId) throws IOException {
        String apiUrl = AppSecrets.get("grade.api.url");
        if (apiUrl.isBlank()) {
            System.out.println("Grade API URL not configured. Skipping API sync for delete.");
            return;
        }

        try {
            String endpoint = apiUrl + "/grades/" + scoreId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + AppSecrets.get("grade.api.key"))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Grade deletion successfully synced to API");
            } else if (response.statusCode() == 404) {
                System.out.println("Grade not found in API (already deleted or never existed)");
            } else {
                System.err.println("Failed to sync grade deletion to API. Status: " + response.statusCode() + ", Body: " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("API sync interrupted: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error syncing grade deletion to API: " + e.getMessage());
        }
    }

    private String buildGradePayload(Score score, Soumission soumission, Evaluation evaluation, String action) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"scoreId\":").append(score.getId()).append(",");
        json.append("\"action\":\"").append(action).append("\",");
        json.append("\"soumissionId\":").append(score.getSoumissionId()).append(",");
        json.append("\"studentId\":\"").append(escapeJson(soumission.getIdEtudiant())).append("\",");
        json.append("\"note\":").append(score.getNote()).append(",");
        json.append("\"noteSur\":").append(score.getNoteSur()).append(",");
        json.append("\"pourcentage\":").append(score.getPourcentage() != null ? score.getPourcentage() : "null").append(",");
        json.append("\"mention\":\"").append(score.getMention() != null ? escapeJson(score.getMention()) : "").append("\",");
        json.append("\"commentaire\":\"").append(score.getCommentaireEnseignant() != null ? escapeJson(score.getCommentaireEnseignant()) : "").append("\",");
        json.append("\"statut\":\"").append(escapeJson(score.getStatutCorrection())).append("\",");
        json.append("\"dateCorrection\":\"").append(score.getDateCorrection() != null ? score.getDateCorrection().format(DATE_FORMATTER) : "").append("\"");

        if (evaluation != null) {
            json.append(",");
            json.append("\"evaluation\":{");
            json.append("\"id\":").append(evaluation.getId()).append(",");
            json.append("\"titre\":\"").append(escapeJson(evaluation.getTitre())).append("\",");
            json.append("\"type\":\"").append(escapeJson(evaluation.getTypeEvaluation())).append("\"");
            json.append("}");
        }

        json.append("}");
        return json.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}
