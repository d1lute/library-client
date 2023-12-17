
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.stream.Collectors;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.service.MediatypeService;

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
    private JPanel menuPanel; // 新增：菜单面板
    private JSplitPane splitPane; // 新增：分割面板
    private List<JPanel> bookPanels;
    private int currentPage = 0; // 当前页码
    private JLabel pageLabel; // 显示页码的标签
    private JTextField pageTextField; // 输入页码的文本框


    public UserFrame() {
        createUI();
    }

    private void createUI() {
        setTitle("电子书阅读器");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 菜单面板
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        JButton addBookButton = new JButton("添加书籍");
        addBookButton.addActionListener(e -> actionPerformed()); 
        menuPanel.add(addBookButton);

        // 书架面板
        bookShelfPanel = new JPanel();
        bookShelfPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();



        JScrollPane scrollPane = new JScrollPane(bookShelfPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // 分页控制面板
        JPanel pageControlPanel = new JPanel();
        JButton prevPageButton = new JButton("上一页");
        JButton nextPageButton = new JButton("下一页");
        JButton gotoPageButton = new JButton("跳转");
        pageLabel = new JLabel("页码: 1");
        pageTextField = new JTextField(5);

        prevPageButton.addActionListener(e -> prevPage());
        nextPageButton.addActionListener(e -> nextPage());
        gotoPageButton.addActionListener(e -> gotoPage());

        pageControlPanel.add(prevPageButton);
        pageControlPanel.add(pageLabel);
        pageControlPanel.add(pageTextField);
        pageControlPanel.add(gotoPageButton);
        pageControlPanel.add(nextPageButton);

        // 将组件添加到主窗体
        add(menuPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
        add(pageControlPanel, BorderLayout.SOUTH);

        bookPanels = new ArrayList<>();
    }

   

    private void prevPage() {
        if (currentPage > 0) {
            currentPage--;
            updateBookShelf();
        }
    }

    private void nextPage() {
        if ((currentPage + 1) * 20 < bookPanels.size()) {
            currentPage++;
            updateBookShelf();
        }
    }

    private void gotoPage() {
        try {
            int page = Integer.parseInt(pageTextField.getText()) - 1;
            if (page >= 0 && page * 20 < bookPanels.size()) {
                currentPage = page;
                updateBookShelf();
            } else {
                JOptionPane.showMessageDialog(this, "输入的页码无效！");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的页码数字！");
        }
    }

    private void updateBookShelf() {
        bookShelfPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // 设置组件间的间距

        int start = currentPage * 20;
        int end = Math.min(start + 20, bookPanels.size());
        int count = 0;

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                gbc.gridx = col;
                gbc.gridy = row;

                if (start + count < end) {
                    bookShelfPanel.add(bookPanels.get(start + count), gbc);
                } else {
                    bookShelfPanel.add(new JPanel(), gbc); // 添加空白面板
                }
                count++;
            }
        }

        bookShelfPanel.revalidate();
        bookShelfPanel.repaint();
        pageLabel.setText("页码: " + (currentPage + 1));
    }




    
    // TODO: 实现文件选择和书籍展示的方法
    private void actionPerformed() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择电子书文件");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("EPUB Files", "epub");
        fileChooser.setFileFilter(filter);
        fileChooser.setMultiSelectionEnabled(true); // 允许选择多个文件

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles(); // 获取所有选择的文件
            for (File file : selectedFiles) {
                JPanel bookPanel = createBookPanel(file); // 创建一个书籍面板
                bookPanels.add(bookPanel); // 添加到书籍列表中
            }
            updateBookShelf(); // 更新书架显示
        }
    }
    
    private JPanel createBookPanel(File bookFile) {
        JPanel bookPanel = new JPanel(new BorderLayout());
        bookPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Dimension bookPanelSize = new Dimension(120, 180);
        bookPanel.setPreferredSize(bookPanelSize);
        bookPanel.setMaximumSize(bookPanelSize);
        bookPanel.setMinimumSize(bookPanelSize);

        try (InputStream epubInputStream = new FileInputStream(bookFile)) {
            Book book = (new EpubReader()).readEpub(epubInputStream);

            // 获取书名
            String title = book.getTitle();
            JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
            bookPanel.add(titleLabel, BorderLayout.SOUTH);

            // 获取封面图片
            Resource coverImage = book.getCoverImage();
            if (coverImage != null) {
                ImageIcon coverIcon = new ImageIcon(coverImage.getData());
                JLabel coverLabel = new JLabel(new ImageIcon(coverIcon.getImage().getScaledInstance(100, 150, java.awt.Image.SCALE_SMOOTH)));
                bookPanel.add(coverLabel, BorderLayout.CENTER);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // 处理异常，比如显示一个错误信息或默认封面
            JLabel errorLabel = new JLabel("无法加载书籍", SwingConstants.CENTER);
            bookPanel.add(errorLabel, BorderLayout.CENTER);
        }
        
     // 添加鼠标事件监听器
        bookPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 实现点击事件，比如打开新窗口阅读书籍
                openBookReader(bookFile);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // 实现鼠标进入时的效果，比如改变边框
                bookPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // 实现鼠标离开时的效果，比如恢复边框
                bookPanel.setBorder(BorderFactory.createEmptyBorder());
            }
            // 可以根据需要添加其他鼠标事件
        });

        return bookPanel;
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
            Dimension bookPanelSize = new Dimension(120, 180);
            bookPanel.setPreferredSize(bookPanelSize);
            bookPanel.setMaximumSize(bookPanelSize);
            bookPanel.setMinimumSize(bookPanelSize);
            bookPanel.addMouseListener(new MouseAdapter() {
            	@Override
                public void mouseEntered(MouseEvent e) {
                    // 鼠标悬停时，添加黑色边框
                    bookPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    // 鼠标离开时，移除边框
                    bookPanel.setBorder(BorderFactory.createEmptyBorder());
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    // 鼠标按下时，创建凹陷效果
                    bookPanel.setBorder(BorderFactory.createLoweredBevelBorder());
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    // 鼠标释放时，恢复原始边框
                    bookPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    
                    // 打开电子书阅读器界面
                    openBookReader(bookFile);
                }
            });


            // 将书籍面板添加到书架
            bookShelfPanel.add(bookPanel);
            bookShelfPanel.revalidate();
            bookShelfPanel.repaint();
        });
    }
    
    private String mergeEpubContent(Book book) throws IOException {
        StringBuilder mergedContent = new StringBuilder();
        
        // 遍历EPUB文件中的所有HTML资源
        for (Resource resource : book.getSpine().getSpineReferences()) {
            if (resource.getMediaType() == MediatypeService.XHTML) {
                String htmlContent = new String(resource.getData(), "UTF-8");
                // 处理资源路径...
                // 合并内容
                mergedContent.append(htmlContent);
            }
        }

        return mergedContent.toString();
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

