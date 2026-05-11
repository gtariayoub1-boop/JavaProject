package gui;
import services.ContenuService;
import entities.Contenu;
import entities.Cours;
import entities.Question;
import entities.Reponse;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import entities.Quiz;
import entities.ResultatQuiz;
import entities.Utilisateur;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import services.CoursService;
import services.PdfService;
import services.QuestionService;
import services.QuizIAService;
import services.QuizService;
import services.ReponseService;
import services.ResultatQuizService;
import utils.QuizNavigation;
import utils.UserSession;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class QuizController implements Initializable {

    // ── Navigation ────────────────────────────────────────────────
    @FXML private BorderPane mainContentPane;

    // ── PAGE 1 : liste quiz ───────────────────────────────────────
    @FXML private ScrollPane quizListSection;
    @FXML private Label      quizListSubtitleLabel;
    @FXML private Label      quizListMessageLabel;
    @FXML private TableView<Quiz>  quizTable;
    @FXML private TableColumn<?,?> quizTitreColumn;
    @FXML private TableColumn<?,?> quizTypeColumn;
    @FXML private TableColumn<?,?> quizDureeColumn;
    @FXML private TableColumn<?,?> quizQuestionsColumn;
    @FXML private TableColumn<?,?> quizDispoColumn;
    @FXML private TableColumn<?,?> quizActionsColumn;
    @FXML private VBox  quizDetailsPanel;
    @FXML private Label detailQuizTitleLabel;
    @FXML private Label detailQuizMetaLabel;
    @FXML private Label detailQuizTypeLabel;
    @FXML private Label detailQuizDureeLabel;
    @FXML private Label detailQuizTentativesLabel;
    @FXML private Label detailQuizCorrectionLabel;
    @FXML private Label detailQuizDispoLabel;
    @FXML private Label detailQuizDescLabel;
    @FXML private Label detailQuizCoursLabel;

    // ── PAGE 2 : formulaire quiz ──────────────────────────────────
    @FXML private BorderPane       quizFormSection;
    @FXML private Label            quizFormTitleLabel;
    @FXML private Label            quizFormStatusLabel;
    @FXML private TextField        quizTitreField;
    @FXML private Label            quizTitreErrorLabel;
    @FXML private ComboBox<String> quizTypeCombo;
    @FXML private Label            quizTypeErrorLabel;
    @FXML private ComboBox<String> quizCorrectionApresCombo;
    @FXML private Label            quizCorrectionErrorLabel;
    @FXML private Spinner<Integer> quizDureeSpinner;
    @FXML private Label            quizDureeErrorLabel;
    @FXML private Spinner<Integer> quizTentativesSpinner;
    @FXML private Label            quizTentativesErrorLabel;
    @FXML private ComboBox<Cours>  quizCoursCombo;
    @FXML private Label            quizCoursErrorLabel;
    @FXML private DatePicker       quizDateDebutPicker;
    @FXML private Label            quizDateDebutErrorLabel;
    @FXML private DatePicker       quizDateFinPicker;
    @FXML private Label            quizDateFinErrorLabel;
    @FXML private Label            quizDateRangeInfoLabel;
    @FXML private TextArea         quizDescriptionArea;
    @FXML private Label            quizDescriptionErrorLabel;
    @FXML private TextArea         quizInstructionsArea;
    @FXML private Label            quizInstructionsErrorLabel;

    // ── PAGE 3 : liste questions ──────────────────────────────────
    @FXML private ScrollPane questionsSection;
    @FXML private Label      questionsSectionTitleLabel;
    @FXML private Label      questionsSectionSubtitleLabel;
    @FXML private Label      questionsMessageLabel;
    @FXML private TableView<Question> questionsTable;
    @FXML private TableColumn<?,?>    qOrdreColumn;
    @FXML private TableColumn<?,?>    qTexteColumn;
    @FXML private TableColumn<?,?>    qTypeColumn;
    @FXML private TableColumn<?,?>    qPointsColumn;
    @FXML private TableColumn<?,?>    qReponsesColumn;
    @FXML private TableColumn<?,?>    qActionsColumn;
    @FXML private VBox  questionDetailsPanel;
    @FXML private Label detailQuestionTexteLabel;
    @FXML private Label detailQuestionMetaLabel;
    @FXML private VBox  detailReponsesContainer;
    @FXML private Label detailQuestionExplicationLabel;

    // ── PAGE 4 : formulaire question ──────────────────────────────
    @FXML private BorderPane       questionFormSection;
    @FXML private Label            questionFormTitleLabel;
    @FXML private Label            questionFormStatusLabel;
    @FXML private TextArea         questionTexteArea;
    @FXML private Label            questionTexteErrorLabel;
    @FXML private ComboBox<String> questionTypeCombo;
    @FXML private Label            questionTypeErrorLabel;
    @FXML private Label            questionTypeHintLabel;
    @FXML private Spinner<Integer> questionPointsSpinner;
    @FXML private Label            questionPointsErrorLabel;
    @FXML private Spinner<Integer> questionOrdreSpinner;
    @FXML private TextArea         questionExplicationArea;
    @FXML private Label            questionExplicationErrorLabel;
    @FXML private VBox        reponsesVraiFauxPanel;
    @FXML private RadioButton vraiFauxVraiRadio;
    @FXML private RadioButton vraiFauxFauxRadio;
    @FXML private Label       vraiFauxErrorLabel;
    @FXML private VBox        reponsesChoixUniquePanel;
    @FXML private VBox        choixUniqueReponsesContainer;
    @FXML private Label       choixUniqueErrorLabel;
    @FXML private VBox        reponsesChoixMultiplePanel;
    @FXML private VBox        choixMultipleReponsesContainer;
    @FXML private Label       choixMultipleErrorLabel;
    @FXML private VBox        reponsesTexteLibrePanel;
    @FXML private TextArea    texteLibreReponseArea;
    @FXML private Label       texteLibreErrorLabel;

    // ── PAGE 5 : réponses ─────────────────────────────────────────
    @FXML private ScrollPane reponsesSection;
    @FXML private Label      reponsesSectionTitleLabel;
    @FXML private Label      reponsesSectionSubtitleLabel;
    @FXML private Label      reponsesMessageLabel;
    @FXML private TableView<Reponse> reponsesTable;
    @FXML private TableColumn<?,?>   rOrdreColumn;
    @FXML private TableColumn<?,?>   rTexteColumn;
    @FXML private TableColumn<?,?>   rCorrecteColumn;
    @FXML private TableColumn<?,?>   rFeedbackColumn;
    @FXML private TableColumn<?,?>   rActionsColumn;
    @FXML private VBox      reponseFormPanel;
    @FXML private Label     reponseFormTitleLabel;
    @FXML private TextArea  reponseTexteArea;
    @FXML private Label     reponseTexteErrorLabel;
    @FXML private CheckBox  reponseCorrecteCheck;
    @FXML private Spinner<Integer> reponseOrdreSpinner;
    @FXML private Label     reponseOrdreErrorLabel;
    @FXML private TextArea  reponseFeedbackArea;
    @FXML private Label     reponseFeedbackErrorLabel;
    @FXML private TextField reponseMediaUrlField;
    @FXML private Label     reponseFormStatusLabel;

    // ── PAGE 6 : résultats ────────────────────────────────────────
    @FXML private ScrollPane resultatsSection;
    @FXML private Label      resultatsSectionTitleLabel;
    @FXML private Label      resultatsSectionSubtitleLabel;
    @FXML private Label      resultatsMessageLabel;
    @FXML private Label      statsMoyenneLabel;
    @FXML private Label      statsMeilleurLabel;
    @FXML private Label      statsPlusBas;
    @FXML private Label      statsTotalLabel;
    @FXML private TableView<Object[]>           resultatsTable;
    @FXML private TableColumn<Object[], String> resNomColumn;
    @FXML private TableColumn<Object[], String> resPrenomColumn;
    @FXML private TableColumn<Object[], String> resMatriculeColumn;
    @FXML private TableColumn<Object[], Double> resScoreColumn;
    @FXML private TableColumn<Object[], String> resPointsColumn;
    @FXML private TableColumn<Object[], String> resDateColumn;
    @FXML private TableColumn<Object[], String> resMentionColumn;

    // ── PAGE 7 : statistiques globales ────────────────────────────
    @FXML private ScrollPane statistiquesSection;
    @FXML private Label      statsGlobalesMessageLabel;
    @FXML private Label kpiTotalQuizLabel;
    @FXML private Label kpiTotalTentativesLabel;
    @FXML private Label kpiMoyenneGeneraleLabel;
    @FXML private Label kpiTauxReussiteLabel;
    @FXML private Label quizPlusDifficileNomLabel;
    @FXML private Label quizPlusDifficileStatLabel;
    @FXML private Label quizPlusDifficileMoyLabel;
    @FXML private Label quizPlusDifficileTauxLabel;
    @FXML private Label quizPlusDifficileTentLabel;
    @FXML private Label quizPlusFacileNomLabel;
    @FXML private Label quizPlusFacileStatLabel;
    @FXML private Label quizPlusFacileMoyLabel;
    @FXML private Label quizPlusFacileTauxLabel;
    @FXML private Label quizPlusFacileTentLabel;
    @FXML private Label quizPlusPopulaireNomLabel;
    @FXML private Label quizPlusPopulaireStatLabel;
    @FXML private Label quizPlusPopulaireTentLabel;
    @FXML private Label quizPlusPopulaireMoyLabel;
    @FXML private TableView<Object[]>           statsTable;
    @FXML private TableColumn<Object[], String> statsTitreColumn;
    @FXML private TableColumn<Object[], String> statsMoyenneColumn;
    @FXML private TableColumn<Object[], String> statsTauxColumn;
    @FXML private TableColumn<Object[], String> statsTentativesColumn;
    @FXML private TableColumn<Object[], String> statsMeilleurColumn;
    @FXML private TableColumn<Object[], String> statsPireColumn;
    @FXML private TableColumn<Object[], String> statsDifficulteColumn;

    // ── État ──────────────────────────────────────────────────────
    private Quiz     selectedQuiz     = null;
    private Quiz     editingQuiz      = null;
    private Quiz     currentQuiz      = null;
    private Question selectedQuestion = null;
    private Question editingQuestion  = null;
    private Reponse  editingReponse   = null;

    private final ToggleGroup choixUniqueGroup = new ToggleGroup();

    private final QuizService         quizService         = new QuizService();
    private final QuestionService     questionService     = new QuestionService();
    private final ReponseService      reponseService      = new ReponseService();
    private final CoursService        coursService        = new CoursService();
    private final ResultatQuizService resultatQuizService = new ResultatQuizService();
    private final PdfService          pdfService          = new PdfService();
    private final QuizIAService       quizIAService       = new QuizIAService();

    private final ObservableList<Quiz>     quizList     = FXCollections.observableArrayList();
    private final ObservableList<Question> questionList = FXCollections.observableArrayList();
    private final ObservableList<Reponse>  reponseList  = FXCollections.observableArrayList();
    private final ObservableList<Object[]> resultatList = FXCollections.observableArrayList();
    private final ObservableList<Object[]> statsList    = FXCollections.observableArrayList();

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ═════════════════════════════════════════════════════════════
    // INIT
    // ═════════════════════════════════════════════════════════════
    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {
        if (quizTypeCombo != null)
            quizTypeCombo.setItems(FXCollections.observableArrayList("QCM","Vrai/Faux","Texte libre","Mixte"));
        if (quizCorrectionApresCombo != null)
            quizCorrectionApresCombo.setItems(FXCollections.observableArrayList("Immédiate","Après soumission","Manuelle"));
        if (quizCoursCombo != null) {
            quizCoursCombo.setConverter(new StringConverter<Cours>() {
                @Override public String toString(Cours c) {
                    if (c == null) return "— Aucun cours —";
                    String code = (c.getCodeCours() != null && !c.getCodeCours().isEmpty()) ? "["+c.getCodeCours()+"] " : "";
                    return code + c.getTitre();
                }
                @Override public Cours fromString(String s) { return null; }
            });
            loadCoursList();
        }
        if (questionTypeCombo != null) {
            questionTypeCombo.setItems(FXCollections.observableArrayList("choix_unique","choix_multiple","vrai_faux","texte_libre"));
            questionTypeCombo.valueProperty().addListener((obs, old, nv) -> onTypeChanged(nv));
        }
        if (quizDateDebutPicker != null) quizDateDebutPicker.valueProperty().addListener((obs, ov, nv) -> updateDateRangeInfo());
        if (quizDateFinPicker   != null) quizDateFinPicker  .valueProperty().addListener((obs, ov, nv) -> updateDateRangeInfo());

        if (quizTable != null) {
            setupColumn(quizTitreColumn, "titre");
            setupColumn(quizTypeColumn,  "typeQuiz");
            setupColumn(quizDureeColumn, "dureeMinutes");
            setupColumn(quizDispoColumn, "dateFinDisponibilite");
            if (quizActionsColumn != null) {
                ((TableColumn<Quiz, Void>) quizActionsColumn).setCellFactory(col -> new TableCell<>() {
                    private final MenuButton menu = new MenuButton("⚙  Actions");
                    {
                        MenuItem mDetail    = new MenuItem("📋  Voir le détail");
                        MenuItem mModifier  = new MenuItem("✏  Modifier");
                        MenuItem mQuestions = new MenuItem("❓  Questions");
                        MenuItem mResultats = new MenuItem("📊  Résultats");
                        MenuItem mSupprimer = new MenuItem("🗑  Supprimer");
                        mSupprimer.setStyle("-fx-text-fill:#ef4444;-fx-font-weight:700;");
                        mDetail   .setOnAction(e -> { Quiz q = getTableView().getItems().get(getIndex()); quizTable.getSelectionModel().select(q); updateQuizDetailPanel(q); });
                        mModifier .setOnAction(e -> startEditQuiz(getTableView().getItems().get(getIndex())));
                        mQuestions.setOnAction(e -> openQuestionsPage(getTableView().getItems().get(getIndex())));
                        mResultats.setOnAction(e -> openResultatsPage(getTableView().getItems().get(getIndex())));
                        mSupprimer.setOnAction(e -> confirmDeleteQuiz(getTableView().getItems().get(getIndex())));
                        menu.getItems().addAll(mDetail, new SeparatorMenuItem(), mModifier, mQuestions, mResultats, new SeparatorMenuItem(), mSupprimer);
                        menu.getStyleClass().add("quiz-actions-menu");
                        menu.setMaxWidth(Double.MAX_VALUE);
                    }
                    @Override protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty); setGraphic(empty ? null : menu); setText(null);
                    }
                });
            }
            quizTable.setItems(quizList);
            quizTable.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> { selectedQuiz = nw; updateQuizDetailPanel(nw); });
        }

        if (questionsTable != null) {
            setupColumn(qOrdreColumn, "ordreAffichage");
            setupColumn(qTexteColumn, "texte");
            setupColumn(qTypeColumn,  "typeQuestion");
            setupColumn(qPointsColumn,"points");
            if (qActionsColumn != null) {
                ((TableColumn<Question, Void>) qActionsColumn).setCellFactory(col -> new TableCell<>() {
                    private final MenuButton menu = new MenuButton("⚙  Actions");
                    {
                        MenuItem mModifier  = new MenuItem("✏  Modifier la question");
                        MenuItem mReponses  = new MenuItem("💬  Gérer les réponses");
                        MenuItem mSupprimer = new MenuItem("🗑  Supprimer la question");
                        mSupprimer.setStyle("-fx-text-fill:#ef4444;-fx-font-weight:700;");
                        mModifier .setOnAction(e -> startEditQuestion(getTableView().getItems().get(getIndex())));
                        mReponses .setOnAction(e -> openReponsesFor(getTableView().getItems().get(getIndex())));
                        mSupprimer.setOnAction(e -> confirmDeleteQuestion(getTableView().getItems().get(getIndex())));
                        menu.getItems().addAll(mModifier, mReponses, new SeparatorMenuItem(), mSupprimer);
                        menu.getStyleClass().add("quiz-actions-menu");
                        menu.setMaxWidth(Double.MAX_VALUE);
                    }
                    @Override protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty); setGraphic(empty ? null : menu); setText(null);
                    }
                });
            }
            questionsTable.setItems(questionList);
            questionsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> { selectedQuestion = nw; updateQuestionDetailPanel(nw); });
        }

        if (reponsesTable != null) {
            setupColumn(rOrdreColumn,   "ordreAffichage");
            setupColumn(rTexteColumn,   "texteReponse");
            setupColumn(rCorrecteColumn,"estCorrecte");
            setupColumn(rFeedbackColumn,"feedbackSpecifique");
            if (rActionsColumn != null) {
                ((TableColumn<Reponse, Void>) rActionsColumn).setCellFactory(col -> new TableCell<>() {
                    private final MenuButton menu = new MenuButton("⚙  Actions");
                    {
                        MenuItem mModifier  = new MenuItem("✏  Modifier la réponse");
                        MenuItem mSupprimer = new MenuItem("🗑  Supprimer la réponse");
                        mSupprimer.setStyle("-fx-text-fill:#ef4444;-fx-font-weight:700;");
                        mModifier .setOnAction(e -> startEditReponse(getTableView().getItems().get(getIndex())));
                        mSupprimer.setOnAction(e -> confirmDeleteReponse(getTableView().getItems().get(getIndex())));
                        menu.getItems().addAll(mModifier, new SeparatorMenuItem(), mSupprimer);
                        menu.getStyleClass().add("quiz-actions-menu");
                        menu.setMaxWidth(Double.MAX_VALUE);
                    }
                    @Override protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty); setGraphic(empty ? null : menu); setText(null);
                    }
                });
            }
            reponsesTable.setItems(reponseList);
        }

        setupResultatsTable();
        setupStatsTable();

        QuizNavigation.TeacherSection init = QuizNavigation.consumeTeacherSection();
        showOnly(quizListSection);
        loadQuizList();
        if (init == QuizNavigation.TeacherSection.CREATE) showQuizCreateForm();
    }

    // ═════════════════════════════════════════════════════════════
    // NAVIGATION
    // ═════════════════════════════════════════════════════════════
    private void showOnly(javafx.scene.layout.Region target) {
        if (mainContentPane == null || target == null) return;
        target.prefHeightProperty().unbind(); target.maxHeightProperty().unbind();
        target.prefWidthProperty().unbind();  target.maxWidthProperty().unbind();
        target.prefHeightProperty().bind(mainContentPane.heightProperty());
        target.maxHeightProperty().bind(mainContentPane.heightProperty());
        target.prefWidthProperty().bind(mainContentPane.widthProperty());
        target.maxWidthProperty().bind(mainContentPane.widthProperty());
        target.setVisible(true); target.setManaged(true);
        mainContentPane.setCenter(target);
    }

    private void loadCoursList() {
        if (quizCoursCombo == null) return;
        try {
            ObservableList<Cours> items = FXCollections.observableArrayList();
            items.add(null); items.addAll(coursService.getAll());
            quizCoursCombo.setItems(items);
        } catch (Exception e) { System.err.println("Cours : " + e.getMessage()); }
    }

    // ═════════════════════════════════════════════════════════════
    // PAGE 1 — LISTE QUIZ
    // ═════════════════════════════════════════════════════════════
    @FXML public void showQuizListPage() {
        currentQuiz = null; showOnly(quizListSection); loadQuizList();
    }

    @FXML private void closeQuizDetails() {
        if (quizDetailsPanel != null) { quizDetailsPanel.setVisible(false); quizDetailsPanel.setManaged(false); }
        selectedQuiz = null;
        if (quizTable != null) quizTable.getSelectionModel().clearSelection();
    }

    @FXML private void editSelectedQuiz() {
        if (selectedQuiz == null) { setStatus(quizListMessageLabel, "Sélectionnez un quiz.", true); return; }
        startEditQuiz(selectedQuiz);
    }

    @FXML private void deleteSelectedQuiz() {
        if (selectedQuiz == null) { setStatus(quizListMessageLabel, "Sélectionnez un quiz.", true); return; }
        confirmDeleteQuiz(selectedQuiz);
    }

    @FXML private void manageQuestionsOfSelectedQuiz() {
        if (selectedQuiz == null) { setStatus(quizListMessageLabel, "Sélectionnez un quiz.", true); return; }
        openQuestionsPage(selectedQuiz);
    }

    private void loadQuizList() {
        if (quizTable == null) return;
        quizList.clear(); setText(quizListMessageLabel, "");
        try {
            Utilisateur u = UserSession.getCurrentUser();
            List<Quiz> list = (u != null) ? quizService.getByCreateur(u.getId().intValue()) : quizService.getAll();
            quizList.setAll(list);
            if (list.isEmpty()) setText(quizListMessageLabel, "Aucun quiz créé.");
        } catch (Exception e) { setStatus(quizListMessageLabel, "Erreur : " + e.getMessage(), true); }
    }

    private void updateQuizDetailPanel(Quiz quiz) {
        if (quizDetailsPanel == null) return;
        boolean v = quiz != null; quizDetailsPanel.setVisible(v); quizDetailsPanel.setManaged(v);
        if (!v) return;
        setText(detailQuizTitleLabel,      quiz.getTitre());
        setText(detailQuizMetaLabel,       safe(quiz.getTypeQuiz()) + " · " + (quiz.getDureeMinutes() != null ? quiz.getDureeMinutes() + " min" : "-"));
        setText(detailQuizTypeLabel,       safe(quiz.getTypeQuiz()));
        setText(detailQuizDureeLabel,      quiz.getDureeMinutes() != null ? quiz.getDureeMinutes() + " min" : "-");
        setText(detailQuizTentativesLabel, quiz.getNombreTentativesAutorisees() != null ? String.valueOf(quiz.getNombreTentativesAutorisees()) : "-");
        setText(detailQuizCorrectionLabel, safe(quiz.getAfficherCorrectionApres()));
        setText(detailQuizDispoLabel,      quiz.getDateFinDisponibilite() != null ? quiz.getDateFinDisponibilite().toLocalDate().toString() : "-");
        setText(detailQuizDescLabel,       safe(quiz.getDescription()));
        if (detailQuizCoursLabel != null && quiz.getIdCours() != null) {
            try {
                Cours c = coursService.getById(quiz.getIdCours());
                String code = (c != null && c.getCodeCours() != null && !c.getCodeCours().isEmpty()) ? "["+c.getCodeCours()+"] " : "";
                setText(detailQuizCoursLabel, c != null ? code + c.getTitre() : "ID " + quiz.getIdCours());
            } catch (Exception e) { setText(detailQuizCoursLabel, "—"); }
        } else setText(detailQuizCoursLabel, "—");
    }

    private void confirmDeleteQuiz(Quiz quiz) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer « " + quiz.getTitre() + " » ?", ButtonType.OK, ButtonType.CANCEL);
        a.setTitle("Confirmation");
        a.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            try {
                quizService.delete(quiz.getId().intValue());
                selectedQuiz = null; updateQuizDetailPanel(null);
                setStatus(quizListMessageLabel, "Quiz supprimé.", false); loadQuizList();
            } catch (Exception e) { setStatus(quizListMessageLabel, "Erreur : " + e.getMessage(), true); }
        });
    }

    // ═════════════════════════════════════════════════════════════
    // PAGE 2 — FORMULAIRE QUIZ
    // ═════════════════════════════════════════════════════════════
    @FXML public void showQuizCreateForm() {
        editingQuiz = null; setText(quizFormTitleLabel, "Nouveau quiz");
        loadCoursList(); clearQuizForm(); showOnly(quizFormSection);
    }

    @FXML private void cancelQuizForm() { editingQuiz = null; showQuizListPage(); }

    @FXML private void saveQuiz() {
        if (!validateQuizForm()) return;
        try {
            boolean isCreation = (editingQuiz == null);

            // ✅ Vérification contenu du cours — uniquement à la création
            if (isCreation) {
                Cours coursSelectionne = quizCoursCombo != null ? quizCoursCombo.getValue() : null;
                if (coursSelectionne != null) {
                    ContenuService contenuService = new ContenuService();
                    List<?> contenuCours = contenuService.getByCoursId(coursSelectionne.getId());
                    if (contenuCours == null || contenuCours.isEmpty()) {
                        Dialog<ButtonType> dialog = new Dialog<>();
                        dialog.setTitle("Cours sans contenu");
                        dialog.setHeaderText(null);

                        DialogPane pane = dialog.getDialogPane();
                        pane.setStyle(
                                "-fx-background-color:#0f172a;" +
                                        "-fx-border-color:rgba(251,191,36,0.5);" +
                                        "-fx-border-width:1.5px;" +
                                        "-fx-border-radius:16px;" +
                                        "-fx-background-radius:16px;"
                        );
                        pane.setPrefWidth(420);

                        VBox content = new VBox(14);
                        content.setPadding(new Insets(24, 28, 10, 28));
                        content.setAlignment(Pos.CENTER_LEFT);

                        // Header
                        HBox header = new HBox(12);
                        header.setAlignment(Pos.CENTER_LEFT);
                        Label icon = new Label("⚠");
                        icon.setStyle("-fx-font-size:32px;");
                        VBox headerText = new VBox(4);
                        Label titre = new Label("Cours sans contenu");
                        titre.setStyle("-fx-font-size:17px;-fx-font-weight:800;-fx-text-fill:#fbbf24;");
                        Label sousTitre = new Label("Impossible de créer le quiz");
                        sousTitre.setStyle("-fx-font-size:12px;-fx-text-fill:#94a3b8;");
                        headerText.getChildren().addAll(titre, sousTitre);
                        header.getChildren().addAll(icon, headerText);

                        // Message
                        VBox msgCard = new VBox(6);
                        msgCard.setStyle(
                                "-fx-background-color:rgba(251,191,36,0.08);" +
                                        "-fx-background-radius:12px;" +
                                        "-fx-border-color:rgba(251,191,36,0.25);" +
                                        "-fx-border-radius:12px;" +
                                        "-fx-padding:14px 16px;"
                        );
                        Label msg1 = new Label("Le cours « " + coursSelectionne.getTitre() + " » n'a aucun contenu pédagogique.");
                        msg1.setWrapText(true);
                        msg1.setStyle("-fx-font-size:13px;-fx-text-fill:#e5e7eb;-fx-font-weight:600;");
                        Label msg2 = new Label("Veuillez d'abord ajouter du contenu à ce cours avant de créer un quiz.");
                        msg2.setWrapText(true);
                        msg2.setStyle("-fx-font-size:12px;-fx-text-fill:#94a3b8;");
                        msgCard.getChildren().addAll(msg1, msg2);

                        content.getChildren().addAll(header, msgCard);
                        pane.setContent(content);

                        ButtonType btnOk = new ButtonType("Compris", ButtonBar.ButtonData.OK_DONE);
                        pane.getButtonTypes().add(btnOk);
                        pane.lookupButton(btnOk).setStyle(
                                "-fx-background-color:linear-gradient(to right,#fbbf24,#f59e0b);" +
                                        "-fx-text-fill:#111827;" +
                                        "-fx-font-weight:800;" +
                                        "-fx-font-size:13px;" +
                                        "-fx-background-radius:12px;" +
                                        "-fx-padding:11px 28px;" +
                                        "-fx-cursor:hand;"
                        );

                        Node bb = pane.lookup(".button-bar");
                        if (bb != null) bb.setStyle("-fx-background-color:rgba(10,13,18,0.95);-fx-padding:14px 28px;");

                        dialog.showAndWait();
                        return;
                    }
                }
            }

            Quiz quiz = isCreation ? new Quiz() : editingQuiz;
            quiz.setTitre(quizTitreField.getText().trim());
            if (quizTypeCombo.getValue() != null)            quiz.setTypeQuiz(quizTypeCombo.getValue());
            if (quizCorrectionApresCombo.getValue() != null) quiz.setAfficherCorrectionApres(quizCorrectionApresCombo.getValue());
            if (quizDureeSpinner.getValue() != null)         quiz.setDureeMinutes(quizDureeSpinner.getValue());
            if (quizTentativesSpinner.getValue() != null)    quiz.setNombreTentativesAutorisees(quizTentativesSpinner.getValue());
            if (quizDateDebutPicker.getValue() != null)      quiz.setDateDebutDisponibilite(quizDateDebutPicker.getValue().atStartOfDay());
            if (quizDateFinPicker.getValue() != null)        quiz.setDateFinDisponibilite(quizDateFinPicker.getValue().atTime(23, 59));
            Cours c = quizCoursCombo != null ? quizCoursCombo.getValue() : null;
            quiz.setIdCours(c != null ? c.getId() : null);
            if (quizInstructionsArea != null) quiz.setInstructions(quizInstructionsArea.getText().trim());
            if (quizDescriptionArea  != null) quiz.setDescription(quizDescriptionArea.getText().trim());
            Utilisateur u = UserSession.getCurrentUser();
            if (u != null && quiz.getCreateur() == null) quiz.setCreateur(u);

            if (isCreation) quizService.create(quiz);
            else            quizService.update(quiz);

            setStatus(quizFormStatusLabel, isCreation ? "Quiz créé ✔" : "Quiz modifié ✔", false);

            editingQuiz = null;
            loadQuizList();

            if (isCreation) {
                selectedQuiz = quiz;
                openQuestionsPage(quiz);
            } else {
                showQuizListPage();
            }

        } catch (Exception e) {
            setStatus(quizFormStatusLabel, "Erreur : " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void startEditQuiz(Quiz quiz) {
        editingQuiz = quiz; setText(quizFormTitleLabel, "Modifier le quiz");
        loadCoursList(); populateQuizForm(quiz); showOnly(quizFormSection);
    }

    private void populateQuizForm(Quiz quiz) {
        clearQuizForm();
        if (quizTitreField           != null) quizTitreField.setText(safe(quiz.getTitre()));
        if (quizTypeCombo            != null) quizTypeCombo.setValue(quiz.getTypeQuiz());
        if (quizCorrectionApresCombo != null) quizCorrectionApresCombo.setValue(quiz.getAfficherCorrectionApres());
        if (quizDureeSpinner         != null && quiz.getDureeMinutes() != null) quizDureeSpinner.getValueFactory().setValue(quiz.getDureeMinutes());
        if (quizTentativesSpinner    != null && quiz.getNombreTentativesAutorisees() != null) quizTentativesSpinner.getValueFactory().setValue(quiz.getNombreTentativesAutorisees());
        if (quizDateDebutPicker      != null && quiz.getDateDebutDisponibilite() != null) quizDateDebutPicker.setValue(quiz.getDateDebutDisponibilite().toLocalDate());
        if (quizDateFinPicker        != null && quiz.getDateFinDisponibilite() != null) quizDateFinPicker.setValue(quiz.getDateFinDisponibilite().toLocalDate());
        if (quizInstructionsArea     != null) quizInstructionsArea.setText(safe(quiz.getInstructions()));
        if (quizDescriptionArea      != null) quizDescriptionArea.setText(safe(quiz.getDescription()));
        if (quizCoursCombo != null && quiz.getIdCours() != null)
            quizCoursCombo.getItems().stream().filter(x -> x != null && x.getId().equals(quiz.getIdCours())).findFirst().ifPresent(quizCoursCombo::setValue);
    }

    private void updateDateRangeInfo() {
        if (quizDateRangeInfoLabel == null) return;
        java.time.LocalDate d = quizDateDebutPicker != null ? quizDateDebutPicker.getValue() : null;
        java.time.LocalDate f = quizDateFinPicker   != null ? quizDateFinPicker.getValue()   : null;
        if (d == null || f == null) { setText(quizDateRangeInfoLabel, "Sélectionnez les deux dates."); quizDateRangeInfoLabel.setStyle("-fx-text-fill:#64748b;"); }
        else if (!f.isAfter(d))    { setText(quizDateRangeInfoLabel, "⚠ La clôture doit être après l'ouverture !"); quizDateRangeInfoLabel.setStyle("-fx-text-fill:#dc2626;-fx-font-weight:700;"); }
        else { long days = java.time.temporal.ChronoUnit.DAYS.between(d, f); setText(quizDateRangeInfoLabel, "✅ Du "+d+" au "+f+" ("+days+" jours)."); quizDateRangeInfoLabel.setStyle("-fx-text-fill:#16a34a;-fx-font-weight:700;"); }
    }

    // ═════════════════════════════════════════════════════════════
    // PAGE 3 — LISTE QUESTIONS
    // ═════════════════════════════════════════════════════════════
    private void openQuestionsPage(Quiz quiz) {
        currentQuiz = quiz; selectedQuestion = null; showOnly(questionsSection);
        setText(questionsSectionTitleLabel,    quiz.getTitre());
        setText(questionsSectionSubtitleLabel, "Ajoutez, modifiez ou supprimez les questions.");
        if (questionDetailsPanel != null) { questionDetailsPanel.setVisible(false); questionDetailsPanel.setManaged(false); }
        loadQuestions();
    }

    @FXML private void closeQuestionDetails() {
        if (questionDetailsPanel != null) { questionDetailsPanel.setVisible(false); questionDetailsPanel.setManaged(false); }
        selectedQuestion = null;
        if (questionsTable != null) questionsTable.getSelectionModel().clearSelection();
    }

    @FXML private void editSelectedQuestion() {
        if (selectedQuestion == null) { setStatus(questionsMessageLabel, "Sélectionnez une question.", true); return; }
        startEditQuestion(selectedQuestion);
    }

    @FXML private void deleteSelectedQuestion() {
        if (selectedQuestion == null) { setStatus(questionsMessageLabel, "Sélectionnez une question.", true); return; }
        confirmDeleteQuestion(selectedQuestion);
    }

    @FXML private void manageReponsesOfSelectedQuestion() {
        if (selectedQuestion == null) { setStatus(questionsMessageLabel, "Sélectionnez une question.", true); return; }
        openReponsesFor(selectedQuestion);
    }

    private void loadQuestions() {
        questionList.clear(); setText(questionsMessageLabel, "");
        if (currentQuiz == null) return;
        try {
            List<Question> list = questionService.getByQuiz(currentQuiz.getId());
            questionList.setAll(list);
            if (list.isEmpty()) setText(questionsMessageLabel, "Aucune question.");
        } catch (Exception e) { setStatus(questionsMessageLabel, "Erreur : " + e.getMessage(), true); }
    }

    private void updateQuestionDetailPanel(Question q) {
        if (questionDetailsPanel == null) return;
        boolean v = q != null; questionDetailsPanel.setVisible(v); questionDetailsPanel.setManaged(v);
        if (!v) return;
        setText(detailQuestionTexteLabel,       q.getTexte());
        setText(detailQuestionMetaLabel,        safe(q.getTypeQuestion()) + " · " + (q.getPoints() != null ? q.getPoints() + " pts" : "-"));
        setText(detailQuestionExplicationLabel, safe(q.getExplicationReponse()));
        if (detailReponsesContainer != null) {
            detailReponsesContainer.getChildren().clear();
            try {
                List<Reponse> rep = reponseService.getByQuestion(q.getId());
                for (Reponse r : rep) {
                    Label lbl = new Label((Boolean.TRUE.equals(r.getEstCorrecte()) ? "✅ " : "❌ ") + safe(r.getTexteReponse()));
                    lbl.setWrapText(true);
                    lbl.getStyleClass().add(Boolean.TRUE.equals(r.getEstCorrecte()) ? "detail-value" : "detail-key");
                    detailReponsesContainer.getChildren().add(lbl);
                }
                if (rep.isEmpty()) { Label e = new Label("Aucune réponse."); e.getStyleClass().add("dashboard-subsection"); detailReponsesContainer.getChildren().add(e); }
            } catch (Exception e) { Label err = new Label("Erreur."); err.getStyleClass().add("field-error-label"); detailReponsesContainer.getChildren().add(err); }
        }
    }

    private void confirmDeleteQuestion(Question q) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette question ?", ButtonType.OK, ButtonType.CANCEL);
        a.setTitle("Confirmation");
        a.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            try {
                questionService.delete(q.getId().intValue());
                if (selectedQuestion != null && selectedQuestion.getId().equals(q.getId())) { selectedQuestion = null; closeQuestionDetails(); }
                setStatus(questionsMessageLabel, "Question supprimée.", false); loadQuestions();
            } catch (Exception e) { setStatus(questionsMessageLabel, "Erreur : " + e.getMessage(), true); }
        });
    }

    // ═════════════════════════════════════════════════════════════
    // PAGE 4 — FORMULAIRE QUESTION
    // ═════════════════════════════════════════════════════════════
    @FXML public void showQuestionCreateForm() {
        if (currentQuiz == null) { setStatus(questionsMessageLabel, "Aucun quiz sélectionné.", true); return; }
        editingQuestion = null; setText(questionFormTitleLabel, "Nouvelle question");
        clearQuestionForm(); showOnly(questionFormSection);
    }

    @FXML private void cancelQuestionForm() {
        editingQuestion = null;
        if (currentQuiz != null) openQuestionsPage(currentQuiz); else showQuizListPage();
    }

    @FXML private void saveQuestion() {
        if (currentQuiz == null) { setStatus(questionFormStatusLabel, "Aucun quiz associé.", true); return; }
        if (!validateQuestionForm()) return;
        try {
            boolean  isNew = (editingQuestion == null);
            Question q     = isNew ? new Question() : editingQuestion;
            q.setTexte(questionTexteArea.getText().trim());
            q.setTypeQuestion(questionTypeCombo.getValue());
            if (questionPointsSpinner.getValue() != null) q.setPoints(questionPointsSpinner.getValue());
            if (questionOrdreSpinner != null && questionOrdreSpinner.getValue() != null) q.setOrdreAffichage(questionOrdreSpinner.getValue());
            if (questionExplicationArea != null) q.setExplicationReponse(questionExplicationArea.getText().trim());
            q.setQuiz(currentQuiz);
            Utilisateur u = UserSession.getCurrentUser();
            if (u != null && q.getCreateur() == null) q.setCreateur(u);
            if (isNew) { questionService.create(q); saveReponsesInline(q); }
            else { questionService.update(q); List<Reponse> old = reponseService.getByQuestion(q.getId()); for (Reponse r : old) reponseService.delete(r.getId().intValue()); saveReponsesInline(q); }
            editingQuestion = null; openQuestionsPage(currentQuiz);
        } catch (Exception e) { setStatus(questionFormStatusLabel, "Erreur : " + e.getMessage(), true); e.printStackTrace(); }
    }

    private void startEditQuestion(Question q) {
        editingQuestion = q; setText(questionFormTitleLabel, "Modifier la question");
        populateQuestionForm(q); showOnly(questionFormSection);
    }

    private void onTypeChanged(String type) {
        hidePanel(reponsesVraiFauxPanel); hidePanel(reponsesChoixUniquePanel); hidePanel(reponsesChoixMultiplePanel); hidePanel(reponsesTexteLibrePanel);
        if (type == null) { setText(questionTypeHintLabel, ""); return; }
        switch (type) {
            case "vrai_faux"      -> { showPanel(reponsesVraiFauxPanel);     setText(questionTypeHintLabel, "ℹ Sélectionnez la bonne réponse."); }
            case "choix_unique"   -> { showPanel(reponsesChoixUniquePanel);   if (choixUniqueReponsesContainer != null && choixUniqueReponsesContainer.getChildren().isEmpty()) { addChoixUniqueRow(); addChoixUniqueRow(); } setText(questionTypeHintLabel, "ℹ Sélectionnez la réponse correcte (●)."); }
            case "choix_multiple" -> { showPanel(reponsesChoixMultiplePanel); if (choixMultipleReponsesContainer != null && choixMultipleReponsesContainer.getChildren().isEmpty()) { addChoixMultipleRow(); addChoixMultipleRow(); } setText(questionTypeHintLabel, "ℹ Cochez les réponses correctes (☑)."); }
            case "texte_libre"    -> { showPanel(reponsesTexteLibrePanel);    setText(questionTypeHintLabel, "ℹ Saisissez la réponse de référence."); }
            default               -> setText(questionTypeHintLabel, "");
        }
    }

    @FXML private void addChoixUniqueRow() {
        if (choixUniqueReponsesContainer == null) return;
        int idx = choixUniqueReponsesContainer.getChildren().size() + 1;
        RadioButton radio = new RadioButton(); radio.setToggleGroup(choixUniqueGroup);
        TextField field = new TextField(); field.setPromptText("Réponse " + idx + "…"); field.getStyleClass().add("input-field");
        HBox.setHgrow(field, Priority.ALWAYS);
        Button del = new Button("🗑"); del.getStyleClass().addAll("danger-button","compact-button");
        HBox row = new HBox(10, radio, field, del); row.setAlignment(Pos.CENTER_LEFT);
        del.setOnAction(e -> choixUniqueReponsesContainer.getChildren().remove(row));
        choixUniqueReponsesContainer.getChildren().add(row);
    }

    @FXML private void addChoixMultipleRow() {
        if (choixMultipleReponsesContainer == null) return;
        int idx = choixMultipleReponsesContainer.getChildren().size() + 1;
        CheckBox check = new CheckBox();
        TextField field = new TextField(); field.setPromptText("Réponse " + idx + "…"); field.getStyleClass().add("input-field");
        HBox.setHgrow(field, Priority.ALWAYS);
        Button del = new Button("🗑"); del.getStyleClass().addAll("danger-button","compact-button");
        HBox row = new HBox(10, check, field, del); row.setAlignment(Pos.CENTER_LEFT);
        del.setOnAction(e -> choixMultipleReponsesContainer.getChildren().remove(row));
        choixMultipleReponsesContainer.getChildren().add(row);
    }

    private void saveReponsesInline(Question question) throws Exception {
        String type = question.getTypeQuestion(); if (type == null) return;
        switch (type) {
            case "vrai_faux" -> {
                boolean vraiOk = vraiFauxVraiRadio != null && vraiFauxVraiRadio.isSelected();
                Reponse v = new Reponse(); v.setTexteReponse("Vrai"); v.setEstCorrecte(vraiOk);  v.setOrdreAffichage(1); v.setQuestion(question); reponseService.create(v);
                Reponse f = new Reponse(); f.setTexteReponse("Faux"); f.setEstCorrecte(!vraiOk); f.setOrdreAffichage(2); f.setQuestion(question); reponseService.create(f);
            }
            case "choix_unique" -> {
                if (choixUniqueReponsesContainer == null) return; int ord = 1;
                for (var node : choixUniqueReponsesContainer.getChildren()) {
                    if (!(node instanceof HBox row)) continue;
                    RadioButton rb = (RadioButton) row.getChildren().get(0); TextField tf = (TextField) row.getChildren().get(1);
                    String txt = tf.getText().trim(); if (txt.isEmpty()) continue;
                    Reponse r = new Reponse(); r.setTexteReponse(txt); r.setEstCorrecte(rb.isSelected()); r.setOrdreAffichage(ord++); r.setQuestion(question); reponseService.create(r);
                }
            }
            case "choix_multiple" -> {
                if (choixMultipleReponsesContainer == null) return; int ord = 1;
                for (var node : choixMultipleReponsesContainer.getChildren()) {
                    if (!(node instanceof HBox row)) continue;
                    CheckBox cb = (CheckBox) row.getChildren().get(0); TextField tf = (TextField) row.getChildren().get(1);
                    String txt = tf.getText().trim(); if (txt.isEmpty()) continue;
                    Reponse r = new Reponse(); r.setTexteReponse(txt); r.setEstCorrecte(cb.isSelected()); r.setOrdreAffichage(ord++); r.setQuestion(question); reponseService.create(r);
                }
            }
            case "texte_libre" -> {
                String txt = texteLibreReponseArea != null ? texteLibreReponseArea.getText().trim() : "";
                if (!txt.isEmpty()) { Reponse r = new Reponse(); r.setTexteReponse(txt); r.setEstCorrecte(true); r.setOrdreAffichage(1); r.setQuestion(question); reponseService.create(r); }
            }
        }
    }

    private void populateQuestionForm(Question q) {
        clearQuestionForm();
        if (questionTexteArea       != null) questionTexteArea.setText(safe(q.getTexte()));
        if (questionTypeCombo       != null) questionTypeCombo.setValue(q.getTypeQuestion());
        if (questionPointsSpinner   != null && q.getPoints() != null) questionPointsSpinner.getValueFactory().setValue(q.getPoints());
        if (questionOrdreSpinner    != null && q.getOrdreAffichage() != null) questionOrdreSpinner.getValueFactory().setValue(q.getOrdreAffichage());
        if (questionExplicationArea != null) questionExplicationArea.setText(safe(q.getExplicationReponse()));
        try {
            List<Reponse> rep = reponseService.getByQuestion(q.getId());
            String type = q.getTypeQuestion(); if (type == null) return;
            switch (type) {
                case "vrai_faux" -> { for (Reponse r : rep) { if ("Vrai".equalsIgnoreCase(r.getTexteReponse()) && Boolean.TRUE.equals(r.getEstCorrecte()) && vraiFauxVraiRadio != null) vraiFauxVraiRadio.setSelected(true); if ("Faux".equalsIgnoreCase(r.getTexteReponse()) && Boolean.TRUE.equals(r.getEstCorrecte()) && vraiFauxFauxRadio != null) vraiFauxFauxRadio.setSelected(true); } }
                case "choix_unique" -> { if (choixUniqueReponsesContainer != null) choixUniqueReponsesContainer.getChildren().clear(); choixUniqueGroup.getToggles().clear(); for (Reponse r : rep) { addChoixUniqueRow(); HBox row = (HBox) choixUniqueReponsesContainer.getChildren().get(choixUniqueReponsesContainer.getChildren().size()-1); ((RadioButton)row.getChildren().get(0)).setSelected(Boolean.TRUE.equals(r.getEstCorrecte())); ((TextField)row.getChildren().get(1)).setText(safe(r.getTexteReponse())); } }
                case "choix_multiple" -> { if (choixMultipleReponsesContainer != null) choixMultipleReponsesContainer.getChildren().clear(); for (Reponse r : rep) { addChoixMultipleRow(); HBox row = (HBox) choixMultipleReponsesContainer.getChildren().get(choixMultipleReponsesContainer.getChildren().size()-1); ((CheckBox)row.getChildren().get(0)).setSelected(Boolean.TRUE.equals(r.getEstCorrecte())); ((TextField)row.getChildren().get(1)).setText(safe(r.getTexteReponse())); } }
                case "texte_libre" -> { if (!rep.isEmpty() && texteLibreReponseArea != null) texteLibreReponseArea.setText(safe(rep.get(0).getTexteReponse())); }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ═════════════════════════════════════════════════════════════
    // IA
    // ═════════════════════════════════════════════════════════════
    @FXML private void genererQuizIA() {
        if (currentQuiz == null) return;
        Integer idCours = currentQuiz.getIdCours();
        if (idCours == null) { setStatus(questionsMessageLabel, "❌ Ce quiz n'a pas de cours associé.", true); return; }
        IADialogResult config = showDialogIA();
        if (config == null) return;
        setStatus(questionsMessageLabel, "🤖 Génération IA en cours (" + config.nbQuestions + " questions)...", false);
        new Thread(() -> {
            List<QuizIAService.QuestionGeneree> questions = quizIAService.genererDepuisCours(idCours, config.nbQuestions, config.typeQuestion);
            javafx.application.Platform.runLater(() -> {
                if (questions.isEmpty()) { setStatus(questionsMessageLabel, "❌ Erreur lors de la génération IA.", true); return; }
                showResultatIA(questions);
            });
        }).start();
    }

    private IADialogResult showDialogIA() {
        Dialog<IADialogResult> dialog = new Dialog<>();
        dialog.setTitle("Génération par Intelligence Artificielle");
        dialog.setHeaderText(null);
        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color:linear-gradient(to bottom right,#0d1117,#131923);-fx-border-color:rgba(45,212,191,0.35);-fx-border-width:1.5px;-fx-border-radius:20px;-fx-background-radius:20px;");
        pane.setPrefWidth(500);
        VBox content = new VBox(20); content.setPadding(new Insets(28,32,10,32));
        HBox header = new HBox(14); header.setAlignment(Pos.CENTER_LEFT);
        Label iconIA = new Label("🤖"); iconIA.setStyle("-fx-font-size:38px;");
        VBox headerText = new VBox(4);
        Label titre = new Label("Générer des questions avec l'IA"); titre.setStyle("-fx-font-size:18px;-fx-font-weight:800;-fx-text-fill:#ffffff;");
        Label sousTitre = new Label("L'IA analysera le contenu du cours associé au quiz"); sousTitre.setStyle("-fx-font-size:12px;-fx-text-fill:#7dd3c8;");
        headerText.getChildren().addAll(titre, sousTitre); header.getChildren().addAll(iconIA, headerText);
        Separator sep = new Separator(); sep.setStyle("-fx-background-color:rgba(45,212,191,0.25);");
        VBox nbQBox = new VBox(8);
        HBox nbQHeader = new HBox(); nbQHeader.setAlignment(Pos.CENTER_LEFT);
        Label nbQLabel = new Label("Nombre de questions"); nbQLabel.setStyle("-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:#aab4c3;");
        HBox sp1 = new HBox(); HBox.setHgrow(sp1, Priority.ALWAYS);
        Label nbQValue = new Label("5"); nbQValue.setStyle("-fx-font-size:22px;-fx-font-weight:900;-fx-text-fill:#2dd4bf;-fx-background-color:rgba(45,212,191,0.12);-fx-background-radius:8px;-fx-padding:2px 14px;");
        nbQHeader.getChildren().addAll(nbQLabel, sp1, nbQValue);
        Slider slider = new Slider(1,20,5); slider.setMajorTickUnit(1); slider.setMinorTickCount(0); slider.setSnapToTicks(true); slider.setStyle("-fx-accent:#2dd4bf;");
        slider.valueProperty().addListener((obs,ov,nv) -> nbQValue.setText(String.valueOf(nv.intValue())));
        HBox sliderLabels = new HBox(); Label minL = new Label("1 question"); minL.setStyle("-fx-font-size:11px;-fx-text-fill:#64748b;"); HBox sp2 = new HBox(); HBox.setHgrow(sp2, Priority.ALWAYS); Label maxL = new Label("20 questions"); maxL.setStyle("-fx-font-size:11px;-fx-text-fill:#64748b;"); sliderLabels.getChildren().addAll(minL,sp2,maxL);
        nbQBox.getChildren().addAll(nbQHeader, slider, sliderLabels);
        VBox typeBox = new VBox(10); Label typeLabel = new Label("Type de questions"); typeLabel.setStyle("-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:#aab4c3;");
        HBox chips = new HBox(8); chips.setAlignment(Pos.CENTER_LEFT);
        ToggleGroup tg = new ToggleGroup();
        String[][] types = {{"choix_unique","☑  Choix unique"},{"choix_multiple","☑☑ Choix multiple"},{"vrai_faux","✓✗  Vrai / Faux"},{"texte_libre","✏  Texte libre"}};
        String sN="-fx-background-color:rgba(255,255,255,0.05);-fx-text-fill:#94a3b8;-fx-font-size:12px;-fx-font-weight:600;-fx-background-radius:999px;-fx-border-color:rgba(255,255,255,0.10);-fx-border-radius:999px;-fx-padding:8px 16px;-fx-cursor:hand;";
        String sS="-fx-background-color:rgba(45,212,191,0.20);-fx-text-fill:#2dd4bf;-fx-font-size:12px;-fx-font-weight:700;-fx-background-radius:999px;-fx-border-color:#2dd4bf;-fx-border-radius:999px;-fx-padding:8px 16px;-fx-cursor:hand;";
        for (String[] t : types) { ToggleButton btn = new ToggleButton(t[1]); btn.setUserData(t[0]); btn.setToggleGroup(tg); btn.setStyle(sN); btn.selectedProperty().addListener((obs,ov,nv)->btn.setStyle(nv?sS:sN)); chips.getChildren().add(btn); if(t[0].equals("choix_unique"))btn.setSelected(true); }
        typeBox.getChildren().addAll(typeLabel, chips);
        HBox infoCard = new HBox(10); infoCard.setAlignment(Pos.CENTER_LEFT); infoCard.setStyle("-fx-background-color:rgba(45,212,191,0.08);-fx-background-radius:12px;-fx-border-color:rgba(45,212,191,0.20);-fx-border-radius:12px;-fx-padding:12px 16px;");
        Label infoIcon = new Label("💡"); infoIcon.setStyle("-fx-font-size:16px;"); Label infoText = new Label("L'IA analysera la description et les contenus pédagogiques du cours pour générer des questions pertinentes."); infoText.setStyle("-fx-font-size:11px;-fx-text-fill:#7dd3c8;"); infoText.setWrapText(true); HBox.setHgrow(infoText, Priority.ALWAYS); infoCard.getChildren().addAll(infoIcon, infoText);
        content.getChildren().addAll(header, sep, nbQBox, typeBox, infoCard); pane.setContent(content);
        ButtonType btnGenerer = new ButtonType("🚀  Générer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAnnuler = new ButtonType("Annuler",     ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(btnGenerer, btnAnnuler);
        pane.lookupButton(btnGenerer).setStyle("-fx-background-color:linear-gradient(to right,#2dd4bf,#0f766e);-fx-text-fill:#effffd;-fx-font-weight:800;-fx-font-size:13px;-fx-background-radius:12px;-fx-padding:11px 24px;-fx-cursor:hand;");
        pane.lookupButton(btnAnnuler).setStyle("-fx-background-color:#1e293b;-fx-text-fill:#94a3b8;-fx-font-weight:700;-fx-background-radius:12px;-fx-border-color:#334155;-fx-padding:11px 20px;");
        Node bb = pane.lookup(".button-bar"); if (bb != null) bb.setStyle("-fx-background-color:rgba(10,13,18,0.95);-fx-padding:16px 32px;");
        dialog.setResultConverter(bt -> { if (bt == btnGenerer) { int nb=(int)slider.getValue(); String type=tg.getSelectedToggle()!=null?(String)tg.getSelectedToggle().getUserData():"choix_unique"; return new IADialogResult(nb,type); } return null; });
        return dialog.showAndWait().orElse(null);
    }

    private void showResultatIA(List<QuizIAService.QuestionGeneree> questions) {
        Dialog<ButtonType> dialog = new Dialog<>(); dialog.setTitle("Questions générées par IA"); dialog.setHeaderText(null);
        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color:linear-gradient(to bottom right,#0d1117,#131923);-fx-border-color:rgba(45,212,191,0.35);-fx-border-width:1.5px;-fx-border-radius:20px;-fx-background-radius:20px;");
        pane.setPrefWidth(580); pane.setPrefHeight(540);
        VBox content = new VBox(16); content.setPadding(new Insets(24,28,10,28));
        HBox header = new HBox(12); header.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label("✨"); icon.setStyle("-fx-font-size:32px;");
        VBox ht = new VBox(3);
        Label t1 = new Label(questions.size()+" questions générées avec succès !"); t1.setStyle("-fx-font-size:17px;-fx-font-weight:800;-fx-text-fill:#ffffff;");
        Label t2 = new Label("Vérifiez les questions avant de les sauvegarder"); t2.setStyle("-fx-font-size:12px;-fx-text-fill:#7dd3c8;");
        ht.getChildren().addAll(t1,t2); header.getChildren().addAll(icon,ht);
        ScrollPane scroll = new ScrollPane(); scroll.setFitToWidth(true); scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;-fx-border-color:transparent;"); scroll.setPrefHeight(380);
        VBox listeQ = new VBox(10); listeQ.setPadding(new Insets(4,4,4,0));
        for (int i=0;i<questions.size();i++) {
            QuizIAService.QuestionGeneree q = questions.get(i);
            VBox card = new VBox(6); card.setStyle("-fx-background-color:rgba(255,255,255,0.04);-fx-background-radius:14px;-fx-border-color:rgba(255,255,255,0.08);-fx-border-radius:14px;-fx-padding:12px 14px;");
            HBox qh = new HBox(8); qh.setAlignment(Pos.CENTER_LEFT);
            Label num = new Label(String.valueOf(i+1)); num.setStyle("-fx-background-color:rgba(45,212,191,0.18);-fx-text-fill:#2dd4bf;-fx-font-weight:800;-fx-font-size:12px;-fx-background-radius:999px;-fx-padding:2px 8px;");
            Label tc = new Label(q.getType()!=null?q.getType().replace("_"," "):""); tc.setStyle("-fx-background-color:rgba(255,255,255,0.06);-fx-text-fill:#94a3b8;-fx-font-size:10px;-fx-font-weight:700;-fx-background-radius:999px;-fx-padding:2px 8px;");
            qh.getChildren().addAll(num,tc);
            Label texte = new Label(q.getTexte()!=null?q.getTexte():""); texte.setStyle("-fx-font-size:13px;-fx-text-fill:#e5e7eb;-fx-font-weight:600;"); texte.setWrapText(true);
            card.getChildren().addAll(qh,texte);
            if (q.getBonneReponse()!=null&&!q.getBonneReponse().isBlank()) { Label rep=new Label("✅ "+q.getBonneReponse()); rep.setStyle("-fx-font-size:11px;-fx-text-fill:#86efac;"); rep.setWrapText(true); card.getChildren().add(rep); }
            listeQ.getChildren().add(card);
        }
        scroll.setContent(listeQ); content.getChildren().addAll(header,scroll); pane.setContent(content);
        ButtonType btnSave = new ButtonType("💾  Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(btnSave,btnCancel);
        pane.lookupButton(btnSave).setStyle("-fx-background-color:linear-gradient(to right,#2dd4bf,#0f766e);-fx-text-fill:#effffd;-fx-font-weight:800;-fx-font-size:13px;-fx-background-radius:12px;-fx-padding:11px 24px;-fx-cursor:hand;");
        pane.lookupButton(btnCancel).setStyle("-fx-background-color:#1e293b;-fx-text-fill:#94a3b8;-fx-font-weight:700;-fx-background-radius:12px;-fx-border-color:#334155;-fx-padding:11px 20px;");
        Node bb = pane.lookup(".button-bar"); if(bb!=null) bb.setStyle("-fx-background-color:rgba(10,13,18,0.95);-fx-padding:16px 28px;");
        dialog.showAndWait().ifPresent(bt -> { if(bt==btnSave){sauvegarderQuestionsIA(questions);loadQuestions();setStatus(questionsMessageLabel,"✅ "+questions.size()+" questions sauvegardées !",false);}else setStatus(questionsMessageLabel,"Génération annulée.",false); });
    }

    private static class IADialogResult { final int nbQuestions; final String typeQuestion; IADialogResult(int nb,String type){this.nbQuestions=nb;this.typeQuestion=type;} }

    private void sauvegarderQuestionsIA(List<QuizIAService.QuestionGeneree> questions) {
        try {
            int ord = questionList.size()+1;
            for (QuizIAService.QuestionGeneree qg : questions) {
                Question q = new Question(); q.setTexte(qg.getTexte()); q.setTypeQuestion(qg.getType()!=null?qg.getType():"choix_unique"); q.setPoints(1); q.setOrdreAffichage(ord++); q.setExplicationReponse(qg.getExplication()); q.setQuiz(currentQuiz); q.setCreateur(UserSession.getCurrentUser()); questionService.create(q);
                if (qg.getChoix()!=null) { int ro=1; for(String choix:qg.getChoix()){Reponse r=new Reponse();r.setTexteReponse(choix);r.setEstCorrecte(choix.equals(qg.getBonneReponse()));r.setOrdreAffichage(ro++);r.setQuestion(q);reponseService.create(r);} }
            }
        } catch (Exception e) { setStatus(questionsMessageLabel,"Erreur sauvegarde : "+e.getMessage(),true); }
    }

    // ═════════════════════════════════════════════════════════════
    // PAGE 5 — RÉPONSES
    // ═════════════════════════════════════════════════════════════
    private void openReponsesFor(Question q) {
        selectedQuestion = q; showOnly(reponsesSection);
        setText(reponsesSectionTitleLabel,    q.getTexte());
        setText(reponsesSectionSubtitleLabel, "Type : " + safe(q.getTypeQuestion()) + " · " + (q.getPoints() != null ? q.getPoints() + " pts" : "-"));
        hidePanel(reponseFormPanel);
        boolean isTextLibre = "texte_libre".equals(q.getTypeQuestion());
        if (reponseCorrecteCheck != null) { reponseCorrecteCheck.setVisible(!isTextLibre); reponseCorrecteCheck.setManaged(!isTextLibre); }
        loadReponses(q.getId());
    }

    @FXML private void backToQuestionsFromReponses() { if (currentQuiz!=null) openQuestionsPage(currentQuiz); else showQuizListPage(); }
    @FXML private void showReponseCreateForm() { editingReponse=null; setText(reponseFormTitleLabel,"Nouvelle réponse"); clearReponseForm(); showPanel(reponseFormPanel); }
    @FXML private void closeReponseForm() { editingReponse=null; hidePanel(reponseFormPanel); }

    @FXML private void saveReponse() {
        if (!validateReponseForm()) return;
        try {
            Reponse r = (editingReponse!=null)?editingReponse:new Reponse();
            r.setTexteReponse(reponseTexteArea.getText().trim());
            r.setEstCorrecte(reponseCorrecteCheck!=null&&reponseCorrecteCheck.isSelected());
            if (reponseOrdreSpinner!=null&&reponseOrdreSpinner.getValue()!=null) r.setOrdreAffichage(reponseOrdreSpinner.getValue());
            if (reponseFeedbackArea!=null) r.setFeedbackSpecifique(reponseFeedbackArea.getText().trim());
            if (reponseMediaUrlField!=null) r.setMediaUrl(reponseMediaUrlField.getText().trim());
            r.setQuestion(selectedQuestion);
            if (editingReponse==null) reponseService.create(r); else reponseService.update(r);
            setStatus(reponseFormStatusLabel,editingReponse==null?"Réponse créée ✔":"Réponse modifiée ✔",false);
            editingReponse=null; hidePanel(reponseFormPanel); loadReponses(selectedQuestion.getId());
        } catch (Exception e) { setStatus(reponseFormStatusLabel,"Erreur : "+e.getMessage(),true); }
    }

    private void startEditReponse(Reponse r) { editingReponse=r; setText(reponseFormTitleLabel,"Modifier la réponse"); populateReponseForm(r); showPanel(reponseFormPanel); }

    private void confirmDeleteReponse(Reponse r) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,"Supprimer cette réponse ?",ButtonType.OK,ButtonType.CANCEL); a.setTitle("Confirmation");
        a.showAndWait().ifPresent(bt -> { if(bt!=ButtonType.OK)return; try{reponseService.delete(r.getId().intValue());setStatus(reponsesMessageLabel,"Réponse supprimée.",false);loadReponses(selectedQuestion.getId());}catch(Exception e){setStatus(reponsesMessageLabel,"Erreur : "+e.getMessage(),true);} });
    }

    private void loadReponses(long questionId) {
        reponseList.clear(); setText(reponsesMessageLabel,"");
        try { List<Reponse> list=reponseService.getByQuestion(questionId); reponseList.setAll(list); if(list.isEmpty())setText(reponsesMessageLabel,"Aucune réponse."); }
        catch (Exception e) { setStatus(reponsesMessageLabel,"Erreur : "+e.getMessage(),true); }
    }

    private void populateReponseForm(Reponse r) {
        clearReponseForm();
        if (reponseTexteArea!=null) reponseTexteArea.setText(safe(r.getTexteReponse()));
        if (reponseCorrecteCheck!=null) reponseCorrecteCheck.setSelected(Boolean.TRUE.equals(r.getEstCorrecte()));
        if (reponseOrdreSpinner!=null&&r.getOrdreAffichage()!=null) reponseOrdreSpinner.getValueFactory().setValue(r.getOrdreAffichage());
        if (reponseFeedbackArea!=null) reponseFeedbackArea.setText(safe(r.getFeedbackSpecifique()));
        if (reponseMediaUrlField!=null) reponseMediaUrlField.setText(safe(r.getMediaUrl()));
    }

    // ═════════════════════════════════════════════════════════════
    // PAGE 6 — RÉSULTATS
    // ═════════════════════════════════════════════════════════════
    private void openResultatsPage(Quiz quiz) {
        currentQuiz=quiz; showOnly(resultatsSection);
        setText(resultatsSectionTitleLabel,    quiz.getTitre());
        setText(resultatsSectionSubtitleLabel, "Durée : "+(quiz.getDureeMinutes()!=null?quiz.getDureeMinutes()+" min":"—")+" · Tentatives max : "+(quiz.getNombreTentativesAutorisees()!=null?quiz.getNombreTentativesAutorisees():"—"));
        loadResultats(quiz);
    }

    @FXML private void backToQuizListFromResultats() { currentQuiz=null; showQuizListPage(); }

    @FXML private void exporterResultatsPDF() {
        if (currentQuiz==null){setStatus(resultatsMessageLabel,"Aucun quiz.",true);return;}
        if (resultatList.isEmpty()){setStatus(resultatsMessageLabel,"Aucun résultat.",true);return;}
        FileChooser fc=new FileChooser(); fc.setTitle("Enregistrer le PDF"); fc.setInitialFileName("resultats_"+currentQuiz.getTitre()+".pdf"); fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF","*.pdf"));
        File fichier=fc.showSaveDialog(null); if(fichier==null)return;
        try {
            List<ResultatQuiz> resultats=resultatList.stream().map(row->(ResultatQuiz)row[0]).collect(Collectors.toList());
            List<entities.Etudiant> etudiants=resultatList.stream().map(row->{entities.Etudiant e=new entities.Etudiant();if(row[1]!=null)e.setNom((String)row[1]);if(row[2]!=null)e.setPrenom((String)row[2]);if(row[3]!=null)e.setMatricule((String)row[3]);e.setId((long)((ResultatQuiz)row[0]).getIdEtudiant());return e;}).collect(Collectors.toList());
            pdfService.exporterResultats(resultats,currentQuiz,etudiants,fichier.getAbsolutePath());
            setStatus(resultatsMessageLabel,"PDF exporté !",false);
        } catch(Exception e){setStatus(resultatsMessageLabel,"Erreur : "+e.getMessage(),true);}
    }

    private void setupResultatsTable() {
        if (resultatsTable==null) return;
        resNomColumn      .setCellValueFactory(cd->new SimpleStringProperty(cd.getValue()[1]!=null?(String)cd.getValue()[1]:"—"));
        resPrenomColumn   .setCellValueFactory(cd->new SimpleStringProperty(cd.getValue()[2]!=null?(String)cd.getValue()[2]:"—"));
        resMatriculeColumn.setCellValueFactory(cd->new SimpleStringProperty(cd.getValue()[3]!=null?(String)cd.getValue()[3]:"—"));
        resScoreColumn.setCellValueFactory(cd->new SimpleObjectProperty<>(((ResultatQuiz)cd.getValue()[0]).getScore()));
        resScoreColumn.setCellFactory(col->new TableCell<>(){@Override protected void updateItem(Double score,boolean empty){super.updateItem(score,empty);if(empty||score==null){setText(null);setStyle("");}else{setText(String.format("%.1f %%",score));setStyle(score>=75?"-fx-text-fill:#16a34a;-fx-font-weight:700;":score>=60?"-fx-text-fill:#b45309;-fx-font-weight:700;":"-fx-text-fill:#dc2626;-fx-font-weight:700;");}}});
        resPointsColumn .setCellValueFactory(cd->{ResultatQuiz r=(ResultatQuiz)cd.getValue()[0];return new SimpleStringProperty(r.getEarnedPoints()+" / "+r.getTotalPoints());});
        resDateColumn   .setCellValueFactory(cd->{ResultatQuiz r=(ResultatQuiz)cd.getValue()[0];return new SimpleStringProperty(r.getDatePassation()!=null?r.getDatePassation().format(DT_FMT):"—");});
        resMentionColumn.setCellValueFactory(cd->new SimpleStringProperty(mention(((ResultatQuiz)cd.getValue()[0]).getScore())));
        resultatsTable.setItems(resultatList);
    }

    private void loadResultats(Quiz quiz) {
        resultatList.clear(); setText(resultatsMessageLabel,""); resetStats();
        try {
            List<Object[]> rows=resultatQuizService.getByQuizWithEtudiant(quiz.getId());
            if(rows.isEmpty()){setText(resultatsMessageLabel,"Aucun étudiant n'a passé ce quiz.");return;}
            resultatList.setAll(rows);
            double[] stats=resultatQuizService.getStatsQuiz(quiz.getId());
            setText(statsMoyenneLabel, String.format("%.1f %%",stats[0]));
            setText(statsMeilleurLabel,String.format("%.1f %%",stats[1]));
            setText(statsPlusBas,      String.format("%.1f %%",stats[2]));
            setText(statsTotalLabel,   String.valueOf((int)stats[3]));
        } catch(Exception e){setStatus(resultatsMessageLabel,"Erreur : "+e.getMessage(),true);}
    }

    private void resetStats(){setText(statsMoyenneLabel,"—");setText(statsMeilleurLabel,"—");setText(statsPlusBas,"—");setText(statsTotalLabel,"—");}

    // ═════════════════════════════════════════════════════════════
    // PAGE 7 — STATISTIQUES GLOBALES
    // ═════════════════════════════════════════════════════════════
    @FXML public void showStatistiquesPage() { showOnly(statistiquesSection); loadStatistiquesGlobales(); }

    private void setupStatsTable() {
        if (statsTable == null) return;
        statsTitreColumn     .setCellValueFactory(cd -> new SimpleStringProperty((String)  cd.getValue()[1]));
        statsMoyenneColumn   .setCellValueFactory(cd -> new SimpleStringProperty(String.format("%.1f %%", (Double) cd.getValue()[2])));
        statsTauxColumn      .setCellValueFactory(cd -> new SimpleStringProperty(String.format("%.1f %%", (Double) cd.getValue()[4])));
        statsTentativesColumn.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf((Long)    cd.getValue()[3])));
        statsMeilleurColumn  .setCellValueFactory(cd -> new SimpleStringProperty(String.format("%.1f %%", (Double) cd.getValue()[5])));
        statsPireColumn      .setCellValueFactory(cd -> new SimpleStringProperty(String.format("%.1f %%", (Double) cd.getValue()[6])));
        statsDifficulteColumn.setCellValueFactory(cd -> new SimpleStringProperty(labelDifficulte((Double) cd.getValue()[2])));
        statsMoyenneColumn.setCellFactory(col -> new TableCell<Object[], String>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                Object[] row = getTableView().getItems().get(getIndex());
                double moy = (Double) row[2];
                if      (moy < 40) setStyle("-fx-text-fill:#ef4444;-fx-font-weight:800;");
                else if (moy < 60) setStyle("-fx-text-fill:#f97316;-fx-font-weight:700;");
                else if (moy < 75) setStyle("-fx-text-fill:#fbbf24;-fx-font-weight:700;");
                else               setStyle("-fx-text-fill:#22c55e;-fx-font-weight:700;");
            }
        });
        statsDifficulteColumn.setCellFactory(col -> new TableCell<Object[], String>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                if      (val.contains("Très difficile")) setStyle("-fx-text-fill:#ef4444;-fx-font-weight:800;");
                else if (val.contains("Difficile"))       setStyle("-fx-text-fill:#f97316;-fx-font-weight:700;");
                else if (val.contains("Moyen"))           setStyle("-fx-text-fill:#fbbf24;-fx-font-weight:700;");
                else                                       setStyle("-fx-text-fill:#22c55e;-fx-font-weight:700;");
            }
        });
        statsTable.setItems(statsList);
    }

    private void loadStatistiquesGlobales() {
        setText(statsGlobalesMessageLabel, ""); statsList.clear();
        Utilisateur u = UserSession.getCurrentUser(); if (u == null) return;
        try {
            double[] resume = resultatQuizService.getResumeGlobalEnseignant(u.getId());
            setText(kpiTotalQuizLabel,       String.valueOf((int) resume[0]));
            setText(kpiTotalTentativesLabel, String.valueOf((int) resume[1]));
            setText(kpiMoyenneGeneraleLabel, String.format("%.1f %%", resume[2]));
            setText(kpiTauxReussiteLabel,    String.format("%.1f %%", resume[3]));
            List<Object[]> stats = resultatQuizService.getStatsGlobalesPourEnseignant(u.getId());
            if (stats.isEmpty()) { setText(statsGlobalesMessageLabel, "Aucune donnée."); resetCardsStats(); return; }
            statsList.setAll(stats);
            Object[] difficile = stats.get(0);
            setText(quizPlusDifficileNomLabel,  (String) difficile[1]);
            setText(quizPlusDifficileStatLabel, "Moyenne : " + String.format("%.1f %%", (Double) difficile[2]));
            setText(quizPlusDifficileMoyLabel,  String.format("%.1f %%", (Double) difficile[2]));
            setText(quizPlusDifficileTauxLabel, String.format("%.1f %%", (Double) difficile[4]));
            setText(quizPlusDifficileTentLabel, String.valueOf((Long) difficile[3]));
            Object[] facile = stats.get(stats.size() - 1);
            setText(quizPlusFacileNomLabel,  (String) facile[1]);
            setText(quizPlusFacileStatLabel, "Moyenne : " + String.format("%.1f %%", (Double) facile[2]));
            setText(quizPlusFacileMoyLabel,  String.format("%.1f %%", (Double) facile[2]));
            setText(quizPlusFacileTauxLabel, String.format("%.1f %%", (Double) facile[4]));
            setText(quizPlusFacileTentLabel, String.valueOf((Long) facile[3]));
            Object[] populaire = stats.stream().max(java.util.Comparator.comparingLong(r -> (Long) r[3])).orElse(stats.get(0));
            setText(quizPlusPopulaireNomLabel,  (String) populaire[1]);
            setText(quizPlusPopulaireStatLabel, populaire[3] + " tentatives");
            setText(quizPlusPopulaireTentLabel, String.valueOf((Long) populaire[3]));
            setText(quizPlusPopulaireMoyLabel,  String.format("%.1f %%", (Double) populaire[2]));
        } catch (Exception e) { setStatus(statsGlobalesMessageLabel, "Erreur : " + e.getMessage(), true); e.printStackTrace(); }
    }

    private void resetCardsStats() {
        setText(quizPlusDifficileNomLabel,"—"); setText(quizPlusDifficileStatLabel,"—"); setText(quizPlusDifficileMoyLabel,"—"); setText(quizPlusDifficileTauxLabel,"—"); setText(quizPlusDifficileTentLabel,"—");
        setText(quizPlusFacileNomLabel,"—");    setText(quizPlusFacileStatLabel,"—");    setText(quizPlusFacileMoyLabel,"—");    setText(quizPlusFacileTauxLabel,"—");    setText(quizPlusFacileTentLabel,"—");
        setText(quizPlusPopulaireNomLabel,"—"); setText(quizPlusPopulaireStatLabel,"—"); setText(quizPlusPopulaireTentLabel,"—");setText(quizPlusPopulaireMoyLabel,"—");
    }

    private static String labelDifficulte(double moyenne) {
        if (moyenne < 40) return "🔴 Très difficile";
        if (moyenne < 60) return "🟠 Difficile";
        if (moyenne < 75) return "🟡 Moyen";
        return "🟢 Facile";
    }

    // ═════════════════════════════════════════════════════════════
    // UTILITAIRES & VALIDATION
    // ═════════════════════════════════════════════════════════════
    private static String mention(double s){return s>=90?"🏆 Excellent":s>=75?"✅ Bien":s>=60?"📘 Passable":"❌ Insuffisant";}
    private void setText(Label l,String t){if(l!=null)l.setText(t!=null?t:"");}
    private void showPanel(VBox p){if(p!=null){p.setVisible(true);p.setManaged(true);}}
    private void hidePanel(VBox p){if(p!=null){p.setVisible(false);p.setManaged(false);}}
    private String safe(String v){return v==null?"":v;}
    private void setStatus(Label l,String msg,boolean error){if(l==null)return;l.setText(msg);l.getStyleClass().removeAll("error-status","neutral-status","success-status");l.getStyleClass().add(error?"error-status":"success-status");}
    @SuppressWarnings({"unchecked","rawtypes"})
    private void setupColumn(TableColumn col,String prop){if(col!=null)col.setCellValueFactory(new PropertyValueFactory<>(prop));}
    public void setCurrentUser(String nom,String role){}

    private boolean validateQuizForm() {
        boolean ok=true; clearQuizFormErrors();
        if(quizTitreField!=null&&quizTitreField.getText().trim().isEmpty()){setText(quizTitreErrorLabel,"Le titre est obligatoire.");ok=false;}
        if(quizTypeCombo!=null&&quizTypeCombo.getValue()==null){setText(quizTypeErrorLabel,"Choisissez un type.");ok=false;}
        if(quizCorrectionApresCombo!=null&&quizCorrectionApresCombo.getValue()==null){setText(quizCorrectionErrorLabel,"Choisissez la correction.");ok=false;}
        if(quizDateDebutPicker!=null&&quizDateDebutPicker.getValue()==null){setText(quizDateDebutErrorLabel,"Date d'ouverture obligatoire.");ok=false;}
        if(quizDateFinPicker!=null&&quizDateFinPicker.getValue()==null){setText(quizDateFinErrorLabel,"Date de clôture obligatoire.");ok=false;}
        if(quizDateDebutPicker!=null&&quizDateFinPicker!=null){java.time.LocalDate d=quizDateDebutPicker.getValue(),f=quizDateFinPicker.getValue();if(d!=null&&f!=null&&!f.isAfter(d)){setText(quizDateFinErrorLabel,"La clôture doit être après l'ouverture.");ok=false;}}
        return ok;
    }
    private boolean validateQuestionForm() {
        boolean ok=true; clearQuestionFormErrors();
        if(questionTexteArea!=null&&questionTexteArea.getText().trim().isEmpty()){setText(questionTexteErrorLabel,"L'énoncé est obligatoire.");ok=false;}
        String type=questionTypeCombo!=null?questionTypeCombo.getValue():null;
        if(type==null){setText(questionTypeErrorLabel,"Choisissez un type.");return false;}
        switch(type){
            case "vrai_faux"->{if((vraiFauxVraiRadio==null||!vraiFauxVraiRadio.isSelected())&&(vraiFauxFauxRadio==null||!vraiFauxFauxRadio.isSelected())){setText(vraiFauxErrorLabel,"Sélectionnez Vrai ou Faux.");ok=false;}}
            case "choix_unique"->{if(choixUniqueReponsesContainer==null||choixUniqueReponsesContainer.getChildren().isEmpty()){setText(choixUniqueErrorLabel,"Ajoutez au moins une réponse.");ok=false;}else{boolean h=choixUniqueReponsesContainer.getChildren().stream().filter(n->n instanceof HBox).anyMatch(n->((RadioButton)((HBox)n).getChildren().get(0)).isSelected());if(!h){setText(choixUniqueErrorLabel,"Sélectionnez la bonne réponse.");ok=false;}}}
            case "choix_multiple"->{if(choixMultipleReponsesContainer==null||choixMultipleReponsesContainer.getChildren().isEmpty()){setText(choixMultipleErrorLabel,"Ajoutez au moins une réponse.");ok=false;}else{boolean h=choixMultipleReponsesContainer.getChildren().stream().filter(n->n instanceof HBox).anyMatch(n->((CheckBox)((HBox)n).getChildren().get(0)).isSelected());if(!h){setText(choixMultipleErrorLabel,"Cochez au moins une réponse correcte.");ok=false;}}}
        }
        return ok;
    }
    private boolean validateReponseForm(){setText(reponseTexteErrorLabel,"");if(reponseTexteArea!=null&&reponseTexteArea.getText().trim().isEmpty()){setText(reponseTexteErrorLabel,"Le texte est obligatoire.");return false;}return true;}
    private void clearQuizFormErrors(){setText(quizTitreErrorLabel,"");setText(quizTypeErrorLabel,"");setText(quizCorrectionErrorLabel,"");setText(quizDureeErrorLabel,"");setText(quizTentativesErrorLabel,"");setText(quizDateDebutErrorLabel,"");setText(quizDateFinErrorLabel,"");setText(quizCoursErrorLabel,"");setText(quizInstructionsErrorLabel,"");setText(quizDescriptionErrorLabel,"");setText(quizFormStatusLabel,"");}
    private void clearQuestionFormErrors(){setText(questionTexteErrorLabel,"");setText(questionTypeErrorLabel,"");setText(questionTypeHintLabel,"");setText(questionPointsErrorLabel,"");setText(questionExplicationErrorLabel,"");setText(questionFormStatusLabel,"");setText(vraiFauxErrorLabel,"");setText(choixUniqueErrorLabel,"");setText(choixMultipleErrorLabel,"");setText(texteLibreErrorLabel,"");}
    private void clearQuizForm(){clearQuizFormErrors();if(quizTitreField!=null)quizTitreField.clear();if(quizTypeCombo!=null)quizTypeCombo.setValue(null);if(quizCorrectionApresCombo!=null)quizCorrectionApresCombo.setValue(null);if(quizDateDebutPicker!=null)quizDateDebutPicker.setValue(null);if(quizDateFinPicker!=null)quizDateFinPicker.setValue(null);if(quizCoursCombo!=null)quizCoursCombo.setValue(null);if(quizInstructionsArea!=null)quizInstructionsArea.clear();if(quizDescriptionArea!=null)quizDescriptionArea.clear();if(quizDateRangeInfoLabel!=null)setText(quizDateRangeInfoLabel,"Sélectionnez les deux dates.");}
    private void clearQuestionForm(){clearQuestionFormErrors();if(questionTexteArea!=null)questionTexteArea.clear();if(questionTypeCombo!=null)questionTypeCombo.setValue(null);if(questionExplicationArea!=null)questionExplicationArea.clear();if(questionPointsSpinner!=null)questionPointsSpinner.getValueFactory().setValue(1);if(questionOrdreSpinner!=null)questionOrdreSpinner.getValueFactory().setValue(1);if(vraiFauxVraiRadio!=null)vraiFauxVraiRadio.setSelected(false);if(vraiFauxFauxRadio!=null)vraiFauxFauxRadio.setSelected(false);if(choixUniqueReponsesContainer!=null)choixUniqueReponsesContainer.getChildren().clear();if(choixMultipleReponsesContainer!=null)choixMultipleReponsesContainer.getChildren().clear();if(texteLibreReponseArea!=null)texteLibreReponseArea.clear();hidePanel(reponsesVraiFauxPanel);hidePanel(reponsesChoixUniquePanel);hidePanel(reponsesChoixMultiplePanel);hidePanel(reponsesTexteLibrePanel);}
    private void clearReponseForm(){setText(reponseTexteErrorLabel,"");setText(reponseOrdreErrorLabel,"");setText(reponseFeedbackErrorLabel,"");setText(reponseFormStatusLabel,"");if(reponseTexteArea!=null)reponseTexteArea.clear();if(reponseCorrecteCheck!=null)reponseCorrecteCheck.setSelected(false);if(reponseFeedbackArea!=null)reponseFeedbackArea.clear();if(reponseMediaUrlField!=null)reponseMediaUrlField.clear();if(reponseOrdreSpinner!=null)reponseOrdreSpinner.getValueFactory().setValue(1);}
}