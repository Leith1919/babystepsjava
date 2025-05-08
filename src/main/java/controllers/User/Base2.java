package controllers.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.RendezVous;
import services.User.RendezVousService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Base2 implements Initializable {
    @FXML
    private Button btnAccueil;

    @FXML
    private MenuButton menuRdv;

    @FXML
    private MenuItem menuItemAjouterRdv;

    @FXML
    private MenuItem menuItemConsulterRdv;

    @FXML
    private MenuButton menuDisp;

    @FXML
    private MenuItem menuItemAjouterDisp;

    @FXML
    private MenuItem menuItemConsulterDisp;

    @FXML
    private Button btnSuiviBebe;

    @FXML
    private Button btnChambres;

    @FXML
    private Button btnBlog;

    @FXML
    private Button btnOrdonnance;

    @FXML
    private Button btnProfile;

    @FXML
    private Button btnNotifications;

    @FXML
    private Button btnLogout;

    @FXML
    private StackPane contentArea;

    @FXML
    private Label lblProchainRdv;

    @FXML
    private Label lblDocteursDispo;

    @FXML
    private Label lblMonSuivi;

    private RendezVousService rendezVousService;

    public Base2() {
        this.rendezVousService = new RendezVousService();
    }

    /**
     * Initialise les composants lors du chargement de l'interface
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Charger les informations pour la page d'accueil
        loadDashboardInfo();
    }

    /**
     * Charge les informations pour la page d'accueil (prochain rendez-vous, etc.)
     */
    private void loadDashboardInfo() {
        // Charger le prochain rendez-vous
        try {
            List<RendezVous> rendezVousList = rendezVousService.afficher();

            // Filtrer pour ne garder que les rendez-vous futurs et trier par date
            List<RendezVous> futurRendezVous = rendezVousList.stream()
                    .filter(rdv -> !rdv.getJour().isBefore(LocalDate.now()))
                    .sorted(Comparator.comparing(RendezVous::getJour).thenComparing(RendezVous::getHeureString))
                    .collect(Collectors.toList());

            if (!futurRendezVous.isEmpty()) {
                RendezVous prochainRdv = futurRendezVous.get(0);
                String formattedDate = prochainRdv.getJour().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                lblProchainRdv.setText(formattedDate + " à " + prochainRdv.getHeureString());
            } else {
                lblProchainRdv.setText("Aucun rendez-vous prévu");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            lblProchainRdv.setText("Erreur de chargement");
        }

        // Note: Pour les autres informations comme les docteurs disponibles et les rapports,
        // vous devrez implémenter des services spécifiques
    }

    /**
     * Navigation vers la page d'accueil
     */
    @FXML
    public void navigateToAccueil(ActionEvent event) {
        try {
            Parent accueilPage = FXMLLoader.load(getClass().getResource("/Base2.fxml"));
            Scene scene = new Scene(accueilPage);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de la page d'accueil: " + e.getMessage());
        }
    }

    /**
     * Navigation vers la page d'ajout de rendez-vous
     */
    @FXML
    public void navigateToAjouterRDV(ActionEvent event) {
        try {
            // Charger uniquement le contenu principal sans la navbar
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterRendezVousFront.fxml"));
            AnchorPane ajouterRdvContent = loader.load();

            // Adapter le contenu à la zone centrale
            adaptContentToMainArea(ajouterRdvContent);

            // Remplacer le contenu dans la zone centrale
            contentArea.getChildren().clear();
            contentArea.getChildren().add(ajouterRdvContent);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface AjouterRendezVous: " + e.getMessage());

            // En cas d'échec, afficher une alerte et rester sur la page actuelle
            showErrorAlert("Erreur de navigation", "Impossible de charger la page d'ajout de rendez-vous.");
        }
    }

    /**
     * Navigation vers la page de consultation des rendez-vous
     */
    @FXML
    public void navigateToConsulterRDV(ActionEvent event) {
        try {
            // Charger uniquement le contenu principal sans la navbar
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherRendezVousCard.fxml"));
            AnchorPane consulterRdvContent = loader.load();

            // Adapter le contenu à la zone centrale
            adaptContentToMainArea(consulterRdvContent);

            // Remplacer le contenu dans la zone centrale
            contentArea.getChildren().clear();
            contentArea.getChildren().add(consulterRdvContent);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface ConsulterRendezVous: " + e.getMessage());

            // En cas d'échec, afficher une alerte
            showErrorAlert("Erreur de navigation", "Impossible de charger la page de consultation des rendez-vous.");
        }
    }

    /**
     * Navigation vers la page des disponibilités
     */
    @FXML
    public void navigateToConsulterDISP(ActionEvent event) {
        try {
            // Charger uniquement le contenu principal sans la navbar
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherDisponibiliteFront.fxml"));
            AnchorPane consulterDispContent = loader.load();

            // Adapter le contenu à la zone centrale
            adaptContentToMainArea(consulterDispContent);

            // Remplacer le contenu dans la zone centrale
            contentArea.getChildren().clear();
            contentArea.getChildren().add(consulterDispContent);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface ConsulterDisponibilite: " + e.getMessage());

            // En cas d'échec, afficher une alerte
            showErrorAlert("Erreur de navigation", "Impossible de charger la page de consultation des disponibilités.");
        }
    }

    /**
     * Navigation vers la page de suivi bébé
     */
    @FXML
    public void navigateToSuiviBebe(ActionEvent event) {
        try {
            // Charger uniquement le contenu principal sans la navbar
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SuiviBebeContent.fxml"));
            AnchorPane suiviBebeContent = loader.load();

            // Adapter le contenu à la zone centrale
            adaptContentToMainArea(suiviBebeContent);

            // Remplacer le contenu dans la zone centrale
            contentArea.getChildren().clear();
            contentArea.getChildren().add(suiviBebeContent);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface SuiviBebe: " + e.getMessage());

            // En cas d'échec, afficher une alerte
            showErrorAlert("Erreur de navigation", "Impossible de charger la page de suivi bébé.");
        }
    }

    /**
     * Navigation vers la page des chambres
     */
    @FXML
    public void navigateToChambres(ActionEvent event) {
        try {
            // Charger uniquement le contenu principal sans la navbar
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChambresContent.fxml"));
            AnchorPane chambresContent = loader.load();

            // Adapter le contenu à la zone centrale
            adaptContentToMainArea(chambresContent);

            // Remplacer le contenu dans la zone centrale
            contentArea.getChildren().clear();
            contentArea.getChildren().add(chambresContent);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface Chambres: " + e.getMessage());

            // En cas d'échec, afficher une alerte
            showErrorAlert("Erreur de navigation", "Impossible de charger la page des chambres.");
        }
    }

    /**
     * Navigation vers la page du blog
     */
    @FXML
    public void navigateToBlog(ActionEvent event) {
        try {
            // Charger uniquement le contenu principal sans la navbar
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogContent.fxml"));
            AnchorPane blogContent = loader.load();

            // Adapter le contenu à la zone centrale
            adaptContentToMainArea(blogContent);

            // Remplacer le contenu dans la zone centrale
            contentArea.getChildren().clear();
            contentArea.getChildren().add(blogContent);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface Blog: " + e.getMessage());

            // En cas d'échec, afficher une alerte
            showErrorAlert("Erreur de navigation", "Impossible de charger la page du blog.");
        }
    }

    /**
     * Navigation vers la page des ordonnances
     */
    @FXML
    public void navigateToOrdonnance(ActionEvent event) {
        try {
            // Charger uniquement le contenu principal sans la navbar
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OrdonnanceContent.fxml"));
            AnchorPane ordonnanceContent = loader.load();

            // Adapter le contenu à la zone centrale
            adaptContentToMainArea(ordonnanceContent);

            // Remplacer le contenu dans la zone centrale
            contentArea.getChildren().clear();
            contentArea.getChildren().add(ordonnanceContent);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface Ordonnance: " + e.getMessage());

            // En cas d'échec, afficher une alerte
            showErrorAlert("Erreur de navigation", "Impossible de charger la page des ordonnances.");
        }
    }

    /**
     * Navigation vers la page d'analyse des symptômes
     */
    @FXML
    public void navigateToAnalyseSymptomes(ActionEvent event) {
        try {
            // Charger uniquement le contenu principal sans la navbar
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AnalyseSymptomes.fxml"));
            AnchorPane analyseContent = loader.load();

            // Adapter le contenu à la zone centrale
            adaptContentToMainArea(analyseContent);

            // Remplacer le contenu dans la zone centrale
            contentArea.getChildren().clear();
            contentArea.getChildren().add(analyseContent);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface d'analyse: " + e.getMessage());

            // Afficher une alerte en cas d'erreur
            showErrorAlert("Erreur de navigation", "Impossible de charger la page d'analyse des symptômes.");
        }
    }

    /**
     * Déconnexion de l'application
     */
    @FXML
    public void onLogout(ActionEvent event) {
        try {
            // Demander confirmation avant de se déconnecter
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation de déconnexion");
            confirmAlert.setHeaderText("Voulez-vous vraiment vous déconnecter ?");
            confirmAlert.setContentText("Toutes les modifications non enregistrées seront perdues.");

            if (confirmAlert.showAndWait().get().getButtonData().isDefaultButton()) {
                Parent loginPage = FXMLLoader.load(getClass().getResource("/Login.fxml"));
                Scene scene = ((Node) event.getSource()).getScene();
                Stage stage = (Stage) scene.getWindow();

                // Transition vers la page de connexion
                scene = new Scene(loginPage, 900, 600);
                scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());
                stage.setScene(scene);
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de la page de connexion: " + e.getMessage());

            // Afficher une alerte en cas d'erreur
            showErrorAlert("Erreur de déconnexion", "Impossible de charger la page de connexion.");
        }
    }

    /**
     * Charge une interface complète (en cas d'échec du chargement partiel)
     */
    private void loadFullInterface(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = ((Node) contentArea).getScene();
            Stage stage = (Stage) scene.getWindow();

            scene = new Scene(root, scene.getWidth(), scene.getHeight());
            scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface " + fxmlPath + ": " + e.getMessage());
        }
    }

    /**
     * Adapte un contenu chargé pour qu'il s'intègre correctement dans la zone principale
     */
    private void adaptContentToMainArea(AnchorPane content) {
        // Configurer les ancrages pour que le contenu remplisse toute la zone centrale
        AnchorPane.setTopAnchor(content, 0.0);
        AnchorPane.setBottomAnchor(content, 0.0);
        AnchorPane.setLeftAnchor(content, 0.0);
        AnchorPane.setRightAnchor(content, 0.0);
    }

    /**
     * Affiche une alerte d'erreur
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}