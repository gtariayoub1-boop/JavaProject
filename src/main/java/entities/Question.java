package entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titre", length = 255)
    private String titre;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_createur", referencedColumnName = "id")
    private Utilisateur createur;

    @Column(name = "id_cours")
    private Integer idCours;

    @Column(name = "type_quiz", length = 255)
    private String typeQuiz;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "ordre_affichage")
    private Integer ordreAffichage;

    @Column(name = "texte", columnDefinition = "TEXT")
    private String texte;

    @Column(name = "points")
    private Integer points;

    @Column(name = "type_question", length = 255)
    private String typeQuestion;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata; // stocké en JSON string, désérialisé au besoin

    @Column(name = "explication_reponse", columnDefinition = "TEXT")
    private String explicationReponse;

    // ─── Relation ManyToOne vers Quiz ─────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", referencedColumnName = "id", nullable = false)
    private Quiz quiz;

    // ─── Relation OneToMany vers Reponse ──────────────────────────

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reponse> reponses = new ArrayList<>();

    // ─── Constructeurs ────────────────────────────────────────────

    public Question() {
        this.dateCreation = LocalDateTime.now();
    }

    public Question(String texte, String typeQuestion, Quiz quiz) {
        this.texte = texte;
        this.typeQuestion = typeQuestion;
        this.quiz = quiz;
        this.dateCreation = LocalDateTime.now();
    }

    // ─── Getters & Setters ────────────────────────────────────────

    public Long getId() { return id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Utilisateur getCreateur() { return createur; }
    public void setCreateur(Utilisateur createur) { this.createur = createur; }

    public Integer getIdCours() { return idCours; }
    public void setIdCours(Integer idCours) { this.idCours = idCours; }

    public String getTypeQuiz() { return typeQuiz; }
    public void setTypeQuiz(String typeQuiz) { this.typeQuiz = typeQuiz; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Integer getOrdreAffichage() { return ordreAffichage; }
    public void setOrdreAffichage(Integer ordreAffichage) { this.ordreAffichage = ordreAffichage; }

    public String getTexte() { return texte; }
    public void setTexte(String texte) { this.texte = texte; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public String getTypeQuestion() { return typeQuestion; }
    public void setTypeQuestion(String typeQuestion) { this.typeQuestion = typeQuestion; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getExplicationReponse() { return explicationReponse; }
    public void setExplicationReponse(String explicationReponse) { this.explicationReponse = explicationReponse; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

    public List<Reponse> getReponses() { return reponses; }

    public void addReponse(Reponse reponse) {
        if (!reponses.contains(reponse)) {
            reponses.add(reponse);
            reponse.setQuestion(this);
        }
    }

    public void removeReponse(Reponse reponse) {
        if (reponses.remove(reponse)) {
            reponse.setQuestion(null);
        }
    }

    // ─── Méthodes utilitaires ─────────────────────────────────────

    public boolean isMultipleChoice() {
        return "choix_multiple".equals(typeQuestion);
    }

    public boolean isTrueFalse() {
        return "vrai_faux".equals(typeQuestion);
    }

    public boolean isTextAnswer() {
        return "texte_libre".equals(typeQuestion);
    }

    public List<Reponse> getCorrectAnswers() {
        List<Reponse> correct = new ArrayList<>();
        for (Reponse r : reponses) {
            if (Boolean.TRUE.equals(r.getEstCorrecte())) {
                correct.add(r);
            }
        }
        return correct;
    }
    public void setId(Long id) { this.id = id; }
    public int getCorrectAnswersCount() {
        return getCorrectAnswers().size();
    }

    // ─── toString ─────────────────────────────────────────────────

    @Override
    public String toString() {
        if (titre != null) return titre;
        if (texte != null) return texte;
        return "Question #" + id;
    }
}