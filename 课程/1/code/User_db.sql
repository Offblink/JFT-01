--
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
--添加测试数据10条
INSERT INTO `user` (`username`, `password`, `email`) VALUES
('testuser1', 'password123', 'testuser1@example.com'),
('testuser2', 'password456', 'testuser2@example.com'),
('testuser3', 'password789', 'testuser3@example.com'),
('testuser4', 'password123', 'testuser4@example.com'),
('testuser5', 'password123', 'testuser5@example.com'),
('testuser6', 'password456', 'testuser6@example.com'),
('testuser7', 'password789', 'testuser7@example.com'),
('testuser8', 'password123', 'testuser8@example.com'),
('testuser9', 'password456', 'testuser9@example.com'),
('testuser10', 'password123', 'testuser10@example.com'),

