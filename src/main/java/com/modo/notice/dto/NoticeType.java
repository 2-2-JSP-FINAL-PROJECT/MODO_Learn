package com.modo.notice.dto;

// 성환작성
// 공지 구분 (notice.n_type / DB는 check)
public enum NoticeType {

	NOTICE("공지"),
	EVENT("이벤트"),
	INFO("안내");

	private final String label;

	private NoticeType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

}
