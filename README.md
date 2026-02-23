CafePOS ☕
CafePOS is a comprehensive Java-based Point of Sale (POS) desktop application tailored specifically for cafes and coffee shops. Built using Java Swing, it features an intuitive user interface, robust database management with MySQL, automated PDF receipt generation, and real-time Telegram notifications.

🌟 Features
User Authentication: Secure login system with dedicated views for standard users and administrators.

Point of Sale Interface: * Easy-to-use cart management.

Product categorization and visual menus.

Drink Customization: Specialized dialogs to configure drink options (e.g., sugar levels, ice levels, add-ons).

Admin Dashboard: Manage inventory, add new products, and track ingredients.

Automated Receipts: Automatically generates printable PDF receipts for every transaction using iTextPDF.

Telegram Integration: Sends real-time transaction alerts or daily summaries to a configured Telegram bot.

Database Driven: Powered by MySQL to reliably store users, products, orders, and ingredient inventories.

🏗️ Architecture & Structure
The project strictly follows the MVC (Model-View-Controller) architectural pattern to keep the code organized and maintainable:

/src/Models: Core data structures and database connection logic (Product.java, Order.java, Ingredient.java, db.java).

/src/Views: Java Swing GUI components (POSView, AdminView, LoginView, DrinkCustomizationDialog).

/src/Controllers: Business logic binding views to models (POSController, AdminController, LoginController).

/src/Services: External utility integrations (ReceiptGenerator.java for PDFs, TelegramService.java for bot alerts).

/lib: Contains necessary dependencies.

💻 Technologies Used
Java SE (GUI via Java Swing)

MySQL (Database via mysql-connector-j-8.0.33.jar)

iTextPDF (itextpdf-5.5.13.4.jar) - For generating .pdf receipts

JCalendar (jcalendar-1.4.jar) - For date selection in UI

Telegram Bot API - For pushing notifications to specific Telegram channels/users.

🚀 Setup & Installation
Prerequisites
Java Development Kit (JDK) 8 or higher installed.

MySQL Server installed and running.

An IDE like Apache NetBeans (Recommended, as this includes nbproject files), Eclipse, or IntelliJ IDEA.

1. Database Configuration
Create a MySQL database for the project (e.g., cafepos_db).

Update the database connection settings (URL, Username, Password) in the src/Models/db.java file.

Java
// Example configuration in db.java
Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cafepos_db", "root", "password");
(Note: Ensure you have the required tables initialized for Users, Products, Orders, and Ingredients).

2. Running the Application
Using NetBeans:

Open Apache NetBeans.

Click File -> Open Project and select the cafePOS folder.

Clean and Build the project to ensure the libraries (/lib) are correctly mapped.

Run the src/Views/Main.java file (or set it as the Main Class for the project) to launch the Login screen.

3. Telegram Bot Setup (Optional)
To enable Telegram notifications:

Create a bot using BotFather on Telegram and copy the API Token.

Obtain the Chat ID of the user or group you want to notify.

Update the TelegramService.java file with your specific BOT_TOKEN and CHAT_ID.

📂 Generated Files
Receipts: Upon successful checkout, PDF receipts are automatically generated and saved directly to the root Receipts/ directory.

🛡️ License
This project is for educational and personal use. Feel free to modify and expand upon it!
