import java.sql.Timestamp;

public class Post {
    private long postId;
    private long createdBy;
    private String content;
    private int likedCnt;
    private Timestamp createdAt;
    private String username;
    private String imagePath;
    private boolean isPublic;


    public Post(long postId, long createdBy, String content, int likedCnt, Timestamp createdAt, String username) {
        this.postId = postId;
        this.createdBy = createdBy;
        this.content = content;
        this.likedCnt = likedCnt;
        this.createdAt = createdAt;
        this.username = username;
    }
    public Post(long createdBy, String content, int likedCnt, Timestamp createdAt,
                String imagePath, boolean isPublic) {
        this.createdBy = createdBy;
        this.content = content;
        this.likedCnt = likedCnt;
        this.createdAt = createdAt;
        this.imagePath = imagePath;
        this.isPublic = isPublic;
    }

    // Getters and Setters
    public long getPostId() {
        return postId;
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

    public String getUsername() {
        return username;
    }

    public String getImagePath(){
        return imagePath;
    }
    public boolean getIsPublic(){
        return isPublic;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}
