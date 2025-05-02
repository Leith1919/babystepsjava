package tn.esprit.Controllers.Article;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.Article;
import tn.esprit.entities.Commentaire;
import tn.esprit.services.CommentaireService;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

public class CommentaireBackController implements Initializable {

    @FXML
    private VBox commentsContainer;

    @FXML
    private TextField emailField;

    @FXML
    private TextArea commentaireField;

    @FXML
    private Button addCommentButton;

    @FXML
    private Label errorLabel;

    private final CommentaireService commentaireService = new CommentaireService();
    private Article currentArticle;
    private Commentaire currentCommentaire = null;

    public void setArticle(Article article) {
        this.currentArticle = article;
        loadCommentaires();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorLabel.setVisible(false); // Masquer au lancement
        addCommentButton.setOnAction(e -> handleSave());
    }

    private void loadCommentaires() {
        commentsContainer.getChildren().clear();
        try {
            List<Commentaire> commentaires = commentaireService.getCommentairesByArticle(currentArticle.getId());
            for (Commentaire com : commentaires) {
                commentsContainer.getChildren().add(createCommentCard(com));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Problème lors du chargement des commentaires.");
        }
    }

    private VBox createCommentCard(Commentaire commentaire) {
        VBox card = new VBox(5);
        card.getStyleClass().add("content-card");

        Label articleTitle = new Label("📰 Article : " + commentaire.getArticleId());
        articleTitle.getStyleClass().add("title-text-secondary");

        Label email = new Label("👤 " + commentaire.getEmail());
        Label contenu = new Label(commentaire.getCommentaire());
        contenu.setWrapText(true);

        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().add("action-button");
        editBtn.setOnAction(e -> fillFormForEdit(commentaire));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().addAll("action-button", "cancel-button");
        deleteBtn.setOnAction(e -> supprimerCommentaire(commentaire));

        card.getChildren().addAll(articleTitle, email, contenu, editBtn, deleteBtn);
        return card;
    }

    private void handleSave() {
        errorLabel.setVisible(false); // Réinitialise l’erreur visuelle

        String email = emailField.getText().trim();
        String texte = commentaireField.getText().trim();

        if (email.isEmpty() || texte.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs !");
            return;
        }

        if (!email.contains("@")) {
            showAlert("Erreur", "Veuillez saisir un email valide !");
            return;
        }

        // 🔥 Vérification impolitesse
        if (isCommentImpoli(texte)) {
            errorLabel.setVisible(true); // Affiche le label rouge dans l’interface
            showBigRedErrorAndClose("⚠️ Commentaire inapproprié", "Vous avez fait un commentaire impoli !");
            return;
        }

        try {
            if (currentCommentaire == null) {
                Commentaire c = new Commentaire();
                c.setArticleId(currentArticle.getId());
                c.setEmail(email);
                c.setCommentaire(texte);
                c.setDateCommentaire(new Timestamp(System.currentTimeMillis()));
                commentaireService.ajouter(c);
                showAlert("Succès", "Commentaire ajouté !");
            } else {
                currentCommentaire.setEmail(email);
                currentCommentaire.setCommentaire(texte);
                commentaireService.modifier(currentCommentaire);
                showAlert("Succès", "Commentaire modifié !");
                currentCommentaire = null;
            }

            clearForm();
            loadCommentaires();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Problème lors de l'enregistrement !");
        }
    }

    private void fillFormForEdit(Commentaire commentaire) {
        this.currentCommentaire = commentaire;
        emailField.setText(commentaire.getEmail());
        commentaireField.setText(commentaire.getCommentaire());
        addCommentButton.setText("Modifier");
    }

    private void supprimerCommentaire(Commentaire commentaire) {
        try {
            commentaireService.supprimer(commentaire.getId());
            loadCommentaires();
            showAlert("Succès", "Commentaire supprimé !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de supprimer ce commentaire !");
        }
    }

    private void clearForm() {
        emailField.clear();
        commentaireField.clear();
        addCommentButton.setText("Ajouter");
        currentCommentaire = null;
        errorLabel.setVisible(false); // Cache l’erreur si présente
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 🔥 Alerte graphique + fermeture de la fenêtre principale
    private void showBigRedErrorAndClose(String title, String message) {
        Stage dialog = new Stage();
        dialog.setTitle(title);

        Label iconLabel = new Label("⚠️");
        iconLabel.setStyle("-fx-font-size: 48px;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: red; -fx-font-size: 22px; -fx-font-weight: bold;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);

        Button closeButton = new Button("Fermer");
        closeButton.setStyle("-fx-background-color: darkred; -fx-text-fill: white;");
        closeButton.setOnAction(e -> {
            dialog.close(); // Ferme l'alerte
            Stage mainStage = (Stage) commentaireField.getScene().getWindow();
            mainStage.close(); // Ferme la fenêtre principale
        });

        VBox box = new VBox(20, iconLabel, messageLabel, closeButton);
        box.setStyle("-fx-padding: 30px; -fx-alignment: center; -fx-background-color: white;");

        dialog.setScene(new Scene(box));
        dialog.setResizable(false);
        dialog.show();
    }

    // 🔥 Détection des mots interdits
    private boolean isCommentImpoli(String commentaire) {
        List<String> motsImpolis = List.of("dog", "noir", "null"); // à adapter selon ton besoin
        for (String mot : motsImpolis) {
            if (commentaire.toLowerCase().contains(mot.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
