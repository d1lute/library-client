
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.stream.Collectors;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

public class UserFrame extends JFrame {

    private JPanel bookShelfPanel;
    private CardLayout cardLayout;

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
        }
    }
    
    private void extractBookInfo(File epubFile) {
        try (InputStream epubInputStream = new FileInputStream(epubFile)) {
            Book book = (new EpubReader()).readEpub(epubInputStream);

            // 获取书名
            String title = book.getTitle();
            System.out.println("书名: " + title);

            // 获取作者
            String author = book.getMetadata().getAuthors().stream()
                              .map(author -> author.getFirstname() + " " + author.getLastname())
                              .collect(Collectors.joining(", "));
            System.out.println("作者: " + author);

            // 获取封面图片
            // 注意: 并非所有EPUB文件都有封面图片
            Resource coverImage = book.getCoverImage();
            if (coverImage != null) {
                // 处理封面图片，例如显示在界面上
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new UserFrame().setVisible(true);
            }
        });
    }
}

