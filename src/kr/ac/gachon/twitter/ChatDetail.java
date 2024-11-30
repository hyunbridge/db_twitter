package kr.ac.gachon.twitter;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Locale;

public class ChatDetail extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private User currentUser;
    private User partner;
    private List<ChatMessage> chats;
    private JPanel messagesPanel;
    private JTextField messageField;

    public ChatDetail(User currentUser, User partner, List<ChatMessage> chats, CardLayout cardLayout, JPanel mainPanel) {
        this.currentUser = currentUser;
        this.partner = partner;
        this.chats = chats != null ? chats : List.of();
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 상대방 프로필 이미지 경로 가져오기
        String profileImagePath = partner.getProfileImage();
        if (profileImagePath == null || profileImagePath.isEmpty()) {
            profileImagePath = "C:/Users/gram/Downloads/twitter_profile.png";
        }

        // 프로필 이미지 설정
        ImageIcon profileImageIcon = new ImageIcon(
                new ImageIcon(profileImagePath).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)
        );

        // 프로필 이미지 버튼 생성
        JButton profileImageButton = new JButton();
        profileImageButton.setIcon(profileImageIcon);
        profileImageButton.setContentAreaFilled(false);
        profileImageButton.setBorderPainted(false);
        profileImageButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 프로필 이미지 버튼 클릭 시 대화 상대의 프로필 화면으로 이동
        profileImageButton.addActionListener(e -> {
            if (mainPanel.getLayout() instanceof CardLayout) {
                // ProfilePanel 생성 및 추가
                ProfilePanel profilePanel = new ProfilePanel(partner);
                mainPanel.add(profilePanel, "Profile");
                // CardLayout을 통해 Profile 화면으로 이동
                ((CardLayout) mainPanel.getLayout()).show(mainPanel, "Profile");
            } else {
                // 잘못된 부모 컨테이너가 설정된 경우 경고 메시지
                JOptionPane.showMessageDialog(this, "Invalid parent panel for CardLayout.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        // 프로필 이미지 버튼 추가
        headerPanel.add(profileImageButton);

        // 대화 상대 이름 추가
        JLabel partnerNameLabel = new JLabel(partner.getUsername());
        partnerNameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        partnerNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        partnerNameLabel.setForeground(Color.BLACK);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(partnerNameLabel);

        // Delete Button
        JButton deleteButton = new JButton("Delete");
        deleteButton.setBackground(Color.RED);
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);

        // *** MODIFIED: Delete button alignment ***
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteButton.setPreferredSize(new Dimension(80, 30));

        deleteButton.addActionListener(e -> {
            deleteChat();
            cardLayout.show(mainPanel, "ChatList");
        });


        // *** MODIFIED: Added delete button to header panel ***
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(deleteButton);

        add(headerPanel, BorderLayout.NORTH);

        // 메시지 리스트
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        refreshMessages();

        // 입력 패널
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        JButton attachButton = new JButton("+");
        JButton sendButton = new JButton("Send");

        attachButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null) {
                    String filePath = selectedFile.getAbsolutePath();
                    String fileName = selectedFile.getName();

                    // 메시지 전송 및 업데이트 UI
                    boolean success = new DatabaseServer().sendMessageWithFile(currentUser.getUid(), partner.getUid(), fileName, filePath);
                    if (success) {
                        sendMessage(filePath, filePath);
                        messageField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to send the file.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        sendButton.addActionListener(e -> {
            String content = messageField.getText();
            if (!content.isEmpty()) {
                sendMessage(content, "");
                messageField.setText("");
            }
        });

        inputPanel.add(attachButton, BorderLayout.WEST);
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
    }



    private JLabel createCenteredLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JLabel createCenteredLabel(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImg);

        JLabel label = new JLabel(scaledIcon);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JPanel createMessageBubble(ChatMessage message) {
        JPanel bubblePanel = new JPanel();
        bubblePanel.setLayout(new BoxLayout(bubblePanel, BoxLayout.Y_AXIS));
        bubblePanel.setOpaque(false);
        bubblePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        boolean isSentByMe = message.getSenderId() == currentUser.getUid();

        // 메시지 라벨
        JLabel messageLabel = new JLabel(message.getMessage());
        messageLabel.setOpaque(true);
        messageLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        messageLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        messageLabel.setBackground(isSentByMe ? Color.CYAN : Color.LIGHT_GRAY);
        messageLabel.setAlignmentX(isSentByMe ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        // 타임스탬프 라벨
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        String formattedTime = timeFormat.format(new Date(message.getCreatedAt().getTime()));
        JLabel timestampLabel = new JLabel(formattedTime);
        timestampLabel.setFont(new Font("맑은 고딕", Font.ITALIC, 10));
        timestampLabel.setForeground(Color.GRAY);
        timestampLabel.setAlignmentX(isSentByMe ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        // 이미지가 포함된 경우
        JLabel imageLabel = null;
        if (message.getFilePath() != null && !message.getFilePath().isEmpty()) {
            imageLabel = createImageLabel(message.getFilePath());
            if (imageLabel != null) {
                imageLabel.setAlignmentX(isSentByMe ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
            }
        }

        // 메시지와 타임스탬프 순서 정렬
        if (isSentByMe) {
            bubblePanel.add(Box.createVerticalStrut(5));
            bubblePanel.add(messageLabel);
            bubblePanel.add(timestampLabel);
            if (imageLabel != null) {
                bubblePanel.add(Box.createVerticalStrut(5));
                bubblePanel.add(imageLabel);
            }
        } else {
            bubblePanel.add(messageLabel);
            bubblePanel.add(timestampLabel);
            if (imageLabel != null) {
                bubblePanel.add(Box.createVerticalStrut(5));
                bubblePanel.add(imageLabel);
            }
        }

        return bubblePanel;
    }



    private JLabel createImageLabel(String filePath) {
        try {
            ImageIcon icon = new ImageIcon(new ImageIcon(filePath).getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH));
            JLabel imageLabel = new JLabel(icon);
            imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // 이미지 클릭 시 풀스크린 창 열기
            imageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    openImageFullScreen(filePath);
                }
            });

            return imageLabel;
        } catch (Exception e) {
            return new JLabel("Image not found");
        }
    }

    private void openImageFullScreen(String filePath) {
        // JDialog로 풀스크린 창 생성
        JDialog fullScreenDialog = new JDialog();
        fullScreenDialog.setLayout(new BorderLayout());
        fullScreenDialog.setTitle("Image Viewer");

        // 이미지 로드
        try {
            ImageIcon fullScreenIcon = new ImageIcon(new ImageIcon(filePath).getImage().getScaledInstance(600, 600, Image.SCALE_SMOOTH));
            JLabel fullScreenImageLabel = new JLabel(fullScreenIcon);
            fullScreenImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            fullScreenDialog.add(fullScreenImageLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load the image.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Save 버튼 생성
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveImage(filePath));
        saveButton.setFocusPainted(false);
        saveButton.setBackground(Color.GREEN);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        // 닫기 버튼 생성
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> fullScreenDialog.dispose());
        closeButton.setFocusPainted(false);
        closeButton.setBackground(Color.RED);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        // 버튼 패널 생성
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);

        fullScreenDialog.add(buttonPanel, BorderLayout.SOUTH);

        // 풀스크린 창 크기와 표시 설정
        fullScreenDialog.setSize(800, 800);
        fullScreenDialog.setModal(true);
        fullScreenDialog.setLocationRelativeTo(null);
        fullScreenDialog.setVisible(true);
    }

    private void saveImage(String filePath) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Image");
        fileChooser.setSelectedFile(new File(new File(filePath).getName()));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File destinationFile = fileChooser.getSelectedFile();
            try {
                Files.copy(new File(filePath).toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "Image saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to save the image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }





    private void refreshMessages() {
        messagesPanel.removeAll();

        for (ChatMessage message : chats) {
            messagesPanel.add(createMessageBubble(message));
            messagesPanel.add(Box.createVerticalStrut(10));
        }

        messagesPanel.revalidate();
        messagesPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane) messagesPanel.getParent().getParent()).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }


    private void sendMessage(String content, String filePath) {
        if (filePath == null) {
            filePath = "";
        }
        // 새 메시지를 생성
        ChatMessage newMessage = new ChatMessage(
                -1,
                currentUser.getUid(),
                currentUser.getUsername(),
                partner.getUid(),
                content,
                new java.sql.Timestamp(System.currentTimeMillis()),
                false,
                filePath
        );

        // 서버로 메시지 전송
        boolean success = new DatabaseServer().sendMessageWithFile(
                currentUser.getUid(),
                partner.getUid(),
                content,
                filePath
        );

        if (success) {
            // 서버 전송 성공 시 리스트에 추가하고 UI 갱신
            chats.add(newMessage);
            messagesPanel.add(createMessageBubble(newMessage));
            messagesPanel.add(Box.createVerticalStrut(10));
            messagesPanel.revalidate();
            messagesPanel.repaint();

            // 자동 스크롤: 메시지가 화면 이상일 때만
            SwingUtilities.invokeLater(() -> {
                JScrollPane parentScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, messagesPanel);
                if (parentScrollPane != null) {
                    JScrollBar verticalBar = parentScrollPane.getVerticalScrollBar();
                    verticalBar.setValue(verticalBar.getMaximum());
                }
            });
        } else {
            JOptionPane.showMessageDialog(this, "Failed to send the message.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteChat() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this chat?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            DatabaseServer db = new DatabaseServer();
            boolean success = db.deleteChatsWithPartner(currentUser.getUid(), partner.getUid());
            if (success) {
                JOptionPane.showMessageDialog(this, "Chat deleted successfully!");
                // Navigate back to MessagePanel
                if (mainPanel.getLayout() instanceof CardLayout) {
                    // Ensure MessagePanel is added to mainPanel
                    MessagePanel messagePanel = new MessagePanel();
                    mainPanel.add(messagePanel, "MessagePanel");

                    // Switch to MessagePanel
                    ((CardLayout) mainPanel.getLayout()).show(mainPanel, "MessagePanel");
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid main panel configuration.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete chat",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }



}
