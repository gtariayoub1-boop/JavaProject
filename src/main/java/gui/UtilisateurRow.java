package gui;

import entities.Administrateur;
import entities.Enseignant;
import entities.Etudiant;
import entities.Utilisateur;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UtilisateurRow {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Utilisateur utilisateur;

    public UtilisateurRow(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public long getId() {
        return utilisateur.getId();
    }

    public String getFullName() {
        return utilisateur.getNomComplet();
    }

    public String getEmail() {
        return utilisateur.getEmail();
    }

    public String getType() {
        return utilisateur.getType();
    }

    public String getIdentifier() {
        if (utilisateur instanceof Etudiant) {
            return ((Etudiant) utilisateur).getMatricule();
        }
        if (utilisateur instanceof Enseignant) {
            return ((Enseignant) utilisateur).getMatriculeEnseignant();
        }
        if (utilisateur instanceof Administrateur) {
            return ((Administrateur) utilisateur).getFonction();
        }
        return "-";
    }

    public String getStatus() {
        if (utilisateur instanceof Etudiant) {
            return ((Etudiant) utilisateur).getStatut();
        }
        if (utilisateur instanceof Enseignant) {
            return ((Enseignant) utilisateur).getStatut();
        }
        if (utilisateur instanceof Administrateur) {
            return ((Administrateur) utilisateur).isActif() ? "actif" : "inactif";
        }
        return "-";
    }

    public String getExtraInfo() {
        if (utilisateur instanceof Etudiant) {
            Etudiant etudiant = (Etudiant) utilisateur;
            return etudiant.getNiveauEtude() + " | " + etudiant.getSpecialisation();
        }
        if (utilisateur instanceof Enseignant) {
            Enseignant enseignant = (Enseignant) utilisateur;
            return enseignant.getSpecialite() + " | " + enseignant.getTypeContrat();
        }
        if (utilisateur instanceof Administrateur) {
            Administrateur administrateur = (Administrateur) utilisateur;
            return administrateur.getDepartement() + " | " + administrateur.getFonction();
        }
        return "-";
    }

    public String getLastLogin() {
        return formatDateTime(utilisateur.getLastLogin());
    }

    public String getDateCreation() {
        return formatDateTime(utilisateur.getDateCreation());
    }

    private String formatDateTime(LocalDateTime value) {
        return value != null ? value.format(DATE_TIME_FORMATTER) : "-";
    }
}
