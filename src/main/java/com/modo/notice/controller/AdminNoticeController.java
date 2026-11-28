package com.modo.notice.controller;

import java.io.IOException;

import com.modo.common.enums.YnType;
import com.modo.common.exception.ModoException;
import com.modo.member.dto.LoginUser;
import com.modo.notice.dto.NoticeDTO;
import com.modo.notice.dto.NoticeType;
import com.modo.notice.service.NoticeService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// 성환작성
// 공지사항 관리자 화면 (LoginFilter가 /admin/* 을 관리자만 들어오게 막음)
//  GET  /admin/notice/list          관리자 공지 목록
//  GET  /admin/notice/write         등록 화면
//  POST /admin/notice/write         등록 처리
//  GET  /admin/notice/edit?id=3     수정 화면
//  POST /admin/notice/edit          수정 처리
//  POST /admin/notice/delete        삭제 처리 (소프트 삭제)
@WebServlet("/admin/notice/*")
public class AdminNoticeController extends HttpServlet {

	private final NoticeService noticeService = new NoticeService();

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
		case "/list":   // 목록
			list(req, resp);
			break;
		case "/write":  // 등록 화면
			req.setAttribute("noticeTypes", NoticeType.values());
			forward(req, resp, "admin/notice/write");
			break;
		case "/edit":   // 수정 화면
			editForm(req, resp);
			break;
		default:
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}


	@Override //Create, Update, Delete 요청 처리
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
		case "/write":
			write(req, resp);
			break;
		case "/edit":
			edit(req, resp);
			break;
		case "/delete":
			delete(req, resp);
			break;
		default:
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}


	// 관리자 공지 목록 (전체 구분)
	private void list(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		try {
			req.setAttribute("noticeList", noticeService.getList(null));
		} catch (ModoException e) {
			req.setAttribute("error", e.getMessage());
		}
		forward(req, resp, "admin/notice/list");
	}

	// 수정 화면 (기존 값 채우기)
	private void editForm(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		try {
			int noticeId = parseId(req.getParameter("id"));
			req.setAttribute("notice", noticeService.getForEdit(getLoginUser(req), noticeId));
			req.setAttribute("noticeTypes", NoticeType.values());
			forward(req, resp, "admin/notice/edit");

		} catch (ModoException e) {
			req.setAttribute("error", e.getMessage());
			list(req, resp);   // 없는 글이면 목록으로
		}
	}

	// 등록 처리
	private void write(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		NoticeDTO dto = formToDto(req);

		try {
			dto.setNoticeType(parseNoticeType(req.getParameter("noticeType")));

			noticeService.write(getLoginUser(req), dto);
			resp.sendRedirect(req.getContextPath() + "/admin/notice/list");

		} catch (ModoException e) {
			// 입력했던 값 그대로 다시 화면에
			req.setAttribute("error", e.getMessage());
			req.setAttribute("notice", dto);
			req.setAttribute("noticeTypes", NoticeType.values());
			forward(req, resp, "admin/notice/write");
		}
	}

	// 수정 처리
	private void edit(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		NoticeDTO dto = formToDto(req);

		try {
			dto.setNoticeId(parseId(req.getParameter("noticeId")));
			dto.setNoticeType(parseNoticeType(req.getParameter("noticeType")));

			noticeService.edit(getLoginUser(req), dto);
			resp.sendRedirect(req.getContextPath() + "/admin/notice/list");

		} catch (ModoException e) {
			req.setAttribute("error", e.getMessage());
			req.setAttribute("notice", dto);
			req.setAttribute("noticeTypes", NoticeType.values());
			forward(req, resp, "admin/notice/edit");
		}
	}

	// 삭제 처리
	private void delete(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		try {
			int noticeId = parseId(req.getParameter("noticeId"));
			noticeService.delete(getLoginUser(req), noticeId);
			resp.sendRedirect(req.getContextPath() + "/admin/notice/list");

		} catch (ModoException e) {
			req.setAttribute("error", e.getMessage());
			list(req, resp);
		}
	}


	// 폼 값(제목·내용·체크박스)을 NoticeDTO에 담기
	private NoticeDTO formToDto(HttpServletRequest req) {
		NoticeDTO dto = new NoticeDTO();
		dto.setTitle(trim(req.getParameter("title")));
		dto.setContent(trim(req.getParameter("content")));
		dto.setPinYn(parseYn(req.getParameter("pinYn")));
		dto.setBannerYn(parseYn(req.getParameter("bannerYn")));
		return dto;
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

	// 체크박스 -> YnType (체크하면 값이 넘어오고, 안 하면 파라미터 자체가 없음)
	private YnType parseYn(String value) {
		return (value == null) ? YnType.N : YnType.Y;
	}

	// 구분 문자열 -> ENUM (선택 안 하면 null -> Service에서 안내)
	private NoticeType parseNoticeType(String value) {
		value = trim(value);
		if (value == null) return null;
		try {
			return NoticeType.valueOf(value);
		} catch (IllegalArgumentException e) {
			throw new ModoException("구분을 선택해주세요.");
		}
	}

	// 공지 번호 문자열 -> int
	private int parseId(String value) {
		value = trim(value);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {   // null도 여기로 옴
			throw new ModoException("공지 번호가 올바르지 않습니다.");
		}
	}

	private void forward(HttpServletRequest req, HttpServletResponse resp, String view)
			throws ServletException, IOException {
		req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, resp);
	}

}
