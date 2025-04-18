package tn.esprit.tests;

import tn.esprit.entities.Ordonnance;
import tn.esprit.entities.Traitement;
import tn.esprit.services.OrdonnanceService;
import tn.esprit.services.TraitementServices;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        OrdonnanceService os = new OrdonnanceService();
        Ordonnance o = new Ordonnance("Doliprane", "2 par jour", "2025-03-25");

        try {
            // ➕ Ajouter une ordonnance
            os.ajouter(o);
            System.out.println("Ordonnance ajoutée avec ID: " + o.getId());

            // ✏️ Modifier l'ordonnance (par exemple changer la date)
            o.setDatePrescription("2025-04-10");  // Update the date
            os.modifier(o);  // Pass the updated Ordonnance object

            // 📄 Afficher les ordonnances
            List<Ordonnance> ordList = os.recuperer();
            System.out.println("Liste des ordonnances:");
            for (Ordonnance ord : ordList) {
                System.out.println(ord);
            }

            // ❌ Supprimer l'ordonnance (décommente si nécessaire)
            // os.supprimer(o);

        } catch (SQLException e) {
            System.out.println("Erreur Ordonnance: " + e.getMessage());
        }

        TraitementServices ts = new TraitementServices();
        LocalDate date = LocalDate.of(2025, 4, 9);
        Traitement t = new Traitement(o.getId(), date, "Casse à la main");

        try {
            // ➕ Ajouter un traitement
            ts.ajouter(t);
            System.out.println("Traitement ajouté !");

            // ✏️ Modifier le traitement (pass the full Traitement object)
            t.setDatePrescription(LocalDate.of(2025, 4, 14));  // Update the datePrescription using the setter method
            ts.modifier(t);  // Pass the updated Traitement object

            // 📄 Afficher les traitements
            List<Traitement> traitements = ts.recuperer();
            System.out.println("Liste des traitements:");
            for (Traitement tr : traitements) {
                System.out.println(tr);
            }

        } catch (SQLException e) {
            System.out.println("Erreur Traitement: " + e.getMessage());
        }
    }
}
