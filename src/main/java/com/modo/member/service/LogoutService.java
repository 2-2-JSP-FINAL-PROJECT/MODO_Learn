package com.modo.member.service;

import jakarta.servlet.http.HttpSession;

/** 로그아웃은 DB 변경 없이 세션을 폐기한다. DAO/DTO는 필요하지 않다. */
public final class LogoutService {
    public void logout(HttpSession session) {
        if (session != null) session.invalidate();
    }
}
