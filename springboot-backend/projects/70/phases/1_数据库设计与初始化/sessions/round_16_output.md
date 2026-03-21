===FILE:back/tongquyouyi.sql===
```sql
-- =============================================
-- 同趣优亿商城数据库初始化脚本
-- 数据库版本: MySQL 8.0.35+
-- 存储引擎: InnoDB
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_0900_ai_ci
-- 创建时间: 202X-XX-XX
-- =============================================

-- 1. 设置全局参数（可选，但更规范）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET DEFAULT_STORAGE_ENGINE = InnoDB;

-- 2. 创建数据库（如果不存在）
DROP DATABASE IF EXISTS `tongquyouyi`;
CREATE DATABASE `tongquyouyi` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_0900_ai_ci
COMMENT '同趣优亿商城数据库';

USE `tongquyouyi`;

-- =============================================
-- 模块1：公共系统表
-- =============================================

-- 1.1 系统字典表
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_type` VARCHAR(64) NOT NULL COMMENT '字典类型',
  `dict_code` VARCHAR(64) NOT NULL COMMENT '字典编码',
  `dict_name` VARCHAR(128) NOT NULL COMMENT '字典名称',
  `dict_value` VARCHAR(256) DEFAULT NULL COMMENT '字典值',
  `sort` INT UNSIGNED DEFAULT 0 COMMENT '排序号（从小到大）',
  `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_code` (`dict_type`, `dict_code`, `deleted`),
  KEY `idx_type` (`dict_type`),
  KEY `idx_status` (`status`)
) COMMENT='系统字典表';

-- 1.2 系统操作日志表
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `oper_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作用户ID',
  `oper_user_name` VARCHAR(64) DEFAULT NULL COMMENT '操作用户名',
  `oper_module` VARCHAR(128) NOT NULL COMMENT '操作模块',
  `oper_type` VARCHAR(32) NOT NULL COMMENT '操作类型（增删改查等）',
  `oper_desc` VARCHAR(512) DEFAULT NULL COMMENT '操作描述',
  `request_url` VARCHAR(512) DEFAULT NULL COMMENT '请求URL',
  `request_method` VARCHAR(16) DEFAULT NULL COMMENT '请求方法（GET/POST等）',
  `request_ip` VARCHAR(64) DEFAULT NULL COMMENT '请求IP',
  `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
  `response_result` TEXT DEFAULT NULL COMMENT '响应结果',
  `cost_time` BIGINT UNSIGNED DEFAULT 0 COMMENT '耗时（毫秒）',
  `oper_status` TINYINT UNSIGNED DEFAULT 1 COMMENT '操作状态：0-失败 1-成功',
  `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_oper_user_id` (`oper_user_id`),
  KEY `idx_oper_module` (`oper_module`),
  KEY `idx_create_time` (`create_time`)
) COMMENT='系统操作日志表';

-- =============================================
-- 模块2：会员管理表
-- =============================================

-- 2.1 会员基础信息表
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(64) NOT NULL COMMENT '会员账号（唯一）',
  `password` VARCHAR(256) NOT NULL COMMENT '密码（加密存储）',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '会员昵称',
  `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
  `mobile` VARCHAR(16) NOT NULL COMMENT '手机号（唯一）',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `avatar_url` VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
  `gender` TINYINT UNSIGNED DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `member_level` TINYINT UNSIGNED DEFAULT 1 COMMENT '会员等级：1-普通 2-银卡 3-金卡 4-钻石',
  `points` BIGINT UNSIGNED DEFAULT 0 COMMENT '积分余额',
  `balance` DECIMAL(12,2) DEFAULT 0.00 COMMENT '账户余额（元）',
  `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
  `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`, `deleted`),
  UNIQUE KEY `uk_mobile` (`mobile`, `deleted`),
  KEY `idx_member_level` (`member_level`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) COMMENT='会员基础信息表';

