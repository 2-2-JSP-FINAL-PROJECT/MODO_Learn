package com.modo.notice.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.modo.common.util.DBUtil;
import com.modo.notice.dto.NoticeDto;

// 공지사항 DB 담당 (사용자 화면용 : 목록 · 상세 · 조회수)
public class NoticeDao {

	// 목록 : 고정글(pin_yn = 'Y') 먼저, 그다음 최신순
	// nType 이 null 이면 전체, "NOTICE" / "EVENT" / "INFO" 면 그 구분만
	public List<NoticeDto> findList(String nType) throws SQLException {
		String sql = "SELECT * FROM notice WHERE del_yn = 'N'";
		if (nType != null) {
			sql += " AND n_type = ?";
		}
		sql += " ORDER BY pin_yn DESC, notice_id DESC";   // 'Y' > 'N' 이라 DESC 면 고정글이 위로

		List<NoticeDto> list = new ArrayList<>();
		try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			if (nType != null) {
				ps.setString(1, nType);
			}
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(toDto(rs));
				}
			}
		}
		return list;
	}// findList

	// 공지 하나
	public NoticeDto findById(int noticeId) throws SQLException {
		String sql = "SELECT * FROM notice WHERE notice_id = ? AND del_yn = 'N'";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, noticeId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? toDto(rs) : null;
			}
		}
	}// findById

	// 조회수 +1
	public int increaseViewCount(int noticeId) throws SQLException {
		String sql = "UPDATE notice SET view_count = view_count + 1 WHERE notice_id = ? AND del_yn = 'N'";

		try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, noticeId);
			return ps.executeUpdate();
		}
	}// increaseViewCount

	// 결과표의 현재 줄 → NoticeDto (목록과 상세가 같이 씀)
	private NoticeDto toDto(ResultSet rs) throws SQLException {
		NoticeDto n = new NoticeDto();
		n.setNoticeId(rs.getInt("notice_id"));
		n.setWriter(rs.getString("writer"));
		n.setnType(rs.getString("n_type"));
		n.setTitle(rs.getString("title"));
		n.setContent(rs.getString("content"));
		n.setPinYn(rs.getString("pin_yn"));
		n.setBannerYn(rs.getString("banner_yn"));
		n.setViewCount(rs.getInt("view_count"));
		n.setDelYn(rs.getString("del_yn"));
		n.setRegDate(rs.getTimestamp("reg_date"));
		n.setModDate(rs.getTimestamp("mod_date"));
		return n;
	}

}// NoticeDao
