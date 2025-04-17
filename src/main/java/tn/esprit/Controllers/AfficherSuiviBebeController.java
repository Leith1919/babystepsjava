package tn.esprit.Controllers;

import javafx.application.Platform;
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
    private ObservableList<suiviBebe> data;

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
        // Initialiser l'ObservableList
        data = FXCollections.observableArrayList();

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
                    suiviBebe suivi = getTableRow().getItem();
                    if (suivi != null) {
                        ouvrirFormulaireModificationBebe(suivi);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
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
                    suiviBebe suivi = getTableRow().getItem();
                    if (suivi != null) {
                        confirmerSuppression(suivi);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
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
            // Vider complètement le conteneur
            formContainer.getChildren().clear();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutSuiviBebe.fxml"));
            Pane form = loader.load();

            // Définir des dimensions explicites
            form.setPrefWidth(600);  // Ajustez selon vos besoins
            form.setPrefHeight(400); // Ajustez selon vos besoins
            form.setVisible(true);
            form.setManaged(true);

            // Configurer le contrôleur
            AjoutSuiviBebeController controller = loader.getController();
            controller.setSuiviGrossesse(this.suiviGrossesse);
            controller.setParentController(this);
            controller.chargerDonneesPourModification(suivi);

            // IMPORTANT: Vérifier que le contrôleur est bien initialisé
            controller.verifierReferences();

            // Créer un overlay avec des dimensions précises
            AnchorPane overlay = new AnchorPane();
            overlay.setPrefSize(formContainer.getWidth(), formContainer.getHeight());
            overlay.setMinSize(formContainer.getWidth(), formContainer.getHeight());
            overlay.setMaxSize(formContainer.getWidth(), formContainer.getHeight());
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

            // Positionner le formulaire au centre exact
            AnchorPane.setTopAnchor(form, (formContainer.getHeight() - form.getPrefHeight()) / 2);
            AnchorPane.setLeftAnchor(form, (formContainer.getWidth() - form.getPrefWidth()) / 2);

            // Ajouter le formulaire à l'overlay
            overlay.getChildren().add(form);

            // Ajouter l'overlay au conteneur
            formContainer.getChildren().add(overlay);

            // Forcer un rafraîchissement de l'affichage
            formContainer.requestLayout();

        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur de chargement", "Erreur lors du chargement du formulaire: " + e.getMessage());
        }
    }
    @FXML
    private void ouvrirFormulaireAjoutBebe(ActionEvent event) {
        try {
            if (suiviGrossesse == null) {
                afficherErreur("Erreur", "Aucun suivi grossesse défini");
                return;
            }

            // Vider complètement le conteneur
            formContainer.getChildren().clear();

            // Charger le formulaire avec toutes les dimensions explicites
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutSuiviBebe.fxml"));
            Pane form = loader.load();

            // Définir des dimensions explicites au formulaire
            form.setPrefWidth(600);  // Ajustez selon vos besoins
            form.setPrefHeight(400); // Ajustez selon vos besoins

            // S'assurer que le formulaire est visible
            form.setVisible(true);
            form.setManaged(true);

            // Configurer le contrôleur
            AjoutSuiviBebeController controller = loader.getController();
            controller.setSuiviGrossesse(this.suiviGrossesse);
            controller.setParentController(this);

            // IMPORTANT: Vérifier que le contrôleur est bien initialisé
            controller.verifierReferences();  // Assurez-vous que cette méthode existe et est publique

            // Créer un overlay dimensionné à la taille exacte du conteneur
            AnchorPane overlay = new AnchorPane();
            overlay.setPrefSize(formContainer.getWidth(), formContainer.getHeight());
            overlay.setMinSize(formContainer.getWidth(), formContainer.getHeight());
            overlay.setMaxSize(formContainer.getWidth(), formContainer.getHeight());
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

            // Centre exactement le formulaire
            AnchorPane.setTopAnchor(form, (formContainer.getHeight() - form.getPrefHeight()) / 2);
            AnchorPane.setLeftAnchor(form, (formContainer.getWidth() - form.getPrefWidth()) / 2);

            // Ajouter le formulaire à l'overlay
            overlay.getChildren().add(form);

            // Ajouter l'overlay au conteneur
            formContainer.getChildren().add(overlay);

            // Forcer un rafraîchissement de l'affichage
            formContainer.requestLayout();

            System.out.println("Formulaire d'ajout ouvert avec succès");

        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur de chargement", "Erreur lors du chargement du formulaire: " + e.getMessage());
        }
    }
    public void rafraichirTableau() {
        if (suiviGrossesse == null) {
            System.out.println("Impossible de rafraîchir : suiviGrossesse est null");
            return;
        }

        try {
            System.out.println("Rafraîchissement du tableau pour suiviGrossesse ID : " + suiviGrossesse.getId());

            // Exécuter dans le thread d'interface utilisateur
            Platform.runLater(() -> {
                try {
                    // Créer une nouvelle liste pour éviter les problèmes de concurrence
                    List<suiviBebe> liste = bebeService.recupererParSuiviGrossesse(suiviGrossesse);

                    // Mettre à jour les données de façon sûre
                    data.clear();
                    if (liste != null && !liste.isEmpty()) {
                        data.addAll(liste);
                        System.out.println("Tableau mis à jour avec " + liste.size() + " enregistrements");
                    } else {
                        System.out.println("Aucun enregistrement trouvé pour ce suivi de grossesse");
                    }

                    // Forcer le rafraîchissement visuel de la table
                    tableView_SuiviGrossesse.refresh();

                    // Animation visuelle pour indiquer le rafraîchissement
                    tableView_SuiviGrossesse.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2px;");
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    Platform.runLater(() -> tableView_SuiviGrossesse.setStyle(""));
                                }
                            },
                            800
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Erreur lors du rafraîchissement du tableau: " + e.getMessage());
                    afficherErreur("Erreur de rafraîchissement",
                            "Erreur lors du rafraîchissement du tableau : " + e.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur de rafraîchissement",
                    "Erreur lors du rafraîchissement du tableau : " + e.getMessage());
        }
    }

    public void fermerFormulaire() {
        Platform.runLater(() -> {
            try {
                if (formContainer != null) {
                    // Vider complètement le conteneur
                    formContainer.getChildren().clear();
                    System.out.println("Formulaire fermé avec succès");

                    // Forcer la mise à jour visuelle
                    formContainer.requestLayout();

                    // Rafraîchir le tableau avec un délai pour assurer que tout est bien fermé
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    Platform.runLater(() -> rafraichirTableau());
                                }
                            },
                            200
                    );
                } else {
                    System.err.println("ATTENTION: formContainer est null");
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la fermeture du formulaire: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    private void afficherErreur(String titre, String... messages) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(titre);
            alert.setHeaderText(null);
            alert.setContentText(String.join("\n", messages));
            alert.showAndWait();
        });
    }

    public void initData(suiviGrossesse sg) {
        setSuiviGrossesse(sg);
    }
}