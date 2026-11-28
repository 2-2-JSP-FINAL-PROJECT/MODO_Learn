package com.modo.report.dto;

// 성환작성
// 신고 처리 상태 (report.report_status / DB는 check)
public enum ReportStatus {

	WAIT("처리 대기"),
	DONE("처리 완료");

	private final String label;

	private ReportStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

}
