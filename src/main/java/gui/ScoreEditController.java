package gui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ScoreEditController implements MainControllerAware {

    @FXML
    private Label studentLabel;
    @FXML
    private Label evaluationTitleLabel;
    @FXML
    private Label evaluationTypeLabel;
    @FXML
    private Label studentIdLabel;
    @FXML
    private Label submissionDateLabel;
    @FXML
    private Hyperlink evalPdfLink;
    @FXML
    private Hyperlink submissionPdfLink;
    @FXML
    private VBox studentCommentBox;
    @FXML
    private Label studentCommentLabel;
    @FXML
    private Label currentScoreLabel;
    @FXML
    private Label currentPercentageLabel;
    @FXML
    private Label currentDateLabel;
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

    private Score score;
    private Soumission soumission;
    private Evaluation evaluation;
    private MainLayoutEnseignantController mainController;

    @Override
    public void setMainController(MainLayoutEnseignantController controller) {
        mainController = controller;
    }

    public void setScore(Score value) {
        score = value;
        loadData();
        populateFields();
    }

    private void loadData() {
        if (score == null) {
            return;
        }
        try {
            soumission = soumissionService.getById(score.getSoumissionId());
            if (soumission != null) {
                evaluation = evaluationService.getById(soumission.getEvaluationId());
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des donnees: " + e.getMessage());
        }
    }

    private void populateFields() {
        if (score == null || soumission == null) {
            return;
        }

        studentLabel.setText(soumission.getIdEtudiant());
        studentIdLabel.setText(soumission.getIdEtudiant());
        submissionDateLabel.setText(soumission.getDateSoumission().toLocalDate().toString());
        studentCommentBox.setVisible(soumission.getCommentaireEtudiant() != null && !soumission.getCommentaireEtudiant().isBlank());
        studentCommentLabel.setText(soumission.getCommentaireEtudiant());
        submissionPdfLink.setVisible(soumission.getPdfFilename() != null && !soumission.getPdfFilename().isBlank());

        if (evaluation != null) {
            evaluationTitleLabel.setText(evaluation.getTitre());
            evaluationTypeLabel.setText(evaluation.getTypeEvaluation());
            evalPdfLink.setVisible(evaluation.getPdfFilename() != null && !evaluation.getPdfFilename().isBlank());
        } else {
            evaluationTitleLabel.setText("Evaluation #" + soumission.getEvaluationId());
            evaluationTypeLabel.setText("-");
            evalPdfLink.setVisible(false);
        }

        currentScoreLabel.setText("Note: " + score.getNote() + "/" + score.getNoteSur());
        currentPercentageLabel.setText("Pourcentage: " + (score.getPourcentage() != null ? score.getPourcentage() + "%" : "-"));
        currentDateLabel.setText("Corrige le: " + (score.getDateCorrection() != null ? score.getDateCorrection().toLocalDate() : "-"));
        noteField.setText(String.valueOf(score.getNote()));
        noteSurField.setText(String.valueOf(score.getNoteSur()));
        commentaireArea.setText(score.getCommentaireEnseignant());
    }

    @FXML
    private void handleDownloadEvalPdf() {
        if (evaluation != null && evaluation.getPdfFilename() != null) {
            downloadPdf(evaluation.getPdfFilename());
        }
    }

    @FXML
    private void handleDownloadSubmissionPdf() {
        if (soumission != null && soumission.getPdfFilename() != null) {
            downloadPdf(soumission.getPdfFilename());
        }
    }

    @FXML
    private void handleAiAutoCorrection() {
        if (soumission == null || evaluation == null) {
            showAlert("Erreur", "Veuillez d'abord charger les données de la soumission.");
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
                String consigne = (consigneArea != null) ? consigneArea.getText() : null;
                AiCorrectionService.CorrectionSuggestion suggestion = aiCorrectionService.suggestCorrection(soumission, evaluation, consigne);

                javafx.application.Platform.runLater(() -> {
                    noteField.setText(String.valueOf(suggestion.suggestedNote));
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
                    showAlert("Erreur AI", "Erreur lors de l'analyse AI: " + e.getMessage());
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
    private void handleCancel() {
        if (mainController != null) {
            mainController.showScoreDetail(score);
        }
    }

    @FXML
    private void handleSave() {
        if (score == null || !ValidationUtils.validateRequired(noteField.getText(), "La note")) {
            return;
        }

        try {
            if (!ValidationUtils.validateDoubleRange(noteField.getText(), 0, score.getNoteSur(), "La note")) {
                return;
            }
            score.setNote(Double.parseDouble(noteField.getText().trim()));
            score.setCommentaireEnseignant(commentaireArea.getText().trim());
            score.setDateCorrection(LocalDateTime.now());
            scoreService.update(score);

            // Send email notification to student about the modification (asynchronously)
            sendScoreNotificationEmail(score, soumission, evaluation, true);

            handleCancel();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "La note doit etre un nombre valide.");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
        }
    }

    private void downloadPdf(String filename) {
        try {
            File pdfFile = Paths.get("uploads", filename).toFile();
            if (!pdfFile.exists()) {
                return;
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Telecharger le PDF");
            fileChooser.setInitialFileName(filename);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            File targetFile = fileChooser.showSaveDialog(stage);
            if (targetFile != null) {
                Files.copy(pdfFile.toPath(), targetFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ignored) {
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void sendScoreNotificationEmail(Score score, Soumission soumission, Evaluation evaluation, boolean isUpdate) {
        if (soumission == null || evaluation == null) {
            System.err.println("Impossible d'envoyer l'email: soumission ou évaluation non disponible");
            return;
        }
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

                System.out.println("Email de notification de modification envoyé à: " + etudiant.getEmail());
            } catch (MessagingException e) {
                System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
            } catch (SQLException e) {
                System.err.println("Erreur SQL lors de la récupération de l'étudiant: " + e.getMessage());
            }
        }).start();
    }
}

