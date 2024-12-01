package kr.ac.gachon.twitter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.sql.Timestamp;

public class DetailPanel extends JPanel {
    private Post post;
    private List<Comment> comments;
    private User currentUser;
    private JPanel messagesPanel;

    public DetailPanel(Post post, List<Comment> comments) {
        this.post = post;
        this.comments = comments;
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        // 전체 내용을 담을 패널
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // 스크롤바 너비 계산
        JScrollBar verticalBar = new JScrollBar(JScrollBar.VERTICAL);
        int scrollBarWidth = verticalBar.getPreferredSize().width;

        // 실제 컨텐츠 영역의 너비 계산 (400 - 스크롤바 너비 - 여백)
        int contentWidth = 400 - scrollBarWidth - 20; // 20은 좌우 여백(10px * 2)
        mainContentPanel.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));

        // Header Panel (상단)
        JPanel headerPanel = createHeaderPanel();
        mainContentPanel.add(headerPanel);

        // 댓글 입력 섹션
        JPanel commentInputPanel = new JPanel(new BorderLayout(0, 0));
        commentInputPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        commentInputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField commentField = new JTextField();
        JButton submitButton = subMitComment("images/Send.png", 30, 30);

        submitButton.addActionListener(e -> {
            String content = commentField.getText();
            if (!content.isEmpty()) {
                DatabaseServer db = new DatabaseServer();
                Comment newComment = new Comment(
                        0,
                        post.getPostId(),
                        0,
                        currentUser.getUid(),
                        content,
                        0,
                        new Timestamp(System.currentTimeMillis()));

                if (db.addComment(newComment)) {
                    commentField.setText("");
                    refreshComments();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to add comment",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 버튼과 텍스트 필드 배치
        commentInputPanel.add(commentField, BorderLayout.CENTER);
        commentInputPanel.add(submitButton, BorderLayout.EAST);

        mainContentPanel.add(commentInputPanel);

        /*
         * 
         * // 버튼 너비를 80으로 하고, 입력 필드는 스크롤바 너비까지 고려해서 조정
         * submitButton.setPreferredSize(new Dimension(80, 30));
         * commentField.setPreferredSize(new Dimension(contentWidth - 80, 30));
         * 
         * submitButton.addActionListener(e -> {
         * String content = commentField.getText();
         * if (!content.isEmpty()) {
         * DatabaseServer db = new DatabaseServer();
         * Comment newComment = new Comment(
         * 0,
         * post.getPostId(),
         * 0,
         * currentUser.getUid(),
         * content,
         * 0,
         * new Timestamp(System.currentTimeMillis()));
         * 
         * if (db.addComment(newComment)) {
         * commentField.setText("");
         * refreshComments();
         * } else {
         * JOptionPane.showMessageDialog(this,
         * "Failed to add comment",
         * "Error",
         * JOptionPane.ERROR_MESSAGE);
         * }
         * }
         * });
         * 
         * commentInputPanel.add(commentField, BorderLayout.CENTER);
         * commentInputPanel.add(submitButton, BorderLayout.EAST);
         * 
         * mainContentPanel.add(commentInputPanel);
         */

        // 댓글 목록 패널
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mainContentPanel.add(messagesPanel);

        // 전체 스크롤 패널
        JScrollPane scrollPane = new JScrollPane(mainContentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // 하단 버튼 패널
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        JButton backButton = BackPage("images/Arrow left.png");

        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshComments();
    }

    private void displayComments() {
        // messagesPanel 초기화
        messagesPanel.removeAll();

        DatabaseServer db = new DatabaseServer();
        for (Comment comment : comments) {
            JPanel commentPanel = new JPanel();
            commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));
            commentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            commentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // 댓글 작성자
            String commentUsername = db.getUsernameById(comment.getCreatedBy());
            JLabel commentUserLabel = new JLabel("<html><u>" + commentUsername + "</u></html>");
            commentUserLabel.setFont(new Font("Arial", Font.BOLD, 12));
            commentUserLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            commentUserLabel.setForeground(new Color(29, 161, 242));
            commentUserLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // 클릭 이벤트 추가
            commentUserLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    User commentUser = db.getUserById(String.valueOf(comment.getCreatedBy()));
                    if (commentUser != null) {
                        Container parent = getParent();
                        while (parent != null && !(parent instanceof TwitterUI)) {
                            parent = parent.getParent();
                        }
                        if (parent instanceof TwitterUI) {
                            ProfilePanel profilePanel = new ProfilePanel(commentUser);
                            JPanel mainPanel = (JPanel) getParent();
                            mainPanel.add(profilePanel, "Profile");
                            CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
                            cardLayout.show(mainPanel, "Profile");
                        }
                    }
                }

                public void mouseEntered(MouseEvent evt) {
                    commentUserLabel.setForeground(new Color(20, 120, 180));
                }

                public void mouseExited(MouseEvent evt) {
                    commentUserLabel.setForeground(new Color(29, 161, 242));
                }
            });

            // 댓글 내용
            JLabel commentContentLabel = new JLabel();
            commentContentLabel.setText(comment.getContent());
            commentContentLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            commentContentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // 좋아요 버튼과 카운트를 포함한 패널
            JPanel likePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            likePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            likePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            JButton likeButton = new JButton(
                    (db.hasLikedComment(comment.getCommentId(), currentUser.getUid()) ? "💔 " : "❤️ ")
                            + comment.getLikedCnt());
            likeButton.setPreferredSize(new Dimension(80, 30)); // 버튼 크기 설정

            likeButton.addActionListener(e -> {
                boolean isLiked = db.hasLikedComment(comment.getCommentId(), currentUser.getUid());
                boolean success;

                if (isLiked) {
                    success = db.unlikeComment(comment.getCommentId(), currentUser.getUid());
                    if (success) {
                        likeButton.setText("❤️ " + (comment.getLikedCnt() - 1));
                    }
                } else {
                    success = db.likeComment(comment.getCommentId(), currentUser.getUid());
                    if (success) {
                        likeButton.setText("💔 " + (comment.getLikedCnt() + 1));
                    }
                }
                if (success) {
                    refreshComments();
                }
            });

            // 댓글 작성자인 경우 삭제 버튼 추가
            if (db.isCommentOwner(comment.getCommentId(), currentUser.getUid())) {
                JButton deleteButton = new JButton("🗑️");
                deleteButton.setPreferredSize(new Dimension(80, 30)); // 버튼 크기 설정
                deleteButton.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(
                            this,
                            "Are you sure you want to delete this comment?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        if (db.deleteComment(comment.getCommentId())) {
                            refreshComments();
                        }
                    }
                });
                likePanel.add(deleteButton);
            }

            likePanel.add(likeButton);
            commentPanel.add(commentUserLabel);
            commentPanel.add(commentContentLabel);
            commentPanel.add(likePanel);
            commentPanel.add(new JSeparator(SwingConstants.HORIZONTAL));

            messagesPanel.add(commentPanel);
            messagesPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        messagesPanel.revalidate();
        messagesPanel.repaint();
    }

    private void refreshComments() {
        DatabaseServer db = new DatabaseServer();
        comments = db.getCommentsByPostId(post.getPostId());
        displayComments();
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

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 유저 이름을 클릭 가능한 링크처럼 만들기
        DatabaseServer db = new DatabaseServer();
        String username = db.getUsernameById(post.getCreatedBy());
        JLabel usernameLabel = new JLabel("<html><u>" + username + "</u></html>");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        usernameLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        usernameLabel.setForeground(new Color(29, 161, 242));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT); // 왼쪽 정렬

        // 클릭 이벤트 추가
        usernameLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
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

            public void mouseEntered(MouseEvent evt) {
                usernameLabel.setForeground(new Color(20, 120, 180));
            }

            public void mouseExited(MouseEvent evt) {
                usernameLabel.setForeground(new Color(29, 161, 242));
            }
        });

        // 글 내용을 해시태그가 클릭 가능하도록 변환
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] words = post.getContent().split("\\s+");
        for (String word : words) {
            if (word.startsWith("#")) {
                // 해시태그인 경우 클릭 가능한 라벨로 생성
                JLabel hashtagLabel = new JLabel(word);
                hashtagLabel.setForeground(new Color(29, 161, 242));
                hashtagLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                hashtagLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // SearchPanel로 이동하고 해시태그 검색 수행
                        Container parent = getParent();
                        while (parent != null && !(parent instanceof JPanel)) {
                            parent = parent.getParent();
                        }
                        if (parent != null) {
                            SearchPanel searchPanel = new SearchPanel();
                            parent.add(searchPanel, "Search");
                            CardLayout layout = (CardLayout) parent.getLayout();
                            layout.show(parent, "Search");
                            // 해시태그 검색 수행
                            searchPanel.performSearch(word);
                        }
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hashtagLabel.setForeground(new Color(20, 120, 180));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hashtagLabel.setForeground(new Color(29, 161, 242));
                    }
                });
                contentPanel.add(hashtagLabel);
            } else {
                // 일반 텍스트인 경우
                contentPanel.add(new JLabel(word));
            }
            contentPanel.add(new JLabel(" ")); // 단어 사이에 공백 추가
        }

        // contentLabel 대신 contentPanel을 추가
        headerPanel.add(contentPanel);

        // 이미지가 있는 경우 표시
        JLabel imageLabel = null;
        if (post.getImagePath() != null && !post.getImagePath().isEmpty()) {
            try {
                ImageIcon originalIcon = new ImageIcon(post.getImagePath());
                Image originalImage = originalIcon.getImage();

                // 이미지 크기 조정 (최대 400x400)
                int maxWidth = 400;
                int maxHeight = 400;
                int originalWidth = originalImage.getWidth(null);
                int originalHeight = originalImage.getHeight(null);

                double scale = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
                int scaledWidth = (int) (originalWidth * scale);
                int scaledHeight = (int) (originalHeight * scale);

                Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaledImage);

                imageLabel = new JLabel(scaledIcon);
                imageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                // 이미지 클릭 시 원 크기로 보기
                imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                imageLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        JDialog dialog = new JDialog();
                        dialog.setTitle("Image Viewer");

                        // 원본 이미지 표시
                        JLabel fullImageLabel = new JLabel(new ImageIcon(post.getImagePath()));
                        JScrollPane scrollPane = new JScrollPane(fullImageLabel);
                        dialog.add(scrollPane);

                        // 닫기 버튼
                        JButton closeButton = new JButton("Close");
                        closeButton.addActionListener(event -> dialog.dispose());
                        dialog.add(closeButton, BorderLayout.SOUTH);

                        dialog.setSize(800, 800);
                        dialog.setLocationRelativeTo(null);
                        dialog.setVisible(true);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                imageLabel = new JLabel("Failed to load image");
                imageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            }
        }

        // 액션 버튼 패널 (좋아요, 스크랩)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        DatabaseServer dbServer = new DatabaseServer();

        // 좋아요 버튼과 카운트
        JButton likeButton = new JButton(
                (dbServer.hasLikedPost(post.getPostId(), currentUser.getUid()) ? "💔 " : "❤️ ") + post.getLikedCnt());
        likeButton.setPreferredSize(new Dimension(80, 30)); // 버튼 크기 설정

        likeButton.addActionListener(e -> {
            boolean isLiked = dbServer.hasLikedPost(post.getPostId(), currentUser.getUid());
            boolean success;
            if (isLiked) {
                success = dbServer.unlikePost(post.getPostId(), currentUser.getUid());
                if (success)
                    likeButton.setText("❤️ " + (post.getLikedCnt() - 1));
            } else {
                success = dbServer.likePost(post.getPostId(), currentUser.getUid());
                if (success)
                    likeButton.setText("💔 " + (post.getLikedCnt() + 1));
            }
            if (success)
                refreshPost();
        });

        // 스크랩 버튼
        JButton saveButton = new JButton(dbServer.hasScraped(post.getPostId(), currentUser.getUid()) ? "📌" : "📍");
        saveButton.setPreferredSize(new Dimension(60, 30)); // 버튼 크기 설정
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
            deleteButton.setPreferredSize(new Dimension(60, 30)); // 튼 크기 설정
            deleteButton.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to delete this post?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (dbServer.deletePost(post.getPostId())) {
                        JOptionPane.showMessageDialog(this, "Post deleted successfully!");
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
        actionPanel.add(saveButton);

        headerPanel.add(usernameLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        headerPanel.add(contentPanel);

        // 이미지가 있는 경우 추가
        if (imageLabel != null) {
            headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            headerPanel.add(imageLabel);
        }

        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        headerPanel.add(actionPanel);

        return headerPanel;
    }

    private JButton BackPage(String filePath) {
        try {
            // 이미지 로드 및 크기 조정
            ImageIcon icon = new ImageIcon(
                    new ImageIcon(filePath).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));

            // 버튼 생성
            JButton imageButton = new JButton(icon);

            // 버튼 스타일 제거 (이미지처럼 보이게 설정)
            imageButton.setBorderPainted(false); // 외곽선 제거
            imageButton.setContentAreaFilled(false);
            imageButton.setFocusPainted(false); // 포커스 표시 제거

            // 클릭 이벤트 추가
            imageButton.addActionListener(e -> {
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

            return imageButton;
        } catch (Exception e) {
            // 이미지가 없는 경우 대체 텍스트 버튼 생성
            JButton placeholderButton = new JButton("Image not found");
            placeholderButton.setEnabled(false); // 클릭 불가능
            return placeholderButton;
        }
    }

    private JButton subMitComment(String filePath, int width, int height) {
        try {
            // 이미지 로드 및 크기 조정
            ImageIcon icon = new ImageIcon(
                    new ImageIcon(filePath).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));

            // 버튼 생성
            JButton imageButton = new JButton(icon);

            // 버튼 스타일 제거 (이미지처럼 보이게 설정)
            imageButton.setBorderPainted(false); // 외곽선 제거
            imageButton.setContentAreaFilled(false); // 배경 제거
            imageButton.setFocusPainted(false); // 포커스 표시 제거

            return imageButton;
        } catch (Exception e) {
            // 이미지가 없는 경우 대체 버튼 생성
            JButton placeholderButton = new JButton("Send");
            placeholderButton.setEnabled(false); // 클릭 불가능
            return placeholderButton;
        }
    }

}
