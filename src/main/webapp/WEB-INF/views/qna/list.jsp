<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.modo.qna.dto.QuestionDto" %>
<%
	// QnaController 가 request.setAttribute("questionList", list) 로 담아 보낸 목록 꺼내기
	// TODO ① request 에서 "questionList" 꺼내서 List<QuestionDto> 로 형변환
	List<QuestionDto> list = null;
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Q&amp;A - 모두의 강의실</title>
</head>
<body>
<h1>Q&amp;A</h1>

<table border="1">
  <tr>
    <th>번호</th><th>구분</th><th>제목</th><th>작성자</th>
    <th>상태</th><th>조회</th><th>작성일</th>
  </tr>
<%
	// TODO ② for 문으로 list 에서 QuestionDto 를 하나씩 꺼내기
%>
  <tr>
    <td><%-- TODO 번호 --%></td>
    
    <td><%-- TODO 구분 (qType) --%></td>
    <td><%-- TODO 제목 --%></td>
    <td><%-- TODO 작성자 이름 --%></td>
    <td><%-- TODO 상태 (qStatus) --%></td>
    <td><%-- TODO 조회수 --%></td>
    <td><%-- TODO 작성일 --%></td>
  </tr>
<%
	// TODO ③ for 문 닫기
%>
</table>

</body>
</html>
