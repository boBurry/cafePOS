package Services;

import Models.Order;
import Models.Product;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Component;
import java.io.FileOutputStream;
import javax.swing.JOptionPane;

public class ReceiptGenerator {

    public static void generatePDF(Component parentView, Order order, int orderId, double total, String payType, double cashGiven, double change) {
        String fileName = "Receipt_" + orderId + ".pdf";

        try {
            String projectPath = System.getProperty("user.dir");
            java.io.File folder = new java.io.File(projectPath + java.io.File.separator + "Receipts");

            if (!folder.exists()) {
                folder.mkdirs();
            }

            com.itextpdf.text.Rectangle pageSize = new com.itextpdf.text.Rectangle(226, 500);
            Document document = new Document(pageSize, 10, 10, 10, 10);

            PdfWriter.getInstance(document, new FileOutputStream(new java.io.File(folder, fileName)));

            document.open();

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

            Paragraph header = new Paragraph("SRR CAFE SHOP", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("Phnom Penh, Cambodia", normalFont);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(subHeader);

            Paragraph date = new Paragraph("Date: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date()), normalFont);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(5);
            document.add(date);

            Paragraph line = new Paragraph("---------------------------------------------", normalFont);
            line.setAlignment(Element.ALIGN_CENTER);
            document.add(line);

            Paragraph oid = new Paragraph("Order ID: " + orderId, boldFont);
            oid.setAlignment(Element.ALIGN_LEFT);
            oid.setSpacingAfter(5);
            document.add(oid);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1f, 2f}); // Name, Qty, Price

            for (Product p : order.getProducts()) {
                // Name
                PdfPCell cellName = new PdfPCell(new Phrase(p.getName(), normalFont));
                cellName.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                table.addCell(cellName);

                // Qty
                PdfPCell cellQty = new PdfPCell(new Phrase("x" + p.getQuantity(), normalFont));
                cellQty.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                cellQty.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellQty);

                // Price
                PdfPCell cellPrice = new PdfPCell(new Phrase(String.format("$%.2f", p.getTotal()), normalFont));
                cellPrice.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                cellPrice.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cellPrice);

                if (!p.getCustomizationDetails().isEmpty()) {
                    PdfPCell cellCust = new PdfPCell(new Phrase("  " + p.getShortCustomization(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                    cellCust.setColspan(3);
                    cellCust.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                    table.addCell(cellCust);
                }
            }
            document.add(table);

            // Totals
            document.add(line);

            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);

            double subtotal = order.calculateSubtotal(); 
            addTextPair(totalTable, "Subtotal:", String.format("$%.2f", subtotal), normalFont);

            if (subtotal > total) {
                double discountAmount = subtotal - total;
                addTextPair(totalTable, "Discount:", String.format("-$%.2f", discountAmount), normalFont);
            }

            addTextPair(totalTable, "TOTAL:", String.format("$%.2f", total), boldFont);
            addTextPair(totalTable, "Payment:", payType, normalFont);

            if (payType.equals("Cash")) {
                addTextPair(totalTable, "Cash Given:", String.format("$%.2f", cashGiven), normalFont);
                addTextPair(totalTable, "Change:", String.format("$%.2f", change), normalFont);
            }

            document.add(totalTable);
            document.add(line);

            // Footer
            Paragraph footer = new Paragraph("THANK YOU!\nPLEASE COME AGAIN", boldFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(10);
            document.add(footer);

            document.close();

            JOptionPane.showMessageDialog(parentView, "PDF Saved Successfully!\nLocation: " + folder.getAbsolutePath());

            // Auto-Open the file
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(new java.io.File(folder, fileName));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentView, "PDF Error: " + e.getMessage());
        }
    }

    private static void addTextPair(PdfPTable table, String left, String right, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(left, font));
        c1.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell c2 = new PdfPCell(new Phrase(right, font));
        c2.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(c1);
        table.addCell(c2);
    }
}