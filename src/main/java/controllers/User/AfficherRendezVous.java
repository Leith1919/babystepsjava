package controllers.User;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Disponibilite;
import models.RendezVous;
import services.User.RendezVousService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

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

    @FXML
    private TextField searchField;

    @FXML
    private Button todayFilterBtn;

    @FXML
    private Button weekFilterBtn;

    @FXML
    private Button allFilterBtn;

    @FXML
    private Label todayCountLabel;

    @FXML
    private Label weekCountLabel;

    @FXML
    private Label totalCountLabel;
    @FXML
    private Button pdfButton;

    private RendezVousService rendezVousService;
    private ObservableList<RendezVous> allRendezVous;

    public AfficherRendezVous() {
        this.rendezVousService = new RendezVousService();
    }

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
        setupFilterButtons();

        // Configurer le champ de recherche
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                handleSearch();
            });
        }

        Platform.runLater(() -> {
            setRowAnimations();
        });
    }

    private void setRowAnimations() {
        int rowIndex = 0;
        for (Node row : tableRendezVous.lookupAll(".table-row-cell")) {
            row.getStyleClass().add("staggered-item");
            row.setStyle("--row-index: " + rowIndex);
            rowIndex++;
        }
    }

    private void rafraichirTable() throws SQLException {
        List<RendezVous> rendezVousList = rendezVousService.afficher();
        allRendezVous = FXCollections.observableArrayList(rendezVousList);
        tableRendezVous.setItems(allRendezVous);
        updateStatCards(allRendezVous);
    }

    private void updateStatCards(ObservableList<RendezVous> rendezVousList) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);

        // Compter les RDV d'aujourd'hui
        long todayCount = rendezVousList.stream()
                .filter(rdv -> rdv.getJour() != null && rdv.getJour().equals(today))
                .count();

        // Compter les RDV de cette semaine
        long weekCount = rendezVousList.stream()
                .filter(rdv -> rdv.getJour() != null &&
                        !rdv.getJour().isBefore(weekStart) &&
                        !rdv.getJour().isAfter(weekEnd))
                .count();

        // Total des RDV
        long totalCount = rendezVousList.size();

        // Mettre à jour les labels
        Platform.runLater(() -> {
            if (todayCountLabel != null) {
                todayCountLabel.setText(todayCount + " rendez-vous");
            }
            if (weekCountLabel != null) {
                weekCountLabel.setText(weekCount + " rendez-vous");
            }
            if (totalCountLabel != null) {
                totalCountLabel.setText(totalCount + " rendez-vous");
            }
        });
    }

    private void setupFilterButtons() {
        if (todayFilterBtn != null) {
            todayFilterBtn.setOnAction(event -> filterToday());
        }

        if (weekFilterBtn != null) {
            weekFilterBtn.setOnAction(event -> filterThisWeek());
        }

        if (allFilterBtn != null) {
            allFilterBtn.setOnAction(event -> showAllAppointments());
        }
    }

    @FXML
    private void filterToday() {
        LocalDate today = LocalDate.now();
        ObservableList<RendezVous> filteredList = FXCollections.observableArrayList(
                allRendezVous.filtered(
                        rdv -> rdv.getJour() != null && rdv.getJour().equals(today)
                )
        );
        tableRendezVous.setItems(filteredList);
    }

    @FXML
    private void filterThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);

        ObservableList<RendezVous> filteredList = FXCollections.observableArrayList(
                allRendezVous.filtered(
                        rdv -> rdv.getJour() != null &&
                                !rdv.getJour().isBefore(weekStart) &&
                                !rdv.getJour().isAfter(weekEnd)
                )
        );
        tableRendezVous.setItems(filteredList);
    }

    @FXML
    private void showAllAppointments() {
        tableRendezVous.setItems(allRendezVous);
    }

    @FXML
    private void handleSearch() {
        if (searchField != null) {
            String searchText = searchField.getText().toLowerCase();

            if (searchText.isEmpty()) {
                tableRendezVous.setItems(allRendezVous);
            } else {
                ObservableList<RendezVous> filteredList = FXCollections.observableArrayList(
                        allRendezVous.filtered(
                                rdv -> (rdv.getMotif() != null && rdv.getMotif().toLowerCase().contains(searchText)) ||
                                        (rdv.getSymptomes() != null && rdv.getSymptomes().toLowerCase().contains(searchText)) ||
                                        (rdv.getTraitementEnCours() != null && rdv.getTraitementEnCours().toLowerCase().contains(searchText)) ||
                                        (rdv.getNotes() != null && rdv.getNotes().toLowerCase().contains(searchText)) ||
                                        (rdv.getMedecin() != null && rdv.getMedecin().getNom() != null &&
                                                rdv.getMedecin().getNom().toLowerCase().contains(searchText))
                        )
                );
                tableRendezVous.setItems(filteredList);
            }
        }
    }

    private void ajouterBoutonsActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox pane = new HBox(5);

            {
                // Style for Modify button - Orange
                btnModifier.getStyleClass().add("scale-effect");
                btnModifier.setStyle("-fx-background-color: #ED8936; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand;");
                btnModifier.setPrefWidth(90);
                btnModifier.setPrefHeight(28);
                btnModifier.setFont(Font.font("System", FontWeight.BOLD, 11));

                // Style for Delete button - Red
                btnSupprimer.getStyleClass().add("scale-effect");
                btnSupprimer.setStyle("-fx-background-color: #E53E3E; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand;");
                btnSupprimer.setPrefWidth(90);
                btnSupprimer.setPrefHeight(28);
                btnSupprimer.setFont(Font.font("System", FontWeight.BOLD, 11));

                // Add button actions
                btnModifier.setOnAction(event -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    ouvrirFenetreModificationRendezVous(rdv);
                });

                btnSupprimer.setOnAction(event -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    try {
                        rendezVousService.supprimerD(rdv);
                        rafraichirTable();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

                pane.setAlignment(javafx.geometry.Pos.CENTER);
                pane.getChildren().addAll(btnModifier, btnSupprimer);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
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

    @FXML
    private void navigateToAjouterRDV() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterRendezVous.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) tableRendezVous.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void navigateToConsulterRDV() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherRendezVous.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) tableRendezVous.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
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

    @FXML
    public void navigateToAjouterDISP(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterDisponibilite.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Scene scene = tableRendezVous.getScene();

            // Remplacer le contenu de la scène par le formulaire de rendez-vous
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface Disponibilite: " + e.getMessage());
        }
    }

    @FXML
    public void navigateToConsulterDISP(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherDisponibilite.fxml"));
            Parent root = loader.load();

            Scene scene = tableRendezVous.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface ConsulterDispo: " + e.getMessage());
        }
    }

    public void exporterPDF(ActionEvent actionEvent) {
        try {
            // Charger le fichier FXML pour la génération de statistiques
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GenerationStatistiques.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène
            Scene scene = new Scene(root);

            // Créer une nouvelle fenêtre (Stage)
            Stage popupStage = new Stage();
            popupStage.setTitle("Génération de PDF");
            popupStage.setScene(scene);

            // Définir la fenêtre principale comme propriétaire (parentalité)
            popupStage.initOwner(pdfButton.getScene().getWindow());

            // Définir le mode modal (empêche l'interaction avec la fenêtre parent)
            popupStage.initModality(Modality.APPLICATION_MODAL);

            // Définir la taille de la fenêtre
            popupStage.setWidth(600);
            popupStage.setHeight(500);

            // Centrer la fenêtre sur l'écran
            popupStage.centerOnScreen();

            // Afficher la fenêtre et attendre qu'elle soit fermée
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface de pdf: " + e.getMessage());

            // Afficher une alerte en cas d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de chargement");
            alert.setContentText("Impossible de charger l'interface de génération PDF: " + e.getMessage());
            alert.showAndWait();
        }
    }
}