package services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import entities.Etudiant;
import entities.Quiz;
import entities.ResultatQuiz;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfService {

    // ── Palette couleurs app ─────────────────────────────────────
    private static final DeviceRgb TEAL       = new DeviceRgb(15, 118, 110);
    private static final DeviceRgb TEAL_LIGHT = new DeviceRgb(240, 253, 250);
    private static final DeviceRgb SUCCESS    = new DeviceRgb(22, 163, 74);
    private static final DeviceRgb WARNING    = new DeviceRgb(202, 138, 4);
    private static final DeviceRgb DANGER     = new DeviceRgb(220, 38, 38);
    private static final DeviceRgb GRAY       = new DeviceRgb(170, 180, 195);
    private static final DeviceRgb DARK       = new DeviceRgb(30, 41, 59);
    private static final DeviceRgb WHITE      = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb ROW_ALT    = new DeviceRgb(248, 250, 252);

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Export résultats (pour l'enseignant) ─────────────────────
    public void exporterResultats(List<ResultatQuiz> resultats,
                                  Quiz quiz,
                                  List<Etudiant> etudiants,
                                  String cheminFichier) {
        try (PdfWriter writer = new PdfWriter(cheminFichier);
             PdfDocument pdf  = new PdfDocument(writer);
             Document doc     = new Document(pdf)) {

            // En-tête
            doc.add(new Paragraph("Résultats — " + quiz.getTitre())
                    .setFontSize(22).setBold()
                    .setFontColor(TEAL));

            doc.add(new Paragraph(
                    "Type : " + quiz.getTypeQuiz()
                            + "   |   Durée : " + quiz.getDureeMinutes() + " min"
                            + "   |   Généré le : " + java.time.LocalDate.now())
                    .setFontSize(11).setFontColor(GRAY));

            doc.add(new Paragraph(" "));

            // Tableau
            Table table = new Table(UnitValue.createPercentArray(
                    new float[]{3, 3, 2, 2, 2, 2}))
                    .useAllAvailableWidth();

            // En-têtes colonnes
            for (String h : List.of("Nom", "Prénom",
                    "Matricule", "Score (%)",
                    "Points", "Mention")) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold())
                        .setBackgroundColor(TEAL)
                        .setFontColor(WHITE)
                        .setPadding(8));
            }

            // Lignes
            boolean pair = false;
            for (ResultatQuiz r : resultats) {

                // Trouver l'étudiant correspondant
                Etudiant e = etudiants.stream()
                        .filter(et -> et.getId() != null
                                && et.getId().intValue() == r.getIdEtudiant())
                        .findFirst().orElse(null);

                String nom      = e != null ? e.getNom()       : "—";
                String prenom   = e != null ? e.getPrenom()    : "—";
                String matricule = e != null ? e.getMatricule() : "—";

                DeviceRgb rowColor = pair ? ROW_ALT : WHITE;
                pair = !pair;

                int    scorePct  = r.getScore().intValue();
                String mention   = getMention(scorePct);
                DeviceRgb mentionColor = getMentionColor(scorePct);

                table.addCell(cellule(nom,      rowColor, DARK,         false));
                table.addCell(cellule(prenom,   rowColor, DARK,         false));
                table.addCell(cellule(matricule,rowColor, DARK,         false));
                table.addCell(cellule(scorePct + "%", rowColor, DARK,   false));
                table.addCell(cellule(
                        r.getEarnedPoints() + "/" + r.getTotalPoints(),
                        rowColor, DARK, false));
                table.addCell(cellule(mention,  rowColor, mentionColor, true));
            }

            doc.add(table);

            // Statistiques
            double moyenne = resultats.stream()
                    .mapToDouble(ResultatQuiz::getScore)
                    .average().orElse(0);
            double meilleur = resultats.stream()
                    .mapToDouble(ResultatQuiz::getScore)
                    .max().orElse(0);
            double plusBas  = resultats.stream()
                    .mapToDouble(ResultatQuiz::getScore)
                    .min().orElse(0);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(
                    "Moyenne : " + String.format("%.1f", moyenne) + "%"
                            + "   |   Meilleur : " + String.format("%.1f", meilleur) + "%"
                            + "   |   Plus bas : " + String.format("%.1f", plusBas) + "%"
                            + "   |   Étudiants : " + resultats.size())
                    .setBold().setFontSize(12).setFontColor(TEAL));

            // Pied de page
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Campus Access — Quiz Manager")
                    .setFontSize(10).setFontColor(GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Export correction individuelle (pour l'étudiant) ─────────
    public void exporterCorrectionEtudiant(ResultatQuiz resultat,
                                           Etudiant etudiant,
                                           String cheminFichier) {
        try (PdfWriter writer = new PdfWriter(cheminFichier);
             PdfDocument pdf  = new PdfDocument(writer);
             Document doc     = new Document(pdf)) {

            int scorePct = resultat.getScore().intValue();

            // En-tête
            doc.add(new Paragraph(
                    "Correction — " + resultat.getQuiz().getTitre())
                    .setFontSize(20).setBold().setFontColor(TEAL));

            doc.add(new Paragraph(
                    "Étudiant : " + etudiant.getNomComplet()
                            + "   |   Matricule : " + etudiant.getMatricule())
                    .setFontSize(12).setFontColor(DARK));

            doc.add(new Paragraph(
                    "Passé le : " + resultat.getDatePassation().format(FMT))
                    .setFontSize(11).setFontColor(GRAY));

            doc.add(new Paragraph(" "));

            // Score
            doc.add(new Paragraph(
                    resultat.getEarnedPoints() + " / " + resultat.getTotalPoints()
                            + "  pts  —  " + scorePct + "%  —  " + getMention(scorePct))
                    .setFontSize(18).setBold()
                    .setFontColor(getMentionColor(scorePct)));

            doc.add(new Paragraph(" "));

            // Pied de page
            doc.add(new Paragraph("Campus Access — Quiz Manager")
                    .setFontSize(10).setFontColor(GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────
    private Cell cellule(String texte, DeviceRgb bg,
                         DeviceRgb couleurTexte, boolean bold) {
        Paragraph p = new Paragraph(texte).setFontColor(couleurTexte);
        if (bold) p.setBold();
        return new Cell().add(p)
                .setBackgroundColor(bg)
                .setPadding(7);
    }

    private String getMention(int score) {
        if (score >= 85) return "Très bien";
        if (score >= 70) return "Bien";
        if (score >= 50) return "Passable";
        return "Insuffisant";
    }

    private DeviceRgb getMentionColor(int score) {
        if (score >= 70) return SUCCESS;
        if (score >= 50) return WARNING;
        return DANGER;
    }
}