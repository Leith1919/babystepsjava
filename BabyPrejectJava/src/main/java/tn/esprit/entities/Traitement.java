package tn.esprit.entities;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Traitement {
    private int id;
    private int ordonnanceId;
    private LocalDate datePrescription;
    private String historiqueTraitement;

    // Constructor for creating without ID (e.g., when adding a new record)
    public Traitement(int ordonnanceId, LocalDate datePrescription, String historiqueTraitement) {
        this.ordonnanceId = ordonnanceId;
        this.datePrescription = datePrescription;
        this.historiqueTraitement = historiqueTraitement;
    }

    // Constructor for updating with full data
    public Traitement(int id, int ordonnanceId, LocalDate datePrescription, String historiqueTraitement) {
        this.id = id;
        this.ordonnanceId = ordonnanceId;
        this.datePrescription = datePrescription;
        this.historiqueTraitement = historiqueTraitement;
    }

    public Traitement() {
        // No-argument constructor
    }


    // ✅ New constructor that accepts date as a String
    public Traitement(int id, int ordonnanceId, String datePrescription, String historiqueTraitement) {
        this.id = id;
        this.ordonnanceId = ordonnanceId;
        try {
            this.datePrescription = LocalDate.parse(datePrescription); // String → LocalDate
        } catch (DateTimeParseException e) {
            System.out.println("⚠️ Format de date invalide : " + datePrescription + ". Utilisez AAAA-MM-JJ.");
            this.datePrescription = null; // or set a default like LocalDate.now();
        }
        this.historiqueTraitement = historiqueTraitement;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrdonnanceId() {
        return ordonnanceId;
    }

    public void setOrdonnanceId(int ordonnanceId) {
        this.ordonnanceId = ordonnanceId;
    }

    public LocalDate getDatePrescription() {
        return datePrescription;
    }

    public void setDatePrescription(LocalDate datePrescription) {
        this.datePrescription = datePrescription;
    }

    public String getHistoriqueTraitement() {
        return historiqueTraitement;
    }

    public void setHistoriqueTraitement(String historiqueTraitement) {
        this.historiqueTraitement = historiqueTraitement;
    }

    @Override
    public String toString() {
        return "Traitement{" +
                "id=" + id +
                ", ordonnanceId=" + ordonnanceId +
                ", datePrescription=" + datePrescription +
                ", historiqueTraitement='" + historiqueTraitement + '\'' +
                '}';
    }
}
