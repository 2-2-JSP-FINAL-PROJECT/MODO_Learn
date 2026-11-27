package com.modo.common.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// 현호작성
public class DBUtil {

	private static final String URL = "jdbc:mysql://localhost:3306/modo_learn";
	private static final String USER = "root";
	private static final String PASSWORD = "test1234";

	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new ExceptionInInitializerError(
					"mysql-connector-j.jar 가 WEB-INF/lib에 없습니다." + e.getMessage());
		}
	}

	private DBUtil() {}

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}

	public static void rollback(Connection conn) {
		if (conn == null) return;

		try {
			conn.rollback();
		} catch (SQLException ignore) {
		}
	}

	public static void close(AutoCloseable resource) {
		if (resource == null) return;

		try {
			resource.close();
		} catch (Exception ignore) {
		}
	}
}
