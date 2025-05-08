package controllers.User;

import models.suiviGrossesse;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AnomalyDetector {

    private static final double MIN_TENSION_NORMAL = 90.0;
    private static final double MAX_TENSION_NORMAL = 140.0;
    private static final double MIN_POIDS_GAIN_WEEKLY = 0.2;
    private static final double MAX_POIDS_GAIN_WEEKLY = 0.5;
    private static final double MIN_POIDS_GAIN_MONTHLY = 1.0;
    private static final double MAX_POIDS_GAIN_MONTHLY = 2.0;

    public enum RiskLevel {
        NORMAL("Normal", "#28a745"),
        WARNING("À surveiller", "#ffc107"),
        HIGH_RISK("Risque élevé", "#dc3545");

        private final String label;
        private final String color;

        RiskLevel(String label, String color) {
            this.label = label;
            this.color = color;
        }

        public String getLabel() { return label; }
        public String getColor() { return color; }
    }

    public static class AnalysisResult {
        private RiskLevel riskLevel = RiskLevel.NORMAL;
        private double confidence = 0.0;
        private final Map<String, String> anomalies = new HashMap<>();
        private final List<String> recommendations = new ArrayList<>();
        private final Map<String, Double> trends = new HashMap<>();
        private final LocalDate lastAnalysisDate = LocalDate.now();

        public RiskLevel getRiskLevel() { return riskLevel; }
        public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public Map<String, String> getAnomalies() { return anomalies; }
        public void addAnomaly(String param, String desc) { anomalies.put(param, desc); }
        public List<String> getRecommendations() { return recommendations; }
        public void addRecommendation(String rec) { recommendations.add(rec); }
        public int getAnomalyCount() { return anomalies.size(); }
        public Map<String, Double> getTrends() { return trends; }
        public void addTrend(String param, double value) { trends.put(param, value); }
        public LocalDate getLastAnalysisDate() { return lastAnalysisDate; }
    }

    public static AnalysisResult analyzeSuivi(suiviGrossesse suivi, List<suiviGrossesse> previousSuivis) {
        AnalysisResult result = new AnalysisResult();
        analyzeLongTermTrends(previousSuivis, result);
        analyzeCurrentSuivi(suivi, previousSuivis, result);
        updateRiskLevel(result);
        generateRecommendations(result);
        return result;
    }

    private static void analyzeLongTermTrends(List<suiviGrossesse> previousSuivis, AnalysisResult result) {
        if (previousSuivis == null || previousSuivis.isEmpty()) return;

        previousSuivis.sort(Comparator.comparing(s -> toLocalDate(s.getDateSuivi())));

        LocalDate currentDate = LocalDate.now();
        LocalDate threeMonthsAgo = currentDate.minusMonths(3);

        List<suiviGrossesse> recentSuivis = previousSuivis.stream()
                .filter(s -> toLocalDate(s.getDateSuivi()).isAfter(threeMonthsAgo))
                .toList();

        if (recentSuivis.size() >= 2) {
            analyzeWeightTrend(recentSuivis, result);
            analyzeTensionTrend(recentSuivis, result);
        }
    }

    private static void analyzeWeightTrend(List<suiviGrossesse> recentSuivis, AnalysisResult result) {
        double totalWeightGain = 0;
        int monthCount = 0;
        LocalDate lastDate = null;

        for (int i = 1; i < recentSuivis.size(); i++) {
            LocalDate currentDate = toLocalDate(recentSuivis.get(i).getDateSuivi());
            LocalDate previousDate = toLocalDate(recentSuivis.get(i - 1).getDateSuivi());
            long monthsBetween = ChronoUnit.MONTHS.between(previousDate, currentDate);

            if (monthsBetween > 0) {
                double monthlyGain = (recentSuivis.get(i).getPoids() - recentSuivis.get(i - 1).getPoids()) / monthsBetween;
                totalWeightGain += monthlyGain;
                monthCount++;
            }
            lastDate = currentDate;
        }

        if (monthCount > 0) {
            double avgMonthlyGain = totalWeightGain / monthCount;
            result.addTrend("Poids", avgMonthlyGain);

            if (avgMonthlyGain > MAX_POIDS_GAIN_MONTHLY) {
                result.addAnomaly("Poids", "Gain de poids mensuel excessif : " + String.format("%.2f", avgMonthlyGain) + " kg/mois");
                result.setConfidence(Math.max(result.getConfidence(), 0.85));
            } else if (avgMonthlyGain < MIN_POIDS_GAIN_MONTHLY) {
                result.addAnomaly("Poids", "Gain de poids mensuel insuffisant : " + String.format("%.2f", avgMonthlyGain) + " kg/mois");
                result.setConfidence(Math.max(result.getConfidence(), 0.80));
            }
        }
    }

    private static void analyzeTensionTrend(List<suiviGrossesse> suivis, AnalysisResult result) {
        double totalTension = 0, max = 0, min = Double.MAX_VALUE;
        int count = 0;

        for (suiviGrossesse s : suivis) {
            double t = s.getTension();
            totalTension += t;
            max = Math.max(max, t);
            min = Math.min(min, t);
            count++;
        }

        if (count > 0) {
            double avg = totalTension / count;
            result.addTrend("Tension", avg);

            if (max > MAX_TENSION_NORMAL) {
                result.addAnomaly("Tension", "Tendance à l'hypertension (moyenne " + String.format("%.2f", avg) + ")");
                result.setConfidence(Math.max(result.getConfidence(), 0.90));
            } else if (min < MIN_TENSION_NORMAL) {
                result.addAnomaly("Tension", "Tendance à l'hypotension (moyenne " + String.format("%.2f", avg) + ")");
                result.setConfidence(Math.max(result.getConfidence(), 0.85));
            }
        }
    }

    private static void analyzeCurrentSuivi(suiviGrossesse suivi, List<suiviGrossesse> previousSuivis, AnalysisResult result) {
        analyzeCurrentTension(suivi, result);
        if (!previousSuivis.isEmpty()) analyzeCurrentWeight(suivi, previousSuivis, result);
        analyzeCurrentSymptoms(suivi, result);
    }

    private static void analyzeCurrentTension(suiviGrossesse suivi, AnalysisResult result) {
        double t = suivi.getTension();
        if (t > MAX_TENSION_NORMAL) {
            result.addAnomaly("Tension", "Hypertension détectée : " + t + " mmHg");
            result.setConfidence(Math.max(result.getConfidence(), 0.95));
        } else if (t < MIN_TENSION_NORMAL) {
            result.addAnomaly("Tension", "Hypotension détectée : " + t + " mmHg");
            result.setConfidence(Math.max(result.getConfidence(), 0.90));
        }
    }

    private static void analyzeCurrentWeight(suiviGrossesse suivi, List<suiviGrossesse> previousSuivis, AnalysisResult result) {
        previousSuivis.sort(Comparator.comparing(s -> toLocalDate(s.getDateSuivi())));
        suiviGrossesse lastSuivi = previousSuivis.get(previousSuivis.size() - 1);

        double currentPoids = suivi.getPoids();
        double lastPoids = lastSuivi.getPoids();
        double diff = currentPoids - lastPoids;

        long weeks = ChronoUnit.WEEKS.between(
                toLocalDate(lastSuivi.getDateSuivi()),
                toLocalDate(suivi.getDateSuivi())
        );

        weeks = Math.max(weeks, 1);
        double weeklyGain = diff / weeks;

        if (weeklyGain > MAX_POIDS_GAIN_WEEKLY) {
            result.addAnomaly("Poids", "Gain de poids rapide : " + String.format("%.2f", weeklyGain) + " kg/semaine");
            result.setConfidence(Math.max(result.getConfidence(), 0.85));
        } else if (weeklyGain < MIN_POIDS_GAIN_WEEKLY && weeklyGain >= 0) {
            result.addAnomaly("Poids", "Gain de poids insuffisant : " + String.format("%.2f", weeklyGain) + " kg/semaine");
            result.setConfidence(Math.max(result.getConfidence(), 0.80));
        }
    }

    private static void analyzeCurrentSymptoms(suiviGrossesse suivi, AnalysisResult result) {
        String symptomes = suivi.getSymptomes();
        if (symptomes != null && symptomes.toLowerCase().contains("saignement")) {
            result.addAnomaly("Symptômes", "Saignement détecté, nécessite une consultation urgente.");
            result.setConfidence(Math.max(result.getConfidence(), 0.95));
        }
    }

    private static void updateRiskLevel(AnalysisResult result) {
        if (result.getConfidence() >= 0.9 || result.getAnomalyCount() >= 3) {
            result.setRiskLevel(RiskLevel.HIGH_RISK);
        } else if (result.getConfidence() >= 0.7 || result.getAnomalyCount() >= 1) {
            result.setRiskLevel(RiskLevel.WARNING);
        } else {
            result.setRiskLevel(RiskLevel.NORMAL);
        }
    }

    private static void generateRecommendations(AnalysisResult result) {
        for (String anomaly : result.getAnomalies().keySet()) {
            switch (anomaly) {
                case "Tension" -> result.addRecommendation("Consultez votre médecin pour un contrôle de la tension.");
                case "Poids" -> result.addRecommendation("Adoptez une alimentation équilibrée adaptée à la grossesse.");
                case "Symptômes" -> result.addRecommendation("Rendez-vous immédiatement aux urgences.");
            }
        }
        if (result.getAnomalies().isEmpty()) {
            result.addRecommendation("Poursuivez vos habitudes actuelles et continuez le suivi régulier.");
        }
    }

    private static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return LocalDate.now();
        }

        // Pour java.sql.Date
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }

        // Pour java.util.Date
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
