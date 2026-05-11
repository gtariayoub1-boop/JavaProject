package entities;

import java.time.LocalDateTime;
import java.util.List;

public class Evaluation {

    private int id;
    private String titre;
    private String typeEvaluation; // projet / examen
    private String description;
    private Integer coursId; // relation simplifiée (clé étrangère)
    private String idEnseignant;
    private LocalDateTime dateCreation;
    private LocalDateTime dateLimite;
    private double noteMax;
    private String modeRemise; // en_ligne / presentiel
    private String statut; // ouverte / fermee
    private String pdfFilename;

    // 🔹 Constructeur vide
    public Evaluation() {
        this.dateCreation = LocalDateTime.now();
        this.statut = "ouverte";
    }

    // 🔹 Constructeur complet
    public Evaluation(int id, String titre, String typeEvaluation, String description,
                      Integer coursId, String idEnseignant, LocalDateTime dateCreation,
                      LocalDateTime dateLimite, double noteMax,
                      String modeRemise, String statut, String pdfFilename) {
        this.id = id;
        this.titre = titre;
        this.typeEvaluation = typeEvaluation;
        this.description = description;
        this.coursId = coursId;
        this.idEnseignant = idEnseignant;
        this.dateCreation = dateCreation;
        this.dateLimite = dateLimite;
        this.noteMax = noteMax;
        this.modeRemise = modeRemise;
        this.statut = statut;
        this.pdfFilename = pdfFilename;
    }

    // ================= GETTERS & SETTERS =================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getTypeEvaluation() { return typeEvaluation; }
    public void setTypeEvaluation(String typeEvaluation) { this.typeEvaluation = typeEvaluation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public void setCoursId(Integer coursId) {
        this.coursId = coursId;
    }

    public Integer getCoursId() { return coursId; }


    public String getIdEnseignant() { return idEnseignant; }
    public void setIdEnseignant(String idEnseignant) { this.idEnseignant = idEnseignant; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateLimite() { return dateLimite; }
    public void setDateLimite(LocalDateTime dateLimite) { this.dateLimite = dateLimite; }

    public double getNoteMax() { return noteMax; }
    public void setNoteMax(double noteMax) { this.noteMax = noteMax; }

    public String getModeRemise() { return modeRemise; }
    public void setModeRemise(String modeRemise) { this.modeRemise = modeRemise; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getPdfFilename() { return pdfFilename; }
    public void setPdfFilename(String pdfFilename) { this.pdfFilename = pdfFilename; }
}
