DELIMITER
$$

DROP PROCEDURE IF EXISTS MigrateIdempotent $$

CREATE PROCEDURE MigrateIdempotent()
BEGIN
-- =====================================================
-- 1. Member 테이블 (uuid: VARCHAR -> BINARY)
-- =====================================================
-- 조건: uuid 칼럼이 문자열(varchar/char)인 경우에만 실행
IF
EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'member'
        AND COLUMN_NAME = 'uuid' AND (DATA_TYPE = 'varchar' OR DATA_TYPE = 'char')
    ) THEN
-- 1) 임시 컬럼 생성 및 데이터 이관
ALTER TABLE member
    ADD COLUMN uuid_binary BINARY(16) AFTER uuid;
UPDATE member
SET uuid_binary = UNHEX(REPLACE(uuid, '-', ''))
WHERE uuid IS NOT NULL;
ALTER TABLE member MODIFY COLUMN uuid_binary BINARY(16) NOT NULL;

-- 2) 기존 인덱스 삭제 (존재하면 삭제)
IF
EXISTS (SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'member' AND INDEX_NAME = 'ux_member_uuid') THEN
ALTER TABLE member DROP INDEX ux_member_uuid;
END IF;

-- 3) 구 컬럼 삭제 및 신규 컬럼 이름 변경
ALTER TABLE member DROP COLUMN uuid;
ALTER TABLE member CHANGE COLUMN uuid_binary uuid BINARY(16) NOT NULL;

-- 4) 신규 인덱스 생성
CREATE UNIQUE INDEX ux_member_uuid ON member (uuid);
END IF;


-- =====================================================
-- 2. Playing History (user_uuid: VARCHAR -> BINARY)
-- =====================================================
IF
EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'playing_history'
        AND COLUMN_NAME = 'user_uuid' AND (DATA_TYPE = 'varchar' OR DATA_TYPE = 'char')
    ) THEN

-- 1) 변환 작업
ALTER TABLE playing_history
    ADD COLUMN user_uuid_binary BINARY(16) AFTER user_uuid;
UPDATE playing_history
SET user_uuid_binary = UNHEX(REPLACE(user_uuid, '-', ''))
WHERE user_uuid IS NOT NULL;

-- 2) 기존 인덱스/제약조건 정리 (이름이 뭐든 존재하면 삭제)
IF
EXISTS (SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'playing_history' AND INDEX_NAME = 'uq_user_hearit') THEN
ALTER TABLE playing_history DROP INDEX uq_user_hearit;
END IF;
        IF
EXISTS (SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'playing_history' AND INDEX_NAME = 'idx_user_updated_at') THEN
ALTER TABLE playing_history DROP INDEX idx_user_updated_at;
END IF;

-- 3) 컬럼 교체
ALTER TABLE playing_history DROP COLUMN user_uuid;
ALTER TABLE playing_history CHANGE COLUMN user_uuid_binary user_uuid BINARY(16) NOT NULL;

-- 4) 신규 제약조건 생성
ALTER TABLE playing_history
    ADD CONSTRAINT uq_user_hearit UNIQUE (user_uuid, hearit_id);
CREATE INDEX idx_user_updated_at ON playing_history (user_uuid, updated_at);
END IF;


-- =====================================================
-- 3. Bookmark (member_id -> member_uuid)
-- =====================================================
-- 조건: 구 컬럼 'member_id'가 아직 살아있는 경우 실행
IF
EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bookmark' AND COLUMN_NAME = 'member_id'
    ) THEN
        -- 1) [중요] 동적 FK 삭제 로직 (어떤 랜덤 이름이라도 찾아서 지움)
        SET @bk_fk_name = (
            SELECT CONSTRAINT_NAME
            FROM information_schema.KEY_COLUMN_USAGE
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'bookmark'
              AND COLUMN_NAME = 'member_id'
              AND REFERENCED_TABLE_NAME = 'member'
            LIMIT 1
        );

IF
@bk_fk_name IS NOT NULL THEN
            SET @drop_sql = CONCAT('ALTER TABLE bookmark DROP FOREIGN KEY ', @bk_fk_name);
PREPARE stmt FROM @drop_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
END IF;

-- 2) 기존 인덱스 정리
IF
EXISTS (SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bookmark' AND INDEX_NAME = 'uq_bookmark_member_hearit') THEN
ALTER TABLE bookmark DROP INDEX uq_bookmark_member_hearit;
END IF;

IF
EXISTS (SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bookmark' AND INDEX_NAME = 'bookmark_unique_constraint') THEN
ALTER TABLE bookmark DROP INDEX bookmark_unique_constraint;
END IF;

-- 3) 컬럼 추가 및 데이터 이관
ALTER TABLE bookmark
    ADD COLUMN member_uuid BINARY(16) AFTER member_id;
UPDATE bookmark b INNER JOIN member m
ON b.member_id = m.id SET b.member_uuid = m.uuid;
ALTER TABLE bookmark MODIFY COLUMN member_uuid BINARY(16) NOT NULL;

-- 4) 구 컬럼 삭제
ALTER TABLE bookmark DROP COLUMN member_id;

