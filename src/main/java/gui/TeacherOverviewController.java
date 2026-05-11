package gui;

import entities.Cours;
import entities.Enseignant;
import entities.Etudiant;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import services.CoursService;
import utils.UserSession;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeacherOverviewController implements MainControllerAware {

    @FXML private Label welcomeLabel;
    @FXML private Label coursesCountLabel;
    @FXML private Label studentsCountLabel;
    @FXML private Label publishedCoursesLabel;
    @FXML private TableView<StatRow> profileTable;
    @FXML private TableColumn<StatRow, String> profileFieldColumn;
    @FXML private TableColumn<StatRow, String> profileValueColumn;
    @FXML private TableView<StatRow> focusTable;
    @FXML private TableColumn<StatRow, String> focusTitleColumn;
    @FXML private TableColumn<StatRow, String> focusDescriptionColumn;

    private final CoursService coursService = new CoursService();

    @FXML
    public void initialize() {
        profileFieldColumn.setCellValueFactory(new PropertyValueFactory<>("metric"));
        profileValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        focusTitleColumn.setCellValueFactory(new PropertyValueFactory<>("metric"));
        focusDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        Utilisateur utilisateur = UserSession.getCurrentUser();
        if (!(utilisateur instanceof Enseignant enseignant) || enseignant.getId() == null) {
            return;
        }

        welcomeLabel.setText("Bienvenue " + enseignant.getPrenom() + ", voici votre pilotage cours.");
        profileTable.setItems(FXCollections.observableArrayList(
                new StatRow("Nom complet", enseignant.getNomComplet()),
                new StatRow("Email", enseignant.getEmail()),
                new StatRow("Matricule", enseignant.getMatriculeEnseignant()),
                new StatRow("Diplome", enseignant.getDiplome()),
                new StatRow("Specialite", enseignant.getSpecialite()),
                new StatRow("Contrat", enseignant.getTypeContrat())
        ));

        try {
            List<Cours> courses = coursService.getByTeacherId(enseignant.getId());
            int published = (int) courses.stream().filter(cours -> "publie".equalsIgnoreCase(cours.getStatut())).count();
            Set<Integer> studentIds = new HashSet<>();
            for (Cours cours : courses) {
                if (cours.getId() == null) {
                    continue;
                }
                for (Etudiant etudiant : coursService.getStudentsByCoursId(cours.getId())) {
                    if (etudiant.getId() != null) {
                        studentIds.add(etudiant.getId().intValue());
                    }
                }
            }

            coursesCountLabel.setText(String.valueOf(courses.size()));
            studentsCountLabel.setText(String.valueOf(studentIds.size()));
            publishedCoursesLabel.setText(String.valueOf(published));

            focusTable.setItems(FXCollections.observableArrayList(
                    new StatRow("Cours", courses.isEmpty() ? "Creez votre premier cours" : courses.size() + " cours assignes"),
                    new StatRow("Publication", published + " cours deja publies"),
                    new StatRow("Etudiants", studentIds.size() + " etudiants relies a vos cours"),
                    new StatRow("Action", "Utilisez Mes cours ou Nouveau cours depuis la sidebar")
            ));
        } catch (SQLException e) {
            focusTable.setItems(FXCollections.observableArrayList(
                    new StatRow("Erreur", "Impossible de charger les donnees enseignant"),
                    new StatRow("Detail", e.getMessage())
            ));
        }
    }

    @Override
    public void setMainController(MainLayoutEnseignantController controller) {
    }
}
