package Views;

import javax.swing.*;
import java.awt.*;

public class DrinkCustomizationDialog extends JDialog {
    // Variables
    private String size = "Medium";
    private String sugarLevel = "100%";
    private String iceLevel = "Normal";
    private boolean extraShot = false;
    private boolean confirmed = false;
    private int selectedQty = 1; 
    
    private double basePrice; 

    // UI Components
    private JComboBox<String> cbSize;
    private JComboBox<String> cbSugar;
    private JComboBox<String> cbIce;
    private JCheckBox chkExtraShot;
    private JLabel lbPricePreview;
    
    // Custom Qty Panel
    private DialogQtyPanel qtyPanel; 

    public DrinkCustomizationDialog(Frame parent, String drinkName, double basePrice) {
        super(parent, "Customize " + drinkName, true);
        this.basePrice = basePrice;
        initComponents(drinkName);
    }

    private void initComponents(String drinkName) {
        setLayout(new BorderLayout());
        setSize(420, 520); // Slightly wider to accommodate the shift
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Title
        JLabel title = new JLabel("Customize Your " + drinkName);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Price Preview
        lbPricePreview = new JLabel(String.format("Price: $%.2f", basePrice));
        lbPricePreview.setFont(new Font("SansSerif", Font.BOLD, 16));
        lbPricePreview.setForeground(new Color(50, 36, 23));
        lbPricePreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lbPricePreview);
        mainPanel.add(Box.createVerticalStrut(20));

        // --- STYLE DEFINITIONS ---
        Dimension comboSize = new Dimension(180, 30);
        Font comboFont = new Font("SansSerif", Font.PLAIN, 14);

        // 1. Size
        cbSize = new JComboBox<>(new String[]{"Medium", "Large"});
        cbSize.setSelectedItem("Medium");
        cbSize.setPreferredSize(comboSize);
        cbSize.setFont(comboFont);
        cbSize.addActionListener(e -> updatePricePreview());
        mainPanel.add(createGenericRow("Size:", cbSize));
        mainPanel.add(Box.createVerticalStrut(15));

        // 2. Sugar
        cbSugar = new JComboBox<>(new String[]{"0%", "25%", "50%", "75%", "100%", "125%"});
        cbSugar.setSelectedItem("100%");
        cbSugar.setPreferredSize(comboSize);
        cbSugar.setFont(comboFont);
        mainPanel.add(createGenericRow("Sugar Level:", cbSugar));
        mainPanel.add(Box.createVerticalStrut(15));

        // 3. Ice
        cbIce = new JComboBox<>(new String[]{"No Ice", "Less Ice", "Normal", "Extra Ice"});
        cbIce.setSelectedItem("Normal");
        cbIce.setPreferredSize(comboSize);
        cbIce.setFont(comboFont);
        mainPanel.add(createGenericRow("Ice Level:", cbIce));
        mainPanel.add(Box.createVerticalStrut(15));

