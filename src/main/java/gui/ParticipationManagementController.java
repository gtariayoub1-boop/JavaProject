package gui;

import entities.ParticipationEvenement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.ParticipationEvenementService;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.stage.FileChooser;
import utils.PDFExporter;
import java.io.File;
import java.util.List;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ParticipationManagementController {

    @FXML
    private TableView<ParticipationEvenement> participationTable;
    @FXML
    private TextField searchField;
    @FXML
    private javafx.scene.control.Button btnPDF;
    @FXML
    private TableColumn<ParticipationEvenement, String> colNom;
    @FXML
    private TableColumn<ParticipationEvenement, String> colPrenom;
    @FXML
    private TableColumn<ParticipationEvenement, String> colTel;
    @FXML
    private TableColumn<ParticipationEvenement, String> colEmail;
    @FXML
    private TableColumn<ParticipationEvenement, String> colAnnee;
    @FXML
    private TableColumn<ParticipationEvenement, String> colDescription;
    @FXML
    private TableColumn<ParticipationEvenement, String> colEvent;
    @FXML
    private TableColumn<ParticipationEvenement, String> colStatut;
    @FXML
    private TableColumn<ParticipationEvenement, LocalDateTime> colDate;

    private ParticipationEvenementService ps = new ParticipationEvenementService();
    private ObservableList<ParticipationEvenement> participationList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAnnee.setCellValueFactory(new PropertyValueFactory<>("anneeScolaire"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("descriptionParticipant"));
        colEvent.setCellValueFactory(new PropertyValueFactory<>("titreEvenement"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_inscription"));

        // -- SEARCH & SORT LOGIC --
        FilteredList<ParticipationEvenement> filteredData = new FilteredList<>(participationList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(p -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (p.getNom().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (p.getPrenom().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (p.getTitreEvenement().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<ParticipationEvenement> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(participationTable.comparatorProperty());
        participationTable.setItems(sortedData);

        loadParticipations();
    }

    private void loadParticipations() {
        try {
            participationList.setAll(ps.getAll());
            // Table items are bound to SortedList
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleExportPDF(ActionEvent event) {
        List<ParticipationEvenement> currentList = participationTable.getItems();
        if (currentList.isEmpty()) {
            showAlert("Info", "La liste est vide.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer la liste des participations PDF");
        fileChooser.setInitialFileName("Participations.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(btnPDF.getScene().getWindow());
        if (file != null) {
            try {
                PDFExporter.exportParticipationList(currentList, file.getAbsolutePath());
                showAlert("Succès", "Le PDF a été généré avec succès !");
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la génération du PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void handleRetour(ActionEvent event) {
        ((Stage) btnPDF.getScene().getWindow()).close();
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            utils.UserSession.clear();
            utils.SceneManager.switchScene("/login.fxml", "Campus Access");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
