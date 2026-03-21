===FILE:back/tongquyouyi.sql===
```sql
-- =====================================================
-- 数据库：tongquyouyi_db
-- 版本：MySQL 8.0.35
-- 字符集：utf8mb4
-- 存储引擎：InnoDB
-- 功能：童趣游艺综合管理系统数据库初始化脚本
-- 作者：代码生成助手
-- 创建时间：202X-XX-XX
-- =====================================================

-- 1. 初始化环境配置
SET NAMES utf8mb4 COLLATE utf8mb4_general_ci;
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- 2. 创建数据库（若不存在）
CREATE DATABASE IF NOT EXISTS `tongquyouyi_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '童趣游艺综合管理系统数据库';
USE `tongquyouyi_db`;

-- 3. 公共表（系统配置、字典、操作日志）
-- 3.1 系统配置表
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` VARCHAR(128) NOT NULL COMMENT '配置键（唯一）',
  `config_value` TEXT NOT NULL COMMENT '配置值',
  `config_desc` VARCHAR(256) DEFAULT NULL COMMENT '配置说明',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0-否 1-是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统配置表';

-- 3.2 字典类型表
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type_code` VARCHAR(64) NOT NULL COMMENT '字典类型编码（唯一）',
  `type_name` VARCHAR(128) NOT NULL COMMENT '字典类型名称',
  `type_desc` VARCHAR(256) DEFAULT NULL COMMENT '字典类型说明',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0-否 1-是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_code` (`type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典类型表';

-- 3.3 字典数据表
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type_id` BIGINT UNSIGNED NOT NULL COMMENT '字典类型ID',
  `dict_code` VARCHAR(64) NOT NULL COMMENT '字典数据编码',
  `dict_value` VARCHAR(128) NOT NULL COMMENT '字典数据值',
  `dict_sort` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序号（从小到大）',
  `dict_desc` VARCHAR(256) DEFAULT NULL COMMENT '字典数据说明',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0-否 1-是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type_id` (`type_id`),
  KEY `idx_dict_code` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典数据表';

-- 3.4 操作日志表
DROP TABLE IF EXISTS `sys_operate_log`;
CREATE TABLE `sys_operate_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `operator_id` BIGINT UNSIGNED NOT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(64) NOT NULL COMMENT '操作人姓名',
  `operate_module` VARCHAR(64) NOT NULL COMMENT '操作模块',
  `operate_type` VARCHAR(32) NOT NULL COMMENT '操作类型',
  `operate_desc` VARCHAR(512) DEFAULT NULL COMMENT '操作描述',
  `request_url` VARCHAR(256) DEFAULT NULL COMMENT '请求URL',
  `request_method` VARCHAR(16) DEFAULT NULL COMMENT '请求方法',
  `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
  `response_result` TEXT DEFAULT NULL COMMENT '响应结果',
  `ip_address` VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
  `operate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_operate_module` (`operate_module`),
  KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

-- 4. 会员管理表
-- 4.1 会员等级表
DROP TABLE IF EXISTS `member_level`;
CREATE TABLE `member_level` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `level_code` VARCHAR(32) NOT NULL COMMENT '等级编码（唯一）',
  `level_name` VARCHAR(64) NOT NULL COMMENT '等级名称',
  `level_sort` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序号（从小到大，值越大等级越高）',
  `min_integral` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '最低累计积分',
  `discount_rate` DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '折扣率（0.01-1.00，1.00不打折）',
  `birthday_bonus` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '生日礼金',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0-否 1-是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level_code` (`level_code`),
  KEY `idx_level_sort` (`level_sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员等级表';

