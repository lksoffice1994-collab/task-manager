--liquibase formatted sql

--changeset user:user_role_1
CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES user_entity(id),
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES role_entity(id)
);
