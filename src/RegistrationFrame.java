
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;


public class RegistrationFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JButton registerButton;
    private final LoginFrame loginFrame;
    private JLabel emailErrorLabel;
    private JLabel passwordErrorLabel;

    public RegistrationFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;
        createUI();
    }  

    private void createUI() {
        setTitle("Sign Up");
        setSize(300, 200);
        setLocationRelativeTo(loginFrame); // Position relative to the login frame
        setLayout(new GridLayout(7, 2, 5, 5));

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();
        emailField = new JTextField();
        registerButton = new JButton("Register");
        
        emailErrorLabel = new JLabel("Invalid email format");
        emailErrorLabel.setForeground(Color.RED);
        emailErrorLabel.setVisible(false); // Initially not visible
        
        passwordErrorLabel = new JLabel("Passwords do not match");
        passwordErrorLabel.setForeground(Color.RED);
        passwordErrorLabel.setVisible(false);
        
       

        registerButton.addActionListener(this::registerAction);

        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(new JLabel("Confirm Password:"));
        add(confirmPasswordField);
        add(new JLabel("Email:"));
        add(emailField);
        add(emailErrorLabel); // 添加错误标签到布局
        add(passwordErrorLabel);
        add(new JLabel()); // Placeholder
        add(registerButton);
    }

    private void registerAction(ActionEvent event) {
        // 这里应该有代码来处理注册逻辑，比如检查输入的有效性，
        // 检查密码和确认密码是否匹配，以及可能的其他逻辑
    	String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String email = emailField.getText();

        if (!isEmailValid(email)) {
        	emailErrorLabel.setVisible(true); // 显示错误信息
            // 不再调用 pack(); 改为调用 revalidate() 和 repaint()
            getContentPane().revalidate();
            getContentPane().repaint();
        } else {
            emailErrorLabel.setVisible(false); // 隐藏错误信息
            if (password.equals(confirmPassword)) {
            	passwordErrorLabel.setVisible(false);
            	 // 假设注册成功后，将信息填写到登录框
                loginFrame.setUsernameAndPassword(usernameField.getText(), new String(passwordField.getPassword()));
                
                
                try {
                    URL url = new URL("http://localhost:8080/register"); // 修改为您的服务器URL
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    con.setDoOutput(true);

                    String urlParameters = String.format("username=%s&password=%s&email=%s", 
                                                         URLEncoder.encode(username, "UTF-8"), 
                                                         URLEncoder.encode(password, "UTF-8"),
                                                         URLEncoder.encode(email, "UTF-8"));

                    try (DataOutputStream out = new DataOutputStream(con.getOutputStream())) {
                        out.writeBytes(urlParameters);
                        out.flush();
                    }

                    int responseCode = con.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // 处理服务器响应
                    } else {
                        // 处理错误
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                // 关闭注册窗口
                dispose();
                // 提交注册信息
                
                // ...
            } else {
            	passwordErrorLabel.setVisible(true);
                // 处理密码不匹配的情况
                // ...
            }
        }
      
    }
    
    // Getter方法用于获取用户输入
    public String getUsername() {
        return usernameField.getText();
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }
    
    public char[] getConfirmPassword() {
        return confirmPasswordField.getPassword();
    }
    
    public String getEmail() {
        return emailField.getText();
    }
    private boolean isEmailValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}

