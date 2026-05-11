package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public final class SceneManager {

    private static Stage primaryStage;

    private SceneManager() {
    }

    public static void init(Stage stage) {
        primaryStage = stage;
        primaryStage.setMinWidth(1080);
        primaryStage.setMinHeight(720);
    }

    public static void switchScene(String resourcePath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(resourcePath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(SceneManager.class.getResource("/style.css").toExternalForm());
        scene.getStylesheets().add(SceneManager.class.getResource("/style.css").toExternalForm());
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void bringToFront() {
        if (primaryStage == null) {
            return;
        }
        primaryStage.show();
        primaryStage.toFront();
        primaryStage.requestFocus();
    }

    public static Stage getStage() {
        return primaryStage;
    }
}