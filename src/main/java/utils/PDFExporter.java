package utils;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import entities.Evenement;
import entities.ParticipationEvenement;

import java.io.IOException;
import java.util.List;

public class PDFExporter {

    // Couleurs du thème NovaLearn (Teal/Turquoise)
    private static final DeviceRgb TURQUOISE = new DeviceRgb(45, 212, 191);
    private static final DeviceRgb DARK = new DeviceRgb(10, 13, 18);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(243, 244, 246);
    private static final DeviceRgb WHITE = new DeviceRgb(255, 255, 255);

    public static void exportEventData(Evenement event, List<ParticipationEvenement> participants, String filePath) throws IOException {
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Header "NovaLearn"
            document.add(new Paragraph("NovaLearn")
                    .setFontSize(28)
                    .setBold()
                    .setFontColor(TURQUOISE)
                    .setTextAlignment(TextAlignment.RIGHT));

            // Title
            document.add(new Paragraph("Rapport de l'Événement")
                    .setFontSize(22)
                    .setBold()
                    .setFontColor(DARK));

            document.add(new Paragraph(" "));

            // Event Details
            document.add(new Paragraph("Détails de l'événement:")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(TURQUOISE));

            document.add(new Paragraph("• Titre: " + event.getTitre()));
            document.add(new Paragraph("• Lieu: " + event.getLieu()));
            document.add(new Paragraph("• Type: " + event.getType_evenement()));
            document.add(new Paragraph("• Période: Du " + event.getDate_debut() + " au " + event.getDate_fin()));
            
            document.add(new Paragraph(" "));

            // Participants Table
            document.add(new Paragraph("Liste des Participants:")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(TURQUOISE));

            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 3, 4, 2}))
                    .useAllAvailableWidth();

            // Table Header
            String[] headers = {"Nom", "Prénom", "Email", "Statut"};
            for (String h : headers) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold())
                        .setBackgroundColor(TURQUOISE)
                        .setFontColor(WHITE)
                        .setPadding(8));
            }

            // Table Rows
            for (ParticipationEvenement p : participants) {
                table.addCell(new Cell().add(new Paragraph(p.getNom())).setPadding(6));
                table.addCell(new Cell().add(new Paragraph(p.getPrenom())).setPadding(6));
                table.addCell(new Cell().add(new Paragraph(p.getEmail() != null ? p.getEmail() : "-")).setPadding(6));
                table.addCell(new Cell().add(new Paragraph(p.getStatut())).setPadding(6));
            }

            document.add(table);
        }
    }

    public static void exportParticipationList(List<ParticipationEvenement> participants, String filePath) throws IOException {
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Header "NovaLearn"
            document.add(new Paragraph("NovaLearn")
                    .setFontSize(28)
                    .setBold()
                    .setFontColor(TURQUOISE)
                    .setTextAlignment(TextAlignment.RIGHT));

            // Title
            document.add(new Paragraph("Registre des Participations")
                    .setFontSize(22)
                    .setBold()
                    .setFontColor(DARK));

            document.add(new Paragraph(" "));

            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 3, 3, 2}))
                    .useAllAvailableWidth();

            // Header
            String[] headers = {"Nom", "Prénom", "Email", "Événement", "Statut"};
            for (String h : headers) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold())
                        .setBackgroundColor(TURQUOISE)
                        .setFontColor(WHITE)
                        .setPadding(8));
            }

            // Body
            for (ParticipationEvenement p : participants) {
                table.addCell(new Cell().add(new Paragraph(p.getNom())).setPadding(6));
                table.addCell(new Cell().add(new Paragraph(p.getPrenom())).setPadding(6));
                table.addCell(new Cell().add(new Paragraph(p.getEmail() != null ? p.getEmail() : "-")).setPadding(6));
                table.addCell(new Cell().add(new Paragraph(p.getTitreEvenement() != null ? p.getTitreEvenement() : "-")).setPadding(6));
                table.addCell(new Cell().add(new Paragraph(p.getStatut())).setPadding(6));
            }

            document.add(table);

            // Footer
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Généré par NovaLearn Management System le " + java.time.LocalDate.now())
                    .setFontSize(10)
                    .setFontColor(new DeviceRgb(128, 128, 128))
                    .setTextAlignment(TextAlignment.RIGHT));
        }
    }
}
