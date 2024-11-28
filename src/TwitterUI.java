//import Chatting.Chat;
//import Chatting.ChatDetail;
//import Chatting.ChatList;
//import Chatting.ChattingPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


public class TwitterUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private long userId; // 현재 사용자의 ID (로그인 후 설정 필요)

    public TwitterUI() {

        setTitle("Twitter Feed");
        setSize(500, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 메인 화면 구성
        JPanel feedPanel = createFeedPanel();
        mainPanel.add(feedPanel, "Feed");

//        // 채팅 화면 추가
//        JPanel chatPanel = createChatPanel();
//        mainPanel.add(chatPanel, "ChatList");
//
//        add(mainPanel);

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

        // Chatting 버튼
        JButton chattingButton = new JButton("Chatting");
        chattingButton.addActionListener(e -> cardLayout.show(mainPanel, "ChatList"));

        // Write Post 버튼 (네모 버튼)
        JButton writePostButton = new JButton();
        writePostButton.setPreferredSize(new Dimension(50, 50)); // 버튼 크기
        writePostButton.setBackground(Color.LIGHT_GRAY); // 배경 색상 설정
        writePostButton.setIcon(new ImageIcon("images/plus.jpg")); // 이미지 넣기
        writePostButton.setFocusPainted(false); // 포커스 효과 제거
        writePostButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Write Post 버튼 클릭 시 WritePostPanel 화면으로 전환
                WritePostGUI w1 = new WritePostGUI();
                w1.setVisible(true);
                // 현재 .. 창 닫기
                //dispose();
            }
        });

        bottomPanel.add(homeButton);
        bottomPanel.add(writePostButton); // 새로운 버튼 추가
        bottomPanel.add(searchButton);
        bottomPanel.add(chattingButton);

        // 하단 버튼 패널을 하단에 배치
        add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createFeedPanel() {
        // 데이터베이스에서 게시물 가져오기
        DatabaseServer server = new DatabaseServer();
        List<Post> posts = server.getPosts();

        return new PostList(this, posts);
    }

    public void showDetailPanel(Post post) {
        // 댓글 가져오기
        DatabaseServer server = new DatabaseServer();
        List<Comment> comments = server.getComments();

        // DetailPanel 생성
        DetailPanel detailPanel = new DetailPanel(post, comments);

        // 상세 화면 추가
        mainPanel.add(detailPanel, "Detail");
        cardLayout.show(mainPanel, "Detail");
    }

//    private JPanel createChatPanel() {
//        // 데이터베이스에서 채팅 리스트 가져오기
//        DatabaseServer server = new DatabaseServer();
//        List<Chat> chats = server.getChats(userId);
//
//        // ChatList 생성
//        return new ChatList(mainPanel, cardLayout, chats, userId);
//    }

// 리팩토링되기 전 코드
//    public void showChatPanel(Chat chat) {
//        DatabaseServer server = new DatabaseServer();
//
//        // DetailPanel 생성
//        ChatDetail chatDetail = new ChatDetail(chat);
//
//        // 상세 화면 추가
//        mainPanel.add(chatDetail, "ChatDetail");
//        cardLayout.show(mainPanel, "ChatDetail");
//    }


//    // 피드 화면 생성
//    private JPanel createFeedPanel() {
//        // 데이터베이스에서 게시물 가져오기
//        DatabaseServer server = new DatabaseServer();
//        List<Post> posts = server.getPosts();
//
//        JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//
//        for (Post post : posts) {
//            JPanel postPanel = createPostPanel(post);
//            postPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//            postPanel.addMouseListener(new MouseAdapter() {
//                @Override
//                public void mouseClicked(MouseEvent e) {
//                    showDetailPanel(post); // 상세 화면 표시
//                }
//            });
//
//            panel.add(postPanel);
//
//            // 구분선 추가
//            JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
//            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1)); // 전체 너비로 설정
//            panel.add(separator);
//
//            panel.add(Box.createRigidArea(new Dimension(0, 10))); // 간격 추가
//        }
//
//        JScrollPane scrollPane = new JScrollPane(panel);
//        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//
//        // 스크롤 속도 조정
//        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
//        verticalScrollBar.setUnitIncrement(20); // 단위 증가 (1회 스크롤바 이동)
//        verticalScrollBar.setBlockIncrement(50); // 블록 증가 (Page Up/Down 동작)
//        add(scrollPane, BorderLayout.CENTER);
//
//        JPanel feedPanel = new JPanel(new BorderLayout());
//        feedPanel.add(scrollPane, BorderLayout.CENTER);
//        return feedPanel;
//    }
//
//    // 개별 글을 나타내는 패널 생성
//    private JPanel createPostPanel(Post post) {
//        JPanel postPanel = new JPanel();
//        postPanel.setLayout(new BoxLayout(postPanel, BoxLayout.Y_AXIS));
//        // 유저 이름 표시
//        JLabel usernameLabel = new JLabel(post.getUsername());
//        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
//        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        // 글 내용 표시
//        JLabel contentLabel = new JLabel("<html><p style='width:400px;'>" + post.getContent() + "</p></html>");
//        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        // 좋아요 수, 조회 수 등 정보 표시
//        //JLabel statsLabel = new JLabel("Likes: " + post.getLikedCnt() + "    Views: " + post.getViewCount());
//        JLabel statsLabel = new JLabel("Likes: " + post.getLikedCnt() + "    Views: not yet..");
//        statsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        postPanel.add(usernameLabel);
//        postPanel.add(contentLabel);
//        postPanel.add(statsLabel);
//        return postPanel;
//    }
//
//    private void showDetailPanel(Post post) {
//        // 댓글 가져오기
//        DatabaseServer server = new DatabaseServer();
//        List<Comment> comments = server.getComments();  // 해당 글의 댓글들 가져오기
//
//        // DetailPanel 생성
//        DetailPanel detailPanel = new DetailPanel(post, comments);
//
//        // 상세 화면 추가
//        mainPanel.add(detailPanel, "Detail");
//        cardLayout.show(mainPanel, "Detail");
//    }

    /*public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TwitterUI().setVisible(true);
        });
    }*/
}

