package tn.esprit.entites;

import java.time.LocalDate;
import java.util.List;

public class Disponibilite {
    private int id;
    private LocalDate jour;
    private List<String> heuresDisp;
    private String statutDisp;
    private int idMedecin;


    public LocalDate getJour() {
        return jour;
    }

    public void setJour(LocalDate jour) {
        this.jour = jour;
    }

    public List<String> getHeuresDisp() {
        return heuresDisp;
    }

    public void setHeuresDisp(List<String> heuresDisp) {
        this.heuresDisp = heuresDisp;
    }

    public String getStatutDisp() {
        return statutDisp;
    }

    public void setStatutDisp(String statutDisp) {
        this.statutDisp = statutDisp;
    }

    public int getIdMedecin() {
        return idMedecin;
    }

    public void setIdMedecin(int idMedecin) {
        this.idMedecin = idMedecin;
    }
    public int getId() {
        return id;

    }
    public void setId(int id) {
        this.id = id;

    }

    public Disponibilite(LocalDate jour, List<String> heuresDisp, String statutDisp, int idMedecin) {
        this.jour = jour;
        this.heuresDisp = heuresDisp;
        this.statutDisp = statutDisp;
        this.idMedecin = idMedecin;
    }
    public Disponibilite() {

    }

    public Disponibilite(int id, LocalDate jour, List<String> heuresDisp, String statutDisp, int idMedecin) {

        this.id = id;
        this.jour = jour;
        this.heuresDisp = heuresDisp;
        this.statutDisp = statutDisp;
        this.idMedecin = idMedecin;
    }

}


