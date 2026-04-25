CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(32) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(128),
    avatar_url VARCHAR(500) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TINYINT DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_username ON users(username);
CREATE UNIQUE INDEX IF NOT EXISTS uk_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_status_created ON users(status, created_at);
