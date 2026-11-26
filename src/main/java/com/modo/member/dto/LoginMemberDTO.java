package com.modo.member.dto;

import java.io.Serializable;

/** 세션 전용. 비밀번호 해시는 포함하지 않는다. JSP EL에서 getter로 접근한다. */
public final class LoginMemberDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String memberId, memberName, email, schoolLevel, memberRole;
    private final Integer grade;
    public LoginMemberDTO(String memberId, String memberName, String email, String schoolLevel, Integer grade, String memberRole) {
        this.memberId = memberId; this.memberName = memberName; this.email = email;
        this.schoolLevel = schoolLevel; this.grade = grade; this.memberRole = memberRole;
    }
    public String getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public String getEmail() { return email; }
    public String getSchoolLevel() { return schoolLevel; }
    public Integer getGrade() { return grade; }
    public String getMemberRole() { return memberRole; }
}
