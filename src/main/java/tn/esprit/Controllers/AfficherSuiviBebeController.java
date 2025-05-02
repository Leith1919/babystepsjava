package tn.esprit.Controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entities.suiviBebe;
import tn.esprit.entities.suiviGrossesse;
import tn.esprit.services.SuiviBebeService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class AfficherSuiviBebeController implements Initializable {

    @FXML
    private Button btnRetour;

    @FXML
    private AnchorPane formContainer;

    @FXML
    private Button ajoutBebeButton;

    @FXML
    private TextField rechercheField;

    @FXML
    private TableView<suiviBebe> tableView_SuiviGrossesse;

    @FXML
    private TableColumn<suiviBebe, Date> colDatebebe;

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
    @FXML
    private Button exportPdfButton;

    private final SuiviBebeService bebeService = new SuiviBebeService();
    private ObservableList<suiviBebe> data;
    private FilteredList<suiviBebe> filteredData;
    private suiviGrossesse suiviGrossesse;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initialisation du contrôleur AfficherSuiviBebeController");

        // Initialisation des données
        data = FXCollections.observableArrayList();

        // Créer la liste filtrée
        filteredData = new FilteredList<>(data, p -> true);

        // Configuration de la table
        setupTableColumns();

        // Configuration de la recherche si le champ existe
        if (rechercheField != null) {
            configureRecherche();
        }

        // Configuration du bouton retour
        if (btnRetour != null) {
            btnRetour.setOnAction(event -> handleRetour());
        }

        // Désactiver le bouton initialement
        if (ajoutBebeButton != null) {
            ajoutBebeButton.setDisable(true);
        }

        // Configurer le bouton d'exportation PDF
        if (exportPdfButton != null) {
            PDFExportUtil.setupExportButton(exportPdfButton, this);
        }

        // Lier les données filtrées à la table via une SortedList
        SortedList<suiviBebe> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView_SuiviGrossesse.comparatorProperty());
        tableView_SuiviGrossesse.setItems(sortedData);
    }

    private void configureRecherche() {
        if (rechercheField != null) {
            rechercheField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(createPredicate(newValue));
            });
        }
    }

    private Predicate<suiviBebe> createPredicate(String searchText) {
        return suivi -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }

            String lowerCaseFilter = searchText.toLowerCase();

            // Vérifier si le texte de recherche correspond à un attribut
            if (suivi.getEtatSante().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            if (suivi.getAppetitBebe().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            if (dateFormat.format(suivi.getDateSuivi()).contains(lowerCaseFilter)) {
                return true;
            }
            if (String.valueOf(suivi.getPoidsBebe()).contains(lowerCaseFilter)) {
                return true;
            }
            if (String.valueOf(suivi.getTailleBebe()).contains(lowerCaseFilter)) {
                return true;
            }
            return String.valueOf(suivi.getBattementCoeur()).contains(lowerCaseFilter);
        };
    }

    private void setupTableColumns() {
        // Configuration des colonnes de données
        colDatebebe.setCellValueFactory(new PropertyValueFactory<>("dateSuivi"));
        colPoidsbebe.setCellValueFactory(new PropertyValueFactory<>("poidsBebe"));
        colTaillebebe.setCellValueFactory(new PropertyValueFactory<>("tailleBebe"));
        colSantebebe.setCellValueFactory(new PropertyValueFactory<>("etatSante"));
        colbattementbebe.setCellValueFactory(new PropertyValueFactory<>("battementCoeur"));
        colappetit.setCellValueFactory(new PropertyValueFactory<>("appetitBebe"));

        // Configuration des colonnes d'actions
        colModifierbebe.setCellFactory(this::createModifierButtonCell);
        colSupprimerbebe.setCellFactory(this::createSupprimerButtonCell);

        // Formatage de la date - CORRECTION IMPORTANTE ICI
        colDatebebe.setCellFactory(column -> new TableCell<suiviBebe, Date>() {
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(date));
                }
            }
        });
    }

    private TableCell<suiviBebe, Void> createModifierButtonCell(TableColumn<suiviBebe, Void> param) {
        return new TableCell<>() {
            private final Button modifierBtn = new Button("Modifier");

            {
                modifierBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white;");
                modifierBtn.setOnAction(event -> {
                    suiviBebe suivi = getTableView().getItems().get(getIndex());
                    if (suivi != null) {
                        ouvrirFormulaireModificationBebe(suivi);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : modifierBtn);
            }
        };
    }

    private TableCell<suiviBebe, Void> createSupprimerButtonCell(TableColumn<suiviBebe, Void> param) {
        return new TableCell<>() {
            private final Button supprimerBtn = new Button("Supprimer");

            {
                supprimerBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                supprimerBtn.setOnAction(event -> {
                    suiviBebe suivi = getTableView().getItems().get(getIndex());
                    if (suivi != null) {
                        confirmerSuppression(suivi);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : supprimerBtn);
            }
        };
    }

    public void setSuiviGrossesse(suiviGrossesse sg) {
        this.suiviGrossesse = sg;
        if (sg == null) {
            System.err.println("Attention: tentative de définir un suiviGrossesse null");
            ajoutBebeButton.setDisable(true);
            return;
        }

        System.out.println("ID Suivi Grossesse défini : " + sg.getId());

        // Activer le bouton
        if (ajoutBebeButton != null) {
            ajoutBebeButton.setDisable(false);
        }

        // Rafraîchir les données immédiatement
        rafraichirTableau();
    }

    @FXML
    private void ouvrirFormulaireAjoutBebe(ActionEvent event) {
        if (suiviGrossesse == null) {
            showAlert("Erreur", "Aucun suivi de grossesse sélectionné.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutSuiviBebe.fxml"));
            Pane form = loader.load();

            AjoutSuiviBebeController controller = loader.getController();
            controller.setSuiviGrossesse(this.suiviGrossesse);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Ajout d'un suivi bébé");
            stage.setScene(new Scene(form));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setOnHidden(e -> {
                // Rafraîchir la table après la fermeture de la fenêtre
                Platform.runLater(this::rafraichirTableau);
            });
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur de chargement: " + e.getMessage());
        }
    }

    private void ouvrirFormulaireModificationBebe(suiviBebe suivi) {
        if (suivi == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutSuiviBebe.fxml"));
            Pane form = loader.load();

            AjoutSuiviBebeController controller = loader.getController();
            controller.setSuiviGrossesse(this.suiviGrossesse);
            controller.setParentController(this);
            controller.chargerDonneesPourModification(suivi);

            Stage stage = new Stage();
            stage.setTitle("Modification d'un suivi bébé");
            stage.setScene(new Scene(form));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setOnHidden(e -> {
                // Rafraîchir la table après la fermeture de la fenêtre
                Platform.runLater(this::rafraichirTableau);
            });
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement du formulaire: " + e.getMessage());
        }
    }

    private void confirmerSuppression(suiviBebe suivi) {
        if (suivi == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce suivi bébé ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                bebeService.supprimer(suivi);
                // Rafraîchir immédiatement la table
                Platform.runLater(this::rafraichirTableau);
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur SQL", e.getMessage());
            }
        }
    }

    public void rafraichirTableau() {
        System.out.println("Début rafraichirTableau");
        if (suiviGrossesse == null) {
            System.err.println("Impossible de rafraîchir: suiviGrossesse est null");
            return;
        }

        try {
            System.out.println("Tentative de récupération des données pour suiviGrossesse #" + suiviGrossesse.getId());
            List<suiviBebe> liste = bebeService.recupererParSuiviGrossesse(suiviGrossesse);
            System.out.println("Données récupérées : " + (liste == null ? "null" : liste.size() + " éléments"));

            if (liste != null) {
                System.out.println("Premier élément : " + (liste.isEmpty() ? "aucun" : liste.get(0).getDateSuivi()));
                Platform.runLater(() -> {
                    data.clear();
                    System.out.println("Data cleared, adding new items: " + liste.size());
                    data.addAll(liste);
                    System.out.println("Data size after adding: " + data.size());
                    filteredData.setPredicate(p -> true);
                    tableView_SuiviGrossesse.refresh();
                    System.out.println("Table refreshed");
                });
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du rafraîchissement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleRetour() {
        try {
            // Au lieu de manipuler le BorderPane existant, nous allons charger
            // complètement la nouvelle vue et la définir comme racine de la scène

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherSuiviGrossesseFront.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur
            AfficherSuiviGrossesseFront controller = loader.getController();

            // Obtenir la scène actuelle
            Scene currentScene = btnRetour.getScene();

            // Remplacer COMPLÈTEMENT le root de la scène
            Platform.runLater(() -> {
                // Définir la nouvelle racine pour la scène
                currentScene.setRoot(root);

                // Initialiser les données après avoir remplacé la vue
                if (suiviGrossesse != null) {
                    controller.afficherSuiviGrossesseById(suiviGrossesse.getId());
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du retour à la vue précédente: " + e.getMessage());
        }
    }
    private void showAlert(String titre, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(titre);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void initData(suiviGrossesse sg) {
        setSuiviGrossesse(sg);
    }


    public SuiviBebeService getBebeService() {
        return this.bebeService;
    }

    /**
     * Renvoie le suivi de grossesse actuel
     */
    public suiviGrossesse getSuiviGrossesse() {
        return this.suiviGrossesse;
    }

    /**
     * Renvoie le bouton d'ajout de bébé (utilisé par PDFExportUtil pour obtenir le stage parent)
     */
    public Button getAjoutBebeButton() {
        return this.ajoutBebeButton;
    }
}