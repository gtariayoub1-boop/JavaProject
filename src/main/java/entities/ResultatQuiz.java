package entities;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resultat_quiz")
public class ResultatQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "id_etudiant", nullable = false)
    private Integer idEtudiant;

    @Column(name = "date_passation", nullable = false)
    private LocalDateTime datePassation;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;

    @Column(name = "earned_points", nullable = false)
    private Integer earnedPoints;

    // JSON stocké en String — désérialisé au niveau service via Jackson
    // Format: [{ questionTexte, studentAnswer, correctAnswer, isCorrect, points }, ...]
    @Column(name = "reponses_etudiant", columnDefinition = "JSON")
    private String reponsesEtudiant;

    // ─── Constructeurs ────────────────────────────────────────────

    public ResultatQuiz() {}

    public ResultatQuiz(Quiz quiz, Integer idEtudiant, Double score,
                        Integer totalPoints, Integer earnedPoints) {
        this.quiz = quiz;
        this.idEtudiant = idEtudiant;
        this.score = score;
        this.totalPoints = totalPoints;
        this.earnedPoints = earnedPoints;
        this.datePassation = LocalDateTime.now();
    }

    // ─── Getters & Setters ────────────────────────────────────────

    public Long getId() { return id; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

    public Integer getIdEtudiant() { return idEtudiant; }
    public void setIdEtudiant(Integer idEtudiant) { this.idEtudiant = idEtudiant; }

    public LocalDateTime getDatePassation() { return datePassation; }
    public void setDatePassation(LocalDateTime datePassation) { this.datePassation = datePassation; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

    public Integer getEarnedPoints() { return earnedPoints; }
    public void setEarnedPoints(Integer earnedPoints) { this.earnedPoints = earnedPoints; }

    public String getReponsesEtudiant() { return reponsesEtudiant; }
    public void setReponsesEtudiant(String reponsesEtudiant) { this.reponsesEtudiant = reponsesEtudiant; }
    public void setId(Long id) { this.id = id; }

    // ─── toString ─────────────────────────────────────────────────

    @Override
    public String toString() {
        return "ResultatQuiz{"
                + "id=" + id
                + ", idEtudiant=" + idEtudiant
                + ", score=" + score
                + ", totalPoints=" + totalPoints
                + ", earnedPoints=" + earnedPoints
                + ", datePassation=" + datePassation
                + '}';
    }
}