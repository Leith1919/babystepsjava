package tn.esprit.Controllers.Article;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Article;
import tn.esprit.entities.Commentaire;
import tn.esprit.services.CommentaireService;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

public class ArticleDetailsController implements Initializable {

    @FXML
    private Label articleTitle;

    @FXML
    private TextArea articleContent;

    @FXML
    private ImageView articleImageView;

    @FXML
    private TextField emailField;

    @FXML
    private TextArea commentaireField;

    @FXML
    private Button addCommentButton;

    @FXML
    private VBox commentsContainer;

    private Article currentArticle;
    private final CommentaireService commentaireService = new CommentaireService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addCommentButton.setOnAction(e -> ajouterCommentaire());
    }

    public void setArticle(Article article) {
        this.currentArticle = article;
        articleTitle.setText(article.getTitre());
        articleContent.setText(article.getContenu());

        // Charger l'image de l'article
        try {
            String imageUrl = "http://localhost/uploads/" + article.getGalerie();
            Image image = new Image(imageUrl, 600, 300, true, true);
            articleImageView.setImage(image);
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadCommentaires();
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
        }
    }

    private VBox createCommentCard(Commentaire commentaire) {
        VBox card = new VBox(5);
        card.getStyleClass().add("content-card");
        card.setPrefWidth(700);

        Label email = new Label("👤 " + commentaire.getEmail());
        Label contenu = new Label(commentaire.getCommentaire());
        contenu.setWrapText(true);

        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().add("action-button");
        editBtn.setOnAction(e -> modifierCommentaire(commentaire));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().addAll("action-button", "cancel-button");
        deleteBtn.setOnAction(e -> {
            try {
                commentaireService.supprimer(commentaire.getId());
                loadCommentaires();
                showAlert("Succès", "Commentaire supprimé !");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        card.getChildren().addAll(email, contenu, editBtn, deleteBtn);
        return card;
    }

    private void ajouterCommentaire() {
        String email = emailField.getText();
        String texte = commentaireField.getText();

        if (email.isEmpty() || texte.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs !");
            return;
        }

        Commentaire c = new Commentaire();
        c.setArticleId(currentArticle.getId());
        c.setEmail(email);
        c.setCommentaire(texte);
        c.setDateCommentaire(new Timestamp(System.currentTimeMillis()));

        try {
            commentaireService.ajouter(c);
            showAlert("Succès", "Commentaire ajouté !");
            loadCommentaires();
            emailField.clear();
            commentaireField.clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void modifierCommentaire(Commentaire commentaire) {
        TextInputDialog dialog = new TextInputDialog(commentaire.getCommentaire());
        dialog.setTitle("Modifier Commentaire");
        dialog.setHeaderText("Modifier le contenu :");

        dialog.showAndWait().ifPresent(newText -> {
            commentaire.setCommentaire(newText);
            try {
                commentaireService.modifier(commentaire);
                loadCommentaires();
                showAlert("Succès", "Commentaire modifié !");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
