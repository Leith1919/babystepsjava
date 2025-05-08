package controllers.User;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import models.suiviGrossesse;
import models.User;
import services.User.SuiviGrossesseService;
import services.User.SuiviBebeService;
import services.User.UserService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class AfficherSuiviGrossesseController implements Initializable {

    @FXML
    private TableView<suiviGrossesse> tableView_SuiviGrossesse;
    @FXML
    private TableColumn<suiviGrossesse, String> colDate;
    @FXML
    private TableColumn<suiviGrossesse, Double> colPoids;
    @FXML
    private TableColumn<suiviGrossesse, String> colTension;
    @FXML
    private TableColumn<suiviGrossesse, String> colSymptomes;
    @FXML
    private TableColumn<suiviGrossesse, String> colEtat;
    @FXML
    private TableColumn<suiviGrossesse, Void> colDetection;
    @FXML
    private TableColumn<suiviGrossesse, Void> colModifier;
    @FXML
    private TableColumn<suiviGrossesse, Void> colSupprimer;
    @FXML
    private TableColumn<suiviGrossesse, Void> colVoirSuiviBebe;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> searchCriteria;
    @FXML
    private TableColumn<suiviGrossesse, String> colPatiente;

    private final SuiviGrossesseService service = new SuiviGrossesseService();
    private final UserService userService = new UserService();
    private ObservableList<suiviGrossesse> observableList = FXCollections.observableArrayList();
    private FilteredList<suiviGrossesse> filteredData;

    // Cache pour stocker les infos des utilisateurs et éviter des appels répétés à la base de données
    private Map<Integer, User> userCache = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateSuivi"));
        colPoids.setCellValueFactory(new PropertyValueFactory<>("poids"));
        colTension.setCellValueFactory(new PropertyValueFactory<>("tension"));
        colSymptomes.setCellValueFactory(new PropertyValueFactory<>("symptomes"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etatGrossesse"));
        colPatiente.setCellValueFactory(cellData -> {
            Integer patientId = cellData.getValue().getPatientId();
            try {
                User patient = getUserFromCache(patientId);
                return new SimpleStringProperty(patient != null ?
                        patient.getNom() + " " + patient.getPrenom() : "");
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleStringProperty("Erreur");
            }
        });

        // Initialiser les critères de recherche avec le nouveau critère "Patiente"
        searchCriteria.setItems(FXCollections.observableArrayList(
                "Tous les champs", "Patiente", "Date", "Poids", "Tension", "Symptômes", "État", "Risque"
        ));
        searchCriteria.getSelectionModel().selectFirst();

        // Configurer la recherche dynamique
        setupSearch();

        // Configuration des colonnes d'action
        configurerColonneAnalyseRisque();
        ajouterColonneVoirSuiviBebe();
        ajouterColonneModifier();
        ajouterColonneSupprimer();

        loadSuivis();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), tableView_SuiviGrossesse);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setCycleCount(1);
        fadeIn.play();
    }

    // Méthode pour récupérer un utilisateur depuis le cache ou la base de données
    private User getUserFromCache(Integer userId) throws SQLException {
        if (userId == null) return null;

        // Vérifier si l'utilisateur est déjà dans le cache
        if (userCache.containsKey(userId)) {
            return userCache.get(userId);
        }

        // Si non, le récupérer de la base de données et l'ajouter au cache
        User patient = userService.getUserById(userId);
        if (patient != null) {
            userCache.put(userId, patient);
        }
        return patient;
    }

    private void setupSearch() {
        // Initialiser le filteredData avec une liste vide
        filteredData = new FilteredList<>(observableList, p -> true);

        // Ajouter un listener pour détecter les changements dans le champ de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(createPredicate(newValue));
        });

        // Ajouter un listener pour détecter les changements dans le critère de recherche
        searchCriteria.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                filteredData.setPredicate(createPredicate(searchField.getText()));
            }
        });

        // Lier le filteredData à la table via une SortedList pour maintenir le tri
        SortedList<suiviGrossesse> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView_SuiviGrossesse.comparatorProperty());
        tableView_SuiviGrossesse.setItems(sortedData);
    }

    private Predicate<suiviGrossesse> createPredicate(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return suivi -> true;
        }

        String lowerCaseSearch = searchText.toLowerCase();
        String selectedCriteria = searchCriteria.getSelectionModel().getSelectedItem();

        return suivi -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String dateStr = suivi.getDateSuivi() != null ? dateFormat.format(suivi.getDateSuivi()) : "";
            String poidsStr = String.valueOf(suivi.getPoids());
            String tensionStr = String.valueOf(suivi.getTension());
            String symptomes = suivi.getSymptomes() != null ? suivi.getSymptomes().toLowerCase() : "";
            String etat = suivi.getEtatGrossesse() != null ? suivi.getEtatGrossesse().toLowerCase() : "";

            // Récupérer les informations du patient pour la recherche
            String nomPrenom = "";
            try {
                User patient = getUserFromCache(suivi.getPatientId());
                if (patient != null) {
                    nomPrenom = (patient.getNom() + " " + patient.getPrenom()).toLowerCase();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Analyser le risque pour la recherche
            String risque = analyserRisque(suivi);

            switch (selectedCriteria) {
                case "Patiente":
                    return nomPrenom.contains(lowerCaseSearch);
                case "Date":
                    return dateStr.contains(lowerCaseSearch);
                case "Poids":
                    return poidsStr.contains(lowerCaseSearch);
                case "Tension":
                    return tensionStr.contains(lowerCaseSearch);
                case "Symptômes":
                    return symptomes.contains(lowerCaseSearch);
                case "État":
                    return etat.contains(lowerCaseSearch);
                case "Risque":
                    return risque.toLowerCase().contains(lowerCaseSearch);
                default: // "Tous les champs"
                    return nomPrenom.contains(lowerCaseSearch) ||
                            dateStr.contains(lowerCaseSearch) ||
                            poidsStr.contains(lowerCaseSearch) ||
                            tensionStr.contains(lowerCaseSearch) ||
                            symptomes.contains(lowerCaseSearch) ||
                            etat.contains(lowerCaseSearch) ||
                            risque.toLowerCase().contains(lowerCaseSearch);
            }
        };
    }

    // Méthode simple pour simuler une analyse de risque basée sur les données existantes
    private String analyserRisque(suiviGrossesse suivi) {
        // Logique simplifiée de détection de risque
        if (suivi.getTension() > 14.0) {
            return "Hypertension";
        } else if (suivi.getTension() < 9.0) {
            return "Hypotension";
        }

        if (suivi.getSymptomes() != null) {
            String symptomes = suivi.getSymptomes().toLowerCase();
            if (symptomes.contains("douleur") || symptomes.contains("saignement")) {
                return "Attention requise";
            }
            if (symptomes.contains("nausée") || symptomes.contains("fatigue")) {
                return "Normal";
            }
        }

        if (suivi.getEtatGrossesse() != null &&
                (suivi.getEtatGrossesse().toLowerCase().contains("risque") ||
                        suivi.getEtatGrossesse().toLowerCase().contains("complication"))) {
            return "Suivi médical urgent";
        }

        return "Normal";
    }

    public void loadSuivis() {
        try {
            List<suiviGrossesse> suivis = service.recuperer();
            observableList.clear();
            observableList.addAll(suivis);

            // Précharger les informations utilisateur pour tous les suivis
            for (suiviGrossesse suivi : suivis) {
                if (suivi.getPatientId() != null) {
                    getUserFromCache(suivi.getPatientId());
                }
            }

            // Si la recherche est déjà configurée, cela va automatiquement mettre à jour la vue
            if (filteredData == null) {
                filteredData = new FilteredList<>(observableList, p -> true);
                SortedList<suiviGrossesse> sortedData = new SortedList<>(filteredData);
                sortedData.comparatorProperty().bind(tableView_SuiviGrossesse.comparatorProperty());
                tableView_SuiviGrossesse.setItems(sortedData);
            }
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
                            System.out.println("Chargement terminé");

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
                        try {
                            // Charger la boîte de dialogue de confirmation personnalisée
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConfirmationSuppression.fxml"));
                            Parent root = loader.load();

                            // Obtenir le contrôleur et lui passer les données
                            ConfirmationSuppressionController controller = loader.getController();
                            controller.setSuiviGrossesse(sg);
                            controller.setParentController(AfficherSuiviGrossesseController.this);

                            // Configurer et afficher la boîte de dialogue
                            Stage stage = new Stage();
                            stage.setTitle("Confirmation de suppression");
                            stage.setScene(new Scene(root));
                            stage.initModality(Modality.APPLICATION_MODAL);
                            stage.showAndWait();

                        } catch (IOException e) {
                            e.printStackTrace();
                            showErrorAlert("Erreur d'affichage", "Impossible d'ouvrir la fenêtre de confirmation", e.getMessage());
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

    // Méthode à ajouter pour permettre la suppression depuis la boîte de dialogue
    public void supprimerSuivi(suiviGrossesse sg) {
        try {
            service.supprimer(sg);
            loadSuivis(); // Recharge après suppression
            showInfoAlert("Succès", "Suppression réussie", "Le suivi de grossesse a été supprimé avec succès.");
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erreur de suppression", "Impossible de supprimer le suivi de grossesse", e.getMessage());
        }
    }

    // Configuration de la colonne d'analyse de risque avec un bouton
    private void configurerColonneAnalyseRisque() {
        Callback<TableColumn<suiviGrossesse, Void>, TableCell<suiviGrossesse, Void>> cellFactory = param -> new TableCell<>() {
            private final Button analyseBtn = new Button();

            {
                analyseBtn.setOnAction(event -> {
                    suiviGrossesse sg = getTableView().getItems().get(getIndex());
                    if (sg != null) {
                        try {
                            // Charger la fenêtre DetectionAnomalies.fxml
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetectionAnomalies.fxml"));
                            Parent root = loader.load();

                            // Récupérer le contrôleur et initialiser les données
                            DetectionAnomaliesController controller = loader.getController();

                            // Récupérer le nom de la patiente
                            String nomPatiente = "Patiente inconnue";
                            try {
                                User patient = getUserFromCache(sg.getPatientId());
                                if (patient != null) {
                                    nomPatiente = patient.getNom() + " " + patient.getPrenom();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            // Récupérer tous les suivis pour cette patiente
                            List<suiviGrossesse> allSuivis = getSuivisPourGrossesse(sg);

                            // Initialiser les données dans le contrôleur
                            controller.initData(allSuivis, nomPatiente);

                            // Créer et configurer la nouvelle fenêtre
                            Stage stage = new Stage();
                            stage.setTitle("Analyse détaillée - " + nomPatiente);
                            stage.setScene(new Scene(root));
                            stage.initModality(Modality.APPLICATION_MODAL);
                            // Définir une taille préférée raisonnable
                            stage.setWidth(1000);
                            stage.setHeight(700);
                            // Définir une taille minimale
                            stage.setMinWidth(800);
                            stage.setMinHeight(600);
                            // Afficher la fenêtre
                            stage.show();

                        } catch (IOException e) {
                            e.printStackTrace();
                            showErrorAlert("Erreur", "Impossible d'ouvrir la fenêtre d'analyse", e.getMessage());
                        }
                    } else {
                        showErrorAlert("Erreur", "Sélection invalide", "Aucun suivi de grossesse sélectionné");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    suiviGrossesse suivi = getTableView().getItems().get(getIndex());
                    String risque = analyserRisque(suivi);

                    // Configurer le bouton en fonction du niveau de risque
                    analyseBtn.setText("Analyser");

                    // Appliquer un style différent selon le niveau de risque
                    if (risque.equals("Normal")) {
                        analyseBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                    } else if (risque.equals("Attention requise")) {
                        analyseBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white;");
                    } else if (risque.contains("urgent") || risque.equals("Hypertension") || risque.equals("Hypotension")) {
                        analyseBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                    } else {
                        analyseBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
                    }

                    setGraphic(analyseBtn);
                }
            }
        };

        colDetection.setCellFactory(cellFactory);
    }

    // Méthode pour ouvrir l'interface de détection IA
    private void ouvrirInterfaceDetectionIA(suiviGrossesse suiviSelectionne) {
        try {
            // Récupérer les suivis pour cette grossesse
            List<suiviGrossesse> allSuivis = getSuivisPourGrossesse(suiviSelectionne);

            if (allSuivis.isEmpty()) {
                showErrorAlert("Erreur", "Données insuffisantes", "Aucune donnée disponible pour l'analyse IA");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetectionAnomalies.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur et initialiser les données
            DetectionAnomaliesController controller = loader.getController();

            // Récupérer le nom de la patiente
            String nomPatiente = "Patiente inconnue";
            try {
                User patient = getUserFromCache(suiviSelectionne.getPatientId());
                if (patient != null) {
                    nomPatiente = patient.getNom() + " " + patient.getPrenom();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Initialiser les données dans le contrôleur de détection
            controller.initData(allSuivis, nomPatiente);

            // Ouvrir dans une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("Analyse de Risque - IA");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir l'interface de détection", e.getMessage());
        }
    }

    // Méthode pour obtenir les suivis liés à une même grossesse
    private List<suiviGrossesse> getSuivisPourGrossesse(suiviGrossesse currentSuivi) {
        try {
            // Récupérer tous les suivis
            List<suiviGrossesse> allSuivis = service.recuperer();

            // Filtrer pour ne garder que les suivis associés au même patient ID
            List<suiviGrossesse> filteredSuivis = new ArrayList<>();
            for (suiviGrossesse suivi : allSuivis) {
                if (currentSuivi.getPatientId() != null &&
                        suivi.getPatientId() != null &&
                        currentSuivi.getPatientId().equals(suivi.getPatientId())) {
                    filteredSuivis.add(suivi);
                }
            }

            // Trier par date croissante pour garantir des courbes/statistiques précises
            filteredSuivis.sort(Comparator.comparing(s -> controllers.User.PregnancyMLAnalyzer.toLocalDate(s.getDateSuivi())));

            return filteredSuivis;
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de récupérer les suivis", e.getMessage());
            return new ArrayList<>();
        }
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