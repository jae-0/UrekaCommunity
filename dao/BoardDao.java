package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import jdbc.DBManager;

/*  [구현된 기능]
	1. 게시판 id 오름차순으로 조회
	2. 해당 board ID의 name을 반환
*/

public class BoardDao {
    public static class BoardRow {
        public int id;
        public String name;
    }
    
    /* 게시판 id 오름차순으로 조회 */
    public static List<BoardRow> listAll() throws Exception {
        String sql = "SELECT id, name FROM boards ORDER BY id ASC";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<BoardRow> out = new ArrayList<>();
            while (rs.next()) {
                BoardRow r = new BoardRow();
                r.id = rs.getInt("id");
                r.name = rs.getString("name");
                out.add(r);
            }
            return out;
        }
    }
    
    /* 해당 board ID의 name을 반환 */
    public static String findNameById(int boardId) throws Exception {
        String sql = "SELECT name FROM boards WHERE id = ?";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, boardId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("name");
                return null; // 없으면 null
            }
        }
    }
}