-- 2.2 会员收货地址表
DROP TABLE IF EXISTS `member_address`;
CREATE TABLE `member_address` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `consignee_name` VARCHAR(64) NOT NULL COMMENT '收货人姓名',
  `consignee_mobile` VARCHAR(16) NOT NULL COMMENT '收货人手机号',
  `province_code` VARCHAR(12) NOT NULL COMMENT '省份编码',
  `province_name` VARCHAR(64) NOT NULL COMMENT '省份名称',
  `city_code` VARCHAR(12) NOT NULL COMMENT '城市编码',
  `city_name` VARCHAR(64) NOT NULL COMMENT '城市名称',
  `district_code` VARCHAR(12) NOT NULL COMMENT '区县编码',
  `district_name` VARCHAR(64) NOT NULL COMMENT '区县名称',
  `detail_address` VARCHAR(256) NOT NULL COMMENT '详细地址',
  `postal_code` VARCHAR(10) DEFAULT NULL COMMENT '邮政编码',
  `is_default` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否默认：0-否 1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_is_default` (`is_default`)
) COMMENT='会员收货地址表';

-- =============================================
-- 模块3：商品管理表
-- =============================================

-- 3.1 商品分类表
DROP TABLE IF EXISTS `goods_category`;
CREATE TABLE `goods_category` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT UNSIGNED DEFAULT 0 COMMENT '父分类ID（0为顶级分类）',
  `category_name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `category_icon` VARCHAR(512) DEFAULT NULL COMMENT '分类图标URL',
  `sort` INT UNSIGNED DEFAULT 0 COMMENT '排序号（从小到大）',
  `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort` (`sort`),
  KEY `idx_status` (`status`)
) COMMENT='商品分类表';

-- 3.2 商品SPU表（标准化产品单元）
DROP TABLE IF EXISTS `goods_spu`;
CREATE TABLE `goods_spu` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `spu_no` VARCHAR(64) NOT NULL COMMENT 'SPU编号（唯一）',
  `goods_name` VARCHAR(256) NOT NULL COMMENT '商品名称',
  `goods_subtitle` VARCHAR(512) DEFAULT NULL COMMENT '商品副标题',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '商品分类ID（末级）',
  `brand_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '品牌ID（预留）',
  `main_image_url` VARCHAR(512) NOT NULL COMMENT '主图URL',
  `image_urls` TEXT DEFAULT NULL COMMENT '商品轮播图URL列表（JSON格式）',
  `detail_desc` TEXT DEFAULT NULL COMMENT '商品详情描述（HTML/Markdown）',
  `unit` VARCHAR(32) DEFAULT '件' COMMENT '商品单位',
  `price_range` VARCHAR(64) DEFAULT NULL COMMENT '价格区间（元，冗余字段）',
  `stock_total` BIGINT UNSIGNED DEFAULT 0 COMMENT '总库存（冗余字段）',
  `sale_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '销量',
  `view_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '浏览量',
  `is_recommend` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否推荐：0-否 1-是',
  `is_new` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否新品：0-否 1-是',
  `is_hot` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否热卖：0-否 1-是',
  `status` TINYINT UNSIGNED DEFAULT 0 COMMENT '状态：0-待上架 1-已上架 2-已下架 3-已售罄',
  `publish_time` DATETIME DEFAULT NULL COMMENT '上架时间',
  `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_no` (`spu_no`, `deleted`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_sale_count` (`sale_count`),
  KEY `idx_view_count` (`view_count`),
  KEY `idx_status` (`status`),
  KEY `idx_is_recommend` (`is_recommend`),
  KEY `idx_create_time` (`create_time`)
) COMMENT='商品SPU表';

-- 3.3 商品SKU表（库存保有单元）
DROP TABLE IF EXISTS `goods_sku`;
CREATE TABLE `goods_sku` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sku_no` VARCHAR(64) NOT NULL COMMENT 'SKU编号（唯一）',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT '关联SPU ID',
  `sku_specs` VARCHAR(512) NOT NULL COMMENT 'SKU规格（JSON格式：[{"spec_name":"颜色","spec_value":"黑色"},{"spec_name":"容量","spec_value":"256G"}]）',
  `specs_simple` VARCHAR(256) DEFAULT NULL COMMENT '规格简名（冗余：黑色+256G）',
  `sku_image_url` VARCHAR(512) DEFAULT NULL COMMENT 'SKU图片URL',
  `original_price` DECIMAL(12,2) NOT NULL COMMENT '原价（元）',
  `sale_price` DECIMAL(12,2) NOT NULL COMMENT '售价（元）',
  `cost_price` DECIMAL(12,2) DEFAULT NULL COMMENT '成本价（元，仅后台可见）',
  `stock` BIGINT UNSIGNED DEFAULT 0 COMMENT '当前库存',
  `lock_stock` BIGINT UNSIGNED DEFAULT 0 COMMENT '锁定库存（下单未付款/未发货）',
  `weight` DECIMAL(8,3) DEFAULT NULL COMMENT '重量（kg）',
  `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_no` (`sku_no`, `deleted`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_sale_price` (`sale_price`),
  KEY `idx_status` (`status`)
) COMMENT='商品SKU表';

