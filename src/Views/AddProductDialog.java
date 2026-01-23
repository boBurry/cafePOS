package Views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File; 

public class AddProductDialog extends JDialog {

    private JTextField tfId, tfName, tfPrice;
    private JComboBox<String> cbCategory, cbType;
    
    // --- FIELDS FOR IMAGE BROWSING ---
    private JLabel lbImageName; 
    private File selectedImageFile = null; 
    
    private boolean confirmed = false;

    public AddProductDialog(Frame parent, String title) {
        super(parent, title, true); 
        setSize(400, 450); 
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // --- FORM PANEL ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // ID
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        tfId = new JTextField(15);
        formPanel.add(tfId, gbc);

        // Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        tfName = new JTextField(15);
        formPanel.add(tfName, gbc);

        // Category
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        cbCategory = new JComboBox<>(new String[]{"Drink", "Snack"});
        formPanel.add(cbCategory, gbc);

        // Type
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        cbType = new JComboBox<>(new String[]{"Hot", "Iced", "Frappe", "Smoothie", "Cake", "Bread", "None"});
        formPanel.add(cbType, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        tfPrice = new JTextField(15);
        formPanel.add(tfPrice, gbc);

        // --- BROWSE IMAGE SECTION ---
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Image:"), gbc);

        gbc.gridx = 1;
        JPanel imgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        
        JButton btnBrowse = new JButton("Browse");
        lbImageName = new JLabel(" No image selected");
        
        // Logic to open file chooser
        btnBrowse.addActionListener(e -> {
            JFileChooser ch = new JFileChooser();
            int opt = ch.showOpenDialog(this);
            if (opt == JFileChooser.APPROVE_OPTION) {
                selectedImageFile = ch.getSelectedFile();
                lbImageName.setText(" " + selectedImageFile.getName());
            }
        });

        imgPanel.add(btnBrowse);
        imgPanel.add(lbImageName);
        formPanel.add(imgPanel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // --- BUTTON PANEL ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> {
            confirmed = true;
            setVisible(false);
        });

        btnCancel.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
        });

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // --- METHODS TO SET/GET DATA ---
    public void setProductData(String id, String name, String cat, String type, String price) {
        tfId.setText(id);
        tfId.setEditable(false);
        tfName.setText(name);
        cbCategory.setSelectedItem(cat);
        cbType.setSelectedItem(type);
        tfPrice.setText(price);
    }

    public boolean isConfirmed() { return confirmed; }

    public String getId() { return tfId.getText(); }
    public String getName() { return tfName.getText(); }
    public String getCategory() { return cbCategory.getSelectedItem().toString(); }
    public String getProductType() { return cbType.getSelectedItem().toString(); }
    public String getPrice() { return tfPrice.getText(); }
    
    public File getSelectedImage() { return selectedImageFile; }
}