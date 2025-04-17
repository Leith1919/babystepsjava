package tn.esprit.Controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.suiviGrossesse;
import tn.esprit.services.SuiviGrossesseService;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class AfficherSuiviGrossesseFront implements Initializable {

    @FXML
    private GridPane accountsGridPane;

    @FXML
    private VBox sidebar;

    @FXML
    private Button btnComptes;

    @FXML
    private Button btnReclamation;

    @FXML
    private Button btnSignOut;

    private FadeTransition fadeIn;

    private SuiviGrossesseService suiviGrossesseService;

    // ID spécifique à afficher
    private final int SUIVI_GROSSESSE_ID = 29;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        suiviGrossesseService = new SuiviGrossesseService();

        // Initialiser l'animation
        fadeIn = new FadeTransition(Duration.millis(500));
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setNode(accountsGridPane);

        setupButtonActions();
        afficherSuiviGrossesseById(SUIVI_GROSSESSE_ID);
        fadeIn.play();
    }

    private void setupButtonActions() {
        btnComptes.setOnAction(event -> {
            System.out.println("Navigation vers Comptes");
        });

        btnReclamation.setOnAction(event -> {
            System.out.println("Navigation vers Réclamations");
        });

        btnSignOut.setOnAction(event -> {
            System.out.println("Déconnexion");
            // Logique de déconnexion ici
        });
    }

    private void afficherSuiviGrossesseById(int id) {
        try {
            List<suiviGrossesse> allRecords = suiviGrossesseService.recuperer();
            suiviGrossesse targetRecord = null;

            for (suiviGrossesse record : allRecords) {
                if (record.getId() == id) {
                    targetRecord = record;
                    break;
                }
            }

            if (targetRecord != null) {
                accountsGridPane.getChildren().clear();
                setupGridHeaders();
                displayRecord(targetRecord);
            } else {
                showAlert(Alert.AlertType.WARNING, "Recherche",
                        "Aucun suivi grossesse trouvé avec ID " + id);
                Label notFoundLabel = new Label("Aucun suivi grossesse trouvé avec ID " + id);
                notFoundLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");
                accountsGridPane.add(notFoundLabel, 0, 1, 5, 1);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur de base de données",
                    "Erreur lors de la récupération des données: " + e.getMessage());
        }
    }

    private void setupGridHeaders() {
        String[] headers = { "Date Suivi", "Poids (kg)", "Tension", "Symptômes", "État Grossesse", "Voir Suivi Bébé"};
        String headerStyle = "-fx-font-weight: bold; -fx-padding: 10px; -fx-background-color: #f0f0f0;";

        for (int i = 0; i < headers.length; i++) {
            Label headerLabel = new Label(headers[i]);
            headerLabel.setStyle(headerStyle);
            headerLabel.setPrefWidth(Double.MAX_VALUE);
            accountsGridPane.add(headerLabel, i, 0);
        }
    }

    private void displayRecord(suiviGrossesse record) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String formattedDate = dateFormat.format(record.getDateSuivi());

        Label idLabel = new Label(String.valueOf(record.getId()));
        Label dateLabel = new Label(formattedDate);
        Label poidsLabel = new Label(String.valueOf(record.getPoids()));
        Label tensionLabel = new Label(String.valueOf(record.getTension()));
        Label symptomesLabel = new Label(record.getSymptomes());
        Label etatLabel = new Label(record.getEtatGrossesse());

        String cellStyle = "-fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;";

        Label[] labels = {idLabel, dateLabel, poidsLabel, tensionLabel, symptomesLabel, etatLabel};
        for (Label label : labels) {
            label.setStyle(cellStyle);
            label.setPrefWidth(Double.MAX_VALUE);
            label.setWrapText(true);
        }

        accountsGridPane.add(idLabel, 0, 1);
        accountsGridPane.add(dateLabel, 1, 1);
        accountsGridPane.add(poidsLabel, 2, 1);
        accountsGridPane.add(tensionLabel, 3, 1);
        accountsGridPane.add(symptomesLabel, 4, 1);
        accountsGridPane.add(etatLabel, 5, 1);

        Button suiviBebeButton = new Button("Suivi bébé");
        suiviBebeButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        suiviBebeButton.setOnAction(event -> handleSuiviBebe(record));

        accountsGridPane.add(suiviBebeButton, 6, 1);
    }

    private void handleSuiviBebe(suiviGrossesse record) {
        try {
            int idGrossesse = record.getId();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherSuiviBebeView.fxml"));
            Parent root = loader.load();

            AfficherSuiviBebeController controller = loader.getController();
            controller.initData(record); // record est bien de type suiviGrossesse


            Stage stage = new Stage();
            stage.setTitle("Suivi bébé - Grossesse #" + idGrossesse);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    "Erreur lors de l'ouverture du suivi bébé: " + e.getMessage());
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
