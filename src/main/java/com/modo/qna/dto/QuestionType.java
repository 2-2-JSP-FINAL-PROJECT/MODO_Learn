package com.modo.qna.dto;

// 성환작성
// 질문 구분 (question.q_type / DB는 check)
public enum QuestionType {

	STUDY("학습 질문"),
	SITE("이용 문의");

	private final String label;

	private QuestionType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

}
