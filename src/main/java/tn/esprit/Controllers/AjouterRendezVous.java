package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import tn.esprit.entites.*;
import tn.esprit.services.*;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AjouterRendezVous implements Initializable {

    @FXML private TextField motifField;
    @FXML private TextField symptomesField;
    @FXML private TextField traitementField;
    @FXML private TextArea notesArea;
    @FXML private ComboBox<String> heureComboBox;
    @FXML private DatePicker jourPicker;
    @FXML private ComboBox<User> medecinComboBox;

    // Labels d'erreur pour validation
    @FXML private Label motifErrorLabel;
    @FXML private Label symptomesErrorLabel;
    @FXML private Label traitementErrorLabel;
    @FXML private Label heureErrorLabel;
    @FXML private Label jourErrorLabel;
    @FXML private Label medecinErrorLabel;

    private final UserService userService = new UserService();
    private final DisponibiliteService disponibiliteService = new DisponibiliteService();
    private final RendezVousService rendezVousService = new RendezVousService();

    private final int idPatient = 67; // ID patient fictif

    // Expression régulière pour valider que le texte ne contient pas uniquement des chiffres
    private final Pattern numbersOnlyPattern = Pattern.compile("^\\d+$");
    // Expression régulière pour valider le format texte (lettres, espaces, quelques caractères spéciaux)
    private final Pattern textPattern = Pattern.compile("^[\\p{L}\\s.,;:!?'\"()-]+$");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialiser les labels d'erreur
            initializeErrorLabels();

            // Configurer les écouteurs de validation
            setupValidationListeners();

            // Charger les médecins
            List<User> medecins = userService.getAllMedecins();
            if (medecins != null && !medecins.isEmpty()) {
                medecinComboBox.getItems().addAll(medecins);
            } else {
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucun médecin disponible dans le système");
            }

            // Remplir les heures possibles
            List<String> heures = Arrays.asList("9-11", "11-13", "14-16", "16-18");
            heureComboBox.getItems().addAll(heures);

            // Désactiver les dates passées dans le DatePicker
            jourPicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isBefore(LocalDate.now()));
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur d'initialisation",
                    "Impossible de charger les données nécessaires: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur d'initialisation",
                    "Une erreur inattendue s'est produite: " + e.getMessage());
        }
    }

    private void initializeErrorLabels() {
        // Configuration des labels d'erreur
        motifErrorLabel.setTextFill(Color.RED);
        motifErrorLabel.setVisible(false);

        symptomesErrorLabel.setTextFill(Color.RED);
        symptomesErrorLabel.setVisible(false);

        traitementErrorLabel.setTextFill(Color.RED);
        traitementErrorLabel.setVisible(false);

        heureErrorLabel.setTextFill(Color.RED);
        heureErrorLabel.setVisible(false);

        jourErrorLabel.setTextFill(Color.RED);
        jourErrorLabel.setVisible(false);

        medecinErrorLabel.setTextFill(Color.RED);
        medecinErrorLabel.setVisible(false);
    }

    private void setupValidationListeners() {
        // Validation des champs textuels
        motifField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateMotif(newValue);
        });

        symptomesField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateSymptomes(newValue);
        });

        traitementField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateTraitement(newValue);
        });

        // Validation de la date (doit être future)
        jourPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateDate(newValue);
        });

        // Validation de l'heure
        heureComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateHeure(newValue);
        });

        // Validation du médecin
        medecinComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateMedecin(newValue);
        });
    }

    private boolean validateMotif(String text) {
        if (text == null || text.trim().isEmpty()) {
            motifErrorLabel.setText("Le motif est obligatoire");
            motifErrorLabel.setVisible(true);
            return false;
        } else if (text.length() < 3) {
            motifErrorLabel.setText("Le motif doit contenir au moins 3 caractères");
            motifErrorLabel.setVisible(true);
            return false;
        } else if (numbersOnlyPattern.matcher(text).matches()) {
            motifErrorLabel.setText("Le motif ne peut pas être uniquement numérique");
            motifErrorLabel.setVisible(true);
            return false;
        } else if (!textPattern.matcher(text).matches()) {
            motifErrorLabel.setText("Le motif contient des caractères non autorisés");
            motifErrorLabel.setVisible(true);
            return false;
        } else {
            motifErrorLabel.setVisible(false);
            return true;
        }
    }

    private boolean validateSymptomes(String text) {
        if (text == null || text.trim().isEmpty()) {
            symptomesErrorLabel.setText("Les symptômes sont obligatoires");
            symptomesErrorLabel.setVisible(true);
            return false;
        } else if (text.length() < 3) {
            symptomesErrorLabel.setText("Les symptômes doivent contenir au moins 3 caractères");
            symptomesErrorLabel.setVisible(true);
            return false;
        } else if (numbersOnlyPattern.matcher(text).matches()) {
            symptomesErrorLabel.setText("Les symptômes ne peuvent pas être uniquement numériques");
            symptomesErrorLabel.setVisible(true);
            return false;
        } else if (!textPattern.matcher(text).matches()) {
            symptomesErrorLabel.setText("Les symptômes contiennent des caractères non autorisés");
            symptomesErrorLabel.setVisible(true);
            return false;
        } else {
            symptomesErrorLabel.setVisible(false);
            return true;
        }
    }

    private boolean validateTraitement(String text) {
        if (text == null || text.trim().isEmpty()) {
            traitementErrorLabel.setText("Le traitement est obligatoire");
            traitementErrorLabel.setVisible(true);
            return false;
        } else if (text.length() < 3) {
            traitementErrorLabel.setText("Le traitement doit contenir au moins 3 caractères");
            traitementErrorLabel.setVisible(true);
            return false;
        } else if (numbersOnlyPattern.matcher(text).matches()) {
            traitementErrorLabel.setText("Le traitement ne peut pas être uniquement numérique");
            traitementErrorLabel.setVisible(true);
            return false;
        } else if (!textPattern.matcher(text).matches()) {
            traitementErrorLabel.setText("Le traitement contient des caractères non autorisés");
            traitementErrorLabel.setVisible(true);
            return false;
        } else {
            traitementErrorLabel.setVisible(false);
            return true;
        }
    }

    private boolean validateDate(LocalDate date) {
        if (date == null) {
            jourErrorLabel.setText("La date est obligatoire");
            jourErrorLabel.setVisible(true);
            return false;
        } else if (date.isBefore(LocalDate.now())) {
            jourErrorLabel.setText("La date doit être dans le futur");
            jourErrorLabel.setVisible(true);
            return false;
        } else {
            jourErrorLabel.setVisible(false);
            return true;
        }
    }

    private boolean validateHeure(String heure) {
        if (heure == null || heure.trim().isEmpty()) {
            heureErrorLabel.setText("L'heure est obligatoire");
            heureErrorLabel.setVisible(true);
            return false;
        } else {
            heureErrorLabel.setVisible(false);
            return true;
        }
    }

    private boolean validateMedecin(User medecin) {
        if (medecin == null) {
            medecinErrorLabel.setText("Le médecin est obligatoire");
            medecinErrorLabel.setVisible(true);
            return false;
        } else {
            medecinErrorLabel.setVisible(false);
            return true;
        }
    }

    private boolean validateAllFields() {
        boolean motifValid = validateMotif(motifField.getText());
        boolean symptomesValid = validateSymptomes(symptomesField.getText());
        boolean traitementValid = validateTraitement(traitementField.getText());
        boolean dateValid = validateDate(jourPicker.getValue());
        boolean heureValid = validateHeure(heureComboBox.getValue());
        boolean medecinValid = validateMedecin(medecinComboBox.getValue());

        return motifValid && symptomesValid && traitementValid
                && dateValid && heureValid && medecinValid;
    }

    @FXML
    private void ajouterRendezVous(ActionEvent event) {
        try {
            // Valider tous les champs avant de procéder
            if (!validateAllFields()) {
                showAlert(Alert.AlertType.WARNING, "Validation",
                        "Veuillez corriger les erreurs dans le formulaire avant de continuer.");
                return;
            }

            String motif = motifField.getText();
            String symptomes = symptomesField.getText();
            String traitement = traitementField.getText();
            String notes = notesArea.getText();
            String heureString = heureComboBox.getValue();
            LocalDate jour = jourPicker.getValue();
            User medecin = medecinComboBox.getValue();

            // Vérifier la disponibilité du médecin
            Disponibilite dispo = disponibiliteService.trouverDisponibilite(medecin.getId(), jour, heureString);

            if (dispo == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur de disponibilité",
                        "Le médecin n'est pas disponible à cette date et cette heure.");
                return;
            }

            // Créer et ajouter le rendez-vous
            RendezVous rv = new RendezVous(
                    dispo, motif, symptomes, traitement, notes,
                    "En attente", LocalDate.now(),
                    heureString, jour, medecin, idPatient
            );

            rendezVousService.ajouter(rv);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Rendez-vous ajouté avec succès !");
            clearForm();
            navigateToAfficherRendezVous(event);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur SQL",
                    "Erreur lors de l'ajout du rendez-vous: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur inattendue s'est produite: " + e.getMessage());
        }
    }

    private void navigateToAfficherRendezVous(ActionEvent event) {
        try {
            Parent afficherRoot = FXMLLoader.load(getClass().getResource("/AfficherRendezVous.fxml"));
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(afficherRoot));
            stage.setTitle("Liste des Rendez-vous");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation",
                    "Impossible d'accéder à la liste des rendez-vous: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur inattendue s'est produite: " + e.getMessage());
        }
    }

    private void clearForm() {
        motifField.clear();
        symptomesField.clear();
        traitementField.clear();
        notesArea.clear();
        heureComboBox.getSelectionModel().clearSelection();
        jourPicker.setValue(null);
        medecinComboBox.getSelectionModel().clearSelection();

        // Réinitialiser les messages d'erreur
        motifErrorLabel.setVisible(false);
        symptomesErrorLabel.setVisible(false);
        traitementErrorLabel.setVisible(false);
        heureErrorLabel.setVisible(false);
        jourErrorLabel.setVisible(false);
        medecinErrorLabel.setVisible(false);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void annuler(ActionEvent event) {
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("/AfficherRendezVous.fxml"));
            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation",
                    "Impossible d'accéder à la liste des rendez-vous: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur inattendue s'est produite: " + e.getMessage());
        }
    }
}