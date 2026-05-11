package gui;

import entities.Enseignant;
import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import entities.Cours;
import entities.Evaluation;
import services.CoursService;
import services.EvaluationService;
import utils.UserSession;
import javafx.util.StringConverter;

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
import java.util.List;

public class EvaluationCreateController implements MainControllerAware {

    @FXML
    private TextField titleField;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ComboBox<Cours> courseComboBox;

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
    private Button createButton;

    private final EvaluationService evaluationService = new EvaluationService();
    private final CoursService coursService = new CoursService();
    private File selectedPdfFile;

    private MainLayoutEnseignantController mainController;

    @Override
    public void setMainController(MainLayoutEnseignantController controller) {
        this.mainController = controller;
    }

    @FXML
    public void initialize() {
        typeComboBox.setItems(javafx.collections.FXCollections.observableArrayList("projet", "examen"));
        typeComboBox.getSelectionModel().selectFirst();

        loadCours();

        submissionModeComboBox.setItems(javafx.collections.FXCollections.observableArrayList("en_ligne", "presentiel"));
        submissionModeComboBox.getSelectionModel().selectFirst();

        statusComboBox.setItems(javafx.collections.FXCollections.observableArrayList("ouverte", "fermee"));
        statusComboBox.getSelectionModel().selectFirst();

        Utilisateur currentUser = UserSession.getCurrentUser();
        if (currentUser instanceof Enseignant enseignant) {
            teacherIdField.setText(enseignant.getMatriculeEnseignant());
        }
    }

    private void loadCours() {
        try {
            List<Cours> coursList = coursService.getAll();
            courseComboBox.setItems(javafx.collections.FXCollections.observableArrayList(coursList));
            
            // Afficher le titre du cours au lieu de l'ID
            courseComboBox.setConverter(new StringConverter<Cours>() {
                @Override
                public String toString(Cours cours) {
                    if (cours == null) {
                        return "";
                    }
                    return cours.getTitre() + " (" + cours.getId() + ")";
                }
                
                @Override
                public Cours fromString(String string) {
                    // Non utilisé pour un ComboBox non éditable
                    return null;
                }
            });
            
            // Sélectionner le premier cours par défaut si disponible
            if (!coursList.isEmpty()) {
                courseComboBox.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les cours: " + e.getMessage());
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
        navigateToList();
    }

    @FXML
    private void handleCreate() {
        if (!validateInput()) {
            return;
        }

        try {
            Evaluation eval = new Evaluation();
            eval.setTitre(titleField.getText().trim());
            eval.setTypeEvaluation(typeComboBox.getValue());
            eval.setDescription(descriptionArea.getText().trim());
            Cours selectedCours = courseComboBox.getValue();
            if (selectedCours != null) {
                eval.setCoursId(selectedCours.getId());
            }
            eval.setIdEnseignant(teacherIdField.getText().trim());

            LocalDate deadlineDate = deadlinePicker.getValue();
            if (deadlineDate != null) {
                eval.setDateLimite(LocalDateTime.of(deadlineDate, LocalTime.of(23, 59)));
            }

            eval.setNoteMax(Double.parseDouble(maxScoreField.getText().trim()));
            eval.setModeRemise(submissionModeComboBox.getValue());
            eval.setStatut(statusComboBox.getValue());

            if (selectedPdfFile != null) {
                String pdfFilename = savePdfFile(selectedPdfFile);
                eval.setPdfFilename(pdfFilename);
            }

            evaluationService.add(eval);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation créée avec succès!");
            navigateToList();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La note maximale doit être un nombre valide.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création: " + e.getMessage());
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

        // Validation PDF: obligatoire pour création
        if (selectedPdfFile == null) {
            ValidationUtils.showError("Le fichier PDF est obligatoire.");
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

    private void navigateToList() {
        if (mainController != null) {
            mainController.showMesEvaluations();
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

