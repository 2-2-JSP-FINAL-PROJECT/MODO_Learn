package com.modo.member.controller;

import com.modo.member.dto.LoginMemberDTO;
import com.modo.member.dto.SignupRequestDTO;
import com.modo.member.service.LoginService;
import com.modo.member.service.LogoutService;
import com.modo.member.service.MemberException;
import com.modo.member.service.SignupService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Set;

/** 회원 기능의 Controller. SQL·비밀번호 검증은 Model 계층에 위임한다. */
@WebServlet("/member/*")
public final class MemberServlet extends HttpServlet {
    private final LoginService loginService = new LoginService();
    private final SignupService signupService = new SignupService();
    private final LogoutService logoutService = new LogoutService();
    private static final Set<String> ROUTES = Set.of("/login", "/signup", "/logout", "/me", "/check-id", "/check-email");

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null || !ROUTES.contains(path)) { response.sendError(404); return; }
        try {
            switch (path) {
                case "/login", "/signup" -> forward(request, response, path.substring(1));
                case "/me" -> showMember(request, response);
                case "/check-id" -> plain(response, 200, Boolean.toString(signupService.isIdAvailable(request.getParameter("memberId"))));
                case "/check-email" -> plain(response, 200, Boolean.toString(signupService.isEmailAvailable(request.getParameter("email"))));
                case "/logout" -> { response.setHeader("Allow", "POST"); response.sendError(405); }
                default -> throw new IllegalStateException("Unhandled member route");
            }
        } catch (MemberException e) { memberError(request, response, e, null); }
        catch (SQLException e) { databaseError(response, e); }
    }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null || !ROUTES.contains(path)) { response.sendError(404); return; }
        if (!Set.of("/login", "/signup", "/logout").contains(path)) {
            response.setHeader("Allow", "GET"); response.sendError(405); return;
        }
        try {
            switch (path) {
                case "/login" -> login(request, response);
                case "/signup" -> {
                    SignupRequestDTO input = new SignupRequestDTO(
                            request.getParameter("memberId"), request.getParameter("memberPw"),
                            request.getParameter("memberPwConfirm"), request.getParameter("memberName"),
                            request.getParameter("email"), request.getParameter("schoolLevel"), request.getParameter("grade"));
                    signupService.signup(input);
                    redirect(response, request.getContextPath() + "/member/login?signup=success");
                }
                case "/logout" -> {
                    logoutService.logout(request.getSession(false));
                    redirect(response, request.getContextPath() + "/member/login?logout=success");
                }
                default -> throw new IllegalStateException("Unhandled member route");
            }
        } catch (MemberException e) {
            memberError(request, response, e, path.equals("/logout") ? null : path.substring(1));
        } catch (SQLException e) { databaseError(response, e); }
    }

    private void login(HttpServletRequest request, HttpServletResponse response) throws SQLException {
        LoginMemberDTO member = loginService.login(request.getParameter("memberId"), request.getParameter("memberPw"));
        HttpSession oldSession = request.getSession(false);
        Object destination = oldSession == null ? null : oldSession.getAttribute("loginRedirect");
        if (oldSession != null) oldSession.invalidate();
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(1800);
        session.setAttribute("loginMember", member);
        byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute("csrfToken", token);
        response.setHeader("X-CSRF-Token", token);
        String context = request.getContextPath();
        String target = context + "/member/me";
        if (destination instanceof String path && path.startsWith(context + "/") && !path.startsWith("//")
                && !path.contains("\\") && !path.contains("%") && !path.contains("\r") && !path.contains("\n") && !path.contains("?")) target = path;
        redirect(response, target);
    }

    private void showMember(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("loginMember") instanceof LoginMemberDTO member))
            throw new MemberException(401, "LOGIN_REQUIRED", "로그인이 필요합니다.");
        LoginMemberDTO current;
        try { current = loginService.currentMember(member.getMemberId()); }
        catch (MemberException e) { session.invalidate(); throw e; }
        session.setAttribute("loginMember", current);
        request.setAttribute("member", current);
        response.setHeader("X-Member-Id", current.getMemberId());
        forward(request, response, "me");
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        String path = "/WEB-INF/views/member/" + view + ".jsp";
        if (getServletContext().getResource(path) != null) request.getRequestDispatcher(path).forward(request, response);
        else response.setStatus(204); // View 제외 범위: JSP가 추가되면 forward만 수행한다.
    }

    private void memberError(HttpServletRequest request, HttpServletResponse response, MemberException error, String view)
            throws ServletException, IOException {
        response.setStatus(error.getStatus());
        response.setHeader("X-Error-Code", error.getCode());
        request.setAttribute("errorCode", error.getCode());
        request.setAttribute("errorMessage", error.getMessage());
        if (view != null && getServletContext().getResource("/WEB-INF/views/member/" + view + ".jsp") != null)
            request.getRequestDispatcher("/WEB-INF/views/member/" + view + ".jsp").forward(request, response);
        else plain(response, error.getStatus(), error.getMessage());
    }
    private void databaseError(HttpServletResponse response, SQLException error) throws IOException {
        getServletContext().log("Member database operation failed", error);
        response.setHeader("X-Error-Code", "DATABASE_ERROR");
        plain(response, 503, "회원 정보를 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.");
    }
    private void plain(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status); response.setContentType("text/plain;charset=UTF-8"); response.getWriter().print(message);
    }
    private void redirect(HttpServletResponse response, String target) {
        response.setStatus(303); response.setHeader("Location", target);
    }
}
