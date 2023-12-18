import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class AddAdmin extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JButton registerButton;
    private JLabel emailErrorLabel;
    private JLabel passwordErrorLabel;

    public AddAdmin() {
        createUI();
    }  

    private void createUI() {
        setTitle("Sign Up");
        setSize(300, 200);
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
        add(emailErrorLabel); 
        add(passwordErrorLabel);
        add(new JLabel()); // Placeholder
        add(registerButton);
    }

    private void registerAction(ActionEvent event) {
        
    	String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String email = emailField.getText();

        if (!isEmailValid(email)) {
        	emailErrorLabel.setVisible(true); // Display error message
            getContentPane().revalidate();
            getContentPane().repaint();
        } else {
            emailErrorLabel.setVisible(false); // Hide error message."
            if (password.equals(confirmPassword)) {
            	passwordErrorLabel.setVisible(false);
            	 // Assuming successful registration, populate the information in the login field.
               
                
                
                try {
                    URL url = new URL("http://localhost:8080/addAdmin");
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
                        // Handle the server response
                    } else {
                        // Handle errors.
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                // Close the registration window.
                dispose();
                // Submit registration information.
                
                // ...
            } else {
            	passwordErrorLabel.setVisible(true);
                // Handle the case where the passwords do not match."

                // ...
            }
        }
      
    }
    
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