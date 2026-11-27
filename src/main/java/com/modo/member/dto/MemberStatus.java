package com.modo.member.dto;

//재관작성
public enum MemberStatus {

	ACTIVE("정상"),
	BLOCKED("차단");

	private final String label;

	MemberStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
