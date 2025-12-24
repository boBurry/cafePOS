package OOP;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Admin extends JFrame {
    private Connection connection;
    private JTextField tfId, tfName, tfPrice;
    private JTable table;
    private DefaultTableModel model;

    public Admin() {
        connection = db.myCon(); // Your database connection
        
        setTitle("Admin Panel");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. MAIN LAYOUT
        setLayout(new BorderLayout(10, 10));

        // --- TOP SECTION: Input Form ---
        // Changed to 5 rows to fit the extra button comfortably
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        tfId = new JTextField();
        tfName = new JTextField();
        tfPrice = new JTextField();

        formPanel.add(new JLabel("Product ID:"));
        formPanel.add(tfId);
        formPanel.add(new JLabel("Product Name:"));
        formPanel.add(tfName);
        formPanel.add(new JLabel("Price:"));
        formPanel.add(tfPrice);

        // Buttons
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update"); // NEW BUTTON
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear Fields");

        formPanel.add(btnAdd);
        formPanel.add(btnUpdate);
        formPanel.add(btnDelete);
        formPanel.add(btnClear);

        add(formPanel, BorderLayout.NORTH);

        // --- BOTTOM SECTION: Data Table ---
        model = new DefaultTableModel(new String[]{"ID", "Name", "Price"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        // Optional: Click row to fill text fields (Makes it easier to get info for delete)
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                tfId.setText(model.getValueAt(row, 0).toString());
                tfName.setText(model.getValueAt(row, 1).toString());
                tfPrice.setText(model.getValueAt(row, 2).toString());
            }
        });

        add(scrollPane, BorderLayout.CENTER);

        // --- LOGIC ---
        btnAdd.addActionListener(e -> addData());
        btnUpdate.addActionListener(e -> updateData()); // NEW ACTION
        btnDelete.addActionListener(e -> deleteData());
        btnClear.addActionListener(e -> clear());
        
        refreshTable();
    }

    private void refreshTable() {
        try {
            model.setRowCount(0);
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM product");
            while(rs.next()) {
                model.addRow(new Object[]{rs.getString("PID"), rs.getString("Name"), rs.getDouble("Price")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void addData() {
        if(tfId.getText().isEmpty() || tfName.getText().isEmpty() || tfPrice.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }
        try {
            PreparedStatement p = connection.prepareStatement("INSERT INTO product VALUES(?,?,?)");
            p.setString(1, tfId.getText());
            p.setString(2, tfName.getText());
            p.setDouble(3, Double.parseDouble(tfPrice.getText()));
            p.executeUpdate();
            refreshTable();
            clear();
            JOptionPane.showMessageDialog(this, "Product Added!");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    // --- NEW UPDATE METHOD ---
    private void updateData() {
        if(tfId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Product ID to update.");
            return;
        }
        try {
            // Update Name and Price where ID matches
            PreparedStatement p = connection.prepareStatement("UPDATE product SET Name=?, Price=? WHERE PID=?");
            p.setString(1, tfName.getText());
            p.setDouble(2, Double.parseDouble(tfPrice.getText()));
            p.setString(3, tfId.getText());
            
            int rows = p.executeUpdate();
            if(rows > 0) {
                refreshTable();
                clear();
                JOptionPane.showMessageDialog(this, "Product Updated!");
            } else {
                JOptionPane.showMessageDialog(this, "Product ID not found.");
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    // --- STRICT DELETE METHOD ---
    private void deleteData() {
        String inputId = tfId.getText();
        String inputName = tfName.getText();
        String inputPrice = tfPrice.getText();

        if (inputId.isEmpty() || inputName.isEmpty() || inputPrice.isEmpty()) {
            JOptionPane.showMessageDialog(this, "STRICT DELETE: You must enter ID, Name, AND Price to delete.");
            return;
        }

        try {
            // 1. First, verify the data exists exactly as typed
            PreparedStatement check = connection.prepareStatement("SELECT * FROM product WHERE PID=? AND Name=? AND Price=?");
            check.setString(1, inputId);
            check.setString(2, inputName);
            check.setDouble(3, Double.parseDouble(inputPrice));
            
            ResultSet rs = check.executeQuery();
            
            if (rs.next()) {
                // 2. If data matches, perform delete
                PreparedStatement del = connection.prepareStatement("DELETE FROM product WHERE PID=?");
                del.setString(1, inputId);
                del.executeUpdate();
                
                refreshTable();
                clear();
                JOptionPane.showMessageDialog(this, "Product Deleted Successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Delete Failed: Information does not match database record.");
            }

        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void clear() {
        tfId.setText(""); tfName.setText(""); tfPrice.setText("");
    }

    public static void main(String[] args) {
        new Admin().setVisible(true);
    }
}