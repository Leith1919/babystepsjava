package controllers.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Disponibilite;
import models.User;
import services.User.DisponibiliteService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.User.UserService;

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
    private TableColumn<Disponibilite, String> colMedecin;

    @FXML
    private TableColumn<Disponibilite, Void> colActions;

    @FXML
    private VBox rdvSubmenu;

    @FXML
    private Button rdvButton;

    @FXML
    private VBox dispSubmenu;

    @FXML
    private Button dispButton;

    @FXML
    private DatePicker searchDatePicker;

    @FXML
    private ComboBox<User> searchMedecinComboBox;

    @FXML
    private Button btnRechercher;

    @FXML
    private Button btnReinitialiser;

    @FXML
    private Label lblInfoResultats;

    private UserService userService = new UserService();
    private final DisponibiliteService service = new DisponibiliteService();

    @FXML
    public void initialize() throws SQLException {
        // Configuration des colonnes
        configureColumns();

        // Chargement des médecins pour la recherche
        loadMedecins();

        // Configuration des boutons d'action
        setupActionButtons();

        // Chargement initial des données
        rafraichirTable();
    }

    private void configureColumns() {
        // Configuration de la colonne Jour
        colJour.setCellValueFactory(new PropertyValueFactory<>("jour"));

        // Configuration de la colonne Heures
        colHeures.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.join(", ", cellData.getValue().getHeuresDisp()))
        );
        colHeures.setCellFactory(column -> {
            return new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        // Formater les heures proprement avec des sauts de ligne
                        setText(item.replace(",", ",\n"));
                        setWrapText(true);
                    }
                }
            };
        });

        // Configuration de la colonne Statut
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutDisp"));
        colStatut.setCellFactory(column -> {
            return new TableCell<Disponibilite, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        getStyleClass().removeAll("statut-disponible", "statut-indisponible");
                    } else {
                        setText(item);

                        // Appliquer le style approprié
                        getStyleClass().removeAll("statut-disponible", "statut-indisponible");
                        if (item.equals("Disponible")) {
                            getStyleClass().add("statut-disponible");
                        } else if (item.equals("Indisponible")) {
                            getStyleClass().add("statut-indisponible");
                        }
                    }
                }
            };
        });

        // Configuration de la colonne Médecin
        // Configuration de la colonne Médecin
        colMedecin.setCellValueFactory(cellData -> {
            int medecinId = cellData.getValue().getIdMedecin();
            try {
                // Utiliser votre méthode existante getOneById
                User medecin = userService.getOneById(medecinId);
                if (medecin != null) {
                    return new SimpleStringProperty("Dr. " + medecin.getPrenom() + " " + medecin.getNom());
                } else {
                    return new SimpleStringProperty("Médecin #" + medecinId);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return new SimpleStringProperty("Erreur");
            }
        });
    }

    private void loadMedecins() {
        try {
            List<User> medecins = userService.getAllMedecins();
            searchMedecinComboBox.getItems().clear();
            searchMedecinComboBox.getItems().addAll(medecins);

            // Configuration de l'affichage des médecins dans la ComboBox
            searchMedecinComboBox.setCellFactory(param -> new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        setText("Dr. " + user.getPrenom() + " " + user.getNom());
                    }
                }
            });

            searchMedecinComboBox.setButtonCell(new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        setText("Dr. " + user.getPrenom() + " " + user.getNom());
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste des médecins");
        }
    }

    private void setupActionButtons() {
        colActions.setCellFactory(param -> new TableCell<Disponibilite, Void>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox actionsBox = new HBox(10, btnModifier, btnSupprimer);

            {
                // Configurer les styles des boutons avec des dimensions plus grandes
                btnModifier.getStyleClass().addAll("action-button", "edit-button");
                btnSupprimer.getStyleClass().addAll("action-button", "delete-button");

                // Fixer les dimensions minimales pour s'assurer que le texte complet est visible
                btnModifier.setMinWidth(100);
                btnSupprimer.setMinWidth(100);
                btnModifier.setPrefHeight(35);
                btnSupprimer.setPrefHeight(35);

                // Ajouter des icônes (optionnel)
                try {
                    ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/assets/edit-icon.png")));
                    ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/assets/delete-icon.png")));
                    editIcon.setFitWidth(16);
                    editIcon.setFitHeight(16);
                    deleteIcon.setFitWidth(16);
                    deleteIcon.setFitHeight(16);

                    btnModifier.setGraphic(editIcon);
                    btnSupprimer.setGraphic(deleteIcon);
                } catch (Exception e) {
                    // Si les icônes ne sont pas disponibles, continuer sans elles
                    System.out.println("Icônes non disponibles: " + e.getMessage());
                }

                // Configurer le conteneur avec plus d'espace
                actionsBox.setAlignment(Pos.CENTER);
                actionsBox.setSpacing(12); // Augmenter l'espacement entre les boutons
                actionsBox.getStyleClass().add("actions-container");

                // S'assurer que la cellule est assez large pour contenir les boutons
                this.setMinWidth(220);
                this.setPrefWidth(220);

                // Ajouter des événements
                btnModifier.setOnAction(event -> {
                    try {
                        modifierDisponibilite(getTableView().getItems().get(getIndex()));
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la modification: " + e.getMessage());
                        e.printStackTrace();
                    }
                });

                btnSupprimer.setOnAction(event -> {
                    try {
                        supprimerDisponibilite(getTableView().getItems().get(getIndex()));
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la suppression: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                // Ajouter la classe pour le centrage et assurer l'espace adéquat
                this.getStyleClass().add("actions-cell");

                if (empty) {
                    setGraphic(null);
                } else {
                    // S'assurer que les boutons sont visibles
                    btnModifier.setVisible(true);
                    btnSupprimer.setVisible(true);
                    setGraphic(actionsBox);
                }
            }
        });
    }

    private void rafraichirTable() throws SQLException {
        List<Disponibilite> disponibilites = service.afficherDisponibilite();
        tableDisponibilite.getItems().setAll(disponibilites);

        // Mettre à jour le compteur de résultats
        if (lblInfoResultats != null) {
            int count = disponibilites.size();
            lblInfoResultats.setText("Nombre de résultats : " + count);
        }
    }

    private void modifierDisponibilite(Disponibilite dispo) {
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
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la fenêtre de modification : " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur de base de données : " + e.getMessage());
        }
    }

    private void supprimerDisponibilite(Disponibilite dispo) {
        // Demander confirmation avant de supprimer
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer cette disponibilité ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimerD(dispo);
                rafraichirTable();

                // Notification de succès
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "La disponibilité a été supprimée avec succès.");

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de supprimer la disponibilité : " + e.getMessage());
            }
        }
    }

    @FXML
    void navigateToAjouterDisponibilite(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterDisponibilite.fxml"));
            Parent root = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible d'ouvrir la page d'ajout : " + e.getMessage());
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
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible d'accéder à la liste des disponibilités");
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
            Stage stage = (Stage) rdvButton.getScene().getWindow();
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
            Stage stage = (Stage) rdvButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void toggleDispSubmenu(ActionEvent event) {
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
    void navigateToAjouterDISP(ActionEvent event) {
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

    @FXML
    void navigateToConsulterDISP(ActionEvent event) {
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

    @FXML
    void rechercherDisponibilites() {
        try {
            LocalDate jourSelectionne = searchDatePicker.getValue();
            User medecinSelectionne = searchMedecinComboBox.getValue();

            if (jourSelectionne == null && medecinSelectionne == null) {
                showAlert(Alert.AlertType.WARNING, "Attention",
                        "Veuillez sélectionner au moins un critère de recherche (jour ou médecin)");
                return;
            }

            List<Disponibilite> resultats;

            if (jourSelectionne != null && medecinSelectionne != null) {
                // Recherche par jour et médecin
                resultats = service.rechercherDisponibiliteParJourEtMedecin(jourSelectionne, medecinSelectionne.getId());
            } else if (jourSelectionne != null) {
                // Recherche par jour uniquement
                resultats = service.rechercherDisponibiliteParJour(jourSelectionne);
            } else {
                // Recherche par médecin uniquement
                resultats = service.rechercherDisponibiliteParMedecin(medecinSelectionne.getId());
            }

            tableDisponibilite.getItems().clear();
            tableDisponibilite.getItems().addAll(resultats);

            // Mettre à jour le compteur de résultats
            if (lblInfoResultats != null) {
                int count = resultats.size();
                lblInfoResultats.setText("Nombre de résultats : " + count);
            }

            if (resultats.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Résultats",
                        "Aucune disponibilité trouvée pour les critères sélectionnés");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue lors de la recherche: " + e.getMessage());
        }
    }

    @FXML
    void reinitialiserRecherche() {
        searchDatePicker.setValue(null);
        searchMedecinComboBox.getSelectionModel().clearSelection();
        try {
            rafraichirTable();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue lors de la réinitialisation: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}