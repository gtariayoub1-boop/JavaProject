package gui;

import entities.Etudiant;
import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import entities.Evaluation;
import entities.Score;
import entities.Soumission;
import services.EvaluationService;
import services.ScoreService;
import services.SoumissionService;
import utils.UserSession;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

/**
 * Contrôleur pour la page détail de la note (vue étudiant)
 * Affichage en lecture seule avec vérification de propriété
 */
public class StudentScoreDetailController implements MainControllerAwareEtudiant {

    @FXML
    private Button backButton;

    @FXML
    private Label evaluationTitleLabel;

    @FXML
    private Label evaluationTypeLabel;

    @FXML
    private Label courseLabel;

    @FXML
    private Label submissionDateLabel;

    @FXML
    private Label submissionStatusLabel;

    @FXML
    private Label noteLabel;

    @FXML
    private Label pourcentageLabel;

    @FXML
    private Label mentionLabel;

    @FXML
    private Label resultatLabel;

    @FXML
    private Label correctionStatusLabel;

    @FXML
    private Label correctionDateLabel;

    @FXML
    private VBox teacherCommentBox;

    @FXML
    private Label teacherCommentLabel;

    @FXML
    private Hyperlink evalPdfLink;

    @FXML
    private Hyperlink submissionPdfLink;

    @FXML
    private Button viewSubmissionButton;

    private Score score;
    private Soumission soumission;
    private Evaluation evaluation;
    private String currentStudentId = "ETU001";

    private final ScoreService scoreService = new ScoreService();
    private final SoumissionService soumissionService = new SoumissionService();
    private final EvaluationService evaluationService = new EvaluationService();

    private MainLayoutEtudiantController mainController;

    public void setScore(Score score) {
        this.score = score;

        // Vérification de sécurité
        if (!verifyAccess()) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé",
                "Vous ne pouvez pas consulter cette note. Cette note appartient à un autre étudiant.");
            handleBack();
            return;
        }

        loadData();
        displayDetails();
    }

    @Override
    public void setMainController(MainLayoutEtudiantController controller) {
        this.mainController = controller;
    }

    public void setStudentId(String studentId) {
        this.currentStudentId = studentId;
    }

    /**
     * Vérifie que l'étudiant connecté est bien le propriétaire de cette note
     */
    private boolean verifyAccess() {
        if (score == null) return false;

        try {
            Soumission soum = soumissionService.getById(score.getSoumissionId());
            if (soum == null) return false;

            return currentStudentId.equals(soum.getIdEtudiant());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la verification de la note: " + e.getMessage());
            return false;
        }
    }

    private void loadData() {
        if (score == null) return;
        try {
            soumission = soumissionService.getById(score.getSoumissionId());
            if (soumission != null) {
                evaluation = evaluationService.getById(soumission.getEvaluationId());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des donnees: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        Utilisateur currentUser = UserSession.getCurrentUser();
        if (currentUser instanceof Etudiant etudiant) {
            currentStudentId = etudiant.getMatricule();
        }
    }

    private void displayDetails() {
        if (score == null || soumission == null) return;

        // Évaluation
        if (evaluation != null) {
            evaluationTitleLabel.setText(evaluation.getTitre());
            evaluationTypeLabel.setText(evaluation.getTypeEvaluation());
            courseLabel.setText(evaluation.getCoursId() != null ? "Cours #" + evaluation.getCoursId() : "-");
            evalPdfLink.setVisible(evaluation.getPdfFilename() != null && !evaluation.getPdfFilename().isEmpty());
        } else {
            evaluationTitleLabel.setText("Évaluation #" + soumission.getEvaluationId());
            evaluationTypeLabel.setText("-");
            courseLabel.setText("-");
            evalPdfLink.setVisible(false);
        }

        // Soumission
        submissionDateLabel.setText(soumission.getDateSoumission().toLocalDate().toString());
        submissionStatusLabel.setText(soumission.getStatut());
        submissionPdfLink.setVisible(soumission.getPdfFilename() != null && !soumission.getPdfFilename().isEmpty());

        // Notation
        noteLabel.setText(score.getNote() + "/" + score.getNoteSur());
        if (score.getPourcentage() != null) {
            pourcentageLabel.setText(score.getPourcentage() + "%");
        }

        // Mention
        if (score.getMention() != null) {
            mentionLabel.setText(score.getMention());
        }

        // Résultat
        if (score.isReussi()) {
            resultatLabel.setText("Réussi");
            resultatLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            resultatLabel.setText("Échoué");
            resultatLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }

        // Statut et date
        correctionStatusLabel.setText("corrigé".equals(score.getStatutCorrection()) ? "Corrigé" : score.getStatutCorrection());
        correctionDateLabel.setText(score.getDateCorrection().toLocalDate().toString());

        // Commentaire de l'enseignant
        if (score.getCommentaireEnseignant() != null && !score.getCommentaireEnseignant().isEmpty()) {
            teacherCommentBox.setVisible(true);
            teacherCommentLabel.setText(score.getCommentaireEnseignant());
        } else {
            teacherCommentBox.setVisible(false);
        }
    }

    @FXML
    private void handleBack() {
        navigateToList();
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
    private void handleViewSubmission() {
        if (soumission == null || mainController == null) return;
        mainController.showSoumissionDetail(soumission);
    }

    private void downloadPdf(String filename) {
        try {
            File pdfFile = Paths.get("uploads", filename).toFile();
            if (pdfFile.exists()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Télécharger le PDF");
                fileChooser.setInitialFileName(filename);
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
                );

                Stage stage = (Stage) backButton.getScene().getWindow();
                File targetFile = fileChooser.showSaveDialog(stage);

                if (targetFile != null) {
                    Files.copy(pdfFile.toPath(), targetFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "PDF téléchargé avec succès!");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le fichier PDF n'existe pas.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de télécharger le PDF: " + e.getMessage());
        }
    }

    private void navigateToList() {
        if (mainController != null) {
            mainController.showMesNotes();
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

