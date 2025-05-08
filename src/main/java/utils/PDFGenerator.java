package utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import models.Ordonnance;

import java.io.FileOutputStream;
import java.io.IOException;

public class PDFGenerator {

    public static void generatePDF(String filePath, Ordonnance ordonnance) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Header with background color
        PdfPTable headerTable = new PdfPTable(1);
        PdfPCell headerCell = new PdfPCell(new Phrase("Ordonnance Médicale", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.WHITE)));
        headerCell.setBackgroundColor(new BaseColor(63, 81, 181)); // Indigo blue
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPadding(20f);
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(headerCell);
        headerTable.setSpacingAfter(30f);
        document.add(headerTable);

        // Details table
        PdfPTable table = new PdfPTable(2); // 2 columns
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        addStyledRow(table, "ID", String.valueOf(ordonnance.getId()));
        addStyledRow(table, "Date de prescription", ordonnance.getDatePrescription());
        addStyledRow(table, "Nom du patient", ordonnance.getPatientName());
        addStyledRow(table, "Médicament", ordonnance.getMedicament());
        addStyledRow(table, "Posologie", ordonnance.getPosologie());

        document.add(table);

        // Footer
        Paragraph footer = new Paragraph("Document généré par l'application BabySteps", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(50);
        document.add(footer);

        document.close();
    }

    private static void addStyledRow(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.DARK_GRAY);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(new BaseColor(224, 224, 224));
        labelCell.setPadding(10f);
        labelCell.setBorderColor(BaseColor.LIGHT_GRAY);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(10f);
        valueCell.setBorderColor(BaseColor.LIGHT_GRAY);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
