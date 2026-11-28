/* =====================================================================
   모두의 강의실 공통 스크립트 (현호작성)
   1. 전체 메뉴(☰) 열기·닫기   2. 학년 펼침 메뉴 키보드 지원
   3. 글자 크기 · 고대비      4. 강의 출처 사이트 이동   5. 맨 위로
   ===================================================================== */
document.addEventListener("DOMContentLoaded", function () {

	var root = document.documentElement;

	// localStorage 는 막혀 있을 수 있어서(시크릿 창 등) 항상 try 로 감싼다
	function load(key) {
		try { return localStorage.getItem(key); } catch (e) { return null; }
	}
	function save(key, value) {
		try { localStorage.setItem(key, value); } catch (e) { }
	}

	/* ---------- 1. 전체 메뉴(☰) ---------- */
	var menuBtn = document.querySelector(".all-menu-btn");
	var allMenu = document.getElementById("all-menu");

	function setAllMenu(open) {
		if (!menuBtn || !allMenu) return;
		menuBtn.setAttribute("aria-expanded", open ? "true" : "false");
		allMenu.hidden = !open;
	}

	if (menuBtn && allMenu) {
		menuBtn.addEventListener("click", function () {
			setAllMenu(menuBtn.getAttribute("aria-expanded") !== "true");
		});

		// 메뉴 바깥을 누르면 닫기
		document.addEventListener("click", function (e) {
			if (!allMenu.hidden && !allMenu.contains(e.target) && !menuBtn.contains(e.target)) {
				setAllMenu(false);
			}
		});
	}

	/* ---------- 2. 학년 펼침 메뉴 : Esc 로 닫기 ---------- */
	// 마우스가 나가거나 포커스가 메뉴 밖으로 나가면 다시 펼칠 수 있게 되돌린다
	document.querySelectorAll(".has-sub").forEach(function (item) {
		item.addEventListener("mouseleave", function () {
			item.classList.remove("closed");
		});
		item.addEventListener("focusout", function (e) {
			if (!item.contains(e.relatedTarget)) item.classList.remove("closed");
		});
	});

	document.addEventListener("keydown", function (e) {
		if (e.key !== "Escape") return;

		if (allMenu && !allMenu.hidden) {
			setAllMenu(false);
			menuBtn.focus();
		}
		var focused = document.activeElement;
		var sub = focused && focused.closest ? focused.closest(".has-sub") : null;
		if (sub) {
			sub.querySelector("a").focus();
			sub.classList.add("closed");
		}
	});

	/* ---------- 3. 글자 크기 · 고대비 ---------- */
	var fontSteps = ["normal", "large", "xlarge"];
	var fontNames = { normal: "글자 보통", large: "글자 크게", xlarge: "글자 아주 크게" };
	var fontBtn = document.querySelector(".js-font-size");
	var fontLabel = document.querySelector(".js-font-label");
	var contrastBtn = document.querySelector(".js-contrast");

	function applyFont(step) {
		root.classList.remove("font-large", "font-xlarge");
		if (step !== "normal") root.classList.add("font-" + step);
		if (fontLabel) fontLabel.textContent = fontNames[step];
	}

	var fontNow = load("modo.fontSize");
	if (fontSteps.indexOf(fontNow) < 0) fontNow = "normal";
	applyFont(fontNow);

	if (fontBtn) {
		fontBtn.addEventListener("click", function () {
			fontNow = fontSteps[(fontSteps.indexOf(fontNow) + 1) % fontSteps.length];
			applyFont(fontNow);
			save("modo.fontSize", fontNow);
		});
	}

	if (contrastBtn) {
		contrastBtn.setAttribute("aria-pressed", root.classList.contains("high-contrast") ? "true" : "false");
		contrastBtn.addEventListener("click", function () {
			var on = !root.classList.contains("high-contrast");
			root.classList.toggle("high-contrast", on);
			contrastBtn.setAttribute("aria-pressed", on ? "true" : "false");
			save("modo.contrast", on ? "Y" : "N");
		});
	}

	/* ---------- 4. 강의 출처 사이트 이동 (새 창) ---------- */
	var familyBtn = document.querySelector(".js-family-go");
	var familySelect = document.getElementById("family-site");

	if (familyBtn && familySelect) {
		familyBtn.addEventListener("click", function () {
			var url = familySelect.value;
			if (!url) {
				alert("이동할 사이트를 선택해주세요.");
				familySelect.focus();
				return;
			}
			window.open(url, "_blank", "noopener");
		});
	}

	/* ---------- 5. 맨 위로 ---------- */
	var topBtn = document.querySelector(".js-top");
	if (topBtn) {
		topBtn.addEventListener("click", function () {
			window.scrollTo({ top: 0, behavior: "smooth" });
			var main = document.getElementById("main");
			if (main) main.focus({ preventScroll: true });
		});
	}
});
