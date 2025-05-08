package controllers.User;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.suiviBebe;
import models.suiviGrossesse;
import services.User.SuiviBebeService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
    private FlowPane cardContainer;

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
    }

    private void configureRecherche() {
        if (rechercheField != null) {
            rechercheField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(createPredicate(newValue));
                rafraichirAffichageCartes();
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
                    rafraichirAffichageCartes();
                    System.out.println("Cards refreshed");
                });
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du rafraîchissement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Méthode qui rafraîchit l'affichage des cartes dans le FlowPane
     */
    private void rafraichirAffichageCartes() {
        if (cardContainer == null) {
            System.err.println("Le conteneur de cartes est null!");
            return;
        }

        cardContainer.getChildren().clear();

        for (suiviBebe suivi : filteredData) {
            cardContainer.getChildren().add(creerCarteSuivi(suivi));
        }

        // Si aucune donnée n'est disponible, afficher un message
        if (filteredData.isEmpty()) {
            Label lblAucunSuivi = new Label("Aucun suivi trouvé");
            lblAucunSuivi.setStyle("-fx-font-size: 16px; -fx-text-fill: #757575;");
            lblAucunSuivi.setPrefWidth(cardContainer.getPrefWidth() - 20);
            lblAucunSuivi.setAlignment(Pos.CENTER);
            lblAucunSuivi.setPadding(new Insets(50, 0, 0, 0));
            cardContainer.getChildren().add(lblAucunSuivi);
        }
    }

    /**
     * Crée une carte représentant un suivi bébé
     */
    private VBox creerCarteSuivi(suiviBebe suivi) {
        // Créer la carte principale
        VBox carte = new VBox();
        carte.setPrefWidth(320);
        carte.setPrefHeight(230);
        carte.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15;");
        carte.setPadding(new Insets(15));
        carte.setSpacing(12);

        // Effet d'ombre
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(5);
        shadow.setOffsetY(2);
        carte.setEffect(shadow);

        // En-tête avec la date
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);

        Label lblDate = new Label(dateFormat.format(suivi.getDateSuivi()));
        lblDate.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4b89e8;");

        header.getChildren().addAll(lblDate);

        // Section avec les informations
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(8);

        // Styles pour les labels
        String labelTitleStyle = "-fx-font-size: 12px; -fx-text-fill: #757575;";
        String labelValueStyle = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;";

        // Première ligne
        addInfoToGrid(infoGrid, 0, 0, "Poids", suivi.getPoidsBebe() + " kg", labelTitleStyle, labelValueStyle);
        addInfoToGrid(infoGrid, 1, 0, "Taille", suivi.getTailleBebe() + " cm", labelTitleStyle, labelValueStyle);

        // Deuxième ligne
        addInfoToGrid(infoGrid, 0, 1, "Battements", suivi.getBattementCoeur() + " bpm", labelTitleStyle, labelValueStyle);
        addInfoToGrid(infoGrid, 1, 1, "Appétit", suivi.getAppetitBebe(), labelTitleStyle, labelValueStyle);

        // Troisième ligne - État de santé
        Label lblSanteTitre = new Label("État de santé");
        lblSanteTitre.setStyle(labelTitleStyle);
        infoGrid.add(lblSanteTitre, 0, 2, 2, 1);

        Label lblSanteValeur = new Label(suivi.getEtatSante());
        lblSanteValeur.setStyle(labelValueStyle);
        lblSanteValeur.setWrapText(true);
        infoGrid.add(lblSanteValeur, 0, 3, 2, 1);

        // Boutons d'action
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsBox.setPadding(new Insets(5, 0, 0, 0));

        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-background-radius: 5;");
        btnModifier.setPrefWidth(100);
        btnModifier.setOnAction(e -> ouvrirFormulaireModificationBebe(suivi));

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5;");
        btnSupprimer.setPrefWidth(100);
        btnSupprimer.setOnAction(e -> confirmerSuppression(suivi));

        buttonsBox.getChildren().addAll(btnModifier, btnSupprimer);

        // Ajouter tous les éléments à la carte
        carte.getChildren().addAll(header, infoGrid, buttonsBox);

        return carte;
    }

    /**
     * Ajoute une information au GridPane avec style
     */
    private void addInfoToGrid(GridPane grid, int col, int row, String title, String value, String titleStyle, String valueStyle) {
        VBox container = new VBox(2);

        Label lblTitle = new Label(title);
        lblTitle.setStyle(titleStyle);

        Label lblValue = new Label(value);
        lblValue.setStyle(valueStyle);

        container.getChildren().addAll(lblTitle, lblValue);
        grid.add(container, col, row);
    }

    private void handleRetour() {
        try {
            // Charger la vue AfficherSuiviGrossesseFront
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherSuiviGrossesseFront.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur
            AfficherSuiviGrossesseFront controller = loader.getController();

            // Obtenir la scène actuelle
            Scene currentScene = btnRetour.getScene();

            // Remplacer la racine de la scène
            Platform.runLater(() -> {
                currentScene.setRoot(root);
                // Appeler afficherSuiviGrossesseByUser pour initialiser les données
                controller.afficherSuiviGrossesseByUser();
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