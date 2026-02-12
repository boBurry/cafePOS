package Models;

public class Product {     
    private String id;
    private String name;
    private double basePrice; 
    private int quantity;
    
    // --- NEW FIELDS ---
    private String category; // "Drink" or "Snack"
    private String type;     // "Hot", "Iced", "Frappe"
    
    // Customization Fields
    private String size;        
    private String sugarLevel; 
    private String iceLevel;    
    private boolean extraShot; 

    // 2. MAIN CONSTRUCTOR (Updated to include Category & Type)
    public Product(String id, String name, double basePrice, int quantity, 
                   String category, String type,
                   String size, String sugarLevel, String iceLevel, boolean extraShot) {
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
        this.quantity = quantity;
        this.category = category;
        this.type = type;
        
        this.size = size;
        this.sugarLevel = sugarLevel;
        this.iceLevel = iceLevel;
        this.extraShot = extraShot;
    }

    // Constructor for "Simple" products (Just fetched from DB, no customization yet)
    public Product(String id, String name, double basePrice, int quantity, String category, String type) {
        this(id, name, basePrice, quantity, category, type, "", "", "", false);
    }

    // 3. Price Logic (Updated to ignore size/shots for Snacks)
    public double getUnitFinalPrice() {
        double finalPrice = basePrice;

        // Safety: Only Drinks have size/shot logic
        if ("Drink".equalsIgnoreCase(category)) {
            
            // A. Adjust for Size
            if (size != null) {
                switch (size) {
                    case "Medium" -> finalPrice = basePrice; // Base price is usually Medium
                    case "Large" -> finalPrice += 0.50;
                }
            }

            // B. Adjust for Extra Shot
            if (extraShot) {
                finalPrice += 0.50;
            }
        }

        return finalPrice;
    }

    // Calculates total for this row
    public double getTotal() {
        return getUnitFinalPrice() * quantity;
    }

    // 4. Formatting Helpers (Updated to return empty string for Snacks)
    public String getShortCustomization() {
        // If it's a Snack, we don't show sugar/ice details
        if ("Snack".equalsIgnoreCase(category)) return "";
        if (size == null || size.isEmpty()) return "";

        String s = size.length() > 0 ? size.substring(0, 1) : ""; 
        String sugar = sugarLevel.replace(" Sugar", "").replace("%", "%");
        String ice = iceLevel.replace(" Ice", "").replace("Normal", "Norm");
        String shot = extraShot ? "+Shot" : "";

        // Format: "M, 50%, Norm +Shot"
        return String.format("%s, %s, %s %s", s, sugar, ice, shot).trim();
    }

    public String getCustomizationDetails() {
         if ("Snack".equalsIgnoreCase(category)) return "";
         if (size == null || size.isEmpty()) return "";
         
         String shotText = extraShot ? ", Extra Shot" : "";
         return String.format("%s (%s), %s Sugar, %s Ice%s", size, type, sugarLevel, iceLevel, shotText);
    }
     
    // 5. Getters & Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getBasePrice() { return basePrice; }
    public int getQuantity() { return quantity; }
    public String getCategory() { return category; }
    public String getType() { return type; }
    
    public String getSize() { return size; }
    public String getSugarLevel() { return sugarLevel; }
    public String getIceLevel() { return iceLevel; }
    public boolean hasExtraShot() { return extraShot; }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setSugarLevel(String sugarLevel) {
        this.sugarLevel = sugarLevel;
    }

    public void setIceLevel(String iceLevel) {
        this.iceLevel = iceLevel;
    }

    public void setExtraShot(boolean extraShot) {
        this.extraShot = extraShot;
    }
}