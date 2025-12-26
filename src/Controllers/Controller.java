package Controllers;

import OOP.GUI;
import OOP.Order;
import OOP.Product;
import OOP.DrinkCustomizationDialog;
import OOP.db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.table.DefaultTableModel;

public class Controller {

    private GUI view;   // Reference to the UI
    private Order order; // Reference to the Data Logic

    // Constructor: connect the View and Model
    public Controller(GUI view, Order order) {
        this.view = view;
        this.order = order;
    }

    // --- LOGIC 1: ADD TO CART ---
    public void addToCart(String pid, String name, JSpinner qtySpinner) {
        // 1. Get Quantity
        int quantity = (Integer) qtySpinner.getValue();

        if (quantity <= 0) {
            JOptionPane.showMessageDialog(view, "Please select a quantity > 0", "Invalid", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. FETCH PRICE FROM DATABASE
        double currentPrice = getPriceFromDB(pid);

        if (currentPrice == 0.0) {
            JOptionPane.showMessageDialog(view, "Error: Product ID " + pid + " not found or price is 0!");
            return;
        }

        // 3. Open Customization Dialog
        // Note: We pass 'view' as the parent Frame
        DrinkCustomizationDialog dialog = new DrinkCustomizationDialog(view, name, currentPrice);
        dialog.setVisible(true);

        // 4. If Confirmed, Update Order and UI
        if (dialog.isConfirmed()) {
            Product product = new Product(
                pid,
                name,
                currentPrice,
                quantity,
                dialog.getDrinkSize(),
                dialog.getSugarLevel(),
                dialog.getIceLevel(),
                dialog.hasExtraShot()
            );

            // Add to Logic (Order object)
            order.addProduct(product);

            // Add to UI (Table) - Using the getter we created!
            DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();
            model.addRow(new Object[] {
                product.getName(),
                String.format("$%.2f", product.getUnitFinalPrice()), 
                quantity,
                product.getCustomizationDetails(),
                String.format("$%.2f", product.getTotal())
            });
            
            // Reset the spinner
            qtySpinner.setValue(0);
        }
    }

    // --- LOGIC 2: SAVE TO DATABASE ---
    public void saveOrderToDatabase(double finalTotal) {
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = db.myCon(); // Use your existing DB connection class

            // A. SAVE ORDER HEAD
            String sqlOrder = "INSERT INTO orders (total_price, order_date) VALUES (?, NOW())";
            pst = con.prepareStatement(sqlOrder, java.sql.Statement.RETURN_GENERATED_KEYS);
            pst.setDouble(1, finalTotal);
            pst.executeUpdate();

            rs = pst.getGeneratedKeys();
            int newOrderId = 0;
            if (rs.next()) {
                newOrderId = rs.getInt(1);
            }

            // B. SAVE ORDER ITEMS
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

            // C. SUCCESS & CLEANUP
            JOptionPane.showMessageDialog(view, "Payment Successful!\nReceipt #" + newOrderId + " Saved.");

            // Clear Logic
            order.clear(); 

            // Clear UI using Getters
            DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();
            model.setRowCount(0); 
            view.getLbSubtotal().setText("");
            view.getLbTotal().setText("");

        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(view, "Database Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Helper method (Private, only used by Controller)
    private double getPriceFromDB(String pid) {
        double price = 0.0;
        try {
            Connection con = db.myCon();
            String sql = "SELECT Price FROM Product WHERE PID = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, pid);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                price = rs.getDouble("Price");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Error fetching price: " + e.getMessage());
        }
        return price;
    }
}