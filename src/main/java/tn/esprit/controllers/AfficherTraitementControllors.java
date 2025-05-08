package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Traitement;
import tn.esprit.services.TraitementServices;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AfficherTraitementControllors {

    @FXML private ListView<String> traitementListView;
    @FXML private Label idLabel, ordonnanceLabel, dateLabel, historiqueLabel, patientIdLabel;
    @FXML private TextField searchField;
    @FXML private Button previousButton, nextButton, ajouterButton, modifierButton, supprimerButton;

    private final TraitementServices ts = new TraitementServices();
    private final ObservableList<String> traitementList = FXCollections.observableArrayList();

    private final int ITEMS_PER_PAGE = 5;
    private int currentPage = 0;
    private List<Traitement> allTraitements = new ArrayList<>();

    @FXML
    public void initialize() {
        loadTraitements();
        setupLiveSearch();
        setupSelectionListener();
    }

    private void loadTraitements() {
        try {
            allTraitements = ts.recuperer();
            allTraitements.sort((t1, t2) -> t2.getDatePrescription().compareTo(t1.getDatePrescription()));
            currentPage = 0;
            updatePage();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les traitements : " + e.getMessage());
        }
    }

    private void updatePage() {
        traitementList.clear();
        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, allTraitements.size());

        for (int i = start; i < end; i++) {
            traitementList.add(allTraitements.get(i).toString());
        }

        traitementListView.setItems(traitementList);
        updateNavigationButtons();
    }

    private void updateNavigationButtons() {
        previousButton.setDisable(currentPage == 0);
        nextButton.setDisable((currentPage + 1) * ITEMS_PER_PAGE >= allTraitements.size());
    }

    private void setupLiveSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            rechercherTraitement(newValue.trim().toLowerCase());
        });
    }

    private void rechercherTraitement(String keyword) {
        if (keyword.isEmpty()) {
            updatePage();
            return;
        }

        ObservableList<String> filteredList = FXCollections.observableArrayList();
        for (Traitement t : allTraitements) {
            if (t.toString().toLowerCase().contains(keyword)) {
                filteredList.add(t.toString());
            }
        }
        traitementListView.setItems(filteredList);
        previousButton.setDisable(true);
        nextButton.setDisable(true);
    }

    private void setupSelectionListener() {
        traitementListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Traitement selectedTraitement = getTraitementFromString(newVal);
                if (selectedTraitement != null) {
                    idLabel.setText(String.valueOf(selectedTraitement.getId()));
                    ordonnanceLabel.setText(String.valueOf(selectedTraitement.getOrdonnanceId()));
                    dateLabel.setText(selectedTraitement.getDatePrescription().toString());
                    historiqueLabel.setText(selectedTraitement.getHistoriqueTraitement());
                    patientIdLabel.setText(String.valueOf(selectedTraitement.getPatientId()));
                }
            }
        });
    }

    private Traitement getTraitementFromString(String traitementString) {
        for (Traitement t : allTraitements) {
            if (t.toString().equals(traitementString)) {
                return t;
            }
        }
        return null;
    }

    @FXML
    private void nextPage(ActionEvent event) {
        if ((currentPage + 1) * ITEMS_PER_PAGE < allTraitements.size()) {
            currentPage++;
            updatePage();
        }
    }

    @FXML
    private void previousPage(ActionEvent event) {
        if (currentPage > 0) {
            currentPage--;
            updatePage();
        }
    }

    @FXML
    private void ajouterTraitement(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/ajouterTraitement.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Traitement");
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d’ouvrir le formulaire d’ajout : " + e.getMessage());
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
            Traitement traitementToModify = ts.recuperer().stream()
                    .filter(t -> t.getId() == idToModify)
                    .findFirst()
                    .orElse(null);

            if (traitementToModify == null) {
                showAlert("Erreur", "Traitement introuvable.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/modifierTraitement.fxml"));
            Parent root = loader.load();

            ModifierTraitementControllors controller = loader.getController();
            controller.setTraitementData(
                    traitementToModify.getId(),
                    traitementToModify.getOrdonnanceId(),
                    traitementToModify.getDatePrescription().toString(),
                    traitementToModify.getHistoriqueTraitement(),
                    traitementToModify.getPatientId()
            );

            traitementListView.getScene().setRoot(root);

        } catch (SQLException | IOException e) {
            showAlert("Erreur", "Erreur : " + e.getMessage());
        }
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
            loadTraitements();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @FXML
    private void transitionToAjouterOrdonnance(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/ajouterOrdonnance.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
