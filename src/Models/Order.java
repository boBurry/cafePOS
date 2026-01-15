package Models;

import java.util.ArrayList;

public class Order {
    private ArrayList<Product> products;
    private double subtotal;
    private double totalAmount;
    private double discountValue;
    private boolean isPercentage;

    public Order() {
        this.products = new ArrayList<>();
        this.subtotal = 0.0;
        this.totalAmount = 0.0;
        this.discountValue = 0.0; 
    }

    public void addProduct(Product product) {
        this.products.add(product);
    }

    public void setDiscount(double value, boolean isPercentage) {
        this.discountValue = value;
        this.isPercentage = isPercentage;
    }
    
    public void removeProduct(int index) {
        if (index >= 0 && index < products.size()) {
            products.remove(index);
        }
    }

    public double calculateSubtotal() {
        double tempSubtotal = 0.0;
        for (Product product : products) {
            tempSubtotal += product.getTotal();
        }
        this.subtotal = tempSubtotal;
        return subtotal;
    }

    public double calculateTotal() {
        calculateSubtotal();
        if (isPercentage) {
            totalAmount = subtotal - (subtotal * (discountValue / 100)); 
        } else {
            totalAmount = subtotal - discountValue;
        }

        if (totalAmount < 0) totalAmount = 0;

        totalAmount = Math.round(totalAmount * 100.0) / 100.0;
        return totalAmount;
    }
    
    public double getSubtotal() { return subtotal; }
    public double getTotalAmount() { return totalAmount; }
    public ArrayList<Product> getProducts() { return products; }
    
    public void clear() {
        products.clear();         // 1. Clear the list
        discountValue = 0.0;           // 2. Reset discount
        subtotal = 0.0;           // 3. Reset subtotal
        totalAmount = 0.0;        // 4. Reset total
    }
}