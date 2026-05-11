package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ForumDiscussion {

    private Long id;
    private String titre;
    private String description;
    private Long createurId;
    private Integer idCours;
    private LocalDateTime dateCreation;
    private String type;
    private String statut;
    private int nombreVues;
    private LocalDateTime derniereActivite;
    private List<String> tags;
    private String reglesModeration;
    private String imageCouvertureUrl;
    private String pieceJointeUrl;
    private int likes;
    private int dislikes;
    private int signalements;
    private boolean estModifie;
    private LocalDateTime dateModification;

    public ForumDiscussion() {
        this.dateCreation = LocalDateTime.now();
        this.derniereActivite = LocalDateTime.now();
        this.type = "public";
        this.statut = "ouvert";
        this.nombreVues = 0;
        this.tags = new ArrayList<>();
        this.likes = 0;
        this.dislikes = 0;
        this.signalements = 0;
        this.estModifie = false;
    }

    public ForumDiscussion(String titre, String description, Long createurId) {
        this();
        this.titre = titre;
        this.description = description;
        this.createurId = createurId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getCreateurId() {
        return createurId;
    }

    public void setCreateurId(Long createurId) {
        this.createurId = createurId;
    }

    public Integer getIdCours() {
        return idCours;
    }

    public void setIdCours(Integer idCours) {
        this.idCours = idCours;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getNombreVues() {
        return nombreVues;
    }

    public void setNombreVues(int nombreVues) {
        this.nombreVues = nombreVues;
    }

    public LocalDateTime getDerniereActivite() {
        return derniereActivite;
    }

    public void setDerniereActivite(LocalDateTime derniereActivite) {
        this.derniereActivite = derniereActivite;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getReglesModeration() {
        return reglesModeration;
    }

    public void setReglesModeration(String reglesModeration) {
        this.reglesModeration = reglesModeration;
    }

    public String getImageCouvertureUrl() {
        return imageCouvertureUrl;
    }

    public void setImageCouvertureUrl(String imageCouvertureUrl) {
        this.imageCouvertureUrl = imageCouvertureUrl;
    }

    public String getPieceJointeUrl() {
        return pieceJointeUrl;
    }

    public void setPieceJointeUrl(String pieceJointeUrl) {
        this.pieceJointeUrl = pieceJointeUrl;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public int getSignalements() {
        return signalements;
    }

    public void setSignalements(int signalements) {
        this.signalements = signalements;
    }

    public boolean isEstModifie() {
        return estModifie;
    }

    public void setEstModifie(boolean estModifie) {
        this.estModifie = estModifie;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public void incrementVues() {
        this.nombreVues++;
        this.derniereActivite = LocalDateTime.now();
    }
}