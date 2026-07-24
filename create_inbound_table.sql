-- 创建资产入库记录表
DROP TABLE IF EXISTS `asset_inbound`;

CREATE TABLE `asset_inbound` (
  `inbound_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '入库 ID',
  `inbound_no` VARCHAR(50) NOT NULL COMMENT '入库单号',
  `asset_id` BIGINT DEFAULT NULL COMMENT '资产 ID（审核后关联）',
  `asset_name` VARCHAR(200) NOT NULL COMMENT '资产名称',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类 ID',
  `category_name` VARCHAR(100) DEFAULT NULL COMMENT '分类名称',
  `brand` VARCHAR(100) DEFAULT NULL COMMENT '品牌',
  `model` VARCHAR(200) DEFAULT NULL COMMENT '型号',
  `serial_number` VARCHAR(100) DEFAULT NULL COMMENT '序列号',
  `purchase_price` DECIMAL(10,2) DEFAULT NULL COMMENT '采购价格',
  `supplier` VARCHAR(200) DEFAULT NULL COMMENT '供应商',
  `storage_location` VARCHAR(200) DEFAULT NULL COMMENT '存放位置',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0-待审核 1-已入库 2-已拒绝',
  `applicant` VARCHAR(50) DEFAULT NULL COMMENT '申请人',
  `apply_time` DATETIME DEFAULT NULL COMMENT '申请时间',
  `auditor` VARCHAR(50) DEFAULT NULL COMMENT '审核人',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`inbound_id`),
  UNIQUE KEY `uk_inbound_no` (`inbound_no`),
  KEY `idx_asset_name` (`asset_name`),
  KEY `idx_status` (`status`),
  KEY `idx_apply_time` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='资产入库记录表';

-- 验证表创建成功
SELECT 'Table asset_inbound created successfully!' AS result;
