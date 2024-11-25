import java.sql.Timestamp;

public class User {
    private long uid;
    private String username;
    private String password;
    private String bio;
    private String email;
    private int followerCnt;
    private int followingCnt;
    private Timestamp createdAt;

    public User(long uid, String username, String password, String bio, String email, int followerCnt, int followingCnt, Timestamp createdAt) {
        this.uid = uid;
        this.username = username;
        this.password = password;
        this.bio = bio;
        this.email = email;
        this.followerCnt = followerCnt;
        this.followingCnt = followingCnt;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public long getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getBio() {
        return bio;
    }

    public String getEmail() {
        return email;
    }

    public int getFollowerCnt() {
        return followerCnt;
    }

    public int getFollowingCnt() {
        return followingCnt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
}
