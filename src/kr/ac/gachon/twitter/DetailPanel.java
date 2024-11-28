package kr.ac.gachon.twitter;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.sql.Timestamp;

public class DetailPanel extends JPanel {
    public DetailPanel(Post post, List<Comment> comments) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 유저 이름
        DatabaseServer db = new DatabaseServer();
        String username = db.getUsernameById(post.getCreatedBy());
        JLabel usernameLabel = new JLabel("Posted by: " + username);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(usernameLabel);

        // 글 내용
        JLabel contentLabel = new JLabel("<html><p style='width:450px;'>" + post.getContent() + "</p></html>");
        contentLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        add(contentLabel);

        // 좋아요 수, 조회 수 등 정보
        JLabel statsLabel = new JLabel("Likes: " + post.getLikedCnt() + " | Views: not yet..");
        statsLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        add(statsLabel);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1)); // 전체 너비로 설정
        add(Box.createRigidArea(new Dimension(0, 10))); // 구분선 위에 간격 추가
        add(separator); // 구분선 추가
        add(Box.createRigidArea(new Dimension(0, 10))); // 구분선 아래에 간격 추가

        // 댓글을 순차적으로 표시
        for (Comment comment : comments) {
            JPanel commentPanel = new JPanel();
            commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));

            // 댓글 작성자
            String commentUsername = db.getUsernameById(comment.getCreatedBy());
            JLabel commentUserLabel = new JLabel("Comment by: " + commentUsername);
            commentUserLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            commentPanel.add(commentUserLabel);

            // 댓글 내용
            JLabel commentContentLabel = new JLabel("<html><p style='width:450px;'>" + comment.getContent() + "</p></html>");
            commentContentLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            commentPanel.add(commentContentLabel);

            // 댓글 좋아요 수
            JLabel commentStatsLabel = new JLabel("Likes: " + comment.getLikedCnt());
            commentStatsLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            commentPanel.add(commentStatsLabel);

            // 댓글 패널 추가
            add(commentPanel);

            // 댓글 간 구분선
            add(new JSeparator(SwingConstants.HORIZONTAL));
        }

        // 돌아가기 버튼
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            CardLayout cardLayout = (CardLayout) getParent().getLayout();
            cardLayout.show(getParent(), "Feed"); // 메인 피드로 돌아가기
        });
        add(Box.createRigidArea(new Dimension(0, 20))); // 여백 추가
        add(backButton);
    }
}
