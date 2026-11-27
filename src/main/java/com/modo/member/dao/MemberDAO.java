package com.modo.member.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;

import com.modo.common.enums.SchoolLevel;
import com.modo.common.enums.YnType;
import com.modo.member.dto.MemberDTO;
import com.modo.member.dto.MemberRole;
import com.modo.member.dto.MemberStatus;

//재관작성
public class MemberDAO {

	// Id로 회원 조회
	public MemberDTO findById(Connection conn, String memberId) throws SQLException {

		String sql = "SELECT * FROM MEMBER WHERE MEMBER_ID = ?";


		try (PreparedStatement ps = conn.prepareStatement(sql)){

			ps.setString(1, memberId);
			try (ResultSet rs = ps.executeQuery()){
				if (rs.next()) {
					return memberDtoMapper(rs);
				}
				return null;

			}
		}


	}

	// 회원 생성 (ROLE, STATUS, DEL_YN, REG_DATE는 DB 기본값)
	public int insert(Connection conn, MemberDTO dto) throws SQLException {
		String sql = "INSERT INTO MEMBER (MEMBER_ID, MEMBER_PW, MEMBER_NAME, EMAIL, SCHOOL_LEVEL, GRADE) VALUES (?, ?, ?, ?, ?, ?)";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, dto.getMemberId());
			ps.setString(2, dto.getMemberPw());
			ps.setString(3, dto.getMemberName());
			ps.setString(4, dto.getEmail());
			ps.setString(5, dto.getSchoolLevel().name());   // ENUM -> 문자열

			// 학교급 NONE이면 학년 null
			if (dto.getGrade() == null) {
				ps.setNull(6, Types.INTEGER);
			} else {
				ps.setInt(6, dto.getGrade());
			}

			return ps.executeUpdate();

		}
	}


	// email 중복확인
	public boolean existsEmail(Connection conn, String email) throws SQLException{
		String sql = "SELECT COUNT(*) FROM MEMBER WHERE EMAIL = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, email);
			try (ResultSet rs = ps.executeQuery()){
				rs.next();
				return rs.getInt(1) > 0;


			}
		}
	}

	// MemberDTO로 변환
	private MemberDTO memberDtoMapper(ResultSet rs) throws SQLException{

		MemberDTO dto = new MemberDTO();

		dto.setMemberId(rs.getString("MEMBER_ID"));
		dto.setMemberPw(rs.getString("MEMBER_PW"));
		dto.setMemberName(rs.getString("MEMBER_NAME"));
		dto.setEmail(rs.getString("EMAIL"));

		// GRADE가 null일 수 있어서 Integer로 받기
		dto.setGrade(rs.getObject("GRADE", Integer.class));

		// 문자열 -> ENUM값으로 변환
		dto.setSchoolLevel(SchoolLevel.valueOf(rs.getString("SCHOOL_LEVEL")));
		dto.setMemberRole(MemberRole.valueOf(rs.getString("MEMBER_ROLE")));
		dto.setMemberStatus(MemberStatus.valueOf(rs.getString("MEMBER_STATUS")));
		dto.setDelYn(YnType.valueOf(rs.getString("DEL_YN")));

		// DATETIME -> LocalDateTime으로 변환
		dto.setRegDate(rs.getObject("REG_DATE", LocalDateTime.class));
		dto.setModDate(rs.getObject("MOD_DATE", LocalDateTime.class));


		return dto;
	}
}
