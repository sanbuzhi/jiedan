===FILE:back/tongquyouyi.sql===
```sql
-- =============================================
-- 数据库：童趣友谊母婴用品商城
-- 版本：MySQL 8.0.35+
-- 引擎：InnoDB
-- 字符集：utf8mb4
-- 作者：童趣友谊开发团队
-- 创建时间：202X-XX-XX
-- 更新记录：
-- =============================================

-- ----------------------------
-- 1. 数据库初始化（可选，根据部署环境调整）
-- ----------------------------
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS `tongquyouyi`;
CREATE DATABASE `tongquyouyi` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `tongquyouyi`;

-- ----------------------------
-- 2. 公共配置表
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT NOT NULL COMMENT '配置值',
  `config_desc` VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
  `group_name` VARCHAR(50) DEFAULT 'DEFAULT' COMMENT '配置分组',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`) USING BTREE,
  KEY `idx_group_name` (`group_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公共配置表';

-- ----------------------------
-- 3. 会员管理表
-- ----------------------------
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  `member_no` VARCHAR(32) NOT NULL COMMENT '会员编号（唯一）',
  `openid` VARCHAR(64) DEFAULT NULL COMMENT '微信OpenID',
  `unionid` VARCHAR(64) DEFAULT NULL COMMENT '微信UnionID',
  `phone` VARCHAR(11) DEFAULT NULL COMMENT '手机号',
  `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
  `gender` TINYINT UNSIGNED DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `birthday` DATE DEFAULT NULL COMMENT '宝宝/家长生日',
  `member_level` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '会员等级：1-普通会员，2-银卡，3-金卡，4-钻石',
  `growth_value` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '成长值',
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额（元）',
  `total_points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计积分',
  `available_points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '可用积分',
  `register_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_order_time` DATETIME DEFAULT NULL COMMENT '最后下单时间',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_no` (`member_no`) USING BTREE,
  UNIQUE KEY `uk_phone` (`phone`) USING BTREE,
  UNIQUE KEY `uk_openid` (`openid`) USING BTREE,
  KEY `idx_unionid` (`unionid`) USING BTREE,
  KEY `idx_member_level` (`member_level`) USING BTREE,
  KEY `idx_register_time` (`register_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员信息表';

-- ----------------------------
-- 4. 收货地址表（关联会员）
-- ----------------------------
DROP TABLE IF EXISTS `member_address`;
CREATE TABLE `member_address` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `consignee` VARCHAR(100) NOT NULL COMMENT '收货人',
  `phone` VARCHAR(11) NOT NULL COMMENT '收货电话',
  `province_code` VARCHAR(10) NOT NULL COMMENT '省份编码',
  `province_name` VARCHAR(50) NOT NULL COMMENT '省份名称',
  `city_code` VARCHAR(10) NOT NULL COMMENT '城市编码',
  `city_name` VARCHAR(50) NOT NULL COMMENT '城市名称',
  `district_code` VARCHAR(10) NOT NULL COMMENT '区县编码',
  `district_name` VARCHAR(50) NOT NULL COMMENT '区县名称',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `zip_code` VARCHAR(10) DEFAULT NULL COMMENT '邮编',
  `is_default` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否默认：0-否，1-是',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员收货地址表';

-- ----------------------------
-- 5. 商品分类表
-- ----------------------------
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类ID，0为一级',
  `category_name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `category_icon` VARCHAR(512) DEFAULT NULL COMMENT '分类图标URL',
  `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序权重（越大越靠前）',
  `level` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '分类层级：1-一级，2-二级',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`) USING BTREE,
  KEY `idx_sort_order` (`sort_order`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- ----------------------------
-- 6. 商品信息表
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `product_no` VARCHAR(32) NOT NULL COMMENT '商品编号（唯一）',
  `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `product_subtitle` VARCHAR(512) DEFAULT NULL COMMENT '商品副标题',
  `cover_image` VARCHAR(512) NOT NULL COMMENT '商品封面图URL',
  `carousel_images` TEXT NOT NULL COMMENT '轮播图URL数组（JSON格式）',
  `detail_images` TEXT DEFAULT NULL COMMENT '详情图URL数组（JSON格式）',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '商品二级分类ID',
  `brand` VARCHAR(100) DEFAULT NULL COMMENT '商品品牌',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品售价（元）',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '商品原价（元）',
  `cost_price` DECIMAL(10,2) DEFAULT NULL COMMENT '商品成本价（元）',
  `is_hot` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否热销：0-否，1-是',
  `is_new` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否新品：0-否，1-是',
  `is_recommend` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
  `sale_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计销量',
  `view_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计浏览量',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_no` (`product_no`) USING BTREE,
  KEY `idx_category_id` (`category_id`) USING BTREE,
  KEY `idx_brand` (`brand`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_hot_new_recommend` (`is_hot`, `is_new`, `is_recommend`) USING BTREE,
  KEY `idx_sale_count` (`sale_count`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品信息表';

-- ----------------------------
-- 7. 商品库存表（关联商品）
-- ----------------------------
DROP TABLE IF EXISTS `product_inventory`;
CREATE TABLE `product_inventory` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `spec_name` VARCHAR(100) DEFAULT '默认规格' COMMENT '规格名称（例如：尺码M、颜色红色）',
  `spec_value` VARCHAR(100) DEFAULT '默认' COMMENT '规格值',
  `spec_sku` VARCHAR(32) NOT NULL COMMENT 'SKU编码（唯一）',
  `total_stock` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '总库存',
  `available_stock` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '可用库存',
  `locked_stock` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '锁定库存（订单未支付时锁定）',
  `warning_stock` INT UNSIGNED NOT NULL DEFAULT 10 COMMENT '预警库存',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spec_sku` (`spec_sku`) USING BTREE,
  KEY `idx_product_id` (`product_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品库存SKU表';

-- ----------------------------
-- 8. 订单表
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号（唯一）',
  `out_trade_no` VARCHAR(64) DEFAULT NULL COMMENT '第三方支付交易号',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `consignee` VARCHAR(100) NOT NULL COMMENT '收货人',
  `phone` VARCHAR(11) NOT NULL COMMENT '收货电话',
  `full_address` VARCHAR(512) NOT NULL COMMENT '完整收货地址',
  `product_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品总金额（元）',
  `shipping_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费（元）',
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠总金额（元）',
  `points_deduct` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '积分抵扣金额（元）',
  `actual_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额（元）',
  `used_points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用积分',
  `earned_points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '获得积分',
  `order_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付待发货，2-已发货待收货，3-已完成，4-已取消，5-已退款',
  `payment_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付，2-已退款',
  `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `shipping_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `remark` VARCHAR(512) DEFAULT NULL COMMENT '订单备注',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`) USING BTREE,
  UNIQUE KEY `uk_out_trade_no` (`out_trade_no`) USING BTREE,
  KEY `idx_member_id` (`member_id`) USING BTREE,
  KEY `idx_order_status` (`order_status`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

-- ----------------------------
-- 9. 订单商品明细表
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `product_no` VARCHAR(32) NOT NULL COMMENT '商品编号',
  `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `cover_image` VARCHAR(512) NOT NULL COMMENT '商品封面图',
  `spec_sku` VARCHAR(32) NOT NULL COMMENT 'SKU编码',
  `spec_name` VARCHAR(100) NOT NULL COMMENT '规格名称',
  `spec_value` VARCHAR(100) NOT NULL COMMENT '规格值',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品单价（元，下单时的价格）',
  `quantity` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '购买数量',
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品小计（元）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`) USING BTREE,
  KEY `idx_order_no` (`order_no`) USING BTREE,
  KEY `idx_product_id` (`product_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品明细表';

-- ----------------------------
-- 10. 预设数据
-- ----------------------------

-- 系统公共配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_desc`, `group_name`) VALUES
('mall_name', '童趣友谊母婴用品商城', '商城名称', 'BASIC'),
('mall_logo', 'https://example.com/logo.png', '商城Logo URL', 'BASIC'),
('shipping_free_amount', '99.00', '包邮门槛（元）', 'SHIPPING'),
('shipping_fee_default', '8.00', '默认运费（元）', 'SHIPPING'),
('points_ratio', '100', '积分抵扣比例（100积分=1元）', 'POINTS'),
('points_earn_ratio', '1', '消费获得积分比例（1元=1积分）', 'POINTS'),
('order_auto_cancel_minutes', '30', '订单自动取消时间（分钟）', 'ORDER'),
('order_auto_confirm_days', '7', '订单自动确认收货时间（天）', 'ORDER');

-- 商品分类（一级分类：奶粉辅食、童装童鞋、玩具益智、车床座椅、尿裤湿巾、母婴用品）
INSERT INTO `product_category` (`parent_id`, `category_name`, `category_icon`, `sort_order`, `level`, `status`) VALUES
(0, '奶粉辅食', 'https://example.com/icon1.png', 100, 1, 1),
(0, '童装童鞋', 'https://example.com/icon2.png', 90, 1, 1),
(0, '玩具益智', 'https://example.com/icon3.png', 80, 1, 1),
(0, '车床座椅', 'https://example.com/icon4.png', 70, 1, 1),
(0, '尿裤湿巾', 'https://example.com/icon5.png', 60, 1, 1),
(0, '母婴用品', 'https://example.com/icon6.png', 50, 1, 1);

-- 商品分类（二级分类示例）
INSERT INTO `product_category` (`parent_id`, `category_name`, `sort_order`, `level`, `status`) VALUES
(1, '婴儿配方奶粉', 100, 2, 1),
(1, '辅食米粉', 90, 2, 1),
(5, '纸尿裤', 100, 2, 1),
(5, '拉拉裤', 90, 2, 1),
(6, '奶瓶奶嘴', 100, 2, 1);

-- 测试会员（仅开发测试用）
INSERT INTO `member` (`member_no`, `phone`, `nickname`, `avatar`, `member_level`, `growth_value`, `balance`, `total_points`, `available_points`, `register_time`) VALUES
('M202X000001', '13800138000', '测试妈妈', 'https://example.com/avatar1.png', 2, 500, 100.00, 200, 200, '202X-01-01 00:00:00'),
('M202X000002', '13900139000', '新手爸爸', 'https://example.com/avatar2.png', 1, 100, 0.00, 50, 50, '202X-02-01 00:00:00');

-- 测试收货地址
INSERT INTO `member_address` (`member_id`, `consignee`, `phone`, `province_code`, `province_name`, `city_code`, `city_name`, `district_code`, `district_name`, `detail_address`, `is_default`) VALUES
(1, '测试妈妈', '13800138000', '110000', '北京市', '110100', '北京市', '110105', '朝阳区', '建国路88号童趣大厦10层', 1),
(1, '测试妈妈', '13800138000', '110000', '北京市', '110100', '北京市', '110108', '海淀区', '中关村大街1号', 0);

-- 测试商品
INSERT INTO `product` (`product_no`, `product_name`, `product_subtitle`, `cover_image`, `carousel_images`, `category_id`, `brand`, `price`, `original_price`, `is_hot`, `is_new`, `is_recommend`, `sale_count`, `status`) VALUES
('P202X000001', '荷兰牛栏婴儿配方奶粉1段800g', '适合0-6个月宝宝，原装进口', 'https://example.com/product1_cover.png', '["https://example.com/product1_1.png","https://example.com/product1_2.png"]', 7, 'Nutrilon/牛栏', 168.00, 198.00, 1, 0, 1, 1234, 1),
('P202X000002', '帮宝适绿帮纸尿裤M码54片', '超薄干爽，宝宝安睡一整晚', 'https://example.com/product2_cover.png', '["https://example.com/product2_1.png","https://example.com/product2_2.png"]', 9, 'Pampers/帮宝适', 59.90, 79.90, 1, 1, 0, 5678, 1),
('P202X000003', '贝亲宽口径玻璃奶瓶240ml', '防胀气设计，耐高温', 'https://example.com/product3_cover.png', '["https://example.com/product3_1.png","https://example.com/product3_2.png"]', 11, 'Pigeon/贝亲', 69.00, 99.00, 0, 0, 1, 890, 1);

-- 测试商品库存
INSERT INTO `product_inventory` (`product_id`, `spec_name`, `spec_value`, `spec_sku`, `total_stock`, `available_stock`, `warning_stock`, `status`) VALUES
(1, '规格', '1段800g', 'SKU202X000001001', 200, 200, 20, 1),
(2, '尺码', 'M码54片', 'SKU202X000002001', 500, 500, 50, 1),
(2, '尺码', 'L码48片', 'SKU202X000002002', 400, 400, 40, 1),
(3, '颜色', '粉色', 'SKU202X000003001', 150, 150, 15, 1),
(3, '颜色', '蓝色', 'SKU202X000003002', 120, 120, 15, 1);

SET FOREIGN_KEY_CHECKS = 1;
```