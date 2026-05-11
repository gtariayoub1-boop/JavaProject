package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Quiz {

    private Long id;
    private String titre;
    private String description;
    private String typeQuiz;
    private Integer dureeMinutes;
    private Integer nombreTentativesAutorisees;
    private Double difficulteMoyenne;
    private String instructions;
    private LocalDateTime dateCreation;
    private LocalDateTime dateDebutDisponibilite;
    private LocalDateTime dateFinDisponibilite;
    private String afficherCorrectionApres;
    private Utilisateur createur;
    private Integer idCours;
    private List<Question> questions = new ArrayList<>();

    public Quiz() {
        this.dateCreation = LocalDateTime.now();
    }

    public Quiz(String titre, Utilisateur createur) {
        this.titre = titre;
        this.createur = createur;
        this.dateCreation = LocalDateTime.now();
    }

    public Long getId()              { return id; }
    public void setId(Long id)       { this.id = id; }

    public String getTitre()         { return titre; }
    public void setTitre(String t)   { this.titre = t; }

    public String getDescription()           { return description; }
    public void setDescription(String d)     { this.description = d; }

    public String getTypeQuiz()              { return typeQuiz; }
    public void setTypeQuiz(String t)        { this.typeQuiz = t; }

    public Integer getDureeMinutes()         { return dureeMinutes; }
    public void setDureeMinutes(Integer d)   { this.dureeMinutes = d; }

    public Integer getNombreTentativesAutorisees()      { return nombreTentativesAutorisees; }
    public void setNombreTentativesAutorisees(Integer n){ this.nombreTentativesAutorisees = n; }

    public Double getDifficulteMoyenne()         { return difficulteMoyenne; }
    public void setDifficulteMoyenne(Double d)   { this.difficulteMoyenne = d; }

    public String getInstructions()          { return instructions; }
    public void setInstructions(String i)    { this.instructions = i; }

    public LocalDateTime getDateCreation()           { return dateCreation; }
    public void setDateCreation(LocalDateTime d)     { this.dateCreation = d; }

    public LocalDateTime getDateDebutDisponibilite()         { return dateDebutDisponibilite; }
    public void setDateDebutDisponibilite(LocalDateTime d)   { this.dateDebutDisponibilite = d; }

    public LocalDateTime getDateFinDisponibilite()           { return dateFinDisponibilite; }
    public void setDateFinDisponibilite(LocalDateTime d)     { this.dateFinDisponibilite = d; }

    public String getAfficherCorrectionApres()           { return afficherCorrectionApres; }
    public void setAfficherCorrectionApres(String s)     { this.afficherCorrectionApres = s; }

    public Utilisateur getCreateur()             { return createur; }
    public void setCreateur(Utilisateur u)       { this.createur = u; }

    public Integer getIdCours()          { return idCours; }
    public void setIdCours(Integer i)    { this.idCours = i; }

    public List<Question> getQuestions() { return questions; }

    public void addQuestion(Question q) {
        if (!questions.contains(q)) { questions.add(q); q.setQuiz(this); }
    }
    public void removeQuestion(Question q) {
        if (questions.remove(q)) q.setQuiz(null);
    }

    @Override
    public String toString() {
        return "Quiz{id=" + id + ", titre='" + titre + "', type='" + typeQuiz + "'}";
    }
}