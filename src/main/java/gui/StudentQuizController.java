package gui;

import entities.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import services.*;
import utils.QuizNavigation;
import utils.SceneManager;
import utils.UserSession;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.ResourceBundle;

public class StudentQuizController implements Initializable {

    // ── TOPBAR ────────────────────────────────────────────────────
    @FXML private Label      sectionTitleLabel;
    @FXML private Label      sectionSubtitleLabel;
    @FXML private Label      currentUserNameLabel;
    @FXML private Label      currentUserRoleLabel;
    @FXML private MenuButton profileMenuButton;

    // ── PAGE 1 : LISTE DES QUIZ ───────────────────────────────────
    @FXML private VBox            quizListSection;
    @FXML private Label           quizListMessageLabel;
    @FXML private TextField       searchField;
    @FXML private ComboBox<String> filterTypeCombo;
    @FXML private ComboBox<String> filterDureeCombo;
    @FXML private Label           filterResultLabel;
    @FXML private TableView<Quiz>  quizTable;
    @FXML private TableColumn<?,?> quizTitreColumn;
    @FXML private TableColumn<?,?> quizTypeColumn;
    @FXML private TableColumn<?,?> quizDureeColumn;
    @FXML private TableColumn<?,?> quizDispoColumn;
    @FXML private TableColumn<?,?> quizActionsColumn;
    @FXML private VBox  quizInfoPanel;
    @FXML private Label detailQuizTitleLabel;
    @FXML private Label detailQuizTypeLabel;
    @FXML private Label detailQuizDureeLabel;
    @FXML private Label detailQuizTentativesLabel;
    @FXML private Label detailQuizDescLabel;
    @FXML private Label detailQuizInstructionsLabel;
    @FXML private Label detailQuizScoreLabel;
    @FXML private Button startQuizButton;

    // ── PAGE 2 : PASSAGE DU QUIZ ──────────────────────────────────
    @FXML private VBox     quizPassageSection;
    @FXML private Label    passageQuizTitleLabel;
    @FXML private Label    questionCounterLabel;
    @FXML private Label    timerLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label    progressLabel;
    @FXML private VBox     questionContainer;
    @FXML private Label    questionTexteLabel;
    @FXML private Label    questionTypeHintLabel;
    @FXML private Label    questionErrorLabel;
    @FXML private Button   prevButton;
    @FXML private Button   nextButton;
    @FXML private Button   submitButton;

    // Panels réponses
    @FXML private VBox        vraiFauxPanel;
    @FXML private RadioButton vraiFauxVraiRadio;
    @FXML private RadioButton vraiFauxFauxRadio;
    @FXML private VBox        choixUniquePanel;
    @FXML private VBox        choixUniqueContainer;
    @FXML private VBox        choixMultiplePanel;
    @FXML private VBox        choixMultipleContainer;
    @FXML private VBox        texteLibrePanel;
    @FXML private TextArea    texteLibreArea;

    // ── PAGE 3 : RÉSULTATS ────────────────────────────────────────
    @FXML private VBox  resultatsSection;
    @FXML private Label resultatScoreLabel;
    @FXML private Label resultatPointsLabel;
    @FXML private Label resultatDateLabel;
    @FXML private Label resultatMsgLabel;
    @FXML private VBox  resultatDetailsContainer;

    // ── ÉTAT ─────────────────────────────────────────────────────
    private Quiz              currentQuiz      = null;
    private List<Question>    questionList     = new ArrayList<>();
    private int               currentIndex     = 0;
    private Map<Long, Object> studentAnswers   = new LinkedHashMap<>();
    private ResultatQuiz      resultatCourant  = null;

    private Timeline countdownTimer   = null;
    private int      secondsRemaining = 0;

    @FXML private ToggleGroup vraiFauxGroup;
    private final ToggleGroup choixUniqueGroup = new ToggleGroup();

    // ── Services ─────────────────────────────────────────────────
    private final QuizService         quizService     = new QuizService();
    private final QuestionService     questionService = new QuestionService();
    private final ReponseService      reponseService  = new ReponseService();
    private final ResultatQuizService resultatService = new ResultatQuizService();
    private final EmailService        emailService    = new EmailService();
    private final PdfService          pdfService      = new PdfService();
    private final MercureService      mercureService  = new MercureService();
    private final QuizService         quizSvc         = new QuizService();

    private final ObservableList<Quiz>  quizObsList      = FXCollections.observableArrayList();
    private FilteredList<Quiz>          filteredQuizList;

