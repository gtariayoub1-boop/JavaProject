package gui;

import entities.Evaluation;
import entities.Soumission;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.EvaluationService;
import services.SoumissionService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class SoumissionEditController implements MainControllerAwareEtudiant {

    @FXML
    private ComboBox<Evaluation> evaluationComboBox;

    @FXML
    private TextField studentIdField;

    @FXML
    private TextField pdfPathField;

    @FXML
    private Button browsePdfButton;

    @FXML
    private TextArea commentArea;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    @FXML
    private Hyperlink evalPdfLink;

    private Soumission soumission;
    private String studentId = "ETU001";
    private final SoumissionService soumissionService = new SoumissionService();
    private final EvaluationService evaluationService = new EvaluationService();
    private File selectedPdfFile;

    private MainLayoutEtudiantController mainController;

    @FXML
    public void initialize() {
        loadEvaluations();

        evaluationComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            evalPdfLink.setVisible(newVal != null && newVal.getPdfFilename() != null);
        });
    }

    public void setSoumission(Soumission soum) {
        this.soumission = soum;
        populateFields();
    }

    @Override
    public void setMainController(MainLayoutEtudiantController controller) {
        this.mainController = controller;
    }

    public void setStudentId(String id) {
        this.studentId = id;
        studentIdField.setText(id);
    }

    private void loadEvaluations() {
        try {
            List<Evaluation> allEvals = evaluationService.getAll();
            evaluationComboBox.setItems(javafx.collections.FXCollections.observableArrayList(allEvals));

            evaluationComboBox.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Evaluation item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getTitre());
                }
            });
            evaluationComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Evaluation item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "Selectionner une evaluation" : item.getTitre());
                }
            });
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des evaluations: " + e.getMessage());
        }
    }

    private void populateFields() {
        if (soumission == null) {
            return;
        }

        studentIdField.setText(soumission.getIdEtudiant());
        commentArea.setText(soumission.getCommentaireEtudiant());

        try {
            Evaluation currentEval = evaluationService.getById(soumission.getEvaluationId());
            if (currentEval != null) {
                for (Evaluation eval : evaluationComboBox.getItems()) {
                    if (eval.getId() == currentEval.getId()) {
                        evaluationComboBox.setValue(eval);
                        break;
                    }
                }
                evalPdfLink.setVisible(currentEval.getPdfFilename() != null);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de l'evaluation: " + e.getMessage());
        }

        if (soumission.getPdfFilename() != null && !soumission.getPdfFilename().isEmpty()) {
            pdfPathField.setText(soumission.getPdfFilename());
        }
    }

    @FXML
    private void handleBrowsePdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un nouveau fichier PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );

        Stage stage = (Stage) browsePdfButton.getScene().getWindow();
        selectedPdfFile = fileChooser.showOpenDialog(stage);

        if (selectedPdfFile != null) {
            pdfPathField.setText(selectedPdfFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleDownloadEvalPdf() {
        Evaluation eval = evaluationComboBox.getValue();
        if (eval != null && eval.getPdfFilename() != null) {
            downloadPdf(eval.getPdfFilename());
        }
    }

    @FXML
    private void handleCancel() {
        navigateToDetail();
    }

    @FXML
    private void handleSave() {
        if (soumission == null) {
            return;
        }

        if (!validateInput()) {
            return;
        }

        Evaluation eval = evaluationComboBox.getValue();

        try {
            soumission.setEvaluationId(eval.getId());
            soumission.setIdEtudiant(studentIdField.getText().trim());
            soumission.setCommentaireEtudiant(commentArea.getText().trim());
            soumission.setDateSoumission(LocalDateTime.now());

            if (LocalDateTime.now().isAfter(eval.getDateLimite())) {
                soumission.setStatut("en_retard");
            } else {
                soumission.setStatut("soumise");
            }

            if (selectedPdfFile != null) {
                String pdfFilename = savePdfFile(selectedPdfFile);
                soumission.setPdfFilename(pdfFilename);
            }

            soumissionService.update(soumission);

            showAlert(Alert.AlertType.INFORMATION, "Succes", "Soumission mise a jour avec succes.");
            navigateToDetail();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise a jour: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (!ValidationUtils.validateNotNull(evaluationComboBox.getValue(), "L'evaluation")) {
            return false;
        }

        Evaluation eval = evaluationComboBox.getValue();

        if ("fermee".equals(eval.getStatut())) {
            ValidationUtils.showError("Cette evaluation est fermee. Modification interdite.");
            return false;
        }

        if (LocalDateTime.now().isAfter(eval.getDateLimite())) {
            ValidationUtils.showError("La date limite est depassee. Modification interdite.");
            return false;
        }

        if (selectedPdfFile != null && selectedPdfFile.length() > 10 * 1024 * 1024) {
            ValidationUtils.showError("Le fichier PDF ne doit pas depasser 10 Mo.");
            return false;
        }

        String commentaire = commentArea.getText();
        if (commentaire != null && !commentaire.isEmpty()) {
            if (!ValidationUtils.validateMaxLength(commentaire, 500, "Le commentaire")) {
                return false;
            }
        }

        return true;
    }

    private String savePdfFile(File sourceFile) {
        try {
            Path uploadDir = Paths.get("uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String filename = "soum_" + System.currentTimeMillis() + "_" + sourceFile.getName();
            Path targetPath = uploadDir.resolve(filename);
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du PDF: " + e.getMessage());
        }
    }

    private void downloadPdf(String filename) {
        try {
            File pdfFile = Paths.get("uploads", filename).toFile();
            if (pdfFile.exists()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Telecharger le PDF");
                fileChooser.setInitialFileName(filename);
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
                );

                Stage stage = (Stage) evalPdfLink.getScene().getWindow();
                File targetFile = fileChooser.showSaveDialog(stage);

                if (targetFile != null) {
                    Files.copy(pdfFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    showAlert(Alert.AlertType.INFORMATION, "Succes", "PDF telecharge avec succes.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le fichier PDF n'existe pas.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de telecharger le PDF: " + e.getMessage());
        }
    }

    private void navigateToDetail() {
        if (mainController != null && soumission != null) {
            mainController.showSoumissionDetail(soumission);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
