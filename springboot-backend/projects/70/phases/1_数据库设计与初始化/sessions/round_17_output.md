===FILE:back/tongquyouyi.sql===
```sql
-- =====================================================
-- 数据库名: tongquyouyi
-- 数据库版本: MySQL 8.0.35
-- 存储引擎: InnoDB
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_0900_ai_ci
-- 功能模块: 公共字典、地区、会员、商品分类/属性/SKU、库存、订单、支付
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `tongquyouyi`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci
COMMENT '童趣优益母婴用品商城数据库';

USE `tongquyouyi`;

-- =====================================================
-- 公共表1: sys_dict_type 字典类型表
-- =====================================================
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '字典类型ID',
  `dict_code` VARCHAR(64) NOT NULL COMMENT '字典类型编码',
  `dict_name` VARCHAR(64) NOT NULL COMMENT '字典类型名称',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_code` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统字典类型表';

-- =====================================================
-- 公共表2: sys_dict_data 字典数据表
-- =====================================================
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '字典数据ID',
  `dict_type_id` BIGINT UNSIGNED NOT NULL COMMENT '字典类型ID',
  `dict_label` VARCHAR(64) NOT NULL COMMENT '字典标签',
  `dict_value` VARCHAR(64) NOT NULL COMMENT '字典值',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_dict_type_id` (`dict_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统字典数据表';

-- =====================================================
-- 公共表3: sys_region 地区表（三级联动）
-- =====================================================
DROP TABLE IF EXISTS `sys_region`;
CREATE TABLE `sys_region` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地区ID',
  `code` VARCHAR(12) NOT NULL COMMENT '地区编码',
  `name` VARCHAR(64) NOT NULL COMMENT '地区名称',
  `parent_code` VARCHAR(12) DEFAULT '0' COMMENT '父级编码',
  `level` TINYINT DEFAULT 1 COMMENT '地区级别:1-省,2-市,3-区/县',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_region_code` (`code`),
  KEY `idx_parent_code` (`parent_code`),
  KEY `idx_region_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统地区表';

