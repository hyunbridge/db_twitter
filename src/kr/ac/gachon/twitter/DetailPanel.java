package kr.ac.gachon.twitter;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.sql.Timestamp;

public class DetailPanel extends JPanel {
    private Post post;
    private List<Comment> comments;
    private User currentUser;

    public DetailPanel(Post post, List<Comment> comments) {
        this.post = post;
        this.comments = comments;
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        initialize();
    }

    private void initialize() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 유저 이름을 클릭 가능한 링크처럼 만들기
        DatabaseServer db = new DatabaseServer();
        String username = db.getUsernameById(post.getCreatedBy());
        JLabel usernameLabel = new JLabel("<html><u>" + username + "</u></html>");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        usernameLabel.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 마우스 오버시 손가락 커서
        usernameLabel.setForeground(new Color(29, 161, 242)); // 트위터 블루 색상
        
        // 클릭 이벤트 추가
        usernameLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // 해당 유저의 프로필로 이동
                User postUser = db.getUserById(String.valueOf(post.getCreatedBy()));
                if (postUser != null) {
                    Container parent = getParent();
                    while (parent != null && !(parent instanceof TwitterUI)) {
                        parent = parent.getParent();
                    }
                    if (parent instanceof TwitterUI) {
                        ProfilePanel profilePanel = new ProfilePanel(postUser);
                        JPanel mainPanel = (JPanel) getParent();
                        mainPanel.add(profilePanel, "Profile");
                        CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
                        cardLayout.show(mainPanel, "Profile");
                    }
                }
            }
            
            // 마우스 오버 효과
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                usernameLabel.setForeground(new Color(20, 120, 180)); // 더 진한 파란색
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                usernameLabel.setForeground(new Color(29, 161, 242)); // 원래 색상으로
            }
        });
        
        add(usernameLabel);

        // 글 내용
        JLabel contentLabel = new JLabel("<html><p style='width:450px;'>" + post.getContent() + "</p></html>");
        contentLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        add(contentLabel);

        // 액션 버튼 패널 (좋아요, 스크랩)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // 좋아요 버튼과 카운트
        DatabaseServer dbServer = new DatabaseServer();
        JButton likeButton = new JButton(dbServer.hasLikedPost(post.getPostId(), currentUser.getUid()) ? "💔" : "❤️");
        JLabel likeCountLabel = new JLabel(String.valueOf(post.getLikedCnt()));
        
        likeButton.addActionListener(e -> {
            boolean isLiked = dbServer.hasLikedPost(post.getPostId(), currentUser.getUid());
            boolean success;
            
            if (isLiked) {
                success = dbServer.unlikePost(post.getPostId(), currentUser.getUid());
                if (success) {
                    likeButton.setText("❤️");
                }
            } else {
                success = dbServer.likePost(post.getPostId(), currentUser.getUid());
                if (success) {
                    likeButton.setText("💔");
                }
            }
            if (success) {
                refreshPost();
            }
        });

        // 스크랩 버튼
        JButton saveButton = new JButton(dbServer.hasScraped(post.getPostId(), currentUser.getUid()) ? "📌" : "📍");
        saveButton.addActionListener(e -> {
            boolean isScraped = dbServer.hasScraped(post.getPostId(), currentUser.getUid());
            boolean success;
            
            if (isScraped) {
                success = dbServer.removeScrap(post.getPostId(), currentUser.getUid());
                if (success) {
                    saveButton.setText("📍");
                    JOptionPane.showMessageDialog(this, "Post removed from scraps");
                }
            } else {
                success = dbServer.addScrap(post.getPostId(), currentUser.getUid());
                if (success) {
                    saveButton.setText("📌");
                    JOptionPane.showMessageDialog(this, "Post added to scraps");
                }
            }
        });

        // 게시물 작성자인 경우 삭제 버튼 추가
        if (dbServer.isPostOwner(post.getPostId(), currentUser.getUid())) {
            JButton deleteButton = new JButton("🗑️");
            deleteButton.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete this post?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    if (dbServer.deletePost(post.getPostId())) {
                        JOptionPane.showMessageDialog(this, "Post deleted successfully!");
                        // 피드로 돌아가기
                        Container parent = getParent();
                        while (parent != null && !(parent instanceof TwitterUI)) {
                            parent = parent.getParent();
                        }
                        if (parent instanceof TwitterUI) {
                            ((TwitterUI) parent).refreshFeed();
                        }
                        CardLayout cardLayout = (CardLayout) getParent().getLayout();
                        cardLayout.show(getParent(), "Feed");
                    }
                }
            });
            actionPanel.add(deleteButton);
        }

        actionPanel.add(likeButton);
        actionPanel.add(likeCountLabel);
        actionPanel.add(saveButton);
        add(actionPanel);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(separator);
        add(Box.createRigidArea(new Dimension(0, 10)));

        // 댓글 입력 섹션 추가
        addCommentSection();

        // 댓글을 순차적으로 표시
        refreshComments();

        // 돌아가기 버튼
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            // 부모 TwitterUI 찾아서 피드 갱신
            Container parent = getParent();
            while (parent != null && !(parent instanceof TwitterUI)) {
                parent = parent.getParent();
            }
            if (parent instanceof TwitterUI) {
                ((TwitterUI) parent).refreshFeed();
            }
            
            CardLayout cardLayout = (CardLayout) getParent().getLayout();
            cardLayout.show(getParent(), "Feed");
        });
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(backButton);
    }

    private void displayComments() {
        // 기존 댓글 컨테이너 제거
        Component[] components = getComponents();
        for (Component component : components) {
            if (component instanceof JPanel && "commentPanel".equals(component.getName())) {
                remove(component);
            }
        }

        DatabaseServer db = new DatabaseServer();
        // 댓글 패널들을 담을 컨테이너
        JPanel commentsContainer = new JPanel();
        commentsContainer.setLayout(new BoxLayout(commentsContainer, BoxLayout.Y_AXIS));
        commentsContainer.setName("commentPanel");

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

            // 좋아요 버튼과 카운트를 포함한 패널
            DatabaseServer dbServer = new DatabaseServer();
            JPanel likePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton likeButton = new JButton(dbServer.hasLikedComment(comment.getCommentId(), currentUser.getUid()) ? "💔" : "❤️");
            JLabel likeCountLabel = new JLabel(String.valueOf(comment.getLikedCnt()));
            
            likeButton.addActionListener(e -> {
                boolean isLiked = dbServer.hasLikedComment(comment.getCommentId(), currentUser.getUid());
                boolean success;
                
                if (isLiked) {
                    success = dbServer.unlikeComment(comment.getCommentId(), currentUser.getUid());
                    if (success) {
                        likeButton.setText("❤️");
                    }
                } else {
                    success = dbServer.likeComment(comment.getCommentId(), currentUser.getUid());
                    if (success) {
                        likeButton.setText("💔");
                    }
                }
                if (success) {
                    refreshComments();
                }
            });

            // 댓글 작성자인 경우 삭제 버튼 추가
            if (dbServer.isCommentOwner(comment.getCommentId(), currentUser.getUid())) {
                JButton deleteButton = new JButton("🗑️");
                deleteButton.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to delete this comment?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                    );
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        if (dbServer.deleteComment(comment.getCommentId())) {
                            refreshComments();
                        }
                    }
                });
                likePanel.add(deleteButton);
            }

            likePanel.add(likeButton);
            likePanel.add(likeCountLabel);
            commentPanel.add(likePanel);

            // 구분선 추가
            commentPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
            
            commentsContainer.add(commentPanel);
        }

        // 댓글 컨테이너를 Back 버튼 앞에 추가
        add(commentsContainer, getComponentCount() - 2);

        revalidate();
        repaint();
    }

    private void refreshComments() {
        DatabaseServer db = new DatabaseServer();
        comments = db.getCommentsByPostId(post.getPostId());
        displayComments();
    }

    private void addCommentSection() {
        JPanel commentInputPanel = new JPanel(new BorderLayout());
        JTextField commentField = new JTextField();
        JButton submitButton = new JButton("Comment");
        
        submitButton.addActionListener(e -> {
            String content = commentField.getText();
            if (!content.isEmpty()) {
                DatabaseServer db = new DatabaseServer();
                Comment newComment = new Comment(
                    0,  // commentId는 DB에서 자동 생성
                    post.getPostId(),
                    0,  // parent를 null 대신 0으로 설정 (데이터베이스에서는 NULL로 처리됨)
                    currentUser.getUid(),
                    content,
                    0,
                    new Timestamp(System.currentTimeMillis())
                );
                
                if (db.addComment(newComment)) {
                    commentField.setText(""); // 입력 필드 초기화
                    refreshComments(); // 댓글 목록 새로고침
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to add comment", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        commentInputPanel.add(commentField, BorderLayout.CENTER);
        commentInputPanel.add(submitButton, BorderLayout.EAST);
        add(commentInputPanel);
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
