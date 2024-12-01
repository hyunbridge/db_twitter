package kr.ac.gachon.twitter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MessagePanel extends JPanel {
    private User currentUser;
    private DatabaseServer db;
    private JPanel chatListPanel;
    private User selectedPartner;
    private Timer refreshTimer;

    public MessagePanel() {
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        this.db = new DatabaseServer();
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        // 대화 상대 목록 패널
        chatListPanel = new JPanel();
        chatListPanel.setLayout(new BoxLayout(chatListPanel, BoxLayout.Y_AXIS));
        JScrollPane chatListScroll = new JScrollPane(chatListPanel);
        chatListScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // 왼쪽 목록만 기본으로 표시
        add(chatListScroll, BorderLayout.CENTER);

        // 하단에 새 메시지 버튼 추가
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton newMessageButton = new JButton("New Message");
        newMessageButton.addActionListener(e -> showNewMessageDialog());
        bottomPanel.add(newMessageButton);
        add(bottomPanel, BorderLayout.SOUTH);


        refreshChatList();
    }

    private void refreshChatList() {
        chatListPanel.removeAll();
        List<User> partners = db.getChatPartners(currentUser.getUid());

        for (User partner : partners) {
            // 사용자 항목 패널 생성
            JPanel partnerPanel = new JPanel(new BorderLayout());
            partnerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            partnerPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));

            // 사용자 이름 및 읽지 않은 메시지 수 표시
            int unreadCount = db.getUnreadMessageCountFromUser(currentUser.getUid(), partner.getUid());
            String displayText = partner.getUsername();
            if (unreadCount > 0) {
                displayText += " (" + unreadCount + ")";
            }

            JLabel nameLabel = new JLabel(displayText);
            nameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            partnerPanel.add(nameLabel, BorderLayout.CENTER);

            // 마우스 이벤트 추가 (배경 색 변경)
            partnerPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedPartner = partner;
                    showChatDetail(partner);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    partnerPanel.setBackground(new Color(240, 240, 240));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    partnerPanel.setBackground(Color.WHITE);
                }
            });

            // 사용자 항목을 목록에 추가
            chatListPanel.add(partnerPanel);
        }

        chatListPanel.revalidate();
        chatListPanel.repaint();
    }

    private void showChatDetail(User partner) {
        List<ChatMessage> messages = db.getChatHistory(currentUser.getUid(), partner.getUid());
        ChatDetail chatDetail;
        chatDetail = new ChatDetail(currentUser, partner, messages, (CardLayout) getParent().getLayout(), (JPanel) getParent());

        // 기존 패널 제거 및 새 ChatDetail 추가
        removeAll();
        add(chatDetail, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void stopRefreshTimer() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

    private void showNewMessageDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "New Message", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 150);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // 보낼 사람 입력 필드
        JTextField receiverField = new JTextField(20);
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("To:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(receiverField, gbc);

        // 확인 버튼
        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> {
            String receiverName = receiverField.getText().trim();
            if (!receiverName.isEmpty()) {
                // 데이터베이스에서 사용자 검색
                User receiver = db.getUserByUsername(receiverName);
                if (receiver != null) {
                    // 유효한 사용자일 경우 ChatDetail로 이동
                    dialog.dispose(); // 다이얼로그 닫기

                    // 채팅 리스트에 추가된 사용자 확인
                    if (!isUserInChatList(receiver)) {
                        addUserToChatList(receiver); // 사용자 목록에 추가
                    }

                    // ChatDetail 화면으로 이동
                    showChatDetail(receiver);
                } else {
                    // 유효하지 않은 사용자 경고 메시지
                    JOptionPane.showMessageDialog(dialog, "User not found", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // 입력이 비어 있는 경우 경고 메시지
                JOptionPane.showMessageDialog(dialog, "Please enter a valid username", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(confirmButton, gbc);

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ChatList에 사용자가 이미 있는지 확인하는 메서드
    private boolean isUserInChatList(User user) {
        List<User> partners = db.getChatPartners(currentUser.getUid());
        for (User partner : partners) {
            if (partner.getUid() == user.getUid()) {
                return true;
            }
        }
        return false;
    }

    // ChatList에 사용자를 추가하는 메서드
    private void addUserToChatList(User user) {
        // 데이터베이스 혹은 내부 리스트에 사용자 추가
        db.addChatPartner(currentUser.getUid(), user.getUid());
        refreshChatList(); // ChatList 갱신
    }

}
