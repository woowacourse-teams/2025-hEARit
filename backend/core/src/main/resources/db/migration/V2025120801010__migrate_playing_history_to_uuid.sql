-- 1. orphan 제거
DELETE FROM playing_history
WHERE user_uuid IS NULL;

-- 2. memberId nullable로 변경
ALTER TABLE playing_history
    MODIFY COLUMN member_id BIGINT NULL;

-- 3. uuid not null
ALTER TABLE playing_history
    MODIFY COLUMN user_uuid CHAR(36) NOT NULL;

-- 4. uuid, hearitId에 유니크 적용
ALTER TABLE playing_history
    ADD CONSTRAINT uq_user_hearit UNIQUE (user_uuid, hearit_id);

-- 5. memberId대신 uuid로 새로운 index 추가
CREATE INDEX idx_user_updated_at
    ON playing_history (user_uuid, updated_at);

-- 6. memberId 기반 unique, index 제거
ALTER TABLE playing_history DROP INDEX uq_playing_history_member_hearit;
ALTER TABLE playing_history DROP INDEX idx_member_updated_at;
