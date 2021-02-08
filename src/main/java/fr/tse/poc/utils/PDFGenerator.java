package fr.tse.poc.utils;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import fr.tse.poc.domain.TimeCheck;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component("pdfGenerator")
public class PDFGenerator {
    private static final Font COURIER = new Font(Font.FontFamily.COURIER, 20, Font.BOLD);
    private static final Font COURIER_SMALL = new Font(Font.FontFamily.COURIER, 16, Font.BOLD);
    private static final Font COURIER_SMALL_FOOTER = new Font(Font.FontFamily.COURIER, 12, Font.BOLD);
    private final String pdfDir = "/PdfReportRepo/";
    private final String reportFileName = "Times-Report";
    private final String reportFileNameDateFormat = "yyyy MM dd";
    private final String localDateFormat = "dd MMMM yyyy HH:mm:ss";
    private final String logoImgPath = "/img_TSE_logo.jpg";
    private final Float[] logoImgScale = {50f, 50f};
    private final int noOfColumns = 4;
    private final List<String> columnNames = Arrays.asList("Id", "Time", "User", "Project");

    private static void leaveEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    public String generatePdfReport(Set<TimeCheck> listTime) {

        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(getPdfNameWithDate()));
            document.open();
            addLogo(document);
            addDocTitle(document);
            createTable(document, noOfColumns, listTime);
            addFooter(document);
            document.close();
            System.out.println("------------------Your PDF Report is ready!-------------------------");

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }

        return getPdfNameWithDate();

    }

    private void addLogo(Document document) throws IOException, DocumentException {
        Path resourceDirectory = Paths.get("src", "main", "resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        Image img = Image.getInstance(absolutePath + pdfDir + logoImgPath);
        img.scalePercent(logoImgScale[0], logoImgScale[1]);
        img.setAlignment(Element.ALIGN_RIGHT);
        document.add(img);
    }

    private void addDocTitle(Document document) throws DocumentException {
        String localDateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern(localDateFormat));
        Paragraph p1 = new Paragraph();
        leaveEmptyLine(p1, 1);
        p1.add(new Paragraph(reportFileName, COURIER));
        p1.setAlignment(Element.ALIGN_CENTER);
        leaveEmptyLine(p1, 1);
        p1.add(new Paragraph("Rapport généré le : " + localDateString, COURIER_SMALL));

        document.add(p1);
    }

    private void createTable(Document document, int noOfColumns, Set<TimeCheck> listTime) throws DocumentException {
        Paragraph paragraph = new Paragraph();
        leaveEmptyLine(paragraph, 3);
        document.add(paragraph);

        PdfPTable table = new PdfPTable(noOfColumns);

        for (int i = 0; i < noOfColumns; i++) {
            PdfPCell cell = new PdfPCell(new Phrase(columnNames.get(i)));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(BaseColor.CYAN);
            table.addCell(cell);
        }

        table.setHeaderRows(1);
        getDbData(table, listTime);
        document.add(table);
    }

    private void getDbData(PdfPTable table, Set<TimeCheck> listTime) {
        for (TimeCheck time : listTime) {

            table.setWidthPercentage(100);
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

            table.addCell(time.getId().toString());
            table.addCell(String.valueOf(time.getTime()));
            table.addCell(time.getUser().getFullName());
            table.addCell(time.getProject().getName());
        }
    }

    private void addFooter(Document document) throws DocumentException {
        Paragraph p2 = new Paragraph();
        leaveEmptyLine(p2, 3);
        p2.setAlignment(Element.ALIGN_MIDDLE);
        p2.add(new Paragraph(
                "------------------------End Of " + reportFileName + "------------------------",
                COURIER_SMALL_FOOTER));

        document.add(p2);
    }

    private String getPdfNameWithDate() {
        Path resourceDirectory = Paths.get("src", "main", "resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();

        String localDateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy MM dd"));
        return absolutePath + pdfDir + reportFileName + "-" + localDateString + ".pdf";
    }
}

