package controllers.User;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import models.*;
import services.User.*;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static utils.UserSession.currentUser;

public class AjouterRendezVous implements Initializable {

    @FXML private TextField motifField;
    @FXML private ListView<String> symptomesListView;

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
    @FXML private Label symptomesInfoLabel;
    @FXML
    private VBox rdvSubmenu;
    @FXML
    private Button rdvButton;
    @FXML
    private VBox dispSubmenu;

    @FXML
    private Button dispButton;

    private final UserService userService = new UserService();
    private final DisponibiliteService disponibiliteService = new DisponibiliteService();
    private final RendezVousService rendezVousService = new RendezVousService();

    private final int idPatient = 2; // ID patient fictif

    private final Pattern numbersOnlyPattern = Pattern.compile("^\\d+$");
    private final Pattern textPattern = Pattern.compile("^[\\p{L}\\s.,;:!?'\"()-]+$");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            initializeErrorLabels();
            setupValidationListeners();

            // Configuration des médecins
            List<User> medecins = userService.getAllMedecins();
            if (medecins != null && !medecins.isEmpty()) {
                medecinComboBox.getItems().addAll(medecins);
                // Personnalisation de l'affichage du ComboBox sans utiliser toString
                medecinComboBox.setCellFactory(lv -> new ListCell<User>() {
                    @Override
                    protected void updateItem(User user, boolean empty) {
                        super.updateItem(user, empty);
                        if (empty || user == null) {
                            setText(null);
                        } else {
                            setText(user.getNom() + " " + user.getPrenom());
                        }
                    }
                });

// Pour l'élément sélectionné (affichage dans la zone du bouton)
                medecinComboBox.setButtonCell(new ListCell<User>() {
                    @Override
                    protected void updateItem(User user, boolean empty) {
                        super.updateItem(user, empty);
                        if (empty || user == null) {
                            setText(null);
                        } else {
                            setText(user.getNom() + " " + user.getPrenom());
                        }
                    }
                });

            } else {
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucun médecin disponible dans le système");
            }

            // Configuration des heures
            List<String> heures = Arrays.asList("9-11", "11-13", "14-16", "16-18");
            heureComboBox.getItems().addAll(heures);

            // Configuration avancée de la liste des symptômes
            symptomesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            ObservableList<String> symptomes = FXCollections.observableArrayList(
                    "Fièvre", "Toux", "Fatigue", "Douleurs musculaires", "Maux de tête",
                    "Nausées", "Vertiges", "Essoufflement", "Perte d'appétit", "Insomnie",
                    "Douleur thoracique", "Diarrhée", "Vomissements", "Éruption cutanée",
                    "Difficultés respiratoires"
            );
            symptomesListView.setItems(symptomes);

            // Message informatif sur la sélection multiple
            if (symptomesInfoLabel != null) {
                symptomesInfoLabel.setText("Maintenez Ctrl pour sélectionner plusieurs symptômes");
                symptomesInfoLabel.setTextFill(Color.GRAY);
            }

            // Ajout d'un listener plus robuste pour la sélection multiple
            symptomesListView.getSelectionModel().getSelectedItems().addListener(
                    (ListChangeListener<String>) change -> {
                        while (change.next()) {
                            validateSymptomes(symptomesListView.getSelectionModel().getSelectedItems());
                        }
                    }
            );

