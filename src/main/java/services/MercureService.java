package services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class MercureService {

    private static final String HUB_URL   = "http://localhost:3000/.well-known/mercure";
    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJtZXJjdXJlIjp7InB1Ymxpc2giOlsiKiJdLCJzdWJzY3JpYmUiOlsiKiJdfX0.nINXJPMGL7u4vvquYYm3zgMosrqrxTSooTs7R_OJLZA";

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // ═══════════════════════════════════════════════════════════
    // NOTIFICATION PROF : un étudiant a passé son quiz
    // ═══════════════════════════════════════════════════════════
    public void notifierProfQuizSoumis(
            long enseignantId, String nomEtudiant,
            String quizTitre, double score,
            int earnedPoints, int totalPoints) {

        // ✅ Topic spécifique au prof propriétaire du quiz
        String topic = "quiz/resultat/" + enseignantId;
        String data  = String.format(
                "{\"type\":\"quiz_soumis\",\"etudiant\":\"%s\",\"quiz\":\"%s\","
                        + "\"score\":%.1f,\"earned\":%d,\"total\":%d}",
                escape(nomEtudiant), escape(quizTitre), score, earnedPoints, totalPoints
        );
        publier(topic, data);
    }

    // ═══════════════════════════════════════════════════════════
    // NOTIFICATION ÉTUDIANT : inscrit à un cours
    // ═══════════════════════════════════════════════════════════
    public void notifierEtudiantInscription(
            long   etudiantId,
            String coursTitre,
            String coursCode) {

        String topic = "cours/inscription/" + etudiantId;
        String data  = String.format(
                "{\"type\":\"inscription\",\"cours\":\"%s\",\"code\":\"%s\"}",
                escape(coursTitre), escape(coursCode != null ? coursCode : "")
        );
        publier(topic, data);
    }

    // ═══════════════════════════════════════════════════════════
    // PUBLICATION HTTP VERS MERCURE
    // ═══════════════════════════════════════════════════════════
    private void publier(String topic, String jsonData) {
        new Thread(() -> {
            try {
                String body = "topic=" + URLEncoder.encode(topic, StandardCharsets.UTF_8)
                        + "&data=" + URLEncoder.encode(jsonData, StandardCharsets.UTF_8);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(HUB_URL))
                        .header("Authorization", "Bearer " + JWT_TOKEN)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .timeout(Duration.ofSeconds(5))
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    System.out.println("✅ Mercure → topic=" + topic);
                } else {
                    System.err.println("⚠ Mercure erreur " + response.statusCode()
                            + " : " + response.body());
                }
            } catch (Exception e) {
                System.err.println("⚠ Mercure non disponible : " + e.getMessage());
            }
        }, "mercure-publisher").start();
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITAIRE
    // ═══════════════════════════════════════════════════════════
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}