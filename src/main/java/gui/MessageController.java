package gui;

import entities.Message;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.MessageService;
import services.NotificationService;
import services.AIService;
import services.SignalementSpamService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.application.Platform;
import java.util.concurrent.CompletableFuture;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import services.UtilisateurService;

public class MessageController {

    @FXML private TextField tfRechercheMessage;
    @FXML private TableView<Message> tvMessages;
    @FXML private TableColumn<Message, Long> colMessageId;
    @FXML private TableColumn<Message, String> colMessageExpediteur;
    @FXML private TableColumn<Message, String> colMessageDestinataire;
    @FXML private TableColumn<Message, String> colMessageObjet;
    @FXML private TableColumn<Message, String> colMessageDate;
    private javafx.collections.ObservableList<Message> messagesList = FXCollections.observableArrayList();

    @FXML private javafx.scene.layout.VBox paneList;
    @FXML private javafx.scene.layout.VBox paneForm;

    private int lastMessageCount = 0;
    private NotificationService notificationService = new NotificationService();
    private final AIService aiService = new AIService();
    private final SignalementSpamService signalementSpamService = new SignalementSpamService();

    @FXML
    private TextField tfExpediteurNom;

    @FXML
    private ComboBox<String> cbDestinataire;

    @FXML
    private TextField tfObjet;

    @FXML
    private TextArea taContenu;

    @FXML
    private ComboBox<String> cbStatut;

    @FXML
    private ComboBox<String> cbPriorite;

    @FXML private ComboBox<String> cbCategorie;
    @FXML private TextField tfParentId;
    @FXML private TextField tfPieceJointeUrl;

    @FXML private javafx.scene.layout.HBox aiSuggestionsContainer;
    @FXML private javafx.scene.layout.HBox aiButtonsBox;

    private MessageService messageService;
    private UtilisateurService utilisateurService;
    private Map<Long, String> idToNameMap = new HashMap<>();
    private Map<String, Long> nameToIdMap = new HashMap<>();

    public void setConnection(Connection connection) {
        this.messageService = new MessageService(connection);
    }

