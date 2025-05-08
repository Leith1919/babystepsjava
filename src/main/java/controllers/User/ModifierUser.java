package controllers.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import models.User;
import services.User.UserService;
import test.HelloApplication;
import javafx.scene.control.ToggleGroup;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ModifierUser implements Initializable {
    private ToggleGroup toggleGroup;
    private RadioButton selectedRadioButton;

    @FXML
    private TextField nomNouv;

    @FXML
    private TextField prenomNouv;

    @FXML
    private TextField nationnaliteNouv;

    @FXML
    private TextField emailNouv;

    @FXML
    private TextField numtelNouv;

    @FXML
    private Button modifier;

    @FXML
    private RadioButton roleAdmin;

    @FXML
    private RadioButton roleUser;

    private UserService userService = new UserService();

    private User selectedUser;

    public void setSelectedUser(User user) {
        this.selectedUser = user;
        populateFields();
    }

    private void populateFields() {
        if (selectedUser != null) {
            nomNouv.setText(selectedUser.getNom());
            prenomNouv.setText(selectedUser.getPrenom());
            nationnaliteNouv.setText(selectedUser.getNationnalite());
            emailNouv.setText(selectedUser.getEmail());
            numtelNouv.setText(String.valueOf(selectedUser.getNumtel()));
            if (selectedUser.getRoles().equals("[\"ROLE_ADMIN\"]")) {
                roleAdmin.setSelected(true);
            } else {
                roleUser.setSelected(true);
            }
        } else {
            // Log or show an error for debugging
            System.err.println("Error: selectedUser is null in populateFields");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        toggleGroup = new ToggleGroup();
        roleAdmin.setToggleGroup(toggleGroup);
        roleUser.setToggleGroup(toggleGroup);
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            selectedRadioButton = (RadioButton) newValue;
        });
    }

    @FXML
    void modifierUser() {
        try {
            if (isValidInput()) {
                selectedUser.setNom(nomNouv.getText());
                selectedUser.setPrenom(prenomNouv.getText());
                selectedUser.setNationnalite(nationnaliteNouv.getText());
                selectedUser.setEmail(emailNouv.getText());
                selectedUser.setNumtel(Integer.parseInt(numtelNouv.getText()));
                selectedUser.setRoles(selectedRadioButton == roleAdmin ? "[\"ROLE_ADMIN\"]" : "[\"ROLE_USER\"]");

                userService.modifier(selectedUser);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setContentText("Utilisateur modifié avec succès !");
                alert.showAndWait();

                // Redirect to AfficherUsers inside back.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/back.fxml"));
                Parent root = loader.load();
                Back backController = loader.getController();
                backController.loadView("/User/AfficherUsers.fxml", "Gestion des Utilisateurs");

                nomNouv.getScene().setRoot(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Erreur lors de la modification de l'utilisateur : " + e.getMessage());
        }
    }

    @FXML
    void annuler(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back.fxml"));
            Parent root = loader.load();
            Back backController = loader.getController();
            backController.loadView("/User/AfficherUsers.fxml", "Gestion des Utilisateurs");
            nomNouv.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Erreur lors du retour : " + e.getMessage());
        }
    }

    private void afficherErreur(String titre, String contenu) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.showAndWait();
    }

    private boolean isValidInput() {
        String nouveauNom = nomNouv.getText();
        String nouveauPrenom = prenomNouv.getText();
        String nouvelleNationnalite = nationnaliteNouv.getText();
        String nouveauEmail = emailNouv.getText();
        String nouveauNumtel = numtelNouv.getText();

        if (nouveauNom.isEmpty() || nouveauPrenom.isEmpty() || nouvelleNationnalite.isEmpty() || nouveauEmail.isEmpty() || nouveauNumtel.isEmpty()) {
            afficherErreur("Champs obligatoires", "Veuillez remplir tous les champs.");
            return false;
        }

        if (!nouveauEmail.matches("\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b")) {
            afficherErreur("Format d'email incorrect", "Veuillez entrer une adresse email valide.");
            return false;
        }

        if (!nouveauNumtel.matches("\\d{8}")) {
            afficherErreur("Format de numéro de téléphone incorrect", "Le numéro de téléphone doit contenir exactement 8 chiffres.");
            return false;
        }

        return true;
    }
}