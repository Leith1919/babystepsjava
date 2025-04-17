package tn.esprit.Tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Charger la première fenêtre AjoutSuiviGrossesse.fxml
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/AjoutSuiviGrossesse.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("AjoutSuiviGrossesse");
        primaryStage.show();
    }
}
