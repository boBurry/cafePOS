package Controllers;

import Views.GUI;
import Models.Order;
import Models.Product;
import Views.DrinkCustomizationDialog;
import Views.CartTableModel; 
import Models.db;
import java.awt.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

// --- NEW IMPORTS FOR PDF ---
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.Phrase;
import java.io.FileOutputStream;

public class Controller {

    private GUI view;   
    private Order order; 

    public Controller(GUI view, Order order) {
        this.view = view;
        this.order = order;
    }

    // --- MAIN ADD LOGIC ---
    public void addToCart(String pid) {
        // 1. Fetch Base Details from DB
        Product dbProduct = getProductDetails(pid);
        if (dbProduct == null) {
            JOptionPane.showMessageDialog(view, "Error: Product ID " + pid + " not found!");
            return;
        }

        String name = dbProduct.getName();
        double currentPrice = dbProduct.getBasePrice();
        Product finalProduct = null;
        String category = dbProduct.getCategory(); 
        String type = dbProduct.getType();     

        // 2. CHECK IF FOOD (Based on Database, not ID!)
        // If the database says "Snack", we skip the customization dialog.
        boolean isFood = "Snack".equalsIgnoreCase(category); 

        if (isFood) {
            // Add immediately using DB category/type
            finalProduct = new Product(pid, name, currentPrice, 1, category, type);
        } else {
            // DRINK: Open Customization Dialog
            DrinkCustomizationDialog dialog = new DrinkCustomizationDialog(view, name, currentPrice);
            dialog.setVisible(true); 

            if (dialog.isConfirmed()) {
                int qty = dialog.getSelectedQuantity(); 

                finalProduct = new Product(
                    pid, name, currentPrice, qty,category, type,
                    dialog.getDrinkSize(), 
                    dialog.getSugarLevel(), 
                    dialog.getIceLevel(), 
                    dialog.hasExtraShot()
                );
            } else {
                return;
            }
        }

        // 3. SMART MERGE: Check for Duplicates
        if (finalProduct != null) {
            boolean found = false;
            
            for (Product p : order.getProducts()) {
                // Check if ID matches AND all Customizations match
                if (p.getId().equals(finalProduct.getId()) &&
                    p.getSize().equals(finalProduct.getSize()) &&
                    p.getSugarLevel().equals(finalProduct.getSugarLevel()) &&
                    p.getIceLevel().equals(finalProduct.getIceLevel()) &&
                    p.hasExtraShot() == finalProduct.hasExtraShot()) {
                    
                    // FOUND IDENTICAL ITEM: Combine Quantities
                    int newQty = p.getQuantity() + finalProduct.getQuantity();
                    p.setQuantity(newQty);
                    
                    found = true;
                    break; 
                }
            }

            // If not found, add as a new row
            if (!found) {
                order.addProduct(finalProduct);
            }

            // 4. REFRESH UI
            ((CartTableModel) view.getTable().getModel()).fireTableDataChanged();            
            updateSubtotal();
        }
    }
    
    // --- DELETE LOGIC ---
    public void deleteItem(int index) {
        if (index >= 0 && index < order.getProducts().size()) {
            order.removeProduct(index);
            ((CartTableModel) view.getTable().getModel()).fireTableDataChanged();
            updateSubtotal();
        }
    }
    
