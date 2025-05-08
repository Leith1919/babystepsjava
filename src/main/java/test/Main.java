package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.User.RappelAutomatiqueService;

import java.io.IOException;
import java.sql.SQLException;

import static javafx.application.Application.launch;

public class Main extends Application {
    private RappelAutomatiqueService rappelService;

    @Override
    public void start(Stage stage) throws IOException {

        rappelService = new RappelAutomatiqueService();
        rappelService.demarrerServiceRappels();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/Base2.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

    }

    @Override
    public void stop() {
        // Arrêter proprement le service lors de la fermeture de l'application
        if (rappelService != null) {
            rappelService.arreterService();
        }
    }

    public static void main(String[] args) throws SQLException, RuntimeException {
        launch();

    }
}