    // ═════════════════════════════════════════════════════════════
    // INITIALISATION
    // ═════════════════════════════════════════════════════════════
    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {




        // TableView quiz
        if (quizTable != null) {
            setupColumn(quizTitreColumn, "titre");
            setupColumn(quizTypeColumn,  "typeQuiz");
            setupColumn(quizDureeColumn, "dureeMinutes");
            setupColumn(quizDispoColumn, "dateFinDisponibilite");

            if (quizActionsColumn != null) {
                ((TableColumn<Quiz, Void>) quizActionsColumn).setCellFactory(col -> new TableCell<>() {
                    private final Button btn = new Button("▶  Commencer");
                    {
                        btn.getStyleClass().addAll("primary-button", "table-action-button");
                        btn.setOnAction(e -> selectQuiz(getTableView().getItems().get(getIndex())));
                    }
                    @Override protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btn);
                    }
                });
            }

            filteredQuizList = new FilteredList<>(quizObsList, p -> true);
            quizTable.setItems(filteredQuizList);
            quizTable.getSelectionModel().selectedItemProperty()
                    .addListener((obs, old, nw) -> selectQuiz(nw));
        }

        // Filtres
        if (filterTypeCombo != null) {
            filterTypeCombo.setItems(FXCollections.observableArrayList(
                    "Tous les types", "QCM", "Vrai/Faux", "Texte libre", "Mixte"));
            filterTypeCombo.setValue("Tous les types");
            filterTypeCombo.valueProperty().addListener((obs, ov, nv) -> applyFilters());
        }
        if (filterDureeCombo != null) {
            filterDureeCombo.setItems(FXCollections.observableArrayList(
                    "Toutes durées", "≤ 15 min", "≤ 30 min", "≤ 60 min", "> 60 min"));
            filterDureeCombo.setValue("Toutes durées");
            filterDureeCombo.valueProperty().addListener((obs, ov, nv) -> applyFilters());
        }
        if (searchField != null)
            searchField.textProperty().addListener((obs, ov, nv) -> applyFilters());

        Utilisateur u = UserSession.getCurrentUser();
        if (u != null) setCurrentUser(u.getNomComplet(), u.getType());

        showOnly(quizListSection);
        setText(sectionTitleLabel,    "Mes Quiz");
        setText(sectionSubtitleLabel, "Consultez et passez les quiz disponibles.");
        loadAvailableQuizzes();

        if (QuizNavigation.consumeStudentSection() == QuizNavigation.StudentSection.RESULTS)
            showMesResultats();
    }

    // ═════════════════════════════════════════════════════════════
    // NAVIGATION
    // ═════════════════════════════════════════════════════════════
    private void showOnly(VBox target) {
        for (VBox s : new VBox[]{quizListSection, quizPassageSection, resultatsSection})
            if (s != null) { s.setVisible(false); s.setManaged(false); }
        if (target != null) { target.setVisible(true); target.setManaged(true); }
    }

    // ═════════════════════════════════════════════════════════════
    // TOPBAR
    // ═════════════════════════════════════════════════════════════
    @FXML private void handleLogout() throws IOException {
        stopTimer();
        UserSession.clear();
        SceneManager.switchScene("/login.fxml", "Campus Access");
    }

    @FXML private void openDashboard() throws IOException {
        stopTimer();
        SceneManager.switchScene("/main-layout-etudiant.fxml", "Student Dashboard");
    }

    // ═════════════════════════════════════════════════════════════
    // PAGE 1 — LISTE DES QUIZ
    // ═════════════════════════════════════════════════════════════
    @FXML public void showQuizListPage() {
        stopTimer();
        currentQuiz = null;
        studentAnswers.clear();
        showOnly(quizListSection);
        setText(sectionTitleLabel,    "Mes Quiz");
        setText(sectionSubtitleLabel, "Consultez et passez les quiz disponibles.");
        hidePanel(quizInfoPanel);
        loadAvailableQuizzes();
    }

    private void loadAvailableQuizzes() {
        quizObsList.clear();
        setText(quizListMessageLabel, "");
        try {
            List<Quiz> all = quizService.getAll();
            LocalDateTime now = LocalDateTime.now();
            List<Quiz> available = new ArrayList<>();
            for (Quiz q : all) {
                boolean debutOk = q.getDateDebutDisponibilite() == null
                        || !now.isBefore(q.getDateDebutDisponibilite());
                boolean finOk   = q.getDateFinDisponibilite() == null
                        || now.isBefore(q.getDateFinDisponibilite());
                if (debutOk && finOk) available.add(q);
            }
            quizObsList.setAll(available);
            if (available.isEmpty())
                setText(quizListMessageLabel, "Aucun quiz disponible pour le moment.");
            applyFilters();
        } catch (Exception e) {
            setStatus(quizListMessageLabel, "Chargement impossible : " + e.getMessage(), true);
        }
    }

    private void selectQuiz(Quiz quiz) {
        if (quiz == null) { hidePanel(quizInfoPanel); return; }
        currentQuiz = quiz;
        showPanel(quizInfoPanel);
        setText(detailQuizTitleLabel,        safe(quiz.getTitre()));
        setText(detailQuizTypeLabel,         safe(quiz.getTypeQuiz()));
        setText(detailQuizDureeLabel,        quiz.getDureeMinutes() != null ? quiz.getDureeMinutes() + " min" : "-");
        setText(detailQuizTentativesLabel,   quiz.getNombreTentativesAutorisees() != null
                ? String.valueOf(quiz.getNombreTentativesAutorisees()) : "Illimité");
        setText(detailQuizDescLabel,         safe(quiz.getDescription()));
        setText(detailQuizInstructionsLabel, safe(quiz.getInstructions()));

        try {
            Utilisateur u = UserSession.getCurrentUser();
            if (u != null) {
                double best  = resultatService.getMeilleurScore(u.getId().intValue(), quiz.getId());
                int    tries = resultatService.countTentatives(u.getId().intValue(), quiz.getId());
                setText(detailQuizScoreLabel, tries > 0
                        ? "Votre meilleur score : " + String.format("%.1f", best) + "%  (" + tries + " tentative(s))"
                        : "Vous n'avez pas encore passé ce quiz.");
            }
        } catch (Exception ignored) {}

        if (startQuizButton != null) {
            try {
                Utilisateur u = UserSession.getCurrentUser();
                int max  = quiz.getNombreTentativesAutorisees() != null
                        ? quiz.getNombreTentativesAutorisees() : Integer.MAX_VALUE;
                int done = u != null ? resultatService.countTentatives(u.getId().intValue(), quiz.getId()) : 0;
                if (done >= max) {
                    startQuizButton.setDisable(true);
                    startQuizButton.setText("Nombre max de tentatives atteint");
                } else {
                    startQuizButton.setDisable(false);
                    startQuizButton.setText("▶  Commencer le quiz");
                }
            } catch (Exception ignored) {
                startQuizButton.setDisable(false);
            }
        }
    }

    // ═════════════════════════════════════════════════════════════
    // PAGE 2 — PASSAGE DU QUIZ
    // ═════════════════════════════════════════════════════════════
    @FXML public void startQuiz() {
        if (currentQuiz == null) return;
        try {
            List<Question> questions = questionService.getByQuiz(currentQuiz.getId());
            if (questions == null || questions.isEmpty()) {
                setStatus(quizListMessageLabel, "Ce quiz n'a aucune question.", true);
                return;
            }
            questionList = questions;
            currentIndex = 0;
            studentAnswers.clear();

            showOnly(quizPassageSection);
            setText(sectionTitleLabel,     "En cours : " + currentQuiz.getTitre());
            setText(sectionSubtitleLabel,  "Répondez à toutes les questions avant la fin du temps.");
            setText(passageQuizTitleLabel, currentQuiz.getTitre());

            if (currentQuiz.getDureeMinutes() != null && currentQuiz.getDureeMinutes() > 0) {
                secondsRemaining = currentQuiz.getDureeMinutes() * 60;
                startTimer();
            } else {
                setText(timerLabel, "∞");
            }

            showQuestion(0);
        } catch (Exception e) {
            setStatus(quizListMessageLabel, "Impossible de démarrer : " + e.getMessage(), true);
        }
    }

    private void showQuestion(int index) {
        if (index < 0 || index >= questionList.size()) return;
        currentIndex = index;
        Question q   = questionList.get(index);
        int total    = questionList.size();

        setText(questionCounterLabel, "Question " + (index + 1) + " / " + total);
        setText(progressLabel,        (index + 1) + " / " + total);
        if (progressBar != null) progressBar.setProgress((double)(index + 1) / total);

        setText(questionTexteLabel, q.getTexte());
        setText(questionErrorLabel, "");

        if (prevButton   != null) prevButton.setDisable(index == 0);
        boolean isLast = (index == total - 1);
        if (nextButton   != null) { nextButton.setVisible(!isLast);  nextButton.setManaged(!isLast); }
        if (submitButton != null) { submitButton.setVisible(isLast); submitButton.setManaged(isLast); }

        hideAllAnswerPanels();
        try {
            List<Reponse> reponses = reponseService.getByQuestion(q.getId());
            switch (safe(q.getTypeQuestion())) {
                case "vrai_faux"      -> showVraiFaux(q.getId());
                case "choix_unique"   -> showChoixUnique(q.getId(), reponses);
                case "choix_multiple" -> showChoixMultiple(q.getId(), reponses);
                case "texte_libre"    -> showTexteLibre(q.getId());
                default               -> setText(questionTypeHintLabel, "Type inconnu : " + q.getTypeQuestion());
            }
        } catch (Exception e) {
            setText(questionTypeHintLabel, "Erreur : " + e.getMessage());
        }
    }

    private void showVraiFaux(Long questionId) {
        showPanel(vraiFauxPanel);
        setText(questionTypeHintLabel, "Sélectionnez Vrai ou Faux");
        vraiFauxGroup.selectToggle(null);
        Object saved = studentAnswers.get(questionId);
        if ("Vrai".equals(saved) && vraiFauxVraiRadio != null) vraiFauxVraiRadio.setSelected(true);
        if ("Faux".equals(saved) && vraiFauxFauxRadio != null) vraiFauxFauxRadio.setSelected(true);
    }

    private void showChoixUnique(Long questionId, List<Reponse> reponses) {
        showPanel(choixUniquePanel);
        setText(questionTypeHintLabel, "Une seule réponse correcte");
        if (choixUniqueContainer == null) return;
        choixUniqueContainer.getChildren().clear();
        choixUniqueGroup.getToggles().clear();
        Object saved = studentAnswers.get(questionId);
        for (Reponse r : reponses) {
            RadioButton rb = new RadioButton(safe(r.getTexteReponse()));
            rb.setToggleGroup(choixUniqueGroup);
            rb.setUserData(r.getId());
            rb.getStyleClass().add("quiz-option");
            rb.setWrapText(true);
            if (saved instanceof Long && saved.equals(r.getId())) rb.setSelected(true);
            choixUniqueContainer.getChildren().add(rb);
        }
    }

    private void showChoixMultiple(Long questionId, List<Reponse> reponses) {
        showPanel(choixMultiplePanel);
        setText(questionTypeHintLabel, "Plusieurs réponses possibles");
        if (choixMultipleContainer == null) return;
        choixMultipleContainer.getChildren().clear();
        @SuppressWarnings("unchecked")
        Set<Long> saved = (studentAnswers.get(questionId) instanceof Set s) ? s : new HashSet<>();
        for (Reponse r : reponses) {
            CheckBox cb = new CheckBox(safe(r.getTexteReponse()));
            cb.setUserData(r.getId());
            cb.getStyleClass().add("quiz-option");
            cb.setWrapText(true);
            if (saved.contains(r.getId())) cb.setSelected(true);
            choixMultipleContainer.getChildren().add(cb);
        }
    }

    private void showTexteLibre(Long questionId) {
        showPanel(texteLibrePanel);
        setText(questionTypeHintLabel, "Rédigez votre réponse");
        if (texteLibreArea != null) {
            Object saved = studentAnswers.get(questionId);
            texteLibreArea.setText(saved instanceof String s ? s : "");
        }
    }

    private void hideAllAnswerPanels() {
        hidePanel(vraiFauxPanel);
        hidePanel(choixUniquePanel);
        hidePanel(choixMultiplePanel);
        hidePanel(texteLibrePanel);
    }

    private boolean saveCurrentAnswer() {
        if (currentIndex >= questionList.size()) return true;
        Question q    = questionList.get(currentIndex);
        String   type = safe(q.getTypeQuestion());

        switch (type) {
            case "vrai_faux" -> {
                Toggle sel = vraiFauxGroup.getSelectedToggle();
                if (sel == null) { setText(questionErrorLabel, "⚠ Sélectionnez Vrai ou Faux."); return false; }
                studentAnswers.put(q.getId(), sel == vraiFauxVraiRadio ? "Vrai" : "Faux");
            }
            case "choix_unique" -> {
                Toggle sel = choixUniqueGroup.getSelectedToggle();
                if (sel == null) { setText(questionErrorLabel, "⚠ Sélectionnez une réponse."); return false; }
                studentAnswers.put(q.getId(), (Long) sel.getUserData());
            }
            case "choix_multiple" -> {
                if (choixMultipleContainer == null) break;
                Set<Long> selected = new HashSet<>();
                for (var node : choixMultipleContainer.getChildren())
                    if (node instanceof CheckBox cb && cb.isSelected())
                        selected.add((Long) cb.getUserData());
                if (selected.isEmpty()) { setText(questionErrorLabel, "⚠ Cochez au moins une réponse."); return false; }
                studentAnswers.put(q.getId(), selected);
            }
            case "texte_libre" -> {
                String txt = texteLibreArea != null ? texteLibreArea.getText().trim() : "";
                if (txt.isEmpty()) { setText(questionErrorLabel, "⚠ Rédigez votre réponse."); return false; }
                studentAnswers.put(q.getId(), txt);
            }
        }
        setText(questionErrorLabel, "");
        return true;
    }

    @FXML private void prevQuestion() {
        saveCurrentAnswer();
        if (currentIndex > 0) showQuestion(currentIndex - 1);
    }

    @FXML private void nextQuestion() {
        if (!saveCurrentAnswer()) return;
        if (currentIndex < questionList.size() - 1) showQuestion(currentIndex + 1);
    }

    @FXML private void submitQuiz() {
        if (!saveCurrentAnswer()) return;
        List<Integer> manquantes = new ArrayList<>();
        for (int i = 0; i < questionList.size(); i++)
            if (!studentAnswers.containsKey(questionList.get(i).getId()))
                manquantes.add(i + 1);
        if (!manquantes.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Questions sans réponse : " + manquantes + "\nSoumettre quand même ?",
                    ButtonType.YES, ButtonType.NO);
            alert.setTitle("Confirmation");
            alert.showAndWait().ifPresent(bt -> { if (bt == ButtonType.YES) doSubmit(); });
        } else {
            doSubmit();
        }
    }

    @FXML private void abandonQuiz() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Abandonner ce quiz ? Votre progression sera perdue.",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Abandon");
        alert.showAndWait().ifPresent(bt -> { if (bt == ButtonType.YES) { stopTimer(); showQuizListPage(); } });
    }

    // ═════════════════════════════════════════════════════════════
    // SOUMISSION & CALCUL DU SCORE
    // ═════════════════════════════════════════════════════════════
    private void doSubmit() {
        stopTimer();
        try {
            Utilisateur etudiant = UserSession.getCurrentUser();
            if (etudiant == null) throw new Exception("Aucun utilisateur connecté.");

            int totalPoints  = 0;
            int earnedPoints = 0;
            List<Map<String, Object>> detailsJson = new ArrayList<>();

            for (Question q : questionList) {
                int pts = q.getPoints() != null ? q.getPoints() : 0;
                totalPoints += pts;

                List<Reponse> reponses       = reponseService.getByQuestion(q.getId());
                Object        studentAnswer  = studentAnswers.get(q.getId());
                boolean       isCorrect      = false;
                String        studentAnswerStr = "";
                String        correctAnswerStr = "";

                switch (safe(q.getTypeQuestion())) {
                    case "vrai_faux" -> {
                        Reponse correct = reponses.stream()
                                .filter(r -> Boolean.TRUE.equals(r.getEstCorrecte())).findFirst().orElse(null);
                        correctAnswerStr = correct != null ? safe(correct.getTexteReponse()) : "?";
                        studentAnswerStr = studentAnswer instanceof String s ? s : "(aucune)";
                        isCorrect        = correctAnswerStr.equalsIgnoreCase(studentAnswerStr);
                        if (isCorrect) earnedPoints += pts;
                    }
                    case "choix_unique" -> {
                        Reponse correct = reponses.stream()
                                .filter(r -> Boolean.TRUE.equals(r.getEstCorrecte())).findFirst().orElse(null);
                        correctAnswerStr = correct != null ? safe(correct.getTexteReponse()) : "?";
                        if (studentAnswer instanceof Long selectedId) {
                            Reponse chosen = reponses.stream()
                                    .filter(r -> r.getId().equals(selectedId)).findFirst().orElse(null);
                            studentAnswerStr = chosen != null ? safe(chosen.getTexteReponse()) : "(inconnue)";
                            isCorrect        = chosen != null && Boolean.TRUE.equals(chosen.getEstCorrecte());
                        } else {
                            studentAnswerStr = "(aucune)";
                        }
                        if (isCorrect) earnedPoints += pts;
                    }
                    case "choix_multiple" -> {
                        Set<Long> correctIds = new HashSet<>();
                        List<String> correctTexts = new ArrayList<>();
                        for (Reponse r : reponses)
                            if (Boolean.TRUE.equals(r.getEstCorrecte())) {
                                correctIds.add(r.getId());
                                correctTexts.add(safe(r.getTexteReponse()));
                            }
                        correctAnswerStr = String.join(", ", correctTexts);
                        @SuppressWarnings("unchecked")
                        Set<Long> selectedIds = studentAnswer instanceof Set s ? s : new HashSet<>();
                        List<String> selectedTexts = new ArrayList<>();
                        for (Reponse r : reponses)
                            if (selectedIds.contains(r.getId())) selectedTexts.add(safe(r.getTexteReponse()));
                        studentAnswerStr = selectedTexts.isEmpty() ? "(aucune)" : String.join(", ", selectedTexts);
                        isCorrect        = selectedIds.equals(correctIds);
                        if (isCorrect) earnedPoints += pts;
                    }
                    case "texte_libre" -> {
                        Reponse correct = reponses.stream()
                                .filter(r -> Boolean.TRUE.equals(r.getEstCorrecte())).findFirst().orElse(null);
                        correctAnswerStr = correct != null ? safe(correct.getTexteReponse()) : "(correction manuelle)";
                        studentAnswerStr = studentAnswer instanceof String s ? s : "(aucune)";
                        isCorrect        = !correctAnswerStr.isBlank()
                                && correctAnswerStr.equalsIgnoreCase(studentAnswerStr.trim());
                        if (isCorrect) earnedPoints += pts;
                    }
                }

                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("questionId",    q.getId());
                detail.put("questionTexte", safe(q.getTexte()));
                detail.put("studentAnswer", studentAnswerStr);
                detail.put("correctAnswer", correctAnswerStr);
                detail.put("isCorrect",     isCorrect);
                detail.put("points",        pts);
                detail.put("earnedPoints",  isCorrect ? pts : 0);
                detailsJson.add(detail);
            }

            double score = totalPoints > 0 ? (earnedPoints * 100.0 / totalPoints) : 0.0;

            // Créer et persister le résultat
            ResultatQuiz resultat = new ResultatQuiz();
            resultat.setQuiz(currentQuiz);
            resultat.setIdEtudiant(etudiant.getId().intValue());
            resultat.setScore(score);
            resultat.setTotalPoints(totalPoints);
            resultat.setEarnedPoints(earnedPoints);
            resultat.setDatePassation(LocalDateTime.now());
            resultat.setReponsesEtudiant(toJson(detailsJson));
            resultatService.create(resultat);
            resultatCourant = resultat;

            // ── Notification Mercure → Enseignant propriétaire du quiz ──
            try {
                long idProprietaire = 0L;
                Quiz quizComplet = quizSvc.getById(currentQuiz.getId().intValue());
                if (quizComplet != null && quizComplet.getCreateur() != null) {
                    idProprietaire = quizComplet.getCreateur().getId();
                } else if (currentQuiz.getCreateur() != null) {
                    idProprietaire = currentQuiz.getCreateur().getId();
                }

                mercureService.notifierProfQuizSoumis(
                        idProprietaire,
                        etudiant.getNomComplet(),
                        currentQuiz.getTitre(),
                        score, earnedPoints, totalPoints
                );
            } catch (Exception ex) {
                System.err.println("Mercure (quiz) : " + ex.getMessage());
            }

            // ── Email de résultat (asynchrone) ────────────────────
            emailService.envoyerResultatAsync(
                    etudiant.getEmail(),
                    etudiant.getNomComplet(),
                    currentQuiz.getTitre(),
                    earnedPoints,
                    totalPoints,
                    (int) Math.round(score),
                    resultat.getDatePassation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );

            showResultats(resultat, detailsJson);

        } catch (Exception e) {
            setStatus(questionErrorLabel, "Erreur soumission : " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    // ═════════════════════════════════════════════════════════════
    // PAGE 3 — RÉSULTATS
    // ═════════════════════════════════════════════════════════════
    private void showResultats(ResultatQuiz resultat, List<Map<String, Object>> details) {
        showOnly(resultatsSection);
        resultatCourant = resultat;
        setText(sectionTitleLabel,    "Résultats");
        setText(sectionSubtitleLabel, "Quiz terminé — consultez votre score ci-dessous.");

        double score = resultat.getScore();
        setText(resultatScoreLabel,  String.format("%.1f%%", score));
        setText(resultatPointsLabel, resultat.getEarnedPoints() + " / " + resultat.getTotalPoints() + " points");
        setText(resultatDateLabel,   resultat.getDatePassation() != null
                ? resultat.getDatePassation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");

        String msg;
        if      (score >= 90) msg = "🎉 Excellent ! Félicitations !";
        else if (score >= 75) msg = "👍 Très bien !";
        else if (score >= 60) msg = "✅ Bien. Continuez vos efforts !";
        else if (score >= 50) msg = "📚 Passable. Révisez les points faibles.";
        else                  msg = "❌ À améliorer. Relisez le cours.";
        setText(resultatMsgLabel, msg);

        if (resultatDetailsContainer != null) {
            resultatDetailsContainer.getChildren().clear();
            for (Map<String, Object> d : details) {
                boolean correct = Boolean.TRUE.equals(d.get("isCorrect"));
                VBox card = new VBox(6);
                card.getStyleClass().add(correct ? "resultat-correct-card" : "resultat-incorrect-card");
                card.setPadding(new Insets(12));

                Label qLabel = new Label((correct ? "✅ " : "❌ ") + d.get("questionTexte"));
                qLabel.setWrapText(true); qLabel.getStyleClass().add("detail-hero-title");

                Label yourLabel = new Label("Votre réponse : " + d.get("studentAnswer"));
                yourLabel.setWrapText(true); yourLabel.getStyleClass().add("detail-value");

                Label corrLabel = new Label("Bonne réponse : " + d.get("correctAnswer"));
                corrLabel.setWrapText(true);
                corrLabel.getStyleClass().add(correct ? "detail-value" : "field-error-label");

                Label ptsLabel = new Label("Points : " + d.get("earnedPoints") + " / " + d.get("points"));
                ptsLabel.getStyleClass().add("detail-key");

                card.getChildren().addAll(qLabel, yourLabel, corrLabel, ptsLabel);
                resultatDetailsContainer.getChildren().add(card);
            }
        }
    }

    @FXML private void exporterMaCorrection() {
        if (resultatCourant == null) return;
        Utilisateur u = UserSession.getCurrentUser();
        if (!(u instanceof Etudiant etudiant)) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer ma correction");
        fc.setInitialFileName("correction_" + resultatCourant.getQuiz().getTitre()
                + "_" + etudiant.getMatricule() + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File fichier = fc.showSaveDialog(null);
        if (fichier != null)
            pdfService.exporterCorrectionEtudiant(resultatCourant, etudiant, fichier.getAbsolutePath());
    }

    @FXML private void retourListeQuiz() { showQuizListPage(); }

    // ═════════════════════════════════════════════════════════════
    // FILTRES
    // ═════════════════════════════════════════════════════════════
    private void applyFilters() {
        if (filteredQuizList == null) return;
        String search   = searchField    != null ? searchField.getText().trim().toLowerCase() : "";
        String typeVal  = filterTypeCombo  != null ? filterTypeCombo.getValue()  : "Tous les types";
        String dureeVal = filterDureeCombo != null ? filterDureeCombo.getValue() : "Toutes durées";

        filteredQuizList.setPredicate(quiz -> {
            if (!search.isEmpty()) {
                boolean m = (quiz.getTitre()       != null && quiz.getTitre().toLowerCase().contains(search))
                        || (quiz.getTypeQuiz()    != null && quiz.getTypeQuiz().toLowerCase().contains(search))
                        || (quiz.getDescription() != null && quiz.getDescription().toLowerCase().contains(search));
                if (!m) return false;
            }
            if (typeVal != null && !"Tous les types".equals(typeVal))
                if (quiz.getTypeQuiz() == null || !quiz.getTypeQuiz().equalsIgnoreCase(typeVal)) return false;
            if (dureeVal != null && !"Toutes durées".equals(dureeVal)) {
                int d = quiz.getDureeMinutes() != null ? quiz.getDureeMinutes() : 0;
                switch (dureeVal) {
                    case "≤ 15 min" -> { if (d > 15)  return false; }
                    case "≤ 30 min" -> { if (d > 30)  return false; }
                    case "≤ 60 min" -> { if (d > 60)  return false; }
                    case "> 60 min" -> { if (d <= 60) return false; }
                }
            }
            return true;
        });

        int total = quizObsList.size(), affiches = filteredQuizList.size();
        if (filterResultLabel != null)
            setText(filterResultLabel, affiches == total
                    ? total + " quiz disponible(s)"
                    : affiches + " résultat(s) sur " + total);
    }

    @FXML private void resetFilters() {
        if (searchField      != null) searchField.clear();
        if (filterTypeCombo  != null) filterTypeCombo.setValue("Tous les types");
        if (filterDureeCombo != null) filterDureeCombo.setValue("Toutes durées");
        applyFilters();
    }

    // ═════════════════════════════════════════════════════════════
    // MES RÉSULTATS
    // ═════════════════════════════════════════════════════════════
    @FXML public void showMesResultats() {
        stopTimer();
        showOnly(resultatsSection);
        setText(sectionTitleLabel,    "Mes Résultats");
        setText(sectionSubtitleLabel, "Historique de vos passages de quiz.");
        if (resultatDetailsContainer != null) resultatDetailsContainer.getChildren().clear();

        try {
            Utilisateur u = UserSession.getCurrentUser();
            if (u == null) return;
            List<ResultatQuiz> resultats = resultatService.getByEtudiant(u.getId().intValue());

            setText(resultatScoreLabel,  "");
            setText(resultatPointsLabel, resultats.size() + " résultat(s) au total");
            setText(resultatDateLabel,   "");
            setText(resultatMsgLabel,    "");

            if (resultats.isEmpty()) {
                Label empty = new Label("Vous n'avez encore passé aucun quiz.");
                empty.getStyleClass().add("sidebar-copy");
                if (resultatDetailsContainer != null) resultatDetailsContainer.getChildren().add(empty);
                return;
            }

            for (ResultatQuiz r : resultats) {
                String quizTitre = "-";
                try {
                    if (r.getQuiz() != null && r.getQuiz().getId() != null) {
                        Quiz q = quizService.getById(r.getQuiz().getId().intValue());
                        if (q != null) quizTitre = q.getTitre();
                    }
                } catch (Exception ignored) {}

                VBox card = new VBox(6);
                card.getStyleClass().add(r.getScore() >= 50 ? "resultat-correct-card" : "resultat-incorrect-card");
                card.setPadding(new Insets(12));

                Label titreLabel = new Label("📋 " + quizTitre);
                titreLabel.setWrapText(true); titreLabel.getStyleClass().add("detail-hero-title");

                Label scoreLabel = new Label(String.format("Score : %.1f%%   (%d / %d pts)",
                        r.getScore(), r.getEarnedPoints(), r.getTotalPoints()));
                scoreLabel.getStyleClass().add("detail-value");

                Label dateLabel = new Label(r.getDatePassation() != null
                        ? r.getDatePassation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
                dateLabel.getStyleClass().add("detail-key");

                card.getChildren().addAll(titreLabel, scoreLabel, dateLabel);
                if (resultatDetailsContainer != null)
                    resultatDetailsContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            Label err = new Label("Erreur : " + e.getMessage());
            err.getStyleClass().add("field-error-label");
            if (resultatDetailsContainer != null) resultatDetailsContainer.getChildren().add(err);
        }
    }

    // ═════════════════════════════════════════════════════════════
    // TIMER
    // ═════════════════════════════════════════════════════════════
    private void startTimer() {
        if (countdownTimer != null) countdownTimer.stop();
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsRemaining--;
            updateTimerLabel();
            if (secondsRemaining <= 0) {
                countdownTimer.stop();
                Platform.runLater(() -> {
                    setStatus(questionErrorLabel, "⏰ Temps écoulé ! Soumission automatique.", true);
                    doSubmit();
                });
            }
        }));
        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();
        updateTimerLabel();
    }

    private void updateTimerLabel() {
        int m = secondsRemaining / 60, s = secondsRemaining % 60;
        setText(timerLabel, String.format("%02d:%02d", m, s));
        if (timerLabel != null)
            timerLabel.setStyle(secondsRemaining <= 60
                    ? "-fx-text-fill:#dc2626;-fx-font-weight:900;"
                    : "-fx-text-fill:#16a34a;-fx-font-weight:700;");
    }

    private void stopTimer() {
        if (countdownTimer != null) { countdownTimer.stop(); countdownTimer = null; }
    }

    // ═════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═════════════════════════════════════════════════════════════
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

    // JSON manuel (pas de dépendance externe)
    private String toJson(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) { if (i > 0) sb.append(","); sb.append(mapToJson(list.get(i))); }
        return sb.append("]").toString();
    }

    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{"); boolean first = true;
        for (var e : map.entrySet()) {
            if (!first) sb.append(","); first = false;
            sb.append("\"").append(escJson(e.getKey())).append("\":").append(valToJson(e.getValue()));
        }
        return sb.append("}").toString();
    }

    private String valToJson(Object v) {
        if (v == null)           return "null";
        if (v instanceof Boolean || v instanceof Number) return v.toString();
        return "\"" + escJson(v.toString()) + "\"";
    }

    private String escJson(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t");
    }

    public void setCurrentUser(String nom, String role) {
        setText(currentUserNameLabel, nom);
        setText(currentUserRoleLabel, role);
        if (profileMenuButton != null)
            profileMenuButton.setText(nom.length() >= 2 ? nom.substring(0, 2).toUpperCase() : nom.toUpperCase());
    }
}