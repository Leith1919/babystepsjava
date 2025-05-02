package tn.esprit.Tests;
import tn.esprit.api.ChatbotApiServer;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MainFX extends Application {
    private ChatbotApiServer apiServer;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            // Définir le style AVANT de rendre la fenêtre visible
            primaryStage.initStyle(StageStyle.DECORATED);

            // Charger la première fenêtre AjoutSuiviGrossesse.fxml
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/AjoutSuiviGrossesse.fxml"));
            Parent root = fxmlLoader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);    // Largeur minimale
            primaryStage.setMinHeight(650);   // Hauteur minimale
            primaryStage.setResizable(true);  // Permettre le redimensionnement
            primaryStage.setTitle("Ajout Suivi Grossesse - MamanSanté");

            // Rendre la fenêtre visible APRÈS avoir défini toutes les propriétés
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du démarrage de l'application: " + e.getMessage());
        }
    }
    private void startApiServer() {
        new Thread(() -> {
            try {
                apiServer = new ChatbotApiServer();
                apiServer.start();
            } catch (IOException e) {
                System.err.println("Erreur lors du démarrage du serveur API: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void stop() throws Exception {
        // Arrêter proprement le serveur API lors de la fermeture de l'application
        if (apiServer != null) {
            apiServer.stop();
        }
        super.stop();
    }


}