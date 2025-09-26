-- 최근 들은 재생 기록 조회
CREATE INDEX idx_member_updated_at ON playing_history(member_id, updated_at DESC);
