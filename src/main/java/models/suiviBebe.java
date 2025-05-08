package models;

import java.util.Date;

public class suiviBebe {
    private int id;
    private suiviGrossesse suiviGrossesse;
    private Date dateSuivi;
    private double poidsBebe;
    private double tailleBebe;
    private String etatSante;
    private double battementCoeur;
    private String appetitBebe;

    // Constructeurs
    public suiviBebe() {}

    public suiviBebe(int id, suiviGrossesse suiviGrossesse, Date dateSuivi, double poidsBebe, double tailleBebe, String etatSante, double battementCoeur, String appetitBebe) {
        this.id = id;
        this.suiviGrossesse = suiviGrossesse;
        this.dateSuivi = dateSuivi;
        this.poidsBebe = poidsBebe;
        this.tailleBebe = tailleBebe;
        this.etatSante = etatSante;
        this.battementCoeur = battementCoeur;
        this.appetitBebe = appetitBebe;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public suiviGrossesse getSuiviGrossesse() {
        return suiviGrossesse;
    }

    public void setSuiviGrossesse(suiviGrossesse suiviGrossesse) {
        this.suiviGrossesse = suiviGrossesse;
    }

    public Date getDateSuivi() {
        return dateSuivi;
    }

    public void setDateSuivi(Date dateSuivi) {
        this.dateSuivi = dateSuivi;
    }

    public double getPoidsBebe() {
        return poidsBebe;
    }

    public void setPoidsBebe(double poidsBebe) {
        this.poidsBebe = poidsBebe;
    }

    public double getTailleBebe() {
        return tailleBebe;
    }

    public void setTailleBebe(double tailleBebe) {
        this.tailleBebe = tailleBebe;
    }

    public String getEtatSante() {
        return etatSante;
    }

    public void setEtatSante(String etatSante) {
        this.etatSante = etatSante;
    }

    public double getBattementCoeur() {
        return battementCoeur;
    }

    public void setBattementCoeur(double battementCoeur) {
        this.battementCoeur = battementCoeur;
    }

    public String getAppetitBebe() {
        return appetitBebe;
    }

    public void setAppetitBebe(String appetitBebe) {
        this.appetitBebe = appetitBebe;
    }

    // Méthode toString()
    @Override
    public String toString() {
        return "SuiviBebe{" +
                "id=" + id +
                ", suiviGrossesse=" + suiviGrossesse +
                ", dateSuivi=" + dateSuivi +
                ", poidsBebe=" + poidsBebe +
                ", tailleBebe=" + tailleBebe +
                ", etatSante='" + etatSante + '\'' +
                ", battementCoeur=" + battementCoeur +
                ", appetitBebe='" + appetitBebe + '\'' +
                '}';
    }
}
