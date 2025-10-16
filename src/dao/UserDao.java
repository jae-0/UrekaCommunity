package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import dto.LoginDto;
import jdbc.DBManager;

/*  [구현된 기능]
	1. 이메일 중복 여부 확인
	2. 회원가입 기능
	3. 로그인 기능
*/

public class UserDao {
	/* 이메일 존재 여부 확인 */
    public static boolean emailExists(String email) throws Exception {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    /* 회원가입 */
    public static long insert(String nickname, String email, String pwHash, String role) throws Exception {
        String sql = "INSERT INTO users (nickname, email, pw_hash, role) VALUES (?,?,?,?)";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nickname); // 이름을 닉네임으로 설정
            ps.setString(2, email);
            ps.setString(3, pwHash);
            ps.setString(4, role);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
    
    /* 로그인 (DB랑 대조) */
    /* 데모 버전이라 비밀번호의 암호화는 진행하지 않았습니다. */
    public static LoginDto findByEmail(String email) throws Exception {
        String sql = "SELECT id, nickname, pw_hash FROM users WHERE email = ? LIMIT 1";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                LoginDto dto = new LoginDto();
                dto.setId(rs.getInt("id"));
                dto.setNickname(rs.getString("nickname"));
                dto.setPw(rs.getString("pw_hash"));
                return dto;
            }
        }
    }

    
}
