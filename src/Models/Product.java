package Models;

public class Product {    
    private String id;
    private String name;
    private double basePrice; 
    private int quantity;
    
    // Customization Fields
    private String size;       
    private String sugarLevel; 
    private String iceLevel;   
    private boolean extraShot; 

    // 2. Constructors
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

    public Product(String id, String name, double basePrice, int quantity) {
        this(id, name, basePrice, quantity, "", "", "", false);
    }

    // 3. Price Logic (Calculates dynamically)
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

    // 4. Formatting Helpers
    public String getShortCustomization() {
        if (size == null || size.isEmpty()) return "";

        String s = size.substring(0, 1); 
        String sugar = sugarLevel.replace(" Sugar", "").replace("%", "%");
        String ice = iceLevel.replace(" Ice", "").replace("Normal", "Norm");
        String shot = extraShot ? "+Shot" : "";

        return String.format("%s, %s, %s %s", s, sugar, ice, shot).trim();
    }

    public String getCustomizationDetails() {
         if (size == null || size.isEmpty()) return "";
         String shotText = extraShot ? ", Extra Shot" : "";
         return String.format("%s, %s Sugar, %s Ice%s", size, sugarLevel, iceLevel, shotText);
    }
    
    // 5. Getters
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

    public void setSize(String size) {
        this.size = size;
        // No need to call 'updateTotal' because getTotal() calculates it dynamically!
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