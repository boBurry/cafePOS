package Views;

public class Main {
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }   
        
        java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new Views.LoginView().setVisible(true);
                }
        });
    }
}
