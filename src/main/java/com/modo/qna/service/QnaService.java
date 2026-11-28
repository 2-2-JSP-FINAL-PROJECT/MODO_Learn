package com.modo.qna.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.modo.common.exception.ModoException;
import com.modo.common.util.DBUtil;
import com.modo.member.dto.LoginUser;
import com.modo.qna.dao.QnaDAO;
import com.modo.qna.dto.AnswerDTO;
import com.modo.qna.dto.QuestionDTO;
import com.modo.qna.dto.QuestionStatus;
import com.modo.qna.dto.QuestionType;

// 성환작성
// Q&A 업무 규칙 (DAO는 SQL만 실행하고, "해도 되는지" 판단은 여기서)
//  - 질문 : 로그인 회원이 작성, 본인만 수정(답변 달리기 전까지), 본인 또는 관리자가 삭제
//  - 답변 : 관리자만 등록·삭제
public class QnaService {

	private final QnaDAO qnaDAO = new QnaDAO();

	private static final int TITLE_MAX = 200;      // title   VARCHAR(200) : 글자 수
	private static final int CONTENT_MAX = 4000;   // content VARCHAR(4000) : 글자 수

	// ===== 조회 =====

	// 질문 목록
	public List<QuestionDTO> getList() {
		try (Connection conn = DBUtil.getConnection()) {
			return qnaDAO.findList(conn);
		} catch (SQLException e) {
			throw new ModoException("질문 목록 조회 중 오류가 발생했습니다.", e);
		}
	}

	// 질문 상세 : 조회수 +1 -> 질문 가져오기 (순서를 바꾸면 화면 조회수가 1 적게 보임)
	public QuestionDTO getDetail(int questionId) {
		try (Connection conn = DBUtil.getConnection()) {
			// 바뀐 줄이 0개면 없거나 삭제된 글
			if (qnaDAO.increaseViewCount(conn, questionId) == 0) {
				throw new ModoException("이미 삭제되었거나 없는 질문입니다.");
			}
			return qnaDAO.findById(conn, questionId);

		} catch (SQLException e) {
			throw new ModoException("질문 조회 중 오류가 발생했습니다.", e);
		}
	}

	// 질문 가져오기 (조회수는 올리지 않음 -> 답변 등록 오류 후 상세 화면을 다시 보여 줄 때)
	public QuestionDTO getQuestion(int questionId) {
		try (Connection conn = DBUtil.getConnection()) {
			QuestionDTO question = qnaDAO.findById(conn, questionId);
			if (question == null) {
				throw new ModoException("이미 삭제되었거나 없는 질문입니다.");
			}
			return question;

		} catch (SQLException e) {
			throw new ModoException("질문 조회 중 오류가 발생했습니다.", e);
		}
	}

	// 상세 화면의 답변 목록
	public List<AnswerDTO> getAnswers(int questionId) {
		try (Connection conn = DBUtil.getConnection()) {
			return qnaDAO.findAnswers(conn, questionId);
		} catch (SQLException e) {
			throw new ModoException("답변 목록 조회 중 오류가 발생했습니다.", e);
		}
	}

	// 수정 화면에 채울 질문 (조회수는 올리지 않음 + 본인 글인지 확인)
	public QuestionDTO getForEdit(LoginUser loginUser, int questionId) {
		requireLogin(loginUser);

		try (Connection conn = DBUtil.getConnection()) {
			QuestionDTO question = qnaDAO.findById(conn, questionId);
			checkCanEdit(question, loginUser);
			return question;

		} catch (SQLException e) {
			throw new ModoException("질문 조회 중 오류가 발생했습니다.", e);
		}
	}

	// ===== 질문 쓰기·수정·삭제 =====

	// 질문 등록 -> 새 질문 번호 반환
	public int write(LoginUser loginUser, QuestionDTO dto) {
		requireLogin(loginUser);
		validateQuestion(dto);

		// 작성자는 화면 값이 아니라 로그인 정보로 (조작 방지)
		dto.setMemberId(loginUser.getMemberId());

		try (Connection conn = DBUtil.getConnection()) {
			checkCategoryAndLecture(conn, dto);
			return qnaDAO.insertQuestion(conn, dto);

		} catch (SQLException e) {
			throw new ModoException("질문 등록 처리 중 오류가 발생했습니다.", e);
		}
	}

	// 질문 수정
	public void update(LoginUser loginUser, QuestionDTO dto) {
		requireLogin(loginUser);
		validateQuestion(dto);

		try (Connection conn = DBUtil.getConnection()) {
			QuestionDTO origin = qnaDAO.findById(conn, dto.getQuestionId());
			checkCanEdit(origin, loginUser);
			checkCategoryAndLecture(conn, dto);

			if (qnaDAO.updateQuestion(conn, dto) == 0) {
				throw new ModoException("이미 삭제되었거나 없는 질문입니다.");
			}

		} catch (SQLException e) {
			throw new ModoException("질문 수정 처리 중 오류가 발생했습니다.", e);
		}
	}

	// 질문 삭제 : 작성자 본인 또는 관리자
	public void delete(LoginUser loginUser, int questionId) {
		requireLogin(loginUser);

		try (Connection conn = DBUtil.getConnection()) {
			QuestionDTO question = qnaDAO.findById(conn, questionId);
			if (question == null) {
				throw new ModoException("이미 삭제되었거나 없는 질문입니다.");
			}
			if (!loginUser.isAdmin() && !question.getMemberId().equals(loginUser.getMemberId())) {
				throw new ModoException("본인이 쓴 질문만 삭제할 수 있습니다.");
			}

			qnaDAO.deleteQuestion(conn, questionId);

		} catch (SQLException e) {
			throw new ModoException("질문 삭제 처리 중 오류가 발생했습니다.", e);
		}
	}

