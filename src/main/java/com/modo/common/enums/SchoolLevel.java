package com.modo.common.enums;

// 현호작성
// 학교급 (member, category 공통 / DB는 check)
public enum SchoolLevel {

	ELEM("초등학교", 6),
	MIDDLE("중학교", 3),
	HIGH("고등학교", 3),
	NONE("해당 없음", 0);       // 학부모·교사 (회원만 사용, 학년 없음)

	private final String label;
	private final int maxGrade;

	private SchoolLevel(String label, int maxGrade) {
		this.label = label;
		this.maxGrade = maxGrade;
	}

	public String getLabel() {
		return label;
	}

	public int getMaxGrade() {
		return maxGrade;
	}

	// 학교급에 맞는 학년인지 확인 (NONE은 학년이 없어야 함)
	public boolean isValidGrade(Integer grade) {
		if (this == NONE) {
			return grade == null;
		}
		return grade != null && grade >= 1 && grade <= maxGrade;
	}

}
