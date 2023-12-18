
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import nl.siegmann.epublib.domain.SpineReference;
import java.util.List;
import java.util.Map;

import javafx.concurrent.Worker;


public class UserFrame extends JFrame {

    private JPanel bookShelfPanel;
    private JFrame bookReaderFrame; // Window for reading books.
    private JFXPanel bookReaderPanel; // avaFX panel for displaying book content.
    private JPanel menuPanel; // Menu panel.
    private int currentPage = 0; //Current page number.
    private List<JPanel> bookPanels;
    private JLabel pageLabel; // Label for displaying the page number.
    private JTextField pageTextField; // Text field for entering the page number.
    private JButton myBookshelfButton;
    private JButton libraryBookshelfButton;
    private String username;
    private int totalPages = 0;
    private String currentBookshelfType; // Current bookshelf type


    public UserFrame(String username) {
    	this.username = username;
    	Platform.startup(() -> {});

        // Construct and display Swing UI.
        SwingUtilities.invokeLater(this::createUI);
    }

    private void createUI() {
        setTitle("E-book Reader");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Menu panel
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        // Bookshelf panel
        bookShelfPanel = new JPanel();
        bookShelfPanel.setLayout(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(bookShelfPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Page control panel
        JPanel pageControlPanel = new JPanel();
        JButton prevPageButton = new JButton("Previous Page");
        JButton nextPageButton = new JButton("Next Page");
        JButton gotoPageButton = new JButton("Go To");
        pageLabel = new JLabel("Page Number: 1");
        pageTextField = new JTextField(5);

        prevPageButton.addActionListener(e -> prevPage());
        nextPageButton.addActionListener(e -> nextPage());
        gotoPageButton.addActionListener(e -> gotoPage());

        pageControlPanel.add(prevPageButton);
        pageControlPanel.add(pageLabel);
        pageControlPanel.add(pageTextField);
        pageControlPanel.add(gotoPageButton);
        pageControlPanel.add(nextPageButton);

        // Add components to the main frame.
        add(menuPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
        add(pageControlPanel, BorderLayout.SOUTH);

        bookPanels = new ArrayList<>();
        
        myBookshelfButton = new JButton("My Bookshelf");
        libraryBookshelfButton = new JButton("Library Bookshelf");

        myBookshelfButton.addActionListener(e -> {currentPage = 0;loadBookshelf("my");});
        libraryBookshelfButton.addActionListener(e -> {currentPage = 0;loadBookshelf("library");});

        menuPanel.add(myBookshelfButton);
        menuPanel.add(libraryBookshelfButton);
    }

   

    private void prevPage() {
        if (currentPage > 0) {
            currentPage--;
            loadBookshelf(currentBookshelfType);
        }
    }

    private void nextPage() {
        if ((currentPage + 1)  < totalPages) {
            currentPage++;
            loadBookshelf(currentBookshelfType);
        }
    }

    private void gotoPage() {
        try {
            int page = Integer.parseInt(pageTextField.getText()) - 1;
            if (page >= 0 && page < totalPages) {
                currentPage = page;
                loadBookshelf(currentBookshelfType);
            } else {
                JOptionPane.showMessageDialog(this, "The entered page number is invalid!");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid page number!");
        }
    }
   
    private void loadBookshelf(String type) {
    	currentBookshelfType = type; 
        clearDirectory("images"); // Clear the images folder.
        clearDirectory("epubs"); //Clear the epubs folder
        bookShelfPanel.removeAll();
        bookShelfPanel.revalidate();
        bookShelfPanel.repaint();

        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
            	if ("library".equals(type)) {
            		return fetchBooksFromServer(currentPage); // Request books based on the current page number
            	}
            	else {
                    return fetchMyBooksFromServer(currentPage); 
            	}
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> books = get();
                    displayBooks(books); // Display books on the new page
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(UserFrame.this, "Failed to get books!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        
        
    }


    
    
    private void clearDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (!file.isDirectory()) { // Ensure that subfolders are not deleted
                    file.delete();
                }
            }
        }
    }

    
    
    public List<Map<String, Object>> fetchBooksFromServer(int page) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/books/all" + "?page="+ page +"&size=20"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONObject jsonObject = new JSONObject(response.body());
                totalPages = jsonObject.getInt("totalPages"); // Update the total number of books.
                JSONArray booksArray = jsonObject.getJSONArray("books");
                return convertJsonArrayToList(booksArray);
            } else {
                // Handle error situations
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // Handle exceptional situations.
        }
        return null;
    }
    
