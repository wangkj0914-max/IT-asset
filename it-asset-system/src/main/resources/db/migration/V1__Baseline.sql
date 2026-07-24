-- V1__Baseline.sql: 当前系统全部表结构基线

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    real_name VARCHAR(50),
    role INT DEFAULT 1 COMMENT '2=admin,1=user',
    status INT DEFAULT 1,
    password_hash_type VARCHAR(20) COMMENT 'BCRYPT/MD5',
    login_fail_count INT DEFAULT 0,
    locked_until DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 资产分类
CREATE TABLE IF NOT EXISTS asset_category (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL,
    icon VARCHAR(50),
    sort_order INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 部门
CREATE TABLE IF NOT EXISTS sys_department (
    dept_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    site VARCHAR(20) DEFAULT '苏州',
    dept_name VARCHAR(100) NOT NULL,
    parent_id BIGINT,
    status INT DEFAULT 1,
    sort_order INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 存放地点
CREATE TABLE IF NOT EXISTS sys_storage_location (
    location_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    site VARCHAR(20) DEFAULT '苏州',
    location_name VARCHAR(100) NOT NULL,
    remark VARCHAR(200),
    sort_order INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 资产信息
CREATE TABLE IF NOT EXISTS asset_info (
    asset_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    site VARCHAR(20) DEFAULT '苏州',
    asset_code VARCHAR(50) COMMENT '资产编号',
    asset_name VARCHAR(200) NOT NULL,
    category_id BIGINT,
    brand VARCHAR(100),
    model VARCHAR(100),
    serial_number VARCHAR(100),
    purchase_price DECIMAL(10,2),
    purchase_date DATE,
    supplier VARCHAR(100),
    storage_location VARCHAR(100),
    status INT DEFAULT 1 COMMENT '0=空闲,1=使用中,2=报废',
    user_id BIGINT,
    user_name VARCHAR(50),
    department VARCHAR(100),
    warranty_info VARCHAR(200),
    remark VARCHAR(500),
    asset_image VARCHAR(500),
    quantity INT DEFAULT 1,
    depreciation_method VARCHAR(20),
    responsible_person VARCHAR(50),
    next_maintenance_date DATE,
    maintenance_cycle_days INT DEFAULT 365,
    warranty_expire_date DATE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 许可证
CREATE TABLE IF NOT EXISTS sys_license (
    license_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    software_name VARCHAR(100) NOT NULL,
    vendor VARCHAR(100),
    license_key VARCHAR(100),
    total_count INT DEFAULT 0,
    used_count INT DEFAULT 0,
    expire_date DATE,
    unit_price DECIMAL(10,2),
    responsible_person VARCHAR(50),
    remark VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 耗材
CREATE TABLE IF NOT EXISTS sys_consumable (
    consumable_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consumable_name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    model_spec VARCHAR(100),
    current_stock INT DEFAULT 0,
    min_stock INT DEFAULT 5,
    unit VARCHAR(20) DEFAULT '个',
    related_asset_id BIGINT,
    related_asset_code VARCHAR(50),
    remark VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 耗材出入库记录
CREATE TABLE IF NOT EXISTS sys_consumable_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consumable_id BIGINT NOT NULL,
    change_type VARCHAR(10) COMMENT 'IN/OUT',
    change_count INT NOT NULL,
    balance_after INT,
    operator VARCHAR(50),
    remark VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 变更历史
CREATE TABLE IF NOT EXISTS sys_asset_change_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    asset_code VARCHAR(50),
    field_name VARCHAR(50),
    old_value VARCHAR(500),
    new_value VARCHAR(500),
    operator VARCHAR(50),
    change_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_asset (asset_id),
    KEY idx_time (change_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 操作日志
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    operation VARCHAR(100),
    method VARCHAR(200),
    params TEXT,
    ip VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 默认管理员
INSERT IGNORE INTO sys_user (username, password, real_name, role, password_hash_type) 
VALUES ('admin', MD5('123456'), '管理员', 2, NULL);
