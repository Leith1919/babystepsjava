package tn.esprit.services;

import tn.esprit.tools.MyDataBase;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StatistiquesService {
    private final Connection cnx;

    public StatistiquesService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    // 1. Nombre de rendez-vous par période
    public Map<String, Integer> getRendezVousParPeriode(LocalDate debut, LocalDate fin, String periode) {
        Map<String, Integer> resultats = new LinkedHashMap<>();
        String groupFormat;

        switch(periode.toLowerCase()) {
            case "jour":
                groupFormat = "%Y-%m-%d";
                break;
            case "semaine":
                groupFormat = "%Y-%u"; // Format année-numéro de semaine
                break;
            case "mois":
                groupFormat = "%Y-%m";
                break;
            default:
                groupFormat = "%Y-%m-%d";
        }

        String sql = "SELECT DATE_FORMAT(jour, ?) as periode, COUNT(*) as total " +
                "FROM rendez_vous WHERE jour BETWEEN ? AND ? " +
                "GROUP BY periode ORDER BY MIN(jour)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, groupFormat);
            ps.setDate(2, Date.valueOf(debut));
            ps.setDate(3, Date.valueOf(fin));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String periodeName = rs.getString("periode");
                    int total = rs.getInt("total");
                    resultats.put(periodeName, total);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des rendez-vous par période: " + e.getMessage());
            e.printStackTrace();
        }

        return resultats;
    }

    // 2. Performance des médecins
    public Map<String, Integer> getRendezVousParMedecin(LocalDate debut, LocalDate fin) {
        Map<String, Integer> resultats = new LinkedHashMap<>();

        String sql = "SELECT u.nom, u.prenom, COUNT(rv.id) as total " +
                "FROM rendez_vous rv " +
                "JOIN user u ON rv.id_medecin_id = u.id " +
                "WHERE rv.jour BETWEEN ? AND ? " +
                "GROUP BY u.id ORDER BY total DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(debut));
            ps.setDate(2, Date.valueOf(fin));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nomMedecin = rs.getString("nom") + " " + rs.getString("prenom");
                    int total = rs.getInt("total");
                    resultats.put(nomMedecin, total);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des rendez-vous par médecin: " + e.getMessage());
            e.printStackTrace();
        }

        return resultats;
    }

    // 3. Taux d'occupation par plages horaires
    public Map<String, Map<String, Integer>> getTauxOccupationPlages(LocalDate debut, LocalDate fin) {
        Map<String, Map<String, Integer>> resultats = new LinkedHashMap<>();
        String[] joursMap = {"Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};

        // Initialiser la structure
        for (String jour : joursMap) {
            Map<String, Integer> plages = new LinkedHashMap<>();
            plages.put("9-11", 0);
            plages.put("11-13", 0);
            plages.put("14-16", 0);
            plages.put("16-18", 0);
            resultats.put(jour, plages);
        }

        String sql = "SELECT DAYOFWEEK(jour) as jour_semaine, heure_string, COUNT(*) as total " +
                "FROM rendez_vous " +
                "WHERE jour BETWEEN ? AND ? " +
                "GROUP BY jour_semaine, heure_string";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(debut));
            ps.setDate(2, Date.valueOf(fin));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int jourSemaine = rs.getInt("jour_semaine"); // 1 = Dimanche, 2 = Lundi, etc.
                    String heure = rs.getString("heure_string");
                    int total = rs.getInt("total");

                    String jourNom = joursMap[jourSemaine - 1];
                    resultats.get(jourNom).put(heure, total);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des taux d'occupation: " + e.getMessage());
            e.printStackTrace();
        }

        return resultats;
    }

    // 4. Motifs de consultation les plus fréquents
    public Map<String, Integer> getMotifsFrequents(LocalDate debut, LocalDate fin, int limit) {
        Map<String, Integer> resultats = new LinkedHashMap<>();

        String sql = "SELECT motif, COUNT(*) as total " +
                "FROM rendez_vous " +
                "WHERE jour BETWEEN ? AND ? " +
                "GROUP BY motif ORDER BY total DESC LIMIT ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(debut));
            ps.setDate(2, Date.valueOf(fin));
            ps.setInt(3, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String motif = rs.getString("motif");
                    int total = rs.getInt("total");
                    resultats.put(motif, total);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des motifs fréquents: " + e.getMessage());
            e.printStackTrace();
        }

        return resultats;
    }

    // 5. Assiduité des patients
    public Map<String, Integer> getStatutRendezVous(LocalDate debut, LocalDate fin) {
        Map<String, Integer> resultats = new HashMap<>();
        resultats.put("Honoré", 0);
        resultats.put("Manqué", 0);
        resultats.put("Reporté", 0);
        resultats.put("Annulé", 0);

        String sql = "SELECT statut_rendez_vous, COUNT(*) as total " +
                "FROM rendez_vous " +
                "WHERE jour BETWEEN ? AND ? " +
                "GROUP BY statut_rendez_vous";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(debut));
            ps.setDate(2, Date.valueOf(fin));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String statut = rs.getString("statut_rendez_vous");
                    int total = rs.getInt("total");

                    // Mapper les statuts aux catégories principales
                    if (statut.contains("Honoré") || statut.contains("Terminé") || statut.equals("Confirmé")) {
                        resultats.put("Honoré", resultats.get("Honoré") + total);
                    } else if (statut.contains("Manqué") || statut.contains("Absent")) {
                        resultats.put("Manqué", resultats.get("Manqué") + total);
                    } else if (statut.contains("Report")) {
                        resultats.put("Reporté", resultats.get("Reporté") + total);
                    } else if (statut.contains("Annul")) {
                        resultats.put("Annulé", resultats.get("Annulé") + total);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des statuts: " + e.getMessage());
            e.printStackTrace();
        }

        return resultats;
    }

    // 6. Délai entre prise de rendez-vous et consultation
    public Map<String, Integer> getDelaiRendezVous(LocalDate debut, LocalDate fin) {
        Map<String, Integer> resultats = new LinkedHashMap<>();
        resultats.put("0-1 jour", 0);
        resultats.put("2-3 jours", 0);
        resultats.put("4-7 jours", 0);
        resultats.put("1-2 semaines", 0);
        resultats.put("Plus de 2 semaines", 0);

        String sql = "SELECT DATEDIFF(jour, creation) as delai, COUNT(*) as total " +
                "FROM rendez_vous " +
                "WHERE jour BETWEEN ? AND ? " +
                "GROUP BY delai";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(debut));
            ps.setDate(2, Date.valueOf(fin));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int delai = rs.getInt("delai");
                    int total = rs.getInt("total");

                    if (delai <= 1) {
                        resultats.put("0-1 jour", resultats.get("0-1 jour") + total);
                    } else if (delai <= 3) {
                        resultats.put("2-3 jours", resultats.get("2-3 jours") + total);
                    } else if (delai <= 7) {
                        resultats.put("4-7 jours", resultats.get("4-7 jours") + total);
                    } else if (delai <= 14) {
                        resultats.put("1-2 semaines", resultats.get("1-2 semaines") + total);
                    } else {
                        resultats.put("Plus de 2 semaines", resultats.get("Plus de 2 semaines") + total);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des délais: " + e.getMessage());
            e.printStackTrace();
        }

        return resultats;
    }
}