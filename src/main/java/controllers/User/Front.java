package controllers.User;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import utils.UserSession;
import models.User;

import java.io.IOException;

public class Front {

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button loginButton;

    @FXML
    private MenuButton menuDisp;

    @FXML
    private MenuItem menuItemAjouterRdv;

    @FXML
    private MenuItem menuItemConsulterDisp;

    @FXML
    private MenuItem menuItemConsulterRdv;

    @FXML
    private MenuItem menuItemSuiviBEBE;

    @FXML
    private MenuItem menuItemSuiviGRS;

    @FXML
    private MenuButton menuRdv;

    @FXML
    private MenuButton menuSuivi;

    @FXML
    private Button signupButton;

    @FXML
    private MenuButton menuProfil;

    @FXML
    private MenuItem menuItemModifierProfil;

    @FXML
    private MenuItem menuItemChangerMdp;

    @FXML
    private MenuItem menuItemOrd;

    @FXML
    private MenuItem menuItemTrait;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button homeButton;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            Stage stage = (Stage) mainAnchorPane.getScene().getWindow();

            // Définir la taille minimale de la fenêtre
            stage.setMinWidth(800);
            stage.setMinHeight(600);

            // Rendre la fenêtre redimensionnable
            stage.setResizable(true);

            // Configurer le message de bienvenue
            User currentUser = UserSession.getCurrentUser();
            if (welcomeLabel != null) {
                if (currentUser == null) {
                    welcomeLabel.setText("Bienvenue sur votre application de gestion des rendez-vous");
                } else {
                    String nom = currentUser.getNom() != null ? currentUser.getNom() : "";
                    String prenom = currentUser.getPrenom() != null ? currentUser.getPrenom() : "";
                    welcomeLabel.setText("Bienvenue " + nom + " " + prenom);
                }
            }
        });
    }

    @FXML
    public void onLoginButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/User/Login.fxml"));
            Parent loginView = loader.load();
            adaptContentToMainArea(loginView);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(loginView);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue de connexion",
                    "Une erreur est survenue lors du chargement de la vue de connexion : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onSignupButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/User/inscription.fxml"));
            Parent signupView = loader.load();
            adaptContentToMainArea(signupView);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(signupView);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue d'inscription",
                    "Une erreur est survenue lors du chargement de la vue d'inscription : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Front.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) mainAnchorPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de se déconnecter",
                    "Une erreur est survenue lors de la déconnexion : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void navigateToAjouterRDV(ActionEvent event) {
        loadView("/AjouterRendezVousFront.fxml");
    }

    @FXML
    public void navigateToConsulterRDV(ActionEvent event) {
        loadView("/AfficherRendezVousCard.fxml");
    }

    @FXML
    public void navigateToConsulterDISP(ActionEvent event) {
        loadView("/AfficherDisponibiliteFront.fxml");
    }

    @FXML
    public void navigateToAnalyseSymptomes(ActionEvent event) {
        loadView("/AnalyseSymptomes.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            adaptContentToMainArea(view);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue",
                    "Une erreur est survenue lors du chargement de la vue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void adaptContentToMainArea(Parent content) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.prefWidthProperty().bind(contentArea.widthProperty());
        scrollPane.prefHeightProperty().bind(contentArea.heightProperty());
        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Stage getStageFromEvent(ActionEvent event) {
        Node source = (Node) event.getSource();
        return (Stage) source.getScene().getWindow();
    }

    public void navigateToSuiviGRS(ActionEvent actionEvent) {
        loadView("/AfficherSuiviGrossesseFront.fxml");
    }

    public void navigateToSuiviBEBE(ActionEvent actionEvent) {
        loadView("/AfficherSuiviBebeView.fxml");
    }

    public void navigateToModifierProfil(ActionEvent actionEvent) {
        loadView("/User/EditProfile.fxml");
    }

    public void navigateToHomePage(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Front/Front.fxml"));
            Parent root = loader.load();
            Scene scene = homeButton.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page d'accueil",
                    "Erreur lors du chargement de l'interface : " + e.getMessage());
        }
    }

    public void navigateToBlog(ActionEvent actionEvent) {
        loadView("/Articles/Front/Article_Front.fxml");
    }

    public void navigateToOrd(ActionEvent actionEvent) {
        loadView("/back/afficherOrdonnance.fxml");
    }

    public void navigateToTrait(ActionEvent actionEvent) {
        loadView("/back/afficherTraitement.fxml");
    }

    public void navigateToHome(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Front/Front.fxml"));
            Parent root = loader.load();
            Scene scene = homeButton.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page d'accueil",
                    "Erreur lors du chargement de l'interface : " + e.getMessage());
        }
    }
}