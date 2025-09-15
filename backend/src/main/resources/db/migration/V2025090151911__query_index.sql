-- explore에서 hearit 조회
CREATE INDEX idx_hearit_created_at ON hearit(created_at DESC);

-- explore cursorId update 시 filter 조건 user_uuid
CREATE INDEX idx_explore_score_user_uuid ON explore_score(user_uuid);

-- explore user_uuid와 cursorId 기반 히어릿 조회
CREATE INDEX idx_explore_score_user_cursor ON explore_score (user_uuid, cursor_id);

-- 추천 카테고리 히어릿 5개 조회 & 카테고리 별 히어릿 조회
CREATE INDEX idx_hearit_category_created ON hearit(category_id, created_at DESC);

-- 추천 히어릿 목록 조회
CREATE INDEX idx_recommend_date_hearit ON recommend_hearit (recommend_date DESC, hearit_id);

-- 북마크된 히어릿 조회
CREATE INDEX idx_bookmark_member_created ON bookmark (member_id, created_at DESC);

-- ngram 기반 Fulltext 검색
ALTER TABLE hearit
    ADD FULLTEXT INDEX ft_title (title) WITH PARSER ngram;

ALTER TABLE keyword
    ADD FULLTEXT INDEX ft_name (name) WITH PARSER ngram;
