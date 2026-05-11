package gui;

import entities.Enseignant;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import entities.Evaluation;
import entities.Score;
import entities.Soumission;
import services.EvaluationService;
import services.ScoreService;
import services.SoumissionService;
import utils.UserSession;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ScoreListController implements MainControllerAware {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortByComboBox;
    @FXML
    private ComboBox<String> sortOrderComboBox;
    @FXML
    private ComboBox<Integer> pageSizeComboBox;
    @FXML
    private TableView<SoumissionWrapper> correctionTableView;
    @FXML
    private TableColumn<SoumissionWrapper, Integer> idColumn;
    @FXML
    private TableColumn<SoumissionWrapper, String> evaluationColumn;
    @FXML
    private TableColumn<SoumissionWrapper, String> studentColumn;
    @FXML
    private TableColumn<SoumissionWrapper, String> noteColumn;
    @FXML
    private TableColumn<SoumissionWrapper, String> pourcentageColumn;
    @FXML
    private TableColumn<SoumissionWrapper, String> statutColumn;
    @FXML
    private TableColumn<SoumissionWrapper, String> dateColumn;
    @FXML
    private TableColumn<SoumissionWrapper, Void> actionsColumn;
    @FXML
    private Label paginationSummaryLabel;
    @FXML
    private Pagination pagination;

    private final SoumissionService soumissionService = new SoumissionService();
    private final ScoreService scoreService = new ScoreService();
    private final EvaluationService evaluationService = new EvaluationService();
    private final ObservableList<SoumissionWrapper> soumissionWrappers = FXCollections.observableArrayList();
    private final ObservableList<SoumissionWrapper> filteredWrappers = FXCollections.observableArrayList();

    private String currentTeacherId = "PROF001";
    private MainLayoutEnseignantController mainController;

    public static class SoumissionWrapper {
        private final Soumission soumission;
        private final Score score;
        private final Evaluation evaluation;

        public SoumissionWrapper(Soumission soumission, Score score, Evaluation evaluation) {
            this.soumission = soumission;
            this.score = score;
            this.evaluation = evaluation;
        }

        public int getId() { return soumission.getId(); }
        public Soumission getSoumission() { return soumission; }
        public Score getScore() { return score; }
        public Evaluation getEvaluation() { return evaluation; }
        public boolean isCorrected() { return score != null; }
    }

    @FXML
    public void initialize() {
        Utilisateur currentUser = UserSession.getCurrentUser();
        if (currentUser instanceof Enseignant enseignant) {
            currentTeacherId = enseignant.getMatriculeEnseignant();
        }

        setupTableColumns();
        setupComboBoxes();
        setupSearch();
        setupPagination();
        loadSoumissions();
    }

    @Override
    public void setMainController(MainLayoutEnseignantController controller) {
        mainController = controller;
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        studentColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSoumission().getIdEtudiant()));
        evaluationColumn.setCellValueFactory(cellData -> {
            Evaluation eval = cellData.getValue().getEvaluation();
            return new javafx.beans.property.SimpleStringProperty(eval != null ? eval.getTitre() : "-");
        });
        noteColumn.setCellValueFactory(cellData -> {
            Score score = cellData.getValue().getScore();
            return new javafx.beans.property.SimpleStringProperty(score != null ? score.getNote() + "/" + score.getNoteSur() : "-");
        });
        pourcentageColumn.setCellValueFactory(cellData -> {
            Score score = cellData.getValue().getScore();
            return new javafx.beans.property.SimpleStringProperty(
                    score != null && score.getPourcentage() != null ? score.getPourcentage() + "%" : "-"
            );
        });
        statutColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().isCorrected() ? "Corrige" : "En attente"));
        dateColumn.setCellValueFactory(cellData -> {
            Score score = cellData.getValue().getScore();
            return new javafx.beans.property.SimpleStringProperty(
                    score != null && score.getDateCorrection() != null ? score.getDateCorrection().toLocalDate().toString() : "-"
            );
        });

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                SoumissionWrapper wrapper = getTableView().getItems().get(getIndex());
                HBox actions = new HBox(6);

                if (wrapper.isCorrected()) {
                    Button viewBtn = createActionButton("Voir", "secondary-button");
                    Button editBtn = createActionButton("Modifier", "primary-button");
                    Button deleteBtn = createActionButton("Supprimer", "danger-button");
                    viewBtn.setOnAction(e -> handleViewScore(wrapper));
                    editBtn.setOnAction(e -> handleEditScore(wrapper));
                    deleteBtn.setOnAction(e -> handleDeleteScore(wrapper));
                    actions.getChildren().addAll(viewBtn, editBtn, deleteBtn);
                } else {
                    Button gradeBtn = createActionButton("Corriger", "primary-button");
                    gradeBtn.setOnAction(e -> handleCorriger(wrapper));
                    actions.getChildren().add(gradeBtn);
                }

                setGraphic(actions);
            }
        });
    }

    private Button createActionButton(String text, String variantClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll(variantClass, "compact-button", "table-action-button");
        return button;
    }

    private void setupComboBoxes() {
        sortByComboBox.setItems(FXCollections.observableArrayList("Date", "Evaluation", "Etudiant", "Statut"));
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

    private void loadSoumissions() {
        try {
            List<Soumission> allSoumissions = soumissionService.getAll();
            List<Evaluation> allEvaluations = evaluationService.getAll();

            List<SoumissionWrapper> wrappers = allSoumissions.stream()
                    .map(soumission -> {
                        try {
                            Score score = scoreService.getBySoumissionId(soumission.getId());
                            Evaluation evaluation = allEvaluations.stream()
                                    .filter(item -> item.getId() == soumission.getEvaluationId())
                                    .findFirst()
                                    .orElse(null);
                            return new SoumissionWrapper(soumission, score, evaluation);
                        } catch (SQLException e) {
                            System.err.println("Error loading score: " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(wrapper -> wrapper != null && wrapper.getEvaluation() != null && currentTeacherId.equals(wrapper.getEvaluation().getIdEnseignant()))
                    .collect(Collectors.toList());

            soumissionWrappers.setAll(wrappers);
            applyFilters();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des soumissions: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        List<SoumissionWrapper> filtered = soumissionWrappers.stream()
                .filter(wrapper -> {
                    String evalName = wrapper.getEvaluation() != null ? wrapper.getEvaluation().getTitre().toLowerCase() : "";
                    String studentId = wrapper.getSoumission().getIdEtudiant().toLowerCase();
                    String type = wrapper.getEvaluation() != null ? wrapper.getEvaluation().getTypeEvaluation().toLowerCase() : "";
                    String status = wrapper.isCorrected() ? "corrige" : "en attente";
                    return evalName.contains(search) || studentId.contains(search) || type.contains(search) || status.contains(search);
                })
                .collect(Collectors.toList());

        String sortBy = sortByComboBox.getValue();
        boolean ascending = !"Decroissant".equals(sortOrderComboBox.getValue());
        filtered.sort((left, right) -> {
            int result = 0;
            if ("Date".equals(sortBy)) {
                result = left.getSoumission().getDateSoumission().compareTo(right.getSoumission().getDateSoumission());
            } else if ("Evaluation".equals(sortBy)) {
                String a = left.getEvaluation() != null ? left.getEvaluation().getTitre() : "";
                String b = right.getEvaluation() != null ? right.getEvaluation().getTitre() : "";
                result = a.compareToIgnoreCase(b);
            } else if ("Etudiant".equals(sortBy)) {
                result = left.getSoumission().getIdEtudiant().compareToIgnoreCase(right.getSoumission().getIdEtudiant());
            } else if ("Statut".equals(sortBy)) {
                String a = left.isCorrected() ? "Corrige" : "En attente";
                String b = right.isCorrected() ? "Corrige" : "En attente";
                result = a.compareToIgnoreCase(b);
            }
            return ascending ? result : -result;
        });

        int pageSize = pageSizeComboBox.getValue() != null ? pageSizeComboBox.getValue() : 10;
        int totalItems = filtered.size();
        int pageCount = Math.max((int) Math.ceil((double) totalItems / pageSize), 1);
        pagination.setPageCount(pageCount);

        int currentPage = Math.min(pagination.getCurrentPageIndex(), pageCount - 1);
        int fromIndex = currentPage * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        filteredWrappers.setAll(filtered.subList(fromIndex, Math.max(fromIndex, toIndex)));
        correctionTableView.setItems(filteredWrappers);
        paginationSummaryLabel.setText(totalItems + " correction" + (totalItems > 1 ? "s" : ""));
    }

    private void handleCorriger(SoumissionWrapper wrapper) {
        if (mainController != null) {
            mainController.showScoreCreate(wrapper.getSoumission());
        }
    }

    private void handleViewScore(SoumissionWrapper wrapper) {
        if (wrapper.getScore() != null && mainController != null) {
            mainController.showScoreDetail(wrapper.getScore());
        }
    }

    private void handleEditScore(SoumissionWrapper wrapper) {
        if (wrapper.getScore() != null && mainController != null) {
            mainController.showScoreEdit(wrapper.getScore());
        }
    }

    private void handleDeleteScore(SoumissionWrapper wrapper) {
        if (wrapper.getScore() == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la correction");
        alert.setContentText("Voulez-vous supprimer cette correction ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    scoreService.delete(wrapper.getScore());
                    loadSoumissions();
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }
}

