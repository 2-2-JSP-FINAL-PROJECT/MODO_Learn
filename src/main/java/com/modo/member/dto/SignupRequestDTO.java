package com.modo.member.dto;

public record SignupRequestDTO(String memberId, String memberPw, String memberPwConfirm,
        String memberName, String email, String schoolLevel, String grade) {
    @Override public String toString() { return "SignupRequestDTO[redacted]"; }
}
