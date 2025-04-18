package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tn.esprit.entites.Disponibilite;
import tn.esprit.services.DisponibiliteService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AjouterDisponibilite implements Initializable {

        @FXML
        private CheckBox check9_11;

        @FXML
        private CheckBox check11_13;

        @FXML
        private CheckBox check14_16;

        @FXML
        private CheckBox check16_18;

        @FXML
        private DatePicker datePicker;

        @FXML
        private TextField medecinIdField;

        @FXML
        private ComboBox<String> statutComboBox;

        @FXML
        private Label dateErrorLabel;

        @FXML
        private Label creneauErrorLabel;

        @FXML
        private Label statutErrorLabel;

        @FXML
        private Label medecinErrorLabel;

        private final DisponibiliteService service = new DisponibiliteService();

        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
                // Initialiser le ComboBox avec les statuts possibles
                statutComboBox.getItems().addAll("Disponible", "Indisponible");

                // Initialiser les labels d'erreur (invisibles au départ)
                initializeErrorLabels();

                // Ajouter des écouteurs pour validation en temps réel
                setupValidationListeners();
        }

        private void initializeErrorLabels() {
                // Configuration des labels d'erreur
                dateErrorLabel.setTextFill(Color.RED);
                dateErrorLabel.setVisible(false);

                creneauErrorLabel.setTextFill(Color.RED);
                creneauErrorLabel.setVisible(false);

                statutErrorLabel.setTextFill(Color.RED);
                statutErrorLabel.setVisible(false);

                medecinErrorLabel.setTextFill(Color.RED);
                medecinErrorLabel.setVisible(false);
        }

        private void setupValidationListeners() {
                // Validation de la date (doit être future)
                datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
                        validateDate(newValue);
                });

                // Validation de l'ID du médecin (doit être un nombre entier positif)
                medecinIdField.textProperty().addListener((observable, oldValue, newValue) -> {
                        validateMedecinId(newValue);
                });

                // Validation du statut
                statutComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                        validateStatut(newValue);
                });

                // Validation des créneaux horaires
                CheckBox[] checkBoxes = {check9_11, check11_13, check14_16, check16_18};
                for (CheckBox checkBox : checkBoxes) {
                        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                                validateCreneaux();
                        });
                }
        }

        private boolean validateDate(LocalDate date) {
                if (date == null) {
                        dateErrorLabel.setText("La date est obligatoire");
                        dateErrorLabel.setVisible(true);
                        return false;
                } else if (date.isBefore(LocalDate.now())) {
                        dateErrorLabel.setText("La date doit être dans le futur");
                        dateErrorLabel.setVisible(true);
                        return false;
                } else {
                        dateErrorLabel.setVisible(false);
                        return true;
                }
        }

        private boolean validateCreneaux() {
                boolean auMoinsUnCreneau = check9_11.isSelected() || check11_13.isSelected()
                        || check14_16.isSelected() || check16_18.isSelected();

                creneauErrorLabel.setVisible(!auMoinsUnCreneau);
                if (!auMoinsUnCreneau) {
                        creneauErrorLabel.setText("Sélectionnez au moins un créneau horaire");
                }

                return auMoinsUnCreneau;
        }

        private boolean validateStatut(String statut) {
                if (statut == null || statut.trim().isEmpty()) {
                        statutErrorLabel.setText("Le statut est obligatoire");
                        statutErrorLabel.setVisible(true);
                        return false;
                } else {
                        statutErrorLabel.setVisible(false);
                        return true;
                }
        }

        private boolean validateMedecinId(String idStr) {
                if (idStr == null || idStr.trim().isEmpty()) {
                        medecinErrorLabel.setText("L'ID du médecin est obligatoire");
                        medecinErrorLabel.setVisible(true);
                        return false;
                }

                try {
                        int id = Integer.parseInt(idStr);
                        if (id <= 0) {
                                medecinErrorLabel.setText("L'ID doit être un nombre positif");
                                medecinErrorLabel.setVisible(true);
                                return false;
                        } else {
                                medecinErrorLabel.setVisible(false);
                                return true;
                        }
                } catch (NumberFormatException e) {
                        medecinErrorLabel.setText("L'ID doit être un nombre entier");
                        medecinErrorLabel.setVisible(true);
                        return false;
                }
        }

        private boolean validateAllFields() {
                boolean dateValid = validateDate(datePicker.getValue());
                boolean creneauxValid = validateCreneaux();
                boolean statutValid = validateStatut(statutComboBox.getValue());
                boolean medecinValid = validateMedecinId(medecinIdField.getText());

                return dateValid && creneauxValid && statutValid && medecinValid;
        }

        @FXML
        void ajouterDisponibilite(ActionEvent event) {
                try {
                        // Valider tous les champs avant de procéder
                        if (!validateAllFields()) {
                                return;
                        }

                        LocalDate selectedDate = datePicker.getValue();
                        String statut = statutComboBox.getValue();
                        int idMedecin = Integer.parseInt(medecinIdField.getText());

                        List<String> heures = new ArrayList<>();
                        if (check9_11.isSelected()) heures.add("9-11");
                        if (check11_13.isSelected()) heures.add("11-13");
                        if (check14_16.isSelected()) heures.add("14-16");
                        if (check16_18.isSelected()) heures.add("16-18");

                        Disponibilite dispo = new Disponibilite();
                        dispo.setJour(selectedDate);
                        dispo.setHeuresDisp(heures);
                        dispo.setStatutDisp(statut);
                        dispo.setIdMedecin(idMedecin);

                        service.ajouter(dispo);
                        showAlert("Succès", "Disponibilité ajoutée avec succès !");
                        clearForm();
                        navigateToAfficherDisponibilite(event);

                } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Erreur", "Une erreur est survenue: " + e.getMessage());
                }
        }

        @FXML
        private void navigateToAfficherDisponibilite(ActionEvent event) {
                try {
                        Parent afficherRoot = FXMLLoader.load(getClass().getResource("/AfficherDisponibilite.fxml"));
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(afficherRoot));
                        stage.setTitle("Liste des Disponibilités");
                        stage.show();
                } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Erreur de Navigation", "Impossible d'accéder à la liste des disponibilités");
                }
        }

        private void showAlert(String titre, String message) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(titre);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
        }

        private void clearForm() {
                datePicker.setValue(null);
                check9_11.setSelected(false);
                check11_13.setSelected(false);
                check14_16.setSelected(false);
                check16_18.setSelected(false);
                statutComboBox.setValue(null);
                medecinIdField.clear();

                // Réinitialiser les messages d'erreur
                dateErrorLabel.setVisible(false);
                creneauErrorLabel.setVisible(false);
                statutErrorLabel.setVisible(false);
                medecinErrorLabel.setVisible(false);
        }

        @FXML
        void annuler(ActionEvent event) {
                try {
                        Parent parent = FXMLLoader.load(getClass().getResource("/AfficherDisponibilite.fxml"));
                        Scene scene = new Scene(parent);
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(scene);
                        stage.show();
                } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Erreur", "Une erreur s'est produite lors de la navigation.");
                }
        }

        @FXML
        private void afficherListeDisponibilites(ActionEvent event) {
                try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherDisponibilite.fxml"));
                        Parent root = loader.load();
                        Scene scene = ((Node) event.getSource()).getScene();
                        scene.setRoot(root);
                } catch (IOException e) {
                        e.printStackTrace();
                        // Afficher une alerte en cas d'erreur
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur de navigation");
                        alert.setHeaderText("Impossible d'accéder à la liste des disponibilités");
                        alert.setContentText("Une erreur s'est produite lors de la tentative d'accès à la liste des disponibilités.");
                        alert.showAndWait();
                }
        }
}

