package Views;

import Controllers.Controller;
import Models.Order;
import Models.db;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class GUI extends javax.swing.JFrame {

    private Order currentOrder = new Order();
    private Controller controller;
    
    java.sql.Connection con = null;
    ResultSet rs = null;
    PreparedStatement pst = null;
    
    public GUI() {
        initComponents(); 
        
        controller = new Controller(this, currentOrder);
        con = db.myCon();
        
        // FLEXIBLE MAXIMIZE: This tells Windows to maximize but NOT cover the Taskbar
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
    
        // Safety check: Ensure the window stays within the "usable" screen bounds
        GraphicsConfiguration config = getGraphicsConfiguration();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
        Rectangle bounds = config.getBounds();

        // This manually calculates the area NOT covered by the taskbar
        int x = bounds.x + insets.left;
        int y = bounds.y + insets.top;
        int width = bounds.width - (insets.left + insets.right);
        int height = bounds.height - (insets.top + insets.bottom);

        this.setMaximizedBounds(new Rectangle(0, 0, width, height));
        
        setupCartTable();
        
        body.removeAll();
        body.add(Drink); 

        loadProducts("Drink", dP1, lbtitle,""); 

        body.revalidate();
        body.repaint();
    }
    // --- METHOD TO CONFIGURE THE SMART TABLE ---
    private void setupCartTable() {
        Views.CartTableModel model = new Views.CartTableModel(currentOrder);
        table.setModel(model);

        // Standard Table Settings
        table.setRowHeight(35); 
        table.setSelectionBackground(new java.awt.Color(51, 153, 255)); // Blue Selection
        table.setSelectionForeground(java.awt.Color.WHITE);
        
        // --- COLUMN 2: QUANTITY (Just Centered Text now) ---
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        // --- COLUMN 5: ACTION (Keep your Red X) ---
        javax.swing.table.TableColumn actionCol = table.getColumnModel().getColumn(5);
        actionCol.setCellRenderer(new ButtonRenderer()); // Keeping this ✅
        actionCol.setCellEditor(new ButtonEditor(new javax.swing.JCheckBox(), controller)); // Keeping this ✅
        actionCol.setPreferredWidth(50);
        
        // Adjust other widths if needed
        table.getColumnModel().getColumn(0).setPreferredWidth(140);
    }
    
    private void loadProducts(String category, javax.swing.JPanel targetPanel, javax.swing.JLabel titleLabel, String searchQuery) {
        // 1. Update Title
        titleLabel.setText(category);

        // 2. Clear & Set Layout [ALIGNMENT FIX #1]
        targetPanel.removeAll();
        // FlowLayout.LEFT makes items start from the left side.
        // 20, 20 are the horizontal and vertical gaps between items.
        targetPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 20));

        try {
            // 3. Database Query
            if (con == null || con.isClosed()) {
                System.out.println("ERROR: Database Connection is NULL or CLOSED!");
                return;
            }
            
            String sql = "SELECT * FROM product WHERE Category = ? AND Name LIKE ?";
            pst = con.prepareStatement(sql);
            pst.setString(1, category);
            pst.setString(2, "%" + searchQuery + "%"); 
            rs = pst.executeQuery();

            int count = 0;
            while (rs.next()) {
                String pid = rs.getString("PID");
                String name = rs.getString("Name");
                double price = rs.getDouble("Price");
                String cat = rs.getString("Category");

                targetPanel.add(createProductPanel(pid, name, price, cat));
                count++;
            }

            // 4. Calculate Height [ALIGNMENT FIX #2]
            // The ScrollPane won't scroll unless we tell it how tall the panel is.
            // We assume 3 items fit in one row (approx 220px width per item).
            int itemsPerRow = 3; 
            int rows = (int) Math.ceil((double)count / itemsPerRow);

            // Height = (Rows * CardHeight) + (Rows * Gap) + Padding
            // Card is 240px tall, Gap is 20px
            int newHeight = (rows * 260) + 40; 

            // Force the panel size so items "flow" down instead of getting cut off
            targetPanel.setPreferredSize(new java.awt.Dimension(800, newHeight));

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

        // 5. Refresh
        targetPanel.revalidate();
        targetPanel.repaint();
    }
    
    private javax.swing.JPanel createProductPanel(String pid, String name, double price, String category) {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setPreferredSize(new java.awt.Dimension(180, 240)); 
        panel.setBackground(java.awt.Color.WHITE);
        panel.setLayout(null);

        // Image (Centered: 180 width - 150 image = 30 margin / 2 = 15 x)
        javax.swing.JLabel imgLabel = new javax.swing.JLabel();
        imgLabel.setBounds(15, 0, 150, 150); // Changed x to 15
        try {
            // 1. Try to find the Product Image
            String path = "/Image/" + pid.toLowerCase() + ".png";
            java.net.URL imgURL = getClass().getResource(path);

            if (imgURL != null) {
                // Success: Found the product image
                imgLabel.setIcon(new javax.swing.ImageIcon(imgURL));
            } else {
                // Fail 1: Product image missing -> Try to find Logo
                java.net.URL logoURL = getClass().getResource("/Image/logo.png");

                if (logoURL != null) {
                    imgLabel.setIcon(new javax.swing.ImageIcon(logoURL));
                } else {
                    // Fail 2: Both missing -> Just show text (Prevents Crash)
                    imgLabel.setText("No Image");
                }
            }
        } catch (Exception e) {
            // 2. Extra Safety: If anything else goes wrong, don't crash
            imgLabel.setText("Error");
            System.err.println("Image Error for PID " + pid + ": " + e.getMessage());
        }
        panel.add(imgLabel);

        // Name Label
        javax.swing.JLabel nameLabel = new javax.swing.JLabel(name);
        nameLabel.setBounds(10, 160, 100, 16); // Reduced width slightly
        panel.add(nameLabel);

        // Price Label
        javax.swing.JLabel priceLabel = new javax.swing.JLabel("$" + price);
        priceLabel.setBounds(10, 180, 50, 16);
        panel.add(priceLabel);

        // ADD Button (Shifted Left to fit in 180px)
        javax.swing.JButton addButton = new javax.swing.JButton("ADD");
        // Previous x=130. New x=110 (110 + 60 width = 170, leaving 10px margin)
        addButton.setBounds(110, 160, 60, 60); 
        addButton.addActionListener(e -> {
            if ("Drink".equals(category)) {
                 controller.addToCart(pid);
            } else {
                 controller.addToCart(pid);
            }
        });
        panel.add(addButton);

        return panel;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sidePanel = new javax.swing.JPanel();
        btExit = new javax.swing.JButton();
        lbSnack = new javax.swing.JLabel();
        lbDrink = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lbAdmin = new javax.swing.JLabel();
        header = new javax.swing.JPanel();
        txtSearch = new javax.swing.JTextField();
        lbtitle = new javax.swing.JLabel();
        btRefresh = new javax.swing.JLabel();
        body = new javax.swing.JPanel();
        Drink = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        dP1 = new javax.swing.JPanel();
        Snack = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        sP1 = new javax.swing.JPanel();
        right = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jPanel17 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        lbSubtotal = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        cbDiscount = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        lbTotal = new javax.swing.JTextField();
        btTotal = new javax.swing.JButton();
        btClear = new javax.swing.JButton();
        btEdit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1512, 982));
        getContentPane().setLayout(null);

        sidePanel.setBackground(new java.awt.Color(255, 255, 255));
        sidePanel.setLayout(null);

        btExit.setText("Exit");
        btExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btExitActionPerformed(evt);
            }
        });
        sidePanel.add(btExit);
        btExit.setBounds(40, 800, 72, 40);

        lbSnack.setBackground(new java.awt.Color(255, 255, 255));
        lbSnack.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        lbSnack.setForeground(new java.awt.Color(51, 51, 51));
        lbSnack.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbSnack.setText("Snack");
        lbSnack.setToolTipText("");
        lbSnack.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lbSnack.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lbSnack.setOpaque(true);
        lbSnack.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbSnackMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lbSnackMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lbSnackMouseExited(evt);
            }
        });
        sidePanel.add(lbSnack);
        lbSnack.setBounds(0, 260, 150, 70);

        lbDrink.setBackground(new java.awt.Color(255, 255, 255));
        lbDrink.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 14)); // NOI18N
        lbDrink.setForeground(new java.awt.Color(51, 51, 51));
        lbDrink.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbDrink.setText("Drink ");
        lbDrink.setToolTipText("");
        lbDrink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lbDrink.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lbDrink.setOpaque(true);
        lbDrink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbDrinkMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lbDrinkMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lbDrinkMouseExited(evt);
            }
        });
        sidePanel.add(lbDrink);
        lbDrink.setBounds(0, 180, 150, 70);

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/logo.png"))); // NOI18N
        sidePanel.add(jLabel6);
        jLabel6.setBounds(10, 30, 120, 120);

        lbAdmin.setBackground(new java.awt.Color(255, 255, 255));
        lbAdmin.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 12)); // NOI18N
        lbAdmin.setForeground(new java.awt.Color(51, 51, 51));
        lbAdmin.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbAdmin.setText("Admin");
        lbAdmin.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lbAdmin.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lbAdmin.setOpaque(true);
        lbAdmin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbAdminMouseClicked(evt);
            }
        });
        sidePanel.add(lbAdmin);
        lbAdmin.setBounds(0, 750, 140, 40);

        getContentPane().add(sidePanel);
        sidePanel.setBounds(0, 0, 150, 980);

        header.setBackground(new java.awt.Color(255, 255, 255));
        header.setLayout(null);

        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
            }
        });
        header.add(txtSearch);
        txtSearch.setBounds(620, 20, 160, 30);

        lbtitle.setFont(new java.awt.Font(".AppleSystemUIFont", 1, 24)); // NOI18N
        lbtitle.setText("Drink");
        header.add(lbtitle);
        lbtitle.setBounds(20, 20, 80, 30);

        btRefresh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btRefreshMouseClicked(evt);
            }
        });
        header.add(btRefresh);
        btRefresh.setBounds(790, 20, 40, 30);

        getContentPane().add(header);
        header.setBounds(150, 0, 850, 60);

        body.setBackground(new java.awt.Color(255, 255, 255));
        body.setLayout(new java.awt.CardLayout());

        Drink.setBackground(new java.awt.Color(255, 255, 255));
        Drink.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        dP1.setBackground(new java.awt.Color(245, 245, 245));
        jScrollPane3.setViewportView(dP1);

        Drink.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 850, 980));

        body.add(Drink, "card2");

        Snack.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        sP1.setBackground(new java.awt.Color(245, 245, 245));
        jScrollPane1.setViewportView(sP1);

        Snack.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 850, 920));

        body.add(Snack, "card2");

        getContentPane().add(body);
        body.setBounds(150, 60, 850, 920);

        right.setBackground(new java.awt.Color(255, 255, 255));
        right.setLayout(null);

        table.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 12)); // NOI18N
        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product", "Price", "Qty", "Customization", "Total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(table);

        right.add(jScrollPane2);
        jScrollPane2.setBounds(0, 0, 510, 410);

        jPanel17.setBackground(new java.awt.Color(255, 255, 255));
        jPanel17.setLayout(null);

        jLabel3.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 20)); // NOI18N
        jLabel3.setText("Discount");
        jPanel17.add(jLabel3);
        jLabel3.setBounds(20, 60, 130, 40);

        lbSubtotal.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 18)); // NOI18N
        lbSubtotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lbSubtotalActionPerformed(evt);
            }
        });
        jPanel17.add(lbSubtotal);
        lbSubtotal.setBounds(160, 10, 190, 40);

        jLabel4.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 20)); // NOI18N
        jLabel4.setText("Subtotal ");
        jPanel17.add(jLabel4);
        jLabel4.setBounds(20, 10, 119, 40);

        cbDiscount.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 18)); // NOI18N
        cbDiscount.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0%", "30%", "50%", "70%" }));
        cbDiscount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbDiscountActionPerformed(evt);
            }
        });
        jPanel17.add(cbDiscount);
        cbDiscount.setBounds(160, 60, 190, 40);

        jLabel5.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 20)); // NOI18N
        jLabel5.setText("Total");
        jPanel17.add(jLabel5);
        jLabel5.setBounds(20, 110, 60, 40);

        lbTotal.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 18)); // NOI18N
        lbTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lbTotalActionPerformed(evt);
            }
        });
        jPanel17.add(lbTotal);
        lbTotal.setBounds(160, 110, 190, 40);

        right.add(jPanel17);
        jPanel17.setBounds(0, 430, 350, 160);

        btTotal.setBackground(new java.awt.Color(102, 255, 102));
        btTotal.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 20)); // NOI18N
        btTotal.setText("Order");
        btTotal.setBorder(null);
        btTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btTotalActionPerformed(evt);
            }
        });
        right.add(btTotal);
        btTotal.setBounds(370, 530, 130, 50);

        btClear.setBackground(new java.awt.Color(255, 51, 51));
        btClear.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 20)); // NOI18N
        btClear.setText("Clear");
        btClear.setBorder(null);
        btClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btClearActionPerformed(evt);
            }
        });
        right.add(btClear);
        btClear.setBounds(370, 480, 130, 50);

        btEdit.setBackground(new java.awt.Color(255, 102, 0));
        btEdit.setFont(new java.awt.Font(".AppleSystemUIFont", 0, 20)); // NOI18N
        btEdit.setText("Edit");
        btEdit.setBorder(null);
        btEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btEditActionPerformed(evt);
            }
        });
        right.add(btEdit);
        btEdit.setBounds(370, 430, 130, 50);

        getContentPane().add(right);
        right.setBounds(1000, 0, 510, 980);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void lbSnackMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbSnackMouseClicked
        body.removeAll();
        body.add(Snack);
        loadProducts("Snack", sP1, lbtitle,"");
        body.revalidate();
        body.repaint();
    }//GEN-LAST:event_lbSnackMouseClicked

    private void lbDrinkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbDrinkMouseClicked
        body.removeAll();
        body.add(Drink);
        loadProducts("Drink", dP1, lbtitle,"");
        body.revalidate();
        body.repaint();
    }//GEN-LAST:event_lbDrinkMouseClicked

    private void lbDrinkMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbDrinkMouseExited
        lbDrink.setBackground(new Color(255,255,255));
    }//GEN-LAST:event_lbDrinkMouseExited

    private void lbDrinkMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbDrinkMouseEntered
        lbDrink.setBackground(new Color(229,214,192));
    }//GEN-LAST:event_lbDrinkMouseEntered

    private void lbSnackMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbSnackMouseEntered
        lbSnack.setBackground(new Color(229,214,192));
    }//GEN-LAST:event_lbSnackMouseEntered

    private void lbSnackMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbSnackMouseExited
        lbSnack.setBackground(new Color(255,255,255));
    }//GEN-LAST:event_lbSnackMouseExited

    private void lbSubtotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lbSubtotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lbSubtotalActionPerformed

    private void lbTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lbTotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lbTotalActionPerformed

    private void btTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btTotalActionPerformed
        // 1. Calculate Total
        double finalTotal = currentOrder.calculateTotal();
        lbTotal.setText(String.format("$%.2f", finalTotal));

        // 2. Start the Payment Process (Cash vs QR)
        if (finalTotal > 0) {
            controller.initiatePayment(finalTotal);
        } else {
            JOptionPane.showMessageDialog(this, "Cart is empty!");
        }
    }//GEN-LAST:event_btTotalActionPerformed

    private void cbDiscountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbDiscountActionPerformed
        // Get the selected discount percentage from the ComboBox
        String selectedDiscount = (String)cbDiscount.getSelectedItem();

        double discountPercentage;
            // Adjust based on selection
            discountPercentage = switch (selectedDiscount) {
                case "0%"  -> 0.0;
                case "30%" -> 30.0;
                case "50%" -> 50.0;
                case "70%" -> 70.0;
                default -> 0.0;
            };
        // Set the discount for the current order
        currentOrder.setDiscount(discountPercentage);
    }//GEN-LAST:event_cbDiscountActionPerformed

    private void btExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_btExitActionPerformed
 
    public javax.swing.JTable getTable() {
        return table;
    }

    public javax.swing.JTextField getLbTotal() {
        return lbTotal;
    }

    public javax.swing.JTextField getLbSubtotal() {
        return lbSubtotal;
    }
    
    private void lbAdminMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbAdminMouseClicked
        Admin​ ad = new Admin();
        ad.setVisible(true);
    }//GEN-LAST:event_lbAdminMouseClicked

    private void btClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btClearActionPerformed
        // 1. Clear the actual data list
        currentOrder.clear(); 

        // 2. Tell the Smart Model to refresh (No casting to DefaultTableModel!)
        ((Views.CartTableModel) table.getModel()).fireTableDataChanged();

        // 3. Reset the price labels
        lbSubtotal.setText("");
        lbTotal.setText("");
    }//GEN-LAST:event_btClearActionPerformed

    private void btEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btEditActionPerformed
        controller.editSelectedItem();
    }//GEN-LAST:event_btEditActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed

    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased
        String text = txtSearch.getText().trim();
    
        // Check which panel is currently visible to search the correct category
        if (body.getComponent(0).isVisible() && body.getComponent(0) == Drink) {
            loadProducts("Drink", dP1, lbtitle, text);
        } else {
            loadProducts("Snack", sP1, lbtitle, text);
        }
    }//GEN-LAST:event_txtSearchKeyReleased

    private void btRefreshMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btRefreshMouseClicked
        if (body.getComponent(0).isVisible() && body.getComponent(0) == Drink) {
            loadProducts("Drink", dP1, lbtitle,"");
        } else {
            loadProducts("Snack", sP1, lbtitle,"");
        }
    }//GEN-LAST:event_btRefreshMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            // 1. MacOS Menu Bar Fix (Optional, keeps Mac happy)
            System.setProperty("apple.laf.useScreenMenuBar", "true");

            // 2. SET THEME TO NIMBUS (Modern Look + Supports Colors)
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Drink;
    private javax.swing.JPanel Snack;
    private javax.swing.JPanel body;
    private javax.swing.JButton btClear;
    private javax.swing.JButton btEdit;
    private javax.swing.JButton btExit;
    private javax.swing.JLabel btRefresh;
    private javax.swing.JButton btTotal;
    private javax.swing.JComboBox<String> cbDiscount;
    private javax.swing.JPanel dP1;
    private javax.swing.JPanel header;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lbAdmin;
    private javax.swing.JLabel lbDrink;
    private javax.swing.JLabel lbSnack;
    private javax.swing.JTextField lbSubtotal;
    private javax.swing.JTextField lbTotal;
    private javax.swing.JLabel lbtitle;
    private javax.swing.JPanel right;
    private javax.swing.JPanel sP1;
    private javax.swing.JPanel sidePanel;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}

