CREATE TABLE `reaction`
(
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `user_uuid`  BINARY(16) NOT NULL,
    `hearit_id`  BIGINT NOT NULL,
    `type`       VARCHAR(30) NOT NULL,
    `created_at` DATETIME(6) NOT NULL,

    PRIMARY KEY (`id`),

    CONSTRAINT `reaction_unique_constraint`
        UNIQUE (`user_uuid`, `hearit_id`, `type`),

    CONSTRAINT `fk_reaction_member`
        FOREIGN KEY (`user_uuid`)
            REFERENCES `member` (`uuid`),

    CONSTRAINT `fk_reaction_hearit`
        FOREIGN KEY (`hearit_id`)
            REFERENCES `hearit` (`id`)
);
