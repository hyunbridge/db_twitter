package kr.ac.gachon.twitter;

import javax.swing.*;
import java.sql.*;
import java.util.Scanner;

public class PostService {
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/twitter", "root", "pw");
    }

    // PostService.java
    public void openWritePostGUI(User user) {
        SwingUtilities.invokeLater(() -> {
            new WritePostGUI(user.getUid());
        });
    }

    public void likePost(Scanner scanner, String userId) {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);

            System.out.println("Enter post ID to like:");
            String postId = scanner.next();

            String checkSql = "SELECT liker_id FROM post_like WHERE liker_id = ? AND post_id = ?";
            try (PreparedStatement checkStmt = con.prepareStatement(checkSql)) {
                checkStmt.setString(1, userId);
                checkStmt.setString(2, postId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    System.out.println("Already liked this post. Please try again!");
                    return;
                }
            }

            String insertSql = "INSERT INTO post_like (liker_id, post_id) VALUES (?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(insertSql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, postId);
                pstmt.executeUpdate();
            }

            String updateSql = "UPDATE posts SET num_of_likes = num_of_likes + 1 WHERE post_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateSql)) {
                pstmt.setString(1, postId);
                pstmt.executeUpdate();
            }

            con.commit();
            System.out.println("Post liked successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException closeException) {
                    closeException.printStackTrace();
                }
            }
        }
    }

    public void writeComment(Scanner scanner, String userId) {
        try (Connection con = getConnection()) {
            System.out.println("Enter the post ID to comment on:");
            String postId = scanner.next();
            scanner.nextLine(); // Consume leftover newline

            System.out.println("Enter your comment:");
            String content = scanner.nextLine();

            // Generate comment_id based on the current count in the comment table
            String idQuery = "SELECT COUNT(*) FROM comment";
            Statement idStmt = con.createStatement();
            ResultSet rs = idStmt.executeQuery(idQuery);
            String commentId = "c1";
            if (rs.next()) {
                commentId = "c" + (rs.getInt(1) + 1);
            }

            // Set current time for createAtTime and modeifiedAtTime
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());

            // Insert comment into the comment table
            String sql = "INSERT INTO comment (comment_id, writer_id, post_id, content, num_of_likes, createAtTime, modeifiedAtTime) VALUES (?, ?, ?, ?, 0, ?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, commentId);         // comment_id
                pstmt.setString(2, userId);            // writer_id
                pstmt.setString(3, postId);            // post_id
                pstmt.setString(4, content);           // content
                pstmt.setTimestamp(5, currentTime);    // createAtTime
                pstmt.setTimestamp(6, currentTime);    // modeifiedAtTime
                pstmt.executeUpdate();
                System.out.println("Comment added successfully to post " + postId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void likeComment(Scanner scanner, String userId) {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);

            System.out.println("Enter the comment ID to like:");
            String commentId = scanner.next();

            String checkSql = "SELECT * FROM comment_like WHERE comment_id = ? AND liker_id = ?";
            try (PreparedStatement checkStmt = con.prepareStatement(checkSql)) {
                checkStmt.setString(1, commentId);
                checkStmt.setString(2, userId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    System.out.println("You have already liked this comment!");
                    return;
                }
            }

            String insertSql = "INSERT INTO comment_like (comment_id, liker_id) VALUES (?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(insertSql)) {
                pstmt.setString(1, commentId);
                pstmt.setString(2, userId);
                pstmt.executeUpdate();
            }


            String updateSql = "UPDATE comment SET num_of_likes = num_of_likes + 1 WHERE comment_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateSql)) {
                pstmt.setString(1, commentId);
                pstmt.executeUpdate();
            }

            con.commit();
            System.out.println("Comment liked successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException closeException) {
                    closeException.printStackTrace();
                }
            }
        }
    }

    public void writeReply(Scanner scanner, String userId) {
        try (Connection con = getConnection()) {
            System.out.println("Enter the comment ID to reply to:");
            String commentId = scanner.next();
            scanner.nextLine(); // Consume leftover newline

            System.out.println("Enter your reply:");
            String content = scanner.nextLine();

            // Generate reply_id based on the current count in the reply table
            String idQuery = "SELECT COUNT(*) FROM reply";
            Statement idStmt = con.createStatement();
            ResultSet rs = idStmt.executeQuery(idQuery);
            String replyId = "r1";
            if (rs.next()) {
                replyId = "r" + (rs.getInt(1) + 1);
            }

            // Set current time for createAtTime and modeifiedAtTime
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());

            // Insert reply into the reply table
            String sql = "INSERT INTO reply (reply_id, writer_id, comment_id, content, num_of_likes, createAtTime, modeifiedAtTime) VALUES (?, ?, ?, ?, 0, ?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, replyId);           // reply_id
                pstmt.setString(2, userId);            // writer_id
                pstmt.setString(3, commentId);         // comment_id
                pstmt.setString(4, content);           // content
                pstmt.setTimestamp(5, currentTime);    // createAtTime
                pstmt.setTimestamp(6, currentTime);    // modeifiedAtTime
                pstmt.executeUpdate();
                System.out.println("Reply added successfully to comment " + commentId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void likeReply(Scanner scanner, String userId) {
        try (Connection con = getConnection()) {
            System.out.println("Enter the reply ID to like:");
            String replyId = scanner.next();

            // 중복 좋아요 확인 (사용자가 이미 좋아요한 경우 확인)
            String checkSql = "SELECT * FROM reply_like WHERE reply_id = ? AND liker_id = ?";
            try (PreparedStatement checkStmt = con.prepareStatement(checkSql)) {
                checkStmt.setString(1, replyId);
                checkStmt.setString(2, userId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    System.out.println("You have already liked this reply!");
                    return;
                }
            }

            // Generate replylike_id based on the current count in the reply_like table
            String idQuery = "SELECT COUNT(*) FROM reply_like";
            Statement idStmt = con.createStatement();
            ResultSet rs = idStmt.executeQuery(idQuery);
            String replyLikeId = "rl1";
            if (rs.next()) {
                replyLikeId = "rl" + (rs.getInt(1) + 1);
            }

            // Insert like into the reply_like table
            String sql = "INSERT INTO reply_like (replylike_id, reply_id, liker_id) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, replyLikeId);   // replylike_id
                pstmt.setString(2, replyId);       // reply_id
                pstmt.setString(3, userId);        // liker_id
                pstmt.executeUpdate();
                System.out.println("Reply liked successfully!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void scrapPost(Scanner scanner, String userId) {
        try (Connection con = getConnection()) {
            System.out.println("Enter the post ID to scrap:");
            String postId = scanner.next();

            // 중복 스크랩 확인 (사용자가 이미 스크랩한 경우 확인)
            String checkSql = "SELECT * FROM scrap WHERE post_id = ? AND user_id = ?";
            try (PreparedStatement checkStmt = con.prepareStatement(checkSql)) {
                checkStmt.setString(1, postId);
                checkStmt.setString(2, userId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    System.out.println("You have already scrapped this post!");
                    return;
                }
            }

            // Generate scrap_id based on the current count in the scrap table
            String idQuery = "SELECT COUNT(*) FROM scrap";
            Statement idStmt = con.createStatement();
            ResultSet rs = idStmt.executeQuery(idQuery);
            String scrapId = "s1";
            if (rs.next()) {
                scrapId = "s" + (rs.getInt(1) + 1);
            }

            // Set current time for scrappedAt
            Timestamp scrappedAt = new Timestamp(System.currentTimeMillis());

            // Insert scrap into the scrap table
            String sql = "INSERT INTO scrap (scrap_id, user_id, post_id, scrappedAt) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, scrapId);       // scrap_id
                pstmt.setString(2, userId);        // user_id
                pstmt.setString(3, postId);        // post_id
                pstmt.setTimestamp(4, scrappedAt); // scrappedAt
                pstmt.executeUpdate();
                System.out.println("Post scrapped successfully!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
