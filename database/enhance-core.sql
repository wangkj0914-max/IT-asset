-- 固定资产系统 - 核心业务流程扩展脚本
-- 执行时间：2026-03-14

USE `it_asset_manage`;

-- ===========================
-- 1. 扩展领用记录表
-- ===========================
ALTER TABLE `asset_use_record` 
ADD COLUMN `department` VARCHAR(100) DEFAULT NULL COMMENT '领用部门' AFTER `user_id`,
ADD COLUMN `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人' AFTER `department`,
ADD COLUMN `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话' AFTER `contact_person`,
ADD COLUMN `approve_user` VARCHAR(50) DEFAULT NULL COMMENT '审批人' AFTER `return_date`,
ADD COLUMN `approve_status` TINYINT DEFAULT 0 COMMENT '审批状态：0-待审批 1-已通过 2-已拒绝' AFTER `approve_user`,
ADD COLUMN `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间' AFTER `approve_status`;

-- ===========================
-- 2. 完善维修记录表
-- ===========================
ALTER TABLE `asset_repair_record` 
ADD COLUMN `repair_fee` DECIMAL(10,2) DEFAULT NULL COMMENT '维修费用' AFTER `repair_cost`,
ADD COLUMN `repair_company` VARCHAR(200) DEFAULT NULL COMMENT '维修单位' AFTER `repair_fee`,
ADD COLUMN `repair_contact` VARCHAR(50) DEFAULT NULL COMMENT '维修联系人' AFTER `repair_company`,
ADD COLUMN `repair_phone` VARCHAR(20) DEFAULT NULL COMMENT '维修电话' AFTER `repair_contact`,
ADD COLUMN `apply_user` VARCHAR(50) DEFAULT NULL COMMENT '报修人' AFTER `repair_reason`,
ADD COLUMN `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报修时间' AFTER `apply_user`;

-- ===========================
-- 3. 完善报废记录表
-- ===========================
ALTER TABLE `asset_scrap_record` 
ADD COLUMN `scrap_type` TINYINT DEFAULT 0 COMMENT '报废类型：0-正常报废 1-损坏报废 2-丢失报废' AFTER `scrap_reason`,
ADD COLUMN `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原值' AFTER `scrap_date`,
ADD COLUMN `residual_value` DECIMAL(10,2) DEFAULT NULL COMMENT '残值' AFTER `original_price`,
ADD COLUMN `approve_status` TINYINT DEFAULT 0 COMMENT '审批状态：0-待审批 1-已通过 2-已拒绝' AFTER `approve_user`,
ADD COLUMN `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间' AFTER `approve_status`;

