===FILE:back/tongquyouyi.sql===
```sql
-- =====================================================
-- 童趣友益儿童用品商城数据库初始化脚本
-- 数据库版本: MySQL 8.0.35+
-- 存储引擎: InnoDB
-- 字符集: utf8mb4 (支持emoji及所有Unicode字符)
-- 排序规则: utf8mb4_0900_ai_ci (MySQL 8.0默认，不区分大小写/重音)
-- =====================================================

-- 创建数据库（如果已存在则先删除）
DROP DATABASE IF EXISTS `tongquyouyi`;
CREATE DATABASE `tongquyouyi`
DEFAULT CHARACTER SET = utf8mb4
DEFAULT COLLATE = utf8mb4_0900_ai_ci;

-- 使用目标数据库
USE `tongquyouyi`;

-- =====================================================
-- 1. 公共系统配置表 (sys_config)
-- =====================================================
CREATE TABLE `sys_config` (
  `config_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置ID主键',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键（唯一）',
  `config_value` TEXT NOT NULL COMMENT '配置值',
  `config_type` TINYINT NOT NULL DEFAULT 1 COMMENT '配置类型：1-文本 2-数字 3-布尔 4-图片 5-富文本',
  `group_name` VARCHAR(50) NOT NULL DEFAULT '基础配置' COMMENT '配置分组',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_group_name` (`group_name`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公共系统配置表';

-- =====================================================
-- 2. 会员管理表 (member)
-- =====================================================
CREATE TABLE `member` (
  `member_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会员ID主键',
  `username` VARCHAR(50) NOT NULL COMMENT '登录用户名（唯一）',
  `password` VARCHAR(255) NOT NULL COMMENT '登录密码（BCrypt加密）',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '会员昵称',
  `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '会员头像URL',
  `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
  `phone` VARCHAR(11) NOT NULL COMMENT '手机号（唯一）',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `level` TINYINT NOT NULL DEFAULT 1 COMMENT '会员等级：1-普通 2-白银 3-黄金 4-钻石',
  `points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '积分余额',
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '会员状态：0-禁用 1-正常',
  `login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_level` (`level`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_points` (`points`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员管理表';

-- =====================================================
-- 3. 商品分类表 (product_category)
-- =====================================================
CREATE TABLE `product_category` (
  `category_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID主键',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类ID（0为一级分类）',
  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon_url` VARCHAR(500) DEFAULT NULL COMMENT '分类图标URL',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '分类状态：0-禁用 1-显示',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_parent_name` (`parent_id`, `category_name`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';

-- =====================================================
-- 4. 商品管理表 (product)
-- =====================================================
CREATE TABLE `product` (
  `product_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID主键',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '所属分类ID',
  `product_no` VARCHAR(50) NOT NULL COMMENT '商品编号（唯一）',
  `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `brand` VARCHAR(100) DEFAULT NULL COMMENT '商品品牌',
  `main_image_url` VARCHAR(500) NOT NULL COMMENT '商品主图URL',
  `sub_image_list` JSON DEFAULT NULL COMMENT '商品副图列表（JSON数组）',
  `description` LONGTEXT DEFAULT NULL COMMENT '商品详情（富文本）',
  `original_price` DECIMAL(10,2) NOT NULL COMMENT '商品原价',
  `discount_price` DECIMAL(10,2) NOT NULL COMMENT '商品折扣价（当前售价）',
  `warn_stock` INT UNSIGNED NOT NULL DEFAULT 10 COMMENT '预警库存值',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '商品状态：0-下架 1-上架 2-预售',
  `is_hot` TINYINT NOT NULL DEFAULT 0 COMMENT '是否热销：0-否 1-是',
  `is_new` TINYINT NOT NULL DEFAULT 0 COMMENT '是否新品：0-否 1-是',
  `sales_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计销量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`product_id`),
  UNIQUE KEY `uk_product_no` (`product_no`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_hot` (`is_hot`),
  KEY `idx_is_new` (`is_new`),
  KEY `idx_sales_count` (`sales_count`),
  KEY `idx_category_status_price` (`category_id`, `status`, `discount_price`, `sort_order`) COMMENT '商品列表组合查询索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品管理表';

-- =====================================================
-- 5. 库存管理表 (product_stock)
-- =====================================================
CREATE TABLE `product_stock` (
  `stock_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '库存ID主键',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `sku_attrs` JSON DEFAULT NULL COMMENT 'SKU属性（JSON键值对，如{"颜色":"红色","尺码":"120"}）',
  `sku_no` VARCHAR(50) NOT NULL COMMENT 'SKU编号（唯一）',
  `available_stock` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '可用库存',
  `frozen_stock` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '冻结库存（已下单未支付/未发货）',
  `total_stock` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '总库存',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`stock_id`),
  UNIQUE KEY `uk_sku_no` (`sku_no`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_available_stock` (`available_stock`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存管理表';

-- =====================================================
-- 6. 订单管理表 (order_info)
-- =====================================================
CREATE TABLE `order_info` (
  `order_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID主键',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号（唯一，格式：yyyyMMddHHmmss+6位随机数）',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '下单会员ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠总金额',
  `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实际支付金额',
  `freight_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费金额',
  `use_points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用积分数量',
  `get_points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '本次获得积分数量',
  `pay_type` TINYINT DEFAULT NULL COMMENT '支付方式：1-微信 2-支付宝 3-余额 4-积分+余额',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `consignee_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `consignee_phone` VARCHAR(11) NOT NULL COMMENT '收货人手机号',
  `consignee_province` VARCHAR(50) NOT NULL COMMENT '收货省份',
  `consignee_city` VARCHAR(50) NOT NULL COMMENT '收货城市',
  `consignee_district` VARCHAR(50) NOT NULL COMMENT '收货区县',
  `consignee_detail` VARCHAR(500) NOT NULL COMMENT '收货详细地址',
  `order_status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付 1-已支付待发货 2-已发货待收货 3-已完成待评价 4-已评价 5-已取消 6-退款中 7-已退款',
  `delivery_type` TINYINT DEFAULT NULL COMMENT '配送方式：1-快递 2-自提',
  `delivery_no` VARCHAR(100) DEFAULT NULL COMMENT '物流单号',
  `delivery_company` VARCHAR(50) DEFAULT NULL COMMENT '物流公司',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '买家备注',
  `seller_remark` VARCHAR(255) DEFAULT NULL COMMENT '卖家备注',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_pay_time` (`pay_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单管理表';

-- =====================================================
-- 7. 订单详情表 (order_item)
-- =====================================================
CREATE TABLE `order_item` (
  `item_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单详情ID主键',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '所属订单ID',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称（快照）',
  `main_image_url` VARCHAR(500) NOT NULL COMMENT '商品主图URL（快照）',
  `sku_attrs` JSON DEFAULT NULL COMMENT 'SKU属性（快照）',
  `sku_no` VARCHAR(50) NOT NULL COMMENT 'SKU编号（快照）',
  `original_price` DECIMAL(10,2) NOT NULL COMMENT '商品原价（快照）',
  `discount_price` DECIMAL(10,2) NOT NULL COMMENT '商品折扣价（快照）',
  `buy_count` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '购买数量',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '商品小计金额',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`item_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单详情表';

-- =====================================================
-- 8. 订单评价表 (order_comment)
-- =====================================================
CREATE TABLE `order_comment` (
  `comment_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评价ID主键',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '所属订单ID',
  `item_id` BIGINT UNSIGNED NOT NULL COMMENT '所属订单详情ID',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '评价会员ID',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `star_rating` TINYINT NOT NULL DEFAULT 5 COMMENT '星级评分：1-5星',
  `content` TEXT DEFAULT NULL COMMENT '评价内容',
  `image_list` JSON DEFAULT NULL COMMENT '评价图片列表（JSON数组）',
  `is_anonymous` TINYINT NOT NULL DEFAULT 0 COMMENT '是否匿名：0-否 1-是',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '评价状态：0-隐藏 1-显示',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`comment_id`),
  UNIQUE KEY `uk_item_id` (`item_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_star_rating` (`star_rating`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单评价表';

-- =====================================================
-- 预设数据初始化
-- =====================================================

-- 1. 预设系统配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `group_name`, `sort_order`, `remark`) VALUES
('site_name', '童趣友益儿童用品商城', 1, '基础配置', 1, '网站名称'),
('site_logo', '/images/logo.png', 4, '基础配置', 2, '网站LOGO'),
('site_description', '专业的0-12岁儿童用品销售平台，提供玩具、童装、童鞋、童车等优质商品', 1, '基础配置', 3, '网站描述'),
('customer_service_phone', '400-888-8888', 1, '客服配置', 1, '客服电话'),
('customer_service_wechat', 'tongquyouyi_kefu', 1, '客服配置', 2, '客服微信号'),
('business_hours', '周一至周日 9:00-21:00', 1, '客服配置', 3, '营业时间'),
('freight_free_threshold', '99.00', 2, '运费配置', 1, '包邮门槛（元）'),
('default_freight', '8.00', 2, '运费配置', 2, '默认运费（元）'),
('points_to_cash_ratio', '100', 2, '积分配置', 1, '积分兑换比例（100积分=1元）'),
('points_reward_ratio', '0.1', 2, '积分配置', 2, '购物获得积分比例（消费1元获得0.1积分）');

-- 2. 预设会员等级配置（预留逻辑使用，这里先通过sys_config补充，后续可单独建表）
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `group_name`, `sort_order`, `remark`) VALUES
('level_1_name', '普通会员', 1, '会员等级配置', 1, '1级会员名称'),
('level_1_discount', '1.00', 2, '会员等级配置', 2, '1级会员折扣'),
('level_1_upgrade_points', '0', 2, '会员等级配置', 3, '1级会员升级积分'),
('level_2_name', '白银会员', 1, '会员等级配置', 4, '2级会员名称'),
('level_2_discount', '0.98', 2, '会员等级配置', 5, '2级会员折扣'),
('level_2_upgrade_points', '1000', 2, '会员等级配置', 6, '2级会员升级积分'),
('level_3_name', '黄金会员', 1, '会员等级配置', 7, '3级会员名称'),
('level_3_discount', '0.95', 2, '会员等级配置', 8, '3级会员折扣'),
('level_3_upgrade_points', '5000', 2, '会员等级配置', 9, '3级会员升级积分'),
('level_4_name', '钻石会员', 1, '会员等级配置', 10, '4级会员名称'),
('level_4_discount', '0.90', 2, '会员等级配置', 11, '4级会员折扣'),
('level_4_upgrade_points', '20000', 2, '会员等级配置', 12, '4级会员升级积分');

-- 3. 预设测试会员（密码均为123456的BCrypt加密值）
INSERT INTO `member` (`username`, `password`, `nickname`, `avatar_url`, `gender`, `phone`, `email`, `level`, `points`, `balance`, `status`) VALUES
('test001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIa', '小明妈妈', '/images/avatar/default1.png', 2, '13800138001', 'test001@tongquyouyi.com', 1, 500, 100.00, 1),
('test002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIa', '小红爸爸', '/images/avatar/default2.png', 1, '13800138002', 'test002@tongquyouyi.com', 2, 1200, 200.00, 1);

-- 4. 预设商品分类
INSERT INTO `product_category` (`parent_id`, `category_name`, `icon_url`, `sort_order`, `status`) VALUES
(0, '玩具', '/images/category/toy.png', 1, 1),
(0, '童装', '/images/category/clothes.png', 2, 1),
(0, '童鞋', '/images/category/shoes.png', 3, 1),
(0, '童车', '/images/category/bike.png', 4, 1),
(0, '喂养用品', '/images/category/feed.png', 5, 1),
(1, '益智玩具', '/images/category/toy_puzzle.png', 1, 1),
(1, '毛绒玩具', '/images/category/toy_plush.png', 2, 1),
(2, '婴儿装', '/images/category/clothes_baby.png', 1, 1),
(2, '幼童装', '/images/category/clothes_kid.png', 2, 1);

-- 5. 预设商品
INSERT INTO `product` (`category_id`, `product_no`, `product_name`, `brand`, `main_image_url`, `sub_image_list`, `description`, `original_price`, `discount_price`, `warn_stock`, `sort_order`, `status`, `is_hot`, `is_new`, `sales_count`) VALUES
(6, 'TOY20240601001', '儿童木质拼图3-6岁100片', '木玩世家', '/images/product/toy_puzzle1.jpg', '["/images/product/toy_puzzle1_2.jpg","/images/product/toy_puzzle1_3.jpg"]', '<p>优质椴木制作，环保水性漆，圆角打磨不伤手，提升孩子专注力和动手能力</p>', 99.00, 69.00, 20, 1, 1, 1, 1, 1256),
(7, 'TOY20240601002', '可爱泰迪熊毛绒玩具50cm', '迪士尼', '/images/product/toy_plush1.jpg', '["/images/product/toy_plush1_2.jpg","/images/product/toy_plush1_3.jpg"]', '<p>正版迪士尼授权，柔软短毛绒，填充PP棉，不掉毛无异味，孩子的好伙伴</p>', 129.00, 89.00, 30, 2, 1, 1, 0, 892),
(8, 'CLO20240601001', '婴儿纯棉连体衣春秋款', '童泰', '/images/product/clothes_baby1.jpg', '["/images/product/clothes_baby1_2.jpg","/images/product/clothes_baby1_3.jpg"]', '<p>100%新疆长绒棉，A类婴儿标准，无骨缝合，透气舒适，适合春秋季节</p>', 79.00, 49.00, 50, 1, 1, 0, 1, 2341);

-- 6. 预设商品库存
INSERT INTO `product_stock` (`product_id`, `sku_attrs`, `sku_no`, `available_stock`, `frozen_stock`, `total_stock`) VALUES
(1, '{"片数":"100片","图案":"动物世界"}', 'TOY20240601001-001', 150, 5, 155),
(1, '{"片数":"100片","图案":"卡通公主"}', 'TOY20240601001-002', 120, 3, 123),
(2, '{"颜色":"棕色","尺寸":"50cm"}', 'TOY20240601002-001', 80, 2, 82),
(3, '{"颜色":"粉色","尺码":"66cm"}', 'CLO20240601001-001', 200, 10, 210),
(3, '{"颜色":"蓝色","尺码":"73cm"}', 'CLO20240601001-002', 180, 8, 188);
```