package com.modo.qna.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.modo.common.enums.SchoolLevel;
import com.modo.common.enums.YnType;
import com.modo.qna.dto.AnswerDTO;
import com.modo.qna.dto.QuestionDTO;
import com.modo.qna.dto.QuestionStatus;
import com.modo.qna.dto.QuestionType;

// 성환작성
// Q&A DB 담당 (질문 + 답변 / Connection은 Service가 열고 닫음)
public class QnaDAO {

	// 답변 조회에 같이 쓰는 SELECT (작성자 이름까지 JOIN)
	private static final String SELECT_ANSWER =
			"SELECT a.*, m.member_name AS writer_name "
			+ "FROM answer a "
			+ "JOIN member m ON m.member_id = a.member_id ";

	// ===== 질문 =====

	// 질문 목록 (question_summary 뷰 : 삭제 안 된 질문 + 작성자 이름 + 학년·과목 + 답변 수)
	public List<QuestionDTO> findList(Connection conn) throws SQLException {

		String sql = "SELECT * FROM question_summary ORDER BY q_id DESC";

		List<QuestionDTO> list = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				list.add(questionDtoMapper(rs));
			}
		}
		return list;
	}

	// 질문 1개 조회 (뷰가 삭제된 글을 빼 주므로 없거나 삭제된 글이면 null)
	public QuestionDTO findById(Connection conn, int questionId) throws SQLException {

		String sql = "SELECT * FROM question_summary WHERE q_id = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, questionId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return questionDtoMapper(rs);
				}
				return null;
			}
		}
	}

	// 조회수 +1 (바뀐 줄 수 반환 -> 0이면 없는 글)
	public int increaseViewCount(Connection conn, int questionId) throws SQLException {

		String sql = "UPDATE question SET view_count = view_count + 1 WHERE q_id = ? AND del_yn = 'N'";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, questionId);
			return ps.executeUpdate();
		}
	}

	// 질문 등록 -> 새로 생긴 질문 번호 반환 (VIEW_COUNT, Q_STATUS, DEL_YN, REG_DATE는 DB 기본값)
	public int insertQuestion(Connection conn, QuestionDTO dto) throws SQLException {

		String sql = "INSERT INTO question (member_id, cat_id, lec_id, q_type, title, content) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";

		// RETURN_GENERATED_KEYS : AUTO_INCREMENT로 만들어진 번호를 돌려받기
		try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, dto.getMemberId());
			setNullableInt(ps, 2, dto.getCatId());   // 선택 안 하면 NULL
			setNullableInt(ps, 3, dto.getLecId());   // 선택 안 하면 NULL
			ps.setString(4, dto.getQuestionType().name());   // ENUM -> 문자열
			ps.setString(5, dto.getTitle());
			ps.setString(6, dto.getContent());
			ps.executeUpdate();

			try (ResultSet keys = ps.getGeneratedKeys()) {
				keys.next();
				return keys.getInt(1);
			}
		}
	}

	// 질문 수정 (작성자 본인 확인은 Service에서 먼저 함, MOD_DATE는 DB가 자동 갱신)
	public int updateQuestion(Connection conn, QuestionDTO dto) throws SQLException {

		String sql = "UPDATE question SET cat_id = ?, lec_id = ?, q_type = ?, title = ?, content = ? "
				+ "WHERE q_id = ? AND del_yn = 'N'";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			setNullableInt(ps, 1, dto.getCatId());
			setNullableInt(ps, 2, dto.getLecId());
			ps.setString(3, dto.getQuestionType().name());
			ps.setString(4, dto.getTitle());
			ps.setString(5, dto.getContent());
			ps.setInt(6, dto.getQuestionId());
			return ps.executeUpdate();
		}
	}

	// 질문 삭제 (진짜 지우지 않고 DEL_YN = 'Y'로 바꾸는 소프트 삭제)
	public int deleteQuestion(Connection conn, int questionId) throws SQLException {

		String sql = "UPDATE question SET del_yn = 'Y' WHERE q_id = ? AND del_yn = 'N'";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, questionId);
			return ps.executeUpdate();
		}
	}

	// 질문 상태 바꾸기 (WAIT / ANSWERED)
	public int updateStatus(Connection conn, int questionId, QuestionStatus status) throws SQLException {

		String sql = "UPDATE question SET q_status = ? WHERE q_id = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, status.name());
			ps.setInt(2, questionId);
			return ps.executeUpdate();
		}
	}

	// 고른 학년·과목 분류가 있는지 (없는 번호면 FK 오류가 나기 전에 막기)
	public boolean categoryExists(Connection conn, int catId) throws SQLException {

		String sql = "SELECT COUNT(*) FROM category WHERE cat_id = ? AND del_yn = 'N'";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, catId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1) > 0;
			}
		}
	}

	// 고른 강의가 있는지
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

	// ===== 답변 =====

	// 질문 하나에 달린 답변 목록 (먼저 쓴 순서)
	public List<AnswerDTO> findAnswers(Connection conn, int questionId) throws SQLException {

		String sql = SELECT_ANSWER + "WHERE a.q_id = ? AND a.del_yn = 'N' ORDER BY a.a_id";

		List<AnswerDTO> list = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, questionId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(answerDtoMapper(rs));
				}
			}
		}
		return list;
	}

	// 답변 1개 조회 (답변 삭제할 때 어느 질문의 답변인지 알아내려고)
	public AnswerDTO findAnswerById(Connection conn, int answerId) throws SQLException {

		String sql = SELECT_ANSWER + "WHERE a.a_id = ? AND a.del_yn = 'N'";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, answerId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return answerDtoMapper(rs);
				}
				return null;
			}
		}
	}

	// 답변 등록 (DEL_YN, REG_DATE는 DB 기본값)
	public int insertAnswer(Connection conn, AnswerDTO dto) throws SQLException {

		String sql = "INSERT INTO answer (q_id, member_id, content) VALUES (?, ?, ?)";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, dto.getQuestionId());
			ps.setString(2, dto.getMemberId());
			ps.setString(3, dto.getContent());
			return ps.executeUpdate();
		}
	}

	// 답변 삭제 (소프트 삭제)
	public int deleteAnswer(Connection conn, int answerId) throws SQLException {

		String sql = "UPDATE answer SET del_yn = 'Y' WHERE a_id = ? AND del_yn = 'N'";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, answerId);
			return ps.executeUpdate();
		}
	}

	// 남아 있는 (삭제 안 된) 답변 수
	public int countAnswers(Connection conn, int questionId) throws SQLException {

		String sql = "SELECT COUNT(*) FROM answer WHERE q_id = ? AND del_yn = 'N'";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, questionId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	// ===== 도우미 =====

	// Integer 값이 null이면 DB에 NULL, 아니면 숫자를 넣음
	private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
		if (value == null) {
			ps.setNull(index, Types.INTEGER);
		} else {
			ps.setInt(index, value);
		}
	}

	// QuestionDTO로 변환 (목록·상세가 같이 씀)
	private QuestionDTO questionDtoMapper(ResultSet rs) throws SQLException {

		QuestionDTO dto = new QuestionDTO();

		dto.setQuestionId(rs.getInt("Q_ID"));
		dto.setMemberId(rs.getString("MEMBER_ID"));
		dto.setTitle(rs.getString("TITLE"));
		dto.setContent(rs.getString("CONTENT"));
		dto.setViewCount(rs.getInt("VIEW_COUNT"));

		// CAT_ID, LEC_ID는 null일 수 있어서 Integer로 받기
		dto.setCatId(rs.getObject("CAT_ID", Integer.class));
		dto.setLecId(rs.getObject("LEC_ID", Integer.class));

		// 문자열 -> ENUM값으로 변환
		dto.setQuestionType(QuestionType.valueOf(rs.getString("Q_TYPE")));
		dto.setQuestionStatus(QuestionStatus.valueOf(rs.getString("Q_STATUS")));
		dto.setDelYn(YnType.valueOf(rs.getString("DEL_YN")));

		// DATETIME -> LocalDateTime으로 변환
		dto.setRegDate(rs.getObject("REG_DATE", LocalDateTime.class));
		dto.setModDate(rs.getObject("MOD_DATE", LocalDateTime.class));

		// question_summary 뷰에서 더해진 값 (분류를 안 골랐으면 학교급·학년·과목은 NULL)
		dto.setWriterName(rs.getString("WRITER_NAME"));
		String schoolLevel = rs.getString("SCHOOL_LEVEL");
		dto.setSchoolLevel(schoolLevel == null ? null : SchoolLevel.valueOf(schoolLevel));
		dto.setGrade(rs.getObject("GRADE", Integer.class));
		dto.setSubjectName(rs.getString("SUBJECT_NAME"));
		dto.setAnswerCount(rs.getInt("ANSWER_COUNT"));

		return dto;
	}

	// AnswerDTO로 변환 (답변 목록·1개 조회가 같이 씀)
	private AnswerDTO answerDtoMapper(ResultSet rs) throws SQLException {

		AnswerDTO dto = new AnswerDTO();

		dto.setAnswerId(rs.getInt("A_ID"));
		dto.setQuestionId(rs.getInt("Q_ID"));
		dto.setMemberId(rs.getString("MEMBER_ID"));
		dto.setContent(rs.getString("CONTENT"));
		dto.setDelYn(YnType.valueOf(rs.getString("DEL_YN")));
		dto.setRegDate(rs.getObject("REG_DATE", LocalDateTime.class));
		dto.setModDate(rs.getObject("MOD_DATE", LocalDateTime.class));
		dto.setWriterName(rs.getString("WRITER_NAME"));

		return dto;
	}
}
