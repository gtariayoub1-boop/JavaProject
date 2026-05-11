package gui;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

public class ScoreDetailController implements MainControllerAware {

    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button backButton;
    @FXML
    private Label evaluationTitleLabel;
    @FXML
    private Label evaluationTypeLabel;
    @FXML
    private Label courseLabel;
    @FXML
    private Label studentLabel;
    @FXML
    private Label submissionDateLabel;
    @FXML
    private Label submissionStatusLabel;
    @FXML
    private Label noteLabel;
    @FXML
    private Label pourcentageLabel;
    @FXML
    private Label resultatLabel;
    @FXML
    private Label correctionStatusLabel;
    @FXML
    private Label correctionDateLabel;
    @FXML
    private Label mentionLabel;
    @FXML
    private Label rankLabel;
    @FXML
    private Label maxNoteLabel;
    @FXML
    private Label minNoteLabel;
    @FXML
    private BarChart<String, Number> gradeDistributionChart;
    @FXML
    private Label studentPositionLabel;
    @FXML
    private VBox teacherCommentBox;
    @FXML
    private Label teacherCommentLabel;
    @FXML
    private VBox studentCommentBox;
    @FXML
    private Label studentCommentLabel;
    @FXML
    private Hyperlink evalPdfLink;
    @FXML
    private Hyperlink submissionPdfLink;
    @FXML
    private Button viewSubmissionButton;

    private final ScoreService scoreService = new ScoreService();
    private final SoumissionService soumissionService = new SoumissionService();
    private final EvaluationService evaluationService = new EvaluationService();

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
        displayDetails();
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
            showAlert("Erreur", "Erreur lors du chargement des données: " + e.getMessage());
        }
    }

    private void displayDetails() {
        if (score == null || soumission == null) {
            return;
        }

        if (evaluation != null) {
            evaluationTitleLabel.setText(evaluation.getTitre());
            evaluationTypeLabel.setText(evaluation.getTypeEvaluation());
            courseLabel.setText(evaluation.getCoursId() != null ? "Cours #" + evaluation.getCoursId() : "-");
            evalPdfLink.setVisible(evaluation.getPdfFilename() != null && !evaluation.getPdfFilename().isBlank());
        } else {
            evaluationTitleLabel.setText("Evaluation #" + soumission.getEvaluationId());
            evaluationTypeLabel.setText("-");
            courseLabel.setText("-");
            evalPdfLink.setVisible(false);
        }

        studentLabel.setText(soumission.getIdEtudiant());
        submissionDateLabel.setText(soumission.getDateSoumission().toLocalDate().toString());
        submissionStatusLabel.setText(soumission.getStatut());
        submissionPdfLink.setVisible(soumission.getPdfFilename() != null && !soumission.getPdfFilename().isBlank());

        noteLabel.setText(score.getNote() + "/" + score.getNoteSur());
        pourcentageLabel.setText(score.getPourcentage() != null ? score.getPourcentage() + "%" : "-");
        resultatLabel.setText(score.isReussi() ? "Reussi" : "Echoue");
        correctionStatusLabel.setText(score.getStatutCorrection());
        correctionDateLabel.setText(score.getDateCorrection() != null ? score.getDateCorrection().toLocalDate().toString() : "-");
        mentionLabel.setText(score.getMention() != null ? score.getMention() : "-");

        // Load and display statistics
        loadAndDisplayStatistics();

        // Load and display grade distribution chart
        loadAndDisplayGradeDistributionChart();

        teacherCommentBox.setVisible(score.getCommentaireEnseignant() != null && !score.getCommentaireEnseignant().isBlank());
        teacherCommentLabel.setText(score.getCommentaireEnseignant());
        studentCommentBox.setVisible(soumission.getCommentaireEtudiant() != null && !soumission.getCommentaireEtudiant().isBlank());
        studentCommentLabel.setText(soumission.getCommentaireEtudiant());
    }

    @FXML
    private void handleEdit() {
        if (score != null && mainController != null) {
            mainController.showScoreEdit(score);
        }
    }

    @FXML
    private void handleDelete() {
        if (score == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la correction");
        alert.setContentText("Voulez-vous supprimer cette correction ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    scoreService.delete(score);
                    handleBack();
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleBack() {
        if (mainController != null) {
            mainController.showMesCorrections();
        }
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("Le detail complet de la soumission reste accessible dans l'espace etudiant.");
        alert.showAndWait();
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
            Stage stage = (Stage) backButton.getScene().getWindow();
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

    private void loadAndDisplayStatistics() {
        if (score == null || soumission == null || evaluation == null) {
            return;
        }
        try {
            int[] stats = scoreService.getEvaluationStatistics(
                evaluation.getId(),
                soumission.getId(),
                score.getNote()
            );
            int rank = stats[0];
            int maxNote = stats[1];
            int minNote = stats[2];
            int totalStudents = stats[3];

            if (totalStudents > 0) {
                rankLabel.setText(rank + " / " + totalStudents + " étudiants");
                maxNoteLabel.setText(maxNote + "/" + score.getNoteSur());
                minNoteLabel.setText(minNote + "/" + score.getNoteSur());
            } else {
                rankLabel.setText("-");
                maxNoteLabel.setText("-");
                minNoteLabel.setText("-");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des statistiques: " + e.getMessage());
            rankLabel.setText("-");
            maxNoteLabel.setText("-");
            minNoteLabel.setText("-");
        }
    }

    private void loadAndDisplayGradeDistributionChart() {
        if (evaluation == null || gradeDistributionChart == null) {
            return;
        }
        try {
            List<Score> allScores = scoreService.getByEvaluationId(evaluation.getId());

            if (allScores.isEmpty()) {
                gradeDistributionChart.setVisible(false);
                return;
            }

            gradeDistributionChart.setVisible(true);
            gradeDistributionChart.getData().clear();

            // Define grade ranges (0-5, 5-10, 10-15, 15-20)
            int[] ranges = new int[4];
            double noteMax = evaluation.getNoteMax();

            for (Score s : allScores) {
                double note = s.getNote();
                double percentage = (note / noteMax) * 20; // Convert to /20 scale

                if (percentage < 5) ranges[0]++;
                else if (percentage < 10) ranges[1]++;
                else if (percentage < 15) ranges[2]++;
                else ranges[3]++;
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre d'étudiants");

            String[] labels = {"0-5", "5-10", "10-15", "15-20"};
            for (int i = 0; i < ranges.length; i++) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(labels[i], ranges[i]);
                series.getData().add(data);
            }

            gradeDistributionChart.getData().add(series);

            // Highlight student's position
            double studentPercentage = (score.getNote() / noteMax) * 20;
            int studentRange;
            if (studentPercentage < 5) studentRange = 0;
            else if (studentPercentage < 10) studentRange = 1;
            else if (studentPercentage < 15) studentRange = 2;
            else studentRange = 3;

            studentPositionLabel.setText("Vous êtes dans la tranche: " + labels[studentRange] +
                " (" + String.format("%.1f", studentPercentage) + "/20)");

        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement du graphique: " + e.getMessage());
            gradeDistributionChart.setVisible(false);
        }
    }
}