        // 4. Extra Shot (Indented slightly to align)
        JPanel extraPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        extraPanel.setBackground(Color.WHITE);
        extraPanel.setMaximumSize(new Dimension(450, 40)); 
        // Move it to the right slightly
        extraPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); 
        
        chkExtraShot = new JCheckBox("Add Extra Shot (+$0.50)");
        chkExtraShot.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chkExtraShot.setBackground(Color.WHITE);
        chkExtraShot.addActionListener(e -> updatePricePreview());
        extraPanel.add(chkExtraShot);
        mainPanel.add(extraPanel);

        mainPanel.add(Box.createVerticalStrut(15));

        // 5. Quantity
        qtyPanel = new DialogQtyPanel(); 
        mainPanel.add(createGenericRow("Quantity:", qtyPanel));

        add(mainPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnConfirm = new JButton("Confirm");
        btnConfirm.setPreferredSize(new Dimension(120, 40));
        btnConfirm.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnConfirm.setBackground(new Color(50, 36, 23)); 
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFocusPainted(false); 
        
        btnConfirm.addActionListener(e -> {
            size = (String) cbSize.getSelectedItem();
            sugarLevel = (String) cbSugar.getSelectedItem();
            iceLevel = cbIce.isEnabled() ? (String) cbIce.getSelectedItem() : "None";
            extraShot = chkExtraShot.isSelected();
            selectedQty = qtyPanel.getQty(); 
            confirmed = true;
            dispose();
        });

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(120, 40));
        btnCancel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        buttonPanel.add(btnConfirm);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
        
        if (drinkName.toLowerCase().contains("hot")) {
            cbIce.setEnabled(false);
            cbIce.setSelectedItem("No Ice");
        }
    }
    
    // --- UPDATED HELPER: Increased Width and Gap ---
    private JPanel createGenericRow(String label, JComponent component) {
        // Increased gap from 10 to 15
        JPanel panel = new JPanel(new BorderLayout(15, 0)); 
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(450, 40)); 
        
        JLabel lbl = new JLabel(label);
        // INCREASED WIDTH: 100 -> 140 (Pushes inputs to the right)
        lbl.setPreferredSize(new Dimension(140, 30)); 
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        component.setBackground(Color.WHITE);
        component.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        panel.add(lbl, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(component);
        
        panel.add(rightPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private void updatePricePreview() {
        double price = basePrice;
        if ("Large".equals(cbSize.getSelectedItem())) price += 0.50;
        if (chkExtraShot.isSelected()) price += 0.50;
        lbPricePreview.setText(String.format("Price: $%.2f", price));
    }

    public String getDrinkSize() { return size; }
    public String getSugarLevel() { return sugarLevel; }
    public String getIceLevel() { return iceLevel; }
    public boolean hasExtraShot() { return extraShot; }
    public boolean isConfirmed() { return confirmed; }
    public int getSelectedQuantity() { return selectedQty; }

    class DialogQtyPanel extends JPanel {
        private final JButton btnMinus;
        private final JButton btnPlus;
        private final JTextField txtValue;

        public DialogQtyPanel() {
            setLayout(new GridBagLayout());
            setBackground(Color.WHITE);

            btnMinus = new JButton("-");
            btnPlus = new JButton("+");
            txtValue = new JTextField("1", 3); 

            Insets zeroMargin = new Insets(0, 0, 0, 0);
            btnMinus.setMargin(zeroMargin);
            btnPlus.setMargin(zeroMargin);
            
            Dimension btnSize = new Dimension(45, 32); 
            btnMinus.setPreferredSize(btnSize);
            btnPlus.setPreferredSize(btnSize);
            
            Font btnFont = new Font("Monospaced", Font.BOLD, 16);
            btnMinus.setFont(btnFont);
            btnPlus.setFont(btnFont);

            btnMinus.setFocusable(false);
            btnPlus.setFocusable(false);
            
            btnMinus.setBorder(null);
            btnPlus.setBorder(null);
            btnMinus.setBorderPainted(false);
            btnPlus.setBorderPainted(false);
            btnMinus.setContentAreaFilled(true);
            btnPlus.setContentAreaFilled(true);
            
            btnMinus.setBackground(new Color(240, 240, 240));
            btnPlus.setBackground(new Color(240, 240, 240));

            txtValue.setHorizontalAlignment(SwingConstants.CENTER);
            txtValue.setEditable(false); 
            txtValue.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5)); 
            txtValue.setFont(new Font("SansSerif", Font.BOLD, 16));
            txtValue.setBackground(Color.WHITE);

            btnMinus.addActionListener(e -> {
                int current = getQty();
                if (current > 1) {
                    setQty(current - 1);
                }
            });

            btnPlus.addActionListener(e -> {
                setQty(getQty() + 1);
            });

            add(btnMinus);
            add(txtValue);
            add(btnPlus);
        }
        
        public void setQty(int qty) {
            txtValue.setText(String.valueOf(qty));
        }
        
        public int getQty() {
            try {
                return Integer.parseInt(txtValue.getText());
            } catch (NumberFormatException e) {
                return 1;
            }
        }
    }
}