	// ===== 관리자 답변 =====

	// 답변 등록 : 관리자만. 답변 저장 + 질문 상태 ANSWERED를 하나로 묶음 (트랜잭션)
	public void writeAnswer(LoginUser loginUser, int questionId, String content) {
		checkAdmin(loginUser, "관리자만 답변할 수 있습니다.");

		if (content == null) {
			throw new ModoException("답변 내용을 입력해주세요.");
		}
		if (content.length() > CONTENT_MAX) {
			throw new ModoException("답변은 " + CONTENT_MAX + "자까지 쓸 수 있습니다.");
		}

		AnswerDTO answer = new AnswerDTO();
		answer.setQuestionId(questionId);
		answer.setMemberId(loginUser.getMemberId());   // 작성자는 로그인한 관리자
		answer.setContent(content);

		try (Connection conn = DBUtil.getConnection()) {
			conn.setAutoCommit(false);   // ① 자동 저장 끄기 -> commit 할 때 한 번에 저장

			try {
				if (qnaDAO.findById(conn, questionId) == null) {
					throw new ModoException("이미 삭제되었거나 없는 질문입니다.");
				}
				qnaDAO.insertAnswer(conn, answer);                               // ② 답변 저장
				qnaDAO.updateStatus(conn, questionId, QuestionStatus.ANSWERED);  // ③ 질문 상태 변경

				conn.commit();   // ④ 둘 다 성공 -> 한 번에 저장

			} catch (SQLException | RuntimeException e) {
				DBUtil.rollback(conn);   // 하나라도 실패 -> 둘 다 취소
				throw e;
			}

		} catch (SQLException e) {
			throw new ModoException("답변 등록 처리 중 오류가 발생했습니다.", e);
		}
	}

	// 답변 삭제 : 관리자만. 남은 답변이 0개가 되면 질문 상태를 다시 WAIT로
	// -> 어느 질문의 답변이었는지(질문 번호)를 돌려줌 (삭제 후 그 질문 상세로 돌아가려고)
	public int deleteAnswer(LoginUser loginUser, int answerId) {
		checkAdmin(loginUser, "관리자만 답변을 삭제할 수 있습니다.");

		try (Connection conn = DBUtil.getConnection()) {
			conn.setAutoCommit(false);

			try {
				AnswerDTO answer = qnaDAO.findAnswerById(conn, answerId);
				if (answer == null) {
					throw new ModoException("이미 삭제되었거나 없는 답변입니다.");
				}
				int questionId = answer.getQuestionId();

				qnaDAO.deleteAnswer(conn, answerId);
				if (qnaDAO.countAnswers(conn, questionId) == 0) {
					qnaDAO.updateStatus(conn, questionId, QuestionStatus.WAIT);
				}

				conn.commit();
				return questionId;

			} catch (SQLException | RuntimeException e) {
				DBUtil.rollback(conn);
				throw e;
			}

		} catch (SQLException e) {
			throw new ModoException("답변 삭제 처리 중 오류가 발생했습니다.", e);
		}
	}

	// ===== 검사용 도우미 (이 클래스 안에서만 씀) =====

	private void requireLogin(LoginUser loginUser) {
		if (loginUser == null) {
			throw new ModoException("로그인 후 이용할 수 있습니다.");
		}
	}

	private void checkAdmin(LoginUser loginUser, String message) {
		if (loginUser == null || !loginUser.isAdmin()) {
			throw new ModoException(message);
		}
	}

	// 수정 가능한지 : 있는 글 + 본인 글 + 아직 답변이 안 달림
	private void checkCanEdit(QuestionDTO question, LoginUser loginUser) {
		if (question == null) {
			throw new ModoException("이미 삭제되었거나 없는 질문입니다.");
		}
		if (!question.getMemberId().equals(loginUser.getMemberId())) {
			throw new ModoException("본인이 쓴 질문만 수정할 수 있습니다.");
		}
		if (question.getQuestionStatus() == QuestionStatus.ANSWERED) {
			throw new ModoException("답변이 달린 질문은 수정할 수 없습니다.");
		}
	}

	// 질문 입력값 검사 (DB 제약조건에 걸리기 전에 먼저 막음)
	// title, content는 Controller에서 trim -> 빈 값이면 null
	private void validateQuestion(QuestionDTO dto) {
		String title = dto.getTitle();
		String content = dto.getContent();

		if (dto.getQuestionType() == null) {
			throw new ModoException("질문 구분을 선택해주세요.");
		}
		if (title == null || title.length() > TITLE_MAX) {
			throw new ModoException("제목을 입력해주세요. (" + TITLE_MAX + "자 이내)");
		}
		if (content == null || content.length() > CONTENT_MAX) {
			throw new ModoException("내용을 입력해주세요. (" + CONTENT_MAX + "자 이내)");
		}

		// 이용 문의(SITE)는 학년·과목·강의와 관계없으니 비움
		if (dto.getQuestionType() == QuestionType.SITE) {
			dto.setCatId(null);
			dto.setLecId(null);
		}
	}

	// 고른 분류·강의가 실제로 있는지 (없는 번호면 FK 오류 대신 안내 문구)
	private void checkCategoryAndLecture(Connection conn, QuestionDTO dto) throws SQLException {
		if (dto.getCatId() != null && !qnaDAO.categoryExists(conn, dto.getCatId())) {
			throw new ModoException("없는 학년·과목입니다.");
		}
		if (dto.getLecId() != null && !qnaDAO.lectureExists(conn, dto.getLecId())) {
			throw new ModoException("없는 강의입니다.");
		}
	}
}
