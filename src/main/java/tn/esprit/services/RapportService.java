package tn.esprit.services;

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
    
    // Polices personnalisées avec des couleurs
    private static final Font TITRE_FONT = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, new BaseColor(44, 62, 80));
    private static final Font SOUS_TITRE_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, new BaseColor(52, 73, 94));
    private static final Font TEXTE_NORMAL = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, new BaseColor(44, 62, 80));
    private static final Font TEXTE_GRAS = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(44, 62, 80));
    private static final Font TEXTE_ITALIQUE = new Font(Font.FontFamily.HELVETICA, 11, Font.ITALIC, new BaseColor(44, 62, 80));
    
    // Couleurs pour les graphiques
    private static final BaseColor COULEUR_PRINCIPALE = new BaseColor(52, 152, 219);
    private static final BaseColor COULEUR_SECONDAIRE = new BaseColor(231, 76, 60);
    private static final BaseColor COULEUR_TERTIAIRE = new BaseColor(46, 204, 113);
    private static final BaseColor COULEUR_QUATERNAIRE = new BaseColor(155, 89, 182);

    public RapportService() {
        this.statsService = new StatistiquesService();
    }

    public boolean genererRapport(String cheminFichier, LocalDate debut, LocalDate fin) {
        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(cheminFichier));
            
            // Ajouter des métadonnées
            document.addTitle("Rapport Statistique des Rendez-Vous");
            document.addAuthor("BabySteps");
            document.addCreator("BabySteps Application");
            
            document.open();

            // Ajouter l'en-tête avec logo
            ajouterEntete(document, debut, fin);

            // Ajouter une table des matières
            ajouterTableDesMatieres(document);

            try {
                // 1. Activité par période
                ajouterSection(document, "Activité par période", "Analyse de l'activité quotidienne");
                Map<String, Integer> rdvParJour = statsService.getRendezVousParPeriode(debut, fin, "jour");
                if (rdvParJour != null && !rdvParJour.isEmpty()) {
                    ajouterGraphiqueLineaire(document, rdvParJour, "Nombre de rendez-vous par jour", "Date", "Nombre de RDV", 500, 300);
                    ajouterResumeDonnees(document, rdvParJour, "Résumé de l'activité");
                } else {
                    ajouterMessageAucuneDonnee(document);
                }

                // 2. Performance des médecins
                ajouterSection(document, "Performance des médecins", "Analyse de la charge de travail");
                Map<String, Integer> rdvParMedecin = statsService.getRendezVousParMedecin(debut, fin);
                if (rdvParMedecin != null && !rdvParMedecin.isEmpty()) {
                    ajouterGraphiqueBarresHorizontales(document, rdvParMedecin, "Charge de travail par médecin", "Médecin", "Nombre de RDV", 500, 300);
                    ajouterResumeDonnees(document, rdvParMedecin, "Résumé des performances");
                } else {
                    ajouterMessageAucuneDonnee(document);
                }

                // 3. Taux d'occupation des plages horaires
                ajouterSection(document, "Taux d'occupation des plages horaires", "Analyse de l'occupation");
                Map<String, Map<String, Integer>> occupationPlages = statsService.getTauxOccupationPlages(debut, fin);
                if (occupationPlages != null && !occupationPlages.isEmpty()) {
                    ajouterGraphiqueHeatmap(document, occupationPlages, "Occupation par jour et plage horaire", 500, 300);
                    ajouterResumeOccupation(document, occupationPlages);
                } else {
                    ajouterMessageAucuneDonnee(document);
                }

                // 4. Motifs de consultation
                ajouterSection(document, "Motifs de consultation fréquents", "Analyse des motifs");
                Map<String, Integer> motifs = statsService.getMotifsFrequents(debut, fin, 5);
                if (motifs != null && !motifs.isEmpty()) {
                    ajouterGraphiqueCamembert(document, motifs, "Top 5 des motifs de consultation", 400, 300);
                    ajouterResumeDonnees(document, motifs, "Résumé des motifs");
                } else {
                    ajouterMessageAucuneDonnee(document);
                }

                // 5. Assiduité des patients
                ajouterSection(document, "Assiduité des patients", "Analyse de l'assiduité");
                Map<String, Integer> statuts = statsService.getStatutRendezVous(debut, fin);
                if (statuts != null && !statuts.isEmpty()) {
                    ajouterGraphiqueCamembert(document, statuts, "Statuts des rendez-vous", 400, 300);
                    ajouterResumeDonnees(document, statuts, "Résumé de l'assiduité");
                } else {
                    ajouterMessageAucuneDonnee(document);
                }

                // 6. Délai de prise de rendez-vous
                ajouterSection(document, "Délai de prise de rendez-vous", "Analyse des délais");
                Map<String, Integer> delais = statsService.getDelaiRendezVous(debut, fin);
                if (delais != null && !delais.isEmpty()) {
                    ajouterGraphiqueBarres(document, delais, "Répartition par délai", "Délai", "Nombre de RDV", 500, 300);
                    ajouterResumeDonnees(document, delais, "Résumé des délais");
                } else {
                    ajouterMessageAucuneDonnee(document);
                }

                // Ajouter un pied de page
                ajouterPiedDePage(document);

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

    private void ajouterEntete(Document document, LocalDate debut, LocalDate fin) throws DocumentException {
        try {
            // Ajouter le logo
            Image logo = Image.getInstance("src/main/resources/assets/logo-modified.png");
            logo.scaleToFit(100, 100);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);
            document.add(Chunk.NEWLINE);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du logo: " + e.getMessage());
        }

        // Titre principal
        Paragraph titre = new Paragraph("Rapport Statistique des Rendez-Vous", TITRE_FONT);
        titre.setAlignment(Element.ALIGN_CENTER);
        titre.setSpacingAfter(10);
        document.add(titre);

        // Période
        Paragraph periode = new Paragraph("Période du " + debut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                " au " + fin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                TEXTE_ITALIQUE);
        periode.setAlignment(Element.ALIGN_CENTER);
        periode.setSpacingAfter(20);
        document.add(periode);

        // Ligne de séparation
        DottedLineSeparator separator = new DottedLineSeparator();
        separator.setLineWidth(1);
        separator.setGap(3);
        separator.setLineColor(new BaseColor(200, 200, 200));
        Chunk linebreak = new Chunk(separator);
        document.add(linebreak);
        document.add(Chunk.NEWLINE);
    }

    private void ajouterTableDesMatieres(Document document) throws DocumentException {
        Paragraph titreTDM = new Paragraph("Table des matières", SOUS_TITRE_FONT);
        titreTDM.setSpacingBefore(20);
        titreTDM.setSpacingAfter(10);
        document.add(titreTDM);

        String[] sections = {
            "1. Activité par période",
            "2. Performance des médecins",
            "3. Taux d'occupation des plages horaires",
            "4. Motifs de consultation fréquents",
            "5. Assiduité des patients",
            "6. Délai de prise de rendez-vous"
        };

        for (String section : sections) {
            Paragraph p = new Paragraph(section, TEXTE_NORMAL);
            p.setIndentationLeft(20);
            document.add(p);
        }

        document.add(Chunk.NEWLINE);
        DottedLineSeparator separator = new DottedLineSeparator();
        separator.setLineWidth(1);
        separator.setGap(3);
        separator.setLineColor(new BaseColor(200, 200, 200));
        Chunk linebreak = new Chunk(separator);
        document.add(linebreak);
        document.add(Chunk.NEWLINE);
    }

    private void ajouterSection(Document document, String titre, String description) throws DocumentException {
        Paragraph pTitre = new Paragraph(titre, SOUS_TITRE_FONT);
        pTitre.setSpacingBefore(20);
        pTitre.setSpacingAfter(5);
        document.add(pTitre);

        Paragraph pDesc = new Paragraph(description, TEXTE_ITALIQUE);
        pDesc.setSpacingAfter(10);
        document.add(pDesc);
    }

    private void ajouterResumeDonnees(Document document, Map<String, Integer> donnees, String titre) throws DocumentException {
        Paragraph pTitre = new Paragraph(titre, TEXTE_GRAS);
        pTitre.setSpacingBefore(10);
        pTitre.setSpacingAfter(5);
        document.add(pTitre);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(5);
        table.setSpacingAfter(10);

        // En-têtes
        PdfPCell cell1 = new PdfPCell(new Phrase("Catégorie", TEXTE_GRAS));
        PdfPCell cell2 = new PdfPCell(new Phrase("Valeur", TEXTE_GRAS));
        cell1.setBackgroundColor(new BaseColor(240, 240, 240));
        cell2.setBackgroundColor(new BaseColor(240, 240, 240));
        table.addCell(cell1);
        table.addCell(cell2);

        // Données
        for (Map.Entry<String, Integer> entry : donnees.entrySet()) {
            table.addCell(new Phrase(entry.getKey(), TEXTE_NORMAL));
            table.addCell(new Phrase(String.valueOf(entry.getValue()), TEXTE_NORMAL));
        }

        document.add(table);
    }

    private void ajouterResumeOccupation(Document document, Map<String, Map<String, Integer>> donnees) throws DocumentException {
        Paragraph pTitre = new Paragraph("Résumé de l'occupation", TEXTE_GRAS);
        pTitre.setSpacingBefore(10);
        pTitre.setSpacingAfter(5);
        document.add(pTitre);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(5);
        table.setSpacingAfter(10);

        // En-têtes
        PdfPCell cell1 = new PdfPCell(new Phrase("Jour", TEXTE_GRAS));
        PdfPCell cell2 = new PdfPCell(new Phrase("Plage horaire", TEXTE_GRAS));
        PdfPCell cell3 = new PdfPCell(new Phrase("Occupation", TEXTE_GRAS));
        cell1.setBackgroundColor(new BaseColor(240, 240, 240));
        cell2.setBackgroundColor(new BaseColor(240, 240, 240));
        cell3.setBackgroundColor(new BaseColor(240, 240, 240));
        table.addCell(cell1);
        table.addCell(cell2);
        table.addCell(cell3);

        // Données
        for (Map.Entry<String, Map<String, Integer>> entry : donnees.entrySet()) {
            String jour = entry.getKey();
            for (Map.Entry<String, Integer> plage : entry.getValue().entrySet()) {
                table.addCell(new Phrase(jour, TEXTE_NORMAL));
                table.addCell(new Phrase(plage.getKey(), TEXTE_NORMAL));
                table.addCell(new Phrase(String.valueOf(plage.getValue()), TEXTE_NORMAL));
            }
        }

        document.add(table);
    }

    private void ajouterMessageAucuneDonnee(Document document) throws DocumentException {
        Paragraph p = new Paragraph("Aucune donnée disponible pour cette période.", TEXTE_ITALIQUE);
        p.setSpacingBefore(10);
        p.setSpacingAfter(10);
        document.add(p);
    }

    private void ajouterPiedDePage(Document document) throws DocumentException {
        document.add(Chunk.NEWLINE);
        DottedLineSeparator separator = new DottedLineSeparator();
        separator.setLineWidth(1);
        separator.setGap(3);
        separator.setLineColor(new BaseColor(200, 200, 200));
        Chunk linebreak = new Chunk(separator);
        document.add(linebreak);

        Paragraph piedDePage = new Paragraph("Rapport généré le " + 
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 
            TEXTE_ITALIQUE);
        piedDePage.setAlignment(Element.ALIGN_CENTER);
        piedDePage.setSpacingBefore(10);
        document.add(piedDePage);
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