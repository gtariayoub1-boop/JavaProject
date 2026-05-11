package entities;

import java.time.LocalDateTime;

public class Soumission {

    private int id;
    private int evaluationId; // relation simplifiée (clé étrangère)
    private String idEtudiant;
    private String fichierSoumissionUrl;
    private String commentaireEtudiant;
    private LocalDateTime dateSoumission;
    private String statut;
    private String pdfFilename;

    // 🔹 Constructeur vide
    public Soumission() {
        this.dateSoumission = LocalDateTime.now();
        this.statut = "non_soumise";
    }

    // 🔹 Constructeur complet
    public Soumission(int id, int evaluationId, String idEtudiant,
                      String fichierSoumissionUrl, String commentaireEtudiant,
                      LocalDateTime dateSoumission, String statut,
                      String pdfFilename) {
        this.id = id;
        this.evaluationId = evaluationId;
        this.idEtudiant = idEtudiant;
        this.fichierSoumissionUrl = fichierSoumissionUrl;
        this.commentaireEtudiant = commentaireEtudiant;
        this.dateSoumission = dateSoumission;
        this.statut = statut;
        this.pdfFilename = pdfFilename;
    }

    // ================= GETTERS & SETTERS =================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEvaluationId() { return evaluationId; }
    public void setEvaluationId(int evaluationId) { this.evaluationId = evaluationId; }

    public String getIdEtudiant() { return idEtudiant; }
    public void setIdEtudiant(String idEtudiant) { this.idEtudiant = idEtudiant; }

    public String getFichierSoumissionUrl() { return fichierSoumissionUrl; }
    public void setFichierSoumissionUrl(String fichierSoumissionUrl) {
        this.fichierSoumissionUrl = fichierSoumissionUrl;
    }

    public String getCommentaireEtudiant() { return commentaireEtudiant; }
    public void setCommentaireEtudiant(String commentaireEtudiant) {
        this.commentaireEtudiant = commentaireEtudiant;
    }

    public LocalDateTime getDateSoumission() { return dateSoumission; }
    public void setDateSoumission(LocalDateTime dateSoumission) {
        this.dateSoumission = dateSoumission;
    }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getPdfFilename() { return pdfFilename; }
    public void setPdfFilename(String pdfFilename) {
        this.pdfFilename = pdfFilename;
    }

    @Override
    public String toString() {
        return "Soumission [id=" + id +
                ", etudiant=" + idEtudiant +
                ", statut=" + statut + "]";
    }
}
