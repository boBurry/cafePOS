package Models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class db {
    private static Connection con = null;
   
    private db() {}
    
    public static Connection myCon() {
        // Check if connection is null or closed before creating a new one
        try {
            if (con == null || con.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // --- 1. FILL IN YOUR FREE MYSQL DETAILS HERE ---
                String host = "sql12.freemysqlhosting.net";   
                String dbName = "sql12816914";                 
                String username = "sql12816914";               
                String password = "YOUR_PASSWORD_HERE";     
                String port = "3306";                    
                // -----------------------------------------------
                
                // 2. CONNECTION URL
                // We add 'useSSL=false' because free hosts often don't have verified certificates
                String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true";
                
                con = DriverManager.getConnection(url, username, password);
                System.out.println("DEBUG: Connected to Remote Database!");
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Error while connecting to database: " + e);
            JOptionPane.showMessageDialog(null, "Connection Failed:\n" + e.getMessage());
        }
        return con; 
    }
}