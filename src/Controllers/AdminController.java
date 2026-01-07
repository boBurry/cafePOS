package Controllers;

import Views.Admin;
import Models.db;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class AdminController {

    private Admin view;
    private Connection connection;

    public AdminController(Admin view) {
        this.view = view;
        this.connection = db.myCon();
        
        refreshTable();
      
        // 1. ADD BUTTON
        this.view.getBtnAdd().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addData();
            }
        });

        // 2. UPDATE BUTTON
        this.view.getBtnUpdate().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateData();
            }
        });

        // 3. DELETE BUTTON
        this.view.getBtnDelete().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteData();
            }
        });

        // 4. CLEAR BUTTON
        this.view.getBtnClear().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });

        // 5. BACK BUTTON
        this.view.getBtnBack().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                view.dispose();
            }
        });
    }

    private void refreshTable() {
        DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();
        try {
            model.setRowCount(0);
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM product");
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("PID"), 
                    rs.getString("Name"), 
                    rs.getDouble("Price")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addData() {
        String id = view.getTfId().getText();
        String name = view.getTfName().getText();
        String price = view.getTfPrice().getText();

        if(id.isEmpty() || name.isEmpty() || price.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please fill in all fields.");
            return;
        }
        try {
            PreparedStatement p = connection.prepareStatement("INSERT INTO product VALUES(?,?,?)");
            p.setString(1, id);
            p.setString(2, name);
            p.setDouble(3, Double.parseDouble(price));
            p.executeUpdate();
            
            refreshTable();
            clearFields();
            JOptionPane.showMessageDialog(view, "Product Added!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Error: " + e.getMessage());
        }
    }

    private void updateData() {
        String id = view.getTfId().getText();
        String name = view.getTfName().getText();
        String price = view.getTfPrice().getText();

        if(id.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Enter Product ID to update.");
            return;
        }
        try {
            PreparedStatement p = connection.prepareStatement("UPDATE product SET Name=?, Price=? WHERE PID=?");
            p.setString(1, name);
            p.setDouble(2, Double.parseDouble(price));
            p.setString(3, id);
            
            int rows = p.executeUpdate();
            if(rows > 0) {
                refreshTable();
                clearFields();
                JOptionPane.showMessageDialog(view, "Product Updated!");
            } else {
                JOptionPane.showMessageDialog(view, "Product ID not found.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Error: " + e.getMessage());
        }
    }

    private void deleteData() {
        String inputId = view.getTfId().getText();
        String inputName = view.getTfName().getText();
        String inputPrice = view.getTfPrice().getText();

        if (inputId.isEmpty() || inputName.isEmpty() || inputPrice.isEmpty()) {
            JOptionPane.showMessageDialog(view, "STRICT DELETE: You must enter ID, Name, AND Price to delete.");
            return;
        }

        try {
            // 1. Verify exact match first
            PreparedStatement check = connection.prepareStatement("SELECT * FROM product WHERE PID=? AND Name=? AND Price=?");
            check.setString(1, inputId);
            check.setString(2, inputName);
            check.setDouble(3, Double.parseDouble(inputPrice));
            
            ResultSet rs = check.executeQuery();
            
            if (rs.next()) {
                PreparedStatement del = connection.prepareStatement("DELETE FROM product WHERE PID=?");
                del.setString(1, inputId);
                del.executeUpdate();
                
                refreshTable();
                clearFields();
                JOptionPane.showMessageDialog(view, "Product Deleted Successfully.");
            } else {
                JOptionPane.showMessageDialog(view, "Delete Failed: Information does not match database record.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Error: " + e.getMessage());
        }
    }

    private void clearFields() {
        view.getTfId().setText("");
        view.getTfName().setText("");
        view.getTfPrice().setText("");
    }
}