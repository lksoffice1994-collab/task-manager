--liquibase formatted sql

--changeset user:role_entity_1
CREATE TABLE role_entity (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(255) NOT NULL UNIQUE
);

--changeset user:insert_roles
INSERT INTO role_entity (role_name) VALUES ('ROLE_ADMIN');
INSERT INTO role_entity (role_name) VALUES ('ROLE_USER_VALIDATED');
INSERT INTO role_entity (role_name) VALUES ('ROLE_USER_NON_VALIDATED');
