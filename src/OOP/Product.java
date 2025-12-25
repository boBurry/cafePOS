package OOP;

public class Product {
    // 1. The Raw Data (The Blueprint)
    private String id;
    private String name;
    private double basePrice; // Store the original menu price 
    private int quantity;
    
    // Customization Fields
    private String size;       // "Small", "Medium", "Large"
    private String sugarLevel; 
    private String iceLevel;   
    private boolean extraShot; 

    // 2. The Constructor (Just saves the data, doesn't do math yet)
    public Product(String id, String name, double basePrice, int quantity, 
                   String size, String sugarLevel, String iceLevel, boolean extraShot) {
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
        this.quantity = quantity;
        this.size = size;
        this.sugarLevel = sugarLevel;
        this.iceLevel = iceLevel;
        this.extraShot = extraShot;
    }

    // Constructor for non-customizable items (Cakes)
    public Product(String id, String name, double basePrice, int quantity) {
        this(id, name, basePrice, quantity, "", "", "", false);
    }

    // 3. The Logic (Calculates price on demand) 
    public double getUnitFinalPrice() {
        double finalPrice = basePrice;

        // A. Adjust for Size
        if (size != null) {
            switch (size) {
                case "Medium" -> finalPrice = basePrice;
                case "Large" -> finalPrice += 0.50;
            }
        }

        // B. Adjust for Extra Shot
        if (extraShot) {
            finalPrice += 0.50;
        }

        return finalPrice;
    }

    // Calculates total for this row (Unit Price * Quantity)
    public double getTotal() {
        return getUnitFinalPrice() * quantity;
    }

    // 4. Formatting for the Table/Database
    public String getCustomizationDetails() {
        if (size == null || size.isEmpty()) {
            return "N/A";
        }
        String shotStr = extraShot ? ", +Shot" : "";
        return String.format("%s, %s Sugar, %s Ice%s", size, sugarLevel, iceLevel, shotStr);
    }

    // 5. Standard Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getBasePrice() { return basePrice; }
    public int getQuantity() { return quantity; }
    public String getSize() { return size; }
    public String getSugarLevel() { return sugarLevel; }
    public String getIceLevel() { return iceLevel; }
    public boolean hasExtraShot() { return extraShot; }
}