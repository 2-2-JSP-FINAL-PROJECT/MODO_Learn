package com.modo.notice.dto;

import java.sql.Timestamp;

// 공지사항 · 이벤트 1개를 담는 그릇 (notice 테이블)
public class NoticeDto {

	private int noticeId;          // notice_id   공지 번호
	private String writer;         // writer      작성자 아이디 (관리자)
	private String nType;          // n_type      NOTICE / EVENT / INFO
	private String title;          // title       제목
	private String content;        // content     내용
	private String pinYn;          // pin_yn      목록 상단 고정 Y / N
	private String bannerYn;       // banner_yn   메인 배너 표시 Y / N
	private int viewCount;         // view_count  조회수
	private String delYn;          // del_yn      Y / N
	private Timestamp regDate;     // reg_date    등록일
	private Timestamp modDate;     // mod_date    수정일

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
	public String getnType() {
		return nType;
	}
	public void setnType(String nType) {
		this.nType = nType;
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
	public String getPinYn() {
		return pinYn;
	}
	public void setPinYn(String pinYn) {
		this.pinYn = pinYn;
	}
	public String getBannerYn() {
		return bannerYn;
	}
	public void setBannerYn(String bannerYn) {
		this.bannerYn = bannerYn;
	}
	public int getViewCount() {
		return viewCount;
	}
	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}
	public String getDelYn() {
		return delYn;
	}
	public void setDelYn(String delYn) {
		this.delYn = delYn;
	}
	public Timestamp getRegDate() {
		return regDate;
	}
	public void setRegDate(Timestamp regDate) {
		this.regDate = regDate;
	}
	public Timestamp getModDate() {
		return modDate;
	}
	public void setModDate(Timestamp modDate) {
		this.modDate = modDate;
	}

}
