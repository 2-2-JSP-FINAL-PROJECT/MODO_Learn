package com.modo.qna.dto;

// 성환작성
// 답변 상태 (question.q_status / 답변이 하나라도 달리면 ANSWERED / DB는 check)
public enum QuestionStatus {

	WAIT("답변 대기"),
	ANSWERED("답변 완료");

	private final String label;

	private QuestionStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

}
