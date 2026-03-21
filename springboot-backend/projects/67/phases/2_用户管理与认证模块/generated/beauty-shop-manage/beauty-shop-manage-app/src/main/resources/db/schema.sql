-- 美妆小店轻量级后端管理系统V1.0 阶段2数据库初始化脚本
-- 数据库名：beauty_shop_manage
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_unicode_ci

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `beauty_shop_manage` DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;

USE `beauty_shop_manage`;

-- 1. 美妆商品类别表（阶段1遗留，必须有默认值）
DROP TABLE IF EXISTS `beauty_category`;
CREATE TABLE `beauty_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_name` VARCHAR(50) NOT NULL COMMENT '商品类别名称',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父类别ID（0为「美妆全品类」）',
    `general_expiration_warning_days` INT NOT NULL DEFAULT 30 COMMENT '通用过期预警天数',
    `general_safety_stock` INT NOT NULL DEFAULT 10 COMMENT '通用安全库存（件）',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人ID',
    `update_by` BIGINT NULL COMMENT '更新人ID',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆商品类别表';

-- 2. 美妆员工角色表
DROP TABLE IF EXISTS `beauty_role`;
CREATE TABLE `beauty_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_desc` VARCHAR(200) NULL COMMENT '角色描述',
    `permission_codes` TEXT NULL COMMENT '权限码列表（逗号分隔）',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人ID',
    `update_by` BIGINT NULL COMMENT '更新人ID',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆员工角色表';

-- 3. 美妆员工档案表
DROP TABLE IF EXISTS `beauty_staff`;
CREATE TABLE `beauty_staff` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `staff_no` VARCHAR(20) NOT NULL COMMENT '员工工号',
    `login_account` VARCHAR(50) NOT NULL COMMENT '登录账号',
    `password` VARCHAR(255) NOT NULL COMMENT '登录密码（AES256加密）',
    `staff_name` VARCHAR(50) NOT NULL COMMENT '员工姓名',
    `contact_phone` VARCHAR(20) NULL COMMENT '联系电话',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `staff_status` TINYINT NOT NULL DEFAULT 1 COMMENT '员工状态（1正常，0停用）',
    `is_first_login` TINYINT NOT NULL DEFAULT 1 COMMENT '是否首次登录（1是，0否）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人ID',
    `update_by` BIGINT NULL COMMENT '更新人ID',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_login_account` (`login_account`),
    UNIQUE INDEX `idx_staff_no` (`staff_no`),
    INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆员工档案表';

-- 4. 美妆会员等级表
DROP TABLE IF EXISTS `beauty_member_level`;
CREATE TABLE `beauty_member_level` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `level_name` VARCHAR(50) NOT NULL COMMENT '等级名称',
    `level_icon` VARCHAR(255) NULL COMMENT '等级图标URL',
    `level_condition` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '升级条件（累计消费金额≥X元）',
    `general_discount` DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '普通折扣率（0.01-1.00）',
    `birthday_discount` DECIMAL(3,2) NOT NULL DEFAULT 0.90 COMMENT '生日折扣率（0.01-1.00）',
    `point_ratio` DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '积分兑换比例（1元=X积分）',
    `point_cash_ratio` INT NOT NULL DEFAULT 100 COMMENT '积分抵现比例（X积分=1元）',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人ID',
    `update_by` BIGINT NULL COMMENT '更新人ID',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆会员等级表';

-- 5. 美妆会员档案表
DROP TABLE IF EXISTS `beauty_member`;
CREATE TABLE `beauty_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `member_barcode` VARCHAR(50) NOT NULL COMMENT '会员条码',
    `member_card_no` VARCHAR(20) NULL COMMENT '会员卡号',
    `member_name` VARCHAR(50) NOT NULL COMMENT '会员姓名',
    `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `gender` TINYINT NOT NULL DEFAULT 2 COMMENT '性别（1男，0女，2未知）',
    `birthday` DATE NULL COMMENT '生日',
    `level_id` BIGINT NOT NULL COMMENT '等级ID',
    `total_consume_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费金额（元）',
    `consume_count` INT NOT NULL DEFAULT 0 COMMENT '消费次数',
    `available_balance` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '可用储值余额（元）',
    `available_points` INT NOT NULL DEFAULT 0 COMMENT '可用积分',
    `member_status` TINYINT NOT NULL DEFAULT 1 COMMENT '会员状态（1正常，0冻结）',
    `member_avatar` VARCHAR(255) NULL COMMENT '会员头像URL',
    `remark` VARCHAR(500) NULL COMMENT '备注',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人ID',
    `update_by` BIGINT NULL COMMENT '更新人ID',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_member_barcode` (`member_barcode`),
    UNIQUE INDEX `idx_contact_phone` (`contact_phone`),
    INDEX `idx_level_id` (`level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆会员档案表';

-- ------------------------------
-- 初始化数据
-- ------------------------------
-- 1. 美妆商品类别初始化数据
INSERT INTO `beauty_category` (`id`, `category_name`, `parent_id`, `general_expiration_warning_days`, `general_safety_stock`, `sort_order`, `create_by`, `update_by`) VALUES
(1, '美妆全品类', 0, 30, 10, 0, 1, 1);

-- 2. 美妆员工角色初始化数据（默认店长，拥有所有阶段2+基础权限）
INSERT INTO `beauty_role` (`id`, `role_name`, `role_desc`, `permission_codes`, `sort_order`, `create_by`, `update_by`) VALUES
(1, '店长', '美妆小店店长，拥有所有权限', 'staff:list,staff:save,staff:update,staff:delete,staff:reset:password,staff:batch:import,staff:template:download,staff:export,role:list,role:save,role:update,role:delete,member:list,member:save,member:update,member:delete,member:batch:import,member:template:download,member:export,member:recharge,member:point:adjust,member:detail,member:flow:list,member:flow:export,level:list,level:save,level:update,level:delete,dashboard,logout,first:password:change,refresh:token', 0, 1, 1);

-- 3. 美妆员工档案初始化数据（默认管理员，密码为AES256加密后的Admin@123，密钥BEAUTY_SHOP_AES256_KEY_32_CHARS）
-- 加密前：Admin@123
-- 密钥：BEAUTY_SHOP_AES256_KEY_32_CHARS
-- 加密后（使用Hutool AES256，IV=BEAUTY_SHOP_IV_16）：需替换为实际加密值
-- 这里暂时使用占位符，请使用实际工具加密
INSERT INTO `beauty_staff` (`id`, `staff_no`, `login_account`, `password`, `staff_name`, `contact_phone`, `role_id`, `staff_status`, `is_first_login`, `create_by`, `update_by`) VALUES
(1, 'YG0001', 'admin', 'REPLACE_WITH_ACTUAL_AES256_ENCRYPTED_PASSWORD', '默认管理员', '13800138000', 1, 1, 1, 1, 1);

-- 4. 美妆会员等级初始化数据（默认普通会员）
INSERT INTO `beauty_member_level` (`id`, `level_name`, `level_icon`, `level_condition`, `general_discount`, `birthday_discount`, `point_ratio`, `point_cash_ratio`, `sort_order`, `create_by`, `update_by`) VALUES
(1, '普通会员', NULL, 0.00, 1.00, 0.90, 1.00, 100, 0, 1, 1);