<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	import="java.util.List, java.util.Arrays, java.net.URLEncoder, java.nio.charset.StandardCharsets" %>
<%--
	메인 화면 (현호작성)

	컨트롤러(/main)가 request 에 넣어 주면 쓰는 값 (없으면 기본값으로 화면을 그린다)
		subjects       : List<String>  과목 목록 (category.subject_name)   없으면 기본 과목 8개
		mySchoolLevel  : String        로그인한 회원의 학교급 (ELEM / MIDDLE / HIGH / NONE)
		myGrade        : Integer       로그인한 회원의 학년
	→ 로그인하면 맞춤형 강의검색의 학교급·학년이 미리 선택된다 (다른 것으로 바꿀 수 있음)

	강의 카드 (인기 강의 / 최근 본 강의 / 찜한 강의 / 내 학년 인기 강의) 는
	강의 DTO·DAO 가 생기면 아래 "카드 자리" 에 반복문으로 넣는다. 지금은 안내 문구를 보여준다.
--%>
<%
	request.setAttribute("menu", "main");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<%
	// ---------- 컨트롤러가 넘겨준 값 ----------
	@SuppressWarnings("unchecked")
	List<String> subjects = (List<String>) request.getAttribute("subjects");
	if (subjects == null || subjects.isEmpty()) {
		subjects = Arrays.asList("국어", "수학", "영어", "사회", "과학", "한국사", "정보", "예체능");
	}

	String mySchoolLevel = (String) request.getAttribute("mySchoolLevel");
	Integer myGrade = (Integer) request.getAttribute("myGrade");

	// 로그인했고 학교급이 있으면(NONE 제외) 검색 조건을 미리 선택한다
	SchoolLevel pickLevel = null;
	if (loginUser != null && mySchoolLevel != null) {
		try {
			pickLevel = SchoolLevel.valueOf(mySchoolLevel);
		} catch (IllegalArgumentException e) {
			pickLevel = null;
		}
		if (pickLevel == SchoolLevel.NONE) pickLevel = null;
	}
	Integer pickGrade = (pickLevel != null && pickLevel.isValidGrade(myGrade)) ? myGrade : null;
%>
<%!
	// 과목 이름 -> 아이콘 (svg path). 모르는 과목은 책 아이콘
	private String subjectIcon(String subject) {
		if (subject == null) subject = "";
		switch (subject) {
		case "국어":
			return "<path d='M6 5h9a3 3 0 0 1 3 3v11H9a3 3 0 0 1-3-3z'/><path d='M9 9h6M9 12h6M9 15h3'/>";
		case "수학":
			return "<path d='M5 7h6M8 4v6M14 7h5M5 16l4-4M5 12l4 4M14 14h5M14 18h5'/>";
		case "영어":
			return "<path d='M4 6h10v8H8l-4 3z'/><path d='M14 10h6v8l-3-2h-3'/><path d='M7 9h4M7 11.5h3'/>";
		case "사회":
			return "<circle cx='12' cy='12' r='8'/><path d='M4 12h16M12 4c2.5 2.3 3.5 5 3.5 8s-1 5.7-3.5 8c-2.5-2.3-3.5-5-3.5-8s1-5.7 3.5-8z'/>";
		case "과학":
			return "<path d='M10 4v6l-5 8.5A1 1 0 0 0 6 20h12a1 1 0 0 0 1-1.5L14 10V4'/><path d='M9 4h6M7.5 15h9'/>";
		case "한국사":
			return "<path d='M4 10l8-5 8 5'/><path d='M6 10v8M10 10v8M14 10v8M18 10v8M4 20h16'/>";
		case "정보":
			return "<rect x='4' y='5' width='16' height='11' rx='1.5'/><path d='M9 20h6M12 16v4M9.5 9l-2 1.5 2 1.5M14.5 9l2 1.5-2 1.5'/>";
		case "예체능":
			return "<path d='M9 18V6l10-2v12'/><circle cx='7' cy='18' r='2'/><circle cx='17' cy='16' r='2'/>";
		default:
			return "<path d='M5 5h5a2 2 0 0 1 2 2v12a2 2 0 0 0-2-2H5zM19 5h-5a2 2 0 0 0-2 2v12a2 2 0 0 1 2-2h5z'/>";
		}
	}

	// 링크 주소에 넣을 한글을 % 형식으로 바꾼다 (예 : 수학 -> %EC%88%98%ED%95%99)
	private String url(String value) {
		return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
	}
