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