# CafePOS ☕

CafePOS is a comprehensive Java-based Point of Sale (POS) desktop application tailored specifically for cafes and coffee shops. Built using Java Swing, it features an intuitive user interface, robust database management with MySQL, automated PDF receipt generation, and real-time Telegram notifications.

## 🌟 Features

* **User Authentication:** Secure login system with dedicated views for standard users and administrators.
* **Point of Sale Interface:** * Easy-to-use cart management.
  * Product categorization and visual menus with image support.
  * **Drink Customization:** Specialized dialogs (`DrinkCustomizationDialog`) to configure drink options (e.g., sugar levels, ice levels, add-ons).
* **Admin Dashboard:** Manage inventory, add new products (`AddProductDialog`), and track ingredients.
* **Automated Receipts:** Automatically generates printable PDF receipts for every transaction using `iTextPDF`, saving them directly to the local `/Receipts` directory.
* **Telegram Integration:** Sends real-time transaction alerts or daily summaries to a configured Telegram bot via `TelegramService`.
* **Database Driven:** Powered by MySQL to reliably store users, products, orders, and ingredient inventories.

## 🏗️ Architecture & Structure

The project strictly follows the **MVC (Model-View-Controller)** architectural pattern to keep the code organized and maintainable:

* **`/src/Models`**: Core data structures and database connection logic (`Product.java`, `Order.java`, `Ingredient.java`, `db.java`).
* **`/src/Views`**: Java Swing GUI components (`POSView`, `AdminView`, `LoginView`, `Main`).
* **`/src/Controllers`**: Business logic binding views to models (`POSController`, `AdminController`, `LoginController`).
* **`/src/Services`**: External utility integrations (`ReceiptGenerator.java`, `TelegramService.java`).
* **`/lib`**: Contains necessary dependencies (MySQL connector, iTextPDF, JCalendar).
* **`/product_images` & `/src/Image`**: Image assets for the UI and product catalog.

## 💻 Technologies Used

* **Java SE** (GUI via Java Swing)
* **MySQL** (Database via `mysql-connector-j-8.0.33.jar`)
* **iTextPDF** (`itextpdf-5.5.13.4.jar`) - For generating `.pdf` receipts
* **JCalendar** (`jcalendar-1.4.jar`) - For date selection in UI
* **Telegram Bot API** - For pushing notifications to specific Telegram channels/users.

## 🚀 Setup & Installation

### Prerequisites
1. **Java Development Kit (JDK) 8 or higher** installed.
2. **MySQL Server** installed and running.
3. An IDE like **Apache NetBeans** (Recommended, as this project uses NetBeans `nbproject` files), Eclipse, or IntelliJ IDEA.

### 1. Database Configuration
1. Create a MySQL database for the project (e.g., `cafepos_db`).
2. Update the database connection settings (URL, Username, Password) in the `src/Models/db.java` file.
   ```java
   // Example configuration in db.java
   Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cafepos_db", "root", "password");
