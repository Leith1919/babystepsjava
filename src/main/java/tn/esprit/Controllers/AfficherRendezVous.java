package tn.esprit.Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
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


}
