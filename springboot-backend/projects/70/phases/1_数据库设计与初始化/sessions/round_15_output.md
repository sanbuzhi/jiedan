===FILE:back/tongquyouyi.sql===
```sql
-- =============================================
-- 数据库名称：tongquyouyi
-- 数据库版本：MySQL 8.0.35
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_unicode_ci
-- 引擎：InnoDB
-- 功能：公共表、商品管理、库存管理、会员管理、订单管理的初始化脚本
-- 注意：
-- 1. 软删除使用deleted_at字段，查询时需加WHERE deleted_at IS NULL
-- 2. 预设数据仅用于测试，实际部署请修改sys_user的密码、商品信息等敏感数据
-- 3. 部分业务字段（如BCrypt密码）使用在线工具生成的123456加密值
-- =============================================

-- 设置SQL_MODE（防止分组、日期等兼容性问题）
SET SQL_MODE = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 删除旧库（如果存在）
DROP DATABASE IF EXISTS `tongquyouyi`;

-- 创建新库
CREATE DATABASE `tongquyouyi` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 切换到新库
USE `tongquyouyi`;

-- =============================================
-- 1. 公共系统表
-- =============================================

-- 1.1 省市区地区表（sys_region）
DROP TABLE IF EXISTS `sys_region`;
CREATE TABLE `sys_region` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `province_code` VARCHAR(6) NOT NULL COMMENT '省份/直辖市代码',
    `province_name` VARCHAR(50) NOT NULL COMMENT '省份/直辖市名称',
    `city_code` VARCHAR(6) NOT NULL COMMENT '城市/区代码',
    `city_name` VARCHAR(50) NOT NULL COMMENT '城市/区名称',
    `district_code` VARCHAR(6) NOT NULL COMMENT '区县代码',
    `district_name` VARCHAR(50) NOT NULL COMMENT '区县名称',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_district_code` (`district_code`),
    INDEX `idx_province_code` (`province_code`),
    INDEX `idx_city_code` (`city_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='省市区地区表';

-- 1.2 系统用户表（管理员，sys_user）
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(32) NOT NULL COMMENT '登录用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '登录密码（BCrypt加密）',
    `nickname` VARCHAR(32) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(16) NOT NULL COMMENT '手机号',
    `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `role` VARCHAR(32) NOT NULL DEFAULT 'OPERATOR' COMMENT '角色：SUPER_ADMIN-超级管理员，ADMIN-管理员，OPERATOR-运营',
    `last_login_time` DATETIME(3) DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表（管理员）';

-- =============================================
-- 2. 会员管理表
-- =============================================

-- 2.1 会员等级表（mem_level）
DROP TABLE IF EXISTS `mem_level`;
CREATE TABLE `mem_level` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `level_name` VARCHAR(32) NOT NULL COMMENT '等级名称',
    `level_icon` VARCHAR(255) DEFAULT NULL COMMENT '等级图标URL',
    `min_points` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '最低升级积分',
    `discount` DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '购物折扣（1.00表示不打折）',
    `points_ratio` DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '积分获取比例（1.00表示消费1元得1积分）',
    `sort` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
    `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_level_name` (`level_name`),
    INDEX `idx_min_points` (`min_points`),
    INDEX `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员等级表';

-- 2.2 会员用户表（mem_user）
DROP TABLE IF EXISTS `mem_user`;
CREATE TABLE `mem_user` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `mem_level_id` BIGINT UNSIGNED NOT NULL COMMENT '会员等级ID',
    `username` VARCHAR(32) NOT NULL COMMENT '登录用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '登录密码（BCrypt加密）',
    `nickname` VARCHAR(32) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `real_name` VARCHAR(32) DEFAULT NULL COMMENT '真实姓名',
    `id_card` VARCHAR(32) DEFAULT NULL COMMENT '身份证号',
    `phone` VARCHAR(16) NOT NULL COMMENT '手机号',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `total_points` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计积分',
    `available_points` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '可用积分',
    `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
    `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `register_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '注册时间',
    `register_ip` VARCHAR(64) DEFAULT NULL COMMENT '注册IP',
    `last_login_time` DATETIME(3) DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    INDEX `idx_mem_level_id` (`mem_level_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员用户表';

-- 2.3 会员收货地址表（mem_address）
DROP TABLE IF EXISTS `mem_address`;
CREATE TABLE `mem_address` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `mem_user_id` BIGINT UNSIGNED NOT NULL COMMENT '会员用户ID',
    `consignee` VARCHAR(32) NOT NULL COMMENT '收货人',
    `phone` VARCHAR(16) NOT NULL COMMENT '收货手机号',
    `province_code` VARCHAR(6) NOT NULL COMMENT '省份/直辖市代码',
    `city_code` VARCHAR(6) NOT NULL COMMENT '城市/区代码',
    `district_code` VARCHAR(6) NOT NULL COMMENT '区县代码',
    `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `full_address` VARCHAR(512) NOT NULL COMMENT '完整地址（省市区+详细）',
    `postal_code` VARCHAR(6) DEFAULT NULL COMMENT '邮政编码',
    `is_default` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否默认：0-否，1-是',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    INDEX `idx_mem_user_id` (`mem_user_id`),
    INDEX `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员收货地址表';

-- 2.4 会员积分日志表（mem_points_log）
DROP TABLE IF EXISTS `mem_points_log`;
CREATE TABLE `mem_points_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `mem_user_id` BIGINT UNSIGNED NOT NULL COMMENT '会员用户ID',
    `change_type` VARCHAR(32) NOT NULL COMMENT '积分变动类型：REGISTER-注册赠送，CONSUME-消费获取，EXCHANGE-积分兑换，REFUND-退款返还，ADMIN-管理员操作',
    `change_points` BIGINT NOT NULL COMMENT '变动积分（正数增加，负数减少）',
    `before_points` BIGINT UNSIGNED NOT NULL COMMENT '变动前可用积分',
    `after_points` BIGINT UNSIGNED NOT NULL COMMENT '变动后可用积分',
    `related_order_no` VARCHAR(64) DEFAULT NULL COMMENT '关联订单号',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_mem_user_id` (`mem_user_id`),
    INDEX `idx_related_order_no` (`related_order_no`),
    INDEX `idx_created_at` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员积分日志表';

-- =============================================
-- 3. 商品管理表
-- =============================================

-- 3.1 商品分类表（goods_category，支持多级分类）
DROP TABLE IF EXISTS `goods_category`;
CREATE TABLE `goods_category` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类ID（0表示一级分类）',
    `category_name` VARCHAR(32) NOT NULL COMMENT '分类名称',
    `category_icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标URL',
    `category_sort` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '分类排序（越小越靠前）',
    `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_category_sort` (`category_sort`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 3.2 商品基础表（goods_base）
DROP TABLE IF EXISTS `goods_base`;
CREATE TABLE `goods_base` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_id` BIGINT UNSIGNED NOT NULL COMMENT '商品分类ID（关联二级或三级分类）',
    `goods_name` VARCHAR(255) NOT NULL COMMENT '商品名称',
    `goods_subtitle` VARCHAR(512) DEFAULT NULL COMMENT '商品副标题',
    `main_image` VARCHAR(255) NOT NULL COMMENT '商品主图URL',
    `images` TEXT DEFAULT NULL COMMENT '商品轮播图URL（JSON数组格式）',
    `description` TEXT DEFAULT NULL COMMENT '商品详情（富文本格式）',
    `unit` VARCHAR(16) NOT NULL DEFAULT '件' COMMENT '商品单位',
    `brand` VARCHAR(64) DEFAULT NULL COMMENT '商品品牌',
    `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0-下架，1-上架',
    `is_hot` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否热门：0-否，1-是',
    `is_new` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否新品：0-否，1-是',
    `is_recommend` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
    `sort` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '商品排序（越小越靠前）',
    `sales_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计销量',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_is_hot` (`is_hot`),
    INDEX `idx_is_new` (`is_new`),
    INDEX `idx_is_recommend` (`is_recommend`),
    INDEX `idx_sales_count` (`sales_count` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品基础表';

-- 3.3 商品规格表（goods_spec，如颜色、尺寸）
DROP TABLE IF EXISTS `goods_spec`;
CREATE TABLE `goods_spec` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `goods_id` BIGINT UNSIGNED NOT NULL COMMENT '商品基础ID',
    `spec_name` VARCHAR(32) NOT NULL COMMENT '规格名称',
    `spec_sort` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '规格排序（越小越靠前）',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    INDEX `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品规格表';

-- 3.4 商品规格值表（goods_spec_value，如黑色、白色、128G）
DROP TABLE IF EXISTS `goods_spec_value`;
CREATE TABLE `goods_spec_value` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `spec_id` BIGINT UNSIGNED NOT NULL COMMENT '商品规格ID',
    `spec_value` VARCHAR(64) NOT NULL COMMENT '规格值',
    `spec_value_sort` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '规格值排序（越小越靠前）',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    INDEX `idx_spec_id` (`spec_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品规格值表';

-- 3.5 商品SKU表（goods_sku，最小库存单元）
DROP TABLE IF EXISTS `goods_sku`;
CREATE TABLE `goods_sku` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `goods_id` BIGINT UNSIGNED NOT NULL COMMENT '商品基础ID',
    `category_id` BIGINT UNSIGNED NOT NULL COMMENT '商品分类ID',
    `spec_ids` VARCHAR(255) NOT NULL COMMENT '规格ID组合（逗号分隔，如1,3）',
    `spec_values` VARCHAR(255) NOT NULL COMMENT '规格值组合（逗号分隔，如黑色,128G）',
    `main_image` VARCHAR(255) DEFAULT NULL COMMENT 'SKU主图URL（为空则使用商品主图）',
    `price` DECIMAL(10,2) NOT NULL COMMENT '销售价格',
    `original_price` DECIMAL(10,2) NOT NULL COMMENT '原价',
    `cost_price` DECIMAL(10,2) NOT NULL COMMENT '成本价格',
    `weight` DECIMAL(10,2) DEFAULT 0.00 COMMENT '重量（单位：kg）',
    `volume` DECIMAL(10,2) DEFAULT 0.00 COMMENT '体积（单位：m³）',
    `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `sales_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'SKU累计销量',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_goods_spec_ids` (`goods_id`, `spec_ids`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_price` (`price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU表';

-- =============================================
-- 4. 库存管理表
-- =============================================

-- 4.1 仓库表（sys_warehouse，简化预设）
DROP TABLE IF EXISTS `sys_warehouse`;
CREATE TABLE `sys_warehouse` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `warehouse_name` VARCHAR(64) NOT NULL COMMENT '仓库名称',
    `warehouse_code` VARCHAR(32) NOT NULL COMMENT '仓库代码',
    `province_code` VARCHAR(6) NOT NULL COMMENT '省份/直辖市代码',
    `city_code` VARCHAR(6) NOT NULL COMMENT '城市/区代码',
    `district_code` VARCHAR(6) NOT NULL COMMENT '区县代码',
    `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `full_address` VARCHAR(512) NOT NULL COMMENT '完整地址',
    `contact_name` VARCHAR(32) DEFAULT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(16) DEFAULT NULL COMMENT '联系电话',
    `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_warehouse_code` (`warehouse_code`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库表';

-- 4.2 库存表（inv_stock，关联SKU和仓库）
DROP TABLE IF EXISTS `inv_stock`;
CREATE TABLE `inv_stock` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT '商品SKU ID',
    `warehouse_id` BIGINT UNSIGNED NOT NULL COMMENT '仓库ID',
    `available_stock` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '可用库存',
    `frozen_stock` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '冻结库存（下单未付款/未出库）',
    `total_stock` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '总库存（可用+冻结）',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_warehouse` (`sku_id`, `warehouse_id`),
    INDEX `idx_available_stock` (`available_stock`),
    INDEX `idx_total_stock` (`total_stock`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

-- 4.3 库存变动记录表（inv_stock_record，流水账）
DROP TABLE IF EXISTS `inv_stock_record`;
CREATE TABLE `inv_stock_record` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT '商品SKU ID',
    `warehouse_id` BIGINT UNSIGNED NOT NULL COMMENT '仓库ID',
    `change_type` VARCHAR(32) NOT NULL COMMENT '变动类型：INBOUND-入库，OUTBOUND-出库，FREEZE-冻结，UNFREEZE-解冻，CHECK-盘点',
    `change_stock` BIGINT NOT NULL COMMENT '变动数量（正数增加，负数减少）',
    `before_available` BIGINT UNSIGNED NOT NULL COMMENT '变动前可用库存',
    `after_available` BIGINT UNSIGNED NOT NULL COMMENT '变动后可用库存',
    `before_frozen` BIGINT UNSIGNED NOT NULL COMMENT '变动前冻结库存',
    `after_frozen` BIGINT UNSIGNED NOT NULL COMMENT '变动后冻结库存',
    `before_total` BIGINT UNSIGNED NOT NULL COMMENT '变动前总库存',
    `after_total` BIGINT UNSIGNED NOT NULL COMMENT '变动后总库存',
    `related_order_no` VARCHAR(64) DEFAULT NULL COMMENT '关联订单号/入库单号/出库单号',
    `related_type` VARCHAR(32) DEFAULT NULL COMMENT '关联类型：PURCHASE-采购入库，SALE-销售出库，REFUND-退货入库，CHECK-盘点调整',
    `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID（关联sys_user或mem_user）',
    `operator_type` VARCHAR(32) DEFAULT NULL COMMENT '操作人类型：SYS-系统用户，MEM-会员用户，AUTO-系统自动',
    `operator_name` VARCHAR(32) DEFAULT NULL COMMENT '操作人姓名',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_sku_warehouse` (`sku_id`, `warehouse_id`),
    INDEX `idx_related_order_no` (`related_order_no`),
    INDEX `idx_created_at` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存变动记录表';

-- =============================================
-- 5. 订单管理表
-- =============================================

-- 5.1 订单基础表（ord_order）
DROP TABLE IF EXISTS `ord_order`;
CREATE TABLE `ord_order` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号（唯一）',
    `mem_user_id` BIGINT UNSIGNED NOT NULL COMMENT '会员用户ID',
    `consignee` VARCHAR(32) NOT NULL COMMENT '收货人',
    `phone` VARCHAR(16) NOT NULL COMMENT '收货手机号',
    `province_code` VARCHAR(6) NOT NULL COMMENT '省份/直辖市代码',
    `city_code` VARCHAR(6) NOT NULL COMMENT '城市/区代码',
    `district_code` VARCHAR(6) NOT NULL COMMENT '区县代码',
    `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `full_address` VARCHAR(512) NOT NULL COMMENT '完整地址',
    `postal_code` VARCHAR(6) DEFAULT NULL COMMENT '邮政编码',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额（商品总金额）',
    `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额（优惠券+积分+会员折扣）',
    `freight_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费金额',
    `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额（总金额-优惠+运费）',
    `pay_type` VARCHAR(32) DEFAULT NULL COMMENT '支付方式：ALIPAY-支付宝，WECHAT-微信，BALANCE-余额，POINTS-积分',
    `pay_time` DATETIME(3) DEFAULT NULL COMMENT '支付时间',
    `order_status` VARCHAR(32) NOT NULL DEFAULT 'UNPAID' COMMENT '订单状态：UNPAID-待付款，PAID-已付款待发货，SHIPPED-已发货待收货，RECEIVED-已收货待评价，COMPLETED-已完成，CANCELLED-已取消',
    `shipping_status` VARCHAR(32) NOT NULL DEFAULT 'UNSHIPPED' COMMENT '配送状态：UNSHIPPED-未发货，SHIPPED-已发货，DELIVERED-已签收',
    `pay_status` VARCHAR(32) NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态：UNPAID-未支付，PAID-已支付，REFUNDING-退款中，REFUNDED-已退款',
    `invoice_type` VARCHAR(32) DEFAULT NULL COMMENT '发票类型：PERSONAL-个人，COMPANY-企业',
    `invoice_title` VARCHAR(255) DEFAULT NULL COMMENT '发票抬头',
    `invoice_tax_no` VARCHAR(32) DEFAULT NULL COMMENT '企业税号',
    `invoice_content` VARCHAR(255) DEFAULT NULL COMMENT '发票内容',
    `user_remark` VARCHAR(255) DEFAULT NULL COMMENT '用户备注',
    `admin_remark` VARCHAR(255) DEFAULT NULL COMMENT '管理员备注',
    `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    `cancel_time` DATETIME(3) DEFAULT NULL COMMENT '取消时间',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    INDEX `idx_mem_user_id` (`mem_user_id`),
    INDEX `idx_order_status` (`order_status`),
    INDEX `idx_pay_status` (`pay_status`),
    INDEX `idx_created_at` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单基础表';

-- 5.2 订单商品项表（ord_order_item）
DROP TABLE IF EXISTS `ord_order_item`;
CREATE TABLE `ord_order_item` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT '商品SKU ID',
    `goods_id` BIGINT UNSIGNED NOT NULL COMMENT '商品基础ID',
    `goods_name` VARCHAR(255) NOT NULL COMMENT '商品名称（快照）',
    `main_image` VARCHAR(255) NOT NULL COMMENT '商品主图（快照）',
    `spec_values` VARCHAR(255) NOT NULL COMMENT '规格值组合（快照）',
    `price` DECIMAL(10,2) NOT NULL COMMENT '销售价格（快照）',
    `original_price` DECIMAL(10,2) NOT NULL COMMENT '原价（快照）',
    `quantity` INT UNSIGNED NOT NULL COMMENT '购买数量',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '商品项总金额（price*quantity）',
    `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品项优惠金额',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品项表';

-- 5.3 订单操作日志表（ord_order_log）
DROP TABLE IF EXISTS `ord_order_log`;
CREATE TABLE `ord_order_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `order_status_before` VARCHAR(32) DEFAULT NULL COMMENT '操作前订单状态',
    `order_status_after` VARCHAR(32) NOT NULL COMMENT '操作后订单状态',
    `pay_status_before` VARCHAR(32) DEFAULT NULL COMMENT '操作前支付状态',
    `pay_status_after` VARCHAR(32) DEFAULT NULL COMMENT '操作后支付状态',
    `shipping_status_before` VARCHAR(32) DEFAULT NULL COMMENT '操作前配送状态',
    `shipping_status_after` VARCHAR(32) DEFAULT NULL COMMENT '操作后配送状态',
    `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID',
    `operator_type` VARCHAR(32) DEFAULT NULL COMMENT '操作人类型：SYS-系统用户，MEM-会员用户，AUTO-系统自动',
    `operator_name` VARCHAR(32) DEFAULT NULL COMMENT '操作人姓名',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_created_at` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单操作日志表';

-- 5.4 订单配送信息表（ord_shipping）
DROP TABLE IF EXISTS `ord_shipping`;
CREATE TABLE `ord_shipping` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `shipping_company` VARCHAR(64) DEFAULT NULL COMMENT '物流公司名称',
    `shipping_company_code` VARCHAR(32) DEFAULT NULL COMMENT '物流公司代码',
    `shipping_no` VARCHAR(64) DEFAULT NULL COMMENT '物流单号',
    `shipping_time` DATETIME(3) DEFAULT NULL COMMENT '发货时间',
    `expected_arrival_time` DATETIME(3) DEFAULT NULL COMMENT '预计到达时间',
    `actual_arrival_time` DATETIME(3) DEFAULT NULL COMMENT '实际到达时间',
    `tracking_url` VARCHAR(255) DEFAULT NULL COMMENT '物流跟踪URL',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    INDEX `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单配送信息表';

-- =============================================
-- 预设数据
-- =============================================

-- 1. 预设省市区地区
INSERT INTO `sys_region` (`province_code`, `province_name`, `city_code`, `city_name`, `district_code`, `district_name`) VALUES
('440000', '广东省', '440300', '深圳市', '440305', '南山区'),
('110000', '北京市', '110100', '北京市', '110105', '朝阳区'),
('310000', '上海市', '310100', '上海市', '310115', '浦东新区');

-- 2. 预设系统用户（超级管理员，密码：123456，BCrypt加密）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `phone`, `status`, `role`) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '超级管理员', '13900139000', 1, 'SUPER_ADMIN');

-- 3. 预设会员等级
INSERT INTO `mem_level` (`level_name`, `min_points`, `discount`, `points_ratio`, `sort`) VALUES
('普通会员', 0, 1.00, 1.00, 1),
('黄金会员', 1000, 0.95, 1.20, 2),
('铂金会员', 5000, 0.90, 1.50, 3);

-- 4. 预设商品分类
INSERT INTO `goods_category` (`parent_id`, `category_name`, `category_sort`) VALUES
(0, '数码电子', 1),
(0, '服饰鞋包', 2),
(0, '美妆个护', 3),
(1, '手机', 1),
(1, '笔记本电脑', 2);

-- 5. 预设商品基础
INSERT INTO `goods_base` (`category_id`, `goods_name`, `goods_subtitle`, `main_image`, `unit`, `brand`, `status`, `is_hot`, `is_new`, `is_recommend`, `sort`) VALUES
(4, 'Apple iPhone 15 (A3092)', '全新A17 Pro芯片，钛金属设计，USB-C接口', 'https://example.com/images/iphone15/main.jpg', '台', 'Apple', 1, 1, 1, 1, 1);

-- 6. 预设商品规格
INSERT INTO `goods_spec` (`goods_id`, `spec_name`, `spec_sort`) VALUES
(1, '颜色', 1),
(1, '存储容量', 2);

-- 7. 预设商品规格值
INSERT INTO `goods_spec_value` (`spec_id`, `spec_value`, `spec_value_sort`) VALUES
(1, '黑色', 1),
(1, '白色', 2),
(2, '128GB', 1),
(2, '256GB', 2);

-- 8. 预设商品SKU
INSERT INTO `goods_sku` (`goods_id`, `category_id`, `spec_ids`, `spec_values`, `price`, `original_price`, `cost_price`, `sales_count`) VALUES
(1, 4, '1,3', '黑色,128GB', 5999.00, 6499.00, 5500.00, 100),
(1, 4, '1,4', '黑色,256GB', 6999.00, 7499.00, 6400.00, 80),
(1, 4, '2,3', '白色,128GB', 5999.00, 6499.00, 5500.00, 120),
(1, 4, '2,4', '白色,256GB', 6999.00, 7499.00, 6400.00, 90);

-- 9. 预设仓库
INSERT INTO `sys_warehouse` (`warehouse_name`, `warehouse_code`, `province_code`, `city_code`, `district_code`, `detail_address`, `full_address`, `contact_name`, `contact_phone`) VALUES
('深圳中心仓库', 'SZ-CENTRAL', '440000', '440300', '440305', '科技园南区xx物流园A栋', '广东省深圳市南山区科技园南区xx物流园A栋', '李仓管', '13700137000');

-- 10. 预设库存
INSERT INTO `inv_stock` (`sku_id`, `warehouse_id`, `available_stock`, `frozen_stock`, `total_stock`) VALUES
(1, 1, 100, 0, 100),
(2, 1, 80, 0, 80),
(3, 1, 120, 0, 120),
(4, 1, 90, 0, 90);

-- 11. 预设库存变动记录（入库）
INSERT INTO `inv_stock_record` (`sku_id`, `warehouse_id`, `change_type`, `change_stock`, `before_available`, `after_available`, `before_frozen`, `after_frozen`, `before_total`, `after_total`, `related_order_no`, `related_type`, `operator_id`, `operator_type`, `operator_name`, `remark`) VALUES
(1, 1, 'INBOUND', 100, 0, 100, 0, 0, 0, 100, 'PO202406010001', 'PURCHASE', 1, 'SYS', 'admin', '首批iPhone15黑色128G入库'),
(2, 1, 'INBOUND', 80, 0, 80, 0, 0, 0, 80, 'PO202406010001', 'PURCHASE', 1, 'SYS', 'admin', '首批iPhone15黑色256G入库'),
(3, 1, 'INBOUND', 120, 0, 120, 0, 0, 0, 120, 'PO202406010001', 'PURCHASE', 1, 'SYS', 'admin', '首批iPhone15白色128G入库'),
(4, 1, 'INBOUND', 90, 0, 90, 0, 0, 0, 90, 'PO202406010001', 'PURCHASE', 1, 'SYS', 'admin', '首批iPhone15白色256G入库');

-- 12. 预设会员用户（测试用户，密码：123456，BCrypt加密）
INSERT INTO `mem_user` (`mem_level_id`, `username`, `password`, `nickname`, `phone`, `total_points`, `available_points`, `status`) VALUES
(1, 'test', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '测试用户', '13800138000', 500, 500, 1);

-- 13. 预设会员收货地址
INSERT INTO `mem_address` (`mem_user_id`, `consignee`, `phone`, `province_code`, `city_code`, `district_code`, `detail_address`, `full_address`, `is_default`) VALUES
(1, '张三', '13800138000', '440000', '440300', '440305', '科技园南区腾讯大厦36层', '广东省深圳市南山区科技园南区腾讯大厦36层', 1);

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 初始化完成
-- =============================================
```