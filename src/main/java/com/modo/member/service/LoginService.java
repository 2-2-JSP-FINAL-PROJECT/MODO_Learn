package com.modo.member.service;

import com.modo.common.util.PasswordUtil;
import com.modo.member.dao.MemberDAO;
import com.modo.member.dto.LoginMemberDTO;
import com.modo.member.dto.MemberDTO;
import java.sql.SQLException;

/** 로그인 및 로그인한 회원의 현재 상태 확인만 담당한다. */
public final class LoginService {
    private static final String DUMMY_HASH = PasswordUtil.hash("dummy-for-missing-account");
    private final MemberDAO dao;
    public LoginService() { this(new MemberDAO()); }
    public LoginService(MemberDAO dao) { this.dao = dao; }

    public LoginMemberDTO login(String memberId, String password) throws SQLException {
        String id = memberId == null ? "" : memberId.strip();
        if (!id.matches("[A-Za-z0-9_]{4,20}") || password == null || password.isEmpty() || password.length() > 128) throw denied();
        MemberDTO found = dao.findById(id).orElse(null);
        boolean matches = PasswordUtil.matches(password, found == null ? DUMMY_HASH : found.passwordHash());
        if (found == null || !matches || !found.isActive()) throw denied();
        return found.member();
    }
    public LoginMemberDTO currentMember(String id) throws SQLException {
        MemberDTO found = dao.findById(id).orElseThrow(LoginService::denied);
        if (!found.isActive()) throw denied();
        return found.member();
    }
    private static MemberException denied() {
        return new MemberException(401, "AUTHENTICATION_FAILED", "로그인 정보를 확인해 주세요.");
    }
}
