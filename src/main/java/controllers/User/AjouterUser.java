package controllers.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import models.User;
import services.User.UserService;
import test.HelloApplication;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AjouterUser implements Initializable {
    private ToggleGroup toggleGroup;
    private RadioButton selectedRadioButton;

    private UserService userService = new UserService();

    @FXML
    private TextField adresseTF;

    @FXML
    private TextField emailTF;

    @FXML
    private TextField mdpTF;

    @FXML
    private TextField nomTF;

    @FXML
    private TextField prenomTF;

    @FXML
    private TextField roleTF;

    @FXML
    private TextField numtelTF; // Added TextField for numtel

    @FXML
    private RadioButton et;

    @FXML
    private RadioButton fo;

    @FXML
    private RadioButton ad;
    @FXML
    private Button btnUtilisateurs;
    @FXML
    void afficherUsers(ActionEvent event) {
        try {
            // Load the full Back layout
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/back.fxml"));
            Parent root = loader.load();

            // Get the Back controller to load the internal content properly
            controllers.User.Back backController = loader.getController();
            backController.loadView("/User/AfficherUsers.fxml", "Gestion des Utilisateurs");

            // Set the full layout as the new root
            nomTF.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to load user list");
            alert.setContentText("An error occurred: " + e.getMessage());
            alert.showAndWait();
        }
    }


//    @FXML
//    void afficherUsers(ActionEvent event) {
//        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/User/AfficherUsers.fxml"));
//        try {
//            nomTF.getScene().setRoot(fxmlLoader.load());
//        } catch (IOException e) {
//            System.err.println(e.getMessage());
//            throw new RuntimeException(e);
//        }
//    }
//    @FXML
//    public void afficherUsers(ActionEvent event) {
//        setActiveButton(btnUtilisateurs);
//        loadView("/User/AfficherUsers.fxml", "Gestion des Utilisateurs");
//    }

    @FXML
    void ajouterUser(ActionEvent event) {
        if (isValidInput()) {
            User user = new User();
            user.setNom(nomTF.getText());
            user.setPrenom(prenomTF.getText());
            user.setNationnalite(adresseTF.getText());
            user.setEmail(emailTF.getText());
            user.setPassword(BCrypt.hashpw(mdpTF.getText(), BCrypt.gensalt())); // Hash the password
            user.setRoles(selectedRadioButton.getText());
            user.setNumtel(Integer.parseInt(numtelTF.getText()));

            try {
                userService.ajouter(user);

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Success");
                alert.setContentText("User added successfully!");
                alert.showAndWait();

                // ✅ Redirect to back.fxml and load AfficherUsers
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/back.fxml"));
                Parent root = loader.load();

                controllers.User.Back backController = loader.getController();
                backController.loadView("/User/AfficherUsers.fxml", "Gestion des Utilisateurs");

                // Switch scene
                nomTF.getScene().setRoot(root);

            } catch (SQLException | IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Error: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }



    private boolean isValidInput() {
        String nom = nomTF.getText();
        String prenom = prenomTF.getText();
        String adresse = adresseTF.getText();
        String email = emailTF.getText();
        String password = mdpTF.getText();
        String numtel = numtelTF.getText();

        if (nom.isEmpty() || prenom.isEmpty() || adresse.isEmpty() || email.isEmpty() || password.isEmpty() || numtel.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs obligatoires");
            alert.setContentText("Veuillez remplir tous les champs.");
            alert.showAndWait();
            return false;
        }

        if (!email.matches("\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Format d'email incorrect");
            alert.setContentText("Veuillez entrer une adresse email valide.");
            alert.showAndWait();
            return false;
        }

        if (!numtel.matches("\\d{8}")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Format de numéro de téléphone incorrect");
            alert.setContentText("Le numéro de téléphone doit contenir exactement 8 chiffres.");
            alert.showAndWait();
            return false;
        }

        return true;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        toggleGroup = new ToggleGroup();
        et.setToggleGroup(toggleGroup);
        fo.setToggleGroup(toggleGroup);

        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            selectedRadioButton = (RadioButton) newValue;
        });
    }
}
