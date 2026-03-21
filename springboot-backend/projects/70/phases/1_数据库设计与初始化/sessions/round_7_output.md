===FILE:back/tongquyouyi.sql===
```sql
-- =====================================================
-- 项目名称：童趣游益（儿童玩具租赁+销售平台）
-- 数据库版本：MySQL 8.0.35
-- 存储引擎：InnoDB
-- 字符集：utf8mb4（支持emoji表情）
-- 排序规则：utf8mb4_general_ci
-- 创建时间：202X-XX-XX
-- =====================================================

-- 全局配置
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
SET time_zone = '+08:00';

-- -----------------------------------------------------
-- 公共表
-- -----------------------------------------------------

-- 系统配置表
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `config_desc` varchar(255) DEFAULT NULL COMMENT '配置描述',
  `is_system` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否系统配置（0否1是，系统配置不可删除）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统配置表';

-- 地区字典表（支持省市区三级联动）
DROP TABLE IF EXISTS `sys_region`;
CREATE TABLE `sys_region` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `region_code` varchar(20) NOT NULL COMMENT '地区编码',
  `region_name` varchar(100) NOT NULL COMMENT '地区名称',
  `region_level` tinyint unsigned NOT NULL COMMENT '地区级别（1省2市3区）',
  `parent_code` varchar(20) NOT NULL DEFAULT '0' COMMENT '上级地区编码',
  `sort_order` int unsigned NOT NULL DEFAULT '0' COMMENT '排序号',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除（0否1是）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_region_code` (`region_code`),
  KEY `idx_parent_code` (`parent_code`),
  KEY `idx_region_level` (`region_level`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='地区字典表';

-- 操作日志表
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned DEFAULT NULL COMMENT '操作人ID（前台/后台用户）',
  `user_type` tinyint unsigned NOT NULL COMMENT '操作人类型（1管理员2会员）',
  `user_name` varchar(100) DEFAULT NULL COMMENT '操作人姓名',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型',
  `operation_desc` varchar(255) DEFAULT NULL COMMENT '操作描述',
  `request_url` varchar(255) DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(20) DEFAULT NULL COMMENT '请求方法',
  `request_params` text COMMENT '请求参数',
  `response_result` text COMMENT '响应结果',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `operation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `execution_time` int unsigned DEFAULT NULL COMMENT '执行时间（毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_type` (`user_type`),
  KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

-- -----------------------------------------------------
-- 会员管理模块
-- -----------------------------------------------------

-- 会员等级表
DROP TABLE IF EXISTS `member_level`;
CREATE TABLE `member_level` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `level_name` varchar(50) NOT NULL COMMENT '等级名称',
  `level_icon` varchar(255) DEFAULT NULL COMMENT '等级图标',
  `min_points` bigint unsigned NOT NULL DEFAULT '0' COMMENT '最低积分要求',
  `discount_rate` decimal(3,2) NOT NULL DEFAULT '1.00' COMMENT '销售折扣率（0.01-1.00）',
  `rental_discount_rate` decimal(3,2) NOT NULL DEFAULT '1.00' COMMENT '租赁折扣率（0.01-1.00）',
  `points_rate` decimal(3,2) NOT NULL DEFAULT '1.00' COMMENT '积分倍率',
  `free_delivery_count` int unsigned NOT NULL DEFAULT '0' COMMENT '每月免费配送次数',
  `sort_order` int unsigned NOT NULL DEFAULT '0' COMMENT '排序号',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除（0否1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_min_points` (`min_points`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员等级表';

-- 会员主表
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `level_id` bigint unsigned NOT NULL DEFAULT '1' COMMENT '会员等级ID',
  `openid` varchar(100) DEFAULT NULL COMMENT '微信OpenID',
  `unionid` varchar(100) DEFAULT NULL COMMENT '微信UnionID',
  `nickname` varchar(100) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '性别（0未知1男2女）',
  `mobile` varchar(20) DEFAULT NULL COMMENT '手机号',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `id_card` varchar(18) DEFAULT NULL COMMENT '身份证号',
  `total_points` bigint unsigned NOT NULL DEFAULT '0' COMMENT '总积分',
  `available_points` bigint unsigned NOT NULL DEFAULT '0' COMMENT '可用积分',
  `frozen_points` bigint unsigned NOT NULL DEFAULT '0' COMMENT '冻结积分',
  `total_rental_count` int unsigned NOT NULL DEFAULT '0' COMMENT '总租赁次数',
  `current_rental_count` int unsigned NOT NULL DEFAULT '0' COMMENT '当前租赁次数',
  `monthly_free_delivery_used` int unsigned NOT NULL DEFAULT '0' COMMENT '本月免费配送已用次数',
  `is_verified` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否实名认证（0否1是）',
  `is_blacklisted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否黑名单（0否1是）',
  `blacklist_reason` varchar(255) DEFAULT NULL COMMENT '黑名单原因',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除（0否1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mobile` (`mobile`),
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_unionid` (`unionid`),
  KEY `idx_level_id` (`level_id`),
  KEY `idx_is_blacklisted` (`is_blacklisted`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_member_level` FOREIGN KEY (`level_id`) REFERENCES `member_level` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员主表';

-- 会员积分记录表
DROP TABLE IF EXISTS `member_points_log`;
CREATE TABLE `member_points_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id` bigint unsigned NOT NULL COMMENT '会员ID',
  `points_change` bigint NOT NULL COMMENT '积分变动（正增负减）',
  `points_type` tinyint unsigned NOT NULL COMMENT '积分类型（1注册2签到3消费4租赁5评价6邀请7兑换8退款9其他）',
  `related_id` bigint unsigned DEFAULT NULL COMMENT '关联业务ID（订单ID/兑换记录ID等）',
  `related_no` varchar(50) DEFAULT NULL COMMENT '关联业务号',
  `change_desc` varchar(255) DEFAULT NULL COMMENT '变动说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_points_type` (`points_type`),
  KEY `idx_related_id` (`related_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_points_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员积分记录表';

-- 会员地址表
DROP TABLE IF EXISTS `member_address`;
CREATE TABLE `member_address` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id` bigint unsigned NOT NULL COMMENT '会员ID',
  `consignee` varchar(100) NOT NULL COMMENT '收货人',
  `mobile` varchar(20) NOT NULL COMMENT '收货人手机号',
  `province_code` varchar(20) NOT NULL COMMENT '省编码',
  `city_code` varchar(20) NOT NULL COMMENT '市编码',
  `district_code` varchar(20) NOT NULL COMMENT '区编码',
  `detail_address` varchar(255) NOT NULL COMMENT '详细地址',
  `full_address` varchar(500) NOT NULL COMMENT '完整地址（自动拼接）',
  `is_default` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否默认地址（0否1是）',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除（0否1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_is_default` (`is_default`),
  CONSTRAINT `fk_address_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员地址表';

-- -----------------------------------------------------
-- 商品管理模块
-- -----------------------------------------------------

-- 商品分类表（支持多级分类）
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '上级分类ID',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `category_icon` varchar(255) DEFAULT NULL COMMENT '分类图标',
  `category_path` varchar(255) NOT NULL DEFAULT '' COMMENT '分类路径（逗号分隔的ID，如0,1,2）',
  `level` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '分类级别（1-3级）',
  `min_age` tinyint unsigned DEFAULT NULL COMMENT '适用最小年龄（月）',
  `max_age` tinyint unsigned DEFAULT NULL COMMENT '适用最大年龄（月）',
  `sort_order` int unsigned NOT NULL DEFAULT '0' COMMENT '排序号',
  `is_show` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '是否显示（0否1是）',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除（0否1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_category_path` (`category_path`),
  KEY `idx_level` (`level`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_is_show` (`is_show`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品分类表';

-- 商品品牌表
DROP TABLE IF EXISTS `product_brand`;
CREATE TABLE `product_brand` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `brand_name` varchar(100) NOT NULL COMMENT '品牌名称',
  `brand_logo` varchar(255) DEFAULT NULL COMMENT '品牌LOGO',
  `brand_desc` text COMMENT '品牌描述',
  `sort_order` int unsigned NOT NULL DEFAULT '0' COMMENT '排序号',
  `is_show` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '是否显示（0否1是）',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除（0否1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_is_show` (`is_show`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品品牌表';

-- 商品主表（SPU）
DROP TABLE IF EXISTS `product_spu`;
CREATE TABLE `product_spu` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `spu_no` varchar(50) NOT NULL COMMENT '商品SPU编号',
  `category_id` bigint unsigned NOT NULL COMMENT '分类ID',
  `brand_id` bigint unsigned DEFAULT NULL COMMENT '品牌ID',
  `product_name` varchar(200) NOT NULL COMMENT '商品名称',
  `product_subtitle` varchar(500) DEFAULT NULL COMMENT '商品副标题',
  `main_image` varchar(255) NOT NULL COMMENT '商品主图',
  `sub_images` text COMMENT '商品副图（逗号分隔的URL）',
  `video_url` varchar(255) DEFAULT NULL COMMENT '商品视频URL',
  `min_age` tinyint unsigned DEFAULT NULL COMMENT '适用最小年龄（月）',
  `max_age` tinyint unsigned DEFAULT NULL COMMENT '适用最大年龄（月）',
  `tags` varchar(255) DEFAULT NULL COMMENT '商品标签（逗号分隔）',
  `detail_content` text COMMENT '商品详情（富文本）',
  `sale_type` tinyint unsigned NOT NULL DEFAULT '3' COMMENT '销售类型（1仅租赁2仅销售3租赁+销售）',
  `is_new` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否新品（0否1是）',
  `is_hot` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否热销（0否1是）',
  `is_recommend` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否推荐（0否1是）',
  `sort_order` int unsigned NOT NULL DEFAULT '0' COMMENT '排序号',
  `sale_count` int unsigned NOT NULL DEFAULT '0' COMMENT '销售数量',
  `rental_count` int unsigned NOT NULL DEFAULT '0' COMMENT '租赁次数',
  `view_count` int unsigned NOT NULL DEFAULT '0' COMMENT '浏览次数',
  `status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '商品状态（0草稿1上架2下架）',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除（0否1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_no` (`spu_no`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_brand_id` (`brand_id`),
  KEY `idx_sale_type` (`sale_type`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_spu_category` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_spu_brand` FOREIGN KEY (`brand_id`) REFERENCES `product_brand` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品主表（SPU）';

-- 商品规格组表
DROP TABLE IF EXISTS `product_spec_group`;
CREATE TABLE `product_spec_group` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `spec_group_name` varchar(100) NOT NULL COMMENT '规格组名称',
  `sort_order` int unsigned NOT NULL DEFAULT '0' COMMENT '排序号',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除（0否1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品规格组表';

-- 商品规格值表
DROP TABLE IF EXISTS `product_spec_value`;
CREATE TABLE `product_spec_value` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `spec_group_id` bigint unsigned NOT NULL COMMENT '规格组ID',
  `spec_value` varchar(100) NOT NULL COMMENT '规格值',
  `sort_order` int unsigned NOT NULL DEFAULT '0' COMMENT '排序号',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除（0否1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_spec_group_id` (`spec_group_id`),
  KEY `idx_sort_order` (`sort_order`),
  CONSTRAINT `fk_spec_value_group` FOREIGN KEY (`spec_group_id`) REFERENCES `product_spec_group` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品规格值表';

-- 商品SKU表
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `spu_id` bigint unsigned NOT NULL COMMENT 'SPU ID',
  `sku_no` varchar(50) NOT NULL COMMENT '商品SKU编号',
  `spec_ids` varchar(255) NOT NULL COMMENT '规格值ID集合（逗号分隔）',
  `spec_desc` varchar(500) NOT NULL COMMENT '规格描述（如：颜色-红色,尺寸-大）',
  `sku_image` varchar(255) DEFAULT NULL COMMENT 'SKU图片',
  `sale_price` decimal(10,2) DEFAULT NULL COMMENT '销售价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `rental_daily_price` decimal(10,2) DEFAULT NULL COMMENT '租赁日单价',
  `rental_weekly_price` decimal(10,2) DEFAULT NULL COMMENT '租赁周单价',
  `rental_monthly_price` decimal(10,2) DEFAULT NULL COMMENT '租赁月单价',
  `deposit` decimal(10,2) DEFAULT NULL COMMENT '租赁押金',
  `total_stock` int unsigned NOT NULL DEFAULT '0' COMMENT '总库存',
  `available_stock` int unsigned NOT NULL DEFAULT '0' COMMENT '可用库存',
  `frozen_stock` int unsigned NOT NULL DEFAULT '0' COMMENT '冻结库存',
  `rental_stock` int unsigned NOT NULL DEFAULT '0' COMMENT '在租库存',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT 'SKU状态（0禁用1启用）',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除（0否1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_no` (`sku_no`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_sku_spu` FOREIGN KEY (`spu_id`) REFERENCES `product_spu` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品SKU表';

-- -----------------------------------------------------
-- 库存管理模块
-- -----------------------------------------------------

-- 库存流水表
DROP TABLE IF EXISTS `stock_log`;
CREATE TABLE `stock_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sku_id` bigint unsigned NOT NULL COMMENT 'SKU ID',
  `spu_id` bigint unsigned NOT NULL COMMENT 'SPU ID',
  `stock_change` int NOT NULL COMMENT '库存变动（正增负减）',
  `stock_type` tinyint unsigned NOT NULL COMMENT '库存类型（1采购入库2租赁归还3销售退货4其他入库5租赁出库6销售出库7其他出库8库存调整）',
  `related_id` bigint unsigned DEFAULT NULL COMMENT '关联业务ID',
  `related_no` varchar(50) DEFAULT NULL COMMENT '关联业务号',
  `before_stock` int unsigned NOT NULL COMMENT '变动前库存',
  `after_stock` int unsigned NOT NULL COMMENT '变动后库存',
  `change_desc` varchar(255) DEFAULT NULL COMMENT '变动说明',
  `operator_id` bigint unsigned DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) DEFAULT NULL COMMENT '操作人姓名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_stock_type` (`stock_type`),
  KEY `idx_related_id` (`related_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_stock_sku` FOREIGN KEY (`sku_id`) REFERENCES `product_sku` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='库存流水表';

-- -----------------------------------------------------
-- 订单管理模块
-- -----------------------------------------------------

-- 订单主表
DROP TABLE IF EXISTS `order_master`;
CREATE TABLE `order_master` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `member_id` bigint unsigned NOT NULL COMMENT '会员ID',
  `order_type` tinyint unsigned NOT NULL COMMENT '订单类型（1销售2租赁）',
  `total_amount` decimal(12,2) NOT NULL COMMENT '订单总金额',
  `pay_amount` decimal(12,2) NOT NULL COMMENT '实付金额',
  `discount_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '优惠金额',
  `freight_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '运费金额',
  `deposit_amount` decimal(12,2) DEFAULT NULL COMMENT '押金金额（仅租赁订单）',
  `points_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '积分抵扣金额',
  `used_points` bigint unsigned NOT NULL DEFAULT '0' COMMENT '使用积分',
  `coupon_id` bigint unsigned DEFAULT NULL COMMENT '优惠券ID',
  `consignee` varchar(100) NOT NULL COMMENT '收货人',
  `mobile` varchar(20) NOT NULL COMMENT '收货人手机号',
  `province_code` varchar(20) NOT NULL COMMENT '省编码',
  `city_code` varchar(20) NOT NULL COMMENT '市编码',
  `district_code` varchar(20) NOT NULL COMMENT '区编码',
  `detail_address` varchar(255) NOT NULL COMMENT '详细地址',
  `full_address` varchar(500) NOT NULL COMMENT '完整地址',
  `buyer_remark` varchar(500) DEFAULT NULL COMMENT '买家备注',
  `seller_remark` varchar(500) DEFAULT NULL COMMENT '卖家备注',
  `pay_type` tinyint unsigned DEFAULT NULL COMMENT '支付方式（1微信2支付宝3余额4混合）',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime DEFAULT NULL COMMENT '收货时间',
  `rental_start_time` datetime DEFAULT NULL COMMENT '租赁开始时间（仅租赁订单）',
  `rental_end_time` datetime DEFAULT NULL COMMENT '租赁结束时间（仅租赁订单）',
  `rental_days` int unsigned DEFAULT NULL COMMENT '租赁天数（仅租赁订单）',
  `return_time` datetime DEFAULT NULL COMMENT '归还时间（仅租赁订单）',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `order_status` tinyint unsigned NOT NULL COMMENT '订单状态（销售：0待付款1待发货2待收货3已完成4已取消5退款中6已退款；租赁：0待付款1待发货2待收货3租赁中4待归还5归还验收中6押金待退7已完成8已取消9退款中10已退款）',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除（0否1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_order_type` (`order_type`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_pay_time` (`pay_time`),
  CONSTRAINT `fk_order_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单主表';

-- 订单详情表
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint unsigned NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `spu_id` bigint unsigned NOT NULL COMMENT 'SPU ID',
  `sku_id` bigint unsigned NOT NULL COMMENT 'SKU ID',
  `product_name` varchar(200) NOT NULL COMMENT '商品名称',
  `spec_desc` varchar(500) DEFAULT NULL COMMENT '规格描述',
  `main_image` varchar(255) NOT NULL COMMENT '商品主图',
  `quantity` int unsigned NOT NULL COMMENT '购买/租赁数量',
  `unit_price` decimal(10,2) NOT NULL COMMENT '单价（销售为售价，租赁为所选周期单价）',
  `total_price` decimal(12,2) NOT NULL COMMENT '小计金额',
  `deposit_unit_price` decimal(10,2) DEFAULT NULL COMMENT '单件押金（仅租赁订单）',
  `deposit_total_price` decimal(12,2) DEFAULT NULL COMMENT '总押金（仅租赁订单）',
  `is_commented` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否已评价（0否1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_sku_id` (`sku_id`),
  CONSTRAINT `fk_detail_order` FOREIGN KEY (`order_id`) REFERENCES `order_master` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_detail_sku` FOREIGN KEY (`sku_id`) REFERENCES `product_sku` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单详情表';

-- 订单操作日志表
DROP TABLE IF EXISTS `order_operation_log`;
CREATE TABLE `order_operation_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint unsigned NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `operator_id` bigint unsigned DEFAULT NULL COMMENT '操作人ID',
  `operator_type` tinyint unsigned NOT NULL COMMENT '操作人类型（1系统2管理员3会员）',
  `operator_name` varchar(100) DEFAULT NULL COMMENT '操作人姓名',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型',
  `operation_desc` varchar(255) DEFAULT NULL COMMENT '操作描述',
  `before_status` tinyint unsigned DEFAULT NULL COMMENT '操作前状态',
  `after_status` tinyint unsigned DEFAULT NULL COMMENT '操作后状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_op_log_order` FOREIGN KEY (`order_id`) REFERENCES `order_master` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单操作日志表';

-- -----------------------------------------------------
-- 预设数据
-- -----------------------------------------------------

-- 预设会员等级
INSERT INTO `member_level` (`id`, `level_name`, `level_icon`, `min_points`, `discount_rate`, `rental_discount_rate`, `points_rate`, `free_delivery_count`, `sort_order`, `is_system`, `create_time`, `update_time`) VALUES
(1, '普通会员', NULL, 0, 1.00, 1.00, 1.00, 0, 1, 1, NOW(), NOW()),
(2, '银卡会员', NULL, 1000, 0.98, 0.95, 1.20, 1, 2, 1, NOW(), NOW()),
(3, '金卡会员', NULL, 5000, 0.95, 0.90, 1.50, 3, 3, 1, NOW(), NOW()),
(4, '钻石会员', NULL, 20000, 0.90, 0.85, 2.00, 5, 4, 1, NOW(), NOW());

-- 预设商品分类
INSERT INTO `product_category` (`id`, `parent_id`, `category_name`, `category_icon`, `category_path`, `level`, `min_age`, `max_age`, `sort_order`, `is_show`, `is_deleted`, `create_time`, `update_time`) VALUES
(1, 0, '益智玩具', NULL, '0,1', 1, 0, 72, 1, 1, 0, NOW(), NOW()),
(2, 0, '积木拼搭', NULL, '0,2', 1, 12, 144, 2, 1, 0, NOW(), NOW()),
(3, 0, '遥控电动', NULL, '0,3', 1, 36, 144, 3, 1, 0, NOW(), NOW()),
(4, 1, '拼图', NULL, '0,1,4', 2, 12, 72, 1, 1, 0, NOW(), NOW()),
(5, 1, '画板', NULL, '0,1,5', 2, 24, 72, 2, 1, 0, NOW(), NOW()),
(6, 2, '颗粒积木', NULL, '0,2,6', 2, 36, 144, 1, 1, 0, NOW(), NOW()),
(7, 2, '雪花片', NULL, '0,2,7', 2, 24, 72, 2, 1, 0, NOW(), NOW());

-- 预设规格组
INSERT INTO `product_spec_group` (`id`, `spec_group_name`, `sort_order`, `is_deleted`, `create_time`, `update_time`) VALUES
(1, '颜色', 1, 0, NOW(), NOW()),
(2, '尺寸', 2, 0, NOW(), NOW()),
(3, '包装', 3, 0, NOW(), NOW());

-- 预设规格值
INSERT INTO `product_spec_value` (`id`, `spec_group_id`, `spec_value`, `sort_order`, `is_deleted`, `create_time`, `update_time`) VALUES
(1, 1, '红色', 1, 0, NOW(), NOW()),
(2, 1, '蓝色', 2, 0, NOW(), NOW()),
(3, 1, '黄色', 3, 0, NOW(), NOW()),
(4, 2, '小号', 1, 0, NOW(), NOW()),
(5, 2, '中号', 2, 0, NOW(), NOW()),
(6, 2, '大号', 3, 0, NOW(), NOW());

-- 预设系统配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_desc`, `is_system`, `create_time`, `update_time`) VALUES
('site_name', '童趣游益', '网站名称', 1, NOW(), NOW()),
('site_logo', '', '网站LOGO', 0, NOW(), NOW()),
('free_delivery_threshold', '99.00', '包邮门槛（元）', 0, NOW(), NOW()),
('default_freight', '10.00', '默认运费（元）', 0, NOW(), NOW()),
('order_auto_cancel_time', '30', '订单自动取消时间（分钟）', 0, NOW(), NOW()),
('order_auto_confirm_time', '7', '订单自动确认收货时间（天）', 0, NOW(), NOW()),
('order_auto_comment_time', '15', '订单自动评价时间（天）', 0, NOW(), NOW()),
('points_to_money_ratio', '100', '积分与人民币兑换比例（100积分=1元）', 0, NOW(), NOW()),
('sign_in_points', '10', '每日签到积分', 0, NOW(), NOW()),
('consumption_points_rate', '1.00', '消费获得积分倍率（每消费1元获得的积分）', 0, NOW(), NOW());

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;
```