    // Fallback Delete
    public void deleteItem() {
        int selectedRow = view.getTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "Select a row first.");
            return;
        }
        deleteItem(selectedRow); 
    }

    // --- HELPER: Update Subtotal Label ---
    public void updateSubtotal() {
        double newSubtotal = order.calculateSubtotal();
        // Assuming your GUI has a getter for the label
        view.getLbSubtotal().setText(String.format("$%.2f", newSubtotal));
    }

    // --- DB HELPER ---
    private Product getProductDetails(String pid) {
        Product p = null;
        try {
            Connection con = db.myCon();
            System.out.println("DEBUG: Connected to database -> " + con.getCatalog());
            String sql = "SELECT * FROM product WHERE PID = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, pid);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // Returns a dummy product just to hold Name/Price
                p = new Product(pid, rs.getString("Name"), rs.getDouble("Price"), 0, rs.getString("Category"), rs.getString("Type"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return p;
    }
    
    // --- EDIT LOGIC ---
    public void editSelectedItem() {
        // 1. Check if a row is selected
        int selectedRow = view.getTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "Please select an item to edit.");
            return;
        }

        // 2. Get the Product from the Order
        Product p = order.getProducts().get(selectedRow);

        // 3. Open Dialog 
        DrinkCustomizationDialog dialog = new DrinkCustomizationDialog(view, p.getName(), p.getBasePrice());
        
        // LOAD DATA INTO DIALOG
        dialog.setInitialValues(
            p.getSize(), 
            p.getSugarLevel(), 
            p.getIceLevel(), 
            p.hasExtraShot(), 
            p.getQuantity()
        );
        
        dialog.setVisible(true);

        // 4. Save Changes if Confirmed
        if (dialog.isConfirmed()) {
            // Update the SAME product object directly
            p.setSize(dialog.getDrinkSize());
            p.setSugarLevel(dialog.getSugarLevel());
            p.setIceLevel(dialog.getIceLevel());
            p.setExtraShot(dialog.hasExtraShot());
            p.setQuantity(dialog.getSelectedQuantity());
            
            // Recalculate that product's total price logic (if you have logic for size upgrades)
            // p.updateTotal(); <--- Ensure your Product class calculates total based on these new fields

            // Refresh Table & Total
            ((CartTableModel) view.getTable().getModel()).fireTableDataChanged();
            updateSubtotal();
        }
    }
    
    // --- PAYMENT LOGIC ---
    public void initiatePayment(double total) {
        String[] options = {"Cash", "QR"};
        int choice = JOptionPane.showOptionDialog(view, "Select Payment Method", "Payment", 
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) handleCashPayment(total);
        else if (choice == 1) handleQRPayment(total);
    }

    private void handleCashPayment(double total) {
        String cashInput = JOptionPane.showInputDialog(view, "Total: $" + String.format("%.2f", total) + "\nEnter Cash Amount:", "0.00");
        if (cashInput == null) return; 

        try {
            double cashGiven = Double.parseDouble(cashInput);
            if (cashGiven < total) {
                JOptionPane.showMessageDialog(view, "Insufficient Cash!");
                return;
            }
            saveOrderToDatabase(total, "Cash", cashGiven, cashGiven - total);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Invalid Amount");
        }
    }
    
    private void handleQRPayment(double total) {
        String imagePath = "/Image/IMG_5467.jpg"; 
        java.net.URL imgURL = getClass().getResource(imagePath);

        if (imgURL != null) {
            ImageIcon qrIcon = new ImageIcon(imgURL);
            Image img = qrIcon.getImage();

            int newWidth = 340;
            int newHeight = 480;

            Image newImg = img.getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH);
            qrIcon = new ImageIcon(newImg);

            JOptionPane.showMessageDialog(view, 
                "", 
                "Scan to Pay: $" + String.format("%.2f", total), 
                JOptionPane.PLAIN_MESSAGE, 
                qrIcon
            );
        } else {
            JOptionPane.showMessageDialog(view, 
                "QR Code image not found at: " + imagePath, 
                "QR Payment", 
                JOptionPane.WARNING_MESSAGE
            );
        }

        // Save transaction
        saveOrderToDatabase(total, "QR Code", total, 0.0);
    }

    // --- SAVE TO DB ---
    private void saveOrderToDatabase(double finalTotal, String payType, double cashGiven, double change) {
        try {
            Connection con = db.myCon();
            // 1. Insert Order
            String sqlOrder = "INSERT INTO orders (total_price, payment_type, order_date) VALUES (?, ?, NOW())";
            PreparedStatement pst = con.prepareStatement(sqlOrder, java.sql.Statement.RETURN_GENERATED_KEYS);
            pst.setDouble(1, finalTotal);
            pst.setString(2, payType);
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            int newOrderId = 0;
            if (rs.next()) newOrderId = rs.getInt(1);

            // 2. Insert Items
            String sqlItem = "INSERT INTO order_items (order_id, product_pid, product_name, quantity, price, customization) VALUES (?, ?, ?, ?, ?, ?)";
            pst = con.prepareStatement(sqlItem);

            for (Product p : order.getProducts()) {
                pst.setInt(1, newOrderId);
                pst.setString(2, p.getId());
                pst.setString(3, p.getName());
                pst.setInt(4, p.getQuantity());
                pst.setDouble(5, p.getTotal());
                pst.setString(6, p.getCustomizationDetails());
                pst.addBatch();
            }
            pst.executeBatch();

            // 3. Receipt & Cleanup
            printReceipt(newOrderId, finalTotal, payType, cashGiven, change);
            
            order.clear(); 
            ((CartTableModel) view.getTable().getModel()).fireTableDataChanged();
            updateSubtotal();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Database Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void printReceipt(int orderId, double total, String payType, double cashGiven, double change) {
        int choice = JOptionPane.showConfirmDialog(
            view, 
            "Order #" + orderId + " Complete!\nDo you want to save the receipt as PDF?", 
            "Success", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            generatePDF(orderId, total, payType, cashGiven, change);
        }
    }

    private void generatePDF(int orderId, double total, String payType, double cashGiven, double change) {
        String fileName = "Receipt_" + orderId + ".pdf";

        try {
            // Gets your project folder path
            String projectPath = System.getProperty("user.dir"); 
            // Creates a path to a folder named "Receipts"
            java.io.File folder = new java.io.File(projectPath + java.io.File.separator + "Receipts");
            
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Setup PDF with the new File Path
            com.itextpdf.text.Rectangle pageSize = new com.itextpdf.text.Rectangle(226, 500);
            Document document = new Document(pageSize, 10, 10, 10, 10);
            
            PdfWriter.getInstance(document, new FileOutputStream(new java.io.File(folder, fileName)));
            
            document.open();

            // Fonts
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            
            // Header
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

            // Items Table 
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
                
                // Customization details on next line
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

            JOptionPane.showMessageDialog(view, "PDF Saved Successfully!\nLocation: " + folder.getAbsolutePath());
            
            // Auto-Open the file
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(new java.io.File(folder, fileName));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "PDF Error: " + e.getMessage());
        }
    }

    private void addTextPair(PdfPTable table, String left, String right, Font font) {
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