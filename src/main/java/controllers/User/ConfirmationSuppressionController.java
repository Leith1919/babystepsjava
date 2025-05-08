package controllers.User;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.suiviGrossesse;

import java.text.SimpleDateFormat;

public class ConfirmationSuppressionController {

    @FXML private Label detailsLabel;
    @FXML private Button annulerButton;
    @FXML private Button confirmerButton;

    private suiviGrossesse suivi;
    private AfficherSuiviGrossesseController parentController;

    public void initialize() {
        // Initialisation du contrôleur
    }

    public void setSuiviGrossesse(suiviGrossesse sg) {
        this.suivi = sg;

        // Formater la date pour l'affichage
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dateStr = dateFormat.format(sg.getDateSuivi());

        // Afficher les détails du suivi à supprimer
        detailsLabel.setText("Date du suivi: " + dateStr +
                " - Poids: " + sg.getPoids() + " kg" +
                " - État: " + sg.getEtatGrossesse());
    }

    public void setParentController(AfficherSuiviGrossesseController controller) {
        this.parentController = controller;
    }

    @FXML
    void handleAnnuler() {
        // Fermer la fenêtre sans supprimer
        Stage stage = (Stage) annulerButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleConfirmer() {
        if (suivi != null && parentController != null) {
            // Appeler la méthode de suppression dans le contrôleur parent
            parentController.supprimerSuivi(suivi);

            // Fermer la fenêtre
            Stage stage = (Stage) confirmerButton.getScene().getWindow();
            stage.close();
        }
    }
}