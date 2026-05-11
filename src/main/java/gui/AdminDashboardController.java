package gui;

import entities.Administrateur;
import entities.Enseignant;
import entities.Etudiant;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import services.AdminAssistantService;
import services.AuthService;
import services.UtilisateurService;
import utils.SceneManager;
import utils.UserSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class AdminDashboardController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+?[0-9]{1,3})?[0-9]{8,15}$");
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{8,}$");
    private static final Map<String, String> UI_LABELS = createUiLabels();
    @FXML private MenuButton notificationMenuButton;
    @FXML
    private Label sectionTitleLabel;

    @FXML
    private Label sectionSubtitleLabel;

    @FXML
    private Label currentUserNameLabel;

    @FXML
    private Label currentUserRoleLabel;

    @FXML
    private MenuButton profileMenuButton;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label studentsCountLabel;

    @FXML
    private Label teachersCountLabel;

    @FXML
    private Label adminsCountLabel;

    @FXML
    private VBox usersSection;

    @FXML
    private VBox statsSection;

    @FXML
    private VBox createSection;

    @FXML
    private VBox modulesSection;

    @FXML
    private javafx.scene.layout.StackPane modulesContentPane;

    @FXML
    private VBox courseStatisticsSection;

    @FXML
    private javafx.scene.layout.StackPane courseStatisticsContentPane;

    @FXML
    private VBox evenementsSection;

    @FXML
    private javafx.scene.layout.StackPane evenementsContentPane;

    @FXML
    private VBox forumSection;

    @FXML
    private javafx.scene.layout.StackPane forumContentPane;

    @FXML
    private VBox messageSection;

    @FXML
    private javafx.scene.layout.StackPane messageContentPane;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> roleFilterCombo;

    @FXML
    private TableView<UtilisateurRow> usersTable;

    @FXML
    private TableColumn<UtilisateurRow, Long> idColumn;

    @FXML
    private TableColumn<UtilisateurRow, String> fullNameColumn;

    @FXML
    private TableColumn<UtilisateurRow, String> emailColumn;

    @FXML
    private TableColumn<UtilisateurRow, String> roleColumn;

    @FXML
    private TableColumn<UtilisateurRow, String> identifierColumn;

    @FXML
    private TableColumn<UtilisateurRow, String> statusColumn;

    @FXML
    private TableColumn<UtilisateurRow, String> lastLoginColumn;

    @FXML
    private TableColumn<UtilisateurRow, Void> actionsColumn;

    @FXML
    private Label detailNameLabel;

    @FXML
    private Label detailEmailLabel;

    @FXML
    private Label detailRoleLabel;

    @FXML
    private Label detailIdentifierLabel;

    @FXML
    private Label detailStatusLabel;

    @FXML
    private Label detailInfoLabel;

    @FXML
    private Label detailCreatedLabel;

    @FXML
    private Label detailLastLoginLabel;

    @FXML
    private ListView<String> activityList;

    @FXML
    private TableView<StatRow> statisticsTable;

    @FXML
    private TableColumn<StatRow, String> metricColumn;

    @FXML
    private TableColumn<StatRow, String> valueColumn;

    @FXML
    private BarChart<String, Number> usersRoleBarChart;
    @FXML
    private PieChart usersPieChart;

    @FXML
    private ComboBox<String> createRoleCombo;

    @FXML
    private TextField createNomField;

    @FXML
    private TextField createPrenomField;

    @FXML
    private TextField createEmailField;

    @FXML
    private TextField createPasswordField;

    @FXML
    private Label createIdentifierLabel;

    @FXML
    private TextField createIdentifierField;
    @FXML
    private ComboBox<String> createIdentifierCombo;

    @FXML
    private Label createFieldOneLabel;

    @FXML
    private TextField createFieldOneField;
    @FXML
    private ComboBox<String> createFieldOneCombo;

    @FXML
    private Label createFieldTwoLabel;

    @FXML
    private TextField createFieldTwoField;
    @FXML
    private ComboBox<String> createFieldTwoCombo;

    @FXML
    private Label createFieldThreeLabel;

    @FXML
    private TextField createFieldThreeField;
    @FXML
    private ComboBox<String> createFieldThreeCombo;

    @FXML
    private Label createFieldFourLabel;

    @FXML
    private TextField createFieldFourField;
    @FXML
    private ComboBox<String> createFieldFourCombo;

    @FXML
    private Label createDateLabel;

    @FXML
    private DatePicker createDatePicker;

    @FXML
    private Label createAreaLabel;

    @FXML
    private TextArea createAreaField;

    @FXML
    private Label createStatusLabel;

    @FXML
    private javafx.scene.control.Button createSubmitButton;

    @FXML
    private javafx.scene.control.Button createBackButton;

    @FXML
    private javafx.scene.control.Button createClearButton;
    @FXML private ListView<String> statsInsightsList;
    private final ObservableList<UtilisateurRow> masterRows = FXCollections.observableArrayList();
    private final ObservableList<String> activityItems = FXCollections.observableArrayList();
    private final ObservableList<StatRow> statRows = FXCollections.observableArrayList();
    private final FilteredList<UtilisateurRow> filteredRows = new FilteredList<>(masterRows, row -> true);
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final AuthService authService = new AuthService();
    private final AdminAssistantService adminAssistantService = new AdminAssistantService();
    private AdminCourseStatisticsController adminCourseStatisticsController;
    private Utilisateur editingUtilisateur;
    private final ObservableList<String> statsInsights = FXCollections.observableArrayList();
    @FXML
    private void initialize() {
        configureCurrentUserHeader();
        configureUserTable();
        configureStatisticsTable();
        configureCreateForm();
        showUsersPage();
        loadDashboardData();
        if (notificationMenuButton != null) {
            utils.NotificationUIHelper.setupNotificationMenu(notificationMenuButton);
        }
    }

    @FXML
    private void showUsersPage() {
        setSectionVisibility(true, false, false, false, false, false, false, false);
        sectionTitleLabel.setText("User Registry");
        sectionSubtitleLabel.setText("Filter, inspect, edit, and remove platform users.");
    }

    @FXML
    private void showStatsPage() {
        setSectionVisibility(false, true, false, false, false, false, false, false);
        sectionTitleLabel.setText("Statistics");
        sectionSubtitleLabel.setText("Track role distribution and system-level user activity.");
    }

    @FXML
    private void showCreatePage() {
        setSectionVisibility(false, false, true, false, false, false, false, false);
        updateCreateSectionHeader();
    }
    @FXML
    private void openForumPage() {
        try {
            SceneManager.switchScene("/gui/admin_forum.fxml", "Forum");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation forum", e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @FXML
    private void openMessagePage() {
        try {
            SceneManager.switchScene("/gui/admin_message.fxml", "Messagerie");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation messagerie", e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @FXML
    private void showModulesPage() {
        setSectionVisibility(false, false, false, true, false, false, false, false);
        sectionTitleLabel.setText("Modules");
        sectionSubtitleLabel.setText("Manage modules, courses, and content without leaving the admin dashboard.");
        loadModulesContent();
    }

    @FXML
    private void showCourseStatisticsPage() {
        setSectionVisibility(false, false, false, false, true, false, false, false);
        sectionTitleLabel.setText("Statistiques cours");
        sectionSubtitleLabel.setText("Analysez les vues, le temps passe et la performance de chaque cours et module.");
        loadCourseStatisticsContent();
        if (adminCourseStatisticsController != null) {
            adminCourseStatisticsController.refreshStats();
        }
    }

    @FXML
    private void showEvenementsPage() {
        setSectionVisibility(false, false, false, false, false, true, false, false);
        sectionTitleLabel.setText("Evenements");
        sectionSubtitleLabel.setText("Manage the event module inside the current admin dashboard template.");
        loadEvenementsContent();
    }

    @FXML
    private void showForumPage() {
        setSectionVisibility(false, false, false, false, false, false, true, false);
        sectionTitleLabel.setText("Forums");
        sectionSubtitleLabel.setText("Integrated forum work from dev-ahmed adapted to the current admin dashboard.");
        loadForumContent();
    }

    @FXML
    private void showMessagePage() {
        setSectionVisibility(false, false, false, false, false, false, false, true);
        sectionTitleLabel.setText("Messages");
        sectionSubtitleLabel.setText("Integrated messaging work from dev-ahmed inside the current admin workspace.");
        loadMessageContent();
    }

    @FXML
    private void refreshData() {
        if (courseStatisticsSection != null && courseStatisticsSection.isVisible() && adminCourseStatisticsController != null) {
            adminCourseStatisticsController.refreshStats();
            return;
        }
        loadDashboardData();
    }

    @FXML
    private void openAdminAssistant() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Admin AI Assistant");
        dialog.setHeaderText("Ask about the desktop app or live database.");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialogPane.getStyleClass().add("assistant-dialog");
        String stylesheet = getClass().getResource("/style.css") != null
                ? getClass().getResource("/style.css").toExternalForm()
                : null;
        if (stylesheet != null && !dialogPane.getStylesheets().contains(stylesheet)) {
            dialogPane.getStylesheets().add(stylesheet);
        }

        Label titleLabel = new Label("Admin AI Assistant");
        titleLabel.getStyleClass().add("assistant-title");

        Label helperLabel = new Label("Ask about users, modules, forums, messages, login, password reset, or database data.");
        helperLabel.getStyleClass().add("assistant-helper");
        helperLabel.setWrapText(true);

        VBox conversationBox = new VBox(12);
        conversationBox.getStyleClass().add("assistant-conversation");
        appendAssistantMessage(
                conversationBox,
                "Hi. I can answer admin questions about the existing desktop app and the current database. I also respond to simple greetings locally."
        );

        ScrollPane conversationPane = new ScrollPane(conversationBox);
        conversationPane.setFitToWidth(true);
        conversationPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        conversationPane.getStyleClass().add("assistant-scroll");

        TextArea inputArea = new TextArea();
        inputArea.setWrapText(true);
        inputArea.setPromptText("Example: How many rows are in utilisateur? What does the reset password flow do?");
        inputArea.setPrefRowCount(4);
        inputArea.getStyleClass().add("assistant-input");

        Label statusLabel = new Label("Scope is limited to existing app functionality and live database info.");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setWrapText(true);

        Button sendButton = new Button("Ask AI");
        sendButton.getStyleClass().add("primary-button");
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(event -> submitAdminAssistantQuestion(inputArea, conversationBox, conversationPane, statusLabel, sendButton));

        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("secondary-button");
        closeButton.setOnAction(event -> dialog.close());

        HBox actions = new HBox(10, closeButton, sendButton);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getStyleClass().add("assistant-actions");

        VBox hero = new VBox(6, titleLabel, helperLabel);
        hero.getStyleClass().add("assistant-hero");

        VBox content = new VBox(14, hero, conversationPane, inputArea, statusLabel, actions);
        content.setPadding(new Insets(18));
        content.getStyleClass().add("assistant-shell");
        VBox.setVgrow(conversationPane, Priority.ALWAYS);
        dialogPane.setContent(content);
        dialogPane.setPrefSize(820, 680);

        dialog.show();
    }

    @FXML
    private void handleProfileLogout() throws IOException {
        UserSession.clear();
        SceneManager.switchScene("/login.fxml", "Campus Access");
    }

    @FXML
    private void createUser() {
        String role = createRoleCombo.getValue();
        String validationError = validateCreateForm(role);
        if (validationError != null) {
            createStatusLabel.setText(validationError);
            showValidationAlert(validationError);
            return;
        }

        try {
            if (editingUtilisateur == null) {
                String hashedPassword = authService.hashPassword(createPasswordField.getText().trim());
                Utilisateur utilisateur = buildUtilisateurFromCreateForm(role, hashedPassword);
                utilisateurService.createUtilisateur(utilisateur);
                createStatusLabel.setText("User created successfully: " + utilisateur.getNomComplet());
                showInfo("User created", "User created successfully: " + utilisateur.getNomComplet());
            } else {
                applyCreateFormToExisting(editingUtilisateur);
                utilisateurService.updateUtilisateur(editingUtilisateur);
                createStatusLabel.setText("User updated successfully: " + editingUtilisateur.getNomComplet());
                showInfo("User updated", "User updated successfully: " + editingUtilisateur.getNomComplet());
            }
            clearCreateForm();
            loadDashboardData();
            showUsersPage();
        } catch (IOException | SQLException e) {
            createStatusLabel.setText((editingUtilisateur == null ? "Create failed: " : "Update failed: ") + e.getMessage());
            showError("Save failed", e.getMessage());
        }
    }

    @FXML
    private void cancelEditMode() {
        enterCreateMode("administrateur");
    }

    @FXML
    private void resetCreateFormView() {
        if (editingUtilisateur != null) {
            populateCreateFormForEdit(editingUtilisateur);
            createStatusLabel.setText("Editing " + editingUtilisateur.getNomComplet() + ". Form restored.");
            return;
        }

        String selectedRole = createRoleCombo.getValue() == null ? "administrateur" : createRoleCombo.getValue();
        clearCreateFormFields();
        createRoleCombo.setValue(selectedRole);
        updateCreateFormForRole(selectedRole);
        createStatusLabel.setText("Form cleared. Ready to add a new user.");
    }

    private void configureCurrentUserHeader() {
        Utilisateur currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            currentUserNameLabel.setText("Guest");
            currentUserRoleLabel.setText("-");
            profileMenuButton.setText("G");
            return;
        }

        currentUserNameLabel.setText(currentUser.getNomComplet());
        currentUserRoleLabel.setText(currentUser.getType());
        profileMenuButton.setText(buildInitials(currentUser));
    }

    private void configureUserTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        identifierColumn.setCellValueFactory(new PropertyValueFactory<>("identifier"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        lastLoginColumn.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));

        roleFilterCombo.setItems(FXCollections.observableArrayList("All", "administrateur", "enseignant", "etudiant"));
        roleFilterCombo.setValue("All");
        configureComboDisplay(roleFilterCombo);
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        roleFilterCombo.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());

        SortedList<UtilisateurRow> sortedRows = new SortedList<>(filteredRows);
        sortedRows.comparatorProperty().bind(usersTable.comparatorProperty());
        usersTable.setItems(sortedRows);
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> updateDetails(newValue));

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final javafx.scene.control.Button viewButton = new javafx.scene.control.Button("View");
            private final javafx.scene.control.Button editButton = new javafx.scene.control.Button("Modify");
            private final javafx.scene.control.Button deleteButton = new javafx.scene.control.Button("Delete");
            private final HBox box = new HBox(8, viewButton, editButton, deleteButton);

            {
                viewButton.getStyleClass().addAll("secondary-button", "table-action-button");
                editButton.getStyleClass().addAll("primary-button", "table-action-button");
                deleteButton.getStyleClass().addAll("danger-button", "table-action-button");

                viewButton.setOnAction(event -> showUserDetailsDialog(getTableView().getItems().get(getIndex())));
                editButton.setOnAction(event -> editUser(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> deleteUser(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        activityList.setItems(activityItems);
    }

    private void configureStatisticsTable() {
        metricColumn.setCellValueFactory(new PropertyValueFactory<>("metric"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        statisticsTable.setItems(statRows);
        if (statsInsightsList != null) statsInsightsList.setItems(statsInsights);
    }

    private void configureCreateForm() {
        createRoleCombo.setItems(FXCollections.observableArrayList("administrateur", "enseignant", "etudiant"));
        createRoleCombo.setValue("administrateur");
        createIdentifierCombo.setItems(FXCollections.observableArrayList(
                "Direction Generale", "Scolarite", "Ressources Humaines", "Finances", "Informatique", "Communication", "Recherche"
        ));
        createFieldOneCombo.setItems(FXCollections.observableArrayList(
                "Responsable administratif", "Chef de service", "Coordinateur", "Assistant administratif", "Directeur"
        ));
        createFieldTwoCombo.setItems(FXCollections.observableArrayList(
                "GL", "IA", "DS", "Cybersecurite", "Reseaux", "IoT", "Cloud", "DevOps", "Finance", "RH"
        ));
        createFieldThreeCombo.setItems(FXCollections.observableArrayList("CDI", "CDD", "Vacataire", "Contractuel"));
        createFieldFourCombo.setItems(FXCollections.observableArrayList("actif", "inactif", "conge", "retraite"));
        createFieldTwoCombo.setEditable(true);
        configureComboDisplay(createRoleCombo);
        configureComboDisplay(createIdentifierCombo);
        configureComboDisplay(createFieldOneCombo);
        configureComboDisplay(createFieldTwoCombo);
        configureComboDisplay(createFieldThreeCombo);
        configureComboDisplay(createFieldFourCombo);
        createRoleCombo.valueProperty().addListener((obs, oldValue, newValue) -> updateCreateFormForRole(newValue));
        createBackButton.setVisible(false);
        createBackButton.setManaged(false);
        updateCreateFormForRole(createRoleCombo.getValue());
    }

    private void updateCreateFormForRole(String role) {
        showCombo(createIdentifierCombo, createIdentifierField, false);
        showCombo(createFieldOneCombo, createFieldOneField, false);
        showCombo(createFieldTwoCombo, createFieldTwoField, false);
        showCombo(createFieldThreeCombo, createFieldThreeField, false);
        showCombo(createFieldFourCombo, createFieldFourField, false);

        if ("etudiant".equals(role)) {
            createIdentifierLabel.setText("Student ID");
            createFieldOneLabel.setText("Level");
            createFieldTwoLabel.setText("Specialization");
            createFieldThreeLabel.setText("Phone");
            createFieldFourLabel.setText("Status");
            createDateLabel.setText("Birth Date");
            createAreaLabel.setText("Address");
            createFieldFourLabel.setVisible(true);
            createFieldFourLabel.setManaged(true);
            createFieldFourField.setVisible(true);
            createFieldFourField.setManaged(true);
            createDateLabel.setVisible(true);
            createDateLabel.setManaged(true);
            createDatePicker.setVisible(true);
            createDatePicker.setManaged(true);
            createAreaLabel.setVisible(true);
            createAreaLabel.setManaged(true);
            createAreaField.setVisible(true);
            createAreaField.setManaged(true);

            createFieldOneCombo.setItems(FXCollections.observableArrayList(
                    "Licence 1", "Licence 2", "Licence 3", "Master 1", "Master 2", "Doctorat"
            ));
            createFieldFourCombo.setItems(FXCollections.observableArrayList("actif", "inactif", "diplome", "suspendu"));
            showCombo(createFieldOneCombo, createFieldOneField, true);
            showCombo(createFieldTwoCombo, createFieldTwoField, true);
            showCombo(createFieldFourCombo, createFieldFourField, true);

            if (createFieldOneCombo.getValue() == null) {
                createFieldOneCombo.setValue("Licence 1");
            }
            if (createFieldFourCombo.getValue() == null) {
                createFieldFourCombo.setValue("actif");
            }
            createDatePicker.setValue(LocalDate.of(2000, 1, 1));
            createAreaField.setText("Desktop created student");
            return;
        }

        if ("enseignant".equals(role)) {
            createIdentifierLabel.setText("Teacher ID");
            createFieldOneLabel.setText("Diploma");
            createFieldTwoLabel.setText("Specialty");
            createFieldThreeLabel.setText("Contract");
            createFieldFourLabel.setText("Status");
            createDateLabel.setText("Unused");
            createAreaLabel.setText("Availability");
            createFieldFourLabel.setVisible(true);
            createFieldFourLabel.setManaged(true);
            createFieldFourField.setVisible(true);
            createFieldFourField.setManaged(true);
            createDateLabel.setVisible(false);
            createDateLabel.setManaged(false);
            createDatePicker.setVisible(false);
            createDatePicker.setManaged(false);
            createAreaLabel.setVisible(true);
            createAreaLabel.setManaged(true);
            createAreaField.setVisible(true);
            createAreaField.setManaged(true);
            createFieldOneCombo.setItems(FXCollections.observableArrayList("Licence", "Master", "Doctorat", "HDR", "Ingenieur"));
            createFieldTwoCombo.setItems(FXCollections.observableArrayList(
                    "Informatique", "Mathematiques", "Physique", "Chimie", "Biologie", "Economie", "Langues", "Droit", "Gestion"
            ));
            createFieldThreeCombo.setItems(FXCollections.observableArrayList("CDI", "CDD", "Vacataire", "Contractuel"));
            createFieldFourCombo.setItems(FXCollections.observableArrayList("actif", "inactif", "conge", "retraite"));
            showCombo(createFieldOneCombo, createFieldOneField, true);
            showCombo(createFieldTwoCombo, createFieldTwoField, true);
            showCombo(createFieldThreeCombo, createFieldThreeField, true);
            showCombo(createFieldFourCombo, createFieldFourField, true);

            if (createFieldThreeCombo.getValue() == null) {
                createFieldThreeCombo.setValue("CDI");
            }
            if (createFieldFourCombo.getValue() == null) {
                createFieldFourCombo.setValue("actif");
            }
            createAreaField.setText("");
            return;
        }

        createIdentifierLabel.setText("Department");
        createFieldOneLabel.setText("Function");
        createFieldTwoLabel.setText("Phone");
        createFieldThreeLabel.setText("Status");
        createFieldFourLabel.setVisible(false);
        createFieldFourLabel.setManaged(false);
        createFieldFourField.setVisible(false);
        createFieldFourField.setManaged(false);
        createDateLabel.setVisible(false);
        createDateLabel.setManaged(false);
        createDatePicker.setVisible(false);
        createDatePicker.setManaged(false);
        createAreaLabel.setVisible(false);
        createAreaLabel.setManaged(false);
        createAreaField.setVisible(false);
        createAreaField.setManaged(false);
        showCombo(createIdentifierCombo, createIdentifierField, true);
        showCombo(createFieldOneCombo, createFieldOneField, true);
        showCombo(createFieldThreeCombo, createFieldThreeField, true);
        createFieldThreeCombo.setItems(FXCollections.observableArrayList("actif", "inactif"));
        if (createIdentifierCombo.getValue() == null) {
            createIdentifierCombo.setValue("Scolarite");
        }
        if (createFieldOneCombo.getValue() == null) {
            createFieldOneCombo.setValue("Responsable administratif");
        }
        if (createFieldThreeCombo.getValue() == null) {
            createFieldThreeCombo.setValue("actif");
        }
    }

    private Utilisateur buildUtilisateurFromCreateForm(String role, String hashedPassword) {
        if ("etudiant".equals(role)) {
            Etudiant etudiant = new Etudiant();
            fillBase(etudiant, hashedPassword);
            etudiant.setMatricule(readValue(createIdentifierField, createIdentifierCombo));
            etudiant.setNiveauEtude(readValue(createFieldOneField, createFieldOneCombo));
            etudiant.setSpecialisation(readValue(createFieldTwoField, createFieldTwoCombo));
            etudiant.setTelephone(readValue(createFieldThreeField, createFieldThreeCombo));
            etudiant.setStatut(defaultIfBlank(readValue(createFieldFourField, createFieldFourCombo), "actif"));
            etudiant.setDateNaissance(createDatePicker.getValue() != null ? createDatePicker.getValue() : LocalDate.of(2000, 1, 1));
            etudiant.setAdresse(defaultIfBlank(createAreaField.getText(), "Desktop created student"));
            etudiant.setDateInscription(LocalDateTime.now());
            return etudiant;
        }

        if ("enseignant".equals(role)) {
            Enseignant enseignant = new Enseignant();
            fillBase(enseignant, hashedPassword);
            enseignant.setMatriculeEnseignant(readValue(createIdentifierField, createIdentifierCombo));
            enseignant.setDiplome(readValue(createFieldOneField, createFieldOneCombo));
            enseignant.setSpecialite(readValue(createFieldTwoField, createFieldTwoCombo));
            enseignant.setTypeContrat(readValue(createFieldThreeField, createFieldThreeCombo));
            enseignant.setDisponibilites(defaultIfBlank(createAreaField.getText(), ""));
            enseignant.setAnneesExperience(0);
            enseignant.setStatut(defaultIfBlank(readValue(createFieldFourField, createFieldFourCombo), "actif"));
            return enseignant;
        }

        Administrateur administrateur = new Administrateur();
        fillBase(administrateur, hashedPassword);
        administrateur.setDepartement(readValue(createIdentifierField, createIdentifierCombo));
        administrateur.setFonction(readValue(createFieldOneField, createFieldOneCombo));
        administrateur.setTelephone(readValue(createFieldTwoField, createFieldTwoCombo));
        administrateur.setActif("actif".equalsIgnoreCase(defaultIfBlank(readValue(createFieldThreeField, createFieldThreeCombo), "actif")));
        administrateur.setDateNomination(LocalDateTime.now());
        return administrateur;
    }

    private void fillBase(Utilisateur utilisateur, String hashedPassword) {
        utilisateur.setNom(createNomField.getText().trim());
        utilisateur.setPrenom(createPrenomField.getText().trim());
        utilisateur.setEmail(createEmailField.getText().trim());
        utilisateur.setMotDePasse(hashedPassword);
        utilisateur.setDateCreation(LocalDateTime.now());
        utilisateur.setLastLogin(null);
        utilisateur.setResetToken(null);
        utilisateur.setResetTokenExpiresAt(null);
    }

    private void clearCreateForm() {
        enterCreateMode("administrateur");
    }

    private void clearCreateFormFields() {
        createNomField.clear();
        createPrenomField.clear();
        createEmailField.clear();
        createPasswordField.clear();
        createIdentifierField.clear();
        createFieldOneField.clear();
        createFieldTwoField.clear();
        createFieldThreeField.clear();
        createFieldFourField.clear();
        createIdentifierCombo.getSelectionModel().clearSelection();
        createFieldOneCombo.getSelectionModel().clearSelection();
        createFieldTwoCombo.getSelectionModel().clearSelection();
        createFieldThreeCombo.getSelectionModel().clearSelection();
        createFieldFourCombo.getSelectionModel().clearSelection();
        createAreaField.clear();
        createDatePicker.setValue(null);
    }

    private void enterCreateMode(String role) {
        editingUtilisateur = null;
        clearCreateFormFields();
        createRoleCombo.setDisable(false);
        createRoleCombo.setValue(role);
        createSubmitButton.setText("Create User");
        createBackButton.setVisible(false);
        createBackButton.setManaged(false);
        createBackButton.setDisable(true);
        createClearButton.setText("Clear Form");
        createStatusLabel.setText("");
        updateCreateSectionHeader();
        updateCreateFormForRole(createRoleCombo.getValue());
    }

    private void applyFilters() {
        String search = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String roleFilter = roleFilterCombo.getValue();

        filteredRows.setPredicate(row -> {
            boolean matchesRole = roleFilter == null || "All".equalsIgnoreCase(roleFilter) || row.getType().equalsIgnoreCase(roleFilter);
            boolean matchesSearch = search.isEmpty()
                    || String.valueOf(row.getId()).contains(search)
                    || row.getFullName().toLowerCase().contains(search)
                    || row.getEmail().toLowerCase().contains(search)
                    || row.getType().toLowerCase().contains(search)
                    || row.getIdentifier().toLowerCase().contains(search)
                    || row.getStatus().toLowerCase().contains(search)
                    || row.getExtraInfo().toLowerCase().contains(search);
            return matchesRole && matchesSearch;
        });
    }

    private void loadDashboardData() {
        try {
            List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();
            masterRows.clear();
            statRows.clear();
            activityItems.clear();

            long students = 0;
            long teachers = 0;
            long admins = 0;
            long activeCount = 0;

            for (Utilisateur utilisateur : utilisateurs) {
                masterRows.add(new UtilisateurRow(utilisateur));

                if ("etudiant".equalsIgnoreCase(utilisateur.getType())) {
                    students++;
                    if (((Etudiant) utilisateur).isActif()) {
                        activeCount++;
                    }
                } else if ("enseignant".equalsIgnoreCase(utilisateur.getType())) {
                    teachers++;
                    if (((Enseignant) utilisateur).isActif()) {
                        activeCount++;
                    }
                } else if ("administrateur".equalsIgnoreCase(utilisateur.getType())) {
                    admins++;
                    if (((Administrateur) utilisateur).isActif()) {
                        activeCount++;
                    }
                }
            }

            totalUsersLabel.setText(String.valueOf(utilisateurs.size()));
            studentsCountLabel.setText(String.valueOf(students));
            teachersCountLabel.setText(String.valueOf(teachers));
            adminsCountLabel.setText(String.valueOf(admins));

            statRows.addAll(
                    new StatRow("Total users", String.valueOf(utilisateurs.size())),
                    new StatRow("Administrators", String.valueOf(admins)),
                    new StatRow("Teachers", String.valueOf(teachers)),
                    new StatRow("Students", String.valueOf(students)),
                    new StatRow("Active profiles", String.valueOf(activeCount))
            );

            double total = Math.max(utilisateurs.size(), 1);
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("Administrators " + formatPercent(admins * 100.0 / total), admins),
                    new PieChart.Data("Teachers " + formatPercent(teachers * 100.0 / total), teachers),
                    new PieChart.Data("Students " + formatPercent(students * 100.0 / total), students)
            );
            usersPieChart.setData(pieData);
            usersRoleBarChart.setData(FXCollections.observableArrayList(
                    buildRoleSeries("Users", admins, teachers, students)
            ));

            activityItems.add("Live registry loaded from database");
            activityItems.add("Profile actions are available from the user circle");
            activityItems.add("Sidebar switches between users, statistics, and create page");
            activityItems.add("ObservableList keeps the admin workspace reactive");

            applyFilters();
            if (!usersTable.getItems().isEmpty()) {
                usersTable.getSelectionModel().select(0);
            } else {
                updateDetails(null);
            }
        } catch (SQLException e) {
            activityItems.setAll("Failed to load admin dashboard: " + e.getMessage());
            usersRoleBarChart.setData(FXCollections.observableArrayList());
        }
    }

    private void updateDetails(UtilisateurRow row) {
        if (row == null) {
            detailNameLabel.setText("-");
            detailEmailLabel.setText("-");
            detailRoleLabel.setText("-");
            detailIdentifierLabel.setText("-");
            detailStatusLabel.setText("-");
            detailInfoLabel.setText("-");
            detailCreatedLabel.setText("-");
            detailLastLoginLabel.setText("-");
            return;
        }

        detailNameLabel.setText(row.getFullName());
        detailEmailLabel.setText(row.getEmail());
        detailRoleLabel.setText(row.getType());
        detailIdentifierLabel.setText(row.getIdentifier());
        detailStatusLabel.setText(row.getStatus());
        detailInfoLabel.setText(row.getExtraInfo());
        detailCreatedLabel.setText(row.getDateCreation());
        detailLastLoginLabel.setText(row.getLastLogin());
    }

    private void showUserDetailsDialog(UtilisateurRow row) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Details");
        alert.setHeaderText(row.getFullName());
        alert.setContentText(buildDetailedText(row.getUtilisateur()));
        alert.showAndWait();
    }

    private void editUser(UtilisateurRow row) {
        editingUtilisateur = row.getUtilisateur();
        populateCreateFormForEdit(editingUtilisateur);
        createRoleCombo.setDisable(true);
        createSubmitButton.setText("Save Changes");
        createBackButton.setVisible(true);
        createBackButton.setManaged(true);
        createBackButton.setDisable(false);
        createClearButton.setText("Reset Changes");
        createStatusLabel.setText("Editing " + editingUtilisateur.getNomComplet() + ". Leave password empty to keep it unchanged.");
        showCreatePage();
    }

    private void deleteUser(UtilisateurRow row) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete User");
        confirmation.setHeaderText("Delete " + row.getFullName() + "?");
        confirmation.setContentText("This action removes the user from the base table and the role-specific table.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                utilisateurService.deleteUtilisateur((int) row.getId());
                activityItems.setAll("User deleted: " + row.getFullName());
                loadDashboardData();
            } catch (SQLException e) {
                showError("Delete failed", e.getMessage());
            }
        }
    }

    private String buildDetailedText(Utilisateur utilisateur) {
        StringBuilder builder = new StringBuilder();
        builder.append("ID: ").append(utilisateur.getId()).append('\n');
        builder.append("Name: ").append(utilisateur.getNomComplet()).append('\n');
        builder.append("Email: ").append(utilisateur.getEmail()).append('\n');
        builder.append("Role: ").append(utilisateur.getType()).append('\n');
        builder.append("Created: ").append(formatDateTime(utilisateur.getDateCreation())).append('\n');
        builder.append("Last login: ").append(formatDateTime(utilisateur.getLastLogin())).append('\n');

        if (utilisateur instanceof Etudiant) {
            Etudiant etudiant = (Etudiant) utilisateur;
            builder.append("Matricule: ").append(etudiant.getMatricule()).append('\n');
            builder.append("Level: ").append(etudiant.getNiveauEtude()).append('\n');
            builder.append("Specialization: ").append(etudiant.getSpecialisation()).append('\n');
            builder.append("Phone: ").append(etudiant.getTelephone()).append('\n');
            builder.append("Address: ").append(etudiant.getAdresse()).append('\n');
            builder.append("Status: ").append(etudiant.getStatut());
        } else if (utilisateur instanceof Enseignant) {
            Enseignant enseignant = (Enseignant) utilisateur;
            builder.append("Teacher ID: ").append(enseignant.getMatriculeEnseignant()).append('\n');
            builder.append("Diploma: ").append(enseignant.getDiplome()).append('\n');
            builder.append("Specialty: ").append(enseignant.getSpecialite()).append('\n');
            builder.append("Contract: ").append(enseignant.getTypeContrat()).append('\n');
            builder.append("Status: ").append(enseignant.getStatut());
        } else if (utilisateur instanceof Administrateur) {
            Administrateur administrateur = (Administrateur) utilisateur;
            builder.append("Department: ").append(administrateur.getDepartement()).append('\n');
            builder.append("Function: ").append(administrateur.getFonction()).append('\n');
            builder.append("Phone: ").append(administrateur.getTelephone()).append('\n');
            builder.append("Status: ").append(administrateur.isActif() ? "actif" : "inactif");
        }

        return builder.toString();
    }

    private void setSectionVisibility(
            boolean usersVisible,
            boolean statsVisible,
            boolean createVisible,
            boolean modulesVisible,
            boolean courseStatisticsVisible,
            boolean evenementsVisible,
            boolean forumVisible,
            boolean messageVisible
    ) {
        usersSection.setVisible(usersVisible);
        usersSection.setManaged(usersVisible);
        statsSection.setVisible(statsVisible);
        statsSection.setManaged(statsVisible);
        createSection.setVisible(createVisible);
        createSection.setManaged(createVisible);
        if (modulesSection != null) {
            modulesSection.setVisible(modulesVisible);
            modulesSection.setManaged(modulesVisible);
        }
        if (courseStatisticsSection != null) {
            courseStatisticsSection.setVisible(courseStatisticsVisible);
            courseStatisticsSection.setManaged(courseStatisticsVisible);
        }
        if (evenementsSection != null) {
            evenementsSection.setVisible(evenementsVisible);
            evenementsSection.setManaged(evenementsVisible);
        }
        if (forumSection != null) {
            forumSection.setVisible(forumVisible);
            forumSection.setManaged(forumVisible);
        }
        if (messageSection != null) {
            messageSection.setVisible(messageVisible);
            messageSection.setManaged(messageVisible);
        }
    }

    private void loadModulesContent() {
        if (modulesContentPane == null || !modulesContentPane.getChildren().isEmpty()) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/course-management.fxml"));
            Node content = loader.load();
            modulesContentPane.getChildren().setAll(content);
        } catch (IOException e) {
            showError("Modules load failed", e.getMessage());
        }
    }

    private void loadCourseStatisticsContent() {
        if (courseStatisticsContentPane == null) {
            return;
        }
        if (!courseStatisticsContentPane.getChildren().isEmpty()) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-course-statistics.fxml"));
            Node content = loader.load();
            adminCourseStatisticsController = loader.getController();
            courseStatisticsContentPane.getChildren().setAll(content);
        } catch (IOException e) {
            showError("Course statistics load failed", e.getMessage());
        }
    }

    private void loadEvenementsContent() {
        if (evenementsContentPane == null || !evenementsContentPane.getChildren().isEmpty()) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/EventManagement.fxml"));
            Node content = loader.load();
            evenementsContentPane.getChildren().setAll(content);
        } catch (IOException e) {
            showError("Events load failed", e.getMessage());
        }
    }

    private void loadForumContent() {
        if (forumContentPane == null || !forumContentPane.getChildren().isEmpty()) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_forum.fxml")); // ✅ corrigé
            Node content = loader.load();
            forumContentPane.getChildren().setAll(content);
        } catch (IOException e) {
            showError("Forum load failed", e.getMessage());
        }
    }

    private void loadMessageContent() {
        if (messageContentPane == null || !messageContentPane.getChildren().isEmpty()) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_message.fxml")); // ✅ corrigé
            Node content = loader.load();
            messageContentPane.getChildren().setAll(content);
        } catch (IOException e) {
            showError("Messages load failed", e.getMessage());
        }
    }

    private void updateCreateSectionHeader() {
        if (editingUtilisateur == null) {
            sectionTitleLabel.setText("Create User");
            sectionSubtitleLabel.setText("Add a new admin, student, or teacher using typed database-backed input controls.");
            return;
        }

        sectionTitleLabel.setText("Modify User");
        sectionSubtitleLabel.setText("Update the selected user or go back to a blank create form.");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void submitAdminAssistantQuestion(TextArea inputArea, VBox conversationBox, ScrollPane conversationPane, Label statusLabel, Button sendButton) {
        String question = inputArea.getText() == null ? "" : inputArea.getText().trim();
        if (question.isEmpty()) {
            statusLabel.setText("Enter a question first.");
            return;
        }

        appendAdminMessage(conversationBox, question);
        conversationPane.layout();
        conversationPane.setVvalue(1.0);
        inputArea.clear();
        sendButton.setDisable(true);
        statusLabel.setText("Generating reply...");

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return adminAssistantService.answer(question);
            }
        };

        task.setOnSucceeded(event -> {
            appendAssistantMessage(conversationBox, task.getValue());
            conversationPane.layout();
            conversationPane.setVvalue(1.0);
            statusLabel.setText("Reply generated.");
            sendButton.setDisable(false);
        });
        task.setOnFailed(event -> {
            Throwable error = task.getException();
            appendAssistantMessage(
                    conversationBox,
                    "I couldn't generate a reply right now. Check the API key permissions in app-secrets.properties or ask a greeting such as hi or hello for the local fallback."
            );
            conversationPane.layout();
            conversationPane.setVvalue(1.0);
            statusLabel.setText("Assistant error: " + (error != null ? error.getMessage() : "Unknown error"));
            sendButton.setDisable(false);
        });

        Thread worker = new Thread(task);
        worker.setDaemon(true);
        worker.start();
    }

    private void appendAdminMessage(VBox conversationBox, String message) {
        conversationBox.getChildren().add(createAssistantBubble("Admin", message, true));
    }

    private void appendAssistantMessage(VBox conversationBox, String message) {
        conversationBox.getChildren().add(createAssistantBubble("Assistant", message, false));
    }

    private Node createAssistantBubble(String sender, String message, boolean adminMessage) {
        Label senderLabel = new Label(sender);
        senderLabel.getStyleClass().add("assistant-bubble-sender");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Region.USE_PREF_SIZE);
        messageLabel.getStyleClass().add(adminMessage ? "assistant-admin-bubble" : "assistant-ai-bubble");

        VBox bubble = new VBox(5, senderLabel, messageLabel);
        bubble.setMaxWidth(540);
        bubble.setFillWidth(true);

        HBox row = new HBox(bubble);
        row.setAlignment(adminMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        row.getStyleClass().add("assistant-row");
        return row;
    }

    private String formatDateTime(LocalDateTime value) {
        return value != null ? value.format(DATE_TIME_FORMATTER) : "-";
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String buildInitials(Utilisateur utilisateur) {
        String prenom = utilisateur.getPrenom() != null && !utilisateur.getPrenom().isBlank()
                ? utilisateur.getPrenom().substring(0, 1).toUpperCase() : "";
        String nom = utilisateur.getNom() != null && !utilisateur.getNom().isBlank()
                ? utilisateur.getNom().substring(0, 1).toUpperCase() : "";
        return prenom + nom;
    }

    private void populateCreateFormForEdit(Utilisateur utilisateur) {
        createNomField.setText(utilisateur.getNom());
        createPrenomField.setText(utilisateur.getPrenom());
        createEmailField.setText(utilisateur.getEmail());
        createPasswordField.clear();
        createRoleCombo.setValue(utilisateur.getType());
        updateCreateFormForRole(utilisateur.getType());

        if (utilisateur instanceof Etudiant) {
            Etudiant etudiant = (Etudiant) utilisateur;
            writeValue(createIdentifierField, createIdentifierCombo, etudiant.getMatricule());
            writeValue(createFieldOneField, createFieldOneCombo, etudiant.getNiveauEtude());
            writeValue(createFieldTwoField, createFieldTwoCombo, etudiant.getSpecialisation());
            writeValue(createFieldThreeField, createFieldThreeCombo, etudiant.getTelephone());
            writeValue(createFieldFourField, createFieldFourCombo, etudiant.getStatut());
            createDatePicker.setValue(etudiant.getDateNaissance());
            createAreaField.setText(etudiant.getAdresse());
            return;
        }

        if (utilisateur instanceof Enseignant) {
            Enseignant enseignant = (Enseignant) utilisateur;
            writeValue(createIdentifierField, createIdentifierCombo, enseignant.getMatriculeEnseignant());
            writeValue(createFieldOneField, createFieldOneCombo, enseignant.getDiplome());
            writeValue(createFieldTwoField, createFieldTwoCombo, enseignant.getSpecialite());
            writeValue(createFieldThreeField, createFieldThreeCombo, enseignant.getTypeContrat());
            writeValue(createFieldFourField, createFieldFourCombo, enseignant.getStatut());
            createAreaField.setText(enseignant.getDisponibilites());
            return;
        }

        Administrateur administrateur = (Administrateur) utilisateur;
        writeValue(createIdentifierField, createIdentifierCombo, administrateur.getDepartement());
        writeValue(createFieldOneField, createFieldOneCombo, administrateur.getFonction());
        writeValue(createFieldTwoField, createFieldTwoCombo, administrateur.getTelephone());
        writeValue(createFieldThreeField, createFieldThreeCombo, administrateur.isActif() ? "actif" : "inactif");
    }

    private void applyCreateFormToExisting(Utilisateur utilisateur) throws IOException {
        utilisateur.setNom(createNomField.getText().trim());
        utilisateur.setPrenom(createPrenomField.getText().trim());
        utilisateur.setEmail(createEmailField.getText().trim());
        if (!createPasswordField.getText().trim().isEmpty()) {
            utilisateur.setMotDePasse(authService.hashPassword(createPasswordField.getText().trim()));
        }

        if (utilisateur instanceof Etudiant) {
            Etudiant etudiant = (Etudiant) utilisateur;
            etudiant.setMatricule(readValue(createIdentifierField, createIdentifierCombo));
            etudiant.setNiveauEtude(readValue(createFieldOneField, createFieldOneCombo));
            etudiant.setSpecialisation(readValue(createFieldTwoField, createFieldTwoCombo));
            etudiant.setTelephone(readValue(createFieldThreeField, createFieldThreeCombo));
            etudiant.setStatut(defaultIfBlank(readValue(createFieldFourField, createFieldFourCombo), etudiant.getStatut()));
            if (createDatePicker.getValue() != null) {
                etudiant.setDateNaissance(createDatePicker.getValue());
            }
            etudiant.setAdresse(defaultIfBlank(createAreaField.getText(), etudiant.getAdresse()));
            if (etudiant.getDateInscription() == null) {
                etudiant.setDateInscription(LocalDateTime.now());
            }
            return;
        }

        if (utilisateur instanceof Enseignant) {
            Enseignant enseignant = (Enseignant) utilisateur;
            enseignant.setMatriculeEnseignant(readValue(createIdentifierField, createIdentifierCombo));
            enseignant.setDiplome(readValue(createFieldOneField, createFieldOneCombo));
            enseignant.setSpecialite(readValue(createFieldTwoField, createFieldTwoCombo));
            enseignant.setTypeContrat(readValue(createFieldThreeField, createFieldThreeCombo));
            enseignant.setStatut(defaultIfBlank(readValue(createFieldFourField, createFieldFourCombo), enseignant.getStatut()));
            enseignant.setAnneesExperience(enseignant.getAnneesExperience() != null ? enseignant.getAnneesExperience() : 0);
            enseignant.setDisponibilites(defaultIfBlank(createAreaField.getText(), enseignant.getDisponibilites()));
            return;
        }

        Administrateur administrateur = (Administrateur) utilisateur;
        administrateur.setDepartement(readValue(createIdentifierField, createIdentifierCombo));
        administrateur.setFonction(readValue(createFieldOneField, createFieldOneCombo));
        administrateur.setTelephone(readValue(createFieldTwoField, createFieldTwoCombo));
        administrateur.setActif("actif".equalsIgnoreCase(defaultIfBlank(readValue(createFieldThreeField, createFieldThreeCombo), administrateur.isActif() ? "actif" : "inactif")));
        if (administrateur.getDateNomination() == null) {
            administrateur.setDateNomination(LocalDateTime.now());
        }
    }

    private String validateCreateForm(String role) {
        if (role == null || role.isBlank()) {
            return "Please select a role.";
        }
        if (createNomField.getText() == null || createNomField.getText().trim().isEmpty()) {
            return "Name is required.";
        }
        if (createPrenomField.getText() == null || createPrenomField.getText().trim().isEmpty()) {
            return "First name is required.";
        }
        if (!EMAIL_PATTERN.matcher(defaultIfBlank(createEmailField.getText(), "")).matches()) {
            return "Email format is invalid.";
        }
        String password = defaultIfBlank(createPasswordField.getText(), "");
        if (editingUtilisateur == null && !isStrongPassword(password)) {
            return "Password must contain at least 8 characters, one uppercase letter, and one number.";
        }
        if (editingUtilisateur != null && !password.isBlank() && !isStrongPassword(password)) {
            return "Password must contain at least 8 characters, one uppercase letter, and one number.";
        }

        String identifier = readValue(createIdentifierField, createIdentifierCombo);
        String fieldOne = readValue(createFieldOneField, createFieldOneCombo);
        String fieldTwo = readValue(createFieldTwoField, createFieldTwoCombo);
        String fieldThree = readValue(createFieldThreeField, createFieldThreeCombo);
        String fieldFour = readValue(createFieldFourField, createFieldFourCombo);

        if ("administrateur".equals(role)) {
            if (identifier.isBlank() || fieldOne.isBlank()) {
                return "Department and function are required.";
            }
            if (!fieldTwo.isBlank() && !PHONE_PATTERN.matcher(fieldTwo).matches()) {
                return "Admin phone format is invalid.";
            }
            if (!Arrays.asList("actif", "inactif").contains(fieldThree)) {
                return "Please select a valid admin status.";
            }
        } else if ("enseignant".equals(role)) {
            if (identifier.isBlank() || !identifier.startsWith("ENS-")) {
                return "Teacher ID must start with ENS-.";
            }
            if (fieldOne.isBlank() || fieldTwo.isBlank()) {
                return "Diploma and speciality are required.";
            }
            if (fieldThree.isBlank()) {
                return "Contract type is required.";
            }
            if (!Arrays.asList("actif", "inactif", "conge", "retraite").contains(fieldFour)) {
                return "Please select a valid teacher status.";
            }
        } else if ("etudiant".equals(role)) {
            if (identifier.isBlank()) {
                return "Student matricule is required.";
            }
            if (fieldOne.isBlank() || fieldTwo.isBlank()) {
                return "Student level and speciality are required.";
            }
            if (!PHONE_PATTERN.matcher(defaultIfBlank(fieldThree, "")).matches()) {
                return "Student phone format is invalid.";
            }
            if (!Arrays.asList("actif", "inactif", "diplome", "suspendu").contains(fieldFour)) {
                return "Please select a valid student status.";
            }
            if (createDatePicker.getValue() == null || !createDatePicker.getValue().isBefore(LocalDate.now())) {
                return "Birth date must be in the past.";
            }
            if (defaultIfBlank(createAreaField.getText(), "").length() < 5) {
                return "Address is required.";
            }
        }
        return null;
    }

    private void showCombo(ComboBox<String> comboBox, TextField textField, boolean visible) {
        comboBox.setVisible(visible);
        comboBox.setManaged(visible);
        textField.setVisible(!visible);
        textField.setManaged(!visible);
    }

    private String readValue(TextField textField, ComboBox<String> comboBox) {
        if (comboBox.isVisible()) {
            return defaultIfBlank(comboBox.getValue(), "");
        }
        return defaultIfBlank(textField.getText(), "");
    }

    private void writeValue(TextField textField, ComboBox<String> comboBox, String value) {
        if (comboBox.isVisible()) {
            if (value != null && !comboBox.getItems().contains(value)) {
                comboBox.getItems().add(value);
            }
            comboBox.setValue(value);
            return;
        }
        textField.setText(defaultIfBlank(value, ""));
    }

    private void showValidationAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Input");
        alert.setHeaderText("Please correct the form.");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String formatPercent(double value) {
        return String.format("%.1f%%", value);
    }

    private boolean isStrongPassword(String password) {
        return STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }

    private XYChart.Series<String, Number> buildRoleSeries(String seriesName, long admins, long teachers, long students) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        series.getData().add(new XYChart.Data<>("Administrators", admins));
        series.getData().add(new XYChart.Data<>("Teachers", teachers));
        series.getData().add(new XYChart.Data<>("Students", students));
        return series;
    }

    private void configureComboDisplay(ComboBox<String> comboBox) {
        StringConverter<String> converter = new StringConverter<>() {
            @Override
            public String toString(String value) {
                return toEnglishLabel(value);
            }

            @Override
            public String fromString(String value) {
                return value;
            }
        };

        Callback<javafx.scene.control.ListView<String>, ListCell<String>> factory = listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : toEnglishLabel(item));
            }
        };

        comboBox.setConverter(converter);
        comboBox.setCellFactory(factory);
        comboBox.setButtonCell(factory.call(null));
    }

    private String toEnglishLabel(String value) {
        if (value == null) {
            return "";
        }
        return UI_LABELS.getOrDefault(value, value);
    }

    private static Map<String, String> createUiLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("administrateur", "Administrator");
        labels.put("enseignant", "Teacher");
        labels.put("etudiant", "Student");
        labels.put("actif", "Active");
        labels.put("inactif", "Inactive");
        labels.put("conge", "On Leave");
        labels.put("retraite", "Retired");
        labels.put("diplome", "Graduated");
        labels.put("suspendu", "Suspended");
        labels.put("Direction Generale", "General Management");
        labels.put("Scolarite", "Registrar");
        labels.put("Ressources Humaines", "Human Resources");
        labels.put("Finances", "Finance");
        labels.put("Informatique", "Computer Science");
        labels.put("Communication", "Communications");
        labels.put("Recherche", "Research");
        labels.put("Responsable administratif", "Administrative Manager");
        labels.put("Chef de service", "Department Head");
        labels.put("Coordinateur", "Coordinator");
        labels.put("Assistant administratif", "Administrative Assistant");
        labels.put("Directeur", "Director");
        labels.put("Licence 1", "Bachelor Year 1");
        labels.put("Licence 2", "Bachelor Year 2");
        labels.put("Licence 3", "Bachelor Year 3");
        labels.put("Master 1", "Master Year 1");
        labels.put("Master 2", "Master Year 2");
        labels.put("Doctorat", "PhD");
        labels.put("Licence", "Bachelor");
        labels.put("Ingenieur", "Engineering Degree");
        labels.put("Mathematiques", "Mathematics");
        labels.put("Physique", "Physics");
        labels.put("Chimie", "Chemistry");
        labels.put("Biologie", "Biology");
        labels.put("Economie", "Economics");
        labels.put("Langues", "Languages");
        labels.put("Droit", "Law");
        labels.put("Gestion", "Management");
        labels.put("Vacataire", "Part-Time");
        labels.put("Contractuel", "Contract");
        labels.put("Cybersecurite", "Cybersecurity");
        labels.put("Reseaux", "Networks");
        return labels;
    }
}
