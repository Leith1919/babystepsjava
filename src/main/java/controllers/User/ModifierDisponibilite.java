package controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import models.Disponibilite;
import services.User.DisponibiliteService;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ModifierDisponibilite implements Initializable {

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> statutComboBox;

    @FXML
    private CheckBox check9_11;

    @FXML
    private CheckBox check11_13;

    @FXML
    private CheckBox check14_16;

    @FXML
    private CheckBox check16_18;

    @FXML
    private Label dateErrorLabel;

    @FXML
    private Label statutErrorLabel;

    @FXML
    private Label creneauErrorLabel;

    @FXML
    private Button saveButton;

    private Disponibilite disponibilite; // la disponibilité à modifier
    private final DisponibiliteService service = new DisponibiliteService();
    private boolean isInitializingData = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser les labels d'erreur
        initializeErrorLabels();

        // Configurer les validations
        setupValidations();

        // Initialiser ComboBox
        statutComboBox.getItems().addAll("disponible", "indisponible", "congé", "formation");

        // Par défaut, le bouton est activé, les validations se feront au moment de la soumission
        saveButton.setDisable(false);
    }

    private void initializeErrorLabels() {
        // Configuration des labels d'erreur
        dateErrorLabel.setTextFill(Color.RED);
        dateErrorLabel.setVisible(false);

        statutErrorLabel.setTextFill(Color.RED);
        statutErrorLabel.setVisible(false);

        creneauErrorLabel.setTextFill(Color.RED);
        creneauErrorLabel.setVisible(false);
    }

    private void setupValidations() {
        // Validation de la date (doit être future)
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isInitializingData) validateDate(newVal);
        });

        // Validation du statut
        statutComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isInitializingData) validateStatut(newVal);
        });

        // Validation des créneaux horaires
        CheckBox[] checkBoxes = {check9_11, check11_13, check14_16, check16_18};
        for (CheckBox checkBox : checkBoxes) {
            checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (!isInitializingData) validateCreneaux();
            });
        }
    }

    public void initData(Disponibilite d) {
        try {
            isInitializingData = true;
            this.disponibilite = d;

            // Initialiser les champs avec les données existantes
            datePicker.setValue(d.getJour());
            statutComboBox.setValue(d.getStatutDisp());

            for (String heure : d.getHeuresDisp()) {
                switch (heure) {
                    case "9-11" -> check9_11.setSelected(true);
                    case "11-13" -> check11_13.setSelected(true);
                    case "14-16" -> check14_16.setSelected(true);
                    case "16-18" -> check16_18.setSelected(true);
                }
            }
        } finally {
            isInitializingData = false;
            // Valider les champs maintenant que l'initialisation est terminée
            // mais ne pas désactiver le bouton
            validateAllFieldsWithoutDisablingButton();
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

    private boolean validateCreneaux() {
        boolean auMoinsUnCreneau = check9_11.isSelected() || check11_13.isSelected()
                || check14_16.isSelected() || check16_18.isSelected();

        creneauErrorLabel.setVisible(!auMoinsUnCreneau);
        if (!auMoinsUnCreneau) {
            creneauErrorLabel.setText("Sélectionnez au moins un créneau horaire");
        }

        return auMoinsUnCreneau;
    }

    // Nouvelle méthode pour valider sans désactiver le bouton
    private boolean validateAllFieldsWithoutDisablingButton() {
        boolean dateValid = validateDate(datePicker.getValue());
        boolean statutValid = validateStatut(statutComboBox.getValue());
        boolean creneauxValid = validateCreneaux();

        return dateValid && statutValid && creneauxValid;
    }

    private boolean validateAllFields() {
        return validateAllFieldsWithoutDisablingButton();
    }

    @FXML
    private void enregistrerModification() {
        if (!validateAllFields()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "Veuillez corriger les erreurs avant d'enregistrer.");
            return;
        }

        try {
            LocalDate date = datePicker.getValue();
            String statut = statutComboBox.getValue();
            List<String> heures = new ArrayList<>();
            if (check9_11.isSelected()) heures.add("9-11");
            if (check11_13.isSelected()) heures.add("11-13");
            if (check14_16.isSelected()) heures.add("14-16");
            if (check16_18.isSelected()) heures.add("16-18");

            disponibilite.setJour(date);
            disponibilite.setStatutDisp(statut);
            disponibilite.setHeuresDisp(heures);

            service.modifierD(disponibilite);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Modification réussie !");
            ((Stage) datePicker.getScene().getWindow()).close(); // Fermer la fenêtre
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur s'est produite : " + e.getMessage());
        }
    }

    @FXML
    private void annuler() {
        ((Stage) datePicker.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}