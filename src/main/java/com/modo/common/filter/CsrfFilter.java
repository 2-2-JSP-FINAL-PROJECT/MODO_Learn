package com.modo.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

/** 표준 Filter로 회원 폼 토큰 발급과 POST 검증을 처리한다. */
public final class CsrfFilter implements Filter {
    private static final SecureRandom RANDOM = new SecureRandom();
    @Override public void doFilter(ServletRequest rawRequest, ServletResponse rawResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) rawRequest;
        HttpServletResponse response = (HttpServletResponse) rawResponse;
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        String path = request.getServletPath();
        if (request.getPathInfo() != null) path += request.getPathInfo();
        if ("GET".equals(request.getMethod()) && Set.of("/member/login", "/member/signup").contains(path)) {
            HttpSession session = request.getSession(true);
            String token;
            synchronized (session) {
                Object old = session.getAttribute("csrfToken");
                if (old instanceof String) token = (String) old;
                else {
                    byte[] bytes = new byte[32]; RANDOM.nextBytes(bytes);
                    token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                    session.setAttribute("csrfToken", token);
                }
            }
            request.setAttribute("csrfToken", token);
            response.setHeader("X-CSRF-Token", token);
        }
        if ("POST".equals(request.getMethod()) && Set.of("/member/login", "/member/signup", "/member/logout").contains(path)) {
            String contentType = request.getContentType();
            if (contentType == null || !contentType.split(";", 2)[0].trim().equalsIgnoreCase("application/x-www-form-urlencoded")) {
                response.setStatus(415); response.setHeader("X-Error-Code", "UNSUPPORTED_MEDIA_TYPE");
                response.setContentType("text/plain;charset=UTF-8"); response.getWriter().print("폼 인코딩으로 요청해 주세요."); return;
            }
            HttpSession session = request.getSession(false);
            Object stored = session == null ? null : session.getAttribute("csrfToken");
            String supplied = request.getHeader("X-CSRF-Token");
            if (supplied == null) supplied = request.getParameter("csrfToken");
            if (!(stored instanceof String) || supplied == null || supplied.length() > 128
                    || !MessageDigest.isEqual(((String) stored).getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
                response.setStatus(403); response.setHeader("X-Error-Code", "CSRF_INVALID");
                response.setContentType("text/plain;charset=UTF-8"); response.getWriter().print("로그인 또는 가입 페이지에서 요청 토큰을 다시 발급받아 주세요."); return;
            }
            request.setAttribute("csrfToken", stored);
        }
        chain.doFilter(request, response);
    }
}
