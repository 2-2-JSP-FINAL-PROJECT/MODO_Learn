<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 테스트용 : 첫 주소(/MODO_Learn/)로 들어오면 메인 화면으로 포워드 (현호작성)
     MainController(/main)가 생기면 "/main" 으로 바꾼다 --%>
<% request.getRequestDispatcher("/WEB-INF/views/main.jsp").forward(request, response); %>
