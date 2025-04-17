package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.entities.suiviBebe;
import tn.esprit.entities.suiviGrossesse;
import tn.esprit.services.SuiviBebeService;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import javafx.application.Platform;

public class AjoutSuiviBebeController {

    @FXML
    private DatePicker dateSuiviPicker;
    @FXML
    private TextField poidsBebeField;
    @FXML
    private TextField tailleBebeField;
    @FXML
    private ComboBox<String> etatSanteComboBox;
    @FXML
    private TextField battementCoeurField;
    @FXML
    private ComboBox<String> appetitBebeComboBox;
    @FXML
    private Button enregistrerButton;
    @FXML
    private Label titreFormulaire;

    // Labels d'erreur pour chaque champ
    @FXML
    private Label dateErreurLabel;
    @FXML
    private Label poidsErreurLabel;
    @FXML
    private Label tailleErreurLabel;
    @FXML
    private Label santeErreurLabel;
    @FXML
    private Label battementErreurLabel;
    @FXML
    private Label appetitErreurLabel;

    private suiviGrossesse suiviGrossesse;
    private AfficherSuiviBebeController parentController;
    private suiviBebe suiviBebeAModifier;
    private boolean modeModification = false;

    private final SuiviBebeService suiviBebeService = new SuiviBebeService();

    // Définir les limites de validation
    private final double POIDS_MIN = 0.5;
    private final double POIDS_MAX = 20.0;
    private final double TAILLE_MIN = 20.0;
    private final double TAILLE_MAX = 120.0;
    private final double BATTEMENT_MIN = 70.0;
    private final double BATTEMENT_MAX = 190.0;

    @FXML
    private void initialize() {
        // Initialiser les listes déroulantes
        etatSanteComboBox.setItems(FXCollections.observableArrayList("Bon", "Fatigué", "Fièvre", "Malade"));
        appetitBebeComboBox.setItems(FXCollections.observableArrayList("Bon", "Faible", "Très bon"));

        // Définir des valeurs par défaut
        dateSuiviPicker.setValue(LocalDate.now());
        etatSanteComboBox.setValue("Bon");
        appetitBebeComboBox.setValue("Bon");

        // Style pour les labels d'erreur
        String errorStyle = "-fx-text-fill: red; -fx-font-size: 11px;";
        dateErreurLabel.setStyle(errorStyle);
        poidsErreurLabel.setStyle(errorStyle);
        tailleErreurLabel.setStyle(errorStyle);
        santeErreurLabel.setStyle(errorStyle);
        battementErreurLabel.setStyle(errorStyle);
        appetitErreurLabel.setStyle(errorStyle);

        // Masquer les labels d'erreur au démarrage
        hideAllErrorLabels();

        // Ajouter des listeners pour validation en temps réel
        setupValidationListeners();

        System.out.println("AjoutSuiviBebeController initialisé");
    }

    private void hideAllErrorLabels() {
        dateErreurLabel.setVisible(false);
        poidsErreurLabel.setVisible(false);
        tailleErreurLabel.setVisible(false);
        santeErreurLabel.setVisible(false);
        battementErreurLabel.setVisible(false);
        appetitErreurLabel.setVisible(false);
    }

