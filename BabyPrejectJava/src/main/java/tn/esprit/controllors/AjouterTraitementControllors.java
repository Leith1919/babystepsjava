package tn.esprit.controllors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import tn.esprit.entities.Ordonnance;
import tn.esprit.entities.Traitement;
import tn.esprit.services.OrdonnanceService;
import tn.esprit.services.TraitementServices;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class AjouterTraitementControllors {

    private final TraitementServices ts = new TraitementServices();
    private final OrdonnanceService ordonnanceService = new OrdonnanceService();

    @FXML
    private TextField dateTF;

    @FXML
    private TextField historiqueTF;

    @FXML
    private TextField idTF;

    @FXML
    private ComboBox<Ordonnance> ordonnanceComboBox;

    private final ObservableList<Ordonnance> ordonnanceList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            ordonnanceList.addAll(ordonnanceService.recuperer());
            ordonnanceComboBox.setItems(ordonnanceList);

            ordonnanceComboBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Ordonnance item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getMedicament() + " - " + item.getDatePrescription());
                }
            });

            ordonnanceComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Ordonnance item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getMedicament() + " - " + item.getDatePrescription());
                }
            });

        } catch (SQLException e) {
            System.out.println("Erreur chargement ordonnances: " + e.getMessage());
        }
    }

    @FXML
    private void ajouter(ActionEvent event) {
        try {
            // ✅ Validation
            if (idTF.getText().isEmpty() || dateTF.getText().isEmpty() || historiqueTF.getText().isEmpty()) {
                showAlert("Veuillez remplir tous les champs.");
                return;
            }

            if (!idTF.getText().matches("\\d+")) {
                showAlert("L'identifiant doit être un nombre.");
                return;
            }

            LocalDate date;
            try {
                date = LocalDate.parse(dateTF.getText());
            } catch (DateTimeParseException e) {
                showAlert("Format de date invalide. Utilisez le format AAAA-MM-JJ.");
                return;
            }

            Ordonnance selectedOrdonnance = ordonnanceComboBox.getValue();
            if (selectedOrdonnance == null) {
                showAlert("Veuillez sélectionner une ordonnance.");
                return;
            }

            // ✅ Create and save traitement
            Traitement traitement = new Traitement(
                    Integer.parseInt(idTF.getText()),
                    selectedOrdonnance.getId(),
                    date,
                    historiqueTF.getText()
            );

            ts.ajouter(traitement);

            // Load afficherTraitement.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/afficherTraitement.fxml"));
            Parent root = loader.load();
            AfficherTraitementControllors ac = loader.getController();

            // Pass the new traitement to be shown at the top
            ac.ajouterEtAfficherPremier(traitement);

            // Switch to the new view
            idTF.getScene().setRoot(root);

        } catch (SQLException | IOException e) {
            showAlert("Erreur: " + e.getMessage());
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
