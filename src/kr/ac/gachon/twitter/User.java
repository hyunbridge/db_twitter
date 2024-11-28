package kr.ac.gachon.twitter;

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
    private String backgroundImage;
    private String profileImage;

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

    public User(String username, String password, String email, Timestamp createdAt){
        this.username = username;
        this.password = password;
        this.email = email;
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

    public String getProfileImage() {
        return profileImage; // `profileImage`가 null일 수 있으므로 체크 필요
    }
}
