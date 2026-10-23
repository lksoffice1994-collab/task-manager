--liquibase formatted sql

--changeset codex:user-profile-photo-1
CREATE TABLE user_profile_photo (
    user_id VARCHAR(255) PRIMARY KEY REFERENCES user_entity(user_id) ON DELETE CASCADE,
    image_path VARCHAR(2048) NOT NULL
);
