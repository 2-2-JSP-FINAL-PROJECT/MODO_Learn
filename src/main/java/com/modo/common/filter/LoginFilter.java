package com.modo.common.filter;

import java.io.IOException;

import com.modo.member.dto.LoginUser;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// 현호작성
// 로그인해야 들어갈 수 있는 경로만 등록 (강의 목록·상세는 비회원도 볼 수 있게 제외)
@WebFilter(urlPatterns = {"/mypage/*", "/admin/*"})
public class LoginFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        LoginUser loginUser = (session == null) ? null : (LoginUser) session.getAttribute("loginUser");

        // 로그인 안 했으면 로그인 화면으로
        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/member/login");
            return;
        }

        // 관리자 페이지는 ADMIN만
        if (req.getRequestURI().startsWith(req.getContextPath() + "/admin/") && !loginUser.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }
}
