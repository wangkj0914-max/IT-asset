-- 检查所有模块的数据打通情况
USE it_asset_manage;

SELECT '=== 资产信息表 ===' AS '';
SELECT asset_id, asset_code, asset_name, category_id, status, user_id 
FROM asset_info 
LIMIT 5;

SELECT '=== 资产入库记录 ===' AS '';
SELECT inbound_id, inbound_no, asset_name, status, create_time 
FROM asset_inbound 
ORDER BY create_time DESC 
LIMIT 5;

SELECT '=== 资产领用记录 ===' AS '';
SELECT record_id, asset_id, asset_name, apply_user, apply_status, create_time 
FROM asset_use_record 
ORDER BY create_time DESC 
LIMIT 5;

SELECT '=== 资产报废记录 ===' AS '';
SELECT scrap_id, asset_code, asset_name, scrap_type, approve_status, apply_user_name, create_time 
FROM asset_scrap_record 
ORDER BY create_time DESC 
LIMIT 5;

SELECT '=== 资产变更日志 ===' AS '';
SELECT change_id, asset_id, change_type, old_value, new_value, create_time 
FROM asset_change_log 
ORDER BY create_time DESC 
LIMIT 10;

SELECT '=== 资产生命周期视图 ===' AS '';
SELECT * FROM v_asset_lifecycle LIMIT 5;
