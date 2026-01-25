package Views;

import Controllers.AdminController;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;

public class AdminView extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel mainContainer;
    
    private JPanel productPanel;
    private JPanel historyPanel;
    private JPanel ingredientPanel; 

    // --- PRODUCT COMPONENTS ---
    private JTextField tfSearch;
    private JComboBox<String> cbTypeFilter;
    private JTable productTable;
    private DefaultTableModel productModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnBack;

    // --- HISTORY COMPONENTS ---
    private JDateChooser dcHistoryDate;
    private JButton btnHistorySearch, btnHistoryRefresh;
    private JLabel lbHistoryTotal, lbHistoryCount;
    private JTable historyTable;
    private DefaultTableModel historyModel;
    
    // --- INGREDIENT COMPONENTS ---
    private JTable ingredientTable; 
    private DefaultTableModel ingredientModel; 
    
    private JTextField tfIngName, tfIngQty, tfIngPrice;
    private JDateChooser dcIngBought, dcIngExpiry;
    private JComboBox<String> cbIngCategory;
    private JButton btnIngAdd, btnIngUpdate, btnIngDelete, btnIngClear, btnIngRefresh;

    public AdminView() {
        setTitle("Admin Dashboard");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setupMenuBar();

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        initProductPanel(); 
        initHistoryPanel();
        initIngredientPanel(); 

        mainContainer.add(productPanel, "Product");
        mainContainer.add(historyPanel, "History");
        mainContainer.add(ingredientPanel, "Ingredient");

        add(mainContainer);

        new AdminController(this); 
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu menuManage = new JMenu("Management");
        JMenuItem itemProd = new JMenuItem("Product Inventory");
        itemProd.addActionListener(e -> cardLayout.show(mainContainer, "Product"));
        menuManage.add(itemProd);
        
        JMenuItem itemIngre = new JMenuItem("Ingredient Inventory");
        itemIngre.addActionListener(e -> cardLayout.show(mainContainer, "Ingredient"));
        menuManage.add(itemIngre);
        
        JMenu menuReport = new JMenu("Reports");
        JMenuItem itemHist = new JMenuItem("Sales History");
        itemHist.addActionListener(e -> cardLayout.show(mainContainer, "History"));
        menuReport.add(itemHist);
        
        JMenu menuSystem = new JMenu("System"); 
        JMenuItem itemExit = new JMenuItem("Exit Application");
        itemExit.addActionListener(e -> this.dispose());
        
        menuSystem.add(itemExit);
        menuBar.add(menuManage);
        menuBar.add(menuReport);
        menuBar.add(Box.createHorizontalGlue()); 
        menuBar.add(menuSystem);
        
        setJMenuBar(menuBar);
    }

    private void initProductPanel() {
        productPanel = new JPanel(new BorderLayout());

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(new Color(245, 245, 245));
        topContainer.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search:"));
        tfSearch = new JTextField(15);
        searchPanel.add(tfSearch);
        searchPanel.add(new JLabel("Filter:"));
        cbTypeFilter = new JComboBox<>(new String[]{"All", "Hot", "Iced", "Frappe", "Smoothie", "Bread", "Cake"});
        searchPanel.add(cbTypeFilter);

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
        topContainer.add(toolbar);

        productPanel.add(topContainer, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Category", "Type", "Price"};
        productModel = new DefaultTableModel(columns, 0);
        productTable = new JTable(productModel);
        productTable.setRowHeight(30);
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(new Color(245, 245, 245));
        tableWrapper.setBorder(new EmptyBorder(0, 20, 20, 20)); 
        tableWrapper.add(scrollPane);
        
        productPanel.add(tableWrapper, BorderLayout.CENTER);
    }

    private void initHistoryPanel() {
        historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(new Color(245, 245, 245));

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(new Color(245, 245, 245));
        topContainer.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBar.setOpaque(false);  

        searchBar.add(new JLabel("Date (YYYY-MM-DD):"));
        
        dcHistoryDate = new JDateChooser();
        dcHistoryDate.setDateFormatString("yyyy-MM-dd"); 
        dcHistoryDate.setDate(new Date()); // Default to Today
        dcHistoryDate.setPreferredSize(new Dimension(150, 25));
        
        btnHistorySearch = new JButton("Search");
        btnHistoryRefresh = new JButton("Refresh");
        
        searchBar.add(dcHistoryDate);
        searchBar.add(btnHistorySearch);
        searchBar.add(btnHistoryRefresh);
        
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        summaryPanel.setOpaque(false);
        
        lbHistoryTotal = new JLabel("Total: $0.00");
        lbHistoryTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        lbHistoryTotal.setForeground(new Color(0, 153, 51)); 
        
        lbHistoryCount = new JLabel("Orders: 0");
        lbHistoryCount.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        summaryPanel.add(lbHistoryTotal);
        summaryPanel.add(lbHistoryCount);

        topContainer.add(searchBar, BorderLayout.WEST);
        topContainer.add(summaryPanel, BorderLayout.EAST);
        
        historyPanel.add(topContainer, BorderLayout.NORTH);

        String[] cols = {"Order ID", "Date/Time", "Total Price", "Payment"};
        historyModel = new DefaultTableModel(cols, 0);
        historyTable = new JTable(historyModel);
        historyTable.setRowHeight(30);
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.getViewport().setBackground(Color.WHITE); 

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(new Color(245, 245, 245));
        tableWrapper.setBorder(new EmptyBorder(0, 20, 20, 20));
        tableWrapper.add(scrollPane);

        historyPanel.add(tableWrapper, BorderLayout.CENTER);
    }
    
    // --- INGREDIENT PANEL ---
    private void initIngredientPanel() {
        ingredientPanel = new JPanel(new BorderLayout());
        ingredientPanel.setBackground(new Color(245, 245, 245));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                BorderFactory.createTitledBorder("Add Inventory Item")
        ));
        formPanel.setPreferredSize(new Dimension(300, 0));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        formPanel.add(new JLabel("Item Name:"), gbc);
        gbc.gridy++;
        tfIngName = new JTextField(15);
        formPanel.add(tfIngName, gbc);
        
        // Category 
        gbc.gridy++;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridy++;
        cbIngCategory = new JComboBox<>(new String[]{
            "Coffee & Tea", 
            "Dairy & Cream", 
            "Syrups & Sauces", 
            "Bakery & Pastry", 
            "Toppings & Dry", 
            "Packaging"
        });
        formPanel.add(cbIngCategory, gbc);

        // Quantity
        gbc.gridy++;
        formPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridy++;
        tfIngQty = new JTextField(15);
        formPanel.add(tfIngQty, gbc);

        // Unit Price
        gbc.gridy++;
        formPanel.add(new JLabel("Unit Price ($):"), gbc);
        gbc.gridy++;
        tfIngPrice = new JTextField(15);
        formPanel.add(tfIngPrice, gbc);

        // Bought Date
        gbc.gridy++;
        formPanel.add(new JLabel("Bought Date:"), gbc);
        gbc.gridy++;
        
        dcIngBought = new JDateChooser();
        dcIngBought.setDateFormatString("yyyy-MM-dd");
        dcIngBought.setDate(new Date()); // Default Today
        formPanel.add(dcIngBought, gbc);

        // Expiry Date
        gbc.gridy++;
        formPanel.add(new JLabel("Expiry Date:"), gbc);
        gbc.gridy++;
        
        dcIngExpiry = new JDateChooser();
        dcIngExpiry.setDateFormatString("yyyy-MM-dd");        
        formPanel.add(dcIngExpiry, gbc);

        // Buttons 
        gbc.gridy++;
        JPanel btnPanel = new JPanel(new GridLayout(1, 4, 5, 0));
        btnPanel.setBackground(Color.WHITE);
        
        btnIngAdd = new JButton("Add");
        btnIngAdd.setBackground(new Color(0, 153, 76));
        btnIngAdd.setForeground(Color.WHITE);
        
        btnIngUpdate = new JButton("Edit");
        btnIngUpdate.setBackground(new Color(255, 128, 0)); 
        btnIngUpdate.setForeground(Color.WHITE);

        btnIngDelete = new JButton("Del");
        btnIngDelete.setBackground(new Color(204, 0, 0));
        btnIngDelete.setForeground(Color.WHITE);

        btnIngClear = new JButton("Clear");
        
        btnPanel.add(btnIngAdd);
        btnPanel.add(btnIngUpdate);
        btnPanel.add(btnIngDelete);
        btnPanel.add(btnIngClear);
        
        formPanel.add(btnPanel, gbc);

        JPanel rightPanel = new JPanel(new BorderLayout()); 
        rightPanel.setBackground(new Color(245, 245, 245));

        JPanel tableTopBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        tableTopBar.setBackground(new Color(245, 245, 245));
        
        btnIngRefresh = new JButton("Refresh Data");
        tableTopBar.add(btnIngRefresh);
        
        String[] cols = {"ID", "Name", "Category", "Qty", "Unit $", "Total $", "Bought", "Expires"};
        ingredientModel = new DefaultTableModel(cols, 0);
        ingredientTable = new JTable(ingredientModel);
        ingredientTable.setRowHeight(25);
        
        ingredientTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        ingredientTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        ingredientTable.getColumnModel().getColumn(0).setMaxWidth(50); 

        ingredientTable.getColumnModel().getColumn(1).setPreferredWidth(250);

        ingredientTable.getColumnModel().getColumn(2).setPreferredWidth(120);

        ingredientTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        ingredientTable.getColumnModel().getColumn(3).setMaxWidth(60);

        ingredientTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        ingredientTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        ingredientTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        ingredientTable.getColumnModel().getColumn(6).setMinWidth(90); 
        
        ingredientTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        ingredientTable.getColumnModel().getColumn(7).setMinWidth(90); 
        
        JScrollPane scrollPane = new JScrollPane(ingredientTable);
        scrollPane.getViewport().setBackground(Color.WHITE);

        rightPanel.add(tableTopBar, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        ingredientPanel.add(formPanel, BorderLayout.WEST);
        ingredientPanel.add(rightPanel, BorderLayout.CENTER);
    }
    
    // Product Getters
    public JTextField getTfSearch() { return tfSearch; }
    public JComboBox<String> getCbTypeFilter() { return cbTypeFilter; }
    public JTable getTable() { return productTable; }
    public JButton getBtnAdd() { return btnAdd; }
    public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnClear() { return btnClear; }
    public JButton getBtnBack() { return btnBack; }
    
    // History Getters
    public JDateChooser getDcHistoryDate() { return dcHistoryDate; }
    public JButton getBtnHistorySearch() { return btnHistorySearch; }
    public JButton getBtnHistoryRefresh() { return btnHistoryRefresh; }
    public JTable getHistoryTable() { return historyTable; }
    public JLabel getLbHistoryTotal() { return lbHistoryTotal; }
    public JLabel getLbHistoryCount() { return lbHistoryCount; }
    
    // Ingredient Getters
    public JTable getTblIngredient() { return ingredientTable; }
    public JTextField getTfIngName() { return tfIngName; }
    public JTextField getTfIngQty() { return tfIngQty; }
    public JTextField getTfIngPrice() { return tfIngPrice; }
    public JDateChooser getDcIngBought() { return dcIngBought; }
    public JDateChooser getDcIngExpiry() { return dcIngExpiry; }
    public JComboBox<String> getCbIngCategory() { return cbIngCategory; }
    
    public JButton getBtnIngAdd() { return btnIngAdd; }
    public JButton getBtnIngClear() { return btnIngClear; }
    public JButton getBtnIngRefresh() { return btnIngRefresh; }
    public JButton getBtnIngUpdate() { return btnIngUpdate; }
    public JButton getBtnIngDelete() { return btnIngDelete; }
    
    public void clear() {
        tfSearch.setText("");
    }

    public static void main(String[] args) { 
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        new AdminView().setVisible(true);
    }
}