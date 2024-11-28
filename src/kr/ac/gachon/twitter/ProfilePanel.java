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
    private User user;
    public ProfilePanel(User loggedInUser) {
        this.user = loggedInUser;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 상단 프로필 정보 패널
        JPanel profileInfoPanel = new JPanel();
        profileInfoPanel.setLayout(new BorderLayout());
        profileInfoPanel.setBackground(Color.WHITE);
        profileInfoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 프로필 이미지
        String profileImagePath = user.getProfileImage() != null ? user.getProfileImage() : "images/profile_default.jpg";
        ImageIcon profileImageIcon = new ImageIcon(profileImagePath);
        JLabel profileImageLabel = new JLabel(profileImageIcon);
        profileInfoPanel.add(profileImageLabel, BorderLayout.WEST);

        // 사용자 이름 및 Bio 패널
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        // 사용자 이름
        JLabel usernameLabel = new JLabel(user.getUsername());
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(usernameLabel);

        // 사용자 Bio
        JLabel bioLabel = new JLabel(user.getBio() != null ? user.getBio() : "No bio available");
        bioLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        bioLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(Box.createRigidArea(new Dimension(0, 10))); // 간격
        textPanel.add(bioLabel);

        profileInfoPanel.add(textPanel, BorderLayout.CENTER);

        // 프로필 정보를 메인 패널에 추가
        add(profileInfoPanel, BorderLayout.NORTH);
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
}
