package dao;

import jdbc.DBManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*  [구현된 기능]
	1. 내가 쓴 글 모아보기
	2. 내가 댓글 단 글 모아보기
	
	[활용할 수 있는 기능]
	1. 좋아요 기능을 추가하여 일일, 주간 베스트 게시글을 모아보기
*/

public class MyActivityDao {
    public static class Row {
        public long id;
        public long authorId;
        public String title;
        public String content;
        public boolean isAnonymous;
        public String nickname;
        public LocalDateTime createdAt;
        public int commentCount;
        public int boardId;
        public String boardName;
    }

    /* 내가 쓴 글 */
    public static List<Row> listWrittenByUser(int userId, int limit, int offset) throws Exception {
        String sql = """
            SELECT p.id, p.author_id, p.title, p.content, p.is_anonymous, p.created_at,
				   u.nickname AS nickname, p.board_id, b.name AS board_name,
				   (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) AS comment_count
				   FROM posts p
				   JOIN boards b ON b.id = p.board_id
				   JOIN users u  ON u.id = p.author_id
				   WHERE p.author_id = ?
				   ORDER BY p.created_at DESC, p.id DESC
				   LIMIT ? OFFSET ?

        """;
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    /* 내가 댓글 단 글 (중복 제거, 최근 댓글 순) */
    public static List<Row> listCommentedByUser(int userId, int limit, int offset) throws Exception {
        String sql = """
            SELECT p.id, p.author_id, p.title, p.content, p.is_anonymous, p.created_at,
				   u.nickname AS nickname, p.board_id, b.name AS board_name,
				   (SELECT COUNT(*) FROM comments c2 WHERE c2.post_id = p.id) AS comment_count
					FROM posts p
					JOIN boards b ON b.id = p.board_id
					JOIN users u  ON u.id = p.author_id
					JOIN (
					  SELECT c.post_id, MAX(c.created_at) AS last_commented_at
					  FROM comments c
					  WHERE c.user_id = ?
					  GROUP BY c.post_id
					  ORDER BY last_commented_at DESC
					  LIMIT ? OFFSET ?
					) x ON x.post_id = p.id
					ORDER BY x.last_commented_at DESC, p.id DESC
        """;
        
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    private static List<Row> mapRows(ResultSet rs) throws Exception {
        List<Row> out = new ArrayList<>();
        while (rs.next()) {
            Row r = new Row();
            r.id = rs.getLong("id");
            r.authorId = rs.getLong("author_id");
            r.title = rs.getString("title");
            r.content = rs.getString("content");
            r.isAnonymous = rs.getInt("is_anonymous") == 1;
            Timestamp ts = rs.getTimestamp("created_at");
            r.createdAt = ts == null ? null : ts.toLocalDateTime();
            r.nickname = rs.getString("nickname");
            r.commentCount = rs.getInt("comment_count");
            r.boardId = rs.getInt("board_id");
            r.boardName = rs.getString("board_name");
            out.add(r);
        }
        return out;
    }
}
