-- P0 改进：资产模型、状态标签、折旧/EOL
-- 执行顺序：1.建新表 2.加列 3.初始化数据

-- ============ 1. 资产模型表 ============
CREATE TABLE IF NOT EXISTS `asset_model` (
  `model_id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模型ID',
  `model_name` VARCHAR(200) NOT NULL COMMENT '模型名称',
  `model_number` VARCHAR(100) COMMENT '模型编号',
  `category_id` BIGINT COMMENT '所属分类ID',
  `manufacturer` VARCHAR(200) COMMENT '制造商',
  `specs` TEXT COMMENT '规格说明(CPU/RAM/HDD等)',
  `eol_months` INT DEFAULT 36 COMMENT '默认EOL周期(月)',
  `depreciation_years` INT DEFAULT 3 COMMENT '默认折旧年限',
  `depreciation_method` VARCHAR(20) DEFAULT 'straight_line' COMMENT '折旧方法: straight_line/declining_balance',
  `site` VARCHAR(50) DEFAULT '苏州' COMMENT '所属站点',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_category (`category_id`),
  INDEX idx_site (`site`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产模型';

-- ============ 2. 状态标签表 ============
CREATE TABLE IF NOT EXISTS `status_label` (
  `status_label_id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '状态标签ID',
  `status_name` VARCHAR(100) NOT NULL COMMENT '状态名称',
  `status_type` INT NOT NULL DEFAULT 0 COMMENT '状态类型: 0=可部署, 1=已部署, 2=不可部署, 3=已归档',
  `color` VARCHAR(20) DEFAULT 'info' COMMENT '显示颜色: primary/success/warning/danger/info',
  `site` VARCHAR(50) DEFAULT '苏州' COMMENT '所属站点',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认状态',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_site (`site`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='状态标签';

-- ============ 3. asset_info 新增字段 ============
-- 资产模型关联
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='it_asset_manage' AND TABLE_NAME='asset_info' AND COLUMN_NAME='model_id') = 0,
  'ALTER TABLE `asset_info` ADD COLUMN `model_id` BIGINT DEFAULT NULL COMMENT ''资产模型ID'' AFTER `model`', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- 状态标签关联
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='it_asset_manage' AND TABLE_NAME='asset_info' AND COLUMN_NAME='status_label_id') = 0,
  'ALTER TABLE `asset_info` ADD COLUMN `status_label_id` BIGINT DEFAULT NULL COMMENT ''状态标签ID'' AFTER `status`', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- 采购成本
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='it_asset_manage' AND TABLE_NAME='asset_info' AND COLUMN_NAME='purchase_cost') = 0,
  'ALTER TABLE `asset_info` ADD COLUMN `purchase_cost` DECIMAL(14,2) DEFAULT NULL COMMENT ''采购成本'' AFTER `purchase_price`', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- 折旧年限
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='it_asset_manage' AND TABLE_NAME='asset_info' AND COLUMN_NAME='depreciation_years') = 0,
  'ALTER TABLE `asset_info` ADD COLUMN `depreciation_years` INT DEFAULT NULL COMMENT ''折旧年限'' AFTER `depreciation_method`', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- 折旧率（年率，百分比）
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='it_asset_manage' AND TABLE_NAME='asset_info' AND COLUMN_NAME='depreciation_rate') = 0,
  'ALTER TABLE `asset_info` ADD COLUMN `depreciation_rate` DECIMAL(5,2) DEFAULT NULL COMMENT ''年折旧率(%)'' AFTER `depreciation_years`', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- EOL日期
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='it_asset_manage' AND TABLE_NAME='asset_info' AND COLUMN_NAME='eol_date') = 0,
  'ALTER TABLE `asset_info` ADD COLUMN `eol_date` DATE DEFAULT NULL COMMENT ''EOL日期'' AFTER `depreciation_rate`', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- 当前价值（后端自动计算）
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='it_asset_manage' AND TABLE_NAME='asset_info' AND COLUMN_NAME='current_value') = 0,
  'ALTER TABLE `asset_info` ADD COLUMN `current_value` DECIMAL(14,2) DEFAULT NULL COMMENT ''当前价值(自动计算)'' AFTER `eol_date`', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============ 4. 初始化状态标签数据 ============
INSERT INTO `status_label` (`status_name`, `status_type`, `color`, `site`, `is_default`, `remark`) VALUES
('待部署', 0, 'primary', '苏州', 1, '资产已入库，等待分配'),
('已部署', 1, 'success', '苏州', 0, '资产已分配给用户使用'),
('维修中', 2, 'warning', '苏州', 0, '资产正在维修'),
('已报废', 3, 'danger', '苏州', 0, '资产已报废处理'),
('已归档', 3, 'info', '苏州', 0, '资产已归档，不再使用'),
('待部署', 0, 'primary', 'Penang', 1, '资产已入库，等待分配'),
('已部署', 1, 'success', 'Penang', 0, '资产已分配给用户使用'),
('维修中', 2, 'warning', 'Penang', 0, '资产正在维修'),
('已报废', 3, 'danger', 'Penang', 0, '资产已报废处理'),
('已归档', 3, 'info', 'Penang', 0, '资产已归档，不再使用');

-- ============ 5. 为现有资产设置默认状态标签 ============
-- status=0(未领用) → 待部署, status=1(已领用) → 已部署, status=2(维修中) → 维修中, status=3(已报废) → 已报废
UPDATE `asset_info` a SET a.`status_label_id` = (
  SELECT s.`status_label_id` FROM `status_label` s
  WHERE s.`site` = a.`site`
  AND (
    (a.`status` = 0 AND s.`status_type` = 0 AND s.`status_name` = '待部署')
    OR (a.`status` = 1 AND s.`status_type` = 1 AND s.`status_name` = '已部署')
    OR (a.`status` = 2 AND s.`status_type` = 2 AND s.`status_name` = '维修中')
    OR (a.`status` = 3 AND s.`status_type` = 3 AND s.`status_name` = '已报废')
  )
  LIMIT 1
) WHERE a.`status_label_id` IS NULL;

-- ============ 6. 为现有资产初始化采购成本和当前价值 ============
-- purchase_cost 默认取 purchase_price
UPDATE `asset_info` SET `purchase_cost` = `purchase_price` WHERE `purchase_cost` IS NULL AND `purchase_price` IS NOT NULL;

-- 为有采购成本和采购日期的资产设置默认折旧年限和当前价值
UPDATE `asset_info`
SET `depreciation_years` = 3,
    `depreciation_rate` = 33.33,
    `eol_date` = DATE_ADD(`purchase_date`, INTERVAL 36 MONTH)
WHERE `purchase_cost` IS NOT NULL
  AND `purchase_date` IS NOT NULL
  AND `depreciation_years` IS NULL;

-- 计算当前价值（直线折旧法：每月折旧 = 采购成本 / (折旧年限*12)）
UPDATE `asset_info`
SET `current_value` = GREATEST(
  0,
  `purchase_cost` - (
    `purchase_cost` * TIMESTAMPDIFF(MONTH, `purchase_date`, CURDATE()) / (`depreciation_years` * 12)
  )
)
WHERE `purchase_cost` IS NOT NULL
  AND `purchase_date` IS NOT NULL
  AND `depreciation_years` IS NOT NULL;
