package entities;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Cours {

    private Integer id;
    private String codeCours;
    private String titre;
    private String description;

    private List<Enseignant> enseignants;
    private List<Etudiant> etudiants;

    private Module module;

    private String niveau;
    private Integer credits;
    private String langue;

    private LocalDateTime dateCreation;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    private String statut;
    private String imageCoursUrl;

    private List<String> prerequis;
    private List<Contenu> contenus;

    // Constructeur
    public Cours() {
        this.dateCreation = LocalDateTime.now();
        this.enseignants = new ArrayList<>();
        this.etudiants = new ArrayList<>();
        this.contenus = new ArrayList<>();
        this.prerequis = new ArrayList<>();
        this.statut = "brouillon";
    }

    // Getters & Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodeCours() {
        return codeCours;
    }

    public void setCodeCours(String codeCours) {
        this.codeCours = codeCours;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Enseignant> getEnseignants() {
        return enseignants;
    }

    public void setEnseignants(List<Enseignant> enseignants) {
        this.enseignants = enseignants;
    }

    public List<Etudiant> getEtudiants() {
        return etudiants;
    }

    public void setEtudiants(List<Etudiant> etudiants) {
        this.etudiants = etudiants;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getImageCoursUrl() {
        return imageCoursUrl;
    }

    public void setImageCoursUrl(String imageCoursUrl) {
        this.imageCoursUrl = imageCoursUrl;
    }

    public List<String> getPrerequis() {
        return prerequis;
    }

    public void setPrerequis(List<String> prerequis) {
        this.prerequis = prerequis;
    }

    public List<Contenu> getContenus() {
        return contenus;
    }

    public void setContenus(List<Contenu> contenus) {
        this.contenus = contenus;
    }

    // Méthodes relationnelles

    public void addEnseignant(Enseignant e) {
        this.enseignants.add(e);
    }

    public void removeEnseignant(Enseignant e) {
        this.enseignants.remove(e);
    }

    public void addEtudiant(Etudiant e) {
        this.etudiants.add(e);
    }

    public void removeEtudiant(Etudiant e) {
        this.etudiants.remove(e);
    }

    public void addContenu(Contenu c) {
        this.contenus.add(c);
        c.setCours(this);
    }

    public void removeContenu(Contenu c) {
        this.contenus.remove(c);
        c.setCours(null);
    }
}