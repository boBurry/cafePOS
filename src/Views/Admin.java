package Views;

import Controllers.AdminController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Admin extends JDialog {
    
    // 1. Components defined at class level so Controller can access them
    private JTextField tfId, tfName, tfPrice;
    private JTable table;
    private DefaultTableModel model;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnBack;
    private JComboBox<String> cbType;
    private JTextField tfSearch;


    public Admin(JFrame parent) {
        super(parent, "Admin Panel", true); // TRUE = modal (blocks everything)
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout(10, 10));

        // --- TOP CONTAINER (Holds Header + Inputs + Buttons) ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        
        // A. Header Panel (Top Left Back Button)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(245, 245, 245)); // Light gray background
        
        btnBack = new JButton("⬅"); // Unicode Arrow
        btnBack.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnBack.setToolTipText("Back to POS");
        btnBack.setForeground(new Color(50, 36, 23)); // Coffee color
        btnBack.setBorderPainted(false);
        btnBack.setContentAreaFilled(false); // Transparent background
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        headerPanel.add(btnBack);
        
        // --- SEARCH & FILTER PANEL ---
JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
filterPanel.setBorder(new EmptyBorder(0, 20, 10, 20));

tfSearch = new JTextField(15);
cbType = new JComboBox<>(new String[]{
    "All", "Hot", "Iced", "Frappe", "Smoothie", "Cake"
});

filterPanel.add(new JLabel("Search:"));
filterPanel.add(tfSearch);
filterPanel.add(new JLabel("Type:"));
filterPanel.add(cbType);

        
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

        // Initialize Buttons
        btnAdd = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        // Add everything to Top Container
topContainer.add(headerPanel);
topContainer.add(filterPanel);   
topContainer.add(inputPanel);
topContainer.add(buttonPanel);

        
        add(topContainer, BorderLayout.NORTH);

        // --- BOTTOM SECTION: Data Table ---
        // Updated to include Category column
        model = new DefaultTableModel(new String[]{"ID", "Name", "Type", "Price"}, 0);              
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        // View Logic: When user clicks a row, fill the text fields
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    tfId.setText(model.getValueAt(row, 0).toString());
                    tfName.setText(model.getValueAt(row, 1).toString());
                    tfId.setText(model.getValueAt(row, 0).toString());
                    tfName.setText(model.getValueAt(row, 1).toString());
                    cbType.setSelectedItem(model.getValueAt(row, 2).toString());
                    tfPrice.setText(model.getValueAt(row, 3).toString());

                }
            }
        });

        add(scrollPane, BorderLayout.CENTER);

        // --- CONNECT TO CONTROLLER ---
        new AdminController(this); 
        // Create a new Controller, and here is a reference to ME (Admin GUI) so you can talk to me later.
    }

    public JTextField getTfId() { return tfId; }
    public JTextField getTfName() { return tfName; }
    public JTextField getTfPrice() { return tfPrice; }
    public JTable getTable() { return table; }
    public JButton getBtnAdd() { return btnAdd; }
    public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnClear() { return btnClear; }
    public JButton getBtnBack() { return btnBack; }
public JComboBox<String> getCbType() { return cbType; }
public JTextField getTfSearch() { return tfSearch; }

    
}