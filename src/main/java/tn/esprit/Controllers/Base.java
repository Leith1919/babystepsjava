package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Base implements Initializable {
    @FXML
    private VBox rdvSubmenu;

    @FXML
    private Button rdvButton;
    @FXML
    private VBox dispSubmenu;

    @FXML
    private Button dispButton;

    @FXML
    private Button front;
    @FXML
    private Button PdfButton;

    @FXML
    void navigateToAjouterRDV() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterRendezVous.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Scene scene = rdvButton.getScene();

            // Remplacer le contenu de la scène par le formulaire de rendez-vous
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface AjouterRendezVous: " + e.getMessage());
        }
    }

    @FXML
    void navigateToConsulterRDV() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherRendezVous.fxml"));
            Parent root = loader.load();

            Scene scene = rdvButton.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface ConsulterRendezVous: " + e.getMessage());
        }
    }

    @FXML
    void toggleRdvSubmenu() {
        rdvSubmenu.setVisible(!rdvSubmenu.isVisible());
        rdvSubmenu.setManaged(!rdvSubmenu.isManaged());

        // Changer le style du bouton en fonction de l'état du sous-menu
        if (rdvSubmenu.isVisible()) {
            rdvButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.4); -fx-background-radius: 8 8 0 0;");
        } else {
            rdvButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3); -fx-background-radius: 8;");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialisation si nécessaire
    }

    public void toggleDispSubmenu(ActionEvent actionEvent) {
        dispSubmenu.setVisible(!dispSubmenu.isVisible());
        dispSubmenu.setManaged(!dispSubmenu.isManaged());

        // Changer le style du bouton en fonction de l'état du sous-menu
        if (dispSubmenu.isVisible()) {
            dispButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.4); -fx-background-radius: 8 8 0 0;");
        } else {
            dispButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3); -fx-background-radius: 8;");
        }
    }

    public void navigateToAjouterDISP(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterDisponibilite.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Scene scene = rdvButton.getScene();

            // Remplacer le contenu de la scène par le formulaire de rendez-vous
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface Disponibilite: " + e.getMessage());
        }
    }

    public void navigateToConsulterDISP(ActionEvent actionEvent) {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherDisponibilite.fxml"));
            Parent root = loader.load();

            Scene scene = rdvButton.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface ConsulterDispo: " + e.getMessage());
        }
    }

    public void pdfstati(ActionEvent actionEvent ) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GenerationStatistiques.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Scene scene = PdfButton.getScene();

            // Remplacer le contenu de la scène par le formulaire de rendez-vous
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface de pdf: " + e.getMessage());
        }
    }
}