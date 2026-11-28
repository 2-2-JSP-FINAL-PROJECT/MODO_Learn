package com.modo.notice.dto;

import java.time.LocalDateTime;

import com.modo.common.enums.YnType;

// 성환작성
// 공지사항·이벤트 1개를 담는 DTO (notice 테이블)
// NoticeType, PinYn, BannerYn, DelYn은 ENUM값으로 관리(DB는 check)
public class NoticeDTO {

	private int noticeId;                     // NOTICE_ID    공지 번호 (AUTO_INCREMENT)
	private String writer;                    // WRITER       작성자 아이디 (관리자)
	private NoticeType noticeType;            // N_TYPE       NOTICE / EVENT / INFO
	private String title;                     // TITLE        제목 (200자 이내)
	private String content;                   // CONTENT      내용 (4000자 이내)
	private YnType pinYn;                     // PIN_YN       목록 상단 고정 Y / N
	private YnType bannerYn;                  // BANNER_YN    메인 배너 표시 Y / N
	private int viewCount;                    // VIEW_COUNT   조회수
	private YnType delYn;                     // DEL_YN       삭제 여부 Y / N
	private LocalDateTime regDate;            // REG_DATE     등록일
	private LocalDateTime modDate;            // MOD_DATE     수정일 (DB가 자동 갱신)

	public NoticeDTO() {
	}

	public int getNoticeId() {
		return noticeId;
	}

	public void setNoticeId(int noticeId) {
		this.noticeId = noticeId;
	}

	public String getWriter() {
		return writer;
	}

	public void setWriter(String writer) {
		this.writer = writer;
	}

	public NoticeType getNoticeType() {
		return noticeType;
	}

	public void setNoticeType(NoticeType noticeType) {
		this.noticeType = noticeType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public YnType getPinYn() {
		return pinYn;
	}

	public void setPinYn(YnType pinYn) {
		this.pinYn = pinYn;
	}

	public YnType getBannerYn() {
		return bannerYn;
	}

	public void setBannerYn(YnType bannerYn) {
		this.bannerYn = bannerYn;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public YnType getDelYn() {
		return delYn;
	}

	public void setDelYn(YnType delYn) {
		this.delYn = delYn;
	}

	public LocalDateTime getRegDate() {
		return regDate;
	}

	public void setRegDate(LocalDateTime regDate) {
		this.regDate = regDate;
	}

	public LocalDateTime getModDate() {
		return modDate;
	}

	public void setModDate(LocalDateTime modDate) {
		this.modDate = modDate;
	}


	@Override
	public String toString() {
		return "NoticeDTO [noticeId=" + noticeId + ", writer=" + writer + ", noticeType=" + noticeType
				+ ", title=" + title + ", pinYn=" + pinYn + ", bannerYn=" + bannerYn
				+ ", viewCount=" + viewCount + ", delYn=" + delYn + ", regDate=" + regDate + "]";
	}
}
