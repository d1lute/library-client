
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

import java.awt.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Base64;
import java.util.stream.Collectors;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import nl.siegmann.epublib.domain.SpineReference;
import java.util.List;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import javafx.concurrent.Worker;


public class UserFrame extends JFrame {

    private JPanel bookShelfPanel;
    private CardLayout cardLayout;
    private JFrame bookReaderFrame; // 新增：用于阅读书籍的窗口
    private JFXPanel bookReaderPanel; // 新增：用于显示书籍内容的JavaFX面板

    public UserFrame() {
    	createUI();
    }
    private void createUI() {
        setTitle("电子书阅读器");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        // 书架面板
        bookShelfPanel = new JPanel();
        bookShelfPanel.setLayout(new FlowLayout());

        // 添加书籍按钮
        JButton addBookButton = new JButton("选择文件");
        addBookButton.addActionListener(e -> actionPerformed());
        bookShelfPanel.add(addBookButton);

        add("BookShelf", bookShelfPanel);

        // TODO: 添加其他面板，如电子书阅读面板
    }

    // TODO: 实现文件选择和书籍展示的方法
    private void actionPerformed() {
    	JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择电子书文件");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("EPUB Files", "epub");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            // TODO: 处理选中的文件，例如显示书籍信息
            System.out.println("选中的文件: " + selectedFile.getAbsolutePath());
            extractBookInfo( selectedFile);
        }
    }
    
    private void extractBookInfo(File epubFile) {
        try (InputStream epubInputStream = new FileInputStream(epubFile)) {
            Book book = (new EpubReader()).readEpub(epubInputStream);

            // 获取书名
            String title = book.getTitle();

            // 获取封面图片
            Resource coverImage = book.getCoverImage();
            ImageIcon coverIcon = null;
            if (coverImage != null) {
                coverIcon = new ImageIcon(coverImage.getData());
            }

            // 在UI中显示书籍
            displayBookInShelf(title, coverIcon, epubFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayBookInShelf(String title, ImageIcon coverIcon, File bookFile) {
        SwingUtilities.invokeLater(() -> {
            // 创建包含封面和标题的面板
            JPanel bookPanel = new JPanel(new BorderLayout());
            bookPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // 如果有封面，设置封面图像
            if (coverIcon != null) {
                JLabel coverLabel = new JLabel(new ImageIcon(coverIcon.getImage().getScaledInstance(100, 150, java.awt.Image.SCALE_SMOOTH)));
                bookPanel.add(coverLabel, BorderLayout.CENTER);
            }

            // 设置书名标签
            JLabel titleLabel = new JLabel(title);
            titleLabel.setHorizontalAlignment(JLabel.CENTER);
            bookPanel.add(titleLabel, BorderLayout.SOUTH);

            // 为书籍面板添加点击事件
            bookPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // TODO: 打开电子书阅读器界面
                    openBookReader(bookFile);
                }
            });

            // 将书籍面板添加到书架
            bookShelfPanel.add(bookPanel);
            bookShelfPanel.revalidate();
            bookShelfPanel.repaint();
        });
    }
    
    private void openBookReader(File bookFile) {
        Platform.runLater(() -> {
            try {
            	if (bookReaderFrame == null) {
                    // 如果窗口尚未创建，则创建新的窗口和面板
                    bookReaderFrame = new JFrame("电子书阅读器");
                    bookReaderPanel = new JFXPanel();
                    bookReaderFrame.add(bookReaderPanel);
                    bookReaderFrame.setSize(800, 600);
                    bookReaderFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); // 改为隐藏而不是销毁
                }
            	
               
                WebView webView = new WebView();
                
                webView.getEngine().getLoadWorker().stateProperty().addListener(
                	    (obs, oldState, newState) -> {
                	        if (newState == Worker.State.SUCCEEDED) {
                	            // 处理新页面加载完成后的情况
                	        }
                	    }
                	);

                	webView.getEngine().locationProperty().addListener(
                	    (obs, oldLocation, newLocation) -> {
                	        System.out.println("Navigated to: " + newLocation);
                	        // 处理URL变化
                	    }
                	);

                	
                	
                	
                // 读取 EPUB 文件
                Book book = (new EpubReader()).readEpub(new FileInputStream(bookFile));

                // 获取第一个内容文件（如封面或第一章）
                Resource firstResource = book.getSpine().getResource(1);
                String content = new String(firstResource.getData(), "UTF-8");

                // 如果内容引用了封面图像，将其转换为 data URI 并嵌入
                Resource coverImage = book.getCoverImage();
                if (coverImage != null) {
                    String imageBase64 = Base64.getEncoder().encodeToString(coverImage.getData());
                    String imageMimeType = coverImage.getMediaType().getName();
                    String imageDataURI = "data:" + imageMimeType + ";base64," + imageBase64;
                    content = content.replace(coverImage.getHref(), imageDataURI);
                }

                // 加载内容到 WebView
                webView.getEngine().loadContent(content, "text/html");

                // 显示新窗口
                Scene scene = new Scene(webView);
                bookReaderPanel.setScene(scene);
                bookReaderFrame.setVisible(true); // 显示或重新显示窗口
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }





    
    
    public static void main(String[] args) {
        // 先启动 JavaFX 应用程序，以初始化 JavaFX
    	 Platform.startup(() -> {});

        // 然后启动 Swing 应用程序
        SwingUtilities.invokeLater(() -> {
            new UserFrame().setVisible(true);
        });
    }
}

