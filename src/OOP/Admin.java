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

        // --- TOP CONTAINER (Holds Header + Inputs + Buttons) ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        
        // A. Header Panel (Top Left Back Button)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(245, 245, 245)); // Light gray background
        
        JButton btnBack = new JButton("⬅"); // Unicode Arrow
        btnBack.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnBack.setToolTipText("Back to POS");
        btnBack.setForeground(new Color(50, 36, 23)); // Coffee color
        btnBack.setBorderPainted(false);
        btnBack.setContentAreaFilled(false); // Transparent background
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        headerPanel.add(btnBack);
        
        // B. Input Fields Panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(new EmptyBorder(10, 20, 10, 20)); // Margin
        
        tfId = new JTextField();
        tfName = new JTextField();
        tfPrice = new JTextField();

        inputPanel.add(new JLabel("Product ID:"));
        inputPanel.add(tfId);
        inputPanel.add(new JLabel("Product Name:"));
        inputPanel.add(tfName);
        inputPanel.add(new JLabel("Price:"));
        inputPanel.add(tfPrice);

        // C. Action Buttons Panel (Add, Update, Delete, Clear)
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 10, 10)); // 1 Row, 4 Columns
        buttonPanel.setBorder(new EmptyBorder(0, 20, 20, 20)); // Margin bottom

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        // Add everything to Top Container
        topContainer.add(headerPanel);
        topContainer.add(inputPanel);
        topContainer.add(buttonPanel);
        
        add(topContainer, BorderLayout.NORTH);

        // --- BOTTOM SECTION: Data Table ---
        model = new DefaultTableModel(new String[]{"ID", "Name", "Price"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0, 20, 20, 20));
        
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
        btnUpdate.addActionListener(e -> updateData());
        btnDelete.addActionListener(e -> deleteData());
        btnClear.addActionListener(e -> clear());
        
        // --- BACK BUTTON LOGIC ---
        btnBack.addActionListener(e -> {
            dispose();
        });
        
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

    private void updateData() {
        if(tfId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Product ID to update.");
            return;
        }
        try {
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

    private void deleteData() {
        String inputId = tfId.getText();
        String inputName = tfName.getText();
        String inputPrice = tfPrice.getText();

        if (inputId.isEmpty() || inputName.isEmpty() || inputPrice.isEmpty()) {
            JOptionPane.showMessageDialog(this, "STRICT DELETE: You must enter ID, Name, AND Price to delete.");
            return;
        }

        try {
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