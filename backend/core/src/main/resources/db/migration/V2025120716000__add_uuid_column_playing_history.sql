-- 1. user_uuid 컬럼 추가 (NULL 허용)
ALTER TABLE playing_history
    ADD COLUMN user_uuid CHAR(36) AFTER id;
