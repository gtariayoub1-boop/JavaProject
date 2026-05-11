package entities;

import java.time.LocalDateTime;

public class ParticipationEvenement {
    private int id;
    private int evenement_id;
    private int utilisateur_id;
    private String statut;
    private LocalDateTime date_inscription;
    private LocalDateTime heure_arrivee;
    private LocalDateTime heure_depart;
    private int feedback_note;
    private String feedback_commentaire;
    
    // User fields
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String anneeScolaire;
    private String titreEvenement;
    private String descriptionParticipant;

    public ParticipationEvenement() {}

    public ParticipationEvenement(int id, int evenement_id, int utilisateur_id, String statut, LocalDateTime date_inscription, LocalDateTime heure_arrivee, LocalDateTime heure_depart, int feedback_note, String feedback_commentaire) {
        this.id = id;
        this.evenement_id = evenement_id;
        this.utilisateur_id = utilisateur_id;
        this.statut = statut;
        this.date_inscription = date_inscription;
        this.heure_arrivee = heure_arrivee;
        this.heure_depart = heure_depart;
        this.feedback_note = feedback_note;
        this.feedback_commentaire = feedback_commentaire;
    }

    public ParticipationEvenement(int evenement_id, int utilisateur_id, String statut, LocalDateTime date_inscription, String nom, String prenom, String telephone, String email, String anneeScolaire) {
        this.evenement_id = evenement_id;
        this.utilisateur_id = utilisateur_id;
        this.statut = statut;
        this.date_inscription = date_inscription;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.email = email;
        this.anneeScolaire = anneeScolaire;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEvenement_id() { return evenement_id; }
    public void setEvenement_id(int evenement_id) { this.evenement_id = evenement_id; }

    public int getUtilisateur_id() { return utilisateur_id; }
    public void setUtilisateur_id(int utilisateur_id) { this.utilisateur_id = utilisateur_id; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDate_inscription() { return date_inscription; }
    public void setDate_inscription(LocalDateTime date_inscription) { this.date_inscription = date_inscription; }

    public LocalDateTime getHeure_arrivee() { return heure_arrivee; }
    public void setHeure_arrivee(LocalDateTime heure_arrivee) { this.heure_arrivee = heure_arrivee; }

    public LocalDateTime getHeure_depart() { return heure_depart; }
    public void setHeure_depart(LocalDateTime heure_depart) { this.heure_depart = heure_depart; }

    public int getFeedback_note() { return feedback_note; }
    public void setFeedback_note(int feedback_note) { this.feedback_note = feedback_note; }

    public String getFeedback_commentaire() { return feedback_commentaire; }
    public void setFeedback_commentaire(String feedback_commentaire) { this.feedback_commentaire = feedback_commentaire; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAnneeScolaire() { return anneeScolaire; }
    public void setAnneeScolaire(String anneeScolaire) { this.anneeScolaire = anneeScolaire; }

    public String getTitreEvenement() { return titreEvenement; }
    public void setTitreEvenement(String titreEvenement) { this.titreEvenement = titreEvenement; }

    public String getDescriptionParticipant() { return descriptionParticipant; }
    public void setDescriptionParticipant(String descriptionParticipant) { this.descriptionParticipant = descriptionParticipant; }

    @Override
    public String toString() {
        return "ParticipationEvenement{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}
