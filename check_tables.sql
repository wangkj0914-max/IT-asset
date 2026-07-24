-- 检查所有表是否存在
SELECT table_name, table_comment 
FROM information_schema.tables 
WHERE table_schema = 'it_asset_manage' 
ORDER BY table_name;

-- 显示所有表的记录数
SELECT 'asset_info' AS table_name, COUNT(*) AS row_count FROM asset_info
UNION ALL
SELECT 'asset_category', COUNT(*) FROM asset_category
UNION ALL
SELECT 'asset_use_record', COUNT(*) FROM asset_use_record
UNION ALL
SELECT 'asset_repair_record', COUNT(*) FROM asset_repair_record
UNION ALL
SELECT 'asset_scrap_record', COUNT(*) FROM asset_scrap_record
UNION ALL
SELECT 'sys_user', COUNT(*) FROM sys_user
UNION ALL
SELECT 'sys_department', COUNT(*) FROM sys_department
UNION ALL
SELECT 'asset_inbound', COUNT(*) FROM asset_inbound;
