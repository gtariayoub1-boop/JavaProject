package gui;

import entities.Etudiant;
import entities.Evaluation;
import entities.Soumission;
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
import services.EvaluationService;
import services.SoumissionService;
import utils.UserSession;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class SoumissionListController implements MainControllerAwareEtudiant {

    @FXML
    private Button newSoumissionButton;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> sortByComboBox;

    @FXML
    private ComboBox<String> sortOrderComboBox;

    @FXML
    private ComboBox<Integer> pageSizeComboBox;

    @FXML
    private TableView<Soumission> soumissionTableView;

    @FXML
    private TableColumn<Soumission, Integer> idColumn;

    @FXML
    private TableColumn<Soumission, String> evaluationColumn;

    @FXML
    private TableColumn<Soumission, String> studentIdColumn;

    @FXML
    private TableColumn<Soumission, String> dateColumn;

    @FXML
    private TableColumn<Soumission, String> statusColumn;

    @FXML
    private TableColumn<Soumission, Void> actionsColumn;

    @FXML
    private Label paginationSummaryLabel;

    @FXML
    private Pagination pagination;

    private final SoumissionService soumissionService = new SoumissionService();
    private final EvaluationService evaluationService = new EvaluationService();
    private final ObservableList<Soumission> soumissions = FXCollections.observableArrayList();
    private final ObservableList<Soumission> filteredSoumissions = FXCollections.observableArrayList();

    private String currentStudentId = "ETU001";

    private MainLayoutEtudiantController mainController;

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
        loadSoumissions();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("idEtudiant"));
        dateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateSoumission().toLocalDate().toString()
                )
        );
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        evaluationColumn.setCellValueFactory(cellData -> {
            String titre;
            try {
                Evaluation eval = evaluationService.getById(cellData.getValue().getEvaluationId());
                titre = eval != null ? eval.getTitre() : "Evaluation #" + cellData.getValue().getEvaluationId();
            } catch (SQLException e) {
                titre = "Evaluation #" + cellData.getValue().getEvaluationId();
            }
            return new javafx.beans.property.SimpleStringProperty(titre);
        });

        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toLowerCase()) {
                        case "soumise":
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            break;
                        case "en_retard":
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            break;
                        case "non_soumise":
                            setStyle("-fx-text-fill: red;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("Voir");
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox pane = new HBox(5, viewBtn, editBtn, deleteBtn);

            {
                viewBtn.setOnAction(event -> {
                    Soumission soum = getTableView().getItems().get(getIndex());
                    handleViewSoumission(soum);
                });
                editBtn.setOnAction(event -> {
                    Soumission soum = getTableView().getItems().get(getIndex());
                    handleEditSoumission(soum);
                });
                deleteBtn.setOnAction(event -> {
                    Soumission soum = getTableView().getItems().get(getIndex());
                    handleDeleteSoumission(soum);
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
        sortByComboBox.setItems(FXCollections.observableArrayList("Date", "Evaluation", "Statut"));
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
            List<Soumission> list = soumissionService.getAll();
            List<Soumission> studentSoumissions = list.stream()
                    .filter(s -> s.getIdEtudiant().equals(currentStudentId))
                    .collect(Collectors.toList());
            soumissions.setAll(studentSoumissions);
            applyFilters();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des soumissions: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        List<Soumission> filtered = soumissions.stream()
                .filter(s -> {
                    String evalName = "";
                    try {
                        Evaluation eval = evaluationService.getById(s.getEvaluationId());
                        if (eval != null) {
                            evalName = eval.getTitre().toLowerCase();
                        }
                    } catch (SQLException ignored) {
                    }
                    return evalName.contains(search)
                            || s.getStatut().toLowerCase().contains(search)
                            || s.getIdEtudiant().toLowerCase().contains(search);
                })
                .collect(Collectors.toList());

        String sortBy = sortByComboBox.getValue();
        boolean ascending = sortOrderComboBox.getValue() == null || sortOrderComboBox.getValue().equals("Croissant");

        filtered.sort((s1, s2) -> {
            int cmp = 0;
            if ("Date".equals(sortBy)) {
                cmp = s1.getDateSoumission().compareTo(s2.getDateSoumission());
            } else if ("Evaluation".equals(sortBy)) {
                String n1 = "";
                String n2 = "";
                try {
                    Evaluation e1 = evaluationService.getById(s1.getEvaluationId());
                    Evaluation e2 = evaluationService.getById(s2.getEvaluationId());
                    n1 = e1 != null ? e1.getTitre() : "";
                    n2 = e2 != null ? e2.getTitre() : "";
                } catch (SQLException ignored) {
                }
                cmp = n1.compareToIgnoreCase(n2);
            } else if ("Statut".equals(sortBy)) {
                cmp = s1.getStatut().compareToIgnoreCase(s2.getStatut());
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

        List<Soumission> pageItems = filtered.subList(fromIndex, Math.max(fromIndex, toIndex));
        filteredSoumissions.setAll(pageItems);
        soumissionTableView.setItems(filteredSoumissions);

        paginationSummaryLabel.setText(totalItems + " soumission" + (totalItems > 1 ? "s" : ""));
    }

    @Override
    public void setMainController(MainLayoutEtudiantController controller) {
        this.mainController = controller;
    }

    @FXML
    private void handleNewSoumission() {
        if (mainController != null) {
            mainController.showSoumissionCreate();
        }
    }

    private void handleViewSoumission(Soumission soum) {
        if (mainController != null) {
            mainController.showSoumissionDetail(soum);
        }
    }

    private void handleEditSoumission(Soumission soum) {
        try {
            Evaluation eval = evaluationService.getById(soum.getEvaluationId());
            if (eval != null && "fermee".equals(eval.getStatut())) {
                showAlert("Information", "Cette evaluation est fermee. La modification est interdite.");
                return;
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement de l'evaluation: " + e.getMessage());
            return;
        }

        if (mainController != null) {
            mainController.showSoumissionEdit(soum);
        }
    }

    private void handleDeleteSoumission(Soumission soum) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la soumission");
        alert.setContentText("Etes-vous sur de vouloir supprimer cette soumission ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    soumissionService.delete(soum);
                    loadSoumissions();
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
