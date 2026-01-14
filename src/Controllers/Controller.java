package Controllers;

import Views.GUI;
import Models.Order;
import Models.Product;
import Views.DrinkCustomizationDialog;
import Views.CartTableModel; 
import Models.db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

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
        
        // 2. Determine if Food or Drink
        // (Assuming IDs starting with 'S' are Snacks/Food)
        boolean isFood = pid.startsWith("S"); 

        if (isFood) {
            // FOOD: Add 1 immediately. Fast.
            // (Pass empty strings for customization fields if your constructor requires them)
            finalProduct = new Product(pid, name, currentPrice, 1);
        } else {
            // DRINK: Open Customization Dialog
            DrinkCustomizationDialog dialog = new DrinkCustomizationDialog(view, name, currentPrice);
            dialog.setVisible(true); // Logic pauses here until dialog closes

            if (dialog.isConfirmed()) {
                // --- FIX: Get Quantity from Dialog ---
                int qty = dialog.getSelectedQuantity(); 

                finalProduct = new Product(
                    pid, name, currentPrice, qty, // Use the selected quantity
                    dialog.getDrinkSize(), 
                    dialog.getSugarLevel(), 
                    dialog.getIceLevel(), 
                    dialog.hasExtraShot()
                );
            } else {
                return; // User Cancelled
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
            
            // REMOVED: qtySpinner.setValue(0); (It doesn't exist anymore)
            
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
            String sql = "SELECT Name, Price FROM Product WHERE PID = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, pid);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // Returns a dummy product just to hold Name/Price
                p = new Product(pid, rs.getString("Name"), rs.getDouble("Price"), 0);
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

        // 3. Open Dialog (Pre-filled)
        DrinkCustomizationDialog dialog = new DrinkCustomizationDialog(view, p.getName(), p.getBasePrice());
        
        // LOAD DATA INTO DIALOG
        dialog.setInitialValues(
            p.getSize(), 
            p.getSugarLevel(), 
            p.getIceLevel(), 
            p.hasExtraShot(), 
            p.getQuantity()
        );
        
        dialog.setVisible(true); // Wait for user...

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
            
            con.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Database Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printReceipt(int orderId, double total, String payType, double cashGiven, double change) {
        // You can add your receipt printing logic here
        JOptionPane.showMessageDialog(view, "Payment Successful!\nOrder ID: " + orderId);
    }
}   