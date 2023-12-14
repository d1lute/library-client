
import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginFrame extends JFrame {
	private JButton registerButton;
	private JPasswordField passwordField;
	private JTextField usernameField;
	private JRadioButton adminButton;
	private JRadioButton userButton;

    public LoginFrame() {
        createUI();
    }

    private void createUI() {
        setTitle("User Login");
        setSize(400, 300);
        setLocationRelativeTo(null); // Center the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // User type selection
        adminButton = new JRadioButton("Admin");
        userButton = new JRadioButton("User");
        ButtonGroup group = new ButtonGroup();
        group.add(adminButton);
        group.add(userButton);
        adminButton.setSelected(true);

        // Input fields
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);

        // Buttons
        JButton loginButton = new JButton("Log in");
        JButton registerButton = new JButton("Sign up");
        
        
        //event
        registerButton.addActionListener(e -> openRegistrationWindow());
        loginButton.addActionListener(e -> loginUser());
        
        
        // Adding components to the frame
        add(new JLabel("User Type:"), gbc);
        JPanel userTypePanel = new JPanel(new FlowLayout());
        userTypePanel.add(adminButton);
        userTypePanel.add(userButton);
        add(userTypePanel, gbc);

        add(new JLabel("Username:"), gbc);
        add(usernameField, gbc);
        add(new JLabel("Password:"), gbc);
        add(passwordField, gbc);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(loginButton);
        buttonsPanel.add(registerButton);
        add(buttonsPanel, gbc);

        gbc.weighty = 1;
        add(new JLabel(""), gbc); // Pushes everything to the top
    }
    
    
    private void loginUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        boolean isAdmin = adminButton.isSelected();
        String userType = isAdmin ? "admin" : "user";
        String loginUrl = "http://localhost:8080/login/" + userType; // 根据用户类型调整URL
        System.out.println(loginUrl);
        try {
        	URL url = new URL(loginUrl); // 修改为您的服务器登录URL
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            con.setDoOutput(true);

            String urlParameters = "username=" + URLEncoder.encode(username, "UTF-8") + 
                                   "&password=" + URLEncoder.encode(password, "UTF-8");

            try (DataOutputStream out = new DataOutputStream(con.getOutputStream())) {
                out.writeBytes(urlParameters);
                out.flush();
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 登录成功，关闭登录界面并打开用户界面
                SwingUtilities.invokeLater(() -> {
                    dispose(); // 关闭登录界面
                    if (isAdmin) {
                        new AdminFrame().setVisible(true); // 打开管理员界面
                    } else {
                        new UserFrame().setVisible(true); // 打开普通用户界面
                    }
                });
            } else {
                // 登录失败，显示错误消息
                JOptionPane.showMessageDialog(this, "Login failed: Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void openRegistrationWindow() {
        RegistrationFrame registrationFrame = new RegistrationFrame(this);
        registrationFrame.setVisible(true);
    }
    
    public void setUsernameAndPassword(String username, String password) {
    	usernameField.setText(username);
        passwordField.setText(password);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
