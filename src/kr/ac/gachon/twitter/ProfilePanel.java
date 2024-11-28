package kr.ac.gachon.twitter;

import javax.swing.*;
import java.awt.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ProfilePanel extends JPanel {
    private User profileUser; // 프로필의 주인
    private User currentUser; // 현재 로그인한 사용자

    public ProfilePanel(User profileUser) {
        this.profileUser = profileUser;
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 상단 프로필 정보 패널
        JPanel profileInfoPanel = createProfileInfoPanel();
        add(profileInfoPanel, BorderLayout.NORTH);

        // 버튼 패널 생성
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // 자신의 프로필이 아닐 경우 팔로우/언팔로우 버튼과 메시지 버튼 표시
        if (profileUser.getUid() != currentUser.getUid()) {
            // 팔로우/언팔로우 버튼
            addFollowButtonToPanel(buttonPanel);
            
            // 메시지 보내기 버튼
            JButton messageButton = new JButton("Send Message");
            messageButton.addActionListener(e -> showMessageDialog());
            buttonPanel.add(messageButton);
        } else {
            // 프로필 수정 버튼 (자신의 프로필일 경우에만 표시)
            JButton editButton = new JButton("Edit Profile");
            editButton.addActionListener(e -> showEditProfileDialog());
            buttonPanel.add(editButton);
        }

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFollowButtonToPanel(JPanel buttonPanel) {
        DatabaseServer db = new DatabaseServer();
        boolean isFollowing = db.isFollowing(currentUser.getUid(), profileUser.getUid());
        
        JButton followButton = new JButton(isFollowing ? "Unfollow" : "Follow");
        followButton.addActionListener(e -> {
            DatabaseServer dbServer = new DatabaseServer();
            boolean success;
            
            if (isFollowing) {
                success = dbServer.unfollowUser(currentUser.getUid(), profileUser.getUid());
                if (success) {
                    followButton.setText("Follow");
                    JOptionPane.showMessageDialog(this, "Successfully unfollowed user!");
                    updateFollowerCount();
                }
            } else {
                success = dbServer.followUser(currentUser.getUid(), profileUser.getUid());
                if (success) {
                    followButton.setText("Unfollow");
                    JOptionPane.showMessageDialog(this, "Successfully followed user!");
                    updateFollowerCount();
                }
            }
        });
        
        // 버튼 스타일 설정
        if (isFollowing) {
            followButton.setBackground(new Color(220, 220, 220));
            followButton.setForeground(Color.BLACK);
        } else {
            followButton.setBackground(new Color(29, 161, 242));
            followButton.setForeground(Color.WHITE);
        }
        
        buttonPanel.add(followButton);
    }

    private void showEditProfileDialog() {
        // 프로필 수정 다이얼로그 구현
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Edit Profile", true);
        // ... 프로필 수정 UI 구현
    }

    private void showMessageDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "New Message", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 받는 사람 표시 (수정 불가)
        JLabel receiverLabel = new JLabel("To: " + profileUser.getUsername());
        receiverLabel.setFont(new Font("Arial", Font.BOLD, 14));
        inputPanel.add(receiverLabel, BorderLayout.NORTH);

        // 메시지 입력
        JTextArea messageArea = new JTextArea(10, 30);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        inputPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        // 전송 버튼
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> {
            String message = messageArea.getText();
            if (!message.isEmpty()) {
                DatabaseServer db = new DatabaseServer();
                if (db.sendMessage(currentUser.getUid(), profileUser.getUid(), message)) {
                    JOptionPane.showMessageDialog(dialog, "Message sent successfully!");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to send message", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(sendButton);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(inputPanel);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * 프로필 이미지를 동그라미로 표시하는 JLabel 생성
     *
     * @param imagePath 이미지 경로
     * @param size      이미지 크기
     * @return JLabel
     */
    private JLabel createProfileImageLabel(String imagePath, int size) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            // 동그라미 모양으로 자르기
            BufferedImage circleImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = circleImage.createGraphics();
            g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
            g2.drawImage(image, 0, 0, size, size, null);
            g2.dispose();

            ImageIcon icon = new ImageIcon(circleImage);
            JLabel label = new JLabel(icon);
            return label;
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 이미지 로드 실패 시 기본 텍스트 반환
        return new JLabel("No Image");
    }

    private JPanel createProfileInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 프로필 이미지
        String profileImagePath = profileUser.getProfileImage() != null ? 
            profileUser.getProfileImage() : "images/profile_default.jpg";
        JLabel profileImageLabel = createProfileImageLabel(profileImagePath, 100);
        panel.add(profileImageLabel, BorderLayout.WEST);

        // 사용자 정보 패널
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        JLabel usernameLabel = new JLabel(profileUser.getUsername());
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        JLabel bioLabel = new JLabel(profileUser.getBio() != null ? 
            profileUser.getBio() : "No bio available");
        bioLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JLabel followersLabel = new JLabel(
            String.format("Followers: %d | Following: %d", 
            profileUser.getFollowerCnt(), 
            profileUser.getFollowingCnt())
        );
        
        infoPanel.add(usernameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(bioLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(followersLabel);

        panel.add(infoPanel, BorderLayout.CENTER);
        return panel;
    }

    private void updateFollowerCount() {
        DatabaseServer db = new DatabaseServer();
        User updatedUser = db.getUserById(String.valueOf(profileUser.getUid()));
        if (updatedUser != null) {
            this.profileUser = updatedUser;
            removeAll();
            initialize();
            revalidate();
            repaint();
        }
    }
}
