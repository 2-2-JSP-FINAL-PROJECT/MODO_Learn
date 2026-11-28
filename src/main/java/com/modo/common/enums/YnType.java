package com.modo.common.enums;

// 현호작성
// Y/N 공통 (del_yn, hide_yn, pin_yn 등 _yn 컬럼 전부 / DB는 check)
public enum YnType {

	Y("예"),
	N("아니오");

	private final String label;

	private YnType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public boolean isYes() {
		return this == Y;
	}

}
