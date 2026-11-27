package com.modo.member.dto;

import java.time.LocalDateTime;

import com.modo.common.enums.SchoolLevel;
import com.modo.common.enums.YnType;



// 재관작성
public class MemberDTO {

	// MemberDTO -> SchoolLevel, Role, Status, DelYn은 ENUM값으로 관리(DB는 check)
	private String memberId;
	private String memberPw;
	private String memberName;
	private String email;
	private SchoolLevel schoolLevel;          // SCHOOL_LEVEL    ELEM / MIDDLE / HIGH / NONE
	private Integer grade;                    // GRADE           초등 1~6, 중·고 1~3, NONE은 null
	private MemberRole memberRole;            // MEMBER_ROLE     USER / ADMIN
	private MemberStatus memberStatus;        // MEMBER_STATUS   ACTIVE / BLOCKED
	private YnType delYn;                     // DEL_YN          Y / N
	private LocalDateTime regDate;
	private LocalDateTime modDate;

	public MemberDTO() {
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getMemberPw() {
		return memberPw;
	}

	public void setMemberPw(String memberPw) {
		this.memberPw = memberPw;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public SchoolLevel getSchoolLevel() {
		return schoolLevel;
	}

	public void setSchoolLevel(SchoolLevel schoolLevel) {
		this.schoolLevel = schoolLevel;
	}

	public Integer getGrade() {
		return grade;
	}

	public void setGrade(Integer grade) {
		this.grade = grade;
	}

	public MemberRole getMemberRole() {
		return memberRole;
	}

	public void setMemberRole(MemberRole memberRole) {
		this.memberRole = memberRole;
	}

	public MemberStatus getMemberStatus() {
		return memberStatus;
	}

	public void setMemberStatus(MemberStatus memberStatus) {
		this.memberStatus = memberStatus;
	}

	public YnType getDelYn() {
		return delYn;
	}

	public void setDelYn(YnType delYn) {
		this.delYn = delYn;
	}

	public LocalDateTime getRegDate() {
		return regDate;
	}

	public void setRegDate(LocalDateTime regDate) {
		this.regDate = regDate;
	}

	public LocalDateTime getModDate() {
		return modDate;
	}

	public void setModDate(LocalDateTime modDate) {
		this.modDate = modDate;
	}


	@Override
	public String toString() {
		return "MemberDTO [memberId=" + memberId + ", memberName=" + memberName
				+ ", email=" + email + ", schoolLevel=" + schoolLevel + ", grade=" + grade
				+ ", memberRole=" + memberRole + ", memberStatus=" + memberStatus
				+ ", delYn=" + delYn + ", regDate=" + regDate + "]";
	}
}
