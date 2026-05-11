package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.DBConnection;

import java.sql.Connection;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Vérifier la connexion à la base de données
        Connection cnx = DBConnection.getInstance().getConnection();
        if (cnx == null) {
            System.err.println("❌ Connexion BD échouée");
            System.exit(1);
        }
        System.out.println("✅ Connexion BD réussie !");

        // Charger la page de liste des évaluations
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main-layout-etudiant.fxml"));
        Parent root = loader.load();
        

        primaryStage.setTitle("Gestion des Évaluations");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Fermer la connexion BD à la fermeture de l'application
        DBConnection.closeConnection();
        System.out.println("✅ Application fermée, connexion BD libérée");
    }

    public static void main(String[] args) {
        launch(args);
    }
}