-- 4.2 会员信息表
DROP TABLE IF EXISTS `member_info`;
CREATE TABLE `member_info` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_no` VARCHAR(32) NOT NULL COMMENT '会员编号（唯一，系统生成）',
  `member_name` VARCHAR(64) NOT NULL COMMENT '会员姓名',
  `phone` VARCHAR(16) NOT NULL COMMENT '手机号（唯一）',
  `id_card` VARCHAR(32) DEFAULT NULL COMMENT '身份证号',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `gender` TINYINT UNSIGNED DEFAULT NULL COMMENT '性别（0-未知 1-男 2-女）',
  `level_id` BIGINT UNSIGNED NOT NULL COMMENT '会员等级ID',
  `current_integral` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '当前可用积分',
  `total_integral` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计获得积分',
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `avatar_url` VARCHAR(256) DEFAULT NULL COMMENT '头像URL',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0-禁用 1-正常）',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0-否 1-是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_no` (`member_no`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_level_id` (`level_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员信息表';

-- 4.3 会员收货地址表
DROP TABLE IF EXISTS `member_address`;
CREATE TABLE `member_address` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `receiver_name` VARCHAR(64) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(16) NOT NULL COMMENT '收货人手机号',
  `province_code` VARCHAR(16) NOT NULL COMMENT '省编码',
  `province_name` VARCHAR(64) NOT NULL COMMENT '省名称',
  `city_code` VARCHAR(16) NOT NULL COMMENT '市编码',
  `city_name` VARCHAR(64) NOT NULL COMMENT '市名称',
  `district_code` VARCHAR(16) DEFAULT NULL COMMENT '区/县编码',
  `district_name` VARCHAR(64) DEFAULT NULL COMMENT '区/县名称',
  `detail_address` VARCHAR(256) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否默认（0-否 1-是）',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0-否 1-是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员收货地址表';

-- 4.4 会员积分变动日志表
DROP TABLE IF EXISTS `member_integral_log`;
CREATE TABLE `member_integral_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `integral_type` TINYINT UNSIGNED NOT NULL COMMENT '积分类型（1-消费获得 2-签到获得 3-活动赠送 4-积分消费 5-积分过期 6-积分退款）',
  `change_amount` BIGINT NOT NULL COMMENT '变动积分（正增加负减少）',
  `before_integral` BIGINT UNSIGNED NOT NULL COMMENT '变动前积分',
  `after_integral` BIGINT UNSIGNED NOT NULL COMMENT '变动后积分',
  `related_order_no` VARCHAR(32) DEFAULT NULL COMMENT '关联订单号',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_related_order_no` (`related_order_no`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员积分变动日志表';

-- 5. 商品管理表
-- 5.1 商品分类表
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类ID（0表示顶级分类）',
  `category_code` VARCHAR(64) NOT NULL COMMENT '分类编码（唯一）',
  `category_name` VARCHAR(128) NOT NULL COMMENT '分类名称',
  `category_icon` VARCHAR(256) DEFAULT NULL COMMENT '分类图标URL',
  `category_sort` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序号（从小到大）',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0-否 1-是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_category_sort` (`category_sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品分类表';

-- 5.2 商品信息表
DROP TABLE IF EXISTS `product_info`;
CREATE TABLE `product_info` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_no` VARCHAR(32) NOT NULL COMMENT '商品编号（唯一，系统生成）',
  `product_name` VARCHAR(256) NOT NULL COMMENT '商品名称',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
  `brand` VARCHAR(128) DEFAULT NULL COMMENT '品牌',
  `product_desc` TEXT DEFAULT NULL COMMENT '商品简介',
  `main_image` VARCHAR(256) NOT NULL COMMENT '主图URL',
  `original_price` DECIMAL(10,2) NOT NULL COMMENT '原价',
  `sale_price` DECIMAL(10,2) NOT NULL COMMENT '销售价',
  `integral_ratio` DECIMAL(5,2) NOT NULL DEFAULT 1.00 COMMENT '积分获取比例（每消费1元获得的积分）',
  `max_integral_deduct` DECIMAL(3,2) NOT NULL DEFAULT 0.30 COMMENT '最大积分抵扣比例（0.00-1.00）',
  `stock_warning` INT UNSIGNED NOT NULL DEFAULT 10 COMMENT '库存预警值',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0-下架 1-上架）',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0-否 1-是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_no` (`product_no`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sale_price` (`sale_price`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品信息表';

-- 5.3 商品详情表
DROP TABLE IF EXISTS `product_detail`;
CREATE TABLE `product_detail` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `product_params` JSON DEFAULT NULL COMMENT '商品规格参数（JSON格式）',
  `product_content` TEXT NOT NULL COMMENT '商品详情内容（富文本）',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0-否 1-是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品详情表';

-- 5.4 商品图片表
DROP TABLE IF EXISTS `product_image`;
CREATE TABLE `product_image` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `image_url` VARCHAR(256) NOT NULL COMMENT '图片URL',
  `image_sort` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序号（从小到大，主图对应0）',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0-否 1-是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_image_sort` (`image_sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品图片表';

-- 6. 库存管理表
-- 6.1 库存信息表
DROP TABLE IF EXISTS `stock_info`;
CREATE TABLE `stock_info` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `specification` JSON DEFAULT NULL COMMENT '商品规格（JSON格式，非规格商品可留空）',
  `available_stock` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '可用库存',
  `frozen_stock` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '冻结库存',
  `total_stock` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '总库存',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0-否 1-是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_spec` (`product_id`, `specification`(256)),
  KEY `idx_available_stock` (`available_stock`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='库存信息表';

-- 6.2 库存变动日志表
DROP TABLE IF EXISTS `stock_log`;
CREATE TABLE `stock_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `stock_id` BIGINT UNSIGNED NOT NULL COMMENT '库存ID',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `specification` JSON DEFAULT NULL COMMENT '商品规格',
  `stock_type` TINYINT UNSIGNED NOT NULL COMMENT '库存类型（1-入库 2-出库 3-调拨 4-盘点调整 5-订单冻结 6-订单解冻 7-退货入库）',
  `change_amount` INT NOT NULL COMMENT '变动库存（正增加负减少）',
  `before_stock` INT UNSIGNED NOT NULL COMMENT '变动前可用库存',
  `after_stock` INT UNSIGNED NOT NULL COMMENT '变动后可用库存',
  `related_order_no` VARCHAR(32) DEFAULT NULL COMMENT '关联单号（订单号/入库单号/盘点单号等）',
  `operator_id` BIGINT UNSIGNED NOT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(64) NOT NULL COMMENT '操作人姓名',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_stock_id` (`stock_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_related_order_no` (`related_order_no`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='库存变动日志表';

-- 7. 订单管理表
-- 7.1 订单信息表
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号（唯一，系统生成）',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `member_name` VARCHAR(64) NOT NULL COMMENT '会员姓名',
  `member_phone` VARCHAR(16) NOT NULL COMMENT '会员手机号',
  `receiver_name` VARCHAR(64) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(16) NOT NULL COMMENT '收货人手机号',
  `receiver_address` VARCHAR(512) NOT NULL COMMENT '收货地址（拼接完整地址）',
  `total_original_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品总原价',
  `total_sale_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品总销售价',
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额（会员折扣+优惠券+积分抵扣）',
  `member_discount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '会员折扣金额',
  `integral_deduct` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '积分抵扣金额',
  `integral_used` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用积分',
  `actual_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
  `pay_type` TINYINT UNSIGNED DEFAULT NULL COMMENT '支付方式（1-微信 2-支付宝 3-余额 4-组合支付）',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `order_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单状态（0-待支付 1-待发货 2-待收货 3-已完成 4-已取消 5-售后中）',
  `cancel_reason` VARCHAR(256) DEFAULT NULL COMMENT '取消原因',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `delivery_company` VARCHAR(128) DEFAULT NULL COMMENT '快递公司',
  `delivery_no` VARCHAR(64) DEFAULT NULL COMMENT '快递单号',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0-否 1-是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_pay_time` (`pay_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单信息表';

-- 7.2 订单详情表
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `product_no` VARCHAR(32) NOT NULL COMMENT '商品编号',
  `product_name` VARCHAR(256) NOT NULL COMMENT '商品名称',
  `product_image` VARCHAR(256) NOT NULL COMMENT '商品主图URL',
  `specification` JSON DEFAULT NULL COMMENT '商品规格',
  `original_price` DECIMAL(10,2) NOT NULL COMMENT '商品原价',
  `sale_price` DECIMAL(10,2) NOT NULL COMMENT '商品销售价',
  `discount_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品优惠单价',
  `quantity` INT UNSIGNED NOT NULL COMMENT '购买数量',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '商品小计金额',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单详情表';

-- 7.3 订单操作日志表
DROP TABLE IF EXISTS `order_log`;
CREATE TABLE `order_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID（NULL表示系统自动操作）',
  `operator_name` VARCHAR(64) NOT NULL COMMENT '操作人姓名（系统自动操作填“系统”）',
  `operate_type` TINYINT UNSIGNED NOT NULL COMMENT '操作类型（0-创建订单 1-支付成功 2-发货 3-收货 4-取消订单 5-申请售后 6-完成售后）',
  `operate_content` VARCHAR(512) DEFAULT NULL COMMENT '操作内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单操作日志表';

-- 7.4 退款申请表
DROP TABLE IF EXISTS `order_refund`;
CREATE TABLE `order_refund` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `refund_no` VARCHAR(32) NOT NULL COMMENT '退款单号（唯一，系统生成）',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `refund_type` TINYINT UNSIGNED NOT NULL COMMENT '退款类型（1-仅退款 2-退货退款）',
  `refund_reason` VARCHAR(256) NOT NULL COMMENT '退款原因',
  `refund_desc` TEXT DEFAULT NULL COMMENT '退款说明',
  `refund_images` JSON DEFAULT NULL COMMENT '退款图片URL数组',
  `refund_amount` DECIMAL(10,2) NOT NULL COMMENT '申请退款金额',
  `return_integral` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '申请退还积分',
  `audit_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '审核状态（0-待审核 1-审核通过 2-审核拒绝）',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `audit_operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人ID',
  `audit_operator_name` VARCHAR(64) DEFAULT NULL COMMENT '审核人姓名',
  `audit_remark` VARCHAR(256) DEFAULT NULL COMMENT '审核备注',
  `delivery_company` VARCHAR(128) DEFAULT NULL COMMENT '退货快递公司',
  `delivery_no` VARCHAR(64) DEFAULT NULL COMMENT '退货快递单号',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '退货发货时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '退货收货时间',
  `refund_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '退款状态（0-待退款 1-退款中 2-退款成功 3-退款失败）',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款完成时间',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0-否 1-是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_refund_status` (`refund_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='退款申请表';

-- 8. 恢复环境配置
SET FOREIGN_KEY_CHECKS = 1;

-- 9. 预设数据
-- 9.1 系统配置预设
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_desc`) VALUES
('system.name', '童趣游艺综合管理系统', '系统名称'),
('system.logo', '/static/logo.png', '系统LogoURL'),
('integral.expire_days', '365', '积分有效期（天）'),
('integral.exchange_rate', '100', '积分兑换比例（100积分=1元）'),
('order.pay_timeout', '1800', '订单支付超时时间（秒，默认30分钟）'),
('order.auto_receive_days', '7', '订单自动收货时间（天）'),
('order.auto_complete_days', '15', '订单自动完成时间（天）');

-- 9.2 字典类型预设
INSERT INTO `sys_dict_type` (`type_code`, `type_name`, `type_desc`) VALUES
('pay_type', '支付方式', '订单支付方式字典'),
('product_status', '商品状态', '商品上下架状态字典'),
('order_status', '订单状态', '订单全流程状态字典'),
('refund_type', '退款类型', '退款申请类型字典'),
('audit_status', '审核状态', '通用审核状态字典'),
('refund_status', '退款状态', '退款全流程状态字典');

-- 9.3 字典数据预设
INSERT INTO `sys_dict_data` (`type_id`, `dict_code`, `dict_value`, `dict_sort`, `dict_desc`) VALUES
-- 支付方式（type_id=1）
(1, 'WECHAT', '微信支付', 1, '微信公众号/小程序/APP支付'),
(1, 'ALIPAY', '支付宝支付', 2, '支付宝APP/网页支付'),
(1, 'BALANCE', '余额支付', 3, '会员账户余额支付'),
(1, 'COMBINATION', '组合支付', 4, '多种支付方式组合'),
-- 商品状态（type_id=2）
(2, 'OFF_SHELF', '下架', 0, '商品下架状态'),
(2, 'ON_SHELF', '上架', 1, '商品上架状态'),
-- 订单状态（type_id=3）
(3, 'WAIT_PAY', '待支付', 0, '订单创建后未支付'),
(3, 'WAIT_DELIVERY', '待发货', 1, '支付成功后未发货'),
(3, 'WAIT_RECEIVE', '待收货', 2, '发货后未确认收货'),
(3, 'COMPLETED', '已完成', 3, '收货确认或自动收货后'),
(3, 'CANCELED', '已取消', 4, '超时或主动取消'),
(3, 'AFTER_SALE', '售后中', 5, '已提交退款申请'),
-- 退款类型（type_id=4）
(4, 'ONLY_REFUND', '仅退款', 1, '未发货或无需退货退款'),
(4, 'RETURN_REFUND', '退货退款', 2, '需退货后退款'),
-- 审核状态（type_id=5）
(5, 'WAIT_AUDIT', '待审核', 0, '未审核状态'),
(5, 'PASSED', '审核通过', 1, '审核通过状态'),
(5, 'REJECTED', '审核拒绝', 2, '审核拒绝状态'),
-- 退款状态（type_id=6）
(6, 'WAIT_REFUND', '待退款', 0, '审核通过后未退款'),
(6, 'REFUNDING', '退款中', 1, '第三方支付处理中'),
(6, 'REFUNDED', '退款成功', 2, '退款到账'),
(6, 'FAILED', '退款失败', 3, '第三方支付退款失败');

-- 9.4 会员等级预设
INSERT INTO `member_level` (`level_code`, `level_name`, `level_sort`, `min_integral`, `discount_rate`, `birthday_bonus`) VALUES
('LV1', '注册会员', 1, 0, 1.00, 0.00),
('LV2', '白银会员', 2, 1000, 0.95, 10.00),
('LV3', '黄金会员', 3, 5000, 0.90, 30.00),
('LV4', '铂金会员', 4, 20000, 0.85, 80.00),
('LV5', '钻石会员', 5, 100000, 0.80, 200.00);

-- 9.5 商品分类预设
INSERT INTO `product_category` (`parent_id`, `category_code`, `category_name`, `category_sort`) VALUES
(0, 'TOYS', '玩具', 1),
(0, 'STATIONERY', '文具', 2),
(0, 'CHILDREN_CLOTHES', '童装', 3),
(1, 'PLUSH_TOYS', '毛绒玩具', 1),
(1, 'INTELLIGENT_TOYS', '益智玩具', 2),
(2, 'SCHOOL_SUPPLIES', '学生用品', 1),
(2, 'ART_SUPPLIES', '美术用品', 2);
```