package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tn.esprit.entities.suiviGrossesse;
import tn.esprit.services.SuiviGrossesseService;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AjoutSuiviGrossesse implements Initializable {

    private final SuiviGrossesseService suiviGrossesseService = new SuiviGrossesseService();
    private boolean isFormValid = true;

    @FXML
    private Button Ajout_SuiviGrossesse;

    @FXML
    private DatePicker Date_SuiviGrossesse;

    @FXML
    private Label dateError;

    @FXML
    private ComboBox<String> Etat_SuiviGrossesse;

    @FXML
    private Label etatError;

    @FXML
    private TextField Poids_SuiviGrossesse;

    @FXML
    private Label poidsError;

    @FXML
    private ComboBox<String> Symptomes_SuiviGrossesse;

    @FXML
    private Label symptomesError;

    @FXML
    private TextField Tension_SuiviGrossesse;

    @FXML
    private Label tensionError;

    @FXML
    private Button front;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialisation des listes déroulantes
        setupComboBoxes();

        // Initialisation du DatePicker à la date actuelle
        Date_SuiviGrossesse.setValue(LocalDate.now());

        // Ajout des écouteurs pour la validation en temps réel
        setupValidationListeners();

        // Initialize button handlers
        front.setOnAction(this::navigateToFront);
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
        Etat_SuiviGrossesse.setItems(etatsGrossesse);

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
        Symptomes_SuiviGrossesse.setItems(symptomes);
    }

    private void setupValidationListeners() {
        // Validation de la date (pas dans le futur)
        Date_SuiviGrossesse.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateDate(newValue);
        });

        // Validation du poids (nombre > 40)
        Poids_SuiviGrossesse.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePoids(newValue);
        });

        // Validation de la tension (entre 10 et 15)
        Tension_SuiviGrossesse.textProperty().addListener((observable, oldValue, newValue) -> {
            validateTension(newValue);
        });

        // Validation que l'état a été sélectionné
        Etat_SuiviGrossesse.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                etatError.setText("Veuillez sélectionner un état");
                isFormValid = false;
            } else {
                etatError.setText("");
            }
        });

        // Validation que les symptômes ont été sélectionnés
        Symptomes_SuiviGrossesse.valueProperty().addListener((observable, oldValue, newValue) -> {
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
        validateDate(Date_SuiviGrossesse.getValue());
        validatePoids(Poids_SuiviGrossesse.getText());
        validateTension(Tension_SuiviGrossesse.getText());

        if (Etat_SuiviGrossesse.getValue() == null || Etat_SuiviGrossesse.getValue().isEmpty()) {
            etatError.setText("Veuillez sélectionner un état");
            isFormValid = false;
        }

        if (Symptomes_SuiviGrossesse.getValue() == null || Symptomes_SuiviGrossesse.getValue().isEmpty()) {
            symptomesError.setText("Veuillez sélectionner un symptôme");
            isFormValid = false;
        }

        return isFormValid;
    }

    @FXML
    void Ajout_SuiviGrossesse(ActionEvent event) {
        try {
            // Valider le formulaire
            if (!validateForm()) {
                return;
            }

            // Conversion des champs
            LocalDate localDate = Date_SuiviGrossesse.getValue();
            Date dateSuivi = Date.valueOf(localDate);

            double poids = Double.parseDouble(Poids_SuiviGrossesse.getText());
            double tension = Double.parseDouble(Tension_SuiviGrossesse.getText());
            String symptomes = Symptomes_SuiviGrossesse.getValue();
            String etatGrossesse = Etat_SuiviGrossesse.getValue();

            // Création de l'objet suivi
            suiviGrossesse sg = new suiviGrossesse(dateSuivi, poids, tension, symptomes, etatGrossesse);
            suiviGrossesseService.ajouter(sg);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Suivi de grossesse ajouté avec succès !");

            // Redirection vers l'affichage
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherSuiviGrossesse.fxml"));
            Parent root = loader.load();

            AfficherSuiviGrossesseController controller = loader.getController();
            controller.loadSuivis(); // méthode à avoir dans le contrôleur d'affichage

            Stage stage = (Stage) Ajout_SuiviGrossesse.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + e.getMessage());
        }
    }

    @FXML
    private void navigateToFront(ActionEvent event) {
        try {
            // Charger la vue AfficherSuiviGrossesseFront.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherSuiviGrossesseFront.fxml"));
            Parent root = loader.load();

            // Obtenir la référence à la scène actuelle
            Stage stage = (Stage) front.getScene().getWindow();

            // Définir la nouvelle scène
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation",
                    "Impossible de charger la page AfficherSuiviGrossesseFront: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}