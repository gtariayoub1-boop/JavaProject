package main;

import javafx.application.Application;
import javafx.stage.Stage;
import utils.DBConnection;
import utils.PasswordResetLinkServer;
import utils.SceneManager;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SceneManager.init(stage);
        PasswordResetLinkServer.start();
        SceneManager.switchScene("/login.fxml", "Campus Access");
    }

    @Override
    public void stop() {
        PasswordResetLinkServer.stop();
        DBConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
