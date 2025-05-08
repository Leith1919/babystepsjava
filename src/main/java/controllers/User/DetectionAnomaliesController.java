package controllers.User;

import javafx.animation.RotateTransition;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import models.suiviGrossesse;
import controllers.User.AnomalyDetector;
import controllers.User.AnomalyDetector.AnalysisResult;
import controllers.User.AnomalyDetector.RiskLevel;
import controllers.User.PregnancyMLAnalyzer;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.text.ParseException;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeParseException;

public class DetectionAnomaliesController implements Initializable {

    @FXML private Label patientInfoLabel;
    @FXML private Label analysisResultLabel;
    @FXML private Label riskLevelLabel;
    @FXML private Label confidenceLabel;
    @FXML private Label anomalyCountLabel;
    @FXML private Label normalCountLabel;
    @FXML private Label warningCountLabel;
    @FXML private Label riskCountLabel;
    @FXML private Label lastUpdateLabel;
    @FXML private Label etatActuelLabel;
    @FXML private Label semaineLabel;
    @FXML private Label prochainRdvLabel;
    @FXML private ProgressBar riskProgressBar;

    @FXML private LineChart<String, Number> poidsChart;
    @FXML private LineChart<String, Number> tensionChart;
    @FXML private PieChart statusPieChart;

    @FXML private GridPane anomalyDetailsGrid;
    @FXML private VBox recommendationsBox;
    @FXML private VBox analysisHistoryContainer;
    @FXML private VBox potentialRisksBox;

    @FXML private Button exportButton;
    @FXML private Button backButton;
    @FXML private Button refreshPredictionsButton;
    @FXML private Button exportHistoryButton;

    private List<suiviGrossesse> suivisList;
    private String patientName;
    private AnalysisResult currentAnalysis;
    private Map<LocalDate, AnalysisResult> analysisHistory;
    private PregnancyMLAnalyzer.MLPrediction mlPrediction;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        analysisHistory = new TreeMap<>(Comparator.reverseOrder());
        setupCharts();

