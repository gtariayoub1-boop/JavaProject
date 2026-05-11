package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import entities.Evaluation;
import entities.Soumission;
import services.EvaluationService;
import services.SoumissionService;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class EvaluationDetailController implements MainControllerAware {

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button backButton;

    @FXML
    private Label idLabel;

    @FXML
    private Label titleLabel;

    @FXML
    private Label typeLabel;

    @FXML
    private Label courseLabel;

    @FXML
    private Label teacherIdLabel;

    @FXML
    private Label creationDateLabel;

    @FXML
    private Label deadlineLabel;

    @FXML
    private Label dateStateLabel;

    @FXML
    private Label maxScoreLabel;

    @FXML
    private Label submissionModeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label shortDescriptionLabel;

    @FXML
    private Label fullDescriptionLabel;

    @FXML
    private Label pdfFileLabel;

    @FXML
    private Button openPdfButton;

    @FXML
    private Button downloadPdfButton;

    @FXML
    private TableView<Soumission> submissionTableView;

    @FXML
    private TableColumn<Soumission, String> studentIdColumn;

    @FXML
    private TableColumn<Soumission, String> submissionDateColumn;

    @FXML
    private TableColumn<Soumission, String> submissionStatusColumn;

    @FXML
    private TableColumn<Soumission, String> submissionPdfColumn;

    @FXML
    private TableColumn<Soumission, Void> submissionActionColumn;

    private Evaluation evaluation;
    private final EvaluationService evaluationService = new EvaluationService();
    private final SoumissionService soumissionService = new SoumissionService();

    private MainLayoutEnseignantController mainController;

    @FXML
    public void initialize() {
        setupSubmissionTable();
    }

    private void setupSubmissionTable() {
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("idEtudiant"));
        submissionDateColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDateSoumission().toLocalDate().toString()
            );
        });
        submissionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        submissionPdfColumn.setCellValueFactory(new PropertyValueFactory<>("pdfFilename"));

        submissionActionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("Voir");
            private final Button gradeBtn = new Button("Noter");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(5, viewBtn, gradeBtn);

            {
                viewBtn.setOnAction(event -> {
                    Soumission sub = getTableView().getItems().get(getIndex());
                    handleViewSubmission(sub);
                });
                gradeBtn.setOnAction(event -> {
                    Soumission sub = getTableView().getItems().get(getIndex());
                    handleGradeSubmission(sub);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    @Override
    public void setMainController(MainLayoutEnseignantController controller) {
        this.mainController = controller;
    }

    public void setEvaluation(Evaluation eval) {
        this.evaluation = eval;
        displayEvaluationDetails();
        loadSubmissions();
    }

    private void displayEvaluationDetails() {
        if (evaluation == null) return;

        idLabel.setText(String.valueOf(evaluation.getId()));
        titleLabel.setText(evaluation.getTitre());
        typeLabel.setText(evaluation.getTypeEvaluation());
        courseLabel.setText(evaluation.getCoursId() != null ? String.valueOf(evaluation.getCoursId()) : "-");
        teacherIdLabel.setText(evaluation.getIdEnseignant());
        creationDateLabel.setText(evaluation.getDateCreation() != null ? evaluation.getDateCreation().toLocalDate().toString() : "-");
        deadlineLabel.setText(evaluation.getDateLimite() != null ? evaluation.getDateLimite().toLocalDate().toString() : "-");
        maxScoreLabel.setText(String.valueOf(evaluation.getNoteMax()));
        submissionModeLabel.setText(evaluation.getModeRemise());
        statusLabel.setText(evaluation.getStatut());

        String desc = evaluation.getDescription();
        if (desc != null && desc.length() > 100) {
            shortDescriptionLabel.setText(desc.substring(0, 100) + "...");
            fullDescriptionLabel.setText(desc);
        } else {
            shortDescriptionLabel.setText(desc);
            fullDescriptionLabel.setText(desc);
        }

        LocalDateTime now = LocalDateTime.now();
        if (evaluation.getDateLimite() != null) {
            if (now.isAfter(evaluation.getDateLimite())) {
                dateStateLabel.setText("Expirée");
                dateStateLabel.setStyle("-fx-text-fill: red;");
            } else {
                dateStateLabel.setText("En cours");
                dateStateLabel.setStyle("-fx-text-fill: green;");
            }
        } else {
            dateStateLabel.setText("-");
        }

        if (evaluation.getPdfFilename() != null && !evaluation.getPdfFilename().isEmpty()) {
            pdfFileLabel.setText(evaluation.getPdfFilename());
            openPdfButton.setDisable(false);
            downloadPdfButton.setDisable(false);
        } else {
            pdfFileLabel.setText("Aucun PDF");
            openPdfButton.setDisable(true);
            downloadPdfButton.setDisable(true);
        }
    }

    private void loadSubmissions() {
        if (evaluation == null) return;

        try {
            List<Soumission> allSubmissions = soumissionService.getAll();
            List<Soumission> evalSubmissions = allSubmissions.stream()
                .filter(s -> s.getEvaluationId() == evaluation.getId())
                .collect(Collectors.toList());

            ObservableList<Soumission> submissions = FXCollections.observableArrayList(evalSubmissions);
            submissionTableView.setItems(submissions);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des soumissions: " + e.getMessage());
        }
    }

    @FXML
    private void handleEdit() {
        if (evaluation == null || mainController == null) return;
        mainController.showEvaluationEdit(evaluation);
    }

    @FXML
    private void handleDelete() {
        if (evaluation == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'évaluation");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer l'évaluation \"" + evaluation.getTitre() + "\" ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    evaluationService.delete(evaluation);
                    navigateToList();
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleBack() {
        navigateToList();
    }

    @FXML
    private void handleOpenPdf() {
        if (evaluation == null || evaluation.getPdfFilename() == null) return;

        try {
            File pdfFile = Paths.get("uploads", evaluation.getPdfFilename()).toFile();
            if (pdfFile.exists()) {
                Desktop.getDesktop().open(pdfFile);
            } else {
                showAlert("Erreur", "Le fichier PDF n'existe pas.");
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le PDF: " + e.getMessage());
        }
    }

    @FXML
    private void handleDownloadPdf() {
        if (evaluation == null || evaluation.getPdfFilename() == null) return;

        try {
            File pdfFile = Paths.get("uploads", evaluation.getPdfFilename()).toFile();
            if (pdfFile.exists()) {
                javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                fileChooser.setTitle("Enregistrer le PDF");
                fileChooser.setInitialFileName(evaluation.getPdfFilename());
                fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
                );

                Stage stage = (Stage) downloadPdfButton.getScene().getWindow();
                File targetFile = fileChooser.showSaveDialog(stage);

                if (targetFile != null) {
                    java.nio.file.Files.copy(pdfFile.toPath(), targetFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "PDF téléchargé avec succès!");
                }
            } else {
                showAlert("Erreur", "Le fichier PDF n'existe pas.");
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de télécharger le PDF: " + e.getMessage());
        }
    }

    private void handleViewSubmission(Soumission sub) {
        if (sub.getPdfFilename() != null && !sub.getPdfFilename().isEmpty()) {
            try {
                File pdfFile = Paths.get("uploads", sub.getPdfFilename()).toFile();
                if (pdfFile.exists()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    showAlert("Erreur", "Le fichier de soumission n'existe pas.");
                }
            } catch (IOException e) {
                showAlert("Erreur", "Impossible d'ouvrir le fichier: " + e.getMessage());
            }
        } else {
            showAlert("Information", "Aucun fichier associé à cette soumission.");
        }
    }

    private void handleGradeSubmission(Soumission sub) {
        showAlert(Alert.AlertType.INFORMATION, "Information", "Fonctionnalité de notation à implémenter.");
    }

    private void navigateToList() {
        if (mainController != null) {
            mainController.showMesEvaluations();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

