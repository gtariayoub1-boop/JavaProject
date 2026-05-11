package gui;

import entities.Cours;
import entities.Enseignant;
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
import services.CoursService;
import utils.UserSession;

import java.sql.SQLException;
import java.util.List;

public class TeacherCoursesController implements MainControllerAware {

    @FXML private TableView<Cours> coursesTable;
    @FXML private TableColumn<Cours, String> codeColumn;
    @FXML private TableColumn<Cours, String> titleColumn;
    @FXML private TableColumn<Cours, String> moduleColumn;
    @FXML private TableColumn<Cours, String> levelColumn;
    @FXML private TableColumn<Cours, String> statusColumn;
    @FXML private TableColumn<Cours, Number> studentsColumn;
    @FXML private TableColumn<Cours, Void> actionsColumn;

    private final CoursService coursService = new CoursService();
    private MainLayoutEnseignantController mainController;

    @FXML
    public void initialize() {
        codeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCodeCours()));
        titleColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitre()));
        moduleColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getModule() != null ? cell.getValue().getModule().getTitreModule() : "-"));
        levelColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNiveau()));
        statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatut()));
        studentsColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(studentCount(cell.getValue())));
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button studentsButton = new Button("Etudiants");
            private final javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(8, editButton, studentsButton);
            {
                editButton.getStyleClass().addAll("secondary-button", "compact-button");
                studentsButton.getStyleClass().addAll("primary-button", "compact-button");
                editButton.setOnAction(event -> openEdit(getTableView().getItems().get(getIndex())));
                studentsButton.setOnAction(event -> openStudents());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
        loadCourses();
    }

    private void loadCourses() {
        Utilisateur utilisateur = UserSession.getCurrentUser();
        if (!(utilisateur instanceof Enseignant enseignant) || enseignant.getId() == null) {
            return;
        }

        try {
            List<Cours> courses = coursService.getByTeacherId(enseignant.getId());
            coursesTable.setItems(FXCollections.observableArrayList(courses));
        } catch (SQLException e) {
            showError("Chargement impossible", e.getMessage());
        }
    }

    private int studentCount(Cours cours) {
        try {
            return cours.getId() == null ? 0 : coursService.getStudentsByCoursId(cours.getId()).size();
        } catch (SQLException e) {
            return 0;
        }
    }

    private void openEdit(Cours cours) {
        if (mainController != null) {
            mainController.showTeacherCourseForm(cours);
        }
    }

    private void openStudents() {
        if (mainController != null) {
            mainController.showTeacherStudents();
        }
    }

    @FXML
    private void handleNewCourse() {
        if (mainController != null) {
            mainController.showTeacherCourseForm(null);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void setMainController(MainLayoutEnseignantController controller) {
        mainController = controller;
    }
}
