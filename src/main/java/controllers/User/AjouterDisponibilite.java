package controllers.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import models.Disponibilite;
import models.User;
import services.User.DisponibiliteService;
import services.User.UserService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
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
        private ComboBox<User> medecinComboBox; // Changé de TextField à ComboBox<User>

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

        @FXML
        private VBox rdvSubmenu;

        @FXML
        private Button rdvButton;

        @FXML
        private VBox dispSubmenu;

        @FXML
        private Button dispButton;

        private final DisponibiliteService service = new DisponibiliteService();
        private final UserService userService = new UserService(); // Ajout du service des utilisateurs

        // Dans la méthode initialize
        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {

                // Configuration du rendu personnalisé pour la ComboBox médecin
                medecinComboBox.setCellFactory(param -> new ListCell<User>() {
                        @Override
                        protected void updateItem(User user, boolean empty) {
                                super.updateItem(user, empty);

                                if (empty || user == null) {
                                        setText(null);
                                } else {
                                        setText(user.getNomComplet());
                                }
                        }
                });

                medecinComboBox.setButtonCell(new ListCell<User>() {
                        @Override
                        protected void updateItem(User user, boolean empty) {
                                super.updateItem(user, empty);

                                if (empty || user == null) {
                                        setText(null);
                                } else {
                                        setText(user.getNomComplet());
                                }
                        }
                });

                // Chargement de la liste des médecins
                try {
                        List<User> medecins = userService.getAllMedecins();
                        if (medecins != null && !medecins.isEmpty()) {
                                medecinComboBox.getItems().addAll(medecins);
                        } else {
                                showAlert("Avertissement", "Aucun médecin disponible dans le système");
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                        showAlert("Erreur", "Impossible de charger la liste des médecins: " + e.getMessage());
                }

                initializeErrorLabels();
                setupValidationListeners();
        }

        private void initializeErrorLabels() {
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
                datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
                        validateDate(newValue);
                });

                medecinComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                        validateMedecin(newValue);
                });

                statutComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                        validateStatut(newValue);
                });

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

        // Nouvelle méthode de validation pour ComboBox<User>
        private boolean validateMedecin(User medecin) {
                if (medecin == null) {
                        medecinErrorLabel.setText("Veuillez sélectionner un médecin");
                        medecinErrorLabel.setVisible(true);
                        return false;
                } else {
                        medecinErrorLabel.setVisible(false);
                        return true;
                }
        }

        private boolean validateAllFields() {
                boolean dateValid = validateDate(datePicker.getValue());
                boolean creneauxValid = validateCreneaux();
                boolean statutValid = validateStatut(statutComboBox.getValue());
                boolean medecinValid = validateMedecin(medecinComboBox.getValue());

                return dateValid && creneauxValid && statutValid && medecinValid;
        }

        @FXML
        void ajouterDisponibilite(ActionEvent event) {
                try {
                        if (!validateAllFields()) {
                                return;
                        }

                        LocalDate selectedDate = datePicker.getValue();
                        String statut = statutComboBox.getValue();
                        User medecin = medecinComboBox.getValue();
                        int idMedecin = medecin.getId(); // Récupérer l'ID du médecin sélectionné

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
                        //navigateToAfficherDisponibilite(event);

                }
                catch (Exception e) {
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
                medecinComboBox.setValue(null); // Modifié pour le ComboBox

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

        // Les méthodes restantes restent identiques

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
}