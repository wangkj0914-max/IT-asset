-- 更新资产报废记录表，添加冗余字段方便查询
USE it_asset_manage;

-- 添加资产编号字段
ALTER TABLE asset_scrap_record ADD COLUMN asset_code VARCHAR(50) COMMENT '资产编号' AFTER asset_id;

-- 添加资产名称字段
ALTER TABLE asset_scrap_record ADD COLUMN asset_name VARCHAR(100) COMMENT '资产名称' AFTER asset_code;

-- 添加申请人姓名字段
ALTER TABLE asset_scrap_record ADD COLUMN apply_user_name VARCHAR(50) COMMENT '申请人姓名' AFTER apply_department;

-- 验证表结构
DESCRIBE asset_scrap_record;
