-- 修复 asset_inbound 表，添加缺失的字段

-- 添加 category_name 字段
ALTER TABLE asset_inbound 
ADD COLUMN IF NOT EXISTS `category_name` VARCHAR(100) DEFAULT NULL COMMENT '分类名称' AFTER `category_id`;

-- 验证字段已添加
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'it_asset_manage' 
  AND TABLE_NAME = 'asset_inbound'
ORDER BY ORDINAL_POSITION;
