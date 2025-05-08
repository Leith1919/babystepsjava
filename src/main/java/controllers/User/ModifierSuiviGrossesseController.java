package controllers.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.suiviGrossesse;
import services.User.SuiviGrossesseService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;

public class ModifierSuiviGrossesseController {

    @FXML private DatePicker dateSuiviField;
    @FXML private Label dateError;

    @FXML private TextField poidsField;
    @FXML private Label poidsError;

    @FXML private TextField tensionField;
    @FXML private Label tensionError;

    @FXML private ComboBox<String> symptomesField;
    @FXML private Label symptomesError;

    @FXML private ComboBox<String> etatGrossesseField;
    @FXML private Label etatError;

    private final SuiviGrossesseService service = new SuiviGrossesseService();
    private suiviGrossesse currentSuivi;
    private boolean isFormValid = true;

    public void initialize() {
        setupComboBoxes();
        setupValidationListeners();
    }

    private void setupComboBoxes() {
        // Remplir la liste déroulante d'états de grossesse
        ObservableList<String> etatsGrossesse = FXCollections.observableArrayList(
                "Normal",
                "À risque",
                "Stable",
                "Critique",
                "Excellente progression"
        );
        etatGrossesseField.setItems(etatsGrossesse);

        // Remplir la liste déroulante de symptômes
        ObservableList<String> symptomes = FXCollections.observableArrayList(
                "Aucun symptôme",
                "Nausées matinales",
                "Fatigue",
                "Maux de dos",
                "Gonflement des pieds",
                "Trouble du sommeil",
                "Autre"
        );
        symptomesField.setItems(symptomes);
    }

    private void setupValidationListeners() {
        // Validation de la date (pas dans le futur)
        dateSuiviField.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateDate(newValue);
        });

        // Validation du poids (nombre > 40)
        poidsField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePoids(newValue);
        });

        // Validation de la tension (entre 10 et 15)
        tensionField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateTension(newValue);
        });

        // Validation que l'état a été sélectionné
        etatGrossesseField.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                etatError.setText("Veuillez sélectionner un état");
                isFormValid = false;
            } else {
                etatError.setText("");
            }
        });

        // Validation que les symptômes ont été sélectionnés
        symptomesField.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                symptomesError.setText("Veuillez sélectionner un symptôme");
                isFormValid = false;
            } else {
                symptomesError.setText("");
            }
        });
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            dateError.setText("Date obligatoire");
            isFormValid = false;
        } else if (date.isAfter(LocalDate.now())) {
            dateError.setText("La date ne peut pas être dans le futur");
            isFormValid = false;
        } else {
            dateError.setText("");
        }
    }

    private void validatePoids(String poids) {
        try {
            double poidsValue = Double.parseDouble(poids);
            if (poidsValue < 40) {
                poidsError.setText("Le poids doit être supérieur à 40");
                isFormValid = false;
            } else {
                poidsError.setText("");
            }
        } catch (NumberFormatException e) {
            poidsError.setText("Veuillez entrer un nombre valide");
            isFormValid = false;
        }
    }

    private void validateTension(String tension) {
        try {
            double tensionValue = Double.parseDouble(tension);
            if (tensionValue < 10 || tensionValue > 15) {
                tensionError.setText("La tension doit être entre 10 et 15");
                isFormValid = false;
            } else {
                tensionError.setText("");
            }
        } catch (NumberFormatException e) {
            tensionError.setText("Veuillez entrer un nombre valide");
            isFormValid = false;
        }
    }

    private boolean validateForm() {
        isFormValid = true;

        // Vérifier tous les champs
        validateDate(dateSuiviField.getValue());
        validatePoids(poidsField.getText());
        validateTension(tensionField.getText());

        if (etatGrossesseField.getValue() == null || etatGrossesseField.getValue().isEmpty()) {
            etatError.setText("Veuillez sélectionner un état");
            isFormValid = false;
        }

        if (symptomesField.getValue() == null || symptomesField.getValue().isEmpty()) {
            symptomesError.setText("Veuillez sélectionner un symptôme");
            isFormValid = false;
        }

        return isFormValid;
    }

    public void setSuiviGrossesse(suiviGrossesse sg) {
        this.currentSuivi = sg;

        // Convert java.util.Date to java.time.LocalDate
        LocalDate localDate = new java.sql.Date(sg.getDateSuivi().getTime()).toLocalDate();
        dateSuiviField.setValue(localDate);

        poidsField.setText(String.valueOf(sg.getPoids()));
        tensionField.setText(String.valueOf(sg.getTension()));

        // Trouver et sélectionner la valeur dans les ComboBox
        String symptomes = sg.getSymptomes();
        if (symptomes != null && !symptomes.isEmpty()) {
            // Chercher dans les items de la ComboBox
            for (String item : symptomesField.getItems()) {
                if (item.equals(symptomes)) {
                    symptomesField.setValue(item);
                    break;
                }
            }
            // Si on ne trouve pas, on ajoute et on sélectionne
            if (symptomesField.getValue() == null) {
                symptomesField.getItems().add(symptomes);
                symptomesField.setValue(symptomes);
            }
        }

        String etatGrossesse = sg.getEtatGrossesse();
        if (etatGrossesse != null && !etatGrossesse.isEmpty()) {
            // Chercher dans les items de la ComboBox
            for (String item : etatGrossesseField.getItems()) {
                if (item.equals(etatGrossesse)) {
                    etatGrossesseField.setValue(item);
                    break;
                }
            }
            // Si on ne trouve pas, on ajoute et on sélectionne
            if (etatGrossesseField.getValue() == null) {
                etatGrossesseField.getItems().add(etatGrossesse);
                etatGrossesseField.setValue(etatGrossesse);
            }
        }
    }

    @FXML
    void handleSave() {
        try {
            // Valider le formulaire
            if (!validateForm()) {
                return;
            }

            double poids = Double.parseDouble(poidsField.getText());
            double tension = Double.parseDouble(tensionField.getText());
            LocalDate localDate = dateSuiviField.getValue();
            Date dateSuivi = java.sql.Date.valueOf(localDate);

            currentSuivi.setDateSuivi(dateSuivi);
            currentSuivi.setPoids(poids);
            currentSuivi.setTension(tension);
            currentSuivi.setSymptomes(symptomesField.getValue());
            currentSuivi.setEtatGrossesse(etatGrossesseField.getValue());

            service.modifier(currentSuivi);

            // Montrer une alerte de succès
            showSuccessAlert("Modification réussie", "Le suivi de grossesse a été modifié avec succès.");

            Stage stage = (Stage) dateSuiviField.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erreur de base de données", "Une erreur est survenue lors de la modification.");
        } catch (NumberFormatException e) {
            showErrorAlert("Erreur de saisie", "Veuillez entrer des valeurs valides pour le poids et la tension.");
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}