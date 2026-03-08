CREATE TABLE hearit_cluster (
    hearit_id       BIGINT         NOT NULL,
    view_count      BIGINT         NOT NULL,
    like_count      BIGINT         NOT NULL,
    bookmark_count  BIGINT         NOT NULL,
    avg_play_time   DOUBLE         NOT NULL,
    completion_rate DOUBLE         NOT NULL,
    created_at      DATETIME(6)    NOT NULL,
    cluster_id      INT            NOT NULL,
    updated_at      DATETIME(6)    NOT NULL,

    PRIMARY KEY (hearit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 군집 기반 후보군 추출 시 성능 최적화를 위한 인덱스
CREATE INDEX idx_hearit_cluster_cluster_id ON hearit_cluster (cluster_id);
