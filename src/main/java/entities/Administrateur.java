package entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "Administrateur")
@PrimaryKeyJoinColumn(name = "id")
public class Administrateur extends Utilisateur {

    @Column(name = "departement", length = 100, nullable = false)
    @NotBlank(message = "Le departement est obligatoire.")
    @Pattern(
            regexp = "^(Direction Generale|Scolarite|Ressources Humaines|Finances|Informatique|Communication|Recherche)$",
            message = "Veuillez choisir un departement valide."
    )
    private String departement;

    @Column(name = "fonction", length = 100, nullable = false)
    @NotBlank(message = "La fonction est obligatoire.")
    @Size(min = 3, max = 100, message = "La fonction doit contenir entre {min} et {max} caracteres.")
    private String fonction;

    @Column(name = "telephone", length = 20)
    @Pattern(
            regexp = "^(\\+?[0-9]{1,3})?[0-9]{8,15}$",
            message = "Le numero de telephone n'est pas valide."
    )
    private String telephone;

    @Column(name = "date_nomination", nullable = false)
    @NotNull(message = "La date de nomination est obligatoire.")
    private LocalDateTime dateNomination;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    public Administrateur() {
        super();
        this.dateNomination = LocalDateTime.now();
    }

    public Administrateur(
            String nom,
            String prenom,
            String email,
            String motDePasse,
            String departement,
            String fonction
    ) {
        super(nom, prenom, email, motDePasse);
        this.departement = departement;
        this.fonction = fonction;
        this.dateNomination = LocalDateTime.now();
        this.actif = true;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public String getFonction() {
        return fonction;
    }

    public void setFonction(String fonction) {
        this.fonction = fonction;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public LocalDateTime getDateNomination() {
        return dateNomination;
    }

    public void setDateNomination(LocalDateTime dateNomination) {
        this.dateNomination = dateNomination;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    @Override
    public String getType() {
        return "administrateur";
    }

    @Override
    public String toString() {
        return "Administrateur{"
                + "id=" + getId()
                + ", nom='" + getNom() + '\''
                + ", prenom='" + getPrenom() + '\''
                + ", email='" + getEmail() + '\''
                + ", departement='" + departement + '\''
                + ", fonction='" + fonction + '\''
                + ", telephone='" + telephone + '\''
                + ", dateNomination=" + dateNomination
                + ", actif=" + actif
                + '}';
    }
}
