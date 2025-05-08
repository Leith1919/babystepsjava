package controllers.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Ordonnance;
import models.User;
import services.User.OrdonnanceServices;
import utils.MyDatabase;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class AjouterOrdonnanceControllors {

    @FXML
    private TextField medicamentTF;

    @FXML
    private TextField posologieTF;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<User> patientComboBox;

    @FXML
    private Button ajouterBtn;

    private final OrdonnanceServices ordonnanceService = new OrdonnanceServices();

    @FXML
    private void initialize() {
        ObservableList<User> userList = FXCollections.observableArrayList();
        Connection cnx = MyDatabase.getInstance().getConnection();

        try {
            String query = "SELECT id, nom, prenom FROM user";
            Statement stmt = cnx.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                User u = new User();
                u.setId(id);
                u.setNom(nom);
                u.setPrenom(prenom);
                userList.add(u);
            }

            patientComboBox.setItems(userList);

            // Show "Nom Prenom" in dropdown
            patientComboBox.setCellFactory(lv -> new ListCell<User>() {
                @Override
                protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNom() + " " + item.getPrenom());
                }
            });

            patientComboBox.setButtonCell(new ListCell<User>() {
                @Override
                protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNom() + " " + item.getPrenom());
                }
            });

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Chargement des patients échoué.");
            e.printStackTrace();
        }
    }

    @FXML
    public void ajouterOrdonnance(ActionEvent event) {
        String medicament = medicamentTF.getText();
        String posologie = posologieTF.getText();
        LocalDate datePrescription = datePicker.getValue();
        User selectedUser = patientComboBox.getValue();

        if (medicament.isEmpty() || posologie.isEmpty() || datePrescription == null || selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            // Extract patient details
            int patientId = selectedUser.getId();
            String patientName = selectedUser.getNom() + " " + selectedUser.getPrenom();

            // Create Ordonnance object
            Ordonnance ordonnance = new Ordonnance();
            ordonnance.setPatientId(patientId);
            ordonnance.setPatientName(patientName); // Optional: For display only
            ordonnance.setMedicament(medicament);
            ordonnance.setPosologie(posologie);
            ordonnance.setDatePrescription(datePrescription.toString()); // You can use `datePrescription` directly without converting to string

            // Save ordonnance to DB
            ordonnanceService.ajouter(ordonnance, patientId);

            // Switch to afficherOrdonnance.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/afficherOrdonnance.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ajouterBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

            // Success alert
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ordonnance ajoutée avec succès !");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Ajout échoué : " + e.getMessage());
        }
    }


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
