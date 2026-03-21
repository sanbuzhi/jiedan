===FILE:back/tongquyouyi.sql===
```sql
-- =============================================
-- 数据库名称：tongquyouyi (童趣优衣)
-- 版本：1.0.0
-- 适用：MySQL 8.0.35+
-- 存储引擎：InnoDB
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_unicode_ci
-- 作者：童趣优衣开发团队
-- 最后更新：202X-XX-XX
-- =============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `tongquyouyi` DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;

USE `tongquyouyi`;

-- =============================================
-- 公共表模块
-- =============================================

-- 1.1 系统字典表
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '字典ID',
  `dict_code` VARCHAR(64) NOT NULL COMMENT '字典编码（唯一）',
  `dict_name` VARCHAR(128) NOT NULL COMMENT '字典名称',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级字典ID（0表示顶级分类）',
  `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0禁用 1启用）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_code` (`dict_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统字典表（商品分类、支付方式等通用配置）';

-- 1.2 系统操作日志表
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `operator_type` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '操作者类型（1管理员 2会员）',
  `operator_id` BIGINT UNSIGNED NOT NULL COMMENT '操作者ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作者姓名/昵称',
  `module` VARCHAR(64) NOT NULL COMMENT '操作模块',
  `operation` VARCHAR(128) NOT NULL COMMENT '操作描述',
  `request_method` VARCHAR(16) DEFAULT NULL COMMENT '请求方法',
  `request_url` VARCHAR(256) DEFAULT NULL COMMENT '请求URL',
  `request_params` TEXT DEFAULT NULL COMMENT '请求参数（JSON格式）',
  `response_result` TEXT DEFAULT NULL COMMENT '响应结果（JSON格式，仅记录关键信息）',
  `ip_address` VARCHAR(45) DEFAULT NULL COMMENT 'IP地址（支持IPv6）',
  `browser` VARCHAR(128) DEFAULT NULL COMMENT '浏览器信息',
  `os` VARCHAR(128) DEFAULT NULL COMMENT '操作系统信息',
  `operation_status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '操作状态（0失败 1成功）',
  `error_msg` TEXT DEFAULT NULL COMMENT '错误信息（失败时记录）',
  `execution_time` INT UNSIGNED DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_operator_type` (`operator_type`),
  KEY `idx_module` (`module`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表';

-- =============================================
-- 会员管理模块
-- =============================================

-- 2.1 会员等级表
DROP TABLE IF EXISTS `member_level`;
CREATE TABLE `member_level` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '等级ID',
  `level_code` VARCHAR(64) NOT NULL COMMENT '等级编码（唯一）',
  `level_name` VARCHAR(64) NOT NULL COMMENT '等级名称',
  `level_icon` VARCHAR(256) DEFAULT NULL COMMENT '等级图标URL',
  `min_points` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '最低累计积分要求',
  `min_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '最低累计消费金额要求',
  `discount_rate` DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '折扣率（如0.95表示95折）',
  `points_rate` DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '积分倍率（如1.2表示消费1元得1.2积分）',
  `birthday_bonus_points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '生日额外赠送积分',
  `free_shipping_threshold` DECIMAL(10,2) NOT NULL DEFAULT 99.00 COMMENT '免运费门槛（元）',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0禁用 1启用）',
  `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level_code` (`level_code`),
  KEY `idx_min_points` (`min_points`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员等级表';

-- 2.2 会员主表
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  `member_no` VARCHAR(32) NOT NULL COMMENT '会员编号（唯一，自动生成）',
  `username` VARCHAR(64) NOT NULL COMMENT '登录用户名（唯一）',
  `password` VARCHAR(256) NOT NULL COMMENT '登录密码（BCrypt加密）',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '会员昵称',
  `real_name` VARCHAR(32) DEFAULT NULL COMMENT '真实姓名',
  `gender` TINYINT UNSIGNED DEFAULT 0 COMMENT '性别（0未知 1男 2女）',
  `avatar_url` VARCHAR(256) DEFAULT NULL COMMENT '头像URL',
  `mobile` VARCHAR(16) NOT NULL COMMENT '手机号（唯一，登录/注册用）',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `level_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '会员等级ID',
  `total_points` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计获得积分',
  `available_points` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '可用积分',
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费金额',
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0禁用 1正常 2锁定）',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(45) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_no` (`member_no`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_mobile` (`mobile`),
  KEY `idx_level_id` (`level_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_deleted` (`is_deleted`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员主表';

-- 2.3 会员积分流水表
DROP TABLE IF EXISTS `member_points_log`;
CREATE TABLE `member_points_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `log_no` VARCHAR(32) NOT NULL COMMENT '流水编号（唯一）',
  `change_type` TINYINT UNSIGNED NOT NULL COMMENT '变动类型（1消费获得 2兑换扣除 3活动赠送 4签到赠送 5退款返还 6管理员调整 7过期扣除）',
  `related_type` VARCHAR(64) DEFAULT NULL COMMENT '关联业务类型（如订单、兑换券、活动）',
  `related_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联业务ID',
  `points` BIGINT NOT NULL COMMENT '变动积分（正数增加 负数扣除）',
  `before_points` BIGINT UNSIGNED NOT NULL COMMENT '变动前可用积分',
  `after_points` BIGINT UNSIGNED NOT NULL COMMENT '变动后可用积分',
  `expire_time` DATETIME DEFAULT NULL COMMENT '积分过期时间（增加时设置，扣除时关联原过期记录）',
  `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作者ID（管理员调整时记录）',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作者姓名',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_log_no` (`log_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_change_type` (`change_type`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员积分流水表';

-- =============================================
-- 商品管理模块
-- =============================================

-- 3.1 商品分类表（与sys_dict关联，但作为独立业务表扩展）
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_code` VARCHAR(64) NOT NULL COMMENT '分类编码（唯一）',
  `category_name` VARCHAR(128) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级分类ID（0表示顶级分类）',
  `level` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '分类层级（最多3级）',
  `icon_url` VARCHAR(256) DEFAULT NULL COMMENT '分类图标URL',
  `banner_url` VARCHAR(256) DEFAULT NULL COMMENT '分类Banner URL',
  `seo_title` VARCHAR(128) DEFAULT NULL COMMENT 'SEO标题',
  `seo_keywords` VARCHAR(256) DEFAULT NULL COMMENT 'SEO关键词',
  `seo_description` VARCHAR(512) DEFAULT NULL COMMENT 'SEO描述',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0禁用 1启用）',
  `is_recommend` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否推荐（0否 1是）',
  `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`),
  KEY `idx_status` (`status`),
  KEY `idx_is_recommend` (`is_recommend`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 3.2 商品品牌表
DROP TABLE IF EXISTS `product_brand`;
CREATE TABLE `product_brand` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '品牌ID',
  `brand_code` VARCHAR(64) NOT NULL COMMENT '品牌编码（唯一）',
  `brand_name` VARCHAR(128) NOT NULL COMMENT '品牌名称',
  `brand_logo` VARCHAR(256) DEFAULT NULL COMMENT '品牌Logo URL',
  `brand_description` TEXT DEFAULT NULL COMMENT '品牌简介',
  `official_website` VARCHAR(256) DEFAULT NULL COMMENT '品牌官网',
  `seo_title` VARCHAR(128) DEFAULT NULL COMMENT 'SEO标题',
  `seo_keywords` VARCHAR(256) DEFAULT NULL COMMENT 'SEO关键词',
  `seo_description` VARCHAR(512) DEFAULT NULL COMMENT 'SEO描述',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0禁用 1启用）',
  `is_recommend` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否推荐（0否 1是）',
  `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_brand_code` (`brand_code`),
  KEY `idx_status` (`status`),
  KEY `idx_is_recommend` (`is_recommend`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品品牌表';

-- 3.3 商品主表（SPU）
DROP TABLE IF EXISTS `product_spu`;
CREATE TABLE `product_spu` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'SPU ID',
  `spu_no` VARCHAR(32) NOT NULL COMMENT 'SPU编号（唯一，自动生成）',
  `spu_name` VARCHAR(256) NOT NULL COMMENT 'SPU名称',
  `sub_title` VARCHAR(512) DEFAULT NULL COMMENT '副标题/卖点',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '商品分类ID',
  `brand_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '商品品牌ID',
  `main_image_url` VARCHAR(256) NOT NULL COMMENT '主图URL',
  `image_urls` TEXT DEFAULT NULL COMMENT '轮播图URL列表（JSON数组）',
  `detail_html` TEXT DEFAULT NULL COMMENT '商品详情（HTML格式）',
  `spec_template_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '规格模板ID',
  `spec_values` TEXT DEFAULT NULL COMMENT '规格值组合（JSON格式，如{"颜色":["红","蓝"],"尺码":["120","130"]}）',
  `unit` VARCHAR(32) NOT NULL DEFAULT '件' COMMENT '计量单位',
  `weight` DECIMAL(10,2) DEFAULT NULL COMMENT '商品重量（kg）',
  `volume` DECIMAL(10,2) DEFAULT NULL COMMENT '商品体积（m³）',
  `original_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '原价（最高SKU原价）',
  `sale_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '售价（最低SKU售价）',
  `stock` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '总库存（所有SKU库存之和）',
  `sales_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '销量',
  `view_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览量',
  `collect_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏量',
  `comment_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论数',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态（0下架 1上架 2待审核 3审核拒绝）',
  `is_new` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否新品（0否 1是）',
  `is_hot` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否热销（0否 1是）',
  `is_recommend` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否推荐（0否 1是）',
  `is_free_shipping` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否包邮（0否 1是）',
  `freight_template_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '运费模板ID',
  `seo_title` VARCHAR(128) DEFAULT NULL COMMENT 'SEO标题',
  `seo_keywords` VARCHAR(256) DEFAULT NULL COMMENT 'SEO关键词',
  `seo_description` VARCHAR(512) DEFAULT NULL COMMENT 'SEO描述',
  `publish_time` DATETIME DEFAULT NULL COMMENT '上架时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_no` (`spu_no`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_brand_id` (`brand_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_new` (`is_new`),
  KEY `idx_is_hot` (`is_hot`),
  KEY `idx_is_recommend` (`is_recommend`),
  KEY `idx_sales_count` (`sales_count`),
  KEY `idx_publish_time` (`publish_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品主表（SPU）';

-- 3.4 商品规格表（SKU）
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `sku_no` VARCHAR(32) NOT NULL COMMENT 'SKU编号（唯一，自动生成）',
  `spec_ids` VARCHAR(256) DEFAULT NULL COMMENT '规格项ID组合（逗号分隔，对应sys_dict字典项）',
  `spec_names` VARCHAR(512) DEFAULT NULL COMMENT '规格值组合（中文逗号分隔，如"红色,120码"）',
  `image_url` VARCHAR(256) DEFAULT NULL COMMENT 'SKU专属图片URL',
  `original_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'SKU原价',
  `sale_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'SKU售价',
  `cost_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'SKU成本价',
  `stock` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'SKU库存',
  `warn_stock` INT UNSIGNED NOT NULL DEFAULT 10 COMMENT '预警库存',
  `sales_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'SKU销量',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0禁用 1启用）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_no` (`sku_no`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_spec_ids` (`spec_ids`),
  KEY `idx_status` (`status`),
  KEY `idx_stock` (`stock`),
  KEY `idx_warn_stock` (`warn_stock`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品规格表（SKU）';

-- =============================================
-- 库存管理模块
-- =============================================

-- 4.1 库存流水表
DROP TABLE IF EXISTS `inventory_log`;
CREATE TABLE `inventory_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `log_no` VARCHAR(32) NOT NULL COMMENT '流水编号（唯一）',
  `change_type` TINYINT UNSIGNED NOT NULL COMMENT '变动类型（1采购入库 2销售出库 3退货入库 4换货出库 5换货入库 6盘点调整 7报损出库 8报溢入库 9调拨出库 10调拨入库）',
  `related_type` VARCHAR(64) DEFAULT NULL COMMENT '关联业务类型（如采购单、订单、盘点单）',
  `related_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联业务ID',
  `related_no` VARCHAR(32) DEFAULT NULL COMMENT '关联业务编号',
  `quantity` INT NOT NULL COMMENT '变动数量（正数增加 负数扣除）',
  `before_stock` INT UNSIGNED NOT NULL COMMENT '变动前库存',
  `after_stock` INT UNSIGNED NOT NULL COMMENT '变动后库存',
  `warehouse_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '仓库ID（预留）',
  `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作者ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作者姓名',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_log_no` (`log_no`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_change_type` (`change_type`),
  KEY `idx_related_id` (`related_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水表';

-- =============================================
-- 订单管理模块
-- =============================================

-- 5.1 订单主表
DROP TABLE IF EXISTS `order_main`;
CREATE TABLE `order_main` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号（唯一，自动生成，格式：TQYY+年月日+6位随机数）',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `member_name` VARCHAR(64) DEFAULT NULL COMMENT '会员昵称',
  `member_mobile` VARCHAR(16) DEFAULT NULL COMMENT '会员手机号',
  `total_quantity` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '商品总数量',
  `original_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品总原价',
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总优惠金额',
  `freight_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费金额',
  `payable_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '应付金额',
  `paid_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
  `points_used` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用积分',
  `points_deduction` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '积分抵扣金额',
  `balance_used` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '使用余额',
  `coupon_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '使用优惠券ID',
  `coupon_deduction` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠券抵扣金额',
  `level_discount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '会员等级折扣金额',
  `receiver_name` VARCHAR(64) NOT NULL COMMENT '收货人姓名',
  `receiver_mobile` VARCHAR(16) NOT NULL COMMENT '收货人手机号',
  `receiver_province` VARCHAR(64) NOT NULL COMMENT '收货省份',
  `receiver_city` VARCHAR(64) NOT NULL COMMENT '收货城市',
  `receiver_district` VARCHAR(64) NOT NULL COMMENT '收货区县',
  `receiver_detail` VARCHAR(512) NOT NULL COMMENT '收货详细地址',
  `receiver_zip_code` VARCHAR(16) DEFAULT NULL COMMENT '收货邮编',
  `order_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单状态（0待付款 1待发货 2已发货 3待收货 4已完成 5已取消 6已退款 7退款中）',
  `pay_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '支付状态（0未支付 1已支付 2支付失败 3已退款 4部分退款）',
  `delivery_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '配送状态（0未发货 1已发货 2配送中 3已签收 4拒收）',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(256) DEFAULT NULL COMMENT '取消原因',
  `cancel_operator` VARCHAR(64) DEFAULT NULL COMMENT '取消操作者',
  `logistics_company` VARCHAR(64) DEFAULT NULL COMMENT '物流公司',
  `logistics_no` VARCHAR(64) DEFAULT NULL COMMENT '物流单号',
  `user_remark` VARCHAR(512) DEFAULT NULL COMMENT '用户备注',
  `admin_remark` VARCHAR(512) DEFAULT NULL COMMENT '管理员备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_pay_status` (`pay_status`),
  KEY `idx_delivery_status` (`delivery_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_pay_time` (`pay_time`),
  KEY `idx_complete_time` (`complete_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

-- 5.2 订单子表
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '子订单ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单主表ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `spu_no` VARCHAR(32) NOT NULL COMMENT 'SPU编号',
  `spu_name` VARCHAR(256) NOT NULL COMMENT 'SPU名称',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `sku_no` VARCHAR(32) NOT NULL COMMENT 'SKU编号',
  `spec_names` VARCHAR(512) DEFAULT NULL COMMENT '规格值组合',
  `image_url` VARCHAR(256) DEFAULT NULL COMMENT '商品图片URL',
  `original_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品原价',
  `sale_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品售价',
  `cost_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品成本价',
  `quantity` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '购买数量',
  `total_original_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '该商品总原价',
  `total_discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '该商品总优惠金额',
  `total_payable_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '该商品应付金额',
  `points_given` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '该商品赠送积分',
  `comment_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论状态（0未评论 1已评论 2已追评）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_comment_status` (`comment_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单子表';

-- 5.3 订单支付记录表
DROP TABLE IF EXISTS `order_payment`;
CREATE TABLE `order_payment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单主表ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
  `payment_no` VARCHAR(32) NOT NULL COMMENT '支付流水号（唯一，自动生成）',
  `third_party_no` VARCHAR(128) DEFAULT NULL COMMENT '第三方支付流水号（微信/支付宝等）',
  `payment_method` TINYINT UNSIGNED NOT NULL COMMENT '支付方式（1微信支付 2支付宝支付 3余额支付 4积分+余额/第三方 5货到付款）',
  `payment_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '支付金额',
  `payment_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '支付状态（0待支付 1支付中 2支付成功 3支付失败 4已关闭）',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付完成时间',
  `fail_reason` VARCHAR(256) DEFAULT NULL COMMENT '支付失败原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_third_party_no` (`third_party_no`),
  KEY `idx_payment_method` (`payment_method`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单支付记录表';

-- =============================================
-- 预设数据模块
-- =============================================

-- 预设会员等级
INSERT INTO `member_level` (`level_code`, `level_name`, `level_icon`, `min_points`, `min_amount`, `discount_rate`, `points_rate`, `birthday_bonus_points`, `free_shipping_threshold`, `status`, `sort_order`, `remark`) VALUES
('LV0', '普通会员', NULL, 0, 0.00, 1.00, 1.00, 0, 99.00, 1, 0, '注册即成为普通会员'),
('LV1', '银卡会员', NULL, 1000, 500.00, 0.98, 1.10, 50, 79.00, 1, 1, '累计积分1000或消费500元'),
('LV2', '金卡会员', NULL, 5000, 2000.00, 0.95, 1.20, 200, 59.00, 1, 2, '累计积分5000或消费2000元'),
('LV3', '钻石会员', NULL, 20000, 8000.00, 0.90, 1.50, 500, 0.00, 1, 3, '累计积分20000或消费8000元');

-- 预设系统字典（支付方式、物流状态、性别、订单状态等）
INSERT INTO `sys_dict` (`dict_code`, `dict_name`, `parent_id`, `sort_order`, `status`, `remark`) VALUES
('PAYMENT_METHOD', '支付方式', 0, 1, 1, '订单支付方式'),
('WECHAT', '微信支付', (SELECT id FROM (SELECT id FROM sys_dict WHERE dict_code='PAYMENT_METHOD') t), 1, 1, NULL),
('ALIPAY', '支付宝支付', (SELECT id FROM (SELECT id FROM sys_dict WHERE dict_code='PAYMENT_METHOD') t), 2, 1, NULL),
('BALANCE', '余额支付', (SELECT id FROM (SELECT id FROM sys_dict WHERE dict_code='PAYMENT_METHOD') t), 3, 1, NULL),
('COD', '货到付款', (SELECT id FROM (SELECT id FROM sys_dict WHERE dict_code='PAYMENT_METHOD') t), 4, 0, '暂未开放'),
('GENDER', '性别', 0, 2, 1, '会员性别'),
('UNKNOWN', '未知', (SELECT id FROM (SELECT id FROM sys_dict WHERE dict_code='GENDER') t), 1, 1, NULL),
('MALE', '男', (SELECT id FROM (SELECT id FROM sys_dict WHERE dict_code='GENDER') t), 2, 1, NULL),
('FEMALE', '女', (SELECT id FROM (SELECT id FROM sys_dict WHERE dict_code='GENDER') t), 3, 1, NULL);

-- 预设商品分类（3级示例）
INSERT INTO `product_category` (`category_code`, `category_name`, `parent_id`, `level`, `is_recommend`, `sort_order`, `status`) VALUES
('CLOTHING', '童装服饰', 0, 1, 1, 1, 1),
('SHOES', '童鞋', 0, 1, 1, 2, 1),
('ACCESSORIES', '配饰', 0, 1, 0, 3, 1),
('BOYS', '男童', (SELECT id FROM (SELECT id FROM product_category WHERE category_code='CLOTHING') t), 2, 1, 1, 1),
('GIRLS', '女童', (SELECT id FROM (SELECT id FROM product_category WHERE category_code='CLOTHING') t), 2, 1, 2, 1),
('BABY', '婴童', (SELECT id FROM (SELECT id FROM product_category WHERE category_code='CLOTHING') t), 2, 0, 3, 1),
('T_SHIRT', 'T恤', (SELECT id FROM (SELECT id FROM product_category WHERE category_code='BOYS') t), 3, 1, 1, 1),
('SHIRT', '衬衫', (SELECT id FROM (SELECT id FROM product_category WHERE category_code='BOYS') t), 3, 0, 2, 1),
('DRESS', '连衣裙', (SELECT id FROM (SELECT id FROM product_category WHERE category_code='GIRLS') t), 3, 1, 1, 1),
('SKIRT', '半身裙', (SELECT id FROM (SELECT id FROM product_category WHERE category_code='GIRLS') t), 3, 0, 2, 1);
```