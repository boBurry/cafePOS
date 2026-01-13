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
        
        filterData();
      
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
       
this.view.getCbType().addActionListener(e -> filterData());
this.view.getTfSearch().addKeyListener(new java.awt.event.KeyAdapter() {
    public void keyReleased(java.awt.event.KeyEvent e) {
        filterData();
    }
});


        
    }

private void refreshTable() {
    DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();
    model.setRowCount(0);

    try {
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM product");

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("PID"),
                rs.getString("Name"),
                rs.getString("Type"),
                rs.getDouble("Price")
            });
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


private void filterData() {
    String search = view.getTfSearch().getText();
    String type = view.getCbType().getSelectedItem().toString();

    DefaultTableModel model =
        (DefaultTableModel) view.getTable().getModel();
    model.setRowCount(0);

    try {
        String sql = "SELECT * FROM product WHERE (PID LIKE ? OR Name LIKE ?)";

        if (!type.equals("All")) {
            sql += " AND Type = ?";
        }

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, "%" + search + "%");
        ps.setString(2, "%" + search + "%");

        if (!type.equals("All")) {
            ps.setString(3, type);
        }

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("PID"),
                rs.getString("Name"),
                rs.getString("Type"),
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
    String selectedType = view.getCbType().getSelectedItem().toString();

    // 1. Validation: Block "All" and empty fields
    if (id.isEmpty() || name.isEmpty() || price.isEmpty() || selectedType.equals("All")) {
        JOptionPane.showMessageDialog(view, "Please fill in all fields and select a specific Type.");
        return;
    }

    try {
        // 2. Map the Category automatically based on the Type selected
        String category;
        switch (selectedType) {
            case "Cake":
                category = "Snack";
                break;
            case "Hot":
            case "Iced":
            case "Frappe":
            case "Smoothie":
                category = "Drinks";
                break;
            default:
                category = "Unknown";
        }

        // 3. Prepare SQL for all 5 columns: PID, Name, Category, Type, Price
        String sql = "INSERT INTO product (PID, Name, Category, Type, Price) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement p = connection.prepareStatement(sql);
        p.setString(1, id);
        p.setString(2, name);
        p.setString(3, category);
        p.setString(4, selectedType);
        p.setDouble(5, Double.parseDouble(price));

        p.executeUpdate();
        filterData();
        clearFields();
        JOptionPane.showMessageDialog(view, "Product Added Successfully!");

    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(view, "Invalid Price format.");
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(view, "Database Error: " + e.getMessage());
    }
}

    private void updateData() {
    String id = view.getTfId().getText();
    String name = view.getTfName().getText();
    String price = view.getTfPrice().getText();
    String selectedType = view.getCbType().getSelectedItem().toString();

    // Validation: Block "All" and ensure ID is present
    if(id.isEmpty() || selectedType.equals("All")) {
        JOptionPane.showMessageDialog(view, "Select a product from the table and a valid Type to update.");
        return;
    }

    try {
        // Map Category based on Type
        String category = (selectedType.equals("Cake")) ? "Snack" : "Drinks";

        // Update statement covering Name, Category, Type, and Price based on PID
        String sql = "UPDATE product SET Name=?, Category=?, Type=?, Price=? WHERE PID=?";
        PreparedStatement p = connection.prepareStatement(sql);
        p.setString(1, name);
        p.setString(2, category);
        p.setString(3, selectedType);
        p.setDouble(4, Double.parseDouble(price));
        p.setString(5, id);
        
        int rows = p.executeUpdate();
        if(rows > 0) {
            filterData();
            clearFields();
            JOptionPane.showMessageDialog(view, "Product Updated Successfully!");
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
                
                filterData();
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