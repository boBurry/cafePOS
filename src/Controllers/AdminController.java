package Controllers;

import Views.Admin;
import Views.AddProductDialog;
import Models.db;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class AdminController {

    private Admin view;
    private Connection connection;

    public AdminController(Admin view) {
        this.view = view;
        this.connection = db.myCon();
        loadData(""); 

        // Actions
        this.view.getBtnAdd().addActionListener(e -> showAddDialog());
        this.view.getBtnUpdate().addActionListener(e -> showUpdateDialog());
        this.view.getBtnDelete().addActionListener(e -> deleteData());
        this.view.getBtnBack().addActionListener(e -> view.dispose());

        // Search
        this.view.getTfSearch().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { filterData(); }
        });
        this.view.getCbTypeFilter().addActionListener(e -> filterData());
    }

    // --- LOGIC TO OPEN POPUP FOR ADDING ---
    private void showAddDialog() {
        AddProductDialog dialog = new AddProductDialog(view, "Add New Product");
        dialog.setVisible(true); // Waits here until closed

        if (dialog.isConfirmed()) {
            insertProduct(dialog);
        }
    }

    // --- LOGIC TO OPEN POPUP FOR UPDATING ---
    private void showUpdateDialog() {
        int row = view.getTable().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(view, "Please select a product to update.");
            return;
        }
        
        // 1. Get data from selected row
        String id = view.getTable().getValueAt(row, 0).toString();
        String name = view.getTable().getValueAt(row, 1).toString();
        String cat = view.getTable().getValueAt(row, 2).toString();
        String type = view.getTable().getValueAt(row, 3).toString();
        String price = view.getTable().getValueAt(row, 4).toString();

        // 2. Open Dialog & Fill Data
        AddProductDialog dialog = new AddProductDialog(view, "Update Product");
        dialog.setProductData(id, name, cat, type, price);
        dialog.setVisible(true);

        // 3. Save if confirmed
        if (dialog.isConfirmed()) {
            updateProduct(dialog);
        }
    }

    // --- DATABASE ACTIONS ---
    private void insertProduct(AddProductDialog dialog) {
        try {
            PreparedStatement p = connection.prepareStatement("INSERT INTO product VALUES(?,?,?,?,?)");
            p.setString(1, dialog.getId());
            p.setString(2, dialog.getName());
            p.setString(3, dialog.getCategory());
            p.setString(4, dialog.getProductType());
            p.setDouble(5, Double.parseDouble(dialog.getPrice()));
            p.executeUpdate();
            
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
            p.setString(5, dialog.getId()); // ID is the WHERE condition
            
            p.executeUpdate();
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
}