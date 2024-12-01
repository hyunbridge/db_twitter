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
            profileImagePath = "images/profile_default.jpg";
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
        JButton deleteButton = deleteChatButton("images/delete.png");

        // *** MODIFIED: Delete button alignment ***
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add deleteButton to header panel
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(deleteButton);


        // *** MODIFIED: Added delete button to header panel ***
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(deleteButton);

        add(headerPanel, BorderLayout.NORTH);

        // 메시지와 날짜를 포함할 레이어드 패널 생성
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(new BorderLayout());
        add(layeredPane, BorderLayout.CENTER);

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

                    // 서버에 메시지 전송
                    boolean success = new DatabaseServer().sendMessageWithFile(currentUser.getUid(), partner.getUid(), fileName, filePath);
                    if (success) {
                        // 성공 시 UI에만 메시지 추가
                        ChatMessage newMessage = new ChatMessage(
                                -1,
                                currentUser.getUid(),
                                currentUser.getUsername(),
                                partner.getUid(),
                                fileName,
                                new java.sql.Timestamp(System.currentTimeMillis()),
                                false,
                                filePath // 전달받은 filePath 그대로 사용
                        );

                        messagesPanel.add(createMessageBubble(newMessage));
                        messagesPanel.add(Box.createVerticalStrut(10));
                        messagesPanel.revalidate();
                        messagesPanel.repaint();

                        // 자동 스크롤
                        SwingUtilities.invokeLater(() -> {
                            JScrollPane parentScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, messagesPanel);
                            if (parentScrollPane != null) {
                                JScrollBar verticalBar = parentScrollPane.getVerticalScrollBar();
                                verticalBar.setValue(verticalBar.getMaximum());
                            }
                        });

                        // 입력창 초기화
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

        // 메시지 내용 또는 이미지 표시
        JLabel contentLabel = (message.getFilePath() != null && !message.getFilePath().isEmpty())
                ? createImageLabel(message.getFilePath())
                : new JLabel(message.getMessage());

        if (contentLabel != null) {
            contentLabel.setOpaque(message.getFilePath() == null || message.getFilePath().isEmpty()); // 텍스트일 때만 배경색
            contentLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            contentLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            contentLabel.setBackground(isSentByMe ? Color.CYAN : Color.LIGHT_GRAY); // 보낸 메시지는 CYAN, 받은 메시지는 LIGHT_GRAY

        }

        // 메시지 라벨
        // JLabel messageLabel = new JLabel(message.getMessage());
        // messageLabel.setOpaque(true);
       //  messageLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        // messageLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        // messageLabel.setBackground(isSentByMe ? Color.CYAN : Color.LIGHT_GRAY);


        // 타임스탬프 라벨
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        String formattedTime = timeFormat.format(new Date(message.getCreatedAt().getTime()));
        JLabel timestampLabel = new JLabel(formattedTime);
        timestampLabel.setFont(new Font("맑은 고딕", Font.ITALIC, 10));
        timestampLabel.setForeground(Color.GRAY);

        // 내부 정렬을 위한 래퍼 패널 생성
        JPanel wrapperPanel = new JPanel();
        wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.Y_AXIS));
        wrapperPanel.setOpaque(false);

        wrapperPanel.add(contentLabel);
        wrapperPanel.add(timestampLabel);

        // 메시지와 타임스탬프를 정렬할 패널 생성
        JPanel messageWithTimestampPanel = new JPanel(new BorderLayout());
        messageWithTimestampPanel.setOpaque(false);


        // 외부 정렬을 위한 패널 생성
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BorderLayout());
        outerPanel.setOpaque(false);

        if (isSentByMe) {
            outerPanel.add(wrapperPanel, BorderLayout.EAST);
        } else {
            outerPanel.add(wrapperPanel, BorderLayout.WEST);
        }

        bubblePanel.add(outerPanel);


        return bubblePanel;
    }


    private JButton deleteChatButton(String filePath) {
        try {
            // Load and resize the image
            ImageIcon icon = new ImageIcon(new ImageIcon(filePath).getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));

            // Create the button
            JButton imageButton = new JButton(icon);

            // Remove button styles to make it look like an image
            imageButton.setBorderPainted(false);
            imageButton.setContentAreaFilled(false);
            imageButton.setFocusPainted(false);

            // Add action listener to delete the chat and navigate
            imageButton.addActionListener(e -> {
                deleteChat(); // Perform deletion
                cardLayout.show(mainPanel, "ChatList");
            });

            return imageButton;
        } catch (Exception e) {
            // Fallback button in case of image loading failure
            JButton placeholderButton = new JButton("Delete");
            placeholderButton.setEnabled(false); // Disable the button
            return placeholderButton;
        }
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

        // Save 버튼 대신 이미지 버튼 생성
        JButton saveImageButton = createImageButton("images/Download@2x.png");

        // 닫기 버튼 생성
        // JButton closeButton = backButton("C:/Users/gram/Downloads/X.png");

        // 버튼 패널 생성
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(saveImageButton);
        // buttonPanel.add(closeButton);

        fullScreenDialog.add(buttonPanel, BorderLayout.SOUTH);

        // 풀스크린 창 크기와 표시 설정
        fullScreenDialog.setSize(800, 800);
        fullScreenDialog.setModal(true);
        fullScreenDialog.setLocationRelativeTo(null);
        fullScreenDialog.setVisible(true);
    }

    private JButton createImageButton(String filePath) {
        try {
            // 이미지 로드 및 크기 조정
            ImageIcon icon = new ImageIcon(new ImageIcon(filePath).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));

            // 버튼 생성
            JButton imageButton = new JButton(icon);

            // 버튼 스타일 제거 (이미지처럼 보이게 설정)
            imageButton.setBorderPainted(false);    // 외곽선 제거
            imageButton.setContentAreaFilled(false); // 배경 제거
            imageButton.setFocusPainted(false);     // 포커스 표시 제거

            // 클릭 이벤트 추가
            imageButton.addActionListener(e -> {
                saveImage(filePath); // 저장 동작 수행
            });

            return imageButton;
        } catch (Exception e) {
            // 이미지가 없는 경우 대체 텍스트 버튼 생성
            JButton placeholderButton = new JButton("Image not found");
            placeholderButton.setEnabled(false); // 클릭 불가능
            return placeholderButton;
        }
    }

    // 기능은 저장 기능임
    /* private JButton backButton(String filePath) {
        try {
            // 이미지 로드 및 크기 조정
            ImageIcon icon = new ImageIcon(new ImageIcon(filePath).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));

            // 버튼 생성
            JButton imageButton = new JButton(icon);

            // 버튼 스타일 제거 (이미지처럼 보이게 설정)
            imageButton.setBorderPainted(false);
            imageButton.setContentAreaFilled(false);
            imageButton.setFocusPainted(false);

            // 클릭 이벤트 추가
            imageButton.addActionListener(e -> {
                saveImage(filePath);
            });

            return imageButton;
        } catch (Exception e) {
            // 이미지가 없는 경우 대체 텍스트 버튼 생성
            JButton placeholderButton = new JButton("Image not found");
            placeholderButton.setEnabled(false);
            return placeholderButton;
        }
    } */


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

        // 서버로 메시지 전송
        boolean success = new DatabaseServer().sendMessageWithFile(
                currentUser.getUid(),
                partner.getUid(),
                content,
                filePath
        );

        if (success) {
            // 서버 전송 성공 시 리스트에 추가하고 UI 갱신
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
