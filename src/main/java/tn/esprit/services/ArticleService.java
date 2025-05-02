package tn.esprit.services;

import tn.esprit.entities.Article;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticleService {

    private final Connection cnx = MyDatabase.getInstance().getCnx();

    public void ajouter(Article a) throws SQLException {
        validateArticle(a);
        
        String sql = "INSERT INTO article (titre, contenu, datearticle, galerie, nbrevue, nbrelike) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setArticleParameters(ps, a);
            ps.executeUpdate();
            
            // Récupérer l'ID généré
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    a.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<Article> recuperer() throws SQLException {
        List<Article> list = new ArrayList<>();
        String sql = "SELECT * FROM article ORDER BY datearticle DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToArticle(rs));
            }
        }
        return list;
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM article WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("L'article avec l'ID " + id + " n'existe pas");
            }
        }
    }

    public void modifier(Article a) throws SQLException {
        validateArticle(a);
        
        String sql = "UPDATE article SET titre = ?, contenu = ?, galerie = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, a.getTitre());
            ps.setString(2, a.getContenu());
            ps.setString(3, a.getGalerie());
            ps.setInt(4, a.getId());
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("L'article avec l'ID " + a.getId() + " n'existe pas");
            }
        }
    }

    public List<Article> searchByTitle(String title) throws SQLException {
        List<Article> list = new ArrayList<>();
        String sql = "SELECT * FROM article WHERE LOWER(titre) LIKE LOWER(?) ORDER BY datearticle DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, "%" + title + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToArticle(rs));
                }
            }
        }
        return list;
    }

    private void validateArticle(Article a) throws SQLException {
        if (a == null) {
            throw new SQLException("L'article ne peut pas être null");
        }
        if (a.getTitre() == null || a.getTitre().trim().isEmpty()) {
            throw new SQLException("Le titre est obligatoire");
        }
        if (a.getContenu() == null || a.getContenu().trim().isEmpty()) {
            throw new SQLException("Le contenu est obligatoire");
        }
        if (a.getGalerie() == null) {
            throw new SQLException("L'image est obligatoire");
        }
    }

    private void setArticleParameters(PreparedStatement ps, Article a) throws SQLException {
        ps.setString(1, a.getTitre().trim());
        ps.setString(2, a.getContenu().trim());
        ps.setTimestamp(3, a.getDateArticle());
        ps.setString(4, a.getGalerie());
        ps.setInt(5, a.getNbreVue());
        ps.setInt(6, a.getNbreLike());
    }

    private Article mapResultSetToArticle(ResultSet rs) throws SQLException {
        Article a = new Article();
        a.setId(rs.getInt("id"));
        a.setTitre(rs.getString("titre"));
        a.setContenu(rs.getString("contenu"));
        a.setDateArticle(rs.getTimestamp("datearticle"));
        a.setGalerie(rs.getString("galerie"));
        a.setNbreVue(rs.getInt("nbrevue"));
        a.setNbreLike(rs.getInt("nbrelike"));
        return a;
    }
    public List<Article> getTop3ArticlesByViews() throws SQLException {
        String req = "SELECT * FROM article ORDER BY nbreVue DESC LIMIT 3";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        List<Article> top3 = new ArrayList<>();
        while (rs.next()) {
            Article a = new Article();
            a.setId(rs.getInt("id"));
            a.setTitre(rs.getString("titre"));
            a.setNbreVue(rs.getInt("nbreVue"));
            a.setNbreLike(rs.getInt("nbreLike"));
            top3.add(a);
        }
        return top3;
    }

}