        // Configuration du PieChart
        if (statusPieChart != null) {
            // Définir une taille plus grande
            statusPieChart.setPrefSize(400, 400);
            statusPieChart.setMinSize(350, 350);
            
            // Ajouter des animations
            statusPieChart.setAnimated(true);
            
            // Style du PieChart
            statusPieChart.setStyle("-fx-background-color: transparent;");
            statusPieChart.setLegendVisible(true);
            statusPieChart.setLabelsVisible(true);
            statusPieChart.setLabelLineLength(20);
            
            // Animation de rotation
            RotateTransition rotateTransition = new RotateTransition(Duration.seconds(2), statusPieChart);
            rotateTransition.setByAngle(360);
            rotateTransition.setCycleCount(1);
            rotateTransition.setInterpolator(Interpolator.EASE_BOTH);
            
            // Animation de fade in
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), statusPieChart);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            
            // Jouer les animations au chargement
            Platform.runLater(() -> {
                rotateTransition.play();
                fadeTransition.play();
            });
        }

        refreshPredictionsButton.setOnAction(event -> refreshMLPredictions());
        exportButton.setOnAction(event -> handleExportButton());
        backButton.setOnAction(event -> handleBackButton());

        // Initialize labels with default values
        if (analysisResultLabel != null) {
            analysisResultLabel.setText("En attente de données...");
        }
        if (riskLevelLabel != null) {
            riskLevelLabel.setText("Non évalué");
        }
        if (anomalyCountLabel != null) {
            anomalyCountLabel.setText("0");
        }
        if (semaineLabel != null) {
            semaineLabel.setText("--");
        }
        if (prochainRdvLabel != null) {
            prochainRdvLabel.setText("--");
        }
        if (riskProgressBar != null) {
            riskProgressBar.setProgress(0.0);
        }

        loadDataFromCSV();
    }

    public void initData(List<suiviGrossesse> suivisList, String patientName) {
        if (suivisList == null || suivisList.isEmpty()) {
            if (analysisResultLabel != null) {
                analysisResultLabel.setText("Aucune donnée disponible pour l'analyse");
            }
            return;
        }

        this.suivisList = suivisList;
        this.patientName = patientName;
        
        if (patientInfoLabel != null) {
            patientInfoLabel.setText("Analyse du suivi de grossesse - " + patientName);
        }
        
        analyzeAndDisplayResults();
    }

    private void setupCharts() {
        poidsChart.setAnimated(false);
        tensionChart.setAnimated(false);
        poidsChart.getStyleClass().add("chart-style");
        tensionChart.getStyleClass().add("chart-style");
    }

    private void analyzeAndDisplayResults() {
        if (suivisList == null || suivisList.isEmpty()) {
            analysisResultLabel.setText("Aucune donnée disponible pour l'analyse");
            return;
        }

        // Trier les suivis par date
        suivisList.sort(Comparator.comparing(s -> {
            if (s.getDateSuivi() instanceof java.sql.Date) {
                return ((java.sql.Date) s.getDateSuivi()).toLocalDate();
            }
            return s.getDateSuivi().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }));

        // Obtenir le dernier suivi
        suiviGrossesse lastSuivi = suivisList.get(suivisList.size() - 1);

        // Obtenir les suivis précédents
        List<suiviGrossesse> previousSuivis = suivisList.subList(0, suivisList.size() - 1);

        // Analyser le dernier suivi
        currentAnalysis = AnomalyDetector.analyzeSuivi(lastSuivi, previousSuivis);
        analysisHistory.put(LocalDate.now(), currentAnalysis);

        // Mettre à jour l'interface
        updateUI();
        updateCharts();
        displayAnomalyDetails();
        displayRecommendations();
        updateAnalysisHistory();
        updatePatientInfo();
    }

    private void updateUI() {
        if (currentAnalysis == null) {
            System.out.println("Analyse courante est null");
            return;
        }

        Platform.runLater(() -> {
            try {
                // Texte du résumé
                String summaryText = "L'analyse " + (currentAnalysis.getAnomalyCount() > 0 ?
                        "a détecté " + currentAnalysis.getAnomalyCount() + " anomalie(s)" :
                        "n'a détecté aucune anomalie") +
                        " dans les données de suivi de grossesse.";

                if (analysisResultLabel != null) {
                    analysisResultLabel.setText(summaryText);
                }

                // Niveau de risque avec code couleur
                if (riskLevelLabel != null) {
                    riskLevelLabel.setText(currentAnalysis.getRiskLevel().getLabel());
                    riskLevelLabel.setStyle("-fx-text-fill: " + currentAnalysis.getRiskLevel().getColor() + ";");
                }

                // Confiance
                if (confidenceLabel != null) {
                    confidenceLabel.setText(String.format("%.0f%%", currentAnalysis.getConfidence() * 100));
                }

                if (riskProgressBar != null) {
                    riskProgressBar.setProgress(currentAnalysis.getConfidence());
                }

                // Nombre d'anomalies
                if (anomalyCountLabel != null) {
                    anomalyCountLabel.setText(String.valueOf(currentAnalysis.getAnomalyCount()));
                }

                // Compter les suivis par niveau de risque
                if (suivisList != null) {
                    long normalCount = suivisList.stream()
                            .filter(s -> s.getEtatGrossesse().equalsIgnoreCase("Normal"))
                            .count();

                    long warningCount = suivisList.stream()
                            .filter(s -> s.getEtatGrossesse().equalsIgnoreCase("À surveiller"))
                            .count();

                    long riskCount = suivisList.stream()
                            .filter(s -> s.getEtatGrossesse().equalsIgnoreCase("Risque élevé"))
                            .count();

                    if (normalCountLabel != null) normalCountLabel.setText(String.valueOf(normalCount));
                    if (warningCountLabel != null) warningCountLabel.setText(String.valueOf(warningCount));
                    if (riskCountLabel != null) riskCountLabel.setText(String.valueOf(riskCount));
                }

                // Mettre à jour la date de dernière analyse
                if (lastUpdateLabel != null) {
                    lastUpdateLabel.setText("Dernière mise à jour: " +
                            LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la mise à jour de l'interface: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void updateCharts() {
        Platform.runLater(() -> {
            try {
                // Clear existing data
                if (statusPieChart != null) {
                    statusPieChart.getData().clear();
                }

                if (suivisList != null) {
                    // Count occurrences of each state
                    Map<String, Integer> stateCount = new HashMap<>();
                    for (suiviGrossesse suivi : suivisList) {
                        String etat = suivi.getEtatGrossesse();
                        stateCount.merge(etat, 1, Integer::sum);
                    }

                    // Create pie chart data
                    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
                    for (Map.Entry<String, Integer> entry : stateCount.entrySet()) {
                        pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                    }

                    // Update pie chart
                    statusPieChart.setData(pieChartData);
                    applyPieChartColors();
                }

                // Mise à jour du graphique de poids avec style amélioré
                XYChart.Series<String, Number> poidsData = new XYChart.Series<>();
                poidsData.setName("Évolution du Poids (kg)");

                if (suivisList != null) {
                    for (suiviGrossesse suivi : suivisList) {
                        Date date = suivi.getDateSuivi();
                        if (date != null) {
                            String dateStr = new SimpleDateFormat("dd/MM").format(date);
                            XYChart.Data<String, Number> data = new XYChart.Data<>(dateStr, suivi.getPoids());
                            poidsData.getData().add(data);
                        }
                    }
                }

                if (poidsChart != null) {
                    poidsChart.getData().clear();
                    poidsChart.getData().add(poidsData);
                    poidsChart.setStyle("-fx-background-color: white;");
                    styleChartData(poidsData, "#2196F3");
                }

                // Mise à jour du graphique de tension avec style amélioré
                XYChart.Series<String, Number> tensionData = new XYChart.Series<>();
                tensionData.setName("Évolution de la Tension (mmHg)");

                if (suivisList != null) {
                    for (suiviGrossesse suivi : suivisList) {
                        Date date = suivi.getDateSuivi();
                        if (date != null) {
                            String dateStr = new SimpleDateFormat("dd/MM").format(date);
                            XYChart.Data<String, Number> data = new XYChart.Data<>(dateStr, suivi.getTension());
                            tensionData.getData().add(data);
                        }
                    }
                }

                if (tensionChart != null) {
                    tensionChart.getData().clear();
                    tensionChart.getData().add(tensionData);
                    tensionChart.setStyle("-fx-background-color: white;");
                    styleChartData(tensionData, "#FF5722");
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la mise à jour des graphiques: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void applyPieChartColors() {
        // Make the pie chart larger
        statusPieChart.setPrefSize(500, 500);
        statusPieChart.setMinSize(400, 400);
        
        // Create a VBox for the legend with better styling
        VBox legendContainer = new VBox(15);
        legendContainer.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-border-color: #e0e0e0; " +
                               "-fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        legendContainer.setPadding(new Insets(20));

        // Title for the legend
        Label legendTitle = new Label("États de Grossesse - Légende Détaillée");
        legendTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        legendContainer.getChildren().add(legendTitle);

        // Configure states with colors and detailed descriptions
        Map<String, String[]> stateInfo = new LinkedHashMap<>();
        stateInfo.put("Normal", new String[]{"#28a745", "État normal - Aucun risque particulier détecté", 
            "Suivi régulier standard recommandé"});
        stateInfo.put("À risque", new String[]{"#ffc107", "Nécessite une surveillance accrue", 
            "Consultations plus fréquentes conseillées"});
        stateInfo.put("Risque élevé", new String[]{"#dc3545", "Consultation médicale urgente recommandée", 
            "Suivi médical intensif nécessaire"});
        stateInfo.put("Critique", new String[]{"#9c27b0", "Intervention médicale immédiate requise", 
            "Hospitalisation possible"});
        stateInfo.put("Stable", new String[]{"#4CAF50", "Progression stable de la grossesse", 
            "Maintenir le suivi actuel"});
        stateInfo.put("Excellente progression", new String[]{"#2196F3", "Développement optimal", 
            "Continuer les bonnes pratiques actuelles"});

        // Create legend entries with enhanced styling
        for (Map.Entry<String, String[]> entry : stateInfo.entrySet()) {
            VBox stateBox = new VBox(8);
            stateBox.setPadding(new Insets(10));
            stateBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5;");

            HBox headerBox = new HBox(10);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            // Create color indicator
            Rectangle colorBox = new Rectangle(24, 24);
            colorBox.setFill(Color.web(entry.getValue()[0]));
            colorBox.setStroke(Color.BLACK);
            colorBox.setStrokeWidth(1);
            colorBox.setArcHeight(5);
            colorBox.setArcWidth(5);

            // State name with enhanced styling
            Label stateLabel = new Label(entry.getKey());
            stateLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");

            headerBox.getChildren().addAll(colorBox, stateLabel);

            // Description with enhanced styling
            Label descLabel = new Label(entry.getValue()[1]);
            descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-wrap-text: true;");
            
            // Additional info with enhanced styling
            Label infoLabel = new Label(entry.getValue()[2]);
            infoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888; -fx-font-style: italic; -fx-wrap-text: true;");

            stateBox.getChildren().addAll(headerBox, descLabel, infoLabel);
            legendContainer.getChildren().add(stateBox);
        }

        // Add the legend container to the chart's parent
        if (statusPieChart.getParent() instanceof VBox) {
            VBox parent = (VBox) statusPieChart.getParent();
            parent.getChildren().removeIf(node -> node instanceof VBox && node != statusPieChart);
            parent.getChildren().add(legendContainer);
            VBox.setMargin(legendContainer, new Insets(20, 0, 0, 0));
        }

        // Apply colors and animations to the pie chart
        for (PieChart.Data data : statusPieChart.getData()) {
            String[] info = stateInfo.getOrDefault(data.getName(), new String[]{"#6c757d", "", ""});
            Node node = data.getNode();
            
            if (node != null) {
                // Apply color
                node.setStyle("-fx-pie-color: " + info[0] + ";");
                
                // Add hover effect
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), node);
                
                node.setOnMouseEntered(e -> {
                    scaleTransition.setToX(1.1);
                    scaleTransition.setToY(1.1);
                    scaleTransition.play();
                });
                
                node.setOnMouseExited(e -> {
                    scaleTransition.setToX(1.0);
                    scaleTransition.setToY(1.0);
                    scaleTransition.play();
                });
                
                // Enhanced tooltip
                Tooltip tooltip = new Tooltip(
                    String.format("%s\n%s\n%s\nProportion: %.1f%%",
                        data.getName(),
                        info[1],
                        info[2],
                        data.getPieValue())
                );
                tooltip.setStyle("-fx-font-size: 14px; -fx-background-color: white; " +
                               "-fx-border-color: " + info[0] + "; -fx-border-width: 2px;");
                Tooltip.install(node, tooltip);
            }
        }

        // Add title to the pie chart
        statusPieChart.setTitle("Répartition des États de Grossesse");
        statusPieChart.setTitleSide(Side.TOP);
        statusPieChart.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Make labels more visible
        statusPieChart.setLabelsVisible(true);
        statusPieChart.setLabelLineLength(20);
        statusPieChart.setLabelsVisible(true);
        statusPieChart.setStartAngle(90);
    }

    private void displayAnomalyDetails() {
        anomalyDetailsGrid.getChildren().clear();
        anomalyDetailsGrid.getRowConstraints().clear();

        if (currentAnalysis == null || currentAnalysis.getAnomalyCount() == 0) {
            Label noAnomalyLabel = new Label("Aucune anomalie détectée - Suivi normal");
            noAnomalyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #28a745; -fx-font-weight: bold;");
            anomalyDetailsGrid.add(noAnomalyLabel, 0, 0, 3, 1);
            return;
        }

        // Section Poids
        Label poidsHeader = new Label("Analyse du Poids");
        poidsHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #000000;");
        anomalyDetailsGrid.add(poidsHeader, 0, 0, 3, 1);

        if (currentAnalysis.getAnomalies().containsKey("Poids")) {
            VBox poidsBox = createAnomalyBox("Poids", currentAnalysis.getAnomalies().get("Poids"));
            anomalyDetailsGrid.add(poidsBox, 0, 1);
        } else {
            VBox normalPoidsBox = createNormalBox("Poids", "Evolution normale du poids");
            anomalyDetailsGrid.add(normalPoidsBox, 0, 1);
        }

        // Section Tension
        Label tensionHeader = new Label("Analyse de la Tension");
        tensionHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #000000;");
        anomalyDetailsGrid.add(tensionHeader, 0, 2, 3, 1);

        if (currentAnalysis.getAnomalies().containsKey("Tension")) {
            VBox tensionBox = createAnomalyBox("Tension", currentAnalysis.getAnomalies().get("Tension"));
            anomalyDetailsGrid.add(tensionBox, 0, 3);
        } else {
            VBox normalTensionBox = createNormalBox("Tension", "Tension artérielle stable");
            anomalyDetailsGrid.add(normalTensionBox, 0, 3);
        }
    }

    private VBox createAnomalyBox(String title, String description) {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: #fff3f3; -fx-background-radius: 8px; -fx-padding: 15px; -fx-border-color: #dc3545; -fx-border-radius: 8px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #000000;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #000000;");
        descLabel.setWrapText(true);

        box.getChildren().addAll(titleLabel, descLabel);
        return box;
    }

    private VBox createNormalBox(String title, String description) {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 8px; -fx-padding: 15px; -fx-border-color: #28a745; -fx-border-radius: 8px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #000000;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #000000;");
        descLabel.setWrapText(true);

        box.getChildren().addAll(titleLabel, descLabel);
        return box;
    }

    private void displayRecommendations() {
        recommendationsBox.getChildren().clear();

        Label titleLabel = new Label("Recommandations Personnalisées");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #000000; -fx-padding: 0 0 10 0;");
        recommendationsBox.getChildren().add(titleLabel);

        if (currentAnalysis == null || currentAnalysis.getRecommendations().isEmpty()) {
            VBox normalBox = new VBox(10);
            normalBox.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 8px; -fx-padding: 15px;");
            
            Label normalLabel = new Label("Suivi Normal");
            normalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #000000;");
            
            Label recommendationLabel = new Label(
                "• Continuez votre suivi régulier\n" +
                "• Maintenez une alimentation équilibrée\n" +
                "• Pratiquez une activité physique adaptée\n" +
                "• N'hésitez pas à contacter votre médecin en cas de changement"
            );
            recommendationLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #000000;");
            recommendationLabel.setWrapText(true);
            
            normalBox.getChildren().addAll(normalLabel, recommendationLabel);
            recommendationsBox.getChildren().add(normalBox);
            return;
        }

        for (String recommendation : currentAnalysis.getRecommendations()) {
            HBox recommendationBox = new HBox(10);
            recommendationBox.setStyle("-fx-background-color: #fff3f3; -fx-background-radius: 8px; -fx-padding: 15px;");

            Label bulletLabel = new Label("•");
            bulletLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #000000;");

            Label recommendationLabel = new Label(recommendation);
            recommendationLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #000000;");
            recommendationLabel.setWrapText(true);

            recommendationBox.getChildren().addAll(bulletLabel, recommendationLabel);
            recommendationsBox.getChildren().add(recommendationBox);
        }

        // Recommandations générales
        VBox generalRecommendationsBox = new VBox(10);
        generalRecommendationsBox.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 8px; -fx-padding: 15px;");

        Label generalTitle = new Label("Recommandations Générales");
        generalTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #000000;");

        VBox recommendationsList = new VBox(5);
        String[] generalRecommendations = {
            "Prenez vos médicaments selon la prescription",
            "Respectez vos rendez-vous de suivi",
            "Signalez tout nouveau symptôme",
            "Maintenez une bonne hygiène de vie",
            "Évitez le stress excessif"
        };

        for (String rec : generalRecommendations) {
            Label recLabel = new Label("• " + rec);
            recLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #000000;");
            recommendationsList.getChildren().add(recLabel);
        }

        generalRecommendationsBox.getChildren().addAll(generalTitle, recommendationsList);
        recommendationsBox.getChildren().add(generalRecommendationsBox);
    }

    private void updateAnalysisHistory() {
        if (analysisHistoryContainer == null) {
            System.err.println("Warning: analysisHistoryContainer is null");
            return;
        }

        analysisHistoryContainer.getChildren().clear();

        if (analysisHistory == null || analysisHistory.isEmpty()) {
            Label noHistoryLabel = new Label("Aucun historique d'analyse disponible");
            noHistoryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
            analysisHistoryContainer.getChildren().add(noHistoryLabel);
            return;
        }

        for (Map.Entry<LocalDate, AnalysisResult> entry : analysisHistory.entrySet()) {
            VBox historyEntry = new VBox(10);
            historyEntry.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8px; -fx-padding: 15px;");

            Label dateLabel = new Label(entry.getKey().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            dateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Label riskLabel = new Label("Niveau de risque: " + entry.getValue().getRiskLevel().getLabel());
            riskLabel.setStyle("-fx-text-fill: " + entry.getValue().getRiskLevel().getColor() + ";");

            Label confidenceLabel = new Label("Confiance: " + String.format("%.0f%%", entry.getValue().getConfidence() * 100));

            historyEntry.getChildren().addAll(dateLabel, riskLabel, confidenceLabel);
            analysisHistoryContainer.getChildren().add(historyEntry);
        }
    }

    private void updatePatientInfo() {
        if (suivisList != null && !suivisList.isEmpty()) {
            suiviGrossesse lastSuivi = suivisList.get(suivisList.size() - 1);

            etatActuelLabel.setText(currentAnalysis.getRiskLevel().getLabel());
            etatActuelLabel.setStyle("-fx-text-fill: " + currentAnalysis.getRiskLevel().getColor() + ";");

            Date dateSuivi = lastSuivi.getDateSuivi();
            if (dateSuivi != null) {
                LocalDate suiviDate;
                if (dateSuivi instanceof java.sql.Date) {
                    suiviDate = ((java.sql.Date) dateSuivi).toLocalDate();
                } else {
                    suiviDate = dateSuivi.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                }

                long weeks = ChronoUnit.WEEKS.between(suiviDate, LocalDate.now());
                semaineLabel.setText(weeks + " SA");

                LocalDate nextAppointment = LocalDate.now().plusWeeks(4);
                prochainRdvLabel.setText(nextAppointment.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else {
                semaineLabel.setText("--");
                prochainRdvLabel.setText("--");
            }
        }
    }

    @FXML
    private void handleBackButton() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleExportButton() {
        // TODO: Implémenter l'exportation des données d'analyse
        System.out.println("Exportation des données d'analyse en cours...");
    }

    @FXML
    private void handlePrintButton() {
        // TODO: Implémenter l'impression des données d'analyse
        System.out.println("Impression des données d'analyse en cours...");
    }

    @FXML
    private void handleRefreshPredictions() {
        // TODO: Implémenter la mise à jour des prédictions
        System.out.println("Actualisation des prédictions en cours...");
    }

    @FXML
    private void handleExportHistory() {
        // TODO: Implémenter l'exportation de l'historique
        System.out.println("Exportation de l'historique en cours...");
    }

    private void refreshMLPredictions() {
        if (suivisList == null || suivisList.isEmpty()) {
            showAlert("Avertissement", "Données insuffisantes", "Pas assez de données pour générer des prédictions.");
            return;
        }

        mlPrediction = PregnancyMLAnalyzer.analyzePregnancyData(suivisList);
        displayMLPredictions();
    }

    private void displayMLPredictions() {
        potentialRisksBox.getChildren().clear();

        // Afficher les prédictions
        VBox predictionsBox = new VBox(10);
        predictionsBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8px; -fx-padding: 15px;");

        Label predictionsTitle = new Label("Analyse détaillée");
        predictionsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        predictionsBox.getChildren().add(predictionsTitle);

        // Afficher le trimestre
        HBox trimesterBox = new HBox(10);
        trimesterBox.setStyle("-fx-background-color: white; -fx-background-radius: 4px; -fx-padding: 10px;");
        Label trimesterLabel = new Label("Trimestre actuel:");
        trimesterLabel.setStyle("-fx-font-weight: bold;");
        Label trimesterValue = new Label(mlPrediction.getPredictions().get("Trimestre"));
        trimesterBox.getChildren().addAll(trimesterLabel, trimesterValue);
        predictionsBox.getChildren().add(trimesterBox);

        // Afficher les tendances
        for (Map.Entry<String, String> prediction : mlPrediction.getPredictions().entrySet()) {
            if (!prediction.getKey().equals("Trimestre")) {
                HBox predictionBox = new HBox(10);
                predictionBox.setStyle("-fx-background-color: white; -fx-background-radius: 4px; -fx-padding: 10px;");

                Label paramLabel = new Label(prediction.getKey() + ":");
                paramLabel.setStyle("-fx-font-weight: bold;");

                Label valueLabel = new Label(prediction.getValue());
                valueLabel.setWrapText(true);

                predictionBox.getChildren().addAll(paramLabel, valueLabel);
                predictionsBox.getChildren().add(predictionBox);
            }
        }

        potentialRisksBox.getChildren().add(predictionsBox);

        // Afficher les statistiques
        VBox statisticsBox = new VBox(10);
        statisticsBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8px; -fx-padding: 15px;");

        Label statisticsTitle = new Label("Statistiques");
        statisticsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        statisticsBox.getChildren().add(statisticsTitle);

        for (Map.Entry<String, Double> stat : mlPrediction.getStatistics().entrySet()) {
            HBox statBox = new HBox(10);
            statBox.setStyle("-fx-background-color: white; -fx-background-radius: 4px; -fx-padding: 10px;");

            Label statLabel = new Label(stat.getKey() + ":");
            statLabel.setStyle("-fx-font-weight: bold;");

            Label valueLabel = new Label(String.format("%.2f", stat.getValue()));
            valueLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #5d9cec;");

            statBox.getChildren().addAll(statLabel, valueLabel);
            statisticsBox.getChildren().add(statBox);
        }

        potentialRisksBox.getChildren().add(statisticsBox);

        // Afficher les probabilités de risque
        VBox risksBox = new VBox(10);
        risksBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8px; -fx-padding: 15px;");

        Label risksTitle = new Label("Probabilités de risque");
        risksTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        risksBox.getChildren().add(risksTitle);

        for (Map.Entry<String, Double> risk : mlPrediction.getRiskProbabilities().entrySet()) {
            HBox riskBox = new HBox(10);
            riskBox.setStyle("-fx-background-color: white; -fx-background-radius: 4px; -fx-padding: 10px;");

            Label riskLabel = new Label(risk.getKey() + ":");
            riskLabel.setStyle("-fx-font-weight: bold;");

            ProgressBar riskBar = new ProgressBar(risk.getValue());
            riskBar.setPrefWidth(200);
            riskBar.setStyle("-fx-accent: " + getRiskColor(risk.getValue()) + ";");

            Label percentageLabel = new Label(String.format("%.0f%%", risk.getValue() * 100));
            percentageLabel.setStyle("-fx-text-fill: " + getRiskColor(risk.getValue()) + ";");

            riskBox.getChildren().addAll(riskLabel, riskBar, percentageLabel);
            risksBox.getChildren().add(riskBox);
        }

        potentialRisksBox.getChildren().add(risksBox);

        // Afficher les recommandations
        VBox recommendationsBox = new VBox(10);
        recommendationsBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8px; -fx-padding: 15px;");

        Label recommendationsTitle = new Label("Recommandations");
        recommendationsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        recommendationsBox.getChildren().add(recommendationsTitle);

        for (String recommendation : mlPrediction.getRecommendations()) {
            HBox recommendationBox = new HBox(10);
            recommendationBox.setStyle("-fx-background-color: white; -fx-background-radius: 4px; -fx-padding: 10px;");

            Label bulletLabel = new Label("•");
            bulletLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #5d9cec;");

            Label recommendationLabel = new Label(recommendation);
            recommendationLabel.setWrapText(true);

            recommendationBox.getChildren().addAll(bulletLabel, recommendationLabel);
            recommendationsBox.getChildren().add(recommendationBox);
        }

        potentialRisksBox.getChildren().add(recommendationsBox);

        // Mettre à jour les graphiques avec les données de tendance
        updateTrendCharts();
    }

    private void updateTrendCharts() {
        // Mettre à jour le graphique de poids
        XYChart.Series<String, Number> poidsData = new XYChart.Series<>();
        poidsData.setName("Poids");

        List<Double> poidsTrend = mlPrediction.getTrendData().get("Poids");
        if (poidsTrend != null) {
            for (int i = 0; i < poidsTrend.size(); i++) {
                poidsData.getData().add(new XYChart.Data<>(String.valueOf(i + 1), poidsTrend.get(i)));
            }
        }

        poidsChart.getData().clear();
        poidsChart.getData().add(poidsData);

        // Mettre à jour le graphique de tension
        XYChart.Series<String, Number> tensionData = new XYChart.Series<>();
        tensionData.setName("Tension");

        List<Double> tensionTrend = mlPrediction.getTrendData().get("Tension");
        if (tensionTrend != null) {
            for (int i = 0; i < tensionTrend.size(); i++) {
                tensionData.getData().add(new XYChart.Data<>(String.valueOf(i + 1), tensionTrend.get(i)));
            }
        }

        tensionChart.getData().clear();
        tensionChart.getData().add(tensionData);
    }

    private String getRiskColor(double risk) {
        if (risk >= 0.8) return "#dc3545"; // Rouge pour risque élevé
        if (risk >= 0.5) return "#ffc107"; // Jaune pour risque moyen
        return "#28a745"; // Vert pour risque faible
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadDataFromCSV() {
        try {
            String projectRoot = System.getProperty("user.dir");
            String csvPath = projectRoot + "/suivis_grossesse.csv";
            File csvFile = new File(csvPath);
            
            if (!csvFile.exists()) {
                System.out.println("Fichier CSV non trouvé: " + csvPath);
                return;
            }

            System.out.println("Lecture du fichier CSV: " + csvPath);
            List<suiviGrossesse> csvSuivis = new ArrayList<>();
            
            try (BufferedReader br = new BufferedReader(new FileReader(csvFile, java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                boolean firstLine = true;
                int lineNumber = 0;
                
                while ((line = br.readLine()) != null) {
                    lineNumber++;
                    if (firstLine) {
                        firstLine = false;
                        System.out.println("En-tête du CSV: " + line);
                        continue;
                    }
                    
                    try {
                        // Split by comma but handle the special case of French number format
                        String[] parts = line.split(",");
                        List<String> data = new ArrayList<>();
                        
                        // Reconstruct the proper fields
                        for (int i = 0; i < parts.length; i++) {
                            if (i < parts.length - 1 && 
                                (parts[i+1].trim().matches("\\d{2}") || 
                                 parts[i+1].trim().matches("\\d{2}\\D.*"))) {
                                // This is a decimal number with comma
                                data.add(parts[i] + "." + parts[i+1].substring(0, 2));
                                i++; // Skip the next part as we've used it
                            } else {
                                data.add(parts[i]);
                            }
                        }
                        
                        if (data.size() >= 7) {
                            suiviGrossesse suivi = new suiviGrossesse();
                            
                            try {
                                // Parse ID
                                suivi.setId(Integer.parseInt(data.get(0).trim()));
                                
                                // Parse date
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                suivi.setDateSuivi(dateFormat.parse(data.get(1).trim()));
                                
                                // Parse poids
                                suivi.setPoids(Double.parseDouble(data.get(2).trim()));
                                
                                // Parse tension
                                suivi.setTension(Double.parseDouble(data.get(3).trim()));
                                
                                // Set symptoms as string
                                suivi.setSymptomes(data.get(4).trim());
                                
                                // Set etat_grossesse as string
                                suivi.setEtatGrossesse(data.get(5).trim());
                                
                                // Parse patient ID
                                suivi.setPatientId(Integer.parseInt(data.get(6).trim()));
                                
                                csvSuivis.add(suivi);
                                System.out.println("Suivi ajouté avec succès: ID=" + suivi.getId() + 
                                    ", Date=" + data.get(1).trim() + 
                                    ", Poids=" + suivi.getPoids() + 
                                    ", Tension=" + suivi.getTension() + 
                                    ", Symptômes=" + suivi.getSymptomes() + 
                                    ", État=" + suivi.getEtatGrossesse() + 
                                    ", PatientId=" + suivi.getPatientId());
                            } catch (ParseException | NumberFormatException e) {
                                System.err.println("Erreur de parsing à la ligne " + lineNumber + ": " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            System.err.println("Ligne " + lineNumber + " invalide (nombre de colonnes insuffisant): " + line);
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur lors du traitement de la ligne " + lineNumber + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            System.out.println("Nombre total de suivis lus: " + csvSuivis.size());
            
            if (!csvSuivis.isEmpty()) {
                this.suivisList = csvSuivis;
                Platform.runLater(() -> {
                    if (analysisResultLabel != null) {
                        analysisResultLabel.setText("Données chargées: " + csvSuivis.size() + " suivis");
                    }
                    analyzeAndDisplayResults();
                });
            } else {
                System.out.println("Aucune donnée valide trouvée dans le fichier CSV");
                Platform.runLater(() -> {
                    if (analysisResultLabel != null) {
                        analysisResultLabel.setText("Aucune donnée valide trouvée");
                    }
                });
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du fichier CSV: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                if (analysisResultLabel != null) {
                    analysisResultLabel.setText("Erreur lors du chargement des données");
                }
            });
        }
    }

    private void styleChartData(XYChart.Series<String, Number> series, String color) {
        String style = String.format("-fx-stroke: %s; -fx-stroke-width: 2px;", color);
        series.getNode().setStyle(style);

        for (XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle(String.format("-fx-background-color: %s, white;", color));
                
                // Ajouter une info-bulle (tooltip)
                Tooltip tooltip = new Tooltip(
                    String.format("Date: %s\nValeur: %.1f", data.getXValue(), data.getYValue())
                );
                Tooltip.install(node, tooltip);
            }
        }
    }

    private List<suiviGrossesse> readCsvFile(String csvFilePath) {
        List<suiviGrossesse> suivis = new ArrayList<>();
        Path path = Paths.get(csvFilePath);
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            // Skip header
            String line = br.readLine();
            System.out.println("En-tête du CSV: " + line);
            
            while ((line = br.readLine()) != null) {
                try {
                    // Séparer la ligne en utilisant la virgule comme délimiteur
                    String[] values = line.split(",");
                    if (values.length >= 7) {
                        suiviGrossesse suivi = new suiviGrossesse();
                        
                        // Parser l'ID
                        suivi.setId(Integer.parseInt(values[0].trim()));
                        
                        // Parser la date
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = dateFormat.parse(values[1].trim());
                        suivi.setDateSuivi(date);
                        
                        // Parser le poids (remplacer la virgule par un point si nécessaire)
                        String poidsStr = values[2].trim().replace(",", ".");
                        suivi.setPoids(Double.parseDouble(poidsStr));
                        
                        // Parser la tension (remplacer la virgule par un point si nécessaire)
                        String tensionStr = values[3].trim().replace(",", ".");
                        suivi.setTension(Double.parseDouble(tensionStr));
                        
                        // Définir les symptômes (pas besoin de parsing)
                        suivi.setSymptomes(values[4].trim());
                        
                        // Définir l'état de grossesse (pas besoin de parsing)
                        suivi.setEtatGrossesse(values[5].trim());
                        
                        // Parser l'ID du patient
                        suivi.setPatientId(Integer.parseInt(values[6].trim()));
                        
                        suivis.add(suivi);
                        System.out.println("Suivi ajouté avec succès: " + suivi.getId());
                    }
                } catch (Exception e) {
                    System.out.println("Erreur lors du traitement de la ligne: " + line);
                    System.out.println("Message d'erreur: " + e.getMessage());
                    // Continue avec la ligne suivante
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("Nombre total de suivis lus avec succès: " + suivis.size());
        return suivis;
    }
}