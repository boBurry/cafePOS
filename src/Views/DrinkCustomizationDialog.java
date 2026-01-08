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
    
    private double basePrice; 

    // UI Components
    private JComboBox<String> cbSize;
    private JComboBox<String> cbSugar;
    private JComboBox<String> cbIce;
    private JCheckBox chkExtraShot;
    private JLabel lbPricePreview; 

    public DrinkCustomizationDialog(Frame parent, String drinkName, double basePrice) {
        super(parent, "Customize " + drinkName, true);
        this.basePrice = basePrice;
        initComponents(drinkName);
    }

    private void initComponents(String drinkName) {
        // 1. Setup Window
        setLayout(new BorderLayout());
        setSize(400, 480); // Taller height to fit dropdowns comfortably
        setLocationRelativeTo(getParent());
        setResizable(false);

        // 2. Main Panel (White Background)
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Title
        JLabel title = new JLabel("Customize Your " + drinkName);
        title.setFont(new Font(".AppleSystemUIFont", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Price Preview
        lbPricePreview = new JLabel(String.format("Price: $%.2f", basePrice));
        lbPricePreview.setFont(new Font(".AppleSystemUIFont", Font.BOLD, 16));
        lbPricePreview.setForeground(new Color(50, 36, 23));
        lbPricePreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lbPricePreview);
        mainPanel.add(Box.createVerticalStrut(20));

        // --- DROPDOWNS ---
        
        // Size
        cbSize = new JComboBox<>(new String[]{"Medium", "Large ($+0.50)"});
        cbSize.setSelectedItem("Medium");
        cbSize.addActionListener(e -> updatePricePreview());
        mainPanel.add(createOptionPanel("Size:", cbSize));
        mainPanel.add(Box.createVerticalStrut(15));

        // Sugar
        cbSugar = new JComboBox<>(new String[]{"0%", "25%", "50%", "75%", "100%", "125%"});
        cbSugar.setSelectedItem("100%");
        mainPanel.add(createOptionPanel("Sugar Level:", cbSugar));
        mainPanel.add(Box.createVerticalStrut(15));

        // Ice
        cbIce = new JComboBox<>(new String[]{"No Ice", "Less Ice", "Normal", "Extra Ice"});
        cbIce.setSelectedItem("Normal");
        mainPanel.add(createOptionPanel("Ice Level:", cbIce));
        mainPanel.add(Box.createVerticalStrut(15));

        // Extra Shot Checkbox
        JPanel extraPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        extraPanel.setBackground(Color.WHITE);
        extraPanel.setMaximumSize(new Dimension(400, 40)); 
        
        chkExtraShot = new JCheckBox("Add Extra Shot (+$0.50)");
        chkExtraShot.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 14));
        chkExtraShot.setBackground(Color.WHITE);
        chkExtraShot.addActionListener(e -> updatePricePreview());
        extraPanel.add(chkExtraShot);
        mainPanel.add(extraPanel);

        add(mainPanel, BorderLayout.CENTER);

        // 3. Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnConfirm = new JButton("Confirm");
        btnConfirm.setPreferredSize(new Dimension(120, 40));
        btnConfirm.setFont(new Font(".AppleSystemUIFont", Font.BOLD, 14));
        btnConfirm.setBackground(new Color(50, 36, 23)); // Coffee Color
        btnConfirm.setForeground(Color.WHITE);
        

        
        btnConfirm.addActionListener(e -> {
            size = (String) cbSize.getSelectedItem();
            sugarLevel = (String) cbSugar.getSelectedItem();
            iceLevel = cbIce.isEnabled() ? (String) cbIce.getSelectedItem() : "None";
            extraShot = chkExtraShot.isSelected();
            confirmed = true;
            dispose();
        });

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(120, 40));
        btnCancel.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 14));
        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        buttonPanel.add(btnConfirm);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 4. Hot Drink Logic
        if (drinkName.toLowerCase().contains("hot")) {
            cbIce.setEnabled(false);
            cbIce.setSelectedItem("No Ice");
        }
    }

    // Helper method to create clean rows
    private JPanel createOptionPanel(String label, JComboBox<String> comboBox) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(400, 40)); // Constrain height
        
        JLabel lbl = new JLabel(label);
        lbl.setPreferredSize(new Dimension(100, 30));
        lbl.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 14));
        
        // Make the combobox white
        comboBox.setBackground(Color.WHITE);
        comboBox.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 14));
        
        panel.add(lbl, BorderLayout.WEST);
        panel.add(comboBox, BorderLayout.CENTER);
        return panel;
    }
    
    private void updatePricePreview() {
        double price = basePrice;
        
        if ("Large".equals(cbSize.getSelectedItem())) {
            price += 0.50;
        }
        if (chkExtraShot.isSelected()) {
            price += 0.50;
        }
        lbPricePreview.setText(String.format("Price: $%.2f", price));
    }

    // Getters
    public String getDrinkSize() { return size; }
    public String getSugarLevel() { return sugarLevel; }
    public String getIceLevel() { return iceLevel; }
    public boolean hasExtraShot() { return extraShot; }
    public boolean isConfirmed() { return confirmed; }
}