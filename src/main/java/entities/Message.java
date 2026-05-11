package entities;

import java.time.LocalDateTime;

public class Message {

    private Long id;
    private Long expediteurId;
    private Long destinataireId;
    private String objet;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private LocalDateTime dateLecture;
    private String statut;
    private String priorite;
    private String pieceJointeUrl;
    private Long parentId;
    private String categorie;
    private boolean estArchiveExpediteur;
    private boolean estArchiveDestinataire;
    private boolean estSupprimeExpediteur;
    private boolean estSupprimeDestinataire;

    public Message() {
        this.dateEnvoi = LocalDateTime.now();
        this.statut = "envoye";
        this.priorite = "normal";
        this.categorie = "personnel";
        this.estArchiveExpediteur = false;
        this.estArchiveDestinataire = false;
        this.estSupprimeExpediteur = false;
        this.estSupprimeDestinataire = false;
    }

    public Message(Long expediteurId, Long destinataireId, String objet, String contenu) {
        this();
        this.expediteurId = expediteurId;
        this.destinataireId = destinataireId;
        this.objet = objet;
        this.contenu = contenu;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExpediteurId() {
        return expediteurId;
    }

    public void setExpediteurId(Long expediteurId) {
        this.expediteurId = expediteurId;
    }

    public Long getDestinataireId() {
        return destinataireId;
    }

    public void setDestinataireId(Long destinataireId) {
        this.destinataireId = destinataireId;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public LocalDateTime getDateLecture() {
        return dateLecture;
    }

    public void setDateLecture(LocalDateTime dateLecture) {
        this.dateLecture = dateLecture;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }

    public String getPieceJointeUrl() {
        return pieceJointeUrl;
    }

    public void setPieceJointeUrl(String pieceJointeUrl) {
        this.pieceJointeUrl = pieceJointeUrl;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public boolean isEstArchiveExpediteur() {
        return estArchiveExpediteur;
    }

    public void setEstArchiveExpediteur(boolean estArchiveExpediteur) {
        this.estArchiveExpediteur = estArchiveExpediteur;
    }

    public boolean isEstArchiveDestinataire() {
        return estArchiveDestinataire;
    }

    public void setEstArchiveDestinataire(boolean estArchiveDestinataire) {
        this.estArchiveDestinataire = estArchiveDestinataire;
    }

    public boolean isEstSupprimeExpediteur() {
        return estSupprimeExpediteur;
    }

    public void setEstSupprimeExpediteur(boolean estSupprimeExpediteur) {
        this.estSupprimeExpediteur = estSupprimeExpediteur;
    }

    public boolean isEstSupprimeDestinataire() {
        return estSupprimeDestinataire;
    }

    public void setEstSupprimeDestinataire(boolean estSupprimeDestinataire) {
        this.estSupprimeDestinataire = estSupprimeDestinataire;
    }

    public boolean isLu() {
        return "lu".equalsIgnoreCase(this.statut);
    }
}