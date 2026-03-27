-- StockMaster Database Initialization Script
-- Compatible with MySQL 8.0+

CREATE DATABASE IF NOT EXISTS stock_master DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE stock_master;

-- System User Table
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar TEXT COMMENT '头像',
    gender TINYINT DEFAULT 0 COMMENT '性别',
    status TINYINT DEFAULT 1 COMMENT '状态',
    admin TINYINT(1) DEFAULT 0 COMMENT '是否管理员',
    dept_id BIGINT COMMENT '部门ID',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    deleted TINYINT(1) DEFAULT 0,
    remark VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- System Role Table
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(50) NOT NULL,
    description VARCHAR(100),
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    deleted TINYINT(1) DEFAULT 0,
    remark VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- System Menu Table
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT,
    menu_name VARCHAR(50) NOT NULL,
    path VARCHAR(100),
    component VARCHAR(100),
    permission VARCHAR(100),
    icon VARCHAR(50),
    menu_type TINYINT DEFAULT 1,
    sort_order INT DEFAULT 0,
    visible TINYINT(1) DEFAULT 1,
    status TINYINT DEFAULT 1,
    is_external TINYINT(1) DEFAULT 0,
    is_cached TINYINT(1) DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    deleted TINYINT(1) DEFAULT 0,
    remark VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User Role Relation Table
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Role Menu Relation Table
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- System Log Table
CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operation_type VARCHAR(20),
    module VARCHAR(50),
    description VARCHAR(200),
    method_name VARCHAR(200),
    request_method VARCHAR(10),
    request_url VARCHAR(200),
    request_params TEXT,
    response_data TEXT,
    ip VARCHAR(50),
    username VARCHAR(50),
    time BIGINT,
    status INT,
    error_msg TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- System Config Table
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    config_name VARCHAR(100),
    description VARCHAR(500),
    config_type VARCHAR(20),
    config_group VARCHAR(50),
    sort_order INT,
    is_system TINYINT(1) DEFAULT 0,
    is_enabled TINYINT(1) DEFAULT 1,
    status TINYINT DEFAULT 1,
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    deleted TINYINT(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Stock Category Table
CREATE TABLE IF NOT EXISTS stock_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT,
    category_name VARCHAR(50) NOT NULL,
    category_code VARCHAR(50) UNIQUE,
    sort_order INT,
    status TINYINT DEFAULT 1,
    icon VARCHAR(100),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    deleted TINYINT(1) DEFAULT 0,
    remark VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Stock Product Table
CREATE TABLE IF NOT EXISTS stock_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL UNIQUE,
    product_name VARCHAR(100) NOT NULL,
    category_id BIGINT,
    brand VARCHAR(50),
    spec VARCHAR(100),
    unit VARCHAR(20),
    barcode VARCHAR(50),
    cost_price DECIMAL(12, 2),
    sale_price DECIMAL(12, 2),
    min_stock INT,
    max_stock INT,
    status VARCHAR(20),
    image_url TEXT,
    description TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    deleted TINYINT(1) DEFAULT 0,
    remark VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Warehouse Table
CREATE TABLE IF NOT EXISTS stock_warehouse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_code VARCHAR(50) NOT NULL UNIQUE,
    warehouse_name VARCHAR(100) NOT NULL,
    address VARCHAR(200),
    contact_person VARCHAR(50),
    contact_phone VARCHAR(20),
    status TINYINT DEFAULT 1,
    capacity INT,
    description VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    deleted TINYINT(1) DEFAULT 0,
    remark VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Inventory Table
CREATE TABLE IF NOT EXISTS stock_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    warehouse_code VARCHAR(50),
    quantity INT NOT NULL DEFAULT 0,
    frozen_quantity INT DEFAULT 0,
    available_quantity INT DEFAULT 0,
    batch_no VARCHAR(50),
    shelf_location VARCHAR(50),
    warning_min INT,
    warning_max INT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    deleted TINYINT(1) DEFAULT 0,
    remark VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Inbound Table
CREATE TABLE IF NOT EXISTS stock_inbound (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inbound_no VARCHAR(50) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12, 2),
    total_price DECIMAL(12, 2),
    supplier_id BIGINT,
    warehouse_code VARCHAR(50),
    batch_no VARCHAR(50),
    inbound_time DATETIME,
    operator VARCHAR(50),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    deleted TINYINT(1) DEFAULT 0,
    remark VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Outbound Table
CREATE TABLE IF NOT EXISTS stock_outbound (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    outbound_no VARCHAR(50) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12, 2),
    total_price DECIMAL(12, 2),
    warehouse_code VARCHAR(50),
    batch_no VARCHAR(50),
    outbound_time DATETIME,
    operator VARCHAR(50),
    outbound_type VARCHAR(20),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    deleted TINYINT(1) DEFAULT 0,
    remark VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Supplier Table
CREATE TABLE IF NOT EXISTS purchase_supplier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_code VARCHAR(50) NOT NULL UNIQUE,
    supplier_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(50),
    contact_phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(200),
    bank_name VARCHAR(100),
    bank_account VARCHAR(50),
    tax_number VARCHAR(50),
    status TINYINT DEFAULT 1,
    rating DECIMAL(3, 2),
    description VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    deleted TINYINT(1) DEFAULT 0,
    remark VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Purchase Order Table
CREATE TABLE IF NOT EXISTS purchase_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    order_date DATETIME,
    expected_date DATETIME,
    total_amount DECIMAL(12, 2),
    status VARCHAR(20),
    buyer VARCHAR(50),
    approve_time DATETIME,
    approver VARCHAR(50),
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    deleted TINYINT(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Purchase Order Item Table
CREATE TABLE IF NOT EXISTS purchase_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    received_quantity INT DEFAULT 0,
    unit_price DECIMAL(12, 2),
    total_price DECIMAL(12, 2),
    remark VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    deleted TINYINT(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Supplier Evaluation Table
CREATE TABLE IF NOT EXISTS purchase_supplier_evaluation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    order_id BIGINT,
    quality_score INT,
    delivery_score INT,
    service_score INT,
    price_score INT,
    total_score DECIMAL(3, 2),
    content TEXT,
    evaluator VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    deleted TINYINT(1) DEFAULT 0,
    remark VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Initialize default admin account (password: 123456)
INSERT INTO sys_user (username, password, real_name, admin, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 1, 1);

-- Initialize default roles
INSERT INTO sys_role (role_code, role_name, description, sort_order, status) VALUES
('ADMIN', '系统管理员', '拥有所有权限', 1, 1),
('USER', '普通用户', '基本操作权限', 2, 1);

-- Link user to admin role
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- Initialize sample categories
INSERT INTO stock_category (category_name, category_code, sort_order, status) VALUES
('电子产品', 'ELEC001', 1, 1),
('办公用品', 'OFFICE001', 2, 1),
('日用百货', 'DAILY001', 3, 1);

-- Initialize sample suppliers
INSERT INTO purchase_supplier (supplier_code, supplier_name, contact_person, contact_phone, status) VALUES
('SUP001', '北京科技有限公司', '张三', '13800138001', 1),
('SUP002', '上海贸易公司', '李四', '13800138002', 1);

-- Initialize sample warehouse
INSERT INTO stock_warehouse (warehouse_code, warehouse_name, address, contact_person, contact_phone, status) VALUES
('WH001', '主仓库', '北京市朝阳区', '王五', '13800138003', 1);
