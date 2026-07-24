-- ==========================================
-- IT 固定资产管理系统 v2 安全加固迁移脚本
-- 日期：2026-07-14
-- ==========================================

USE `it_asset_manage`;

-- 1. sys_user 表新增密码哈希类型字段（兼容旧 MD5 密码）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'it_asset_manage' AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'password_hash_type');

SET @sql = IF(@col_exists = 0, 'ALTER TABLE sys_user ADD COLUMN password_hash_type VARCHAR(20) DEFAULT ''MD5'' COMMENT ''密码哈希类型：MD5/BCRYPT''', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 创建角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `role_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色 ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 创建权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `permission_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限 ID',
  `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父权限 ID',
  `type` TINYINT DEFAULT 1 COMMENT '类型：1-菜单 2-按钮 3-接口',
  `url` VARCHAR(200) DEFAULT NULL COMMENT '资源路径',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`permission_id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 4. 创建用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `role_id` BIGINT NOT NULL COMMENT '角色 ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 5. 创建角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL COMMENT '角色 ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限 ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 6. 预置角色
INSERT IGNORE INTO `sys_role` (`role_name`, `role_code`, `description`) VALUES
('普通用户', 'USER', '普通用户，拥有基本查看和操作权限'),
('管理员', 'ADMIN', '系统管理员，拥有所有权限');

-- 7. 预置权限
INSERT IGNORE INTO `sys_permission` (`permission_name`, `permission_code`, `type`, `url`, `sort_order`) VALUES
('资产管理', 'asset:manage', 1, '/asset-manage', 1),
('资产入库', 'asset:inbound', 1, '/asset-inbound', 2),
('资产领用', 'asset:use', 1, '/asset-use', 3),
('资产归还', 'asset:return', 1, '/asset-return', 4),
('资产维修', 'asset:repair', 1, '/asset-repair', 5),
('资产报废', 'asset:scrap', 1, '/asset-scrap', 6),
('资产盘点', 'asset:inventory', 1, '/asset-inventory', 7),
('分类管理', 'category:manage', 1, '/category-manage', 8),
('用户管理', 'user:manage', 1, '/user-manage', 9),
('系统管理', 'system:manage', 1, '/system', 10);

-- 8. 为 admin 用户分配管理员角色
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT 1, role_id FROM `sys_role` WHERE role_code = 'ADMIN';
