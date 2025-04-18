package tn.esprit.tests;

import tn.esprit.entites.Disponibilite;
import tn.esprit.services.DisponibiliteService;
import tn.esprit.tools.MyDataBase;

import java.time.LocalDate;
import java.util.Arrays;

public class main {
    public static void main(String[] args) {
        DisponibiliteService service = new DisponibiliteService();
        Disponibilite d= new Disponibilite(
                LocalDate.of(2025, 4, 15),
                Arrays.asList("9-11", "11-13", "13-15"),
                "disponible",
                69);
        try {
            service.ajouter(d);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }




    }
}
