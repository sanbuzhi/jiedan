===FILE:back/tongquyouyi.sql===
```sql
-- =============================================
-- 数据库名称：tongquyouyi
-- 适用版本：MySQL 8.0.35
-- 存储引擎：InnoDB
-- 字符集/排序规则：utf8mb4 / utf8mb4_0900_ai_ci
-- 功能：童趣友谊（母婴/儿童玩具社交电商）数据库初始化脚本
-- 创建日期：202X-XX-XX
-- =============================================

-- 1. 创建数据库并设置字符集
CREATE DATABASE IF NOT EXISTS `tongquyouyi`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_0900_ai_ci
DEFAULT ENCRYPTION = 'N';

USE `tongquyouyi`;

-- =============================================
-- 2. 公共表模块
-- =============================================

-- 2.1 系统配置表
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键（唯一）',
  `config_value` TEXT NOT NULL COMMENT '配置值',
  `config_desc` VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
  `group_name` VARCHAR(50) DEFAULT 'common' COMMENT '配置分组（common/user/order/payment等）',
  `is_editable` TINYINT UNSIGNED DEFAULT 1 COMMENT '是否可编辑（0不可/1可）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_group_name` (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置表';

-- 2.2 区域表（省市区三级联动）
DROP TABLE IF EXISTS `sys_region`;
CREATE TABLE `sys_region` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID（统一编码，参考GB/T 2260-2020）',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级ID（0为省级）',
  `region_name` VARCHAR(50) NOT NULL COMMENT '区域名称',
  `region_level` TINYINT UNSIGNED NOT NULL COMMENT '区域级别（1省/2市/3区）',
  `sort_order` INT UNSIGNED DEFAULT 0 COMMENT '排序',
  `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除（0否/1是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_level` (`parent_id`, `region_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='区域表';

-- 2.3 文件上传记录表
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径',
  `file_size` BIGINT UNSIGNED NOT NULL COMMENT '文件大小（字节）',
  `file_type` VARCHAR(50) NOT NULL COMMENT '文件MIME类型',
  `file_ext` VARCHAR(20) NOT NULL COMMENT '文件扩展名',
  `storage_type` TINYINT UNSIGNED DEFAULT 1 COMMENT '存储类型（1本地/2阿里云OSS/3腾讯云COS）',
  `upload_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '上传用户ID',
  `upload_user_type` TINYINT UNSIGNED DEFAULT 1 COMMENT '上传用户类型（1会员/2管理员）',
  `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除（0否/1是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_upload_user` (`upload_user_id`, `upload_user_type`),
  KEY `idx_storage_type` (`storage_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件上传记录表';

-- =============================================
-- 3. 会员管理表模块
-- =============================================

-- 3.1 会员基础信息表
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mobile` VARCHAR(11) DEFAULT NULL COMMENT '手机号（唯一登录标识）',
  `wechat_openid` VARCHAR(100) DEFAULT NULL COMMENT '微信小程序openid',
  `wechat_unionid` VARCHAR(100) DEFAULT NULL COMMENT '微信开放平台unionid',
  `nickname` VARCHAR(50) NOT NULL DEFAULT '童趣用户' COMMENT '昵称',
  `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `gender` TINYINT UNSIGNED DEFAULT 0 COMMENT '性别（0未知/1男/2女）',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `member_level` TINYINT UNSIGNED DEFAULT 1 COMMENT '会员等级（1普通/2银卡/3金卡/4钻石）',
  `points` BIGINT UNSIGNED DEFAULT 0 COMMENT '积分',
  `balance` DECIMAL(10,2) UNSIGNED DEFAULT 0.00 COMMENT '账户余额',
  `invite_code` VARCHAR(20) DEFAULT NULL COMMENT '邀请码（唯一）',
  `inviter_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '邀请人ID',
  `register_source` TINYINT UNSIGNED DEFAULT 1 COMMENT '注册来源（1小程序/2APP/3H5/4后台）',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态（0禁用/1正常）',
  `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除（0否/1是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mobile` (`mobile`),
  UNIQUE KEY `uk_wechat_openid` (`wechat_openid`),
  UNIQUE KEY `uk_wechat_unionid` (`wechat_unionid`),
  UNIQUE KEY `uk_invite_code` (`invite_code`),
  KEY `idx_inviter_id` (`inviter_id`),
  KEY `idx_member_level` (`member_level`),
  KEY `idx_register_source` (`register_source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员基础信息表';

-- 3.2 会员地址表
DROP TABLE IF EXISTS `member_address`;
CREATE TABLE `member_address` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `consignee_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `consignee_mobile` VARCHAR(11) NOT NULL COMMENT '收货人手机号',
  `province_id` BIGINT UNSIGNED NOT NULL COMMENT '省份ID',
  `province_name` VARCHAR(50) NOT NULL COMMENT '省份名称',
  `city_id` BIGINT UNSIGNED NOT NULL COMMENT '城市ID',
  `city_name` VARCHAR(50) NOT NULL COMMENT '城市名称',
  `district_id` BIGINT UNSIGNED NOT NULL COMMENT '区县ID',
  `district_name` VARCHAR(50) NOT NULL COMMENT '区县名称',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `postal_code` VARCHAR(10) DEFAULT NULL COMMENT '邮政编码',
  `is_default` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否默认地址（0否/1是）',
  `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除（0否/1是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员地址表';

-- =============================================
-- 4. 商品管理表模块
-- =============================================

-- 4.1 商品分类表
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级ID（0为一级）',
  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `category_icon` VARCHAR(500) DEFAULT NULL COMMENT '分类图标URL',
  `category_banner` VARCHAR(500) DEFAULT NULL COMMENT '分类横幅URL',
  `sort_order` INT UNSIGNED DEFAULT 0 COMMENT '排序',
  `is_show` TINYINT UNSIGNED DEFAULT 1 COMMENT '是否显示（0否/1是）',
  `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除（0否/1是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_is_show` (`is_show`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';

-- 4.2 商品品牌表
DROP TABLE IF EXISTS `product_brand`;
CREATE TABLE `product_brand` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `brand_name` VARCHAR(50) NOT NULL COMMENT '品牌名称（唯一）',
  `brand_logo` VARCHAR(500) DEFAULT NULL COMMENT '品牌Logo URL',
  `brand_desc` TEXT DEFAULT NULL COMMENT '品牌简介',
  `sort_order` INT UNSIGNED DEFAULT 0 COMMENT '排序',
  `is_show` TINYINT UNSIGNED DEFAULT 1 COMMENT '是否显示（0否/1是）',
  `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除（0否/1是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_brand_name` (`brand_name`),
  KEY `idx_is_show` (`is_show`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品品牌表';

-- 4.3 商品SPU表
DROP TABLE IF EXISTS `product_spu`;
CREATE TABLE `product_spu` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `spu_no` VARCHAR(50) NOT NULL COMMENT 'SPU编号（唯一）',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '商品分类ID（三级）',
  `brand_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '商品品牌ID',
  `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `product_subtitle` VARCHAR(500) DEFAULT NULL COMMENT '商品副标题',
  `main_image` VARCHAR(500) NOT NULL COMMENT '主图URL',
  `image_list` TEXT DEFAULT NULL COMMENT '商品轮播图URL列表（JSON格式）',
  `detail_html` TEXT DEFAULT NULL COMMENT '商品详情（HTML格式）',
  `price_range` VARCHAR(50) DEFAULT NULL COMMENT '价格区间（冗余字段，便于展示）',
  `sale_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '销量',
  `view_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '浏览量',
  `collect_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '收藏量',
  `sort_order` INT UNSIGNED DEFAULT 0 COMMENT '排序',
  `is_new` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否新品（0否/1是）',
  `is_hot` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否热销（0否/1是）',
  `is_recommend` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否推荐（0否/1是）',
  `is_on_sale` TINYINT UNSIGNED DEFAULT 1 COMMENT '是否上架（0下架/1上架）',
  `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除（0否/1是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_no` (`spu_no`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_brand_id` (`brand_id`),
  KEY `idx_is_on_sale` (`is_on_sale`),
  KEY `idx_sale_count` (`sale_count` DESC),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SPU表';

-- 4.4 商品SKU表
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `sku_no` VARCHAR(50) NOT NULL COMMENT 'SKU编号（唯一）',
  `sku_name` VARCHAR(200) NOT NULL COMMENT 'SKU名称（如：红色/3岁+）',
  `spec_values` TEXT DEFAULT NULL COMMENT '规格值组合（JSON格式，如：[{"specId":1,"specName":"颜色","specValue":"红色"},{"specId":2,"specName":"年龄","specValue":"3岁+"}]）',
  `sku_image` VARCHAR(500) DEFAULT NULL COMMENT 'SKU图片URL',
  `original_price` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '原价',
  `sale_price` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '售价',
  `cost_price` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '成本价',
  `weight` DECIMAL(10,3) UNSIGNED DEFAULT 0.000 COMMENT '重量（kg）',
  `volume` DECIMAL(10,3) UNSIGNED DEFAULT 0.000 COMMENT '体积（m³）',
  `is_on_sale` TINYINT UNSIGNED DEFAULT 1 COMMENT '是否上架（0下架/1上架）',
  `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除（0否/1是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_no` (`sku_no`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_is_on_sale` (`is_on_sale`),
  KEY `idx_sale_price` (`sale_price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SKU表';

-- =============================================
-- 5. 库存管理表模块
-- =============================================

-- 5.1 仓库表
DROP TABLE IF EXISTS `warehouse`;
CREATE TABLE `warehouse` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_name` VARCHAR(100) NOT NULL COMMENT '仓库名称',
  `warehouse_code` VARCHAR(50) NOT NULL COMMENT '仓库编码（唯一）',
  `province_id` BIGINT UNSIGNED NOT NULL COMMENT '省份ID',
  `city_id` BIGINT UNSIGNED NOT NULL COMMENT '城市ID',
  `district_id` BIGINT UNSIGNED NOT NULL COMMENT '区县ID',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
  `contact_mobile` VARCHAR(11) DEFAULT NULL COMMENT '联系人手机号',
  `is_default` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否默认仓库（0否/1是）',
  `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态（0停用/1启用）',
  `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除（0否/1是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_code` (`warehouse_code`),
  KEY `idx_province_city` (`province_id`, `city_id`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仓库表';

-- 5.2 SKU库存表
DROP TABLE IF EXISTS `sku_stock`;
CREATE TABLE `sku_stock` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_id` BIGINT UNSIGNED NOT NULL COMMENT '仓库ID',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `available_stock` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '可用库存',
  `locked_stock` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '锁定库存',
  `total_stock` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '总库存（冗余字段=可用+锁定）',
  `warning_stock` BIGINT UNSIGNED DEFAULT 10 COMMENT '预警库存',
  `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除（0否/1是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_sku` (`warehouse_id`, `sku_id`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_warning_stock` (`available_stock`, `warning_stock`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SKU库存表';

-- 5.3 库存变动记录表
DROP TABLE IF EXISTS `stock_log`;
CREATE TABLE `stock_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_id` BIGINT UNSIGNED NOT NULL COMMENT '仓库ID',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `change_type` TINYINT UNSIGNED NOT NULL COMMENT '变动类型（1入库/2出库/3锁定/4解锁/5盘点调整/6退货入库/7其他）',
  `change_qty` BIGINT NOT NULL COMMENT '变动数量（正增负减）',
  `before_qty` BIGINT UNSIGNED NOT NULL COMMENT '变动前可用库存',
  `after_qty` BIGINT UNSIGNED NOT NULL COMMENT '变动后可用库存',
  `related_no` VARCHAR(50) DEFAULT NULL COMMENT '关联单号（如订单号、入库单号、盘点单号等）',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID',
  `operator_type` TINYINT UNSIGNED DEFAULT 1 COMMENT '操作人类型（1系统/2管理员）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变动时间',
  PRIMARY KEY (`id`),
  KEY `idx_warehouse_sku` (`warehouse_id`, `sku_id`),
  KEY `idx_change_type` (`change_type`),
  KEY `idx_related_no` (`related_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存变动记录表';

-- =============================================
-- 6. 订单管理表模块
-- =============================================

-- 6.1 订单主表
DROP TABLE IF EXISTS `order_main`;
CREATE TABLE `order_main` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号（唯一）',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `inviter_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '邀请人ID（冗余，便于分销统计）',
  `order_type` TINYINT UNSIGNED DEFAULT 1 COMMENT '订单类型（1普通订单/2拼团订单/3秒杀订单/4积分兑换订单）',
  `pay_type` TINYINT UNSIGNED DEFAULT NULL COMMENT '支付方式（1微信支付/2支付宝/3余额支付/4组合支付）',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `pay_no` VARCHAR(100) DEFAULT NULL COMMENT '第三方支付流水号',
  `total_amount` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '订单总金额',
  `payable_amount` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '应付金额',
  `freight_amount` DECIMAL(10,2) UNSIGNED DEFAULT 0.00 COMMENT '运费金额',
  `discount_amount` DECIMAL(10,2) UNSIGNED DEFAULT 0.00 COMMENT '优惠金额',
  `points_used` BIGINT UNSIGNED DEFAULT 0 COMMENT '使用积分',
  `balance_used` DECIMAL(10,2) UNSIGNED DEFAULT 0.00 COMMENT '使用余额',
  `consignee_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `consignee_mobile` VARCHAR(11) NOT NULL COMMENT '收货人手机号',
  `province_id` BIGINT UNSIGNED NOT NULL COMMENT '省份ID',
  `province_name` VARCHAR(50) NOT NULL COMMENT '省份名称',
  `city_id` BIGINT UNSIGNED NOT NULL COMMENT '城市ID',
  `city_name` VARCHAR(50) NOT NULL COMMENT '城市名称',
  `district_id` BIGINT UNSIGNED NOT NULL COMMENT '区县ID',
  `district_name` VARCHAR(50) NOT NULL COMMENT '区县名称',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `postal_code` VARCHAR(10) DEFAULT NULL COMMENT '邮政编码',
  `delivery_no` VARCHAR(100) DEFAULT NULL COMMENT '物流单号',
  `delivery_company` VARCHAR(50) DEFAULT NULL COMMENT '物流公司',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '订单备注',
  `order_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单状态（0待支付/1待发货/2待收货/3已完成/4已取消/5已退款）',
  `cancel_reason` VARCHAR(500) DEFAULT NULL COMMENT '取消原因',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `auto_cancel_time` DATETIME DEFAULT NULL COMMENT '自动取消时间',
  `auto_confirm_time` DATETIME DEFAULT NULL COMMENT '自动确认收货时间',
  `is_commented` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否已评价（0否/1是）',
  `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除（0否/1是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_inviter_id` (`inviter_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_create_time` (`create_time` DESC),
  KEY `idx_pay_time` (`pay_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单主表';

-- 6.2 订单明细表
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号（冗余）',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `spu_name` VARCHAR(200) NOT NULL COMMENT 'SPU名称（冗余）',
  `sku_name` VARCHAR(200) NOT NULL COMMENT 'SKU名称（冗余）',
  `sku_image` VARCHAR(500) DEFAULT NULL COMMENT 'SKU图片（冗余）',
  `spec_values` TEXT DEFAULT NULL COMMENT '规格值组合（冗余，JSON格式）',
  `original_price` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '原价（冗余）',
  `sale_price` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '售价（冗余）',
  `cost_price` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '成本价（冗余）',
  `buy_qty` INT UNSIGNED NOT NULL COMMENT '购买数量',
  `total_price` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '小计金额',
  `freight_share` DECIMAL(10,2) UNSIGNED DEFAULT 0.00 COMMENT '分摊运费',
  `discount_share` DECIMAL(10,2) UNSIGNED DEFAULT 0.00 COMMENT '分摊优惠',
  `warehouse_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '发货仓库ID',
  `is_commented` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否已评价（0否/1是）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';

-- =============================================
-- 7. 预设数据模块
-- =============================================

-- 7.1 插入系统配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_desc`, `group_name`, `is_editable`) VALUES
('site_name', '童趣友谊', '网站名称', 'common', 1),
('site_logo', '', '网站Logo URL', 'common', 1),
('site_icp', '', '网站ICP备案号', 'common', 1),
('order_auto_cancel_minutes', '30', '订单自动取消时间（分钟）', 'order', 1),
('order_auto_confirm_days', '7', '订单自动确认收货时间（天）', 'order', 1),
('freight_free_amount', '99.00', '包邮门槛金额', 'payment', 1),
('freight_default_amount', '8.00', '默认运费金额', 'payment', 1),
('points_exchange_rate', '100', '积分兑换比例（100积分=1元）', 'user', 1);

-- 7.2 插入区域简化数据（GB/T 2260-2020 部分省市区）
INSERT INTO `sys_region` (`id`, `parent_id`, `region_name`, `region_level`, `sort_order`) VALUES
-- 省级
(110000, 0, '北京市', 1, 1),
(310000, 0, '上海市', 1, 2),
(440000, 0, '广东省', 1, 3),
-- 市级
(110100, 110000, '北京市', 2, 1),
(310100, 310000, '上海市', 2, 1),
(440100, 440000, '广州市', 2, 1),
(440300, 440000, '深圳市', 2, 2),
-- 区县级
(110101, 110100, '东城区', 3, 1),
(110102, 110100, '西城区', 3, 2),
(310101, 310100, '黄浦区', 3, 1),
(310104, 310100, '徐汇区', 3, 2),
(440103, 440100, '荔湾区', 3, 1),
(440104, 440100, '越秀区', 3, 2),
(440303, 440300, '罗湖区', 3, 1),
(440304, 440300, '福田区', 3, 2);

-- 7.3 插入仓库简化数据
INSERT INTO `warehouse` (`warehouse_name`, `warehouse_code`, `province_id`, `city_id`, `district_id`, `detail_address`, `contact_name`, `contact_mobile`, `is_default`, `status`) VALUES
('北京中心仓', 'BJ-ZX-001', 110000, 110100, 110101, '北京市东城区xxx物流园A栋', '张师傅', '13800138001', 1, 1),
('广东中心仓', 'GD-ZX-001', 440000, 440100, 440103, '广州市荔湾区xxx物流园B栋', '李师傅', '13800138002', 0, 1);

-- 7.4 插入商品分类简化数据
INSERT INTO `product_category` (`parent_id`, `category_name`, `category_icon`, `sort_order`, `is_show`) VALUES
-- 一级分类
(0, '益智玩具', '', 1, 1),
(0, '婴童用品', '', 2, 1),
(0, '童装童鞋', '', 3, 1),
-- 二级分类
((SELECT id FROM product_category WHERE category_name='益智玩具' AND parent_id=0), '积木拼插', '', 1, 1),
((SELECT id FROM product_category WHERE category_name='益智玩具' AND parent_id=0), '早教机', '', 2, 1),
((SELECT id FROM product_category WHERE category_name='婴童用品' AND parent_id=0), '婴儿推车', '', 1, 1),
((SELECT id FROM product_category WHERE category_name='婴童用品' AND parent_id=0), '纸尿裤', '', 2, 1),
-- 三级分类
((SELECT id FROM product_category WHERE category_name='积木拼插' AND parent_id=(SELECT id FROM product_category WHERE category_name='益智玩具' AND parent_id=0)), '大颗粒积木', '', 1, 1),
((SELECT id FROM product_category WHERE category_name='积木拼插' AND parent_id=(SELECT id FROM product_category WHERE category_name='益智玩具' AND parent_id=0)), '小颗粒积木', '', 2, 1);

-- 7.5 插入商品品牌简化数据
INSERT INTO `product_brand` (`brand_name`, `brand_logo`, `brand_desc`, `sort_order`, `is_show`) VALUES
('乐高', '', '丹麦知名积木品牌', 1, 1),
('费雪', '', '美国知名婴童玩具品牌', 2, 1),
('好奇', '', '美国金佰利旗下纸尿裤品牌', 3, 1);

-- 7.6 插入商品SPU简化数据
INSERT INTO `product_spu` (`spu_no`, `category_id`, `brand_id`, `product_name`, `product_subtitle`, `main_image`, `price_range`, `is_new`, `is_hot`, `is_recommend`) VALUES
('SPU202X0001', (SELECT id FROM product_category WHERE category_name='大颗粒积木' AND parent_id=(SELECT id FROM product_category WHERE category_name='积木拼插' AND parent_id=(SELECT id FROM product_category WHERE category_name='益智玩具' AND parent_id=0))), (SELECT id FROM product_brand WHERE brand_name='乐高'), '乐高得宝10913 我的第一套汽车大颗粒', '适合1.5-5岁宝宝，培养动手能力', '', '199.00-299.00', 1, 1, 1),
('SPU202X0002', (SELECT id FROM product_category WHERE category_name='早教机' AND parent_id=(SELECT id FROM product_category WHERE category_name='益智玩具' AND parent_id=0)), (SELECT id FROM product_brand WHERE brand_name='费雪'), '费雪智玩学习桌DWN37', '双语学习，多功能游戏桌', '', '259.00', 1, 0, 1);

-- 7.7 插入商品SKU简化数据
INSERT INTO `product_sku` (`spu_id`, `sku_no`, `sku_name`, `original_price`, `sale_price`, `cost_price`, `weight`, `is_on_sale`) VALUES
((SELECT id FROM product_spu WHERE spu_no='SPU202X0001'), 'SKU202X0001001', '基础版（30颗粒）', 299.00, 199.00, 120.00, 1.5, 1),
((SELECT id FROM product_spu WHERE spu_no='SPU202X0001'), 'SKU202X0001002', '豪华版（60颗粒+收纳箱）', 399.00, 299.00, 180.00, 2.5, 1),
((SELECT id FROM product_spu WHERE spu_no='SPU202X0002'), 'SKU202X0002001', '标配', 359.00, 259.00, 160.00, 2.0, 1);
```