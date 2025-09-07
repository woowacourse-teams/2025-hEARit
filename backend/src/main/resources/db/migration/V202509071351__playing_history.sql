CREATE TABLE `playing_history` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `hearit_id` BIGINT NOT NULL,
    `last_play_time` INT NOT NULL,
    `is_finished` BOOLEAN NOT NULL,
    `updated_at` DATETIME NOT NULL,
     PRIMARY KEY (`id`),
     CONSTRAINT `uq_playing_history_member_hearit` UNIQUE (`member_id`, `hearit_id`)
);
