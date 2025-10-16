package jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// DB 연결
// Resource Release
public class DBManager {
	static String url = "jdbc:mysql://localhost:3306/miniproject";
	static String user = "root";
	static String pwd = "root";
	
	public static Connection getConnection() {
		Connection con = null;
		
		try {
			// db 연결
			con = DriverManager.getConnection(url, user, pwd);
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return con;
	}
	
	public static void releaseConnection(PreparedStatement pstmt, Connection con) {
		try {
			pstmt.close();
			con.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void releaseConnection(ResultSet rs, PreparedStatement pstmt, Connection con) {
		try {
			// connection 부터 닫으면 안됨
			// 만든 순서의 역순으로 close
			rs.close();
			pstmt.close();
			con.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
