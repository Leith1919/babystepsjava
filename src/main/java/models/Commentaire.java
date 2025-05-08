package models;

import java.sql.Timestamp;

public class Commentaire {
    private int id;
    private int articleId;
    private String email;
    private String commentaire;
    private Timestamp dateCommentaire;

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getArticleId() { return articleId; }
    public void setArticleId(int articleId) { this.articleId = articleId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public Timestamp getDateCommentaire() { return dateCommentaire; }
    public void setDateCommentaire(Timestamp dateCommentaire) { this.dateCommentaire = dateCommentaire; }
}
