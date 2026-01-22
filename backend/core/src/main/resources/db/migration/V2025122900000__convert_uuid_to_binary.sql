
-- 1. 멤버테이블 마이그레이션 :  uuid VARCHAR(36) → BINARY(16)
-- =====================================================

-- 임시 칼럼 생성
ALTER TABLE member
    ADD COLUMN uuid_binary BINARY(16) AFTER uuid;

-- 마이그레이션
UPDATE member
SET uuid_binary = UNHEX(REPLACE(uuid, '-', ''))
WHERE uuid IS NOT NULL;

-- uuid not null 처리
ALTER TABLE member
    MODIFY COLUMN uuid_binary BINARY(16) NOT NULL;

-- 기존 임시 drop 하고 rename
ALTER TABLE member
    DROP INDEX ux_member_uuid,
DROP COLUMN uuid;

ALTER TABLE member
    CHANGE COLUMN uuid_binary uuid BINARY(16) NOT NULL;

CREATE UNIQUE INDEX ux_member_uuid ON member (uuid);


-- 2. 재생기록
-- =====================================================

ALTER TABLE playing_history
    ADD COLUMN user_uuid_binary BINARY(16) AFTER user_uuid;

UPDATE playing_history
SET user_uuid_binary = UNHEX(REPLACE(user_uuid, '-', ''))
WHERE user_uuid IS NOT NULL;

ALTER TABLE playing_history
    DROP INDEX uq_user_hearit,
    DROP INDEX idx_user_updated_at;

ALTER TABLE playing_history
DROP COLUMN user_uuid;

ALTER TABLE playing_history
    CHANGE COLUMN user_uuid_binary user_uuid BINARY(16) NOT NULL;

ALTER TABLE playing_history
    ADD CONSTRAINT uq_user_hearit UNIQUE (user_uuid, hearit_id);

CREATE INDEX idx_user_updated_at
    ON playing_history (user_uuid, updated_at);


-- 3. 북마크
-- =====================================================

ALTER TABLE bookmark
    ADD COLUMN member_uuid BINARY(16) AFTER member_id;

UPDATE bookmark b
    INNER JOIN member m ON b.member_id = m.id
SET b.member_uuid = m.uuid;

ALTER TABLE bookmark
    MODIFY COLUMN member_uuid BINARY(16) NOT NULL;

ALTER TABLE bookmark
    DROP FOREIGN KEY fk_bookmark_member,
    DROP INDEX uq_bookmark_member_hearit;

ALTER TABLE bookmark
DROP COLUMN member_id;

ALTER TABLE bookmark
    ADD CONSTRAINT uq_bookmark_member_hearit UNIQUE (member_uuid, hearit_id);


-- 4. 리프레시 토큰
-- =====================================================

ALTER TABLE refresh_token
    ADD COLUMN member_uuid BINARY(16) AFTER member_id;

UPDATE refresh_token rt
    INNER JOIN member m ON rt.member_id = m.id
SET rt.member_uuid = m.uuid;

ALTER TABLE refresh_token
    MODIFY COLUMN member_uuid BINARY(16) NOT NULL;

ALTER TABLE refresh_token
    DROP INDEX uq_refresh_token_member_id;

ALTER TABLE refresh_token
DROP COLUMN member_id;

ALTER TABLE refresh_token
    ADD CONSTRAINT uq_refresh_token_member_uuid UNIQUE (member_uuid);


-- 5. 탐색 스코어
-- =====================================================

ALTER TABLE explore_score
    ADD COLUMN user_uuid_binary BINARY(16) AFTER user_uuid;

UPDATE explore_score
SET user_uuid_binary = UNHEX(REPLACE(user_uuid, '-', ''))
WHERE user_uuid IS NOT NULL;

ALTER TABLE explore_score
    DROP INDEX ux_user_uuid_hearit_id;

ALTER TABLE explore_score
DROP COLUMN user_uuid;

ALTER TABLE explore_score
    CHANGE COLUMN user_uuid_binary user_uuid BINARY(16) NOT NULL;

ALTER TABLE explore_score
    ADD CONSTRAINT uq_explore_score_user_hearit UNIQUE (user_uuid, hearit_id);
