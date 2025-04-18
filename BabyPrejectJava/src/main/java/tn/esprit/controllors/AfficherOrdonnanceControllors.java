package tn.esprit.controllors;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import tn.esprit.entities.Ordonnance;
import tn.esprit.services.OrdonnanceService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherOrdonnanceControllors {

    @FXML
    private ListView<Ordonnance> ordonnanceListView;

    @FXML
    private Label idLabel, dateLabel, medicamentLabel, posologieLabel;

    private final OrdonnanceService ordonnanceService = new OrdonnanceService();

    private static Ordonnance newAddedOrdonnance = null;

    @FXML
    private void initialize() {
        loadOrdonnances();
        ordonnanceListView.setOnMouseClicked(this::showOrdonnanceDetails);
    }

    public void loadOrdonnances() {
        try {
            List<Ordonnance> ordonnances = ordonnanceService.getAllOrdonnances();

            if (newAddedOrdonnance != null) {
                ordonnances.removeIf(o -> o.getId() == newAddedOrdonnance.getId());
                ordonnances.add(0, newAddedOrdonnance);
                newAddedOrdonnance = null; // clear after use
            }

            ordonnanceListView.getItems().setAll(ordonnances);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des ordonnances.");
        }
    }

    @FXML
    private void supprimerOrdonnance(ActionEvent event) {
        Ordonnance selectedOrdonnance = ordonnanceListView.getSelectionModel().getSelectedItem();
        if (selectedOrdonnance != null) {
            try {
                ordonnanceService.supprimer(selectedOrdonnance);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Ordonnance supprimée avec succès !");
                loadOrdonnances();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de l'ordonnance.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Aucun élément sélectionné", "Veuillez sélectionner une ordonnance.");
        }
    }

    @FXML
    private void modifierOrdonnance(ActionEvent event) {
        Ordonnance selectedOrdonnance = ordonnanceListView.getSelectionModel().getSelectedItem();
        if (selectedOrdonnance != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifierOrdonnance.fxml"));
                Scene scene = new Scene(loader.load());

                ModifierOrdonnanceControllors controller = loader.getController();
                controller.setOrdonnance(selectedOrdonnance);

                Stage stage = (Stage) ordonnanceListView.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Aucun élément sélectionné", "Veuillez sélectionner une ordonnance.");
        }
    }

    @FXML
    private void transitionToAjouterOrdonnance(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterOrdonnance.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ordonnanceListView.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du changement de scène.");
            e.printStackTrace();
        }
    }

    private void showOrdonnanceDetails(MouseEvent event) {
        Ordonnance selectedOrdonnance = ordonnanceListView.getSelectionModel().getSelectedItem();
        if (selectedOrdonnance != null) {
            idLabel.setText(String.valueOf(selectedOrdonnance.getId()));
            dateLabel.setText(selectedOrdonnance.getDatePrescription());
            medicamentLabel.setText(selectedOrdonnance.getMedicament());
            posologieLabel.setText(selectedOrdonnance.getPosologie());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ✅ This is now non-static to allow FXML injection to work properly
    public void setNewAddedOrdonnance(Ordonnance ordonnance) {
        newAddedOrdonnance = ordonnance;
        loadOrdonnances();  // Immediately update the list
    }
}
