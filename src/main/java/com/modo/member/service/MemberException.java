package com.modo.member.service;

public final class MemberException extends RuntimeException {
    private final int status;
    private final String code;
    public MemberException(int status, String code, String message) { super(message); this.status = status; this.code = code; }
    public int getStatus() { return status; }
    public String getCode() { return code; }
}
