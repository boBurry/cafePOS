package Models;

import java.sql.Date;

public class Ingredient {
    private int id;
    private String name;
    private String category;
    private int quantity;
    private double unitPrice;
    private double totalValue;
    private Date boughtDate;
    private Date expiryDate;

    public Ingredient(int id, String name, String category, int quantity, 
                      double unitPrice, double totalValue, Date boughtDate, Date expiryDate) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalValue = totalValue;
        this.boughtDate = boughtDate;
        this.expiryDate = expiryDate;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalValue() { return totalValue; }
    public Date getBoughtDate() { return boughtDate; }
    public Date getExpiryDate() { return expiryDate; }
}