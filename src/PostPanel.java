import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PostPanel extends JPanel {
    private Post post;
    public PostPanel(Post post) {
        this.post = post;
        initialize();
    }
    private void initialize() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 유저 이름 표시
        JLabel usernameLabel = new JLabel(post.getUsername());
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 글 내용 표시 (HTML 태그를 사용하여 줄바꿈)
        String content = post.getContent();
        int maxLineLength = 50; // 한 줄에 최대 표시할 글자 수
        String formattedContent = formatContent(content, maxLineLength);
        JLabel contentLabel = new JLabel("<html><div style='width:400px;'>" + formattedContent + "</div></html>");
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // 패널에 구성 요소 추가
        add(usernameLabel);
        add(contentLabel);

        // 글과 이미지 사이 간격 추가
        add(Box.createRigidArea(new Dimension(0, 10))); // 글과 이미지 사이 간격 추가
        // 이미지가 존재하면 표시
        if (post.getImagePath() != null) {
            JLabel imageLabel = createImageLabel(post.getImagePath());
            imageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(Box.createRigidArea(new Dimension(0, 10))); // 글과 이미지 사이 간격
            add(imageLabel); // 이미지 추가
        }
        // UI 간격 추가
        add(Box.createRigidArea(new Dimension(0, 10))); // 이미지와 다른 UI 요소 사이 간격 추가
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
}
