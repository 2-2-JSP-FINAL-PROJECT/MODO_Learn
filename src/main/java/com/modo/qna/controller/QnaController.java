package com.modo.qna.controller;

import java.io.IOException;

import com.modo.common.exception.ModoException;
import com.modo.member.dto.LoginUser;
import com.modo.qna.dto.QuestionDTO;
import com.modo.qna.dto.QuestionType;
import com.modo.qna.service.QnaService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// 성환작성
// Q&A 주소를 전부 받는 Controller
//  GET  /qna/list                 목록
//  GET  /qna/detail?qId=3         상세 (조회수 +1)
//  GET  /qna/write                질문 작성 화면          (로그인 필요)
//  GET  /qna/edit?qId=3           질문 수정 화면          (로그인 필요, write.jsp 재사용)
//  POST /qna/write                질문 등록
//  POST /qna/edit                 질문 수정
//  POST /qna/delete               질문 삭제 (소프트 삭제)
//  POST /qna/answer/write         관리자 답변 등록
//  POST /qna/answer/delete        관리자 답변 삭제
@WebServlet("/qna/*")  // qna 시작 경로 다 받은 다음 -> switch로 분기처리
public class QnaController extends HttpServlet {

	private final QnaService qnaService = new QnaService();

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
		case "/write":   // 작성 화면
			if (redirectIfNotLogin(req, resp)) return;
			req.setAttribute("questionTypes", QuestionType.values());
			forward(req, resp, "qna/write");
			break;
		case "/edit":    // 수정 화면
			if (redirectIfNotLogin(req, resp)) return;
			editForm(req, resp);
			break;
		default:
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}


	@Override //Create, Update, Delete 요청 처리
	protected void doPost(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {

		// POST는 전부 로그인이 필요함
		if (redirectIfNotLogin(req, resp)) return;

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
		case "/answer/write":
			writeAnswer(req, resp);
			break;
		case "/answer/delete":
			deleteAnswer(req, resp);
			break;
		default:
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}


	// 질문 목록
	private void list(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		try {
			req.setAttribute("questionList", qnaService.getList());
		} catch (ModoException e) {
			req.setAttribute("error", e.getMessage());
		}
		forward(req, resp, "qna/list");
	}

	// 질문 상세 (조회수 +1)
	private void detail(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		try {
			int questionId = parseId(req.getParameter("qId"), "질문 번호가 올바르지 않습니다.");
			showDetail(req, resp, qnaService.getDetail(questionId));

		} catch (ModoException e) {
			// 없는 글·삭제된 글 -> question 없이 error만 들고 화면으로
			req.setAttribute("error", e.getMessage());
			forward(req, resp, "qna/detail");
		}
	}

	// 수정 화면 (작성 화면 재사용, 기존 값을 채워서)
	private void editForm(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		try {
			int questionId = parseId(req.getParameter("qId"), "질문 번호가 올바르지 않습니다.");
			req.setAttribute("question", qnaService.getForEdit(getLoginUser(req), questionId));
			req.setAttribute("questionTypes", QuestionType.values());
			forward(req, resp, "qna/write");

		} catch (ModoException e) {
			req.setAttribute("error", e.getMessage());
			list(req, resp);   // 수정할 수 없으면 목록으로
		}
	}

	// 질문 등록
	private void write(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		QuestionDTO dto = formToDto(req);

		try {
			dto.setQuestionType(parseQuestionType(req.getParameter("questionType")));
			dto.setCatId(parseNullableId(req.getParameter("catId"), "학년·과목 선택이 올바르지 않습니다."));
			dto.setLecId(parseNullableId(req.getParameter("lecId"), "강의 선택이 올바르지 않습니다."));

			int newId = qnaService.write(getLoginUser(req), dto);
			resp.sendRedirect(req.getContextPath() + "/qna/detail?qId=" + newId);

		} catch (ModoException e) {
			// 입력했던 값 그대로 다시 화면에
			req.setAttribute("error", e.getMessage());
			req.setAttribute("question", dto);
			req.setAttribute("questionTypes", QuestionType.values());
			forward(req, resp, "qna/write");
		}
	}

	// 질문 수정
	private void edit(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		QuestionDTO dto = formToDto(req);

		try {
			dto.setQuestionId(parseId(req.getParameter("qId"), "질문 번호가 올바르지 않습니다."));
			dto.setQuestionType(parseQuestionType(req.getParameter("questionType")));
			dto.setCatId(parseNullableId(req.getParameter("catId"), "학년·과목 선택이 올바르지 않습니다."));
			dto.setLecId(parseNullableId(req.getParameter("lecId"), "강의 선택이 올바르지 않습니다."));

			qnaService.update(getLoginUser(req), dto);
			resp.sendRedirect(req.getContextPath() + "/qna/detail?qId=" + dto.getQuestionId());

		} catch (ModoException e) {
			req.setAttribute("error", e.getMessage());
			req.setAttribute("question", dto);
			req.setAttribute("questionTypes", QuestionType.values());
			forward(req, resp, "qna/write");
		}
	}

	// 질문 삭제
	private void delete(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		try {
			int questionId = parseId(req.getParameter("qId"), "질문 번호가 올바르지 않습니다.");
			qnaService.delete(getLoginUser(req), questionId);
			resp.sendRedirect(req.getContextPath() + "/qna/list");

		} catch (ModoException e) {
			req.setAttribute("error", e.getMessage());
			list(req, resp);
		}
	}

	// 답변 등록 (관리자)
	private void writeAnswer(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		try {
			int questionId = parseId(req.getParameter("qId"), "질문 번호가 올바르지 않습니다.");
			try {
				qnaService.writeAnswer(getLoginUser(req), questionId, trim(req.getParameter("content")));
				resp.sendRedirect(req.getContextPath() + "/qna/detail?qId=" + questionId);

			} catch (ModoException e) {
				// 오류 문구를 들고 같은 질문 상세를 다시 보여 줌 (조회수는 올리지 않음)
				req.setAttribute("error", e.getMessage());
				showDetail(req, resp, qnaService.getQuestion(questionId));
			}

		} catch (ModoException e) {
			// 질문 번호가 이상하거나 질문이 없어진 경우 -> 목록으로
			req.setAttribute("error", e.getMessage());
			list(req, resp);
		}
	}

	// 답변 삭제 (관리자)
	private void deleteAnswer(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		try {
			int answerId = parseId(req.getParameter("aId"), "답변 번호가 올바르지 않습니다.");
			int questionId = qnaService.deleteAnswer(getLoginUser(req), answerId);
			resp.sendRedirect(req.getContextPath() + "/qna/detail?qId=" + questionId);

		} catch (ModoException e) {
			req.setAttribute("error", e.getMessage());
			list(req, resp);
		}
	}


	// 상세 화면에 필요한 값 담고 forward (질문 + 답변 목록 + 버튼 표시용 값)
	private void showDetail(HttpServletRequest req, HttpServletResponse resp, QuestionDTO question)
			throws ServletException, IOException {
		LoginUser loginUser = getLoginUser(req);

		req.setAttribute("question", question);
		req.setAttribute("answerList", qnaService.getAnswers(question.getQuestionId()));
		// 답변 폼을 보여 줄지
		req.setAttribute("isAdmin", loginUser != null && loginUser.isAdmin());
		// 수정·삭제 버튼을 보여 줄지
		req.setAttribute("isWriter", loginUser != null && question.getMemberId().equals(loginUser.getMemberId()));

		forward(req, resp, "qna/detail");
	}

	// 폼 값(제목·내용)을 QuestionDTO에 담기
	private QuestionDTO formToDto(HttpServletRequest req) {
		QuestionDTO dto = new QuestionDTO();
		dto.setTitle(trim(req.getParameter("title")));
		dto.setContent(trim(req.getParameter("content")));
		return dto;
	}

	// 로그인 안 했으면 로그인 화면으로 보내고 true
	private boolean redirectIfNotLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (getLoginUser(req) == null) {
			resp.sendRedirect(req.getContextPath() + "/member/login");
			return true;
		}
		return false;
	}

	// 세션에서 로그인 정보 꺼내기 (없으면 null)
	private LoginUser getLoginUser(HttpServletRequest req) {
		HttpSession session = req.getSession(false);   // false : 세션이 없으면 새로 만들지 않음
		return (session == null) ? null : (LoginUser) session.getAttribute("loginUser");
	}

	private String trim(String value) {
		if (value == null) return null;
		value = value.trim();
		return value.isEmpty() ? null : value;
	}

	// 질문 구분 문자열 -> ENUM (선택 안 하면 null -> Service에서 안내)
	private QuestionType parseQuestionType(String value) {
		value = trim(value);
		if (value == null) return null;
		try {
			return QuestionType.valueOf(value);
		} catch (IllegalArgumentException e) {
			throw new ModoException("질문 구분을 선택해주세요.");
		}
	}

	// 번호 문자열 -> int (꼭 있어야 하는 번호)
	private int parseId(String value, String message) {
		value = trim(value);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {   // null도 여기로 옴
			throw new ModoException(message);
		}
	}

	// 번호 문자열 -> Integer (선택 안 하면 null)
	private Integer parseNullableId(String value, String message) {
		value = trim(value);
		if (value == null) return null;
		return parseId(value, message);
	}

	private void forward(HttpServletRequest req, HttpServletResponse resp, String view)
			throws ServletException, IOException {
		req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, resp);
	}

}