-- 5) 신규 UK 생성 (FK는 요청하신 대로 생성하지 않음)
ALTER TABLE bookmark
    ADD CONSTRAINT uq_bookmark_member_hearit UNIQUE (member_uuid, hearit_id);
END IF;


-- =====================================================
-- 4. Refresh Token (member_id -> member_uuid)
-- =====================================================
IF
EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'refresh_token' AND COLUMN_NAME = 'member_id'
    ) THEN
        -- 1) 동적 FK 삭제 (혹시 있을 경우 대비)
        SET @rt_fk_name = (
            SELECT CONSTRAINT_NAME
            FROM information_schema.KEY_COLUMN_USAGE
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'refresh_token'
              AND COLUMN_NAME = 'member_id'
              AND REFERENCED_TABLE_NAME = 'member'
            LIMIT 1
        );
        IF
@rt_fk_name IS NOT NULL THEN
            SET @drop_rt_sql = CONCAT('ALTER TABLE refresh_token DROP FOREIGN KEY ', @rt_fk_name);
PREPARE stmt_rt FROM @drop_rt_sql;
EXECUTE stmt_rt;
DEALLOCATE PREPARE stmt_rt;
END IF;

-- 2) 기존 인덱스 삭제
IF
EXISTS (SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'refresh_token' AND INDEX_NAME = 'uq_refresh_token_member_id') THEN
ALTER TABLE refresh_token DROP INDEX uq_refresh_token_member_id;
END IF;

IF
EXISTS (SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'refresh_token' AND INDEX_NAME = 'UKdnbbikqdsc2r2cee1afysqfk9') THEN
ALTER TABLE refresh_token DROP INDEX UKdnbbikqdsc2r2cee1afysqfk9;
END IF;

        -- 3) 컬럼 작업
ALTER TABLE refresh_token
    ADD COLUMN member_uuid BINARY(16) AFTER member_id;
UPDATE refresh_token rt INNER JOIN member m
ON rt.member_id = m.id SET rt.member_uuid = m.uuid;
ALTER TABLE refresh_token MODIFY COLUMN member_uuid BINARY(16) NOT NULL;
ALTER TABLE refresh_token DROP COLUMN member_id;

-- 4) 신규 제약조건
ALTER TABLE refresh_token
    ADD CONSTRAINT uq_refresh_token_member_uuid UNIQUE (member_uuid);
END IF;


-- =====================================================
-- 5. Explore Score (user_uuid: VARCHAR -> BINARY)
-- =====================================================
IF
EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'explore_score'
    AND COLUMN_NAME = 'user_uuid' AND (DATA_TYPE = 'varchar' OR DATA_TYPE = 'char')
) THEN

-- 1) 변환 작업
ALTER TABLE explore_score
    ADD COLUMN user_uuid_binary BINARY(16) AFTER user_uuid;
UPDATE explore_score
SET user_uuid_binary = UNHEX(REPLACE(user_uuid, '-', ''))
WHERE user_uuid IS NOT NULL;

-- 2) 인덱스 정리 (Test DB 등에서 충돌나는 이름들 확실히 제거)
IF
EXISTS (SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'explore_score' AND INDEX_NAME = 'ux_user_uuid_hearit_id') THEN
ALTER TABLE explore_score DROP INDEX ux_user_uuid_hearit_id;
END IF;
        IF
EXISTS (SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'explore_score' AND INDEX_NAME = 'idx_explore_score_user_cursor') THEN
ALTER TABLE explore_score DROP INDEX idx_explore_score_user_cursor;
END IF;
        IF
EXISTS (SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'explore_score' AND INDEX_NAME = 'idx_explore_score_user_uuid') THEN
ALTER TABLE explore_score DROP INDEX idx_explore_score_user_uuid;
END IF;

-- 3) 컬럼 교체
ALTER TABLE explore_score DROP COLUMN user_uuid;
ALTER TABLE explore_score CHANGE COLUMN user_uuid_binary user_uuid BINARY(16) NOT NULL;

-- 4) 신규 제약조건/인덱스 생성
ALTER TABLE explore_score
    ADD CONSTRAINT uq_explore_score_user_hearit UNIQUE (user_uuid, hearit_id);
CREATE INDEX idx_explore_score_user_uuid ON explore_score (user_uuid);
CREATE INDEX idx_explore_score_user_cursor ON explore_score (user_uuid, cursor_id);
END IF;

END $$

DELIMITER ;

CALL MigrateIdempotent();
DROP PROCEDURE MigrateIdempotent;
