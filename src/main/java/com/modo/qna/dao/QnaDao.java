package com.modo.qna.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.modo.common.util.DBUtil;
import com.modo.qna.dto.AnswerDto;
import com.modo.qna.dto.QuestionDto;

public class QnaDao {

	public List<QuestionDto> findList() throws SQLException {
		String sql = "SELECT * FROM question_summary ORDER BY q_id DESC";
		List<QuestionDto> list = new ArrayList<>();

		try (Connection conn = DBUtil.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) { // 결과표의 다음 줄로 이동 (없으면 false)
				QuestionDto q = new QuestionDto();
				// question 테이블 컬럼
				q.setqId(rs.getInt("q_id"));
				q.setUserId(rs.getString("user_id"));
				q.setCatId(rs.getObject("cat_id", Integer.class)); // NULL 가능
				q.setLecId(rs.getObject("lec_id", Integer.class)); // NULL 가능
				q.setqType(rs.getString("q_type"));
				q.setTitle(rs.getString("title"));
				q.setContent(rs.getString("content"));
				q.setViewCount(rs.getInt("view_count"));
				q.setqStatus(rs.getString("q_status"));
				q.setDelYn(rs.getString("del_yn"));
				q.setRegDate(rs.getTimestamp("reg_date"));
				q.setModDate(rs.getTimestamp("mod_date"));

				// question_summary 뷰에서 추가된 컬럼
				q.setWriterName(rs.getString("writer_name"));
				q.setSchoolLevel(rs.getString("school_level"));
				q.setGrade(rs.getObject("grade", Integer.class)); // NULL 가능
				q.setSubjectName(rs.getString("subject_name"));
				q.setAnswerCount(rs.getInt("answer_count"));

				list.add(q);
			} // while
		}
		return list;
	}// findList

	public QuestionDto findById(int qId) throws SQLException {
		String sql = "SELECT * FROM question_summary WHERE q_id = ?";
		QuestionDto q = null; // 못 찾으면 null을 돌려줌

		try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			// TODO ① 첫 번째 ? 에 qId 넣기
			ps.setInt(1, qId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) { // 결과가 있으면 (최대 1줄)
					q = new QuestionDto();
					// TODO ② findList와 똑같이 17개 채우기
					q.setqId(rs.getInt("q_id"));
					q.setUserId(rs.getString("user_id"));
					q.setCatId(rs.getObject("cat_id", Integer.class)); // NULL 가능
					q.setLecId(rs.getObject("lec_id", Integer.class)); // NULL 가능
					q.setqType(rs.getString("q_type"));
					q.setTitle(rs.getString("title"));
					q.setContent(rs.getString("content"));
					q.setViewCount(rs.getInt("view_count"));
					q.setqStatus(rs.getString("q_status"));
					q.setDelYn(rs.getString("del_yn"));
					q.setRegDate(rs.getTimestamp("reg_date"));
					q.setModDate(rs.getTimestamp("mod_date"));

					// question_summary 뷰에서 추가된 컬럼
					q.setWriterName(rs.getString("writer_name"));
					q.setSchoolLevel(rs.getString("school_level"));
					q.setGrade(rs.getObject("grade", Integer.class)); // NULL 가능
					q.setSubjectName(rs.getString("subject_name"));
					q.setAnswerCount(rs.getInt("answer_count"));
				} // if
			} // try
		} // try

		return q;
	}// findbyid

	public int increaseViewCount(int qId) throws SQLException {
		String sql = "UPDATE question SET view_count=view_count+1 WHERE q_id= ?  AND del_yn='N'";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, qId);
			return ps.executeUpdate(); // 실제로 바뀐 줄 수를 돌려줌
		}
	}// increaseView

	public List<AnswerDto> findAnswers(int qId) throws SQLException {
		String sql="SELECT a.*, u.user_name AS writer_name\r\n"
				+ "  FROM answer a\r\n"
				+ "  JOIN users u ON u.user_id = a.user_id\r\n"
				+ " WHERE a.q_id = ?\r\n"
				+ "   AND a.del_yn = 'N'\r\n"
				+ " ORDER BY a.a_id";
		List<AnswerDto> list = new ArrayList<>();
		try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, qId);
			try (ResultSet rs = ps.executeQuery())
			{
				while(rs.next())
				{
					AnswerDto a= new AnswerDto();
					a.setqId(rs.getInt("q_id"));
					a.setaId(rs.getInt("a_id"));
					a.setUserId(rs.getString("user_id"));
					a.setContent(rs.getString("content"));
					a.setDelYn(rs.getString("del_yn"));
					a.setRegDate(rs.getTimestamp("reg_date"));
					a.setModDate(rs.getTimestamp("mod_date"));
					a.setWriterName(rs.getString("writer_name"));
					list.add(a);
				}
			}
			return list;
		}

	}//findanswer

	// =====================================================================
	// 여기부터 쓰기(INSERT / UPDATE) 메서드
	//  - 오라클 : 새 번호는 seq_테이블.NEXTVAL, mod_date 는 트리거가 자동 갱신
	//  - 삭제는 진짜 DELETE 가 아니라 del_yn = 'Y' 로 바꾸는 소프트 삭제
	// =====================================================================

	// 질문 등록 → 새로 만들어진 질문 번호를 돌려줌 (등록 후 상세 화면으로 보내려고)
	public int insertQuestion(QuestionDto q) throws SQLException {
		String seqSql = "SELECT seq_question.NEXTVAL FROM dual";
		String sql = "INSERT INTO question (q_id, user_id, cat_id, lec_id, q_type, title, content) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = DBUtil.getConnection()) {
			// ① 시퀀스에서 새 번호를 먼저 받는다
			int newId;
			try (PreparedStatement ps = conn.prepareStatement(seqSql);
					ResultSet rs = ps.executeQuery()) {
				rs.next();
				newId = rs.getInt(1);
			}
			// ② 그 번호로 INSERT
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setInt(1, newId);
				ps.setString(2, q.getUserId());
				setNullableInt(ps, 3, q.getCatId());   // 선택 안 하면 NULL
				setNullableInt(ps, 4, q.getLecId());   // 선택 안 하면 NULL
				ps.setString(5, q.getqType());
				ps.setString(6, q.getTitle());
				ps.setString(7, q.getContent());
				ps.executeUpdate();
			}
			return newId;
		}
	}// insertQuestion

	// 질문 수정 (작성자 본인 확인은 Service 에서 먼저 함)
	public int updateQuestion(QuestionDto q) throws SQLException {
		String sql = "UPDATE question SET cat_id = ?, lec_id = ?, q_type = ?, title = ?, content = ? "
				+ "WHERE q_id = ? AND del_yn = 'N'";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			setNullableInt(ps, 1, q.getCatId());
			setNullableInt(ps, 2, q.getLecId());
			ps.setString(3, q.getqType());
			ps.setString(4, q.getTitle());
			ps.setString(5, q.getContent());
			ps.setInt(6, q.getqId());
			return ps.executeUpdate();
		}
	}// updateQuestion

	// 질문 삭제 (소프트 삭제 : 데이터는 남기고 del_yn 만 'Y')
	public int deleteQuestion(int qId) throws SQLException {
		String sql = "UPDATE question SET del_yn = 'Y' WHERE q_id = ? AND del_yn = 'N'";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, qId);
			return ps.executeUpdate();
		}
	}// deleteQuestion

	// 답변 하나 찾기 (답변 삭제할 때 어느 질문의 답변인지 알아내려고)
	public AnswerDto findAnswerById(int aId) throws SQLException {
		String sql = "SELECT * FROM answer WHERE a_id = ? AND del_yn = 'N'";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, aId);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}
				AnswerDto a = new AnswerDto();
				a.setaId(rs.getInt("a_id"));
				a.setqId(rs.getInt("q_id"));
				a.setUserId(rs.getString("user_id"));
				a.setContent(rs.getString("content"));
				a.setDelYn(rs.getString("del_yn"));
				a.setRegDate(rs.getTimestamp("reg_date"));
				a.setModDate(rs.getTimestamp("mod_date"));
				return a;
			}
		}
	}// findAnswerById

	// ---------------------------------------------------------------------
	// 아래 3개는 Service 가 넘겨준 Connection 을 그대로 쓴다.
	// 답변 저장 + 질문 상태 변경을 "둘 다 성공 또는 둘 다 취소" 로 묶기 위해서
	// (그래서 여기서는 conn 을 닫지 않는다. 닫는 건 Service 가 함)
	// ---------------------------------------------------------------------

	// 답변 등록
	public int insertAnswer(Connection conn, AnswerDto a) throws SQLException {
		String sql = "INSERT INTO answer (a_id, q_id, user_id, content) "
				+ "VALUES (seq_answer.NEXTVAL, ?, ?, ?)";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, a.getqId());
			ps.setString(2, a.getUserId());
			ps.setString(3, a.getContent());
			return ps.executeUpdate();
		}
	}// insertAnswer

	// 답변 삭제 (소프트 삭제)
	public int deleteAnswer(Connection conn, int aId) throws SQLException {
		String sql = "UPDATE answer SET del_yn = 'Y' WHERE a_id = ? AND del_yn = 'N'";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, aId);
			return ps.executeUpdate();
		}
	}// deleteAnswer

	// 남아 있는 (삭제 안 된) 답변 수
	public int countAnswers(Connection conn, int qId) throws SQLException {
		String sql = "SELECT COUNT(*) FROM answer WHERE q_id = ? AND del_yn = 'N'";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, qId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}// countAnswers

	// 질문 상태 바꾸기 : 'WAIT' 또는 'ANSWERED'
	public int updateStatus(Connection conn, int qId, String status) throws SQLException {
		String sql = "UPDATE question SET q_status = ? WHERE q_id = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, status);
			ps.setInt(2, qId);
			return ps.executeUpdate();
		}
	}// updateStatus

	// Integer 값이 null 이면 DB 에 NULL, 아니면 숫자를 넣는다
	private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
		if (value == null) {
			ps.setNull(index, java.sql.Types.INTEGER);
		} else {
			ps.setInt(index, value);
		}
	}

}// QnaDao
