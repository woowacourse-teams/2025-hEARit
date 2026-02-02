-- AI 백오피스 처리 결과 테이블
CREATE TABLE `ai_process_result` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `status` VARCHAR(30) NOT NULL,

    -- 원본 파일 정보
    `original_file_name` VARCHAR(255) DEFAULT NULL,
    `original_file_key` VARCHAR(500) DEFAULT NULL,

    -- 생성된 파일들 (S3 temp 경로)
    `generated_org_key` VARCHAR(500) DEFAULT NULL,
    `generated_shr_key` VARCHAR(500) DEFAULT NULL,
    `generated_scr_key` VARCHAR(500) DEFAULT NULL,

    -- AI 생성 결과 (JSON)
    `raw_transcript` JSON DEFAULT NULL,
    `corrected_script` JSON DEFAULT NULL,
    `suggested_title` VARCHAR(35) DEFAULT NULL,
    `suggested_summary` TEXT DEFAULT NULL,

    -- 수정된 값 (사용자 입력)
    `edited_script` JSON DEFAULT NULL,
    `edited_title` VARCHAR(35) DEFAULT NULL,
    `edited_summary` TEXT DEFAULT NULL,

    -- 재생 시간 (초)
    `play_time` INT DEFAULT NULL,

    -- 에러 정보
    `error_message` TEXT DEFAULT NULL,

    -- 타임스탬프
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `completed_at` DATETIME DEFAULT NULL,
    `expires_at` DATETIME DEFAULT NULL,

    -- 확인 후 생성된 Hearit
    `confirmed_hearit_id` BIGINT DEFAULT NULL,

    PRIMARY KEY (`id`),

    -- 외래 키
    CONSTRAINT `fk_ai_result_hearit`
        FOREIGN KEY (`confirmed_hearit_id`) REFERENCES `hearit`(`id`) ON DELETE SET NULL,

    -- 인덱스
    INDEX `idx_ai_result_status` (`status`),
    INDEX `idx_ai_result_expires_at` (`expires_at`),
    INDEX `idx_ai_result_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
