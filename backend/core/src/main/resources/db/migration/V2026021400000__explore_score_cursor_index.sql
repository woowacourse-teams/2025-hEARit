CREATE INDEX idx_explore_score_user_score_hearit
    ON explore_score (user_uuid, score DESC, hearit_id DESC);
