package org.example;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WritePostGUI {
    private JFrame frame;
    private JTextArea postContentArea;
    private JComboBox<String> visibilityComboBox; // 수정된 부분
    private JButton submitButton;
    private String userId;

    public WritePostGUI(String userId) {
        this.userId = userId;
        initialize();
    }

    private void initialize() {
        // Frame 설정
        frame = new JFrame("Write a Post");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Post Content Area
        JLabel contentLabel = new JLabel("Post Content:");
        postContentArea = new JTextArea(5, 30);
        JScrollPane contentScrollPane = new JScrollPane(postContentArea);

        // Visibility ComboBox --> 메뉴 항목 추가
        JLabel visibilityLabel = new JLabel("Visibility:");
        String[] visibilityOptions = {"Everyone", "Accounts you follow"};
        visibilityComboBox = new JComboBox<>(visibilityOptions);

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
        bottomPanel.add(visibilityLabel); // 추가된 부분
        bottomPanel.add(visibilityComboBox); // 추가된 부분
        bottomPanel.add(submitButton);

        frame.add(topPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void submitPost() {
        String content = postContentArea.getText();
        String selectedVisibility = (String) visibilityComboBox.getSelectedItem(); // 선택한 옵션 가져오기
        boolean isPublic = selectedVisibility.equals("Everyone"); // 공개 여부 결정

        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Post content cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/twitter", "root", "pw")) {
            // Post ID 생성
            String idQuery = "SELECT COUNT(*) FROM posts";
            Statement idStmt = con.createStatement();
            ResultSet rs = idStmt.executeQuery(idQuery);

            // 인덱스 형식은 쿼리 짜시는 분이....
            String postId = "p1";
            if (rs.next()) {
                postId = "p" + (rs.getInt(1) + 1);
            }

            // 현재 시간 생성
            java.sql.Timestamp currentTime = new java.sql.Timestamp(System.currentTimeMillis());

            // Post 삽입
            String sql = "INSERT INTO posts (post_id, content, writer_id, num_of_likes, createAtTime, modeifiedAtTime, isPublic) VALUES (?, ?, ?, 0, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, postId);
            pstmt.setString(2, content);
            pstmt.setString(3, userId);
            pstmt.setTimestamp(4, currentTime);
            pstmt.setTimestamp(5, currentTime);
            pstmt.setBoolean(6, isPublic); // 공개 여부 저장
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Post created successfully!");
            frame.dispose(); // 창 닫기
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Failed to create post!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // 테스트용 유저 ID
        SwingUtilities.invokeLater(() -> new WritePostGUI("testUserId"));
    }
}
