package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import dto.CommentDto;
import jdbc.DBManager;

/*   [구현된 기능]
     1. 게시글의 댓글들을 오래된 시간 순대로 조회
     2. 댓글 등록 (익명이면 anon_index 배정 후 저장)
     3. 자식이 없으면 본인 댓글만 삭제 (영향받은 행 수 반환: 1이면 성공, 0이면 권한 없음/없음)
     4. 본인 댓글 삭제 시 '[삭제된 댓글입니다]' 로 변경
     5. 글의 댓글 개수 반환
     6. 단일 댓글 조회 (수정/권한 확인 등에 사용)
*/
public class CommentDao {

    /* 게시글의 댓글들을 오래된 시간 순대로 조회 */
    public static List<CommentDto> findByPostId(int postId) throws Exception {
        String sql = """
            SELECT c.id, c.post_id, c.user_id, c.parent_id, c.content, c.is_anonymous,
                   c.anon_index, c.created_at,
                   u.nickname AS author_nickname
              FROM comments c
         LEFT JOIN users u ON u.id = c.user_id
             WHERE c.post_id = ?
          ORDER BY c.created_at ASC, c.id ASC
        """;
        List<CommentDto> list = new ArrayList<>();

        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CommentDto c = new CommentDto();
                    c.setId(rs.getInt("id"));
                    c.setPostId(rs.getInt("post_id"));
                    c.setUserId(rs.getInt("user_id"));

                    Object parent = rs.getObject("parent_id");
                    c.setParentId(parent == null ? null : rs.getInt("parent_id"));

                    c.setContent(rs.getString("content"));
                    c.setAnonymous(rs.getInt("is_anonymous") == 1);

                    // anon_index 매핑 (NULL 허용)
                    c.setAliasNo((Integer) rs.getObject("anon_index"));

                    Timestamp ts = rs.getTimestamp("created_at");
                    c.setCreatedAt(ts == null ? null : ts.toLocalDateTime());

                    c.setUserNickname(rs.getString("author_nickname"));

                    list.add(c);
                }
            }
        }
        return list;
    }

    /* 댓글 등록 (대댓글이면 parentId 넣고, 아니면 null) */
    public static int insert(int postId, int userId, Integer parentId,
                             String content, boolean anonymous) throws Exception {
        String sql = """
            INSERT INTO comments (post_id, user_id, parent_id, content, is_anonymous, anon_index)
            VALUES (?,?,?,?,?,?)
        """;

        try (Connection con = DBManager.getConnection()) {
            boolean prevAuto = con.getAutoCommit();
            try {
                con.setAutoCommit(false);

                Integer alias = null;
                if (anonymous) {
                    alias = getOrCreateAnonIndex(con, postId, userId);
                }

                int newId;
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, postId);
                    ps.setInt(2, userId);
                    if (parentId == null) ps.setNull(3, Types.INTEGER);
                    else ps.setInt(3, parentId);
                    ps.setString(4, content);
                    ps.setInt(5, anonymous ? 1 : 0);
                    if (alias == null) ps.setNull(6, Types.INTEGER);
                    else ps.setInt(6, alias);

                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        newId = rs.getInt(1);
                    }
                }

                con.commit();
                return newId;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(prevAuto); // ✅ 원래 상태로 복구
            }
        }
    }

    /* 자식이 없으면 본인 댓글만 삭제 (영향받은 행 수 반환: 1이면 성공, 0이면 권한 없음/없음) */
    public static int deleteByIdAndUser(int commentId, int userId) throws Exception {
        String sql = "DELETE FROM comments WHERE id = ? AND user_id = ?";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        }
    }

    /* 본인 댓글의 내용을 '[삭제된 댓글입니다]' 로 변경 (영향 행 수 반환) */
    public static int softDeleteByIdAndUser(int commentId, int userId) throws Exception {
        String sql = """
            UPDATE comments
               SET content = '[삭제된 댓글입니다]'
             WHERE id = ? AND user_id = ? AND content <> '[삭제된 댓글입니다]'
        """;
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        }
    }

    /* 글의 댓글 개수 반환 */
    public static int countByPostId(int postId) throws Exception {
        String sql = "SELECT COUNT(*) FROM comments WHERE post_id = ?";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /* 단일 댓글 조회 (수정/권한 확인 등에 사용) */
    public static CommentDto findById(int id) throws Exception {
        String sql = """
            SELECT c.id, c.post_id, c.user_id, c.parent_id, c.content, c.is_anonymous,
                   c.anon_index, c.created_at,
                   u.nickname AS author_nickname
              FROM comments c
         LEFT JOIN users u ON u.id = c.user_id
             WHERE c.id = ?
        """;
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                CommentDto c = new CommentDto();
                c.setId(rs.getInt("id"));
                c.setPostId(rs.getInt("post_id"));
                c.setUserId(rs.getInt("user_id"));

                Object parent = rs.getObject("parent_id");
                c.setParentId(parent == null ? null : rs.getInt("parent_id"));

                c.setContent(rs.getString("content"));
                c.setAnonymous(rs.getInt("is_anonymous") == 1);

                // anon_index 매핑
                c.setAliasNo((Integer) rs.getObject("anon_index"));

                Timestamp ts = rs.getTimestamp("created_at");
                c.setCreatedAt(ts == null ? null : ts.toLocalDateTime());

                c.setUserNickname(rs.getString("author_nickname"));

                return c;
            }
        }
    }

    /** (post_id, user_id) -> anon_index 를 반환. 없으면 배정 후 반환 */
    private static int getOrCreateAnonIndex(Connection con, int postId, int userId) throws Exception {
        // 1) 이미 있으면 바로 반환 (락)
        String sel = "SELECT anon_index FROM post_anon_map WHERE post_id=? AND user_id=? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sel)) {
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        // 2) posts.anon_counter 원자 증가 + 회수
        int nextIdx;
        String bump = "UPDATE posts SET anon_counter = LAST_INSERT_ID(anon_counter + 1) WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(bump)) {
            ps.setInt(1, postId);
            if (ps.executeUpdate() == 0) throw new IllegalStateException("post not found: " + postId);
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT LAST_INSERT_ID()")) {
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                nextIdx = rs.getInt(1);
            }
        }

        // 3) 맵 삽입 (동시성 충돌 시 재조회)
        String ins = "INSERT INTO post_anon_map (post_id, user_id, anon_index) VALUES (?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(ins)) {
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            ps.setInt(3, nextIdx);
            ps.executeUpdate();
            return nextIdx;
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            String sel2 = "SELECT anon_index FROM post_anon_map WHERE post_id=? AND user_id=? FOR UPDATE";
            try (PreparedStatement ps2 = con.prepareStatement(sel2)) {
                ps2.setInt(1, postId);
                ps2.setInt(2, userId);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next()) return rs2.getInt(1);
                }
            }
            throw e;
        }
    }
}
