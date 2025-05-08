package tn.esprit.entities;

public class Ordonnance {
    private int id;
    private String medicament;
    private String posologie;
    private String datePrescription;
    private int patientId;
    private String patientName; // Patient's name, will be fetched from the User entity

    // Constructors
    public Ordonnance() {}

    // New constructor with 3 parameters
    public Ordonnance(String medicament, String posologie, String datePrescription) {
        this.medicament = medicament;
        this.posologie = posologie;
        this.datePrescription = datePrescription;
        this.patientName = "Unknown"; // Default value or can be set later
    }

    public Ordonnance(String medicament, String posologie, String datePrescription, String patientName) {
        this.medicament = medicament;
        this.posologie = posologie;
        this.datePrescription = datePrescription;
        this.patientName = patientName;
    }

    public Ordonnance(int id, String medicament, String posologie, String datePrescription, String patientName) {
        this.id = id;
        this.medicament = medicament;
        this.posologie = posologie;
        this.datePrescription = datePrescription;
        this.patientName = patientName;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMedicament() { return medicament; }
    public void setMedicament(String medicament) { this.medicament = medicament; }

    public String getPosologie() { return posologie; }
    public void setPosologie(String posologie) { this.posologie = posologie; }

    public String getDatePrescription() { return datePrescription; }
    public void setDatePrescription(String datePrescription) { this.datePrescription = datePrescription; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    @Override
    public String toString() {
        return "Ordonnance{" +
                "id=" + id +
                ", medicament='" + medicament + '\'' +
                ", posologie='" + posologie + '\'' +
                ", datePrescription='" + datePrescription + '\'' +
                ", patientName='" + patientName + '\'' +
                '}';
    }
}
