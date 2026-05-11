package gui;

import entities.Cours;
import entities.Etudiant;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import services.ContenuService;
import services.CoursService;
import utils.UserSession;

import java.sql.SQLException;
import java.util.List;

public class StudentOverviewController implements MainControllerAwareEtudiant {

    @FXML private Label welcomeLabel;
    @FXML private Label enrolledCoursesCountLabel;
    @FXML private Label availableCoursesCountLabel;
    @FXML private Label accessibleContentsCountLabel;
    @FXML private TableView<StatRow> profileTable;
    @FXML private TableColumn<StatRow, String> profileFieldColumn;
    @FXML private TableColumn<StatRow, String> profileValueColumn;
    @FXML private TableView<StatRow> focusTable;
    @FXML private TableColumn<StatRow, String> focusTitleColumn;
    @FXML private TableColumn<StatRow, String> focusDescriptionColumn;

    private final CoursService coursService = new CoursService();
    private final ContenuService contenuService = new ContenuService();

    @FXML
    public void initialize() {
        profileFieldColumn.setCellValueFactory(new PropertyValueFactory<>("metric"));
        profileValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        focusTitleColumn.setCellValueFactory(new PropertyValueFactory<>("metric"));
        focusDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        Utilisateur utilisateur = UserSession.getCurrentUser();
        if (!(utilisateur instanceof Etudiant etudiant) || etudiant.getId() == null) {
            welcomeLabel.setText("Vue d'ensemble etudiant");
            return;
        }

        welcomeLabel.setText("Bienvenue " + etudiant.getPrenom() + ", voici votre espace cours.");
        profileTable.setItems(FXCollections.observableArrayList(
                new StatRow("Nom complet", etudiant.getNomComplet()),
                new StatRow("Email", etudiant.getEmail()),
                new StatRow("Matricule", etudiant.getMatricule()),
                new StatRow("Niveau", etudiant.getNiveauEtude()),
                new StatRow("Specialisation", etudiant.getSpecialisation()),
                new StatRow("Statut", etudiant.getStatut())
        ));

        try {
            List<Cours> enrolledCourses = coursService.getByStudentId(etudiant.getId());
            List<Cours> availableCourses = coursService.getAvailableForStudentId(etudiant.getId());
            int accessibleContents = 0;
            for (Cours cours : enrolledCourses) {
                if (cours.getId() != null) {
                    accessibleContents += contenuService.getByCoursId(cours.getId()).size();
                }
            }

            enrolledCoursesCountLabel.setText(String.valueOf(enrolledCourses.size()));
            availableCoursesCountLabel.setText(String.valueOf(availableCourses.size()));
            accessibleContentsCountLabel.setText(String.valueOf(accessibleContents));

            focusTable.setItems(FXCollections.observableArrayList(
                    new StatRow("Priorite", enrolledCourses.isEmpty() ? "Inscrivez-vous a un cours" : "Continuez vos cours actifs"),
                    new StatRow("Catalogue", availableCourses.isEmpty() ? "Aucun nouveau cours disponible" : availableCourses.size() + " cours a explorer"),
                    new StatRow("Contenus", accessibleContents + " ressources consultables"),
                    new StatRow("Conseil", "Utilisez le menu de gauche pour ouvrir vos cours et le catalogue")
            ));
        } catch (SQLException e) {
            enrolledCoursesCountLabel.setText("-");
            availableCoursesCountLabel.setText("-");
            accessibleContentsCountLabel.setText("-");
            focusTable.setItems(FXCollections.observableArrayList(
                    new StatRow("Erreur", "Impossible de charger les donnees cours"),
                    new StatRow("Detail", e.getMessage())
            ));
        }
    }

    @Override
    public void setMainController(MainLayoutEtudiantController controller) {
    }
}