%>

<%-- 메인 화면 전용 스타일 (본문보다 먼저 읽어서 화면이 깨져 보이지 않게) --%>
<link rel="stylesheet" href="<%= ctx %>/css/main.css">

<%-- ===================== 1. 메인 배너 ===================== --%>
<section class="main-hero">
	<div class="inner">

		<%-- 과목 바로가기 --%>
		<nav class="subject-quick" aria-label="과목별 강의 바로가기">
			<button type="button" class="sq-arrow sq-prev js-sq-prev" aria-label="이전 과목 보기">
				<svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true"><path d="M15 5l-7 7 7 7" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
			</button>
			<ul class="sq-list js-sq-list">
<% for (String subject : subjects) { %>
				<li>
					<a href="<%= ctx %>/lecture/list?subject=<%= url(subject) %>">
						<span class="sq-icon" aria-hidden="true">
							<svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><%= subjectIcon(subject) %></svg>
						</span>
						<span class="sq-name"><%= h(subject) %></span>
					</a>
				</li>
<% } %>
			</ul>
			<button type="button" class="sq-arrow sq-next js-sq-next" aria-label="다음 과목 보기">
				<svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true"><path d="M9 5l7 7-7 7" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
			</button>
		</nav>

		<%-- 배너 문구 : 비로그인 / 로그인 --%>
		<div class="hero-text">
<% if (loginUser == null) { %>
			<p class="hero-kicker">SDG4 · 모두를 위한 양질의 교육</p>
			<h1 class="hero-title">흩어진 무료 강의,<br>학년별로 한곳에서</h1>
			<p class="hero-desc">EBS · K-MOOC · 유튜브의 초·중·고 무료 강의를 학교급·학년·과목별로 골라 모았어요.</p>
			<a class="hero-more" href="<%= ctx %>/member/join">회원가입하고 맞춤 강의 받기
				<svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true"><path d="M9 5l7 7-7 7" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
			</a>
<% } else if (loginUser.isAdmin()) { %>
			<p class="hero-kicker">관리자</p>
			<h1 class="hero-title"><%= h(loginUser.getName()) %>님,<br>오늘도 좋은 강의를 모아 볼까요?</h1>
			<p class="hero-desc">추천 승인, 신고 처리, 자막·수어 확인을 관리자 페이지에서 할 수 있어요.</p>
			<a class="hero-more" href="<%= ctx %>/admin">관리자 페이지로 가기
				<svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true"><path d="M9 5l7 7-7 7" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
			</a>
<% } else { %>
			<p class="hero-kicker">오늘의 공부</p>
			<h1 class="hero-title"><%= h(loginUser.getName()) %>님,<br>오늘도 한 걸음 더!</h1>
			<p class="hero-desc">보던 강의를 이어서 보거나, 내 학년에 맞는 강의를 찾아보세요.</p>
			<a class="hero-more" href="<%= ctx %>/mascot">내 마스코트 보러 가기
				<svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true"><path d="M9 5l7 7-7 7" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
			</a>
<% } %>
		</div>
	</div>
</section>

<%-- ===================== 2. 맞춤형 강의검색 ===================== --%>
<section class="custom-search" aria-labelledby="cs-title">
	<div class="inner">
		<form class="cs-form" action="<%= ctx %>/lecture/list" method="get">
			<h2 id="cs-title" class="cs-title">
				맞춤형 강의검색
				<svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true"><circle cx="10" cy="8" r="3.5" fill="currentColor"/><path d="M3.5 19c.8-3.6 3.4-5.5 6.5-5.5 1.3 0 2.5.3 3.5 1" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><path d="M15 17l2 2 4-4" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
			</h2>

			<div class="cs-field">
				<label class="sr-only" for="cs-level">학교급</label>
				<select id="cs-level" name="level" class="js-cs-level">
					<option value="" data-max-grade="0">학교급 선택</option>
