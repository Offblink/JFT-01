-- ============================================
-- 实验一数据库初始化脚本
-- 修复点：老师原稿最后一条 INSERT 以逗号结尾，会报语法错误
-- ============================================
CREATE DATABASE IF NOT EXISTS mybatis_db DEFAULT CHARACTER SET utf8mb4;
USE mybatis_db;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `user` (`username`, `password`, `email`) VALUES
('zhangsan', 'pass123', 'zhangsan@example.com'),
('lisi', 'pass456', 'lisi@example.com'),
('wangwu', 'pass789', 'wangwu@example.com'),
('zhaoliu', 'pass111', 'zhaoliu@example.com'),
('sunqi', 'pass222', 'sunqi@example.com');
