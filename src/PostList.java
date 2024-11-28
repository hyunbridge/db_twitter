import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.List;

public class PostList extends JPanel {
    private TwitterUI parent;

    public PostList(TwitterUI parent, List<Post> posts) {
        this.parent = parent;
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (Post post : posts) {
            JPanel postPanel = createPostPanel(post);
            postPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            postPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    parent.showDetailPanel(post); // 부모 클래스에서 상세 화면 표시
                }
            });

            panel.add(postPanel);

            // 구분선 추가
            JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1)); // 전체 너비로 설정
            panel.add(separator);

            panel.add(Box.createRigidArea(new Dimension(0, 10))); // 간격 추가
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // 스크롤 속도 조정
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(20); // 단위 증가
        verticalScrollBar.setBlockIncrement(50); // 블록 증가
        add(scrollPane, BorderLayout.CENTER);
    }

    // 개별 글을 나타내는 패널 생성
    private JPanel createPostPanel(Post post) {
        JPanel postPanel = new JPanel();
        postPanel.setLayout(new BoxLayout(postPanel, BoxLayout.Y_AXIS));

        // 유저 이름 표시
        JLabel usernameLabel = new JLabel(post.getUsername());
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 글 내용 표시
        JLabel contentLabel = new JLabel("<html><p style='width:400px;'>" + post.getContent() + "</p></html>");
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 좋아요 수, 조회 수 등 정보 표시
        JLabel statsLabel = new JLabel("Likes: " + post.getLikedCnt() + "    Views: not yet..");
        statsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        postPanel.add(usernameLabel);
        postPanel.add(contentLabel);
        postPanel.add(statsLabel);
        return postPanel;
    }
}
