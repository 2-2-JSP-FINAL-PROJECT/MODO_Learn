package com.modo.common.filter;

import com.modo.member.dto.LoginMemberDTO;
import com.modo.member.service.MemberException;
import com.modo.member.service.LoginService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Set;

/** REQUEST에 적용. 공개 경로 외의 URL은 기본적으로 회원 전용이다. */
public final class LoginFilter implements Filter {
    private final LoginService loginService = new LoginService();
    private static final Set<String> PUBLIC = Set.of("/member/login", "/member/signup", "/member/check-id", "/member/check-email", "/favicon.ico");
    @Override public void doFilter(ServletRequest rawRequest, ServletResponse rawResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) rawRequest;
        HttpServletResponse response = (HttpServletResponse) rawResponse;
        String path = request.getServletPath();
        if (request.getPathInfo() != null) path += request.getPathInfo();
        boolean asset = path.startsWith("/assets/") || path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/");
        HttpSession session = request.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("loginMember") instanceof LoginMemberDTO;
        if (PUBLIC.contains(path) || asset) {
            if (!asset) response.setHeader("Cache-Control", "no-store");
            chain.doFilter(request, response);
            return;
        }
        if (loggedIn) {
            try {
                LoginMemberDTO member = (LoginMemberDTO) session.getAttribute("loginMember");
                session.setAttribute("loginMember", loginService.currentMember(member.getMemberId()));
            } catch (MemberException e) { session.invalidate(); loggedIn = false; }
            catch (java.sql.SQLException e) {
                request.getServletContext().log("Login status lookup failed", e);
                response.setStatus(503);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().print("로그인 상태를 확인하지 못했습니다. 잠시 후 다시 시도해 주세요.");
                return;
            }
            if (loggedIn) {
                response.setHeader("Cache-Control", "no-store");
                chain.doFilter(request, response);
                return;
            }
        }
        if ("GET".equals(request.getMethod())) {
            session = request.getSession(true);
            session.setAttribute("loginRedirect", request.getRequestURI());
        }
        response.setHeader("Cache-Control", "no-store");
        response.sendRedirect(request.getContextPath() + "/member/login");
    }
}
