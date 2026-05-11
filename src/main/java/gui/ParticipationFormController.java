package gui;

import entities.Evenement;
import entities.ParticipationEvenement;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ParticipationEvenementService;
import services.WeatherService;
import utils.GroqService;
import java.time.LocalDateTime;

public class ParticipationFormController {
    @FXML private Label lblEventInfo;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField telField;
    @FXML private TextField emailField;
    @FXML private TextField anneeField;
    @FXML private TextArea descriptionField;
    @FXML private Label weatherLabel;

    private final ParticipationEvenementService ps = new ParticipationEvenementService();
    private final WeatherService weatherService = new WeatherService();
    private final GroqService groqService = new GroqService();
    private Evenement selectedEvent;

    public void setEvent(Evenement event) {
        this.selectedEvent = event;
        lblEventInfo.setText("Événement: " + event.getTitre() + " à " + event.getLieu());
        updateWeather();
    }

    private void updateWeather() {
        if (selectedEvent != null) {
            new Thread(() -> {
                String forecast = weatherService.getWeatherForecast(selectedEvent.getLieu(), selectedEvent.getDate_debut());
                Platform.runLater(() -> weatherLabel.setText(forecast));
            }).start();
        }
    }

    @FXML
    void handleAI(ActionEvent event) {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String annee = anneeField.getText();

        if (nom.isEmpty() || prenom.isEmpty()) {
            showAlert("Info", "Veuillez saisir votre nom et prénom pour personnaliser la description.");
            return;
        }

        descriptionField.setText("Génération en cours...");
        new Thread(() -> {
            try {
                String description = groqService.generateDescription(nom, prenom, annee, selectedEvent.getTitre(), selectedEvent.getType_evenement());
                Platform.runLater(() -> descriptionField.setText(description));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    descriptionField.setText("Erreur lors de la génération IA.");
                    showAlert("Erreur IA", e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez remplir les champs obligatoires.");
            return;
        }

        ParticipationEvenement p = new ParticipationEvenement();
        p.setNom(nomField.getText());
        p.setPrenom(prenomField.getText());
        p.setTelephone(telField.getText());
        p.setEmail(emailField.getText());
        p.setAnneeScolaire(anneeField.getText());
        p.setDescriptionParticipant(descriptionField.getText());
        p.setEvenement_id(selectedEvent.getId());
        if (utils.UserSession.getCurrentUser() != null) {
            p.setUtilisateur_id(utils.UserSession.getCurrentUser().getId().intValue());
        } else {
            p.setUtilisateur_id(1); // fallback
        }
        p.setStatut("Confirmé");
        p.setDate_inscription(LocalDateTime.now());

        try {
            ps.create(p);
            
            // Alerte spéciale météo
            if (weatherLabel.getText().toLowerCase().contains("pluie") || weatherLabel.getText().toLowerCase().contains("rain")) {
                showAlert("Inscription Confirmée", "Votre inscription est enregistrée ! ⚠️ Attention: De la pluie est prévue pour cet événement, n'oubliez pas votre parapluie !");
            } else {
                showAlert("Succès", "Votre participation a été enregistrée avec succès !");
            }

            ((Stage) nomField.getScene().getWindow()).close();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry") && e.getMessage().contains("unique_participation")) {
                showAlert("Déjà inscrit", "Vous êtes déjà inscrit à cet événement !");
            } else {
                showAlert("Erreur", "Une erreur est survenue lors de l'enregistrement : " + e.getMessage());
            }
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        ((Stage) nomField.getScene().getWindow()).close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
