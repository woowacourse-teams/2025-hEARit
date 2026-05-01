ALTER TABLE `hearit`
    DROP FOREIGN KEY `fk_hearit_series`,
    DROP COLUMN `series_id`;

CREATE TABLE `hearit_series` (
    `id`         BIGINT NOT NULL AUTO_INCREMENT,
    `hearit_id`  BIGINT NOT NULL,
    `series_id`  BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hearit_series` (`hearit_id`, `series_id`),
    CONSTRAINT `fk_hearit_series_hearit` FOREIGN KEY (`hearit_id`) REFERENCES `hearit` (`id`),
    CONSTRAINT `fk_hearit_series_series` FOREIGN KEY (`series_id`) REFERENCES `series` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
