package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import tn.esprit.entities.suiviBebe;
import tn.esprit.entities.suiviGrossesse;
import tn.esprit.services.SuiviBebeService;
import javafx.scene.control.TableCell;
import javafx.scene.control.Button;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AfficherSuiviBebeController implements Initializable {

    @FXML
    private Button ajoutBebeButton;

    @FXML
    private AnchorPane formContainer;

    @FXML
    private TableView<suiviBebe> tableView_SuiviGrossesse;

    @FXML
    private TableColumn<suiviBebe, String> colDatebebe;

    @FXML
    private TableColumn<suiviBebe, Double> colPoidsbebe;

    @FXML
    private TableColumn<suiviBebe, Double> colTaillebebe;

    @FXML
    private TableColumn<suiviBebe, String> colSantebebe;

    @FXML
    private TableColumn<suiviBebe, Double> colbattementbebe;

    @FXML
    private TableColumn<suiviBebe, String> colappetit;

    @FXML
    private TableColumn<suiviBebe, Void> colModifierbebe;

    @FXML
    private TableColumn<suiviBebe, Void> colSupprimerbebe;

    private SuiviBebeService bebeService = new SuiviBebeService();
    private ObservableList<suiviBebe> data = FXCollections.observableArrayList();

    private suiviGrossesse suiviGrossesse;

    public void setSuiviGrossesse(suiviGrossesse sg) {
        this.suiviGrossesse = sg;
        System.out.println("ID Suivi Grossesse reçu : " + sg.getId());

        // Activer le bouton une fois que le suiviGrossesse est défini
        if (ajoutBebeButton != null) {
            ajoutBebeButton.setDisable(false);
        }

        // Charger les données liées à ce suivi grossesse
        rafraichirTableau();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Désactiver le bouton jusqu'à ce qu'un suiviGrossesse soit défini
        ajoutBebeButton.setDisable(true);

        // Initialiser les colonnes de données
        colDatebebe.setCellValueFactory(new PropertyValueFactory<>("dateSuivi"));
        colPoidsbebe.setCellValueFactory(new PropertyValueFactory<>("poidsBebe"));
        colTaillebebe.setCellValueFactory(new PropertyValueFactory<>("tailleBebe"));
        colSantebebe.setCellValueFactory(new PropertyValueFactory<>("etatSante"));
        colbattementbebe.setCellValueFactory(new PropertyValueFactory<>("battementCoeur"));
        colappetit.setCellValueFactory(new PropertyValueFactory<>("appetitBebe"));

        // Configuration des colonnes d'action
        configureButtonColumns();

        // Lier les données à la table
        tableView_SuiviGrossesse.setItems(data);
    }

    private void configureButtonColumns() {
        // Colonne Modifier
        colModifierbebe.setCellFactory(param -> new TableCell<suiviBebe, Void>() {
            private final Button modifierBtn = new Button("Modifier");

            {
                // Style du bouton
                modifierBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white;");

                // Action du bouton
                modifierBtn.setOnAction(event -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        suiviBebe suivi = (suiviBebe) getTableRow().getItem();
                        ouvrirFormulaireModificationBebe(suivi);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(modifierBtn);
                }
            }
        });

        // Colonne Supprimer
        colSupprimerbebe.setCellFactory(param -> new TableCell<suiviBebe, Void>() {
            private final Button supprimerBtn = new Button("Supprimer");

            {
                // Style du bouton
                supprimerBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                // Action du bouton
                supprimerBtn.setOnAction(event -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        suiviBebe suivi = (suiviBebe) getTableRow().getItem();
                        confirmerSuppression(suivi);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(supprimerBtn);
                }
            }
        });
    }

    private void confirmerSuppression(suiviBebe suivi) {
        if (suivi == null) {
            afficherErreur("Erreur", "Aucun suivi sélectionné");
            return;
        }

        System.out.println("Tentative de suppression du suivi ID: " + suivi.getId());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce suivi bébé ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                System.out.println("Suppression confirmée, exécution...");
                bebeService.supprimer(suivi);
                System.out.println("Suppression réussie!");
                rafraichirTableau();
            } catch (SQLException e) {
                System.err.println("Erreur SQL lors de la suppression: " + e.getMessage());
                e.printStackTrace();
                afficherErreur("Erreur lors de la suppression", e.getMessage());
            } catch (Exception e) {
                System.err.println("Erreur générale lors de la suppression: " + e.getMessage());
                e.printStackTrace();
                afficherErreur("Erreur inattendue", "Une erreur s'est produite: " + e.getMessage());
            }
        } else {
            System.out.println("Suppression annulée par l'utilisateur");
        }
    }

    private void ouvrirFormulaireModificationBebe(suiviBebe suivi) {
        if (suivi == null) {
            afficherErreur("Erreur", "Aucun suivi sélectionné");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutSuiviBebe.fxml"));
            Pane form = loader.load();

            // Configurer le contrôleur
            AjoutSuiviBebeController controller = loader.getController();
            controller.setSuiviGrossesse(this.suiviGrossesse);
            controller.setParentController(this);
            controller.chargerDonneesPourModification(suivi);

            // Styliser le formulaire comme une fenêtre flottante
            form.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0); -fx-background-radius: 5;");

            // Ajouter un fond semi-transparent
            AnchorPane overlay = new AnchorPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
            overlay.setPrefSize(formContainer.getWidth(), formContainer.getHeight());

            // Ajouter le formulaire sur l'overlay et le centrer
            form.setLayoutX((formContainer.getWidth() - form.getPrefWidth()) / 2);
            form.setLayoutY(60);
            overlay.getChildren().add(form);

            // Remplacer le contenu du container
            formContainer.getChildren().clear();
            formContainer.getChildren().add(overlay);

        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur de chargement", "Erreur lors du chargement du formulaire: " + e.getMessage());
        }
    }
    @FXML
    private void ouvrirFormulaireAjoutBebe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutSuiviBebe.fxml"));
            Pane form = loader.load();

            // Configurer le contrôleur
            AjoutSuiviBebeController controller = loader.getController();
            controller.setSuiviGrossesse(this.suiviGrossesse);
            controller.setParentController(this);

            // Styliser le formulaire comme une fenêtre flottante
            form.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0); -fx-background-radius: 5;");

            // Positionner le formulaire au centre
            form.setLayoutX((formContainer.getWidth() - form.getPrefWidth()) / 2);
            form.setLayoutY(60); // Position en Y ajustable selon vos besoins

            // Ajouter un fond semi-transparent
            AnchorPane overlay = new AnchorPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
            overlay.setPrefSize(formContainer.getWidth(), formContainer.getHeight());

            // Ajouter le formulaire sur l'overlay
            overlay.getChildren().add(form);

            // Remplacer le contenu du container
            formContainer.getChildren().clear();
            formContainer.getChildren().add(overlay);

            // Rafraîchir le tableau immédiatement pour voir les données actuelles
            rafraichirTableau();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du formulaire: " + e.getMessage());
        }
    }

    public void rafraichirTableau() {
        if (suiviGrossesse == null) {
            System.out.println("Impossible de rafraîchir: suiviGrossesse est null");
            return;
        }

        try {
            System.out.println("Rafraîchissement du tableau pour suiviGrossesse ID: " + suiviGrossesse.getId());

            // Sauvegarder la sélection actuelle pour la restaurer après
            suiviBebe selected = tableView_SuiviGrossesse.getSelectionModel().getSelectedItem();

            // Effacer et recharger les données
            data.clear();
            List<suiviBebe> liste = bebeService.recupererParSuiviGrossesse(suiviGrossesse);
            data.addAll(liste);
            System.out.println("Tableau mis à jour avec " + liste.size() + " enregistrements !");

            // Restaurer la sélection si possible
            if (selected != null) {
                for (suiviBebe sb : data) {
                    if (sb.getId() == selected.getId()) {
                        tableView_SuiviGrossesse.getSelectionModel().select(sb);
                        break;
                    }
                }
            }

            // Appliquer une animation pour indiquer la mise à jour
            tableView_SuiviGrossesse.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2px;");

            // Remettre le style normal après un court délai
            javafx.application.Platform.runLater(() -> {
                try {
                    Thread.sleep(1000);
                    tableView_SuiviGrossesse.setStyle("");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur de rafraîchissement", "Erreur lors du rafraîchissement du tableau: " + e.getMessage());
        }
    }

    public void fermerFormulaire() {
        // Vider le conteneur de formulaire
        formContainer.getChildren().clear();

        // Rafraîchir le tableau après la fermeture du formulaire
        System.out.println("Formulaire fermé, rafraîchissement du tableau");
        rafraichirTableau();
    }

    private void afficherErreur(String titre, String... messages) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(String.join("\n", messages));
        alert.showAndWait();
    }

    public void initData(suiviGrossesse sg) {
        setSuiviGrossesse(sg);
    }
}