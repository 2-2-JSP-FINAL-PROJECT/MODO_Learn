package com.modo.qna.dto;

import java.time.LocalDateTime;

import com.modo.common.enums.YnType;

// 성환작성
// Q&A 답변 1개를 담는 DTO (answer 테이블 + 작성자 이름)
public class AnswerDTO {

	private int answerId;                     // A_ID           답변 번호 (AUTO_INCREMENT)
	private int questionId;                   // Q_ID           어느 질문의 답변인지
	private String memberId;                  // MEMBER_ID      답변 작성자 아이디 (관리자)
	private String content;                   // CONTENT        답변 내용 (4000자 이내)
	private YnType delYn;                     // DEL_YN         삭제 여부 Y / N
	private LocalDateTime regDate;            // REG_DATE       등록일
	private LocalDateTime modDate;            // MOD_DATE       수정일 (DB가 자동 갱신)

	// answer 테이블 컬럼이 아니라 JOIN으로 가져오는 값
	private String writerName;                // member.MEMBER_NAME   답변 작성자 이름

	public AnswerDTO() {
	}

	public int getAnswerId() {
		return answerId;
	}

	public void setAnswerId(int answerId) {
		this.answerId = answerId;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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


	@Override
	public String toString() {
		return "AnswerDTO [answerId=" + answerId + ", questionId=" + questionId + ", memberId=" + memberId
				+ ", content=" + content + ", delYn=" + delYn + ", regDate=" + regDate
				+ ", writerName=" + writerName + "]";
	}
}
