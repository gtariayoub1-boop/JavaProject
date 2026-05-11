package entities;



import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Module {

    private Integer id;
    private String titreModule;
    private String description;
    private int ordreAffichage;
    private String objectifsApprentissage;
    private Integer dureeEstimeeHeures;
    private LocalDateTime datePublication;
    private String statut;
    private List<Cours> cours;
    private List<String> ressourcesComplementaires;

    // Constructeur
    public Module() {
        this.cours = new ArrayList<>();
        this.ressourcesComplementaires = new ArrayList<>();
        this.statut = "brouillon";
    }

    // Getters & Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitreModule() {
        return titreModule;
    }

    public void setTitreModule(String titreModule) {
        this.titreModule = titreModule;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrdreAffichage() {
        return ordreAffichage;
    }

    public void setOrdreAffichage(int ordreAffichage) {
        this.ordreAffichage = ordreAffichage;
    }

    public String getObjectifsApprentissage() {
        return objectifsApprentissage;
    }

    public void setObjectifsApprentissage(String objectifsApprentissage) {
        this.objectifsApprentissage = objectifsApprentissage;
    }

    public Integer getDureeEstimeeHeures() {
        return dureeEstimeeHeures;
    }

    public void setDureeEstimeeHeures(Integer dureeEstimeeHeures) {
        this.dureeEstimeeHeures = dureeEstimeeHeures;
    }

    public LocalDateTime getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDateTime datePublication) {
        this.datePublication = datePublication;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public List<String> getRessourcesComplementaires() {
        return ressourcesComplementaires;
    }

    public void setRessourcesComplementaires(List<String> ressourcesComplementaires) {
        this.ressourcesComplementaires = ressourcesComplementaires;
    }

    public List<Cours> getCours() {
        return cours;
    }

    public void setCours(List<Cours> cours) {
        this.cours = cours;
    }

    // Méthodes pour gérer la relation avec Cours

    public void addCours(Cours c) {
        this.cours.add(c);
        c.setModule(this);
    }

    public void removeCours(Cours c) {
        this.cours.remove(c);
        c.setModule(null);
    }
}