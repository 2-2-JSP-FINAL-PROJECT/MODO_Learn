package com.modo.qna.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.modo.member.dto.LoginUser;
import com.modo.qna.dto.AnswerDto;
import com.modo.qna.dto.QuestionDto;
import com.modo.qna.service.QnaService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/*
 * Q&A 의 모든 주소를 받는 Controller
 *
 *  GET  /qna/list                 목록
 *  GET  /qna/detail?qId=3         상세 (조회수 +1)
 *  GET  /qna/write                질문 작성 화면
 *  GET  /qna/edit?qId=3           질문 수정 화면
 *  POST /qna/write                질문 등록
 *  POST /qna/edit                 질문 수정
 *  POST /qna/delete               질문 삭제 (소프트 삭제)
 *  POST /qna/answer/write         관리자 답변 등록
 *  POST /qna/answer/delete        관리자 답변 삭제
 *
 *  ※ 로그인 정보는 팀 로그인이 세션에 넣어 둔 "loginUser"(LoginUser) 에서 꺼낸다.
 *    로그인 담당 팀원이 정한 이름과 다르면 아래 getLoginId(), isAdmin() 두 곳만 고치면 된다.
 */
@WebServlet({ "/qna/list", "/qna/detail", "/qna/write", "/qna/edit", "/qna/delete",
		"/qna/answer/write", "/qna/answer/delete" })
public class QnaController extends HttpServlet {

	private QnaService qnaservice = new QnaService();

	private static final String VIEW = "/WEB-INF/views/qna/";

	// =====================================================================
	// GET : 화면 보여 주기
	// =====================================================================
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String path = request.getServletPath(); // 어느 주소로 들어왔는지

		try {
			if ("/qna/list".equals(path)) {
				// ---------- 목록 ----------
				List<QuestionDto> list = qnaservice.getList();
				request.setAttribute("questionList", list);
				forward(request, response, "list.jsp");

			} else if ("/qna/detail".equals(path)) {
				// ---------- 상세 ----------
				Integer qId = parseId(request.getParameter("qId"));  // ① 주소의 qId=3 → 3
				if (qId == null) {
					response.sendError(400, "질문 번호가 올바르지 않습니다.");
					return;
				}
				QuestionDto q = qnaservice.getDetail(qId);           // ② 조회수 +1 후 질문 하나
				if (q == null) {                                     // ③ 없거나 삭제된 글
					response.sendError(404, "이미 삭제되었거나 없는 질문입니다.");
					return;
				}
				List<AnswerDto> answers = qnaservice.getAnswers(qId); // ④ 답변 목록

				request.setAttribute("question", q);                 // ⑤ request 에 담기
				request.setAttribute("answerList", answers);
				request.setAttribute("isAdmin", isAdmin(request));   // 답변 폼을 보여 줄지 판단용
				request.setAttribute("isWriter", q.getUserId().equals(getLoginId(request))); // 수정·삭제 버튼용
				forward(request, response, "detail.jsp");            // ⑥ JSP 로

			} else if ("/qna/write".equals(path)) {
				// ---------- 작성 화면 ----------
				if (getLoginId(request) == null) {
					response.sendError(401, "로그인 후 이용할 수 있습니다.");
					return;
				}
				forward(request, response, "write.jsp");

			} else if ("/qna/edit".equals(path)) {
				// ---------- 수정 화면 (작성 화면 재사용, 기존 값을 채워서) ----------
				Integer qId = parseId(request.getParameter("qId"));
				if (qId == null) {
					response.sendError(400, "질문 번호가 올바르지 않습니다.");
					return;
				}
				QuestionDto q = qnaservice.getForEdit(qId, getLoginId(request)); // 본인 글인지 확인 포함
				request.setAttribute("question", q);
				forward(request, response, "write.jsp");

			} else {
				response.sendError(404);   // 화면이 없는 주소(POST 전용)를 GET 으로 열었을 때
			}

		} catch (IllegalArgumentException | SecurityException e) {
			sendRuleError(request, response, e);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}// doGet

