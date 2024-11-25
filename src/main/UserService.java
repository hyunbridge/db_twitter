package org.example;

import java.sql.*;
import java.util.Scanner;

public class UserService {
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/twitter", "root", "pw");
    }

    public void displayAllUsers() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            stmt = con.createStatement();
            String sql = "SELECT * FROM users";
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String id = rs.getString(1);
                String pwd = rs.getString(2);

                if (rs.wasNull()) id = "null";
                if (rs.wasNull()) pwd = "null";

                System.out.printf("%15s %15s\n", id, pwd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null && !stmt.isClosed()) stmt.close();
                if (rs != null && !rs.isClosed()) rs.close();
                if (con != null && !con.isClosed()) con.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    public String logIn(Scanner scanner) {
        try (Connection con = getConnection()) {
            System.out.println("Input userid / password");
            String id = scanner.next();
            String pwd = scanner.next();

            String sql = "SELECT user_id FROM users WHERE user_id = ? AND pwd = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, id);
            stmt.setString(2, pwd);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("Logged in!");
                return id;
            } else {
                System.out.println("Wrong id/password. Please log in again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void signUp(Scanner scanner) {
        try (Connection con = getConnection()) {
            System.out.println("Input new userid / password");
            String id = scanner.next();
            String pwd = scanner.next();

            Statement stmt = con.createStatement();
            String checkSql = "SELECT user_id FROM users WHERE user_id = '" + id + "'";
            ResultSet rs = stmt.executeQuery(checkSql);

            if (rs.next()) {
                System.out.println("User name already exists. Please try again!");
            } else {
                String insertSql = "INSERT INTO users (user_id, pwd) VALUES ('" + id + "', '" + pwd + "')";
                PreparedStatement pstmt = con.prepareStatement(insertSql);
                pstmt.executeUpdate();
                System.out.println("Sign up successful!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void followUser(Scanner scanner, String userId) {
        try (Connection con = getConnection()) {
            System.out.println("Input user ID to follow:");
            String followId = scanner.next();

            if (followId.equals(userId)) {
                System.out.println("Can't follow yourself.");
                return;
            }

            String checkSql = "SELECT follower_id FROM follower WHERE user_id = ? AND follower_id = ?";
            try (PreparedStatement checkStmt = con.prepareStatement(checkSql)) {
                checkStmt.setString(1, userId);
                checkStmt.setString(2, followId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    System.out.println("Already following this user. Please try again!");
                    return;
                }
            }

            String insertSql = "INSERT INTO follower (user_id, follower_id) VALUES (?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(insertSql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, followId);
                pstmt.executeUpdate();
                System.out.println("Successfully followed the user!");
            }

            String updateCountSql = "UPDATE users SET followerCount = COALESCE(followerCount, 0) + 1 WHERE user_id = ?";
            try (PreparedStatement updateCountStmt = con.prepareStatement(updateCountSql)) {
                updateCountStmt.setString(1, userId);
                updateCountStmt.executeUpdate();
                System.out.println("Updated follower count for " + userId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Scanner scanner, String senderId) {
        try (Connection con = getConnection()) {
            System.out.println("Enter the receiver ID:");
            String receiverId = scanner.next();

            System.out.println("Enter your message content:");
            scanner.nextLine();  // Consume leftover newline
            String content = scanner.nextLine();

            String idQuery = "SELECT COUNT(*) FROM message";
            Statement idStmt = con.createStatement();
            ResultSet rs = idStmt.executeQuery(idQuery);
            String messageId = "m1";
            if (rs.next()) {
                messageId = "m" + (rs.getInt(1) + 1);
            }

            Timestamp sentAt = new Timestamp(System.currentTimeMillis());

            String sql = "INSERT INTO message (message_id, sender_id, receiver_id, content, sentAt) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, messageId);
                pstmt.setString(2, senderId);
                pstmt.setString(3, receiverId);
                pstmt.setString(4, content);
                pstmt.setTimestamp(5, sentAt);
                pstmt.executeUpdate();
                System.out.println("Message sent successfully!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}