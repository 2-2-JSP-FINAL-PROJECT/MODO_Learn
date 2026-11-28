package com.modo.qna.dto;

import java.sql.Timestamp;

// Q&A 질문 1개를 담는 그릇 (question 테이블 + question_summary 뷰)
public class QuestionDto {

	// ---- question 테이블 ----
	private int qId;               // q_id       질문 번호
	private String userId;         // user_id     작성자 아이디
	private Integer catId;         // cat_id      학년·과목 (NULL 가능)
	private Integer lecId;         // lec_id      관련 강의 (NULL 가능)
	private String qType;          // q_type      STUDY / SITE
	private String title;          // title       제목
	private String content;        // content     내용
	private int viewCount;         // view_count  조회수
	private String qStatus;        // q_status    WAIT / ANSWERED
	private String delYn;          // del_yn      Y / N
	private Timestamp regDate;     // reg_date    등록일
	private Timestamp modDate;     // mod_date    수정일

	// ---- question_summary 뷰에서 추가되는 값 ----
	private String writerName;     // writer_name   작성자 이름
	private String schoolLevel;    // school_level  학교급 (NULL 가능)
	private Integer grade;         // grade         학년 (NULL 가능)
	private String subjectName;    // subject_name  과목 (NULL 가능)
	private int answerCount;       // answer_count  답변 수
	public int getqId() {
		return qId;
	}
	public void setqId(int qId) {
		this.qId = qId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
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
	public String getqType() {
		return qType;
	}
	public void setqType(String qType) {
		this.qType = qType;
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
	public String getqStatus() {
		return qStatus;
	}
	public void setqStatus(String qStatus) {
		this.qStatus = qStatus;
	}
	public String getDelYn() {
		return delYn;
	}
	public void setDelYn(String delYn) {
		this.delYn = delYn;
	}
	public Timestamp getRegDate() {
		return regDate;
	}
	public void setRegDate(Timestamp regDate) {
		this.regDate = regDate;
	}
	public Timestamp getModDate() {
		return modDate;
	}
	public void setModDate(Timestamp modDate) {
		this.modDate = modDate;
	}
	public String getWriterName() {
		return writerName;
	}
	public void setWriterName(String writerName) {
		this.writerName = writerName;
	}
	public String getSchoolLevel() {
		return schoolLevel;
	}
	public void setSchoolLevel(String schoolLevel) {
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

}
