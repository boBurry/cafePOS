package Controllers;

import Models.db;
import Views.AdminView;
import Views.AddProductDialog;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AdminController {

    private AdminView view;
    private Connection connection;

    public AdminController(AdminView view) {
        this.view = view;
        this.connection = db.myCon();

        loadData(""); 
        loadHistoryData("");
        loadInventory();
        
        // Ingredient Table Listener
        this.view.getTblIngredient().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = view.getTblIngredient().getSelectedRow();
                if(row >= 0) {
                    try {
                        // 1. Text Fields
                        view.getTfIngName().setText(view.getTblIngredient().getValueAt(row, 1).toString());
                        view.getCbIngCategory().setSelectedItem(view.getTblIngredient().getValueAt(row, 2).toString());
                        view.getTfIngQty().setText(view.getTblIngredient().getValueAt(row, 3).toString());
                        
                        String priceStr = view.getTblIngredient().getValueAt(row, 4).toString().replace("$", "");
                        view.getTfIngPrice().setText(priceStr);
                        
                        // 2. Date Choosers (Parse String -> Date)
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        
                        // Set Bought Date
                        String boughtStr = view.getTblIngredient().getValueAt(row, 6).toString();
                        view.getDcIngBought().setDate(sdf.parse(boughtStr));
                        
                        // Set Expiry Date (Check for null first)
                        Object expiryObj = view.getTblIngredient().getValueAt(row, 7);
                        if (expiryObj != null && !expiryObj.toString().isEmpty()) {
                            view.getDcIngExpiry().setDate(sdf.parse(expiryObj.toString()));
                        } else {
                            view.getDcIngExpiry().setDate(null);
                        }
                        
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        this.view.getBtnIngAdd().addActionListener(e -> addInventoryItem());
        this.view.getBtnIngUpdate().addActionListener(e -> updateInventoryItem()); 
        this.view.getBtnIngDelete().addActionListener(e -> deleteInventoryItem());
        this.view.getBtnIngClear().addActionListener(e -> clearInventoryForm());

        // Product
        this.view.getBtnAdd().addActionListener(e -> showAddDialog());
        this.view.getBtnUpdate().addActionListener(e -> showUpdateDialog());
        this.view.getBtnDelete().addActionListener(e -> deleteData());
        this.view.getBtnClear().addActionListener(e-> {
            view.clear();
            loadData("");
        });

        this.view.getTfSearch().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { filterData(); }
        });
        this.view.getCbTypeFilter().addActionListener(e -> filterData());

        // History Search
        this.view.getBtnHistorySearch().addActionListener(e -> {
            java.util.Date rawDate = view.getDcHistoryDate().getDate();
            String dateStr = "";
            
            if (rawDate != null) {
                dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(rawDate);
            }
            loadHistoryData(dateStr);
        });
        
        // History Refresh
        this.view.getBtnHistoryRefresh().addActionListener(e -> {
            
            view.getDcHistoryDate().setDate(new java.util.Date()); 
            loadHistoryData("");
        });
    }
    
    // History 
    private void loadHistoryData(String dateFilter) {
        DefaultTableModel model = (DefaultTableModel) view.getHistoryTable().getModel();
        model.setRowCount(0); 
        
        double totalRev = 0;
        int orderCount = 0;
        String sql;

        try {
            PreparedStatement p;
            
            if (dateFilter.isEmpty()) {
                sql = "SELECT * FROM orders ORDER BY order_date DESC LIMIT 50";
                p = connection.prepareStatement(sql);
            } else {
                sql = "SELECT * FROM orders WHERE DATE(order_date) = ? ORDER BY order_date DESC";
                p = connection.prepareStatement(sql);
                p.setString(1, dateFilter);
            }

            ResultSet rs = p.executeQuery();

            while(rs.next()) {
                String id = rs.getString("order_id");
                String date = rs.getString("order_date");
                double price = rs.getDouble("total_price");
                String type = rs.getString("payment_type");

                model.addRow(new Object[]{ id, date, String.format("$%.2f", price), type });
                
                totalRev += price;
                orderCount++;
            }
            
            // Update Summary Labels
            view.getLbHistoryTotal().setText(String.format("Total: $%.2f", totalRev));
            view.getLbHistoryCount().setText("Orders: " + orderCount);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Product
    private void showAddDialog() {
        AddProductDialog dialog = new AddProductDialog(view, "Add New Product");
        dialog.setVisible(true); 

        if (dialog.isConfirmed()) {
            insertProduct(dialog);
        }
    }

    private void showUpdateDialog() {
        int row = view.getTable().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(view, "Please select a product to update.");
            return;
        }
        
        String id = view.getTable().getValueAt(row, 0).toString();
        String name = view.getTable().getValueAt(row, 1).toString();
        String cat = view.getTable().getValueAt(row, 2).toString();
        String type = view.getTable().getValueAt(row, 3).toString();
        String price = view.getTable().getValueAt(row, 4).toString();

        AddProductDialog dialog = new AddProductDialog(view, "Update Product");
        dialog.setProductData(id, name, cat, type, price);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            updateProduct(dialog);
        }
    }

    private void insertProduct(AddProductDialog dialog) {
        try {
            PreparedStatement p = connection.prepareStatement("INSERT INTO product VALUES(?,?,?,?,?)");
            p.setString(1, dialog.getId());
            p.setString(2, dialog.getName());
            p.setString(3, dialog.getCategory());
            p.setString(4, dialog.getProductType());
            p.setDouble(5, Double.parseDouble(dialog.getPrice()));
            p.executeUpdate();
            
            if (dialog.getSelectedImage() != null) {
                System.out.println("DEBUG: Image found! Saving now...");
                saveImage(dialog.getSelectedImage(), dialog.getId());
            } else {
                System.out.println("DEBUG: No image was selected.");
            }
            
            filterData();
            JOptionPane.showMessageDialog(view, "Added Successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Error: " + e.getMessage());
        }
    }

    private void updateProduct(AddProductDialog dialog) {
        try {
            PreparedStatement p = connection.prepareStatement(
                "UPDATE product SET name=?, category=?, type=?, price=? WHERE pid=?");
            p.setString(1, dialog.getName());
            p.setString(2, dialog.getCategory());
            p.setString(3, dialog.getProductType());
            p.setDouble(4, Double.parseDouble(dialog.getPrice()));
            p.setString(5, dialog.getId()); 
            
            p.executeUpdate();
            
            if (dialog.getSelectedImage() != null) {
                saveImage(dialog.getSelectedImage(), dialog.getId());
            }
            
            filterData();
            JOptionPane.showMessageDialog(view, "Updated Successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Error: " + e.getMessage());
        }
    }

    private void deleteData() {
        int row = view.getTable().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(view, "Select a row to delete.");
            return;
        }
        
        String id = view.getTable().getValueAt(row, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(view, "Delete Product " + id + "?");
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement p = connection.prepareStatement("DELETE FROM product WHERE pid=?");
                p.setString(1, id);
                p.executeUpdate();
                filterData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(view, "Error: " + e.getMessage());
            }
        }
    }

    private void filterData() {
        String search = view.getTfSearch().getText();
        String type = view.getCbTypeFilter().getSelectedItem().toString();
        String sql = "SELECT * FROM product WHERE (pid LIKE ? OR name LIKE ?)";
        
        if (!type.equals("All")) sql += " AND type = '" + type + "'";
        loadData(sql, "%" + search + "%");
    }
    
    // --- SAVE IMAGE ---
    private void saveImage(File source, String pid) {
        try {
            // 1. Get the path and print it
            String projectPath = System.getProperty("user.dir"); 
            // Project Path is: /Users/sunsovisal/NetBeansProjects/ITC-I3
            
            // 2. Define the folder
            File folder = new File(projectPath + File.separator + "product_images");
            // Target Folder: /Users/sunsovisal/NetBeansProjects/ITC-I3/product_images
            
            // 3. Create it if missing (with check)
            if (!folder.exists()) {
                boolean created = folder.mkdirs();       
                System.out.println("DEBUG: Folder did not exist. Created? " + created);
            } else {
                System.out.println("DEBUG: Folder already exists.");
            } 
            
            // 4. Clean ID and define destination
            String cleanId = pid.trim().toUpperCase();
            File dest = new File(folder, cleanId + ".png");
            
            // 5. Copy
            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            // Image saved to: /Users/sunsovisal/NetBeansProjects/ITC-I3/product_images/D13.png
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Failed to save image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadData(String query, String... params) {
        DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();
        model.setRowCount(0);
        try {
            PreparedStatement p;
            if (query.isEmpty()) p = connection.prepareStatement("SELECT * FROM product");
            else {
                p = connection.prepareStatement(query);
                for(int i=0; i<params.length; i++) {
                    p.setString(i+1, params[i]);
                    if(params.length==1 && query.contains("OR")) p.setString(2, params[i]);
                }
            }
            ResultSet rs = p.executeQuery();
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("pid"), rs.getString("name"),
                    rs.getString("category"), rs.getString("type"),
                    rs.getDouble("price")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    // --- INVENTORY ---
    private void loadInventory() {
        DefaultTableModel model = (DefaultTableModel) view.getTblIngredient().getModel();
        model.setRowCount(0); 

        try {
            // Sort by Expiry Date so you see expiring items first!
            String sql = "SELECT * FROM ingredient ORDER BY expiry_date ASC";
            PreparedStatement p = connection.prepareStatement(sql);
            ResultSet rs = p.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getInt("stock_qty"),
                    String.format("$%.2f", rs.getDouble("unit_price")),
                    String.format("$%.2f", rs.getDouble("total_value")),
                    rs.getDate("bought_date"),
                    rs.getDate("expiry_date")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addInventoryItem() {
        try {
            String name = view.getTfIngName().getText();
            String cat = view.getCbIngCategory().getSelectedItem().toString();
            String qtyStr = view.getTfIngQty().getText();
            String priceStr = view.getTfIngPrice().getText();

            // Get Dates directly from Chooser
            java.util.Date rawBought = view.getDcIngBought().getDate();
            java.util.Date rawExpiry = view.getDcIngExpiry().getDate();

            if (name.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty() || rawBought == null) {
                JOptionPane.showMessageDialog(view, "Please fill in Name, Qty, Price, and Bought Date.");
                return;
            }

            int qty = Integer.parseInt(qtyStr);
            double price = Double.parseDouble(priceStr);
            double total = qty * price; 
            
            // (java.util.Date -> java.sql.Date)
            java.sql.Date sqlBought = new java.sql.Date(rawBought.getTime());
            
            java.sql.Date sqlExpiry = null;
            if (rawExpiry != null) {
                sqlExpiry = new java.sql.Date(rawExpiry.getTime());
            }

            String sql = "INSERT INTO ingredient (name, category, stock_qty, unit_price, total_value, bought_date, expiry_date) VALUES (?,?,?,?,?,?,?)";
            PreparedStatement p = connection.prepareStatement(sql);
            
            p.setString(1, name);
            p.setString(2, cat);
            p.setInt(3, qty);
            p.setDouble(4, price);
            p.setDouble(5, total);
            p.setDate(6, sqlBought);
            p.setDate(7, sqlExpiry);
            
            p.executeUpdate();
            
            JOptionPane.showMessageDialog(view, "Inventory Added!");
            clearInventoryForm();
            loadInventory();

        } catch (NumberFormatException ne) {
            JOptionPane.showMessageDialog(view, "Quantity and Price must be numbers!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Error: " + e.getMessage());
        }
    }
    
    private void clearInventoryForm() {
        view.getTfIngName().setText("");
        view.getTfIngQty().setText("");
        view.getTfIngPrice().setText("");
        view.getDcIngExpiry().setDate(null); 
        view.getDcIngBought().setDate(new java.util.Date()); 
    }
    
    private void updateInventoryItem() {
        int row = view.getTblIngredient().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(view, "Please select an item to edit first!");
            return;
        }

        try {
            int id = (int) view.getTblIngredient().getValueAt(row, 0);

            String name = view.getTfIngName().getText();
            String cat = view.getCbIngCategory().getSelectedItem().toString();
            int qty = Integer.parseInt(view.getTfIngQty().getText());
            double price = Double.parseDouble(view.getTfIngPrice().getText());
            double total = qty * price;
            
            java.util.Date rawBought = view.getDcIngBought().getDate();
            java.util.Date rawExpiry = view.getDcIngExpiry().getDate();

            java.sql.Date sqlBought = null;
            if (rawBought != null) {
                sqlBought = new java.sql.Date(rawExpiry.getTime());
            }
            
            java.sql.Date sqlExpiry = null;
            if (rawExpiry != null) {
                sqlExpiry = new java.sql.Date(rawExpiry.getTime());
            }

            String sql = "UPDATE ingredient SET name=?, category=?, stock_qty=?, unit_price=?, total_value=?, bought_date=?, expiry_date=? WHERE id=?";
            PreparedStatement p = connection.prepareStatement(sql);
            p.setString(1, name);
            p.setString(2, cat);
            p.setInt(3, qty);
            p.setDouble(4, price);
            p.setDouble(5, total);
            p.setDate(6, sqlBought);
            p.setDate(7, sqlExpiry);
            p.setInt(8, id);

            p.executeUpdate();
            
            JOptionPane.showMessageDialog(view, "Item Updated!");
            loadInventory();
            clearInventoryForm();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Error: Quantity and Price must be numbers.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Error Updating: " + e.getMessage());
        }
    }

    private void deleteInventoryItem() {
        int row = view.getTblIngredient().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(view, "Please select an item to delete!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(view, "Are you sure you want to delete this item?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = (int) view.getTblIngredient().getValueAt(row, 0);
                
                String sql = "DELETE FROM ingredient WHERE id=?";
                PreparedStatement p = connection.prepareStatement(sql);
                p.setInt(1, id);
                p.executeUpdate();

                loadInventory();
                clearInventoryForm();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(view, "Error Deleting: " + e.getMessage());
            }
        }
    }
}