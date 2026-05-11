package gui;

import entities.Contenu;
import entities.Cours;
import entities.Etudiant;
import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import services.ContenuService;
import services.CoursService;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Locale;

public class StudentCourseContentController implements MainControllerAwareEtudiant {

    @FXML private Label topCourseLabel;
    @FXML private Label detailTitleLabel;
    @FXML private Label detailCodeLabel;
    @FXML private Label detailModuleLabel;
    @FXML private Label detailStatusLabel;
    @FXML private Label detailDescriptionLabel;
    @FXML private Label detailContentsCountLabel;
    @FXML private Label detailLevelLabel;
    @FXML private Label detailLanguageLabel;
    @FXML private Label detailScheduleLabel;
    @FXML private Label detailMetaLabel;
    @FXML private Label contentSummaryLabel;
    @FXML private Hyperlink firstResourceLink;
    @FXML private VBox contentListBox;
    @FXML private Label teacherSummaryLabel;
    @FXML private Label sideModuleLabel;
    @FXML private Label sideCodeLabel;
    @FXML private Label sideOrganizationLabel;

    private final ContenuService contenuService = new ContenuService();
    private final CoursService coursService = new CoursService();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy 'a' HH:mm", Locale.FRENCH);

    private MainLayoutEtudiantController mainController;
    private Cours cours;
    private List<Contenu> contenus = List.of();

    @FXML
    public void initialize() {
        renderEmptyState();
    }

    public void setCours(Cours cours) {
        this.cours = cours;
        loadCourseContent();
    }

    private void loadCourseContent() {
        if (cours == null) {
            renderEmptyState();
            return;
        }

        if (cours.getId() != null) {
            try {
                Cours freshCours = coursService.getById(cours.getId());
                if (freshCours != null) {
                    cours = freshCours;
                }
            } catch (SQLException ignored) {
            }
        }

        detailTitleLabel.setText(cours.getTitre());
        topCourseLabel.setText(cours.getTitre());
        detailCodeLabel.setText(valueOrDash(cours.getCodeCours()));
        detailModuleLabel.setText(cours.getModule() != null ? valueOrDash(cours.getModule().getTitreModule()) : "-");
        detailStatusLabel.setText(valueOrDash(cours.getStatut()).toUpperCase(Locale.ROOT));
        detailDescriptionLabel.setText(valueOrFallback(cours.getDescription(), "Aucune description pour ce cours."));
        detailLevelLabel.setText(valueOrDash(cours.getNiveau()));
        detailLanguageLabel.setText(valueOrDash(cours.getLangue()));
        detailScheduleLabel.setText(buildSchedule(cours));
        detailMetaLabel.setText("Credits: " + (cours.getCredits() != null ? cours.getCredits() : "-") + " | Cree le " + formatDateTime(cours.getDateCreation()));
        sideModuleLabel.setText("Afficher tous les membres du module " + (cours.getModule() != null ? valueOrDash(cours.getModule().getTitreModule()) : "du cours"));
        sideCodeLabel.setText("Afficher votre presence pour " + valueOrDash(cours.getCodeCours()));
        sideOrganizationLabel.setText("Afficher les outils et ressources pour " + valueOrDash(cours.getTitre()));
        teacherSummaryLabel.setText(buildTeacherSummary(cours));

        try {
            contenus = cours.getContenus() != null && !cours.getContenus().isEmpty()
                    ? cours.getContenus()
                    : (cours.getId() == null ? List.of() : contenuService.getByCoursId(cours.getId()));
            detailContentsCountLabel.setText(String.valueOf(contenus.size()));
            contentSummaryLabel.setText(contenus.isEmpty()
                    ? "Ce cours ne contient pas encore de ressources visibles pour l'etudiant."
                    : contenus.size() + " ressources disponibles dans cet espace de cours.");
            boolean canOpenFirst = !contenus.isEmpty() && hasResource(contenus.get(0));
            firstResourceLink.setVisible(canOpenFirst);
            firstResourceLink.setManaged(canOpenFirst);
            renderContents();
        } catch (SQLException e) {
            detailContentsCountLabel.setText("0");
            contentSummaryLabel.setText("Impossible de charger le contenu du cours.");
            firstResourceLink.setVisible(false);
            firstResourceLink.setManaged(false);
            renderEmptyContentState("Impossible de charger les contenus: " + e.getMessage());
        }
    }

