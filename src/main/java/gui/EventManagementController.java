package gui;

import entities.Evenement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.EvenementService;
import utils.UserSession;
import java.io.IOException;

public class EventManagementController {

    @FXML private TableView<Evenement> eventTable;
    @FXML private TableColumn<Evenement, String> colTitre, colLieu, colType;
    @FXML private TableColumn<Evenement, String> colDateDebut, colDateFin;
    @FXML private TextField searchField;
    @FXML private HBox adminActions, userActions;

    private final EvenementService es = new EvenementService();
    private ObservableList<Evenement> eventList;

    @FXML
    public void initialize() {
        setupTable();
        loadEvents();
        setupSearch();
        setupRoleAccess();
    }

    private void setupTable() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type_evenement"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("date_debut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("date_fin"));
    }

    private void loadEvents() {
        try {
            eventList = FXCollections.observableArrayList(es.getAll());
            eventTable.setItems(eventList);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les événements : " + e.getMessage());
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            ObservableList<Evenement> filtered = FXCollections.observableArrayList();
            for (Evenement e : eventList) {
                if (e.getTitre().toLowerCase().contains(newVal.toLowerCase()) || 
                    e.getLieu().toLowerCase().contains(newVal.toLowerCase())) {
                    filtered.add(e);
                }
            }
            eventTable.setItems(filtered);
        });
    }

    private void setupRoleAccess() {
        entities.Utilisateur user = utils.UserSession.getCurrentUser();
        if (user != null && "administrateur".equalsIgnoreCase(user.getType())) {
            adminActions.setManaged(true);
            adminActions.setVisible(true);
            userActions.setManaged(false);
            userActions.setVisible(false);
        } else {
            adminActions.setManaged(false);
            adminActions.setVisible(false);
            userActions.setManaged(true);
            userActions.setVisible(true);
        }
    }

    @FXML
    void handleAjouter(ActionEvent event) throws IOException {
        openEventForm(null);
    }

    @FXML
    void handleModifier(ActionEvent event) throws IOException {
        Evenement selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Veuillez sélectionner un événement à modifier.");
            return;
        }
        openEventForm(selected);
    }

    private void openEventForm(Evenement e) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/EventForm.fxml"));
        Parent root = loader.load();
        EventFormController controller = loader.getController();
        controller.setEvent(e);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(e == null ? "Nouvel Événement" : "Modifier l'Événement");
        stage.setScene(new Scene(root));
        stage.showAndWait();

        if (controller.isSaved()) {
            loadEvents();
        }
    }

    @FXML
    void handleParticiper(ActionEvent event) throws IOException {
        Evenement selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Veuillez sélectionner un événement pour participer.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ParticipationForm.fxml"));
        Parent root = loader.load();
        ParticipationFormController controller = loader.getController();
        controller.setEvent(selected);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Inscription - " + selected.getTitre());
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }

    @FXML
    void handleSupprimer(ActionEvent event) {
        Evenement selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Veuillez sélectionner un événement à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cet événement ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    es.delete(selected.getId());
                    loadEvents();
                } catch (Exception e) {
                    showAlert("Erreur", "Impossible de supprimer l'événement : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    void handleVoirParticipations(ActionEvent event) throws IOException {
        changeScene(event, "/gui/ParticipationManagement.fxml");
    }

    @FXML
    void handleVoirStats(ActionEvent event) throws IOException {
        changeScene(event, "/gui/Statistics.fxml");
    }

    @FXML
    void handleExportPDF(ActionEvent event) {
        // Logique PDF (déjà implémentée dans ParticipationManagement mais peut être adaptée ici)
        showAlert("PDF", "Exportation PDF lancée...");
    }

    @FXML
    void handleGenerateQR(ActionEvent event) {
        Evenement selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Sélectionnez un événement pour le QR Code.");
            return;
        }

        try {
            String data = "Événement: " + selected.getTitre() + "\nLieu: " + selected.getLieu() + "\nDate: " + selected.getDate_debut();
            javafx.scene.image.Image qrImage = utils.QRCodeGenerator.generateQRCodeImage(data, 300, 300);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/QRCodeDisplay.fxml"));
            Parent root = loader.load();
            
            // On peut accéder aux éléments directement si on n'a pas de contrôleur dédié pour le display simple
            Label lblTitle = (Label) root.lookup("#lblEventTitle");
            ImageView imgView = (ImageView) root.lookup("#imgQRCode");
            Button btnClose = (Button) root.lookup("#btnClose"); // Le bouton Fermer

            lblTitle.setText(selected.getTitre());
            imgView.setImage(qrImage);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("QR Code - " + selected.getTitre());
            stage.setScene(new Scene(root));
            
            // Correction pour le bouton fermer sans contrôleur
            if (btnClose != null) btnClose.setOnAction(e -> stage.close());
            
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de générer le QR Code : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(ActionEvent event) throws IOException {
        utils.UserSession.clear();
        utils.SceneManager.switchScene("/login.fxml", "Campus Access");
    }

    private void changeScene(ActionEvent event, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.setTitle("Détails");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
