package tn.esprit.Controllers.Article;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import tn.esprit.entities.Article;
import tn.esprit.services.ArticleService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class StatistiquesController implements Initializable {

    @FXML private Label totalVuesLabel;
    @FXML private Label totalLikesLabel;
    @FXML private ListView<String> top3List;
    @FXML private BarChart<String, Number> barChart;

    private final ArticleService articleService = new ArticleService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            List<Article> articles = articleService.recuperer();

            int totalVues = 0;
            int totalLikes = 0;

            // Nettoyer le graphique avant d'ajouter
            barChart.getData().clear();

            XYChart.Series<String, Number> vuesSeries = new XYChart.Series<>();
            vuesSeries.setName("Vues");

            XYChart.Series<String, Number> likesSeries = new XYChart.Series<>();
            likesSeries.setName("Likes");

            for (Article article : articles) {
                totalVues += article.getNbreVue();
                totalLikes += article.getNbreLike();

                vuesSeries.getData().add(new XYChart.Data<>(article.getTitre(), article.getNbreVue()));
                likesSeries.getData().add(new XYChart.Data<>(article.getTitre(), article.getNbreLike()));
            }

            totalVuesLabel.setText("Total Vues : " + totalVues);
            totalLikesLabel.setText("Total Likes : " + totalLikes);

            barChart.getData().addAll(vuesSeries, likesSeries);

            // Incliner les titres pour les rendre lisibles
            CategoryAxis xAxis = (CategoryAxis) barChart.getXAxis();
            xAxis.setTickLabelRotation(-45);

            // Appliquer une couleur pour chaque série
            vuesSeries.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: #FFA500;"); // Orange
                }
            });
            likesSeries.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: #00BFFF;"); // Bleu
                }
            });

            // Afficher le top 3
            List<Article> top3 = articleService.getTop3ArticlesByViews();
            for (Article a : top3) {
                top3List.getItems().add(a.getTitre() + " - " + a.getNbreVue() + " vues");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
