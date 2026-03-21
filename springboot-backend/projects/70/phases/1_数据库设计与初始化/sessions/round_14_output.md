===FILE:back/tongquyouyi.sql===
```sql
-- MySQL 8.0.35初始化脚本
-- 数据库名：tongquyouyi
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_0900_ai_ci
-- 引擎：InnoDB

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 创建数据库
-- ----------------------------
DROP DATABASE IF EXISTS `tongquyouyi`;
CREATE DATABASE `tongquyouyi` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `tongquyouyi`;

-- ----------------------------
-- 2. 公共字典表（商品分类、订单状态等通用配置）
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_type` VARCHAR(50) NOT NULL COMMENT '字典类型（如goods_category, order_status）',
  `dict_code` VARCHAR(50) NOT NULL COMMENT '字典编码',
  `dict_name` VARCHAR(100) NOT NULL COMMENT '字典名称',
  `dict_sort` INT NOT NULL DEFAULT 0 COMMENT '排序（升序）',
  `dict_status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0禁用 1启用）',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type_code` (`dict_type`, `dict_code`) COMMENT '类型+编码唯一索引',
  KEY `idx_dict_type` (`dict_type`) COMMENT '字典类型索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公共字典表';

-- ----------------------------
-- 3. 省市区地区表（简化版三级联动）
-- ----------------------------
DROP TABLE IF EXISTS `sys_region`;
CREATE TABLE `sys_region` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `region_code` VARCHAR(20) NOT NULL COMMENT '地区编码',
  `region_name` VARCHAR(50) NOT NULL COMMENT '地区名称',
  `parent_code` VARCHAR(20) NOT NULL DEFAULT '0' COMMENT '上级地区编码（0为省级）',
  `region_level` TINYINT NOT NULL COMMENT '地区级别（1省 2市 3区/县）',
  `region_sort` INT NOT NULL DEFAULT 0 COMMENT '排序（升序）',
  `region_status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0禁用 1启用）',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_region_code` (`region_code`) COMMENT '地区编码唯一索引',
  KEY `idx_parent_code` (`parent_code`) COMMENT '上级编码索引',
  KEY `idx_region_level` (`region_level`) COMMENT '地区级别索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='省市区地区表';

-- ----------------------------
-- 4. 商品主表
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `goods_sn` VARCHAR(50) NOT NULL COMMENT '商品货号',
  `goods_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `goods_category_code` VARCHAR(50) NOT NULL COMMENT '商品分类编码（关联sys_dict）',
  `goods_brand` VARCHAR(100) DEFAULT NULL COMMENT '商品品牌',
  `goods_desc` TEXT DEFAULT NULL COMMENT '商品简介',
  `market_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '市场价',
  `sale_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '销售价',
  `goods_cover` VARCHAR(255) DEFAULT NULL COMMENT '商品封面图URL',
  `goods_status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0下架 1上架 2预售）',
  `is_recommend` TINYINT NOT NULL DEFAULT 0 COMMENT '是否推荐（0否 1是）',
  `is_hot` TINYINT NOT NULL DEFAULT 0 COMMENT '是否热销（0否 1是）',
  `sales_volume` INT NOT NULL DEFAULT 0 COMMENT '销量',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_goods_sn` (`goods_sn`) COMMENT '商品货号唯一索引',
  KEY `idx_goods_category_code` (`goods_category_code`) COMMENT '分类编码索引',
  KEY `idx_goods_status` (`goods_status`) COMMENT '状态索引',
  KEY `idx_sales_volume` (`sales_volume`) COMMENT '销量索引',
  KEY `idx_is_recommend` (`is_recommend`) COMMENT '推荐索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品主表';

-- ----------------------------
-- 5. 商品图片表
-- ----------------------------
DROP TABLE IF EXISTS `goods_image`;
CREATE TABLE `goods_image` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `goods_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID（关联goods）',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
  `image_sort` INT NOT NULL DEFAULT 0 COMMENT '排序（升序）',
  `is_cover` TINYINT NOT NULL DEFAULT 0 COMMENT '是否封面（0否 1是）',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_goods_id` (`goods_id`) COMMENT '商品ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品图片表';

-- ----------------------------
-- 6. 商品库存表
-- ----------------------------
DROP TABLE IF EXISTS `goods_stock`;
CREATE TABLE `goods_stock` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `goods_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID（关联goods）',
  `goods_spec` VARCHAR(200) DEFAULT NULL COMMENT '商品规格（如红色+XL）',
  `stock_num` INT NOT NULL DEFAULT 0 COMMENT '当前库存',
  `lock_stock_num` INT NOT NULL DEFAULT 0 COMMENT '锁定库存（下单未支付/未发货）',
  `alert_stock_num` INT NOT NULL DEFAULT 10 COMMENT '预警库存',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_goods_id` (`goods_id`) COMMENT '商品ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品库存表';

-- ----------------------------
-- 7. 库存变动流水表
-- ----------------------------
DROP TABLE IF EXISTS `stock_change_log`;
CREATE TABLE `stock_change_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `stock_id` BIGINT UNSIGNED NOT NULL COMMENT '库存ID（关联goods_stock）',
  `goods_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID（关联goods）',
  `change_type` VARCHAR(50) NOT NULL COMMENT '变动类型（如init初始化, purchase采购, lock锁定, unlock解锁, deduct扣减, return退货）',
  `change_num` INT NOT NULL COMMENT '变动数量（正数增加 负数减少）',
  `before_stock` INT NOT NULL COMMENT '变动前库存',
  `after_stock` INT NOT NULL COMMENT '变动后库存',
  `change_remark` VARCHAR(255) DEFAULT NULL COMMENT '变动备注',
  `related_order_sn` VARCHAR(50) DEFAULT NULL COMMENT '关联订单号',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_stock_id` (`stock_id`) COMMENT '库存ID索引',
  KEY `idx_goods_id` (`goods_id`) COMMENT '商品ID索引',
  KEY `idx_related_order_sn` (`related_order_sn`) COMMENT '关联订单号索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存变动流水表';

