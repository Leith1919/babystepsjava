package services.User;

import com.itextpdf.text.*;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class RapportService {
    private final StatistiquesService statsService;
    private static final Font TITRE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font SOUS_TITRE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font TEXTE_NORMAL = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

    public RapportService() {
        this.statsService = new StatistiquesService();
    }

    public boolean genererRapport(String cheminFichier, LocalDate debut, LocalDate fin) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(cheminFichier));
            document.open();

            // Ajouter l'en-tête
            ajouterEntete(document, debut, fin);

            try {
                // 1. Activité par période
                document.add(new Paragraph("Activité par période", TITRE_FONT));
                document.add(Chunk.NEWLINE);
                Map<String, Integer> rdvParJour = statsService.getRendezVousParPeriode(debut, fin, "jour");
                if (rdvParJour != null && !rdvParJour.isEmpty()) {
                    ajouterGraphiqueLineaire(document, rdvParJour, "Nombre de rendez-vous par jour", "Date", "Nombre de RDV", 500, 300);
                } else {
                    document.add(new Paragraph("Aucune donnée disponible pour cette période.", TEXTE_NORMAL));
                }
                document.add(Chunk.NEWLINE);

                // 2. Performance des médecins
                document.add(new Paragraph("Performance des médecins", TITRE_FONT));
                document.add(Chunk.NEWLINE);
                Map<String, Integer> rdvParMedecin = statsService.getRendezVousParMedecin(debut, fin);
                if (rdvParMedecin != null && !rdvParMedecin.isEmpty()) {
                    ajouterGraphiqueBarresHorizontales(document, rdvParMedecin, "Charge de travail par médecin", "Médecin", "Nombre de RDV", 500, 300);
                } else {
                    document.add(new Paragraph("Aucune donnée disponible pour cette période.", TEXTE_NORMAL));
                }
                document.add(Chunk.NEWLINE);

                // 3. Taux d'occupation des plages horaires
                document.add(new Paragraph("Taux d'occupation des plages horaires", TITRE_FONT));
                document.add(Chunk.NEWLINE);
                Map<String, Map<String, Integer>> occupationPlages = statsService.getTauxOccupationPlages(debut, fin);
                if (occupationPlages != null && !occupationPlages.isEmpty()) {
                    ajouterGraphiqueHeatmap(document, occupationPlages, "Occupation par jour et plage horaire", 500, 300);
                } else {
                    document.add(new Paragraph("Aucune donnée disponible pour cette période.", TEXTE_NORMAL));
                }
                document.add(Chunk.NEWLINE);

                // 4. Motifs de consultation
                document.add(new Paragraph("Motifs de consultation fréquents", TITRE_FONT));
                document.add(Chunk.NEWLINE);
                Map<String, Integer> motifs = statsService.getMotifsFrequents(debut, fin, 5);
                if (motifs != null && !motifs.isEmpty()) {
                    ajouterGraphiqueCamembert(document, motifs, "Top 5 des motifs de consultation", 400, 300);
                } else {
                    document.add(new Paragraph("Aucune donnée disponible pour cette période.", TEXTE_NORMAL));
                }
                document.add(Chunk.NEWLINE);

                // 5. Assiduité des patients
                // 6. Délai de prise de rendez-vous
                document.add(new Paragraph("Délai entre prise de rendez-vous et consultation", TITRE_FONT));
                document.add(Chunk.NEWLINE);
                Map<String, Integer> delais = statsService.getDelaiRendezVous(debut, fin);
                if (delais != null && !delais.isEmpty()) {
                    ajouterGraphiqueBarres(document, delais, "Répartition par délai", "Délai", "Nombre de RDV", 500, 300);
                } else {
                    document.add(new Paragraph("Aucune donnée disponible pour cette période.", TEXTE_NORMAL));
                }
            } catch (Exception e) {
                document.add(new Paragraph("Une erreur s'est produite lors de la génération des graphiques: " + e.getMessage(), TEXTE_NORMAL));
                e.printStackTrace();
            }

            document.close();
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du rapport: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Version personnalisable du rapport
    public boolean genererRapportPersonnalise(String cheminFichier, LocalDate debut, LocalDate fin,
                                              boolean inclureActivite, boolean inclureMedecins,
                                              boolean inclureOccupation, boolean inclureMotifs,
                                              boolean inclureAssiduite, boolean inclureDelai) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(cheminFichier));
            document.open();

            // Ajouter l'en-tête
            ajouterEntete(document, debut, fin);

            // 1. Activité par période
            if (inclureActivite) {
                document.add(new Paragraph("Activité par période", TITRE_FONT));
                document.add(Chunk.NEWLINE);
                Map<String, Integer> rdvParJour = statsService.getRendezVousParPeriode(debut, fin, "jour");
                ajouterGraphiqueLineaire(document, rdvParJour, "Nombre de rendez-vous par jour", "Date", "Nombre de RDV", 500, 300);
                document.add(Chunk.NEWLINE);
            }

            // 2. Performance des médecins
            if (inclureMedecins) {
                document.add(new Paragraph("Performance des médecins", TITRE_FONT));
                document.add(Chunk.NEWLINE);
                Map<String, Integer> rdvParMedecin = statsService.getRendezVousParMedecin(debut, fin);
                ajouterGraphiqueBarresHorizontales(document, rdvParMedecin, "Charge de travail par médecin", "Médecin", "Nombre de RDV", 500, 300);
                document.add(Chunk.NEWLINE);
            }

            // 3. Taux d'occupation des plages horaires
            if (inclureOccupation) {
                document.add(new Paragraph("Taux d'occupation des plages horaires", TITRE_FONT));
                document.add(Chunk.NEWLINE);
                Map<String, Map<String, Integer>> occupationPlages = statsService.getTauxOccupationPlages(debut, fin);
                ajouterGraphiqueHeatmap(document, occupationPlages, "Occupation par jour et plage horaire", 500, 300);
                document.add(Chunk.NEWLINE);
            }

            // 4. Motifs de consultation
            if (inclureMotifs) {
                document.add(new Paragraph("Motifs de consultation fréquents", TITRE_FONT));
                document.add(Chunk.NEWLINE);
                Map<String, Integer> motifs = statsService.getMotifsFrequents(debut, fin, 5);
                ajouterGraphiqueCamembert(document, motifs, "Top 5 des motifs de consultation", 400, 300);
                document.add(Chunk.NEWLINE);
            }

            // 5. Assiduité des patients
            // 6. Délai de prise de rendez-vous
            if (inclureDelai) {
                document.add(new Paragraph("Délai entre prise de rendez-vous et consultation", TITRE_FONT));
                document.add(Chunk.NEWLINE);
                Map<String, Integer> delais = statsService.getDelaiRendezVous(debut, fin);
                ajouterGraphiqueBarres(document, delais, "Répartition par délai", "Délai", "Nombre de RDV", 500, 300);
            }

            document.close();
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du rapport: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void ajouterEntete(Document document, LocalDate debut, LocalDate fin) throws DocumentException {
        Paragraph titre = new Paragraph("Rapport Statistique des Rendez-Vous", new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.DARK_GRAY));
        titre.setAlignment(Element.ALIGN_CENTER);
        document.add(titre);

        Paragraph periode = new Paragraph("Période du " + debut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                " au " + fin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC));
        periode.setAlignment(Element.ALIGN_CENTER);
        document.add(periode);

        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
    }

    private void ajouterGraphiqueLineaire(Document document, Map<String, Integer> donnees, String titre,
                                          String axeX, String axeY, int largeur, int hauteur)
            throws DocumentException, java.io.IOException {
        if (donnees == null || donnees.isEmpty()) {
            document.add(new Paragraph("Aucune donnée disponible pour ce graphique.", TEXTE_NORMAL));
            return;
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : donnees.entrySet()) {
            dataset.addValue(entry.getValue(), "RDV", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createLineChart(
                titre,
                axeX,
                axeY,
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );

        File tempFile = File.createTempFile("chart", ".png");
        ChartUtils.saveChartAsPNG(tempFile, chart, largeur, hauteur);

        Image chartImage = Image.getInstance(tempFile.getAbsolutePath());
        chartImage.setAlignment(Element.ALIGN_CENTER);
        document.add(chartImage);

        tempFile.delete();
    }

    private void ajouterGraphiqueBarres(Document document, Map<String, Integer> donnees, String titre,
                                        String axeX, String axeY, int largeur, int hauteur)
            throws DocumentException, java.io.IOException {
        if (donnees == null || donnees.isEmpty()) {
            document.add(new Paragraph("Aucune donnée disponible pour ce graphique.", TEXTE_NORMAL));
            return;
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : donnees.entrySet()) {
            dataset.addValue(entry.getValue(), "RDV", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                titre,
                axeX,
                axeY,
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );

        File tempFile = File.createTempFile("chart", ".png");
        ChartUtils.saveChartAsPNG(tempFile, chart, largeur, hauteur);

        Image chartImage = Image.getInstance(tempFile.getAbsolutePath());
        chartImage.setAlignment(Element.ALIGN_CENTER);
        document.add(chartImage);

        tempFile.delete();
    }

    private void ajouterGraphiqueBarresHorizontales(Document document, Map<String, Integer> donnees, String titre,
                                                    String axeX, String axeY, int largeur, int hauteur)
            throws DocumentException, java.io.IOException {
        if (donnees == null || donnees.isEmpty()) {
            document.add(new Paragraph("Aucune donnée disponible pour ce graphique.", TEXTE_NORMAL));
            return;
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : donnees.entrySet()) {
            dataset.addValue(entry.getValue(), "RDV", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                titre,
                axeY, // Inversé pour les barres horizontales
                axeX, // Inversé pour les barres horizontales
                dataset,
                PlotOrientation.HORIZONTAL, // Orientation horizontale
                false, true, false
        );

        File tempFile = File.createTempFile("chart", ".png");
        ChartUtils.saveChartAsPNG(tempFile, chart, largeur, hauteur);

        Image chartImage = Image.getInstance(tempFile.getAbsolutePath());
        chartImage.setAlignment(Element.ALIGN_CENTER);
        document.add(chartImage);

        tempFile.delete();
    }

    private void ajouterGraphiqueCamembert(Document document, Map<String, Integer> donnees, String titre,
                                           int largeur, int hauteur)
            throws DocumentException, java.io.IOException {
        if (donnees == null || donnees.isEmpty()) {
            document.add(new Paragraph("Aucune donnée disponible pour ce graphique.", TEXTE_NORMAL));
            return;
        }

        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Integer> entry : donnees.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                titre,
                dataset,
                true, // Légende
                true, // Tooltips
                false // URLs
        );

        File tempFile = File.createTempFile("chart", ".png");
        ChartUtils.saveChartAsPNG(tempFile, chart, largeur, hauteur);

        Image chartImage = Image.getInstance(tempFile.getAbsolutePath());
        chartImage.setAlignment(Element.ALIGN_CENTER);
        document.add(chartImage);

        tempFile.delete();
    }

    private void ajouterGraphiqueHeatmap(Document document, Map<String, Map<String, Integer>> donnees,
                                         String titre, int largeur, int hauteur)
            throws DocumentException {
        if (donnees == null || donnees.isEmpty()) {
            document.add(new Paragraph("Aucune donnée disponible pour ce graphique.", TEXTE_NORMAL));
            return;
        }

        // Pour une heatmap, nous allons créer un tableau avec code couleur
        // car JFreeChart n'a pas de heatmap intégrée
        PdfPTable table = new PdfPTable(5); // 1 colonne pour les jours + 4 plages horaires
        table.setWidthPercentage(90);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // En-tête
        PdfPCell cellTitre = new PdfPCell(new Phrase(titre, SOUS_TITRE_FONT));
        cellTitre.setColspan(5);
        cellTitre.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellTitre.setPadding(10);
        table.addCell(cellTitre);

        // En-têtes des colonnes
        table.addCell(new Phrase("Jour", TEXTE_NORMAL));
        table.addCell(new Phrase("9h-11h", TEXTE_NORMAL));
        table.addCell(new Phrase("11h-13h", TEXTE_NORMAL));
        table.addCell(new Phrase("14h-16h", TEXTE_NORMAL));
        table.addCell(new Phrase("16h-18h", TEXTE_NORMAL));

        // Trouver le maximum pour la normalisation des couleurs
        int maxValue = 0;
        for (Map<String, Integer> jour : donnees.values()) {
            for (Integer val : jour.values()) {
                if (val != null && val > maxValue) maxValue = val;
            }
        }

        // Ajouter les données avec coloration
        String[] plages = {"9-11", "11-13", "14-16", "16-18"};
        for (Map.Entry<String, Map<String, Integer>> entry : donnees.entrySet()) {
            // Jour
            table.addCell(new Phrase(entry.getKey(), TEXTE_NORMAL));

            // Plages horaires
            for (String plage : plages) {
                Integer valeur = entry.getValue().getOrDefault(plage, 0);
                if (valeur == null) valeur = 0;

                float intensite = maxValue > 0 ? (float)valeur / maxValue : 0;

                // Créer une couleur RGB avec des valeurs entières (0-255)
                BaseColor couleur = new BaseColor(
                        (int)(255 * (1f - intensite)),  // Rouge (moins = plus intense)
                        255,                            // Vert (constant)
                        (int)(255 * (1f - 0.5f * intensite)), // Bleu (moins = plus intense, mais moins que rouge)
                        255                             // Alpha (opacité)
                );

                PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(valeur), TEXTE_NORMAL));
                cell.setBackgroundColor(couleur);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }
        }

        document.add(table);
    }

}