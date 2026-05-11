package gui;

import entities.Enseignant;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import entities.Evaluation;
import services.EvaluationService;
import utils.UserSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class EvaluationListController implements MainControllerAware {

    @FXML
    private Button newEvaluationButton;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> sortByComboBox;

    @FXML
    private ComboBox<String> sortOrderComboBox;

    @FXML
    private ComboBox<Integer> pageSizeComboBox;

    @FXML
    private TableView<Evaluation> evaluationTableView;

    @FXML
    private TableColumn<Evaluation, String> titleColumn;

    @FXML
    private TableColumn<Evaluation, String> typeColumn;

    @FXML
    private TableColumn<Evaluation, Integer> courseColumn;

    @FXML
    private TableColumn<Evaluation, String> creationDateColumn;

    @FXML
    private TableColumn<Evaluation, String> deadlineColumn;

    @FXML
    private TableColumn<Evaluation, Double> maxScoreColumn;

    @FXML
    private TableColumn<Evaluation, String> statusColumn;

    @FXML
    private TableColumn<Evaluation, Void> actionsColumn;

    @FXML
    private Label paginationSummaryLabel;

    @FXML
    private Pagination pagination;

    private final EvaluationService evaluationService = new EvaluationService();
    private ObservableList<Evaluation> evaluations = FXCollections.observableArrayList();
    private ObservableList<Evaluation> filteredEvaluations = FXCollections.observableArrayList();

    private MainLayoutEnseignantController mainController;
    private String currentTeacherId = "PROF001";

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
        loadEvaluations();
    }

    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeEvaluation"));
        courseColumn.setCellValueFactory(new PropertyValueFactory<>("coursId"));
        creationDateColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDateCreation().toLocalDate().toString()
            );
        });
        deadlineColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDateLimite().toLocalDate().toString()
            );
        });
        maxScoreColumn.setCellValueFactory(new PropertyValueFactory<>("noteMax"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("Voir");
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox pane = new HBox(5, viewBtn, editBtn, deleteBtn);

            {
                viewBtn.setOnAction(event -> {
                    Evaluation eval = getTableView().getItems().get(getIndex());
                    handleViewEvaluation(eval);
                });
                editBtn.setOnAction(event -> {
                    Evaluation eval = getTableView().getItems().get(getIndex());
                    handleEditEvaluation(eval);
                });
                deleteBtn.setOnAction(event -> {
                    Evaluation eval = getTableView().getItems().get(getIndex());
                    handleDeleteEvaluation(eval);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupComboBoxes() {
        sortByComboBox.setItems(FXCollections.observableArrayList("Titre", "Type", "Date création", "Date limite"));
        sortByComboBox.getSelectionModel().selectFirst();

        sortOrderComboBox.setItems(FXCollections.observableArrayList("Croissant", "Décroissant"));
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
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
        });
    }

    private void loadEvaluations() {
        try {
            List<Evaluation> list = evaluationService.getAll().stream()
                    .filter(evaluation -> currentTeacherId.equals(evaluation.getIdEnseignant()))
                    .toList();
            evaluations.setAll(list);
            applyFilters();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des évaluations: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String search = searchField.getText().toLowerCase();
        List<Evaluation> filtered = evaluations.stream()
            .filter(e ->
                e.getTitre().toLowerCase().contains(search) ||
                e.getTypeEvaluation().toLowerCase().contains(search) ||
                (e.getCoursId() != null && e.getCoursId().toString().contains(search)) ||
                e.getStatut().toLowerCase().contains(search)
            )
            .collect(Collectors.toList());

        String sortBy = sortByComboBox.getValue();
        boolean ascending = sortOrderComboBox.getValue() == null || sortOrderComboBox.getValue().equals("Croissant");

        filtered.sort((e1, e2) -> {
            int cmp = 0;
            if ("Titre".equals(sortBy)) {
                cmp = e1.getTitre().compareToIgnoreCase(e2.getTitre());
            } else if ("Type".equals(sortBy)) {
                cmp = e1.getTypeEvaluation().compareToIgnoreCase(e2.getTypeEvaluation());
            } else if ("Date création".equals(sortBy)) {
                cmp = e1.getDateCreation().compareTo(e2.getDateCreation());
            } else if ("Date limite".equals(sortBy)) {
                cmp = e1.getDateLimite().compareTo(e2.getDateLimite());
            }
            return ascending ? cmp : -cmp;
        });

        int pageSize = pageSizeComboBox.getValue() != null ? pageSizeComboBox.getValue() : 10;
        int totalItems = filtered.size();
        int pageCount = (int) Math.ceil((double) totalItems / pageSize);
        pageCount = Math.max(pageCount, 1);
        pagination.setPageCount(pageCount);

        int currentPage = pagination.getCurrentPageIndex();
        int fromIndex = currentPage * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        List<Evaluation> pageItems = filtered.subList(fromIndex, Math.max(fromIndex, toIndex));
        filteredEvaluations.setAll(pageItems);
        evaluationTableView.setItems(filteredEvaluations);

        paginationSummaryLabel.setText(totalItems + " évaluation" + (totalItems > 1 ? "s" : ""));
    }

    @Override
    public void setMainController(MainLayoutEnseignantController controller) {
        this.mainController = controller;
    }

    @FXML
    private void handleNewEvaluation() {
        if (mainController != null) {
            mainController.showEvaluationCreate();
        }
    }

    private void handleViewEvaluation(Evaluation eval) {
        if (mainController != null) {
            mainController.showEvaluationDetail(eval);
        }
    }

    private void handleEditEvaluation(Evaluation eval) {
        if (mainController != null) {
            mainController.showEvaluationEdit(eval);
        }
    }

    private void handleDeleteEvaluation(Evaluation eval) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'évaluation");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer l'évaluation \"" + eval.getTitre() + "\" ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    evaluationService.delete(eval);
                    loadEvaluations();
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

