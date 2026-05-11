package entities;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "Utilisateur",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_utilisateur_email", columnNames = "email")
        }
)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "typeUtilisateur", discriminatorType = DiscriminatorType.STRING)
public abstract class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", length = 100, nullable = false)
    @NotBlank(message = "Le nom est obligatoire.")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre {min} et {max} caracteres.")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Le nom contient des caracteres invalides.")
    private String nom;

    @Column(name = "prenom", length = 100, nullable = false)
    @NotBlank(message = "Le prenom est obligatoire.")
    @Size(min = 2, max = 100, message = "Le prenom doit contenir entre {min} et {max} caracteres.")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Le prenom contient des caracteres invalides.")
    private String prenom;

    @Column(name = "email", length = 180, unique = true, nullable = false)
    @NotBlank(message = "L'email est obligatoire.")
    @Email(message = "L'email ${validatedValue} n'est pas valide.")
    private String email;

    @Column(name = "mot_de_passe", length = 255, nullable = false)
    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins {min} caracteres.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Le mot de passe doit contenir au moins une majuscule, une minuscule et un chiffre."
    )
    private String motDePasse;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "reset_token", length = 255)
    private String resetToken;

    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    public Utilisateur() {
        this.dateCreation = LocalDateTime.now();
    }

    public Utilisateur(String nom, String prenom, String email, String motDePasse) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.dateCreation = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenExpiresAt() {
        return resetTokenExpiresAt;
    }

    public void setResetTokenExpiresAt(LocalDateTime resetTokenExpiresAt) {
        this.resetTokenExpiresAt = resetTokenExpiresAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public String getUsername() {
        return prenom + " " + nom;
    }

    public abstract String getType();

    public boolean isResetTokenValid() {
        return resetToken != null
                && resetTokenExpiresAt != null
                && resetTokenExpiresAt.isAfter(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "Utilisateur{"
                + "id=" + id
                + ", nom='" + nom + '\''
                + ", prenom='" + prenom + '\''
                + ", email='" + email + '\''
                + ", dateCreation=" + dateCreation
                + ", type=" + getType()
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Utilisateur)) {
            return false;
        }
        Utilisateur that = (Utilisateur) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
