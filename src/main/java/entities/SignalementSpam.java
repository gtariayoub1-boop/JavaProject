package entities;

import java.time.LocalDateTime;

public class SignalementSpam {
    private Long id;
    private Long expediteurId;
    private String contenuMessage;
    private LocalDateTime dateSignalement;
    private boolean estTraite;

    public SignalementSpam(Long id, Long expediteurId, String contenuMessage, LocalDateTime dateSignalement, boolean estTraite) {
        this.id = id;
        this.expediteurId = expediteurId;
        this.contenuMessage = contenuMessage;
        this.dateSignalement = dateSignalement;
        this.estTraite = estTraite;
    }

    public SignalementSpam(Long expediteurId, String contenuMessage) {
        this.expediteurId = expediteurId;
        this.contenuMessage = contenuMessage;
        this.dateSignalement = LocalDateTime.now();
        this.estTraite = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getExpediteurId() { return expediteurId; }
    public void setExpediteurId(Long expediteurId) { this.expediteurId = expediteurId; }

    public String getContenuMessage() { return contenuMessage; }
    public void setContenuMessage(String contenuMessage) { this.contenuMessage = contenuMessage; }

    public LocalDateTime getDateSignalement() { return dateSignalement; }
    public void setDateSignalement(LocalDateTime dateSignalement) { this.dateSignalement = dateSignalement; }

    public boolean isEstTraite() { return estTraite; }
    public void setEstTraite(boolean estTraite) { this.estTraite = estTraite; }
}