-- =============================================
-- 模块4：库存管理表
-- =============================================

-- 4.1 库存主表（实时库存汇总）
-- 注：简单场景可合并到goods_sku，但拆分更规范，便于后续多仓库扩展
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_id` BIGINT UNSIGNED DEFAULT 1 COMMENT '仓库ID（默认1：主仓）',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `current_stock` BIGINT UNSIGNED DEFAULT 0 COMMENT '当前可用库存',
  `locked_stock` BIGINT UNSIGNED DEFAULT 0 COMMENT '锁定库存',
  `total_in` BIGINT UNSIGNED DEFAULT 0 COMMENT '累计入库',
  `total_out` BIGINT UNSIGNED DEFAULT 0 COMMENT '累计出库',
  `last_in_time` DATETIME DEFAULT NULL COMMENT '最后入库时间',
  `last_out_time` DATETIME DEFAULT NULL COMMENT '最后出库时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_sku` (`warehouse_id`, `sku_id`),
  KEY `idx_spu_id` (`spu_id`)
) COMMENT='库存主表';

-- 4.2 库存出入库流水表
DROP TABLE IF EXISTS `inventory_flow`;
CREATE TABLE `inventory_flow` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `flow_no` VARCHAR(64) NOT NULL COMMENT '流水编号（唯一）',
  `warehouse_id` BIGINT UNSIGNED DEFAULT 1 COMMENT '仓库ID',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `flow_type` TINYINT UNSIGNED NOT NULL COMMENT '流水类型：1-采购入库 2-退货入库 3-销售出库 4-赠品出库 5-调拨出库 6-盘点调整',
  `flow_direction` TINYINT UNSIGNED NOT NULL COMMENT '出入方向：1-入库 2-出库',
  `related_no` VARCHAR(64) DEFAULT NULL COMMENT '关联单号（采购单号/订单号/盘点单号等）',
  `change_num` BIGINT NOT NULL COMMENT '变动数量（正数为增，负数为减）',
  `before_num` BIGINT UNSIGNED NOT NULL COMMENT '变动前库存',
  `after_num` BIGINT UNSIGNED NOT NULL COMMENT '变动后库存',
  `oper_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作用户ID',
  `oper_user_name` VARCHAR(64) DEFAULT NULL COMMENT '操作用户名',
  `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_flow_no` (`flow_no`),
  KEY `idx_warehouse_sku` (`warehouse_id`, `sku_id`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_flow_type` (`flow_type`),
  KEY `idx_related_no` (`related_no`),
  KEY `idx_create_time` (`create_time`)
) COMMENT='库存出入库流水表';

-- =============================================
-- 模块5：订单管理表
-- =============================================

-- 5.1 订单主表
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号（唯一）',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `member_username` VARCHAR(64) DEFAULT NULL COMMENT '会员账号（冗余）',
  `consignee_name` VARCHAR(64) NOT NULL COMMENT '收货人姓名',
  `consignee_mobile` VARCHAR(16) NOT NULL COMMENT '收货人手机号',
  `consignee_full_address` VARCHAR(512) NOT NULL COMMENT '收货人完整地址',
  `postal_code` VARCHAR(10) DEFAULT NULL COMMENT '邮政编码',
  `total_quantity` INT UNSIGNED NOT NULL COMMENT '商品总数量',
  `total_amount` DECIMAL(12,2) NOT NULL COMMENT '订单总金额（元）',
  `freight_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '运费金额（元）',
  `discount_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '优惠金额（元）',
  `payable_amount` DECIMAL(12,2) NOT NULL COMMENT '应付金额（元）',
  `paid_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '实付金额（元）',
  `pay_type` TINYINT UNSIGNED DEFAULT NULL COMMENT '支付方式：1-余额支付 2-微信支付 3-支付宝支付 4-银行卡支付',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `order_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单状态：0-待付款 1-待发货 2-已发货 3-已签收 4-已完成 5-已取消 6-售后中',
  `delivery_type` TINYINT UNSIGNED DEFAULT NULL COMMENT '配送方式：1-快递 2-自提',
  `delivery_no` VARCHAR(64) DEFAULT NULL COMMENT '物流单号',
  `delivery_company` VARCHAR(64) DEFAULT NULL COMMENT '物流公司',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '签收时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(256) DEFAULT NULL COMMENT '取消原因',
  `member_remark` VARCHAR(512) DEFAULT NULL COMMENT '会员备注',
  `shop_remark` VARCHAR(512) DEFAULT NULL COMMENT '商家备注',
  `points_used` BIGINT UNSIGNED DEFAULT 0 COMMENT '使用积分',
  `points_earned` BIGINT UNSIGNED DEFAULT 0 COMMENT '获得积分',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_pay_time` (`pay_time`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_deleted` (`deleted`)
) COMMENT='订单主表';

