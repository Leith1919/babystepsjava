package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import tn.esprit.entities.Ordonnance;
import tn.esprit.services.OrdonnanceServices;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ModifierOrdonnanceControllors {

    private final OrdonnanceServices ordonnanceService = new OrdonnanceServices();

    @FXML
    private TextField idTF;

    @FXML
    private TextField medicamentTF;

    @FXML
    private TextField posologieTF;

    @FXML
    private ComboBox<String> patientNameTF;

    @FXML
    private DatePicker datePicker;

    private Ordonnance ordonnanceToModify;

    @FXML
    public void initialize() {
        loadPatientNames(); // Load names initially (optional)
    }

    public void setOrdonnanceToModify(Ordonnance ordonnance) {
        this.ordonnanceToModify = ordonnance;
        populateFormFields();
    }

    private void loadPatientNames() {
        try {
            List<String> patientNames = ordonnanceService.getAllPatientNames();
            patientNameTF.getItems().setAll(patientNames); // Clear then set
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", "Impossible de charger les patients : " + e.getMessage());
        }
    }

    private void populateFormFields() {
        if (ordonnanceToModify == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune ordonnance sélectionnée pour modification.");
            return;
        }

        // Ensure patient names are loaded before setting selection
        loadPatientNames();

        idTF.setText(String.valueOf(ordonnanceToModify.getId()));
        medicamentTF.setText(ordonnanceToModify.getMedicament());
        posologieTF.setText(ordonnanceToModify.getPosologie());

        try {
            LocalDate date = LocalDate.parse(ordonnanceToModify.getDatePrescription());
            datePicker.setValue(date);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de date", "Format de date invalide.");
        }

        // Set the selected patient in ComboBox
        patientNameTF.setValue(ordonnanceToModify.getPatientName());
    }

    @FXML
    private void modifier(ActionEvent event) {
        String idText = idTF.getText().trim();
        String medicament = medicamentTF.getText().trim();
        String posologie = posologieTF.getText().trim();
        String patientName = patientNameTF.getValue();
        LocalDate date = datePicker.getValue();

        if (idText.isEmpty() || medicament.isEmpty() || posologie.isEmpty() || patientName == null || date == null) {
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

        Ordonnance updatedOrdonnance = new Ordonnance(id, medicament, posologie, date.toString(), patientName);

        try {
            ordonnanceService.modifier(updatedOrdonnance);
            goToAfficherOrdonnance();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Modification échouée : " + e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger l'affichage des ordonnances.");
        }
    }

    private void goToAfficherOrdonnance() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/afficherOrdonnance.fxml"));
        Parent root = loader.load();
        idTF.getScene().setRoot(root);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
