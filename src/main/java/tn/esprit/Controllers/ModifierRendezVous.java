package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tn.esprit.entites.RendezVous;
import tn.esprit.services.RendezVousService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ModifierRendezVous implements Initializable {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> heureComboBox;
    @FXML private TextField motifField;
    @FXML private TextArea symptomesArea;
    @FXML private TextArea traitementArea;
    @FXML private TextArea notesArea;

    // Labels d'erreur pour validation
    @FXML private Label motifErrorLabel;
    @FXML private Label symptomesErrorLabel;
    @FXML private Label traitementErrorLabel;
    @FXML private Label heureErrorLabel;
    @FXML private Label jourErrorLabel;

    private RendezVous rendezVous;
    private final RendezVousService rendezVousService = new RendezVousService();

    // Expression régulière pour valider que le texte ne contient pas uniquement des chiffres
    private final Pattern numbersOnlyPattern = Pattern.compile("^\\d+$");
    // Expression régulière pour valider le format texte (lettres, espaces, quelques caractères spéciaux)
    private final Pattern textPattern = Pattern.compile("^[\\p{L}\\s.,;:!?'\"()-]+$");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the hours dropdown
        List<String> heures = new ArrayList<>();
        heures.add("9-11");
        heures.add("11-13");
        heures.add("14-16");
        heures.add("16-18");
        ObservableList<String> observableHeures = FXCollections.observableArrayList(heures);
        heureComboBox.setItems(observableHeures);

        // Initialiser les labels d'erreur
        initializeErrorLabels();

        // Configurer les écouteurs de validation
        setupValidationListeners();

        // Désactiver les dates passées dans le DatePicker
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
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
    }

    private void setupValidationListeners() {
        // Validation des champs textuels
        motifField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateMotif(newValue);
        });

        symptomesArea.textProperty().addListener((observable, oldValue, newValue) -> {
            validateSymptomes(newValue);
        });

        traitementArea.textProperty().addListener((observable, oldValue, newValue) -> {
            validateTraitement(newValue);
        });

        // Validation de la date (doit être future)
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateDate(newValue);
        });

        // Validation de l'heure
        heureComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateHeure(newValue);
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

    private boolean validateAllFields() {
        boolean motifValid = validateMotif(motifField.getText());
        boolean symptomesValid = validateSymptomes(symptomesArea.getText());
        boolean traitementValid = validateTraitement(traitementArea.getText());
        boolean dateValid = validateDate(datePicker.getValue());
        boolean heureValid = validateHeure(heureComboBox.getValue());

        return motifValid && symptomesValid && traitementValid && dateValid && heureValid;
    }

    /**
     * Initialize the form with the rendez-vous data
     * @param rdv The rendez-vous to modify
     */
    public void initData(RendezVous rdv) {
        this.rendezVous = rdv;

        // Populate fields with data from the rendez-vous
        motifField.setText(rdv.getMotif());
        symptomesArea.setText(rdv.getSymptomes());
        traitementArea.setText(rdv.getTraitementEnCours());
        notesArea.setText(rdv.getNotes());

        // Set the date picker
        if (rdv.getJour() != null) {
            datePicker.setValue(rdv.getJour());
        }

        // Set the selected hour
        if (rdv.getHeureString() != null) {
            heureComboBox.setValue(rdv.getHeureString());
        }
    }

    @FXML
    private void enregistrerModification() {
        try {
            // Valider tous les champs avant de procéder
            if (!validateAllFields()) {
                showAlert(Alert.AlertType.WARNING, "Validation",
                        "Veuillez corriger les erreurs dans le formulaire avant de continuer.");
                return;
            }

            // Update rendez-vous object with form data
            rendezVous.setMotif(motifField.getText());
            rendezVous.setSymptomes(symptomesArea.getText());
            rendezVous.setTraitementEnCours(traitementArea.getText());
            rendezVous.setNotes(notesArea.getText());
            rendezVous.setJour(datePicker.getValue());
            rendezVous.setHeureString(heureComboBox.getValue());

            // Save changes to database
            rendezVousService.modifierD(rendezVous);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Rendez-vous modifié avec succès !");

            // Close the window
            Stage stage = (Stage) motifField.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur SQL",
                    "Erreur lors de la modification du rendez-vous: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur inattendue s'est produite: " + e.getMessage());
        }
    }

    @FXML
    private void annuler() {
        // Close the window without saving changes
        Stage stage = (Stage) motifField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}