-- 5.2 订单商品明细表
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号（冗余）',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `spu_no` VARCHAR(64) NOT NULL COMMENT 'SPU编号（冗余）',
  `goods_name` VARCHAR(256) NOT NULL COMMENT '商品名称（冗余）',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `sku_no` VARCHAR(64) NOT NULL COMMENT 'SKU编号（冗余）',
  `sku_specs` VARCHAR(512) NOT NULL COMMENT 'SKU规格（冗余）',
  `sku_image_url` VARCHAR(512) DEFAULT NULL COMMENT 'SKU图片URL（冗余）',
  `original_price` DECIMAL(12,2) NOT NULL COMMENT '原价（元，冗余）',
  `sale_price` DECIMAL(12,2) NOT NULL COMMENT '售价（元，下单时的价格）',
  `cost_price` DECIMAL(12,2) DEFAULT NULL COMMENT '成本价（元，冗余，仅后台可见）',
  `quantity` INT UNSIGNED NOT NULL COMMENT '购买数量',
  `total_amount` DECIMAL(12,2) NOT NULL COMMENT '小计金额（元）',
  `discount_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '该商品优惠金额（元）',
  `after_sale_status` TINYINT UNSIGNED DEFAULT 0 COMMENT '售后状态：0-无售后 1-售后中 2-已售后',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_sku_id` (`sku_id`)
) COMMENT='订单商品明细表';

-- 5.3 订单操作日志表
DROP TABLE IF EXISTS `order_oper_log`;
CREATE TABLE `order_oper_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号（冗余）',
  `oper_type` VARCHAR(32) NOT NULL COMMENT '操作类型',
  `oper_desc` VARCHAR(512) NOT NULL COMMENT '操作描述',
  `before_status` TINYINT UNSIGNED DEFAULT NULL COMMENT '操作前订单状态',
  `after_status` TINYINT UNSIGNED DEFAULT NULL COMMENT '操作后订单状态',
  `oper_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作用户ID（NULL表示系统操作）',
  `oper_user_name` VARCHAR(64) DEFAULT NULL COMMENT '操作用户名',
  `oper_ip` VARCHAR(64) DEFAULT NULL COMMENT '操作IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_create_time` (`create_time`)
) COMMENT='订单操作日志表';

-- =============================================
-- 模块6：预设数据（字典、测试会员、测试商品、测试库存等）
-- =============================================

