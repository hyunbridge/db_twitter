package kr.ac.gachon.twitter;

import javax.swing.*;
import java.awt.*;

public class SearchPanel extends JPanel {
    public SearchPanel() {
        setLayout(new BorderLayout());

        // 여기에 검색 화면을 구성할 내용 추가
        JLabel searchLabel = new JLabel("Search results will be displayed here");
        searchLabel.setHorizontalAlignment(SwingConstants.CENTER);
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        // 검색 내용 레이블 추가
        add(searchLabel, BorderLayout.CENTER);
    }
}
