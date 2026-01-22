package Views;

import Controllers.AdminController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Admin extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private JPanel productPanel;
    private JPanel historyPanel;

    private JTextField tfSearch;
    private JComboBox<String> cbTypeFilter;
    private JTable productTable;
    private DefaultTableModel productModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnBack;

    private JTextField tfHistoryDate;
    private JButton btnHistorySearch, btnHistoryRefresh;
    private JLabel lbHistoryTotal, lbHistoryCount;
    private JTable historyTable;
    private DefaultTableModel historyModel;

    public Admin() {
        setTitle("Admin Dashboard");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setupMenuBar();

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        initProductPanel(); 
        initHistoryPanel();

        mainContainer.add(productPanel, "Product");
        mainContainer.add(historyPanel, "History");

        add(mainContainer);

        new AdminController(this); 
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu menuManage = new JMenu("Management");
        JMenuItem itemProd = new JMenuItem("Product Inventory");
        itemProd.addActionListener(e -> cardLayout.show(mainContainer, "Product"));
        menuManage.add(itemProd);

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
        
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        tfHistoryDate = new JTextField(today, 10);
        btnHistorySearch = new JButton("Search");
        btnHistoryRefresh = new JButton("Refresh");
        
        searchBar.add(tfHistoryDate);
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
    public JTextField getTfHistoryDate() { return tfHistoryDate; }
    public JButton getBtnHistorySearch() { return btnHistorySearch; }
    public JButton getBtnHistoryRefresh() { return btnHistoryRefresh; }
    public JTable getHistoryTable() { return historyTable; }
    public JLabel getLbHistoryTotal() { return lbHistoryTotal; }
    public JLabel getLbHistoryCount() { return lbHistoryCount; }
    
    public void clear() {
        tfSearch.setText("");
    }

    public static void main(String[] args) { 
        new Admin().setVisible(true);
    }
}