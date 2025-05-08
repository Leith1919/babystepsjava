package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.services.RappelAutomatiqueService;

public class mainFX extends Application {
    private RappelAutomatiqueService rappelService;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialiser et démarrer le service de rappels automatiques
        rappelService = new RappelAutomatiqueService();
        rappelService.demarrerServiceRappels();

        // Charger la vue principale
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Base2.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Gestion des rendez-vous");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Arrêter proprement le service lors de la fermeture de l'application
        if (rappelService != null) {
            rappelService.arreterService();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}