    private void renderEmptyState() {
        detailTitleLabel.setText("Cours introuvable");
        topCourseLabel.setText("Cours");
        detailCodeLabel.setText("-");
        detailModuleLabel.setText("-");
        detailStatusLabel.setText("-");
        detailDescriptionLabel.setText("Aucun cours selectionne.");
        detailContentsCountLabel.setText("0");
        detailLevelLabel.setText("-");
        detailLanguageLabel.setText("-");
        detailScheduleLabel.setText("-");
        detailMetaLabel.setText("-");
        contentSummaryLabel.setText("Aucun contenu disponible.");
        teacherSummaryLabel.setText("Enseignant non renseigne");
        sideModuleLabel.setText("-");
        sideCodeLabel.setText("-");
        sideOrganizationLabel.setText("Ouvrez un cours depuis Mes cours.");
        firstResourceLink.setVisible(false);
        firstResourceLink.setManaged(false);
        renderEmptyContentState("Aucun contenu a afficher.");
    }

    private void renderContents() {
        contentListBox.getChildren().clear();
        if (contenus == null || contenus.isEmpty()) {
            renderEmptyContentState("Ce cours ne contient pas encore de ressources.");
            return;
        }
        for (int index = 0; index < contenus.size(); index++) {
            contentListBox.getChildren().add(buildContentCard(contenus.get(index), index + 1));
        }
    }

    private void renderEmptyContentState(String message) {
        VBox emptyCard = new VBox(8);
        emptyCard.getStyleClass().add("content-empty-card");

        Label title = new Label("Contenu indisponible");
        title.getStyleClass().add("content-empty-title");

        Label copy = new Label(message);
        copy.getStyleClass().add("content-empty-copy");
        copy.setWrapText(true);

        emptyCard.getChildren().setAll(title, copy);
        contentListBox.getChildren().setAll(emptyCard);
    }

    private VBox buildContentCard(Contenu contenu, int index) {
        VBox card = new VBox(0);
        card.getStyleClass().add("blackboard-content-card");

        String primaryType = primaryType(contenu);

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("blackboard-content-header");

        Label icon = new Label(iconFor(primaryType));
        icon.getStyleClass().addAll("course-content-icon", typeStyleClass(primaryType));

        VBox titleBox = new VBox(5);
        Label order = new Label(String.format("%02d", index));
        order.getStyleClass().add("course-content-order");

        Label title = new Label(valueOrFallback(contenu.getTitre(), "Ressource sans titre"));
        title.getStyleClass().add("course-content-title");
        title.setWrapText(true);

        FlowPane badges = new FlowPane(8, 8);
        for (String typeValue : contenu.getTypeContenuList()) {
            Label badge = new Label(formatType(typeValue));
            badge.getStyleClass().addAll("course-content-badge", typeBadgeClass(typeValue));
            badges.getChildren().add(badge);
        }

        Label fileInfo = new Label(fileMeta(contenu, primaryType));
        fileInfo.getStyleClass().add("course-content-summary");
        fileInfo.setWrapText(true);

        titleBox.getChildren().addAll(order, title, badges, fileInfo);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox metaBox = new VBox(4);
        metaBox.setAlignment(Pos.CENTER_RIGHT);
        Label views = new Label(contenu.getNombreVues() + " vues");
        views.getStyleClass().add("course-content-meta");
        Label duration = new Label(metaLabel(contenu));
        duration.getStyleClass().add("course-content-meta");
        metaBox.getChildren().addAll(views, duration);

        header.getChildren().addAll(icon, titleBox, spacer, metaBox);

        VBox body = new VBox(12);
        body.getStyleClass().add("blackboard-content-body");

        Label description = new Label(valueOrFallback(contenu.getDescription(), "Aucune description pour cette ressource."));
        description.getStyleClass().add("course-content-copy");
        description.setWrapText(true);

        VBox elementsBox = new VBox(8);
        elementsBox.getStyleClass().add("course-elements-box");
        for (String typeValue : contenu.getTypeContenuList()) {
            elementsBox.getChildren().add(buildContentElementRow(contenu, typeValue));
        }

        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label resourceLabel = new Label(buildResourceText(contenu));
        resourceLabel.getStyleClass().add("course-content-link");
        resourceLabel.setWrapText(true);
        HBox.setHgrow(resourceLabel, Priority.ALWAYS);
        footer.getChildren().add(resourceLabel);

        if (hasResource(contenu)) {
            Hyperlink openLink = new Hyperlink("Ouvrir");
            openLink.getStyleClass().addAll("course-open-link", "course-open-button");
            openLink.setOnAction(event -> openContent(contenu));
            footer.getChildren().add(openLink);
        }

        body.getChildren().addAll(description, elementsBox, footer);
        card.getChildren().addAll(header, body);
        return card;
    }