<% for (SchoolLevel level : SchoolLevel.values()) {
	if (level == SchoolLevel.NONE) continue; %>
					<option value="<%= level.name() %>" data-max-grade="<%= level.getMaxGrade() %>"<%= level == pickLevel ? " selected" : "" %>><%= level.getLabel() %></option>
<% } %>
				</select>
			</div>

			<div class="cs-field">
				<label class="sr-only" for="cs-grade">학년</label>
				<select id="cs-grade" name="grade" class="js-cs-grade"<%= pickLevel == null ? " disabled" : "" %>>
					<option value="">학년 선택</option>
<% if (pickLevel != null) {
	for (int g = 1; g <= pickLevel.getMaxGrade(); g++) { %>
					<option value="<%= g %>"<%= pickGrade != null && pickGrade == g ? " selected" : "" %>><%= g %>학년</option>
<%	}
} %>
				</select>
			</div>

			<div class="cs-field">
				<label class="sr-only" for="cs-subject">과목</label>
				<select id="cs-subject" name="subject">
					<option value="">과목 선택</option>
<% for (String subject : subjects) { %>
					<option value="<%= h(subject) %>"><%= h(subject) %></option>
<% } %>
				</select>
			</div>

			<div class="cs-field cs-keyword">
				<label class="sr-only" for="cs-q">강의명</label>
				<svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true"><circle cx="11" cy="11" r="6.5" fill="none" stroke="currentColor" stroke-width="2"/><path d="M20 20l-4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
				<input id="cs-q" type="text" name="q" placeholder="강의명을 입력해주세요." maxlength="50">
			</div>

			<button type="submit" class="cs-submit">
				<svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true"><circle cx="11" cy="11" r="6.5" fill="none" stroke="currentColor" stroke-width="2.2"/><path d="M20 20l-4-4" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/></svg>
				검색
			</button>
		</form>
<% if (pickLevel != null) { %>
		<p class="cs-hint">
			<svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true"><circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" stroke-width="2"/><path d="M12 11v5M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
			<%= h(loginUser.getName()) %>님의 학교급·학년이 미리 선택되어 있어요. 다른 학년도 고를 수 있어요.
		</p>
<% } %>
	</div>
</section>

<%-- ===================== 3. 맞춤형 강의 ===================== --%>
<section id="custom" class="main-section custom-lecture" aria-labelledby="custom-title">
	<div class="inner">
		<div class="sec-head">
			<h2 id="custom-title" class="sec-title">맞춤형 강의</h2>
<% if (loginUser == null) { %>
			<p class="sec-desc">모두의 강의실에서 지금 가장 많이 보는 인기 강의예요!</p>
<% } else { %>
			<p class="sec-desc"><%= h(loginUser.getName()) %>님이 보던 강의와 관심 있는 강의를 모았어요!</p>
<% } %>
		</div>

		<div class="sec-tools">
<% if (loginUser == null) { %>
			<span class="sec-label">인기 강의 TOP</span>
			<a class="sec-link" href="<%= ctx %>/member/login">
				<svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true"><circle cx="12" cy="12" r="8.5" fill="none" stroke="currentColor" stroke-width="2"/></svg>
				<strong>로그인</strong> 맞춤형 보기
			</a>
<% } else { %>
			<div class="tabs" role="tablist" aria-label="맞춤형 강의 종류">
				<button type="button" role="tab" id="tab-recent" aria-controls="panel-recent" aria-selected="true">최근 본 강의</button>
				<button type="button" role="tab" id="tab-favorite" aria-controls="panel-favorite" aria-selected="false" tabindex="-1">찜한 강의</button>
				<button type="button" role="tab" id="tab-grade" aria-controls="panel-grade" aria-selected="false" tabindex="-1">내 학년 인기 강의</button>
			</div>
			<a class="sec-link" href="<%= ctx %>/mypage">마이페이지에서 전체 보기
				<svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path d="M9 5l7 7-7 7" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
			</a>
<% } %>
		</div>

