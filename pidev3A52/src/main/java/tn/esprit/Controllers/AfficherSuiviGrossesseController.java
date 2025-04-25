package tn.esprit.Controllers;

import com.sun.javafx.menu.MenuItemBase;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import tn.esprit.entities.suiviGrossesse;
import tn.esprit.services.SuiviGrossesseService;
import tn.esprit.services.SuiviBebeService;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AfficherSuiviGrossesseController implements Initializable {

    @FXML private TableView<suiviGrossesse> tableView_SuiviGrossesse;
    @FXML private TableColumn<suiviGrossesse, String> colDate;
    @FXML private TableColumn<suiviGrossesse, Double> colPoids;
    @FXML private TableColumn<suiviGrossesse, String> colTension;
    @FXML private TableColumn<suiviGrossesse, String> colSymptomes;
    @FXML private TableColumn<suiviGrossesse, String> colEtat;
    @FXML private TableColumn<suiviGrossesse, Void> colModifier;
    @FXML private TableColumn<suiviGrossesse, Void> colSupprimer;
    @FXML private TableColumn<suiviGrossesse, Void> colVoirSuiviBebe;

    private final SuiviGrossesseService service = new SuiviGrossesseService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        colDate.setCellValueFactory(new PropertyValueFactory<>("dateSuivi"));
        colPoids.setCellValueFactory(new PropertyValueFactory<>("poids"));
        colTension.setCellValueFactory(new PropertyValueFactory<>("tension"));
        colSymptomes.setCellValueFactory(new PropertyValueFactory<>("symptomes"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etatGrossesse"));

        ajouterColonneVoirSuiviBebe();  // 👶 Voir Suivi Bébé
        ajouterColonneModifier();
        ajouterColonneSupprimer();

        loadSuivis();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), tableView_SuiviGrossesse);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setCycleCount(1);
        fadeIn.play();
    }

    public void loadSuivis() {
        try {
            List<suiviGrossesse> suivis = service.recuperer();
            ObservableList<suiviGrossesse> observableList = FXCollections.observableArrayList(suivis);
            tableView_SuiviGrossesse.setItems(observableList);
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erreur de chargement", "Impossible de charger les suivis de grossesse", e.getMessage());
        }
    }

    private void ajouterColonneVoirSuiviBebe() {
        Callback<TableColumn<suiviGrossesse, Void>, TableCell<suiviGrossesse, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btn = new Button("Suivi Bébé");

            {
                btn.setOnAction(event -> {
                    suiviGrossesse sg = getTableView().getItems().get(getIndex());
                    if (sg != null) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherSuiviBebe.fxml"));
                            Parent root = loader.load();

                            // Transmettre le suivi grossesse au contrôleur Suivi Bébé
                            AfficherSuiviBebeController controller = loader.getController();
                            controller.setSuiviGrossesse(sg);
                            System.out.println("Transmission du suivi grossesse ID: " + sg.getId() + " à AfficherSuiviBebeController");

                            Stage stage = new Stage();
                            stage.setTitle("Détails Suivi Bébé");
                            stage.setScene(new Scene(root));
                            stage.initModality(Modality.APPLICATION_MODAL);
                            stage.showAndWait();

                        } catch (IOException e) {
                            e.printStackTrace();
                            showErrorAlert("Erreur d'affichage", "Impossible d'ouvrir la fenêtre de suivi bébé", e.getMessage());
                        }
                    } else {
                        System.err.println("Erreur: suiviGrossesse est null!");
                        showErrorAlert("Erreur", "Sélection invalide", "Aucun suivi de grossesse sélectionné");
                    }
                });

                btn.setStyle("-fx-background-color: #5bc0de; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        };

        colVoirSuiviBebe.setCellFactory(cellFactory);
    }

    private void ajouterColonneModifier() {
        Callback<TableColumn<suiviGrossesse, Void>, TableCell<suiviGrossesse, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btn = new Button("Modifier");

            {
                btn.setOnAction(event -> {
                    suiviGrossesse sg = getTableView().getItems().get(getIndex());
                    if (sg != null) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierSuiviGrossesse.fxml"));
                            Parent root = loader.load();

                            ModifierSuiviGrossesseController controller = loader.getController();
                            controller.setSuiviGrossesse(sg);

                            Stage stage = new Stage();
                            stage.setTitle("Modifier Suivi Grossesse");
                            stage.setScene(new Scene(root));
                            stage.initModality(Modality.APPLICATION_MODAL);
                            stage.showAndWait();

                            loadSuivis(); // Recharge après modification

                        } catch (IOException e) {
                            e.printStackTrace();
                            showErrorAlert("Erreur d'affichage", "Impossible d'ouvrir la fenêtre de modification", e.getMessage());
                        }
                    } else {
                        showErrorAlert("Erreur", "Sélection invalide", "Aucun suivi de grossesse sélectionné");
                    }
                });

                btn.setStyle("-fx-background-color: #f0ad4e; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        };

        colModifier.setCellFactory(cellFactory);
    }

    private void ajouterColonneSupprimer() {
        Callback<TableColumn<suiviGrossesse, Void>, TableCell<suiviGrossesse, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btn = new Button("Supprimer");

            {
                btn.setOnAction(event -> {
                    suiviGrossesse sg = getTableView().getItems().get(getIndex());
                    if (sg != null) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirmation");
                        alert.setHeaderText("Suppression");
                        alert.setContentText("Voulez-vous vraiment supprimer ce suivi de grossesse ?");

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            try {
                                service.supprimer(sg);
                                loadSuivis(); // Recharge après suppression
                                showInfoAlert("Succès", "Suppression réussie", "Le suivi de grossesse a été supprimé avec succès.");
                            } catch (SQLException e) {
                                e.printStackTrace();
                                showErrorAlert("Erreur de suppression", "Impossible de supprimer le suivi de grossesse", e.getMessage());
                            }
                        }
                    } else {
                        showErrorAlert("Erreur", "Sélection invalide", "Aucun suivi de grossesse sélectionné");
                    }
                });

                btn.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        };

        colSupprimer.setCellFactory(cellFactory);
    }

    // Méthodes utilitaires pour les alertes
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


}