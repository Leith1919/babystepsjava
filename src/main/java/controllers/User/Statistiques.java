package controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.stage.Stage;
import services.User.RapportService;

public class Statistiques implements Initializable {

    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField cheminDossierField;
    @FXML private Button genererButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label messageLabel;

    @FXML private CheckBox cbActivite;
    @FXML private CheckBox cbMedecins;
    @FXML private CheckBox cbOccupation;
    @FXML private CheckBox cbMotifs;
    @FXML private CheckBox cbAssiduite;
    @FXML private CheckBox cbDelai;

    private final RapportService rapportService = new RapportService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser avec les dates par défaut (mois en cours)
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        dateDebutPicker.setValue(firstDayOfMonth);
        dateFinPicker.setValue(now);

        // Initialiser le chemin par défaut
        cheminDossierField.setText(System.getProperty("user.home") + File.separator + "Documents");
        cheminDossierField.setEditable(false);

        // Valider les dates
        dateDebutPicker.valueProperty().addListener((obs, oldVal, newVal) -> validerDates());
        dateFinPicker.valueProperty().addListener((obs, oldVal, newVal) -> validerDates());
    }

    private void validerDates() {
        LocalDate debut = dateDebutPicker.getValue();
        LocalDate fin = dateFinPicker.getValue();

        if (debut != null && fin != null) {
            if (fin.isBefore(debut)) {
                messageLabel.setText("La date de fin doit être postérieure à la date de début");
                genererButton.setDisable(true);
            } else {
                messageLabel.setText("");
                genererButton.setDisable(false);
            }
        }
    }

    @FXML
    private void parcourirDossier() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Sélectionner le dossier d'enregistrement");

        // Définir le répertoire initial
        String cheminActuel = cheminDossierField.getText();
        if (cheminActuel != null && !cheminActuel.isEmpty()) {
            File dossierInitial = new File(cheminActuel);
            if (dossierInitial.exists()) {
                directoryChooser.setInitialDirectory(dossierInitial);
            }
        }

        // Afficher le sélecteur de dossier
        File dossierSelectionne = directoryChooser.showDialog(cheminDossierField.getScene().getWindow());

        if (dossierSelectionne != null) {
            cheminDossierField.setText(dossierSelectionne.getAbsolutePath());
        }
    }

    @FXML
    private void genererRapport() {
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();
        String cheminDossier = cheminDossierField.getText();

        if (dateDebut == null || dateFin == null) {
            messageLabel.setText("Veuillez sélectionner les dates de début et de fin");
            return;
        }

        if (cheminDossier == null || cheminDossier.isEmpty()) {
            messageLabel.setText("Veuillez sélectionner un dossier d'enregistrement");
            return;
        }

        // Vérifier qu'au moins une option est sélectionnée
        if (!cbActivite.isSelected() && !cbMedecins.isSelected() && !cbOccupation.isSelected() &&
                !cbMotifs.isSelected() && !cbAssiduite.isSelected() && !cbDelai.isSelected()) {
            messageLabel.setText("Veuillez sélectionner au moins un type de statistique");
            return;
        }

        // Désactiver les contrôles pendant la génération
        desactiverControles(true);
        messageLabel.setText("Génération du rapport en cours...");

        // Nom du fichier basé sur la période
        String nomFichier = "Statistiques_" +
                dateDebut.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "_" +
                dateFin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";

        String cheminComplet = cheminDossier + File.separator + nomFichier;

        // Générer le rapport dans un thread séparé
        new Thread(() -> {
            boolean succes = rapportService.genererRapport(cheminComplet, dateDebut, dateFin);

            // Mettre à jour l'interface
            javafx.application.Platform.runLater(() -> {
                desactiverControles(false);

                if (succes) {
                    messageLabel.setText("Rapport généré avec succès: " + nomFichier);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Génération de rapport");
                    alert.setHeaderText("Rapport généré avec succès");
                    alert.setContentText("Le rapport a été enregistré dans:\n" + cheminComplet);

                    ButtonType ouvrir = new ButtonType("Ouvrir le rapport");
                    ButtonType fermer = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(ouvrir, fermer);

                    alert.showAndWait().ifPresent(type -> {
                        if (type == ouvrir) {
                            try {
                                File file = new File(cheminComplet);
                                if (file.exists()) {
                                    java.awt.Desktop.getDesktop().open(file);
                                }
                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'ouverture du fichier: " + e.getMessage());
                            }
                        }
                    });
                } else {
                    messageLabel.setText("Erreur lors de la génération du rapport");

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText("Échec de la génération du rapport");
                    alert.setContentText("Une erreur s'est produite lors de la génération du rapport.");
                    alert.showAndWait();
                }
            });
        }).start();
    }

    private void desactiverControles(boolean desactiver) {
        dateDebutPicker.setDisable(desactiver);
        dateFinPicker.setDisable(desactiver);
        cheminDossierField.setDisable(desactiver);
        genererButton.setDisable(desactiver);
        cbActivite.setDisable(desactiver);
        cbMedecins.setDisable(desactiver);
        cbOccupation.setDisable(desactiver);
        cbMotifs.setDisable(desactiver);
        cbAssiduite.setDisable(desactiver);
        cbDelai.setDisable(desactiver);
        progressIndicator.setVisible(desactiver);
    }
    @FXML
    private void ouvrirStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GenerationStatistiques.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Statistiques des rendez-vous");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture de la fenêtre de statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }
}