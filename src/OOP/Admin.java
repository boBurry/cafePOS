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

        // 1. MAIN LAYOUT: Top (Input) and Bottom (Table)
        setLayout(new BorderLayout(10, 10));

        // --- TOP SECTION: Input Form ---
        // GridLayout(rows, cols, h-gap, v-gap) -> 4 rows, 2 columns
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
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

        // Buttons row
        JButton btnAdd = new JButton("Add");
        JButton btnDelete = new JButton("Delete");
        formPanel.add(btnAdd);
        formPanel.add(btnDelete);

        add(formPanel, BorderLayout.NORTH);

        // --- BOTTOM SECTION: Data Table ---
        model = new DefaultTableModel(new String[]{"ID", "Name", "Price"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorde​r(10, 20, 20, 20));
        
        add(scrollPane, BorderLayout.CENTER);

        // --- LOGIC ---
        btnAdd.addActionListener(e -> addData());
        btnDelete.addActionListener(e -> deleteData());
        
        refreshTable(); // Load data on start
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
        try {
            PreparedStatement p = connection.prepareStatement("INSERT INTO product VALUES(?,?,?)");
            p.setString(1, tfId.getText());
            p.setString(2, tfName.getText());
            p.setDouble(3, Double.parseDouble(tfPrice.getText()));
            p.executeUpdate();
            refreshTable();
            clear();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void deleteData() {
        try {
            PreparedStatement p = connection.prepareStatement("DELETE FROM product WHERE PID=?");
            p.setString(1, tfId.getText());
            p.executeUpdate();
            refreshTable();
            clear();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void clear() {
        tfId.setText(""); tfName.setText(""); tfPrice.setText("");
    }

    public static void main(String[] args) {
        new Admin().setVisible(true);
    }
}