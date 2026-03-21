===FILE:back/tongquyouyi.sql===
```sql
-- 童趣友谊商城 MySQL 8.0.35+ 数据库初始化脚本
-- 字符集: utf8mb4  存储引擎: InnoDB
-- 202X-XX-XX 初始版本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS tongquyouyi DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tongquyouyi;

-- =========================================
-- 公共基础表
-- =========================================

-- 地区表
DROP TABLE IF EXISTS `common_region`;
CREATE TABLE `common_region` (
  `region_id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地区ID',
  `parent_id` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '父级地区ID(0为省/直辖市)',
  `region_name` VARCHAR(64) NOT NULL COMMENT '地区名称',
  `region_code` VARCHAR(12) NOT NULL COMMENT '地区编码',
  `level` TINYINT UNSIGNED NOT NULL DEFAULT '1' COMMENT '地区级别(1省2市3区县4街道)',
  `sort` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '排序',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否删除(0否1是)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`region_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_region_code` (`region_code`),
  KEY `idx_level_sort` (`level`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公共地区表';

-- 字典表
DROP TABLE IF EXISTS `common_dict`;
CREATE TABLE `common_dict` (
  `dict_id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '字典ID',
  `dict_type` VARCHAR(32) NOT NULL COMMENT '字典类型编码',
  `dict_label` VARCHAR(64) NOT NULL COMMENT '字典标签',
  `dict_value` VARCHAR(64) NOT NULL COMMENT '字典值',
  `sort` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '排序',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT '1' COMMENT '状态(0禁用1启用)',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否删除(0否1是)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`dict_id`),
  KEY `idx_dict_type` (`dict_type`),
  KEY `idx_dict_value` (`dict_value`),
  UNIQUE KEY `uk_type_value` (`dict_type`, `dict_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公共字典表';

-- =========================================
-- 会员管理表
-- =========================================

-- 会员用户表
DROP TABLE IF EXISTS `member_user`;
CREATE TABLE `member_user` (
  `user_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(32) NOT NULL COMMENT '用户名(唯一)',
  `password` VARCHAR(128) NOT NULL COMMENT '密码(加密)',
  `nickname` VARCHAR(32) NOT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `gender` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '性别(0未知1男2女)',
  `mobile` VARCHAR(16) DEFAULT NULL COMMENT '手机号(唯一)',
  `email` VARCHAR(64) DEFAULT NULL COMMENT '邮箱(唯一)',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `balance` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT '0.00' COMMENT '账户余额',
  `point` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '积分',
  `level_id` INT UNSIGNED NOT NULL DEFAULT '1' COMMENT '会员等级ID',
  `real_name` VARCHAR(32) DEFAULT NULL COMMENT '真实姓名',
  `id_card` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT '1' COMMENT '状态(0禁用1启用)',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否删除(0否1是)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_mobile` (`mobile`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_level_id` (`level_id`),
  KEY `idx_status_create` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员用户表';

-- 会员等级表
DROP TABLE IF EXISTS `member_level`;
CREATE TABLE `member_level` (
  `level_id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '等级ID',
  `level_name` VARCHAR(32) NOT NULL COMMENT '等级名称',
  `level_code` VARCHAR(32) NOT NULL COMMENT '等级编码',
  `min_point` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '最低升级积分',
  `discount_rate` DECIMAL(3,2) UNSIGNED NOT NULL DEFAULT '1.00' COMMENT '折扣率(1.00为不打折)',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '等级图标URL',
  `privileges` TEXT DEFAULT NULL COMMENT '等级特权(JSON格式)',
  `sort` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '排序',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否删除(0否1是)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`level_id`),
  UNIQUE KEY `uk_level_code` (`level_code`),
  KEY `idx_min_point_sort` (`min_point`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员等级表';

-- 会员收货地址表
DROP TABLE IF EXISTS `member_address`;
CREATE TABLE `member_address` (
  `address_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `consignee` VARCHAR(32) NOT NULL COMMENT '收货人',
  `mobile` VARCHAR(16) NOT NULL COMMENT '联系电话',
  `province_id` INT UNSIGNED NOT NULL COMMENT '省ID',
  `city_id` INT UNSIGNED NOT NULL COMMENT '市ID',
  `district_id` INT UNSIGNED NOT NULL COMMENT '区县ID',
  `street_id` INT UNSIGNED DEFAULT NULL COMMENT '街道ID',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `full_address` VARCHAR(512) NOT NULL COMMENT '完整地址',
  `zip_code` VARCHAR(8) DEFAULT NULL COMMENT '邮编',
  `is_default` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否默认(0否1是)',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否删除(0否1是)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`address_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_default` (`user_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员收货地址表';

-- =========================================
-- 商品管理表
-- =========================================

-- 商品分类表
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `category_id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '父级分类ID(0为一级)',
  `category_name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `category_code` VARCHAR(32) NOT NULL COMMENT '分类编码',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标URL',
  `banner` VARCHAR(255) DEFAULT NULL COMMENT '分类Banner URL',
  `level` TINYINT UNSIGNED NOT NULL DEFAULT '1' COMMENT '分类级别(1-3)',
  `sort` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '排序',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT '1' COMMENT '状态(0禁用1启用)',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否删除(0否1是)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level_sort` (`level`, `sort`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 商品品牌表
DROP TABLE IF EXISTS `product_brand`;
CREATE TABLE `product_brand` (
  `brand_id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '品牌ID',
  `brand_name` VARCHAR(64) NOT NULL COMMENT '品牌名称',
  `brand_code` VARCHAR(32) NOT NULL COMMENT '品牌编码',
  `logo` VARCHAR(255) DEFAULT NULL COMMENT '品牌Logo URL',
  `description` TEXT DEFAULT NULL COMMENT '品牌简介',
  `sort` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '排序',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT '1' COMMENT '状态(0禁用1启用)',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否删除(0否1是)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`brand_id`),
  UNIQUE KEY `uk_brand_code` (`brand_code`),
  KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品品牌表';

-- 商品属性组表
DROP TABLE IF EXISTS `product_attr_group`;
CREATE TABLE `product_attr_group` (
  `group_id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '属性组ID',
  `category_id` INT UNSIGNED NOT NULL COMMENT '关联分类ID',
  `group_name` VARCHAR(64) NOT NULL COMMENT '属性组名称',
  `sort` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '排序',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否删除(0否1是)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`group_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品属性组表';

-- 商品属性表
DROP TABLE IF EXISTS `product_attr`;
CREATE TABLE `product_attr` (
  `attr_id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '属性ID',
  `group_id` INT UNSIGNED NOT NULL COMMENT '属性组ID',
  `attr_name` VARCHAR(64) NOT NULL COMMENT '属性名称',
  `attr_type` TINYINT UNSIGNED NOT NULL DEFAULT '1' COMMENT '属性类型(1单选2多选3文本4数字)',
  `attr_values` TEXT DEFAULT NULL COMMENT '属性可选值(JSON数组,单选/多选必填)',
  `is_searchable` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否支持搜索(0否1是)',
  `is_spec` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否规格属性(0否1是)',
  `sort` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '排序',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否删除(0否1是)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`attr_id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_is_spec` (`is_spec`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品属性表';

-- 商品SPU表
DROP TABLE IF EXISTS `product_spu`;
CREATE TABLE `product_spu` (
  `spu_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'SPU ID',
  `spu_code` VARCHAR(32) NOT NULL COMMENT 'SPU编码(唯一)',
  `spu_name` VARCHAR(255) NOT NULL COMMENT 'SPU名称',
  `category_id` INT UNSIGNED NOT NULL COMMENT '分类ID',
  `brand_id` INT UNSIGNED DEFAULT NULL COMMENT '品牌ID',
  `main_image` VARCHAR(255) NOT NULL COMMENT '主图URL',
  `sub_images` TEXT DEFAULT NULL COMMENT '副图URL(JSON数组)',
  `video_url` VARCHAR(255) DEFAULT NULL COMMENT '商品视频URL',
  `description` TEXT DEFAULT NULL COMMENT '商品富文本详情',
  `tags` VARCHAR(255) DEFAULT NULL COMMENT '商品标签(逗号分隔)',
  `unit` VARCHAR(16) NOT NULL DEFAULT '件' COMMENT '销售单位',
  `weight` DECIMAL(8,3) UNSIGNED DEFAULT NULL COMMENT '商品重量(kg)',
  `volume` DECIMAL(8,3) UNSIGNED DEFAULT NULL COMMENT '商品体积(m³)',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '状态(0下架1上架2待审核)',
  `is_hot` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否热销(0否1是)',
  `is_new` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否新品(0否1是)',
  `is_recommend` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否推荐(0否1是)',
  `sort` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '排序',
  `sales` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '销量',
  `view_count` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '浏览量',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否删除(0否1是)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`spu_id`),
  UNIQUE KEY `uk_spu_code` (`spu_code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_brand_id` (`brand_id`),
  KEY `idx_status_sales` (`status`, `sales`),
  KEY `idx_hot_new_recommend` (`is_hot`, `is_new`, `is_recommend`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SPU表';

-- 商品SKU表
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku` (
  `sku_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `sku_code` VARCHAR(32) NOT NULL COMMENT 'SKU编码(唯一)',
  `specs` TEXT NOT NULL COMMENT '规格组合(JSON格式,如[{"attr_id":1,"attr_value":"红色"},{"attr_id":2,"attr_value":"L"}])',
  `sku_name` VARCHAR(255) NOT NULL COMMENT 'SKU名称',
  `main_image` VARCHAR(255) DEFAULT NULL COMMENT 'SKU主图URL(为空则用SPU主图)',
  `price` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '售价',
  `cost_price` DECIMAL(10,2) UNSIGNED DEFAULT NULL COMMENT '成本价',
  `original_price` DECIMAL(10,2) UNSIGNED DEFAULT NULL COMMENT '原价',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT '1' COMMENT '状态(0禁用1启用)',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否删除(0否1是)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`sku_id`),
  UNIQUE KEY `uk_sku_code` (`sku_code`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_spu_status` (`spu_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU表';

-- =========================================
-- 库存管理表
-- =========================================

-- 商品库存表
DROP TABLE IF EXISTS `inventory_stock`;
CREATE TABLE `inventory_stock` (
  `stock_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `warehouse_id` INT UNSIGNED NOT NULL DEFAULT '1' COMMENT '仓库ID(默认1)',
  `stock_num` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '实际库存',
  `locked_stock` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '锁定库存',
  `available_stock` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '可用库存(实际库存-锁定库存)',
  `warning_stock` INT UNSIGNED NOT NULL DEFAULT '10' COMMENT '预警库存',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否删除(0否1是)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`stock_id`),
  UNIQUE KEY `uk_sku_warehouse` (`sku_id`, `warehouse_id`),
  KEY `idx_available_stock` (`available_stock`),
  KEY `idx_warning_stock` (`available_stock`, `warning_stock`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品库存表';

-- 库存变动记录表
DROP TABLE IF EXISTS `inventory_log`;
CREATE TABLE `inventory_log` (
  `log_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `warehouse_id` INT UNSIGNED NOT NULL DEFAULT '1' COMMENT '仓库ID',
  `change_type` TINYINT UNSIGNED NOT NULL COMMENT '变动类型(1入库2出库3锁定4释放5盘点)',
  `change_num` INT NOT NULL COMMENT '变动数量(正数增加负数减少)',
  `before_stock` INT UNSIGNED NOT NULL COMMENT '变动前库存',
  `after_stock` INT UNSIGNED NOT NULL COMMENT '变动后库存',
  `before_locked` INT UNSIGNED NOT NULL COMMENT '变动前锁定',
  `after_locked` INT UNSIGNED NOT NULL COMMENT '变动后锁定',
  `related_id` VARCHAR(64) DEFAULT NULL COMMENT '关联业务ID(订单号/入库单号等)',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID(0为系统)',
  `operator_name` VARCHAR(32) DEFAULT NULL COMMENT '操作人姓名',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_sku_warehouse` (`sku_id`, `warehouse_id`),
  KEY `idx_change_type` (`change_type`),
  KEY `idx_related_id` (`related_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存变动记录表';

-- =========================================
-- 订单管理表
-- =========================================

-- 订单主表
DROP TABLE IF EXISTS `order_main`;
CREATE TABLE `order_main` (
  `order_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号(唯一)',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `total_amount` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '订单总金额',
  `payable_amount` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '应付金额',
  `discount_amount` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT '0.00' COMMENT '优惠总金额',
  `freight_amount` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT '0.00' COMMENT '运费金额',
  `point_amount` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT '0.00' COMMENT '积分抵扣金额',
  `use_point` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '使用积分',
  `gain_point` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '获得积分',
  `consignee` VARCHAR(32) NOT NULL COMMENT '收货人',
  `mobile` VARCHAR(16) NOT NULL COMMENT '联系电话',
  `province_id` INT UNSIGNED NOT NULL COMMENT '省ID',
  `city_id` INT UNSIGNED NOT NULL COMMENT '市ID',
  `district_id` INT UNSIGNED NOT NULL COMMENT '区县ID',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `full_address` VARCHAR(512) NOT NULL COMMENT '完整地址',
  `order_status` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '订单状态(0待付款1待发货2已发货3已完成4已取消5退款中6已退款)',
  `pay_status` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '支付状态(0未支付1已支付2退款中3已退款)',
  `pay_type` TINYINT UNSIGNED DEFAULT NULL COMMENT '支付方式(1微信2支付宝3余额)',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `confirm_time` DATETIME DEFAULT NULL COMMENT '确认收货时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '用户备注',
  `admin_remark` VARCHAR(255) DEFAULT NULL COMMENT '管理员备注',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否删除(0否1是)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_pay_status` (`pay_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_user_status` (`user_id`, `order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

-- 订单商品表
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `item_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `spu_name` VARCHAR(255) NOT NULL COMMENT 'SPU名称',
  `sku_name` VARCHAR(255) NOT NULL COMMENT 'SKU名称',
  `specs` TEXT NOT NULL COMMENT '规格组合(JSON格式)',
  `main_image` VARCHAR(255) NOT NULL COMMENT '商品主图',
  `price` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '商品单价',
  `original_price` DECIMAL(10,2) UNSIGNED DEFAULT NULL COMMENT '商品原价',
  `quantity` INT UNSIGNED NOT NULL DEFAULT '1' COMMENT '购买数量',
  `total_price` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '商品总价',
  `discount_price` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT '0.00' COMMENT '商品优惠金额',
  `is_comment` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否已评价(0否1是)',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否删除(0否1是)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`item_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品表';

-- 订单操作日志表
DROP TABLE IF EXISTS `order_log`;
CREATE TABLE `order_log` (
  `log_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
  `order_status` TINYINT UNSIGNED NOT NULL COMMENT '操作后订单状态',
  `pay_status` TINYINT UNSIGNED NOT NULL COMMENT '操作后支付状态',
  `operator_type` TINYINT UNSIGNED NOT NULL COMMENT '操作人类型(1用户2管理员3系统)',
  `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(32) DEFAULT NULL COMMENT '操作人姓名',
  `operate_content` VARCHAR(255) NOT NULL COMMENT '操作内容',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单操作日志表';

-- =========================================
-- 预设数据
-- =========================================

-- 预设会员等级
INSERT INTO `member_level` (`level_id`, `level_name`, `level_code`, `min_point`, `discount_rate`, `icon`, `privileges`, `sort`) VALUES
(1, '普通会员', 'NORMAL', 0, 1.00, NULL, '["基础购物"]', 1),
(2, '白银会员', 'SILVER', 1000, 0.98, NULL, '["基础购物","专属客服","生日礼包"]', 2),
(3, '黄金会员', 'GOLD', 5000, 0.95, NULL, '["基础购物","专属客服","生日礼包","优先发货","专属折扣"]', 3),
(4, '钻石会员', 'DIAMOND', 20000, 0.90, NULL, '["基础购物","专属客服","生日礼包","优先发货","专属折扣","免费退换"]', 4);

-- 预设公共字典
INSERT INTO `common_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `status`) VALUES
-- 性别
('gender', '未知', '0', 1, 1),
('gender', '男', '1', 2, 1),
('gender', '女', '2', 3, 1),
-- 支付方式
('pay_type', '微信支付', '1', 1, 1),
('pay_type', '支付宝', '2', 2, 1),
('pay_type', '余额支付', '3', 3, 1),
-- 订单状态
('order_status', '待付款', '0', 1, 1),
('order_status', '待发货', '1', 2, 1),
('order_status', '已发货', '2', 3, 1),
('order_status', '已完成', '3', 4, 1),
('order_status', '已取消', '4', 5, 1),
('order_status', '退款中', '5', 6, 1),
('order_status', '已退款', '6', 7, 1);

-- 预设地区（简化版）
INSERT INTO `common_region` (`region_id`, `parent_id`, `region_name`, `region_code`, `level`, `sort`) VALUES
(1, 0, '北京市', '110000', 1, 1),
(2, 1, '北京市', '110100', 2, 1),
(3, 2, '东城区', '110101', 3, 1),
(4, 2, '西城区', '110102', 3, 2),
(5, 2, '朝阳区', '110105', 3, 3),
(6, 0, '上海市', '310000', 1, 2),
(7, 6, '上海市', '310100', 2, 1),
(8, 7, '黄浦区', '310101', 3, 1),
(9, 7, '徐汇区', '310104', 3, 2),
(10, 7, '浦东新区', '310115', 3, 3);

-- 预设商品分类（简化版）
INSERT INTO `product_category` (`category_id`, `parent_id`, `category_name`, `category_code`, `level`, `sort`, `status`) VALUES
(1, 0, '益智玩具', 'EDUCATIONAL', 1, 1, 1),
(2, 1, '积木拼插', 'BLOCKS', 2, 1, 1),
(3, 1, '拼图', 'PUZZLE', 2, 2, 1),
(4, 0, '娃娃玩偶', 'DOLLS', 1, 2, 1),
(5, 4, '毛绒玩具', 'PLUSH', 2, 1, 1),
(6, 4, '芭比娃娃', 'BARBIE', 2, 2, 1),
(7, 0, '遥控玩具', 'RC', 1, 3, 1),
(8, 7, '遥控汽车', 'RC_CAR', 2, 1, 1),
(9, 7, '遥控飞机', 'RC_PLANE', 2, 2, 1);

-- 预设商品品牌（简化版）
INSERT INTO `product_brand` (`brand_id`, `brand_name`, `brand_code`, `sort`, `status`) VALUES
(1, '乐高', 'LEGO', 1, 1),
(2, '芭比', 'BARBIE', 2, 1),
(3, '奥迪双钻', 'AULDEY', 3, 1),
(4, '费雪', 'FISHER_PRICE', 4, 1),
(5, '小猪佩奇', 'PEPPA_PIG', 5, 1);

-- 预设测试用户（密码为123456的BCrypt加密值）
INSERT INTO `member_user` (`username`, `password`, `nickname`, `mobile`, `level_id`, `status`) VALUES
('test001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '测试用户001', '13800138001', 1, 1),
('test002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '测试用户002', '13800138002', 2, 1);
```