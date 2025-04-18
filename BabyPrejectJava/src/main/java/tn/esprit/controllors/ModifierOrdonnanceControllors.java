package tn.esprit.controllors;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import tn.esprit.entities.Ordonnance;
import tn.esprit.services.OrdonnanceService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ModifierOrdonnanceControllors {

    private final OrdonnanceService os = new OrdonnanceService();

    @FXML
    private TextField idTF;

    @FXML
    private TextField medicamentTF;

    @FXML
    private TextField posologieTF;

    @FXML
    private TextField dateTF;

    // Method to prefill data when modifying
    public void setOrdonnance(Ordonnance ordonnance) {
        idTF.setText(String.valueOf(ordonnance.getId()));
        medicamentTF.setText(ordonnance.getMedicament());
        posologieTF.setText(ordonnance.getPosologie());
        dateTF.setText(ordonnance.getDatePrescription());
    }

    @FXML
    private void modifier(ActionEvent event) {
        String medicament = medicamentTF.getText();
        String posologie = posologieTF.getText();
        String datePrescription = dateTF.getText();
        String idText = idTF.getText();

        // ✅ Check if fields are empty
        if (idText.isEmpty() || medicament.isEmpty() || posologie.isEmpty() || datePrescription.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "ID invalide", "L'ID doit être un nombre entier.");
            return;
        }

        // ✅ Check if date is in valid format
        try {
            LocalDate.parse(datePrescription);
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Date invalide", "Le format de la date est invalide. Utilisez AAAA-MM-JJ.");
            return;
        }

        try {
            Ordonnance ordonnance = new Ordonnance(id, medicament, posologie, datePrescription);
            os.modifier(ordonnance);

            // ✅ Redirect to afficherOrdonnance.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/afficherOrdonnance.fxml"));
            Parent root = loader.load();
            idTF.getScene().setRoot(root);
        } catch (SQLException | IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
