package controllers.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.geometry.*;
import models.Article;
import services.User.ArticleService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

public class ArticleBackController implements Initializable {

    @FXML private FlowPane cardsContainer;
    @FXML private Button addButton;
    @FXML private VBox formContainer;
    @FXML private TextField titreField;
    @FXML private TextArea contenuField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button uploadImageButton;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button sortAZButton;
    @FXML private TextField pageNumberField;
    @FXML private Button goToPageButton;
    @FXML private Label pageLabel;

    private final ArticleService articleService = new ArticleService();
    private Article currentArticle = null;
    private String selectedImageName = "default.png";
    private int currentPage = 1;
    private final int itemsPerPage = 3;
    private List<Article> allArticles = List.of();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadArticles();

        addButton.setOnAction(e -> {
            currentArticle = null;
            clearForm();
            formContainer.setVisible(true);
        });

        cancelButton.setOnAction(e -> formContainer.setVisible(false));
        saveButton.setOnAction(e -> handleSave());
        uploadImageButton.setOnAction(e -> handleImageUpload());

        searchButton.setOnAction(e -> handleSearch());
        searchField.setOnAction(e -> handleSearch());

        sortAZButton.setOnAction(e -> handleSortAZ());

        goToPageButton.setOnAction(e -> {
            try {
                int page = Integer.parseInt(pageNumberField.getText().trim());
                showPage(page);
            } catch (NumberFormatException ex) {
                showAlert("Erreur", "Veuillez entrer un numéro de page valide.");
            }
        });
    }

    private void loadArticles() {
        try {
            allArticles = articleService.recuperer();
            showPage(currentPage);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createArticleCard(Article article) {
        VBox card = new VBox(10);
        card.getStyleClass().add("content-card");
        card.setPrefSize(200, 300);
        card.setStyle("-fx-padding: 10;");

        ImageView imageView = new ImageView();
        String imagePath = "http://localhost/uploads/" + article.getGalerie();
        Image image = new Image(imagePath, 180, 120, true, true);
        imageView.setImage(image);

        Label titre = new Label(article.getTitre());
        titre.getStyleClass().add("title-text-secondary");

        Label contenu = new Label(article.getContenu());
        contenu.setWrapText(true);

        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        Label ratingLabel = new Label("Notez l'article : ");
        ratingLabel.getStyleClass().add("info-label");

        HBox starsBox = new HBox(2);
        Label[] stars = new Label[5];
        int[] currentRating = {0};

        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            Label star = new Label("☆");
            stars[i] = star;
            star.getStyleClass().add("star-rating");

            star.setOnMouseEntered(e -> {
                for (int j = 0; j < rating; j++) stars[j].setText("★");
                for (int j = rating; j < 5; j++) stars[j].setText("☆");
            });

            star.setOnMouseExited(e -> {
                for (int j = 0; j < 5; j++) {
                    stars[j].setText(j < currentRating[0] ? "★" : "☆");
                }
            });

            star.setOnMouseClicked(e -> {
                currentRating[0] = rating;
                for (int j = 0; j < 5; j++) {
                    stars[j].setText(j < rating ? "★" : "☆");
                }
                showAlert("Note sur les Article ", "Vous avez donné une note de " + rating + " étoiles.");
                // TODO: Enregistrer la note dans la base via un service
            });

            starsBox.getChildren().add(star);
        }

        ratingBox.getChildren().addAll(ratingLabel, starsBox);

        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().add("action-button");
        editBtn.setOnAction(e -> {
            currentArticle = article;
            fillForm(article);
            formContainer.setVisible(true);
        });

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().addAll("action-button", "cancel-button");
        deleteBtn.setOnAction(e -> {
            try {
                articleService.supprimer(article.getId());
                loadArticles();
                showAlert("Succès", "Article supprimé !");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        Button commentBtn = new Button("Commentaire");
        commentBtn.getStyleClass().add("action-button");
        commentBtn.setOnAction(e -> handleComment(article));

        card.getChildren().addAll(imageView, titre, contenu, ratingBox, editBtn, deleteBtn, commentBtn);
        return card;
    }

    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null && selectedFile.exists()) {
            try {
                String fileName = selectedFile.getName().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
                Path destination = Paths.get("C:/xampp/htdocs/uploads/" + fileName);
                Files.createDirectories(destination.getParent());
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                selectedImageName = fileName;
                showAlert("Succès", "Image téléchargée : " + selectedImageName);
            } catch (IOException ex) {
                ex.printStackTrace();
                showAlert("Erreur", "Erreur lors du téléchargement de l'image : " + ex.getMessage());
            }
        } else {
            showAlert("Fichier invalide", "Le fichier sélectionné n'existe plus ou est introuvable.");
        }
    }

    private void handleSave() {
        String titre = titreField.getText();
        String contenu = contenuField.getText();

        if (titre.isEmpty() || contenu.isEmpty()) {
            showAlert("Erreur", "Tous les champs sont obligatoires !");
            return;
        }

        try {
            if (currentArticle == null) {
                Article a = new Article();
                a.setTitre(titre);
                a.setContenu(contenu);
                a.setDateArticle(new Timestamp(System.currentTimeMillis()));
                a.setGalerie(selectedImageName);
                a.setNbreVue(0);
                a.setNbreLike(0);
                articleService.ajouter(a);
                showAlert("Succès", "Article ajouté !");
            } else {
                currentArticle.setTitre(titre);
                currentArticle.setContenu(contenu);
                currentArticle.setGalerie(selectedImageName);
                articleService.modifier(currentArticle);
                showAlert("Succès", "Article modifié !");
            }

            formContainer.setVisible(false);
            loadArticles();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Problème lors de l'enregistrement !");
        }
    }

    private void handleComment(Article article) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Articles/back/Commentaire_back.fxml"));
// ✅ Corrigé : accepte n’importe quel type de racine
            Parent commentairePane = loader.load();

            CommentaireBackController controller = loader.getController();
            controller.setArticle(article);

            Stage stage = new Stage();
            stage.setTitle("Commentaires : " + article.getTitre());
            stage.setScene(new Scene(commentairePane));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        titreField.clear();
        contenuField.clear();
        selectedImageName = "default.png";
    }

    private void fillForm(Article article) {
        titreField.setText(article.getTitre());
        contenuField.setText(article.getContenu());
        selectedImageName = article.getGalerie();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        cardsContainer.getChildren().clear();
        try {
            List<Article> articles = articleService.recuperer();
            for (Article article : articles) {
                if (article.getTitre().toLowerCase().contains(searchText)) {
                    cardsContainer.getChildren().add(createArticleCard(article));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleSortAZ() {
        cardsContainer.getChildren().clear();
        try {
            List<Article> articles = articleService.recuperer();
            articles.sort((a1, a2) -> a1.getTitre().compareToIgnoreCase(a2.getTitre()));
            for (Article article : articles) {
                cardsContainer.getChildren().add(createArticleCard(article));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showPage(int page) {
        cardsContainer.getChildren().clear();

        int totalPages = (int) Math.ceil((double) allArticles.size() / itemsPerPage);
        if (page < 1 || page > totalPages) {
            showAlert("Erreur", "Numéro de page invalide. Max : " + totalPages);
            return;
        }

        int fromIndex = (page - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, allArticles.size());
        List<Article> pageArticles = allArticles.subList(fromIndex, toIndex);

        for (Article article : pageArticles) {
            cardsContainer.getChildren().add(createArticleCard(article));
        }

        currentPage = page;
        pageLabel.setText("Page " + currentPage + " / " + totalPages);
    }

    @FXML
    private void handleStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Articles/back/Statistiques.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Statistiques des Articles");
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
