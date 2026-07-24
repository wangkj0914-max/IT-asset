-- Create tables for Repair, Scrap, Inventory modules
USE `it_asset_manage`;

-- 1. Asset Repair Record Table
DROP TABLE IF EXISTS `asset_repair_record`;
CREATE TABLE `asset_repair_record` (
  `repair_id` BIGINT NOT NULL AUTO_INCREMENT,
  `asset_id` BIGINT NOT NULL,
  `apply_user_id` BIGINT DEFAULT NULL,
  `apply_user_name` VARCHAR(50) DEFAULT NULL,
  `apply_department` VARCHAR(100) DEFAULT NULL,
  `repair_reason` VARCHAR(500) NOT NULL,
  `repair_cost` DECIMAL(10,2) DEFAULT NULL,
  `repair_status` TINYINT DEFAULT 0,
  `repair_date` DATETIME DEFAULT NULL,
  `repair_man` VARCHAR(50) DEFAULT NULL,
  `remark` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`repair_id`),
  KEY `idx_asset_id` (`asset_id`),
  KEY `idx_status` (`repair_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Asset Scrap Record Table  
DROP TABLE IF EXISTS `asset_scrap_record`;
CREATE TABLE `asset_scrap_record` (
  `scrap_id` BIGINT NOT NULL AUTO_INCREMENT,
  `asset_id` BIGINT NOT NULL,
  `apply_user_id` BIGINT DEFAULT NULL,
  `apply_user_name` VARCHAR(50) DEFAULT NULL,
  `apply_department` VARCHAR(100) DEFAULT NULL,
  `scrap_reason` VARCHAR(500) NOT NULL,
  `scrap_type` TINYINT DEFAULT 0,
  `original_price` DECIMAL(10,2) DEFAULT NULL,
  `residual_value` DECIMAL(10,2) DEFAULT NULL,
  `approve_status` TINYINT DEFAULT 0,
  `approve_user` VARCHAR(50) DEFAULT NULL,
  `approve_time` DATETIME DEFAULT NULL,
  `remark` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`scrap_id`),
  KEY `idx_asset_id` (`asset_id`),
  KEY `idx_status` (`approve_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Asset Inventory Table
DROP TABLE IF EXISTS `asset_inventory`;
CREATE TABLE `asset_inventory` (
  `inventory_id` BIGINT NOT NULL AUTO_INCREMENT,
  `inventory_no` VARCHAR(50) NOT NULL,
  `inventory_name` VARCHAR(100) NOT NULL,
  `inventory_range` TINYINT DEFAULT 0,
  `range_value` VARCHAR(500) DEFAULT NULL,
  `inventory_date` DATETIME DEFAULT NULL,
  `operator_id` BIGINT DEFAULT NULL,
  `operator_name` VARCHAR(50) DEFAULT NULL,
  `status` TINYINT DEFAULT 0,
  `surplus_count` INT DEFAULT 0,
  `loss_count` INT DEFAULT 0,
  `remark` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`inventory_id`),
  UNIQUE KEY `uk_inventory_no` (`inventory_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Asset Inventory Detail Table
DROP TABLE IF EXISTS `asset_inventory_detail`;
CREATE TABLE `asset_inventory_detail` (
  `detail_id` BIGINT NOT NULL AUTO_INCREMENT,
  `inventory_id` BIGINT NOT NULL,
  `asset_id` BIGINT NOT NULL,
  `asset_code` VARCHAR(50) DEFAULT NULL,
  `asset_name` VARCHAR(200) DEFAULT NULL,
  `category_name` VARCHAR(100) DEFAULT NULL,
  `storage_location` VARCHAR(200) DEFAULT NULL,
  `department` VARCHAR(100) DEFAULT NULL,
  `user_name` VARCHAR(50) DEFAULT NULL,
  `status` TINYINT DEFAULT 0,
  `result_remark` VARCHAR(500) DEFAULT NULL,
  `check_time` DATETIME DEFAULT NULL,
  `checker_name` VARCHAR(50) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`detail_id`),
  KEY `idx_inventory_id` (`inventory_id`),
  KEY `idx_asset_id` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
