package kr.ac.gachon.twitter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SearchPanel extends JPanel {
    private JTextField searchField;
    private JPanel resultsPanel;
    private User currentUser;
    private DatabaseServer db;

    public SearchPanel() {
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        this.db = new DatabaseServer();
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        // 스크롤바 너비 계산
        JScrollBar verticalBar = new JScrollBar(JScrollBar.VERTICAL);
        int scrollBarWidth = verticalBar.getPreferredSize().width;
        int contentWidth = 400 - scrollBarWidth - 20; // 20은 좌우 여백(10px * 2)

        // 검색 패널
        JPanel searchPanel = new JPanel(new BorderLayout(0, 0));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        searchPanel.setMaximumSize(new Dimension(contentWidth, 30)); // 높이 제한
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(contentWidth - 50, 30)); // 검색 버튼 너비(50)를 뺀 나머지
        JButton searchButton = new JButton("🔍");
        searchButton.setPreferredSize(new Dimension(50, 30));

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // 결과 패널
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        resultsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 전체 컨텐츠 패널
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(searchPanel);
        contentPanel.add(resultsPanel);

        // 스크롤 패널
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 검색 이벤트
        ActionListener searchAction = e -> {
            String keyword = searchField.getText().trim();
            if (!keyword.isEmpty()) {
                performSearch(keyword);
            }
        };

        searchButton.addActionListener(searchAction);
        searchField.addActionListener(searchAction);

        add(scrollPane, BorderLayout.CENTER);

        // 뒤로가기 버튼
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        JButton backButton = backStep("C:/Users/gram/Downloads/Arrow left.png");

        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void performSearch(String keyword) {
        resultsPanel.removeAll();

        // 사용자 검색 결과
        List<User> users = db.searchUsers(keyword);
        if (!users.isEmpty()) {
            JLabel usersHeader = new JLabel("Users");
            usersHeader.setFont(new Font("Arial", Font.BOLD, 16));
            usersHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            resultsPanel.add(usersHeader);
            resultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            for (User user : users) {
                JPanel userPanel = createUserPanel(user);
                resultsPanel.add(userPanel);
                resultsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        // 게시물 검색 결과
        List<Post> posts = db.searchPosts(keyword, currentUser.getUid());
        if (!posts.isEmpty()) {
            if (!users.isEmpty()) {
                resultsPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
                resultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }

            JLabel postsHeader = new JLabel("Posts");
            postsHeader.setFont(new Font("Arial", Font.BOLD, 16));
            postsHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            resultsPanel.add(postsHeader);
            resultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            for (Post post : posts) {
                PostPanel postPanel = new PostPanel(post, currentUser);
                postPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                postPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showDetailPanel(post);
                    }
                });
                resultsPanel.add(postPanel);
                resultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        if (users.isEmpty() && posts.isEmpty()) {
            JLabel noResultsLabel = new JLabel("No results found for \"" + keyword + "\"");
            noResultsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            noResultsLabel.setForeground(Color.GRAY);
            noResultsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            resultsPanel.add(noResultsLabel);
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private JPanel createUserPanel(User user) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 사용자 정보
        JLabel nameLabel = new JLabel(user.getUsername());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel bioLabel = new JLabel(user.getBio() != null ? user.getBio() : "");
        bioLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        bioLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(nameLabel);
        panel.add(bioLabel);

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

    private JButton backStep(String filePath) {
        try {
            // 이미지 로드 및 크기 조정
            ImageIcon icon = new ImageIcon(new ImageIcon(filePath).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));

            // 버튼 생성
            JButton imageButton = new JButton(icon);

            // 버튼 스타일 제거 (이미지처럼 보이게 설정)
            imageButton.setBorderPainted(false);    // 외곽선 제거
            imageButton.setContentAreaFilled(false);
            imageButton.setFocusPainted(false);     // 포커스 표시 제거

            // 클릭 이벤트 추가
            imageButton.addActionListener(e -> {
                if (getParent().getLayout() instanceof CardLayout) {
                    ((CardLayout) getParent().getLayout()).show(getParent(), "Feed");
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

}
