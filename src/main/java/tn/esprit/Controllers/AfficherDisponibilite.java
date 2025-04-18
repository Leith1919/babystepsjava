package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import tn.esprit.entites.Disponibilite;
import tn.esprit.services.DisponibiliteService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AfficherDisponibilite {

    @FXML
    private TableView<Disponibilite> tableDisponibilite;

    @FXML
    private TableColumn<Disponibilite, LocalDate> colJour;

    @FXML
    private TableColumn<Disponibilite, String> colHeures;

    @FXML
    private TableColumn<Disponibilite, String> colStatut;

    @FXML
    private TableColumn<Disponibilite, Integer> colMedecin;

    @FXML
    private TableColumn<Disponibilite, Void> colActions;

    private final DisponibiliteService service = new DisponibiliteService();

    @FXML
    public void initialize() throws SQLException {
        colJour.setCellValueFactory(new PropertyValueFactory<>("jour"));
        colHeures.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.join(", ", cellData.getValue().getHeuresDisp()))
        );

        // Configuration de l'affichage des cellules pour permettre les sauts de ligne
        colHeures.setCellFactory(column -> {
            return new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        // Formater les heures proprement avec des sauts de ligne si nécessaire
                        setText(item.replace(",", ",\n"));
                        setWrapText(true); // Permet le retour à la ligne dans la cellule
                    }
                }
            };
        });
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutDisp"));

        ajouterBoutonsActions();
        rafraichirTable();
    }

    private void rafraichirTable() throws SQLException {
        tableDisponibilite.getItems().setAll(service.afficherDisponibilite());
    }

    private void ajouterBoutonsActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");

            {
                btnModifier.setOnAction(event -> {
                    Disponibilite dispo = getTableView().getItems().get(getIndex());
                    ouvrirFenetreModification(dispo);
                });

                btnSupprimer.setOnAction(event -> {
                    Disponibilite dispo = getTableView().getItems().get(getIndex());
                    try {
                        service.supprimerD(dispo);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        rafraichirTable();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(10, btnModifier, btnSupprimer);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void ouvrirFenetreModification(Disponibilite dispo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierDisponibilite.fxml"));
            Parent root = loader.load();

            ModifierDisponibilite controller = loader.getController();
            controller.initData(dispo);

            Stage stage = new Stage();
            stage.setTitle("Modifier Disponibilité");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Recharger après modification
            rafraichirTable();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


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

    public void afficherFormulaireAjout(ActionEvent actionEvent) {
        try {
            // Charger le fichier FXML du formulaire d'ajout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterRendezVous.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène
            Scene scene = new Scene(root);

            // Obtenir la fenêtre (Stage) à partir du bouton
            Stage stage = new Stage();

            // Configurer la fenêtre
            stage.setTitle("Formulaire d'ajout");
            stage.setScene(scene);

            // Afficher la fenêtre
            stage.show();

            // Facultatif: fermer la fenêtre actuelle
            // ((Stage) front.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
