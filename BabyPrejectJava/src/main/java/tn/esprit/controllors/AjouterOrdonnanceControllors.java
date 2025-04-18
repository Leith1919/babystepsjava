package tn.esprit.controllors;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.Ordonnance;
import tn.esprit.services.OrdonnanceService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class AjouterOrdonnanceControllors {

    @FXML
    private TextField medicamentTF;

    @FXML
    private TextField posologieTF;

    @FXML
    private TextField dateTF;

    @FXML
    private Button ajouterBtn;

    private final OrdonnanceService ordonnanceService = new OrdonnanceService();

    @FXML
    private void initialize() {
        ajouterBtn.setOnAction(this::ajouterOrdonnance);
    }

    @FXML
    private void ajouterOrdonnance(ActionEvent event) {
        String medicament = medicamentTF.getText();
        String posologie = posologieTF.getText();
        String datePrescription = dateTF.getText();

        // ✅ Empty fields check
        if (medicament.isEmpty() || posologie.isEmpty() || datePrescription.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        // ✅ Date format check
        try {
            LocalDate.parse(datePrescription); // Just validates the format
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Date invalide", "Format de date invalide. Utilisez le format AAAA-MM-JJ.");
            return;
        }

        try {
            Ordonnance ordonnance = new Ordonnance(medicament, posologie, datePrescription);
            ordonnanceService.ajouter(ordonnance);

            // ✅ Load new scene and pass data
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/afficherOrdonnance.fxml"));
            Scene scene = new Scene(loader.load());

            AfficherOrdonnanceControllors controller = loader.getController();
            controller.setNewAddedOrdonnance(ordonnance); // Pass the new ordonnance

            Stage stage = (Stage) ajouterBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ordonnance ajoutée avec succès !");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
