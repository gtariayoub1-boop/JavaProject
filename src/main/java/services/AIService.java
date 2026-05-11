package services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class AIService {
    
    // L'utilisateur peut y placer sa propre clé d'API Gemini.
    private static final String API_KEY = "AIzaSyA4ROVhrcqWZOUPRVea0SnedQKS9a5eYf0";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    private HttpClient client;

    public AIService() {
        this.client = HttpClient.newHttpClient();
    }

    private String callGeminiApi(String prompt) {
        if (API_KEY.equals("YOUR_GEMINI_API_KEY_HERE") || API_KEY.isBlank()) {
            System.err.println("Gemini API Key missing. Returning fallback.");
            return null;
        }
        
        try {
            // Echapement basique pour du JSON
            String escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", " ");
            String requestBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + escapedPrompt + "\"}]}]}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String body = response.body();
                // Extraction naïve du texte (évite l'ajout d'une librairie externe type Jackson/Gson)
                String searchStr = "\"text\": \"";
                int startIndex = body.indexOf(searchStr);
                if (startIndex != -1) {
                    startIndex += searchStr.length();
                    int endIndex = body.indexOf("\"\n", startIndex);
                    if (endIndex == -1) endIndex = body.indexOf("\"", startIndex);
                    
                    if (endIndex != -1) {
                        String result = body.substring(startIndex, endIndex);
                        return result.replace("\\n", "\n").replace("\\\"", "\"").trim();
                    }
                }
            } else {
                System.err.println("Gemini API Error: " + response.statusCode() + " " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> genererSuggestions(String messageRecu) {
        List<String> suggestions = new ArrayList<>();
        
        String prompt = "Donne exactement 3 suggestions de reponses courtes (max 8 mots) et polies pour ce message. Sépare-les par un tiret au d\u00E9but de ligne. Message : " + messageRecu;
        
        String apiResponse = callGeminiApi(prompt);
        
        if (apiResponse != null && !apiResponse.isBlank()) {
            String[] lines = apiResponse.split("\\n");
            for (String line : lines) {
                String cleanLine = line.trim();
                // Retirer un éventuel tiret et astérisques markdown
                cleanLine = cleanLine.replaceAll("^[-*•]+", "").trim();
                cleanLine = cleanLine.replace("**", "").trim();
                
                if (!cleanLine.isBlank()) {
                    suggestions.add(cleanLine);
                }
                if(suggestions.size() == 3) break;
            }
        }
        
        // Mode Fallback (si pas de clé API ou requete echouée)
        if (suggestions.isEmpty()) {
            suggestions.add("C'est not\u00E9, merci !");
            suggestions.add("Je m'en occupe.");
            suggestions.add("D'accord, \u00E0 bient\u00F4t.");
        }
        
        return suggestions;
    }

    public boolean estContenuInapproprie(String texte) {
        String prompt = "Analyse ce texte. Reponds UNIQUEMENT par 'OUI' s'il contient des mots vulgaires, du harcelement, ou apparent\u00E9 au spam evident. Sinon reponds 'NON'. Ne justifie pas. Texte : " + texte;
        
        String apiResponse = callGeminiApi(prompt);
        
        if (apiResponse != null) {
            if (apiResponse.toUpperCase().contains("OUI")) {
                return true;
            } else if (apiResponse.toUpperCase().contains("NON")) {
                return false;
            }
        }
        
        // Mode Fallback basique
        String[] motsInterdits = {"insulte", "spam", "arnaque", "idiot", "connard", "putain", "merde", "chier", "salope", "click ici"};
        String lowerTexte = texte.toLowerCase();
        for (String mot : motsInterdits) {
            if (lowerTexte.contains(mot)) {
                return true;
            }
        }
        return false;
    }
}
