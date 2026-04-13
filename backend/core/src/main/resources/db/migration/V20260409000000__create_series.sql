CREATE TABLE `series` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `title`       VARCHAR(50)  COLLATE utf8mb4_unicode_ci NOT NULL,
    `description` VARCHAR(500) COLLATE utf8mb4_unicode_ci,
    `image_url`   VARCHAR(500) COLLATE utf8mb4_unicode_ci,
    `created_at`  DATETIME(6)  NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE `hearit`
    ADD COLUMN `series_id` BIGINT DEFAULT NULL,
    ADD CONSTRAINT `fk_hearit_series` FOREIGN KEY (`series_id`) REFERENCES `series` (`id`);
