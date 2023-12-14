
import javax.swing.*;
import java.awt.*;

public class UserFrame extends JFrame {

    public UserFrame() {
        createUI();
    }

    private void createUI() {
        setTitle("Admin Dashboard");
        setSize(600, 400);
        setLocationRelativeTo(null); // 居中窗口
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 您可以在这里添加更多的UI组件

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminFrame().setVisible(true));
    }
}

