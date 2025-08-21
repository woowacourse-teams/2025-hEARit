ALTER TABLE member
    ADD CONSTRAINT social_member_unique_constraint
        UNIQUE (social_id, oauth_provider, deleted_at);

ALTER TABLE bookmark
    ADD CONSTRAINT bookmark_unique_constraint
        UNIQUE (member_id, hearit_id);
