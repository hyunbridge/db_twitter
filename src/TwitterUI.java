import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class TwitterUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private User loggedInUser; // 현재 로그인된 사용자 정보

    public TwitterUI(User loggedInUser) {
        this.loggedInUser = loggedInUser;
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
        JButton homeButton = new JButton("Home");
        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Home 버튼 클릭 시 메인 화면으로 돌아가기
                cardLayout.show(mainPanel, "Feed");
            }
        });

        // Search 버튼
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Search 버튼 클릭 시 SearchPanel 화면으로 전환
                SearchPanel searchPanel = new SearchPanel();
                mainPanel.add(searchPanel, "Search");
                cardLayout.show(mainPanel, "Search");
            }
        });

        // Write Post 버튼 (네모 버튼)
        JButton writePostButton = new JButton();
        writePostButton.setPreferredSize(new Dimension(30, 30)); // 버튼 크기
        writePostButton.setBackground(Color.LIGHT_GRAY); // 배경 색상 설정
        writePostButton.setIcon(new ImageIcon("images/plus1.png")); // 이미지 넣기
        writePostButton.setFocusPainted(false); // 포커스 효과 제거
        writePostButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // WritePostPanel로 전환
                WritePostGUI writePostPanel = new WritePostGUI();
                mainPanel.add(writePostPanel, "WritePost");
                cardLayout.show(mainPanel, "WritePost"); // WritePost 화면 표시
            }
        });

        bottomPanel.add(homeButton);
        bottomPanel.add(writePostButton);
        bottomPanel.add(searchButton);

        // 상단 패널 구성
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBackground(Color.lightGray); // 배경색 설정
        topPanel.setPreferredSize(new Dimension(getWidth(), 50)); // 상단 바의 높이를 설정

        // 프로필 이미지를 원본 그대로 사용
        String profileImagePath = loggedInUser.getProfileImage();
        if (profileImagePath == null) {
            profileImagePath = "images/profile_default1.jpg";  // 기본 이미지
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
        profileImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 프로필 버튼 클릭 시 ProfilePanel로 전환
                ProfilePanel profilePanel = new ProfilePanel(loggedInUser);
                mainPanel.add(profilePanel, "Profile");
                cardLayout.show(mainPanel, "Profile");
            }
        });
        // 상단 패널에 프로필 이미지 버튼을 추가
        topPanel.add(profileImageButton, BorderLayout.WEST);

        // 상단 패널을 상단에 배치
        add(topPanel, BorderLayout.NORTH);
        // 하단 버튼 패널을 하단에 배치
        add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    // 피드 화면 생성
    private JPanel createFeedPanel() {
        // 데이터베이스에서 게시물 가져오기
        DatabaseServer server = new DatabaseServer();
        List<Post> posts = server.getPosts();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (Post post : posts) {
            JPanel postPanel = new PostPanel(post);
            postPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            postPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showDetailPanel(((PostPanel) postPanel).getPost()); // PostPanel에서 Post 객체 가져오기
                }
            });
            // Action Bar (하트, 스크랩, 댓글 버튼) 생성
            JPanel actionBar = createActionBar(post);
            // Post 패널과 Action Bar를 포함한 컨테이너 패널 생성
            JPanel postContainer = new JPanel();
            postContainer.setLayout(new BorderLayout());
            postContainer.add(postPanel, BorderLayout.CENTER);
            postContainer.add(actionBar, BorderLayout.SOUTH);
            // 컨테이너 패널 추가
            panel.add(postContainer);

            // 구분선 추가
            JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1)); // 전체 너비로 설정
            panel.add(separator);

            panel.add(Box.createRigidArea(new Dimension(0, 10))); // 간격 추가
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // 수평 스크롤 비활성화

        // 스크롤 영역의 너비 동적 조정
        panel.setPreferredSize(new Dimension(scrollPane.getWidth(), panel.getPreferredSize().height));
        scrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // 패널 너비를 스크롤패널 크기에 맞춤
                panel.setPreferredSize(new Dimension(scrollPane.getViewport().getWidth(), panel.getPreferredSize().height));
                panel.revalidate();
            }
        });

        // 스크롤 속도 조정
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(20); // 단위 증가 (1회 스크롤바 이동)
        verticalScrollBar.setBlockIncrement(50); // 블록 증가 (Page Up/Down 동작)
        add(scrollPane, BorderLayout.CENTER);

        JPanel feedPanel = new JPanel(new BorderLayout());
        feedPanel.add(scrollPane, BorderLayout.CENTER);
        return feedPanel;
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
        // 댓글 가져오기
        DatabaseServer server = new DatabaseServer();
        List<Comment> comments = server.getComments();  // 해당 글의 댓글들 가져오기

        // DetailPanel 생성
        DetailPanel detailPanel = new DetailPanel(post, comments);

        // 상세 화면 추가
        mainPanel.add(detailPanel, "Detail");
        cardLayout.show(mainPanel, "Detail");
    }

    /*public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TwitterUI().setVisible(true);
        });
    }*/
}

