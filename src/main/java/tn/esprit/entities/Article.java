package tn.esprit.entities;

import java.sql.Timestamp;

public class Article {
    private int id;
    private String titre;
    private String contenu;
    private Timestamp dateArticle;
    private String galerie;
    private int nbreVue;
    private int nbreLike;

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public Timestamp getDateArticle() { return dateArticle; }
    public void setDateArticle(Timestamp dateArticle) { this.dateArticle = dateArticle; }

    public String getGalerie() { return galerie; }
    public void setGalerie(String galerie) { this.galerie = galerie; }

    public int getNbreVue() { return nbreVue; }
    public void setNbreVue(int nbreVue) { this.nbreVue = nbreVue; }

    public int getNbreLike() { return nbreLike; }
    public void setNbreLike(int nbreLike) { this.nbreLike = nbreLike; }
}
