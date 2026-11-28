package com.modo.report.dto;

import java.time.LocalDateTime;

import com.modo.common.enums.YnType;

// 성환작성
// 링크 오류 신고 1건을 담는 DTO (report 테이블)
// ReasonType, ReportStatus, Result, DelYn은 ENUM값으로 관리(DB는 check)
public class ReportDTO {

	private int reportId;                     // REPORT_ID      신고 번호 (AUTO_INCREMENT)
	private int lecId;                        // LEC_ID         신고한 강의 번호
	private String memberId;                  // MEMBER_ID      신고한 회원 아이디
	private ReasonType reasonType;            // REASON_TYPE    DELETED / PRIVATE / WRONG_CATEGORY / ETC
	private String content;                   // CONTENT        상세 내용 (300자 이내, 없으면 null)
	private ReportStatus reportStatus;        // REPORT_STATUS  WAIT / DONE
	private ReportResult result;              // RESULT         FIXED / HIDDEN / NO_PROBLEM (WAIT면 null)
	private LocalDateTime procDate;           // PROC_DATE      처리일 (WAIT면 null)
	private YnType delYn;                     // DEL_YN         삭제 여부 Y / N
	private LocalDateTime regDate;            // REG_DATE       신고일
	private LocalDateTime modDate;            // MOD_DATE       수정일 (DB가 자동 갱신)

	// 아래 2개는 report 테이블 컬럼이 아니라 관리자 목록에서 JOIN으로 가져오는 값
	private String lecTitle;                  // lecture.TITLE        강의명
	private String memberName;                // member.MEMBER_NAME   신고한 회원 이름

	public ReportDTO() {
	}

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

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public ReasonType getReasonType() {
		return reasonType;
	}

	public void setReasonType(ReasonType reasonType) {
		this.reasonType = reasonType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public ReportStatus getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(ReportStatus reportStatus) {
		this.reportStatus = reportStatus;
	}

	public ReportResult getResult() {
		return result;
	}

	public void setResult(ReportResult result) {
		this.result = result;
	}

	public LocalDateTime getProcDate() {
		return procDate;
	}

	public void setProcDate(LocalDateTime procDate) {
		this.procDate = procDate;
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

	public String getLecTitle() {
		return lecTitle;
	}

	public void setLecTitle(String lecTitle) {
		this.lecTitle = lecTitle;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}


	@Override
	public String toString() {
		return "ReportDTO [reportId=" + reportId + ", lecId=" + lecId + ", memberId=" + memberId
				+ ", reasonType=" + reasonType + ", content=" + content + ", reportStatus=" + reportStatus
				+ ", result=" + result + ", procDate=" + procDate + ", delYn=" + delYn
				+ ", regDate=" + regDate + "]";
	}
}
