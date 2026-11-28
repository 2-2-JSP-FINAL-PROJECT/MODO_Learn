package com.modo.qna.dto;

import java.sql.Timestamp;

// Q&A 답변 1개를 담는 그릇 (answer 테이블 + 작성자 이름)
public class AnswerDto {

	private int aId;               // a_id       답변 번호
	private int qId;               // q_id       어느 질문의 답변인지
	private String userId;         // user_id    답변 작성자 아이디
	private String content;        // content    답변 내용
	private String delYn;          // del_yn     Y / N
	private Timestamp regDate;     // reg_date   등록일
	private Timestamp modDate;     // mod_date   수정일
	private String writerName;     // users.user_name (JOIN으로 채움)

	public int getaId() {
		return aId;
	}
	public void setaId(int aId) {
		this.aId = aId;
	}
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
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
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

}
