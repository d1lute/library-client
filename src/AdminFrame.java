
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

public class AdminFrame extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);

    public AdminFrame() {
        initUI();
    }
    private void initUI() {
        setTitle("Library Management System - Admin Interface");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create the left-side menu bar
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        JButton uploadButton = new JButton("Upload E-book");
        JButton borrowButton = new JButton("Borrow Book");
        JButton returnButton = new JButton("Return Book");
        JButton stockButton = new JButton("Stock Books");
        JButton coverImageButton = new JButton("Upload Cover Image");
        JButton addAdminButton = new JButton("addAdmin");

        menuPanel.add(uploadButton);
        menuPanel.add(borrowButton);
        menuPanel.add(returnButton);
        menuPanel.add(stockButton);
        menuPanel.add(coverImageButton);
        menuPanel.add(addAdminButton);

       
        cardPanel.add(createUploadPanel(), "Upload");
        cardPanel.add(createBorrowPanel(), "Borrow");
        cardPanel.add(createReturnPanel(), "Return");
        cardPanel.add(createStockPanel(), "Stock");
        cardPanel.add(createCoverImagePanel(), "CoverImage");

        
        uploadButton.addActionListener(e -> cardLayout.show(cardPanel, "Upload"));
        borrowButton.addActionListener(e -> cardLayout.show(cardPanel, "Borrow"));
        returnButton.addActionListener(e -> cardLayout.show(cardPanel, "Return"));
        stockButton.addActionListener(e -> cardLayout.show(cardPanel, "Stock"));
        coverImageButton.addActionListener(e -> cardLayout.show(cardPanel, "CoverImage"));
        addAdminButton.addActionListener(e -> openAddadminwindow());

        getContentPane().add(menuPanel, BorderLayout.WEST);
        getContentPane().add(cardPanel, BorderLayout.CENTER);
    }
    
    private void openAddadminwindow() {
        AddAdmin addAdmin = new AddAdmin();
        addAdmin.setVisible(true);
    }
    
    private void uploadEbook(File file) {
        try {
            URL url = new URL("http://localhost:8080/api/books/upload");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            String boundary = Long.toHexString(System.currentTimeMillis()); // Random Boundary String
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setDoOutput(true);

            try (OutputStream output = con.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {

                // Add file part
                writer.append("--" + boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"").append("\r\n");
                writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(file.getName())).append("\r\n");
                writer.append("Content-Transfer-Encoding: binary").append("\r\n");
                writer.append("\r\n").flush();
                Files.copy(file.toPath(), output);
                output.flush(); // Important before continuing with writer!
                writer.append("\r\n").flush(); // CRLF is important! It indicates end of boundary.

                // End of multipart/form-data.
                writer.append("--" + boundary + "--").append("\r\n").flush();
            }

            // Read Response
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Handle Successful Reques
                try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                    }
                }
            } else {
                // Handle Failed Request
                System.out.println("Upload failed: HTTP error code " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void stockBook(String title, int quantity) {
        try {
            URL url = new URL("http://localhost:8080/api/books/stock?title=" + URLEncoder.encode(title, "UTF-8") + "&quantity=" + quantity);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            // Send POST Request
            try (OutputStream os = con.getOutputStream()) {
                os.flush();
            }

            // Read Response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void borrowBook(String title, String email) {
        try {
            URL url = new URL("http://localhost:8080/api/books/borrow?title=" + URLEncoder.encode(title, "UTF-8") + "&email=" + URLEncoder.encode(email, "UTF-8"));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            // Send POST Request
            try (OutputStream os = con.getOutputStream()) {
                os.flush();
            }

            // Check Response Code
            int responseCode = con.getResponseCode();
            if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) { // Check for 400 and above
                try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JOptionPane.showMessageDialog(null, response.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Read Response
                try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println(response.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void returnBook(String title, String email) {
        try {
            URL url = new URL("http://localhost:8080/api/books/return?title=" + URLEncoder.encode(title, "UTF-8") + "&email=" + URLEncoder.encode(email, "UTF-8"));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            // Send POST Request
            try (OutputStream os = con.getOutputStream()) {
                os.flush();
            }

            // Check Response Code
            int responseCode = con.getResponseCode();
            if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) { // Check for 400 and above
                try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JOptionPane.showMessageDialog(null, response.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Read Response
                try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println(response.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createCoverImagePanel() {
        JPanel panel = new JPanel();
        
        // Create and add book name input field.
        JLabel bookNameLabel = new JLabel("Book Name:");
        JTextField bookNameField = new JTextField(20);
        panel.add(bookNameLabel);
        panel.add(bookNameField);

        // Create and add select image button
        JButton selectImageButton = new JButton("Select and Upload Cover Image");
        selectImageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image files", "jpg", "png", "jpeg", "gif", "bmp");
            fileChooser.setFileFilter(filter);
            
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String bookName = bookNameField.getText(); // Get Book Name
                uploadCoverImage(file, bookName); // Call Upload Method
            }
        });
        panel.add(selectImageButton);
        return panel;
    }
    
    private void uploadCoverImage(File file, String bookName) {
        String boundary = Long.toHexString(System.currentTimeMillis()); // Random Boundary
        String CRLF = "\r\n"; // Line Separator
        HttpURLConnection connection = null;

        try {
            URL url = new URL("http://localhost:8080/api/books/uploadCover");
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream output = connection.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {

                // Send text field
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"title\"").append(CRLF);
                writer.append("Content-Type: text/plain; charset=UTF-8").append(CRLF);
                writer.append(CRLF).append(bookName).append(CRLF).flush();

                // Send file
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"").append(CRLF);
                writer.append("Content-Type: " + Files.probeContentType(file.toPath())).append(CRLF); // Guess File Type
                writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                writer.append(CRLF).flush();
                Files.copy(file.toPath(), output);
                output.flush(); 
                writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

                // End Boundary
                writer.append("--" + boundary + "--").append(CRLF).flush();
            }

            // Get response, check if successful
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println("Response: " + response.toString());
                }
            } else {
                System.out.println("Upload failed, HTTP response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    
    private JPanel createUploadPanel() {
        JPanel panel = new JPanel();
        JButton uploadFileButton = new JButton("Select Files and Upload");
        uploadFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true); // Enable multiple file selection
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File[] files = fileChooser.getSelectedFiles(); // Get selected files
                for (File file : files) {
                    uploadEbook(file); // Upload each file
                }
            }
        });
        panel.add(uploadFileButton);
        return panel;
    }

    private JPanel createBorrowPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Create and Setup Input Fields and Labels
        JLabel bookNameLabel = new JLabel("Book Name:");
        JTextField bookNameField = new JTextField(20);
        JLabel emailLabel = new JLabel("Your Email:");
        JTextField emailField = new JTextField(20);  // Email Input Field
        // Create and set up submit button
        JButton submitButton = new JButton("Submit Borrow");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String bookName = bookNameField.getText();
                String email = emailField.getText();  // Get Email Address
                int quantity;
                try {
                    if (!email.isEmpty()) {
                        borrowBook(bookName, email);
                    } else {
                        JOptionPane.showMessageDialog(AdminFrame.this,
                                "Quantity must be greater than 0 and email address cannot be empty",
                                "Input Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(AdminFrame.this,
                            "Please enter a valid quantity",
                            "Input Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Add components to panel
        panel.add(bookNameLabel);
        panel.add(bookNameField);
        panel.add(emailLabel);
        panel.add(emailField);  // Add email input field to panel
        panel.add(submitButton);
        
        return panel;
    }
    
    private JPanel createReturnPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Create and Setup Input Fields and Labels
        JLabel bookNameLabel = new JLabel("Book Name:");
        JTextField bookNameField = new JTextField(20);
        JLabel emailLabel = new JLabel("Your Email:");
        JTextField emailField = new JTextField(20);  // Email Input Field
        // Create and set up submit button
        JButton submitButton = new JButton("Submit Return");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String bookName = bookNameField.getText();
                String email = emailField.getText();  // Get Email Address
                int quantity;
                try {
                    if (!email.isEmpty()) {
                        returnBook(bookName, email);
                    } else {
                        JOptionPane.showMessageDialog(AdminFrame.this,
                                "Quantity must be greater than 0 and email address cannot be empty",
                                "Input Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(AdminFrame.this,
                            "Please enter a valid quantity",
                            "Input Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Add components to panel
        panel.add(bookNameLabel);
        panel.add(bookNameField);
        panel.add(emailLabel);
        panel.add(emailField);  // Add email input field to panel
        panel.add(submitButton);
        
        return panel;
    }

    private JPanel createStockPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Create and Setup Input Fields and Labels
        JLabel bookNameLabel = new JLabel("Book Name:");
        JTextField bookNameField = new JTextField(20);
        JLabel quantityLabel = new JLabel("Stock Quantity:");
        JTextField quantityField = new JTextField(5);
        
        // Create and set up submit button
        JButton submitButton = new JButton("Submit Stock");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String bookName = bookNameField.getText();
                String quantityStr = quantityField.getText();
                int quantity;
                try {
                    quantity = Integer.parseInt(quantityStr);
                    if (quantity > 0) {
                    	stockBook(bookName, quantity);
                    } else {
                        JOptionPane.showMessageDialog(AdminFrame.this,
                                "Quantity must be greater than 0",
                                "Input Erro",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(AdminFrame.this,
                            "Please enter a valid quantity",
                            "Input Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Add components to panel
        panel.add(bookNameLabel);
        panel.add(bookNameField);
        panel.add(quantityLabel);
        panel.add(quantityField);
        panel.add(submitButton);
        
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminFrame frame = new AdminFrame();
            frame.setVisible(true);
        });
    }
}
