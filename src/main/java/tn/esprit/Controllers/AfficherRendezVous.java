package tn.esprit.Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entites.Disponibilite;
import tn.esprit.entites.RendezVous;
import tn.esprit.services.RendezVousService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherRendezVous {

    @FXML
    private TableView<RendezVous> tableRendezVous;

    @FXML
    private TableColumn<RendezVous, String> colMotif;

    @FXML
    private TableColumn<RendezVous, String> colSymptomes;

    @FXML
    private TableColumn<RendezVous, String> colTraitement;

    @FXML
    private TableColumn<RendezVous, String> colNotes;

    @FXML
    private TableColumn<RendezVous, String> colHeure;

    @FXML
    private TableColumn<RendezVous, String> colJour;

    @FXML
    private TableColumn<RendezVous, String> colMedecin;

    @FXML
    private TableColumn<RendezVous, String> colActions;
    @FXML
    private VBox rdvSubmenu;
    @FXML
    private Button rdvButton;
    @FXML
    private VBox dispSubmenu;

    @FXML
    private Button dispButton;

    private RendezVousService rendezVousService;

    public AfficherRendezVous() {
        this.rendezVousService = new RendezVousService();
    }

    // Cette méthode est appelée lors de l'initialisation de la page
    @FXML
    private void initialize() throws SQLException {
        // Initialiser les colonnes
        colMotif.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMotif()));
        colSymptomes.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSymptomes()));
        colTraitement.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTraitementEnCours()));
        colNotes.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNotes()));
        colHeure.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHeureString()));
        colJour.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJour().toString()));
        colMedecin.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMedecin().getNom()));


        ajouterBoutonsActions();
        rafraichirTable();
    }

    // Méthode pour rafraîchir la table
    private void rafraichirTable() throws SQLException {
        List<RendezVous> rendezVousList = rendezVousService.afficher();
        ObservableList<RendezVous> observableList = FXCollections.observableArrayList(rendezVousList);
        tableRendezVous.getItems().setAll(observableList);
    }
    private void ajouterBoutonsActions() {
        colActions.setCellFactory(param -> new TableCell<>()
        {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");

            {
                btnModifier.setOnAction(event -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    ouvrirFenetreModificationRendezVous(rdv);
                });

                btnSupprimer.setOnAction(event -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    try {
                        rendezVousService.supprimerD(rdv);  // Changed from service.supprimerRendezVous(rdv)
                        rafraichirTable();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {  // Changed parameter type from Void to String to match colActions
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
    private void ouvrirFenetreModificationRendezVous(RendezVous rdv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierRendezVous.fxml"));
            Parent root = loader.load();

            ModifierRendezVous controller = loader.getController();
            controller.initData(rdv);

            Stage stage = new Stage();
            stage.setTitle("Modifier Rendez Vous");
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
