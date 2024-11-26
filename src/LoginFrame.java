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

        // 메인 패널
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // 사용자 입력 필드
        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(20);

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(20);

        JButton loginButton = new JButton("Login");

        // 컴포넌트 배치
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(passField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(loginButton, gbc);

        add(mainPanel);

        // 로그인 버튼 액션
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 로그인 성공 후 TwitterUI 창 띄우기
                TwitterUI twitterUI = new TwitterUI();
                twitterUI.setVisible(true);
                // 현재 로그인 창 닫기
                dispose();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
