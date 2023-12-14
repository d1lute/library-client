import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;



public class BookshelfApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Bookshelf Application");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600); // 设置窗口大小
            frame.setLayout(new BorderLayout()); // 设置布局管理器

            // 初始化UI组件
            
            initUI(frame);

            frame.setVisible(true); // 显示窗口
        });
    }
    
    private static List<Book> getBooks(String shelfType) {
        String urlString = shelfType.equals("myBookshelf") ?
                           "http://localhost:8080/api/books/myBookshelf" :
                           "http://localhost:8080/api/books/libraryShelf";

        List<Book> books = new ArrayList<>();
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // 解析JSON响应
                JSONArray jsonArray = new JSONArray(response.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String title = jsonObject.getString("title");
                    String imageUrl = jsonObject.getString("imageUrl");
                    books.add(new Book(title, imageUrl));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return books;
    }
    
    
    private static void loadImageAndDisplay(final String imageUrl, final JLabel label) {
        SwingUtilities.invokeLater(() -> {
            try {
                URL url = new URL(imageUrl);
                BufferedImage image = ImageIO.read(url);
                ImageIcon imageIcon = new ImageIcon(image);
                label.setIcon(imageIcon);
                label.setText(""); // 移除文本，只显示图片
            } catch (IOException e) {
                e.printStackTrace();
                label.setText("Image load failed");
            }
        });
    }

    
    private static void displayBooks(String shelfType, JPanel bookshelfDisplay) {
        List<Book> books = getBooks(shelfType);
        bookshelfDisplay.removeAll(); // 清除旧的内容

        for (Book book : books) {
            JPanel bookPanel = new JPanel();
            bookPanel.setLayout(new BorderLayout());

            JLabel titleLabel = new JLabel(book.getTitle(), SwingConstants.CENTER);
            JLabel imageLabel = new JLabel("Loading image...");
            
            // 异步加载图片
            loadImageAndDisplay(book.getImageUrl(), imageLabel);

            bookPanel.add(titleLabel, BorderLayout.NORTH);
            bookPanel.add(imageLabel, BorderLayout.CENTER);

            bookshelfDisplay.add(bookPanel);
        }

        bookshelfDisplay.revalidate();
        bookshelfDisplay.repaint();
    }



    private static void initUI(JFrame frame) {
        // 创建菜单栏
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Options");
        JMenuItem loginItem = new JMenuItem("Login");

        // 添加登录选项的事件处理（暂时保留空白）
        loginItem.addActionListener(e -> {
            // 登录事件处理
        });

        menu.add(loginItem);
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);

        // ...（后续添加侧边栏和书架展示区域）

        // 创建侧边栏
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        JButton myBookshelfButton = new JButton("我的书架");
        JButton libraryShelfButton = new JButton("图书馆书架");

        // 书架按钮事件处理
        myBookshelfButton.addActionListener(e -> displayBooks("myBookshelf"));
        libraryShelfButton.addActionListener(e -> displayBooks("libraryShelf"));

        sidebar.add(myBookshelfButton);
        sidebar.add(libraryShelfButton);

        // 创建书架展示区域
        JPanel bookshelfDisplay = new JPanel();
        bookshelfDisplay.setLayout(new GridLayout(0, 3)); // 根据需要调整布局

        // 将侧边栏和书架展示区域添加到窗口
        frame.add(sidebar, BorderLayout.WEST);
        frame.add(bookshelfDisplay, BorderLayout.CENTER);
    }

    private static void displayBooks(String shelfType) {
        // 根据书架类型显示书籍（暂时为空，后续实现）
    }
 
}

