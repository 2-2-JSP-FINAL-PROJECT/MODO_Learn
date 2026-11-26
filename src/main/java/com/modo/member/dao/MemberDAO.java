package com.modo.member.dao;

import com.modo.common.util.DBUtil;
import com.modo.member.dto.LoginMemberDTO;
import com.modo.member.dto.MemberDTO;
import java.sql.*;
import java.util.Optional;

public class MemberDAO {
    @FunctionalInterface public interface ConnectionProvider { Connection get() throws SQLException; }
    private final ConnectionProvider connections;
    public MemberDAO() { this(DBUtil::getConnection); }
    public MemberDAO(ConnectionProvider connections) { this.connections = connections; }

    public Optional<MemberDTO> findById(String id) throws SQLException {
        try (Connection connection = connections.get(); PreparedStatement statement = connection.prepareStatement(
                "SELECT member_id, member_pw, member_name, email, school_level, grade, member_role, member_status, del_yn FROM member WHERE member_id = ?")) {
            statement.setString(1, id);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) return Optional.empty();
                int value = row.getInt("grade");
                Integer grade = row.wasNull() ? null : value;
                LoginMemberDTO member = new LoginMemberDTO(row.getString("member_id"), row.getString("member_name"),
                        row.getString("email"), row.getString("school_level"), grade, row.getString("member_role"));
                return Optional.of(new MemberDTO(member, row.getString("member_pw"), row.getString("member_status"), row.getString("del_yn")));
            }
        }
    }
    public boolean existsById(String id) throws SQLException { return exists("SELECT 1 FROM member WHERE member_id = ?", id); }
    public boolean existsByEmail(String email) throws SQLException { return exists("SELECT 1 FROM member WHERE email = ?", email); }
    private boolean exists(String sql, String value) throws SQLException {
        try (Connection connection = connections.get(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet row = statement.executeQuery()) { return row.next(); }
        }
    }
    public void insert(Connection connection, LoginMemberDTO member, String passwordHash) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO member (member_id, member_pw, member_name, email, school_level, grade, member_role, member_status, del_yn) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'USER', 'ACTIVE', 'N')")) {
            statement.setString(1, member.getMemberId()); statement.setString(2, passwordHash);
            statement.setString(3, member.getMemberName()); statement.setString(4, member.getEmail());
            statement.setString(5, member.getSchoolLevel());
            if (member.getGrade() == null) statement.setNull(6, Types.INTEGER); else statement.setInt(6, member.getGrade());
            statement.executeUpdate();
        }
    }
    public Connection openConnection() throws SQLException { return connections.get(); }
}
