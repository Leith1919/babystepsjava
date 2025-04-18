package tn.esprit.entities;

public class Ordonnance {
    private int id;
    private String medicament;
    private String posologie;
    private String datePrescription;

    public Ordonnance() {}

    public Ordonnance(String medicament, String posologie, String datePrescription) {
        this.medicament = medicament;
        this.posologie = posologie;
        this.datePrescription = datePrescription;
    }

    public Ordonnance(int id, String medicament, String posologie, String datePrescription) {
        this.id = id;
        this.medicament = medicament;
        this.posologie = posologie;
        this.datePrescription = datePrescription;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMedicament() {
        return medicament;
    }

    public void setMedicament(String medicament) {
        this.medicament = medicament;
    }

    public String getPosologie() {
        return posologie;
    }

    public void setPosologie(String posologie) {
        this.posologie = posologie;
    }

    public String getDatePrescription() {
        return datePrescription;
    }

    public void setDatePrescription(String datePrescription) {
        this.datePrescription = datePrescription;
    }

    @Override
    public String toString() {
        return "Ordonnance{" +
                "id=" + id +
                ", medicament='" + medicament + '\'' +
                ", posologie='" + posologie + '\'' +
                ", datePrescription='" + datePrescription + '\'' +
                '}';
    }
}
