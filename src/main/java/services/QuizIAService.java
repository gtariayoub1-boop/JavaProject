package services;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import entities.Contenu;
import entities.Cours;
import okhttp3.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class QuizIAService {

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL    = "llama-3.3-70b-versatile";

    private final String       apiKey;
    private final OkHttpClient client;
    private final Gson         gson = new GsonBuilder().setPrettyPrinting().create();
    private final CoursService coursService = new CoursService();

    public QuizIAService() {
        try (InputStream input = getClass().getResourceAsStream("/config.properties")) {
            Properties config = new Properties();
            config.load(input);
            this.apiKey = config.getProperty("groq.api.key");
        } catch (IOException e) {
            throw new RuntimeException("config.properties introuvable", e);
        }
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTHODE PRINCIPALE — visite le cours et génère des questions
    // ═══════════════════════════════════════════════════════════════
    public List<QuestionGeneree> genererDepuisCours(Integer idCours, int nbQuestions, String typeQuestion) {
        if (idCours == null) {
            System.err.println("QuizIAService : aucun cours associé.");
            return new ArrayList<>();
        }

        try {
            Cours cours = coursService.getById(idCours);
            if (cours == null) {
                System.err.println("QuizIAService : cours introuvable id=" + idCours);
                return new ArrayList<>();
            }

            StringBuilder contexte = new StringBuilder();

            if (cours.getDescription() != null && !cours.getDescription().isBlank())
                contexte.append(cours.getDescription()).append("\n\n");

            if (cours.getNiveau() != null && !cours.getNiveau().isBlank())
                contexte.append("Niveau : ").append(cours.getNiveau()).append("\n");

            if (cours.getModule() != null) {
                if (cours.getModule().getTitreModule() != null)
                    contexte.append("Module : ").append(cours.getModule().getTitreModule()).append("\n");
                if (cours.getModule().getDescription() != null && !cours.getModule().getDescription().isBlank())
                    contexte.append("Description module : ").append(cours.getModule().getDescription()).append("\n");
                if (cours.getModule().getObjectifsApprentissage() != null && !cours.getModule().getObjectifsApprentissage().isBlank())
                    contexte.append("Objectifs : ").append(cours.getModule().getObjectifsApprentissage()).append("\n");
            }

            if (cours.getContenus() != null && !cours.getContenus().isEmpty()) {
                contexte.append("\n--- Contenus du cours ---\n");
                for (Contenu c : cours.getContenus()) {
                    String type = c.getTypeContenu() != null ? c.getTypeContenu().toUpperCase() : "CONTENU";
                    if (c.getTitre() != null && !c.getTitre().isBlank())
                        contexte.append("\n[").append(type).append("] ").append(c.getTitre()).append("\n");
                    if (c.getDescription() != null && !c.getDescription().isBlank())
                        contexte.append(c.getDescription()).append("\n");
                    if (c.getRessources() != null)
                        for (String res : c.getRessources())
                            if (res != null && !res.isBlank())
                                contexte.append("  • ").append(res).append("\n");
                }
            }

            if (cours.getPrerequis() != null && !cours.getPrerequis().isEmpty())
                contexte.append("\nPrérequis : ").append(String.join(", ", cours.getPrerequis())).append("\n");

            String contexteStr = contexte.toString().trim();
            if (contexteStr.isEmpty()) contexteStr = "Cours de " + cours.getTitre();

            System.out.println("=== Contexte IA ===\n" + contexteStr.substring(0, Math.min(500, contexteStr.length())));

            String prompt = String.format(
                    "Tu es un professeur expert. " +
                            "En te basant UNIQUEMENT sur le contenu pédagogique ci-dessous du cours '%s', " +
                            "génère %d questions de type '%s' pertinentes et précises.\n\n" +
                            "=== CONTENU PÉDAGOGIQUE ===\n%s\n===========================\n\n" +
                            "IMPORTANT :\n" +
                            "- Génère des questions sur les SUJETS traités dans ce contenu\n" +
                            "- NE génère PAS de questions sur la structure du cours (titre, niveau, module...)\n" +
                            "- Les questions doivent tester la compréhension des concepts enseignés\n" +
                            "- Réponds UNIQUEMENT avec un tableau JSON, sans texte avant ni après :\n" +
                            "[\n" +
                            "  {\n" +
                            "    \"texte\": \"Question concrète sur le contenu ?\",\n" +
                            "    \"type\": \"%s\",\n" +
                            "    \"choix\": [\"A. ...\", \"B. ...\", \"C. ...\", \"D. ...\"],\n" +
                            "    \"bonneReponse\": \"A. ...\",\n" +
                            "    \"explication\": \"Explication basée sur le contenu du cours\"\n" +
                            "  }\n" +
                            "]\n" +
                            "Pour 'vrai_faux' : choix=[\"Vrai\",\"Faux\"].\n" +
                            "Pour 'texte_libre' : choix=[], bonneReponse=réponse attendue.\n" +
                            "Pour 'choix_multiple' : plusieurs bonnes réponses séparées par virgule.",
                    cours.getTitre(), nbQuestions, typeQuestion, contexteStr, typeQuestion
            );

            return appellerIA(prompt);

        } catch (Exception e) {
            System.err.println("QuizIAService erreur : " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTHODE COMPATIBLE ancienne signature
    // ═══════════════════════════════════════════════════════════════
    public List<QuestionGeneree> genererQuizDepuisCours(
            String titreCours, String resumeCours, int nbQuestions, String typeQuestion) {

        String ctx = (resumeCours != null && !resumeCours.isBlank()) ? resumeCours : "Cours de " + titreCours;

        String prompt = String.format(
                "Tu es un professeur expert. " +
                        "En te basant UNIQUEMENT sur le contenu pédagogique ci-dessous du cours '%s', " +
                        "génère %d questions de type '%s'.\n\n" +
                        "=== CONTENU ===\n%s\n===============\n\n" +
                        "IMPORTANT : génère des questions sur les SUJETS traités, pas sur la structure du cours.\n" +
                        "Réponds UNIQUEMENT avec un tableau JSON :\n" +
                        "[\n" +
                        "  {\n" +
                        "    \"texte\": \"Question ?\",\n" +
                        "    \"type\": \"%s\",\n" +
                        "    \"choix\": [\"A. ...\", \"B. ...\", \"C. ...\", \"D. ...\"],\n" +
                        "    \"bonneReponse\": \"A. ...\",\n" +
                        "    \"explication\": \"Explication\"\n" +
                        "  }\n" +
                        "]",
                titreCours, nbQuestions, typeQuestion, ctx, typeQuestion
        );

        return appellerIA(prompt);
    }

    // ═══════════════════════════════════════════════════════════════
    // AUTO-ÉVALUATION
    // ═══════════════════════════════════════════════════════════════
    public List<QuestionGeneree> genererAutoEvaluation(
            String titreCours, String resumeCours, String niveauEtudiant) {

        String prompt = String.format(
                "Génère 5 questions d'auto-évaluation pour un étudiant de niveau '%s' " +
                        "sur le cours '%s'.\n\nContenu : %s\n\n" +
                        "Réponds UNIQUEMENT avec un tableau JSON :\n" +
                        "[\n" +
                        "  {\n" +
                        "    \"texte\": \"Question ?\",\n" +
                        "    \"type\": \"choix_unique\",\n" +
                        "    \"choix\": [\"A. ...\", \"B. ...\", \"C. ...\", \"D. ...\"],\n" +
                        "    \"bonneReponse\": \"B. ...\",\n" +
                        "    \"explication\": \"Explication\"\n" +
                        "  }\n" +
                        "]",
                niveauEtudiant, titreCours, resumeCours != null ? resumeCours : ""
        );

        return appellerIA(prompt);
    }

    // ═══════════════════════════════════════════════════════════════
    // APPEL HTTP GROQ
    // ═══════════════════════════════════════════════════════════════
    private List<QuestionGeneree> appellerIA(String prompt) {
        try {
            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", prompt);

            JsonArray messages = new JsonArray();
            messages.add(message);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", MODEL);
            requestBody.add("messages", messages);
            requestBody.addProperty("temperature", 0.7);
            requestBody.addProperty("max_tokens", 3000);

            String bodyStr = gson.toJson(requestBody);

            Request request = new Request.Builder()
                    .url(GROQ_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(bodyStr, MediaType.parse("application/json; charset=utf-8")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    System.err.println("Erreur Groq : " + response.code() + " — " + responseBody);
                    throw new RuntimeException("Erreur API Groq : " + response.code() + " " + responseBody);
                }

                JsonObject obj = JsonParser.parseString(responseBody).getAsJsonObject();
                String content = obj
                        .getAsJsonArray("choices").get(0)
                        .getAsJsonObject().get("message")
                        .getAsJsonObject().get("content").getAsString();

                content = content
                        .replaceAll("(?s)```json\\s*", "")
                        .replaceAll("(?s)```\\s*", "")
                        .trim();

                int start = content.indexOf('[');
                int end   = content.lastIndexOf(']');
                if (start >= 0 && end > start)
                    content = content.substring(start, end + 1);

                Type listType = new TypeToken<List<QuestionGeneree>>() {}.getType();
                return gson.fromJson(content, listType);
            }

        } catch (Exception e) {
            System.err.println("Erreur QuizIAService : " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // DTO
    // ═══════════════════════════════════════════════════════════════
    public static class QuestionGeneree {
        private String       texte;
        private String       type;
        private List<String> choix;
        private String       bonneReponse;
        private String       explication;

        public String       getTexte()        { return texte; }
        public String       getType()         { return type; }
        public List<String> getChoix()        { return choix; }
        public String       getBonneReponse() { return bonneReponse; }
        public String       getExplication()  { return explication; }
    }
}