// =========================================================
//              HELPER CLASSES FOR THE SMART TABLE
// =========================================================
// 1. LABEL RENDERER: Draws a red label with "X" instead of a button
class ButtonRenderer extends javax.swing.JLabel implements javax.swing.table.TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true); // Crucial for JLabel background color to show
        setText("X");
        setHorizontalAlignment(javax.swing.SwingConstants.CENTER); // Center the text
        setBackground(new java.awt.Color(255, 102, 102)); // Light Red background
        setForeground(java.awt.Color.WHITE); // White text font
        setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14)); // Nice bold font
    }
    
    @Override
    public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        // Optional: Makes it look interactive by slightly darkening when the row is selected
        if (isSelected) {
             setBackground(new java.awt.Color(220, 80, 80)); 
        } else {
             setBackground(new java.awt.Color(255, 102, 102));
        }
        return this;
    }
}

// 2. BUTTON EDITOR: Handles the click on the "X" button
class ButtonEditor extends javax.swing.DefaultCellEditor {
    private javax.swing.JButton button;
    private Controllers.Controller controller;
    private int currentRow;

    public ButtonEditor(javax.swing.JCheckBox checkBox, Controllers.Controller controller) {
        super(checkBox);
        this.controller = controller;
        button = new javax.swing.JButton();
        button.setOpaque(true);
        button.addActionListener(e -> {
            fireEditingStopped(); // Stop editing mode
            controller.deleteItem(currentRow); // Call Controller to remove item
        });
    }

    public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value,
            boolean isSelected, int row, int column) {
        this.currentRow = row;
        button.setText("X");
        button.setBackground(new java.awt.Color(255, 102, 102));
        button.setForeground(java.awt.Color.WHITE);
        return button;
    }

    public Object getCellEditorValue() { return "X"; }
}