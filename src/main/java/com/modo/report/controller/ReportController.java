package com.modo.report.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.modo.member.dto.LoginUser;
import com.modo.report.dto.ReportDto;
import com.modo.report.service.ReportService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/*
 * 링크 오류 신고 접수 (강의 상세 페이지의 신고 창에서 호출)
 *
 *  POST /report/write   파라미터 : lecId, reasonType, content
 *
 *  신고 창은 페이지를 이동하지 않고 자바스크립트(fetch)로 보내므로,
 *  화면(JSP) 대신 JSON 을 돌려준다.
 *    성공      200 {"ok":true,  "message":"신고가 접수되었습니다."}
 *    로그인 X  401 {"ok":false, "message":"로그인 후 신고할 수 있습니다."}  → 화면에서 로그인 페이지로 이동
 *    입력 오류 400 {"ok":false, "message":"..."}
 *
 *  ※ 로그인 정보는 세션의 "loginUser"(LoginUser) 에서 꺼낸다 (QnaController 와 같음)
 */
@WebServlet("/report/write")
public class ReportController extends HttpServlet {

	private ReportService reportService = new ReportService();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 팀 로그인이 세션에 넣어 둔 "loginUser" 에서 아이디를 꺼낸다 (로그인 안 했으면 null)
		HttpSession session = request.getSession(false);
		LoginUser loginUser = (session == null) ? null : (LoginUser) session.getAttribute("loginUser");
		String loginId = (loginUser == null) ? null : loginUser.getMemberId();

		try {
			ReportDto r = new ReportDto();
			r.setLecId(Integer.parseInt(request.getParameter("lecId")));
			r.setReasonType(request.getParameter("reasonType"));
			r.setContent(request.getParameter("content"));

			reportService.write(r, loginId);
			sendJson(response, 200, true, "신고가 접수되었습니다. 확인 후 조치하겠습니다.");

		} catch (NumberFormatException e) {          // lecId 가 비었거나 숫자가 아님
			sendJson(response, 400, false, "강의 번호가 올바르지 않습니다.");
		} catch (SecurityException e) {
			sendJson(response, 401, false, e.getMessage());
		} catch (IllegalArgumentException e) {
			sendJson(response, 400, false, e.getMessage());
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}// doPost

	// {"ok":true,"message":"..."} 형태로 응답
	private void sendJson(HttpServletResponse response, int status, boolean ok, String message)
			throws IOException {
		response.setStatus(status);
		response.setContentType("application/json; charset=UTF-8");
		// 메시지 안의 " 와 \ 가 JSON 을 깨뜨리지 않도록 앞에 \ 를 붙인다
		String safe = message.replace("\\", "\\\\").replace("\"", "\\\"");
		response.getWriter().write("{\"ok\":" + ok + ",\"message\":\"" + safe + "\"}");
	}

}// ReportController
