package com.modo.report.controller;

import java.io.IOException;

import com.modo.common.exception.ModoException;
import com.modo.member.dto.LoginUser;
import com.modo.report.dto.ReportResult;
import com.modo.report.dto.ReportStatus;
import com.modo.report.service.ReportService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// 성환작성
// 링크 오류 신고 관리자 화면 (LoginFilter가 /admin/* 을 관리자만 들어오게 막음)
//  GET  /admin/report/list               신고 목록 (WAIT 먼저)
//  GET  /admin/report/list?status=WAIT   상태별 목록 (WAIT / DONE)
//  POST /admin/report/process            신고 처리 (파라미터 : reportId, result)
@WebServlet("/admin/report/*")
public class AdminReportController extends HttpServlet {

	private final ReportService reportService = new ReportService();

	@Override //Get요청 Read처리
	protected void doGet(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {

		// 로그인 안 했으면 로그인 화면으로
		if (getLoginUser(req) == null) {
			resp.sendRedirect(req.getContextPath() + "/member/login");
			return;
		}

		String path = req.getPathInfo();
		// 잘못된 경로 처리
		if (path == null) path = "/";

		switch (path) {
		case "/list":   // 신고 목록
			list(req, resp);
			break;
		default:
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}


	@Override //Update 요청 처리
	protected void doPost(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {

		// 로그인 안 했으면 로그인 화면으로
		if (getLoginUser(req) == null) {
			resp.sendRedirect(req.getContextPath() + "/member/login");
			return;
		}

		String path = req.getPathInfo();
		// 잘못된 경로 처리
		if (path == null) path = "/";

		switch (path) {
		case "/process":   // 신고 처리
			process(req, resp);
			break;
		default:
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}


	// 신고 목록
	private void list(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {

		// status가 없거나 이상한 값이면 null -> 전체 목록
		ReportStatus status = parseStatus(req.getParameter("status"));

		try {
			req.setAttribute("reportList", reportService.getList(getLoginUser(req), status));
		} catch (ModoException e) {
			req.setAttribute("error", e.getMessage());
		}
		req.setAttribute("status", status);                     // 선택된 탭 표시용 (전체면 null)
		req.setAttribute("reportResults", ReportResult.values()); // 처리 결과 select용

		forward(req, resp, "admin/report/list");
	}

	// 신고 처리
	private void process(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		try {
			int reportId = parseReportId(req.getParameter("reportId"));
			ReportResult result = parseResult(req.getParameter("result"));

			reportService.process(getLoginUser(req), reportId, result);
			resp.sendRedirect(req.getContextPath() + "/admin/report/list");

		} catch (ModoException e) {
			// 오류 문구를 들고 목록 화면을 다시 보여 줌
			req.setAttribute("error", e.getMessage());
			list(req, resp);
		}
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

	// 상태 문자열 -> ENUM (없거나 이상한 값이면 null = 전체)
	private ReportStatus parseStatus(String value) {
		value = trim(value);
		if (value == null) return null;
		try {
			return ReportStatus.valueOf(value);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	// 처리 결과 문자열 -> ENUM (선택 안 하면 null -> Service에서 안내)
	private ReportResult parseResult(String value) {
		value = trim(value);
		if (value == null) return null;
		try {
			return ReportResult.valueOf(value);
		} catch (IllegalArgumentException e) {
			throw new ModoException("처리 결과를 선택해주세요.");
		}
	}

	// 신고 번호 문자열 -> int
	private int parseReportId(String value) {
		value = trim(value);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {   // null도 여기로 옴
			throw new ModoException("신고 번호가 올바르지 않습니다.");
		}
	}

	private void forward(HttpServletRequest req, HttpServletResponse resp, String view)
			throws ServletException, IOException {
		req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, resp);
	}

}
