package kr.ac.gachon.twitter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Timestamp;

public class WritePostGUI extends JPanel{
    private JTextArea postContentArea;
    private JComboBox<String> visibilityComboBox; // 수정된 부분
    private JButton submitButton;
    private JButton imageButton;
    private String uploadedImagePath = null; // 업로드된 이미지 경로
    private long userId; // 추가: 사용자 ID 저장

    public WritePostGUI(long userId) { // 생성자 수정
        this.userId = userId; // 사용자 ID 저장
        setLayout(new BorderLayout());

        // Post Content Area
        JLabel contentLabel = new JLabel("Post Content:");
        postContentArea = new JTextArea(5, 30);
        JScrollPane contentScrollPane = new JScrollPane(postContentArea);

        // Visibility ComboBox --> 메뉴 항목 추가
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

        add(topPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void uploadImage() {
        // 파일 선택 다이얼로그 생성
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an Image");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            uploadedImagePath = selectedFile.getAbsolutePath(); // 이미지 경로 저장
            JOptionPane.showMessageDialog(this, "Image selected: " + uploadedImagePath);
        } else {
            JOptionPane.showMessageDialog(this, "No image selected.");
        }
    }

    private void submitPost() {
        String content = postContentArea.getText();
        String selectedVisibility = (String) visibilityComboBox.getSelectedItem();
        boolean isPublic = selectedVisibility.equals("Everyone");

        // userId 사용
        long createdBy = this.userId; // 저장된 userId 사용
        int likedCnt = 0; // 초기값은 0으로 설정
        String imagePath = uploadedImagePath;
        java.sql.Timestamp currentTime = new java.sql.Timestamp(System.currentTimeMillis());

        Post post = new Post(createdBy, content, likedCnt, currentTime, imagePath, isPublic);

        DatabaseServer server = new DatabaseServer();
        boolean success = server.insertPost(post);
        
        if (success) {
            JOptionPane.showMessageDialog(this, "Post created successfully!");
            // 피드 화면으로 돌아가기
            CardLayout cardLayout = (CardLayout) getParent().getLayout();
            cardLayout.show(getParent(), "Feed");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create post", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}