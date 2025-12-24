package OOP;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class db {
    private static Connection con = null;
   
    private db() {}
    
    public static Connection myCon() {
        if (con == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://localhost:3308/cafePOS", "root", "");
            } catch (ClassNotFoundException | SQLException e) {
                System.out.println("Error while connecting to database: " + e);
            }
        }
        return con; 
    }
}

