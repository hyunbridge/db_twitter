package kr.ac.gachon.twitter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    public LoginFrame() {
        setTitle("Login");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 메인 패널 (CardLayout을 사용할 패널)
        JPanel cardPanel = new JPanel();
        CardLayout cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        // 로그인 화면 패널
        JPanel loginPanel = new LoginPanel(cardLayout, cardPanel);

        // 회원가입 화면 패널
        JPanel signupPanel = new SignupPanel(cardLayout, cardPanel);

        // CardLayout에 패널 추가
        cardPanel.add(loginPanel, "Login");
        cardPanel.add(signupPanel, "Signup");

        // CardLayout의 기본 화면을 로그인 화면으로 설정
        cardLayout.show(cardPanel, "Login");

        // 메인 프레임에 cardPanel 추가
        add(cardPanel);

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
