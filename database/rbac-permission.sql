-- RBAC 权限管理模块 - 数据库脚本
-- 数据库：it_asset_manage
-- 创建时间：2026-03-21

USE `it_asset_manage`;

-- 1. 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `role_id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 2. 权限表
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `permission_id` BIGINT NOT NULL AUTO_INCREMENT,
  `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
  `permission_type` TINYINT DEFAULT 1 COMMENT '类型：1-菜单 2-按钮/操作',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父权限 ID',
  `path` VARCHAR(200) DEFAULT NULL COMMENT '路径',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`permission_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 3. 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `role_id` BIGINT NOT NULL COMMENT '角色 ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 4. 角色权限关联表
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL COMMENT '角色 ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限 ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 5. 操作日志表
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL COMMENT '用户 ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
  `module` VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
  `operation_type` VARCHAR(50) DEFAULT NULL COMMENT '操作类型',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
  `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法',
  `request_url` VARCHAR(200) DEFAULT NULL COMMENT '请求 URL',
  `request_params` TEXT COMMENT '请求参数',
  `response_result` TEXT COMMENT '响应结果',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP 地址',
  `operation_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `execute_time` BIGINT DEFAULT NULL COMMENT '执行时长 (ms)',
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 6. 插入默认角色
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`, `status`) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有系统所有权限', 1),
('管理员', 'ADMIN', '拥有系统管理权限', 1),
('普通用户', 'USER', '普通用户，拥有基本操作权限', 1),
('审计人员', 'AUDITOR', '拥有查看和审计权限', 1);

-- 7. 插入默认权限（菜单）
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `path`, `icon`, `sort_order`) VALUES
('系统管理', 'system', 1, 0, '/system', 'Setting', 100),
('用户管理', 'system:user', 1, 1, '/user-manage', 'User', 101),
('角色管理', 'system:role', 1, 1, '/role-manage', 'Role', 102),
('权限管理', 'system:permission', 1, 1, '/permission-manage', 'Lock', 103),
('操作日志', 'system:log', 1, 1, '/operation-log', 'Document', 104),
('资产管理', 'asset', 1, 0, '/asset', 'Document', 1),
('资产列表', 'asset:list', 1, 6, '/asset-manage', 'Document', 10),
('资产入库', 'asset:inbound', 1, 6, '/asset-inbound', 'FolderAdd', 11),
('资产领用', 'asset:use', 1, 6, '/asset-use', 'Download', 12),
('资产归还', 'asset:return', 1, 6, '/asset-return', 'Refresh', 13),
('资产维修', 'asset:repair', 1, 6, '/asset-repair', 'Tools', 14),
('资产报废', 'asset:scrap', 1, 6, '/asset-scrap', 'Delete', 15),
('资产盘点', 'asset:inventory', 1, 6, '/asset-inventory', 'DataAnalysis', 16);

-- 8. 插入默认权限（按钮/操作）
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`) VALUES
('创建用户', 'system:user:create', 2, 2, 1),
('修改用户', 'system:user:update', 2, 2, 2),
('删除用户', 'system:user:delete', 2, 2, 3),
('重置密码', 'system:user:reset', 2, 2, 4),
('创建角色', 'system:role:create', 2, 3, 1),
('修改角色', 'system:role:update', 2, 3, 2),
('删除角色', 'system:role:delete', 2, 3, 3),
('分配权限', 'system:role:assign', 2, 3, 4),
('资产新增', 'asset:add', 2, 7, 1),
('资产修改', 'asset:edit', 2, 7, 2),
('资产删除', 'asset:delete', 2, 7, 3),
('资产审批', 'asset:approve', 2, 7, 4);

-- 9. 为超级管理员分配所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, permission_id FROM sys_permission;

-- 10. 为管理员分配大部分权限（排除审计相关）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 2, permission_id FROM sys_permission WHERE permission_code NOT LIKE 'system:log%';

-- 11. 为普通用户分配基本权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 3, permission_id FROM sys_permission WHERE permission_code LIKE 'asset:%' AND permission_type = 1;

-- 12. 为审计人员分配查看权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 4, permission_id FROM sys_permission WHERE permission_code LIKE '%:list%' OR permission_code LIKE 'system:log%';

-- 13. 为 admin 用户分配超级管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT user_id, 1 FROM sys_user WHERE username = 'admin';

-- 查询验证
SELECT '角色列表' as type;
SELECT * FROM sys_role;

SELECT '权限列表' as type;
SELECT * FROM sys_permission ORDER BY parent_id, sort_order;

SELECT '用户角色关联' as type;
SELECT u.username, r.role_name FROM sys_user u
JOIN sys_user_role ur ON u.user_id = ur.user_id
JOIN sys_role r ON ur.role_id = r.role_id;
