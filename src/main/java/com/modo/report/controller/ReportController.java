package com.modo.report.controller;

import java.io.IOException;

import com.modo.common.exception.ModoException;
import com.modo.member.dto.LoginUser;
import com.modo.report.dto.ReasonType;
import com.modo.report.dto.ReportDTO;
import com.modo.report.service.ReportService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// 성환작성
// 링크 오류 신고 접수 (강의 상세 페이지의 신고 창에서 fetch로 호출)
//  POST /report/write   파라미터 : lecId, reasonType, content
//
// 신고 창은 페이지 이동 없이 자바스크립트로 보내므로 JSP 대신 JSON으로 응답
//  성공      200 {"ok":true,  "message":"..."}
//  로그인 X  401 {"ok":false, "message":"..."}  -> 화면(JS)에서 로그인 페이지로 이동
//  입력 오류 400 {"ok":false, "message":"..."}
//  DB 오류   500 {"ok":false, "message":"..."}
@WebServlet("/report/*")  // report 시작 경로 다 받은 다음 -> switch로 분기처리
public class ReportController extends HttpServlet {

	private final ReportService reportService = new ReportService();

	// GET은 만들지 않음 (신고는 등록만 있음 -> GET으로 오면 405)

	@Override //Create 요청 처리
	protected void doPost(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {

		String path = req.getPathInfo();
		// 잘못된 경로 처리
		if (path == null) path = "/";

		switch (path) {
		case "/write":   // 신고 접수
			write(req, resp);
			break;
		default:
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}


	// 신고 접수 처리
	private void write(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {

		// 로그인 안 했으면 401 (fetch라서 redirect 대신 JSON으로 알려 줌)
		LoginUser loginUser = getLoginUser(req);
		if (loginUser == null) {
			sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, false, "로그인 후 신고할 수 있습니다.");
			return;
		}

		ReportDTO dto = new ReportDTO();
		dto.setContent(trim(req.getParameter("content")));   // 빈 값이면 null -> DB에 NULL

		try {
			dto.setLecId(parseLecId(req.getParameter("lecId")));
			dto.setReasonType(parseReasonType(req.getParameter("reasonType")));

			reportService.write(loginUser, dto);
			sendJson(resp, HttpServletResponse.SC_OK, true, "신고가 접수되었습니다. 확인 후 조치하겠습니다.");

		} catch (ModoException e) {
			// 원인(cause)이 있으면 DB 오류(500), 없으면 입력 규칙 위반(400)
			int status = (e.getCause() != null)
					? HttpServletResponse.SC_INTERNAL_SERVER_ERROR
					: HttpServletResponse.SC_BAD_REQUEST;
			sendJson(resp, status, false, e.getMessage());
		}
	}


	// {"ok":true,"message":"..."} 형태로 응답
	private void sendJson(HttpServletResponse resp, int status, boolean ok, String message)
			throws IOException {
		resp.setStatus(status);
		resp.setContentType("application/json; charset=UTF-8");
		// 메시지 안의 \ 와 " 가 JSON을 깨뜨리지 않도록 앞에 \ 를 붙임
		String safe = message.replace("\\", "\\\\").replace("\"", "\\\"");
		resp.getWriter().write("{\"ok\":" + ok + ",\"message\":\"" + safe + "\"}");
	}

	// 세션에서 로그인 정보 꺼내기 (없으면 null)
	private LoginUser getLoginUser(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		return (session == null) ? null : (LoginUser) session.getAttribute("loginUser");
	}

	private String trim(String value) {
		if (value == null) return null;
		value = value.trim();
		return value.isEmpty() ? null : value;
	}

	// 강의 번호 문자열 -> int
	private int parseLecId(String value) {
		value = trim(value);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {   // null도 여기로 옴
			throw new ModoException("강의 번호가 올바르지 않습니다.");
		}
	}

	// 신고 사유 문자열 -> ENUM (선택 안 하면 null -> Service에서 안내)
	private ReasonType parseReasonType(String value) {
		value = trim(value);
		if (value == null) return null;
		try {
			return ReasonType.valueOf(value);
		} catch (IllegalArgumentException e) {
			throw new ModoException("신고 사유를 선택해주세요.");
		}
	}

}
