package kr.ac.gachon.twitter;

import java.sql.*;
import java.util.*;

public class DatabaseServer {

    // 데이터베이스 연결 정보
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/twitterapp?useUnicode=true&characterEncoding=UTF-8";

    private static final String USER = "root"; // 본인 DB 사용자명
    private static final String PASSWORD = "mshywjmjkv1024@";// 본인 DB 비밀번호


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

    // 이거 username이랑 password 만 비교하기
    public User authenticateUser(String username, String password) {
        String query = "SELECT uid, username, password, bio, email, followerCnt, followingCnt, createdAt " +
                "FROM user WHERE username = ? AND password = ?";
        try (Connection con = connect(); PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long uid = rs.getLong("uid");
                    String bio = rs.getString("bio");
                    String email = rs.getString("email");
                    int followerCnt = rs.getInt("followerCnt");
                    int followingCnt = rs.getInt("followingCnt");
                    Timestamp createdAt = rs.getTimestamp("createdAt");

                    return new User(uid, username, password, bio, email, followerCnt, followingCnt, createdAt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // 로그인 실패 시
    }

    // 게시물 정보 가져오기 (Post 테이블에서)
    public List<Post> getPosts() {
        List<Post> posts = new ArrayList<>();
        String query = """
            SELECT 
                p.postId,
                p.createdBy,
                p.content,
                p.likedCnt,
                p.createdAt,
                p.imagePath,
                p.isPublic
            FROM 
                post p
            ORDER BY 
                p.createdAt DESC
        """;
        
        try (Connection con = connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                long postId = rs.getLong("postId");
                long createdBy = rs.getLong("createdBy");
                String content = rs.getString("content");
                int likedCnt = rs.getInt("likedCnt");
                Timestamp createdAt = rs.getTimestamp("createdAt");
                String imagePath = rs.getString("imagePath");
                boolean isPublic = rs.getBoolean("isPublic");
                
                posts.add(new Post(postId, createdBy, content, likedCnt, createdAt, imagePath, isPublic));
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

    public boolean checkUserEmail(String email) {
        // 이메일 중복을 확인하는 쿼리
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (Connection con = connect(); PreparedStatement stmt = con.prepareStatement(query)) {
            // 이메일 파라미터 설정
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);  // 결과에서 count 값을 얻어옵니다
                    return count > 0;  // 이메일이 존재하면 true 반환
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;  // 이메일이 존재하지 않으면 false 반환
    }

    public boolean addNewUser(User user) {
        String query = "INSERT INTO user (username, password, email, createdAt) VALUES (?, ?, ?, ?)";
        try (Connection con = connect(); PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setTimestamp(4, user.getCreatedAt());  // 현재 시간을 createdAt으 정
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;  // 성공적으로 추가되면 true 반환
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getUsernameById(long userId) {
        String query = "SELECT username FROM user WHERE uid = ?";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown User";
    }

    public User getUserById(String userId) {
        String query = "SELECT uid, username, password, bio, email, followerCnt, followingCnt, createdAt " +
                "FROM user WHERE uid = ?";
        try (Connection con = connect(); 
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getLong("uid"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("bio"),
                        rs.getString("email"),
                        rs.getInt("followerCnt"),
                        rs.getInt("followingCnt"),
                        rs.getTimestamp("createdAt")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByUsername(String username) {
        String query = "SELECT uid, username, password, bio, email, followerCnt, followingCnt, createdAt " +
                "FROM user WHERE username = ?";
        try (Connection con = connect(); 
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getLong("uid"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("bio"),
                        rs.getString("email"),
                        rs.getInt("followerCnt"),
                        rs.getInt("followingCnt"),
                        rs.getTimestamp("createdAt")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean followUser(long followerId, long followingId) {
        String query = "INSERT INTO follow (subject, createdBy, followedAt) VALUES (?, ?, ?)";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, followingId);
            stmt.setLong(2, followerId);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                // 팔로워/팔로잉 카운트 업데이트
                updateFollowCounts(followerId, followingId);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateFollowCounts(long followerId, long followingId) {
        String updateFollowerQuery = "UPDATE user SET followerCnt = followerCnt + 1 WHERE uid = ?";
        String updateFollowingQuery = "UPDATE user SET followingCnt = followingCnt + 1 WHERE uid = ?";
        
        try (Connection con = connect()) {
            try (PreparedStatement followerStmt = con.prepareStatement(updateFollowerQuery);
                 PreparedStatement followingStmt = con.prepareStatement(updateFollowingQuery)) {
                
                followerStmt.setLong(1, followingId);
                followingStmt.setLong(1, followerId);
                
                followerStmt.executeUpdate();
                followingStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addComment(Comment comment) {
        String query = "INSERT INTO comment (post, parent, createdBy, content, likedCnt, createdAt) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, comment.getPost());
            if (comment.getParent() == 0) {
                stmt.setNull(2, java.sql.Types.BIGINT); // parent가 0이면 NULL로 설정
            } else {
                stmt.setLong(2, comment.getParent());
            }
            stmt.setLong(3, comment.getCreatedBy());
            stmt.setString(4, comment.getContent());
            stmt.setInt(5, comment.getLikedCnt());
            stmt.setTimestamp(6, comment.getCreatedAt());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePost(long postId, String newContent) {
        String query = "UPDATE post SET content = ? WHERE postId = ?";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, newContent);
            stmt.setLong(2, postId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePost(long postId) {
        try (Connection con = connect()) {
            con.setAutoCommit(false);
            try {
                // 1. 게시물의 좋아요 삭제
                String deleteLikesQuery = "DELETE FROM postLike WHERE post = ?";
                try (PreparedStatement stmt = con.prepareStatement(deleteLikesQuery)) {
                    stmt.setLong(1, postId);
                    stmt.executeUpdate();
                }

                // 2. 댓글의 좋아요 삭제
                String deleteCommentLikesQuery = """
                    DELETE FROM commentLike 
                    WHERE comment IN (SELECT commentId FROM comment WHERE post = ?)
                    """;
                try (PreparedStatement stmt = con.prepareStatement(deleteCommentLikesQuery)) {
                    stmt.setLong(1, postId);
                    stmt.executeUpdate();
                }

                // 3. 댓글 삭제
                String deleteCommentsQuery = "DELETE FROM comment WHERE post = ?";
                try (PreparedStatement stmt = con.prepareStatement(deleteCommentsQuery)) {
                    stmt.setLong(1, postId);
                    stmt.executeUpdate();
                }

                // 4. 게시물 삭제
                String deletePostQuery = "DELETE FROM post WHERE postId = ?";
                try (PreparedStatement stmt = con.prepareStatement(deletePostQuery)) {
                    stmt.setLong(1, postId);
                    int result = stmt.executeUpdate();
                    
                    if (result > 0) {
                        con.commit();
                        return true;
                    }
                }

                con.rollback();
                return false;
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Post getPostById(long postId) {
        String query = "SELECT * FROM post WHERE postId = ?";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Post(
                        rs.getLong("postId"),
                        rs.getLong("createdBy"),
                        rs.getString("content"),
                        rs.getInt("likedCnt"),
                        rs.getTimestamp("createdAt"),
                        rs.getString("imagePath"),
                        rs.getBoolean("isPublic")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 특정 게시물의 댓글만 가져오는 메서 
    public List<Comment> getCommentsByPostId(long postId) {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT commentId, post, parent, createdBy, content, likedCnt, createdAt " +
                      "FROM comment WHERE post = ? ORDER BY createdAt DESC";

        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(new Comment(
                        rs.getLong("commentId"),
                        rs.getLong("post"),
                        rs.getLong("parent"),
                        rs.getLong("createdBy"),
                        rs.getString("content"),
                        rs.getInt("likedCnt"),
                        rs.getTimestamp("createdAt")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    // 댓글 좋아요 관련 메서드들
    public boolean hasLikedComment(long commentId, long userId) {
        String query = "SELECT COUNT(*) FROM commentLike WHERE comment = ? AND likedBy = ?";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, commentId);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean likeComment(long commentId, long userId) {
        // 트랜잭션 시작 전에 다시 한번 체크
        if (hasLikedComment(commentId, userId)) {
            return false;
        }

        String insertQuery = "INSERT INTO commentLike (comment, likedBy, likedAt) VALUES (?, ?, ?)";
        String updateQuery = "UPDATE comment SET likedCnt = (SELECT COUNT(*) FROM commentLike WHERE comment = ?) WHERE commentId = ?";
        
        try (Connection con = connect()) {
            con.setAutoCommit(false);
            try {
                // commentLike 테이블에 추가
                try (PreparedStatement stmt = con.prepareStatement(insertQuery)) {
                    stmt.setLong(1, commentId);
                    stmt.setLong(2, userId);
                    stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    stmt.executeUpdate();
                }

                // comment 테이블의 likedCnt를 실제 좋아요 수로 업데이트
                try (PreparedStatement stmt = con.prepareStatement(updateQuery)) {
                    stmt.setLong(1, commentId);
                    stmt.setLong(2, commentId);
                    stmt.executeUpdate();
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 게시물 좋아요 관련 메서드들
    public boolean hasLikedPost(long postId, long userId) {
        String query = "SELECT COUNT(*) FROM postLike WHERE post = ? AND likedBy = ?";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, postId);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean likePost(long postId, long userId) {
        // 트랜잭션 시작 전에 다시 한번 체크
        if (hasLikedPost(postId, userId)) {
            return false;
        }

        String insertQuery = "INSERT INTO postLike (post, likedBy, likedAt) VALUES (?, ?, ?)";
        String updateQuery = "UPDATE post SET likedCnt = (SELECT COUNT(*) FROM postLike WHERE post = ?) WHERE postId = ?";
        
        try (Connection con = connect()) {
            con.setAutoCommit(false);
            try {
                // postLike 테이블에 추가
                try (PreparedStatement stmt = con.prepareStatement(insertQuery)) {
                    stmt.setLong(1, postId);
                    stmt.setLong(2, userId);
                    stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    stmt.executeUpdate();
                }

                // post 테이블의 likedCnt를 실제 좋아요 수로 업데이트
                try (PreparedStatement stmt = con.prepareStatement(updateQuery)) {
                    stmt.setLong(1, postId);
                    stmt.setLong(2, postId);
                    stmt.executeUpdate();
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 팔로우 여부 확인 메서드
    public boolean isFollowing(long followerId, long followingId) {
        String query = "SELECT COUNT(*) FROM follow WHERE subject = ? AND createdBy = ?";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, followingId);
            stmt.setLong(2, followerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 언팔로우 메서드
    public boolean unfollowUser(long followerId, long followingId) {
        String query = "DELETE FROM follow WHERE subject = ? AND createdBy = ?";
        try (Connection con = connect()) {
            con.setAutoCommit(false);
            try {
                // follow 테이블에서 삭제
                try (PreparedStatement stmt = con.prepareStatement(query)) {
                    stmt.setLong(1, followingId);
                    stmt.setLong(2, followerId);
                    int result = stmt.executeUpdate();
                    
                    if (result > 0) {
                        // 팔로워/팔로잉 카운트 감소
                        String updateFollowerQuery = "UPDATE user SET followerCnt = followerCnt - 1 WHERE uid = ?";
                        String updateFollowingQuery = "UPDATE user SET followingCnt = followingCnt - 1 WHERE uid = ?";
                        
                        try (PreparedStatement followerStmt = con.prepareStatement(updateFollowerQuery);
                             PreparedStatement followingStmt = con.prepareStatement(updateFollowingQuery)) {
                            
                            followerStmt.setLong(1, followingId);
                            followingStmt.setLong(1, followerId);
                            
                            followerStmt.executeUpdate();
                            followingStmt.executeUpdate();
                        }
                        
                        con.commit();
                        return true;
                    }
                }
                con.rollback();
                return false;
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Post> getPosts(String filter, long userId) {
        List<Post> posts = new ArrayList<>();
        String query = switch (filter) {
            case "Following" -> """
                SELECT 
                    p.postId, p.createdBy, p.content, p.likedCnt, 
                    p.createdAt, p.imagePath, p.isPublic
                FROM post p
                INNER JOIN follow f ON p.createdBy = f.subject
                WHERE f.createdBy = ? 
                AND (p.isPublic = true OR p.createdBy = ? OR EXISTS (
                    SELECT 1 FROM follow f2 
                    WHERE f2.subject = ? 
                    AND f2.createdBy = p.createdBy
                ))
                ORDER BY p.createdAt DESC
                """;
            case "Hot" -> """
                SELECT 
                    p.postId, p.createdBy, p.content, p.likedCnt, 
                    p.createdAt, p.imagePath, p.isPublic
                FROM post p
                WHERE p.isPublic = true 
                OR p.createdBy = ? 
                OR EXISTS (
                    SELECT 1 FROM follow f 
                    WHERE f.subject = ? 
                    AND f.createdBy = p.createdBy
                )
                ORDER BY p.likedCnt DESC, p.createdAt DESC
                """;
            case "Scraped" -> """
                SELECT 
                    p.postId, p.createdBy, p.content, p.likedCnt, 
                    p.createdAt, p.imagePath, p.isPublic
                FROM post p
                INNER JOIN scrap s ON p.postId = s.post
                WHERE s.scrappedBy = ? 
                AND (p.isPublic = true 
                    OR p.createdBy = ? 
                    OR EXISTS (
                        SELECT 1 FROM follow f 
                        WHERE f.subject = ? 
                        AND f.createdBy = p.createdBy
                    ))
                ORDER BY s.scrappedAt DESC
                """;
            default -> """
                SELECT 
                    p.postId, p.createdBy, p.content, p.likedCnt, 
                    p.createdAt, p.imagePath, p.isPublic
                FROM post p
                WHERE p.isPublic = true 
                OR p.createdBy = ? 
                OR EXISTS (
                    SELECT 1 FROM follow f 
                    WHERE f.subject = ? 
                    AND f.createdBy = p.createdBy
                )
                ORDER BY p.createdAt DESC
                """;
        };
        
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            
            switch (filter) {
                case "Following" -> {
                    stmt.setLong(1, userId);
                    stmt.setLong(2, userId);
                    stmt.setLong(3, userId);
                }
                case "Scraped" -> {
                    stmt.setLong(1, userId);
                    stmt.setLong(2, userId);
                    stmt.setLong(3, userId);
                }
                default -> {
                    stmt.setLong(1, userId);
                    stmt.setLong(2, userId);
                }
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(new Post(
                        rs.getLong("postId"),
                        rs.getLong("createdBy"),
                        rs.getString("content"),
                        rs.getInt("likedCnt"),
                        rs.getTimestamp("createdAt"),
                        rs.getString("imagePath"),
                        rs.getBoolean("isPublic")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    // 댓글 삭제
    public boolean deleteComment(long commentId) {
        String query = "DELETE FROM comment WHERE commentId = ?";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, commentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 게시물 좋아요 취소
    public boolean unlikePost(long postId, long userId) {
        String deleteQuery = "DELETE FROM postLike WHERE post = ? AND likedBy = ?";
        String updateQuery = "UPDATE post SET likedCnt = (SELECT COUNT(*) FROM postLike WHERE post = ?) WHERE postId = ?";
        
        try (Connection con = connect()) {
            con.setAutoCommit(false);
            try {
                // postLike 테이블에서 삭제
                try (PreparedStatement stmt = con.prepareStatement(deleteQuery)) {
                    stmt.setLong(1, postId);
                    stmt.setLong(2, userId);
                    stmt.executeUpdate();
                }

                // post 테이블의 likedCnt 업데이트
                try (PreparedStatement stmt = con.prepareStatement(updateQuery)) {
                    stmt.setLong(1, postId);
                    stmt.setLong(2, postId);
                    stmt.executeUpdate();
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 댓글 좋아요 취소
    public boolean unlikeComment(long commentId, long userId) {
        String deleteQuery = "DELETE FROM commentLike WHERE comment = ? AND likedBy = ?";
        String updateQuery = "UPDATE comment SET likedCnt = (SELECT COUNT(*) FROM commentLike WHERE comment = ?) WHERE commentId = ?";
        
        try (Connection con = connect()) {
            con.setAutoCommit(false);
            try {
                // commentLike 테이블에서 삭제
                try (PreparedStatement stmt = con.prepareStatement(deleteQuery)) {
                    stmt.setLong(1, commentId);
                    stmt.setLong(2, userId);
                    stmt.executeUpdate();
                }

                // comment 테이블의 likedCnt 업데이트
                try (PreparedStatement stmt = con.prepareStatement(updateQuery)) {
                    stmt.setLong(1, commentId);
                    stmt.setLong(2, commentId);
                    stmt.executeUpdate();
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 게시물 작성자 확인
    public boolean isPostOwner(long postId, long userId) {
        String query = "SELECT createdBy FROM post WHERE postId = ?";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("createdBy") == userId;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 댓글 작성자 확인
    public boolean isCommentOwner(long commentId, long userId) {
        String query = "SELECT createdBy FROM comment WHERE commentId = ?";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, commentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("createdBy") == userId;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 스크랩 여부 확인
    public boolean hasScraped(long postId, long userId) {
        String query = "SELECT COUNT(*) FROM scrap WHERE post = ? AND scrappedBy = ?";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, postId);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 스크랩 추가
    public boolean addScrap(long postId, long userId) {
        if (hasScraped(postId, userId)) {
            return false;
        }

        String query = "INSERT INTO scrap (post, scrappedBy, scrappedAt) VALUES (?, ?, ?)";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, postId);
            stmt.setLong(2, userId);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 스크랩 취소
    public boolean removeScrap(long postId, long userId) {
        String query = "DELETE FROM scrap WHERE post = ? AND scrappedBy = ?";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, postId);
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 쪽지 보내기
    public boolean sendMessage(long senderId, long receiverId, String content) {
        String query = "INSERT INTO chat (senderId, receiverId, message, createdAt, isRead) VALUES (?, ?, ?, ?, false)";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, senderId);
            stmt.setLong(2, receiverId);
            stmt.setString(3, content);
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addChatPartner(long senderId, long receiverId) {
        String checkExistingChatQuery = """
        SELECT COUNT(*) 
        FROM chat 
        WHERE (senderId = ? AND receiverId = ?) 
        OR (senderId = ? AND receiverId = ?)
    """;

        try (Connection con = connect()) {
            // 기존 대화 여부 확인
            try (PreparedStatement checkStmt = con.prepareStatement(checkExistingChatQuery)) {
                checkStmt.setLong(1, senderId);
                checkStmt.setLong(2, receiverId);
                checkStmt.setLong(3, receiverId);
                checkStmt.setLong(4, senderId);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // 기존 대화가 존재하면 추가 작업 없이 true 반환
                        return true;
                    }
                }
            }

            // 새로운 대화만 생성 (초기 메시지 추가 없음)
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 받은 쪽지 목록 가져오기
    public List<ChatMessage> getReceivedMessages(long userId) {
        List<ChatMessage> messages = new ArrayList<>();
        String query = """
            SELECT c.*, u.username as senderName 
            FROM chat c 
            JOIN user u ON c.senderId = u.uid 
            WHERE c.receiverId = ? 
            ORDER BY c.createdAt DESC
            """;

        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(new ChatMessage(
                        rs.getLong("chatId"),
                        rs.getLong("senderId"),
                        rs.getString("senderName"),
                        rs.getLong("receiverId"),
                        rs.getString("message"),
                        rs.getTimestamp("createdAt"),
                        rs.getBoolean("isRead"),
                            rs.getString("filePath")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    // 보낸 쪽지 목록 가져오기
    public List<ChatMessage> getSentMessages(long userId) {
        List<ChatMessage> messages = new ArrayList<>();
        String query = """
            SELECT c.*, u.username as receiverName 
            FROM chat c 
            JOIN user u ON c.receiverId = u.uid 
            WHERE c.senderId = ? 
            ORDER BY c.createdAt DESC
            """;

        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(new ChatMessage(
                        rs.getLong("chatId"),
                        rs.getLong("senderId"),
                        rs.getString("receiverName"),
                        rs.getLong("receiverId"),
                        rs.getString("message"),
                        rs.getTimestamp("createdAt"),
                        rs.getBoolean("isRead"),
                            rs.getString("filePath")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    // 쪽지 읽음 처리
    public boolean markMessageAsRead(long messageId) {
        String query = "UPDATE chat SET isRead = true WHERE chatId = ?";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, messageId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 안 읽은 쪽지 수 가져오기
    public int getUnreadMessageCount(long userId) {
        String query = "SELECT COUNT(*) FROM chat WHERE receiverId = ? AND isRead = false";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 대화 상대 목록 가져오기
    public List<User> getChatPartners(long userId) {
        List<User> partners = new ArrayList<>();
        String query = """
            SELECT DISTINCT u.* 
            FROM user u 
            JOIN chat c ON (u.uid = c.senderId OR u.uid = c.receiverId)
            WHERE (c.senderId = ? OR c.receiverId = ?) 
            AND u.uid != ?
            ORDER BY (
                SELECT MAX(createdAt) 
                FROM chat 
                WHERE (senderId = ? AND receiverId = u.uid) 
                OR (senderId = u.uid AND receiverId = ?)
            ) DESC
            """;
        
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, userId);
            stmt.setLong(3, userId);
            stmt.setLong(4, userId);
            stmt.setLong(5, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    partners.add(new User(
                        rs.getLong("uid"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("bio"),
                        rs.getString("email"),
                        rs.getInt("followerCnt"),
                        rs.getInt("followingCnt"),
                        rs.getTimestamp("createdAt")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return partners;
    }

    // 특정 사용자와의 대화 내용 가져오기
    public List<ChatMessage> getChatHistory(long userId, long partnerId) {
        List<ChatMessage> messages = new ArrayList<>();
        String query = """
            SELECT c.*, 
                   s.username as senderName,
                   r.username as receiverName
            FROM chat c
            JOIN user s ON c.senderId = s.uid
            JOIN user r ON c.receiverId = r.uid
            WHERE (c.senderId = ? AND c.receiverId = ?)
            OR (c.senderId = ? AND c.receiverId = ?)
            ORDER BY c.createdAt ASC
            """;
        
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, partnerId);
            stmt.setLong(3, partnerId);
            stmt.setLong(4, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(new ChatMessage(
                        rs.getLong("chatId"),
                        rs.getLong("senderId"),
                        rs.getString("senderName"),
                        rs.getLong("receiverId"),
                        rs.getString("message"),
                        rs.getTimestamp("createdAt"),
                        rs.getBoolean("isRead"),
                            rs.getString("filePath")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    // 특정 사용자와의 안 읽은 메시지 수 가져오기
    public int getUnreadMessageCountFromUser(long userId, long partnerId) {
        String query = "SELECT COUNT(*) FROM chat WHERE senderId = ? AND receiverId = ? AND isRead = false";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, partnerId);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean deleteChatsWithPartner(long userId, long partnerId) {
        try (Connection con = connect()) {
            con.setAutoCommit(false); // Begin transaction
            try {
                // 1. Delete the chat messages directly
                String deleteChatsQuery = """
                DELETE FROM chat 
                WHERE (senderId = ? AND receiverId = ?)
                   OR (senderId = ? AND receiverId = ?);
            """;
                try (PreparedStatement stmt = con.prepareStatement(deleteChatsQuery)) {
                    stmt.setLong(1, userId);
                    stmt.setLong(2, partnerId);
                    stmt.setLong(3, partnerId);
                    stmt.setLong(4, userId);
                    int result = stmt.executeUpdate();

                    if (result > 0) {
                        con.commit(); // Commit the transaction
                        return true; // Chats successfully deleted
                    }
                }

                con.rollback(); // Rollback the transaction if no rows were affected
                return false;
            } catch (SQLException e) {
                con.rollback(); // Rollback the transaction in case of an error
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendMessageWithFile(long senderId, long receiverId, String content, String filePath) {
        String query = "INSERT INTO chat (senderId, receiverId, message, filePath, createdAt, isRead) VALUES (?, ?, ?, ?, ?, false)";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, senderId);
            stmt.setLong(2, receiverId);
            stmt.setString(3, content);
            stmt.setString(4, filePath);
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<User> searchUsers(String keyword) {
        List<User> users = new ArrayList<>();
        String query = """
            SELECT uid, username, password, bio, email, followerCnt, followingCnt, createdAt 
            FROM user 
            WHERE username LIKE ? OR bio LIKE ?
            """;
            
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new User(
                        rs.getLong("uid"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("bio"),
                        rs.getString("email"),
                        rs.getInt("followerCnt"),
                        rs.getInt("followingCnt"),
                        rs.getTimestamp("createdAt")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<Post> searchPosts(String keyword, long currentUserId) {
        List<Post> posts = new ArrayList<>();
        String query = """
            SELECT p.* 
            FROM post p
            WHERE (p.content LIKE ? OR EXISTS (
                SELECT 1 FROM user u 
                WHERE u.uid = p.createdBy 
                AND u.username LIKE ?
            ))
            AND (p.isPublic = true 
                OR p.createdBy = ? 
                OR EXISTS (
                    SELECT 1 FROM follow f 
                    WHERE f.subject = ? 
                    AND f.createdBy = p.createdBy
                ))
            ORDER BY p.createdAt DESC
            """;
            
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setLong(3, currentUserId);
            stmt.setLong(4, currentUserId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(new Post(
                        rs.getLong("postId"),
                        rs.getLong("createdBy"),
                        rs.getString("content"),
                        rs.getInt("likedCnt"),
                        rs.getTimestamp("createdAt"),
                        rs.getString("imagePath"),
                        rs.getBoolean("isPublic")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public List<User> getFollowers(long userId) {
        List<User> followers = new ArrayList<>();
        // 팔로워 목록: userId를 팔로우하는 사용자들
        String query = """
            SELECT u.* FROM user u
            JOIN follow f ON u.uid = f.createdBy
            WHERE f.subject = ?
            ORDER BY u.username
        """;
        
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, userId);
            
            // 쿼리 결과 확인을 위한 로그
            System.out.println("Executing followers query for userId: " + userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    followers.add(new User(
                        rs.getLong("uid"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("bio"),
                        rs.getString("email"),
                        rs.getInt("followerCnt"),
                        rs.getInt("followingCnt"),
                        rs.getTimestamp("createdAt")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting followers: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Found " + followers.size() + " followers");
        return followers;
    }

    public List<User> getFollowing(long userId) {
        List<User> following = new ArrayList<>();
        // 팔로잉 목록: userId가 팔로우하는 사용자들
        String query = """
            SELECT u.* FROM user u
            JOIN follow f ON u.uid = f.subject
            WHERE f.createdBy = ?
            ORDER BY u.username
        """;
        
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setLong(1, userId);
            
            // 쿼리 결과 확인을 위한 로그
            System.out.println("Executing following query for userId: " + userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    following.add(new User(
                        rs.getLong("uid"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("bio"),
                        rs.getString("email"),
                        rs.getInt("followerCnt"),
                        rs.getInt("followingCnt"),
                        rs.getTimestamp("createdAt")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting following: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Found " + following.size() + " following");
        return following;
    }

}
