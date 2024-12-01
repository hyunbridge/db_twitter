package kr.ac.gachon.twitter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class TwitterUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel feedPanel;
    private JComboBox<String> filterComboBox;
    private String currentFilter = "All";

    public TwitterUI() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("No user logged in");
        }
        initialize();
    }

    private void initialize(){
        setTitle("Twitter Feed");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 메인 화면 구성
        JPanel feedPanel = createFeedPanel();
        mainPanel.add(feedPanel, "Feed");

        add(mainPanel);

        // 하단 버튼 패널 구성
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Home 버튼
        JButton homeButton = mainHomeButton("C:/Users/gram/Downloads/Home.png");

        // Search 버튼
        JButton searchButton = mainSearchButton("C:/Users/gram/Downloads/Search.png");

        // Write Post 버튼 (네모 버튼)
        JButton writePostButton = new JButton();
        writePostButton.setPreferredSize(new Dimension(30, 30)); // 버튼 크기
        writePostButton.setBackground(Color.LIGHT_GRAY); // 배경 색상 설정
        writePostButton.setIcon(new ImageIcon("images/plus1.png")); // 이미지 넣기
        writePostButton.setFocusPainted(false); // 포커스 효과 제거
        writePostButton.addActionListener(e -> {
            WritePostGUI writePostPanel = new WritePostGUI();
            mainPanel.add(writePostPanel, "WritePost");
            cardLayout.show(mainPanel, "WritePost");
        });

        bottomPanel.add(homeButton);
        bottomPanel.add(writePostButton);
        bottomPanel.add(searchButton);

        // 상단 패널 구성
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBackground(Color.lightGray);
        topPanel.setPreferredSize(new Dimension(getWidth(), 50));

        // 프로필 이미지 관련 부분 수정
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String profileImagePath = currentUser.getProfileImage();
        if (profileImagePath == null) {
            profileImagePath = "images/profile_default.jpg";  // 기본 이미지
        }

        // 이미지 크기 조정 (예: 50x50 크기로 줄임)
        ImageIcon profileImageIcon = new ImageIcon(profileImagePath);
        Image image = profileImageIcon.getImage(); // 원본 이미지를 가져옴
        Image scaledImage = image.getScaledInstance(30 , 30, Image.SCALE_SMOOTH); // 이미지 크기 조정
        profileImageIcon = new ImageIcon(scaledImage);

        // 프로필 이미지 버튼 만들기
        JButton profileImageButton = new JButton();
        profileImageButton.setIcon(profileImageIcon);  // 버튼에 이미지 설정
        profileImageButton.setContentAreaFilled(false);  // 버튼 배경 제거
        profileImageButton.setBorderPainted(false);  // 버튼 테두리 제거

        // 버튼 클릭 시 프로필 화면으로 전환
        profileImageButton.addActionListener(e -> {
            ProfilePanel profilePanel = new ProfilePanel(currentUser);
            mainPanel.add(profilePanel, "Profile");
            cardLayout.show(mainPanel, "Profile");
        });

        // 쪽지함 버튼 (오른쪽)
        JButton messageButton = new JButton("✉️");
        DatabaseServer db = new DatabaseServer();
        int unreadCount = db.getUnreadMessageCount(currentUser.getUid());
        if (unreadCount > 0) {
            messageButton.setText("✉️ (" + unreadCount + ")");
        }
        messageButton.addActionListener(e -> {
            MessagePanel messagePanel = new MessagePanel();
            mainPanel.add(messagePanel, "Messages");
            cardLayout.show(mainPanel, "Messages");
        });

        topPanel.add(profileImageButton, BorderLayout.WEST);
        topPanel.add(messageButton, BorderLayout.EAST);

        // 상단 패널을 상단에 배치
        add(topPanel, BorderLayout.NORTH);
        // 하단 버튼 패널을 하단에 배치
        add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
    }




    // 피드 화면 생성
    private JPanel createFeedPanel() {
        // 피드 컨테이너 패널
        JPanel containerPanel = new JPanel(new BorderLayout());
        
        // 필터 패널 (상단)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] filterOptions = {"All posts", "Following user's posts", "Hot posts", "Scraped posts"};
        filterComboBox = new JComboBox<>(filterOptions);
        
        JButton refreshButton = new JButton("🔄");
        refreshButton.setToolTipText("Refresh");
        
        // 필터나 새로고침 버튼 클릭 시 피드 갱신
        ActionListener refreshAction = e -> {
            currentFilter = switch((String)filterComboBox.getSelectedItem()) {
                case "Following user's posts" -> "Following";
                case "Hot posts" -> "Hot";
                case "Scraped posts" -> "Scraped";
                default -> "All";
            };
            refreshFeed(currentFilter);
        };
        
        filterComboBox.addActionListener(refreshAction);
        refreshButton.addActionListener(refreshAction);
        
        filterPanel.add(filterComboBox);
        filterPanel.add(refreshButton);
        
        // 피드 패널
        feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 초기 피드 로드
        refreshFeed("All");

        // 스크롤 패널 설정
        JScrollPane scrollPane = new JScrollPane(feedPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // 스크롤 속도 조정
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(20);
        verticalScrollBar.setBlockIncrement(50);

        // 컨테이너에 필터 패널과 스크롤 패널 추가
        containerPanel.add(filterPanel, BorderLayout.NORTH);
        containerPanel.add(scrollPane, BorderLayout.CENTER);
        
        return containerPanel;
    }

    // 피드 갱신 메서드 수정
    public void refreshFeed() {
        refreshFeed(currentFilter);
    }

    public void refreshFeed(String filter) {
        feedPanel.removeAll();

        DatabaseServer server = new DatabaseServer();
        User currentUser = SessionManager.getInstance().getCurrentUser();
        List<Post> posts = server.getPosts(filter, currentUser.getUid());

        for (Post post : posts) {
            JPanel postPanel = new PostPanel(post, currentUser);
            postPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            postPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showDetailPanel(((PostPanel) postPanel).getPost());
                }
            });

            feedPanel.add(postPanel);

            // 구분선 추가
            JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            feedPanel.add(separator);
            feedPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        feedPanel.revalidate();
        feedPanel.repaint();
    }

    // Action Bar 생성 메서드
    private JPanel createActionBar(Post post) {
        JPanel actionBar = new JPanel();
        actionBar.setLayout(new FlowLayout(FlowLayout.LEFT)); // 버튼을 왼쪽으로 정렬
        actionBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // 좋아요 수 Label
        JLabel likeCountLabel = new JLabel(" " + post.getLikedCnt());
        // 하트 버튼
        JButton likeButton = new JButton("❤️");
        likeButton.addActionListener(e -> {
            post.plusLikedCnt();  //업데이트된 likedcnt 값 바로 화면에 업데이트 필요
            likeCountLabel.setText(" " + post.getLikedCnt()); // UI 업데이트
        });

        // 스크랩 버튼
        JButton saveButton = new JButton("📌");
        saveButton.addActionListener(e -> {
            // 스크랩 기능 구현
        });

        // 댓글 버튼
        JButton commentButton = new JButton("💬");
        // 2. 댓글 수 표시를 위한 JLabel
        //JLabel commentCountLabel = new JLabel(String.valueOf(post.getCommentCount()));;
        JLabel commentCountLabel = new JLabel(" "+String.valueOf(999));;
        commentCountLabel.setText(" ");

        // Action Bar에 버튼 추가
        actionBar.add(likeButton);
        actionBar.add(likeCountLabel); // 좋아요 수 표시
        actionBar.add(saveButton);
        actionBar.add(commentButton);
        actionBar.add(commentCountLabel); // 댓글 수 표시

        return actionBar;
    }

    private void showDetailPanel(Post post) {
        DatabaseServer server = new DatabaseServer();
        List<Comment> comments = server.getCommentsByPostId(post.getPostId());
        DetailPanel detailPanel = new DetailPanel(post, comments) {
            @Override
            public void removeNotify() {
                super.removeNotify();
                refreshFeed();
            }
        };
        mainPanel.add(detailPanel, "Detail");
        cardLayout.show(mainPanel, "Detail");
    }

    private JButton mainHomeButton(String filePath) {
        try {
            // 이미지 로드 및 크기 조정
            ImageIcon icon = new ImageIcon(new ImageIcon(filePath).getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));

            // 버튼 생성
            JButton imageButton = new JButton(icon);

            // 버튼 스타일 제거 (이미지처럼 보이게 설정)
            imageButton.setBorderPainted(false);
            imageButton.setContentAreaFilled(false);
            imageButton.setFocusPainted(false);

            // 클릭 이벤트 추가
            imageButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Home 버튼 클릭 시 메인 화면으로 돌아가기
                    cardLayout.show(mainPanel, "Feed");
                }
            });

            return imageButton;
        } catch (Exception e) {
            // 이미지가 없는 경우 대체 텍스트 버튼 생성
            JButton placeholderButton = new JButton("Image not found");
            placeholderButton.setEnabled(false); // 클릭 불가능
            return placeholderButton;
        }
    }

    private JButton mainSearchButton(String filePath) {
        try {
            // 이미지 로드 및 크기 조정
            ImageIcon icon = new ImageIcon(new ImageIcon(filePath).getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));

            // 버튼 생성
            JButton imageButton = new JButton(icon);

            // 버튼 스타일 제거 (이미지처럼 보이게 설정)
            imageButton.setBorderPainted(false);
            imageButton.setContentAreaFilled(false);
            imageButton.setFocusPainted(false);

            imageButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Search 버튼 클릭 시 SearchPanel 화면으로 전환
                    SearchPanel searchPanel = new SearchPanel();
                    mainPanel.add(searchPanel, "Search");
                    cardLayout.show(mainPanel, "Search");
                }
            });

            return imageButton;
        } catch (Exception e) {
            // 이미지가 없는 경우 대체 텍스트 버튼 생성
            JButton placeholderButton = new JButton("Image not found");
            placeholderButton.setEnabled(false); // 클릭 불가능
            return placeholderButton;
        }
    }



    /*public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TwitterUI().setVisible(true);
        });
    }*/
}

