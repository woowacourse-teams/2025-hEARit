CREATE TABLE `keyword` (
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                           PRIMARY KEY (`id`)
);

CREATE TABLE `member` (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `local_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
                          `social_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
                          `nickname` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                          `password` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
                          `profile_image` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
                          `oauth_provider` enum('GOOGLE','KAKAO','NONE') COLLATE utf8mb4_general_ci DEFAULT NULL,
                          `created_at` datetime(6) DEFAULT NULL,
                          `deleted_at` datetime(6) DEFAULT NULL,
                          PRIMARY KEY (`id`)
);

CREATE TABLE `category` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                            `color_code` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                            PRIMARY KEY (`id`)
);

CREATE TABLE `hearit` (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `category_id` bigint NOT NULL,
                          `title` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                          `summary` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
                          `original_audio_url` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                          `short_audio_url` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                          `script_url` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                          `play_time` int NOT NULL,
                          `created_at` datetime(6) NOT NULL,
                          PRIMARY KEY (`id`),
                          CONSTRAINT `fk_hearit_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
);

CREATE TABLE `admin` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `login_id` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                         `nickname` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                         `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                         PRIMARY KEY (`id`),
                         CONSTRAINT `uq_admin_login_id` UNIQUE (`login_id`)
);

CREATE TABLE `bookmark` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `member_id` bigint NOT NULL,
                            `hearit_id` bigint NOT NULL,
                            `created_at` datetime(6) DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            CONSTRAINT `uq_bookmark_member_hearit` UNIQUE (`member_id`, `hearit_id`),
                            CONSTRAINT `fk_bookmark_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
                            CONSTRAINT `fk_bookmark_hearit` FOREIGN KEY (`hearit_id`) REFERENCES `hearit` (`id`)
);

CREATE TABLE `explore_score` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `member_id` bigint DEFAULT NULL,
                                 `hearit_id` bigint NOT NULL,
                                 `score` double NOT NULL,
                                 `cursor_id` bigint DEFAULT NULL,
                                 PRIMARY KEY (`id`)
);

CREATE TABLE `hearit_keyword` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `hearit_id` bigint NOT NULL,
                                  `keyword_id` bigint NOT NULL,
                                  PRIMARY KEY (`id`),
                                  CONSTRAINT `uq_hearit_keyword` UNIQUE (`hearit_id`, `keyword_id`),
                                  CONSTRAINT `fk_hearit_keyword_hearit` FOREIGN KEY (`hearit_id`) REFERENCES `hearit` (`id`),
                                  CONSTRAINT `fk_hearit_keyword_keyword` FOREIGN KEY (`keyword_id`) REFERENCES `keyword` (`id`)
);

CREATE TABLE `hearit_source` (
                                 `hearit_id` bigint NOT NULL,
                                 `source_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
                                 `source_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
                                 KEY (`hearit_id`),
                                 CONSTRAINT `fk_hearit_source_hearit` FOREIGN KEY (`hearit_id`) REFERENCES `hearit` (`id`)
);

CREATE TABLE `recommend_hearit` (
                                    `id` bigint NOT NULL AUTO_INCREMENT,
                                    `hearit_id` bigint NOT NULL,
                                    `recommend_date` date NOT NULL,
                                    PRIMARY KEY (`id`),
                                    CONSTRAINT `uq_recommend_hearit_date` UNIQUE (`hearit_id`, `recommend_date`),
                                    CONSTRAINT `fk_recommend_hearit_hearit` FOREIGN KEY (`hearit_id`) REFERENCES `hearit` (`id`)
);

CREATE TABLE `refresh_token` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `member_id` bigint NOT NULL,
                                 `token` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                                 `expiry_date` datetime(6) NOT NULL,
                                 PRIMARY KEY (`id`),
                                 CONSTRAINT `uq_refresh_token_member_id` UNIQUE (`member_id`)
);
