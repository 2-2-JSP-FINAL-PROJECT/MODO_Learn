package com.modo.notice.service;

import java.sql.SQLException;
import java.util.List;

import com.modo.notice.dao.NoticeDao;
import com.modo.notice.dto.NoticeDto;

// 공지사항 업무 규칙 (사용자 화면용)
public class NoticeService {

	private NoticeDao dao = new NoticeDao();

	// 목록 : 구분 값이 이상하면 전체로 보여 준다 (주소를 직접 바꿔 넣는 경우 대비)
	public List<NoticeDto> getList(String nType) throws SQLException {
		if (!"NOTICE".equals(nType) && !"EVENT".equals(nType) && !"INFO".equals(nType)) {
			nType = null;
		}
		return dao.findList(nType);
	}

	// 상세 : 조회수 +1 → 공지 가져오기
	public NoticeDto getDetail(int noticeId) throws SQLException {
		dao.increaseViewCount(noticeId);
		return dao.findById(noticeId);
	}
}
