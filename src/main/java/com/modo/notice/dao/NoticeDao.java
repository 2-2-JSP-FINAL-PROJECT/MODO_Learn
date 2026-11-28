package com.modo.notice.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.modo.common.enums.YnType;
import com.modo.notice.dto.NoticeDTO;
import com.modo.notice.dto.NoticeType;

// 성환작성
// 공지사항 DB 담당 (Connection은 Service가 열고 닫음)
public class NoticeDAO {

	// 공지 목록 (삭제 안 된 글만)
	// noticeType이 null이면 전체, 아니면 그 구분만
	// 고정글(PIN_YN = 'Y') 먼저, 그다음 최신순 ('Y' > 'N' 이라 DESC면 Y가 위로)
	public List<NoticeDTO> findList(Connection conn, NoticeType noticeType) throws SQLException {

		String sql = "SELECT * FROM notice WHERE del_yn = 'N'";
		if (noticeType != null) {
			sql += " AND n_type = ?";
		}
		sql += " ORDER BY pin_yn DESC, notice_id DESC";

		List<NoticeDTO> list = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			if (noticeType != null) {
				ps.setString(1, noticeType.name());   // ENUM -> 문자열
			}
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(noticeDtoMapper(rs));
				}
			}
		}
		return list;
	}

	// 공지 1개 조회 (삭제된 글이면 null)
	public NoticeDTO findById(Connection conn, int noticeId) throws SQLException {

		String sql = "SELECT * FROM notice WHERE notice_id = ? AND del_yn = 'N'";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, noticeId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return noticeDtoMapper(rs);
				}
				return null;
			}
		}
	}

	// 조회수 +1 (바뀐 줄 수 반환 -> 0이면 없는 글)
	public int increaseViewCount(Connection conn, int noticeId) throws SQLException {

		String sql = "UPDATE notice SET view_count = view_count + 1 WHERE notice_id = ? AND del_yn = 'N'";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, noticeId);
			return ps.executeUpdate();
		}
	}

	// 공지 등록 -> 새로 생긴 공지 번호 반환 (VIEW_COUNT, DEL_YN, REG_DATE는 DB 기본값)
	public int insert(Connection conn, NoticeDTO dto) throws SQLException {

		String sql = "INSERT INTO notice (writer, n_type, title, content, pin_yn, banner_yn) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";

		// RETURN_GENERATED_KEYS : AUTO_INCREMENT로 만들어진 번호를 돌려받기
		try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, dto.getWriter());
			ps.setString(2, dto.getNoticeType().name());
			ps.setString(3, dto.getTitle());
			ps.setString(4, dto.getContent());
			ps.setString(5, dto.getPinYn().name());
			ps.setString(6, dto.getBannerYn().name());
			ps.executeUpdate();

			try (ResultSet keys = ps.getGeneratedKeys()) {
				keys.next();
				return keys.getInt(1);
			}
		}
	}

	// 공지 수정 (작성자는 그대로, MOD_DATE는 DB가 자동 갱신)
	public int update(Connection conn, NoticeDTO dto) throws SQLException {

		String sql = "UPDATE notice SET n_type = ?, title = ?, content = ?, pin_yn = ?, banner_yn = ? "
				+ "WHERE notice_id = ? AND del_yn = 'N'";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, dto.getNoticeType().name());
			ps.setString(2, dto.getTitle());
			ps.setString(3, dto.getContent());
			ps.setString(4, dto.getPinYn().name());
			ps.setString(5, dto.getBannerYn().name());
			ps.setInt(6, dto.getNoticeId());
			return ps.executeUpdate();
		}
	}

	// 공지 삭제 (진짜 지우지 않고 DEL_YN = 'Y'로 바꾸는 소프트 삭제)
	public int delete(Connection conn, int noticeId) throws SQLException {

		String sql = "UPDATE notice SET del_yn = 'Y' WHERE notice_id = ? AND del_yn = 'N'";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, noticeId);
			return ps.executeUpdate();
		}
	}

	// NoticeDTO로 변환 (목록·상세가 같이 씀)
	private NoticeDTO noticeDtoMapper(ResultSet rs) throws SQLException {

		NoticeDTO dto = new NoticeDTO();

		dto.setNoticeId(rs.getInt("NOTICE_ID"));
		dto.setWriter(rs.getString("WRITER"));
		dto.setTitle(rs.getString("TITLE"));
		dto.setContent(rs.getString("CONTENT"));
		dto.setViewCount(rs.getInt("VIEW_COUNT"));

		// 문자열 -> ENUM값으로 변환
		dto.setNoticeType(NoticeType.valueOf(rs.getString("N_TYPE")));
		dto.setPinYn(YnType.valueOf(rs.getString("PIN_YN")));
		dto.setBannerYn(YnType.valueOf(rs.getString("BANNER_YN")));
		dto.setDelYn(YnType.valueOf(rs.getString("DEL_YN")));

		// DATETIME -> LocalDateTime으로 변환
		dto.setRegDate(rs.getObject("REG_DATE", LocalDateTime.class));
		dto.setModDate(rs.getObject("MOD_DATE", LocalDateTime.class));

		return dto;
	}
}
