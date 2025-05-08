package controllers.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Callback;
import models.Disponibilite;
import models.User;
import services.User.DisponibiliteService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.User.UserService;

public class AfficherDisponibiliteFront {

    @FXML
    private TableView<Disponibilite> tableDisponibilite;

    @FXML
    private TableColumn<Disponibilite, LocalDate> colJour;

    @FXML
    private TableColumn<Disponibilite, String> colHeures;

    @FXML
    private TableColumn<Disponibilite, String> colStatut;

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

    private UserService userService = new UserService();
    private final DisponibiliteService service = new DisponibiliteService();

    @FXML
    public void initialize() throws SQLException {
        // Configuration de l'affichage des dates
        colJour.setCellValueFactory(new PropertyValueFactory<>("jour"));
        setupJourColumn();

        // Configuration de l'affichage des heures
        colHeures.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.join(", ", cellData.getValue().getHeuresDisp()))
        );
        setupHeuresColumn();

        // Configuration de l'affichage du statut
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutDisp"));
        setupStatutColumn();

        // Chargement des médecins dans le ComboBox
        try {
            List<User> medecins = userService.getAllMedecins();
            searchMedecinComboBox.getItems().clear();
            searchMedecinComboBox.getItems().addAll(medecins);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste des médecins");
        }
        searchMedecinComboBox.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getNom() + " " + user.getPrenom());
                }
            }
        });

