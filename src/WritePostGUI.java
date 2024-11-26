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

    public WritePostGUI() {
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
        // 1. 사용자 입력값 읽기
        String content = postContentArea.getText();// 텍스트 영역에서 입력된 내용 읽기
        String selectedVisibility = (String) visibilityComboBox.getSelectedItem();
        boolean isPublic = selectedVisibility.equals("Everyone");

        // 2. Post 객체 생성
        long createdBy = 1;
        int likedCnt = 999;
        String imagePath = uploadedImagePath;
        // 현재 시간 생성
        java.sql.Timestamp currentTime = new java.sql.Timestamp(System.currentTimeMillis());

        Post post = new Post(createdBy, content, likedCnt, currentTime, imagePath, isPublic);

        // 3. 서버에 Post 객체 전달
        DatabaseServer server = new DatabaseServer();
        server.insertPost(post); // insertPost 메서드 수정 필요
    }
}