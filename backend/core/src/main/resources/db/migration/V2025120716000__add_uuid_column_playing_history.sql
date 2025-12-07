-- 1. user_uuid 컬럼 추가 (NULL 허용)
ALTER TABLE playing_history
    ADD COLUMN user_uuid CHAR(36) AFTER id;

-- 2. 기존 유니크 제약 (member_id, hearit_id) 제거
ALTER TABLE playing_history
DROP INDEX uq_playing_history_member_hearit;

-- 3. 기존 인덱스 제거 (member_id, updated_at)
DROP INDEX idx_member_updated_at ON playing_history;
