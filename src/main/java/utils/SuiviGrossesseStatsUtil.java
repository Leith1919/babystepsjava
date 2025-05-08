package utils;

import models.suiviGrossesse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SuiviGrossesseStatsUtil {
    
    public static String generateStatsForPatient(List<suiviGrossesse> suivis, int patientId) {
        // Filtrer les suivis pour le patient spécifique
        List<suiviGrossesse> patientSuivis = suivis.stream()
                .filter(s -> s.getPatientId() == patientId)
                .collect(Collectors.toList());

        if (patientSuivis.isEmpty()) {
            return "Aucun suivi trouvé pour ce patient.";
        }

        StringBuilder stats = new StringBuilder();
        stats.append("=== Statistiques de suivi de grossesse ===\n\n");

        // Nombre total de suivis
        stats.append("Nombre total de suivis : ").append(patientSuivis.size()).append("\n");

        // Poids
        double poidsMoyen = patientSuivis.stream()
                .mapToDouble(suiviGrossesse::getPoids)
                .average()
                .orElse(0.0);
        double poidsMin = patientSuivis.stream()
                .mapToDouble(suiviGrossesse::getPoids)
                .min()
                .orElse(0.0);
        double poidsMax = patientSuivis.stream()
                .mapToDouble(suiviGrossesse::getPoids)
                .max()
                .orElse(0.0);
        stats.append("\n=== Poids ===\n");
        stats.append("Poids moyen : ").append(String.format("%.2f", poidsMoyen)).append(" kg\n");
        stats.append("Poids minimum : ").append(String.format("%.2f", poidsMin)).append(" kg\n");
        stats.append("Poids maximum : ").append(String.format("%.2f", poidsMax)).append(" kg\n");

        // Tension
        double tensionMoyenne = patientSuivis.stream()
                .mapToDouble(suiviGrossesse::getTension)
                .average()
                .orElse(0.0);
        double tensionMin = patientSuivis.stream()
                .mapToDouble(suiviGrossesse::getTension)
                .min()
                .orElse(0.0);
        double tensionMax = patientSuivis.stream()
                .mapToDouble(suiviGrossesse::getTension)
                .max()
                .orElse(0.0);
        stats.append("\n=== Tension ===\n");
        stats.append("Tension moyenne : ").append(String.format("%.2f", tensionMoyenne)).append("\n");
        stats.append("Tension minimum : ").append(String.format("%.2f", tensionMin)).append("\n");
        stats.append("Tension maximum : ").append(String.format("%.2f", tensionMax)).append("\n");

        // Analyse des symptômes
        Map<String, Long> symptomesFrequence = patientSuivis.stream()
                .filter(s -> s.getSymptomes() != null && !s.getSymptomes().isEmpty())
                .flatMap(s -> List.of(s.getSymptomes().split(";")).stream())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        if (!symptomesFrequence.isEmpty()) {
            stats.append("\n=== Symptômes fréquents ===\n");
            symptomesFrequence.forEach((symptome, count) -> 
                stats.append(symptome).append(" : ").append(count).append(" occurrence(s)\n"));
        }

        // Analyse de l'état de grossesse
        Map<String, Long> etatFrequence = patientSuivis.stream()
                .filter(s -> s.getEtatGrossesse() != null && !s.getEtatGrossesse().isEmpty())
                .collect(Collectors.groupingBy(suiviGrossesse::getEtatGrossesse, Collectors.counting()));

        if (!etatFrequence.isEmpty()) {
            stats.append("\n=== État de grossesse ===\n");
            etatFrequence.forEach((etat, count) -> 
                stats.append(etat).append(" : ").append(count).append(" occurrence(s)\n"));
        }

        // Recommandations basées sur les statistiques
        stats.append("\n=== Recommandations ===\n");
        if (poidsMoyen < 50) {
            stats.append("- Le poids moyen est faible, consultez un nutritionniste.\n");
        } else if (poidsMoyen > 90) {
            stats.append("- Le poids moyen est élevé, surveillez votre alimentation.\n");
        }

        if (tensionMoyenne > 140) {
            stats.append("- La tension moyenne est élevée, surveillez régulièrement votre tension.\n");
        }

        if (symptomesFrequence.containsKey("fatigue")) {
            stats.append("- La fatigue est fréquente, pensez à vous reposer davantage.\n");
        }

        return stats.toString();
    }
} 