-- ===========================
-- 4. 创建资产变更日志表
-- ===========================
DROP TABLE IF EXISTS `asset_change_log`;
CREATE TABLE `asset_change_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志 ID',
  `asset_id` BIGINT NOT NULL COMMENT '资产 ID',
  `change_type` TINYINT NOT NULL COMMENT '变更类型：1-领用 2-归还 3-维修 4-报废 5-调拨',
  `before_value` TEXT COMMENT '变更前值',
  `after_value` TEXT COMMENT '变更后值',
  `operator` VARCHAR(50) DEFAULT NULL COMMENT '操作人',
  `operate_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `remark` TEXT COMMENT '备注',
  PRIMARY KEY (`log_id`),
  KEY `idx_asset` (`asset_id`),
  KEY `idx_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产变更日志表';

-- ===========================
-- 5. 创建部门表（为领用功能服务）
-- ===========================
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department` (
  `dept_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门 ID',
  `dept_name` VARCHAR(100) NOT NULL COMMENT '部门名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父部门 ID',
  `dept_code` VARCHAR(50) DEFAULT NULL COMMENT '部门编码',
  `manager` VARCHAR(50) DEFAULT NULL COMMENT '负责人',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`dept_id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 插入默认部门
INSERT INTO `sys_department` (`dept_name`, `dept_code`, `manager`, `phone`) VALUES
('总经办', 'ZJB', '张总', '13800138001'),
('行政部', 'XZB', '李行政', '13800138002'),
('财务部', 'CWB', '王财务', '13800138003'),
('研发部', 'YFB', '赵研发', '13800138004'),
('市场部', 'SCB', '孙市场', '13800138005'),
('人力资源部', 'RLB', '周人事', '13800138006');

-- ===========================
-- 6. 更新 sys_user 表
-- ===========================
ALTER TABLE `sys_user` 
ADD COLUMN `dept_id` BIGINT DEFAULT NULL COMMENT '部门 ID' AFTER `department`,
ADD COLUMN `position` VARCHAR(50) DEFAULT NULL COMMENT '职位' AFTER `dept_id`;

-- ===========================
-- 7. 更新测试数据
-- ===========================
-- 更新用户部门
UPDATE `sys_user` SET `dept_id` = 1, `position` = '系统管理员' WHERE `username` = 'admin';

-- 插入领用记录测试数据
INSERT INTO `asset_use_record` (`asset_id`, `user_id`, `department`, `contact_person`, `contact_phone`, `use_type`, `approve_status`, `approve_user`, `remark`) VALUES
(1, 1, '研发部', '赵研发', '13800138004', 1, 1, 'admin', '研发部服务器领用'),
(2, 1, '财务部', '王财务', '13800138003', 1, 1, 'admin', '财务部台式机领用');

-- 插入维修记录测试数据
INSERT INTO `asset_repair_record` (`asset_id`, `repair_reason`, `repair_status`, `apply_user`, `remark`) VALUES
(5, '打印机卡纸故障', 2, 'admin', '已更换硒鼓');

-- 插入报废记录测试数据
INSERT INTO `asset_scrap_record` (`asset_id`, `scrap_reason`, `scrap_type`, `approve_user`, `approve_status`, `remark`) VALUES
(4, '设备老化，无法修复', 0, 'admin', 1, '已批准报废');

-- ===========================
-- 8. 创建视图：资产全生命周期视图
-- ===========================
DROP VIEW IF EXISTS `v_asset_lifecycle`;
CREATE VIEW `v_asset_lifecycle` AS
SELECT 
    ai.asset_id,
    ai.asset_code,
    ai.asset_name,
    ac.category_name,
    ai.status,
    CASE ai.status 
        WHEN 0 THEN '未领用'
        WHEN 1 THEN '已领用'
        WHEN 2 THEN '维修中'
        WHEN 3 THEN '已报废'
    END AS status_name,
    ai.storage_location,
    aur.department AS use_department,
    aur.contact_person,
    arr.repair_status,
    arr.repair_reason,
    asr.scrap_reason,
    asr.approve_status AS scrap_approve_status
FROM asset_info ai
LEFT JOIN asset_category ac ON ai.category_id = ac.category_id
LEFT JOIN (
    SELECT asset_id, department, contact_person, use_type 
    FROM asset_use_record 
    WHERE (asset_id, record_id) IN (
        SELECT asset_id, MAX(record_id) 
        FROM asset_use_record 
        WHERE use_type IN (1, 2) 
        GROUP BY asset_id
    )
) aur ON ai.asset_id = aur.asset_id AND aur.use_type = 1
LEFT JOIN (
    SELECT asset_id, repair_status, repair_reason 
    FROM asset_repair_record 
    WHERE (asset_id, repair_id) IN (
        SELECT asset_id, MAX(repair_id) 
        FROM asset_repair_record 
        GROUP BY asset_id
    )
) arr ON ai.asset_id = arr.asset_id
LEFT JOIN (
    SELECT asset_id, scrap_reason, approve_status 
    FROM asset_scrap_record 
    WHERE (asset_id, scrap_id) IN (
        SELECT asset_id, MAX(scrap_id) 
        FROM asset_scrap_record 
        GROUP BY asset_id
    )
) asr ON ai.asset_id = asr.asset_id;

-- 查看视图
SELECT * FROM v_asset_lifecycle LIMIT 5;
