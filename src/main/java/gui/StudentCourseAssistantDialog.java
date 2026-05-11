package gui;

import entities.Cours;
import entities.Etudiant;
import entities.Utilisateur;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import services.StudentCourseAssistantService;
import utils.UserSession;

import java.sql.SQLException;
import java.util.List;

public final class StudentCourseAssistantDialog {

    private StudentCourseAssistantDialog() {
    }

    public static void open(Cours preselectedCourse) {
        Utilisateur utilisateur = UserSession.getCurrentUser();
        if (!(utilisateur instanceof Etudiant etudiant) || etudiant.getId() == null) {
            return;
        }

        StudentCourseAssistantService assistantService = new StudentCourseAssistantService();

        Dialog<Void> dialog = new Dialog<>();
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setHeaderText(null);

        DialogPane dialogPane = dialog.getDialogPane();
        String stylesheet = StudentCourseAssistantDialog.class.getResource("/style.css") != null
                ? StudentCourseAssistantDialog.class.getResource("/style.css").toExternalForm()
                : null;
        if (stylesheet != null && !dialogPane.getStylesheets().contains(stylesheet)) {
            dialogPane.getStylesheets().add(stylesheet);
        }
        dialogPane.getStyleClass().add("assistant-dialog");

        VBox root = new VBox(14);
        root.getStyleClass().add("assistant-shell");

        Label titleLabel = new Label("AI Assistant Etudiant");
        titleLabel.getStyleClass().add("assistant-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("assistant-close-button");
        closeButton.setOnAction(event -> {
            Window window = closeButton.getScene() != null ? closeButton.getScene().getWindow() : null;
            if (window != null) {
                window.hide();
            } else {
                dialog.close();
            }
        });

        HBox header = new HBox(12, titleLabel, spacer, closeButton);
        header.getStyleClass().add("assistant-header");

        Label helperLabel = new Label(
                "Choisissez un cours inscrit puis posez une question. L'assistant repond a partir du cours selectionne, de la description du contenu et du texte extrait des fichiers PDF/PPT/texte disponibles."
        );
        helperLabel.getStyleClass().add("assistant-helper");
        helperLabel.setWrapText(true);

        ComboBox<Cours> courseCombo = new ComboBox<>();
        courseCombo.setMaxWidth(Double.MAX_VALUE);
        courseCombo.setPromptText("Choisir un cours");
        courseCombo.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Cours item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitre());
            }
        });
        courseCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Cours item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitre());
            }
        });

        try {
            List<Cours> enrolledCourses = assistantService.getEnrolledCourses(etudiant.getId());
            courseCombo.getItems().setAll(enrolledCourses);
            if (preselectedCourse != null && preselectedCourse.getId() != null) {
                enrolledCourses.stream()
                        .filter(item -> preselectedCourse.getId().equals(item.getId()))
                        .findFirst()
                        .ifPresent(courseCombo::setValue);
            } else if (!enrolledCourses.isEmpty()) {
                courseCombo.setValue(enrolledCourses.get(0));
            }
        } catch (SQLException e) {
            helperLabel.setText("Impossible de charger les cours inscrits: " + e.getMessage());
        }

        VBox conversationBox = new VBox(10);
        conversationBox.getStyleClass().add("assistant-conversation");
        appendAssistantMessage(
                conversationBox,
                "Je peux expliquer le contenu du cours selectionne, lire les PDF/PPT/texte disponibles, les relier a la description du contenu et repondre aux questions de l'etudiant."
        );

        ScrollPane conversationPane = new ScrollPane(conversationBox);
        conversationPane.setFitToWidth(true);
        conversationPane.setPrefViewportHeight(320);
        conversationPane.getStyleClass().add("assistant-scroll");

        TextArea inputArea = new TextArea();
        inputArea.setPromptText("Exemple: resume le chapitre 1, explique ce PDF, quelle est la difference entre ...");
        inputArea.setWrapText(true);
        inputArea.setPrefRowCount(4);
        inputArea.getStyleClass().add("assistant-input");

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("assistant-status");
        statusLabel.setWrapText(true);

        Button sendButton = new Button("Envoyer");
        sendButton.getStyleClass().addAll("primary-button", "compact-button");
        sendButton.setOnAction(event -> submitQuestion(
                assistantService,
                courseCombo,
                inputArea,
                conversationBox,
                conversationPane,
                statusLabel,
                sendButton
        ));

        HBox actions = new HBox(10, sendButton);
        actions.getStyleClass().add("assistant-actions");

        root.getChildren().addAll(header, helperLabel, courseCombo, conversationPane, inputArea, actions, statusLabel);
        dialogPane.setContent(root);
        dialog.showAndWait();
    }

    private static void submitQuestion(
            StudentCourseAssistantService assistantService,
            ComboBox<Cours> courseCombo,
            TextArea inputArea,
            VBox conversationBox,
            ScrollPane conversationPane,
            Label statusLabel,
            Button sendButton
    ) {
        String question = inputArea.getText() != null ? inputArea.getText().trim() : "";
        Cours selectedCourse = courseCombo.getValue();
        if (question.isBlank()) {
            statusLabel.setText("Saisissez une question pour l'assistant.");
            return;
        }
        if (selectedCourse == null) {
            statusLabel.setText("Choisissez un cours inscrit.");
            return;
        }

        appendUserMessage(conversationBox, question);
        inputArea.clear();
        statusLabel.setText("Analyse du cours en cours...");
        sendButton.setDisable(true);

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return assistantService.answer(question, selectedCourse);
            }
        };

        task.setOnSucceeded(event -> {
            appendAssistantMessage(conversationBox, task.getValue());
            statusLabel.setText("Reponse generee pour " + selectedCourse.getTitre() + ".");
            sendButton.setDisable(false);
            conversationPane.setVvalue(1.0);
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            appendAssistantMessage(conversationBox, "Erreur assistant: " + (error != null ? error.getMessage() : "inconnue"));
            statusLabel.setText("Echec de l'assistant.");
            sendButton.setDisable(false);
            conversationPane.setVvalue(1.0);
        });

        Thread thread = new Thread(task, "student-course-assistant");
        thread.setDaemon(true);
        thread.start();
    }

    private static void appendAssistantMessage(VBox conversationBox, String message) {
        VBox bubble = new VBox(6);
        bubble.getStyleClass().addAll("assistant-bubble", "assistant-bubble-ai");
        Label author = new Label("Assistant");
        author.getStyleClass().add("assistant-bubble-author");
        Label content = new Label(message);
        content.getStyleClass().add("assistant-bubble-text");
        content.setWrapText(true);
        bubble.getChildren().addAll(author, content);
        conversationBox.getChildren().add(bubble);
    }

    private static void appendUserMessage(VBox conversationBox, String message) {
        VBox bubble = new VBox(6);
        bubble.getStyleClass().addAll("assistant-bubble", "assistant-bubble-user");
        Label author = new Label("Etudiant");
        author.getStyleClass().add("assistant-bubble-author");
        Label content = new Label(message);
        content.getStyleClass().add("assistant-bubble-text");
        content.setWrapText(true);
        bubble.getChildren().addAll(author, content);
        conversationBox.getChildren().add(bubble);
    }
}
