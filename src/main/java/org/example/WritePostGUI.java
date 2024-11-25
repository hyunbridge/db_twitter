package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.*;

public class WritePostGUI {
    private JFrame frame;
    private JTextArea postContentArea;
    private JComboBox<String> visibilityComboBox;
    private JButton submitButton;
    private JButton imageButton;
    private String userId;
    private String uploadedImagePath = null; // 업로드된 이미지 경로

    public WritePostGUI(String userId) {
        this.userId = userId;
        initialize();
    }

    private void initialize() {
        // Frame 설정
        frame = new JFrame("Write a Post");
        frame.setSize(500, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Post Content Area
        JLabel contentLabel = new JLabel("Post Content:");
        postContentArea = new JTextArea(5, 30);
        JScrollPane contentScrollPane = new JScrollPane(postContentArea);

        // Visibility ComboBox
        JLabel visibilityLabel = new JLabel("Visibility:");
        String[] visibilityOptions = {"Everyone", "Accounts you follow"};
        visibilityComboBox = new JComboBox<>(visibilityOptions);

        // Image Upload Button
        imageButton = new JButton("Files");
        imageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadImage();
            }
        });

        // Submit Button
        submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitPost();
            }
        });

        // Layout 구성
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(contentLabel, BorderLayout.NORTH);
        topPanel.add(contentScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(visibilityLabel);
        bottomPanel.add(visibilityComboBox);
        bottomPanel.add(imageButton); // 이미지 버튼 추가
        bottomPanel.add(submitButton);

        frame.add(topPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void uploadImage() {
        // 파일 선택 다이얼로그 생성
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an Image");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            uploadedImagePath = selectedFile.getAbsolutePath(); // 이미지 경로 저장
            JOptionPane.showMessageDialog(frame, "Image selected: " + uploadedImagePath);
        } else {
            JOptionPane.showMessageDialog(frame, "No image selected.");
        }
    }

    private void submitPost() {
        String content = postContentArea.getText();
        String selectedVisibility = (String) visibilityComboBox.getSelectedItem();
        boolean isPublic = selectedVisibility.equals("Everyone");

        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Post content cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/twitter", "root", "pw")) {
            // Post ID 생성
            String idQuery = "SELECT COUNT(*) FROM posts";
            Statement idStmt = con.createStatement();
            ResultSet rs = idStmt.executeQuery(idQuery);

            String postId = "p1";
            if (rs.next()) {
                postId = "p" + (rs.getInt(1) + 1);
            }

            // 현재 시간 생성
            java.sql.Timestamp currentTime = new java.sql.Timestamp(System.currentTimeMillis());

            // Post 삽입
            String sql = "INSERT INTO posts (post_id, content, writer_id, num_of_likes, createAtTime, modeifiedAtTime, isPublic, imagePath) VALUES (?, ?, ?, 0, ?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, postId);
            pstmt.setString(2, content);
            pstmt.setString(3, userId);
            pstmt.setTimestamp(4, currentTime);
            pstmt.setTimestamp(5, currentTime);
            pstmt.setBoolean(6, isPublic);
            pstmt.setString(7, uploadedImagePath); // 이미지 경로 저장
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Post created successfully!");
            frame.dispose();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Failed to create post!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WritePostGUI("testUserId"));
    }
}
