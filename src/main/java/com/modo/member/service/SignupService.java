package com.modo.member.service;

import com.modo.common.util.PasswordUtil;
import com.modo.member.dao.MemberDAO;
import com.modo.member.dto.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;

public class SignupService {
    private final MemberDAO dao;
    public SignupService() { this(new MemberDAO()); }
    public SignupService(MemberDAO dao) { this.dao = dao; }

    public void signup(SignupRequestDTO request) throws SQLException {
        if (request == null) throw invalid("가입 정보가 필요합니다.");
        String id = validId(request.memberId());
        String password = request.memberPw();
        if (password == null || password.length() < 8 || password.length() > 128 || password.isBlank())
            throw invalid("비밀번호는 8~128자로 입력해 주세요.");
        if (!password.equals(request.memberPwConfirm())) throw invalid("비밀번호 확인이 일치하지 않습니다.");
        String name = clean(request.memberName());
        if (name.isEmpty() || name.codePointCount(0, name.length()) > 30 || name.codePoints().anyMatch(Character::isISOControl))
            throw invalid("이름은 제어문자 없이 1~30자로 입력해 주세요.");
        String email = validEmail(request.email());
        String school = clean(request.schoolLevel());
        if (!Set.of("NONE", "ELEM", "MIDDLE", "HIGH").contains(school)) throw invalid("학교급을 확인해 주세요.");
        String rawGrade = clean(request.grade());
        Integer grade = null;
        if (school.equals("NONE")) {
            if (!rawGrade.isEmpty()) throw invalid("학교급이 NONE이면 학년을 비워 주세요.");
        } else {
            if (!rawGrade.matches("[1-6]")) throw invalid("학교급에 맞는 학년을 입력해 주세요.");
            grade = Integer.valueOf(rawGrade);
            if (!school.equals("ELEM") && grade > 3) throw invalid("중·고등학교 학년은 1~3입니다.");
        }
        LoginMemberDTO member = new LoginMemberDTO(id, name, email, school, grade, "USER");
        String encoded = PasswordUtil.hash(password);
        // 트랜잭션은 Service가 관리한다. 추가 기본 회원 데이터도 같은 연결로 저장할 수 있다.
        try (Connection connection = dao.openConnection()) {
            connection.setAutoCommit(false);
            try { dao.insert(connection, member, encoded); connection.commit(); }
            catch (SQLException e) {
                try { connection.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        } catch (SQLException e) {
            for (SQLException cause = e; cause != null; cause = cause.getNextException()) {
                if (cause.getErrorCode() == 1062 || "23505".equals(cause.getSQLState()))
                    throw new MemberException(409, "DUPLICATE_MEMBER", "이미 사용 중인 아이디 또는 이메일입니다.");
            }
            throw e;
        }
    }
    public boolean isIdAvailable(String id) throws SQLException { return !dao.existsById(validId(id)); }
    public boolean isEmailAvailable(String email) throws SQLException { return !dao.existsByEmail(validEmail(email)); }
    private static String validId(String value) {
        String id = clean(value);
        if (!id.matches("[A-Za-z0-9_]{4,20}")) throw invalid("아이디는 영문, 숫자, 밑줄 4~20자여야 합니다.");
        return id;
    }
    private static String validEmail(String value) {
        String email = clean(value).toLowerCase(Locale.ROOT);
        if (email.length() > 100 || !email.matches("[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+"))
            throw invalid("올바른 이메일을 100자 이내로 입력해 주세요.");
        return email;
    }
    private static String clean(String value) { return value == null ? "" : value.strip(); }
    private static MemberException invalid(String message) { return new MemberException(400, "VALIDATION_ERROR", message); }
}
