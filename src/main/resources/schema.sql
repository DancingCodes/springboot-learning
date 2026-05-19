CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    email VARCHAR(100),
    age INT,
    create_time DATETIME,
    avatar VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    balance DECIMAL(10, 2),
    user_id BIGINT
);
