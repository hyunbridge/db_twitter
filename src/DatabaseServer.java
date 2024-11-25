import java.sql.*;
import java.util.*;

public class DatabaseServer {

    // 데이터베이스 연결 정보
    private static final String DB_URL = "jdbc:mysql://localhost:3306/twitterapp"; // 실제 DB URL로 변경
    private static final String USER = "root"; // 본인 DB 사용자명
    private static final String PASSWORD = "pw";// 본인 DB 비밀번호


    // 데이터베이스 연결 메서드
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    // 사용자 정보 가져오기 (User 테이블에서)
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT uid, username, password, bio, email, followerCnt, followingCnt, createdAt FROM user";

        try (Connection con = connect(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                long uid = rs.getLong("uid");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String bio = rs.getString("bio");
                String email = rs.getString("email");
                int followerCnt = rs.getInt("followerCnt");
                int followingCnt = rs.getInt("followingCnt");
                Timestamp createdAt = rs.getTimestamp("createdAt");

                users.add(new User(uid, username, password, bio, email, followerCnt, followingCnt, createdAt));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // 게시물 정보 가져오기 (Post 테이블에서)
    public List<Post> getPosts() {
        List<Post> posts = new ArrayList<>();
        String query = """
        SELECT 
            post.postId, 
            post.content, 
            post.likedCnt, 
            post.createdAt, 
            post.createdby,
            user.username
        FROM 
            post
        JOIN 
            user
        ON 
            post.createdBy = user.uid
    """;

        try (Connection con = connect(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                long postId = rs.getLong("postId");
                long createdBy = rs.getLong("createdBy");
                String content = rs.getString("content");
                int likedCnt = rs.getInt("likedCnt");
                Timestamp createdAt = rs.getTimestamp("createdAt");
                String username = rs.getString("username");

                posts.add(new Post(postId, createdBy, content, likedCnt, createdAt, username));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    // 댓글 정보 가져오기 (Comment 테이블에서)
    public List<Comment> getComments() {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT commentId, post, parent, createdBy, content, likedCnt, createdAt FROM comment";

        try (Connection con = connect(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                long commentId = rs.getLong("commentId");
                long post = rs.getLong("post");
                long parent = rs.getLong("parent");
                long createdBy = rs.getLong("createdBy");
                String content = rs.getString("content");
                int likedCnt = rs.getInt("likedCnt");
                Timestamp createdAt = rs.getTimestamp("createdAt");

                comments.add(new Comment(commentId, post, parent, createdBy, content, likedCnt, createdAt));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    // 게시물 삽입 메서드 (예시)
    public boolean insertPost(Post post) {
        //public Post(long createdBy, String content, int likedCnt, Timestamp createdAt,
        //        String imagePath, boolean isPublic) {
        String query = "INSERT INTO post (createdBy, content, likedCnt, createdAt, imagePath, isPublic) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = connect(); PreparedStatement pstmt = con.prepareStatement(query)) {
            // PreparedStatement에 파라미터 설정
            pstmt.setLong(1, post.getCreatedBy()); // 작성자 ID
            pstmt.setString(2, post.getContent()); // 게시물 내용
            pstmt.setInt(3, post.getLikedCnt());   // 좋아요 수
            pstmt.setTimestamp(4, post.getCreatedAt()); // 생성일
            pstmt.setString(5, post.getImagePath()); // 이미지 경로
            pstmt.setBoolean(6,post.getIsPublic()); // public인지

            // 쿼리 실행
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // 삽입 성공 시 true 반환
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // 실패 시 false 반환
    }
}
