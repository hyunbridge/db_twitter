package kr.ac.gachon.twitter;

import java.sql.Timestamp;

public class Comment {
    private long commentId;
    private long post;
    private long parent;
    private long createdBy;
    private String content;
    private int likedCnt;
    private Timestamp createdAt;

    public Comment(long commentId, long post, long parent, long createdBy, String content, int likedCnt, Timestamp createdAt) {
        this.commentId = commentId;
        this.post = post;
        this.parent = parent;
        this.createdBy = createdBy;
        this.content = content;
        this.likedCnt = likedCnt;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public long getCommentId() {
        return commentId;
    }

    public long getPost() {
        return post;
    }

    public long getParent() {
        return parent;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public String getContent() {
        return content;
    }

    public int getLikedCnt() {
        return likedCnt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
}
