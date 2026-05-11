package gui;

import entities.Enseignant;
import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import entities.Etudiant;
import entities.Evaluation;
import entities.Score;
import entities.Soumission;
import jakarta.mail.MessagingException;
import services.AiCorrectionService;
import services.EtudiantService;
import services.EvaluationService;
import services.MailService;
import services.ScoreService;
import services.SoumissionService;
import utils.UserSession;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ScoreCreateController implements MainControllerAware {

    @FXML
    private ComboBox<Soumission> soumissionComboBox;
    @FXML
    private VBox soumissionDetailsBox;
    @FXML
    private Label submissionDateLabel;
    @FXML
    private Label studentCommentLabel;
    @FXML
    private Hyperlink downloadPdfLink;
    @FXML
    private TextField noteField;
    @FXML
    private TextField noteSurField;
    @FXML
    private TextArea commentaireArea;
    @FXML
    private Button cancelButton;
    @FXML
    private Button aiCorrectionButton;
    @FXML
    private Label aiStatusLabel;
    @FXML
    private TextArea consigneArea;

    private final ScoreService scoreService = new ScoreService();
    private final SoumissionService soumissionService = new SoumissionService();
    private final EvaluationService evaluationService = new EvaluationService();
    private final AiCorrectionService aiCorrectionService = new AiCorrectionService();
    private final EtudiantService etudiantService = new EtudiantService();
    private final MailService mailService = new MailService();

    private Soumission selectedSoumission;
    private String teacherId = "PROF001";
    private MainLayoutEnseignantController mainController;

    @FXML
    public void initialize() {
        Utilisateur currentUser = UserSession.getCurrentUser();
        if (currentUser instanceof Enseignant enseignant) {
            teacherId = enseignant.getMatriculeEnseignant();
        }

        loadSoumissions();
        soumissionComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedSoumission = newVal;
                showSoumissionDetails(newVal);
                try {
                    Evaluation evaluation = evaluationService.getById(newVal.getEvaluationId());
                    if (evaluation != null) {
                        noteSurField.setText(String.valueOf(evaluation.getNoteMax()));
                    }
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de l'évaluation: " + e.getMessage());
                }
            }
        });

        // ...existing code...
    }

    @Override
    public void setMainController(MainLayoutEnseignantController controller) {
        mainController = controller;
    }

    public void setSoumission(Soumission soumission) {
        selectedSoumission = soumission;
        if (soumission != null) {
            soumissionComboBox.setValue(soumission);
            soumissionComboBox.setDisable(true);
        }
    }

    public void setTeacherId(String id) {
        teacherId = id;
    }

    private void loadSoumissions() {
        try {
            var soumissions = soumissionService.getAll();
            var evaluations = evaluationService.getAll();

            var pendingSoumissions = soumissions.stream()
                    .filter(soumission -> {
                        try {
                            if (scoreService.getBySoumissionId(soumission.getId()) != null) {
                                return false;
                            }
                        } catch (SQLException e) {
                            return false;
                        }
                        Evaluation evaluation = evaluations.stream()
                                .filter(item -> item.getId() == soumission.getEvaluationId())
                                .findFirst()
                                .orElse(null);
                        return evaluation != null && teacherId.equals(evaluation.getIdEnseignant());
                    })
                    .toList();

            soumissionComboBox.setItems(javafx.collections.FXCollections.observableArrayList(pendingSoumissions));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des soumissions: " + e.getMessage());
        }
    }

    private String buildLabel(Soumission soumission) {
        try {
            Evaluation evaluation = evaluationService.getById(soumission.getEvaluationId());
            String title = evaluation != null ? evaluation.getTitre() : "Evaluation #" + soumission.getEvaluationId();
            return soumission.getIdEtudiant() + " - " + title;
        } catch (SQLException e) {
            return soumission.getIdEtudiant() + " - Evaluation #" + soumission.getEvaluationId();
        }
    }

    private void showSoumissionDetails(Soumission soumission) {
        soumissionDetailsBox.setVisible(true);
        submissionDateLabel.setText("Date de soumission: " + soumission.getDateSoumission().toLocalDate());
        studentCommentLabel.setText(soumission.getCommentaireEtudiant() == null || soumission.getCommentaireEtudiant().isBlank()
                ? "Aucun commentaire etudiant."
                : "Commentaire: " + soumission.getCommentaireEtudiant());
        downloadPdfLink.setVisible(soumission.getPdfFilename() != null && !soumission.getPdfFilename().isBlank());
    }

    @FXML
    private void handleDownloadSubmissionPdf() {
        if (selectedSoumission != null && selectedSoumission.getPdfFilename() != null) {
            downloadPdf(selectedSoumission.getPdfFilename());
        }
    }

    @FXML
    private void handleCancel() {
        if (mainController != null) {
            mainController.showMesCorrections();
        }
    }

    @FXML
    private void handleAiAutoCorrection() {
        if (selectedSoumission == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez d'abord sélectionner une soumission à corriger.");
            return;
        }

        if (aiStatusLabel != null) {
            aiStatusLabel.setText("Analyse AI en cours...");
            aiStatusLabel.setStyle("-fx-text-fill: #5eead4;");
        }
        if (aiCorrectionButton != null) {
            aiCorrectionButton.setDisable(true);
        }

        new Thread(() -> {
            try {
                Evaluation evaluation = evaluationService.getById(selectedSoumission.getEvaluationId());
                if (evaluation == null) {
                    javafx.application.Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'évaluation associée.");
                        resetAiStatus();
                    });
                    return;
                }

                String consigne = (consigneArea != null) ? consigneArea.getText() : null;
                AiCorrectionService.CorrectionSuggestion suggestion = aiCorrectionService.suggestCorrection(selectedSoumission, evaluation, consigne);

                javafx.application.Platform.runLater(() -> {
                    noteField.setText(String.valueOf(suggestion.suggestedNote));
                    noteSurField.setText(String.valueOf(suggestion.noteSur));
                    commentaireArea.setText(suggestion.suggestedComment);

                    if (aiStatusLabel != null) {
                        aiStatusLabel.setText("Suggestion AI appliquée! " + suggestion.suggestedNote + "/" + suggestion.noteSur);
                        aiStatusLabel.setStyle("-fx-text-fill: #4ade80;");
                    }

                    Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                    infoAlert.setTitle("Suggestion AI");
                    infoAlert.setHeaderText("Correction suggérée par l'IA");
                    infoAlert.setContentText("Note suggérée: " + suggestion.suggestedNote + "/" + suggestion.noteSur + "\n\n" +
                            "Raisonnement:\n" + suggestion.aiReasoning + "\n\n" +
                            "Vous pouvez modifier la note et le commentaire avant d'enregistrer.");
                    infoAlert.showAndWait();

                    resetAiStatus();
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur AI", "Erreur lors de l'analyse AI: " + e.getMessage());
                    resetAiStatus();
                });
            }
        }).start();
    }

    private void resetAiStatus() {
        if (aiCorrectionButton != null) {
            aiCorrectionButton.setDisable(false);
        }
    }

    @FXML
    private void handleSave() {
        if (!ValidationUtils.validateNotNull(selectedSoumission, "La soumission")) {
            return;
        }
        if (!ValidationUtils.validateRequired(noteField.getText(), "La note")) {
            return;
        }

        try {
            Evaluation evaluation = evaluationService.getById(selectedSoumission.getEvaluationId());
            if (evaluation == null || !ValidationUtils.validateDoubleRange(noteField.getText(), 0, evaluation.getNoteMax(), "La note")) {
                return;
            }

            Score score = new Score();
            score.setSoumissionId(selectedSoumission.getId());
            score.setNote(Double.parseDouble(noteField.getText().trim()));
            score.setNoteSur(Double.parseDouble(noteSurField.getText().trim()));
            score.setCommentaireEnseignant(commentaireArea.getText().trim());
            score.setDateCorrection(LocalDateTime.now());
            score.setStatutCorrection("corrige");
            scoreService.add(score);

            // Send email notification to student (asynchronously)
            sendScoreNotificationEmail(score, selectedSoumission, evaluation, false);

            showAlert(Alert.AlertType.INFORMATION, "Succes", "Correction enregistree avec succes.");
            handleCancel();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La note doit etre un nombre valide.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
        }
    }

    private void downloadPdf(String filename) {
        try {
            File pdfFile = Paths.get("uploads", filename).toFile();
            if (!pdfFile.exists()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le fichier PDF n'existe pas.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Telecharger le PDF");
            fileChooser.setInitialFileName(filename);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            Stage stage = (Stage) downloadPdfLink.getScene().getWindow();
            File targetFile = fileChooser.showSaveDialog(stage);
            if (targetFile != null) {
                Files.copy(pdfFile.toPath(), targetFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                showAlert(Alert.AlertType.INFORMATION, "Succes", "PDF telecharge avec succes.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de telecharger le PDF: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void sendScoreNotificationEmail(Score score, Soumission soumission, Evaluation evaluation, boolean isUpdate) {
        new Thread(() -> {
            try {
                // Get student by matricule (idEtudiant is the matricule)
                Etudiant etudiant = etudiantService.getByMatricule(soumission.getIdEtudiant());
                if (etudiant == null || etudiant.getEmail() == null || etudiant.getEmail().isBlank()) {
                    System.err.println("Impossible d'envoyer l'email: étudiant ou email non trouvé");
                    return;
                }

                String studentName = etudiant.getPrenom() + " " + etudiant.getNom();
                double pourcentage = (score.getNote() / score.getNoteSur()) * 100;

                mailService.sendScoreNotificationEmail(
                    etudiant.getEmail(),
                    studentName,
                    evaluation.getTitre(),
                    evaluation.getTypeEvaluation(),
                    score.getNote(),
                    score.getNoteSur(),
                    pourcentage,
                    score.getCommentaireEnseignant(),
                    score.getDateCorrection(),
                    isUpdate
                );

                System.out.println("Email de notification envoyé à: " + etudiant.getEmail());
            } catch (MessagingException e) {
                System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
            } catch (SQLException e) {
                System.err.println("Erreur SQL lors de la récupération de l'étudiant: " + e.getMessage());
            }
        }).start();
    }
}

