package models;

import java.util.Date;

public class suiviGrossesse {

    private int id;
    private Date dateSuivi;
    private double poids;
    private double tension;
    private String symptomes;
    private String etatGrossesse;
    private Integer patientId; // Nouvel attribut

    // --- Constructeurs ---

    // Constructeur vide
    public suiviGrossesse() {}

    // Constructeur pour l'insertion (sans id car auto-incrémenté)
    public suiviGrossesse(Date dateSuivi, double poids, double tension, String symptomes, String etatGrossesse, Integer patientId) {
        this.dateSuivi = dateSuivi;
        this.poids = poids;
        this.tension = tension;
        this.symptomes = symptomes;
        this.etatGrossesse = etatGrossesse;
        this.patientId = patientId;
    }

    // Constructeur complet (avec id, utile pour la récupération depuis la base)
    public suiviGrossesse(int id, Date dateSuivi, double poids, double tension, String symptomes, String etatGrossesse, Integer patientId) {
        this.id = id;
        this.dateSuivi = dateSuivi;
        this.poids = poids;
        this.tension = tension;
        this.symptomes = symptomes;
        this.etatGrossesse = etatGrossesse;
        this.patientId = patientId;
    }

    // --- Getters et Setters ---

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

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    // --- toString() ---
    @Override
    public String toString() {
        return "SuiviGrossesse{" +
                "id=" + id +
                ", dateSuivi=" + dateSuivi +
                ", poids=" + poids +
                ", tension=" + tension +
                ", symptomes='" + symptomes + '\'' +
                ", etatGrossesse='" + etatGrossesse + '\'' +
                ", patientId=" + patientId +
                '}';
    }
}
