package services;

import entities.Contenu;
import entities.Cours;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import utils.AppSecrets;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class StudentCourseAssistantService {

    private static final String OUT_OF_SCOPE_MESSAGE =
            "Je peux repondre uniquement au sujet du cours selectionne et de ses contenus.";
    private static final int MAX_CONTEXT_CHARS = 18000;
    private static final int MAX_EXTRACT_PER_RESOURCE = 5000;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final CoursService coursService = new CoursService();
    private final ContenuService contenuService = new ContenuService();
    private final Map<Integer, String> contextCache = new HashMap<>();

    public List<Cours> getEnrolledCourses(long studentId) throws SQLException {
        return coursService.getByStudentId(studentId);
    }

    public String answer(String question, Cours selectedCourse) throws IOException, InterruptedException, SQLException {
        Optional<String> localReply = tryLocalReply(question, selectedCourse);
        if (localReply.isPresent()) {
            return localReply.get();
        }

        if (!isInScope(question)) {
            return OUT_OF_SCOPE_MESSAGE;
        }

        if (selectedCourse == null || selectedCourse.getId() == null) {
            return "Aucun cours selectionne pour l'assistant.";
        }

        String apiKey = resolveGroqApiKey();
        if (apiKey.isBlank()) {
            throw new IOException("Groq API key is missing. Configure groq.apiKey or groq.api.key in app-secrets.properties or environment variables.");
        }

        Cours freshCourse = coursService.getById(selectedCourse.getId());
        if (freshCourse == null) {
            return "Le cours selectionne est introuvable.";
        }

        String endpoint = defaultIfBlank(AppSecrets.get("groq.endpoint"), "https://api.groq.com/openai/v1/chat/completions");
        String model = defaultIfBlank(AppSecrets.get("groq.model"), "llama-3.3-70b-versatile");
        String context = buildCourseContext(freshCourse);
        String payload = buildPayload(model, question, freshCourse, context);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            return "Le service Groq a refuse la cle API configuree. Remplacez groq.apiKey par une cle valide.";
        }
        if (response.statusCode() >= 400) {
            throw new IOException("Groq API error " + response.statusCode() + ": " + response.body());
        }

        return extractAssistantMessage(response.body());
    }

    private String resolveGroqApiKey() {
        String apiKey = AppSecrets.get("groq.apiKey");
        if (!apiKey.isBlank()) {
            return apiKey;
        }
        return AppSecrets.get("groq.api.key");
    }

    private Optional<String> tryLocalReply(String question, Cours selectedCourse) {
        if (question == null) {
            return Optional.empty();
        }

        String normalized = question.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return Optional.of("Posez une question sur le cours selectionne.");
        }
        if (normalized.matches("^(bonjour|salut|hello|hi|hey)[!. ]*$")) {
            String courseName = selectedCourse != null ? selectedCourse.getTitre() : "le cours selectionne";
            return Optional.of("Bonjour. Je peux repondre sur " + courseName + " et expliquer ses contenus.");
        }
        return Optional.empty();
    }

    private boolean isInScope(String question) {
        if (question == null || question.isBlank()) {
            return false;
        }

        String normalized = question.toLowerCase(Locale.ROOT);
        List<String> blocked = List.of("autre cours", "hors cours", "politique", "actualite", "meteo");
        for (String value : blocked) {
            if (normalized.contains(value)) {
                return false;
            }
        }
        return true;
    }

    private String buildCourseContext(Cours course) throws SQLException {
        Integer courseId = course.getId();
        if (courseId != null && contextCache.containsKey(courseId)) {
            return contextCache.get(courseId);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Cours: ").append(nullToDash(course.getTitre())).append('\n');
        builder.append("Code: ").append(nullToDash(course.getCodeCours())).append('\n');
        builder.append("Module: ").append(course.getModule() != null ? nullToDash(course.getModule().getTitreModule()) : "-").append('\n');
        builder.append("Description du cours: ").append(nullToDash(course.getDescription())).append("\n\n");

        List<Contenu> contenus = course.getContenus() != null && !course.getContenus().isEmpty()
                ? course.getContenus()
                : (courseId == null ? List.of() : contenuService.getByCoursId(courseId));

        for (int i = 0; i < contenus.size(); i++) {
            Contenu contenu = contenus.get(i);
            builder.append("Contenu ").append(i + 1).append(":\n");
            builder.append("- titre: ").append(nullToDash(contenu.getTitre())).append('\n');
            builder.append("- types: ").append(String.join(", ", contenu.getTypeContenuList())).append('\n');
            builder.append("- description: ").append(nullToDash(contenu.getDescription())).append('\n');
            builder.append("- format: ").append(nullToDash(contenu.getFormat())).append('\n');
            builder.append("- ressource principale: ").append(nullToDash(contenu.getUrlContenu())).append('\n');
            if (!contenu.getVisibleRessources().isEmpty()) {
                builder.append("- ressources: ").append(String.join(", ", contenu.getVisibleRessources())).append('\n');
            }
            String extracted = extractReadableContent(contenu);
            if (!extracted.isBlank()) {
                builder.append("- texte extrait:\n").append(extracted).append('\n');
                builder.append("- consigne: expliquer a partir de la description et du texte extrait de ce contenu.\n");
            } else {
                builder.append("- consigne: se limiter a la description si aucun texte exploitable n'a pu etre extrait.\n");
            }
            builder.append('\n');

            if (builder.length() > MAX_CONTEXT_CHARS) {
                builder.append("Le contexte est tronque apres les ressources principales du cours.\n");
                break;
            }
        }

        String context = builder.length() > MAX_CONTEXT_CHARS
                ? builder.substring(0, MAX_CONTEXT_CHARS)
                : builder.toString();

        if (courseId != null) {
            contextCache.put(courseId, context);
        }
        return context;
    }

    private String extractReadableContent(Contenu contenu) {
        try {
            List<Path> resourcePaths = resolveResourcePaths(contenu);
            if (resourcePaths.isEmpty()) {
                return "";
            }

            StringBuilder builder = new StringBuilder();
            for (Path resourcePath : resourcePaths) {
                String type = readableTypeFor(contenu, resourcePath);
                if (type.isBlank()) {
                    continue;
                }

                String extracted = switch (type) {
                    case "pdf" -> truncate(extractPdf(resourcePath), MAX_EXTRACT_PER_RESOURCE);
                    case "ppt" -> truncate(extractPresentation(resourcePath), MAX_EXTRACT_PER_RESOURCE);
                    case "texte" -> truncate(readPlainText(resourcePath), MAX_EXTRACT_PER_RESOURCE);
                    default -> "";
                };

                if (extracted.isBlank()) {
                    continue;
                }

                if (builder.length() > 0) {
                    builder.append("\n\n");
                }
                builder.append("Fichier ").append(resourcePath.getFileName()).append(" (").append(type).append("):\n");
                builder.append(extracted);

                if (builder.length() >= MAX_CONTEXT_CHARS) {
                    break;
                }
            }

            return builder.toString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private List<Path> resolveResourcePaths(Contenu contenu) {
        Set<Path> paths = new LinkedHashSet<>();
        addResolvedPath(paths, contenu.getUrlContenu());
        for (String resource : contenu.getVisibleRessources()) {
            addResolvedPath(paths, resource);
        }
        for (String resource : contenu.getTypedRessources().values()) {
            addResolvedPath(paths, resource);
        }
        return new ArrayList<>(paths);
    }

    private void addResolvedPath(Set<Path> collector, String target) {
        Path path = resolveResourcePath(target);
        if (path != null) {
            collector.add(path);
        }
    }

    private Path resolveResourcePath(String target) {
        if (target == null || target.isBlank()) {
            return null;
        }
        if (target.startsWith("http://") || target.startsWith("https://")) {
            return null;
        }

        Path path = Paths.get(target.trim());
        if (path.isAbsolute() && Files.exists(path)) {
            return path;
        }

        Path projectRelative = Paths.get("").toAbsolutePath().resolve(path).normalize();
        if (Files.exists(projectRelative)) {
            return projectRelative;
        }

        Path uploadsRelative = Paths.get("").toAbsolutePath().resolve("uploads").resolve(path).normalize();
        if (Files.exists(uploadsRelative)) {
            return uploadsRelative;
        }

        return null;
    }

    private String readableTypeFor(Contenu contenu, Path resourcePath) {
        String inferredType = inferTypeFromPath(resourcePath);
        if (!inferredType.isBlank()) {
            return inferredType;
        }

        String primaryType = primaryType(contenu);
        return switch (primaryType) {
            case "pdf", "ppt", "texte" -> primaryType;
            default -> "";
        };
    }

    private String inferTypeFromPath(Path resourcePath) {
        String fileName = resourcePath.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".pdf")) {
            return "pdf";
        }
        if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
            return "ppt";
        }
        if (fileName.endsWith(".txt") || fileName.endsWith(".md") || fileName.endsWith(".csv")) {
            return "texte";
        }
        return "";
    }

    private String extractPdf(Path path) throws IOException {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractPresentation(Path path) throws IOException {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".pptx")) {
            try (InputStream inputStream = Files.newInputStream(path);
                 XMLSlideShow slideShow = new XMLSlideShow(inputStream)) {
                StringBuilder builder = new StringBuilder();
                for (XSLFSlide slide : slideShow.getSlides()) {
                    for (XSLFShape shape : slide.getShapes()) {
                        if (shape instanceof XSLFTextShape textShape) {
                            builder.append(textShape.getText()).append('\n');
                        }
                    }
                }
                return builder.toString();
            }
        }

        try (InputStream inputStream = Files.newInputStream(path);
             HSLFSlideShow slideShow = new HSLFSlideShow(inputStream)) {
            StringBuilder builder = new StringBuilder();
            for (HSLFSlide slide : slideShow.getSlides()) {
                builder.append(slide.getTitle()).append('\n');
                for (HSLFShape shape : slide.getShapes()) {
                    if (shape instanceof HSLFTextShape textShape) {
                        builder.append(textShape.getText()).append('\n');
                    }
                }
            }
            return builder.toString();
        }
    }

    private String readPlainText(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private String buildPayload(String model, String question, Cours course, String context) {
        String systemPrompt = """
                Tu es l'assistant pedagogique d'un etudiant dans une plateforme d'apprentissage.
                Regles strictes:
                1. Reponds uniquement a propos du cours selectionne.
                2. Base tes reponses uniquement sur le contexte fourni et le contenu du cours.
                3. Si l'information n'est pas dans le cours, dis clairement que ce n'est pas present dans les contenus disponibles.
                4. Explique de facon simple, pedagogique et structuree.
                5. N'invente ni chapitres, ni definitions, ni fichiers inexistants.
                6. Si l'etudiant demande un autre cours, refuse poliment et demande de changer de cours dans l'assistant.
                7. Quand un PDF, PPT ou fichier texte est fourni, utilise d'abord le texte extrait de ce fichier.
                8. Relie toujours ton explication a la description du contenu et a la description du cours quand elles existent.
                9. Quand c'est utile, cite le titre du contenu et le nom du fichier utilise pour repondre.
                """;

        String userPrompt = "Cours selectionne: " + nullToDash(course.getTitre()) + "\n\n"
                + "Contexte du cours:\n" + context + "\n\n"
                + "Question de l'etudiant:\n" + question;

        return "{"
                + "\"model\":\"" + escapeJson(model) + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + escapeJson(systemPrompt) + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(userPrompt) + "\"}"
                + "],"
                + "\"temperature\":0.2"
                + "}";
    }

    private String extractAssistantMessage(String responseBody) {
        String marker = "\"content\":";
        int contentIndex = responseBody.indexOf(marker);
        if (contentIndex < 0) {
            return "L'assistant a retourne une reponse inattendue.";
        }

        int quoteStart = responseBody.indexOf('"', contentIndex + marker.length());
        if (quoteStart < 0) {
            return "L'assistant a retourne une reponse inattendue.";
        }

        StringBuilder builder = new StringBuilder();
        boolean escaping = false;
        for (int i = quoteStart + 1; i < responseBody.length(); i++) {
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

    private String primaryType(Contenu contenu) {
        List<String> types = contenu.getTypeContenuList();
        if (types.contains("video")) {
            return "video";
        }
        if (types.contains("pdf")) {
            return "pdf";
        }
        if (types.contains("ppt")) {
            return "ppt";
        }
        if (types.contains("quiz")) {
            return "quiz";
        }
        if (types.contains("lien")) {
            return "lien";
        }
        return types.isEmpty() ? "texte" : types.get(0);
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace("\u0000", "").replaceAll("\\s+", " ").trim();
        return normalized.length() > max ? normalized.substring(0, max) + "..." : normalized;
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
