package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jdbc.DBManager;

/*  [구현된 기능]
	1. 게시글 생성
	2. 게시판별로 게시글 모아보기
	3. 작성자 본인일 때 글 수정 (성공 시 1 이상 반환)
	4. 작성자 본인일 때 글 삭제 (true = 삭제됨)
	5. 단어에 맞는 검색 기능
	
	[활용할 수 있는 기능]
	1. 좋아요 기능을 추가하여 일일, 주간 베스트 게시글을 모아보기
*/

public class PostDao {
	/* 게시글 생성 */
    public static long insertPost(int boardId, long authorId, String title, String content, boolean isAnonymous) throws Exception {
        String sql = "INSERT INTO posts (board_id, author_id, title, content, is_anonymous) VALUES (?,?,?,?,?)";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, boardId);
            ps.setLong(2, authorId);
            ps.setString(3, title);
            ps.setString(4, content);
            ps.setBoolean(5, isAnonymous);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
                throw new SQLException("Generated key not returned");
            }
        }
    }

    /* 게시판별 최신순 목록 (작성자 닉네임 포함, 댓글수 포함) */
    public static List<PostRow> listByBoard(int boardId, int limit, int offset) throws Exception {
        String sql = """
            SELECT p.id, p.author_id, u.nickname,
                   p.title, p.content, p.is_anonymous, p.created_at,
                   (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) AS comment_count
            FROM posts p
            JOIN users u ON u.id = p.author_id
            WHERE p.board_id = ?
            ORDER BY p.id DESC
            LIMIT ? OFFSET ?
        """;
        List<PostRow> out = new ArrayList<>();
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, boardId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PostRow r = new PostRow();
                    r.id = rs.getInt("id");
                    r.authorId = rs.getInt("author_id");
                    r.nickname = rs.getString("nickname");
                    r.title = rs.getString("title");
                    r.content = rs.getString("content");
                    r.isAnonymous = rs.getBoolean("is_anonymous");
                    r.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    r.commentCount = rs.getInt("comment_count");
                    out.add(r);
                }
            }
        }
        return out;
    }
    
    /* 작성자 본인일 때만 글 수정 (성공 시 1 이상 반환) */
    public static int updatePost(int postId, long authorId,
                                 String title, String content, boolean isAnonymous) throws Exception {
        String sql = """
            UPDATE posts
               SET title = ?, content = ?, is_anonymous = ?, updated_at = NOW()
             WHERE id = ? AND author_id = ?
        """;
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setBoolean(3, isAnonymous);
            ps.setInt(4, postId);
            ps.setLong(5, authorId);
            return ps.executeUpdate(); // 0이면 권한 없음/존재X
        }
    }

    /* 작성자 본인일 때만 글 삭제 (true = 삭제됨) */
    public static boolean deletePost(int postId, long authorId) throws Exception {
        String sql = "DELETE FROM posts WHERE id = ? AND author_id = ?";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setLong(2, authorId);
            return ps.executeUpdate() > 0;
        }
    }
    
    /* 단어에 맞게 검색 */
    public static List<PostRow> searchByBoardTokens(
            int boardId, List<String> tokens, int limit, int offset) throws Exception {

        if (tokens == null || tokens.isEmpty()) return List.of();

        StringBuilder sql = new StringBuilder("""
          SELECT p.id, p.author_id, p.title, p.content, p.is_anonymous, p.created_at,
                 COALESCE(c.cnt,0) AS comment_count, u.nickname
          FROM posts p
          LEFT JOIN users u ON u.id = p.author_id
          LEFT JOIN (SELECT post_id, COUNT(*) cnt FROM comments GROUP BY post_id) c ON c.post_id = p.id
          WHERE p.board_id = ?
        """);

        // 각 토큰은 제목 또는 본문에 포함 (AND)
        for (int i = 0; i < tokens.size(); i++) {
            sql.append(" AND (p.title LIKE ? ESCAPE '\\\\' OR p.content LIKE ? ESCAPE '\\\\')");
        }
        sql.append(" ORDER BY p.created_at DESC LIMIT ? OFFSET ?");

        try (Connection con = jdbc.DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setInt(idx++, boardId);
            for (String t : tokens) {
                String like = "%" + escapeLike(t) + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            ps.setInt(idx++, limit);
            ps.setInt(idx,   offset);

            try (ResultSet rs = ps.executeQuery()) {
                var out = new java.util.ArrayList<PostRow>();
                while (rs.next()) {
                    var r = new PostRow();
                    r.id = rs.getInt("id");
                    r.authorId = rs.getInt("author_id");
                    r.title = rs.getString("title");
                    r.content = rs.getString("content");
                    r.isAnonymous = rs.getBoolean("is_anonymous");
                    r.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    r.commentCount = rs.getInt("comment_count");
                    r.nickname = rs.getString("nickname");
                    out.add(r);
                }
                return out;
            }
        }
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
    


	public static class Row {
	    public final int id, boardId, authorId, commentCount;
	    public final String boardName, title, content, nickname;
	    public final boolean isAnonymous;
	    public final java.time.LocalDateTime createdAt;
	    public Row(int id, int boardId, String boardName, int authorId, String title, String content,
	               boolean isAnonymous, String nickname, java.time.LocalDateTime createdAt, int commentCount) {
	         this.id=id; this.boardId=boardId; this.boardName=boardName; this.authorId=authorId;
	         this.title=title; this.content=content; this.isAnonymous=isAnonymous; this.nickname=nickname;
	         this.createdAt=createdAt; this.commentCount=commentCount;
	    }
	}

    // 목록 DTO
    public static class PostRow {
        public int id, authorId, commentCount;
        public String title, content, nickname;
        public boolean isAnonymous;
        public LocalDateTime createdAt;
    }
}
