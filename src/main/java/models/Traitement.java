package models;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Traitement {
    private int id;
    private int ordonnanceId;
    private LocalDate datePrescription;
    private String historiqueTraitement;
    private int patientId; // Foreign key: references user.id (patient)

    // Constructors
    public Traitement() {
        // No-argument constructor
    }

    // For adding new records
    public Traitement(int ordonnanceId, LocalDate datePrescription, String historiqueTraitement, int patientId) {
        this.ordonnanceId = ordonnanceId;
        this.datePrescription = datePrescription;
        this.historiqueTraitement = historiqueTraitement;
        this.patientId = patientId;
    }

    // For updating full data
    public Traitement(int id, int ordonnanceId, LocalDate datePrescription, String historiqueTraitement, int patientId) {
        this.id = id;
        this.ordonnanceId = ordonnanceId;
        this.datePrescription = datePrescription;
        this.historiqueTraitement = historiqueTraitement;
        this.patientId = patientId;
    }

    // For data where date is a String
    public Traitement(int id, int ordonnanceId, String datePrescription, String historiqueTraitement, int patientId) {
        this.id = id;
        this.ordonnanceId = ordonnanceId;
        try {
            this.datePrescription = LocalDate.parse(datePrescription); // String → LocalDate
        } catch (DateTimeParseException e) {
            System.out.println("⚠️ Format de date invalide : " + datePrescription + ". Utilisez AAAA-MM-JJ.");
            this.datePrescription = null;
        }
        this.historiqueTraitement = historiqueTraitement;
        this.patientId = patientId;
    }

    // Getters & Setters
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

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    @Override
    public String toString() {
        return "Traitement{" +
                "id=" + id +
                ", ordonnanceId=" + ordonnanceId +
                ", datePrescription=" + datePrescription +
                ", historiqueTraitement='" + historiqueTraitement + '\'' +
                ", patientId=" + patientId +
                '}';
    }
}
