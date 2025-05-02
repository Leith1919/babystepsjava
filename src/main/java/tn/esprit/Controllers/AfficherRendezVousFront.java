package tn.esprit.Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.entites.Disponibilite;
import tn.esprit.entites.RendezVous;
import tn.esprit.services.RendezVousService;

// Ajout des imports manquants
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import tn.esprit.services.AnnulationRendezVousService;
import tn.esprit.services.AnnulationRendezVousService.ResultatAnnulation;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;

public class AfficherRendezVousFront {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private Label lblRendezVousCount;

    @FXML
    private TextField searchField;

    @FXML
    private Button btnToday;

    @FXML
    private Button btnWeek;

    @FXML
    private Button btnAll;

    @FXML
    private VBox rdvSubmenu;

    @FXML
    private Button rdvButton;

    @FXML
    private VBox dispSubmenu;

    @FXML
    private Button dispButton;

    private RendezVousService rendezVousService;
    private AnnulationRendezVousService annulationService;

    public AfficherRendezVousFront() {
        this.rendezVousService = new RendezVousService();
        this.annulationService = new AnnulationRendezVousService();
    }

    // Cette méthode est appelée lors de l'initialisation de la page
    @FXML
    private void initialize() throws SQLException {
        // Configuration des composants supplémentaires
        setupAdditionalComponents();

        // Configuration des événements de filtrage
        setupFilterEvents();

        // Configuration du champ de recherche
        setupSearchField();

        // Chargement initial des données
        rafraichirCards();
    }

    // Méthode pour rafraîchir les cartes
    private void rafraichirCards() throws SQLException {
        List<RendezVous> rendezVousList = rendezVousService.afficher();
        cardsContainer.getChildren().clear();

        for (RendezVous rdv : rendezVousList) {
            VBox cardNode = createRendezVousCard(rdv);
            cardsContainer.getChildren().add(cardNode);
        }

        // Mettre à jour le compteur
        updateRendezVousCount();
    }

