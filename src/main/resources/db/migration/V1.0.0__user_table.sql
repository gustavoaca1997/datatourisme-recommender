CREATE TABLE IF NOT EXISTS user (
    uid int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(55) UNIQUE KEY
)