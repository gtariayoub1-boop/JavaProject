package gui;

import entities.ForumDiscussion;
import entities.Notification;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import services.ForumDiscussionService;
import services.NotificationService;
import services.UtilisateurService;
import utils.DBConnection;
import utils.SceneManager;
import utils.UserSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ForumDiscussionController {

    // ── FXML ids du nouveau forum-dashboard.fxml ───────────────────────────────
    @FXML private TableView<ForumDiscussion> forumTable;
    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField courseIdField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> statutCombo;
    @FXML private TextField tagsField;
    @FXML private TextArea moderationArea;
    @FXML private TextField imageField;
    @FXML private TextField attachmentField;
    @FXML private Label statusLabel;
    @FXML private Button deleteButton;
    @FXML private Button saveButton;
    @FXML private Label pageTitleLabel;
    @FXML private Label pageSubtitleLabel;
    @FXML private Label roleBadgeLabel;
    @FXML private Label creatorInfoLabel;

    // ── FXML ids de l'ancien contrôleur (compatibilité) ───────────────────────
    @FXML private TextField tfRechercheForum;
    @FXML private TableView<ForumDiscussion> tvForums;
    @FXML private TableColumn<ForumDiscussion, Long> colForumId;
    @FXML private TableColumn<ForumDiscussion, String> colForumTitre;
    @FXML private TableColumn<ForumDiscussion, String> colForumCreateur;
    @FXML private TableColumn<ForumDiscussion, String> colForumType;
    @FXML private TableColumn<ForumDiscussion, String> colForumStatut;

    // Colonnes du nouveau FXML
    @FXML private TableColumn<ForumDiscussion, Long> idColumn;
    @FXML private TableColumn<ForumDiscussion, String> titreColumn;
    @FXML private TableColumn<ForumDiscussion, String> auteurColumn;
    @FXML private TableColumn<ForumDiscussion, String> typeColumn;
    @FXML private TableColumn<ForumDiscussion, String> statutColumn;
    @FXML private TableColumn<ForumDiscussion, String> activiteColumn;
    @FXML private TableColumn<ForumDiscussion, String> tagsColumn;

    @FXML private javafx.scene.layout.VBox paneList;
    @FXML private javafx.scene.layout.VBox paneForm;

    // ── Ancien FXML fields (compatibilité) ─────────────────────────────────────
    @FXML private TextField tfTitre;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbCreateur;
    @FXML private TextField tfIdCours;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<String> cbStatut;
    @FXML private TextField tfTags;
    @FXML private TextArea taReglesModeration;
    @FXML private TextField tfImageCouvertureUrl;
    @FXML private TextField tfPieceJointeUrl;

    // ── Data ───────────────────────────────────────────────────────────────────
    private javafx.collections.ObservableList<ForumDiscussion> forumsList = FXCollections.observableArrayList();

    // ── Services ───────────────────────────────────────────────────────────────
    private ForumDiscussionService forumService;
    private UtilisateurService utilisateurService;
    private NotificationService notificationService = new NotificationService();
    private Map<Long, String> idToNameMap = new HashMap<>();
    private Map<String, Long> nameToIdMap = new HashMap<>();

    // ══════════════════════════════════════════════════════════════════════════
    // INIT
    // ══════════════════════════════════════════════════════════════════════════

    public void setConnection(Connection connection) {
        this.forumService = new ForumDiscussionService(connection);
    }

    @FXML
    public void initialize() {
        if (this.forumService == null) {
            Connection cnx = DBConnection.getInstance().getConnection();
            verifyForumTable(cnx);
            this.forumService = new ForumDiscussionService(cnx);
        }
        if (this.utilisateurService == null) {
            this.utilisateurService = new UtilisateurService();
        }

        // Charger les utilisateurs
        try {
            List<Utilisateur> users = utilisateurService.getAllUtilisateurs();
            for (Utilisateur u : users) {
                String fullname = u.getNom() + " " + u.getPrenom() + " (" + u.getEmail() + ")";
                idToNameMap.put(u.getId(), fullname);
                nameToIdMap.put(fullname, u.getId());
                if (cbCreateur != null) cbCreateur.getItems().add(fullname);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Initialiser les combos (nouveau FXML)
        if (typeCombo != null) {
            typeCombo.setItems(FXCollections.observableArrayList("public", "prive"));
            typeCombo.setValue("public");
        }
        if (statutCombo != null) {
            statutCombo.setItems(FXCollections.observableArrayList("ouvert", "ferme"));
            statutCombo.setValue("ouvert");
        }

        // Initialiser les combos (ancien FXML)
        if (cbType != null) {
            cbType.setItems(FXCollections.observableArrayList("public", "prive"));
            cbType.setValue("public");
        }
        if (cbStatut != null) {
            cbStatut.setItems(FXCollections.observableArrayList("ouvert", "ferme"));
            cbStatut.setValue("ouvert");
        }

        // Utilisateur courant
        Utilisateur currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            String currentFullName = currentUser.getNom() + " " + currentUser.getPrenom() + " (" + currentUser.getEmail() + ")";
            if (cbCreateur != null) {
                cbCreateur.setValue(currentFullName);
                if (!currentUser.getType().equalsIgnoreCase("administrateur")) {
                    cbCreateur.setDisable(true);
                }
            }
            if (roleBadgeLabel != null) roleBadgeLabel.setText(currentUser.getType().toUpperCase());
            if (creatorInfoLabel != null) creatorInfoLabel.setText(currentFullName);
        }

        // Configurer le nouveau tableau (forumTable)
        if (forumTable != null) {
            configureNewTable();
            loadForums();
            forumTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
                if (newVal != null) populateNewForm(newVal);
            });
        }

        // Configurer l'ancien tableau (tvForums)
        if (tvForums != null) {
            configureOldTable();
            loadForums();
            if (tfRechercheForum != null) {
                FilteredList<ForumDiscussion> filteredData = new FilteredList<>(forumsList, b -> true);
                tfRechercheForum.textProperty().addListener((obs, oldVal, newVal) -> {
                    filteredData.setPredicate(forum -> {
                        if (newVal == null || newVal.isEmpty()) return true;
                        String lower = newVal.toLowerCase();
                        if (forum.getTitre() != null && forum.getTitre().toLowerCase().contains(lower)) return true;
                        return forum.getDescription() != null && forum.getDescription().toLowerCase().contains(lower);
                    });
                });
                SortedList<ForumDiscussion> sortedData = new SortedList<>(filteredData);
                sortedData.comparatorProperty().bind(tvForums.comparatorProperty());
                tvForums.setItems(sortedData);
            }
            tvForums.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
                if (newVal != null) populateForm(newVal);
            });
        }
    }

    private void configureNewTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (titreColumn != null) titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        if (auteurColumn != null) {
            auteurColumn.setCellValueFactory(cellData -> {
                Long createurId = cellData.getValue().getCreateurId();
                String nom = idToNameMap.getOrDefault(createurId, "Inconnu (" + createurId + ")");
                return new SimpleStringProperty(nom);
            });
        }
        if (typeColumn != null) typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        if (statutColumn != null) statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        if (activiteColumn != null) {
            activiteColumn.setCellValueFactory(cellData -> {
                LocalDateTime activite = cellData.getValue().getDerniereActivite();
                return new SimpleStringProperty(activite != null ? activite.toString() : "-");
            });
        }
        if (tagsColumn != null) {
            tagsColumn.setCellValueFactory(cellData -> {
                List<String> tags = cellData.getValue().getTags();
                return new SimpleStringProperty(tags != null ? String.join(", ", tags) : "");
            });
        }
        forumTable.setItems(forumsList);
    }

    private void configureOldTable() {
        if (colForumId != null) colForumId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colForumTitre != null) colForumTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        if (colForumCreateur != null) {
            colForumCreateur.setCellValueFactory(cellData -> {
                Long createurId = cellData.getValue().getCreateurId();
                String nom = idToNameMap.getOrDefault(createurId, "Inconnu (" + createurId + ")");
                return new SimpleStringProperty(nom);
            });
        }
        if (colForumType != null) colForumType.setCellValueFactory(new PropertyValueFactory<>("type"));
        if (colForumStatut != null) colForumStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        tvForums.setItems(forumsList);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CHARGEMENT
    // ══════════════════════════════════════════════════════════════════════════

    private void loadForums() {
        try {
            forumsList.clear();
            forumsList.addAll(forumService.getAll());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HANDLERS NOUVEAU FXML
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleRefresh() {
        loadForums();
        setStatus("Discussions actualisées.", false);
    }

    @FXML
    private void handleClearForm() {
        viderNouveauForm();
        setStatus("Formulaire vidé.", false);
    }

    @FXML
    private void handleSaveForum() {
        ForumDiscussion selected = forumTable != null
                ? forumTable.getSelectionModel().getSelectedItem() : null;
        if (selected != null) {
            modifierForumNouveauFxml(selected.getId());
        } else {
            ajouterForumNouveauFxml();
        }
    }

    @FXML
    private void handleDeleteForum() {
        TableView<ForumDiscussion> table = forumTable != null ? forumTable : tvForums;
        if (table == null) return;
        ForumDiscussion selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Sélectionnez un forum à supprimer.", true);
            return;
        }
        try {
            forumService.supprimer(selected.getId());
            loadForums();
            viderNouveauForm();
            setStatus("Forum supprimé avec succès.", false);
        } catch (SQLException e) {
            setStatus("Erreur SQL : " + e.getMessage(), true);
        }
    }

    private void ajouterForumNouveauFxml() {
        String erreurs = validerNouveauForm();
        if (!erreurs.isEmpty()) { setStatus(erreurs, true); return; }
        try {
            ForumDiscussion forum = buildForumFromNewForm();
            forumService.ajouter(forum);
            envoyerNotification(forum.getTitre(), "créé");
            loadForums();
            viderNouveauForm();
            setStatus("Forum publié avec succès.", false);
        } catch (SQLException e) { setStatus("Erreur SQL : " + e.getMessage(), true);
        } catch (Exception e) { setStatus("Erreur : " + e.getMessage(), true); }
    }

    private void modifierForumNouveauFxml(long id) {
        String erreurs = validerNouveauForm();
        if (!erreurs.isEmpty()) { setStatus(erreurs, true); return; }
        try {
            ForumDiscussion forum = buildForumFromNewForm();
            forum.setId(id);
            forum.setEstModifie(true);
            forum.setDateModification(LocalDateTime.now());
            forumService.modifier(forum);
            loadForums();
            setStatus("Forum mis à jour avec succès.", false);
        } catch (SQLException e) { setStatus("Erreur SQL : " + e.getMessage(), true);
        } catch (Exception e) { setStatus("Erreur : " + e.getMessage(), true); }
    }

    private ForumDiscussion buildForumFromNewForm() {
        ForumDiscussion forum = new ForumDiscussion();
        forum.setTitre(getTitre());
        forum.setDescription(getDescription());
        forum.setDateCreation(LocalDateTime.now());
        forum.setDerniereActivite(LocalDateTime.now());
        forum.setType(getType());
        forum.setStatut(getStatut());

        Utilisateur currentUser = UserSession.getCurrentUser();
        Long idCreateur = currentUser != null ? currentUser.getId() : -1L;
        if (cbCreateur != null && cbCreateur.getValue() != null) {
            Long mapped = nameToIdMap.get(cbCreateur.getValue());
            if (mapped != null) idCreateur = mapped;
        }
        forum.setCreateurId(idCreateur);

        String idCoursStr = getCourseId();
        if (!idCoursStr.isEmpty()) {
            try { forum.setIdCours(Integer.parseInt(idCoursStr)); } catch (NumberFormatException ignored) {}
        }

        String tagsStr = getTags();
        if (!tagsStr.isEmpty()) {
            forum.setTags(Arrays.stream(tagsStr.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        }

        String regles = getModeration();
        if (!regles.isEmpty()) forum.setReglesModeration(regles);

        String img = getImage();
        if (!img.isEmpty()) forum.setImageCouvertureUrl(img);

        String pj = getAttachment();
        if (!pj.isEmpty()) forum.setPieceJointeUrl(pj);

        return forum;
    }

    private void populateNewForm(ForumDiscussion forum) {
        setTitre(forum.getTitre());
        setDescription(forum.getDescription());
        setCourseId(forum.getIdCours() != null ? String.valueOf(forum.getIdCours()) : "");
        setType(forum.getType());
        setStatut(forum.getStatut());
        setTags(forum.getTags() != null ? String.join(", ", forum.getTags()) : "");
        setModeration(forum.getReglesModeration() != null ? forum.getReglesModeration() : "");
        setImage(forum.getImageCouvertureUrl() != null ? forum.getImageCouvertureUrl() : "");
        setAttachment(forum.getPieceJointeUrl() != null ? forum.getPieceJointeUrl() : "");
        if (statusLabel != null) statusLabel.setText("");
    }

    private void viderNouveauForm() {
        setTitre(""); setDescription(""); setCourseId("");
        setType("public"); setStatut("ouvert");
        setTags(""); setModeration(""); setImage(""); setAttachment("");
        if (forumTable != null) forumTable.getSelectionModel().clearSelection();
    }

    private void setStatus(String message, boolean isError) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setStyle(isError ? "-fx-text-fill: #ef4444;" : "-fx-text-fill: #22c55e;");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HANDLERS ANCIEN FXML (compatibilité)
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    public void goBack() {
        try {
            Utilisateur user = UserSession.getCurrentUser();
            if (user != null) {
                switch (user.getType().toLowerCase()) {
                    case "administrateur": SceneManager.switchScene("/gui/admin-dashboard.fxml", "Admin Dashboard"); break;
                    case "enseignant": SceneManager.switchScene("/gui/teacher-dashboard.fxml", "Teacher Dashboard"); break;
                    case "etudiant": SceneManager.switchScene("/gui/student-dashboard.fxml", "Student Dashboard"); break;
                    default: SceneManager.switchScene("/gui/admin-dashboard.fxml", "Admin Dashboard");
                }
            } else {
                SceneManager.switchScene("/gui/admin-dashboard.fxml", "Admin Dashboard");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void showFormCreate() {
        viderChamps();
        if (tvForums != null) tvForums.getSelectionModel().clearSelection();
        if (paneList != null) paneList.setVisible(false);
        if (paneForm != null) paneForm.setVisible(true);
    }

    @FXML
    public void showFormEdit() {
        if (tvForums == null) return;
        ForumDiscussion selected = tvForums.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner un forum à modifier.");
            return;
        }
        populateForm(selected);
        if (paneList != null) paneList.setVisible(false);
        if (paneForm != null) paneForm.setVisible(true);
    }

    @FXML
    public void hideForm() {
        if (paneForm != null) paneForm.setVisible(false);
        if (paneList != null) paneList.setVisible(true);
        if (tvForums != null) tvForums.getSelectionModel().clearSelection();
        viderChamps();
    }

    @FXML
    public void ajouterForum() {
        try {
            String erreurs = validerSaisie();
            if (!erreurs.isEmpty()) { showAlert(Alert.AlertType.ERROR, "Erreurs de saisie", erreurs); return; }

            ForumDiscussion forum = new ForumDiscussion();
            forum.setTitre(tfTitre != null ? tfTitre.getText().trim() : "");
            forum.setDescription(taDescription != null ? taDescription.getText().trim() : "");

            Long idCreateur = UserSession.getCurrentUser().getId();
            if (cbCreateur != null && cbCreateur.getValue() != null) {
                Long mapped = nameToIdMap.get(cbCreateur.getValue());
                if (mapped != null) idCreateur = mapped;
            }
            forum.setCreateurId(idCreateur);
            forum.setDateCreation(LocalDateTime.now());
            forum.setDerniereActivite(LocalDateTime.now());
            forum.setType(cbType != null ? cbType.getValue() : "public");
            forum.setStatut(cbStatut != null ? cbStatut.getValue() : "ouvert");

            if (tfIdCours != null && !tfIdCours.getText().trim().isEmpty()) {
                forum.setIdCours(Integer.parseInt(tfIdCours.getText().trim()));
            }
            if (tfTags != null && !tfTags.getText().trim().isEmpty()) {
                forum.setTags(Arrays.stream(tfTags.getText().split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
            }
            if (taReglesModeration != null && !taReglesModeration.getText().trim().isEmpty()) forum.setReglesModeration(taReglesModeration.getText().trim());
            if (tfImageCouvertureUrl != null && !tfImageCouvertureUrl.getText().trim().isEmpty()) forum.setImageCouvertureUrl(tfImageCouvertureUrl.getText().trim());
            if (tfPieceJointeUrl != null && !tfPieceJointeUrl.getText().trim().isEmpty()) forum.setPieceJointeUrl(tfPieceJointeUrl.getText().trim());

            forumService.ajouter(forum);
            envoyerNotification(forum.getTitre(), "créé");
            if (tvForums != null) loadForums();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Forum ajouté avec succès.");
            hideForm();
        } catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage()); }
    }

    @FXML
    public void modifierForum() {
        if (tvForums == null || tvForums.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun forum n'est sélectionné."); return;
        }
        try {
            String erreurs = validerSaisie();
            if (!erreurs.isEmpty()) { showAlert(Alert.AlertType.ERROR, "Erreurs de saisie", erreurs); return; }

            long id = tvForums.getSelectionModel().getSelectedItem().getId();
            Utilisateur currentUser = UserSession.getCurrentUser();
            boolean isAdmin = currentUser != null && currentUser.getType().equalsIgnoreCase("administrateur");
            ForumDiscussion fCheck = forumsList.stream().filter(f -> f.getId() == id).findFirst().orElse(null);

            if (fCheck != null && !isAdmin && fCheck.getCreateurId() != currentUser.getId()) {
                showAlert(Alert.AlertType.ERROR, "Accès refusé", "Vous ne pouvez modifier que vos propres forums."); return;
            }

            ForumDiscussion forum = new ForumDiscussion();
            forum.setId(id);
            forum.setTitre(tfTitre != null ? tfTitre.getText().trim() : "");
            forum.setDescription(taDescription != null ? taDescription.getText().trim() : "");

            Long idCreateur = fCheck != null ? fCheck.getCreateurId() : currentUser.getId();
            if (isAdmin && cbCreateur != null && cbCreateur.getValue() != null) {
                Long mapped = nameToIdMap.get(cbCreateur.getValue());
                if (mapped != null) idCreateur = mapped;
            }
            forum.setCreateurId(idCreateur);
            forum.setDateCreation(LocalDateTime.now());
            forum.setDerniereActivite(LocalDateTime.now());
            forum.setType(cbType != null ? cbType.getValue() : "public");
            forum.setStatut(cbStatut != null ? cbStatut.getValue() : "ouvert");

            if (tfIdCours != null && !tfIdCours.getText().trim().isEmpty()) forum.setIdCours(Integer.parseInt(tfIdCours.getText().trim()));
            if (tfTags != null && !tfTags.getText().trim().isEmpty()) forum.setTags(Arrays.stream(tfTags.getText().split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
            if (taReglesModeration != null && !taReglesModeration.getText().trim().isEmpty()) forum.setReglesModeration(taReglesModeration.getText().trim());
            if (tfImageCouvertureUrl != null && !tfImageCouvertureUrl.getText().trim().isEmpty()) forum.setImageCouvertureUrl(tfImageCouvertureUrl.getText().trim());
            if (tfPieceJointeUrl != null && !tfPieceJointeUrl.getText().trim().isEmpty()) forum.setPieceJointeUrl(tfPieceJointeUrl.getText().trim());
            forum.setEstModifie(true);
            forum.setDateModification(LocalDateTime.now());

            forumService.modifier(forum);
            if (tvForums != null) loadForums();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Forum modifié avec succès.");
            hideForm();
        } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage()); }
    }

    @FXML
    public void supprimerForum() {
        TableView<ForumDiscussion> table = tvForums != null ? tvForums : forumTable;
        if (table == null) return;

        long idToDelete = -1;
        boolean isSelected = false;

        if (table.getSelectionModel().getSelectedItem() != null) {
            isSelected = true;
            idToDelete = table.getSelectionModel().getSelectedItem().getId();
        }

        if (!isSelected) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Suppression"); dialog.setHeaderText("Supprimer un forum");
            dialog.setContentText("Entrer l'ID du forum à supprimer :");
            java.util.Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                try { idToDelete = Long.parseLong(result.get().trim()); isSelected = true; }
                catch (NumberFormatException e) { showAlert(Alert.AlertType.ERROR, "Erreur", "L'ID doit être numérique."); }
            }
        }

        if (isSelected) {
            try {
                long finalId = idToDelete;
                Utilisateur currentUser = UserSession.getCurrentUser();
                boolean isAdmin = currentUser != null && currentUser.getType().equalsIgnoreCase("administrateur");
                ForumDiscussion fCheck = forumsList.stream().filter(f -> f.getId() == finalId).findFirst().orElse(null);
                if (fCheck != null && !isAdmin && fCheck.getCreateurId() != currentUser.getId()) {
                    showAlert(Alert.AlertType.ERROR, "Accès refusé", "Vous ne pouvez supprimer que vos propres forums."); return;
                }
                forumService.supprimer(idToDelete);
                loadForums();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Forum supprimé avec succès.");
            } catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage()); }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private void populateForm(ForumDiscussion forum) {
        if (tfTitre != null) tfTitre.setText(forum.getTitre());
        if (taDescription != null) taDescription.setText(forum.getDescription());
        if (cbCreateur != null) { String name = idToNameMap.get(forum.getCreateurId()); if (name != null) cbCreateur.setValue(name); }
        if (tfIdCours != null) { if (forum.getIdCours() != null) tfIdCours.setText(String.valueOf(forum.getIdCours())); else tfIdCours.clear(); }
        if (cbType != null) cbType.setValue(forum.getType());
        if (cbStatut != null) cbStatut.setValue(forum.getStatut());
        if (tfTags != null) { if (forum.getTags() != null) tfTags.setText(String.join(", ", forum.getTags())); else tfTags.clear(); }
        if (taReglesModeration != null) { if (forum.getReglesModeration() != null) taReglesModeration.setText(forum.getReglesModeration()); else taReglesModeration.clear(); }
        if (tfImageCouvertureUrl != null) { if (forum.getImageCouvertureUrl() != null) tfImageCouvertureUrl.setText(forum.getImageCouvertureUrl()); else tfImageCouvertureUrl.clear(); }
        if (tfPieceJointeUrl != null) { if (forum.getPieceJointeUrl() != null) tfPieceJointeUrl.setText(forum.getPieceJointeUrl()); else tfPieceJointeUrl.clear(); }
    }

    private void viderChamps() {
        if (tfTitre != null) tfTitre.clear();
        if (taDescription != null) taDescription.clear();
        if (tfIdCours != null) tfIdCours.clear();
        if (tfTags != null) tfTags.clear();
        if (taReglesModeration != null) taReglesModeration.clear();
        if (tfImageCouvertureUrl != null) tfImageCouvertureUrl.clear();
        if (tfPieceJointeUrl != null) tfPieceJointeUrl.clear();
        if (cbType != null) cbType.setValue("public");
        if (cbStatut != null) cbStatut.setValue("ouvert");
        Utilisateur currentUser = UserSession.getCurrentUser();
        if (currentUser != null && cbCreateur != null) {
            String name = currentUser.getNom() + " " + currentUser.getPrenom() + " (" + currentUser.getEmail() + ")";
            cbCreateur.setValue(name);
        }
    }

    private String validerSaisie() {
        StringBuilder erreurs = new StringBuilder();
        String titre = tfTitre != null ? tfTitre.getText().trim() : "";
        String description = taDescription != null ? taDescription.getText().trim() : "";
        if (titre.isEmpty()) erreurs.append("- Le titre est obligatoire.\n");
        else if (titre.length() < 3) erreurs.append("- Le titre doit contenir au moins 3 caractères.\n");
        else if (titre.length() > 255) erreurs.append("- Le titre ne doit pas dépasser 255 caractères.\n");
        if (description.isEmpty()) erreurs.append("- La description est obligatoire.\n");
        else if (description.length() < 10) erreurs.append("- La description doit contenir au moins 10 caractères.\n");
        return erreurs.toString();
    }

    private String validerNouveauForm() {
        StringBuilder erreurs = new StringBuilder();
        String titre = getTitre();
        String description = getDescription();
        if (titre.isEmpty()) erreurs.append("Le titre est obligatoire. ");
        else if (titre.length() < 3) erreurs.append("Le titre doit contenir au moins 3 caractères. ");
        if (description.isEmpty()) erreurs.append("La description est obligatoire. ");
        else if (description.length() < 10) erreurs.append("La description doit contenir au moins 10 caractères. ");
        return erreurs.toString();
    }

    private void envoyerNotification(String titre, String action) {
        try {
            Utilisateur currentUser = UserSession.getCurrentUser();
            if (currentUser != null) {
                Notification notif = new Notification(currentUser.getId(), "Forum " + action,
                        "Votre forum '" + titre + "' a été " + action + " avec succès.");
                notificationService.ajouter(notif);
            }
        } catch (Exception ignored) {}
    }

    private void verifyForumTable(Connection connection) {
        try (java.sql.Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE forum_discussion ADD COLUMN IF NOT EXISTS derniere_activite TIMESTAMP NULL");
            stmt.execute("ALTER TABLE forum_discussion ADD COLUMN IF NOT EXISTS tags VARCHAR(255) DEFAULT ''");
            stmt.execute("ALTER TABLE forum_discussion ADD COLUMN IF NOT EXISTS regles_moderation TEXT NULL");
            stmt.execute("ALTER TABLE forum_discussion ADD COLUMN IF NOT EXISTS image_couverture_url VARCHAR(255) NULL");
            stmt.execute("ALTER TABLE forum_discussion ADD COLUMN IF NOT EXISTS piece_jointe_url VARCHAR(255) NULL");
            stmt.execute("ALTER TABLE forum_discussion ADD COLUMN IF NOT EXISTS likes INT DEFAULT 0");
            stmt.execute("ALTER TABLE forum_discussion ADD COLUMN IF NOT EXISTS dislikes INT DEFAULT 0");
            stmt.execute("ALTER TABLE forum_discussion ADD COLUMN IF NOT EXISTS signalements INT DEFAULT 0");
            stmt.execute("ALTER TABLE forum_discussion ADD COLUMN IF NOT EXISTS est_modifie BOOLEAN DEFAULT FALSE");
            stmt.execute("ALTER TABLE forum_discussion ADD COLUMN IF NOT EXISTS date_modification TIMESTAMP NULL");
        } catch (Exception e) { System.err.println("Note migration: " + e.getMessage()); }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }

    // ── Getters/Setters pour les deux FXML ────────────────────────────────────
    private String getTitre() { return titreField != null ? titreField.getText().trim() : (tfTitre != null ? tfTitre.getText().trim() : ""); }
    private String getDescription() { return descriptionArea != null ? descriptionArea.getText().trim() : (taDescription != null ? taDescription.getText().trim() : ""); }
    private String getCourseId() { return courseIdField != null ? courseIdField.getText().trim() : (tfIdCours != null ? tfIdCours.getText().trim() : ""); }
    private String getType() { return typeCombo != null && typeCombo.getValue() != null ? typeCombo.getValue() : (cbType != null && cbType.getValue() != null ? cbType.getValue() : "public"); }
    private String getStatut() { return statutCombo != null && statutCombo.getValue() != null ? statutCombo.getValue() : (cbStatut != null && cbStatut.getValue() != null ? cbStatut.getValue() : "ouvert"); }
    private String getTags() { return tagsField != null ? tagsField.getText().trim() : (tfTags != null ? tfTags.getText().trim() : ""); }
    private String getModeration() { return moderationArea != null ? moderationArea.getText().trim() : (taReglesModeration != null ? taReglesModeration.getText().trim() : ""); }
    private String getImage() { return imageField != null ? imageField.getText().trim() : (tfImageCouvertureUrl != null ? tfImageCouvertureUrl.getText().trim() : ""); }
    private String getAttachment() { return attachmentField != null ? attachmentField.getText().trim() : (tfPieceJointeUrl != null ? tfPieceJointeUrl.getText().trim() : ""); }

    private void setTitre(String v) { if (titreField != null) titreField.setText(v); if (tfTitre != null) tfTitre.setText(v); }
    private void setDescription(String v) { if (descriptionArea != null) descriptionArea.setText(v); if (taDescription != null) taDescription.setText(v); }
    private void setCourseId(String v) { if (courseIdField != null) courseIdField.setText(v); if (tfIdCours != null) tfIdCours.setText(v); }
    private void setType(String v) { if (typeCombo != null) typeCombo.setValue(v); if (cbType != null) cbType.setValue(v); }
    private void setStatut(String v) { if (statutCombo != null) statutCombo.setValue(v); if (cbStatut != null) cbStatut.setValue(v); }
    private void setTags(String v) { if (tagsField != null) tagsField.setText(v); if (tfTags != null) tfTags.setText(v); }
    private void setModeration(String v) { if (moderationArea != null) moderationArea.setText(v); if (taReglesModeration != null) taReglesModeration.setText(v); }
    private void setImage(String v) { if (imageField != null) imageField.setText(v); if (tfImageCouvertureUrl != null) tfImageCouvertureUrl.setText(v); }
    private void setAttachment(String v) { if (attachmentField != null) attachmentField.setText(v); if (tfPieceJointeUrl != null) tfPieceJointeUrl.setText(v); }
}