-- ----------------------------
-- 8. 会员主表
-- ----------------------------
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_no` VARCHAR(50) NOT NULL COMMENT '会员编号',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别（0未知 1男 2女）',
  `mobile` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `member_level_code` VARCHAR(50) NOT NULL DEFAULT 'ordinary' COMMENT '会员等级编码（关联sys_dict）',
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `points` INT NOT NULL DEFAULT 0 COMMENT '账户积分',
  `member_status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0禁用 1正常 2注销）',
  `last_login_time` TIMESTAMP NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_no` (`member_no`) COMMENT '会员编号唯一索引',
  UNIQUE KEY `uk_username` (`username`) COMMENT '用户名唯一索引',
  UNIQUE KEY `uk_mobile` (`mobile`) COMMENT '手机号唯一索引',
  KEY `idx_member_level_code` (`member_level_code`) COMMENT '会员等级编码索引',
  KEY `idx_member_status` (`member_status`) COMMENT '状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员主表';

-- ----------------------------
-- 9. 会员收货地址表
-- ----------------------------
DROP TABLE IF EXISTS `member_address`;
CREATE TABLE `member_address` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID（关联member）',
  `consignee_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `consignee_mobile` VARCHAR(20) NOT NULL COMMENT '收货人手机号',
  `province_code` VARCHAR(20) NOT NULL COMMENT '省级编码（关联sys_region）',
  `city_code` VARCHAR(20) NOT NULL COMMENT '市级编码（关联sys_region）',
  `district_code` VARCHAR(20) NOT NULL COMMENT '区/县级编码（关联sys_region）',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `full_address` VARCHAR(500) NOT NULL COMMENT '完整地址（省市区+详细）',
  `postal_code` VARCHAR(10) DEFAULT NULL COMMENT '邮政编码',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认（0否 1是）',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`) COMMENT '会员ID索引',
  KEY `idx_is_default` (`is_default`) COMMENT '默认地址索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员收货地址表';