	// =====================================================================
	// POST : 저장 · 수정 · 삭제 → 끝나면 redirect (새로고침해도 두 번 저장되지 않게)
	// =====================================================================
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {


		String path = request.getServletPath();
		String ctx = request.getContextPath(); // "/MODO_Learn" 같은 앞부분 주소
		String loginId = getLoginId(request);

		try {
			if ("/qna/write".equals(path)) {
				// ---------- 질문 등록 ----------
				QuestionDto q = readQuestionForm(request);
				int newId = qnaservice.write(q, loginId);
				response.sendRedirect(ctx + "/qna/detail?qId=" + newId);

			} else if ("/qna/edit".equals(path)) {
				// ---------- 질문 수정 ----------
				QuestionDto q = readQuestionForm(request);
				Integer qId = parseId(request.getParameter("qId"));
				if (qId == null) {
					throw new IllegalArgumentException("질문 번호가 올바르지 않습니다.");
				}
				q.setqId(qId);
				qnaservice.update(q, loginId);
				response.sendRedirect(ctx + "/qna/detail?qId=" + qId);

			} else if ("/qna/delete".equals(path)) {
				// ---------- 질문 삭제 ----------
				Integer qId = parseId(request.getParameter("qId"));
				if (qId == null) {
					throw new IllegalArgumentException("질문 번호가 올바르지 않습니다.");
				}
				qnaservice.delete(qId, loginId, isAdmin(request));
				response.sendRedirect(ctx + "/qna/list");

			} else if ("/qna/answer/write".equals(path)) {
				// ---------- 답변 등록 (관리자) ----------
				Integer qId = parseId(request.getParameter("qId"));
				if (qId == null) {
					throw new IllegalArgumentException("질문 번호가 올바르지 않습니다.");
				}
				qnaservice.writeAnswer(qId, request.getParameter("content"), loginId, isAdmin(request));
				response.sendRedirect(ctx + "/qna/detail?qId=" + qId);

			} else if ("/qna/answer/delete".equals(path)) {
				// ---------- 답변 삭제 (관리자) ----------
				Integer aId = parseId(request.getParameter("aId"));
				if (aId == null) {
					throw new IllegalArgumentException("답변 번호가 올바르지 않습니다.");
				}
				int qId = qnaservice.deleteAnswer(aId, isAdmin(request));
				response.sendRedirect(ctx + "/qna/detail?qId=" + qId);

			} else {
				response.sendError(404);
			}

		} catch (IllegalArgumentException | SecurityException e) {
			sendRuleError(request, response, e);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}// doPost

	// =====================================================================
	// 도우미 메서드
	// =====================================================================

	// JSP 로 이동
	private void forward(HttpServletRequest request, HttpServletResponse response, String jsp)
			throws ServletException, IOException {
		request.getRequestDispatcher(VIEW + jsp).forward(request, response);
	}

	// 작성·수정 폼의 값을 QuestionDto 에 담기
	// 폼 name : qType, catId, lecId, title, content
	private QuestionDto readQuestionForm(HttpServletRequest request) {
		QuestionDto q = new QuestionDto();
		q.setqType(request.getParameter("qType"));
		q.setCatId(parseId(request.getParameter("catId")));   // 비어 있으면 null
		q.setLecId(parseId(request.getParameter("lecId")));   // 비어 있으면 null
		q.setTitle(request.getParameter("title"));
		q.setContent(request.getParameter("content"));
		return q;
	}

	// "3" → 3, 비었거나 숫자가 아니면 null
	private Integer parseId(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	// 세션에 저장된 로그인 회원 (팀 로그인이 "loginUser" 로 넣어 둠, 로그인 안 했으면 null)
	private LoginUser getLoginUser(HttpServletRequest request) {
		HttpSession session = request.getSession(false); // false : 세션이 없으면 새로 만들지 않음
		return session == null ? null : (LoginUser) session.getAttribute("loginUser");
	}

	// 로그인한 아이디 (로그인 안 했으면 null)
	private String getLoginId(HttpServletRequest request) {
		LoginUser loginUser = getLoginUser(request);
		return loginUser == null ? null : loginUser.getMemberId();
	}

	// 관리자인지
	private boolean isAdmin(HttpServletRequest request) {
		LoginUser loginUser = getLoginUser(request);
		return loginUser != null && loginUser.isAdmin();
	}

	// Service 가 던진 규칙 위반을 상태 코드 + 안내 문구로 응답
	//  - 로그인 안 함 401 / 권한 없음 403 / 입력값 잘못 400
	//  - 화면(JSP)을 만들면 alert 띄우고 뒤로 가기 같은 방식으로 바꾸면 된다
	private void sendRuleError(HttpServletRequest request, HttpServletResponse response, RuntimeException e)
			throws IOException {
		int status;
		if (e instanceof SecurityException) {
			status = (getLoginId(request) == null) ? 401 : 403;
		} else {
			status = 400;
		}
		response.sendError(status, e.getMessage());
	}

}// QnaController
