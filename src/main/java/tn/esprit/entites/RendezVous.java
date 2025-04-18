package tn.esprit.entites;

import java.time.LocalDate;

public class RendezVous {
    private int id;
    private Disponibilite disponibilite;
    private String motif;
    private String symptomes;
    private String traitementEnCours;
    private String notes;
    private String statutRendezVous;
    private LocalDate creation;
    private String heureString;
    private LocalDate jour;
    private User medecin;
    private int patientId;
public RendezVous(){}
    public RendezVous(int id, Disponibilite disponibilite, String motif, String symptomes, String traitementEnCours, String notes, String statutRendezVous, LocalDate creation, String heureString, LocalDate jour, User medecin, int patientId) {
        this.id = id;
        this.disponibilite = disponibilite;
        this.motif = motif;
        this.symptomes = symptomes;
        this.traitementEnCours = traitementEnCours;
        this.notes = notes;
        this.statutRendezVous = statutRendezVous;
        this.creation = creation;
        this.heureString = heureString;
        this.jour = jour;
        this.medecin = medecin;
        this.patientId = patientId;
    }

    public RendezVous(Disponibilite disponibilite, String motif, String symptomes, String traitementEnCours, String notes, String statutRendezVous, LocalDate creation, String heureString, LocalDate jour, User medecin, int patientId) {
        this.disponibilite = disponibilite;
        this.motif = motif;
        this.symptomes = symptomes;
        this.traitementEnCours = traitementEnCours;
        this.notes = notes;
        this.statutRendezVous = statutRendezVous;
        this.creation = creation;
        this.heureString = heureString;
        this.jour = jour;
        this.medecin = medecin;
        this.patientId = patientId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Disponibilite getDisponibilite() {
        return disponibilite;
    }

    public void setDisponibilite(Disponibilite disponibilite) {
        this.disponibilite = disponibilite;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getSymptomes() {
        return symptomes;
    }

    public void setSymptomes(String symptomes) {
        this.symptomes = symptomes;
    }

    public String getTraitementEnCours() {
        return traitementEnCours;
    }

    public void setTraitementEnCours(String traitementEnCours) {
        this.traitementEnCours = traitementEnCours;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatutRendezVous() {
        return statutRendezVous;
    }

    public void setStatutRendezVous(String statutRendezVous) {
        this.statutRendezVous = statutRendezVous;
    }

    public LocalDate getCreation() {
        return creation;
    }

    public void setCreation(LocalDate creation) {
        this.creation = creation;
    }

    public String getHeureString() {
        return heureString;
    }

    public void setHeureString(String heureString) {
        this.heureString = heureString;
    }

    public LocalDate getJour() {
        return jour;
    }

    public void setJour(LocalDate jour) {
        this.jour = jour;
    }

    public User getMedecin() {
        return medecin;
    }

    public void setMedecin(User medecin) {
        this.medecin = medecin;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId (int patientId) {
        this.patientId = patientId;
    }
}