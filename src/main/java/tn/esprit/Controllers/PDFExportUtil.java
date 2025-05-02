package tn.esprit.Controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import tn.esprit.entities.suiviBebe;
import tn.esprit.entities.suiviGrossesse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class PDFExportUtil {

    private static final String DEFAULT_FILENAME = "Suivi_Bebe_";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLUE);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
    private static final Font SECTION_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);
    private static final Font INFO_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.DARK_GRAY);

    /**
     * Ajoute le bouton d'exportation PDF à votre contrôleur existant
     */
    public static void setupExportButton(Button exportButton, AfficherSuiviBebeController controller) {
        exportButton.setOnAction(event -> {
            try {
                exportToPDF(controller);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de l'exportation du PDF: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        });
    }

    /**
     * Méthode principale pour l'exportation de données vers un PDF
     */
    public static void exportToPDF(AfficherSuiviBebeController controller) throws Exception {
        // Vérifier si un suivi de grossesse est sélectionné
        suiviGrossesse sg = controller.getSuiviGrossesse();
        if (sg == null) {
            showAlert("Information", "Aucun suivi de grossesse sélectionné.", Alert.AlertType.INFORMATION);
            return;
        }

        // Récupérer les données à exporter
        List<suiviBebe> listeBebes = controller.getBebeService().recupererParSuiviGrossesse(sg);
        if (listeBebes == null || listeBebes.isEmpty()) {
            showAlert("Information", "Aucune donnée à exporter.", Alert.AlertType.INFORMATION);
            return;
        }

        // Configurer la boîte de dialogue de sauvegarde
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        String defaultFileName = DEFAULT_FILENAME + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
        fileChooser.setInitialFileName(defaultFileName);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        File file = fileChooser.showSaveDialog(controller.getAjoutBebeButton().getScene().getWindow());

        if (file != null) {
            // Créer le document PDF
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));

            // Ajouter un événement de pied de page
            writer.setPageEvent(new FooterPageEvent());

            document.open();

            // Ajouter le logo et l'en-tête
            addHeaderWithLogo(document, writer);

            // Ajouter les informations de la patiente
            addPatientInfo(document, sg);

            // Ajouter le tableau de données
            addDataTable(document, listeBebes);

            // Ajouter le résumé et les statistiques
            addSummaryAndStats(document, listeBebes);

            document.close();

            showAlert("Succès", "Le rapport PDF a été généré avec succès.", Alert.AlertType.INFORMATION);

            // Proposer d'ouvrir le fichier
            Optional<ButtonType> result = confirmOpenFile("Voulez-vous ouvrir le fichier PDF ?");
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    java.awt.Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    showAlert("Erreur", "Impossible d'ouvrir le fichier: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        }
    }

    /**
     * Ajoute l'en-tête avec le logo au document
     */
    private static void addHeaderWithLogo(Document document, PdfWriter writer) throws Exception {
        // Créer un tableau pour l'en-tête avec logo
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 3});
        headerTable.setSpacingAfter(20);

        // Ajouter le logo
        try {
            Image logo = Image.getInstance(PDFExportUtil.class.getResource("/assets/logo-modified.png"));
            logo.scaleToFit(100, 100);

            PdfPCell logoCell = new PdfPCell(logo);
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(logoCell);
        } catch (Exception e) {
            // Si le logo ne peut pas être chargé, ajouter une cellule vide
            PdfPCell emptyCell = new PdfPCell(new Phrase("LOGO"));
            emptyCell.setBorder(Rectangle.NO_BORDER);
            emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerTable.addCell(emptyCell);
        }

        // Ajouter le titre
        Paragraph title = new Paragraph("Rapport: Suivi du Bébé", TITLE_FONT);
        Paragraph subTitle = new Paragraph("Généré le: " + DATE_FORMAT.format(new Date()), INFO_FONT);

        PdfPCell titleCell = new PdfPCell();
        titleCell.addElement(title);
        titleCell.addElement(subTitle);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setPaddingLeft(10);
        headerTable.addCell(titleCell);

        document.add(headerTable);

        // Ajouter une ligne de séparation
        Paragraph emptyLine = new Paragraph(" ");
        emptyLine.setSpacingAfter(5);
        document.add(emptyLine);

        // Utiliser PdfContentByte pour dessiner une ligne
        PdfContentByte contentByte = writer.getDirectContent();
        contentByte.setColorStroke(new BaseColor(73, 137, 232));
        contentByte.setLineWidth(2);
        contentByte.moveTo(document.left(), document.getPageSize().getHeight() - 100);
        contentByte.lineTo(document.right(), document.getPageSize().getHeight() - 100);
        contentByte.stroke();

        document.add(Chunk.NEWLINE);
    }

    /**
     * Ajoute les informations de la patiente au document
     */
    private static void addPatientInfo(Document document, suiviGrossesse sg) throws Exception {
        Paragraph patientTitle = new Paragraph("INFORMATIONS DE LA PATIENTE", SECTION_FONT);
        patientTitle.setSpacingBefore(10);
        patientTitle.setSpacingAfter(10);
        document.add(patientTitle);

        // Créer un tableau pour les informations
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 3});
        infoTable.setSpacingAfter(15);

        // Ajouter les informations disponibles
        addPatientInfoRow(infoTable, "ID Grossesse:", String.valueOf(sg.getId()));
        addPatientInfoRow(infoTable, "Date début:", DATE_FORMAT.format(sg.getDateSuivi()));

        // Si plus d'informations sont disponibles dans votre objet suiviGrossesse, ajoutez-les ici
        addPatientInfoRow(infoTable, "Poids:", String.valueOf(sg.getPoids()) + " kg");
        addPatientInfoRow(infoTable, "Tension:", String.valueOf(sg.getTension()));
        addPatientInfoRow(infoTable, "État de la grossesse:", sg.getEtatGrossesse());
        addPatientInfoRow(infoTable, "Symptômes:", sg.getSymptomes());

        document.add(infoTable);
    }

    /**
     * Ajoute une ligne d'information à la table d'informations patient
     */
    private static void addPatientInfoRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, SECTION_FONT));
        labelCell.setBackgroundColor(new BaseColor(240, 247, 255));
        labelCell.setBorderColor(BaseColor.LIGHT_GRAY);
        labelCell.setPadding(5);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorderColor(BaseColor.LIGHT_GRAY);
        valueCell.setPadding(5);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    /**
     * Ajoute le tableau des données de suivi au document
     */
    private static void addDataTable(Document document, List<suiviBebe> listeBebes) throws Exception {
        Paragraph tableTitle = new Paragraph("HISTORIQUE DES SUIVIS", SECTION_FONT);
        tableTitle.setSpacingBefore(10);
        tableTitle.setSpacingAfter(10);
        document.add(tableTitle);

        // Créer le tableau pour les données
        PdfPTable dataTable = new PdfPTable(6);
        dataTable.setWidthPercentage(100);
        dataTable.setWidths(new float[]{1.5f, 1, 1, 1.5f, 1.5f, 1.5f});
        dataTable.setSpacingAfter(15);

        // Ajouter l'en-tête du tableau
        String[] headers = {"Date", "Poids (kg)", "Taille (cm)", "État de Santé", "Battement Cœur", "Appétit"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(new BaseColor(75, 137, 232));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(5);
            cell.setBorderWidth(1);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setMinimumHeight(25);
            cell.setNoWrap(false);
            dataTable.addCell(cell);
        }

        // Ajouter les données au tableau
        boolean alternateColor = false;
        for (suiviBebe bebe : listeBebes) {
            // Alternance des couleurs de lignes pour une meilleure lisibilité
            BaseColor rowColor = alternateColor ? new BaseColor(240, 247, 255) : BaseColor.WHITE;
            alternateColor = !alternateColor;

            // Date
            PdfPCell dateCell = new PdfPCell(new Phrase(DATE_FORMAT.format(bebe.getDateSuivi()), NORMAL_FONT));
            dateCell.setBackgroundColor(rowColor);
            dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            dateCell.setPadding(5);
            dataTable.addCell(dateCell);

            // Poids
            PdfPCell poidsCell = new PdfPCell(new Phrase(String.format("%.2f", bebe.getPoidsBebe()), NORMAL_FONT));
            poidsCell.setBackgroundColor(rowColor);
            poidsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            poidsCell.setPadding(5);
            dataTable.addCell(poidsCell);

            // Taille
            PdfPCell tailleCell = new PdfPCell(new Phrase(String.format("%.1f", bebe.getTailleBebe()), NORMAL_FONT));
            tailleCell.setBackgroundColor(rowColor);
            tailleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tailleCell.setPadding(5);
            dataTable.addCell(tailleCell);

            // État de santé
            PdfPCell santeCell = new PdfPCell(new Phrase(bebe.getEtatSante(), NORMAL_FONT));
            santeCell.setBackgroundColor(rowColor);
            santeCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            santeCell.setPadding(5);
            dataTable.addCell(santeCell);

            // Battement cœur
            PdfPCell battementCell = new PdfPCell(new Phrase(String.format("%.0f", bebe.getBattementCoeur()), NORMAL_FONT));
            battementCell.setBackgroundColor(rowColor);
            battementCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            battementCell.setPadding(5);
            dataTable.addCell(battementCell);

            // Appétit
            PdfPCell appetitCell = new PdfPCell(new Phrase(bebe.getAppetitBebe(), NORMAL_FONT));
            appetitCell.setBackgroundColor(rowColor);
            appetitCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            appetitCell.setPadding(5);
            dataTable.addCell(appetitCell);
        }

        document.add(dataTable);
    }

    /**
     * Ajoute un résumé et des statistiques des données de suivi
     */
    private static void addSummaryAndStats(Document document, List<suiviBebe> listeBebes) throws Exception {
        // Calcul des statistiques
        double totalPoids = 0;
        double totalTaille = 0;
        double totalBattements = 0;
        double minPoids = Double.MAX_VALUE;
        double maxPoids = Double.MIN_VALUE;
        double minTaille = Double.MAX_VALUE;
        double maxTaille = Double.MIN_VALUE;

        for (suiviBebe bebe : listeBebes) {
            // Poids
            double poids = bebe.getPoidsBebe();
            totalPoids += poids;
            minPoids = Math.min(minPoids, poids);
            maxPoids = Math.max(maxPoids, poids);

            // Taille
            double taille = bebe.getTailleBebe();
            totalTaille += taille;
            minTaille = Math.min(minTaille, taille);
            maxTaille = Math.max(maxTaille, taille);

            // Battements
            totalBattements += bebe.getBattementCoeur();
        }

        int count = listeBebes.size();
        double moyPoids = totalPoids / count;
        double moyTaille = totalTaille / count;
        double moyBattements = totalBattements / count;

        // Créer une section pour les statistiques
        Paragraph statsTitle = new Paragraph("STATISTIQUES ET PROGRESSION", SECTION_FONT);
        statsTitle.setSpacingBefore(10);
        statsTitle.setSpacingAfter(10);
        document.add(statsTitle);

        // Créer un tableau pour les statistiques
        PdfPTable statsTable = new PdfPTable(4);
        statsTable.setWidthPercentage(100);
        statsTable.setSpacingAfter(15);

        // En-tête des statistiques
        String[] statHeaders = {"Mesure", "Minimum", "Maximum", "Moyenne"};
        for (String header : statHeaders) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(new BaseColor(75, 137, 232));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            cell.setBorderWidth(1);
            cell.setBorderColor(BaseColor.WHITE);
            statsTable.addCell(cell);
        }

        // Ligne pour le poids
        statsTable.addCell(createStatsCell("Poids (kg)", false));
        statsTable.addCell(createStatsCell(String.format("%.2f", minPoids), false));
        statsTable.addCell(createStatsCell(String.format("%.2f", maxPoids), false));
        statsTable.addCell(createStatsCell(String.format("%.2f", moyPoids), false));

        // Ligne pour la taille
        statsTable.addCell(createStatsCell("Taille (cm)", true));
        statsTable.addCell(createStatsCell(String.format("%.1f", minTaille), true));
        statsTable.addCell(createStatsCell(String.format("%.1f", maxTaille), true));
        statsTable.addCell(createStatsCell(String.format("%.1f", moyTaille), true));

        // Ligne pour les battements de cœur
        statsTable.addCell(createStatsCell("Battement Cœur", false));
        statsTable.addCell(createStatsCell("N/A", false));
        statsTable.addCell(createStatsCell("N/A", false));
        statsTable.addCell(createStatsCell(String.format("%.0f", moyBattements), false));

        document.add(statsTable);

        // Ajouter des notes ou commentaires
        Paragraph notes = new Paragraph("Notes et recommandations:", SECTION_FONT);
        notes.setSpacingBefore(10);
        notes.setSpacingAfter(5);
        document.add(notes);

        Paragraph noteContent = new Paragraph(
                "Ce rapport présente un récapitulatif du suivi du développement du bébé. " +
                        "Il est recommandé de consulter régulièrement votre médecin pour interpréter correctement " +
                        "ces données et ajuster les soins si nécessaire.", NORMAL_FONT);
        document.add(noteContent);
    }

    /**
     * Crée une cellule pour le tableau des statistiques
     */
    private static PdfPCell createStatsCell(String content, boolean alternate) {
        PdfPCell cell = new PdfPCell(new Phrase(content, NORMAL_FONT));
        cell.setBackgroundColor(alternate ? new BaseColor(240, 247, 255) : BaseColor.WHITE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        return cell;
    }

    /**
     * Affiche une alerte dans l'interface utilisateur
     */
    private static void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une confirmation pour ouvrir le fichier
     */
    private static Optional<ButtonType> confirmOpenFile(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    /**
     * Classe interne pour gérer les pieds de page
     */
    static class FooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String dateString = dateFormat.format(new Date());

            Phrase footer = new Phrase("Page " + writer.getPageNumber() + " | Généré le: " + dateString + " | Suivi de Bébé App",
                    new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.DARK_GRAY));

            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    footer,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 20, 0);

            // Ligne en bas de page
            cb.setLineWidth(0.5f);
            cb.setColorStroke(BaseColor.LIGHT_GRAY);
            cb.moveTo(document.left(), document.bottom() - 10);
            cb.lineTo(document.right(), document.bottom() - 10);
            cb.stroke();
        }
    }
}