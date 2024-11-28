package kr.ac.gachon.twitter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SignupPanel extends JPanel {

    private String email;  // 이메일 저장

    public SignupPanel(CardLayout cardLayout, JPanel cardPanel) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // 이메일 입력 필드 및 버튼
        JLabel emailLabel = new JLabel("Enter your email:");
        JTextField emailField = new JTextField(20);
        JButton continueButton = new JButton("Continue");

        // 컴포넌트 배치
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(emailLabel, gbc);
        gbc.gridx = 1;
        add(emailField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(continueButton, gbc);

        // Continue 버튼 클릭 시 이메일 검사
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                email = emailField.getText().trim(); // 이메일 저장

                if (email.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Email field cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // DB 검사 및 이메일 확인
                DatabaseServer db = new DatabaseServer();
                boolean emailExists = db.checkUserEmail(email);

                if (emailExists) {
                    JOptionPane.showMessageDialog(null, "Email already exists. Please use a different email.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Email is available. Proceeding to the next step.");

                    // 이메일이 없으면 다음 단계로 넘어가기 위한 로직
                    showNextStep(cardLayout, cardPanel);
                }
            }
        });
    }

    private void showNextStep(CardLayout cardLayout, JPanel cardPanel) {
        // 다음 단계 UI로 이동하는 로직 구현
        // 예를 들어, 이름과 비밀번호 입력을 받는 새로운 패널을 추가하고 전환하는 방식
        JPanel userDetailsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel nameLabel = new JLabel("Enter your name:");
        JTextField nameField = new JTextField(20);
        JLabel passwordLabel = new JLabel("Enter your password:");
        JPasswordField passwordField = new JPasswordField(20);
        JButton signupButton = new JButton("Sign Up");

        gbc.gridx = 0;
        gbc.gridy = 0;
        userDetailsPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        userDetailsPanel.add(nameField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        userDetailsPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        userDetailsPanel.add(passwordField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        userDetailsPanel.add(signupButton, gbc);

        // Sign Up 버튼 클릭 시 사용자 정보 DB에 추가
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();
                java.sql.Timestamp currentTime = new java.sql.Timestamp(System.currentTimeMillis());

                if (name.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 데이터베이스에 새로운 유저 추가
                User newUser = new User(name, password, email, currentTime);
                DatabaseServer db = new DatabaseServer();
                boolean success = db.addNewUser(newUser);

                if (success) {
                    JOptionPane.showMessageDialog(null, "User created successfully!");
                    // TwitterUI 열기
                    TwitterUI twitterUI = new TwitterUI(newUser);
                    twitterUI.setVisible(true);

                    // 현재 창 닫기
                    ((JFrame) SwingUtilities.getWindowAncestor(SignupPanel.this)).dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Error occurred while creating the user.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 'cardPanel'에 새로운 userDetailsPanel을 추가하고, CardLayout 전환
        cardPanel.add(userDetailsPanel, "UserDetailsPanel");
        cardLayout.show(cardPanel, "UserDetailsPanel");
    }
}
