package Controllers;

import Views.GUI;
import Models.Order;
import Models.Product;
import Views.DrinkCustomizationDialog;
import Models.db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
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
                "Regular", "0%", "None", false // Dummies
            );
            
            tableCustomizationText = ""; // BLANK FOR UI

        } else {
            // PATH B: DRINK (Open Dialog)
            DrinkCustomizationDialog dialog = new DrinkCustomizationDialog(view, name, currentPrice);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                finalProduct = new Product(
                    pid, name, currentPrice, quantity,
                    dialog.getDrinkSize(), dialog.getSugarLevel(), dialog.getIceLevel(), dialog.hasExtraShot()
                );
                
                tableCustomizationText = finalProduct.getCustomizationDetails(); // ACTUAL TEXT FOR UI
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
        }
    }
    
    public void saveOrderToDatabase(double finalTotal) {
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = db.myCon(); 

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
                
                // --- FIX FOR DATABASE SAVE ---
                // Check if this specific item is food
                boolean isFoodItem = p.getId().equals("S01") || p.getId().equals("S02") || p.getId().equals("S03");
                
                if (isFoodItem) {
                    pst.setString(6, ""); // Save BLANK to DB
                } else {
                    pst.setString(6, p.getCustomizationDetails()); // Save Details to DB
                }
                
                pst.addBatch();
            }
            pst.executeBatch();

            // C. SUCCESS & CLEANUP
            JOptionPane.showMessageDialog(view, "Payment Successful!\nReceipt #" + newOrderId + " Saved.");

            order.clear(); 
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }
}