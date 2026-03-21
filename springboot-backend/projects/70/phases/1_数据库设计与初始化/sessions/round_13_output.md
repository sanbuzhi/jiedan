===FILE:back/tongquyouyi.sql===
```sql
-- =============================================
-- 数据库名称: tongquyouyi
-- 适用版本: MySQL 8.0.35+
-- 存储引擎: InnoDB
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- =============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `tongquyouyi`
DEFAULT CHARACTER SET = utf8mb4
DEFAULT COLLATE = utf8mb4_unicode_ci;

-- 使用数据库
USE `tongquyouyi`;

-- =============================================
-- 模块1：公共表
-- =============================================

-- 1.1 系统字典表（存储公共枚举/字典项）
DROP TABLE IF EXISTS `system_dict`;
CREATE TABLE `system_dict` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '字典项ID',
  `dict_code` VARCHAR(64) NOT NULL COMMENT '字典分组编码（如gender、order_status）',
  `dict_name` VARCHAR(64) NOT NULL COMMENT '字典分组名称',
  `item_code` VARCHAR(32) NOT NULL COMMENT '字典项编码（如MALE、WAIT_PAY）',
  `item_name` VARCHAR(32) NOT NULL COMMENT '字典项显示名称',
  `item_value` VARCHAR(128) DEFAULT NULL COMMENT '字典项附加值',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序（升序）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1启用 0禁用）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_code_item_code` (`dict_code`, `item_code`),
  KEY `idx_dict_code` (`dict_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统字典表';

-- 1.2 系统配置表（存储全局配置）
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(64) NOT NULL COMMENT '配置键（唯一）',
  `config_value` TEXT NOT NULL COMMENT '配置值',
  `config_name` VARCHAR(128) NOT NULL COMMENT '配置名称',
  `config_type` TINYINT NOT NULL DEFAULT 1 COMMENT '配置类型（1字符串 2数字 3布尔 4JSON）',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1启用 0禁用）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 1.3 操作日志表（记录管理员/关键操作）
DROP TABLE IF EXISTS `system_operation_log`;
CREATE TABLE `system_operation_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID（用户或管理员）',
  `operator_type` TINYINT NOT NULL DEFAULT 1 COMMENT '操作人类型（1管理员 2会员）',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名/昵称',
  `operation_module` VARCHAR(64) NOT NULL COMMENT '操作模块',
  `operation_type` VARCHAR(32) NOT NULL COMMENT '操作类型（新增/修改/删除/查询/导出等）',
  `operation_desc` VARCHAR(255) NOT NULL COMMENT '操作描述',
  `request_url` VARCHAR(512) DEFAULT NULL COMMENT '请求URL',
  `request_method` VARCHAR(16) DEFAULT NULL COMMENT '请求方法（GET/POST/PUT/DELETE等）',
  `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
  `response_result` TEXT DEFAULT NULL COMMENT '响应结果（失败时存）',
  `ip_address` VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
  `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `duration` INT DEFAULT NULL COMMENT '请求耗时（毫秒）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '操作状态（1成功 0失败）',
  PRIMARY KEY (`id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_operator_type` (`operator_type`),
  KEY `idx_operation_module` (`operation_module`),
  KEY `idx_operation_time` (`operation_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =============================================
-- 模块2：会员管理表
-- =============================================

-- 2.1 会员表（核心用户表）
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  `username` VARCHAR(64) NOT NULL COMMENT '登录账号（唯一）',
  `password` VARCHAR(255) NOT NULL COMMENT '登录密码（加密）',
  `nickname` VARCHAR(64) NOT NULL COMMENT '昵称',
  `real_name` VARCHAR(32) DEFAULT NULL COMMENT '真实姓名',
  `gender` VARCHAR(16) DEFAULT 'UNKNOWN' COMMENT '性别（关联system_dict的gender分组）',
  `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
  `mobile` VARCHAR(16) DEFAULT NULL COMMENT '手机号（唯一）',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱（唯一）',
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `points` INT NOT NULL DEFAULT 0 COMMENT '积分',
  `member_level` VARCHAR(32) NOT NULL DEFAULT 'NORMAL' COMMENT '会员等级（关联system_dict的member_level分组）',
  `register_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1正常 0禁用）',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除（1已删 0未删）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_mobile` (`mobile`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_member_level` (`member_level`),
  KEY `idx_register_time` (`register_time`),
  KEY `idx_status` (`status`),
  KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员表';

-- 2.2 会员收货地址表
DROP TABLE IF EXISTS `member_address`;
CREATE TABLE `member_address` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `consignee` VARCHAR(32) NOT NULL COMMENT '收货人',
  `mobile` VARCHAR(16) NOT NULL COMMENT '收货手机号',
  `province_code` VARCHAR(16) NOT NULL COMMENT '省份编码',
  `province_name` VARCHAR(32) NOT NULL COMMENT '省份名称',
  `city_code` VARCHAR(16) NOT NULL COMMENT '城市编码',
  `city_name` VARCHAR(32) NOT NULL COMMENT '城市名称',
  `district_code` VARCHAR(16) NOT NULL COMMENT '区县编码',
  `district_name` VARCHAR(32) NOT NULL COMMENT '区县名称',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `zip_code` VARCHAR(16) DEFAULT NULL COMMENT '邮编',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认地址（1是 0否）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员收货地址表';

-- =============================================
-- 模块3：商品管理表
-- =============================================

-- 3.1 商品分类表
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类ID（0为一级分类）',
  `category_name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `category_code` VARCHAR(64) DEFAULT NULL COMMENT '分类编码（唯一）',
  `icon` VARCHAR(512) DEFAULT NULL COMMENT '分类图标URL',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `level` TINYINT NOT NULL DEFAULT 1 COMMENT '分类层级（1/2/3级）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1启用 0禁用）',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除（1已删 0未删）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`),
  KEY `idx_sort` (`sort`),
  KEY `idx_status` (`status`),
  KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 3.2 商品品牌表
DROP TABLE IF EXISTS `product_brand`;
CREATE TABLE `product_brand` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '品牌ID',
  `brand_name` VARCHAR(64) NOT NULL COMMENT '品牌名称',
  `brand_code` VARCHAR(64) DEFAULT NULL COMMENT '品牌编码（唯一）',
  `logo` VARCHAR(512) DEFAULT NULL COMMENT '品牌logo URL',
  `description` TEXT DEFAULT NULL COMMENT '品牌描述',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1启用 0禁用）',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除（1已删 0未删）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_brand_code` (`brand_code`),
  KEY `idx_brand_name` (`brand_name`),
  KEY `idx_sort` (`sort`),
  KEY `idx_status` (`status`),
  KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品品牌表';

-- 3.3 商品SPU表（标准产品单元）
DROP TABLE IF EXISTS `product_spu`;
CREATE TABLE `product_spu` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'SPU ID',
  `spu_code` VARCHAR(64) NOT NULL COMMENT 'SPU编码（唯一）',
  `spu_name` VARCHAR(255) NOT NULL COMMENT 'SPU名称',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '商品分类ID（关联三级分类）',
  `brand_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '商品品牌ID',
  `main_image` VARCHAR(512) NOT NULL COMMENT '主图URL',
  `sub_images` TEXT DEFAULT NULL COMMENT '副图URL（JSON数组）',
  `description` TEXT DEFAULT NULL COMMENT '商品详情（富文本）',
  `sale_count` INT NOT NULL DEFAULT 0 COMMENT '销量',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `is_hot` TINYINT NOT NULL DEFAULT 0 COMMENT '是否热门（1是 0否）',
  `is_new` TINYINT NOT NULL DEFAULT 0 COMMENT '是否新品（1是 0否）',
  `is_recommend` TINYINT NOT NULL DEFAULT 0 COMMENT '是否推荐（1是 0否）',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0下架 1上架）',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除（1已删 0未删）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_code` (`spu_code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_brand_id` (`brand_id`),
  KEY `idx_sale_count` (`sale_count`),
  KEY `idx_is_hot` (`is_hot`),
  KEY `idx_is_new` (`is_new`),
  KEY `idx_is_recommend` (`is_recommend`),
  KEY `idx_status` (`status`),
  KEY `idx_is_deleted` (`is_deleted`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SPU表';

-- 3.4 商品SKU表（库存保有单元）
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
  `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码（唯一）',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `spec_attrs` VARCHAR(512) NOT NULL COMMENT '规格属性（JSON数组，如[{"spec_id":1,"spec_name":"颜色","attr_id":1,"attr_name":"红色"},{"spec_id":2,"spec_name":"尺寸","attr_id":2,"attr_name":"L"}]）',
  `price` DECIMAL(10,2) NOT NULL COMMENT '销售价格',
  `cost_price` DECIMAL(10,2) DEFAULT NULL COMMENT '成本价格',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价（划线价）',
  `sku_image` VARCHAR(512) DEFAULT NULL COMMENT 'SKU图片URL',
  `weight` DECIMAL(10,2) DEFAULT NULL COMMENT '重量（克）',
  `volume` DECIMAL(10,2) DEFAULT NULL COMMENT '体积（立方厘米）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_code` (`sku_code`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU表';

-- 3.5 商品规格表（如颜色、尺寸）
DROP TABLE IF EXISTS `product_spec`;
CREATE TABLE `product_spec` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '规格ID',
  `spec_name` VARCHAR(64) NOT NULL COMMENT '规格名称',
  `spec_code` VARCHAR(64) DEFAULT NULL COMMENT '规格编码',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_spec_name` (`spec_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品规格表';

-- 3.6 商品规格属性表（如红色、L）
DROP TABLE IF EXISTS `product_spec_attr`;
CREATE TABLE `product_spec_attr` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '属性ID',
  `spec_id` BIGINT UNSIGNED NOT NULL COMMENT '规格ID',
  `attr_name` VARCHAR(64) NOT NULL COMMENT '属性名称',
  `attr_code` VARCHAR(64) DEFAULT NULL COMMENT '属性编码',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_spec_id` (`spec_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品规格属性表';

-- =============================================
-- 模块4：库存管理表
-- =============================================

-- 4.1 商品库存表
DROP TABLE IF EXISTS `product_stock`;
CREATE TABLE `product_stock` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `warehouse_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '仓库ID（默认1为总仓）',
  `total_stock` INT NOT NULL DEFAULT 0 COMMENT '总库存',
  `available_stock` INT NOT NULL DEFAULT 0 COMMENT '可用库存',
  `locked_stock` INT NOT NULL DEFAULT 0 COMMENT '锁定库存（待发货/待付款）',
  `warn_stock` INT NOT NULL DEFAULT 0 COMMENT '预警库存',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_warehouse` (`sku_id`, `warehouse_id`),
  KEY `idx_available_stock` (`available_stock`),
  KEY `idx_warn_stock` (`warn_stock`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品库存表';

-- 4.2 库存变动记录表
DROP TABLE IF EXISTS `stock_change_log`;
CREATE TABLE `stock_change_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '变动记录ID',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `warehouse_id` BIGINT UNSIGNED NOT NULL COMMENT '仓库ID',
  `change_type` VARCHAR(32) NOT NULL COMMENT '变动类型（关联system_dict的stock_change_type分组，如入库、出库、锁定、解锁）',
  `change_num` INT NOT NULL COMMENT '变动数量（正数增加，负数减少）',
  `before_stock` INT NOT NULL COMMENT '变动前可用库存',
  `after_stock` INT NOT NULL COMMENT '变动后可用库存',
  `before_locked` INT NOT NULL COMMENT '变动前锁定库存',
  `after_locked` INT NOT NULL COMMENT '变动后锁定库存',
  `related_order_no` VARCHAR(64) DEFAULT NULL COMMENT '关联单号（订单号/入库单号/出库单号）',
  `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID',
  `operator_type` TINYINT NOT NULL DEFAULT 1 COMMENT '操作人类型（1管理员 2系统 3会员）',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名',
  `change_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变动时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_change_type` (`change_type`),
  KEY `idx_related_order_no` (`related_order_no`),
  KEY `idx_change_time` (`change_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存变动记录表';

-- =============================================
-- 模块5：订单管理表
-- =============================================

-- 5.1 订单主表
DROP TABLE IF EXISTS `order_main`;
CREATE TABLE `order_main` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号（唯一）',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `consignee` VARCHAR(32) NOT NULL COMMENT '收货人',
  `mobile` VARCHAR(16) NOT NULL COMMENT '收货手机号',
  `province_code` VARCHAR(16) NOT NULL COMMENT '省份编码',
  `province_name` VARCHAR(32) NOT NULL COMMENT '省份名称',
  `city_code` VARCHAR(16) NOT NULL COMMENT '城市编码',
  `city_name` VARCHAR(32) NOT NULL COMMENT '城市名称',
  `district_code` VARCHAR(16) NOT NULL COMMENT '区县编码',
  `district_name` VARCHAR(32) NOT NULL COMMENT '区县名称',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `zip_code` VARCHAR(16) DEFAULT NULL COMMENT '邮编',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `freight_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费金额',
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `points_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '积分抵扣金额',
  `points_used` INT NOT NULL DEFAULT 0 COMMENT '使用积分数量',
  `pay_type` VARCHAR(32) DEFAULT NULL COMMENT '支付方式（关联system_dict的pay_type分组）',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `order_status` VARCHAR(32) NOT NULL DEFAULT 'WAIT_PAY' COMMENT '订单状态（关联system_dict的order_status分组）',
  `delivery_status` VARCHAR(32) NOT NULL DEFAULT 'UNDELIVERED' COMMENT '配送状态（关联system_dict的delivery_status分组）',
  `delivery_type` VARCHAR(32) DEFAULT NULL COMMENT '配送方式（关联system_dict的delivery_type分组）',
  `tracking_no` VARCHAR(64) DEFAULT NULL COMMENT '物流单号',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
  `comment_time` DATETIME DEFAULT NULL COMMENT '评价时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `buyer_remark` VARCHAR(255) DEFAULT NULL COMMENT '买家备注',
  `seller_remark` VARCHAR(255) DEFAULT NULL COMMENT '卖家备注',
  `auto_cancel_time` DATETIME DEFAULT NULL COMMENT '自动取消时间',
  `auto_confirm_time` DATETIME DEFAULT NULL COMMENT '自动确认收货时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除（1已删 0未删）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_delivery_status` (`delivery_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_pay_time` (`pay_time`),
  KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

-- 5.2 订单详情表
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '详情ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `spu_code` VARCHAR(64) NOT NULL COMMENT 'SPU编码',
  `spu_name` VARCHAR(255) NOT NULL COMMENT 'SPU名称',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
  `spec_attrs` VARCHAR(512) NOT NULL COMMENT '规格属性（JSON数组，同product_sku）',
  `sku_image` VARCHAR(512) DEFAULT NULL COMMENT 'SKU图片URL',
  `price` DECIMAL(10,2) NOT NULL COMMENT '购买单价',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `quantity` INT NOT NULL COMMENT '购买数量',
  `total_price` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单详情表';

-- 5.3 订单操作日志表
DROP TABLE IF EXISTS `order_operation_log`;
CREATE TABLE `order_operation_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号',
  `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID',
  `operator_type` TINYINT NOT NULL DEFAULT 1 COMMENT '操作人类型（1管理员 2会员 3系统）',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名',
  `operation_type` VARCHAR(32) NOT NULL COMMENT '操作类型（关联system_dict的order_operation_type分组）',
  `before_status` VARCHAR(32) DEFAULT NULL COMMENT '操作前订单状态',
  `after_status` VARCHAR(32) DEFAULT NULL COMMENT '操作后订单状态',
  `operation_desc` VARCHAR(255) NOT NULL COMMENT '操作描述',
  `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单操作日志表';

-- =============================================
-- 预设数据
-- =============================================

-- 1. 系统字典预设数据
INSERT INTO `system_dict` (`dict_code`, `dict_name`, `item_code`, `item_name`, `sort`, `status`) VALUES
-- 性别
('gender', '性别', 'UNKNOWN', '未知', 0, 1),
('gender', '性别', 'MALE', '男', 1, 1),
('gender', '性别', 'FEMALE', '女', 2, 1),
-- 会员等级
('member_level', '会员等级', 'NORMAL', '普通会员', 0, 1),
('member_level', '会员等级', 'SILVER', '白银会员', 1, 1),
('member_level', '会员等级', 'GOLD', '黄金会员', 2, 1),
('member_level', '会员等级', 'PLATINUM', '铂金会员', 3, 1),
-- 订单状态
('order_status', '订单状态', 'WAIT_PAY', '待付款', 0, 1),
('order_status', '订单状态', 'WAIT_DELIVERY', '待发货', 1, 1),
('order_status', '订单状态', 'WAIT_RECEIVE', '待收货', 2, 1),
('order_status', '订单状态', 'WAIT_COMMENT', '待评价', 3, 1),
('order_status', '订单状态', 'COMPLETED', '已完成', 4, 1),
('order_status', '订单状态', 'CANCELLED', '已取消', 5, 1),
('order_status', '订单状态', 'REFUNDING', '退款中', 6, 1),
('order_status', '订单状态', 'REFUNDED', '已退款', 7, 1),
-- 配送状态
('delivery_status', '配送状态', 'UNDELIVERED', '未发货', 0, 1),
('delivery_status', '配送状态', 'DELIVERED', '已发货', 1, 1),
('delivery_status', '配送状态', 'IN_TRANSIT', '运输中', 2, 1),
('delivery_status', '配送状态', 'DELIVERING', '派送中', 3, 1),
('delivery_status', '配送状态', 'RECEIVED', '已签收', 4, 1),
-- 支付方式
('pay_type', '支付方式', 'ALIPAY', '支付宝', 0, 1),
('pay_type', '支付方式', 'WECHAT', '微信支付', 1, 1),
('pay_type', '支付方式', 'BALANCE', '余额支付', 2, 1),
-- 配送方式
('delivery_type', '配送方式', 'EXPRESS', '快递', 0, 1),
('delivery_type', '配送方式', 'SELF_PICKUP', '自提', 1, 1),
('delivery_type', '配送方式', 'LOCAL_DELIVERY', '同城配送', 2, 1),
-- 库存变动类型
('stock_change_type', '库存变动类型', 'INBOUND', '入库', 0, 1),
('stock_change_type', '库存变动类型', 'OUTBOUND', '出库', 1, 1),
('stock_change_type', '库存变动类型', 'LOCK', '锁定', 2, 1),
('stock_change_type', '库存变动类型', 'UNLOCK', '解锁', 3, 1),
('stock_change_type', '库存变动类型', 'RETURN', '退货入库', 4, 1),
-- 订单操作类型
('order_operation_type', '订单操作类型', 'CREATE', '创建订单', 0, 1),
('order_operation_type', '订单操作类型', 'PAY', '支付订单', 1, 1),
('order_operation_type', '订单操作类型', 'DELIVERY', '发货', 2, 1),
('order_operation_type', '订单操作类型', 'RECEIVE', '收货', 3, 1),
('order_operation_type', '订单操作类型', 'COMMENT', '评价', 4, 1),
('order_operation_type', '订单操作类型', 'CANCEL', '取消订单', 5, 1),
('order_operation_type', '订单操作类型', 'REFUND', '申请退款', 6, 1);

-- 2. 系统配置预设数据
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_type`, `sort`, `status`) VALUES
('shop_name', '童趣优衣', '店铺名称', 1, 0, 1),
('shop_logo', '', '店铺Logo', 1, 1, 1),
('shop_description', '专注于高品质儿童服饰的销售平台', '店铺描述', 1, 2, 1),
('shop_contact', '400-888-8888', '店铺联系方式', 1, 3, 1),
('shop_address', '北京市朝阳区童趣路1号', '店铺地址', 1, 4, 1),
('freight_free_amount', '99.00', '包邮门槛（元）', 2, 5, 1),
('default_freight', '10.00', '默认运费（元）', 2, 6, 1),
('auto_cancel_minutes', '30', '订单自动取消时间（分钟）', 2, 7, 1),
('auto_confirm_days', '7', '订单自动确认收货时间（天）', 2, 8, 1),
('points_rate', '100', '积分抵扣比例（100积分=1元）', 2, 9, 1);

-- 3. 商品分类预设数据（示例）
INSERT INTO `product_category` (`parent_id`, `category_name`, `category_code`, `sort`, `level`, `status`) VALUES
-- 一级分类
(0, '0-1岁婴装', 'BABY_0_1', 0, 1, 1),
(0, '1-3岁童装', 'BABY_1_3', 1, 1, 1),
(0, '3-6岁童装', 'KID_3_6', 2, 1, 1),
(0, '6-12岁童装', 'KID_6_12', 3, 1, 1),
(0, '配饰专区', 'ACCESSORY', 4, 1, 1);

-- 4. 商品规格预设数据（示例）
INSERT INTO `product_spec` (`spec_name`, `spec_code`, `sort`) VALUES
('颜色', 'COLOR', 0),
('尺寸', 'SIZE', 1);

-- 5. 商品规格属性预设数据（示例）
INSERT INTO `product_spec_attr` (`spec_id`, `attr_name`, `attr_code`, `sort`) VALUES
-- 颜色属性
(1, '红色', 'RED', 0),
(1, '蓝色', 'BLUE', 1),
(1, '黄色', 'YELLOW', 2),
(1, '粉色', 'PINK', 3),
-- 尺寸属性（0-1岁）
(2, '59', '59', 0),
(2, '66', '66', 1),
(2, '73', '73', 2);
```