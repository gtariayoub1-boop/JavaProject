package gui;

import entities.Evenement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.EvenementService;
import java.time.LocalDateTime;

public class EventFormController {
    @FXML private Label lblTitle;
    @FXML private TextField titreField;
    @FXML private TextField lieuField;
    @FXML private TextField typeField;
    @FXML private DatePicker dpDebut;
    @FXML private DatePicker dpFin;

    private final EvenementService es = new EvenementService();
    private Evenement currentEvent;
    private boolean saved = false;

    public void setEvent(Evenement event) {
        this.currentEvent = event;
        if (event != null) {
            lblTitle.setText("Modifier l'Événement");
            titreField.setText(event.getTitre());
            lieuField.setText(event.getLieu());
            typeField.setText(event.getType_evenement());
            if (event.getDate_debut() != null) dpDebut.setValue(event.getDate_debut().toLocalDate());
            if (event.getDate_fin() != null) dpFin.setValue(event.getDate_fin().toLocalDate());
        } else {
            lblTitle.setText("Ajouter un Événement");
        }
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (titreField.getText().isEmpty() || lieuField.getText().isEmpty() || dpDebut.getValue() == null || dpFin.getValue() == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        LocalDateTime start = dpDebut.getValue().atStartOfDay();
        LocalDateTime end = dpFin.getValue().atStartOfDay();

        try {
            if (currentEvent == null) {
                Evenement e = new Evenement(
                    titreField.getText(),
                    lieuField.getText(),
                    typeField.getText(),
                    start,
                    end
                );
                if (utils.UserSession.getCurrentUser() != null) {
                    e.setCreateur_id(utils.UserSession.getCurrentUser().getId().intValue());
                } else {
                    e.setCreateur_id(1); // fallback
                }
                es.create(e);
            } else {
                currentEvent.setTitre(titreField.getText());
                currentEvent.setLieu(lieuField.getText());
                currentEvent.setType_evenement(typeField.getText());
                currentEvent.setDate_debut(start);
                currentEvent.setDate_fin(end);
                es.update(currentEvent);
            }

            saved = true;
            ((Stage) titreField.getScene().getWindow()).close();
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue lors de l'enregistrement : " + e.getMessage());
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        ((Stage) titreField.getScene().getWindow()).close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