// Pour l'élément sélectionné (affichage dans la zone du bouton)
        searchMedecinComboBox.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getNom() + " " + user.getPrenom());
                }
            }
        });


        // Ajout des boutons d'action et chargement des données
        ajouterBoutonsActions();
        rafraichirTable();
    }

    /**
     * Configure la colonne des jours pour un affichage amélioré
     */
    private void setupJourColumn() {
        colJour.setCellFactory(column -> new TableCell<Disponibilite, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if (empty || date == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox dateContainer = new VBox(2);
                    dateContainer.setAlignment(Pos.CENTER_LEFT);

                    // Format de date : Jour de semaine + date complète
                    Label dateFormatee = new Label(date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRANCE)));
                    dateFormatee.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e293b;");

                    Label jourSemaine = new Label(date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRANCE));
                    jourSemaine.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

                    dateContainer.getChildren().addAll(dateFormatee, jourSemaine);
                    setGraphic(dateContainer);
                }
            }
        });
    }

    /**
     * Configure la colonne des heures pour un affichage amélioré
     */
    private void setupHeuresColumn() {
        colHeures.setCellFactory(column -> new TableCell<Disponibilite, String>() {
            @Override
            protected void updateItem(String heures, boolean empty) {
                super.updateItem(heures, empty);

                if (empty || heures == null || heures.isEmpty()) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Créer un conteneur pour les plages horaires
                    VBox plagesContainer = new VBox(5);
                    plagesContainer.setPadding(new Insets(5, 0, 5, 0));

                    // Diviser les heures par virgule
                    String[] plages = heures.split(",");

                    for (String plage : plages) {
                        String plageFormatted = plage.trim();

                        // Créer un HBox pour chaque plage horaire
                        HBox plageBox = new HBox(10);
                        plageBox.setAlignment(Pos.CENTER_LEFT);

                        // Ajouter un indicateur visuel (cercle bleu)
                        Circle circle = new Circle(4);
                        circle.setFill(Color.valueOf("#3182ce"));
                        plageBox.getChildren().add(circle);

                        // Formatter la plage horaire
                        if (plageFormatted.contains("-")) {
                            String[] parts = plageFormatted.split("-");
                            if (parts.length == 2) {
                                plageFormatted = parts[0].trim() + " → " + parts[1].trim();
                            }
                        }

                        // Ajouter le label de la plage horaire
                        Label plageLabel = new Label(plageFormatted);
                        plageLabel.setStyle("-fx-font-size: 13px;");
                        plageBox.getChildren().add(plageLabel);

                        // Ajouter un style au conteneur de la plage
                        plageBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 4px; -fx-padding: 4px 8px;");

                        // Ajouter cette plage au conteneur principal
                        plagesContainer.getChildren().add(plageBox);
                    }

                    setGraphic(plagesContainer);
                }
            }
        });
    }

    /**
     * Configure la colonne de statut pour un affichage amélioré
     */
    private void setupStatutColumn() {
        colStatut.setCellFactory(column -> new TableCell<Disponibilite, String>() {
            private final Label statutLabel = new Label();

            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);

                if (empty || statut == null) {
                    setGraphic(null);
                } else {
                    // Appliquer un style selon le statut
                    switch (statut.toLowerCase()) {
                        case "disponible":
                            statutLabel.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 12px; -fx-padding: 3px 10px; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                        case "indisponible":
                            statutLabel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 12px; -fx-padding: 3px 10px; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                        case "partiel":
                        case "partiellement disponible":
                            statutLabel.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-background-radius: 12px; -fx-padding: 3px 10px; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                        default:
                            statutLabel.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-background-radius: 12px; -fx-padding: 3px 10px; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                    }

                    statutLabel.setText(statut);
                    setGraphic(statutLabel);
                }
            }
        });
    }

    private void rafraichirTable() throws SQLException {
        tableDisponibilite.getItems().setAll(service.afficherDisponibilite());
    }

    private void ajouterBoutonsActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnAjouter = new Button("Prendre RDV");

            {
                // Style amélioré pour le bouton
                btnAjouter.setStyle("-fx-background-color: #2c5282; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 5px 10px;");

                // Ajouter des effets de survol
                btnAjouter.setOnMouseEntered(e -> btnAjouter.setStyle("-fx-background-color: #1e40af; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 5px 10px;"));
                btnAjouter.setOnMouseExited(e -> btnAjouter.setStyle("-fx-background-color: #2c5282; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 5px 10px;"));

                btnAjouter.setOnAction(event -> {
                    Disponibilite dispo = getTableView().getItems().get(getIndex());

                    // Récupérer seulement le nom et prénom du médecin
                    try {
                        int medecinId = dispo.getIdMedecin();
                        User medecin = userService.getOneById(medecinId);

                        if (medecin != null) {
                            // Extraire seulement le nom et prénom
                            String nomMedecin = medecin.getNom();
                            String prenomMedecin = medecin.getPrenom();

                            // Passer seulement le nom et prénom au formulaire
                            ouvrirFormulaireRendezVous(dispo, nomMedecin, prenomMedecin);
                        } else {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Information manquante");
                            alert.setHeaderText(null);
                            alert.setContentText("Impossible de trouver les informations du médecin.");
                            alert.showAndWait();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur");
                        alert.setHeaderText(null);
                        alert.setContentText("Erreur lors de la récupération des informations du médecin: " + e.getMessage());
                        alert.showAndWait();
                    }
                });
            }

            private void ouvrirFormulaireRendezVous(Disponibilite dispo, String nomMedecin, String prenomMedecin) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/PrendreRendezVous.fxml"));
                    Parent root = loader.load();

                    // Obtenir le contrôleur et initialiser les données
                    AjouterRendezVous controller = loader.getController();

                    // Passer seulement le nom et prénom du médecin
                    controller.initialiserAvecDisponibilite(dispo, nomMedecin, prenomMedecin);

                    // Créer une nouvelle scène ou utiliser une fenêtre existante
                    Stage stage = new Stage();
                    stage.setTitle("Ajouter un rendez-vous");
                    stage.setScene(new Scene(root));
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Impossible d'ouvrir le formulaire d'ajout de rendez-vous: " + e.getMessage());
                    alert.showAndWait();
                }
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnAjouter);
                }
            }
        });
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

   /* public void afficherFormulaireAjout(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterRendezVousFront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Formulaire d'ajout");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

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

  /*  @FXML
    private void navigateToAjouterRDV() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterRendezVousFront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) rdvButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

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
            Scene scene = rdvButton.getScene();
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

    @FXML
    private void rechercherDisponibilites() {
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
    private void reinitialiserRecherche() {
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