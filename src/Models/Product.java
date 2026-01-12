package Models;

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

    // 2. The Constructor (Just saves the data)
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

    public String getShortCustomization() {
        if (size == null || size.isEmpty()) return "";

        // Convert "Medium" -> "M", "Large" -> "L"
        String s = size.substring(0, 1); 

        // Clean up Sugar (remove " Sugar" text if exists, or just keep number)
        String sugar = sugarLevel.replace(" Sugar", "").replace("%", "%");

        // Clean up Ice
        String ice = iceLevel.replace(" Ice", "").replace("Normal", "Norm");

        String shot = extraShot ? "+Shot" : "";

        return String.format("%s, %s, %s %s", s, sugar, ice, shot).trim();
    }

    public String getCustomizationDetails() {
         // If it's food (empty size), return blank
         if (size == null || size.isEmpty()) return "";
         
         String shotText = extraShot ? ", Extra Shot" : "";
         return String.format("%s, %s Sugar, %s Ice%s", size, sugarLevel, iceLevel, shotText);
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
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}