    private void setupValidationListeners() {
        // Validation de la date
        dateSuiviPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateDate(newVal);
        });

        // Validation du poids
        poidsBebeField.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePoids(newVal);
        });

        // Validation de la taille
        tailleBebeField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateTaille(newVal);
        });

        // Validation des battements de cœur
        battementCoeurField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateBattement(newVal);
        });

        // Validation des listes déroulantes
        etatSanteComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            santeErreurLabel.setVisible(newVal == null || newVal.isEmpty());
        });

        appetitBebeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            appetitErreurLabel.setVisible(newVal == null || newVal.isEmpty());
        });
    }

    private boolean validateDate(LocalDate date) {
        if (date == null) {
            dateErreurLabel.setText("Date requise");
            dateErreurLabel.setVisible(true);
            return false;
        } else if (date.isAfter(LocalDate.now())) {
            dateErreurLabel.setText("La date ne peut pas être future");
            dateErreurLabel.setVisible(true);
            return false;
        } else {
            dateErreurLabel.setVisible(false);
            return true;
        }
    }

    private boolean validatePoids(String poidsStr) {
        try {
            if (poidsStr.isEmpty()) {
                poidsErreurLabel.setText("Poids requis");
                poidsErreurLabel.setVisible(true);
                return false;
            }

            double poids = Double.parseDouble(poidsStr);
            if (poids < POIDS_MIN || poids > POIDS_MAX) {
                poidsErreurLabel.setText("Poids doit être entre " + POIDS_MIN + " et " + POIDS_MAX + " kg");
                poidsErreurLabel.setVisible(true);
                return false;
            } else {
                poidsErreurLabel.setVisible(false);
                return true;
            }
        } catch (NumberFormatException e) {
            poidsErreurLabel.setText("Format numérique invalide");
            poidsErreurLabel.setVisible(true);
            return false;
        }
    }

    private boolean validateTaille(String tailleStr) {
        try {
            if (tailleStr.isEmpty()) {
                tailleErreurLabel.setText("Taille requise");
                tailleErreurLabel.setVisible(true);
                return false;
            }

            double taille = Double.parseDouble(tailleStr);
            if (taille < TAILLE_MIN || taille > TAILLE_MAX) {
                tailleErreurLabel.setText("Taille doit être entre " + TAILLE_MIN + " et " + TAILLE_MAX + " cm");
                tailleErreurLabel.setVisible(true);
                return false;
            } else {
                tailleErreurLabel.setVisible(false);
                return true;
            }
        } catch (NumberFormatException e) {
            tailleErreurLabel.setText("Format numérique invalide");
            tailleErreurLabel.setVisible(true);
            return false;
        }
    }

    private boolean validateBattement(String battementStr) {
        try {
            if (battementStr.isEmpty()) {
                battementErreurLabel.setText("Battement requis");
                battementErreurLabel.setVisible(true);
                return false;
            }

            double battement = Double.parseDouble(battementStr);
            if (battement < BATTEMENT_MIN || battement > BATTEMENT_MAX) {
                battementErreurLabel.setText("Battement doit être entre " + BATTEMENT_MIN + " et " + BATTEMENT_MAX + " bpm");
                battementErreurLabel.setVisible(true);
                return false;
            } else {
                battementErreurLabel.setVisible(false);
                return true;
            }
        } catch (NumberFormatException e) {
            battementErreurLabel.setText("Format numérique invalide");
            battementErreurLabel.setVisible(true);
            return false;
        }
    }

    private boolean validateAllFields() {
        boolean dateValid = validateDate(dateSuiviPicker.getValue());
        boolean poidsValid = validatePoids(poidsBebeField.getText());
        boolean tailleValid = validateTaille(tailleBebeField.getText());
        boolean battementValid = validateBattement(battementCoeurField.getText());
        boolean santeValid = etatSanteComboBox.getValue() != null && !etatSanteComboBox.getValue().isEmpty();
        boolean appetitValid = appetitBebeComboBox.getValue() != null && !appetitBebeComboBox.getValue().isEmpty();

        // Afficher les erreurs pour les combobox si nécessaire
        santeErreurLabel.setVisible(!santeValid);
        appetitErreurLabel.setVisible(!appetitValid);

        return dateValid && poidsValid && tailleValid && battementValid && santeValid && appetitValid;
    }

    public void setSuiviGrossesse(suiviGrossesse sg) {
        if (sg == null) {
            System.err.println("ERREUR: suiviGrossesse null passé à AjoutSuiviBebeController");
            return;
        }
        this.suiviGrossesse = sg;
        System.out.println("SuiviGrossesse défini dans AjoutSuiviBebeController: " + sg.getId());
    }

    public void setParentController(AfficherSuiviBebeController controller) {
        if (controller == null) {
            System.err.println("ERREUR: parentController null passé à AjoutSuiviBebeController");
            return;
        }
        this.parentController = controller;
        System.out.println("ParentController défini dans AjoutSuiviBebeController");
    }

    public void chargerDonneesPourModification(suiviBebe suivi) {
        if (suivi == null) {
            System.err.println("ERREUR: suiviBebe null passé à chargerDonneesPourModification");
            return;
        }

        this.suiviBebeAModifier = suivi;
        this.modeModification = true;

        try {
            // Masquer toutes les erreurs au chargement des données
            hideAllErrorLabels();

            // Changer le titre du formulaire
            if (titreFormulaire != null) {
                titreFormulaire.setText("Modifier le suivi bébé");
            }

            // Traitement de la date avec gestion d'erreurs renforcée
            if (suivi.getDateSuivi() != null) {
                try {
                    // Conversion de java.sql.Date à LocalDate directement
                    LocalDate localDate = ((Date) suivi.getDateSuivi()).toLocalDate();
                    dateSuiviPicker.setValue(localDate);
                } catch (Exception e) {
                    System.err.println("Erreur lors de la conversion de date: " + e.getMessage());
                    // Méthode alternative si la première échoue
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(suivi.getDateSuivi());
                    LocalDate localDate = LocalDate.of(
                            cal.get(java.util.Calendar.YEAR),
                            cal.get(java.util.Calendar.MONTH) + 1,
                            cal.get(java.util.Calendar.DAY_OF_MONTH)
                    );
                    dateSuiviPicker.setValue(localDate);
                }
            } else {
                dateSuiviPicker.setValue(LocalDate.now());
            }

            // Remplir les champs numériques avec gestion des erreurs
            try {
                poidsBebeField.setText(String.valueOf(suivi.getPoidsBebe()));
                tailleBebeField.setText(String.valueOf(suivi.getTailleBebe()));
                battementCoeurField.setText(String.valueOf(suivi.getBattementCoeur()));
            } catch (Exception e) {
                System.err.println("Erreur lors du remplissage des champs numériques: " + e.getMessage());
                // Valeurs par défaut en cas d'erreur
                poidsBebeField.setText("0.0");
                tailleBebeField.setText("0.0");
                battementCoeurField.setText("0.0");
            }

            // Définir les valeurs des ComboBox avec vérification
            if (suivi.getEtatSante() != null && !suivi.getEtatSante().isEmpty() &&
                    etatSanteComboBox.getItems().contains(suivi.getEtatSante())) {
                etatSanteComboBox.setValue(suivi.getEtatSante());
            } else {
                etatSanteComboBox.setValue("Bon"); // Valeur par défaut
            }

            if (suivi.getAppetitBebe() != null && !suivi.getAppetitBebe().isEmpty() &&
                    appetitBebeComboBox.getItems().contains(suivi.getAppetitBebe())) {
                appetitBebeComboBox.setValue(suivi.getAppetitBebe());
            } else {
                appetitBebeComboBox.setValue("Bon"); // Valeur par défaut
            }

            // Changer le texte du bouton
            enregistrerButton.setText("Mettre à jour");

            System.out.println("Données chargées pour modification du suivi bébé ID: " + suivi.getId());
        } catch (Exception e) {
            System.err.println("Erreur générale lors du chargement des données: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                    "Impossible de charger les données pour modification: " + e.getMessage());
        }
    }


    @FXML
    public void enregistrerSuiviBebe(ActionEvent actionEvent) {
        try {
            if (suiviGrossesse == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Suivi grossesse non défini.");
                return;
            }

            // Validation complète des champs
            if (!validateAllFields()) {
                showAlert(Alert.AlertType.ERROR, "Validation échouée",
                        "Veuillez corriger les erreurs dans le formulaire.");
                return;
            }

            // Conversion des données
            LocalDate dateSuivi = dateSuiviPicker.getValue();
            double poidsBebe = Double.parseDouble(poidsBebeField.getText().trim());
            double tailleBebe = Double.parseDouble(tailleBebeField.getText().trim());
            double battementCoeur = Double.parseDouble(battementCoeurField.getText().trim());
            String etatSante = etatSanteComboBox.getValue();
            String appetitBebe = appetitBebeComboBox.getValue();

            // Création ou modification de l'objet
            suiviBebe sb;
            if (modeModification && suiviBebeAModifier != null) {
                sb = suiviBebeAModifier;
                System.out.println("Modification du suivi bébé ID: " + suiviBebeAModifier.getId());
            } else {
                sb = new suiviBebe();
                System.out.println("Création d'un nouveau suivi bébé");
            }

            // Définition des valeurs
            sb.setSuiviGrossesse(suiviGrossesse);
            sb.setDateSuivi(Date.valueOf(dateSuivi));
            sb.setPoidsBebe(poidsBebe);
            sb.setTailleBebe(tailleBebe);
            sb.setBattementCoeur(battementCoeur);
            sb.setEtatSante(etatSante);
            sb.setAppetitBebe(appetitBebe);

            // Enregistrement ou mise à jour
            if (modeModification) {
                suiviBebeService.modifier(sb);
                System.out.println("Suivi Bébé mis à jour avec succès !");
            } else {
                suiviBebeService.ajouter(sb);
                System.out.println("Suivi Bébé ajouté avec succès !");
            }

            // Fermer le formulaire et rafraîchir le tableau
            if (parentController != null) {
                // Afficher message de succès
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            modeModification ? "Suivi Bébé mis à jour avec succès !" : "Suivi Bébé ajouté avec succès !");

                    // Fermer le formulaire immédiatement après la fermeture de l'alerte
                    parentController.fermerFormulaire();

                    // Rafraîchir le tableau après un court délai
                    Platform.runLater(() -> {
                        parentController.rafraichirTableau();
                    });
                });
            } else {
                System.err.println("ERREUR: parentController est null lors de la tentative de fermeture");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur lors de l'opération: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'opération: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void annuler(ActionEvent event) {
        // Fermer le formulaire immédiatement
        if (parentController != null) {
            Platform.runLater(() -> {
                System.out.println("Annulation et fermeture du formulaire");
                parentController.fermerFormulaire();
            });
        } else {
            System.err.println("ERREUR: parentController est null lors de la tentative d'annulation");
        }
    }

    public void verifierReferences() {
        if (suiviGrossesse == null) {
            System.err.println("ERREUR: suiviGrossesse n'est pas défini!");
        } else {
            System.out.println("OK: suiviGrossesse défini avec ID: " + suiviGrossesse.getId());
        }

        if (parentController == null) {
            System.err.println("ERREUR: parentController n'est pas défini!");
        } else {
            System.out.println("OK: parentController est défini");
        }
    }
}