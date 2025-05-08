package controllers.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Ordonnance;
import services.User.OrdonnanceServices;
import utils.MailSender;
import utils.PDFGenerator;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AfficherOrdonnanceControllors {

    @FXML private ListView<Ordonnance> ordonnanceListView;
    @FXML private TextField searchField;
    @FXML private Label idLabel, dateLabel, medicamentLabel, posologieLabel, patientLabel;
    @FXML private Button previousButton, nextButton;
    @FXML private Label pageLabel;

    private final OrdonnanceServices ordonnanceService = new OrdonnanceServices();
    private final ObservableList<Ordonnance> allOrdonnances = FXCollections.observableArrayList();
    private static final int ITEMS_PER_PAGE = 5;
    private int currentPage = 1;
    private int totalPages = 1;

    @FXML
    private void initialize() {
        try {
            loadOrdonnances();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des ordonnances : " + e.getMessage());
        }
        setupLiveSearch();
        setupSelectionListener();
    }

    private void loadOrdonnances() throws SQLException {
        List<Ordonnance> ordList = ordonnanceService.recuperer();

        // Sort by most recent date
        ordList.sort((o1, o2) -> {
            try {
                LocalDate date1 = LocalDate.parse(o1.getDatePrescription());
                LocalDate date2 = LocalDate.parse(o2.getDatePrescription());
                return date2.compareTo(date1);
            } catch (DateTimeParseException e) {
                return 0;
            }
        });

        allOrdonnances.setAll(ordList);
        totalPages = (int) Math.ceil((double) allOrdonnances.size() / ITEMS_PER_PAGE);
        currentPage = 1;
        updateListView();
    }

    private void updateListView() {
        int fromIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allOrdonnances.size());
        ordonnanceListView.setItems(FXCollections.observableArrayList(allOrdonnances.subList(fromIndex, toIndex)));
        pageLabel.setText("Page " + currentPage + " / " + totalPages);
    }

    @FXML
    private void previousPage(ActionEvent event) {
        if (currentPage > 1) {
            currentPage--;
            updateListView();
        }
    }

    @FXML
    private void nextPage(ActionEvent event) {
        if (currentPage < totalPages) {
            currentPage++;
            updateListView();
        }
    }

    private void setupLiveSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> rechercherOrdonnance(newValue.trim().toLowerCase()));
    }

    private void rechercherOrdonnance(String keyword) {
        if (keyword.isEmpty()) {
            updateListView();
            return;
        }

        ObservableList<Ordonnance> filtered = FXCollections.observableArrayList();
        for (Ordonnance o : allOrdonnances) {
            if (o.toString().toLowerCase().contains(keyword)) {
                filtered.add(o);
            }
        }

        ordonnanceListView.setItems(filtered);
        pageLabel.setText("Résultats filtrés: " + filtered.size());
    }

    private void setupSelectionListener() {
        ordonnanceListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) setOrdonnanceDetails(newVal);
        });
    }

    private void setOrdonnanceDetails(Ordonnance o) {
        idLabel.setText(String.valueOf(o.getId()));
        dateLabel.setText(o.getDatePrescription());
        medicamentLabel.setText(o.getMedicament());
        posologieLabel.setText(o.getPosologie());
        patientLabel.setText(o.getPatientName());
    }

    @FXML
    private void supprimerOrdonnance(ActionEvent event) {
        Ordonnance selected = ordonnanceListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner une ordonnance à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer cette ordonnance ?");
        confirm.setContentText(selected.toString());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    ordonnanceService.supprimer(selected);
                    showAlert("Succès", "Ordonnance supprimée.");
                    loadOrdonnances();
                    clearDetails();
                } catch (SQLException e) {
                    showAlert("Erreur", "Suppression échouée : " + e.getMessage());
                }
            }
        });
    }

    private void clearDetails() {
        idLabel.setText("");
        dateLabel.setText("");
        medicamentLabel.setText("");
        posologieLabel.setText("");
        patientLabel.setText("");
    }

    @FXML
    private void transitionToAjouterOrdonnance(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/front/ajouterOrdonnance.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la vue d'ajout : " + e.getMessage());
        }
    }

    @FXML
    private void modifierOrdonnance(ActionEvent event) {
        Ordonnance selected = ordonnanceListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner une ordonnance à modifier.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/modifierOrdonnance.fxml"));
            Parent root = loader.load();

            ModifierOrdonnanceControllors controller = loader.getController();
            controller.setOrdonnanceToModify(selected);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification : " + e.getMessage());
        }
    }

    @FXML
    public void generatePdf(ActionEvent event) {
        Ordonnance selected = ordonnanceListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucune sélection", "Sélectionnez une ordonnance.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter Ordonnance en PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());

        if (file != null) {
            try {
                PDFGenerator.generatePDF(file.getAbsolutePath(), selected);
                showAlert("Succès", "PDF généré avec succès.");

                TextInputDialog emailDialog = new TextInputDialog();
                emailDialog.setTitle("Envoyer par Email");
                emailDialog.setHeaderText("Souhaitez-vous envoyer cette ordonnance par email ?");
                emailDialog.setContentText("Adresse email du destinataire :");

                emailDialog.showAndWait().ifPresent(email -> {
                    try {
                        String subject = "Ordonnance du patient : " + selected.getPatientName();
                        String content = "Voici les détails de l'ordonnance :\n"
                                + "Date : " + selected.getDatePrescription() + "\n"
                                + "Médicament : " + selected.getMedicament() + "\n"
                                + "Posologie : " + selected.getPosologie();

                        MailSender.sendMailWithAttachment(email, subject, content, file.getAbsolutePath());
                        showAlert("Succès", "Email envoyé avec succès !");
                    } catch (Exception e) {
                        showAlert("Erreur", "Erreur lors de l'envoi de l'email : " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la génération du PDF : " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
