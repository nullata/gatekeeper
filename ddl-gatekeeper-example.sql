CREATE DATABASE IF NOT EXISTS gatekeeper;

USE gatekeeper;

DROP TABLE IF EXISTS `api_tokens`;

CREATE TABLE `api_tokens` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_tokens` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `api_tokens_unique` (`user_tokens`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

LOCK TABLES `api_tokens` WRITE;
INSERT INTO `api_tokens` VALUES ('NBDjgQwUe6TD0Nr3jlU646qBFvEwfcK6dUFsGA88sRk1kXVItT');
UNLOCK TABLES;

