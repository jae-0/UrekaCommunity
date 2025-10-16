package dto;

import java.time.LocalDateTime;

public class CommentDto {
    private int id;
    private int postId;
    private int userId;
    private Integer parentId;   // 대댓글 아니면 null
    private String content;
    private boolean anonymous;
    private LocalDateTime createdAt;
    private String userNickname;
    private Integer aliasNo;    // 익명일 때만 값이 있고, 아니면 null
    
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }
    
    public Integer getAliasNo() { return aliasNo; }
    public void setAliasNo(Integer aliasNo) { this.aliasNo = aliasNo; }
}
