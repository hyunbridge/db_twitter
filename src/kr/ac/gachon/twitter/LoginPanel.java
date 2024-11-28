package kr.ac.gachon.twitter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {

    public LoginPanel(CardLayout cardLayout, JPanel cardPanel) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // 로그인 필드 및 버튼
        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(20);
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(20);
        JButton loginButton = new JButton("Login");

        // 컴포넌트 배치
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(userLabel, gbc);
        gbc.gridx = 1;
        add(userField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passLabel, gbc);
        gbc.gridx = 1;
        add(passField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(loginButton, gbc);

        // "New to Twitter? Create an account" 텍스트 라벨
        JLabel createAccountLabel = new JLabel("<html>New to Twitter? <a href='#'>Create an account</a></html>");
        createAccountLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));  // 커서가 손 모양으로 바뀌게 함
        createAccountLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Create an account 링크 클릭 시 회원가입 화면으로 전환
                cardLayout.show(cardPanel, "Signup");
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(createAccountLabel, gbc);

        // 로그인 버튼 액션
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userField.getText();
                String password = new String(passField.getPassword());

                DatabaseServer db = new DatabaseServer();
                User loggedInUser = db.authenticateUser(username, password);

                if (loggedInUser != null) {
                    JOptionPane.showMessageDialog(null, "Login Successful!");

                    // 로그인 후 TwitterUI 창 열기
                    TwitterUI twitterUI = new TwitterUI(loggedInUser);
                    twitterUI.setVisible(true);
                    // 로그인 창을 닫음
                    ((JFrame) SwingUtilities.getWindowAncestor(LoginPanel.this)).dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