    public List<Map<String, Object>> fetchMyBooksFromServer(int page) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/books/collections/"+ username + "?page="+ page +"&size=20"))
                .header("Content-Type", "application/json")
                .GET()
                .build();
      

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONObject jsonObject = new JSONObject(response.body());
                totalPages = jsonObject.getInt("totalPages");
                JSONArray booksArray = jsonObject.getJSONArray("books");
                return convertJsonArrayToList(booksArray);
            } else {
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String extractFileName(String path) {
        path = path.replace('\\', '/');
        return path.substring(path.lastIndexOf('/') + 1);
    }
    private String encodeFileNameForURL(String fileName) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        return encoded.replace("+", "%20");
    }

    private List<Map<String, Object>> convertJsonArrayToList(JSONArray jsonArray) {
        List<Map<String, Object>> booksList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject bookObject = jsonArray.getJSONObject(i);
            Map<String, Object> bookMap = new HashMap<>();
            if(!bookObject.isNull("COVER_IMAGE_PATH")) {
            String coverImagePath = bookObject.getString("COVER_IMAGE_PATH");
            if (coverImagePath != null && !coverImagePath.isEmpty()) {
                String fileName = extractFileName(coverImagePath);
                coverImagePath = downloadFile("http://localhost:8080/images/" + fileName, "images/" + fileName);
            }
            bookMap.put("COVER_IMAGE_PATH", coverImagePath);
            }
            if(!bookObject.isNull("EPUB_PATH")) {
            String epubPath = bookObject.optString("EPUB_PATH", null);
            if (epubPath != null && !epubPath.isEmpty()) {
                String fileName = extractFileName(epubPath);
                epubPath = downloadFile("http://localhost:8080/epubs/" + fileName, "epubs/" + fileName);
                bookMap.put("EPUB_PATH", epubPath); 
            }
            }


            bookMap.put("BOOK_ID", bookObject.getInt("BOOK_ID"));
            bookMap.put("TITLE", bookObject.getString("TITLE"));
            bookMap.put("QUANTITY", bookObject.getInt("QUANTITY"));
            if(bookObject.has("daysRemaining")){
            	bookMap.put("daysRemaining", bookObject.getInt("daysRemaining"));
            }
            
            booksList.add(bookMap);
        }
        return booksList;
    }

    
    
    private String downloadFile(String urlString, String localPath) {
        try {
            String encodedFileName = encodeFileNameForURL(extractFileName(urlString));
            urlString = urlString.replace(extractFileName(urlString), encodedFileName);

            URL url = new URL(urlString);
            Path path = Paths.get(localPath);

            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            try (InputStream in = url.openStream()) {
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }

            return localPath; 
        } catch (IOException e) {
            e.printStackTrace();
            return null; 
        }
    }


    private void displayBooks(List<Map<String, Object>> books) {
    	if (books == null) {
            return; 
        }
    	int row = 0; 
        int col = 0; 
        for (Map<String, Object> book : books) {
            String title = (String) book.get("TITLE");
            String coverImagePath = (String) book.get("COVER_IMAGE_PATH");
            String epubPath = (String) book.get("EPUB_PATH");
            int quantity = (Integer) book.get("QUANTITY");

            ImageIcon coverIcon = null;
            if (coverImagePath != null && !coverImagePath.isEmpty()) {
                coverIcon = new ImageIcon(coverImagePath);
            }

            File bookFile = null;
            if (epubPath != null && !epubPath.isEmpty()) {
                bookFile = new File(epubPath);
            }
            if (col >= 5) { 
                col = 0;
                row++;
            }

            displayBookInShelf(title, coverIcon, bookFile, quantity, row, col, book); // Now includes the quantity
            col++; 
        }
    }
    

    private void displayBookInShelf(String title, ImageIcon coverIcon, File bookFile, int quantity, int row, int col, Map<String, Object> book) {
        SwingUtilities.invokeLater(() -> {
            
            JPanel bookPanel = new JPanel(new BorderLayout());
            bookPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

           
            if (coverIcon != null) {
                JLabel coverLabel = new JLabel(new ImageIcon(coverIcon.getImage().getScaledInstance(100, 150, java.awt.Image.SCALE_SMOOTH)));
                bookPanel.add(coverLabel, BorderLayout.CENTER);
            }

          
            JLabel titleLabel = new JLabel(title);
            titleLabel.setHorizontalAlignment(JLabel.CENTER);
            bookPanel.add(titleLabel, BorderLayout.SOUTH);
            
            
            JLabel topLabel;
            if ("my".equals(currentBookshelfType) && book.containsKey("daysRemaining")) {
                int daysRemaining = (int) book.get("daysRemaining");
                topLabel = new JLabel("Days remaining: " + daysRemaining, SwingConstants.CENTER);
            } else {
                topLabel = new JLabel("Stock:  " + quantity, SwingConstants.CENTER);
            }
            bookPanel.add(topLabel, BorderLayout.NORTH);

            
            Dimension bookPanelSize = new Dimension(120, 180);
            bookPanel.setPreferredSize(bookPanelSize);
            bookPanel.setMaximumSize(bookPanelSize);
            bookPanel.setMinimumSize(bookPanelSize);
            bookPanel.addMouseListener(new MouseAdapter() {
            	@Override
                public void mouseEntered(MouseEvent e) {
                    
                    bookPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    
                    bookPanel.setBorder(BorderFactory.createEmptyBorder());
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    
                    bookPanel.setBorder(BorderFactory.createLoweredBevelBorder());
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    
                    bookPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    
                    
                    openBookReader(bookFile);
                }
            });


            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = col; 
            gbc.gridy = row; 
            gbc.insets = new Insets(5, 5, 5, 5); 
            bookShelfPanel.add(bookPanel, gbc);
            bookShelfPanel.revalidate();
            bookShelfPanel.repaint();
        });
    }
    
    private String convertImageToDataURI(Resource imageResource) throws IOException {
        String imageBase64 = Base64.getEncoder().encodeToString(imageResource.getData());
        String imageMimeType = imageResource.getMediaType().getName();
        return "data:" + imageMimeType + ";base64," + imageBase64;
    }
    private void openBookReader(File bookFile) {
        Platform.runLater(() -> {
            try {
            	if (bookFile == null || !bookFile.exists()) {
                    JOptionPane.showMessageDialog(null, "The electronic version of this book is not yet ready", "File does not exist", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (bookReaderFrame == null) {
                    bookReaderFrame = new JFrame("E-book Reader");
                    bookReaderPanel = new JFXPanel();
                    bookReaderFrame.add(bookReaderPanel);
                    bookReaderFrame.setSize(800, 600);
                    bookReaderFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                }
                
                WebView webView = new WebView();
                
                webView.getEngine().getLoadWorker().stateProperty().addListener(
                	    (obs, oldState, newState) -> {
                	        if (newState == Worker.State.SUCCEEDED) {
                	           
                	        }
                	    }
                	);

                	webView.getEngine().locationProperty().addListener(
                	    (obs, oldLocation, newLocation) -> {
                	        
                	    }
                	);

                	
                	
                	
               
                	Book book = (new EpubReader()).readEpub(new FileInputStream(bookFile));
                    StringBuilder allPagesContent = new StringBuilder();

                    for (SpineReference spineReference : book.getSpine().getSpineReferences()) {
                        Resource pageResource = spineReference.getResource();
                        String pageContent = new String(pageResource.getData(), "UTF-8");

                        
                        for (Resource imageResource : book.getResources().getAll()) {
                            if (isImageResource(imageResource)) {
                                String imageDataURI = convertImageToDataURI(imageResource);
                                pageContent = pageContent.replace(imageResource.getHref(), imageDataURI);
                            }
                        }

                        allPagesContent.append(pageContent).append("\n");
                    }

                    webView.getEngine().loadContent(allPagesContent.toString(), "text/html");

                    Scene scene = new Scene(webView);
                    bookReaderPanel.setScene(scene);
                    bookReaderFrame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
         }
    
	    private boolean isImageResource(Resource resource) {
	        String mimeType = resource.getMediaType().getName();
	        return mimeType.startsWith("image/");
	    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new UserFrame("123").setVisible(true);
        });
    }
}

