USE it_asset_manage;

-- 测试插入盘点任务
INSERT INTO asset_inventory (inventory_no, inventory_name, inventory_range, inventory_date, operator_id, operator_name, status, remark) 
VALUES ('PD202603210001', '测试盘点任务', 0, NOW(), 1, '系统管理员', 0, '测试创建');

-- 查询结果
SELECT * FROM asset_inventory WHERE inventory_no = 'PD202603210001';
