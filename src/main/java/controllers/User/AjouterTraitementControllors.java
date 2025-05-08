package controllers.User;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import models.Traitement;
import services.User.TraitementServices;
import services.User.OrdonnanceServices;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AjouterTraitementControllors {

    private final TraitementServices ts = new TraitementServices();
    private final OrdonnanceServices ordonnanceService = new OrdonnanceServices();

    @FXML
    private TextField idTF;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField historiqueTF;

    @FXML
    private ComboBox<Integer> ordonnanceComboBox;

    @FXML
    private ComboBox<Integer> patientIdComboBox;

    @FXML
    public void initialize() {
        try {
            // Populate Patient IDs
            List<Integer> patientIds = ts.getAllPatientIds();
            patientIdComboBox.setItems(FXCollections.observableArrayList(patientIds));

            // Populate Ordonnance IDs
            List<Integer> ordonnanceIds = ts.getAllOrdonnanceIds();
            ordonnanceComboBox.setItems(FXCollections.observableArrayList(ordonnanceIds));

            // Add input validation for idTF: only accept digits
            idTF.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    idTF.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });

        } catch (SQLException e) {
            showAlert("Erreur lors du chargement des données : " + e.getMessage());
        }
    }

    @FXML
    private void ajouterTraitement(ActionEvent event) {
        try {
            if (idTF.getText().isEmpty() || ordonnanceComboBox.getValue() == null || datePicker.getValue() == null
                    || historiqueTF.getText().isEmpty() || patientIdComboBox.getValue() == null) {
                showAlert("Tous les champs doivent être remplis.");
                return;
            }

            int id = Integer.parseInt(idTF.getText());
            int ordonnanceId = ordonnanceComboBox.getValue();
            LocalDate date = datePicker.getValue();
            String historique = historiqueTF.getText();
            int patientId = patientIdComboBox.getValue();

            Traitement traitement = new Traitement(id, ordonnanceId, date, historique, patientId);
            ts.ajouter(traitement);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/afficherTraitement.fxml"));
            Parent root = loader.load();
            ordonnanceComboBox.getScene().setRoot(root);

        } catch (NumberFormatException e) {
            showAlert("L'ID doit être un nombre entier.");
        } catch (SQLException | IOException e) {
            showAlert("Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
