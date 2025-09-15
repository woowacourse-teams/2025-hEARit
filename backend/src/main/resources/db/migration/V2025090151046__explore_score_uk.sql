-- explore_score 테이블의 기존 데이터 삭제
TRUNCATE TABLE explore_score;

-- explore_score user_uuid, hearit_id uk 추가
CREATE UNIQUE INDEX ux_user_uuid_hearit_id ON explore_score (user_uuid, hearit_id);
