package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import entities.Evaluation;
import services.EvaluationService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EvaluationEditController implements MainControllerAware {

    @FXML
    private TextField titleField;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ComboBox<Integer> courseComboBox;

    @FXML
    private TextField teacherIdField;

    @FXML
    private DatePicker deadlinePicker;

    @FXML
    private TextField maxScoreField;

    @FXML
    private ComboBox<String> submissionModeComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private TextField pdfPathField;

    @FXML
    private Button browsePdfButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    private Evaluation evaluation;
    private final EvaluationService evaluationService = new EvaluationService();
    private File selectedPdfFile;

    private MainLayoutEnseignantController mainController;

    @FXML
    public void initialize() {
        typeComboBox.setItems(javafx.collections.FXCollections.observableArrayList("projet", "examen"));
        courseComboBox.setItems(javafx.collections.FXCollections.observableArrayList(2, 3));
        submissionModeComboBox.setItems(javafx.collections.FXCollections.observableArrayList("en_ligne", "presentiel"));
        statusComboBox.setItems(javafx.collections.FXCollections.observableArrayList("ouverte", "fermee"));
    }

    @Override
    public void setMainController(MainLayoutEnseignantController controller) {
        this.mainController = controller;
    }

    public void setEvaluation(Evaluation eval) {
        this.evaluation = eval;
        populateFields();
    }

    private void populateFields() {
        if (evaluation == null) return;

        titleField.setText(evaluation.getTitre());
        typeComboBox.setValue(evaluation.getTypeEvaluation());
        descriptionArea.setText(evaluation.getDescription());
        courseComboBox.setValue(evaluation.getCoursId());
        teacherIdField.setText(evaluation.getIdEnseignant());

        if (evaluation.getDateLimite() != null) {
            deadlinePicker.setValue(evaluation.getDateLimite().toLocalDate());
        }

        maxScoreField.setText(String.valueOf(evaluation.getNoteMax()));
        submissionModeComboBox.setValue(evaluation.getModeRemise());
        statusComboBox.setValue(evaluation.getStatut());

        if (evaluation.getPdfFilename() != null && !evaluation.getPdfFilename().isEmpty()) {
            pdfPathField.setText(evaluation.getPdfFilename());
        }
    }

    @FXML
    private void handleBrowsePdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier PDF");
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
    private void handleCancel() {
        navigateBack();
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            evaluation.setTitre(titleField.getText().trim());
            evaluation.setTypeEvaluation(typeComboBox.getValue());
            evaluation.setDescription(descriptionArea.getText().trim());
            evaluation.setCoursId(courseComboBox.getValue());
            evaluation.setIdEnseignant(teacherIdField.getText().trim());

            LocalDate deadlineDate = deadlinePicker.getValue();
            if (deadlineDate != null) {
                evaluation.setDateLimite(LocalDateTime.of(deadlineDate, LocalTime.of(23, 59)));
            }

            evaluation.setNoteMax(Double.parseDouble(maxScoreField.getText().trim()));
            evaluation.setModeRemise(submissionModeComboBox.getValue());
            evaluation.setStatut(statusComboBox.getValue());

            if (selectedPdfFile != null) {
                String pdfFilename = savePdfFile(selectedPdfFile);
                evaluation.setPdfFilename(pdfFilename);
            }

            evaluationService.update(evaluation);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation mise à jour avec succès!");
            navigateBack();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La note maximale doit être un nombre valide.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        String titre = titleField.getText();

        // Validation titre: obligatoire, 3-100 caractères
        if (!ValidationUtils.validateRequired(titre, "Le titre")) {
            return false;
        }
        if (!ValidationUtils.validateMinLength(titre, 3, "Le titre")) {
            return false;
        }
        if (!ValidationUtils.validateMaxLength(titre, 100, "Le titre")) {
            return false;
        }

        // Validation type: obligatoire
        if (!ValidationUtils.validateNotNull(typeComboBox.getValue(), "Le type d'évaluation")) {
            return false;
        }

        // Validation cours: obligatoire
        if (!ValidationUtils.validateNotNull(courseComboBox.getValue(), "Le cours")) {
            return false;
        }

        // Validation date limite: obligatoire, >= aujourd'hui
        if (!ValidationUtils.validateDateNotPast(deadlinePicker.getValue(), "La date limite")) {
            return false;
        }

        // Validation note maximale: obligatoire, > 0, <= 100
        if (!ValidationUtils.validateDoubleRange(maxScoreField.getText(), 0.5, 100, "La note maximale")) {
            return false;
        }

        // Validation mode de remise: obligatoire
        if (!ValidationUtils.validateNotNull(submissionModeComboBox.getValue(), "Le mode de remise")) {
            return false;
        }

        // Validation statut: obligatoire
        if (!ValidationUtils.validateNotNull(statusComboBox.getValue(), "Le statut")) {
            return false;
        }

        // Validation description: max 1000 caractères (optionnel)
        String description = descriptionArea.getText();
        if (description != null && !description.isEmpty()) {
            if (!ValidationUtils.validateMaxLength(description, 1000, "La description")) {
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

            String filename = System.currentTimeMillis() + "_" + sourceFile.getName();
            Path targetPath = uploadDir.resolve(filename);
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du PDF: " + e.getMessage());
        }
    }

    private void navigateBack() {
        if (mainController != null && evaluation != null) {
            mainController.showEvaluationDetail(evaluation);
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

