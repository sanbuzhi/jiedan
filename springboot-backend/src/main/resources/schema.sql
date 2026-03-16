CREATE DATABASE IF NOT EXISTS jiedan CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE jiedan;

-- ============================================
-- 积分兑换商品表
-- ============================================
CREATE TABLE IF NOT EXISTS exchange_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    image_url VARCHAR(500) COMMENT '商品图片URL',
    points_required INT NOT NULL COMMENT '所需积分',
    cash_price DECIMAL(10, 2) COMMENT '现金价格',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否上架',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_is_active (is_active),
    INDEX idx_points_required (points_required)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分兑换商品表';

-- ============================================
-- 兑换订单表
-- ============================================
CREATE TABLE IF NOT EXISTS exchange_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    item_id BIGINT NOT NULL COMMENT '商品ID',
    points_used INT NOT NULL COMMENT '使用积分',
    cash_paid DECIMAL(10, 2) DEFAULT 0.00 COMMENT '支付现金',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/COMPLETED/CANCELLED/FAILED',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兑换订单表';

-- ============================================
-- 积分规则表（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '规则名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '规则代码',
    description TEXT COMMENT '规则描述',
    type VARCHAR(50) NOT NULL COMMENT '类型',
    points INT NOT NULL COMMENT '积分值',
    max_level INT NOT NULL DEFAULT 3 COMMENT '最大层级',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否激活',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_code (code),
    INDEX idx_type (type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分规则表';

-- ============================================
-- 积分记录表（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS point_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(50) NOT NULL COMMENT '类型：REGISTER/REFERRAL/EXCHANGE等',
    amount INT NOT NULL COMMENT '变动金额（正为收入，负为支出）',
    balance INT NOT NULL COMMENT '变动后余额',
    description TEXT COMMENT '描述',
    related_user_id BIGINT COMMENT '关联用户ID',
    rule_id BIGINT COMMENT '规则ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分记录表';


