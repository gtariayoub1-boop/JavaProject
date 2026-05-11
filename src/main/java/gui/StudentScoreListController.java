package gui;

import entities.Etudiant;
import entities.Evaluation;
import entities.Score;
import entities.Soumission;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import services.EvaluationService;
import services.ScoreService;
import services.SoumissionService;
import utils.UserSession;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class StudentScoreListController implements MainControllerAwareEtudiant {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> sortByComboBox;

    @FXML
    private ComboBox<String> sortOrderComboBox;

    @FXML
    private ComboBox<Integer> pageSizeComboBox;

    @FXML
    private TableView<ScoreWrapper> notesTableView;

    @FXML
    private TableColumn<ScoreWrapper, Integer> idColumn;

    @FXML
    private TableColumn<ScoreWrapper, String> evaluationColumn;

    @FXML
    private TableColumn<ScoreWrapper, String> typeColumn;

    @FXML
    private TableColumn<ScoreWrapper, String> noteColumn;

    @FXML
    private TableColumn<ScoreWrapper, String> pourcentageColumn;

    @FXML
    private TableColumn<ScoreWrapper, String> statutColumn;

    @FXML
    private TableColumn<ScoreWrapper, String> dateColumn;

    @FXML
    private TableColumn<ScoreWrapper, Void> actionsColumn;

    @FXML
    private Label paginationSummaryLabel;

    @FXML
    private Pagination pagination;

    private final ScoreService scoreService = new ScoreService();
    private final SoumissionService soumissionService = new SoumissionService();
    private final EvaluationService evaluationService = new EvaluationService();

    private final ObservableList<ScoreWrapper> scoreWrappers = FXCollections.observableArrayList();
    private final ObservableList<ScoreWrapper> filteredWrappers = FXCollections.observableArrayList();

    private String currentStudentId = "ETU001";

    private MainLayoutEtudiantController mainController;

    public static class ScoreWrapper {
        private final Score score;
        private final Soumission soumission;
        private final Evaluation evaluation;

        public ScoreWrapper(Score score, Soumission soumission, Evaluation evaluation) {
            this.score = score;
            this.soumission = soumission;
            this.evaluation = evaluation;
        }

        public int getId() {
            return score.getId();
        }

        public Score getScore() {
            return score;
        }

        public Soumission getSoumission() {
            return soumission;
        }

        public Evaluation getEvaluation() {
            return evaluation;
        }
    }

    @FXML
    public void initialize() {
        Utilisateur currentUser = UserSession.getCurrentUser();
        if (currentUser instanceof Etudiant etudiant) {
            currentStudentId = etudiant.getMatricule();
        }
        setupTableColumns();
        setupComboBoxes();
        setupSearch();
        setupPagination();
        loadScores();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        evaluationColumn.setCellValueFactory(cellData -> {
            Evaluation eval = cellData.getValue().getEvaluation();
            return new javafx.beans.property.SimpleStringProperty(eval != null ? eval.getTitre() : "-");
        });

        typeColumn.setCellValueFactory(cellData -> {
            Evaluation eval = cellData.getValue().getEvaluation();
            return new javafx.beans.property.SimpleStringProperty(eval != null ? eval.getTypeEvaluation() : "-");
        });

        noteColumn.setCellValueFactory(cellData -> {
            Score score = cellData.getValue().getScore();
            return new javafx.beans.property.SimpleStringProperty(score.getNote() + "/" + score.getNoteSur());
        });

        pourcentageColumn.setCellValueFactory(cellData -> {
            Score score = cellData.getValue().getScore();
            if (score.getPourcentage() != null) {
                return new javafx.beans.property.SimpleStringProperty(score.getPourcentage() + "%");
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        statutColumn.setCellValueFactory(cellData -> {
            Score score = cellData.getValue().getScore();
            String statut = score.getStatutCorrection();
            return new javafx.beans.property.SimpleStringProperty(
                    "corrige".equals(statut) ? "Corrige" : "En attente"
            );
        });

        statutColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Corrige".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    }
                }
            }
        });

        dateColumn.setCellValueFactory(cellData -> {
            Score score = cellData.getValue().getScore();
            if (score.getDateCorrection() != null) {
                return new javafx.beans.property.SimpleStringProperty(score.getDateCorrection().toLocalDate().toString());
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button voirBtn = new Button("Voir");

            {
                voirBtn.setOnAction(event -> {
                    ScoreWrapper wrapper = getTableView().getItems().get(getIndex());
                    handleVoir(wrapper);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : voirBtn);
            }
        });
    }

    private void setupComboBoxes() {
        sortByComboBox.setItems(FXCollections.observableArrayList("Date", "Evaluation", "Type", "Note"));
        sortByComboBox.getSelectionModel().selectFirst();

        sortOrderComboBox.setItems(FXCollections.observableArrayList("Croissant", "Decroissant"));
        sortOrderComboBox.getSelectionModel().selectFirst();

        pageSizeComboBox.setItems(FXCollections.observableArrayList(10, 20, 50));
        pageSizeComboBox.getSelectionModel().selectFirst();

        sortByComboBox.setOnAction(e -> applyFilters());
        sortOrderComboBox.setOnAction(e -> applyFilters());
        pageSizeComboBox.setOnAction(e -> {
            pagination.setCurrentPageIndex(0);
            applyFilters();
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            pagination.setCurrentPageIndex(0);
            applyFilters();
        });
    }

    private void setupPagination() {
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void loadScores() {
        try {
            List<Soumission> studentSoumissions = soumissionService.getAll().stream()
                    .filter(s -> currentStudentId.equals(s.getIdEtudiant()))
                    .collect(Collectors.toList());

            List<Evaluation> allEvaluations = evaluationService.getAll();

            List<ScoreWrapper> wrappers = studentSoumissions.stream()
                    .map(s -> {
                        try {
                            Score score = scoreService.getBySoumissionId(s.getId());
                            Evaluation eval = allEvaluations.stream()
                                    .filter(e -> e.getId() == s.getEvaluationId())
                                    .findFirst()
                                    .orElse(null);
                            return new ScoreWrapper(score, s, eval);
                        } catch (SQLException e) {
                            return null;
                        }
                    })
                    .filter(w -> w != null && w.getScore() != null)
                    .collect(Collectors.toList());

            scoreWrappers.setAll(wrappers);

            if (wrappers.isEmpty()) {
                showEmptyMessage();
            } else {
                applyFilters();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des notes: " + e.getMessage());
        }
    }

    private void showEmptyMessage() {
        notesTableView.setPlaceholder(new Label("Aucune note disponible pour le moment.\nVos corrections apparaitront ici une fois que l'enseignant aura corrige vos soumissions."));
        paginationSummaryLabel.setText("0 note");
        pagination.setPageCount(1);
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        List<ScoreWrapper> filtered = scoreWrappers.stream()
                .filter(w -> {
                    String evalName = w.getEvaluation() != null ? w.getEvaluation().getTitre().toLowerCase() : "";
                    String type = w.getEvaluation() != null ? w.getEvaluation().getTypeEvaluation().toLowerCase() : "";
                    return evalName.contains(search) || type.contains(search);
                })
                .collect(Collectors.toList());

        String sortBy = sortByComboBox.getValue();
        boolean ascending = sortOrderComboBox.getValue() == null || sortOrderComboBox.getValue().equals("Croissant");

        filtered.sort((w1, w2) -> {
            int cmp = 0;
            if ("Date".equals(sortBy)) {
                cmp = w1.getScore().getDateCorrection().compareTo(w2.getScore().getDateCorrection());
            } else if ("Evaluation".equals(sortBy)) {
                String n1 = w1.getEvaluation() != null ? w1.getEvaluation().getTitre() : "";
                String n2 = w2.getEvaluation() != null ? w2.getEvaluation().getTitre() : "";
                cmp = n1.compareToIgnoreCase(n2);
            } else if ("Type".equals(sortBy)) {
                String t1 = w1.getEvaluation() != null ? w1.getEvaluation().getTypeEvaluation() : "";
                String t2 = w2.getEvaluation() != null ? w2.getEvaluation().getTypeEvaluation() : "";
                cmp = t1.compareToIgnoreCase(t2);
            } else if ("Note".equals(sortBy)) {
                cmp = Double.compare(w1.getScore().getNote(), w2.getScore().getNote());
            }
            return ascending ? cmp : -cmp;
        });

        int pageSize = pageSizeComboBox.getValue() != null ? pageSizeComboBox.getValue() : 10;
        int totalItems = filtered.size();
        int pageCount = Math.max((int) Math.ceil((double) totalItems / pageSize), 1);
        pagination.setPageCount(pageCount);

        int currentPage = Math.min(pagination.getCurrentPageIndex(), pageCount - 1);
        int fromIndex = currentPage * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        List<ScoreWrapper> pageItems = filtered.subList(fromIndex, Math.max(fromIndex, toIndex));
        filteredWrappers.setAll(pageItems);
        notesTableView.setItems(filteredWrappers);

        paginationSummaryLabel.setText(totalItems + " note" + (totalItems > 1 ? "s" : ""));
    }

    @Override
    public void setMainController(MainLayoutEtudiantController controller) {
        this.mainController = controller;
    }

    private void handleVoir(ScoreWrapper wrapper) {
        if (!currentStudentId.equals(wrapper.getSoumission().getIdEtudiant())) {
            showAlert(Alert.AlertType.ERROR, "Acces refuse", "Vous ne pouvez pas consulter la note d'un autre etudiant.");
            return;
        }

        if (mainController != null) {
            mainController.showStudentScoreDetail(wrapper.getScore());
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
