package controllers.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.User.IAService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnalyseSymptomes {

    @FXML
    private ComboBox<String> comboSemaine;

    @FXML
    private TextArea txtDescription;

    @FXML
    private Button btnAnalyser;

    @FXML
    private Button btnRetour;

    @FXML
    private Button btnConsulter;

    @FXML
    private VBox zoneResultats;

    @FXML
    private Label lblEtatGeneral;

    @FXML
    private Label lblConseils;

    @FXML
    private Label lblResultatTitre;

    @FXML
    private ProgressIndicator progressAnalyse;
    private IAService iaService = new IAService();

    // Mots-clés qui suggèrent une consultation
    private final List<String> motsClesConsultation = Arrays.asList(
            "saignement", "sang", "douleur intense", "très mal", "mal de tête intense",
            "fièvre", "contractions régulières", "perte d'eau", "vision floue",
            "vertiges", "étourdissements", "mal de tête sévère", "points lumineux",
            "gonflement soudain", "gonflement visage", "gonflement mains",
            "essoufflement", "difficultés à respirer", "douleur abdominale",
            "douleur sous les côtes", "sensation de malaise", "inquiète"
    );

    @FXML
    public void initialize() {
        // Remplir la liste des semaines de grossesse
        List<String> semaines = new ArrayList<>();
        for (int i = 1; i <= 42; i++) {
            semaines.add("Semaine " + i);
        }
        ObservableList<String> semainesList = FXCollections.observableArrayList(semaines);
        comboSemaine.setItems(semainesList);

        // Sélectionner la première semaine par défaut
        comboSemaine.getSelectionModel().selectFirst();

        // Masquer la zone de résultats au démarrage
        zoneResultats.setVisible(false);
        btnConsulter.setVisible(false);
        progressAnalyse.setVisible(false);

        // Définir des styles par défaut améliorés
        lblConseils.setStyle("-fx-font-size: 16px; -fx-line-spacing: 1.5em;");
        lblResultatTitre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c5282;");
        lblEtatGeneral.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // S'assurer que la zone de résultats peut s'agrandir pour afficher tout le contenu
        zoneResultats.setMinHeight(400);
        zoneResultats.setPrefHeight(500);
        zoneResultats.setMaxHeight(Double.MAX_VALUE); // Défini programmatiquement plutôt que dans le FXML
    }

    /**
     * Transforme un texte avec des points en lignes formattées avec des puces et des emojis
     */
    private String formaterConseilsAvecPuces(String conseils) {
        if (conseils == null || conseils.isEmpty()) {
            return "";
        }

        // Remplacer les puces classiques par des emojis et améliorer le formatage
        String[] lignes = conseils.split("\n");
        StringBuilder result = new StringBuilder();

        for (String ligne : lignes) {
            ligne = ligne.trim();
            if (ligne.isEmpty()) {
                result.append("\n");
                continue;
            }

            // Remplacer les puces existantes ou ajouter des emojis appropriés
            if (ligne.startsWith("•") || ligne.startsWith("-")) {
                ligne = ligne.substring(1).trim();
                result.append("💡 ").append(ligne).append("\n\n");
            } else if (ligne.startsWith("Pour")) {
                result.append("✅ ").append(ligne).append("\n\n");
            } else if (ligne.contains("attention") || ligne.contains("évitez") ||
                    ligne.contains("consultez") || ligne.contains("important")) {
                result.append("⚠️ ").append(ligne).append("\n\n");
            } else {
                result.append("• ").append(ligne).append("\n\n");
            }
        }

        return result.toString();
    }

    /**
     * Analyse les symptômes saisis par l'utilisateur et affiche les résultats
     * avec une mise en forme améliorée et colorée
     */
    @FXML
    private void analyserSymptomes(ActionEvent event) {
        // Vérifier que la description n'est pas vide
        if (txtDescription.getText().trim().isEmpty()) {
            afficherAlerte(Alert.AlertType.WARNING, "Description manquante",
                    "Veuillez décrire vos symptômes pour permettre l'analyse.");
            return;
        }

        // Récupérer la semaine sélectionnée
        String semaineText = comboSemaine.getSelectionModel().getSelectedItem();
        int semaine = 1;

        if (semaineText != null && !semaineText.isEmpty()) {
            try {
                semaine = Integer.parseInt(semaineText.replace("Semaine ", ""));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // Afficher l'indicateur de progression
        progressAnalyse.setVisible(true);
        btnAnalyser.setDisable(true);

        // Utiliser le service IA pour analyser les symptômes
        int finalSemaine = semaine;
        iaService.analyserSymptomes(txtDescription.getText(), finalSemaine)
                .thenAcceptAsync(resultat -> {
                    // Mettre à jour l'interface sur le thread JavaFX
                    javafx.application.Platform.runLater(() -> {
                        // Cacher l'indicateur de progression
                        progressAnalyse.setVisible(false);
                        btnAnalyser.setDisable(false);

                        // Afficher les résultats
                        zoneResultats.setVisible(true);

                        if (resultat.consultationNecessaire) {
                            // Mise en forme pour les cas qui nécessitent une consultation
                            lblEtatGeneral.setText("Vos symptômes nécessitent une consultation médicale.");
                            lblEtatGeneral.setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold; -fx-font-size: 18px;");
                            lblResultatTitre.setText("⚠️ Consultation recommandée");
                            lblResultatTitre.setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold; -fx-font-size: 22px;");

                            // Formater la raison en gras
                            String raisonFormatee = "🔍 RAISON DE LA CONSULTATION:\n" + resultat.raison;

                            // Formater les conseils avec des puces et coloration
                            String conseilsFormates = "\n\n💊 CONSEILS MÉDICAUX:\n" + formaterConseilsAvecPuces(resultat.conseils);

                            // Afficher avec mise en forme améliorée
                            lblConseils.setStyle("-fx-line-spacing: 1.5em; -fx-wrap-text: true; -fx-font-size: 16px; -fx-text-fill: #e53e3e; -fx-background-color: #fff5f5; -fx-background-radius: 8; -fx-padding: 10;");
                            lblConseils.setText(raisonFormatee + conseilsFormates);

                            // Ajuster dynamiquement la hauteur de la zone de résultats
                            // selon la longueur du contenu des conseils
                            zoneResultats.setPrefHeight(Math.max(500, 350 + (resultat.raison.length() + resultat.conseils.length()) / 2));
                            btnConsulter.setVisible(true);
                        } else {
                            // Mise en forme pour les cas normaux
                            lblEtatGeneral.setText("Vos symptômes semblent normaux pour votre stade de grossesse.");
                            lblEtatGeneral.setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold; -fx-font-size: 18px;");
                            lblResultatTitre.setText("✓ Symptômes normaux");
                            lblResultatTitre.setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold; -fx-font-size: 22px;");

                            // Formater la raison et les conseils
                            String raisonFormatee = "🔍 ÉVALUATION:\n" + resultat.raison;
                            String conseilsFormates = "\n\n💡 CONSEILS POUR VOTRE BIEN-ÊTRE:\n" + formaterConseilsAvecPuces(resultat.conseils);

                            // Afficher avec mise en forme améliorée
                            lblConseils.setStyle("-fx-line-spacing: 1.5em; -fx-wrap-text: true; -fx-font-size: 16px; -fx-text-fill: #38a169; -fx-background-color: #f0fff4; -fx-background-radius: 8; -fx-padding: 10;");
                            lblConseils.setText(raisonFormatee + conseilsFormates);

                            // Ajuster dynamiquement la hauteur de la zone de résultats
                            // selon la longueur du contenu des conseils
                            zoneResultats.setPrefHeight(Math.max(500, 350 + (resultat.raison.length() + resultat.conseils.length()) / 2));
                            btnConsulter.setVisible(false);
                        }
                    });
                })
                .exceptionally(ex -> {
                    // En cas d'erreur, afficher un message
                    javafx.application.Platform.runLater(() -> {
                        progressAnalyse.setVisible(false);
                        btnAnalyser.setDisable(false);
                        afficherAlerte(Alert.AlertType.ERROR, "Erreur d'analyse",
                                "Une erreur est survenue lors de l'analyse des symptômes.");
                    });
                    return null;
                });
    }

    @FXML
    private void retourAccueil(ActionEvent event) {
        try {
            // Charger la page d'accueil
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AccueilPatient.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Scene scene = btnRetour.getScene();

            // Remplacer le contenu
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la page d'accueil.");
        }
    }

    @FXML
    private void prendreRendezVous(ActionEvent event) {
        try {
            // Charger la page de prise de rendez-vous
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterRendezVousFront.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Scene scene = btnConsulter.getScene();

            // Remplacer le contenu
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la page de prise de rendez-vous.");
        }
    }

    /**
     * Affiche une alerte
     */
    private void afficherAlerte(Alert.AlertType type, String titre, String contenu) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
}