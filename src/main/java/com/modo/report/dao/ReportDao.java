package com.modo.report.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.modo.common.enums.YnType;
import com.modo.report.dto.ReasonType;
import com.modo.report.dto.ReportDTO;
import com.modo.report.dto.ReportResult;
import com.modo.report.dto.ReportStatus;

// 성환작성
// 링크 오류 신고 DB 담당 (Connection은 Service가 열고 닫음)
public class ReportDAO {

	// 관리자 목록·1건 조회에 같이 쓰는 SELECT (강의명, 신고자 이름까지 JOIN)
	private static final String SELECT_JOIN =
			"SELECT r.*, l.title AS lec_title, m.member_name "
			+ "FROM report r "
			+ "JOIN lecture l ON r.lec_id = l.lec_id "
			+ "JOIN member m ON r.member_id = m.member_id ";

	// ===== 사용자 =====

	// 신고 등록 (REPORT_STATUS = 'WAIT', DEL_YN, REG_DATE는 DB 기본값)
	public int insert(Connection conn, ReportDTO dto) throws SQLException {

		String sql = "INSERT INTO report (lec_id, member_id, reason_type, content) VALUES (?, ?, ?, ?)";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, dto.getLecId());
			ps.setString(2, dto.getMemberId());
			ps.setString(3, dto.getReasonType().name());   // ENUM -> 문자열

			// 상세 내용을 안 썼으면 NULL
			if (dto.getContent() == null) {
				ps.setNull(4, Types.VARCHAR);
			} else {
				ps.setString(4, dto.getContent());
			}
			return ps.executeUpdate();
		}
	}

	// 오늘 이 회원이 이 강의를 이미 신고했는지 (하루 1번 제한)
	// CURDATE() = 오늘 0시 -> 오늘 0시 ~ 내일 0시 사이에 신고한 건수를 셈
	public boolean existsToday(Connection conn, String memberId, int lecId) throws SQLException {

		String sql = "SELECT COUNT(*) FROM report "
				+ "WHERE member_id = ? AND lec_id = ? AND del_yn = 'N' "
				+ "AND reg_date >= CURDATE() AND reg_date < CURDATE() + INTERVAL 1 DAY";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, memberId);
			ps.setInt(2, lecId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1) > 0;
			}
		}
	}

	// 신고하려는 강의가 있는지 (없는 번호·삭제된 강의 막기)
	public boolean lectureExists(Connection conn, int lecId) throws SQLException {

		String sql = "SELECT COUNT(*) FROM lecture WHERE lec_id = ? AND del_yn = 'N'";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, lecId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1) > 0;
			}
		}
	}

	// ===== 관리자 =====

	// 신고 목록 (status가 null이면 전체)
	// WAIT 먼저 ('WAIT' > 'DONE' 이라 DESC면 WAIT가 위로), 그다음 최신순
	public List<ReportDTO> findList(Connection conn, ReportStatus status) throws SQLException {

		String sql = SELECT_JOIN + "WHERE r.del_yn = 'N'";
		if (status != null) {
			sql += " AND r.report_status = ?";
		}
		sql += " ORDER BY r.report_status DESC, r.report_id DESC";

		List<ReportDTO> list = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			if (status != null) {
				ps.setString(1, status.name());
			}
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(reportDtoMapper(rs));
				}
			}
		}
		return list;
	}

	// 신고 1건 조회 (없거나 삭제된 신고면 null)
	public ReportDTO findById(Connection conn, int reportId) throws SQLException {

		String sql = SELECT_JOIN + "WHERE r.report_id = ? AND r.del_yn = 'N'";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, reportId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return reportDtoMapper(rs);
				}
				return null;
			}
		}
	}

	// 신고 처리 완료 : 상태·결과·처리일을 한 번에 저장 (DB check : DONE이면 result, proc_date 필수)
	// WAIT인 신고만 바꿈 -> 바뀐 줄이 0이면 이미 처리된 신고
	public int updateDone(Connection conn, int reportId, ReportResult result) throws SQLException {

		String sql = "UPDATE report SET report_status = 'DONE', result = ?, proc_date = NOW() "
				+ "WHERE report_id = ? AND report_status = 'WAIT' AND del_yn = 'N'";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, result.name());
			ps.setInt(2, reportId);
			return ps.executeUpdate();
		}
	}

	// 강의 숨김 (처리 결과가 HIDDEN일 때 lecture.hide_yn = 'Y')
	public int hideLecture(Connection conn, int lecId) throws SQLException {

		String sql = "UPDATE lecture SET hide_yn = 'Y' WHERE lec_id = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, lecId);
			return ps.executeUpdate();
		}
	}

	// ReportDTO로 변환 (목록·1건 조회가 같이 씀)
	private ReportDTO reportDtoMapper(ResultSet rs) throws SQLException {

		ReportDTO dto = new ReportDTO();

		dto.setReportId(rs.getInt("REPORT_ID"));
		dto.setLecId(rs.getInt("LEC_ID"));
		dto.setMemberId(rs.getString("MEMBER_ID"));
		dto.setContent(rs.getString("CONTENT"));

		// 문자열 -> ENUM값으로 변환
		dto.setReasonType(ReasonType.valueOf(rs.getString("REASON_TYPE")));
		dto.setReportStatus(ReportStatus.valueOf(rs.getString("REPORT_STATUS")));
		dto.setDelYn(YnType.valueOf(rs.getString("DEL_YN")));

		// RESULT는 대기 중이면 NULL -> null 체크 후 변환
		String result = rs.getString("RESULT");
		dto.setResult(result == null ? null : ReportResult.valueOf(result));

		// DATETIME -> LocalDateTime으로 변환 (PROC_DATE는 NULL이면 null)
		dto.setProcDate(rs.getObject("PROC_DATE", LocalDateTime.class));
		dto.setRegDate(rs.getObject("REG_DATE", LocalDateTime.class));
		dto.setModDate(rs.getObject("MOD_DATE", LocalDateTime.class));

		// JOIN으로 가져온 값
		dto.setLecTitle(rs.getString("LEC_TITLE"));
		dto.setMemberName(rs.getString("MEMBER_NAME"));

		return dto;
	}
}