<% if (loginUser == null) { %>
		<%-- 카드 자리 : 인기 강의 (조회수 순) --%>
		<div class="lec-carousel">
			<div class="empty-box">
				<span class="empty-icon" aria-hidden="true">
					<svg viewBox="0 0 24 24" width="34" height="34"><path d="M12 3l2.6 5.6 6 .6-4.5 4 1.3 6-5.4-3.1-5.4 3.1 1.3-6-4.5-4 6-.6z" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/></svg>
				</span>
				<p class="empty-title">아직 등록된 인기 강의가 없어요</p>
				<p class="empty-desc">강의가 등록되면 조회수가 많은 순서로 여기에 보여 드릴게요.</p>
				<a class="btn btn-sm" href="<%= ctx %>/lecture/list">전체 강의 보기</a>
			</div>
		</div>
<% } else { %>
		<%-- 카드 자리 : 최근 본 강의 (watch_log) --%>
		<div class="lec-carousel tab-panel" role="tabpanel" id="panel-recent" aria-labelledby="tab-recent">
			<div class="empty-box">
				<span class="empty-icon" aria-hidden="true">
					<svg viewBox="0 0 24 24" width="34" height="34"><circle cx="12" cy="12" r="8.5" fill="none" stroke="currentColor" stroke-width="1.6"/><path d="M12 7.5V12l3 2" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>
				</span>
				<p class="empty-title">아직 본 강의가 없어요</p>
				<p class="empty-desc">강의를 보면 여기에서 바로 이어서 볼 수 있어요.</p>
				<a class="btn btn-sm" href="<%= ctx %>/lecture/list">강의 둘러보기</a>
			</div>
		</div>

		<%-- 카드 자리 : 찜한 강의 (favorite) --%>
		<div class="lec-carousel tab-panel" role="tabpanel" id="panel-favorite" aria-labelledby="tab-favorite" hidden>
			<div class="empty-box">
				<span class="empty-icon" aria-hidden="true">
					<svg viewBox="0 0 24 24" width="34" height="34"><path d="M12 20s-7-4.4-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 10c0 5.6-7 10-7 10z" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/></svg>
				</span>
				<p class="empty-title">아직 찜한 강의가 없어요</p>
				<p class="empty-desc">강의 화면에서 ♡ 를 누르면 여기에 모아 드릴게요.</p>
				<a class="btn btn-sm" href="<%= ctx %>/lecture/list">강의 둘러보기</a>
			</div>
		</div>

		<%-- 카드 자리 : 내 학년 인기 강의 --%>
		<div class="lec-carousel tab-panel" role="tabpanel" id="panel-grade" aria-labelledby="tab-grade" hidden>
			<div class="empty-box">
				<span class="empty-icon" aria-hidden="true">
					<svg viewBox="0 0 24 24" width="34" height="34"><path d="M12 3l2.6 5.6 6 .6-4.5 4 1.3 6-5.4-3.1-5.4 3.1 1.3-6-4.5-4 6-.6z" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/></svg>
				</span>
				<p class="empty-title">아직 내 학년 강의가 없어요</p>
				<p class="empty-desc">내 학년 강의가 등록되면 많이 본 순서로 보여 드릴게요.</p>
				<a class="btn btn-sm" href="<%= ctx %>/lecture/list<%= pickLevel != null ? "?level=" + pickLevel.name() : "" %>">학년별 강의 보기</a>
			</div>
		</div>
<% } %>

		<%--
			강의 카드 모양 (강의 DTO 가 생기면 위 "카드 자리" 의 empty-box 대신 반복문으로 넣는다)
			<ul class="lec-track js-lec-track">
				<li class="lec-card lv-ELEM">
					<a href="/lecture/detail?id=1">
						<div class="lec-thumb">
							<span class="lec-subject">수학</span>
							<strong class="lec-thumb-title">분수의 덧셈</strong>
							<span class="lec-badge">초등학교 3학년</span>
						</div>
						<div class="lec-body">
							<p class="lec-kicker">많이 보는 인기 강의</p>
							<p class="lec-title">분수의 덧셈과 뺄셈</p>
							<p class="lec-meta"><span>EBS</span><span>조회 1,234</span><span>★ 4.5</span></p>
						</div>
					</a>
				</li>
			</ul>
		--%>
	</div>
