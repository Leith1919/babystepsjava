package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import tn.esprit.entities.Traitement;
import tn.esprit.services.TraitementServices;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ModifierTraitementControllors {

    private final TraitementServices traitementServices = new TraitementServices();

    @FXML
    private TextField idTF;

    @FXML
    private TextField ordonnanceTF;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField historiqueTF;

    @FXML
    private ComboBox<Integer> patientIdComboBox;

    @FXML
    public void initialize() {
        try {
            List<Integer> patientIds = traitementServices.getAllPatientIds();
            ObservableList<Integer> options = FXCollections.observableArrayList(patientIds);
            patientIdComboBox.setItems(options);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", "Impossible de charger les IDs patients : " + e.getMessage());
        }
    }

    public void setTraitementData(int id, int ordonnanceId, String date, String historique, int patientId) {
        idTF.setText(String.valueOf(id));
        ordonnanceTF.setText(String.valueOf(ordonnanceId));
        datePicker.setValue(LocalDate.parse(date));
        historiqueTF.setText(historique);
        patientIdComboBox.setValue(patientId);
    }

    @FXML
    private void modifier(ActionEvent event) {
        String idText = idTF.getText().trim();
        String ordonnanceText = ordonnanceTF.getText().trim();
        LocalDate date = datePicker.getValue();
        String historique = historiqueTF.getText().trim();
        Integer patientId = patientIdComboBox.getValue();

        if (idText.isEmpty() || ordonnanceText.isEmpty() || date == null || historique.isEmpty() || patientId == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        int id, ordonnanceId;
        try {
            id = Integer.parseInt(idText);
            ordonnanceId = Integer.parseInt(ordonnanceText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Format invalide", "L'ID et l'ID ordonnance doivent être des nombres.");
            return;
        }

        Traitement traitement = new Traitement(id, ordonnanceId, date, historique, patientId);

        try {
            traitementServices.modifier(traitement);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/afficherTraitement.fxml"));
            Parent root = loader.load();
            idTF.getScene().setRoot(root);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Modification échouée : " + e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger l'affichage des traitements.");
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
