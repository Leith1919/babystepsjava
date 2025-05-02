package tn.esprit.Controllers;

import tn.esprit.entities.suiviGrossesse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class PregnancyMLAnalyzer {
    
    private static final int PREDICTION_WINDOW = 4; // weeks
    private static final double WEIGHT_TREND_THRESHOLD = 0.3;
    private static final double TENSION_TREND_THRESHOLD = 5.0;
    
    // Normes de poids selon le trimestre
    private static final double[] TRIMESTER_WEIGHT_GAIN = {1.0, 4.0, 5.0}; // kg par trimestre
    private static final double[] TRIMESTER_WEIGHT_RATE = {0.2, 0.4, 0.5}; // kg/semaine
    
    public static class MLPrediction {
        private final Map<String, Double> riskProbabilities;
        private final Map<String, String> predictions;
        private final List<String> recommendations;
        private final Map<String, Double> statistics;
        private final Map<String, List<Double>> trendData;
        
        public MLPrediction() {
            riskProbabilities = new HashMap<>();
            predictions = new HashMap<>();
            recommendations = new ArrayList<>();
            statistics = new HashMap<>();
            trendData = new HashMap<>();
        }
        
        public Map<String, Double> getRiskProbabilities() { return riskProbabilities; }
        public Map<String, String> getPredictions() { return predictions; }
        public List<String> getRecommendations() { return recommendations; }
        public Map<String, Double> getStatistics() { return statistics; }
        public Map<String, List<Double>> getTrendData() { return trendData; }
    }
    
    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return LocalDate.now();
        }
        
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
    
    public static MLPrediction analyzePregnancyData(List<suiviGrossesse> suivis) {
        MLPrediction prediction = new MLPrediction();
        
        if (suivis == null || suivis.size() < 2) {
            prediction.predictions.put("Statut", "Données insuffisantes pour l'analyse");
            return prediction;
        }
        
        // Trier les suivis par date
        suivis.sort(Comparator.comparing(s -> toLocalDate(s.getDateSuivi())));
        
        // Calculer le trimestre actuel
        int currentTrimester = calculateTrimester(suivis);
        prediction.predictions.put("Trimestre", "Trimestre " + currentTrimester);
        
        // Analyser les tendances
        analyzeWeightTrend(suivis, prediction, currentTrimester);
        analyzeTensionTrend(suivis, prediction);
        analyzeSymptomPatterns(suivis, prediction);
        
        // Calculer les statistiques
        calculateStatistics(suivis, prediction);
        
        // Calculer les probabilités de risque
        calculateRiskProbabilities(prediction, currentTrimester);
        
        // Générer des recommandations
        generateRecommendations(prediction, currentTrimester);
        
        return prediction;
    }
    
    private static int calculateTrimester(List<suiviGrossesse> suivis) {
        LocalDate firstDate = toLocalDate(suivis.get(0).getDateSuivi());
        LocalDate lastDate = toLocalDate(suivis.get(suivis.size() - 1).getDateSuivi());
        long weeks = ChronoUnit.WEEKS.between(firstDate, lastDate);
        
        if (weeks < 13) return 1;
        if (weeks < 27) return 2;
        return 3;
    }
    
    private static void analyzeWeightTrend(List<suiviGrossesse> suivis, MLPrediction prediction, int trimester) {
        double[] weights = suivis.stream()
                .mapToDouble(suiviGrossesse::getPoids)
                .toArray();
        
        // Calculer la tendance linéaire
        double trend = calculateLinearTrend(weights);
        prediction.predictions.put("Tendance poids", String.format("%.2f kg/semaine", trend));
        
        // Prédire le poids dans 4 semaines
        double lastWeight = weights[weights.length - 1];
        double predictedWeight = lastWeight + (trend * PREDICTION_WINDOW);
        prediction.predictions.put("Poids prédit", String.format("%.2f kg dans %d semaines", predictedWeight, PREDICTION_WINDOW));
        
        // Comparer avec les normes du trimestre
        double expectedGain = TRIMESTER_WEIGHT_RATE[trimester - 1];
        double deviation = Math.abs(trend - expectedGain);
        
        if (deviation > WEIGHT_TREND_THRESHOLD) {
            prediction.riskProbabilities.put("Poids", 0.7);
            prediction.predictions.put("Écart poids", String.format("%.2f kg/semaine par rapport à la norme", deviation));
        }
        
        // Stocker les données de tendance
        List<Double> weightTrend = new ArrayList<>();
        for (int i = 0; i < weights.length; i++) {
            weightTrend.add(weights[i]);
        }
        prediction.trendData.put("Poids", weightTrend);
    }
    
    private static void analyzeTensionTrend(List<suiviGrossesse> suivis, MLPrediction prediction) {
        double[] tensions = suivis.stream()
                .mapToDouble(suiviGrossesse::getTension)
                .toArray();
        
        // Calculer la tendance linéaire
        double trend = calculateLinearTrend(tensions);
        prediction.predictions.put("Tendance tension", String.format("%.2f mmHg/semaine", trend));
        
        // Prédire la tension dans 4 semaines
        double lastTension = tensions[tensions.length - 1];
        double predictedTension = lastTension + (trend * PREDICTION_WINDOW);
        prediction.predictions.put("Tension prédite", String.format("%.2f mmHg dans %d semaines", predictedTension, PREDICTION_WINDOW));
        
        if (Math.abs(trend) > TENSION_TREND_THRESHOLD) {
            prediction.riskProbabilities.put("Tension", 0.8);
        }
        
        // Stocker les données de tendance
        List<Double> tensionTrend = new ArrayList<>();
        for (int i = 0; i < tensions.length; i++) {
            tensionTrend.add(tensions[i]);
        }
        prediction.trendData.put("Tension", tensionTrend);
    }
    
    private static void analyzeSymptomPatterns(List<suiviGrossesse> suivis, MLPrediction prediction) {
        Map<String, Integer> symptomFrequency = new HashMap<>();
        Map<String, List<LocalDate>> symptomDates = new HashMap<>();
        
        for (suiviGrossesse suivi : suivis) {
            String symptomes = suivi.getSymptomes();
            if (symptomes != null && !symptomes.isEmpty()) {
                String[] symptoms = symptomes.toLowerCase().split(",");
                for (String symptom : symptoms) {
                    symptom = symptom.trim();
                    symptomFrequency.merge(symptom, 1, Integer::sum);
                    symptomDates.computeIfAbsent(symptom, k -> new ArrayList<>())
                            .add(toLocalDate(suivi.getDateSuivi()));
                }
            }
        }
        
        // Identifier les symptômes récurrents et leur fréquence
        symptomFrequency.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .forEach(entry -> {
                    String symptom = entry.getKey();
                    int frequency = entry.getValue();
                    List<LocalDate> dates = symptomDates.get(symptom);
                    long daysBetween = ChronoUnit.DAYS.between(dates.get(0), dates.get(dates.size() - 1));
                    
                    prediction.predictions.put("Symptôme récurrent", 
                        String.format("%s (apparu %d fois en %d jours)", symptom, frequency, daysBetween));
                    prediction.riskProbabilities.put("Symptômes", 0.6);
                });
    }
    
    private static void calculateStatistics(List<suiviGrossesse> suivis, MLPrediction prediction) {
        // Statistiques de poids
        double[] weights = suivis.stream()
                .mapToDouble(suiviGrossesse::getPoids)
                .toArray();
        prediction.statistics.put("Poids moyen", calculateMean(weights));
        prediction.statistics.put("Écart-type poids", calculateStandardDeviation(weights));
        
        // Statistiques de tension
        double[] tensions = suivis.stream()
                .mapToDouble(suiviGrossesse::getTension)
                .toArray();
        prediction.statistics.put("Tension moyenne", calculateMean(tensions));
        prediction.statistics.put("Écart-type tension", calculateStandardDeviation(tensions));
        
        // Fréquence des suivis
        LocalDate firstDate = toLocalDate(suivis.get(0).getDateSuivi());
        LocalDate lastDate = toLocalDate(suivis.get(suivis.size() - 1).getDateSuivi());
        long totalDays = ChronoUnit.DAYS.between(firstDate, lastDate);
        double averageDaysBetween = (double) totalDays / (suivis.size() - 1);
        prediction.statistics.put("Fréquence moyenne des suivis (jours)", averageDaysBetween);
    }
    
    private static double calculateMean(double[] values) {
        return Arrays.stream(values).average().orElse(0.0);
    }
    
    private static double calculateStandardDeviation(double[] values) {
        double mean = calculateMean(values);
        double sumSquaredDiff = Arrays.stream(values)
                .map(x -> Math.pow(x - mean, 2))
                .sum();
        return Math.sqrt(sumSquaredDiff / values.length);
    }
    
    private static double calculateLinearTrend(double[] values) {
        if (values.length < 2) return 0.0;
        
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = values.length;
        
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values[i];
            sumXY += i * values[i];
            sumX2 += i * i;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        return slope;
    }
    
    private static void calculateRiskProbabilities(MLPrediction prediction, int trimester) {
        // Calculer la probabilité globale de risque
        double globalRisk = prediction.riskProbabilities.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        // Ajuster selon le trimestre
        if (trimester == 3) {
            globalRisk *= 1.2; // Augmenter la sensibilité au risque en fin de grossesse
        }
        
        prediction.riskProbabilities.put("Global", Math.min(globalRisk, 1.0));
    }
    
    private static void generateRecommendations(MLPrediction prediction, int trimester) {
        if (prediction.riskProbabilities.containsKey("Poids")) {
            prediction.recommendations.add("Surveillez votre alimentation et consultez un nutritionniste.");
        }
        
        if (prediction.riskProbabilities.containsKey("Tension")) {
            prediction.recommendations.add("Faites contrôler régulièrement votre tension artérielle.");
        }
        
        if (prediction.riskProbabilities.containsKey("Symptômes")) {
            prediction.recommendations.add("Consultez votre médecin pour les symptômes récurrents.");
        }
        
        // Recommandations spécifiques au trimestre
        switch (trimester) {
            case 1:
                prediction.recommendations.add("Privilégiez une alimentation riche en acide folique.");
                break;
            case 2:
                prediction.recommendations.add("Maintenez une activité physique modérée.");
                break;
            case 3:
                prediction.recommendations.add("Préparez-vous à l'accouchement et surveillez les signes de travail.");
                break;
        }
        
        if (prediction.riskProbabilities.getOrDefault("Global", 0.0) > 0.7) {
            prediction.recommendations.add("Un suivi médical plus rapproché est recommandé.");
        }
    }
} 