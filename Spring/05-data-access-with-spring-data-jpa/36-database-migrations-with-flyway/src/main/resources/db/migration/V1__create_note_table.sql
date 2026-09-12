-- Flyway migration naming: V<version>__<description>.sql (TWO underscores after the
-- version number). Flyway parses this filename to decide the version and the label
-- shown in flyway_schema_history - nothing about the name is arbitrary.
CREATE TABLE note (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(255) NOT NULL
);
