package services.User;



import models.Article;
import models.Commentaire;
import utils.MyDatabase;
import utils.UserSession;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService {

    private final Connection cnx = MyDatabase.getInstance().getConnection();
    private final EmailService emailService = new EmailService(); // Service d'email

    // Méthode pour ajouter un commentaire
    public void ajouter(Commentaire c) throws SQLException {

        String sql = "INSERT INTO commentaire (article_id, email, commentaire, datecommentaire,user_id) VALUES (?, ?, ?, ?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, c.getArticleId());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getCommentaire());
            ps.setTimestamp(4, c.getDateCommentaire());
            ps.setInt(5, UserSession.getCurrentUser().getId());
            ps.executeUpdate();
        }
    }

    // Méthode pour récupérer tous les commentaires
    public List<Commentaire> recuperer() throws SQLException {
        List<Commentaire> list = new ArrayList<>();
        String sql = "SELECT * FROM commentaire";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setId(rs.getInt("id"));
                c.setArticleId(rs.getInt("article_id"));
                c.setEmail(rs.getString("email"));
                c.setCommentaire(rs.getString("commentaire"));
                c.setDateCommentaire(rs.getTimestamp("datecommentaire"));
                list.add(c);
            }
        }
        return list;
    }

    // Méthode pour récupérer les commentaires d'un article
    public List<Commentaire> getCommentairesByArticle(int articleId) throws SQLException {
        List<Commentaire> list = new ArrayList<>();
        String sql = "SELECT c.*, a.titre FROM commentaire c JOIN article a ON c.article_id = a.id WHERE c.article_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, articleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Commentaire c = new Commentaire();
                    c.setId(rs.getInt("id"));
                    c.setArticleId(rs.getInt("article_id"));
                    c.setEmail(rs.getString("email"));
                    c.setCommentaire(rs.getString("commentaire"));
                    c.setDateCommentaire(rs.getTimestamp("datecommentaire"));

                    // Associer l'article avec son titre
                    Article article = new Article();
                    article.setId(rs.getInt("article_id"));
                    article.setTitre(rs.getString("titre"));
                    c.setArticleId(article.getId());

                    list.add(c);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des commentaires : " + e.getMessage());
            throw e;
        }

        return list;
    }

    // Méthode pour supprimer un commentaire
    public void supprimer(int id) throws SQLException {
        PreparedStatement ps = cnx.prepareStatement("DELETE FROM commentaire WHERE id = ?");
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // Méthode pour modifier un commentaire
    public void modifier(Commentaire c) throws SQLException {
        PreparedStatement ps = cnx.prepareStatement("UPDATE commentaire SET commentaire = ? WHERE id = ?");
        ps.setString(1, c.getCommentaire());
        ps.setInt(2, c.getId());
        ps.executeUpdate();
    }

    // Méthode pour détecter les commentaires impolis
    private boolean isCommentaireImpoli(Commentaire commentaire) {
        // Liste de mots impolis à rechercher dans le commentaire
        String[] motsImpolits = {"dog", "propos violents", "termes inappropriés"};
        for (String mot : motsImpolits) {
            if (commentaire.getCommentaire().toLowerCase().contains(mot)) {
                return true;
            }
        }
        return false;
    }
}
