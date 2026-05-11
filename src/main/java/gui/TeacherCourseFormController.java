package gui;

import entities.Cours;
import entities.Enseignant;
import entities.Module;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import services.CoursService;
import services.ModuleService;
import utils.UserSession;

import java.sql.SQLException;
import java.util.Map;

public class TeacherCourseFormController implements MainControllerAware {

    @FXML private Label titleLabel;
    @FXML private ComboBox<Module> moduleCombo;
    @FXML private TextField codeField;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField levelField;
    @FXML private TextField creditsField;
    @FXML private TextField languageField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField imageField;
    @FXML private TextArea prerequisArea;
    @FXML private Label feedbackLabel;

    private final ModuleService moduleService = new ModuleService();
    private final CoursService coursService = new CoursService();
    private final CoursController helper = new CoursController();
    private MainLayoutEnseignantController mainController;
    private Cours editingCourse;

    @FXML
    public void initialize() {
        helper.configureFormDefaults(statusCombo, startDatePicker, endDatePicker);
        moduleCombo.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Module item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitreModule());
            }
        });
        moduleCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Module item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Choisir un module" : item.getTitreModule());
            }
        });
        try {
            moduleCombo.setItems(FXCollections.observableArrayList(moduleService.getAll()));
        } catch (SQLException e) {
            feedbackLabel.setText("Chargement modules impossible: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        Map<String, String> errors = helper.validateCoursForm(
                codeField, titleField, descriptionArea, levelField, creditsField, languageField,
                startDatePicker, endDatePicker, statusCombo, imageField, prerequisArea
        );
        if (moduleCombo.getValue() == null) {
            errors.put("module", "Selectionnez un module.");
        }
        if (!errors.isEmpty()) {
            feedbackLabel.setText(String.join(" ", errors.values()));
            return;
        }

        try {
            Cours cours = helper.buildCours(
                    editingCourse, moduleCombo.getValue(), codeField, titleField, descriptionArea,
                    levelField, creditsField, languageField, startDatePicker, endDatePicker,
                    statusCombo, imageField, prerequisArea
            );

            Utilisateur utilisateur = UserSession.getCurrentUser();
            if (editingCourse == null) {
                if (utilisateur instanceof Enseignant enseignant && enseignant.getId() != null) {
                    coursService.createForTeacher(cours, enseignant.getId());
                } else {
                    coursService.create(cours);
                }
                feedbackLabel.setText("Cours cree avec succes.");
            } else {
                if (utilisateur instanceof Enseignant enseignant && enseignant.getId() != null) {
                    coursService.updateForTeacher(cours, enseignant.getId());
                } else {
                    coursService.update(cours);
                }
                feedbackLabel.setText("Cours mis a jour.");
            }

            if (mainController != null) {
                mainController.showTeacherCourses();
            }
        } catch (SQLException e) {
            feedbackLabel.setText("Enregistrement impossible: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        if (mainController != null) {
            mainController.showTeacherCourses();
        }
    }

    @Override
    public void setMainController(MainLayoutEnseignantController controller) {
        mainController = controller;
        editingCourse = controller.consumeEditingCourse();
        if (editingCourse != null) {
            titleLabel.setText("Modifier le cours");
            helper.populateForm(
                    editingCourse, codeField, titleField, descriptionArea, levelField, creditsField,
                    languageField, startDatePicker, endDatePicker, statusCombo, imageField, prerequisArea
            );
            if (editingCourse.getModule() != null && editingCourse.getModule().getId() != null) {
                moduleCombo.getItems().stream()
                        .filter(module -> module.getId() != null && module.getId().equals(editingCourse.getModule().getId()))
                        .findFirst()
                        .ifPresent(moduleCombo::setValue);
            }
        }
    }
}
