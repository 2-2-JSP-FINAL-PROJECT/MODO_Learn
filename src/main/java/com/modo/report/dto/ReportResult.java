package com.modo.report.dto;

// 성환작성
// 신고 처리 결과 (report.result / 대기 중이면 NULL / DB는 check)
public enum ReportResult {

	FIXED("링크 수정"),
	HIDDEN("강의 숨김"),
	NO_PROBLEM("문제 없음");

	private final String label;

	private ReportResult(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

}
