package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import tn.esprit.entites.Disponibilite;
import tn.esprit.entites.User;
import tn.esprit.services.DisponibiliteService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.services.UserService;

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
        try {
            List<User> medecins = userService.getAllMedecins();
            searchMedecinComboBox.getItems().clear();
            searchMedecinComboBox.getItems().addAll(medecins);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste des médecins");
        }

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

    // Ajouter cette méthode pour réinitialiser la recherche
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

    // Ajouter cette méthode utilitaire pour afficher des alertes
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
