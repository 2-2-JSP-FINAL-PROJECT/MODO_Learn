package com.modo.qna.service;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.modo.common.util.DBUtil;
import com.modo.qna.dao.QnaDao;
import com.modo.qna.dto.AnswerDto;
import com.modo.qna.dto.QuestionDto;

/*
 * Q&A 업무 규칙 담당
 *  - Dao 는 SQL 만 실행하고, "해도 되는지" 판단은 여기서 한다.
 *  - 규칙 위반은 예외로 알린다. Controller 가 받아서 화면에 안내한다.
 *      IllegalArgumentException : 입력값이 잘못됨   (400)
 *      SecurityException        : 권한이 없음       (403)
 */
public class QnaService {

	private QnaDao dao = new QnaDao();

	// DB 컬럼 크기 (조장 SQL 기준)
	private static final int TITLE_MAX = 200;         // title   VARCHAR2(200 CHAR) : 글자 수
	private static final int CONTENT_MAX_BYTE = 4000; // content VARCHAR2(4000)     : 바이트 수 (한글 1자 = 3바이트)

	// ---------------------------------------------------------------------
	// 읽기
	// ---------------------------------------------------------------------

	// 목록
	public List<QuestionDto> getList() throws SQLException {
		return dao.findList();
	}

	// 상세 : 조회수 +1 → 질문 가져오기 (순서를 바꾸면 화면 조회수가 1 적게 보임)
	public QuestionDto getDetail(int qId) throws SQLException {
		dao.increaseViewCount(qId);
		return dao.findById(qId);
	}

	// 상세 화면의 답변 목록
	public List<AnswerDto> getAnswers(int qId) throws SQLException {
		return dao.findAnswers(qId);
	}

	// 수정 화면에 채울 질문 (조회수는 올리지 않음 + 본인 글인지 확인)
	public QuestionDto getForEdit(int qId, String loginId) throws SQLException {
		QuestionDto q = dao.findById(qId);
		checkCanEdit(q, loginId);
		return q;
	}

	// ---------------------------------------------------------------------
	// 질문 쓰기 · 수정 · 삭제
	// ---------------------------------------------------------------------

	// 질문 등록 → 새 질문 번호를 돌려줌
	public int write(QuestionDto q, String loginId) throws SQLException {
		requireLogin(loginId);
		validateQuestion(q);
		q.setUserId(loginId);          // 작성자는 화면 값이 아니라 로그인 정보로 정한다 (조작 방지)
		return dao.insertQuestion(q);
	}

	// 질문 수정
	public void update(QuestionDto q, String loginId) throws SQLException {
		QuestionDto origin = dao.findById(q.getqId());
		checkCanEdit(origin, loginId);
		validateQuestion(q);
		dao.updateQuestion(q);
	}

	// 질문 삭제 : 작성자 본인 또는 관리자
	public void delete(int qId, String loginId, boolean isAdmin) throws SQLException {
		requireLogin(loginId);
		QuestionDto q = dao.findById(qId);
		if (q == null) {
			throw new IllegalArgumentException("이미 삭제되었거나 없는 질문입니다.");
		}
		if (!isAdmin && !q.getUserId().equals(loginId)) {
			throw new SecurityException("본인이 쓴 질문만 삭제할 수 있습니다.");
		}
		dao.deleteQuestion(qId);
	}

	// ---------------------------------------------------------------------
	// 관리자 답변
	// ---------------------------------------------------------------------

	// 답변 등록 : 관리자만. 답변 저장 + 질문 상태 ANSWERED 를 하나로 묶는다 (트랜잭션)
	public void writeAnswer(int qId, String content, String loginId, boolean isAdmin) throws SQLException {
		if (!isAdmin) {
			throw new SecurityException("관리자만 답변할 수 있습니다.");
		}
		content = trim(content);
		if (content.isEmpty()) {
			throw new IllegalArgumentException("답변 내용을 입력해 주세요.");
		}
		if (byteLength(content) > CONTENT_MAX_BYTE) {
			throw new IllegalArgumentException("답변이 너무 깁니다. (한글 약 1,300자까지)");
		}
		if (dao.findById(qId) == null) {
			throw new IllegalArgumentException("이미 삭제되었거나 없는 질문입니다.");
		}

		AnswerDto a = new AnswerDto();
		a.setqId(qId);
		a.setUserId(loginId);
		a.setContent(content);

		Connection conn = null;
		try {
			conn = DBUtil.getConnection();
			conn.setAutoCommit(false);              // ① 자동 저장 끄기

			dao.insertAnswer(conn, a);              // ② 답변 저장
			dao.updateStatus(conn, qId, "ANSWERED"); // ③ 질문 상태 변경

			conn.commit();                          // ④ 둘 다 성공 → 한 번에 저장
		} catch (SQLException e) {
			DBUtil.rollback(conn);                  // 하나라도 실패 → 둘 다 취소
			throw e;
		} finally {
			DBUtil.close(conn);
		}
	}

