package entities;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class Contenu {
    private static final String TYPED_RESOURCE_PREFIX = "__typed__:";
    private static final String TYPED_RESOURCE_SEPARATOR = "::";

    public static final List<String> AVAILABLE_TYPES =
            Arrays.asList("video", "pdf", "ppt", "texte", "quiz", "lien");

    private Integer id;
    private Cours cours;

    private String typeContenu;
    private String titre;
    private String urlContenu;
    private String description;

    private Integer duree;
    private int ordreAffichage;
    private boolean estPublic;

    private LocalDateTime dateAjout;
    private int nombreVues;

    private String format;
    private List<String> ressources;

    // Constructeur
    public Contenu() {
        this.dateAjout = LocalDateTime.now();
        this.typeContenu = "texte";
        this.ressources = new ArrayList<>();
        this.nombreVues = 0;
        this.ordreAffichage = 0;
        this.estPublic = false;
    }

    // Getters & Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Cours getCours() {
        return cours;
    }

    public void setCours(Cours cours) {
        this.cours = cours;
    }

    public String getTypeContenu() {
        return typeContenu;
    }

    public void setTypeContenu(String typeContenu) {
        this.typeContenu = typeContenu;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getUrlContenu() {
        return urlContenu;
    }

    public void setUrlContenu(String urlContenu) {
        this.urlContenu = urlContenu;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDuree() {
        return duree;
    }

    public void setDuree(Integer duree) {
        this.duree = duree;
    }

    public int getOrdreAffichage() {
        return ordreAffichage;
    }

    public void setOrdreAffichage(int ordreAffichage) {
        this.ordreAffichage = ordreAffichage;
    }

    public boolean isEstPublic() {
        return estPublic;
    }

    public void setEstPublic(boolean estPublic) {
        this.estPublic = estPublic;
    }

    public LocalDateTime getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDateTime dateAjout) {
        this.dateAjout = dateAjout;
    }

    public int getNombreVues() {
        return nombreVues;
    }

    public void setNombreVues(int nombreVues) {
        this.nombreVues = nombreVues;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public List<String> getRessources() {
        return ressources;
    }

    public void setRessources(List<String> ressources) {
        this.ressources = ressources;
    }

    // Méthodes utiles (comme Symfony mais simplifiées)

    public List<String> getTypeContenuList() {
        if (typeContenu == null || typeContenu.isEmpty()) {
            return Arrays.asList("texte");
        }
        return Arrays.stream(typeContenu.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
    }

    public void setTypeContenuFromArray(List<String> types) {
        this.typeContenu = String.join(",", types);
    }

    public boolean hasType(String type) {
        return getTypeContenuList().contains(type);
    }

    public List<String> getRessourcesForDisplay() {
        List<String> result = getVisibleRessources();

        if (result.isEmpty() && urlContenu != null && !urlContenu.isEmpty()) {
            result.add(urlContenu);
        }

        if (description != null && !description.isEmpty()) {
            result.add(description);
        }

        return result;
    }

    public List<String> getVisibleRessources() {
        if (ressources == null || ressources.isEmpty()) {
            return new ArrayList<>();
        }
        return ressources.stream()
                .filter(value -> !isTypedResourceEntry(value))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Map<String, String> getTypedRessources() {
        Map<String, String> typedResources = new LinkedHashMap<>();
        if (ressources == null || ressources.isEmpty()) {
            return typedResources;
        }

        for (String resource : ressources) {
            if (!isTypedResourceEntry(resource)) {
                continue;
            }

            String type = decodeTypedResourceType(resource);
            String value = decodeTypedResourceValue(resource);
            if (!type.isEmpty() && !value.isEmpty()) {
                typedResources.put(type, value);
            }
        }
        return typedResources;
    }

    public String getTypedResource(String type) {
        if (type == null || type.isBlank()) {
            return "";
        }
        return getTypedRessources().getOrDefault(type.trim().toLowerCase(Locale.ROOT), "");
    }

    public static String encodeTypedResource(String type, String value) {
        String normalizedType = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
        String normalizedValue = value == null ? "" : value.trim();
        if (normalizedType.isEmpty() || normalizedValue.isEmpty()) {
            return "";
        }
        return TYPED_RESOURCE_PREFIX + normalizedType + TYPED_RESOURCE_SEPARATOR + normalizedValue;
    }

    public static boolean isTypedResourceEntry(String value) {
        return value != null
                && value.startsWith(TYPED_RESOURCE_PREFIX)
                && value.contains(TYPED_RESOURCE_SEPARATOR);
    }

    public static String decodeTypedResourceType(String value) {
        if (!isTypedResourceEntry(value)) {
            return "";
        }
        String raw = value.substring(TYPED_RESOURCE_PREFIX.length());
        int separatorIndex = raw.indexOf(TYPED_RESOURCE_SEPARATOR);
        if (separatorIndex <= 0) {
            return "";
        }
        return raw.substring(0, separatorIndex).trim().toLowerCase(Locale.ROOT);
    }

    public static String decodeTypedResourceValue(String value) {
        if (!isTypedResourceEntry(value)) {
            return "";
        }
        String raw = value.substring(TYPED_RESOURCE_PREFIX.length());
        int separatorIndex = raw.indexOf(TYPED_RESOURCE_SEPARATOR);
        if (separatorIndex < 0 || separatorIndex + TYPED_RESOURCE_SEPARATOR.length() >= raw.length()) {
            return "";
        }
        return raw.substring(separatorIndex + TYPED_RESOURCE_SEPARATOR.length()).trim();
    }
}
