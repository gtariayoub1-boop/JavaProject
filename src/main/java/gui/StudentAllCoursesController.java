package gui;

import entities.Cours;
import entities.Etudiant;
import entities.Utilisateur;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import services.ContenuService;
import services.CoursService;
import utils.UserSession;

import java.sql.SQLException;
import java.util.List;

public class StudentAllCoursesController implements MainControllerAwareEtudiant {

    @FXML private TableView<Cours> coursesTable;
    @FXML private TableColumn<Cours, String> codeColumn;
    @FXML private TableColumn<Cours, String> titleColumn;
    @FXML private TableColumn<Cours, String> moduleColumn;
    @FXML private TableColumn<Cours, String> levelColumn;
    @FXML private TableColumn<Cours, String> statusColumn;
    @FXML private TableColumn<Cours, Number> contentsColumn;
    @FXML private TableColumn<Cours, Void> actionColumn;

    private final CoursService coursService = new CoursService();
    private final ContenuService contenuService = new ContenuService();
    private MainLayoutEtudiantController mainController;

    @FXML
    public void initialize() {
        codeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCodeCours()));
        titleColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitre()));
        moduleColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getModule() != null ? cell.getValue().getModule().getTitreModule() : "-"));
        levelColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNiveau()));
        statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatut()));
        contentsColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(contentCount(cell.getValue())));
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button enrollButton = new Button("S'inscrire");
            {
                enrollButton.getStyleClass().addAll("primary-button", "compact-button");
                enrollButton.setOnAction(event -> enrollCourse(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : enrollButton);
            }
        });
        loadCourses();
    }

    private void loadCourses() {
        Utilisateur utilisateur = UserSession.getCurrentUser();
        if (!(utilisateur instanceof Etudiant etudiant) || etudiant.getId() == null) {
            return;
        }
        try {
            List<Cours> courses = coursService.getAvailableForStudentId(etudiant.getId());
            coursesTable.setItems(FXCollections.observableArrayList(courses));
        } catch (SQLException e) {
            showInfo("Erreur catalogue", e.getMessage());
        }
    }

    private int contentCount(Cours cours) {
        try {
            return cours.getId() == null ? 0 : contenuService.getByCoursId(cours.getId()).size();
        } catch (SQLException e) {
            return 0;
        }
    }

    private void enrollCourse(Cours cours) {
        Utilisateur utilisateur = UserSession.getCurrentUser();
        if (!(utilisateur instanceof Etudiant etudiant) || etudiant.getId() == null || cours.getId() == null) {
            return;
        }
        try {
            coursService.enrollStudent(cours.getId(), etudiant.getId());
            showInfo("Inscription reussie", "Vous etes inscrit au cours " + cours.getTitre() + ".");
            if (mainController != null) {
                mainController.showMyCourses();
            } else {
                loadCourses();
            }
        } catch (SQLException e) {
            showInfo("Inscription impossible", e.getMessage());
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void setMainController(MainLayoutEtudiantController controller) {
        mainController = controller;
    }
}
