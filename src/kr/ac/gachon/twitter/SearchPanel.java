package kr.ac.gachon.twitter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class SearchPanel extends JPanel {
    private JTextField searchField;
    private JPanel resultsPanel;
    private JTabbedPane tabbedPane;
    private User currentUser;
    private DatabaseServer db;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public SearchPanel() {
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        this.db = new DatabaseServer();
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        
        // 검색 패널
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        JButton searchButton = new JButton("🔍");
        
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        
        // 탭 패널
        tabbedPane = new JTabbedPane();
        
        // All 결과 패널
        JPanel allPanel = new JPanel();
        allPanel.setLayout(new BoxLayout(allPanel, BoxLayout.Y_AXIS));
        
        // 사용자 결과 패널
        JPanel usersPanel = new JPanel();
        usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));
        
        // 게시물 결과 패널
        JPanel postsPanel = new JPanel();
        postsPanel.setLayout(new BoxLayout(postsPanel, BoxLayout.Y_AXIS));
        
        // 스크롤 패널에 결과 패널 추가
        JScrollPane allScroll = new JScrollPane(allPanel);
        JScrollPane usersScroll = new JScrollPane(usersPanel);
        JScrollPane postsScroll = new JScrollPane(postsPanel);
        
        // 탭에 스크롤 패널 추가
        tabbedPane.addTab("All", allScroll);
        tabbedPane.addTab("Users", usersScroll);
        tabbedPane.addTab("Posts", postsScroll);
        
        // 검색 버튼 이벤트
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (!keyword.isEmpty()) {
                performSearch(keyword, allPanel, usersPanel, postsPanel);
            }
        });
        
        // 엔터 키 이벤트
        searchField.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (!keyword.isEmpty()) {
                performSearch(keyword, allPanel, usersPanel, postsPanel);
            }
        });

        add(searchPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        
        // 뒤로가기 버튼
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            if (getParent().getLayout() instanceof CardLayout) {
                ((CardLayout) getParent().getLayout()).show(getParent(), "Feed");
            }
        });
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void performSearch(String keyword, JPanel allPanel, JPanel usersPanel, JPanel postsPanel) {
        // 기존 결과 초기화
        allPanel.removeAll();
        usersPanel.removeAll();
        postsPanel.removeAll();
        
        // 사용자 검색
        List<User> users = db.searchUsers(keyword);
        if (!users.isEmpty()) {
            // All 탭에 "Users" 헤더 추가
            JLabel usersHeader = new JLabel("Users");
            usersHeader.setFont(new Font("Arial", Font.BOLD, 16));
            usersHeader.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
            allPanel.add(usersHeader);
        }
        
        // 사용자 결과 추가
        for (User user : users) {
            JPanel userPanel = createUserPanel(user);
            // Users 탭에 추가
            usersPanel.add(userPanel);
            usersPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            // All 탭에도 추가
            allPanel.add(userPanel);
            allPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        // 게시물 검색
        List<Post> posts = db.searchPosts(keyword, currentUser.getUid());
        if (!posts.isEmpty()) {
            // All 탭에 구분선과 "Posts" 헤더 추가
            if (!users.isEmpty()) {
                allPanel.add(new JSeparator());
                allPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
            JLabel postsHeader = new JLabel("Posts");
            postsHeader.setFont(new Font("Arial", Font.BOLD, 16));
            postsHeader.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
            allPanel.add(postsHeader);
        }
        
        // 게시물 결과 추가
        for (Post post : posts) {
            PostPanel postPanel = new PostPanel(post, currentUser);
            postPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showDetailPanel(post);
                }
            });
            // Posts 탭에 추가
            postsPanel.add(postPanel);
            postsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            // All 탭에도 추가
            allPanel.add(postPanel);
            allPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        
        // 결과가 없는 경우 메시지 표시
        if (users.isEmpty() && posts.isEmpty()) {
            JLabel noResultsLabel = new JLabel("No results found for \"" + keyword + "\"");
            noResultsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            noResultsLabel.setForeground(Color.GRAY);
            noResultsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            allPanel.add(Box.createVerticalGlue());
            allPanel.add(noResultsLabel);
            allPanel.add(Box.createVerticalGlue());
        }
        
        // UI 갱신
        allPanel.revalidate();
        allPanel.repaint();
        usersPanel.revalidate();
        usersPanel.repaint();
        postsPanel.revalidate();
        postsPanel.repaint();
    }

    private JPanel createUserPanel(User user) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setBackground(Color.WHITE);
        
        // 사용자 정보
        JLabel nameLabel = new JLabel(user.getUsername());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel bioLabel = new JLabel(user.getBio() != null ? user.getBio() : "");
        bioLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.add(nameLabel);
        infoPanel.add(bioLabel);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        
        // 프로필로 이동하는 클릭 이벤트
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Container parent = getParent();
                while (parent != null && !(parent instanceof JPanel)) {
                    parent = parent.getParent();
                }
                if (parent != null) {
                    ProfilePanel profilePanel = new ProfilePanel(user);
                    parent.add(profilePanel, "Profile");
                    CardLayout layout = (CardLayout) parent.getLayout();
                    layout.show(parent, "Profile");
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(new Color(240, 240, 240));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(Color.WHITE);
            }
        });
        
        return panel;
    }

    private void showDetailPanel(Post post) {
        List<Comment> comments = db.getCommentsByPostId(post.getPostId());
        DetailPanel detailPanel = new DetailPanel(post, comments);
        
        Container parent = getParent();
        if (parent != null && parent.getLayout() instanceof CardLayout) {
            parent.add(detailPanel, "Detail");
            ((CardLayout) parent.getLayout()).show(parent, "Detail");
        }
    }
}
