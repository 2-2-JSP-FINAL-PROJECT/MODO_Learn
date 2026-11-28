package com.modo.notice.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.modo.notice.dto.NoticeDto;
import com.modo.notice.service.NoticeService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * 공지사항 · 이벤트 (사용자 화면)
 *
 *  GET /notice/list               전체 목록
 *  GET /notice/list?type=EVENT    구분별 목록 (NOTICE / EVENT / INFO)
 *  GET /notice/detail?id=3        상세 (조회수 +1)
 *
 *  ※ 공지 등록 · 수정 · 삭제는 관리자 화면 담당 (여기 없음)
 */
@WebServlet({ "/notice/list", "/notice/detail" })
public class NoticeController extends HttpServlet {

	private NoticeService noticeService = new NoticeService();

	private static final String VIEW = "/WEB-INF/views/notice/";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String path = request.getServletPath();

		try {
			if ("/notice/list".equals(path)) {
				// ---------- 목록 ----------
				String type = request.getParameter("type");
				List<NoticeDto> list = noticeService.getList(type);
				request.setAttribute("noticeList", list);
				request.setAttribute("type", type);   // 화면에서 선택된 탭 표시용
				request.getRequestDispatcher(VIEW + "list.jsp").forward(request, response);

			} else if ("/notice/detail".equals(path)) {
				// ---------- 상세 ----------
				int id;
				try {
					id = Integer.parseInt(request.getParameter("id"));
				} catch (NumberFormatException e) {   // 비었거나 숫자가 아님 (null 도 여기로 옴)
					response.sendError(400, "공지 번호가 올바르지 않습니다.");
					return;
				}
				NoticeDto n = noticeService.getDetail(id);
				if (n == null) {
					response.sendError(404, "이미 삭제되었거나 없는 공지입니다.");
					return;
				}
				request.setAttribute("notice", n);
				request.getRequestDispatcher(VIEW + "detail.jsp").forward(request, response);
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}// doGet

}// NoticeController
