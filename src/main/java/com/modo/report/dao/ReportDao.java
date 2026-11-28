package com.modo.report.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.modo.common.util.DBUtil;
import com.modo.report.dto.ReportDto;

// 링크 오류 신고 DB 담당 (사용자 쪽 : 접수만. 신고 처리는 관리자 화면 담당)
public class ReportDao {

	// 신고 등록
	public int insert(ReportDto r) throws SQLException {
		String sql = "INSERT INTO report (report_id, lec_id, user_id, reason_type, content) "
				+ "VALUES (seq_report.NEXTVAL, ?, ?, ?, ?)";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, r.getLecId());
			ps.setString(2, r.getUserId());
			ps.setString(3, r.getReasonType());
			if (r.getContent() == null) {
				ps.setNull(4, Types.VARCHAR);   // 상세 내용을 안 썼으면 NULL
			} else {
				ps.setString(4, r.getContent());
			}
			return ps.executeUpdate();
		}
	}// insert

	// 오늘 이 회원이 이 강의를 이미 신고했는지 (하루 1회 제한)
	//  TRUNC(SYSDATE) = 오늘 0시. 오늘 0시 ~ 내일 0시 사이에 신고한 건수를 센다
	public boolean existsToday(String userId, int lecId) throws SQLException {
		String sql = "SELECT COUNT(*) FROM report "
				+ "WHERE user_id = ? AND lec_id = ? AND del_yn = 'N' "
				+ "AND reg_date >= TRUNC(SYSDATE) AND reg_date < TRUNC(SYSDATE) + 1";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, userId);
			ps.setInt(2, lecId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1) > 0;
			}
		}
	}// existsToday

	// 신고하려는 강의가 실제로 있는지 (삭제된 강의 · 없는 번호 막기)
	public boolean lectureExists(int lecId) throws SQLException {
		String sql = "SELECT COUNT(*) FROM lecture WHERE lec_id = ? AND del_yn = 'N'";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, lecId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1) > 0;
			}
		}
	}// lectureExists

}// ReportDao
