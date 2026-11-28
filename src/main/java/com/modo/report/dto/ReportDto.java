package com.modo.report.dto;

import java.sql.Timestamp;

// 링크 오류 신고 1건을 담는 그릇 (report 테이블)
public class ReportDto {

	private int reportId;          // report_id      신고 번호
	private int lecId;             // lec_id         신고한 강의
	private String userId;         // user_id        신고한 회원
	private String reasonType;     // reason_type    DELETED / PRIVATE / WRONG_CATEGORY / ETC
	private String content;        // content        상세 내용 (NULL 가능, 300자까지)
	private String reportStatus;   // report_status  WAIT / DONE   (처리는 관리자 담당)
	private String result;         // result         FIXED / HIDDEN / NO_PROBLEM (NULL 가능)
	private Timestamp procDate;    // proc_date      처리일 (NULL 가능)
	private String delYn;          // del_yn         Y / N
	private Timestamp regDate;     // reg_date       신고일
	private Timestamp modDate;     // mod_date       수정일

	public int getReportId() {
		return reportId;
	}
	public void setReportId(int reportId) {
		this.reportId = reportId;
	}
	public int getLecId() {
		return lecId;
	}
	public void setLecId(int lecId) {
		this.lecId = lecId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getReasonType() {
		return reasonType;
	}
	public void setReasonType(String reasonType) {
		this.reasonType = reasonType;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getReportStatus() {
		return reportStatus;
	}
	public void setReportStatus(String reportStatus) {
		this.reportStatus = reportStatus;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public Timestamp getProcDate() {
		return procDate;
	}
	public void setProcDate(Timestamp procDate) {
		this.procDate = procDate;
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
