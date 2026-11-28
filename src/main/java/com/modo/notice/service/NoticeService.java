package com.modo.notice.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.modo.common.exception.ModoException;
import com.modo.common.util.DBUtil;
import com.modo.member.dto.LoginUser;
import com.modo.notice.dao.NoticeDAO;
import com.modo.notice.dto.NoticeDTO;
import com.modo.notice.dto.NoticeType;

// 성환작성
// 공지사항 업무 규칙 (사용자: 목록·상세 / 관리자: 등록·수정·삭제)
public class NoticeService {

	private final NoticeDAO noticeDAO = new NoticeDAO();

	private static final int TITLE_MAX = 200;      // title   VARCHAR(200) : 글자 수
	private static final int CONTENT_MAX = 4000;   // content VARCHAR(4000) : 글자 수

	// ===== 사용자 =====

	// 공지 목록 (noticeType이 null이면 전체)
	public List<NoticeDTO> getList(NoticeType noticeType) {
		try (Connection conn = DBUtil.getConnection()) {
			return noticeDAO.findList(conn, noticeType);
		} catch (SQLException e) {
			throw new ModoException("공지 목록 조회 중 오류가 발생했습니다.", e);
		}
	}

	// 공지 상세 : 조회수 +1 -> 공지 가져오기
	public NoticeDTO getDetail(int noticeId) {
		try (Connection conn = DBUtil.getConnection()) {
			// 바뀐 줄이 0개면 없거나 삭제된 글
			if (noticeDAO.increaseViewCount(conn, noticeId) == 0) {
				throw new ModoException("이미 삭제되었거나 없는 공지입니다.");
			}
			return noticeDAO.findById(conn, noticeId);

		} catch (SQLException e) {
			throw new ModoException("공지 조회 중 오류가 발생했습니다.", e);
		}
	}

	// ===== 관리자 =====

	// 수정 화면에 채울 공지 (조회수는 올리지 않음)
	public NoticeDTO getForEdit(LoginUser loginUser, int noticeId) {
		checkAdmin(loginUser);

		try (Connection conn = DBUtil.getConnection()) {
			NoticeDTO notice = noticeDAO.findById(conn, noticeId);
			if (notice == null) {
				throw new ModoException("이미 삭제되었거나 없는 공지입니다.");
			}
			return notice;

		} catch (SQLException e) {
			throw new ModoException("공지 조회 중 오류가 발생했습니다.", e);
		}
	}

	// 공지 등록 -> 새 공지 번호 반환
	public int write(LoginUser loginUser, NoticeDTO dto) {
		checkAdmin(loginUser);
		validateNotice(dto);

		// 작성자는 화면 값이 아니라 로그인한 관리자 아이디로
		dto.setWriter(loginUser.getMemberId());

		try (Connection conn = DBUtil.getConnection()) {
			return noticeDAO.insert(conn, dto);
		} catch (SQLException e) {
			throw new ModoException("공지 등록 처리 중 오류가 발생했습니다.", e);
		}
	}

	// 공지 수정
	public void edit(LoginUser loginUser, NoticeDTO dto) {
		checkAdmin(loginUser);
		validateNotice(dto);

		try (Connection conn = DBUtil.getConnection()) {
			if (noticeDAO.update(conn, dto) == 0) {
				throw new ModoException("이미 삭제되었거나 없는 공지입니다.");
			}
		} catch (SQLException e) {
			throw new ModoException("공지 수정 처리 중 오류가 발생했습니다.", e);
		}
	}

	// 공지 삭제 (소프트 삭제)
	public void delete(LoginUser loginUser, int noticeId) {
		checkAdmin(loginUser);

		try (Connection conn = DBUtil.getConnection()) {
			if (noticeDAO.delete(conn, noticeId) == 0) {
				throw new ModoException("이미 삭제되었거나 없는 공지입니다.");
			}
		} catch (SQLException e) {
			throw new ModoException("공지 삭제 처리 중 오류가 발생했습니다.", e);
		}
	}

	// 관리자인지 한 번 더 확인 (LoginFilter가 막아 주지만 Service에서도 확인)
	private void checkAdmin(LoginUser loginUser) {
		if (loginUser == null || !loginUser.isAdmin()) {
			throw new ModoException("관리자만 이용할 수 있습니다.");
		}
	}

	// 공지 입력값 검사 (DB 컬럼 길이와 같은 규칙)
	private void validateNotice(NoticeDTO dto) {
		String title = dto.getTitle();
		String content = dto.getContent();

		if (dto.getNoticeType() == null) {
			throw new ModoException("구분을 선택해주세요.");
		}
		if (title == null || title.length() > TITLE_MAX) {
			throw new ModoException("제목을 입력해주세요. (" + TITLE_MAX + "자 이내)");
		}
		if (content == null || content.length() > CONTENT_MAX) {
			throw new ModoException("내용을 입력해주세요. (" + CONTENT_MAX + "자 이내)");
		}
		if (dto.getPinYn() == null || dto.getBannerYn() == null) {
			throw new ModoException("상단 고정·배너 여부가 올바르지 않습니다.");
		}
	}
}
