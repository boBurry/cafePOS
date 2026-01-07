package Controllers;

import Views.GUI;
import Models.Order;
import Models.Product;
import Views.DrinkCustomizationDialog;
import Models.db;
import java.awt.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

public class Controller {

    private GUI view;   
    private Order order; 

    public Controller(GUI view, Order order) {
        this.view = view;
        this.order = order;
    }

    public void addToCart(String pid, JSpinner qtySpinner) {
        // 1. Get Quantity
        int quantity = (Integer) qtySpinner.getValue();

        if (quantity <= 0) {
            JOptionPane.showMessageDialog(view, "Please select a quantity > 0", "Invalid", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. FETCH DETAILS FROM DB
        Product dbProduct = getProductDetails(pid);

        if (dbProduct == null) {
            JOptionPane.showMessageDialog(view, "Error: Product ID " + pid + " not found in database!");
            return;
        }

        String name = dbProduct.getName();
        double currentPrice = dbProduct.getBasePrice();
        Product finalProduct = null;
        
        String tableCustomizationText = ""; 

        // 3. CHECK IF IT IS FOOD (S01, S02, S03)
        boolean isFood = pid.equals("S01") || pid.equals("S02") || pid.equals("S03");

        if (isFood) {
            // PATH A: FOOD (Direct Add, Dummy Values)
            finalProduct = new Product(
                pid, name, currentPrice, quantity,
                "Regular", "0%", "None", false
            );
            
            tableCustomizationText = ""; // Blank For UI

        } else {
            // PATH B: DRINK (Open Dialog)
            DrinkCustomizationDialog dialog = new DrinkCustomizationDialog(view, name, currentPrice);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                finalProduct = new Product(
                    pid, name, currentPrice, quantity,
                    dialog.getDrinkSize(), dialog.getSugarLevel(), dialog.getIceLevel(), dialog.hasExtraShot()
                );
                
                tableCustomizationText = finalProduct.getCustomizationDetails();
            } else {
                return; 
            }
        }

        // 4. ADD TO ORDER & UI
        if (finalProduct != null) {
            order.addProduct(finalProduct);

            DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();
            model.addRow(new Object[] {
                finalProduct.getName(),
                String.format("$%.2f", finalProduct.getUnitFinalPrice()), 
                quantity,
                tableCustomizationText, // Blank if food
                String.format("$%.2f", finalProduct.getTotal())
            });

            qtySpinner.setValue(0);
            
            // Auto Display Subtotal
            double newSubtotal = order.calculateSubtotal();
            view.getLbSubtotal().setText(String.format("$%.2f", newSubtotal));
        }
    }
    
    private Product getProductDetails(String pid) {
        Product p = null;
        try {
            Connection con = db.myCon();
            String sql = "SELECT Name, Price FROM Product WHERE PID = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, pid);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                p = new Product(pid, rs.getString("Name"), rs.getDouble("Price"), 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return p;
    }
    
    public void deleteItem() {
        javax.swing.JTable table = view.getTable();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "Please select an item to delete.", "Delete Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        order.removeProduct(selectedRow);

        model.removeRow(selectedRow);

        double newSubtotal = order.calculateSubtotal();
        view.getLbSubtotal().setText(String.format("$%.2f", newSubtotal));

        view.getLbTotal().setText("");
    }
    
    // --- PAYMENT LOGIC ---
    public void initiatePayment(double total) {
        // 1. Ask User: Cash or QR?
        String[] options = {"Cash", "QR Code"};
        int choice = JOptionPane.showOptionDialog(view, 
                "Select Payment Method", 
                "Payment", 
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.INFORMATION_MESSAGE, 
                null, options, options[0]);

        if (choice == 0) {
            // --- OPTION A: CASH ---
            handleCashPayment(total);
        } else if (choice == 1) {
            // --- OPTION B: QR CODE ---
            handleQRPayment(total);
        }
    }

    private void handleCashPayment(double total) {
        // 1. Ask for Cash Input
        String cashInput = JOptionPane.showInputDialog(view, "Total: $" + String.format("%.2f", total) + "\nEnter Cash Amount:", "0.00");

        // If they cancel, stop
        if (cashInput == null) return; 

        double cashGiven = 0.0;
        try {
            cashGiven = Double.parseDouble(cashInput);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Invalid Amount");
            return;
        }

        if (cashGiven < total) {
            JOptionPane.showMessageDialog(view, "Insufficient Cash!");
            return;
        }

        double change = cashGiven - total;

        // 2. Save & Print
        saveOrderToDatabase(total, "Cash", cashGiven, change);
    }

    private void handleQRPayment(double total) {
        // 1. SET YOUR IMAGE PATH HERE
        // Make sure to paste your actual QR image into your 'src/Image' folder
        String imagePath = "/Image/IMG_5467.jpg"; 

        java.net.URL imgURL = getClass().getResource(imagePath);

        if (imgURL != null) {
            // Image Found: Show it in the popup
            ImageIcon qrIcon = new ImageIcon(imgURL);

            // Optional: Resize if the image is too big
            Image img = qrIcon.getImage();
            Image newImg = img.getScaledInstance(200, 200, java.awt.Image.SCALE_SMOOTH);
            qrIcon = new ImageIcon(newImg);

            JOptionPane.showMessageDialog(view, 
                "", 
                "Scan to Pay: $" + String.format("%.2f", total), 
                JOptionPane.PLAIN_MESSAGE, 
                qrIcon
            );
        } else {
            // Image Not Found: Show text fallback
            JOptionPane.showMessageDialog(view, 
                "QR Code image not found at: " + imagePath, 
                "QR Payment", 
                JOptionPane.WARNING_MESSAGE
            );
        }

        // 2. Save & Print (Cash Given = Total, Change = 0)
        saveOrderToDatabase(total, "QR Code", total, 0.0);
    }

    // FINAL DB SAVE METHOD
    private void saveOrderToDatabase(double finalTotal, String payType, double cashGiven, double change) {
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = db.myCon();

            // 1. INSERT INTO ORDERS TABLE
            // We added 'payment_type' to the SQL query
            String sqlOrder = "INSERT INTO orders (total_price, payment_type, order_date) VALUES (?, ?, NOW())";
            pst = con.prepareStatement(sqlOrder, java.sql.Statement.RETURN_GENERATED_KEYS);
            pst.setDouble(1, finalTotal);
            pst.setString(2, payType); // Saves "Cash" or "QR Code"
            pst.executeUpdate();

            // 2. GET THE NEW ORDER ID (For the receipt)
            rs = pst.getGeneratedKeys();
            int newOrderId = 0;
            if (rs.next()) {
                newOrderId = rs.getInt(1);
            }

            // 3. INSERT INTO ORDER_ITEMS TABLE
            String sqlItem = "INSERT INTO order_items (order_id, product_pid, product_name, quantity, price, customization) VALUES (?, ?, ?, ?, ?, ?)";
            pst = con.prepareStatement(sqlItem);

            for (Product p : order.getProducts()) {
                pst.setInt(1, newOrderId);
                pst.setString(2, p.getId());
                pst.setString(3, p.getName());
                pst.setInt(4, p.getQuantity());
                pst.setDouble(5, p.getTotal());

                // Check if it is Food (S01, S02, S03)
                boolean isFoodItem = p.getId().equals("S01") || p.getId().equals("S02") || p.getId().equals("S03");

                if (isFoodItem) {
                    pst.setString(6, ""); // Save BLANK for food
                } else {
                    pst.setString(6, p.getCustomizationDetails()); // Save DETAILS for drinks
                }

                pst.addBatch();
            }
            pst.executeBatch();

            // 4. SHOW RECEIPT POPUP
            printReceipt(newOrderId, finalTotal, payType, cashGiven, change);

            // 5. CLEANUP (Reset the app for the next customer)
            order.clear(); 
            DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();
            model.setRowCount(0); 
            view.getLbSubtotal().setText("");
            view.getLbTotal().setText("");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Database Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
                // We do NOT close 'con' here because your db.java uses a shared static connection
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    // Updated Receipt Method
    private void printReceipt(int orderId, double total, String payType, double cashGiven, double change) {
        StringBuilder sb = new StringBuilder();
        sb.append("       MINI POS RECEIPT       \n");
        sb.append("       Order ID: ").append(orderId).append("\n");
        sb.append("------------------------------\n");

        for (Product p : order.getProducts()) {
            String lineItem = String.format("%-18s x%d  $%.2f\n", 
                p.getName(), 
                p.getQuantity(), 
                p.getTotal()
            );
            sb.append(lineItem);
        }

        sb.append("------------------------------\n");
        sb.append(String.format("TOTAL : $%.2f\n", total));
        sb.append("TYPE  : ").append(payType).append("\n"); // Shows Cash or QR

        if (payType.equals("Cash")) {
            sb.append(String.format("CASH  : $%.2f\n", cashGiven));
            sb.append(String.format("CHANGE: $%.2f\n", change));
        }

        sb.append("------------------------------\n");
        sb.append("    THANK YOU! COME AGAIN     \n");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 14));
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(300, 400));

        JOptionPane.showMessageDialog(view, scrollPane, "Receipt", JOptionPane.PLAIN_MESSAGE);
    }
}