package tn.esprit.controllors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import tn.esprit.entities.Traitement;
import tn.esprit.services.TraitementServices;

import java.io.IOException;
import java.sql.SQLException;

public class AfficherTraitementControllors {

    @FXML
    private ListView<String> traitementListView;

    @FXML
    private Label idLabel;

    @FXML
    private Label ordonnanceLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label historiqueLabel;

    private final TraitementServices ts = new TraitementServices();
    private final ObservableList<String> traitementList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadTraitements();
    }

    private void loadTraitements() {
        try {
            traitementList.clear();
            for (Traitement t : ts.recuperer()) {
                traitementList.add(t.toString());
            }
            traitementListView.setItems(traitementList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les traitements : " + e.getMessage());
        }
    }

    public void ajouterEtAfficherPremier(Traitement t) {
        traitementList.add(0, t.toString()); // Add at the beginning
        traitementListView.setItems(traitementList);

        // Show the details in the labels
        setrId(String.valueOf(t.getId()));
        setrOrdonnance(String.valueOf(t.getOrdonnanceId()));
        setrDate(t.getDatePrescription().toString());
        setrHistorique(t.getHistoriqueTraitement());
    }

    @FXML
    private void supprimerTraitement(ActionEvent event) {
        String selected = traitementListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un traitement à supprimer.");
            return;
        }

        try {
            int idToDelete = Integer.parseInt(selected.split(",")[0].replaceAll("[^0-9]", ""));
            Traitement t = new Traitement();
            t.setId(idToDelete);
            ts.supprimer(t);
            showAlert("Succès", "Traitement supprimé avec succès !");
            loadTraitements(); // Refresh list
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @FXML
    private void modifierTraitement(ActionEvent event) {
        String selected = traitementListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un traitement à modifier.");
            return;
        }

        try {
            int idToModify = Integer.parseInt(selected.split(",")[0].replaceAll("[^0-9]", ""));
            Traitement traitementToModify = null;
            for (Traitement t : ts.recuperer()) {
                if (t.getId() == idToModify) {
                    traitementToModify = t;
                    break;
                }
            }

            if (traitementToModify == null) {
                showAlert("Erreur", "Traitement introuvable.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifierTraitement.fxml"));
            Parent root = loader.load();

            ModifierTraitementControllors controller = loader.getController();
            controller.setTraitementData(
                    traitementToModify.getId(),
                    traitementToModify.getOrdonnanceId(),
                    traitementToModify.getDatePrescription().toString(),
                    traitementToModify.getHistoriqueTraitement()
            );

            traitementListView.getScene().setRoot(root);

        } catch (SQLException | IOException e) {
            showAlert("Erreur", "Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void transitionToAjouterOrdonnance(ActionEvent event) {
        try {
            // Close the current window (Traitement window)
            Stage currentStage = (Stage) traitementListView.getScene().getWindow();
            currentStage.close();

            // Load the new window for Ordonnance CRUD
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterOrdonnance.fxml"));
            Parent root = loader.load();

            // Open a new stage for Ordonnance CRUD
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("CRUD Ordonnance");
            newStage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de l'ouverture de la fenêtre Ordonnance : " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Setters to receive new traitement details
    public void setrId(String id) {
        if (idLabel != null) idLabel.setText(id);
    }

    public void setrOrdonnance(String ordonnanceId) {
        if (ordonnanceLabel != null) ordonnanceLabel.setText(ordonnanceId);
    }

    public void setrDate(String date) {
        if (dateLabel != null) dateLabel.setText(date);
    }

    public void setrHistorique(String historique) {
        if (historiqueLabel != null) historiqueLabel.setText(historique);
    }
}
