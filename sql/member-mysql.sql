-- MySQL 8.0.16+ / 8.4. 접속할 데이터베이스를 먼저 선택한 후 실행한다.
-- 기존 테이블/데이터를 삭제하지 않는다. 기존 member가 있으면 구조를 검토한 후 별도로 마이그레이션한다.
CREATE TABLE IF NOT EXISTS member (
    member_id VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    member_pw VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    member_name VARCHAR(30) NOT NULL,
    email VARCHAR(100) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL,
    school_level VARCHAR(6) NOT NULL DEFAULT 'NONE',
    grade TINYINT UNSIGNED NULL,
    member_role VARCHAR(5) NOT NULL DEFAULT 'USER',
    member_status VARCHAR(7) NOT NULL DEFAULT 'ACTIVE',
    del_yn CHAR(1) NOT NULL DEFAULT 'N',
    reg_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    mod_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_member PRIMARY KEY (member_id),
    CONSTRAINT uk_member_email UNIQUE (email),
    CONSTRAINT ck_member_school CHECK (school_level IN ('NONE','ELEM','MIDDLE','HIGH')),
    CONSTRAINT ck_member_grade CHECK (
        (school_level = 'NONE' AND grade IS NULL)
        OR (school_level = 'ELEM' AND grade IS NOT NULL AND grade BETWEEN 1 AND 6)
        OR (school_level IN ('MIDDLE','HIGH') AND grade IS NOT NULL AND grade BETWEEN 1 AND 3)),
    CONSTRAINT ck_member_role CHECK (member_role IN ('USER','ADMIN')),
    CONSTRAINT ck_member_status CHECK (member_status IN ('ACTIVE','BLOCKED')),
    CONSTRAINT ck_member_del CHECK (del_yn IN ('N','Y'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