    /**
     * Créer une carte de rendez-vous pour l'affichage
     */
    private VBox createRendezVousCard(RendezVous rdv) {
        try {
            // Charger le FXML de la carte
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RendezVousCard.fxml"));
            VBox cardPane = loader.load();

            // Récupérer les éléments de la carte
            Label lblJour = (Label) cardPane.lookup("#lblJour");
            Label lblMois = (Label) cardPane.lookup("#lblMois");
            Label lblJourSemaine = (Label) cardPane.lookup("#lblJourSemaine");
            Label lblHeure = (Label) cardPane.lookup("#lblHeure");
            Label lblMotif = (Label) cardPane.lookup("#lblMotif");
            Label lblMedecin = (Label) cardPane.lookup("#lblMedecin");
            Label lblMedecinInitial = (Label) cardPane.lookup("#lblMedecinInitial");
            Label lblTraitement = (Label) cardPane.lookup("#lblTraitement");
            Label lblNotes = (Label) cardPane.lookup("#lblNotes");
            VBox symptomsContainer = (VBox) cardPane.lookup("#symptomsContainer");
            VBox notesContainer = (VBox) cardPane.lookup("#notesContainer");
            VBox traitementContainer = (VBox) cardPane.lookup("#traitementContainer");
            ImageView imgClock = (ImageView) cardPane.lookup("#imgClock");
            Circle statusIndicator = (Circle) cardPane.lookup("#statusIndicator");
            Label lblStatus = (Label) cardPane.lookup("#lblStatus");

            Button btnModifier = (Button) cardPane.lookup("#btnModifier");
            Button btnAnnuler = (Button) cardPane.lookup("#btnAnnuler");
            Button btnSupprimer = (Button) cardPane.lookup("#btnSupprimer");

            // Configurer les données du rendez-vous
            LocalDate date = rdv.getJour();
            DateTimeFormatter jourFormatter = DateTimeFormatter.ofPattern("dd");
            DateTimeFormatter moisFormatter = DateTimeFormatter.ofPattern("MMM", Locale.FRANCE);

            // Format de la date
            lblJour.setText(date.format(jourFormatter));
            lblMois.setText(date.format(moisFormatter).toUpperCase());
            lblJourSemaine.setText(date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRANCE));
            lblHeure.setText(rdv.getHeureString());

            // Ajout de l'icône d'horloge
            try {
                Image clockImage = new Image(getClass().getResourceAsStream("/assets/icons/clock.png"));
                imgClock.setImage(clockImage);
            } catch (Exception e) {
                System.out.println("Erreur chargement image horloge: " + e.getMessage());
            }

            // Status du rendez-vous (passé ou à venir)
            boolean estPasse = date.isBefore(LocalDate.now());
            if (estPasse) {
                statusIndicator.setFill(Color.valueOf("#718096"));
                lblStatus.setText("Passé");
                lblStatus.setTextFill(Color.valueOf("#718096"));
            } else {
                statusIndicator.setFill(Color.valueOf("#4299e1"));
                lblStatus.setText("À venir");
                lblStatus.setTextFill(Color.valueOf("#4299e1"));
            }

            // Motif
            lblMotif.setText(rdv.getMotif());

            // Médecin
            String nomMedecin = rdv.getMedecin().getNom();
            lblMedecin.setText("Dr. " + nomMedecin);
            lblMedecinInitial.setText(nomMedecin.substring(0, 1).toUpperCase());

            // Traitement
            String traitement = rdv.getTraitementEnCours();
            if (traitement == null || traitement.isEmpty()) {
                traitementContainer.setVisible(false);
                traitementContainer.setManaged(false);
            } else {
                lblTraitement.setText(traitement);
            }

            // Notes
            String notes = rdv.getNotes();
            if (notes == null || notes.isEmpty()) {
                notesContainer.setVisible(false);
                notesContainer.setManaged(false);
            } else {
                lblNotes.setText(notes);
            }

            // Symptômes
            String symptomes = rdv.getSymptomes();
            if (symptomes == null || symptomes.isEmpty()) {
                symptomsContainer.setVisible(false);
                symptomsContainer.setManaged(false);
            } else {
                String[] symptomeList = symptomes.split(",");
                for (String symptome : symptomeList) {
                    String trimmedSymptome = symptome.trim();
                    if (!trimmedSymptome.isEmpty()) {
                        HBox sympItemBox = new HBox(5);
                        sympItemBox.setAlignment(Pos.CENTER_LEFT);

                        // Cercle comme marqueur
                        Circle circle = new Circle(3);
                        circle.setFill(Color.valueOf("#e74c3c"));
                        sympItemBox.getChildren().add(circle);

                        // Texte du symptôme
                        Label sympLabel = new Label(trimmedSymptome);
                        sympLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4A5568;");
                        sympItemBox.getChildren().add(sympLabel);

                        symptomsContainer.getChildren().add(sympItemBox);
                    }
                }
            }

            // Configurer les boutons d'action
            // Vérifier si le rendez-vous est passé (pour désactiver les boutons si nécessaire)
            boolean peutAnnuler = annulationService.peutEtreAnnule(rdv);
            String styleDesactive = "-fx-background-color: #E2E8F0; -fx-text-fill: #A0AEC0; -fx-background-radius: 6;";

            if (estPasse) {
                btnModifier.setDisable(true);
                btnSupprimer.setDisable(true);
                btnAnnuler.setDisable(true);

                btnModifier.setStyle(styleDesactive);
                btnSupprimer.setStyle(styleDesactive);
                btnAnnuler.setStyle(styleDesactive);

                btnModifier.setTooltip(new Tooltip("Impossible de modifier un rendez-vous passé"));
                btnSupprimer.setTooltip(new Tooltip("Impossible de supprimer un rendez-vous passé"));
                btnAnnuler.setTooltip(new Tooltip("Impossible d'annuler un rendez-vous passé"));
            } else {
                // Gérer le bouton d'annulation
                if (!peutAnnuler) {
                    btnAnnuler.setDisable(true);
                    btnAnnuler.setStyle(styleDesactive);
                    btnAnnuler.setTooltip(new Tooltip("Impossible d'annuler moins de 24h avant le rendez-vous"));
                }

                // Ajouter les actions sur les boutons
                btnModifier.setOnAction(event -> ouvrirFenetreModificationRendezVous(rdv));
                btnSupprimer.setOnAction(event -> confirmerSuppression(rdv));
                btnAnnuler.setOnAction(event -> tenterAnnulationRendezVous(rdv));
            }

            return cardPane;
        } catch (IOException e) {
            e.printStackTrace();

            // En cas d'erreur, créer une carte basique
            VBox fallbackCard = new VBox();
            fallbackCard.getChildren().add(new Label("Erreur de chargement: " + rdv.getMotif()));
            return fallbackCard;
        }
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
            rafraichirCards();

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