-- 6.1 预设系统字典
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_name`, `dict_value`, `sort`, `status`) VALUES
-- 会员等级
('member_level', '1', '普通会员', NULL, 1, 1),
('member_level', '2', '银卡会员', NULL, 2, 1),
('member_level', '3', '金卡会员', NULL, 3, 1),
('member_level', '4', '钻石会员', NULL, 4, 1),
-- 支付方式
('pay_type', '1', '余额支付', NULL, 1, 1),
('pay_type', '2', '微信支付', NULL, 2, 1),
('pay_type', '3', '支付宝支付', NULL, 3, 1),
('pay_type', '4', '银行卡支付', NULL, 4, 1),
-- 配送方式
('delivery_type', '1', '快递', NULL, 1, 1),
('delivery_type', '2', '自提', NULL, 2, 1),
-- 库存流水类型
('inventory_flow_type', '1', '采购入库', '1', 1, 1),
('inventory_flow_type', '2', '退货入库', '1', 2, 1),
('inventory_flow_type', '3', '销售出库', '2', 3, 1),
('inventory_flow_type', '4', '赠品出库', '2', 4, 1),
('inventory_flow_type', '5', '调拨出库', '2', 5, 1),
('inventory_flow_type', '6', '盘点调整', NULL, 6, 1);

-- 6.2 预设商品分类（3级）
INSERT INTO `goods_category` (`parent_id`, `category_name`, `sort`, `status`) VALUES
-- 顶级分类
(0, '数码产品', 1, 1),
(0, '服装鞋帽', 2, 1),
(0, '食品饮料', 3, 1),
-- 数码产品二级分类
(1, '手机', 1, 1),
(1, '电脑', 2, 1),
(1, '耳机', 3, 1),
-- 服装鞋帽二级分类
(2, '男装', 1, 1),
(2, '女装', 2, 1),
(2, '鞋靴', 3, 1),
-- 手机三级分类（末级）
(4, '智能手机', 1, 1),
(4, '老人机', 2, 1);

-- 6.3 预设测试会员（密码：123456，使用BCrypt加密）
INSERT INTO `member` (`username`, `password`, `nickname`, `real_name`, `mobile`, `email`, `gender`, `member_level`, `points`, `balance`, `status`) VALUES
('test001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '测试用户001', '张三', '13800138001', 'test001@example.com', 1, 1, 1000, 500.00, 1),
('test002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '测试用户002', '李四', '13800138002', 'test002@example.com', 2, 2, 5000, 2000.00, 1);

-- 6.4 预设测试收货地址
INSERT INTO `member_address` (`member_id`, `consignee_name`, `consignee_mobile`, `province_code`, `province_name`, `city_code`, `city_name`, `district_code`, `district_name`, `detail_address`, `is_default`) VALUES
(1, '张三', '13800138001', '110000', '北京市', '110100', '北京市', '110105', '朝阳区', '建国路88号SOHO现代城A座1001室', 1),
(2, '李四', '13800138002', '310000', '上海市', '310100', '上海市', '310101', '黄浦区', '南京东路123号外滩中心B座2002室', 1);

-- 6.5 预设测试商品SPU
INSERT INTO `goods_spu` (`spu_no`, `goods_name`, `goods_subtitle`, `category_id`, `main_image_url`, `image_urls`, `detail_desc`, `unit`, `status`, `is_recommend`, `is_new`, `is_hot`) VALUES
('SPU202X0001', 'iPhone 16 Pro Max', 'A18 Pro芯片，钛金属设计，潜望式长焦镜头', 10, 'https://example.com/iphone16pm-main.jpg', '["https://example.com/iphone16pm-1.jpg","https://example.com/iphone16pm-2.jpg"]', '<h1>iPhone 16 Pro Max详细介绍</h1><p>这是一款旗舰手机</p>', '台', 1, 1, 1, 1),
('SPU202X0002', '华为Mate 70 Pro', '麒麟9100S芯片，卫星通信2.0，可变光圈', 10, 'https://example.com/huaweimate70p-main.jpg', '["https://example.com/huaweimate70p-1.jpg","https://example.com/huaweimate70p-2.jpg"]', '<h1>华为Mate 70 Pro详细介绍</h1><p>这是一款国产旗舰手机</p>', '台', 1, 1, 1, 0);

-- 6.6 预设测试商品SKU
INSERT INTO `goods_sku` (`sku_no`, `spu_id`, `sku_specs`, `specs_simple`, `sku_image_url`, `original_price`, `sale_price`, `cost_price`, `stock`) VALUES
('SKU202X0001001', 1, '[{"spec_name":"颜色","spec_value":"钛金属黑"},{"spec_name":"容量","spec_value":"256GB"}]', '钛金属黑+256GB', 'https://example.com/iphone16pm-black-256.jpg', 9999.00, 9499.00, 7500.00, 100),
('SKU202X0001002', 1, '[{"spec_name":"颜色","spec_value":"钛金属黑"},{"spec_name":"容量","spec_value":"512GB"}]', '钛金属黑+512GB', 'https://example.com/iphone16pm-black-512.jpg', 11999.00, 11499.00, 9000.00, 50),
('SKU202X0002001', 2, '[{"spec_name":"颜色","spec_value":"陶瓷黑"},{"spec_name":"容量","spec_value":"256GB"}]', '陶瓷黑+256GB', 'https://example.com/huaweimate70p-black-256.jpg', 7999.00, 7499.00, 5800.00, 200),
('SKU202X0002002', 2, '[{"spec_name":"颜色","spec_value":"陶瓷白"},{"spec_name":"容量","spec_value":"256GB"}]', '陶瓷白+256GB', 'https://example.com/huaweimate70p-white-256.jpg', 7999.00, 7499.00, 5800.00, 150);

-- 6.7 预设测试库存主表
INSERT INTO `inventory` (`warehouse_id`, `sku_id`, `spu_id`, `current_stock`, `total_in`) VALUES
(1, 1, 1, 100, 100),
(1, 2, 1, 50, 50),
(1, 3, 2, 200, 200),
(1, 4, 2, 150, 150);

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;
```