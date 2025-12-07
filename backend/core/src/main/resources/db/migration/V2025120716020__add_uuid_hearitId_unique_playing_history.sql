-- 1. 새로운 유니크 제약 추가
ALTER TABLE playing_history
    ADD CONSTRAINT uq_playing_history_user_hearit
        UNIQUE (user_uuid, hearit_id);

-- 2. 인덱스 추가 (기존 member_id, updated_at과 동일하게)
CREATE INDEX idx_user_updated_at
    ON playing_history (user_uuid, updated_at);
