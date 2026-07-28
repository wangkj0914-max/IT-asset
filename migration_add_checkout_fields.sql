-- 数据库迁移：为 asset_use_record 表添加预期归还日期、实际归还日期和逾期状态字段
-- 使用 INFORMATION_SCHEMA 检查列是否已存在，避免重复添加

SET @table_name = 'asset_use_record';
SET @db_name = 'it_asset_manage';

-- expected_return_date
SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = @table_name AND COLUMN_NAME = 'expected_return_date';
SET @sql = IF(@col_exists = 0, 'ALTER TABLE asset_use_record ADD COLUMN expected_return_date DATETIME NULL COMMENT ''预期归还日期'' AFTER use_date', 'SELECT ''column expected_return_date already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- actual_return_date
SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = @table_name AND COLUMN_NAME = 'actual_return_date';
SET @sql = IF(@col_exists = 0, 'ALTER TABLE asset_use_record ADD COLUMN actual_return_date DATETIME NULL COMMENT ''实际归还日期'' AFTER expected_return_date', 'SELECT ''column actual_return_date already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- overdue_status
SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = @table_name AND COLUMN_NAME = 'overdue_status';
SET @sql = IF(@col_exists = 0, 'ALTER TABLE asset_use_record ADD COLUMN overdue_status TINYINT DEFAULT 0 COMMENT ''逾期状态: 0=正常 1=已逾期 2=已关闭'' AFTER actual_return_date', 'SELECT ''column overdue_status already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
