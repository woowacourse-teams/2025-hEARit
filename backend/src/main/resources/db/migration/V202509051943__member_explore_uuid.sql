-- member 테이블에 uuid 컬럼 문자열 32자 추가
ALTER TABLE member
    ADD COLUMN uuid CHAR(32);

-- explore_score 테이블의 기존 데이터 삭제
TRUNCATE TABLE explore_score;

-- explore_score 테이블에 member_id 컬럼 삭제
ALTER TABLE explore_score
    DROP COLUMN member_id;

-- explore_score 테이블에 uuid 컬럼 문자열 32자 추가
ALTER TABLE explore_score
    ADD COLUMN user_uuid CHAR(32);
