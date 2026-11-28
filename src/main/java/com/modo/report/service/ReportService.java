package com.modo.report.service;

import java.sql.SQLException;

import com.modo.report.dao.ReportDao;
import com.modo.report.dto.ReportDto;

/*
 * 링크 오류 신고 업무 규칙
 *  - 로그인한 회원만
 *  - 사유는 4가지 중 하나, "기타"는 상세 내용 필수
 *  - 상세 내용 300자까지
 *  - 같은 강의는 하루에 한 번만
 *  규칙 위반 : IllegalArgumentException (입력값) / SecurityException (로그인 안 함)
 */
public class ReportService {

	private ReportDao dao = new ReportDao();

	private static final int CONTENT_MAX = 300;   // content VARCHAR2(300 CHAR) : 글자 수

	public void write(ReportDto r, String loginId) throws SQLException {
		if (loginId == null) {
			throw new SecurityException("로그인 후 신고할 수 있습니다.");
		}

		String reason = r.getReasonType();
		if (!"DELETED".equals(reason) && !"PRIVATE".equals(reason)
				&& !"WRONG_CATEGORY".equals(reason) && !"ETC".equals(reason)) {
			throw new IllegalArgumentException("신고 사유를 선택해 주세요.");
		}

		String content = r.getContent() == null ? "" : r.getContent().trim();
		if ("ETC".equals(reason) && content.isEmpty()) {
			throw new IllegalArgumentException("'기타'를 고르면 상세 내용을 적어 주세요.");
		}
		if (content.length() > CONTENT_MAX) {
			throw new IllegalArgumentException("상세 내용은 " + CONTENT_MAX + "자까지 쓸 수 있습니다.");
		}
		r.setContent(content.isEmpty() ? null : content);   // 빈 내용은 NULL 로 저장

		if (!dao.lectureExists(r.getLecId())) {
			throw new IllegalArgumentException("없는 강의입니다.");
		}
		if (dao.existsToday(loginId, r.getLecId())) {
			throw new IllegalArgumentException("이 강의는 오늘 이미 신고하셨습니다. 내일 다시 신고할 수 있어요.");
		}

		r.setUserId(loginId);   // 신고자는 로그인 정보로 정한다
		dao.insert(r);
	}
}
