package gui;

import entities.Contenu;
import entities.Cours;
import entities.Enseignant;
import entities.Module;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.ContenuService;
import services.CourseContentNotificationService;
import services.CoursService;
import services.CoursVueService;
import services.ModuleService;
import utils.UserSession;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CourseManagementController implements MainControllerAware {

    @FXML
    private TabPane managementTabPane;
    @FXML
    private Tab modulesTab;
    @FXML
    private Tab coursesTab;
    @FXML
    private Tab contentsTab;
    @FXML
    private Label managementModeLabel;
    @FXML
    private Label navigationStatusLabel;
    @FXML
    private Label courseFilterLabel;
    @FXML
    private Label contentFilterLabel;
    @FXML
    private Button moduleNewButton;
    @FXML
    private Button moduleEditButton;
    @FXML
    private Button moduleOpenCoursesButton;
    @FXML
    private Button moduleDeleteButton;
    @FXML
    private VBox moduleFormPanel;
    @FXML
    private VBox courseFormPanel;
    @FXML
    private VBox contentFormPanel;
    @FXML
    private Label moduleFormTitleLabel;
    @FXML
    private Label courseFormTitleLabel;
    @FXML
    private Label contentFormTitleLabel;

    @FXML
    private TableView<Module> moduleTable;
    @FXML
    private TableColumn<Module, String> moduleTitleColumn;
    @FXML
    private TableColumn<Module, String> moduleStatusColumn;
    @FXML
    private TableColumn<Module, Integer> moduleOrderColumn;
    @FXML
    private TextField moduleTitleField;
    @FXML
    private TextArea moduleDescriptionArea;
    @FXML
    private TextField moduleOrderField;
    @FXML
    private TextArea moduleObjectivesArea;
    @FXML
    private TextField moduleDurationField;
    @FXML
    private DatePicker modulePublicationDatePicker;
    @FXML
    private ComboBox<String> moduleStatusCombo;
    @FXML
    private TextArea moduleResourcesArea;
    @FXML
    private Label moduleStatusLabel;

    @FXML
    private TableView<Cours> courseTable;
    @FXML
    private TableColumn<Cours, String> courseCodeColumn;
    @FXML
    private TableColumn<Cours, String> courseTitleColumn;
    @FXML
    private TableColumn<Cours, String> courseModuleColumn;
    @FXML
    private TableColumn<Cours, String> courseStatusColumn;
    @FXML
    private TableColumn<Cours, Number> courseViewsColumn;
    @FXML
    private TextField courseCodeField;
    @FXML
    private TextField courseTitleField;
    @FXML
    private TextArea courseDescriptionArea;
    @FXML
    private ComboBox<Module> courseModuleCombo;
    @FXML
    private TextField courseLevelField;
    @FXML
    private TextField courseCreditsField;
    @FXML
    private TextField courseLanguageField;
    @FXML
    private DatePicker courseStartDatePicker;
    @FXML
    private DatePicker courseEndDatePicker;
    @FXML
    private ComboBox<String> courseStatusCombo;
    @FXML
    private TextField courseImageUrlField;
    @FXML
    private TextArea coursePrerequisitesArea;
    @FXML
    private Label courseStatusLabel;

    @FXML
    private TableView<Contenu> contentTable;
    @FXML
    private TableColumn<Contenu, String> contentTitleColumn;
    @FXML
    private TableColumn<Contenu, String> contentCourseColumn;
    @FXML
    private TableColumn<Contenu, String> contentTypeColumn;
    @FXML
    private TableColumn<Contenu, Integer> contentOrderColumn;
    @FXML
    private ComboBox<Cours> contentCourseCombo;
    @FXML
    private CheckBox contentVideoCheck;
    @FXML
    private CheckBox contentPdfCheck;
    @FXML
    private CheckBox contentPptCheck;
    @FXML
    private CheckBox contentTextCheck;
    @FXML
    private CheckBox contentQuizCheck;
    @FXML
    private CheckBox contentLinkCheck;
    @FXML
    private VBox contentPdfBox;
    @FXML
    private VBox contentPptBox;
    @FXML
    private VBox contentVideoBox;
    @FXML
    private VBox contentLinkBox;
    @FXML
    private VBox contentQuizBox;
    @FXML
    private TextField contentTitleField;
    @FXML
    private TextField contentUrlField;
    @FXML
    private TextArea contentDescriptionArea;
    @FXML
    private TextField contentDurationField;
    @FXML
    private TextField contentOrderField;
    @FXML
    private ComboBox<String> contentPublicCombo;
    @FXML
    private DatePicker contentAddedDatePicker;
    @FXML
    private TextField contentViewsField;
    @FXML
    private TextField contentFormatField;
    @FXML
    private TextArea contentResourcesArea;
    @FXML
    private TextField contentPdfField;
    @FXML
    private TextField contentPptField;
    @FXML
    private TextField contentVideoField;
    @FXML
    private TextField contentLinkField;
    @FXML
    private TextField contentQuizField;
    @FXML
    private Label contentStatusLabel;

    private final ModuleService moduleService = new ModuleService();
    private final CoursService coursService = new CoursService();
    private final CoursVueService coursVueService = new CoursVueService();
    private final ContenuService contenuService = new ContenuService();
    private final CourseContentNotificationService contentNotificationService = new CourseContentNotificationService();

    private final ModuleController moduleFormHelper = new ModuleController();
    private final CoursController coursFormHelper = new CoursController();
    private final ContenuController contenuFormHelper = new ContenuController();

    private final ObservableList<Module> modules = FXCollections.observableArrayList();
    private final ObservableList<Cours> courses = FXCollections.observableArrayList();
    private final ObservableList<Contenu> contents = FXCollections.observableArrayList();
    private final ObservableList<Cours> filteredCourses = FXCollections.observableArrayList();
    private final ObservableList<Contenu> filteredContents = FXCollections.observableArrayList();
    private final Map<Integer, Integer> courseViewCounts = new HashMap<>();

    private Module editingModule;
    private Cours editingCours;
    private Contenu editingContenu;
    private Module selectedModule;
    private Cours selectedCours;
    private Long currentTeacherId;
    private boolean moduleReadOnly;
    private File selectedContentPdfFile;
    private File selectedContentPptFile;
    private File selectedContentVideoFile;
    private boolean syncingCourseSelection;
    private boolean syncingContentSelection;

    private MainLayoutEnseignantController mainController;

    @FXML
    public void initialize() {
        Utilisateur currentUser = UserSession.getCurrentUser();
        if (currentUser instanceof Enseignant enseignant && enseignant.getId() != null) {
            currentTeacherId = enseignant.getId();
        }
        moduleReadOnly = currentTeacherId != null;

        setupModuleTable();
        setupCourseTable();
        setupContentTable();
        setupCombos();
        configureRoleMode();
        resetModuleForm();
        resetCourseForm();
        resetContentForm();
        hideModuleForm();
        hideCourseForm();
        hideContentForm();
        loadAllData();
    }

    @Override
    public void setMainController(MainLayoutEnseignantController controller) {
        this.mainController = controller;
    }

    private void configureRoleMode() {
        if (managementModeLabel != null) {
            managementModeLabel.setText(moduleReadOnly ? "Parcours enseignant" : "Parcours admin");
        }
        if (navigationStatusLabel != null) {
            navigationStatusLabel.setText(moduleReadOnly
                    ? "Les modules sont geres par l'administration. Selectionnez un module pour afficher vos cours, puis un cours pour gerer ses contenus."
                    : "Selectionnez un module pour afficher ses cours, puis un cours pour afficher ses contenus.");
        }
        if (!moduleReadOnly) {
            return;
        }
        if (moduleNewButton != null) {
            moduleNewButton.setVisible(false);
            moduleNewButton.setManaged(false);
        }
        if (moduleEditButton != null) {
            moduleEditButton.setVisible(false);
            moduleEditButton.setManaged(false);
        }
        if (moduleDeleteButton != null) {
            moduleDeleteButton.setVisible(false);
            moduleDeleteButton.setManaged(false);
        }
        if (moduleOpenCoursesButton != null) {
            moduleOpenCoursesButton.setText("Voir mes cours");
        }
        hideModuleForm();
    }

    private void setupModuleTable() {
        moduleTitleColumn.setCellValueFactory(new PropertyValueFactory<>("titreModule"));
        moduleStatusColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        moduleOrderColumn.setCellValueFactory(new PropertyValueFactory<>("ordreAffichage"));
        moduleTable.setItems(modules);
        moduleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            editingModule = newValue;
            selectedModule = newValue;
            if (newValue == null) {
                applyCourseFilter();
                return;
            }
            applyCourseFilter();
        });
    }

    private void setupCourseTable() {
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("codeCours"));
        courseTitleColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        courseModuleColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getModule() != null ? cell.getValue().getModule().getTitreModule() : "-"
        ));
        courseStatusColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        courseViewsColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(courseViewCount(cell.getValue())));
        courseTable.setItems(filteredCourses);
        courseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (syncingCourseSelection) {
                return;
            }
            selectedCours = newValue;
            editingCours = newValue;
            if (newValue == null) {
                applyContentFilter();
                return;
            }
            selectModuleForCourse(newValue);
            applyContentFilter();
        });
    }

    private void setupContentTable() {
        contentTitleColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        contentCourseColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getCours() != null ? cell.getValue().getCours().getTitre() : "-"
        ));
        contentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("typeContenu"));
        contentOrderColumn.setCellValueFactory(new PropertyValueFactory<>("ordreAffichage"));
        contentTable.setItems(filteredContents);
        contentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (syncingContentSelection) {
                return;
            }
            editingContenu = newValue;
            if (newValue != null) {
                selectCourseForContent(newValue);
            }
        });
    }

    private void setupCombos() {
        moduleFormHelper.configureFormDefaults(moduleStatusCombo, modulePublicationDatePicker);
        coursFormHelper.configureFormDefaults(courseStatusCombo, courseStartDatePicker, courseEndDatePicker);
        contenuFormHelper.configureFormDefaults(
                contentVideoCheck,
                contentPdfCheck,
                contentPptCheck,
                contentTextCheck,
                contentQuizCheck,
                contentLinkCheck,
                contentPublicCombo,
                contentAddedDatePicker
        );
        configureContentTypeVisibility();

        courseModuleCombo.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Module item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitreModule());
            }
        });
        courseModuleCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Module item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Choisir un module" : item.getTitreModule());
            }
        });

        contentCourseCombo.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Cours item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitre());
            }
        });
        contentCourseCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Cours item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Choisir un cours" : item.getTitre());
            }
        });
    }

    private void configureContentTypeVisibility() {
        contentPdfCheck.selectedProperty().addListener((obs, oldValue, newValue) -> updateContentTypeVisibility());
        contentPptCheck.selectedProperty().addListener((obs, oldValue, newValue) -> updateContentTypeVisibility());
        contentVideoCheck.selectedProperty().addListener((obs, oldValue, newValue) -> updateContentTypeVisibility());
        contentLinkCheck.selectedProperty().addListener((obs, oldValue, newValue) -> updateContentTypeVisibility());
        contentQuizCheck.selectedProperty().addListener((obs, oldValue, newValue) -> updateContentTypeVisibility());
        updateContentTypeVisibility();
    }

    private void updateContentTypeVisibility() {
        toggleNode(contentPdfBox, contentPdfCheck.isSelected());
        toggleNode(contentPptBox, contentPptCheck.isSelected());
        toggleNode(contentVideoBox, contentVideoCheck.isSelected());
        toggleNode(contentLinkBox, contentLinkCheck.isSelected());
        toggleNode(contentQuizBox, contentQuizCheck.isSelected());
    }

    private void toggleNode(VBox node, boolean visible) {
        if (node == null) {
            return;
        }
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void loadAllData() {
        loadModules();
        loadCourses();
        loadContents();
    }

    private void loadModules() {
        try {
            modules.setAll(moduleService.getAll());
            courseModuleCombo.setItems(FXCollections.observableArrayList(modules));
            if (selectedModule != null && selectedModule.getId() != null) {
                selectedModule = modules.stream()
                        .filter(module -> Objects.equals(module.getId(), selectedModule.getId()))
                        .findFirst()
                        .orElse(null);
            }
            applyCourseFilter();
        } catch (SQLException e) {
            showError("Chargement modules impossible: " + e.getMessage());
        }
    }

    private void loadCourses() {
        try {
            List<Cours> loadedCourses;
            if (currentTeacherId != null) {
                loadedCourses = coursService.getByTeacherId(currentTeacherId);
                if (loadedCourses.isEmpty()) {
                    loadedCourses = coursService.getAll();
                }
            } else {
                loadedCourses = coursService.getAll();
            }
            courses.setAll(loadedCourses);
            reloadCourseViewCounts();
            contentCourseCombo.setItems(FXCollections.observableArrayList(courses));
            if (selectedCours != null && selectedCours.getId() != null) {
                selectedCours = courses.stream()
                        .filter(cours -> Objects.equals(cours.getId(), selectedCours.getId()))
                        .findFirst()
                        .orElse(null);
            }
            applyCourseFilter();
        } catch (SQLException e) {
            showError("Chargement cours impossible: " + e.getMessage());
        }
    }

    private void reloadCourseViewCounts() {
        courseViewCounts.clear();
        List<Integer> courseIds = courses.stream()
                .map(Cours::getId)
                .filter(Objects::nonNull)
                .toList();
        if (courseIds.isEmpty()) {
            if (courseTable != null) {
                courseTable.refresh();
            }
            return;
        }

        try {
            courseViewCounts.putAll(coursVueService.getViewCountsByCourseIds(courseIds));
        } catch (SQLException ignored) {
        }

        if (courseTable != null) {
            courseTable.refresh();
        }
    }

    private int courseViewCount(Cours cours) {
        if (cours == null || cours.getId() == null) {
            return 0;
        }
        return courseViewCounts.getOrDefault(cours.getId(), 0);
    }

    private void loadContents() {
        try {
            List<Contenu> loadedContents = new ArrayList<>();
            for (Cours cours : courses) {
                if (cours.getId() != null) {
                    loadedContents.addAll(contenuService.getByCoursId(cours.getId()));
                }
            }
            contents.setAll(loadedContents);
            applyContentFilter();
        } catch (SQLException e) {
            showError("Chargement contenus impossible: " + e.getMessage());
        }
    }

    @FXML
    private void handleNewModule() {
        if (moduleReadOnly) {
            showError("Les modules sont en lecture seule pour l'enseignant.");
            return;
        }
        resetModuleForm();
        showModuleForm("Nouveau module");
        managementTabPane.getSelectionModel().select(modulesTab);
    }

    @FXML
    private void handleSaveModule() {
        if (moduleReadOnly) {
            showError("Les modules sont en lecture seule pour l'enseignant.");
            return;
        }
        Map<String, String> errors = moduleFormHelper.validateModuleForm(
                moduleTitleField,
                moduleDescriptionArea,
                moduleOrderField,
                moduleObjectivesArea,
                moduleDurationField,
                moduleStatusCombo
        );
        if (!errors.isEmpty()) {
            showError(String.join("\n", errors.values()));
            return;
        }

        try {
            Module module = moduleFormHelper.buildModule(
                    editingModule,
                    moduleTitleField,
                    moduleDescriptionArea,
                    moduleOrderField,
                    moduleObjectivesArea,
                    moduleDurationField,
                    modulePublicationDatePicker,
                    moduleStatusCombo,
                    moduleResourcesArea
            );
            if (editingModule == null) {
                moduleService.create(module);
                moduleStatusLabel.setText("Module cree avec succes.");
            } else {
                moduleService.update(module);
                moduleStatusLabel.setText("Module mis a jour.");
            }
            selectedModule = module;
            loadModules();
            hideModuleForm();
            resetModuleForm();
        } catch (SQLException e) {
            showError("Enregistrement module impossible: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteModule() {
        if (moduleReadOnly) {
            showError("Les modules sont en lecture seule pour l'enseignant.");
            return;
        }
        if (editingModule == null || editingModule.getId() == null) {
            showError("Selectionnez un module a supprimer.");
            return;
        }
        try {
            moduleService.delete(editingModule.getId());
            selectedModule = null;
            selectedCours = null;
            moduleStatusLabel.setText("Module supprime.");
            loadAllData();
            hideModuleForm();
            resetModuleForm();
        } catch (SQLException e) {
            showError("Suppression module impossible: " + e.getMessage());
        }
    }

    @FXML
    private void handleNewCourse() {
        resetCourseForm();
        if (selectedModule != null) {
            courseModuleCombo.setValue(selectedModule);
        }
        showCourseForm("Nouveau cours");
        managementTabPane.getSelectionModel().select(coursesTab);
    }

    @FXML
    private void handleSaveCourse() {
        Map<String, String> errors = coursFormHelper.validateCoursForm(
                courseCodeField,
                courseTitleField,
                courseDescriptionArea,
                courseLevelField,
                courseCreditsField,
                courseLanguageField,
                courseStartDatePicker,
                courseEndDatePicker,
                courseStatusCombo,
                courseImageUrlField,
                coursePrerequisitesArea
        );
        if (courseModuleCombo.getValue() == null) {
            errors.put("module", "Choisissez un module pour le cours.");
        }
        if (!errors.isEmpty()) {
            showError(String.join("\n", errors.values()));
            return;
        }

        try {
            Cours cours = coursFormHelper.buildCours(
                    editingCours,
                    courseModuleCombo.getValue(),
                    courseCodeField,
                    courseTitleField,
                    courseDescriptionArea,
                    courseLevelField,
                    courseCreditsField,
                    courseLanguageField,
                    courseStartDatePicker,
                    courseEndDatePicker,
                    courseStatusCombo,
                    courseImageUrlField,
                    coursePrerequisitesArea
            );
            if (editingCours == null) {
                if (currentTeacherId != null) {
                    coursService.createForTeacher(cours, currentTeacherId);
                } else {
                    coursService.create(cours);
                }
                courseStatusLabel.setText("Cours cree avec succes.");
            } else {
                if (currentTeacherId != null) {
                    coursService.updateForTeacher(cours, currentTeacherId);
                } else {
                    coursService.update(cours);
                }
                courseStatusLabel.setText("Cours mis a jour.");
            }
            selectedModule = cours.getModule();
            selectedCours = cours;
            loadCourses();
            loadContents();
            hideCourseForm();
            resetCourseForm();
        } catch (SQLException e) {
            showError("Enregistrement cours impossible: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteCourse() {
        if (editingCours == null || editingCours.getId() == null) {
            showError("Selectionnez un cours a supprimer.");
            return;
        }
        try {
            coursService.delete(editingCours.getId());
            selectedCours = null;
            courseStatusLabel.setText("Cours supprime.");
            loadCourses();
            loadContents();
            hideCourseForm();
            resetCourseForm();
        } catch (SQLException e) {
            showError("Suppression cours impossible: " + e.getMessage());
        }
    }

    @FXML
    private void handleNewContent() {
        resetContentForm();
        if (selectedCours != null) {
            contentCourseCombo.setValue(selectedCours);
        }
        showContentForm("Nouveau contenu");
        managementTabPane.getSelectionModel().select(contentsTab);
    }

    @FXML
    private void handleSaveContent() {
        List<String> selectedTypes = selectedContentTypes();
        Map<String, String> errors = contenuFormHelper.validateContenuForm(
                selectedTypes,
                contentTitleField,
                contentDescriptionArea,
                contentDurationField,
                contentOrderField,
                contentPublicCombo,
                contentViewsField,
                contentFormatField,
                contentResourcesArea,
                contentUrlField,
                contentPdfField,
                contentPptField,
                contentVideoField,
                contentLinkField,
                contentQuizField
        );
        if (contentCourseCombo.getValue() == null) {
            errors.put("cours", "Choisissez un cours pour le contenu.");
        }
        if (!errors.isEmpty()) {
            showError(String.join("\n", errors.values()));
            return;
        }

        try {
            List<String> fileWarnings = persistSelectedContentFiles();
            Contenu contenu = contenuFormHelper.buildContenu(
                    editingContenu,
                    contentCourseCombo.getValue(),
                    selectedTypes,
                    contentTitleField,
                    contentUrlField,
                    contentDescriptionArea,
                    contentDurationField,
                    contentOrderField,
                    contentPublicCombo,
                    contentAddedDatePicker,
                    contentViewsField,
                    contentFormatField,
                    contentResourcesArea,
                    contentPdfField,
                    contentPptField,
                    contentVideoField,
                    contentLinkField,
                    contentQuizField
            );
            CourseContentNotificationService.NotificationDispatchResult notificationResult =
                    CourseContentNotificationService.NotificationDispatchResult.none();
            if (editingContenu == null) {
                contenuService.create(contenu);
                notificationResult = notifyStudentsForNewContent(contenu);
                contentStatusLabel.setText(buildCreatedContentMessage(fileWarnings, notificationResult));
            } else {
                contenuService.update(contenu);
                contentStatusLabel.setText(fileWarnings.isEmpty()
                        ? "Contenu mis a jour."
                        : "Contenu mis a jour. Certains fichiers sont restes a leur emplacement d'origine.");
            }
            showContentWarnings(fileWarnings, notificationResult);
            selectedCours = contenu.getCours();
            loadContents();
            hideContentForm();
            resetContentForm();
        } catch (SQLException | IOException e) {
            showError("Enregistrement contenu impossible: " + e.getMessage());
        }
    }

    private CourseContentNotificationService.NotificationDispatchResult notifyStudentsForNewContent(Contenu contenu) {
        try {
            return contentNotificationService.notifyStudentsForNewContent(contenu, UserSession.getCurrentUser());
        } catch (SQLException e) {
            return CourseContentNotificationService.NotificationDispatchResult.failed(0, 0, e.getMessage());
        }
    }

    private String buildCreatedContentMessage(
            List<String> fileWarnings,
            CourseContentNotificationService.NotificationDispatchResult notificationResult
    ) {
        StringBuilder message = new StringBuilder("Contenu cree avec succes.");
        if (notificationResult.hasFailure()) {
            if (notificationResult.getDeliveredCount() > 0) {
                message.append(" ")
                        .append(notificationResult.getDeliveredCount())
                        .append("/")
                        .append(notificationResult.getRecipientCount())
                        .append(" email(s) ont ete envoyes.");
            } else {
                message.append(" Aucun email n'a ete envoye.");
            }
        } else if (notificationResult.hasRecipients()) {
            message.append(" ")
                    .append(notificationResult.getDeliveredCount())
                    .append(notificationResult.getDeliveredCount() > 1 ? " emails ont ete envoyes." : " email a ete envoye.");
        } else {
            message.append(" Aucun etudiant inscrit a notifier.");
        }
        if (!fileWarnings.isEmpty()) {
            message.append(" Certains fichiers sont restes a leur emplacement d'origine.");
        }
        return message.toString();
    }

    private void showContentWarnings(
            List<String> fileWarnings,
            CourseContentNotificationService.NotificationDispatchResult notificationResult
    ) {
        List<String> warnings = new ArrayList<>(fileWarnings);
        if (notificationResult.hasFailure()) {
            StringBuilder builder = new StringBuilder("Le contenu a ete cree, mais l'envoi des emails a echoue");
            if (notificationResult.getRecipientCount() > 0) {
                builder.append(" (")
                        .append(notificationResult.getDeliveredCount())
                        .append("/")
                        .append(notificationResult.getRecipientCount())
                        .append(" envoyes)");
            }
            if (!notificationResult.getErrorMessage().isBlank()) {
                builder.append(": ").append(notificationResult.getErrorMessage());
            } else {
                builder.append(".");
            }
            warnings.add(builder.toString());
        }
        if (!warnings.isEmpty()) {
            showWarning(String.join("\n", warnings));
        }
    }

    @FXML
    private void handleDeleteContent() {
        if (editingContenu == null || editingContenu.getId() == null) {
            showError("Selectionnez un contenu a supprimer.");
            return;
        }
        try {
            contenuService.delete(editingContenu.getId());
            contentStatusLabel.setText("Contenu supprime.");
            loadContents();
            hideContentForm();
            resetContentForm();
        } catch (SQLException e) {
            showError("Suppression contenu impossible: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditModule() {
        if (moduleReadOnly) {
            showError("Les modules sont en lecture seule pour l'enseignant.");
            return;
        }
        if (editingModule == null) {
            showError("Selectionnez un module a modifier.");
            return;
        }
        moduleFormHelper.populateForm(
                editingModule,
                moduleTitleField,
                moduleDescriptionArea,
                moduleOrderField,
                moduleObjectivesArea,
                moduleDurationField,
                modulePublicationDatePicker,
                moduleStatusCombo,
                moduleResourcesArea
        );
        moduleStatusLabel.setText("Edition du module #" + editingModule.getId());
        showModuleForm("Modifier module");
        managementTabPane.getSelectionModel().select(modulesTab);
    }

    @FXML
    private void handleOpenModuleCourses() {
        Module module = moduleTable.getSelectionModel().getSelectedItem();
        if (module == null) {
            showError("Selectionnez un module pour afficher ses cours.");
            return;
        }
        selectedModule = module;
        applyCourseFilter();
        resetCourseForm();
        managementTabPane.getSelectionModel().select(coursesTab);
    }

    @FXML
    private void handleEditCourse() {
        if (editingCours == null) {
            showError("Selectionnez un cours a modifier.");
            return;
        }
        coursFormHelper.populateForm(
                editingCours,
                courseCodeField,
                courseTitleField,
                courseDescriptionArea,
                courseLevelField,
                courseCreditsField,
                courseLanguageField,
                courseStartDatePicker,
                courseEndDatePicker,
                courseStatusCombo,
                courseImageUrlField,
                coursePrerequisitesArea
        );
        selectModuleForCourse(editingCours);
        courseStatusLabel.setText("Edition du cours #" + editingCours.getId());
        showCourseForm("Modifier cours");
        managementTabPane.getSelectionModel().select(coursesTab);
    }

    @FXML
    private void handleOpenCourseContents() {
        Cours cours = courseTable.getSelectionModel().getSelectedItem();
        if (cours == null) {
            showError("Selectionnez un cours pour afficher ses contenus.");
            return;
        }
        selectedCours = cours;
        applyContentFilter();
        resetContentForm();
        managementTabPane.getSelectionModel().select(contentsTab);
    }

    @FXML
    private void handleEditContent() {
        if (editingContenu == null) {
            showError("Selectionnez un contenu a modifier.");
            return;
        }
        contenuFormHelper.populateForm(
                editingContenu,
                contentVideoCheck,
                contentPdfCheck,
                contentPptCheck,
                contentTextCheck,
                contentQuizCheck,
                contentLinkCheck,
                contentTitleField,
                contentUrlField,
                contentDescriptionArea,
                contentDurationField,
                contentOrderField,
                contentPublicCombo,
                contentAddedDatePicker,
                contentViewsField,
                contentFormatField,
                contentResourcesArea,
                contentPdfField,
                contentPptField,
                contentVideoField,
                contentLinkField,
                contentQuizField
        );
        selectCourseForContent(editingContenu);
        contentStatusLabel.setText("Edition du contenu #" + editingContenu.getId());
        showContentForm("Modifier contenu");
        managementTabPane.getSelectionModel().select(contentsTab);
    }

    @FXML
    private void handleCancelModule() {
        hideModuleForm();
        resetModuleForm();
    }

    @FXML
    private void handleCancelCourse() {
        hideCourseForm();
        resetCourseForm();
    }

    @FXML
    private void handleCancelContent() {
        hideContentForm();
        resetContentForm();
    }

    @FXML
    private void handleBrowseContentPdf() {
        File file = chooseFile("Choisir un fichier PDF", new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        if (file != null) {
            selectedContentPdfFile = file;
            contentPdfField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleBrowseContentPpt() {
        File file = chooseFile(
                "Choisir un fichier PowerPoint",
                new FileChooser.ExtensionFilter("Fichiers PowerPoint", "*.ppt", "*.pptx")
        );
        if (file != null) {
            selectedContentPptFile = file;
            contentPptField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleBrowseContentVideo() {
        File file = chooseFile(
                "Choisir une video",
                new FileChooser.ExtensionFilter("Fichiers video", "*.mp4", "*.avi", "*.mov", "*.mkv", "*.wmv", "*.webm")
        );
        if (file != null) {
            selectedContentVideoFile = file;
            contentVideoField.setText(file.getAbsolutePath());
        }
    }

    private List<String> selectedContentTypes() {
        List<String> types = new ArrayList<>();
        if (contentVideoCheck.isSelected()) {
            types.add("video");
        }
        if (contentPdfCheck.isSelected()) {
            types.add("pdf");
        }
        if (contentPptCheck.isSelected()) {
            types.add("ppt");
        }
        if (contentTextCheck.isSelected()) {
            types.add("texte");
        }
        if (contentQuizCheck.isSelected()) {
            types.add("quiz");
        }
        if (contentLinkCheck.isSelected()) {
            types.add("lien");
        }
        return types;
    }

    private void selectModuleForCourse(Cours cours) {
        if (cours.getModule() == null || cours.getModule().getId() == null) {
            courseModuleCombo.setValue(null);
            return;
        }
        Module resolvedModule = modules.stream()
                .filter(module -> Objects.equals(module.getId(), cours.getModule().getId()))
                .findFirst()
                .orElse(cours.getModule());
        boolean moduleChanged = selectedModule == null
                || !Objects.equals(selectedModule.getId(), resolvedModule.getId());
        selectedModule = resolvedModule;
        courseModuleCombo.getItems().stream()
                .filter(module -> Objects.equals(module.getId(), cours.getModule().getId()))
                .findFirst()
                .ifPresent(courseModuleCombo::setValue);
        if (moduleChanged) {
            applyCourseFilter();
        }
    }

    private void selectCourseForContent(Contenu contenu) {
        if (contenu.getCours() == null || contenu.getCours().getId() == null) {
            contentCourseCombo.setValue(null);
            return;
        }
        Cours resolvedCourse = courses.stream()
                .filter(cours -> Objects.equals(cours.getId(), contenu.getCours().getId()))
                .findFirst()
                .orElse(contenu.getCours());
        boolean courseChanged = selectedCours == null
                || !Objects.equals(selectedCours.getId(), resolvedCourse.getId());
        selectedCours = resolvedCourse;
        contentCourseCombo.getItems().stream()
                .filter(cours -> Objects.equals(cours.getId(), contenu.getCours().getId()))
                .findFirst()
                .ifPresent(contentCourseCombo::setValue);
        if (courseChanged) {
            applyContentFilter();
        }
    }

    private void resetModuleForm() {
        editingModule = null;
        moduleFormHelper.clearForm(
                moduleTitleField,
                moduleDescriptionArea,
                moduleOrderField,
                moduleObjectivesArea,
                moduleDurationField,
                modulePublicationDatePicker,
                moduleStatusCombo,
                moduleResourcesArea
        );
        moduleStatusLabel.setText("Nouveau module.");
    }

    private void resetCourseForm() {
        editingCours = null;
        coursFormHelper.clearForm(
                courseCodeField,
                courseTitleField,
                courseDescriptionArea,
                courseLevelField,
                courseCreditsField,
                courseLanguageField,
                courseStartDatePicker,
                courseEndDatePicker,
                courseStatusCombo,
                courseImageUrlField,
                coursePrerequisitesArea
        );
        courseModuleCombo.setValue(selectedModule);
        courseStatusLabel.setText("Nouveau cours.");
    }

    private void resetContentForm() {
        editingContenu = null;
        selectedContentPdfFile = null;
        selectedContentPptFile = null;
        selectedContentVideoFile = null;
        contenuFormHelper.clearForm(
                contentVideoCheck,
                contentPdfCheck,
                contentPptCheck,
                contentTextCheck,
                contentQuizCheck,
                contentLinkCheck,
                contentTitleField,
                contentUrlField,
                contentDescriptionArea,
                contentDurationField,
                contentOrderField,
                contentPublicCombo,
                contentAddedDatePicker,
                contentViewsField,
                contentFormatField,
                contentResourcesArea,
                contentPdfField,
                contentPptField,
                contentVideoField,
                contentLinkField,
                contentQuizField
        );
        contentCourseCombo.setValue(selectedCours);
        contentStatusLabel.setText("Nouveau contenu.");
    }

    private File chooseFile(String title, FileChooser.ExtensionFilter extensionFilter) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(extensionFilter);

        Stage stage = contentTitleField != null && contentTitleField.getScene() != null
                ? (Stage) contentTitleField.getScene().getWindow()
                : null;
        return stage != null ? fileChooser.showOpenDialog(stage) : null;
    }

    private List<String> persistSelectedContentFiles() throws IOException {
        List<String> warnings = new ArrayList<>();
        if (selectedContentPdfFile != null) {
            contentPdfField.setText(saveUploadedContentFile(selectedContentPdfFile, "PDF", warnings));
        }
        if (selectedContentPptFile != null) {
            contentPptField.setText(saveUploadedContentFile(selectedContentPptFile, "PPT", warnings));
        }
        if (selectedContentVideoFile != null) {
            contentVideoField.setText(saveUploadedContentFile(selectedContentVideoFile, "video", warnings));
        }
        return warnings;
    }

    private String saveUploadedContentFile(File sourceFile, String resourceLabel, List<String> warnings) throws IOException {
        Path sourcePath = sourceFile.toPath().toAbsolutePath().normalize();
        Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();

        if (sourcePath.startsWith(uploadDir)) {
            return sourcePath.toString();
        }

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        if (hasInsufficientSpace(uploadDir, sourcePath)) {
            warnings.add(buildInsufficientSpaceWarning(resourceLabel, sourcePath));
            return sourcePath.toString();
        }

        String filename = System.currentTimeMillis() + "_" + sourceFile.getName();
        Path targetPath = uploadDir.resolve(filename);

        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return Paths.get("uploads").resolve(filename).toString();
        } catch (FileSystemException e) {
            if (isInsufficientSpaceError(e)) {
                warnings.add(buildInsufficientSpaceWarning(resourceLabel, sourcePath));
                return sourcePath.toString();
            }
            throw e;
        }
    }

    private boolean hasInsufficientSpace(Path uploadDir, Path sourcePath) throws IOException {
        long fileSize = Files.size(sourcePath);
        long usableSpace = Files.getFileStore(uploadDir).getUsableSpace();
        return fileSize > 0 && usableSpace < fileSize;
    }

    private boolean isInsufficientSpaceError(IOException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return false;
        }
        String normalized = message.toLowerCase(Locale.ROOT);
        return normalized.contains("espace insuffisant")
                || normalized.contains("not enough space")
                || normalized.contains("insufficient disk space");
    }

    private String buildInsufficientSpaceWarning(String resourceLabel, Path sourcePath) {
        return "Espace insuffisant dans le dossier uploads. Le fichier "
                + resourceLabel
                + " sera conserve avec son chemin local: "
                + sourcePath;
    }

    private void showModuleForm(String title) {
        if (moduleReadOnly) {
            return;
        }
        moduleFormPanel.setManaged(true);
        moduleFormPanel.setVisible(true);
        moduleFormTitleLabel.setText(title);
    }

    private void hideModuleForm() {
        moduleFormPanel.setVisible(false);
        moduleFormPanel.setManaged(false);
        moduleFormTitleLabel.setText("Formulaire module");
    }

    private void showCourseForm(String title) {
        courseFormPanel.setManaged(true);
        courseFormPanel.setVisible(true);
        courseFormTitleLabel.setText(title);
    }

    private void hideCourseForm() {
        courseFormPanel.setVisible(false);
        courseFormPanel.setManaged(false);
        courseFormTitleLabel.setText("Formulaire cours");
    }

    private void showContentForm(String title) {
        contentFormPanel.setManaged(true);
        contentFormPanel.setVisible(true);
        contentFormTitleLabel.setText(title);
    }

    private void hideContentForm() {
        contentFormPanel.setVisible(false);
        contentFormPanel.setManaged(false);
        contentFormTitleLabel.setText("Formulaire contenu");
    }

    private void applyCourseFilter() {
        List<Cours> nextCourses = courses.stream()
                .filter(cours -> selectedModule == null
                        || selectedModule.getId() == null
                        || (cours.getModule() != null && Objects.equals(cours.getModule().getId(), selectedModule.getId())))
                .toList();

        syncingCourseSelection = true;
        try {
            filteredCourses.setAll(nextCourses);
            if (selectedCours != null && selectedCours.getId() != null) {
                Cours matchingCourse = filteredCourses.stream()
                        .filter(cours -> Objects.equals(cours.getId(), selectedCours.getId()))
                        .findFirst()
                        .orElse(null);
                if (matchingCourse != null) {
                    courseTable.getSelectionModel().select(matchingCourse);
                    selectedCours = matchingCourse;
                    editingCours = matchingCourse;
                } else {
                    courseTable.getSelectionModel().clearSelection();
                    selectedCours = null;
                    editingCours = null;
                }
            } else {
                courseTable.getSelectionModel().clearSelection();
            }
        } finally {
            syncingCourseSelection = false;
        }

        if (selectedModule == null) {
            courseFilterLabel.setText(moduleReadOnly ? "Vos cours sur tous les modules." : "Cours de tous les modules.");
            navigationStatusLabel.setText(moduleReadOnly
                    ? "Les modules sont geres par l'administration. Selectionnez un module pour afficher vos cours, puis un cours pour gerer ses contenus."
                    : "Selectionnez un module pour afficher ses cours, puis un cours pour afficher ses contenus.");
        } else {
            courseFilterLabel.setText((moduleReadOnly ? "Vos cours du module: " : "Cours du module: ") + selectedModule.getTitreModule());
            navigationStatusLabel.setText("Module actif: " + selectedModule.getTitreModule()
                    + (moduleReadOnly
                    ? ". Vous pouvez gerer vos cours et leurs contenus."
                    : ". Selectionnez un cours pour afficher ses contenus."));
        }

        applyContentFilter();
    }

    private void applyContentFilter() {
        List<Contenu> nextContents = contents.stream()
                .filter(contenu -> selectedCours == null
                        || selectedCours.getId() == null
                        || (contenu.getCours() != null && Objects.equals(contenu.getCours().getId(), selectedCours.getId())))
                .toList();

        syncingContentSelection = true;
        try {
            filteredContents.setAll(nextContents);
            if (editingContenu != null && editingContenu.getId() != null) {
                Contenu matchingContent = filteredContents.stream()
                        .filter(contenu -> Objects.equals(contenu.getId(), editingContenu.getId()))
                        .findFirst()
                        .orElse(null);
                if (matchingContent != null) {
                    contentTable.getSelectionModel().select(matchingContent);
                    editingContenu = matchingContent;
                } else {
                    contentTable.getSelectionModel().clearSelection();
                    editingContenu = null;
                }
            } else {
                contentTable.getSelectionModel().clearSelection();
            }
        } finally {
            syncingContentSelection = false;
        }

        if (selectedCours == null) {
            contentFilterLabel.setText("Contenus de tous les cours.");
        } else {
            contentFilterLabel.setText("Contenus du cours: " + selectedCours.getTitre());
            navigationStatusLabel.setText("Cours actif: " + selectedCours.getTitre() + ". Vous pouvez maintenant gerer les contenus de ce cours.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Avertissement");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
