package tn.esprit.Controllers.Article;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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

    private final CommentaireService commentaireService = new CommentaireService();
    private Article currentArticle;
    private Commentaire currentCommentaire = null;  // Null = Ajout, sinon = Modification

    public void setArticle(Article article) {
        this.currentArticle = article;
        loadCommentaires();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

        // 🔹 Affichage du titre de l'article associé
        Label articleTitle = new Label("📰 Article : " + commentaire.getArticleId());
        articleTitle.getStyleClass().add("title-text-secondary");

        // 🔹 Infos du commentaire
        Label email = new Label("👤 " + commentaire.getEmail());
        Label contenu = new Label(commentaire.getCommentaire());
        contenu.setWrapText(true);

        // 🔹 Boutons actions
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

        try {
            if (currentCommentaire == null) {
                // ➕ Ajout
                Commentaire c = new Commentaire();
                c.setArticleId(currentArticle.getId());
                c.setEmail(email);
                c.setCommentaire(texte);
                c.setDateCommentaire(new Timestamp(System.currentTimeMillis()));

                commentaireService.ajouter(c);
                showAlert("Succès", "Commentaire ajouté !");
            } else {
                // ✏️ Modification
                currentCommentaire.setEmail(email);
                currentCommentaire.setCommentaire(texte);
                commentaireService.modifier(currentCommentaire);
                showAlert("Succès", "Commentaire modifié !");
                currentCommentaire = null;  // Reset après modification
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
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
