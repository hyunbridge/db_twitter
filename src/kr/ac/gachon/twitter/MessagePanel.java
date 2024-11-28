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
    private JPanel chatPanel;
    private User selectedPartner;
    private Timer refreshTimer;

    public MessagePanel() {
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        this.db = new DatabaseServer();
        initialize();
        startRefreshTimer();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        // 왼쪽 패널 - 대화 상대 목록
        chatListPanel = new JPanel();
        chatListPanel.setLayout(new BoxLayout(chatListPanel, BoxLayout.Y_AXIS));
        chatListPanel.setPreferredSize(new Dimension(150, getHeight()));
        JScrollPane chatListScroll = new JScrollPane(chatListPanel);
        
        // 오른쪽 패널 - 대화 내용
        chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(new JLabel("Select a chat to start messaging", SwingConstants.CENTER), BorderLayout.CENTER);
        
        // 새로운 메시지 작성 버튼
        JButton newMessageButton = new JButton("New Message");
        newMessageButton.addActionListener(e -> showNewMessageDialog());

        // 뒤로가기 버튼
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            stopRefreshTimer();
            CardLayout cardLayout = (CardLayout) getParent().getLayout();
            cardLayout.show(getParent(), "Feed");
        });

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(newMessageButton);
        buttonPanel.add(backButton);

        // Split Pane으로 좌우 분할
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatListScroll, chatPanel);
        splitPane.setDividerLocation(150);

        add(buttonPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        refreshChatList();
    }

    private void refreshChatList() {
        chatListPanel.removeAll();
        List<User> partners = db.getChatPartners(currentUser.getUid());
        
        for (User partner : partners) {
            JPanel partnerPanel = new JPanel(new BorderLayout());
            partnerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            partnerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            // 상대방 이름과 안 읽은 메시지 수
            int unreadCount = db.getUnreadMessageCountFromUser(currentUser.getUid(), partner.getUid());
            String displayText = partner.getUsername();
            if (unreadCount > 0) {
                displayText += " (" + unreadCount + ")";
            }
            
            JLabel nameLabel = new JLabel(displayText);
            partnerPanel.add(nameLabel);
            
            partnerPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedPartner = partner;
                    showChatHistory(partner);
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    partnerPanel.setBackground(new Color(230, 230, 230));
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    partnerPanel.setBackground(null);
                }
            });
            
            chatListPanel.add(partnerPanel);
            chatListPanel.add(Box.createRigidArea(new Dimension(0, 1)));
        }
        
        chatListPanel.revalidate();
        chatListPanel.repaint();
    }

    private void showChatHistory(User partner) {
        chatPanel.removeAll();
        
        // 대화 내용을 표시할 패널
        JPanel messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        
        // 대화 내용 가져오기
        List<ChatMessage> messages = db.getChatHistory(currentUser.getUid(), partner.getUid());
        
        for (ChatMessage message : messages) {
            JPanel messagePanel = new JPanel();
            messagePanel.setLayout(new BorderLayout());
            
            JTextArea messageText = new JTextArea(message.getMessage());
            messageText.setEditable(false);
            messageText.setLineWrap(true);
            messageText.setWrapStyleWord(true);
            messageText.setBackground(null);
            
            // 메시지 정렬 (보낸 사람이 현재 사용자면 오른쪽, 아니면 왼쪽)
            boolean isSentByMe = message.getSenderId() == currentUser.getUid();
            messagePanel.add(messageText, isSentByMe ? BorderLayout.EAST : BorderLayout.WEST);
            
            // 시간 표시
            JLabel timeLabel = new JLabel(message.getCreatedAt().toString());
            timeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            messagePanel.add(timeLabel, isSentByMe ? BorderLayout.WEST : BorderLayout.EAST);
            
            messagesPanel.add(messagePanel);
            messagesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        
        JScrollPane scrollPane = new JScrollPane(messagesPanel);
        
        // 메시지 입력 패널
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField messageField = new JTextField();
        JButton sendButton = new JButton("Send");
        
        sendButton.addActionListener(e -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                if (db.sendMessage(currentUser.getUid(), partner.getUid(), message)) {
                    messageField.setText("");
                    showChatHistory(partner);
                }
            }
        });
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        
        chatPanel.revalidate();
        chatPanel.repaint();
    }

    private void startRefreshTimer() {
        refreshTimer = new Timer(5000, e -> {
            if (selectedPartner != null) {
                showChatHistory(selectedPartner);
            }
            refreshChatList();
        });
        refreshTimer.start();
    }

    private void stopRefreshTimer() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

    private void showNewMessageDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "New Message", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // 받는 사람 입력
        JTextField receiverField = new JTextField(20);
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("To:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(receiverField, gbc);

        // 메시지 입력
        JTextArea messageArea = new JTextArea(10, 30);
        messageArea.setLineWrap(true);
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        inputPanel.add(new JScrollPane(messageArea), gbc);

        // 전송 버튼
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> {
            String receiverName = receiverField.getText();
            String message = messageArea.getText();
            
            if (!receiverName.isEmpty() && !message.isEmpty()) {
                User receiver = db.getUserByUsername(receiverName);
                if (receiver != null) {
                    if (db.sendMessage(currentUser.getUid(), receiver.getUid(), message)) {
                        JOptionPane.showMessageDialog(dialog, "Message sent successfully!");
                        dialog.dispose();
                        refreshChatList();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to send message", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "User not found", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(sendButton, gbc);

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
} 