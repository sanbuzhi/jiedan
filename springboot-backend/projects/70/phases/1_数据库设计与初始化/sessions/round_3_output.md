===FILE:back/tongquyouyi.sql===
```sql
-- 童趣友谊童装购物系统 - 数据库初始化脚本
-- 数据库版本：MySQL 8.0.35
-- 存储引擎：InnoDB
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_general_ci
-- 创建时间：202X-XX-XX
-- 适用场景：小型童装B2C/B2B2C平台

-- ========================================
-- 1. 创建并切换数据库
-- ========================================
CREATE DATABASE IF NOT EXISTS `tongquyouyi`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci
COMMENT '童趣友谊童装购物系统数据库';

USE `tongquyouyi`;

-- ========================================
-- 2. 公共基础表
-- ========================================

-- 2.1 系统配置表
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `config_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(128) NOT NULL COMMENT '配置键',
  `config_value` TEXT COMMENT '配置值',
  `config_desc` VARCHAR(256) DEFAULT NULL COMMENT '配置描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统配置表';

-- 2.2 地区表（省市区三级联动）
DROP TABLE IF EXISTS `sys_region`;
CREATE TABLE `sys_region` (
  `region_id` BIGINT UNSIGNED NOT NULL COMMENT '地区ID',
  `region_name` VARCHAR(64) NOT NULL COMMENT '地区名称',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级ID（0=省级）',
  `region_level` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '地区级别（1=省，2=市，3=区/县）',
  `sort` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序权重（数值越大越靠前）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`region_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_region_level` (`region_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='省市区三级联动地区表';

-- ========================================
-- 3. 会员管理表
-- ========================================

-- 3.1 会员等级表
DROP TABLE IF EXISTS `member_level`;
CREATE TABLE `member_level` (
  `level_id` TINYINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '等级ID',
  `level_name` VARCHAR(32) NOT NULL COMMENT '等级名称',
  `min_points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '最低积分门槛',
  `discount_rate` DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '折扣率（1.00=不打折，0.95=95折）',
  `points_rate` DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '积分获取倍率（1.00=1元1积分）',
  `birthday_points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '生日赠送积分',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`level_id`),
  UNIQUE KEY `uk_level_name` (`level_name`),
  KEY `idx_min_points` (`min_points`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员等级表';

-- 3.2 会员信息表
DROP TABLE IF EXISTS `member_info`;
CREATE TABLE `member_info` (
  `member_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  `username` VARCHAR(64) NOT NULL COMMENT '登录账号',
  `password` VARCHAR(256) NOT NULL COMMENT '登录密码（BCrypt加密）',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(256) DEFAULT NULL COMMENT '头像URL',
  `gender` TINYINT UNSIGNED DEFAULT 0 COMMENT '性别（0=未知，1=男，2=女）',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `mobile` VARCHAR(16) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `level_id` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '等级ID',
  `total_points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计积分',
  `available_points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '可用积分',
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0=禁用，1=正常，2=锁定）',
  `register_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_mobile` (`mobile`),
  KEY `idx_level_id` (`level_id`),
  KEY `idx_status` (`status`),
  KEY `idx_register_time` (`register_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员信息表';

-- 3.3 会员收货地址表
DROP TABLE IF EXISTS `member_address`;
CREATE TABLE `member_address` (
  `address_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `consignee` VARCHAR(64) NOT NULL COMMENT '收货人姓名',
  `mobile` VARCHAR(16) NOT NULL COMMENT '收货人手机号',
  `province_id` BIGINT UNSIGNED NOT NULL COMMENT '省份ID',
  `city_id` BIGINT UNSIGNED NOT NULL COMMENT '城市ID',
  `district_id` BIGINT UNSIGNED NOT NULL COMMENT '区县ID',
  `detail_address` VARCHAR(256) NOT NULL COMMENT '详细地址',
  `postal_code` VARCHAR(16) DEFAULT NULL COMMENT '邮政编码',
  `is_default` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否默认（0=否，1=是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`address_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员收货地址表';

-- 3.4 会员积分流水表
DROP TABLE IF EXISTS `member_points_log`;
CREATE TABLE `member_points_log` (
  `log_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `points_type` TINYINT UNSIGNED NOT NULL COMMENT '积分类型（1=消费获取，2=活动赠送，3=生日赠送，4=退款扣除，5=积分消费）',
  `points_change` INT NOT NULL COMMENT '积分变化（正数增加，负数减少）',
  `current_points` INT UNSIGNED NOT NULL COMMENT '变化后的可用积分',
  `related_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联ID（订单ID/活动ID等）',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_points_type` (`points_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员积分流水表';

-- ========================================
-- 4. 商品管理表
-- ========================================

-- 4.1 商品分类表（支持多级）
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `category_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级分类ID（0=一级分类）',
  `category_name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `category_icon` VARCHAR(256) DEFAULT NULL COMMENT '分类图标URL',
  `category_level` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '分类级别（1-3级）',
  `sort` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序权重',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0=禁用，1=启用）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`category_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_category_level` (`category_level`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品分类表';

-- 4.2 商品品牌表
DROP TABLE IF EXISTS `product_brand`;
CREATE TABLE `product_brand` (
  `brand_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '品牌ID',
  `brand_name` VARCHAR(64) NOT NULL COMMENT '品牌名称',
  `brand_logo` VARCHAR(256) DEFAULT NULL COMMENT '品牌LOGO URL',
  `brand_desc` TEXT COMMENT '品牌简介',
  `sort` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序权重',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0=禁用，1=启用）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`brand_id`),
  UNIQUE KEY `uk_brand_name` (`brand_name`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品品牌表';

-- 4.3 商品SKU属性表（如颜色、尺码）
DROP TABLE IF EXISTS `product_sku_attr`;
CREATE TABLE `product_sku_attr` (
  `attr_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '属性ID',
  `attr_name` VARCHAR(64) NOT NULL COMMENT '属性名称',
  `attr_type` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '属性类型（1=销售属性，2=规格参数）',
  `sort` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序权重',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0=禁用，1=启用）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`attr_id`),
  KEY `idx_attr_type` (`attr_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品SKU属性表';

-- 4.4 商品SKU属性值表
DROP TABLE IF EXISTS `product_sku_attr_value`;
CREATE TABLE `product_sku_attr_value` (
  `value_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '属性值ID',
  `attr_id` BIGINT UNSIGNED NOT NULL COMMENT '属性ID',
  `attr_value` VARCHAR(64) NOT NULL COMMENT '属性值',
  `sort` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序权重',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`value_id`),
  KEY `idx_attr_id` (`attr_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品SKU属性值表';

-- 4.5 商品SPU表
DROP TABLE IF EXISTS `product_spu`;
CREATE TABLE `product_spu` (
  `spu_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'SPU ID',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
  `brand_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '品牌ID',
  `spu_name` VARCHAR(128) NOT NULL COMMENT 'SPU名称',
  `spu_subtitle` VARCHAR(256) DEFAULT NULL COMMENT 'SPU副标题',
  `main_image` VARCHAR(256) NOT NULL COMMENT '主图URL',
  `image_list` TEXT COMMENT '轮播图列表（JSON数组）',
  `detail_html` TEXT COMMENT '商品详情（HTML格式）',
  `detail_text` TEXT COMMENT '商品详情（纯文本格式，用于搜索）',
  `price_range_min` DECIMAL(10,2) DEFAULT NULL COMMENT '价格范围最小值',
  `price_range_max` DECIMAL(10,2) DEFAULT NULL COMMENT '价格范围最大值',
  `sale_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '销量',
  `view_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览量',
  `comment_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论数',
  `is_hot` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否热销（0=否，1=是）',
  `is_new` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否新品（0=否，1=是）',
  `is_recommend` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否推荐（0=否，1=是）',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态（0=下架，1=上架，2=待审核）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `publish_time` DATETIME DEFAULT NULL COMMENT '上架时间',
  PRIMARY KEY (`spu_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_brand_id` (`brand_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sale_count` (`sale_count`),
  KEY `idx_create_time` (`create_time`),
  FULLTEXT KEY `ft_detail_text` (`detail_text`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品SPU表';

-- 4.6 商品SPU与销售属性关联表
DROP TABLE IF EXISTS `product_spu_attr`;
CREATE TABLE `product_spu_attr` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `attr_id` BIGINT UNSIGNED NOT NULL COMMENT '属性ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_attr_id` (`attr_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品SPU与销售属性关联表';

-- 4.7 商品SKU表
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku` (
  `sku_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码（唯一）',
  `sku_attrs` VARCHAR(256) NOT NULL COMMENT 'SKU属性组合（JSON格式，如[{"attrId":1,"attrValue":"红色"},{"attrId":2,"attrValue":"120"}]）',
  `sku_image` VARCHAR(256) DEFAULT NULL COMMENT 'SKU图片URL',
  `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
  `cost_price` DECIMAL(10,2) DEFAULT NULL COMMENT '成本价',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `weight` DECIMAL(6,2) DEFAULT NULL COMMENT '重量（kg）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`sku_id`),
  UNIQUE KEY `uk_sku_code` (`sku_code`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品SKU表';

-- ========================================
-- 5. 库存管理表
-- ========================================

-- 5.1 商品库存表
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory` (
  `inventory_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `total_stock` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '总库存',
  `available_stock` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '可用库存',
  `locked_stock` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '锁定库存',
  `warning_stock` INT UNSIGNED NOT NULL DEFAULT 10 COMMENT '预警库存',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`inventory_id`),
  UNIQUE KEY `uk_sku_id` (`sku_id`),
  KEY `idx_available_stock` (`available_stock`),
  KEY `idx_warning_stock` (`warning_stock`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品库存表';

-- 5.2 库存流水表
DROP TABLE IF EXISTS `inventory_log`;
CREATE TABLE `inventory_log` (
  `log_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `log_type` TINYINT UNSIGNED NOT NULL COMMENT '流水类型（1=采购入库，2=销售出库，3=退货入库，4=换货出库，5=盘点调整，6=锁定库存，7=解锁库存）',
  `stock_change` INT NOT NULL COMMENT '库存变化（正数增加，负数减少）',
  `current_total_stock` INT UNSIGNED NOT NULL COMMENT '变化后的总库存',
  `current_available_stock` INT UNSIGNED NOT NULL COMMENT '变化后的可用库存',
  `current_locked_stock` INT UNSIGNED NOT NULL COMMENT '变化后的锁定库存',
  `related_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联ID（采购单ID/订单ID/盘点单ID等）',
  `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID（管理员ID）',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_log_type` (`log_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='库存流水表';

-- ========================================
-- 6. 订单管理表
-- ========================================

-- 6.1 订单主表
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info` (
  `order_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号（唯一）',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `payable_amount` DECIMAL(10,2) NOT NULL COMMENT '应付金额',
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠总金额',
  `coupon_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠券金额',
  `points_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '积分抵扣金额',
  `freight_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费金额',
  `pay_type` TINYINT UNSIGNED DEFAULT NULL COMMENT '支付方式（1=微信支付，2=支付宝支付，3=余额支付）',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `order_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单状态（0=待付款，1=待发货，2=已发货，3=已完成，4=已取消，5=退货退款中，6=已退货退款）',
  `refund_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '退款状态（0=无退款，1=部分退款，2=全额退款）',
  `consignee` VARCHAR(64) NOT NULL COMMENT '收货人姓名',
  `mobile` VARCHAR(16) NOT NULL COMMENT '收货人手机号',
  `province_id` BIGINT UNSIGNED NOT NULL COMMENT '省份ID',
  `city_id` BIGINT UNSIGNED NOT NULL COMMENT '城市ID',
  `district_id` BIGINT UNSIGNED NOT NULL COMMENT '区县ID',
  `detail_address` VARCHAR(256) NOT NULL COMMENT '详细地址',
  `postal_code` VARCHAR(16) DEFAULT NULL COMMENT '邮政编码',
  `shipping_code` VARCHAR(64) DEFAULT NULL COMMENT '物流单号',
  `shipping_company` VARCHAR(64) DEFAULT NULL COMMENT '物流公司',
  `shipping_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
  `member_remark` VARCHAR(256) DEFAULT NULL COMMENT '会员备注',
  `admin_remark` VARCHAR(256) DEFAULT NULL COMMENT '管理员备注',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(256) DEFAULT NULL COMMENT '取消原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_pay_time` (`pay_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单主表';

-- 6.2 订单商品明细表
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `item_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `spu_name` VARCHAR(128) NOT NULL COMMENT 'SPU名称',
  `sku_attrs` VARCHAR(256) NOT NULL COMMENT 'SKU属性组合',
  `sku_image` VARCHAR(256) DEFAULT NULL COMMENT 'SKU图片URL',
  `price` DECIMAL(10,2) NOT NULL COMMENT '购买单价',
  `quantity` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '购买数量',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '商品小计金额',
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品优惠金额',
  `refund_quantity` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '退货数量',
  `refund_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '退款金额',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`item_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单商品明细表';

-- 6.3 订单操作日志表
DROP TABLE IF EXISTS `order_log`;
CREATE TABLE `order_log` (
  `log_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `operator_type` TINYINT UNSIGNED NOT NULL COMMENT '操作人类型（1=会员，2=管理员，3=系统）',
  `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名',
  `order_status_before` TINYINT UNSIGNED DEFAULT NULL COMMENT '操作前订单状态',
  `order_status_after` TINYINT UNSIGNED DEFAULT NULL COMMENT '操作后订单状态',
  `action` VARCHAR(128) NOT NULL COMMENT '操作动作',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单操作日志表';

-- ========================================
-- 7. 预设数据
-- ========================================

-- 7.1 系统配置预设
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_desc`) VALUES
('site_name', '童趣友谊童装', '网站名称'),
('site_logo', '/static/images/logo.png', '网站LOGO'),
('site_keywords', '童装,儿童服装,宝宝衣服,童趣友谊', '网站关键词'),
('site_description', '童趣友谊童装，专注0-12岁儿童服饰，提供安全、舒适、时尚的童装产品', '网站描述'),
('freight_free_amount', '99.00', '满额免运费门槛'),
('default_freight', '8.00', '默认运费'),
('points_exchange_rate', '100', '积分兑换比例（100积分=1元）'),
('order_auto_cancel_hours', '24', '订单自动取消时间（小时）'),
('order_auto_confirm_days', '7', '订单自动确认收货时间（天）');

-- 7.2 会员等级预设
INSERT INTO `member_level` (`level_name`, `min_points`, `discount_rate`, `points_rate`, `birthday_points`) VALUES
('普通会员', 0, 1.00, 1.00, 0),
('银卡会员', 1000, 0.98, 1.20, 50),
('金卡会员', 5000, 0.95, 1.50, 200),
('钻石会员', 20000, 0.90, 2.00, 500);

-- 7.3 商品分类预设（示例）
INSERT INTO `product_category` (`parent_id`, `category_name`, `category_level`, `sort`, `status`) VALUES
(0, '婴儿装', 1, 1, 1),
(0, '幼童装', 1, 2, 1),
(0, '儿童装', 1, 3, 1),
(1, '连体衣/哈衣', 2, 1, 1),
(1, '婴儿套装', 2, 2, 1),
(2, 'T恤/上衣', 2, 1, 1),
(2, '裤子', 2, 2, 1),
(3, '连衣裙', 2, 1, 1),
(3, '运动套装', 2, 2, 1);

-- 7.4 商品品牌预设（示例）
INSERT INTO `product_brand` (`brand_name`, `brand_desc`, `sort`, `status`) VALUES
('巴拉巴拉', '森马旗下专业童装品牌', 1, 1),
('安奈儿', '国内知名高端童装品牌', 2, 1),
('小猪班纳', '专注儿童健康舒适服饰', 3, 1),
('迪士尼童装', '迪士尼授权正品童装', 4, 1);

-- 7.5 商品SKU属性预设
INSERT INTO `product_sku_attr` (`attr_name`, `attr_type`, `sort`, `status`) VALUES
('颜色', 1, 1, 1),
('尺码', 1, 2, 1),
('材质', 2, 1, 1),
('适用季节', 2, 2, 1);

-- 7.6 商品SKU属性值预设
INSERT INTO `product_sku_attr_value` (`attr_id`, `attr_value`, `sort`) VALUES
(1, '红色', 1),
(1, '蓝色', 2),
(1, '粉色', 3),
(1, '黄色', 4),
(1, '白色', 5),
(2, '66', 1),
(2, '73', 2),
(2, '80', 3),
(2, '90', 4),
(2, '100', 5),
(2, '110', 6),
(2, '120', 7),
(2, '130', 8),
(2, '140', 9),
(3, '纯棉', 1),
(3, '棉麻', 2),
(3, '聚酯纤维', 3),
(4, '春季', 1),
(4, '夏季', 2),
(4, '秋季', 3),
(4, '冬季', 4);
```