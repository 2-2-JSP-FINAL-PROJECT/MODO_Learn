package com.modo.qna.dto;

import java.time.LocalDateTime;

import com.modo.common.enums.SchoolLevel;
import com.modo.common.enums.YnType;

// 성환작성
// Q&A 질문 1개를 담는 DTO (question 테이블 + question_summary 뷰)
// QuestionType, QuestionStatus, DelYn, SchoolLevel은 ENUM값으로 관리(DB는 check)
public class QuestionDTO {

	// ----- question 테이블 컬럼 -----
	private int questionId;                   // Q_ID           질문 번호 (AUTO_INCREMENT)
	private String memberId;                  // MEMBER_ID      작성자 아이디
	private Integer catId;                    // CAT_ID         학년·과목 (선택, 없으면 null)
	private Integer lecId;                    // LEC_ID         관련 강의 (선택, 없으면 null)
	private QuestionType questionType;        // Q_TYPE         STUDY / SITE
	private String title;                     // TITLE          제목 (200자 이내)
	private String content;                   // CONTENT        내용 (4000자 이내)
	private int viewCount;                    // VIEW_COUNT     조회수
	private QuestionStatus questionStatus;    // Q_STATUS       WAIT / ANSWERED
	private YnType delYn;                     // DEL_YN         삭제 여부 Y / N
	private LocalDateTime regDate;            // REG_DATE       등록일
	private LocalDateTime modDate;            // MOD_DATE       수정일 (DB가 자동 갱신)

	// ----- question_summary 뷰에서 더해지는 값 (JOIN) -----
	private String writerName;                // WRITER_NAME    작성자 이름 (member.member_name)
	private SchoolLevel schoolLevel;          // SCHOOL_LEVEL   학교급 (분류 없으면 null)
	private Integer grade;                    // GRADE          학년 (분류 없으면 null)
	private String subjectName;               // SUBJECT_NAME   과목 (분류 없으면 null)
	private int answerCount;                  // ANSWER_COUNT   답변 수

	public QuestionDTO() {
	}

	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public Integer getCatId() {
		return catId;
	}

	public void setCatId(Integer catId) {
		this.catId = catId;
	}

	public Integer getLecId() {
		return lecId;
	}

	public void setLecId(Integer lecId) {
		this.lecId = lecId;
	}

	public QuestionType getQuestionType() {
		return questionType;
	}

	public void setQuestionType(QuestionType questionType) {
		this.questionType = questionType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public QuestionStatus getQuestionStatus() {
		return questionStatus;
	}

	public void setQuestionStatus(QuestionStatus questionStatus) {
		this.questionStatus = questionStatus;
	}

	public YnType getDelYn() {
		return delYn;
	}

	public void setDelYn(YnType delYn) {
		this.delYn = delYn;
	}

	public LocalDateTime getRegDate() {
		return regDate;
	}

	public void setRegDate(LocalDateTime regDate) {
		this.regDate = regDate;
	}

	public LocalDateTime getModDate() {
		return modDate;
	}

	public void setModDate(LocalDateTime modDate) {
		this.modDate = modDate;
	}

	public String getWriterName() {
		return writerName;
	}

	public void setWriterName(String writerName) {
		this.writerName = writerName;
	}

	public SchoolLevel getSchoolLevel() {
		return schoolLevel;
	}

	public void setSchoolLevel(SchoolLevel schoolLevel) {
		this.schoolLevel = schoolLevel;
	}

	public Integer getGrade() {
		return grade;
	}

	public void setGrade(Integer grade) {
		this.grade = grade;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public int getAnswerCount() {
		return answerCount;
	}

	public void setAnswerCount(int answerCount) {
		this.answerCount = answerCount;
	}


	@Override
	public String toString() {
		return "QuestionDTO [questionId=" + questionId + ", memberId=" + memberId + ", catId=" + catId
				+ ", lecId=" + lecId + ", questionType=" + questionType + ", title=" + title
				+ ", viewCount=" + viewCount + ", questionStatus=" + questionStatus + ", delYn=" + delYn
				+ ", regDate=" + regDate + ", writerName=" + writerName + ", answerCount=" + answerCount + "]";
	}
}
