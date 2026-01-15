package Controllers;

import Views.Admin;
import Views.AddProductDialog;
import Models.db; // Assuming 'db' is your connection class
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
        loadHistoryData("");

        this.view.getBtnAdd().addActionListener(e -> showAddDialog());
        this.view.getBtnUpdate().addActionListener(e -> showUpdateDialog());
        this.view.getBtnDelete().addActionListener(e -> deleteData());

        this.view.getTfSearch().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { filterData(); }
        });
        this.view.getCbTypeFilter().addActionListener(e -> filterData());

        this.view.getBtnHistorySearch().addActionListener(e -> {
            String date = view.getTfHistoryDate().getText().trim();
            loadHistoryData(date);
        });
        
        this.view.getBtnHistoryRefresh().addActionListener(e -> {
            view.getTfHistoryDate().setText(""); 
            loadHistoryData("");
        });
    }

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