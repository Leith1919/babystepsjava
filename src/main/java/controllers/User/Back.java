package controllers.User;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Back {
    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private StackPane contentArea;

    @FXML
    private Label pageTitleLabel;

    // Boutons de la sidebar
    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnRendezVous;

    @FXML
    private Button btnDisponibilitesAdd;

    @FXML
    private Button btnDisponibilitesView;

    @FXML
    private Button btnAjouterGrossesse;

    @FXML
    private Button btnSuiviGrossesse;

    @FXML
    private Button btnChambres;
    @FXML
    private Button btnBlog;

    @FXML
    private Button btnOrdonnances;
    @FXML
    private Button btnModifOrdonnances;
    @FXML
    private Button btnTraitements;
    @FXML
    private Button btnModifTraitements;

    @FXML
    private Button btnUtilisateurs;

    @FXML
    private Button btnSymptomes;

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnRefresh;

    @FXML
    private Button btnSettings;
    @FXML
    private Button btnSupprimer;


    // Variable pour stocker la vue actuellement active
    private String currentView = null;

    // Méthode d'initialisation
    public void initialize() {
        Platform.runLater(() -> {
            // Définir la taille minimale de la fenêtre
            try {
                Stage stage = (Stage) mainAnchorPane.getScene().getWindow();
                stage.setMinWidth(1000);
                stage.setMinHeight(700);
                stage.setResizable(true);
            } catch (Exception e) {
                System.out.println("Impossible de configurer la fenêtre: " + e.getMessage());
            }

            // Charger le tableau de bord par défaut
            //loadView("/Admin/Dashboard.fxml", "Tableau de bord");
        });
    }

    // Actions des boutons de la sidebar
    @FXML
    public void onDashboardClick(ActionEvent event) {
        setActiveButton(btnDashboard);
        loadView("/Admin/Dashboard.fxml", "Tableau de bord");
    }

    @FXML
    public void onRendezVousClick(ActionEvent event) {
        setActiveButton(btnRendezVous);
        loadView("/AfficherRendezVous.fxml", "Gestion des Rendez-vous");
    }

    @FXML
    public void onDisponibilitesAddClick(ActionEvent event) {
        setActiveButton(btnDisponibilitesAdd);
        loadView("/AjouterDisponibilite.fxml", "Gestion des Disponibilités");
    }

    @FXML
    public void onDisponibilitesViewClick(ActionEvent event) {
        setActiveButton(btnDisponibilitesView);
        loadView("/AfficherDisponibilite.fxml", "Gestion des Disponibilités");
    }

    @FXML
    public void onSuiviGrossesseClick(ActionEvent event) {
        setActiveButton(btnSuiviGrossesse);
        loadView("/AfficherSuiviGrossesse.fxml", "Gestion des Suivis de Grossesse");
    }

    @FXML
    public void onAjouterGrossesseClick(ActionEvent event) {
        setActiveButton(btnAjouterGrossesse);
        loadView("/AjoutSuiviGrossesse.fxml", "Ajout d'un Suivi de Grossesse");
    }

    @FXML
    public void onChambresClick(ActionEvent event) {
        setActiveButton(btnChambres);
        loadView("/Admin/Chambres.fxml", "Gestion des Chambres");
    }

    @FXML
    public void onBlogClick(ActionEvent event) {
        setActiveButton(btnBlog);
        loadView("/Articles/back/Article_back.fxml", "Gestion du Blog");
    }

    @FXML
    public void onOrdonnancesClick(ActionEvent event) {
        setActiveButton(btnOrdonnances);
        loadView("/ajouterOrdonnance.fxml", "Gestion des Ordonnances");
    }

    public void onModifOrdonnancesClick(ActionEvent actionEvent) {
        setActiveButton(btnModifOrdonnances);
        loadView("/modifierOrdonnance.fxml", "Gestion des Ordonnances");
    }
    public void onTraitementsClick(ActionEvent event) {
        setActiveButton(btnTraitements);
            loadView("/ajouterTraitement.fxml", "Gestion des Traitements");
    }
    public void onModifTraitementsClick(ActionEvent event) {
        setActiveButton(btnModifTraitements);
        loadView("/modifierTraitement.fxml", "Gestion des Traitements");
    }

    @FXML
    public void onUtilisateursClick(ActionEvent event) {
        setActiveButton(btnUtilisateurs);
        loadView("/User/AfficherUsers.fxml", "Gestion des Utilisateurs");
    }

    @FXML
    public void onAjouterClick(ActionEvent event) {
        setActiveButton(btnSymptomes);
        loadView("/User/AjouterUser.fxml", "Ajout d'un Utilisateur");
    }
    public void onSupprimerClick(ActionEvent actionEvent) {
        setActiveButton(btnSupprimer);
        loadView("/User/SupprimerUser.fxml", "Suppression d'un Utilisateur");
    }

    @FXML
    public void onLogoutClick(ActionEvent event) {
        handleLogout();
    }

    // Actions des boutons d'action
    @FXML
    public void onRefreshClick(ActionEvent event) {
        refreshCurrentView();
    }

    @FXML
    public void onSettingsClick(ActionEvent event) {
        showSettings();
    }


    // Méthode pour définir le bouton actif dans la sidebar
    private void setActiveButton(Button activeButton) {
        // Enlever la classe active de tous les boutons
        btnDashboard.getStyleClass().remove("active-sidebar-button");
        btnRendezVous.getStyleClass().remove("active-sidebar-button");
        btnDisponibilitesAdd.getStyleClass().remove("active-sidebar-button");
        btnDisponibilitesView.getStyleClass().remove("active-sidebar-button");
        btnSuiviGrossesse.getStyleClass().remove("active-sidebar-button");
        btnAjouterGrossesse.getStyleClass().remove("active-sidebar-button");
        btnChambres.getStyleClass().remove("active-sidebar-button");
        btnBlog.getStyleClass().remove("active-sidebar-button");
        btnOrdonnances.getStyleClass().remove("active-sidebar-button");
        btnModifOrdonnances.getStyleClass().remove("active-sidebar-button");
        btnTraitements.getStyleClass().remove("active-sidebar-button");
        btnModifTraitements.getStyleClass().remove("active-sidebar-button");
        btnUtilisateurs.getStyleClass().remove("active-sidebar-button");
        btnSymptomes.getStyleClass().remove("active-sidebar-button");
        btnSupprimer.getStyleClass().remove("active-sidebar-button");

        // Ajouter la classe active au bouton sélectionné
        activeButton.getStyleClass().add("active-sidebar-button");
    }

    /**
     * Méthode pour charger une vue FXML dans la zone de contenu principale
     *
     * @param fxmlPath Chemin vers le fichier FXML à charger
     * @param title Titre de la page à afficher dans l'en-tête
     */
    public void loadView(String fxmlPath, String title) {
        try {
            // Mettre à jour le titre de la page
            pageTitleLabel.setText(title);

            // Réinitialiser les classes de style du titre
            pageTitleLabel.getStyleClass().remove("disponibilite-title");
            pageTitleLabel.getStyleClass().remove("grossesse-title");

            // Appliquer la classe de style appropriée selon le contenu
            if (title.contains("Disponibilité")) {
                pageTitleLabel.getStyleClass().add("disponibilite-title");
            } else if (title.contains("Grossesse")) {
                pageTitleLabel.getStyleClass().add("grossesse-title");
            }

            // Stocker la vue actuelle
            currentView = fxmlPath;

            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Appliquer une couleur de fond uniforme à la vue chargée
            view.setStyle("-fx-background-color: #E9F6FF;");

            // Créer un ScrollPane pour le contenu
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(view);
            scrollPane.setFitToWidth(true);
            scrollPane.getStyleClass().add("content-scroll-pane");

            // Appliquer un style au ScrollPane pour un fond uniforme
            scrollPane.setStyle("-fx-background: #E9F6FF; -fx-background-color: #E9F6FF;");

            // Définir les propriétés du ScrollPane pour qu'il remplisse le contentArea
            scrollPane.prefWidthProperty().bind(contentArea.widthProperty());
            scrollPane.prefHeightProperty().bind(contentArea.heightProperty());

            // Remplacer le contenu de la zone centrale
            contentArea.getChildren().clear();
            contentArea.getChildren().add(scrollPane);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue",
                    "Une erreur est survenue lors du chargement de la vue : " + e.getMessage());
            e.printStackTrace();

            // En cas d'erreur, afficher un message dans la zone de contenu
            createErrorContent("Impossible de charger " + title,
                    "Erreur : " + e.getMessage() + "\nVérifiez que le fichier " + fxmlPath + " existe.");
        }
    }

    // Méthode pour créer un contenu d'erreur en cas de problème de chargement
    private void createErrorContent(String title, String errorMessage) {
        javafx.scene.layout.VBox errorBox = new javafx.scene.layout.VBox(20);
        errorBox.setAlignment(javafx.geometry.Pos.CENTER);
        errorBox.setPadding(new javafx.geometry.Insets(50));
        errorBox.setStyle("-fx-background-color: #E9F6FF;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ff5a87;");

        Label messageLabel = new Label(errorMessage);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
        messageLabel.setWrapText(true);

        Button retryButton = new Button("Réessayer");
        retryButton.getStyleClass().add("action-button");
        retryButton.setOnAction(event -> refreshCurrentView());

        errorBox.getChildren().addAll(titleLabel, messageLabel, retryButton);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(errorBox);
    }

    // Actualiser la vue actuelle
    private void refreshCurrentView() {
        if (currentView != null) {
            // Recharger la vue actuelle
            loadView(currentView, pageTitleLabel.getText());

            // Notification de rechargement
            showAlert(Alert.AlertType.INFORMATION, "Actualisation", null,
                    "La page a été actualisée avec succès.");
        }
    }

    // Afficher les paramètres
    private void showSettings() {
        loadView("/Admin/Settings.fxml", "Paramètres");
    }

    // Gérer la déconnexion
    private void handleLogout() {
        // Confirmation avant déconnexion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Déconnexion");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Redirection vers la page de login
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/User/Login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) mainAnchorPane.getScene().getWindow();
                    stage.setScene(new javafx.scene.Scene(root));
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de se déconnecter",
                            "Une erreur est survenue lors de la déconnexion : " + e.getMessage());
                }
            }
        });
    }

    // Afficher une alerte
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    /**
     * Loads a pre-loaded Parent root into the content area
     *
     * @param view  The pre-loaded Parent root to display
     * @param title The title of the page to display in the header
     */
    public void loadViewWithRoot(Parent view, String title) {
        // Update the page title
        pageTitleLabel.setText(title);

        // Reset title style classes
        pageTitleLabel.getStyleClass().remove("disponibilite-title");
        pageTitleLabel.getStyleClass().remove("grossesse-title");

        // Apply appropriate style class based on content
        if (title.contains("Disponibilité")) {
            pageTitleLabel.getStyleClass().add("disponibilite-title");
        } else if (title.contains("Grossesse")) {
            pageTitleLabel.getStyleClass().add("grossesse-title");
        }

        // Apply consistent background color to the view
        view.setStyle("-fx-background-color: #E9F6FF;");

        // Create a ScrollPane for the content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(view);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("content-scroll-pane");

        // Apply style to ScrollPane for consistent background
        scrollPane.setStyle("-fx-background: #E9F6FF; -fx-background-color: #E9F6FF;");

        // Set ScrollPane properties to fill contentArea
        scrollPane.prefWidthProperty().bind(contentArea.widthProperty());
        scrollPane.prefHeightProperty().bind(contentArea.heightProperty());

        // Replace the content of the content area
        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }
}