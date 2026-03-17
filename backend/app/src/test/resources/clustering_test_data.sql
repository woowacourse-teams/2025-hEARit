-- 1. Steady Seller (초고지표: 5000~6000)
INSERT INTO hearit_cluster (hearit_id, view_count, like_count, bookmark_count, avg_play_time, completion_rate, created_at, cluster_id, updated_at)
WITH RECURSIVE seq AS (SELECT 1 AS x UNION ALL SELECT x + 1 FROM seq WHERE x < 30)
SELECT x, 5000 + (RAND() * 1000), 500, 200, 1000.0, 0.9 + (RAND() * 0.05), '2024-01-01 00:00:00', 0, NOW() FROM seq;

-- 2. Rising Star (신작: 1000~2000 + 최신성 극대화)
INSERT INTO hearit_cluster (hearit_id, view_count, like_count, bookmark_count, avg_play_time, completion_rate, created_at, cluster_id, updated_at)
WITH RECURSIVE seq AS (SELECT 31 AS x UNION ALL SELECT x + 1 FROM seq WHERE x < 60)
SELECT x, 1500 + (RAND() * 500), 100, 50, 500.0, 0.85 + (RAND() * 0.05), DATE_SUB(NOW(), INTERVAL 1 HOUR), 0, NOW() FROM seq;

-- 3. Hidden Gem (매우 낮은 조회수 + 매우 높은 완독률)
INSERT INTO hearit_cluster (hearit_id, view_count, like_count, bookmark_count, avg_play_time, completion_rate, created_at, cluster_id, updated_at)
WITH RECURSIVE seq AS (SELECT 61 AS x UNION ALL SELECT x + 1 FROM seq WHERE x < 90)
SELECT x, 10 + (RAND() * 20), 5, 2, 800.0, 0.98, '2025-06-01 00:00:00', 0, NOW() FROM seq;

-- 4. Old Archive (그냥 바닥 지표)
INSERT INTO hearit_cluster (hearit_id, view_count, like_count, bookmark_count, avg_play_time, completion_rate, created_at, cluster_id, updated_at)
WITH RECURSIVE seq AS (SELECT 91 AS x UNION ALL SELECT x + 1 FROM seq WHERE x < 120)
SELECT x, 1 + RAND(), 0, 0, 10.0, 0.01, '2020-01-01 00:00:00', 0, NOW() FROM seq;

-- 5. General (중간 지표)
INSERT INTO hearit_cluster (hearit_id, view_count, like_count, bookmark_count, avg_play_time, completion_rate, created_at, cluster_id, updated_at)
WITH RECURSIVE seq AS (SELECT 121 AS x UNION ALL SELECT x + 1 FROM seq WHERE x < 150)
SELECT x, 500 + (RAND() * 100), 50, 20, 200.0, 0.5, '2025-01-01 00:00:00', 0, NOW() FROM seq;
