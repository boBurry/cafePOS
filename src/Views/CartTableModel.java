package Views;

import Models.Order;
import Models.Product;
import javax.swing.table.AbstractTableModel;


public class CartTableModel extends AbstractTableModel {
    
    private final Order order;
    // Columns: Product, Price, Qty (Spinner), Custom, Total, Action (Button)
    private final String[] columnNames = {"Product", "Price", "Qty", "Custom", "Total", "Action"};
    
    // Define types so Java knows Col 2 is a Number and Col 5 is a Button
    private final Class[] columnTypes = {String.class, String.class, Integer.class, String.class, String.class, Object.class};

    public CartTableModel(Order order) {
        this.order = order;
    }

    @Override
    public int getRowCount() {
        return order.getProducts().size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Allow editing ONLY for Qty (Col 2) and Remove Button (Col 5)
        return columnIndex == 2 || columnIndex == 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= order.getProducts().size()) return null;
        
        Product p = order.getProducts().get(rowIndex);
        
        switch (columnIndex) {
            case 0: return p.getName();
            case 1: return String.format("$%.2f", p.getUnitFinalPrice());
            case 2: return p.getQuantity(); // Return Integer for Spinner
            case 3: return p.getShortCustomization(); // Use the new helper
            case 4: return String.format("$%.2f", p.getTotal());
            case 5: return "X"; // Text for the button
            default: return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        // If user changes Qty (Col 2)
        if (columnIndex == 2) {
            int newQty = (Integer) aValue;
            if (newQty < 1) newQty = 1; // Minimum 1
            
            // 1. Update the Product Logic
            order.getProducts().get(rowIndex).setQuantity(newQty);
            
            // 2. Notify table to redraw this row (updates Total price column automatically)
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }
}

// =========================================================
//              HELPER CLASSES FOR THE SMART TABLE
// =========================================================
// 1. LABEL RENDERER: X
class ButtonRenderer extends javax.swing.JLabel implements javax.swing.table.TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
        setText("X");
        setHorizontalAlignment(javax.swing.SwingConstants.CENTER); 
        setBackground(new java.awt.Color(255, 102, 102));
        setForeground(java.awt.Color.WHITE); 
        setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
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