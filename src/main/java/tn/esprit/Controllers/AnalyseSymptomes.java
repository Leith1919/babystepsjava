package tn.esprit.Controllers;

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
import tn.esprit.services.IAService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    }

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

        // Analyser les symptômes (simuler un délai)
        int finalSemaine = semaine;
        new Thread(() -> {
            try {
                // Simuler un traitement
                Thread.sleep(1500);

                // Analyser le texte
                String description = txtDescription.getText().toLowerCase();
                boolean consultationNecessaire = false;

                // Vérifier si des mots-clés nécessitant une consultation sont présents
                for (String motCle : motsClesConsultation) {
                    if (description.contains(motCle.toLowerCase())) {
                        consultationNecessaire = true;
                        break;
                    }
                }

                // Générer des conseils selon la semaine de grossesse
                String conseils = genererConseils(finalSemaine, description, consultationNecessaire);

                // Mettre à jour l'interface sur le thread JavaFX
                boolean finalConsultationNecessaire = consultationNecessaire;
                javafx.application.Platform.runLater(() -> {
                    // Cacher l'indicateur de progression
                    progressAnalyse.setVisible(false);
                    btnAnalyser.setDisable(false);

                    // Afficher les résultats
                    zoneResultats.setVisible(true);

                    if (finalConsultationNecessaire) {
                        lblEtatGeneral.setText("Vos symptômes nécessitent une consultation médicale.");
                        lblEtatGeneral.setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold;");
                        lblResultatTitre.setText("Consultation recommandée");
                        btnConsulter.setVisible(true);
                    } else {
                        lblEtatGeneral.setText("Vos symptômes semblent normaux pour votre stade de grossesse.");
                        lblEtatGeneral.setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold;");
                        lblResultatTitre.setText("Symptômes normaux");
                        btnConsulter.setVisible(false);
                    }

                    // Afficher les conseils
                    lblConseils.setText(conseils);
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String genererConseils(int semaine, String description, boolean consultationNecessaire) {
        StringBuilder conseilsBuilder = new StringBuilder();

        if (consultationNecessaire) {
            // Pour les symptômes préoccupants du troisième trimestre (risque de pré-éclampsie)
            if (semaine >= 20 && (
                    description.contains("mal de tête") ||
                            description.contains("vision floue") ||
                            description.contains("gonflement") ||
                            description.contains("douleur abdominale"))) {
                conseilsBuilder.append("• Les symptômes que vous décrivez peuvent être liés à la pré-éclampsie, une complication grave de la grossesse qui nécessite une prise en charge médicale immédiate.\n\n");
            } else {
                conseilsBuilder.append("• Nous avons détecté des symptômes qui nécessitent l'avis d'un professionnel de santé.\n\n");
            }

            conseilsBuilder.append("• En attendant votre rendez-vous, restez calme et reposez-vous.\n\n");

            // Si saignements
            if (description.contains("saignement") || description.contains("sang")) {
                conseilsBuilder.append("• En cas de saignements, notez leur quantité et leur fréquence pour informer votre médecin.\n\n");
            }

            // Si douleurs
            if (description.contains("douleur") || description.contains("mal")) {
                conseilsBuilder.append("• Pour les douleurs, évitez l'automédication et prenez uniquement les médicaments prescrits par votre médecin.\n\n");
            }

            // Si problèmes de vision
            if (description.contains("vision") || description.contains("points lumineux")) {
                conseilsBuilder.append("• Les troubles visuels pendant la grossesse doivent être signalés rapidement à votre médecin.\n\n");
            }
        } else {
            // Premier trimestre (semaines 1-13)
            if (semaine <= 13) {
                if (description.contains("nausée") || description.contains("vomissement")) {
                    conseilsBuilder.append("• Pour les nausées : mangez de petits repas fréquents, évitez les odeurs fortes, et essayez de manger un biscuit sec avant de vous lever le matin.\n\n");
                }

                if (description.contains("fatigue")) {
                    conseilsBuilder.append("• La fatigue est normale en début de grossesse. Accordez-vous plus de repos et de petites siestes si possible.\n\n");
                }
            }

            // Deuxième trimestre (semaines 14-26)
            else if (semaine <= 26) {
                if (description.contains("dos") || description.contains("lombaire")) {
                    conseilsBuilder.append("• Pour soulager les douleurs lombaires : maintenir une bonne posture, porter des chaussures confortables et utiliser un coussin de grossesse pour dormir.\n\n");
                }

                if (description.contains("constipation")) {
                    conseilsBuilder.append("• Pour la constipation : buvez beaucoup d'eau, augmentez votre consommation de fibres et pratiquez une activité physique légère.\n\n");
                }

                if (description.contains("brûlure") || description.contains("estomac")) {
                    conseilsBuilder.append("• Pour les brûlures d'estomac : mangez de plus petits repas, évitez les aliments épicés ou acides, et restez en position verticale après les repas.\n\n");
                }
            }

            // Troisième trimestre (semaines 27-42)
            else {
                if (description.contains("sommeil") || description.contains("dormir")) {
                    conseilsBuilder.append("• Pour améliorer votre sommeil : utilisez des coussins de positionnement, limitez les liquides avant le coucher et pratiquez des techniques de relaxation.\n\n");
                }

                if (description.contains("jambe") || description.contains("gonflement") || description.contains("cheville")) {
                    conseilsBuilder.append("• Pour les jambes lourdes ou gonflées : surélevez vos jambes plusieurs fois par jour, évitez de rester debout trop longtemps et portez des bas de contention si recommandé.\n\n");
                }

                if (description.contains("contractions")) {
                    conseilsBuilder.append("• Les contractions occasionnelles sont normales à ce stade. Reposez-vous et buvez de l'eau. Si elles deviennent régulières ou douloureuses, consultez votre médecin.\n\n");
                }
            }

            // Conseils généraux
            conseilsBuilder.append("• Continuez à prendre soin de vous avec une alimentation équilibrée, de l'exercice adapté et suffisamment de repos.\n\n");
            conseilsBuilder.append("• N'hésitez pas à consulter votre médecin si ces symptômes s'aggravent ou si de nouveaux symptômes apparaissent.");
        }

        return conseilsBuilder.toString();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterRendezVous.fxml"));
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