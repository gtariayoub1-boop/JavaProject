package entities;

import java.time.LocalDateTime;

public class Evenement {
    private int id;
    private int createur_id;
    private String titre;
    private String description;
    private String type_evenement;
    private LocalDateTime date_debut;
    private LocalDateTime date_fin;
    private String lieu;
    private int capacite_max;
    private String statut;
    private String visibilite;

    public Evenement() {}

    public Evenement(String titre, String lieu, String type_evenement, LocalDateTime date_debut, LocalDateTime date_fin) {
        this.titre = titre;
        this.lieu = lieu;
        this.type_evenement = type_evenement;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.description = "";
        this.statut = "En attente";
        this.visibilite = "Public";
    }

    public Evenement(int id, int createur_id, String titre, String description, String type_evenement, LocalDateTime date_debut, LocalDateTime date_fin, String lieu, int capacite_max, String statut, String visibilite) {
        this.id = id;
        this.createur_id = createur_id;
        this.titre = titre;
        this.description = description;
        this.type_evenement = type_evenement;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.lieu = lieu;
        this.capacite_max = capacite_max;
        this.statut = statut;
        this.visibilite = visibilite;
    }

    public Evenement(int createur_id, String titre, String description, String type_evenement, LocalDateTime date_debut, LocalDateTime date_fin, String lieu, int capacite_max, String statut, String visibilite) {
        this.createur_id = createur_id;
        this.titre = titre;
        this.description = description;
        this.type_evenement = type_evenement;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.lieu = lieu;
        this.capacite_max = capacite_max;
        this.statut = statut;
        this.visibilite = visibilite;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCreateur_id() { return createur_id; }
    public void setCreateur_id(int createur_id) { this.createur_id = createur_id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType_evenement() { return type_evenement; }
    public void setType_evenement(String type_evenement) { this.type_evenement = type_evenement; }

    public LocalDateTime getDate_debut() { return date_debut; }
    public void setDate_debut(LocalDateTime date_debut) { this.date_debut = date_debut; }

    public LocalDateTime getDate_fin() { return date_fin; }
    public void setDate_fin(LocalDateTime date_fin) { this.date_fin = date_fin; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public int getCapacite_max() { return capacite_max; }
    public void setCapacite_max(int capacite_max) { this.capacite_max = capacite_max; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getVisibilite() { return visibilite; }
    public void setVisibilite(String visibilite) { this.visibilite = visibilite; }

    @Override
    public String toString() {
        return "Evenement{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", lieu='" + lieu + '\'' +
                ", date_debut=" + date_debut +
                '}';
    }
}
