package Views;

import Controllers.AdminController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Admin extends JFrame {
    
    // Only Search, Filter, Buttons, and Table remain
    private JTextField tfSearch;
    private JComboBox<String> cbTypeFilter;
    private JTable table;
    private DefaultTableModel model;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnBack;

    public Admin() {
        setTitle("Admin Panel");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        new AdminController(this); 
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // --- TOP PANEL (Header + Search + Action Buttons) ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(new Color(245, 245, 245));
        topContainer.setBorder(new EmptyBorder(15, 20, 15, 20));

        // 1. Header (Back + Title)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setOpaque(false);
        btnBack = new JButton("⬅");
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        JLabel lblTitle = new JLabel("  Product Management");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        
        headerPanel.add(btnBack);
        headerPanel.add(lblTitle);

        // 2. Toolbar (Search Left, Buttons Right)
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(15, 0, 0, 0));

        // Search Side
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search:"));
        tfSearch = new JTextField(15);
        searchPanel.add(tfSearch);
        searchPanel.add(new JLabel("Filter:"));
        cbTypeFilter = new JComboBox<>(new String[]{"All", "Hot", "Iced", "Frappe", "Smoothie", "Cake"});
        searchPanel.add(cbTypeFilter);

        // Button Side
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnPanel.setOpaque(false);
        btnAdd = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");
        
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(btnPanel, BorderLayout.EAST);

        topContainer.add(headerPanel);
        topContainer.add(toolbar);
        add(topContainer, BorderLayout.NORTH);

        // --- CENTER: TABLE ---
        String[] columns = {"ID", "Name", "Category", "Type", "Price"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setRowHeight(30);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(new Color(245, 245, 245));
        tableWrapper.setBorder(new EmptyBorder(0, 20, 40, 20)); 
        tableWrapper.add(scrollPane);
        
        add(tableWrapper, BorderLayout.CENTER);
    }

    public JTextField getTfSearch() { return tfSearch; }
    public JComboBox<String> getCbTypeFilter() { return cbTypeFilter; }
    public JTable getTable() { return table; }
    public JButton getBtnAdd() { return btnAdd; }
    public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnBack() { return btnBack; }
    
    public static void main(String[] args) { 
        new Admin().setVisible(true);
    }
}