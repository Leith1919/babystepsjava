package tn.esprit.Controllers.Article;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.Article;
import tn.esprit.services.ArticleService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ArticleFrontCrontroller implements Initializable {

    @FXML
    private FlowPane articlesContainer;

    private final ArticleService articleService = new ArticleService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadArticles();
    }

    private void loadArticles() {
        try {
            List<Article> articles = articleService.recuperer();
            for (Article article : articles) {
                articlesContainer.getChildren().add(createArticleCard(article));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createArticleCard(Article article) {
        VBox card = new VBox(10);
        card.getStyleClass().add("content-card");
        card.setPrefSize(200, 280);
        card.setStyle("-fx-padding: 10;");

        // 🔹 Image
        ImageView imageView = new ImageView();
        try {
            String imageUrl = "http://localhost/uploads/" + article.getGalerie();
            Image image = new Image(imageUrl, 180, 120, true, true); // width, height, preserveRatio, smooth
            imageView.setImage(image);
        } catch (Exception e) {
            e.printStackTrace(); // En cas d'URL invalide
        }

        // 🔹 Titre
        Label titre = new Label(article.getTitre());
        titre.getStyleClass().add("title-text-secondary");

        // 🔹 Contenu Preview
        Label preview = new Label(article.getContenu().length() > 50 ? article.getContenu().substring(0, 50) + "..." : article.getContenu());
        preview.setWrapText(true);

        // 🔹 Détails
        Button detailsBtn = new Button("Détails");
        detailsBtn.getStyleClass().add("action-button");
        detailsBtn.setOnAction(e -> openArticleDetails(article));

        card.getChildren().addAll(imageView, titre, preview, detailsBtn);
        return card;
    }

    private void openArticleDetails(Article article) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Articles/Front/ArticleDetails.fxml"));
            AnchorPane pane = loader.load();

            ArticleDetailsController controller = loader.getController();
            controller.setArticle(article);

            Stage stage = new Stage();
            stage.setTitle("Détails de l'Article");
            stage.setScene(new Scene(pane));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