    private HBox buildContentElementRow(Contenu contenu, String typeValue) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("course-element-row");

        Label icon = new Label(iconFor(typeValue));
        icon.getStyleClass().addAll("course-element-icon", typeStyleClass(typeValue));

        VBox textBox = new VBox(3);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label title = new Label(elementTitle(typeValue));
        title.getStyleClass().add("course-element-title");

        Label meta = new Label(elementMeta(contenu, typeValue));
        meta.getStyleClass().add("course-element-meta");
        meta.setWrapText(true);

        textBox.getChildren().addAll(title, meta);

        row.getChildren().addAll(icon, textBox);

        if (isActionableElement(contenu, typeValue)) {
            Hyperlink action = new Hyperlink(actionLabel(typeValue));
            action.getStyleClass().add("course-element-action");
            action.setOnAction(event -> openContent(contenu, typeValue));
            row.getChildren().add(action);
        }

        return row;
    }

    @FXML
    private void handleOpenCourseAssistant() {
        StudentCourseAssistantDialog.open(cours);
    }

    @FXML
    private void handleBackToCourses() {
        if (mainController != null) {
            mainController.showMyCourses();
        }
    }

    @FXML
    private void handleOpenFirstResource() {
        if (contenus != null && !contenus.isEmpty()) {
            openContent(contenus.get(0));
        }
    }

    private void openContent(Contenu contenu) {
        openContent(contenu, null);
    }

    private void openContent(Contenu contenu, String requestedType) {
        String target = resolveTargetForType(contenu, requestedType);
        if (target == null || target.isBlank()) {
            showOpenError("Aucune ressource n'est associee a ce contenu.");
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            showOpenError("L'ouverture automatique n'est pas disponible sur cette machine.");
            return;
        }

        try {
            Desktop desktop = Desktop.getDesktop();
            if (isWebTarget(target)) {
                if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                    showOpenError("Le navigateur par defaut n'est pas disponible sur cette machine.");
                    return;
                }
                desktop.browse(URI.create(target.trim()));
                return;
            }

            Path resourcePath = resolveResourcePath(target);
            if (resourcePath == null) {
                showOpenError("Fichier introuvable: " + target);
                return;
            }

            if (!desktop.isSupported(Desktop.Action.OPEN)) {
                showOpenError("L'ouverture de fichiers n'est pas disponible sur cette machine.");
                return;
            }

            desktop.open(resourcePath.toFile());
        } catch (Exception e) {
            showOpenError("Impossible d'ouvrir la ressource: " + e.getMessage());
        }
    }

    private String buildResourceText(Contenu contenu) {
        if (contenu.getUrlContenu() != null && !contenu.getUrlContenu().isBlank()) {
            return contenu.getUrlContenu();
        }
        if (!contenu.getRessourcesForDisplay().isEmpty()) {
            return contenu.getRessourcesForDisplay().get(0);
        }
        return "Ressource integree a consulter dans la plateforme.";
    }

    private boolean hasResource(Contenu contenu) {
        return (contenu.getUrlContenu() != null && !contenu.getUrlContenu().isBlank())
                || !contenu.getRessourcesForDisplay().isEmpty()
                || !contenu.getTypedRessources().isEmpty();
    }

    private String resolveTargetForType(Contenu contenu, String requestedType) {
        String normalizedType = requestedType == null ? "" : requestedType.trim().toLowerCase(Locale.ROOT);
        if (!normalizedType.isEmpty()) {
            String typedTarget = contenu.getTypedResource(normalizedType);
            if (!typedTarget.isBlank()) {
                return typedTarget;
            }
        }

        String primaryTarget = contenu.getUrlContenu();
        if (primaryTarget != null && !primaryTarget.isBlank()) {
            return primaryTarget;
        }

        if (!contenu.getRessourcesForDisplay().isEmpty()) {
            return contenu.getRessourcesForDisplay().get(0);
        }

        if (!contenu.getTypedRessources().isEmpty()) {
            return contenu.getTypedRessources().values().iterator().next();
        }

        return null;
    }

    private boolean isWebTarget(String target) {
        String normalized = target == null ? "" : target.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("http://") || normalized.startsWith("https://");
    }

    private Path resolveResourcePath(String target) {
        if (target == null || target.isBlank()) {
            return null;
        }

        String normalizedTarget = target.trim();
        if (normalizedTarget.toLowerCase(Locale.ROOT).startsWith("file:/")) {
            try {
                Path fileUriPath = Paths.get(URI.create(normalizedTarget)).normalize();
                if (Files.exists(fileUriPath)) {
                    return fileUriPath;
                }
            } catch (Exception ignored) {
            }
        }

        Path path = Paths.get(normalizedTarget.replace("\\", "/"));
        for (Path candidate : buildResourceCandidates(path)) {
            if (Files.exists(candidate) && Files.isRegularFile(candidate)) {
                return candidate.normalize();
            }
        }

        return null;
    }

    private List<Path> buildResourceCandidates(Path path) {
        LinkedHashSet<Path> candidates = new LinkedHashSet<>();

        if (path.isAbsolute()) {
            candidates.add(path.normalize());
        }

        String filename = path.getFileName() != null ? path.getFileName().toString() : "";
        Path trimmedUploadsPath = trimLeadingUploads(path);

        for (Path baseDir : collectLookupDirectories()) {
            candidates.add(baseDir.resolve(path).normalize());

            if (trimmedUploadsPath != null) {
                candidates.add(baseDir.resolve(trimmedUploadsPath).normalize());
            }

            if (!filename.isBlank()) {
                candidates.add(baseDir.resolve(filename).normalize());
                candidates.add(baseDir.resolve("uploads").resolve(filename).normalize());
                candidates.add(baseDir.resolve("public").resolve("uploads").resolve(filename).normalize());
                candidates.add(baseDir.resolve("src").resolve("main").resolve("resources").resolve("uploads").resolve(filename).normalize());
                candidates.add(baseDir.resolve("target").resolve("classes").resolve("uploads").resolve(filename).normalize());
            }
        }

        return List.copyOf(candidates);
    }

    private List<Path> collectLookupDirectories() {
        LinkedHashSet<Path> directories = new LinkedHashSet<>();
        addDirectoryAndParents(directories, Paths.get("").toAbsolutePath().normalize(), 6);
        addDirectoryAndParents(directories, resolveClassLocationDirectory(), 6);
        return List.copyOf(directories);
    }

    private void addDirectoryAndParents(LinkedHashSet<Path> directories, Path start, int maxLevels) {
        Path current = start;
        int level = 0;
        while (current != null && level < maxLevels) {
            directories.add(current.normalize());
            current = current.getParent();
            level++;
        }
    }

    private Path resolveClassLocationDirectory() {
        try {
            URI location = StudentCourseContentController.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path classLocation = Paths.get(location).toAbsolutePath().normalize();
            return Files.isDirectory(classLocation) ? classLocation : classLocation.getParent();
        } catch (Exception ignored) {
            return null;
        }
    }

    private Path trimLeadingUploads(Path path) {
        if (path == null || path.getNameCount() < 2) {
            return null;
        }
        String firstSegment = path.getName(0).toString();
        if (!"uploads".equalsIgnoreCase(firstSegment)) {
            return null;
        }
        return path.subpath(1, path.getNameCount());
    }

    private void showOpenError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ouverture impossible");
        alert.setHeaderText("La ressource n'a pas pu etre ouverte");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isActionableElement(Contenu contenu, String typeValue) {
        return switch (typeValue == null ? "" : typeValue.toLowerCase(Locale.ROOT)) {
            case "video", "pdf", "ppt", "lien", "quiz" -> hasResource(contenu);
            default -> false;
        };
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

    private String iconFor(String type) {
        return switch (type == null ? "" : type.toLowerCase(Locale.ROOT)) {
            case "video" -> "VID";
            case "pdf" -> "PDF";
            case "ppt" -> "PPT";
            case "quiz" -> "?";
            case "lien" -> "URL";
            default -> "TXT";
        };
    }

    private String typeStyleClass(String type) {
        return "type-" + (type == null || type.isBlank() ? "texte" : type.toLowerCase(Locale.ROOT));
    }

    private String typeBadgeClass(String type) {
        return "badge-" + (type == null || type.isBlank() ? "texte" : type.toLowerCase(Locale.ROOT));
    }

    private String formatType(String type) {
        if (type == null || type.isBlank()) {
            return "Contenu";
        }
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "video" -> "Video";
            case "pdf" -> "Document PDF";
            case "ppt" -> "Presentation";
            case "quiz" -> "Quiz";
            case "lien" -> "Lien externe";
            case "texte" -> "Lecture";
            default -> type.substring(0, 1).toUpperCase(Locale.ROOT) + type.substring(1).toLowerCase(Locale.ROOT);
        };
    }

    private String typeSummary(Contenu contenu) {
        String primaryType = primaryType(contenu);
        return switch (primaryType) {
            case "video" -> "Contenu video structure avec duree et acces direct.";
            case "pdf" -> "Document PDF a consulter ou telecharger.";
            case "ppt" -> "Support de presentation pour le cours.";
            case "quiz" -> "Activite interactive ou evaluation associee.";
            case "lien" -> "Ressource externe referencee dans le cours.";
            default -> "Contenu textuel organise pour la lecture de l'etudiant.";
        };
    }

    private String fileMeta(Contenu contenu, String primaryType) {
        String resource = buildResourceText(contenu);
        String shortName = resource;
        int slashIndex = Math.max(resource.lastIndexOf('/'), resource.lastIndexOf('\\'));
        if (slashIndex >= 0 && slashIndex < resource.length() - 1) {
            shortName = resource.substring(slashIndex + 1);
        }
        if (shortName.length() > 48) {
            shortName = shortName.substring(0, 48) + "...";
        }
        return formatType(primaryType) + " | " + shortName;
    }

    private String metaLabel(Contenu contenu) {
        if (contenu.getDuree() != null) {
            return contenu.getDuree() + " min";
        }
        if (contenu.getFormat() != null && !contenu.getFormat().isBlank()) {
            return contenu.getFormat();
        }
        return formatType(primaryType(contenu));
    }

    private String elementTitle(String typeValue) {
        return switch (typeValue == null ? "" : typeValue.toLowerCase(Locale.ROOT)) {
            case "video" -> "Video du contenu";
            case "pdf" -> "Document PDF";
            case "ppt" -> "Presentation";
            case "quiz" -> "Quiz associe";
            case "lien" -> "Lien externe";
            default -> "Texte du contenu";
        };
    }

    private String elementMeta(Contenu contenu, String typeValue) {
        return switch (typeValue == null ? "" : typeValue.toLowerCase(Locale.ROOT)) {
            case "video" -> (contenu.getDuree() != null ? "Duree " + contenu.getDuree() + " min" : "Video pedagogique disponible");
            case "pdf" -> "Fichier documentaire a consulter ou telecharger";
            case "ppt" -> "Support de presentation du cours";
            case "quiz" -> "Activite interactive liee a ce contenu";
            case "lien" -> valueOrFallback(contenu.getUrlContenu(), "Lien externe disponible");
            default -> valueOrFallback(contenu.getDescription(), "Lecture ecrite associee a ce contenu");
        };
    }

    private String actionLabel(String typeValue) {
        return switch (typeValue == null ? "" : typeValue.toLowerCase(Locale.ROOT)) {
            case "video" -> "Lire";
            case "pdf" -> "Ouvrir PDF";
            case "ppt" -> "Ouvrir";
            case "quiz" -> "Acceder";
            case "lien" -> "Visiter";
            default -> "Ouvrir";
        };
    }

    private String buildSchedule(Cours cours) {
        String debut = formatDate(cours.getDateDebut());
        String fin = formatDate(cours.getDateFin());
        if (!"-".equals(debut) && !"-".equals(fin)) {
            return debut + " - " + fin;
        }
        if (!"-".equals(debut)) {
            return "Debut " + debut;
        }
        if (!"-".equals(fin)) {
            return "Fin " + fin;
        }
        return "-";
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : DATE_FORMATTER.format(date);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "-" : DATE_TIME_FORMATTER.format(dateTime);
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String buildTeacherSummary(Cours cours) {
        if (cours.getEnseignants() == null || cours.getEnseignants().isEmpty()) {
            return "Enseignant non renseigne";
        }
        return cours.getEnseignants().stream()
                .map(enseignant -> valueOrFallback(enseignant.getNomComplet(), "Enseignant"))
                .reduce((left, right) -> left + ", " + right)
                .orElse("Enseignant non renseigne");
    }

    @Override
    public void setMainController(MainLayoutEtudiantController controller) {
        mainController = controller;
    }
}