    @FXML
    public void goBack() {
        try {
            entities.Utilisateur user = utils.UserSession.getCurrentUser();
            if (user != null) {
                switch (user.getType().toLowerCase()) {
                    case "administrateur": utils.SceneManager.switchScene("/admin-dashboard.fxml", "Admin Dashboard"); break;
                    case "enseignant": utils.SceneManager.switchScene("/teacher-dashboard.fxml", "Teacher Dashboard"); break;
                    case "etudiant": utils.SceneManager.switchScene("/student-dashboard.fxml", "Student Dashboard"); break;
                    default: utils.SceneManager.switchScene("/admin-dashboard.fxml", "Admin Dashboard");
                }
            } else {
                utils.SceneManager.switchScene("/gui/admin-dashboard.fxml", "Admin Dashboard");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void verifyMessageTable(Connection connection) {
        try {
            try (java.sql.Statement stmt = connection.createStatement()) {
                stmt.execute("ALTER TABLE message ADD COLUMN IF NOT EXISTS priorite VARCHAR(50) DEFAULT 'normal'");
                stmt.execute("ALTER TABLE message ADD COLUMN IF NOT EXISTS piece_jointe_url VARCHAR(255) NULL");
                stmt.execute("ALTER TABLE message ADD COLUMN IF NOT EXISTS parent_id BIGINT NULL");
                stmt.execute("ALTER TABLE message ADD COLUMN IF NOT EXISTS categorie VARCHAR(50) DEFAULT 'personnel'");
                stmt.execute("ALTER TABLE message ADD COLUMN IF NOT EXISTS est_archive_expediteur BOOLEAN DEFAULT FALSE");
                stmt.execute("ALTER TABLE message ADD COLUMN IF NOT EXISTS est_archive_destinataire BOOLEAN DEFAULT FALSE");
                stmt.execute("ALTER TABLE message ADD COLUMN IF NOT EXISTS est_supprime_expediteur BOOLEAN DEFAULT FALSE");
                stmt.execute("ALTER TABLE message ADD COLUMN IF NOT EXISTS est_supprime_destinataire BOOLEAN DEFAULT FALSE");
            }
        } catch (Exception e) {
            System.err.println("Note migration message: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        if (this.messageService == null) {
            Connection cnx = utils.DBConnection.getInstance().getConnection();
            verifyMessageTable(cnx);
            this.messageService = new MessageService(cnx);
        }

        if (this.utilisateurService == null) {
            this.utilisateurService = new UtilisateurService();
        }

        try {
            List<entities.Utilisateur> users = utilisateurService.getAllUtilisateurs();
            for (entities.Utilisateur u : users) {
                String fullname = u.getNom() + " " + u.getPrenom() + " (" + u.getEmail() + ")";
                idToNameMap.put(u.getId(), fullname);
                nameToIdMap.put(fullname, u.getId());
                if (cbDestinataire != null) cbDestinataire.getItems().add(fullname);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        cbStatut.setItems(FXCollections.observableArrayList("envoye", "lu"));
        cbPriorite.setItems(FXCollections.observableArrayList("basse", "normal", "haute"));
        cbCategorie.setItems(FXCollections.observableArrayList("personnel", "important", "administratif"));

        cbStatut.setValue("envoye");
        cbPriorite.setValue("normal");
        entities.Utilisateur currentUser = utils.UserSession.getCurrentUser();
        if (currentUser != null && tfExpediteurNom != null) {
            tfExpediteurNom.setText(currentUser.getNom() + " " + currentUser.getPrenom() + " (" + currentUser.getEmail() + ")");
        }

        if (tvMessages != null) {
            colMessageId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
            if (colMessageExpediteur != null) {
                colMessageExpediteur.setCellValueFactory(cellData -> {
                    Long expId = cellData.getValue().getExpediteurId();
                    String nom = idToNameMap.getOrDefault(expId, "Inconnu (" + expId + ")");
                    return new javafx.beans.property.SimpleStringProperty(nom);
                });
            }
            if (colMessageDestinataire != null) {
                colMessageDestinataire.setCellValueFactory(cellData -> {
                    Long destId = cellData.getValue().getDestinataireId();
                    String nom = idToNameMap.getOrDefault(destId, "Inconnu (" + destId + ")");
                    return new javafx.beans.property.SimpleStringProperty(nom);
                });
            }
            colMessageObjet.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("objet"));
            colMessageDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("dateEnvoi"));

            loadMessages();

            if (tfRechercheMessage != null) {
                javafx.collections.transformation.FilteredList<Message> filteredData = new javafx.collections.transformation.FilteredList<>(messagesList, b -> true);
                tfRechercheMessage.textProperty().addListener((observable, oldValue, newValue) -> {
                    filteredData.setPredicate(message -> {
                        if (newValue == null || newValue.isEmpty()) return true;
                        String lowerCaseFilter = newValue.toLowerCase();
                        if (message.getObjet() != null && message.getObjet().toLowerCase().contains(lowerCaseFilter)) return true;
                        if (message.getContenu() != null && message.getContenu().toLowerCase().contains(lowerCaseFilter)) return true;
                        return false;
                    });
                });
                javafx.collections.transformation.SortedList<Message> sortedData = new javafx.collections.transformation.SortedList<>(filteredData);
                sortedData.comparatorProperty().bind(tvMessages.comparatorProperty());
                tvMessages.setItems(sortedData);
            }

            tvMessages.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    populateForm(newSelection);
                }
            });

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
                checkRealTimeUpdates();
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        }
    }

    private void checkRealTimeUpdates() {
        try {
            entities.Utilisateur user = utils.UserSession.getCurrentUser();
            if (user != null && messageService != null) {
                int currentCount = messageService.getMessagesRecus(user.getId()).size() + messageService.getMessagesEnvoyes(user.getId()).size();
                if (currentCount != lastMessageCount && lastMessageCount != 0) {
                    loadMessages();
                }
            }
        } catch (SQLException e) {
            // Ignore background error
        }
    }

    private void loadMessages() {
        try {
            messagesList.clear();
            entities.Utilisateur user = utils.UserSession.getCurrentUser();
            if (user != null) {
                if (user.getType().equalsIgnoreCase("administrateur")) {
                    // Admin voit tous les messages
                    messagesList.addAll(messageService.getAll());
                } else {
                    java.util.List<Message> allMyMessages = new java.util.ArrayList<>();
                    allMyMessages.addAll(messageService.getMessagesRecus(user.getId()));
                    allMyMessages.addAll(messageService.getMessagesEnvoyes(user.getId()));
                    messagesList.addAll(allMyMessages);
                    lastMessageCount = allMyMessages.size();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateForm(Message message) {
        if (cbDestinataire != null) {
            String name = idToNameMap.get(message.getDestinataireId());
            if (name != null) cbDestinataire.setValue(name);
        }
        if (tfExpediteurNom != null) {
            String name = idToNameMap.get(message.getExpediteurId());
            if (name != null) {
                tfExpediteurNom.setText(name);
            } else {
                entities.Utilisateur u = utils.UserSession.getCurrentUser();
                tfExpediteurNom.setText(u.getNom() + " " + u.getPrenom() + " (" + u.getEmail() + ")");
            }
        }
        tfObjet.setText(message.getObjet());
        taContenu.setText(message.getContenu());
        cbStatut.setValue(message.getStatut());
        cbPriorite.setValue(message.getPriorite());
        cbCategorie.setValue(message.getCategorie());
        if (message.getParentId() != null) tfParentId.setText(String.valueOf(message.getParentId()));
        else tfParentId.clear();
        if (message.getPieceJointeUrl() != null) tfPieceJointeUrl.setText(message.getPieceJointeUrl());
        else tfPieceJointeUrl.clear();
    }

    @FXML
    public void showFormCreate() {
        viderChamps();
        tvMessages.getSelectionModel().clearSelection();
        if (paneList != null) paneList.setVisible(false);
        if (paneForm != null) paneForm.setVisible(true);
    }

    @FXML
    public void showFormEdit() {
        Message selected = tvMessages.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner un message à modifier.");
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
        tvMessages.getSelectionModel().clearSelection();
        if (aiSuggestionsContainer != null) {
            aiSuggestionsContainer.setVisible(false);
            aiSuggestionsContainer.setManaged(false);
            if (aiButtonsBox != null) aiButtonsBox.getChildren().clear();
        }
        
        viderChamps();
    }

    @FXML
    public void handleRepondre() {
        if (tvMessages == null || tvMessages.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un message auquel répondre.");
            return;
        }

        Message selectedMsg = tvMessages.getSelectionModel().getSelectedItem();
        showFormCreate();

        // Preset fields
        if (selectedMsg.getObjet() != null) {
            tfObjet.setText(selectedMsg.getObjet().startsWith("Re:") ? selectedMsg.getObjet() : "Re: " + selectedMsg.getObjet());
        }
        
        // Find sender to set as destination (using ExpediteurId)
        Long expId = selectedMsg.getExpediteurId();
        if (expId != null && cbDestinataire != null && idToNameMap.containsKey(expId)) {
            cbDestinataire.setValue(idToNameMap.get(expId));
        }
        
        tfParentId.setText(String.valueOf(selectedMsg.getId()));
        
        // Load AI Suggestions asynchronously to avoid freezing UI
        if (aiSuggestionsContainer != null && aiButtonsBox != null) {
            aiSuggestionsContainer.setVisible(true);
            aiSuggestionsContainer.setManaged(true);
            aiButtonsBox.getChildren().clear();
            
            Label loadingLabel = new Label("Analyse par Gemini...");
            loadingLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic;");
            aiButtonsBox.getChildren().add(loadingLabel);
            
            String contenuOriginal = selectedMsg.getContenu();
            
            CompletableFuture.supplyAsync(() -> aiService.genererSuggestions(contenuOriginal))
                .thenAccept(suggestions -> {
                    Platform.runLater(() -> {
                        aiButtonsBox.getChildren().clear();
                        if (suggestions == null || suggestions.isEmpty()) {
                            Label errorLabel = new Label("Suggestions indisponibles.");
                            errorLabel.setStyle("-fx-text-fill: #ef4444;");
                            aiButtonsBox.getChildren().add(errorLabel);
                        } else {
                            for (String sugg : suggestions) {
                                Button btn = new Button(sugg);
                                btn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #4b5563; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-cursor: hand;");
                                btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4338ca; -fx-background-radius: 12; -fx-border-color: #c7d2fe; -fx-border-radius: 12; -fx-cursor: hand;"));
                                btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #4b5563; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-cursor: hand;"));
                                btn.setOnAction(e -> {
                                    taContenu.setText(sugg + "\n\n" + taContenu.getText());
                                });
                                aiButtonsBox.getChildren().add(btn);
                            }
                        }
                    });
                });
        }
    }

    @FXML
    public void ajouterMessage() {
        try {
            String erreurs = validerSaisie();

            if (!erreurs.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreurs de saisie", erreurs);
                return;
            }

            String texteAEnvoyer = taContenu.getText().trim();
            if (aiService.estContenuInapproprie(texteAEnvoyer)) {
                entities.Utilisateur currentUser = utils.UserSession.getCurrentUser();
                Long expId = (currentUser != null) ? currentUser.getId() : -1L;
                signalementSpamService.ajouter(new entities.SignalementSpam(expId, texteAEnvoyer));
                
                showAlert(Alert.AlertType.ERROR, "Message Bloqué par l'IA", 
                          "Notre Intelligence Artificielle a détecté que votre message ne respecte pas nos conditions d'utilisation (spam, mots inappropriés). L'envoi est bloqué et un signalement a été transmis à l'administration.");
                return;
            }

            Message message = new Message();
            entities.Utilisateur currentUser = utils.UserSession.getCurrentUser();
            message.setExpediteurId(currentUser.getId());

            Long idDestinataire = -1L;
            if (cbDestinataire != null && cbDestinataire.getValue() != null) {
                idDestinataire = nameToIdMap.getOrDefault(cbDestinataire.getValue(), -1L);
            }
            message.setDestinataireId(idDestinataire);
            
            message.setObjet(tfObjet.getText().trim());
            message.setContenu(taContenu.getText().trim());
            message.setDateEnvoi(LocalDateTime.now());
            message.setStatut(cbStatut.getValue());
            message.setPriorite(cbPriorite.getValue());
            message.setCategorie(cbCategorie.getValue());

            String pieceJointe = tfPieceJointeUrl.getText().trim();
            if (!pieceJointe.isEmpty()) {
                message.setPieceJointeUrl(pieceJointe);
            }

            String parentId = tfParentId.getText().trim();
            if (!parentId.isEmpty()) {
                message.setParentId(Long.parseLong(parentId));
            }

            messageService.ajouter(message);

            // Trigger Notification to Receiver
            entities.Utilisateur currentUserEntity = utils.UserSession.getCurrentUser();
            entities.Notification notif = new entities.Notification(
                message.getDestinataireId(), 
                "Nouveau Message", 
                "Vous avez reçu un message de " + currentUserEntity.getNomComplet() + " : " + message.getObjet()
            );
            notificationService.ajouter(notif);

            if (tvMessages != null) loadMessages();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Message ajouté avec succès.");
            hideForm();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void modifierMessage() {
        try {
            String erreurs = validerSaisie();

            if (!erreurs.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreurs de saisie", erreurs);
                return;
            }

            String texteAEnvoyer = taContenu.getText().trim();
            if (aiService.estContenuInapproprie(texteAEnvoyer)) {
                entities.Utilisateur currentUser = utils.UserSession.getCurrentUser();
                Long expId = (currentUser != null) ? currentUser.getId() : -1L;
                signalementSpamService.ajouter(new entities.SignalementSpam(expId, texteAEnvoyer));
                
                showAlert(Alert.AlertType.ERROR, "Modification Bloquée par l'IA", 
                          "Notre Intelligence Artificielle a détecté que votre message ne respecte pas nos conditions d'utilisation (spam, mots inappropriés). La modification est bloquée et un signalement a été transmis à l'administration.");
                return;
            }

            boolean isSelected = false;
            long idToModify = -1;
            if (tvMessages != null && tvMessages.getSelectionModel().getSelectedItem() != null) {
                isSelected = true;
                idToModify = tvMessages.getSelectionModel().getSelectedItem().getId();
            }

            if (!isSelected) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun message n'est sélectionné.");
                return;
            }

            if (isSelected) {
                try {
                    long id = idToModify;

                    Message message = new Message();
                    message.setId(id);
                    // For modification we keep the original sender or set to current user? Keep current user.
                    entities.Utilisateur currentUser = utils.UserSession.getCurrentUser();
                    message.setExpediteurId(currentUser.getId());
                    
                    Long idDestinataire = -1L;
                    if (cbDestinataire != null && cbDestinataire.getValue() != null) {
                        idDestinataire = nameToIdMap.getOrDefault(cbDestinataire.getValue(), -1L);
                    }
                    message.setDestinataireId(idDestinataire);
                    
                    message.setObjet(tfObjet.getText().trim());
                    message.setContenu(taContenu.getText().trim());
                    message.setDateEnvoi(LocalDateTime.now());
                    message.setStatut(cbStatut.getValue());
                    message.setPriorite(cbPriorite.getValue());
                    message.setCategorie(cbCategorie.getValue());

                    String pieceJointe = tfPieceJointeUrl.getText().trim();
                    if (!pieceJointe.isEmpty()) {
                        message.setPieceJointeUrl(pieceJointe);
                    }

                    String parentId = tfParentId.getText().trim();
                    if (!parentId.isEmpty()) {
                        message.setParentId(Long.parseLong(parentId));
                    }

                    messageService.modifier(message);
                    if (tvMessages != null) loadMessages();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Message modifié avec succès.");
                    hideForm();

                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void supprimerMessage() {
        boolean isSelected = false;
        long idToDelete = -1;
        if (tvMessages != null && tvMessages.getSelectionModel().getSelectedItem() != null) {
            isSelected = true;
            idToDelete = tvMessages.getSelectionModel().getSelectedItem().getId();
        }

        if (!isSelected) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Suppression");
            dialog.setHeaderText("Supprimer un message");
            dialog.setContentText("Entrer l'ID du message à supprimer :");
            java.util.Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                try {
                    idToDelete = Long.parseLong(result.get().trim());
                    isSelected = true;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "L'ID doit être numérique.");
                }
            } else if (result.isPresent()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "L'ID est obligatoire.");
            }
        }

        if (isSelected) {
            try {
                messageService.supprimer(idToDelete);
                if (tvMessages != null) loadMessages();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Message supprimé avec succès.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
            }
        }
    }

    private String validerSaisie() {
        StringBuilder erreurs = new StringBuilder();

        String destinataire = cbDestinataire != null ? cbDestinataire.getValue() : "";
        String objet = tfObjet.getText().trim();
        String contenu = taContenu.getText().trim();
        String parentId = tfParentId.getText().trim();
        String pieceJointe = tfPieceJointeUrl.getText().trim();

        if (destinataire == null || destinataire.trim().isEmpty()) {
            erreurs.append("- Le destinataire est obligatoire.\n");
        } else {
            Long destId = nameToIdMap.get(destinataire);
            if (destId == null) {
                erreurs.append("- Le destinataire sélectionné est invalide.\n");
            } else if (utils.UserSession.getCurrentUser() != null && destId.equals(utils.UserSession.getCurrentUser().getId())) {
                erreurs.append("- L'expéditeur et le destinataire ne doivent pas être identiques.\n");
            }
        }

        if (objet.isEmpty()) {
            erreurs.append("- L'objet est obligatoire.\n");
        } else if (objet.length() < 3) {
            erreurs.append("- L'objet doit contenir au moins 3 caractères.\n");
        } else if (objet.length() > 255) {
            erreurs.append("- L'objet ne doit pas dépasser 255 caractères.\n");
        }

        if (contenu.isEmpty()) {
            erreurs.append("- Le contenu est obligatoire.\n");
        } else if (contenu.length() < 5) {
            erreurs.append("- Le contenu doit contenir au moins 5 caractères.\n");
        }

        if (cbStatut.getValue() == null || cbStatut.getValue().trim().isEmpty()) {
            erreurs.append("- Le statut est obligatoire.\n");
        }

        if (cbPriorite.getValue() == null || cbPriorite.getValue().trim().isEmpty()) {
            erreurs.append("- La priorité est obligatoire.\n");
        }

        if (cbCategorie.getValue() == null || cbCategorie.getValue().trim().isEmpty()) {
            erreurs.append("- La catégorie est obligatoire.\n");
        }

        if (!parentId.isEmpty() && !parentId.matches("\\d+")) {
            erreurs.append("- Le parentId doit être numérique.\n");
        }

        if (!pieceJointe.isEmpty() && pieceJointe.length() > 255) {
            erreurs.append("- L'URL de la pièce jointe ne doit pas dépasser 255 caractères.\n");
        }

        return erreurs.toString();
    }

    private void viderChamps() {
        if (cbDestinataire != null) cbDestinataire.getSelectionModel().clearSelection();
        tfObjet.clear();
        taContenu.clear();
        tfPieceJointeUrl.clear();
        tfParentId.clear();

        cbStatut.setValue("envoye");
        cbPriorite.setValue("normal");
        cbCategorie.setValue("personnel");

        entities.Utilisateur currentUser = utils.UserSession.getCurrentUser();
        if (currentUser != null && tfExpediteurNom != null) {
            tfExpediteurNom.setText(currentUser.getNom() + " " + currentUser.getPrenom() + " (" + currentUser.getEmail() + ")");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}