	// 답변 삭제 : 관리자만. 남은 답변이 0개가 되면 질문 상태를 다시 WAIT 로
	// → 어느 질문의 답변이었는지(질문 번호)를 돌려줌 (삭제 후 그 질문 상세로 돌아가려고)
	public int deleteAnswer(int aId, boolean isAdmin) throws SQLException {
		if (!isAdmin) {
			throw new SecurityException("관리자만 답변을 삭제할 수 있습니다.");
		}
		AnswerDto a = dao.findAnswerById(aId);
		if (a == null) {
			throw new IllegalArgumentException("이미 삭제되었거나 없는 답변입니다.");
		}
		int qId = a.getqId();

		Connection conn = null;
		try {
			conn = DBUtil.getConnection();
			conn.setAutoCommit(false);

			dao.deleteAnswer(conn, aId);
			if (dao.countAnswers(conn, qId) == 0) {
				dao.updateStatus(conn, qId, "WAIT");
			}

			conn.commit();
		} catch (SQLException e) {
			DBUtil.rollback(conn);
			throw e;
		} finally {
			DBUtil.close(conn);
		}
		return qId;
	}

	// ---------------------------------------------------------------------
	// 검사용 도우미 메서드 (이 클래스 안에서만 씀)
	// ---------------------------------------------------------------------

	private void requireLogin(String loginId) {
		if (loginId == null) {
			throw new SecurityException("로그인 후 이용할 수 있습니다.");
		}
	}

	// 수정 가능한지 : 로그인 + 본인 글 + 아직 답변이 안 달림
	private void checkCanEdit(QuestionDto q, String loginId) {
		requireLogin(loginId);
		if (q == null) {
			throw new IllegalArgumentException("이미 삭제되었거나 없는 질문입니다.");
		}
		if (!q.getUserId().equals(loginId)) {
			throw new SecurityException("본인이 쓴 질문만 수정할 수 있습니다.");
		}
		if ("ANSWERED".equals(q.getqStatus())) {
			throw new IllegalArgumentException("답변이 달린 질문은 수정할 수 없습니다.");
		}
	}

	// 질문 입력값 검사 (DB 제약조건에 걸리기 전에 먼저 막는다)
	private void validateQuestion(QuestionDto q) {
		q.setTitle(trim(q.getTitle()));
		q.setContent(trim(q.getContent()));

		if (!"STUDY".equals(q.getqType()) && !"SITE".equals(q.getqType())) {
			throw new IllegalArgumentException("질문 구분을 선택해 주세요.");
		}
		if (q.getTitle().isEmpty()) {
			throw new IllegalArgumentException("제목을 입력해 주세요.");
		}
		if (q.getTitle().length() > TITLE_MAX) {
			throw new IllegalArgumentException("제목은 " + TITLE_MAX + "자까지 쓸 수 있습니다.");
		}
		if (q.getContent().isEmpty()) {
			throw new IllegalArgumentException("내용을 입력해 주세요.");
		}
		if (byteLength(q.getContent()) > CONTENT_MAX_BYTE) {
			throw new IllegalArgumentException("내용이 너무 깁니다. (한글 약 1,300자까지)");
		}
		// 이용 문의(SITE)는 학년·과목·강의와 관계없으니 비운다
		if ("SITE".equals(q.getqType())) {
			q.setCatId(null);
			q.setLecId(null);
		}
	}

	// null 이면 "" 로, 앞뒤 공백 제거
	private String trim(String s) {
		return s == null ? "" : s.trim();
	}

	// 오라클에 저장될 때의 바이트 수 (UTF-8 기준)
	private int byteLength(String s) {
		return s.getBytes(StandardCharsets.UTF_8).length;
	}
}
