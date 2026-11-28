package com.modo.report.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.modo.common.exception.ModoException;
import com.modo.common.util.DBUtil;
import com.modo.member.dto.LoginUser;
import com.modo.report.dao.ReportDAO;
import com.modo.report.dto.ReasonType;
import com.modo.report.dto.ReportDTO;
import com.modo.report.dto.ReportResult;
import com.modo.report.dto.ReportStatus;

// 성환작성
// 링크 오류 신고 업무 규칙 (사용자: 신고 접수 / 관리자: 목록·처리)
public class ReportService {

	private final ReportDAO reportDAO = new ReportDAO();

	private static final int CONTENT_MAX = 300;   // content VARCHAR(300) : 글자 수

	// ===== 사용자 =====

	// 신고 접수
	//  - 로그인 회원만 / 사유 필수 / ETC면 상세 내용 필수 / 300자 이내
	//  - 없는 강의 X / 같은 강의는 하루 1번
	public void write(LoginUser loginUser, ReportDTO dto) {
		if (loginUser == null) {
			throw new ModoException("로그인 후 신고할 수 있습니다.");
		}
		validateReport(dto);

		// 신고자는 화면 값이 아니라 로그인 정보로
		dto.setMemberId(loginUser.getMemberId());

		try (Connection conn = DBUtil.getConnection()) {

			if (!reportDAO.lectureExists(conn, dto.getLecId())) {
				throw new ModoException("없는 강의입니다.");
			}
			if (reportDAO.existsToday(conn, dto.getMemberId(), dto.getLecId())) {
				throw new ModoException("이 강의는 오늘 이미 신고하셨습니다. 내일 다시 신고할 수 있어요.");
			}

			reportDAO.insert(conn, dto);

		} catch (SQLException e) {
			throw new ModoException("신고 접수 처리 중 오류가 발생했습니다.", e);
		}
	}

	// ===== 관리자 =====

	// 신고 목록 (status가 null이면 전체)
	public List<ReportDTO> getList(LoginUser loginUser, ReportStatus status) {
		checkAdmin(loginUser);

		try (Connection conn = DBUtil.getConnection()) {
			return reportDAO.findList(conn, status);
		} catch (SQLException e) {
			throw new ModoException("신고 목록 조회 중 오류가 발생했습니다.", e);
		}
	}

	// 신고 처리 : DONE + 결과 + 처리일 저장, HIDDEN이면 강의도 숨김
	// 두 테이블(report, lecture)을 바꾸므로 트랜잭션으로 묶음 (둘 다 되거나 둘 다 안 되거나)
	public void process(LoginUser loginUser, int reportId, ReportResult result) {
		checkAdmin(loginUser);

		if (result == null) {
			throw new ModoException("처리 결과를 선택해주세요.");
		}

		try (Connection conn = DBUtil.getConnection()) {
			conn.setAutoCommit(false);   // 자동 저장 끄기 -> commit 할 때 한 번에 저장

			try {
				ReportDTO report = reportDAO.findById(conn, reportId);
				if (report == null) {
					throw new ModoException("없는 신고입니다.");
				}
				if (report.getReportStatus() == ReportStatus.DONE) {
					throw new ModoException("이미 처리된 신고입니다.");
				}

				// 그 사이에 다른 관리자가 먼저 처리했으면 0
				if (reportDAO.updateDone(conn, reportId, result) == 0) {
					throw new ModoException("이미 처리된 신고입니다.");
				}

				if (result == ReportResult.HIDDEN) {
					reportDAO.hideLecture(conn, report.getLecId());
				}

				conn.commit();

			} catch (SQLException | RuntimeException e) {
				// 중간에 하나라도 실패하면 전부 취소
				DBUtil.rollback(conn);
				throw e;
			}

		} catch (SQLException e) {
			throw new ModoException("신고 처리 중 오류가 발생했습니다.", e);
		}
	}

	// 관리자인지 한 번 더 확인 (LoginFilter가 막아 주지만 Service에서도 확인)
	private void checkAdmin(LoginUser loginUser) {
		if (loginUser == null || !loginUser.isAdmin()) {
			throw new ModoException("관리자만 이용할 수 있습니다.");
		}
	}

	// 신고 입력값 검사
	private void validateReport(ReportDTO dto) {
		ReasonType reason = dto.getReasonType();
		String content = dto.getContent();   // Controller에서 trim -> 빈 값이면 null

		if (reason == null) {
			throw new ModoException("신고 사유를 선택해주세요.");
		}
		if (reason == ReasonType.ETC && content == null) {
			throw new ModoException("'기타'를 고르면 상세 내용을 적어주세요.");
		}
		if (content != null && content.length() > CONTENT_MAX) {
			throw new ModoException("상세 내용은 " + CONTENT_MAX + "자까지 쓸 수 있습니다.");
		}
	}
}
