package kr.ac.gachon.twitter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PostPanel extends JPanel {
    private Post post;
    private User loggedInUser;
    public PostPanel(Post post, User loggedInUser) {
        this.post = post;
        this.loggedInUser = loggedInUser;
        initialize();
    }
    private void initialize() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        // 유저 이름 표시
        JLabel usernameLabel = new JLabel(getUsernameFromId(post.getCreatedBy()));
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 글 내용 표시
        JLabel contentLabel = new JLabel();
        contentLabel.setText(post.getContent());
        contentLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(usernameLabel);
        add(contentLabel);

        // 이미지가 존재하면 표시
        if (post.getImagePath() != null) {
            JLabel imageLabel = createImageLabel(post.getImagePath());
            imageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(Box.createRigidArea(new Dimension(0, 10)));
            add(imageLabel);
        }

        // 좋아요 수 표시
        JLabel likeCountLabel = new JLabel("❤️ " + post.getLikedCnt());
        likeCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(likeCountLabel);

        add(Box.createRigidArea(new Dimension(0, 10)));
    }

    /**
     * 글 내용을 일정 길이마다 줄바꿈하도록 포맷팅
     * @param content 원본 텍스트
     * @param maxLineLength 한 줄의 최대 글자 수
     * @return 줄바꿈된 HTML 텍스트
     */
    private String formatContent(String content, int maxLineLength) {
        StringBuilder sb = new StringBuilder();
        int length = content.length();
        for (int i = 0; i < length; i += maxLineLength) {
            int endIndex = Math.min(i + maxLineLength, length);
            sb.append(content, i, endIndex).append("<br>"); // 줄바꿈 추가
        }
        return sb.toString();
    }
    /**
     * 이미지 경로를 기반으로 JLabel에 이미지 생성
     * @param imagePath 이미지 파일 경로
     * @return 이미지가 포함된 JLabel
     */
    private JLabel createImageLabel(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists() || !imageFile.isFile()) {
                return new JLabel("Image not found: " + imagePath);
            }
            BufferedImage originalImage = ImageIO.read(imageFile);
            if (originalImage == null) {
                return new JLabel("Invalid image format: " + imagePath);
            }
            int maxWidth = 400, maxHeight = 300;
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            double widthRatio = (double) maxWidth / originalWidth;
            double heightRatio = (double) maxHeight / originalHeight;
            double scale = Math.min(widthRatio, heightRatio);

            BufferedImage resizedImage = resizeImage(originalImage, (int) (originalWidth * scale), (int) (originalHeight * scale));
            return new JLabel(new ImageIcon(resizedImage));
        } catch (IOException e) {
            e.printStackTrace();
            return new JLabel("Failed to load image");
        }
    }
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();

        return outputImage;
    }

    public Post getPost() {
        return post;
    }

    private String getUsernameFromId(long userId) {
        DatabaseServer db = new DatabaseServer();
        return db.getUsernameById(userId);
    }

    private void addPostOptions() {
        if (post.getCreatedBy() == loggedInUser.getUid()) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem editItem = new JMenuItem("Edit");
            JMenuItem deleteItem = new JMenuItem("Delete");
            
            editItem.addActionListener(e -> editPost());
            deleteItem.addActionListener(e -> deletePost());
            
            menu.add(editItem);
            menu.add(deleteItem);
            
            addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        menu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });
        }
    }

    private void editPost() {
        String newContent = JOptionPane.showInputDialog(
            this,
            "Edit post:",
            post.getContent()
        );
        
        if (newContent != null && !newContent.trim().isEmpty()) {
            DatabaseServer db = new DatabaseServer();
            boolean success = db.updatePost(post.getPostId(), newContent);
            if (success) {
                JOptionPane.showMessageDialog(this, "Post updated successfully!");
                // UI 업데이트
                refreshPost();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update post", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deletePost() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this post?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            DatabaseServer db = new DatabaseServer();
            boolean success = db.deletePost(post.getPostId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Post deleted successfully!");
                // 부모 컨테이너 새로고침
                Container parent = getParent();
                if (parent instanceof JPanel) {
                    ((JPanel) parent).remove(this);
                    parent.revalidate();
                    parent.repaint();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete post", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshPost() {
        DatabaseServer db = new DatabaseServer();
        Post updatedPost = db.getPostById(post.getPostId());
        if (updatedPost != null) {
            this.post = updatedPost;
            removeAll();
            initialize();
            revalidate();
            repaint();
        }
    }
}
