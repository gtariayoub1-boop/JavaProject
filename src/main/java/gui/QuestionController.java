package gui;

import entities.Question;
import entities.Reponse;
import entities.Quiz;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.QuestionService;
import services.ReponseService;
import utils.UserSession;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class QuestionController implements Initializable {

    // ── TOPBAR ────────────────────────────────────────────────────
    @FXML private Label      sectionTitleLabel;
    @FXML private Label      sectionSubtitleLabel;
    @FXML private Label      currentUserNameLabel;
    @FXML private Label      currentUserRoleLabel;
    @FXML private MenuButton profileMenuButton;

    // ── PAGE 1 : LISTE QUESTIONS ──────────────────────────────────
    @FXML private VBox  questionsSection;
    @FXML private Label questionsSectionTitleLabel;
    @FXML private Label questionsSectionSubtitleLabel;
    @FXML private Label questionsMessageLabel;

    @FXML private TableView<Question> questionsTable;
    @FXML private TableColumn<?,?>   qOrdreColumn;
    @FXML private TableColumn<?,?>   qTexteColumn;
    @FXML private TableColumn<?,?>   qTypeColumn;
    @FXML private TableColumn<?,?>   qPointsColumn;
    @FXML private TableColumn<?,?>   qActionsColumn;

    @FXML private VBox  questionDetailsPanel;
    @FXML private Label detailQuestionTexteLabel;
    @FXML private Label detailQuestionMetaLabel;
    @FXML private VBox  detailReponsesContainer;
    @FXML private Label detailQuestionExplicationLabel;

    // ── PAGE 2 : FORMULAIRE QUESTION ─────────────────────────────
    @FXML private VBox      questionFormSection;
    @FXML private Label     questionFormTitleLabel;
    @FXML private TextArea  questionTexteArea;
    @FXML private Label     questionTexteErrorLabel;
    @FXML private ComboBox<String> questionTypeCombo;
    @FXML private Label     questionTypeErrorLabel;
    @FXML private Label     questionTypeHintLabel;
    @FXML private Spinner<Integer> questionPointsSpinner;
    @FXML private Spinner<Integer> questionOrdreSpinner;
    @FXML private TextArea  questionExplicationArea;
    @FXML private Label     questionFormStatusLabel;

    // ── Panels dynamiques réponses ────────────────────────────────

    // Vrai / Faux
    @FXML private VBox        reponsesVraiFauxPanel;
    @FXML private RadioButton vraiFauxVraiRadio;
    @FXML private RadioButton vraiFauxFauxRadio;
    @FXML private Label       vraiFauxErrorLabel;

    // Choix unique
    @FXML private VBox  reponsesChoixUniquePanel;
    @FXML private VBox  choixUniqueReponsesContainer;
    @FXML private Label choixUniqueErrorLabel;

    // Choix multiple
    @FXML private VBox  reponsesChoixMultiplePanel;
    @FXML private VBox  choixMultipleReponsesContainer;
    @FXML private Label choixMultipleErrorLabel;

    // Texte libre
    @FXML private VBox     reponsesTexteLibrePanel;
    @FXML private TextArea texteLibreReponseArea;
    @FXML private Label    texteLibreErrorLabel;

    // ── PAGE 3 : LISTE RÉPONSES ───────────────────────────────────
    @FXML private VBox  reponsesSection;
    @FXML private Label reponsesSectionTitleLabel;
    @FXML private Label reponsesSectionSubtitleLabel;
    @FXML private Label reponsesMessageLabel;

    @FXML private TableView<Reponse> reponsesTable;
    @FXML private TableColumn<?,?>  rOrdreColumn;
    @FXML private TableColumn<?,?>  rTexteColumn;
    @FXML private TableColumn<?,?>  rCorrecteColumn;
    @FXML private TableColumn<?,?>  rFeedbackColumn;
    @FXML private TableColumn<?,?>  rActionsColumn;

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

    // ── État interne ──────────────────────────────────────────────
    private Quiz     currentQuiz      = null;
    private Question selectedQuestion = null;
    private Question editingQuestion  = null;
    private Reponse  editingReponse   = null;

    // ToggleGroup créé en code (non injecté via FXML) pour choix unique
    private final ToggleGroup choixUniqueGroup = new ToggleGroup();

    private final QuestionService questionService = new QuestionService();
    private final ReponseService  reponseService  = new ReponseService();

    private final ObservableList<Question> questionList = FXCollections.observableArrayList();
    private final ObservableList<Reponse>  reponseList  = FXCollections.observableArrayList();

    // ── INITIALISATION ────────────────────────────────────────────
    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {

        if (questionTypeCombo != null) {
            questionTypeCombo.setItems(FXCollections.observableArrayList(
                    "choix_unique", "choix_multiple", "vrai_faux", "texte_libre"));
            questionTypeCombo.valueProperty().addListener((obs, old, nv) -> onTypeChanged(nv));
        }

        // TableView questions
        if (questionsTable != null) {
            setupColumn(qOrdreColumn,  "ordreAffichage");
            setupColumn(qTexteColumn,  "texte");
            setupColumn(qTypeColumn,   "typeQuestion");
            setupColumn(qPointsColumn, "points");

            if (qActionsColumn != null) {
                ((TableColumn<Question, Void>) qActionsColumn).setCellFactory(col -> new TableCell<>() {
                    private final Button btnEdit = new Button("✏ Modifier");
                    private final Button btnRep  = new Button("💬 Réponses");
                    private final Button btnDel  = new Button("🗑 Supprimer");
                    private final HBox   box     = new HBox(6, btnEdit, btnRep, btnDel);
                    {
                        btnEdit.getStyleClass().addAll("secondary-button", "table-action-button");
                        btnRep .getStyleClass().addAll("primary-button",   "table-action-button");
                        btnDel .getStyleClass().addAll("danger-button",    "table-action-button");
                        btnEdit.setOnAction(e -> startEditQuestion(getTableView().getItems().get(getIndex())));
                        btnRep .setOnAction(e -> openReponsesFor(getTableView().getItems().get(getIndex())));
                        btnDel .setOnAction(e -> confirmDeleteQuestion(getTableView().getItems().get(getIndex())));
                    }
                    @Override protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : box);
                    }
                });
            }

            questionsTable.setItems(questionList);
            questionsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, old, nw) -> { selectedQuestion = nw; updateQuestionDetailPanel(nw); });
        }

        // TableView réponses
        if (reponsesTable != null) {
            setupColumn(rOrdreColumn,    "ordreAffichage");
            setupColumn(rTexteColumn,    "texteReponse");
            setupColumn(rCorrecteColumn, "estCorrecte");
            setupColumn(rFeedbackColumn, "feedbackSpecifique");

            if (rActionsColumn != null) {
                ((TableColumn<Reponse, Void>) rActionsColumn).setCellFactory(col -> new TableCell<>() {
                    private final Button btnEdit = new Button("✏");
                    private final Button btnDel  = new Button("🗑");
                    private final HBox   box     = new HBox(6, btnEdit, btnDel);
                    {
                        btnEdit.getStyleClass().addAll("secondary-button", "table-action-button");
                        btnDel .getStyleClass().addAll("danger-button",    "table-action-button");
                        btnEdit.setOnAction(e -> startEditReponse(getTableView().getItems().get(getIndex())));
                        btnDel .setOnAction(e -> confirmDeleteReponse(getTableView().getItems().get(getIndex())));
                    }
                    @Override protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : box);
                    }
                });
            }
            reponsesTable.setItems(reponseList);
        }

        Utilisateur u = UserSession.getCurrentUser();
        if (u != null) setCurrentUser(u.getNomComplet(), u.getType());
    }

    public void initWithQuiz(Quiz quiz) {
        this.currentQuiz = quiz;
        setText(sectionTitleLabel,             "Questions — " + quiz.getTitre());
        setText(sectionSubtitleLabel,          "Gérez les questions de ce quiz.");
        setText(questionsSectionTitleLabel,    quiz.getTitre());
        setText(questionsSectionSubtitleLabel, "Ajoutez, modifiez ou supprimez les questions.");
        showOnly(questionsSection);
        loadQuestions();
    }

    // ── NAVIGATION ────────────────────────────────────────────────
    private void showOnly(VBox target) {
        for (VBox s : new VBox[]{ questionsSection, questionFormSection, reponsesSection })
            if (s != null) { s.setVisible(false); s.setManaged(false); }
        if (target != null) { target.setVisible(true); target.setManaged(true); }
    }

    // ── PAGE 1 ────────────────────────────────────────────────────
    @FXML public void showQuizListPage() {
        if (questionsSection != null)
            questionsSection.getScene().getWindow().hide();
    }

    @FXML public void showQuestionCreateForm() {
        if (currentQuiz == null) {
            setStatus(questionsMessageLabel, "Erreur : aucun quiz associé.", true);
            return;
        }
        editingQuestion = null;
        showOnly(questionFormSection);
        setText(sectionTitleLabel,      "Nouvelle question");
        setText(questionFormTitleLabel, "Nouvelle question");
        clearQuestionForm();
    }

    @FXML private void closeQuestionDetails() {
        if (questionDetailsPanel != null) {
            questionDetailsPanel.setVisible(false);
            questionDetailsPanel.setManaged(false);
        }
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

    // ── CHANGEMENT DE TYPE → affichage du bon panel ───────────────
    private void onTypeChanged(String type) {
        // Masquer tous les panels de réponses
        hidePanel(reponsesVraiFauxPanel);
        hidePanel(reponsesChoixUniquePanel);
        hidePanel(reponsesChoixMultiplePanel);
        hidePanel(reponsesTexteLibrePanel);

        if (type == null) {
            setText(questionTypeHintLabel, "");
            return;
        }

        switch (type) {
            case "vrai_faux" -> {
                showPanel(reponsesVraiFauxPanel);
                setStatus(questionTypeHintLabel,
                        "ℹ  Sélectionnez la bonne réponse ci-dessous.", false);
            }
            case "choix_unique" -> {
                showPanel(reponsesChoixUniquePanel);
                // Ajouter 2 lignes par défaut si le conteneur est vide
                if (choixUniqueReponsesContainer != null
                        && choixUniqueReponsesContainer.getChildren().isEmpty()) {
                    addChoixUniqueRow();
                    addChoixUniqueRow();
                }
                setStatus(questionTypeHintLabel,
                        "ℹ  Saisissez les réponses et sélectionnez la correcte (●).", false);
            }
            case "choix_multiple" -> {
                showPanel(reponsesChoixMultiplePanel);
                if (choixMultipleReponsesContainer != null
                        && choixMultipleReponsesContainer.getChildren().isEmpty()) {
                    addChoixMultipleRow();
                    addChoixMultipleRow();
                }
                setStatus(questionTypeHintLabel,
                        "ℹ  Saisissez les réponses et cochez les correctes (☑).", false);
            }
            case "texte_libre" -> {
                showPanel(reponsesTexteLibrePanel);
                setStatus(questionTypeHintLabel,
                        "ℹ  Saisissez la réponse correcte de référence.", false);
            }
            default -> setText(questionTypeHintLabel, "");
        }
    }

    // ── AJOUT DE LIGNES RÉPONSE DYNAMIQUES ───────────────────────

    /**
     * Ajoute une ligne [● TextField  🗑] dans le panel Choix unique.
     */
    @FXML private void addChoixUniqueRow() {
        if (choixUniqueReponsesContainer == null) return;
        int index = choixUniqueReponsesContainer.getChildren().size() + 1;

        RadioButton radio = new RadioButton();
        radio.setToggleGroup(choixUniqueGroup);
        radio.setUserData(index);

        TextField field = new TextField();
        field.setPromptText("Réponse " + index + "…");
        field.getStyleClass().add("input-field");
        HBox.setHgrow(field, javafx.scene.layout.Priority.ALWAYS);

        Button btnDel = new Button("🗑");
        btnDel.getStyleClass().addAll("danger-button", "compact-button");

        HBox row = new HBox(10, radio, field, btnDel);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        btnDel.setOnAction(e -> choixUniqueReponsesContainer.getChildren().remove(row));

        choixUniqueReponsesContainer.getChildren().add(row);
    }

    /**
     * Ajoute une ligne [☐ TextField  🗑] dans le panel Choix multiple.
     */
    @FXML private void addChoixMultipleRow() {
        if (choixMultipleReponsesContainer == null) return;
        int index = choixMultipleReponsesContainer.getChildren().size() + 1;

        CheckBox check = new CheckBox();

        TextField field = new TextField();
        field.setPromptText("Réponse " + index + "…");
        field.getStyleClass().add("input-field");
        HBox.setHgrow(field, javafx.scene.layout.Priority.ALWAYS);

        Button btnDel = new Button("🗑");
        btnDel.getStyleClass().addAll("danger-button", "compact-button");

        HBox row = new HBox(10, check, field, btnDel);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        btnDel.setOnAction(e -> choixMultipleReponsesContainer.getChildren().remove(row));

        choixMultipleReponsesContainer.getChildren().add(row);
    }

    // ── PAGE 2 : SAUVEGARDE ───────────────────────────────────────
    @FXML private void cancelQuestionForm() {
        editingQuestion = null;
        showOnly(questionsSection);
        setText(sectionTitleLabel, "Questions — " + (currentQuiz != null ? currentQuiz.getTitre() : ""));
    }

    @FXML private void saveQuestion() {
        if (currentQuiz == null) {
            setStatus(questionFormStatusLabel, "Erreur critique : aucun quiz associé.", true);
            return;
        }
        if (!validateQuestionForm()) return;

        try {
            boolean isNew = (editingQuestion == null);
            Question question = isNew ? new Question() : editingQuestion;

            question.setTexte(questionTexteArea.getText().trim());
            String type = questionTypeCombo.getValue();
            question.setTypeQuestion(type);

            if (questionPointsSpinner.getValue() != null)
                question.setPoints(questionPointsSpinner.getValue());
            if (questionOrdreSpinner != null && questionOrdreSpinner.getValue() != null)
                question.setOrdreAffichage(questionOrdreSpinner.getValue());
            if (questionExplicationArea != null)
                question.setExplicationReponse(questionExplicationArea.getText().trim());

            question.setQuiz(currentQuiz);

            Utilisateur u = UserSession.getCurrentUser();
            if (u != null && question.getCreateur() == null)
                question.setCreateur(u);

            if (isNew) {
                questionService.create(question);
                saveReponsesInline(question);
            } else {
                questionService.update(question);
                // En mode édition, effacer les anciennes réponses et recréer
                List<Reponse> old = reponseService.getByQuestion(question.getId());
                for (Reponse r : old) reponseService.delete(r.getId().intValue());
                saveReponsesInline(question);
            }

            setStatus(questionFormStatusLabel,
                    isNew ? "Question créée avec succès ✔" : "Question modifiée ✔", false);

            editingQuestion = null;
            loadQuestions();
            showOnly(questionsSection);
            setText(sectionTitleLabel, "Questions — " + currentQuiz.getTitre());

        } catch (Exception e) {
            setStatus(questionFormStatusLabel,
                    "Erreur (" + e.getClass().getSimpleName() + ") : " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    /**
     * Lit les widgets du panel actif et crée les Reponse en BDD.
     */
    private void saveReponsesInline(Question question) throws Exception {
        String type = question.getTypeQuestion();
        if (type == null) return;

        switch (type) {

            case "vrai_faux" -> {
                boolean vraiEstCorrect = vraiFauxVraiRadio != null && vraiFauxVraiRadio.isSelected();

                Reponse vrai = new Reponse();
                vrai.setTexteReponse("Vrai");
                vrai.setEstCorrecte(vraiEstCorrect);
                vrai.setOrdreAffichage(1);
                vrai.setQuestion(question);
                reponseService.create(vrai);

                Reponse faux = new Reponse();
                faux.setTexteReponse("Faux");
                faux.setEstCorrecte(!vraiEstCorrect);
                faux.setOrdreAffichage(2);
                faux.setQuestion(question);
                reponseService.create(faux);
            }

            case "choix_unique" -> {
                if (choixUniqueReponsesContainer == null) return;
                int ordre = 1;
                for (var node : choixUniqueReponsesContainer.getChildren()) {
                    if (!(node instanceof HBox row)) continue;
                    RadioButton radio = (RadioButton) row.getChildren().get(0);
                    TextField   field = (TextField)   row.getChildren().get(1);
                    String texte = field.getText().trim();
                    if (texte.isEmpty()) continue;

                    Reponse r = new Reponse();
                    r.setTexteReponse(texte);
                    r.setEstCorrecte(radio.isSelected());
                    r.setOrdreAffichage(ordre++);
                    r.setQuestion(question);
                    reponseService.create(r);
                }
            }

            case "choix_multiple" -> {
                if (choixMultipleReponsesContainer == null) return;
                int ordre = 1;
                for (var node : choixMultipleReponsesContainer.getChildren()) {
                    if (!(node instanceof HBox row)) continue;
                    CheckBox  check = (CheckBox)  row.getChildren().get(0);
                    TextField field = (TextField) row.getChildren().get(1);
                    String texte = field.getText().trim();
                    if (texte.isEmpty()) continue;

                    Reponse r = new Reponse();
                    r.setTexteReponse(texte);
                    r.setEstCorrecte(check.isSelected());
                    r.setOrdreAffichage(ordre++);
                    r.setQuestion(question);
                    reponseService.create(r);
                }
            }

            case "texte_libre" -> {
                String texte = texteLibreReponseArea != null
                        ? texteLibreReponseArea.getText().trim() : "";
                if (!texte.isEmpty()) {
                    Reponse r = new Reponse();
                    r.setTexteReponse(texte);
                    r.setEstCorrecte(true);
                    r.setOrdreAffichage(1);
                    r.setQuestion(question);
                    reponseService.create(r);
                }
            }
        }
    }

    // ── PAGE 3 : RÉPONSES ─────────────────────────────────────────
    @FXML private void backToQuestionsFromReponses() {
        showOnly(questionsSection);
        setText(sectionTitleLabel, "Questions — " + (currentQuiz != null ? currentQuiz.getTitre() : ""));
    }

    @FXML private void showReponseCreateForm() {
        editingReponse = null;
        setText(reponseFormTitleLabel, "Nouvelle réponse");
        clearReponseForm();
        showPanel(reponseFormPanel);
    }

    @FXML private void closeReponseForm() {
        editingReponse = null;
        hidePanel(reponseFormPanel);
    }

    @FXML private void saveReponse() {
        if (!validateReponseForm()) return;
        try {
            Reponse reponse = (editingReponse != null) ? editingReponse : new Reponse();
            reponse.setTexteReponse(reponseTexteArea.getText().trim());
            reponse.setEstCorrecte(reponseCorrecteCheck != null && reponseCorrecteCheck.isSelected());
            if (reponseOrdreSpinner != null && reponseOrdreSpinner.getValue() != null)
                reponse.setOrdreAffichage(reponseOrdreSpinner.getValue());
            if (reponseFeedbackArea != null)
                reponse.setFeedbackSpecifique(reponseFeedbackArea.getText().trim());
            if (reponseMediaUrlField != null)
                reponse.setMediaUrl(reponseMediaUrlField.getText().trim());
            reponse.setQuestion(selectedQuestion);

            if (editingReponse == null) reponseService.create(reponse);
            else                        reponseService.update(reponse);

            setStatus(reponseFormStatusLabel,
                    editingReponse == null ? "Réponse créée ✔" : "Réponse modifiée ✔", false);
            editingReponse = null;
            hidePanel(reponseFormPanel);
            loadReponses(selectedQuestion.getId());

        } catch (Exception e) {
            setStatus(reponseFormStatusLabel,
                    "Erreur : " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    // ── CHARGEMENT ────────────────────────────────────────────────
    private void loadQuestions() {
        questionList.clear();
        setText(questionsMessageLabel, "");
        if (currentQuiz == null) return;
        try {
            List<Question> list = questionService.getByQuiz(currentQuiz.getId());
            questionList.setAll(list);
            if (list.isEmpty()) setText(questionsMessageLabel, "Aucune question pour ce quiz.");
        } catch (Exception e) {
            setStatus(questionsMessageLabel, "Chargement impossible : " + e.getMessage(), true);
        }
    }

    private void loadReponses(long questionId) {
        reponseList.clear();
        setText(reponsesMessageLabel, "");
        try {
            List<Reponse> list = reponseService.getByQuestion(questionId);
            reponseList.setAll(list);
            if (list.isEmpty()) setText(reponsesMessageLabel, "Aucune réponse pour cette question.");
        } catch (Exception e) {
            setStatus(reponsesMessageLabel, "Chargement impossible : " + e.getMessage(), true);
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────
    private void startEditQuestion(Question q) {
        editingQuestion = q;
        showOnly(questionFormSection);
        setText(sectionTitleLabel,      "Modifier la question");
        setText(questionFormTitleLabel, "Modifier la question");
        populateQuestionForm(q);
    }

    private void openReponsesFor(Question q) {
        selectedQuestion = q;
        showOnly(reponsesSection);
        setText(sectionTitleLabel,            "Réponses");
        setText(reponsesSectionTitleLabel,    q.getTexte());
        setText(reponsesSectionSubtitleLabel, "Type : " + safe(q.getTypeQuestion())
                + " · " + safe(String.valueOf(q.getPoints())) + " pts");
        hidePanel(reponseFormPanel);

        boolean isTextLibre = "texte_libre".equals(q.getTypeQuestion());
        if (reponseCorrecteCheck != null) {
            reponseCorrecteCheck.setVisible(!isTextLibre);
            reponseCorrecteCheck.setManaged(!isTextLibre);
        }
        updateReponsesHint(q.getTypeQuestion());
        loadReponses(q.getId());
    }

    private void confirmDeleteQuestion(Question q) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cette question ?\n« " + q.getTexte() + " »",
                ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Confirmation");
        alert.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            try {
                questionService.delete(q.getId().intValue());
                if (selectedQuestion != null && selectedQuestion.getId().equals(q.getId())) {
                    selectedQuestion = null;
                    closeQuestionDetails();
                }
                setStatus(questionsMessageLabel, "Question supprimée.", false);
                loadQuestions();
            } catch (Exception e) {
                setStatus(questionsMessageLabel, "Suppression impossible : " + e.getMessage(), true);
            }
        });
    }

    private void startEditReponse(Reponse r) {
        editingReponse = r;
        setText(reponseFormTitleLabel, "Modifier la réponse");
        populateReponseForm(r);
        showPanel(reponseFormPanel);
    }

    private void confirmDeleteReponse(Reponse r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cette réponse ?", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Confirmation");
        alert.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            try {
                reponseService.delete(r.getId().intValue());
                setStatus(reponsesMessageLabel, "Réponse supprimée.", false);
                loadReponses(selectedQuestion.getId());
            } catch (Exception e) {
                setStatus(reponsesMessageLabel, "Suppression impossible : " + e.getMessage(), true);
            }
        });
    }

    private void updateQuestionDetailPanel(Question q) {
        if (questionDetailsPanel == null) return;
        boolean v = q != null;
        questionDetailsPanel.setVisible(v);
        questionDetailsPanel.setManaged(v);
        if (!v) return;

        setText(detailQuestionTexteLabel,       q.getTexte());
        setText(detailQuestionMetaLabel,        safe(q.getTypeQuestion())
                + " · " + (q.getPoints() != null ? q.getPoints() + " pts" : "-"));
        setText(detailQuestionExplicationLabel, safe(q.getExplicationReponse()));

        if (detailReponsesContainer != null) {
            detailReponsesContainer.getChildren().clear();
            try {
                List<Reponse> reponses = reponseService.getByQuestion(q.getId());
                for (Reponse r : reponses) {
                    String icone = Boolean.TRUE.equals(r.getEstCorrecte()) ? "✅ " : "❌ ";
                    Label lbl = new Label(icone + safe(r.getTexteReponse()));
                    lbl.setWrapText(true);
                    lbl.getStyleClass().add(
                            Boolean.TRUE.equals(r.getEstCorrecte()) ? "detail-value" : "detail-key");
                    detailReponsesContainer.getChildren().add(lbl);
                }
                if (reponses.isEmpty()) {
                    Label empty = new Label("Aucune réponse définie.");
                    empty.getStyleClass().add("dashboard-subsection");
                    detailReponsesContainer.getChildren().add(empty);
                }
            } catch (Exception e) {
                Label err = new Label("Chargement réponses impossible.");
                err.getStyleClass().add("field-error-label");
                detailReponsesContainer.getChildren().add(err);
            }
        }
    }

    private void populateQuestionForm(Question q) {
        clearQuestionForm();
        if (questionTexteArea       != null) questionTexteArea.setText(safe(q.getTexte()));
        if (questionTypeCombo       != null) questionTypeCombo.setValue(q.getTypeQuestion());
        if (questionPointsSpinner   != null && q.getPoints() != null)
            questionPointsSpinner.getValueFactory().setValue(q.getPoints());
        if (questionOrdreSpinner    != null && q.getOrdreAffichage() != null)
            questionOrdreSpinner.getValueFactory().setValue(q.getOrdreAffichage());
        if (questionExplicationArea != null)
            questionExplicationArea.setText(safe(q.getExplicationReponse()));

        // Pré-remplir les réponses existantes
        try {
            List<Reponse> reponses = reponseService.getByQuestion(q.getId());
            String type = q.getTypeQuestion();
            if (type == null) return;

            switch (type) {
                case "vrai_faux" -> {
                    for (Reponse r : reponses) {
                        if ("Vrai".equalsIgnoreCase(r.getTexteReponse()) && Boolean.TRUE.equals(r.getEstCorrecte()))
                            if (vraiFauxVraiRadio != null) vraiFauxVraiRadio.setSelected(true);
                        if ("Faux".equalsIgnoreCase(r.getTexteReponse()) && Boolean.TRUE.equals(r.getEstCorrecte()))
                            if (vraiFauxFauxRadio != null) vraiFauxFauxRadio.setSelected(true);
                    }
                }
                case "choix_unique" -> {
                    if (choixUniqueReponsesContainer != null)
                        choixUniqueReponsesContainer.getChildren().clear();
                    choixUniqueGroup.getToggles().clear();
                    for (Reponse r : reponses) {
                        addChoixUniqueRow();
                        HBox row = (HBox) choixUniqueReponsesContainer.getChildren()
                                .get(choixUniqueReponsesContainer.getChildren().size() - 1);
                        ((RadioButton) row.getChildren().get(0)).setSelected(Boolean.TRUE.equals(r.getEstCorrecte()));
                        ((TextField)   row.getChildren().get(1)).setText(safe(r.getTexteReponse()));
                    }
                }
                case "choix_multiple" -> {
                    if (choixMultipleReponsesContainer != null)
                        choixMultipleReponsesContainer.getChildren().clear();
                    for (Reponse r : reponses) {
                        addChoixMultipleRow();
                        HBox row = (HBox) choixMultipleReponsesContainer.getChildren()
                                .get(choixMultipleReponsesContainer.getChildren().size() - 1);
                        ((CheckBox)  row.getChildren().get(0)).setSelected(Boolean.TRUE.equals(r.getEstCorrecte()));
                        ((TextField) row.getChildren().get(1)).setText(safe(r.getTexteReponse()));
                    }
                }
                case "texte_libre" -> {
                    if (!reponses.isEmpty() && texteLibreReponseArea != null)
                        texteLibreReponseArea.setText(safe(reponses.get(0).getTexteReponse()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateReponseForm(Reponse r) {
        clearReponseForm();
        if (reponseTexteArea     != null) reponseTexteArea.setText(safe(r.getTexteReponse()));
        if (reponseCorrecteCheck != null) reponseCorrecteCheck.setSelected(Boolean.TRUE.equals(r.getEstCorrecte()));
        if (reponseOrdreSpinner  != null && r.getOrdreAffichage() != null)
            reponseOrdreSpinner.getValueFactory().setValue(r.getOrdreAffichage());
        if (reponseFeedbackArea  != null) reponseFeedbackArea.setText(safe(r.getFeedbackSpecifique()));
        if (reponseMediaUrlField != null) reponseMediaUrlField.setText(safe(r.getMediaUrl()));
    }

    private void updateReponsesHint(String type) {
        if (type == null || reponsesMessageLabel == null) return;
        switch (type) {
            case "choix_unique"   -> setStatus(reponsesMessageLabel, "ℹ  Une seule réponse correcte.", false);
            case "choix_multiple" -> setStatus(reponsesMessageLabel, "ℹ  Plusieurs réponses correctes possibles.", false);
            case "vrai_faux"      -> setStatus(reponsesMessageLabel, "ℹ  Vérifiez la bonne réponse.", false);
            case "texte_libre"    -> setStatus(reponsesMessageLabel, "ℹ  Correction manuelle.", false);
            default               -> setText(reponsesMessageLabel, "");
        }
    }

    // ── VALIDATION ────────────────────────────────────────────────
    private boolean validateQuestionForm() {
        boolean ok = true;
        clearQuestionFormErrors();

        if (questionTexteArea != null && questionTexteArea.getText().trim().isEmpty()) {
            setText(questionTexteErrorLabel, "L'énoncé est obligatoire.");
            ok = false;
        }

        String type = questionTypeCombo != null ? questionTypeCombo.getValue() : null;
        if (type == null) {
            setText(questionTypeErrorLabel, "Veuillez choisir un type.");
            return false;
        }

        // Validation spécifique par type
        switch (type) {
            case "vrai_faux" -> {
                boolean aucunSelectionne = (vraiFauxVraiRadio == null || !vraiFauxVraiRadio.isSelected())
                        && (vraiFauxFauxRadio == null || !vraiFauxFauxRadio.isSelected());
                if (aucunSelectionne) {
                    setText(vraiFauxErrorLabel, "Sélectionnez la bonne réponse (Vrai ou Faux).");
                    ok = false;
                }
            }
            case "choix_unique" -> {
                if (choixUniqueReponsesContainer == null
                        || choixUniqueReponsesContainer.getChildren().isEmpty()) {
                    setText(choixUniqueErrorLabel, "Ajoutez au moins une réponse.");
                    ok = false;
                } else {
                    boolean hasCorrect = choixUniqueReponsesContainer.getChildren().stream()
                            .filter(n -> n instanceof HBox)
                            .anyMatch(n -> ((RadioButton) ((HBox) n).getChildren().get(0)).isSelected());
                    if (!hasCorrect) {
                        setText(choixUniqueErrorLabel, "Sélectionnez la bonne réponse (●).");
                        ok = false;
                    }
                }
            }
            case "choix_multiple" -> {
                if (choixMultipleReponsesContainer == null
                        || choixMultipleReponsesContainer.getChildren().isEmpty()) {
                    setText(choixMultipleErrorLabel, "Ajoutez au moins une réponse.");
                    ok = false;
                } else {
                    boolean hasCorrect = choixMultipleReponsesContainer.getChildren().stream()
                            .filter(n -> n instanceof HBox)
                            .anyMatch(n -> ((CheckBox) ((HBox) n).getChildren().get(0)).isSelected());
                    if (!hasCorrect) {
                        setText(choixMultipleErrorLabel, "Cochez au moins une réponse correcte.");
                        ok = false;
                    }
                }
            }
            // texte_libre : réponse optionnelle
        }

        return ok;
    }

    private boolean validateReponseForm() {
        setText(reponseTexteErrorLabel, "");
        if (reponseTexteArea != null && reponseTexteArea.getText().trim().isEmpty()) {
            setText(reponseTexteErrorLabel, "Le texte de la réponse est obligatoire.");
            return false;
        }
        return true;
    }

    private void clearQuestionFormErrors() {
        setText(questionTexteErrorLabel, "");
        setText(questionTypeErrorLabel,  "");
        setText(questionTypeHintLabel,   "");
        setText(questionFormStatusLabel, "");
        setText(vraiFauxErrorLabel,      "");
        setText(choixUniqueErrorLabel,   "");
        setText(choixMultipleErrorLabel, "");
        setText(texteLibreErrorLabel,    "");
    }

    private void clearQuestionForm() {
        clearQuestionFormErrors();
        if (questionTexteArea       != null) questionTexteArea.clear();
        if (questionTypeCombo       != null) questionTypeCombo.setValue(null);
        if (questionExplicationArea != null) questionExplicationArea.clear();
        if (questionPointsSpinner   != null) questionPointsSpinner.getValueFactory().setValue(1);
        if (questionOrdreSpinner    != null) questionOrdreSpinner.getValueFactory().setValue(1);

        // Reset panels réponses
        if (vraiFauxVraiRadio != null) vraiFauxVraiRadio.setSelected(false);
        if (vraiFauxFauxRadio != null) vraiFauxFauxRadio.setSelected(false);
        if (choixUniqueReponsesContainer  != null) choixUniqueReponsesContainer.getChildren().clear();
        if (choixMultipleReponsesContainer != null) choixMultipleReponsesContainer.getChildren().clear();
        if (texteLibreReponseArea != null) texteLibreReponseArea.clear();

        // Masquer tous les panels dynamiques
        hidePanel(reponsesVraiFauxPanel);
        hidePanel(reponsesChoixUniquePanel);
        hidePanel(reponsesChoixMultiplePanel);
        hidePanel(reponsesTexteLibrePanel);
    }

    private void clearReponseForm() {
        setText(reponseTexteErrorLabel,    "");
        setText(reponseOrdreErrorLabel,    "");
        setText(reponseFeedbackErrorLabel, "");
        setText(reponseFormStatusLabel,    "");
        if (reponseTexteArea     != null) reponseTexteArea.clear();
        if (reponseCorrecteCheck != null) reponseCorrecteCheck.setSelected(false);
        if (reponseFeedbackArea  != null) reponseFeedbackArea.clear();
        if (reponseMediaUrlField != null) reponseMediaUrlField.clear();
        if (reponseOrdreSpinner  != null) reponseOrdreSpinner.getValueFactory().setValue(1);
    }

    // ── UTILITAIRES ───────────────────────────────────────────────
    private void setText(Label l, String t)  { if (l != null) l.setText(t != null ? t : ""); }
    private void showPanel(VBox p)           { if (p != null) { p.setVisible(true);  p.setManaged(true); } }
    private void hidePanel(VBox p)           { if (p != null) { p.setVisible(false); p.setManaged(false); } }
    private String safe(String v)            { return v == null ? "" : v; }

    private void setStatus(Label l, String msg, boolean error) {
        if (l == null) return;
        l.setText(msg);
        l.getStyleClass().removeAll("error-status", "neutral-status", "success-status");
        l.getStyleClass().add(error ? "error-status" : "success-status");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setupColumn(TableColumn col, String prop) {
        if (col != null) col.setCellValueFactory(new PropertyValueFactory<>(prop));
    }

    public void setCurrentUser(String nom, String role) {
        setText(currentUserNameLabel, nom);
        setText(currentUserRoleLabel, role);
        if (profileMenuButton != null)
            profileMenuButton.setText(nom.length() >= 2
                    ? nom.substring(0, 2).toUpperCase() : nom.toUpperCase());
    }
}