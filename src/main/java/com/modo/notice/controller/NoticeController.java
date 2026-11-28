package com.modo.notice.controller;

import java.io.IOException;

import com.modo.common.exception.ModoException;
import com.modo.notice.dto.NoticeType;
import com.modo.notice.service.NoticeService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 성환작성
// 공지사항·이벤트 사용자 화면 (조회만, 로그인 없이 볼 수 있음)
//  GET /notice/list               전체 목록
//  GET /notice/list?type=EVENT    구분별 목록 (NOTICE / EVENT / INFO)
//  GET /notice/detail?id=3        상세 (조회수 +1)
@WebServlet("/notice/*")  // notice 시작 경로 다 받은 다음 -> switch로 분기처리
public class NoticeController extends HttpServlet {

	private final NoticeService noticeService = new NoticeService();

	@Override //Get요청 Read처리
	protected void doGet(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {

		String path = req.getPathInfo();
		// 잘못된 경로 처리
		if (path == null) path = "/";

		switch (path) {
		case "/list":    // 목록
			list(req, resp);
			break;
		case "/detail":  // 상세
			detail(req, resp);
			break;
		default:
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}


	// 공지 목록
	private void list(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {

		// type이 없거나 이상한 값이면 null -> 전체 목록
		NoticeType type = parseNoticeType(req.getParameter("type"));

		try {
			req.setAttribute("noticeList", noticeService.getList(type));
		} catch (ModoException e) {
			req.setAttribute("error", e.getMessage());
		}
		req.setAttribute("type", type);   // 화면에서 선택된 탭 표시용 (전체면 null)

		forward(req, resp, "notice/list");
	}

	// 공지 상세
	private void detail(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {

		try {
			int noticeId = parseId(req.getParameter("id"));
			req.setAttribute("notice", noticeService.getDetail(noticeId));

		} catch (ModoException e) {
			// 없는 글·삭제된 글 -> notice 없이 error만 들고 화면으로
			req.setAttribute("error", e.getMessage());
		}
		forward(req, resp, "notice/detail");
	}


	private String trim(String value) {
		if (value == null) return null;
		value = value.trim();
		return value.isEmpty() ? null : value;
	}

	// 구분 문자열 -> ENUM (없거나 이상한 값이면 null = 전체)
	private NoticeType parseNoticeType(String value) {
		value = trim(value);
		if (value == null) return null;
		try {
			return NoticeType.valueOf(value);
		} catch (IllegalArgumentException e) {
			return null;
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