            // Configuration du calendrier
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
        motifField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateMotif(newValue);
        });

        // Le listener pour les symptômes est ajouté dans initialize() avec le ListChangeListener

        traitementField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateTraitement(newValue);
        });

        jourPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateDate(newValue);
        });

        heureComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateHeure(newValue);
        });

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

    private boolean validateSymptomes(List<String> symptomes) {
        if (symptomes == null || symptomes.isEmpty()) {
            symptomesErrorLabel.setText("Veuillez sélectionner au moins un symptôme");
            symptomesErrorLabel.setVisible(true);
            return false;
        }
        symptomesErrorLabel.setVisible(false);
        return true;
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
        boolean symptomesValid = validateSymptomes(symptomesListView.getSelectionModel().getSelectedItems());
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

            // Récupération des symptômes sélectionnés
            ObservableList<String> symptomesSelectionnes = symptomesListView.getSelectionModel().getSelectedItems();
            String symptomes = String.join(", ", symptomesSelectionnes);

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
                    heureString, jour, medecin, currentUser.getId()
            );

            rendezVousService.ajouter(rv);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Rendez-vous ajouté avec succès !");
            clearForm();
            // Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            //stage.close();


            //  navigateToAfficherRendezVous(event);

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
            Parent afficherRoot = FXMLLoader.load(getClass().getResource("/AjouterRendezVousFront.fxml"));
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
        symptomesListView.getSelectionModel().clearSelection();
        traitementField.clear();
        notesArea.clear();
        heureComboBox.getSelectionModel().clearSelection();
        jourPicker.setValue(null);
        medecinComboBox.getSelectionModel().clearSelection();

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
    @FXML
    private void toggleRdvSubmenu() {
        rdvSubmenu.setVisible(!rdvSubmenu.isVisible());
        rdvSubmenu.setManaged(!rdvSubmenu.isManaged());

        // Changer le style du bouton en fonction de l'état du sous-menu
        if (rdvSubmenu.isVisible()) {
            rdvButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.4); -fx-background-radius: 8 8 0 0;");
        } else {
            rdvButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3); -fx-background-radius: 8;");
        }
    }

    // Méthode pour naviguer vers l'interface d'ajout de rendez-vous
    @FXML
    private void navigateToAjouterRDV() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterRendezVous.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) rdvButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour naviguer vers l'interface de consultation des rendez-vous
    @FXML
    private void navigateToConsulterRDV() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherRendezVous.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) rdvButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toggleDispSubmenu(ActionEvent actionEvent) {
        dispSubmenu.setVisible(!dispSubmenu.isVisible());
        dispSubmenu.setManaged(!dispSubmenu.isManaged());

        // Changer le style du bouton en fonction de l'état du sous-menu
        if (dispSubmenu.isVisible()) {
            dispButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.4); -fx-background-radius: 8 8 0 0;");
        } else {
            dispButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3); -fx-background-radius: 8;");
        }
    }

    public void navigateToAjouterDISP(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterDisponibilite.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Scene scene = rdvButton.getScene();

            // Remplacer le contenu de la scène par le formulaire de rendez-vous
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface Disponibilite: " + e.getMessage());
        }
    }

    public void navigateToConsulterDISP(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherDisponibilite.fxml"));
            Parent root = loader.load();

            Scene scene = rdvButton.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface ConsulterDispo: " + e.getMessage());
        }
    }

    public void initialiserAvecDisponibilite(Disponibilite dispo, String nomMedecin, String prenomMedecin) {
        // Créer une chaîne avec le nom et prénom formatés
        String nomCompletMedecin = "Dr. " + prenomMedecin + " " + nomMedecin;

        // Créer un modèle de cellule personnalisée pour le ComboBox
        ListCell<User> cell = new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : nomCompletMedecin);
            }
        };

        // Appliquer ce modèle au buttonCell pour l'affichage principal
        medecinComboBox.setButtonCell(cell);

        // Remplacer TOUS les éléments du ComboBox par ce seul élément personnalisé
        medecinComboBox.getItems().clear();

        try {
            // Récupérer l'objet User pour maintenir la validation
            User medecin = userService.getOneById(dispo.getIdMedecin());
            medecinComboBox.getItems().add(medecin);
            medecinComboBox.setValue(medecin);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Pré-remplir la date et désactiver les modifications
        jourPicker.setValue(dispo.getJour());

        // Configurer les heures disponibles
        List<String> heuresDisponibles = dispo.getHeuresDisp();
        if (heuresDisponibles != null && !heuresDisponibles.isEmpty()) {
            heureComboBox.getItems().clear();
            heureComboBox.getItems().addAll(heuresDisponibles);

            if (heuresDisponibles.size() == 1) {
                heureComboBox.setValue(heuresDisponibles.get(0));
            }
        }

        // Désactiver les champs pour éviter les modifications
        medecinComboBox.setDisable(true);
        jourPicker.setDisable(true);

        // Valider les champs pré-remplis
        if (medecinComboBox.getValue() != null) {
            validateMedecin(medecinComboBox.getValue());
        }
        validateDate(jourPicker.getValue());
        if (heureComboBox.getValue() != null) {
            validateHeure(heureComboBox.getValue());
        }
    }


}