-- =====================================================
-- 会员管理表1: ums_member 会员基础表
-- =====================================================
DROP TABLE IF EXISTS `ums_member`;
CREATE TABLE `ums_member` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  `username` VARCHAR(64) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `phone` VARCHAR(16) NOT NULL COMMENT '手机号',
  `nickname` VARCHAR(64) DEFAULT '' COMMENT '昵称',
  `avatar` VARCHAR(512) DEFAULT '' COMMENT '头像URL',
  `gender` TINYINT DEFAULT 2 COMMENT '性别:0-女,1-男,2-未知',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `register_source` VARCHAR(32) DEFAULT 'pc' COMMENT '注册来源:pc,app,miniapp',
  `level_id` BIGINT UNSIGNED DEFAULT 1 COMMENT '会员等级ID',
  `integration` INT DEFAULT 0 COMMENT '积分余额',
  `growth_value` INT DEFAULT 0 COMMENT '成长值',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-正常,2-注销',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(64) DEFAULT '' COMMENT '最后登录IP',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_phone` (`phone`),
  UNIQUE KEY `uk_member_username` (`username`),
  KEY `idx_member_level_id` (`level_id`),
  KEY `idx_member_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员基础表';

-- =====================================================
-- 会员管理表2: ums_member_level 会员等级表
-- =====================================================
DROP TABLE IF EXISTS `ums_member_level`;
CREATE TABLE `ums_member_level` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '等级ID',
  `level_name` VARCHAR(32) NOT NULL COMMENT '等级名称',
  `growth_value_min` INT DEFAULT 0 COMMENT '最低成长值',
  `growth_value_max` INT DEFAULT 99999999 COMMENT '最高成长值',
  `discount_rate` DECIMAL(3,2) DEFAULT 1.00 COMMENT '折扣率:1.00=100%',
  `birthday_integration_rate` DECIMAL(3,2) DEFAULT 2.00 COMMENT '生日积分倍率',
  `privileges` TEXT COMMENT '等级特权JSON',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_growth_value_range` (`growth_value_min`, `growth_value_max`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员等级表';

-- =====================================================
-- 会员管理表3: ums_member_address 会员收货地址表
-- =====================================================
DROP TABLE IF EXISTS `ums_member_address`;
CREATE TABLE `ums_member_address` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `consignee_name` VARCHAR(32) NOT NULL COMMENT '收货人姓名',
  `consignee_phone` VARCHAR(16) NOT NULL COMMENT '收货人手机号',
  `province_code` VARCHAR(12) NOT NULL COMMENT '省编码',
  `province_name` VARCHAR(64) NOT NULL COMMENT '省名称',
  `city_code` VARCHAR(12) NOT NULL COMMENT '市编码',
  `city_name` VARCHAR(64) NOT NULL COMMENT '市名称',
  `district_code` VARCHAR(12) NOT NULL COMMENT '区/县编码',
  `district_name` VARCHAR(64) NOT NULL COMMENT '区/县名称',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `zip_code` VARCHAR(10) DEFAULT '' COMMENT '邮编',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认:0-否,1-是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员收货地址表';

-- =====================================================
-- 商品管理表1: pms_category 商品分类表（三级）
-- =====================================================
DROP TABLE IF EXISTS `pms_category`;
CREATE TABLE `pms_category` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` BIGINT UNSIGNED DEFAULT 0 COMMENT '父分类ID',
  `category_name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `category_icon` VARCHAR(512) DEFAULT '' COMMENT '分类图标URL',
  `level` TINYINT DEFAULT 1 COMMENT '分类级别:1-一级,2-二级,3-三级',
  `path` VARCHAR(512) DEFAULT '' COMMENT '分类路径（逗号分隔）',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_category_level` (`level`),
  KEY `idx_category_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';

-- =====================================================
-- 商品管理表2: pms_attr_group 商品属性分组表
-- =====================================================
DROP TABLE IF EXISTS `pms_attr_group`;
CREATE TABLE `pms_attr_group` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '属性分组ID',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
  `group_name` VARCHAR(64) NOT NULL COMMENT '属性分组名称',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品属性分组表';

-- =====================================================
-- 商品管理表3: pms_attr 商品属性表
-- =====================================================
DROP TABLE IF EXISTS `pms_attr`;
CREATE TABLE `pms_attr` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '属性ID',
  `attr_group_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '属性分组ID',
  `attr_name` VARCHAR(64) NOT NULL COMMENT '属性名称',
  `attr_type` TINYINT DEFAULT 0 COMMENT '属性类型:0-规格,1-参数',
  `search_type` TINYINT DEFAULT 0 COMMENT '是否检索:0-否,1-是',
  `value_type` TINYINT DEFAULT 0 COMMENT '值类型:0-单选,1-多选,2-输入',
  `value_list` TEXT COMMENT '可选值列表（逗号分隔）',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_attr_group_id` (`attr_group_id`),
  KEY `idx_attr_type` (`attr_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品属性表';

-- =====================================================
-- 商品管理表4: pms_spu 商品SPU表
-- =====================================================
DROP TABLE IF EXISTS `pms_spu`;
CREATE TABLE `pms_spu` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'SPU ID',
  `spu_no` VARCHAR(64) NOT NULL COMMENT 'SPU编码',
  `spu_name` VARCHAR(128) NOT NULL COMMENT 'SPU名称',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
  `brand_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '品牌ID',
  `description` TEXT COMMENT '商品描述',
  `main_images` TEXT COMMENT '主图列表（JSON数组，最多5张）',
  `detail_images` TEXT COMMENT '详情图列表（JSON数组）',
  `is_new` TINYINT DEFAULT 0 COMMENT '是否新品:0-否,1-是',
  `is_hot` TINYINT DEFAULT 0 COMMENT '是否热销:0-否,1-是',
  `is_recommend` TINYINT DEFAULT 0 COMMENT '是否推荐:0-否,1-是',
  `sale_count` INT DEFAULT 0 COMMENT '销量',
  `view_count` INT DEFAULT 0 COMMENT '浏览量',
  `status` TINYINT DEFAULT 0 COMMENT '状态:0-下架,1-上架,2-待审核,3-审核失败',
  `publish_time` DATETIME DEFAULT NULL COMMENT '上架时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_no` (`spu_no`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_brand_id` (`brand_id`),
  KEY `idx_spu_status` (`status`),
  KEY `idx_sale_count` (`sale_count`),
  KEY `idx_is_new_hot_recommend` (`is_new`, `is_hot`, `is_recommend`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SPU表';

-- =====================================================
-- 商品管理表5: pms_spu_attr_value SPU属性值表
-- =====================================================
DROP TABLE IF EXISTS `pms_spu_attr_value`;
CREATE TABLE `pms_spu_attr_value` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `attr_id` BIGINT UNSIGNED NOT NULL COMMENT '属性ID',
  `attr_name` VARCHAR(64) NOT NULL COMMENT '属性名称（冗余）',
  `attr_value` VARCHAR(255) NOT NULL COMMENT '属性值',
  `sort` INT DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_attr_id` (`attr_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SPU属性值表';

-- =====================================================
-- 商品管理表6: pms_sku 商品SKU表
-- =====================================================
DROP TABLE IF EXISTS `pms_sku`;
CREATE TABLE `pms_sku` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
  `sku_no` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `sku_name` VARCHAR(128) NOT NULL COMMENT 'SKU名称',
  `spec_values` TEXT NOT NULL COMMENT '规格值JSON（{attrId: attrValue, ...}）',
  `main_image` VARCHAR(512) DEFAULT '' COMMENT 'SKU主图',
  `price` DECIMAL(10,2) NOT NULL COMMENT '销售价',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `cost_price` DECIMAL(10,2) DEFAULT NULL COMMENT '成本价',
  `weight` DECIMAL(10,3) DEFAULT 0.000 COMMENT '重量（kg）',
  `volume` DECIMAL(10,3) DEFAULT 0.000 COMMENT '体积（m³）',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_no` (`sku_no`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_sku_price` (`price`),
  KEY `idx_sku_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SKU表';

-- =====================================================
-- 商品管理表7: pms_brand 商品品牌表
-- =====================================================
DROP TABLE IF EXISTS `pms_brand`;
CREATE TABLE `pms_brand` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '品牌ID',
  `brand_name` VARCHAR(64) NOT NULL COMMENT '品牌名称',
  `brand_logo` VARCHAR(512) DEFAULT '' COMMENT '品牌Logo URL',
  `brand_desc` TEXT COMMENT '品牌描述',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_brand_name` (`brand_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品品牌表';

-- =====================================================
-- 库存管理表1: wms_sku_stock SKU库存表
-- =====================================================
DROP TABLE IF EXISTS `wms_sku_stock`;
CREATE TABLE `wms_sku_stock` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `warehouse_id` BIGINT UNSIGNED DEFAULT 1 COMMENT '仓库ID',
  `total_stock` INT DEFAULT 0 COMMENT '总库存',
  `available_stock` INT DEFAULT 0 COMMENT '可用库存',
  `locked_stock` INT DEFAULT 0 COMMENT '锁定库存（下单未支付/待发货）',
  `warn_stock` INT DEFAULT 10 COMMENT '预警库存',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_warehouse` (`sku_id`, `warehouse_id`),
  KEY `idx_available_stock` (`available_stock`),
  KEY `idx_warn_stock` (`warn_stock`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SKU库存表';

-- =====================================================
-- 订单管理表1: oms_order 订单主表
-- =====================================================
DROP TABLE IF EXISTS `oms_order`;
CREATE TABLE `oms_order` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `username` VARCHAR(64) DEFAULT '' COMMENT '用户名（冗余）',
  `phone` VARCHAR(16) DEFAULT '' COMMENT '手机号（冗余）',
  `consignee_name` VARCHAR(32) NOT NULL COMMENT '收货人姓名',
  `consignee_phone` VARCHAR(16) NOT NULL COMMENT '收货人手机号',
  `province_code` VARCHAR(12) NOT NULL COMMENT '省编码',
  `province_name` VARCHAR(64) NOT NULL COMMENT '省名称',
  `city_code` VARCHAR(12) NOT NULL COMMENT '市编码',
  `city_name` VARCHAR(64) NOT NULL COMMENT '市名称',
  `district_code` VARCHAR(12) NOT NULL COMMENT '区/县编码',
  `district_name` VARCHAR(64) NOT NULL COMMENT '区/县名称',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `freight_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '运费',
  `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
  `coupon_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '优惠券ID',
  `coupon_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠券金额',
  `integration_used` INT DEFAULT 0 COMMENT '使用积分',
  `integration_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '积分抵扣金额',
  `pay_type` TINYINT DEFAULT NULL COMMENT '支付方式:1-微信,2-支付宝,3-银联',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `order_status` TINYINT DEFAULT 0 COMMENT '订单状态:0-待付款,1-待发货,2-已发货,3-已完成,4-已取消,5-售后中',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '确认收货时间',
  `auto_cancel_time` DATETIME DEFAULT NULL COMMENT '自动取消时间',
  `auto_confirm_time` DATETIME DEFAULT NULL COMMENT '自动确认收货时间',
  `remark` VARCHAR(512) DEFAULT '' COMMENT '订单备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_pay_time` (`pay_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单主表';

-- =====================================================
-- 订单管理表2: oms_order_item 订单明细项表
-- =====================================================
DROP TABLE IF EXISTS `oms_order_item`;
CREATE TABLE `oms_order_item` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号（冗余）',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `spu_name` VARCHAR(128) NOT NULL COMMENT 'SPU名称',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `sku_no` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
  `spec_values` TEXT NOT NULL COMMENT '规格值JSON',
  `main_image` VARCHAR(512) DEFAULT '' COMMENT '商品主图',
  `price` DECIMAL(10,2) NOT NULL COMMENT '购买单价',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `quantity` INT NOT NULL COMMENT '购买数量',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '明细总金额',
  `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '明细优惠金额',
  `integration_give` INT DEFAULT 0 COMMENT '赠送积分',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细项表';

-- =====================================================
-- 订单管理表3: oms_order_operation_log 订单操作日志表
-- =====================================================
DROP TABLE IF EXISTS `oms_order_operation_log`;
CREATE TABLE `oms_order_operation_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号（冗余）',
  `operator_type` TINYINT DEFAULT 0 COMMENT '操作人类型:0-系统,1-会员,2-管理员',
  `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(64) DEFAULT '' COMMENT '操作人姓名',
  `operation_type` VARCHAR(32) NOT NULL COMMENT '操作类型',
  `before_status` TINYINT DEFAULT NULL COMMENT '操作前状态',
  `after_status` TINYINT DEFAULT NULL COMMENT '操作后状态',
  `operation_remark` VARCHAR(512) DEFAULT '' COMMENT '操作备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单操作日志表';

-- =====================================================
-- 预设数据: 字典类型
-- =====================================================
INSERT INTO `sys_dict_type` (`dict_code`, `dict_name`, `sort`, `remark`) VALUES
('order_status', '订单状态', 1, ''),
('pay_type', '支付方式', 2, ''),
('member_gender', '会员性别', 3, ''),
('register_source', '注册来源', 4, '');

-- =====================================================
-- 预设数据: 字典数据
-- =====================================================
INSERT INTO `sys_dict_data` (`dict_type_id`, `dict_label`, `dict_value`, `sort`, `remark`) VALUES
(1, '待付款', '0', 1, ''),
(1, '待发货', '1', 2, ''),
(1, '已发货', '2', 3, ''),
(1, '已完成', '3', 4, ''),
(1, '已取消', '4', 5, ''),
(1, '售后中', '5', 6, ''),
(2, '微信支付', '1', 1, ''),
(2, '支付宝支付', '2', 2, ''),
(2, '银联支付', '3', 3, ''),
(3, '女', '0', 1, ''),
(3, '男', '1', 2, ''),
(3, '未知', '2', 3, ''),
(4, 'PC端', 'pc', 1, ''),
(4, 'APP端', 'app', 2, ''),
(4, '小程序', 'miniapp', 3, '');

-- =====================================================
-- 预设数据: 会员等级
-- =====================================================
INSERT INTO `ums_member_level` (`level_name`, `growth_value_min`, `growth_value_max`, `discount_rate`, `birthday_integration_rate`, `privileges`, `sort`) VALUES
('普通会员', 0, 999, 1.00, 1.00, '["基础购物权益"]', 1),
('银卡会员', 1000, 4999, 0.98, 1.50, '["基础购物权益","生日礼券","专属客服"]', 2),
('金卡会员', 5000, 19999, 0.95, 2.00, '["基础购物权益","生日礼券","专属客服","免费退换货"]', 3),
('钻石会员', 20000, 99999999, 0.90, 3.00, '["基础购物权益","生日礼券","专属客服","免费退换货","优先发货","专属活动"]', 4);

-- =====================================================
-- 预设数据: 商品分类
-- =====================================================
INSERT INTO `pms_category` (`parent_id`, `category_name`, `category_icon`, `level`, `path`, `sort`) VALUES
(0, '奶粉辅食', 'https://example.com/icon/milk.png', 1, '1', 1),
(0, '尿裤湿巾', 'https://example.com/icon/diaper.png', 1, '2', 2),
(0, '童装童鞋', 'https://example.com/icon/clothes.png', 1, '3', 3),
(1, '婴儿奶粉', '', 2, '1,4', 1),
(1, '营养辅食', '', 2, '1,5', 2),
(2, '纸尿裤', '', 2, '2,6', 1),
(2, '拉拉裤', '', 2, '2,7', 2),
(3, '婴儿装', '', 2, '3,8', 1),
(3, '学步鞋', '', 2, '3,9', 2);

-- =====================================================
-- 预设数据: 商品品牌
-- =====================================================
INSERT INTO `pms_brand` (`brand_name`, `brand_logo`, `brand_desc`, `sort`) VALUES
('美赞臣', 'https://example.com/logo/meadjohnson.png', '百年营养品牌，专注婴幼儿营养', 1),
('雀巢', 'https://example.com/logo/nestle.png', '全球知名食品饮料公司', 2),
('帮宝适', 'https://example.com/logo/pampers.png', '宝洁旗下知名纸尿裤品牌', 3),
('好奇', 'https://example.com/logo/huggies.png', '金佰利旗下纸尿裤品牌', 4),
('巴拉巴拉', 'https://example.com/logo/balabala.png', '森马旗下童装品牌', 5);
```