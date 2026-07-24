-- IT 固定资产管理系统数据库初始化脚本
-- 数据库：it_asset_manage

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `it_asset_manage`;
USE `it_asset_manage`;

-- ===========================
-- 1. 用户表
-- ===========================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（加密）',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `department` VARCHAR(100) DEFAULT NULL COMMENT '部门',
  `role` TINYINT DEFAULT 1 COMMENT '角色：1-普通用户 2-管理员',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认管理员账号（密码：123456）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `status`) 
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 2, 1);

-- ===========================
-- 2. 资产分类表
-- ===========================
DROP TABLE IF EXISTS `asset_category`;
CREATE TABLE `asset_category` (
  `category_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类 ID',
  `category_name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父分类 ID（0 为一级分类）',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产分类表';

-- 插入默认分类
INSERT INTO `asset_category` (`category_name`, `parent_id`, `sort_order`) VALUES
('服务器', 0, 1),
('计算机设备', 0, 2),
('台式机', 2, 1),
('笔记本电脑', 2, 2),
('网络设备', 0, 3),
('打印机/复印机', 0, 4),
('办公设备', 0, 5),
('其他设备', 0, 99);

-- ===========================
-- 3. 资产信息表
-- ===========================
DROP TABLE IF EXISTS `asset_info`;
CREATE TABLE `asset_info` (
  `asset_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '资产 ID',
  `asset_code` VARCHAR(50) DEFAULT NULL COMMENT '资产编号（唯一）',
  `asset_name` VARCHAR(200) NOT NULL COMMENT '资产名称',
  `category_id` BIGINT NOT NULL COMMENT '分类 ID',
  `brand` VARCHAR(100) DEFAULT NULL COMMENT '品牌',
  `model` VARCHAR(100) DEFAULT NULL COMMENT '型号',
  `serial_number` VARCHAR(100) DEFAULT NULL COMMENT '序列号',
  `purchase_price` DECIMAL(10,2) DEFAULT NULL COMMENT '采购价格',
  `purchase_date` DATE DEFAULT NULL COMMENT '采购日期',
  `supplier` VARCHAR(200) DEFAULT NULL COMMENT '供应商',
  `storage_location` VARCHAR(200) DEFAULT NULL COMMENT '存放位置',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-未领用 1-已领用 2-维修中 3-已报废',
  `user_id` BIGINT DEFAULT NULL COMMENT '当前使用人 ID',
  `remark` TEXT COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`asset_id`),
  UNIQUE KEY `uk_asset_code` (`asset_code`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产信息表';

-- ===========================
-- 4. 资产领用记录表
-- ===========================
DROP TABLE IF EXISTS `asset_use_record`;
CREATE TABLE `asset_use_record` (
  `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录 ID',
  `asset_id` BIGINT NOT NULL COMMENT '资产 ID',
  `user_id` BIGINT NOT NULL COMMENT '使用人 ID',
  `use_type` TINYINT DEFAULT 1 COMMENT '类型：1-领用 2-归还 3-调拨',
  `use_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领用时间',
  `return_date` DATETIME DEFAULT NULL COMMENT '归还时间',
  `remark` TEXT COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`record_id`),
  KEY `idx_asset` (`asset_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产领用记录表';

-- ===========================
-- 5. 资产维修记录表
-- ===========================
DROP TABLE IF EXISTS `asset_repair_record`;
CREATE TABLE `asset_repair_record` (
  `repair_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '维修 ID',
  `asset_id` BIGINT NOT NULL COMMENT '资产 ID',
  `repair_reason` VARCHAR(500) NOT NULL COMMENT '维修原因',
  `repair_cost` DECIMAL(10,2) DEFAULT NULL COMMENT '维修费用',
  `repair_status` TINYINT DEFAULT 0 COMMENT '状态：0-待维修 1-维修中 2-已完成',
  `repair_date` DATETIME DEFAULT NULL COMMENT '维修完成时间',
  `remark` TEXT COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`repair_id`),
  KEY `idx_asset` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产维修记录表';

-- ===========================
-- 6. 资产报废记录表
-- ===========================
DROP TABLE IF EXISTS `asset_scrap_record`;
CREATE TABLE `asset_scrap_record` (
  `scrap_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报废 ID',
  `asset_id` BIGINT NOT NULL COMMENT '资产 ID',
  `scrap_reason` VARCHAR(500) NOT NULL COMMENT '报废原因',
  `scrap_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报废时间',
  `approve_user` VARCHAR(50) DEFAULT NULL COMMENT '审批人',
  `remark` TEXT COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`scrap_id`),
  KEY `idx_asset` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产报废记录表';

-- ===========================
-- 插入测试数据
-- ===========================
INSERT INTO `asset_info` (`asset_code`, `asset_name`, `category_id`, `brand`, `model`, `serial_number`, `purchase_price`, `purchase_date`, `storage_location`, `status`) VALUES
('ZC20240001', '戴尔 PowerEdge R750 服务器', 1, '戴尔', 'R750', 'DELL-R750-001', 25000.00, '2024-01-15', '一号机房 A 区', 1),
('ZC20240002', '联想 ThinkCentre 台式机', 3, '联想', 'M730s', 'LEN-M730-002', 4500.00, '2024-02-10', '财务部', 1),
('ZC20240003', 'MacBook Pro 14 寸', 4, '苹果', 'MKGP3', 'APPLE-MBP-003', 14999.00, '2024-03-01', '研发部', 0),
('ZC20240004', '华为 S5735 交换机', 5, '华为', 'S5735-L24P4X-A', 'HW-SW-004', 3200.00, '2024-01-20', '网络机房', 0),
('ZC20240005', '惠普 LaserJet 打印机', 6, '惠普', 'M437n', 'HP-LJ-005', 2800.00, '2024-02-25', '行政部', 0);

-- 插入领用记录
INSERT INTO `asset_use_record` (`asset_id`, `user_id`, `use_type`, `remark`) VALUES
(1, 1, 1, '研发部服务器领用'),
(2, 1, 1, '财务部台式机领用');
