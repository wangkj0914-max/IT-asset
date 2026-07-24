-- ============================================
-- 资产维修、报废、盘点模块 - 数据库脚本
-- 数据库：it_asset_manage
-- 创建时间：2026-03-21
-- ============================================

USE `it_asset_manage`;

-- ============================================
-- 1. 资产维修记录表
-- ============================================
DROP TABLE IF EXISTS `asset_repair_record`;
CREATE TABLE `asset_repair_record` (
  `repair_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '维修 ID',
  `asset_id` BIGINT NOT NULL COMMENT '资产 ID',
  `apply_user_id` BIGINT DEFAULT NULL COMMENT '报修人 ID',
  `apply_user_name` VARCHAR(50) DEFAULT NULL COMMENT '报修人姓名',
  `apply_department` VARCHAR(100) DEFAULT NULL COMMENT '报修部门',
  `repair_reason` VARCHAR(500) NOT NULL COMMENT '维修原因',
  `repair_cost` DECIMAL(10,2) DEFAULT NULL COMMENT '维修费用',
  `repair_status` TINYINT DEFAULT 0 COMMENT '状态：0-待维修 1-维修中 2-已完成',
  `repair_date` DATETIME DEFAULT NULL COMMENT '维修完成时间',
  `repair_man` VARCHAR(50) DEFAULT NULL COMMENT '维修人员',
  `remark` TEXT COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`repair_id`),
  KEY `idx_asset_id` (`asset_id`),
  KEY `idx_status` (`repair_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产维修记录表';

-- ============================================
-- 2. 资产报废记录表
-- ============================================
DROP TABLE IF EXISTS `asset_scrap_record`;
CREATE TABLE `asset_scrap_record` (
  `scrap_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报废 ID',
  `asset_id` BIGINT NOT NULL COMMENT '资产 ID',
  `apply_user_id` BIGINT DEFAULT NULL COMMENT '申请人 ID',
  `apply_user_name` VARCHAR(50) DEFAULT NULL COMMENT '申请人姓名',
  `apply_department` VARCHAR(100) DEFAULT NULL COMMENT '申请部门',
  `scrap_reason` VARCHAR(500) NOT NULL COMMENT '报废原因',
  `scrap_type` TINYINT DEFAULT 0 COMMENT '报废类型：0-正常 1-损坏 2-丢失',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原值',
  `residual_value` DECIMAL(10,2) DEFAULT NULL COMMENT '残值',
  `approve_status` TINYINT DEFAULT 0 COMMENT '审批状态：0-待审批 1-已通过 2-已拒绝',
  `approve_user` VARCHAR(50) DEFAULT NULL COMMENT '审批人',
  `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `remark` TEXT COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`scrap_id`),
  KEY `idx_asset_id` (`asset_id`),
  KEY `idx_status` (`approve_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产报废记录表';

-- ============================================
-- 3. 资产盘点任务表
-- ============================================
DROP TABLE IF EXISTS `asset_inventory`;
CREATE TABLE `asset_inventory` (
  `inventory_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '盘点 ID',
  `inventory_no` VARCHAR(50) NOT NULL COMMENT '盘点单号',
  `inventory_name` VARCHAR(100) NOT NULL COMMENT '盘点名称',
  `inventory_range` TINYINT DEFAULT 0 COMMENT '盘点范围：0-全部 1-部门 2-分类 3-指定',
  `range_value` VARCHAR(500) DEFAULT NULL COMMENT '盘点范围值',
  `inventory_date` DATETIME DEFAULT NULL COMMENT '盘点日期',
  `operator_id` BIGINT DEFAULT NULL COMMENT '盘点人 ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '盘点人姓名',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待盘点 1-盘点中 2-已完成',
  `surplus_count` INT DEFAULT 0 COMMENT '盘盈数量',
  `loss_count` INT DEFAULT 0 COMMENT '盘亏数量',
  `remark` TEXT COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`inventory_id`),
  UNIQUE KEY `uk_inventory_no` (`inventory_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产盘点任务表';

-- ============================================
-- 4. 资产盘点明细表
-- ============================================
DROP TABLE IF EXISTS `asset_inventory_detail`;
CREATE TABLE `asset_inventory_detail` (
  `detail_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细 ID',
  `inventory_id` BIGINT NOT NULL COMMENT '盘点 ID',
  `asset_id` BIGINT NOT NULL COMMENT '资产 ID',
  `asset_code` VARCHAR(50) DEFAULT NULL COMMENT '资产编号',
  `asset_name` VARCHAR(200) DEFAULT NULL COMMENT '资产名称',
  `category_name` VARCHAR(100) DEFAULT NULL COMMENT '资产分类',
  `storage_location` VARCHAR(200) DEFAULT NULL COMMENT '存放位置',
  `department` VARCHAR(100) DEFAULT NULL COMMENT '使用部门',
  `user_name` VARCHAR(50) DEFAULT NULL COMMENT '使用人',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待盘点 1-正常 2-盘盈 3-盘亏',
  `result_remark` VARCHAR(500) DEFAULT NULL COMMENT '盘点结果说明',
  `check_time` DATETIME DEFAULT NULL COMMENT '盘点时间',
  `checker_name` VARCHAR(50) DEFAULT NULL COMMENT '盘点人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`detail_id`),
  KEY `idx_inventory_id` (`inventory_id`),
  KEY `idx_asset_id` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产盘点明细表';

-- ============================================
-- 5. 插入测试数据
-- ============================================

-- 维修记录测试数据
INSERT INTO `asset_repair_record` (`asset_id`, `apply_user_id`, `apply_user_name`, `apply_department`, `repair_reason`, `repair_status`, `remark`) VALUES
(1, 1, '系统管理员', '研发部', '服务器风扇故障，噪音大', 2, '已更换风扇'),
(2, 1, '系统管理员', '财务部', '台式机无法开机', 1, '正在检测电源'),
(5, 1, '系统管理员', '行政部', '打印机卡纸', 0, '待维修');

-- 报废记录测试数据
INSERT INTO `asset_scrap_record` (`asset_id`, `apply_user_id`, `apply_user_name`, `apply_department`, `scrap_reason`, `scrap_type`, `original_price`, `residual_value`, `approve_status`, `remark`) VALUES
(4, 1, '系统管理员', '研发部', '设备老化，无法修复', 0, 3200.00, 100.00, 1, '已批准报废'),
(3, 1, '系统管理员', '研发部', '笔记本屏幕损坏', 1, 14999.00, 500.00, 0, '待审批');

-- 盘点任务测试数据
INSERT INTO `asset_inventory` (`inventory_no`, `inventory_name`, `inventory_range`, `inventory_date`, `operator_id`, `operator_name`, `status`, `remark`) VALUES
('PD202603210001', '2026 年第一季度盘点', 0, '2026-03-21 10:00:00', 1, '系统管理员', 2, '已完成'),
('PD202603210002', '研发部专项盘点', 1, '2026-03-21 14:00:00', 1, '系统管理员', 0, '待盘点');

-- 盘点明细测试数据
INSERT INTO `asset_inventory_detail` (`inventory_id`, `asset_id`, `asset_code`, `asset_name`, `department`, `user_name`, `status`, `remark`) VALUES
(1, 1, 'ZC20240001', '戴尔 PowerEdge R750 服务器', '研发部', '赵研发', 1, '正常'),
(1, 2, 'ZC20240002', '联想 ThinkCentre 台式机', '财务部', '王财务', 1, '正常'),
(2, 1, 'ZC20240001', '戴尔 PowerEdge R750 服务器', '研发部', '赵研发', 0, '待盘点');

-- ============================================
-- 6. 查询视图
-- ============================================

-- 维修统计视图
DROP VIEW IF EXISTS `v_repair_stats`;
CREATE VIEW `v_repair_stats` AS
SELECT 
    COUNT(*) AS total_count,
    SUM(CASE WHEN repair_status = 0 THEN 1 ELSE 0 END) AS pending_count,
    SUM(CASE WHEN repair_status = 1 THEN 1 ELSE 0 END) AS processing_count,
    SUM(CASE WHEN repair_status = 2 THEN 1 ELSE 0 END) AS completed_count,
    SUM(repair_cost) AS total_cost
FROM asset_repair_record;

-- 报废统计视图
DROP VIEW IF EXISTS `v_scrap_stats`;
CREATE VIEW `v_scrap_stats` AS
SELECT 
    COUNT(*) AS total_count,
    SUM(CASE WHEN approve_status = 0 THEN 1 ELSE 0 END) AS pending_count,
    SUM(CASE WHEN approve_status = 1 THEN 1 ELSE 0 END) AS approved_count,
    SUM(CASE WHEN approve_status = 2 THEN 1 ELSE 0 END) AS rejected_count,
    SUM(original_price) AS total_original_price,
    SUM(residual_value) AS total_residual_value
FROM asset_scrap_record;

-- 盘点统计视图
DROP VIEW IF EXISTS `v_inventory_stats`;
CREATE VIEW `v_inventory_stats` AS
SELECT 
    COUNT(*) AS total_count,
    SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS pending_count,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS processing_count,
    SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS completed_count,
    SUM(surplus_count) AS total_surplus,
    SUM(loss_count) AS total_loss
FROM asset_inventory;

-- ============================================
-- 查询测试
-- ============================================

-- 查询维修记录
SELECT * FROM asset_repair_record LIMIT 5;

-- 查询报废记录
SELECT * FROM asset_scrap_record LIMIT 5;

-- 查询盘点任务
SELECT * FROM asset_inventory LIMIT 5;

-- 查询盘点明细
SELECT * FROM asset_inventory_detail LIMIT 10;

-- 查询统计视图
SELECT * FROM v_repair_stats;
SELECT * FROM v_scrap_stats;
SELECT * FROM v_inventory_stats;