</section>

<%-- ===================== 4. 학교급별로 찾기 ===================== --%>
<section class="main-section level-section" aria-labelledby="level-title">
	<div class="inner">
		<div class="sec-head">
			<h2 id="level-title" class="sec-title">학교급별로 찾기</h2>
			<p class="sec-desc">학년을 누르면 그 학년의 강의만 모아서 보여드려요</p>
		</div>

		<ul class="level-cards">
<% for (SchoolLevel level : SchoolLevel.values()) {
	if (level == SchoolLevel.NONE) continue; %>
			<li class="level-card lv-<%= level.name() %>">
				<a class="level-card-head" href="<%= ctx %>/lecture/list?level=<%= level.name() %>">
					<span class="level-name"><%= level.getLabel() %></span>
					<span class="level-sub">1~<%= level.getMaxGrade() %>학년 전체 강의
						<svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true"><path d="M9 5l7 7-7 7" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
					</span>
				</a>
				<ul class="level-grades">
<%	for (int g = 1; g <= level.getMaxGrade(); g++) { %>
					<li><a href="<%= ctx %>/lecture/list?level=<%= level.name() %>&amp;grade=<%= g %>"><%= g %>학년</a></li>
<%	} %>
				</ul>
			</li>
<% } %>
		</ul>
	</div>
</section>

<%-- ===================== 5. 이런 것도 할 수 있어요 ===================== --%>
<section class="main-section feature-section" aria-labelledby="feature-title">
	<div class="inner">
		<div class="sec-head">
			<h2 id="feature-title" class="sec-title">이런 것도 할 수 있어요</h2>
			<p class="sec-desc">강의를 보는 것에서 끝나지 않도록 함께 준비했어요</p>
		</div>

		<ul class="feature-list">
			<li>
				<a href="<%= ctx %>/lecture/own">
					<span class="ft-icon ft-green" aria-hidden="true"><svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="6" width="13" height="12" rx="2"/><path d="M16 10l5-3v10l-5-3"/></svg></span>
					<strong>자체제작 강의</strong>
					<span>다른 곳에 없는 단원을 우리가 직접 만들었어요</span>
				</a>
			</li>
			<li>
				<a href="<%= ctx %>/quiz">
					<span class="ft-icon ft-blue" aria-hidden="true"><svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="3" width="16" height="18" rx="2"/><path d="M8 8l1.5 1.5L12 7M8 14l1.5 1.5L12 13M14 8h3M14 14h3"/></svg></span>
					<strong>퀴즈 · 오답노트</strong>
					<span>강의를 본 뒤 퀴즈로 확인하고 틀린 문제는 다시</span>
				</a>
			</li>
			<li>
				<a href="<%= ctx %>/lecture/access">
					<span class="ft-icon ft-purple" aria-hidden="true"><svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="5" width="18" height="14" rx="2"/><path d="M7 13h4M13 13h4M7 16h6"/></svg></span>
					<strong>자막 · 수어 강의</strong>
					<span>자막과 수어가 있는 강의만 골라 볼 수 있어요</span>
				</a>
			</li>
			<li>
				<a href="<%= ctx %>/suggest">
					<span class="ft-icon ft-orange" aria-hidden="true"><svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M12 4v16M4 12h16"/><circle cx="12" cy="12" r="9"/></svg></span>
					<strong>강의 추천하기</strong>
					<span>좋은 무료 강의를 알고 있다면 알려 주세요</span>
				</a>
			</li>
		</ul>
	</div>
</section>

<script src="<%= ctx %>/js/main.js" defer></script>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
