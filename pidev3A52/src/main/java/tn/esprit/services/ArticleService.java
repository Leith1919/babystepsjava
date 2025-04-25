package tn.esprit.services;

import tn.esprit.entities.Article;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticleService {

    private final Connection cnx = MyDatabase.getInstance().getCnx();

    public void ajouter(Article a) throws SQLException {
        String sql = "INSERT INTO article (titre, contenu, datearticle, galerie, nbrevue, nbrelike) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, a.getTitre());
            ps.setString(2, a.getContenu());
            ps.setTimestamp(3, a.getDateArticle());
            ps.setString(4, a.getGalerie());
            ps.setInt(5, a.getNbreVue());
            ps.setInt(6, a.getNbreLike());
            ps.executeUpdate();
        }
    }

    public List<Article> recuperer() throws SQLException {
        List<Article> list = new ArrayList<>();
        String sql = "SELECT * FROM article";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Article a = new Article();
                a.setId(rs.getInt("id"));
                a.setTitre(rs.getString("titre"));
                a.setContenu(rs.getString("contenu"));
                a.setDateArticle(rs.getTimestamp("datearticle"));
                a.setGalerie(rs.getString("galerie"));
                a.setNbreVue(rs.getInt("nbrevue"));
                a.setNbreLike(rs.getInt("nbrelike"));
                list.add(a);
            }
        }
        return list;
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM article WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
    public void modifier(Article a) throws SQLException {
        String sql = "UPDATE article SET titre = ?, contenu = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, a.getTitre());
            ps.setString(2, a.getContenu());
            ps.setInt(3, a.getId());
            ps.executeUpdate();
        }
    }

}
