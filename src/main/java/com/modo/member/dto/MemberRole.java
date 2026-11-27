package com.modo.member.dto;

//재관작성
public enum MemberRole {

	USER("회원"),
	ADMIN("관리자");

	private final String label;

	private MemberRole(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

}
