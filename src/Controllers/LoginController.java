package Controllers;

import Views.Login;
import Views.GUI;
import Models.db;
import java.sql.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

public class LoginController {
    
    private Login view;

    public LoginController(Login view) {
        this.view = view;
        
        this.view.getBtnLogin().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        this.view.getRootPane().setDefaultButton(view.getBtnLogin());
    }

    private void performLogin() {
        String user = view.getUsername();
        String pass = view.getPassword();
        
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please fill in all fields.");
            return;
        }

        String role = checkLogin(user, pass);

        if (role != null) {
            JOptionPane.showMessageDialog(view, "Welcome, " + role + "!");
            view.dispose();
            
           
            new GUI(role).setVisible(true); 
        } else {
            JOptionPane.showMessageDialog(view, "Invalid Username or Password.");
        }
    }

    public String checkLogin(String username, String password) {
        Connection con = db.myCon();
        String role = null;
        
        try {
            String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);
            
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                role = rs.getString("role"); 
            }   
        } catch (Exception e) {
            System.out.println("DB Error: " + e.getMessage());
            e.printStackTrace();
        }
        return role;
    }
}