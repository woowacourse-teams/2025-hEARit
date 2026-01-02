-- 1. member_id → user_uuid 매핑
UPDATE playing_history ph
    JOIN member m
ON ph.member_id = m.id
    SET ph.user_uuid = m.uuid
WHERE ph.user_uuid IS NULL;
