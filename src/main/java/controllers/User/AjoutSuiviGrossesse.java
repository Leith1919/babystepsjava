package controllers.User;

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
import javafx.stage.StageStyle;
import models.User;
import models.suiviGrossesse;
import services.User.EmailService;
import services.User.SuiviGrossesseService;
import services.User.UserService;
import javafx.scene.Node;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AjoutSuiviGrossesse implements Initializable {

    private final SuiviGrossesseService suiviGrossesseService = new SuiviGrossesseService();
    private final UserService userService = new UserService();
    private final EmailService emailService = new EmailService();
    private boolean isFormValid = true;

    @FXML
    private Button Ajout_SuiviGrossesse;

    @FXML
    private Button Annuler_SuiviGrossesse;

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

    @FXML
    private ComboBox<String> Patient_id;

    @FXML
    private Label patientError;

    @FXML
    private TextArea Notes_SuiviGrossesse;
    @FXML
    void navigateToListeSuiviGrossesse(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/affichersuivigrossesse.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Liste des Suivis de Grossesse");
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de Navigation");
            alert.setHeaderText("Impossible de charger la page");
            alert.setContentText("Une erreur s'est produite lors du chargement de la page de liste des suivis de grossesse. Détails: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser les ComboBox
        setupComboBoxes();

        // Charger les patients - assurez-vous que cette méthode s'exécute correctement
        loadPatients();

        // Vérifier que le ComboBox Patient_id n'est pas vide après le chargement
        System.out.println("Nombre de patients chargés: " +
                (Patient_id.getItems() != null ? Patient_id.getItems().size() : 0));

        // Initialiser la date à aujourd'hui
        Date_SuiviGrossesse.setValue(LocalDate.now());

        // Setup validation en temps réel
        setupValidationListeners();

        // Lier les boutons aux actions
        front.setOnAction(this::navigateToFront);
        Ajout_SuiviGrossesse.setOnAction(this::Ajout_SuiviGrossesse);

        if (Annuler_SuiviGrossesse != null) {
            Annuler_SuiviGrossesse.setOnAction(this::annulerAction);
        }

        // Configurer la fenêtre
        configureStage();
    }

    private void configureStage() {
        javafx.application.Platform.runLater(() -> {
            try {
                Stage stage = (Stage) Ajout_SuiviGrossesse.getScene().getWindow();
                stage.setResizable(true);
                stage.setMinWidth(900);
                stage.setMinHeight(650);
                // Ne pas essayer de réinitialiser le style après l'affichage
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de configuration de la fenêtre: " + e.getMessage());
            }
        });
    }

    private void setupComboBoxes() {
        ObservableList<String> etatsGrossesse = FXCollections.observableArrayList(
                "Normal", "À risque", "Stable", "Critique", "Excellente progression"
        );
        Etat_SuiviGrossesse.setItems(etatsGrossesse);
        // Sélectionner par défaut
        Etat_SuiviGrossesse.setValue("Normal");

        ObservableList<String> symptomes = FXCollections.observableArrayList(
                "Aucun symptôme", "Nausées matinales", "Fatigue", "Maux de dos",
                "Gonflement des pieds", "Trouble du sommeil", "Autre"
        );
        Symptomes_SuiviGrossesse.setItems(symptomes);
        // Sélectionner par défaut
        Symptomes_SuiviGrossesse.setValue("Aucun symptôme");
    }

    private void loadPatients() {
        try {
            // Récupérer tous les utilisateurs
            List<User> allUsers = userService.recuperer();

            // Créer une liste pour stocker les patients
            ObservableList<String> patientItems = FXCollections.observableArrayList();

            // Vérifier s'il y a des utilisateurs récupérés
            System.out.println("Nombre total d'utilisateurs récupérés: " + allUsers.size());

            // Parcourir tous les utilisateurs
            for (User u : allUsers) {
                // Déboguer les rôles pour voir ce qui est récupéré
                System.out.println("User ID: " + u.getId() + ", Nom: " + u.getNom() +
                        ", Prénom: " + u.getPrenom() + ", Rôles: " + u.getRoles());

                // Si l'utilisateur a un rôle qui contient "ROLE_PATIENT"
                if (u.getRoles() != null && u.getRoles().contains("ROLE_PATIENT")) {
                    String patientEntry = u.getId() + " - " + u.getNom() + " " + u.getPrenom();
                    patientItems.add(patientEntry);
                    System.out.println("Ajout du patient: " + patientEntry);
                }
            }

            // Si aucun patient n'est trouvé, ajouter une entrée par défaut pour le débogage
            if (patientItems.isEmpty()) {
                patientItems.add("Aucun patient trouvé dans la base de données");
                System.out.println("Aucun patient avec ROLE_PATIENT trouvé");
            }

            // Définir les éléments du ComboBox
            Patient_id.setItems(patientItems);

            // Sélectionner le premier élément
            if (!patientItems.isEmpty()) {
                Patient_id.setValue(patientItems.get(0));
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement des patients: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors du chargement des patients: " + e.getMessage());

            // En cas d'erreur, ajouter au moins une entrée pour éviter les NPE
            ObservableList<String> fallbackList = FXCollections.observableArrayList(
                    "Erreur de chargement des patients");
            Patient_id.setItems(fallbackList);
            Patient_id.setValue(fallbackList.get(0));
        }
    }

    private void setupValidationListeners() {
        Date_SuiviGrossesse.valueProperty().addListener((observable, oldValue, newValue) -> validateDate(newValue));
        Poids_SuiviGrossesse.textProperty().addListener((observable, oldValue, newValue) -> validatePoids(newValue));
        Tension_SuiviGrossesse.textProperty().addListener((observable, oldValue, newValue) -> validateTension(newValue));
        Etat_SuiviGrossesse.valueProperty().addListener((observable, oldValue, newValue) -> validateEtat(newValue));
        Symptomes_SuiviGrossesse.valueProperty().addListener((observable, oldValue, newValue) -> validateSymptomes(newValue));
        Patient_id.valueProperty().addListener((observable, oldValue, newValue) -> validatePatient(newValue));
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
        if (poids == null || poids.isEmpty()) {
            poidsError.setText("Poids obligatoire");
            isFormValid = false;
            return;
        }

        try {
            double poidsValue = Double.parseDouble(poids);
            if (poidsValue < 40) {
                poidsError.setText("Le poids doit être supérieur à 40 kg");
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
        if (tension == null || tension.isEmpty()) {
            tensionError.setText("Tension obligatoire");
            isFormValid = false;
            return;
        }

        try {
            // Vérifier si la tension est au format "systolique/diastolique"
            if (tension.contains("/")) {
                String[] parts = tension.split("/");
                if (parts.length == 2) {
                    int systolique = Integer.parseInt(parts[0].trim());
                    int diastolique = Integer.parseInt(parts[1].trim());

                    if (systolique < 90 || systolique > 180) {
                        tensionError.setText("Systolique doit être entre 90 et 180");
                        isFormValid = false;
                    } else if (diastolique < 60 || diastolique > 120) {
                        tensionError.setText("Diastolique doit être entre 60 et 120");
                        isFormValid = false;
                    } else {
                        tensionError.setText("");
                    }
                    return;
                }
            }

            // Si ce n'est pas au format x/y, vérifier comme un nombre simple
            double tensionValue = Double.parseDouble(tension);
            if (tensionValue < 10 || tensionValue > 15) {
                tensionError.setText("La tension doit être entre 10 et 15");
                isFormValid = false;
            } else {
                tensionError.setText("");
            }
        } catch (NumberFormatException e) {
            tensionError.setText("Format invalide (utilisez x/y ou un nombre)");
            isFormValid = false;
        }
    }

    private void validateEtat(String etat) {
        if (etat == null || etat.isEmpty()) {
            etatError.setText("Veuillez sélectionner un état");
            isFormValid = false;
        } else {
            etatError.setText("");
        }
    }

    private void validateSymptomes(String symptome) {
        if (symptome == null || symptome.isEmpty()) {
            symptomesError.setText("Veuillez sélectionner un symptôme");
            isFormValid = false;
        } else {
            symptomesError.setText("");
        }
    }

    private void validatePatient(String patient) {
        if (patient == null || patient.isEmpty() || patient.equals("Aucun patient trouvé dans la base de données")
                || patient.equals("Erreur de chargement des patients")) {
            patientError.setText("Veuillez sélectionner un patient valide");
            isFormValid = false;
        } else {
            patientError.setText("");
        }
    }

    private boolean validateForm() {
        isFormValid = true;

        validateDate(Date_SuiviGrossesse.getValue());
        validatePoids(Poids_SuiviGrossesse.getText());
        validateTension(Tension_SuiviGrossesse.getText());
        validateEtat(Etat_SuiviGrossesse.getValue());
        validateSymptomes(Symptomes_SuiviGrossesse.getValue());
        validatePatient(Patient_id.getValue());

        return isFormValid;
    }

    @FXML
    void Ajout_SuiviGrossesse(ActionEvent event) {
        try {
            // Commencer par réinitialiser l'état de validation du formulaire
            isFormValid = true;

            // Valider tous les champs
            if (!validateForm()) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Veuillez corriger les champs invalides");
                return;
            }

            // Récupérer les valeurs des champs
            LocalDate localDate = Date_SuiviGrossesse.getValue();
            Date dateSuivi = Date.valueOf(localDate);

            String poidsStr = Poids_SuiviGrossesse.getText().trim();
            double poids = Double.parseDouble(poidsStr);

            String tensionStr = Tension_SuiviGrossesse.getText().trim();
            double tension;

            // Amélioration du traitement de la tension
            if (tensionStr.contains("/")) {
                String[] parts = tensionStr.split("/");
                try {
                    int systolique = Integer.parseInt(parts[0].trim());
                    int diastolique = Integer.parseInt(parts[1].trim());
                    tension = (systolique + diastolique) / 2.0;
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    tensionError.setText("Format de tension invalide");
                    isFormValid = false;
                    return;
                }
            } else {
                tension = Double.parseDouble(tensionStr);
            }

            String symptomes = Symptomes_SuiviGrossesse.getValue();
            String etatGrossesse = Etat_SuiviGrossesse.getValue();
            String notes = Notes_SuiviGrossesse.getText();

            // Récupération et traitement de l'ID patient améliorée
            String selectedPatient = Patient_id.getValue();
            if (selectedPatient == null || selectedPatient.equals("Aucun patient trouvé dans la base de données")
                    || selectedPatient.equals("Erreur de chargement des patients")) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un patient valide");
                return;
            }

            int patientId;

            try {
                // Extraire l'ID du patient du format "ID - Nom Prénom"
                String idPart = selectedPatient.split(" - ")[0].trim();
                patientId = Integer.parseInt(idPart);
                System.out.println("ID patient sélectionné: " + patientId); // Log pour débogage
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Format d'ID patient invalide: " + selectedPatient);
                return;
            }

            // Créer l'objet suivi
            suiviGrossesse sg = new suiviGrossesse(dateSuivi, poids, tension, symptomes, etatGrossesse, patientId);

            // Ajouter les notes si la classe suiviGrossesse le prend en charge
            // Si vous avez modifié la classe suiviGrossesse pour inclure les notes:
            // sg.setNotes(notes);

            // Tentative d'ajout avec affichage clair des erreurs
            try {
                // Ajouter le suivi de grossesse à la base de données
                suiviGrossesseService.ajouter(sg);

                // Récupérer l'objet utilisateur complet pour accéder à l'email
                User patiente = userService.getUserById(patientId);

                if (patiente != null && patiente.getEmail() != null && !patiente.getEmail().isEmpty()) {
                    // Envoyer l'email de notification
                    boolean emailEnvoye = emailService.envoyerEmailNouveauSuivi(patiente, sg);

                    if (emailEnvoye) {
                        showAlert(Alert.AlertType.INFORMATION, "Succès",
                                "Suivi de grossesse ajouté avec succès et notification envoyée à " + patiente.getEmail());
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Attention",
                                "Suivi de grossesse ajouté avec succès mais impossible d'envoyer l'email de notification.");
                    }
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            "Suivi de grossesse ajouté avec succès mais aucune adresse email disponible pour la notification.");
                }

                // Naviguer vers l'affichage des suivis
               // navigateToAffichage();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur SQL",
                        "Erreur lors de l'ajout en base de données: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur inattendue",
                    "Une erreur s'est produite: " + e.getMessage());
        }
    }

    @FXML
    private void annulerAction(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Voulez-vous vraiment annuler ? Les données non enregistrées seront perdues.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    navigateToAffichage();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur Navigation",
                            "Impossible de revenir à la vue d'affichage : " + e.getMessage());
                }
            }
        });
    }

    private void navigateToAffichage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Back.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène
            Scene scene = new Scene(root);

            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) Ajout_SuiviGrossesse.getScene().getWindow();

            // Configurer la fenêtre
            stage.setMinWidth(900);
            stage.setMinHeight(650);
            stage.setResizable(true);

            // Appliquer la nouvelle scène
            stage.setScene(scene);

            // Si nécessaire, initialiser le contrôleur
            try {
                AfficherSuiviGrossesseController controller = loader.getController();
                if (controller != null) {
                    controller.loadSuivis();
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de l'initialisation du contrôleur: " + e.getMessage());
                // Continuer car ce n'est pas bloquant
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la navigation : " + e.getMessage());
        }
    }

    @FXML
    private void navigateToFront(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherSuiviGrossesseFront.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène
            Scene scene = new Scene(root);

            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) front.getScene().getWindow();

            // Configurer la fenêtre
            stage.setMinWidth(900);
            stage.setMinHeight(650);
            stage.setResizable(true);

            // Appliquer la nouvelle scène
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur Navigation",
                    "Impossible de charger AfficherSuiviGrossesseFront : " + e.getMessage());
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