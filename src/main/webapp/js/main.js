/* =====================================================================
   메인 화면 스크립트 (현호작성)
   1. 맞춤형 강의검색 : 학교급을 고르면 학년 목록을 바꾼다
   2. 과목 바로가기 넘기기   3. 맞춤형 강의 탭   4. 강의 카드 넘기기
   ===================================================================== */
document.addEventListener("DOMContentLoaded", function () {

	/* ---------- 1. 학교급 -> 학년 목록 ---------- */
	var levelSelect = document.querySelector(".js-cs-level");
	var gradeSelect = document.querySelector(".js-cs-grade");

	if (levelSelect && gradeSelect) {
		levelSelect.addEventListener("change", function () {
			var option = levelSelect.options[levelSelect.selectedIndex];
			var maxGrade = parseInt(option.getAttribute("data-max-grade"), 10) || 0;

			// 학년 목록 새로 만들기 (초등학교 1~6, 중·고등학교 1~3)
			gradeSelect.length = 1;   // "학년 선택" 만 남긴다
			for (var g = 1; g <= maxGrade; g++) {
				gradeSelect.add(new Option(g + "학년", String(g)));
			}
			gradeSelect.disabled = maxGrade === 0;
		});
	}

	/* ---------- 2·4. 옆으로 넘기기 (과목 바로가기 · 강의 카드 공통) ---------- */
	function setupSlider(list, prevBtn, nextBtn) {
		if (!list || !prevBtn || !nextBtn) return;

		function update() {
			var max = list.scrollWidth - list.clientWidth;
			prevBtn.disabled = list.scrollLeft <= 2;
			nextBtn.disabled = list.scrollLeft >= max - 2;
		}

		prevBtn.addEventListener("click", function () {
			list.scrollBy({ left: -list.clientWidth * 0.8, behavior: "smooth" });
		});
		nextBtn.addEventListener("click", function () {
			list.scrollBy({ left: list.clientWidth * 0.8, behavior: "smooth" });
		});
		list.addEventListener("scroll", update);
		window.addEventListener("resize", update);
		update();
	}

	setupSlider(
		document.querySelector(".js-sq-list"),
		document.querySelector(".js-sq-prev"),
		document.querySelector(".js-sq-next")
	);

	// 강의 카드 목록이 있으면 좌우 버튼을 붙인다 (카드가 없으면 아무것도 안 함)
	document.querySelectorAll(".js-lec-track").forEach(function (track) {
		var wrap = track.parentElement;
		var prev = document.createElement("button");
		var next = document.createElement("button");
		prev.type = next.type = "button";
		prev.className = "lec-arrow prev";
		next.className = "lec-arrow next";
		prev.setAttribute("aria-label", "이전 강의 보기");
		next.setAttribute("aria-label", "다음 강의 보기");
		prev.innerHTML = '<svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true"><path d="M15 5l-7 7 7 7" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>';
		next.innerHTML = '<svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true"><path d="M9 5l7 7-7 7" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>';
		wrap.appendChild(prev);
		wrap.appendChild(next);
		setupSlider(track, prev, next);
	});

	/* ---------- 3. 맞춤형 강의 탭 (로그인했을 때만 있음) ---------- */
	var tabs = Array.prototype.slice.call(document.querySelectorAll('.tabs [role="tab"]'));

	function selectTab(tab, moveFocus) {
		tabs.forEach(function (t) {
			var selected = t === tab;
			t.setAttribute("aria-selected", selected ? "true" : "false");
			t.tabIndex = selected ? 0 : -1;
			var panel = document.getElementById(t.getAttribute("aria-controls"));
			if (panel) panel.hidden = !selected;
		});
		if (moveFocus) tab.focus();
	}

	tabs.forEach(function (tab, index) {
		tab.addEventListener("click", function () {
			selectTab(tab, false);
		});
		// 왼쪽·오른쪽 화살표로 탭 이동
		tab.addEventListener("keydown", function (e) {
			var move = 0;
			if (e.key === "ArrowRight") move = 1;
			if (e.key === "ArrowLeft") move = -1;
			if (move === 0) return;
			e.preventDefault();
			selectTab(tabs[(index + move + tabs.length) % tabs.length], true);
		});
	});
});