-- ----------------------------
-- 10. 会员余额/积分变动流水表
-- ----------------------------
DROP TABLE IF EXISTS `member_account_log`;
CREATE TABLE `member_account_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID（关联member）',
  `account_type` VARCHAR(20) NOT NULL COMMENT '账户类型（balance余额 points积分）',
  `change_type` VARCHAR(50) NOT NULL COMMENT '变动类型（如recharge充值, consume消费, refund退款, sign签到, exchange兑换）',
  `change_amount` DECIMAL(10,2) NOT NULL COMMENT '变动金额/数量（正数增加 负数减少）',
  `before_amount` DECIMAL(10,2) NOT NULL COMMENT '变动前金额/数量',
  `after_amount` DECIMAL(10,2) NOT NULL COMMENT '变动后金额/数量',
  `change_remark` VARCHAR(255) DEFAULT NULL COMMENT '变动备注',
  `related_order_sn` VARCHAR(50) DEFAULT NULL COMMENT '关联订单号',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`) COMMENT '会员ID索引',
  KEY `idx_account_type` (`account_type`) COMMENT '账户类型索引',
  KEY `idx_related_order_sn` (`related_order_sn`) COMMENT '关联订单号索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员余额/积分变动流水表';

-- ----------------------------
-- 11. 订单主表
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_sn` VARCHAR(50) NOT NULL COMMENT '订单号',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID（关联member）',
  `order_status_code` VARCHAR(50) NOT NULL COMMENT '订单状态编码（关联sys_dict）',
  `pay_status_code` VARCHAR(50) NOT NULL DEFAULT 'unpaid' COMMENT '支付状态编码（关联sys_dict）',
  `delivery_status_code` VARCHAR(50) NOT NULL DEFAULT 'undelivered' COMMENT '物流状态编码（关联sys_dict）',
  `goods_total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品总金额',
  `freight_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费金额',
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `order_total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  `pay_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实际支付金额',
  `balance_pay_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '余额支付金额',
  `points_pay_amount` INT NOT NULL DEFAULT 0 COMMENT '积分支付数量',
  `consignee_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `consignee_mobile` VARCHAR(20) NOT NULL COMMENT '收货人手机号',
  `province_code` VARCHAR(20) NOT NULL COMMENT '省级编码',
  `city_code` VARCHAR(20) NOT NULL COMMENT '市级编码',
  `district_code` VARCHAR(20) NOT NULL COMMENT '区/县级编码',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `full_address` VARCHAR(500) NOT NULL COMMENT '完整地址',
  `postal_code` VARCHAR(10) DEFAULT NULL COMMENT '邮政编码',
  `order_remark` VARCHAR(500) DEFAULT NULL COMMENT '订单备注',
  `pay_time` TIMESTAMP NULL DEFAULT NULL COMMENT '支付时间',
  `delivery_time` TIMESTAMP NULL DEFAULT NULL COMMENT '发货时间',
  `receive_time` TIMESTAMP NULL DEFAULT NULL COMMENT '收货时间',
  `cancel_time` TIMESTAMP NULL DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `auto_cancel_minutes` INT NOT NULL DEFAULT 30 COMMENT '自动取消分钟数',
  `auto_confirm_days` INT NOT NULL DEFAULT 7 COMMENT '自动确认收货天数',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_sn` (`order_sn`) COMMENT '订单号唯一索引',
  KEY `idx_member_id` (`member_id`) COMMENT '会员ID索引',
  KEY `idx_order_status_code` (`order_status_code`) COMMENT '订单状态编码索引',
  KEY `idx_pay_status_code` (`pay_status_code`) COMMENT '支付状态编码索引',
  KEY `idx_created_at` (`created_at`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单主表';

-- ----------------------------
-- 12. 订单商品明细表
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID（关联order）',
  `order_sn` VARCHAR(50) NOT NULL COMMENT '订单号（冗余，提高查询效率）',
  `goods_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID（关联goods）',
  `goods_sn` VARCHAR(50) NOT NULL COMMENT '商品货号（冗余）',
  `goods_name` VARCHAR(200) NOT NULL COMMENT '商品名称（冗余）',
  `goods_cover` VARCHAR(255) DEFAULT NULL COMMENT '商品封面图（冗余）',
  `goods_spec` VARCHAR(200) DEFAULT NULL COMMENT '商品规格',
  `goods_stock_id` BIGINT UNSIGNED NOT NULL COMMENT '商品库存ID（关联goods_stock）',
  `market_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品当时市场价（冗余）',
  `sale_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品当时销售价（冗余）',
  `purchase_num` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
  `goods_total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品小计金额',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`) COMMENT '订单ID索引',
  KEY `idx_order_sn` (`order_sn`) COMMENT '订单号索引',
  KEY `idx_goods_id` (`goods_id`) COMMENT '商品ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单商品明细表';

-- ----------------------------
-- 13. 订单支付记录表
-- ----------------------------
DROP TABLE IF EXISTS `order_pay_log`;
CREATE TABLE `order_pay_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID（关联order）',
  `order_sn` VARCHAR(50) NOT NULL COMMENT '订单号（冗余）',
  `pay_no` VARCHAR(100) NOT NULL COMMENT '第三方支付流水号',
  `pay_type_code` VARCHAR(50) NOT NULL COMMENT '支付方式编码（关联sys_dict）',
  `pay_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '支付金额',
  `pay_status_code` VARCHAR(50) NOT NULL COMMENT '支付状态编码（关联sys_dict）',
  `pay_time` TIMESTAMP NULL DEFAULT NULL COMMENT '支付时间',
  `pay_remark` VARCHAR(255) DEFAULT NULL COMMENT '支付备注',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_no` (`pay_no`) COMMENT '第三方支付流水号唯一索引',
  KEY `idx_order_id` (`order_id`) COMMENT '订单ID索引',
  KEY `idx_order_sn` (`order_sn`) COMMENT '订单号索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单支付记录表';

-- ----------------------------
-- 预设数据
-- ----------------------------

-- 预设字典数据
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_name`, `dict_sort`, `dict_status`) VALUES
-- 商品分类
('goods_category', 'clothing', '服装', 1, 1),
('goods_category', 'food', '食品', 2, 1),
('goods_category', 'digital', '数码', 3, 1),
('goods_category', 'home', '家居', 4, 1),
-- 订单状态
('order_status', 'unpaid', '待付款', 1, 1),
('order_status', 'undelivered', '待发货', 2, 1),
('order_status', 'shipped', '待收货', 3, 1),
('order_status', 'completed', '已完成', 4, 1),
('order_status', 'cancelled', '已取消', 5, 1),
('order_status', 'refunding', '退款中', 6, 1),
('order_status', 'refunded', '已退款', 7, 1),
-- 支付状态
('pay_status', 'unpaid', '未支付', 1, 1),
('pay_status', 'paid', '已支付', 2, 1),
('pay_status', 'refunded', '已退款', 3, 1),
-- 物流状态
('delivery_status', 'undelivered', '未发货', 1, 1),
('delivery_status', 'shipped', '已发货', 2, 1),
('delivery_status', 'in_transit', '运输中', 3, 1),
('delivery_status', 'arrived', '已到达', 4, 1),
('delivery_status', 'signed', '已签收', 5, 1),
-- 支付方式
('pay_type', 'wechat', '微信支付', 1, 1),
('pay_type', 'alipay', '支付宝', 2, 1),
('pay_type', 'balance', '余额支付', 3, 1),
('pay_type', 'points', '积分兑换', 4, 1),
-- 会员等级
('member_level', 'ordinary', '普通会员', 1, 1),
('member_level', 'silver', '银卡会员', 2, 1),
('member_level', 'gold', '金卡会员', 3, 1),
('member_level', 'platinum', '白金会员', 4, 1);

-- 预设省市区数据（简化版）
INSERT INTO `sys_region` (`region_code`, `region_name`, `parent_code`, `region_level`, `region_sort`, `region_status`) VALUES
-- 省级
('110000', '北京市', '0', 1, 1, 1),
('310000', '上海市', '0', 1, 2, 1),
('440000', '广东省', '0', 1, 3, 1),
-- 市级
('110100', '北京市', '110000', 2, 1, 1),
('310100', '上海市', '310000', 2, 1, 1),
('440100', '广州市', '440000', 2, 1, 1),
('440300', '深圳市', '440000', 2, 2, 1),
-- 区/县级
('110101', '东城区', '110100', 3, 1, 1),
('110102', '西城区', '110100', 3, 2, 1),
('310101', '黄浦区', '310100', 3, 1, 1),
('310104', '徐汇区', '310100', 3, 2, 1),
('440103', '荔湾区', '440100', 3, 1, 1),
('440104', '越秀区', '440100', 3, 2, 1),
('440303', '罗湖区', '440300', 3, 1, 1),
('440304', '福田区', '440300', 3, 2, 1);

SET FOREIGN_KEY_CHECKS = 1;
```