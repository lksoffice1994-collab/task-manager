--liquibase formatted sql

--changeset lokesh:user_entity_1
CREATE TABLE user_entity (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    contact_number VARCHAR(255),
    password VARCHAR(255),
    address VARCHAR(255),
    user_type VARCHAR(50) NOT NULL
);

--changeset lokesh:user_entity_add_flags
ALTER TABLE user_entity ADD COLUMN is_otp_validated BOOLEAN DEFAULT FALSE;
ALTER TABLE user_entity ADD COLUMN is_active BOOLEAN DEFAULT FALSE;

--changeset user:user_entity_add_otp
ALTER TABLE user_entity ADD COLUMN otp VARCHAR(20);