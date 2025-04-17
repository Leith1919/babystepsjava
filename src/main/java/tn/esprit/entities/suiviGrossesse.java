package tn.esprit.entities;

import java.util.Date;

public class suiviGrossesse {
    private int id;
    private Date dateSuivi;
    private double poids;
    private double tension;
    private String symptomes;
    private String etatGrossesse;

    // Constructeur vide (obligatoire pour certains frameworks ou outils)
    public suiviGrossesse() {}

    // Constructeur pour l'insertion (sans id, car il est auto-incrémenté)
    public suiviGrossesse(Date dateSuivi, double poids, double tension, String symptomes, String etatGrossesse) {
        this.dateSuivi = dateSuivi;
        this.poids = poids;
        this.tension = tension;
        this.symptomes = symptomes;
        this.etatGrossesse = etatGrossesse;
    }

    // Constructeur complet (utile lors de la récupération depuis la BD)
    public suiviGrossesse(int id, Date dateSuivi, double poids, double tension, String symptomes, String etatGrossesse) {
        this.id = id;
        this.dateSuivi = dateSuivi;
        this.poids = poids;
        this.tension = tension;
        this.symptomes = symptomes;
        this.etatGrossesse = etatGrossesse;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDateSuivi() {
        return dateSuivi;
    }

    public void setDateSuivi(Date dateSuivi) {
        this.dateSuivi = dateSuivi;
    }

    public double getPoids() {
        return poids;
    }

    public void setPoids(double poids) {
        this.poids = poids;
    }

    public double getTension() {
        return tension;
    }

    public void setTension(double tension) {
        this.tension = tension;
    }

    public String getSymptomes() {
        return symptomes;
    }

    public void setSymptomes(String symptomes) {
        this.symptomes = symptomes;
    }

    public String getEtatGrossesse() {
        return etatGrossesse;
    }

    public void setEtatGrossesse(String etatGrossesse) {
        this.etatGrossesse = etatGrossesse;
    }

    // toString()
    @Override
    public String toString() {
        return "SuiviGrossesse{" +
                "id=" + id +
                ", dateSuivi=" + dateSuivi +
                ", poids=" + poids +
                ", tension=" + tension +
                ", symptomes='" + symptomes + '\'' +
                ", etatGrossesse='" + etatGrossesse + '\'' +
                '}';
    }
}
