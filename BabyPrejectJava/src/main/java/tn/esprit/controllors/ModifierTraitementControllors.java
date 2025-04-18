package tn.esprit.controllors;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import tn.esprit.entities.Traitement;
import tn.esprit.services.TraitementServices;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ModifierTraitementControllors {

    private final TraitementServices ts = new TraitementServices();

    @FXML
    private TextField idTF;

    @FXML
    private TextField dateTF;

    @FXML
    private TextField historiqueTF;

    @FXML
    private TextField ordonnanceTF;

    /**
     * This method is called by the previous controller to prefill the fields.
     */
    public void setTraitementData(int id, int ordonnanceId, String date, String historique) {
        idTF.setText(String.valueOf(id));
        ordonnanceTF.setText(String.valueOf(ordonnanceId));
        dateTF.setText(date);
        historiqueTF.setText(historique);
    }

    /**
     * Called when the user clicks the "Modifier" button in the GUI.
     */
    @FXML
    private void modifier(ActionEvent event) {
        try {
            // ✅ Input validation
            if (idTF.getText().isEmpty() || ordonnanceTF.getText().isEmpty()
                    || dateTF.getText().isEmpty() || historiqueTF.getText().isEmpty()) {
                showAlert("Tous les champs doivent être remplis.");
                return;
            }

            if (!idTF.getText().matches("\\d+")) {
                showAlert("L'identifiant du traitement doit être un nombre.");
                return;
            }

            if (!ordonnanceTF.getText().matches("\\d+")) {
                showAlert("L'identifiant de l'ordonnance doit être un nombre.");
                return;
            }

            LocalDate date;
            try {
                date = LocalDate.parse(dateTF.getText()); // Just to validate format
            } catch (DateTimeParseException e) {
                showAlert("Format de date invalide. Utilisez le format AAAA-MM-JJ.");
                return;
            }

            Traitement t = new Traitement(
                    Integer.parseInt(idTF.getText()),
                    Integer.parseInt(ordonnanceTF.getText()),
                    date,
                    historiqueTF.getText()
            );

            ts.modifier(t);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/afficherTraitement.fxml"));
            Parent root = loader.load();
            idTF.getScene().setRoot(root);

        } catch (SQLException | IOException e) {
            showAlert("Erreur lors de la modification : " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de saisie");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
