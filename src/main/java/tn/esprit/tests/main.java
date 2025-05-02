package tn.esprit.tests;

import tn.esprit.entites.Disponibilite;
import tn.esprit.services.DisponibiliteService;
import tn.esprit.services.RappelAutomatiqueService;
import tn.esprit.tools.MyDataBase;

import java.time.LocalDate;
import java.util.Arrays;
import java.sql.Connection;

public class main {
    public static void main(String[] args) {
        try {
            // Connexion à la base
            Connection cnx ;
            cnx= MyDataBase.getInstance().getCnx();

            // Créer l'instance de RappelService
            RappelAutomatiqueService rappelService = new RappelAutomatiqueService();

            // ID du rendez-vous à rappeler
            int idRendezVous = 47; // <-- Remplace avec un ID existant dans ta base

            // Tester l'envoi
            boolean succes = rappelService.envoyerRappelManuel(idRendezVous);

            if (succes) {
                System.out.println("Rappel manuel envoyé avec succès !");
            } else {
                System.out.println("Échec lors de l'envoi du rappel manuel.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
