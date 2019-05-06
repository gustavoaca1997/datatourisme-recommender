CREATE TABLE IF NOT EXISTS class_properties (
    pid INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    uri VARCHAR(255) NOT NULL,
    preference DOUBLE NOT NULL DEFAULT 1.0,
    confidence DOUBLE NOT NULL DEFAULT 0.5,
    activation DOUBLE NOT NULL DEFAULT 0.0
)