    // Méthode pour naviguer vers l'interface d'ajout de rendez-vous
    @FXML
    private void navigateToAjouterRDV() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterRendezVous.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) searchField.getScene().getWindow();
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
            Stage stage = (Stage) searchField.getScene().getWindow();
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
            Scene scene = searchField.getScene();

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

            Scene scene = searchField.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface ConsulterDispo: " + e.getMessage());
        }
    }

    /**
     * Pour actualiser le compteur de rendez-vous en fonction des résultats affichés
     */
    private void updateRendezVousCount() {
        int count = cardsContainer.getChildren().size();
        if (lblRendezVousCount != null) {
            lblRendezVousCount.setText(count + " rendez-vous trouvé" + (count > 1 ? "s" : ""));
        }
    }

    /**
     * Configurations supplémentaires pour les composants UI
     */
    private void setupAdditionalComponents() {
        // Vérifier que les composants existent avant de les manipuler
        if (btnToday != null && btnWeek != null && btnAll != null) {
            // Configuration des boutons de filtre
            btnToday.getStyleClass().add("filter-button");
            btnWeek.getStyleClass().add("filter-button");
            btnAll.getStyleClass().add("filter-button-active");
        }

        // Configuration du FlowPane des cartes
        if (cardsContainer != null) {
            // Configurer l'espacement pour un bon flux
            cardsContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);
            cardsContainer.setHgap(20);
            cardsContainer.setVgap(20);
            cardsContainer.setPadding(new Insets(15));
        }

        // Mettre à jour le compteur à l'initialisation
        updateRendezVousCount();
    }

    /**
     * Méthode pour mettre à jour l'apparence des filtres
     */
    private void updateFilterButtonsAppearance(Button activeButton) {
        // S'assurer que les boutons existent
        if (btnToday == null || btnWeek == null || btnAll == null) {
            return;
        }

        // Retirer la classe active de tous les boutons
        btnToday.getStyleClass().remove("filter-button-active");
        btnWeek.getStyleClass().remove("filter-button-active");
        btnAll.getStyleClass().remove("filter-button-active");

        btnToday.getStyleClass().add("filter-button");
        btnWeek.getStyleClass().add("filter-button");
        btnAll.getStyleClass().add("filter-button");

        // Ajouter la classe active au bouton sélectionné
        activeButton.getStyleClass().remove("filter-button");
        activeButton.getStyleClass().add("filter-button-active");
    }

    /**
     * Configuration des événements de filtrage
     */
    private void setupFilterEvents() {
        // S'assurer que les boutons existent
        if (btnToday == null || btnWeek == null || btnAll == null) {
            return;
        }

        // Configuration des boutons de filtre
        btnToday.setOnAction(e -> {
            try {
                // Filtrage par jour
                List<RendezVous> allRendezVous = rendezVousService.afficher();
                List<RendezVous> todayList = allRendezVous.stream()
                        .filter(rdv -> rdv.getJour().equals(LocalDate.now()))
                        .collect(Collectors.toList());

                // Mettre à jour l'affichage
                cardsContainer.getChildren().clear();
                for (RendezVous rdv : todayList) {
                    VBox cardNode = createRendezVousCard(rdv);
                    cardsContainer.getChildren().add(cardNode);
                }

                // Mettre à jour le compteur et l'apparence des boutons
                updateRendezVousCount();
                updateFilterButtonsAppearance(btnToday);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        btnWeek.setOnAction(e -> {
            try {
                // Filtrage par semaine
                LocalDate today = LocalDate.now();
                LocalDate endOfWeek = today.plusDays(6);

                List<RendezVous> allRendezVous = rendezVousService.afficher();
                List<RendezVous> weekList = allRendezVous.stream()
                        .filter(rdv -> !rdv.getJour().isBefore(today) && !rdv.getJour().isAfter(endOfWeek))
                        .collect(Collectors.toList());

                // Mettre à jour l'affichage
                cardsContainer.getChildren().clear();
                for (RendezVous rdv : weekList) {
                    VBox cardNode = createRendezVousCard(rdv);
                    cardsContainer.getChildren().add(cardNode);
                }

                // Mettre à jour le compteur et l'apparence des boutons
                updateRendezVousCount();
                updateFilterButtonsAppearance(btnWeek);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        btnAll.setOnAction(e -> {
            try {
                // Recharger tous les rendez-vous
                rafraichirCards();

                // Mettre à jour l'apparence des boutons
                updateFilterButtonsAppearance(btnAll);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * Pour ajouter la recherche en temps réel dans le champ de recherche
     */
    private void setupSearchField() {
        if (searchField == null) {
            return;
        }

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue == null || newValue.isEmpty()) {
                    rafraichirCards();
                } else {
                    // Filtrage client pour la recherche
                    filtrerCardParRecherche(newValue);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Méthode de filtrage client pour le champ de recherche
     */
    private void filtrerCardParRecherche(String searchText) throws SQLException {
        // Recharger tous les rendez-vous d'abord
        List<RendezVous> allRendezVous = rendezVousService.afficher();

        // Filtrer la liste en fonction du texte de recherche
        List<RendezVous> filteredList = allRendezVous.stream()
                .filter(rdv -> {
                    String searchLower = searchText.toLowerCase();

                    // Vérifier si le motif contient le texte recherché
                    if (rdv.getMotif() != null && rdv.getMotif().toLowerCase().contains(searchLower)) {
                        return true;
                    }

                    // Vérifier si les symptômes contiennent le texte recherché
                    if (rdv.getSymptomes() != null && rdv.getSymptomes().toLowerCase().contains(searchLower)) {
                        return true;
                    }

                    // Vérifier si le traitement contient le texte recherché
                    if (rdv.getTraitementEnCours() != null && rdv.getTraitementEnCours().toLowerCase().contains(searchLower)) {
                        return true;
                    }

                    // Vérifier si les notes contiennent le texte recherché
                    if (rdv.getNotes() != null && rdv.getNotes().toLowerCase().contains(searchLower)) {
                        return true;
                    }

                    // Vérifier si le nom du médecin contient le texte recherché
                    if (rdv.getMedecin() != null && rdv.getMedecin().getNom() != null &&
                            rdv.getMedecin().getNom().toLowerCase().contains(searchLower)) {
                        return true;
                    }

                    // Si aucun champ ne correspond, exclure cet élément
                    return false;
                })
                .collect(Collectors.toList());

        // Mettre à jour les cartes avec les résultats filtrés
        cardsContainer.getChildren().clear();
        for (RendezVous rdv : filteredList) {
            VBox cardNode = createRendezVousCard(rdv);
            cardsContainer.getChildren().add(cardNode);
        }

        // Mettre à jour le compteur
        updateRendezVousCount();
    }

    /**
     * Demande confirmation avant de supprimer un rendez-vous
     */
    private void confirmerSuppression(RendezVous rdv) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer ce rendez-vous ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce rendez-vous ? Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                rendezVousService.supprimerD(rdv);
                rafraichirCards();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le rendez-vous: " + e.getMessage());
            }
        }
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void tenterAnnulationRendezVous(RendezVous rdv) {
        // Vérifier si le rendez-vous peut être annulé
        if (!annulationService.peutEtreAnnule(rdv)) {
            showAlert(Alert.AlertType.WARNING, "Annulation impossible",
                    "Ce rendez-vous ne peut pas être annulé car il est prévu dans moins de 24 heures.");
            return;
        }

        // Demander confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation d'annulation");
        alert.setHeaderText("Annuler ce rendez-vous ?");
        alert.setContentText("Êtes-vous sûr de vouloir annuler ce rendez-vous prévu le " +
                rdv.getJour().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                " à " + rdv.getHeureString() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Patient ID à récupérer de la session ou du contrôleur parent
            int patientId = rdv.getPatientId();

            // Procéder à l'annulation
            AnnulationRendezVousService.ResultatAnnulation resultat = annulationService.annulerRendezVous(rdv.getId(), patientId);

            // Traiter le résultat
            switch (resultat) {
                case SUCCES:
                    showAlert(Alert.AlertType.INFORMATION, "Annulation réussie",
                            "Votre rendez-vous a été annulé avec succès.");
                    try {
                        rafraichirCards(); // Rafraîchir les cartes pour refléter le changement
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                case DELAI_DEPASSE:
                    showAlert(Alert.AlertType.ERROR, "Délai dépassé",
                            "Impossible d'annuler : ce rendez-vous est prévu dans moins de 24 heures.");
                    break;
                case RENDEZ_VOUS_PASSE:
                    showAlert(Alert.AlertType.ERROR, "Rendez-vous passé",
                            "Impossible d'annuler : ce rendez-vous est déjà passé.");
                    break;
            }
        }
    }
}