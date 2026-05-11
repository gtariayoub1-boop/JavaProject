package entities;

import java.sql.Timestamp;

public class Notification {
    private long id;
    private long utilisateurId;
    private String titre;
    private String contenu;
    private boolean estLu;
    private Timestamp dateCreation;

    public Notification() {}

    public Notification(long utilisateurId, String titre, String contenu) {
        this.utilisateurId = utilisateurId;
        this.titre = titre;
        this.contenu = contenu;
        this.estLu = false;
    }

    public Notification(long id, long utilisateurId, String titre, String contenu, boolean estLu, Timestamp dateCreation) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.titre = titre;
        this.contenu = contenu;
        this.estLu = estLu;
        this.dateCreation = dateCreation;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(long utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public boolean isEstLu() { return estLu; }
    public void setEstLu(boolean estLu) { this.estLu = estLu; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", utilisateurId=" + utilisateurId +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", estLu=" + estLu +
                ", dateCreation=" + dateCreation +
                '}';
    }
}
