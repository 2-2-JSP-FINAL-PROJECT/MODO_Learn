package com.modo.member.dto;

/** DAO와 Service 사이에서만 사용한다. 응답 및 세션에 저장하지 않는다. */
public record MemberDTO(LoginMemberDTO member, String passwordHash, String memberStatus, String delYn) {
    public boolean isActive() { return "ACTIVE".equals(memberStatus) && "N".equals(delYn); }
    @Override public String toString() { return "MemberDTO[redacted]"; }
}
