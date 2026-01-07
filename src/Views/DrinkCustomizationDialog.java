package Views;

import javax.swing.*;
import java.awt.*;

public class DrinkCustomizationDialog extends JDialog {
    private String size = "Medium";
    private String sugarLevel = "100%";
    private String iceLevel = "Normal";
    private boolean extraShot = false;
    private boolean confirmed = false;
    
    private double basePrice; // Store base price

    private JComboBox<String> cbSize;
    private JComboBox<String> cbSugar;
    private JComboBox<String> cbIce;
    private JCheckBox chkExtraShot;
    private JLabel lbPricePreview; // Show price preview

    public DrinkCustomizationDialog(Frame parent, String drinkName, double basePrice) {
        super(parent, "Customize " + drinkName, true);
        this.basePrice = basePrice;
        initComponents(drinkName);
    }

    private void initComponents(String drinkName) {
        setLayout(new BorderLayout());
        setSize(400, 400);
        setLocationRelativeTo(getParent());
        setResizable(false);

        // Main panel
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

        // Size selection with price info (REMOVED SMALL)
        cbSize = new JComboBox<>(new String[]{"Medium (Base)", "Large (+$0.50)"});
        cbSize.setSelectedItem("Medium (Base)");
        cbSize.addActionListener(e -> updatePricePreview());
        mainPanel.add(createOptionPanel("Size:", cbSize));
        
        mainPanel.add(Box.createVerticalStrut(15));

        // Sugar level
        cbSugar = new JComboBox<>(new String[]{"0%", "25%", "50%", "75%", "100%", "125%"});
        cbSugar.setSelectedItem("100%");
        mainPanel.add(createOptionPanel("Sugar Level:", cbSugar));
        
        mainPanel.add(Box.createVerticalStrut(15));

        // Ice level
        cbIce = new JComboBox<>(new String[]{"No Ice", "Less Ice", "Normal", "Extra Ice"});
        cbIce.setSelectedItem("Normal");
        mainPanel.add(createOptionPanel("Ice Level:", cbIce));
        
        mainPanel.add(Box.createVerticalStrut(15));

        // Extra shot checkbox
        JPanel extraPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        extraPanel.setBackground(Color.WHITE);
        chkExtraShot = new JCheckBox("Add Extra Shot (+$0.50)");
        chkExtraShot.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 14));
        chkExtraShot.setBackground(Color.WHITE);
        chkExtraShot.addActionListener(e -> updatePricePreview());
        extraPanel.add(chkExtraShot);
        mainPanel.add(extraPanel);

        add(mainPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnConfirm = new JButton("Confirm");
        btnConfirm.setPreferredSize(new Dimension(120, 40));
        btnConfirm.setFont(new Font(".AppleSystemUIFont", Font.BOLD, 14));
        btnConfirm.setBackground(new Color(50, 36, 23));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFocusPainted(false);
        btnConfirm.addActionListener(e -> {
            // Updated logic to only check for Large (Default is Medium)
            String selectedSize = (String) cbSize.getSelectedItem();
            if (selectedSize.contains("Large")) {
                size = "Large";
            } else {
                size = "Medium";
            }
            
            sugarLevel = (String) cbSugar.getSelectedItem();
            iceLevel = (String) cbIce.getSelectedItem();
            extraShot = chkExtraShot.isSelected();
            confirmed = true;
            dispose();
        });

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(120, 40));
        btnCancel.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 14));
        btnCancel.setBackground(Color.LIGHT_GRAY);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        buttonPanel.add(btnConfirm);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Updated Hot Drink logic (Removed code that added/removed "Small")
        if (drinkName.equalsIgnoreCase("Hot Latte") || drinkName.equalsIgnoreCase("Hot Cappuccino")
            || drinkName.equalsIgnoreCase("Hot Green Tea") || drinkName.equalsIgnoreCase("Hot Espresso")) {
            // Disable the ice level customization for hot drinks
            cbIce.setEnabled(false);
            // Set the ice level to a default value
            iceLevel = "None"; 
        } else {
            // Enable ice customization for cold drinks
            cbIce.setEnabled(true);
        }
    }

    private JPanel createOptionPanel(String label, JComboBox<String> comboBox) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(350, 35));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 14));
        lbl.setPreferredSize(new Dimension(100, 30));
        
        comboBox.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 14));
        comboBox.setPreferredSize(new Dimension(200, 30));

        panel.add(lbl, BorderLayout.WEST);
        panel.add(comboBox, BorderLayout.CENTER);

        return panel;
    }
    
    // Update price preview when size or extra shot changes
    private void updatePricePreview() {
        double price = basePrice;
        
        // Add size adjustment (Removed logic for Small)
        String selectedSize = (String) cbSize.getSelectedItem();
        if (selectedSize != null) {
            if (selectedSize.contains("Large")) {
                price += 0.50;
            }
        }
        
        // Add extra shot
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

