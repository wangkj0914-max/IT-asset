-- 插入资产报废测试数据
USE it_asset_manage;

-- 插入 3 条测试报废记录
INSERT INTO asset_scrap_record 
(asset_id, asset_code, asset_name, scrap_reason, scrap_type, original_price, residual_value, apply_user_name, apply_department, approve_status, remark, create_time) 
VALUES
(8, 'ZC001', '测试资产 1', '设备老化，无法继续使用', 0, 5000.00, 200.00, '张三', '技术部', 0, '已使用 5 年，维修成本过高', NOW()),
(12, 'ZC0004', '资产 4', '意外损坏，无法修复', 1, 8000.00, 100.00, '李四', '市场部', 0, '搬运过程中摔坏', NOW()),
(15, 'ZC0007', '资产 7', '资产丢失', 2, 3000.00, 0.00, '王五', '行政部', 0, '盘点时发现丢失', NOW());

-- 查询插入结果
SELECT scrap_id, asset_code, asset_name, scrap_type, approve_status, apply_user_name, create_time 
FROM asset_scrap_record 
ORDER BY create_time DESC;
