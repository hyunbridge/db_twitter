package kr.ac.gachon.twitter;

import javax.swing.*;
import java.awt.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

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
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Edit Profile", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);

        // 입력 패널
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Bio 입력
        JLabel bioLabel = new JLabel("Bio:");
        JTextArea bioArea = new JTextArea(currentUser.getBio());
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        JScrollPane bioScrollPane = new JScrollPane(bioArea);
        bioScrollPane.setPreferredSize(new Dimension(300, 100));

        // 새 비밀번호 입력
        JLabel passwordLabel = new JLabel("New Password (leave blank to keep current):");
        JPasswordField passwordField = new JPasswordField();

        // 현재 비밀번호 입력 (확인용)
        JLabel currentPasswordLabel = new JLabel("Current Password (required):");
        JPasswordField currentPasswordField = new JPasswordField();

        // 저장 버튼
        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> {
            String currentPasswordInput = new String(currentPasswordField.getPassword());
            
            // 현재 비밀번호 확인
            if (!currentPasswordInput.equals(currentUser.getPassword())) {
                JOptionPane.showMessageDialog(dialog,
                    "Current password is incorrect",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 새 비밀번호 처리
            String newPassword = new String(passwordField.getPassword());
            String finalPassword = newPassword.isEmpty() ? currentUser.getPassword() : newPassword;
            
            // 프로필 업데이트
            DatabaseServer db = new DatabaseServer();
            boolean success = db.updateUserProfile(currentUser.getUid(), bioArea.getText(), finalPassword);
            
            if (success) {
                JOptionPane.showMessageDialog(dialog,
                    "Profile updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // 세션 업데이트
                User updatedUser = db.getUserById(String.valueOf(currentUser.getUid()));
                if (updatedUser != null) {
                    SessionManager.getInstance().setCurrentUser(updatedUser);
                    this.currentUser = updatedUser;
                    // UI 새로고침
                    removeAll();
                    initialize();
                    revalidate();
                    repaint();
                }
                
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Failed to update profile",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // 컴포넌트 추가
        inputPanel.add(bioLabel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        inputPanel.add(bioScrollPane);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        inputPanel.add(passwordLabel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        inputPanel.add(passwordField);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        inputPanel.add(currentPasswordLabel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        inputPanel.add(currentPasswordField);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        inputPanel.add(saveButton);

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        JLabel usernameLabel = new JLabel(profileUser.getUsername());
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel bioLabel = new JLabel(profileUser.getBio() != null ? 
            profileUser.getBio() : "No bio available");
        bioLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        bioLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // 팔로워/팔로잉 패널
        JPanel followPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        followPanel.setBackground(Color.WHITE);
        followPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 팔로워 라벨
        JLabel followersLabel = new JLabel(String.format("<html><u>Followers: %d</u></html>", profileUser.getFollowerCnt()));
        followersLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        followersLabel.setForeground(Color.BLACK);
        followersLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showFollowList(true);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                followersLabel.setForeground(new Color(100, 100, 100));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                followersLabel.setForeground(Color.BLACK);
            }
        });

        // 구분자
        JLabel separator = new JLabel(" | ");

        // 팔로잉 라벨
        JLabel followingLabel = new JLabel(String.format("<html><u>Following: %d</u></html>", profileUser.getFollowingCnt()));
        followingLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        followingLabel.setForeground(Color.BLACK);
        followingLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showFollowList(false);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                followingLabel.setForeground(new Color(100, 100, 100));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                followingLabel.setForeground(Color.BLACK);
            }
        });

        followPanel.add(followersLabel);
        followPanel.add(separator);
        followPanel.add(followingLabel);

        infoPanel.add(usernameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(bioLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(followPanel);

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

    private void showFollowList(boolean isFollowers) {
        DatabaseServer db = new DatabaseServer();
        List<User> users = isFollowers ? 
            db.getFollowers(profileUser.getUid()) : 
            db.getFollowing(profileUser.getUid());
        
        String title = isFollowers ? "Followers" : "Following";
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setLayout(new BorderLayout());
        
        // 유저 목록 패널
        JPanel usersPanel = new JPanel();
        usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));
        usersPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        for (User user : users) {
            JPanel userPanel = new JPanel();
            userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
            userPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            userPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // 유저 이름을 클릭 가능한 링크처럼 만들기
            JLabel nameLabel = new JLabel("<html><u>" + user.getUsername() + "</u></html>");
            nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
            nameLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            nameLabel.setForeground(Color.BLACK);
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // 클릭 이벤트 추가
            nameLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    dialog.dispose();
                    ProfilePanel profilePanel = new ProfilePanel(user);
                    JPanel mainPanel = (JPanel) getParent();
                    mainPanel.add(profilePanel, "Profile");
                    CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
                    cardLayout.show(mainPanel, "Profile");
                }
                
                public void mouseEntered(MouseEvent evt) {
                    nameLabel.setForeground(new Color(100, 100, 100));
                }
                
                public void mouseExited(MouseEvent evt) {
                    nameLabel.setForeground(Color.BLACK);
                }
            });
            
            userPanel.add(nameLabel);
            
            // bio 표시
            if (user.getBio() != null && !user.getBio().isEmpty()) {
                JLabel bioLabel = new JLabel(user.getBio());
                bioLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                bioLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                userPanel.add(bioLabel);
            }
            
            usersPanel.add(userPanel);
            usersPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        JScrollPane scrollPane = new JScrollPane(usersPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.setSize(300, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
