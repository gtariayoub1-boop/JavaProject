package entities;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "reponse")
public class Reponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "texte_reponse", columnDefinition = "TEXT")
    private String texteReponse;

    @Column(name = "est_correcte")
    private Boolean estCorrecte;

    @Column(name = "ordre_affichage")
    private Integer ordreAffichage;

    @Column(name = "pourcentage_points", precision = 5, scale = 2)
    private BigDecimal pourcentagePoints;

    @Column(name = "feedback_specifique", columnDefinition = "TEXT")
    private String feedbackSpecifique;

    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    // ─── Constructeurs ────────────────────────────────────────────

    public Reponse() {}

    public Reponse(String texteReponse, Boolean estCorrecte, Question question) {
        this.texteReponse = texteReponse;
        this.estCorrecte = estCorrecte;
        this.question = question;
    }

    // ─── Getters & Setters ────────────────────────────────────────

    public Long getId() { return id; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public String getTexteReponse() { return texteReponse; }
    public void setTexteReponse(String texteReponse) { this.texteReponse = texteReponse; }

    public Boolean getEstCorrecte() { return estCorrecte; }
    public void setEstCorrecte(Boolean estCorrecte) { this.estCorrecte = estCorrecte; }

    public Integer getOrdreAffichage() { return ordreAffichage; }
    public void setOrdreAffichage(Integer ordreAffichage) { this.ordreAffichage = ordreAffichage; }

    public BigDecimal getPourcentagePoints() { return pourcentagePoints; }
    public void setPourcentagePoints(BigDecimal pourcentagePoints) { this.pourcentagePoints = pourcentagePoints; }

    public String getFeedbackSpecifique() { return feedbackSpecifique; }
    public void setFeedbackSpecifique(String feedbackSpecifique) { this.feedbackSpecifique = feedbackSpecifique; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public void setId(Long id) { this.id = id; }
    // ─── toString ─────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Reponse{"
                + "id=" + id
                + ", texteReponse='" + texteReponse + '\''
                + ", estCorrecte=" + estCorrecte
                + '}';
    }
}