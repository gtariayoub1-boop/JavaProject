package entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Table(
        name = "Enseignant",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_enseignant_matricule", columnNames = "matriculeEnseignant")
        }
)
public class Enseignant extends Utilisateur {

    @Column(name = "matriculeEnseignant", length = 50, unique = true, nullable = false)
    @NotBlank(message = "Le matricule enseignant est obligatoire.")
    @Size(min = 5, max = 50, message = "Le matricule doit contenir entre {min} et {max} caracteres.")
    @Pattern(
            regexp = "^ENS-[A-Z0-9-]+$",
            message = "Le matricule doit commencer par ENS- suivi de lettres majuscules et chiffres."
    )
    private String matriculeEnseignant;

    @Column(name = "diplome", length = 100, nullable = false)
    @NotBlank(message = "Le diplome est obligatoire.")
    @Pattern(regexp = "^(Licence|Master|Doctorat|HDR|Ingenieur)$", message = "Veuillez choisir un diplome valide.")
    private String diplome;

    @Column(name = "specialite", length = 100, nullable = false)
    @NotBlank(message = "La specialite est obligatoire.")
    @Size(min = 3, max = 100, message = "La specialite doit contenir entre {min} et {max} caracteres.")
    private String specialite;

    @Column(name = "anneesExperience", nullable = false)
    @NotNull(message = "Les annees d'experience sont obligatoires.")
    @Min(value = 0, message = "Les annees d'experience doivent etre au minimum {value}")
    @Max(value = 50, message = "Les annees d'experience doivent etre au maximum {value}")
    private Integer anneesExperience;

    @Column(name = "typeContrat", length = 50, nullable = false)
    @NotBlank(message = "Le type de contrat est obligatoire.")
    @Pattern(regexp = "^(CDI|CDD|Vacataire|Contractuel)$", message = "Veuillez choisir un type de contrat valide.")
    private String typeContrat;

    @Column(name = "tauxHoraire", precision = 10, scale = 2)
    @DecimalMin(value = "10.0", message = "Le taux horaire doit etre au minimum {value} euros")
    @DecimalMax(value = "200.0", message = "Le taux horaire doit etre au maximum {value} euros")
    private BigDecimal tauxHoraire;

    @Column(name = "disponibilites", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Les disponibilites ne peuvent pas depasser {max} caracteres.")
    private String disponibilites;

    @Column(name = "statut", length = 20, nullable = false)
    @Pattern(regexp = "^(actif|inactif|conge|retraite)$", message = "Statut invalide.")
    private String statut = "actif";

    public Enseignant() {
        super();
    }

    public Enseignant(
            String nom,
            String prenom,
            String email,
            String motDePasse,
            String matriculeEnseignant,
            String diplome,
            String specialite,
            Integer anneesExperience,
            String typeContrat
    ) {
        super(nom, prenom, email, motDePasse);
        this.matriculeEnseignant = matriculeEnseignant;
        this.diplome = diplome;
        this.specialite = specialite;
        this.anneesExperience = anneesExperience;
        this.typeContrat = typeContrat;
        this.statut = "actif";
    }

    public String getMatriculeEnseignant() {
        return matriculeEnseignant;
    }

    public void setMatriculeEnseignant(String matriculeEnseignant) {
        this.matriculeEnseignant = matriculeEnseignant;
    }

    public String getDiplome() {
        return diplome;
    }

    public void setDiplome(String diplome) {
        this.diplome = diplome;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public Integer getAnneesExperience() {
        return anneesExperience;
    }

    public void setAnneesExperience(Integer anneesExperience) {
        this.anneesExperience = anneesExperience;
    }

    public String getTypeContrat() {
        return typeContrat;
    }

    public void setTypeContrat(String typeContrat) {
        this.typeContrat = typeContrat;
    }

    public BigDecimal getTauxHoraire() {
        return tauxHoraire;
    }

    public void setTauxHoraire(BigDecimal tauxHoraire) {
        this.tauxHoraire = tauxHoraire;
    }

    public String getDisponibilites() {
        return disponibilites;
    }

    public void setDisponibilites(String disponibilites) {
        this.disponibilites = disponibilites;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String getType() {
        return "enseignant";
    }

    public boolean isVacataire() {
        return "Vacataire".equals(typeContrat);
    }

    public boolean isActif() {
        return "actif".equals(statut);
    }

    public String getDisplayName() {
        return getNomComplet() + " (" + matriculeEnseignant + ")";
    }

    @Override
    public String toString() {
        return "Enseignant{"
                + "id=" + getId()
                + ", nom='" + getNom() + '\''
                + ", prenom='" + getPrenom() + '\''
                + ", email='" + getEmail() + '\''
                + ", matriculeEnseignant='" + matriculeEnseignant + '\''
                + ", diplome='" + diplome + '\''
                + ", specialite='" + specialite + '\''
                + ", anneesExperience=" + anneesExperience
                + ", typeContrat='" + typeContrat + '\''
                + ", statut='" + statut + '\''
                + '}';
    }
}
