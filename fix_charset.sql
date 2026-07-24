-- 更新数据库字符集为 utf8mb4
ALTER DATABASE it_asset_manage CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 更新 sys_user 表字符集
ALTER TABLE sys_user CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 更新 asset_category 表字符集
ALTER TABLE asset_category CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 更新 admin 用户姓名为"系统管理员"
UPDATE sys_user SET real_name = '系统管理员' WHERE username = 'admin';

-- 更新分类名称（如果显示乱码）
UPDATE asset_category SET category_name = '电子设备' WHERE category_id = 1;
UPDATE asset_category SET category_name = '电脑设备' WHERE category_id = 2;
UPDATE asset_category SET category_name = '笔记本' WHERE category_id = 3;
UPDATE asset_category SET category_name = '台式机' WHERE category_id = 4;
UPDATE asset_category SET category_name = '办公设备' WHERE category_id = 5;
UPDATE asset_category SET category_name = '打印机/复印机' WHERE category_id = 6;
UPDATE asset_category SET category_name = '办公家具' WHERE category_id = 7;
UPDATE asset_category SET category_name = '其他' WHERE category_id = 8;

-- 验证更新结果
SELECT user_id, username, real_name, role FROM sys_user;
SELECT category_id, category_name FROM asset_category;
