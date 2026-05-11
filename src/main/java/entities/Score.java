package entities;

import java.time.LocalDateTime;

public class Score {

    private int id;
    private int soumissionId; // relation simplifiée (clé étrangère)
    private double note;
    private double noteSur;
    private String commentaireEnseignant;
    private LocalDateTime dateCorrection;
    private String statutCorrection;

    // 🔹 Constructeur vide
    public Score() {
        this.dateCorrection = LocalDateTime.now();
        this.statutCorrection = "a_corriger";
    }

    // 🔹 Constructeur complet
    public Score(int id, int soumissionId, double note, double noteSur,
                 String commentaireEnseignant, LocalDateTime dateCorrection,
                 String statutCorrection) {
        this.id = id;
        this.soumissionId = soumissionId;
        this.note = note;
        this.noteSur = noteSur;
        this.commentaireEnseignant = commentaireEnseignant;
        this.dateCorrection = dateCorrection;
        this.statutCorrection = statutCorrection;
    }

    // ================= GETTERS & SETTERS =================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSoumissionId() { return soumissionId; }
    public void setSoumissionId(int soumissionId) { this.soumissionId = soumissionId; }

    public double getNote() { return note; }
    public void setNote(double note) { this.note = note; }

    public double getNoteSur() { return noteSur; }
    public void setNoteSur(double noteSur) { this.noteSur = noteSur; }

    public String getCommentaireEnseignant() { return commentaireEnseignant; }
    public void setCommentaireEnseignant(String commentaireEnseignant) {
        this.commentaireEnseignant = commentaireEnseignant;
    }

    public LocalDateTime getDateCorrection() { return dateCorrection; }
    public void setDateCorrection(LocalDateTime dateCorrection) {
        this.dateCorrection = dateCorrection;
    }

    public String getStatutCorrection() { return statutCorrection; }
    public void setStatutCorrection(String statutCorrection) {
        this.statutCorrection = statutCorrection;
    }

    // ================= LOGIQUE MÉTIER =================

    // 🔹 Calcul pourcentage
    public Double getPourcentage() {
        if (noteSur > 0) {
            return Math.round((note / noteSur) * 10000.0) / 100.0;
        }
        return null;
    }

    // 🔹 Mention
    public String getMention() {
        Double p = getPourcentage();
        if (p == null) return null;

        if (p >= 90) return "Excellent";
        else if (p >= 80) return "Très bien";
        else if (p >= 70) return "Bien";
        else if (p >= 60) return "Assez bien";
        else if (p >= 50) return "Passable";
        else return "Insuffisant";
    }

    // 🔹 Réussi ou non
    public boolean isReussi() {
        Double p = getPourcentage();
        return p != null && p >= 50;
    }

    // 🔹 Validation (équivalent Assert Callback)
    public boolean isValidNote() {
        return note <= noteSur;
    }

    @Override
    public String toString() {
        return "Score: " + note + "/" + noteSur;
    }
}
