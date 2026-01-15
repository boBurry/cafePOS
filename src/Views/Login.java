package Views;

import Controllers.LoginController;
import java.awt.*;
import javax.swing.*;

public class Login extends JFrame {

    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JButton btnLogin;

    public Login() {
        initComponents();
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
       
        new LoginController(this);
    }

    private void initComponents() {
        setTitle("SRR Cafe Shop - Login");
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setUndecorated(true); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
        JPanel mainPanel = new JPanel(new GridBagLayout()); 
        mainPanel.setBackground(new Color(245, 245, 245));
        add(mainPanel);
      
        JPanel card = new JPanel(null); 
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(500, 300)); 
        mainPanel.add(card);

        JLabel lbLogo = new JLabel();
        lbLogo.setHorizontalAlignment(SwingConstants.CENTER);
        java.net.URL imgURL = getClass().getResource("/Image/logo.png");
        if (imgURL != null) {
            lbLogo.setIcon(new ImageIcon(
                new ImageIcon(imgURL).getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH)
            ));
        }
        lbLogo.setBounds(30, 70, 150, 150);
        card.add(lbLogo);
        
        JLabel lbUser = new JLabel("Username:");
        lbUser.setBounds(230, 60, 100, 25);
        card.add(lbUser);

        tfUsername = new JTextField();
        tfUsername.setBounds(230, 90, 230, 35);
        card.add(tfUsername);

        JLabel lbPass = new JLabel("Password:");
        lbPass.setBounds(230, 140, 100, 25);
        card.add(lbPass);

        pfPassword = new JPasswordField();
        pfPassword.setBounds(230, 170, 230, 35);
        card.add(pfPassword);

        btnLogin = new JButton("Login");
        btnLogin.setBounds(230, 230, 120, 40);
        card.add(btnLogin);
        
        JButton btnExit = new JButton("X");
        btnExit.setBounds(440, 10, 45, 30);
        btnExit.setBackground(Color.RED);
        btnExit.setForeground(Color.WHITE);
        btnExit.setBorder(null);
        btnExit.setFocusPainted(false);
        btnExit.setOpaque(false);
        btnExit.addActionListener(e -> System.exit(0));
        card.add(btnExit);
    }
    
    public String getUsername() {
        return tfUsername.getText().trim();
    }

    public String getPassword() {
        return String.valueOf(pfPassword.getPassword());
    }

    public JButton getBtnLogin() {
        return btnLogin;
    }

    public static void main(String[] args) {
        new Login().setVisible(true);
    }
}