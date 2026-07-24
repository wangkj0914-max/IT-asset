USE `it_asset_manage`;

-- 1. 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `role_id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_name` VARCHAR(50) NOT NULL,
  `role_code` VARCHAR(50) NOT NULL,
  `description` VARCHAR(200) DEFAULT NULL,
  `status` TINYINT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `permission_id` BIGINT NOT NULL AUTO_INCREMENT,
  `permission_name` VARCHAR(50) NOT NULL,
  `permission_code` VARCHAR(100) NOT NULL,
  `permission_type` TINYINT DEFAULT 1,
  `parent_id` BIGINT DEFAULT 0,
  `path` VARCHAR(200) DEFAULT NULL,
  `icon` VARCHAR(50) DEFAULT NULL,
  `sort_order` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 操作日志表
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL,
  `username` VARCHAR(50) DEFAULT NULL,
  `module` VARCHAR(50) DEFAULT NULL,
  `operation_type` VARCHAR(50) DEFAULT NULL,
  `description` VARCHAR(500) DEFAULT NULL,
  `request_method` VARCHAR(10) DEFAULT NULL,
  `request_url` VARCHAR(200) DEFAULT NULL,
  `request_params` TEXT,
  `response_result` TEXT,
  `ip_address` VARCHAR(50) DEFAULT NULL,
  `operation_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `execute_time` BIGINT DEFAULT NULL,
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入默认角色
INSERT IGNORE INTO `sys_role` (`role_id`, `role_name`, `role_code`, `description`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '拥有系统所有权限'),
(2, '管理员', 'ADMIN', '拥有系统管理权限'),
(3, '普通用户', 'USER', '普通用户'),
(4, '审计人员', 'AUDITOR', '审计权限');
