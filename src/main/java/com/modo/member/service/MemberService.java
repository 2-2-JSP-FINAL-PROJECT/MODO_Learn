package com.modo.member.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import com.modo.common.enums.SchoolLevel;
import com.modo.common.enums.YnType;
import com.modo.common.exception.DuplicateMemberException;
import com.modo.common.exception.ModoException;
import com.modo.common.util.DBUtil;
import com.modo.common.util.PasswordUtil;
import com.modo.member.dao.MemberDAO;
import com.modo.member.dto.LoginUser;
import com.modo.member.dto.MemberDTO;


//재관작성
public class MemberService {

	private final MemberDAO memberDAO = new MemberDAO();

	// 로그인 기능
	public LoginUser login(String memberId, String memberPw) {

		if (memberId == null || memberId.isBlank() ||
				memberPw == null || memberPw.isBlank()) {
			throw new ModoException("아이디와 비밀번호를 입력해주세요.");
		}

		// Member 테이블 조회 후 메서드를 이용해 일치 확인
		try (Connection conn = DBUtil.getConnection()) {
			MemberDTO member = memberDAO.findById(conn, memberId.trim());

			if (member == null || !PasswordUtil.matches(memberPw, member.getMemberPw())) {
				throw new ModoException("아이디 또는 비밀번호가 일치하지 않습니다.");
			}

			// 탈퇴는 DEL_YN = 'Y'
			if (member.getDelYn() == YnType.Y) {
				throw new ModoException("탈퇴한 회원입니다.");
			}

			switch (member.getMemberStatus()) {
				case BLOCKED:
					throw new ModoException("차단된 계정입니다. 관리자에게 문의해주세요.");
				case ACTIVE:
					break;
			}

			// MemberDTO 그대로 내보내기 X -> 민감한 정보를 뺀 LoginUser만 내보내기
			return new LoginUser(member.getMemberId(), member.getMemberName(),
					member.getMemberRole());

		} catch (SQLException e) {
			throw new ModoException("로그인 처리 중 오류가 발생했습니다.", e);
		}

	}

	// 회원가입 기능
	public void join(MemberDTO dto, String memberPw2) {
		validateJoin(dto, memberPw2);

		try (Connection conn = DBUtil.getConnection()){

			if (memberDAO.findById(conn, dto.getMemberId()) != null) {
				throw new DuplicateMemberException("이미 사용 중인 아이디입니다.");
			}
			if (memberDAO.existsEmail(conn, dto.getEmail())) {
				throw new DuplicateMemberException("이미 사용 중인 이메일입니다.");
			}
			// util에 PasswordUtil 사용해 64자리 암호화
			dto.setMemberPw(PasswordUtil.sha256(dto.getMemberPw()));

			memberDAO.insert(conn, dto);

		} catch (SQLIntegrityConstraintViolationException e) {
			throw new DuplicateMemberException("이미 사용중인 아이디 또는 이메일입니다.");
		} catch (SQLException e) {
			throw new ModoException("회원가입 처리 중 오류가 발생하였습니다.", e);
		}
	}

	// 회원가입 정규식
	private void validateJoin(MemberDTO dto, String memberPw2) {
		String id     = dto.getMemberId();
		String pw     = dto.getMemberPw();
		String name   = dto.getMemberName();
		String email  = dto.getEmail();
		SchoolLevel school = dto.getSchoolLevel();
		Integer grade = dto.getGrade();

		if (id == null || !id.matches("[a-z0-9]{4,20}")) {
			throw new ModoException("아이디는 영문 소문자와 숫자 4~20자로 입력해주세요.");
		}
		if (pw == null || !pw.matches("(?=.*[A-Za-z])(?=.*\\d).{8,20}")) {
			throw new ModoException("비밀번호는 영문과 숫자를 포함해 8~20자로 입력해주세요.");
		}
		if (!pw.equals(memberPw2)) {
			throw new ModoException("비밀번호 확인이 일치하지 않습니다.");
		}
		if (name == null || name.isBlank() || name.length() > 30) {
			throw new ModoException("이름을 입력해주세요. (30자 이내)");
		}
		if (email == null || email.length() > 100 || !email.matches("[\\w.+-]+@[\\w-]+\\.[\\w.]+")) {
			throw new ModoException("이메일 형식이 올바르지 않습니다.");
		}
		if (school == null) {
			throw new ModoException("학교급을 선택해주세요.");
		}

		// 학교급별 학년 (DB check와 같은 규칙, 범위는 SchoolLevel ENUM에)
		if (!school.isValidGrade(grade)) {
			if (school == SchoolLevel.NONE) {
				throw new ModoException("학부모·교사는 학년을 선택하지 않습니다.");
			}
			throw new ModoException(school.getLabel() + "는 1~" + school.getMaxGrade() + "학년 중에서 선택해주세요.");
		}
	}
}
