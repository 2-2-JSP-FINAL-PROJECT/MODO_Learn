package com.modo.report.dto;

// 성환작성
// 신고 사유 (report.reason_type / DB는 check)
public enum ReasonType {

	DELETED("영상이 삭제됨"),
	PRIVATE("비공개 영상"),
	WRONG_CATEGORY("분류가 잘못됨"),
	ETC("기타");

	private final String label;

	private ReasonType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

}
